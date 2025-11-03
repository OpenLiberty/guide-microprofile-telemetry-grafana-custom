// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
// end::copyright[]
package io.openliberty.guides.inventory;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

// tag::postConstruct[]
import jakarta.annotation.PostConstruct;
// end::postConstruct[]
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

// tag::metricsImports[]
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
// end::metricsImports[]
// tag::spanImport[]
import io.opentelemetry.api.trace.Span;
// end::spanImport[]
// tag::spanAnnotations[]
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
// end::spanAnnotations[]

import io.openliberty.guides.inventory.client.SystemClient;
import io.openliberty.guides.inventory.model.InventoryList;
import io.openliberty.guides.inventory.model.SystemData;

@ApplicationScoped
public class InventoryManager {

    // tag::getLogger[]
    private static final Logger LOGGER =
        Logger.getLogger(InventoryManager.class.getName());
    // end::getLogger[]

    @Inject
    @ConfigProperty(name = "system.http.port")
    private int SYSTEM_PORT;

    // tag::meter[]
    @Inject
    private Meter meter;
    // end::meter[]

    private Map<String, SystemData> systems = new ConcurrentHashMap<>();

    // tag::metricsVars[]
    // tag::systemLoadSuccessCountVar[]
    private LongCounter systemLoadSuccessCounter;
    // end::systemLoadSuccessCountVar[]
    // tag::systemLoadErrorCountVar[]
    private LongCounter systemLoadErrorCounter;
    // end::systemLoadErrorCountVar[]
    // tag::systemLoadDurationVar[]
    private DoubleHistogram systemLoadDuration;
    // end::systemLoadDurationVar[]
    // end::metricsVars

    // tag::postConstructMethod[]
    @PostConstruct
    public void init() {
        // tag::gaugeBuilder[]
        // tag::inventorySize[]
        meter.gaugeBuilder("inventory.size")
        // end::inventorySize[]
            .setDescription("Number of systems in the inventory")
            .setUnit("1")
            // tag::buildWithCallback[]
            .buildWithCallback(g -> g.record((double) systems.size()));
            // end::buildWithCallback[]
        // end::gaugeBuilder[]

        // tag::counterBuilder1[]
        // tag::systemLoadSuccessCount[]
        systemLoadSuccessCounter = meter.counterBuilder("inventory.system.load.successes")
        // end::systemLoadSuccessCount[]
            .setDescription("Number of successful system load retrievals")
            .setUnit("1")
            .build();
        // end::counterBuilder1[]

        // tag::counterBuilder2[]
        // tag::systemLoadErrorCount[]
        systemLoadErrorCounter = meter.counterBuilder("inventory.system.load.errors")
        // end::systemLoadErrorCount[]
            .setDescription("Number of failed system load retrievals")
            .setUnit("1")
            .build();
        // end::counterBuilder2[]

        // tag::histogramBuilder[]
        // tag::systemLoadDuration[]
        systemLoadDuration = meter.histogramBuilder("inventory.system.load.duration")
        // end::systemLoadDuration[]
            .setDescription("Duration of system load retrievals")
            .setUnit("s")
            .build();
        // end::histogramBuilder[]
    }
    // end::postConstructMethod[]

    public ArrayList<String> getHosts() {
        return new ArrayList<>(systems.keySet());
    }

    // tag::listWithSpan[]
    @WithSpan
    // end::listWithSpan[]
    // tag::listMethod[]
    public InventoryList list() {
        return new InventoryList(new ArrayList<>(systems.values()));
    }
    // end::listMethod[]

    // tag::getSystemLoadWithSpan[]
    @WithSpan("Inventory Manager GetSystemLoad")
    // end::getSystemLoadWithSpan[]
    // tag::getSystemLoadMethod[]
    // tag::spanAttribute1[]
    public JsonObject getSystemLoad(@SpanAttribute("hostname") String host) {
    // end::spanAttribute1[]
        // tag::spanCurrent1[]
        Span currentSpan = Span.current();
        // end::spanCurrent1[]
        String uriString = "http://" + host + ":" + SYSTEM_PORT + "/system";
        // tag::start[]
        long startTime = System.nanoTime();
        // end::start[]
        try (SystemClient client = RestClientBuilder.newBuilder()
                .baseUri(URI.create(uriString))
                .build(SystemClient.class)) {

            // tag::getSystemLoadFromSystem[]
            JsonObject obj = client.getSystemLoad();
            // end::getSystemLoadFromSystem[]
            // tag::duration[]
            double duration = (System.nanoTime() - startTime) / 1_000_000_000.0;
            systemLoadDuration.record(duration);
            // end::duration[]
            // tag::log1[]
            LOGGER.log(Level.INFO,
                "Retrieved system load from {0}", host);
            // end::log1[]
            // tag::systemLoadSuccessCounterAdd[]
            systemLoadSuccessCounter.add(1);
            // end::systemLoadSuccessCounterAdd[]
            // tag::addEvent1[]
            currentSpan.addEvent("Retrieved system load");
            // end::addEvent1[]
            return obj;
        } catch (RuntimeException e) {
            // tag::log2[]
            LOGGER.log(Level.WARNING,
                "Runtime exception while invoking system service", e);
            // end::log2[]
        } catch (Exception e) {
            // tag::log3[]
            LOGGER.log(Level.WARNING,
                "Unexpected exception while processing system service request", e);
            // end::log3[]
        }
        // tag::systemLoadErrorCounterAdd[]
        systemLoadErrorCounter.add(1);
        // end::systemLoadErrorCounterAdd[]
        // tag::addEvent2[]
        currentSpan.addEvent("Cannot get system load");
        // end::addEvent2[]
        return null;
    }
    // end::getSystemLoadMethod[]

    // tag::setWithSpan[]
    @WithSpan("Inventory Manager Set")
    // end::setWithSpan[]
    // tag::setMethod[]
    // tag::spanAttribute2[]
    public void set(@SpanAttribute("hostname") String host,
    // end::spanAttribute2[]
                    JsonObject systemLoad) {
        // tag::spanCurrent2[]
        Span currentSpan = Span.current();
        // end::spanCurrent2[]
        SystemData system = systems.get(host);
        if (system != null) {
            // tag::setAttribute1[]
            currentSpan.setAttribute("operation", "update");
            // end::setAttribute1[]
            system.setSystemLoad(systemLoad);
        } else {
            // tag::setAttribute2[]
            currentSpan.setAttribute("operation", "add");
            // end::setAttribute2[]
            systems.put(host, new SystemData(host, systemLoad));
        }
    }
    // end::setMethod[]

    public int clear() {
        int systemsClearedCount = systems.size();
        systems.clear();
        return systemsClearedCount;
    }
}
