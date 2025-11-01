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

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;

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
    
    private LongCounter systemLoadSuccessCounter;
    private LongCounter systemLoadFailureCounter;
    private DoubleHistogram systemLoadDuration;

    private Map<String, SystemData> systems = new ConcurrentHashMap<>();

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
            
        // Initialize system load retrieval success counter
        systemLoadSuccessCounter = meter.counterBuilder("inventory.system.load.retrieval.success")
            .setDescription("Number of successful system load retrievals")
            .setUnit("1")
            .build();
            
        // Initialize system load retrieval failure counter
        systemLoadFailureCounter = meter.counterBuilder("inventory.system.load.retrieval.failure")
            .setDescription("Number of failed system load retrievals")
            .setUnit("1")
            .build();
            
        // Initialize system load retrieval duration histogram
        systemLoadDuration = meter.histogramBuilder("inventory.system.load.retrieval.duration")
            .setDescription("Duration of system load retrievals")
            .setUnit("s")
            .build();
    }

    public ArrayList<String> getHosts() {
        return new ArrayList<>(systems.keySet());
    }

    @WithSpan
    public InventoryList list() {
        return new InventoryList(new ArrayList<>(systems.values()));
    }

    @WithSpan("Inventory Manager GetSystemLoad")
    public JsonObject getSystemLoad(@SpanAttribute("hostname") String host) {
        String uriString = "http://" + host + ":" + SYSTEM_PORT + "/system";
        long startTime = System.nanoTime();
        
        try (SystemClient client = RestClientBuilder.newBuilder()
                .baseUri(URI.create(uriString))
                .build(SystemClient.class)) {

            JsonObject obj = client.getSystemLoad();
            
            // Record success metric
            systemLoadSuccessCounter.add(1);
            
            // Record duration metric
            double duration = (System.nanoTime() - startTime) / 1_000_000_000.0;
            systemLoadDuration.record(duration);

            // tag::log1[]
            LOGGER.log(Level.INFO,
                "Retrieved system load from {0}", host);
            // end::log1[]
            Span.current().addEvent("Retrieved system load");
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

        // Record failure metric for any exception path
        systemLoadFailureCounter.add(1);
        Span.current().addEvent("Cannot get system load");
        return null;
    }

    @WithSpan("Inventory Manager Set")
    public void set(@SpanAttribute("hostname") String host,
                    JsonObject systemLoad) {
        SystemData system = systems.get(host);
        if (system != null) {
            Span.current().setAttribute("operation", "update");
            Span.current().addEvent("Updating existing system data");
            system.setSystemLoad(systemLoad);
        } else {
            Span.current().setAttribute("operation", "add");
            Span.current().addEvent("Adding new system data");
            systems.put(host, new SystemData(host, systemLoad));
        }
    }

    public int clear() {
        int systemsClearedCount = systems.size();
        systems.clear();
        return systemsClearedCount;
    }
}
