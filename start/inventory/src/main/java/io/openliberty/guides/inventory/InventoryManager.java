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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

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

    private Map<String, SystemData> systems = new ConcurrentHashMap<>();

    public ArrayList<String> getHosts() {
        return new ArrayList<>(systems.keySet());
    }

    public InventoryList list() {
        return new InventoryList(new ArrayList<>(systems.values()));
    }

    public JsonObject getSystemLoad(String host) {
        String uriString = "http://" + host + ":" + SYSTEM_PORT + "/system";
        try (SystemClient client = RestClientBuilder.newBuilder()
                .baseUri(URI.create(uriString))
                .build(SystemClient.class)) {

            JsonObject obj = client.getSystemLoad();
            // tag::log1[]
            LOGGER.log(Level.INFO,
                "Retrieved system load from {0}", host);
            // end::log1[]
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
        return null;
    }

    public void set(String host, JsonObject systemLoad) {
        SystemData system = systems.get(host);
        if (system != null) {
            system.setSystemLoad(systemLoad);
        } else {
            systems.put(host, new SystemData(host, systemLoad));
        }
    }

    public int clear() {
        int systemsClearedCount = systems.size();
        systems.clear();
        return systemsClearedCount;
    }
}
