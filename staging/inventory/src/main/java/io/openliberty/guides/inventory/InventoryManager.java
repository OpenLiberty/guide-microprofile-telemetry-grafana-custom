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

import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;

import io.openliberty.guides.inventory.client.SystemClient;
import io.openliberty.guides.inventory.model.InventoryList;
import io.openliberty.guides.inventory.model.SystemData;

@ApplicationScoped
public class InventoryManager {

    @Inject
    @ConfigProperty(name = "system.http.port")
    private int SYSTEM_PORT;

    private Map<String, SystemData> systems = new ConcurrentHashMap<>();

    public boolean contains(String host) {
        return systems.containsKey(host);
    }

    public Properties getProperties(String hostname) {
        try (SystemClient client = new SystemClient()) {
            client.init(hostname, SYSTEM_PORT);
            return client.getProperties();
        }
    }

    public String getHealth(String hostname) {
        try (SystemClient client = new SystemClient()) {
            client.init(hostname, SYSTEM_PORT);
            return client.getHealth();
        }
    }

    // tag::listWithSpan[]
    @WithSpan
    // end::listWithSpan[]
    // tag::listMethod[]
    public InventoryList list() {
        return new InventoryList(new ArrayList<>(systems.values()));
    }
    // end::listMethod[]

    // tag::addWithSpan[]
    @WithSpan("Inventory Manager Add")
    // end::addWithSpan[]
    // tag::addMethod[]
    // tag::spanAttribute1[]
    public void add(@SpanAttribute("hostname") String host,
    // end::spanAttribute1[]
                    Properties systemProps,
                    String health) {
        Properties props = new Properties();
        props.setProperty("os.name", systemProps.getProperty("os.name"));
        props.setProperty("user.name", systemProps.getProperty("user.name"));

        systems.put(host, new SystemData(host, props, health));
    }
    // end::addMethod[]

    // tag::updateWithSpan[]
    @WithSpan("Inventory Manager Update")
    // end::updateWithSpan[]
    // tag::updateMethod[]
    // tag::spanAttribute2[]
    public void update(@SpanAttribute("hostname") String host, String health) {
    // end::spanAttribute2[]
        SystemData system = systems.get(host);
        system.setHealth(health);
    }
    // end::updateMethod[]

    public int refreshAllSystemsHealth() {
        int updated = 0;
        for (SystemData system : systems.values()) {
            String hostname = system.getHostname();
            String newHealth = getHealth(hostname);
            if (!newHealth.equals(system.getHealth())) {
                system.setHealth(newHealth);
                updated++;
            }
        }
        return updated;
    }

    int clear() {
        int propertiesClearedCount = systems.size();
        systems.clear();
        return propertiesClearedCount;
    }
}
