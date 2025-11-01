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

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;

import io.opentelemetry.api.trace.Tracer;

import java.util.List;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.Context;

@Singleton
@Startup
public class SystemLoadRefreshScheduler {

    @Inject
    private InventoryManager inventoryManager;

    @Inject
    private Tracer tracer;

    @Schedule(hour = "*", minute = "*", second = "*/15", persistent = false)
    public void refreshSystemLoads() {
        Span refreshSpan = tracer.spanBuilder("RefreshingSystemLoads").startSpan();
        try (Scope scope = refreshSpan.makeCurrent()) {
            List<String> hosts = inventoryManager.getHosts();
            refreshSpan.setAttribute("systems.total", hosts.size());

            if (hosts.isEmpty()) {
                refreshSpan.addEvent("No systems found to refresh");
                refreshSpan.setAttribute("systems.refreshed", 0);
            } else {
                int refreshed = 0;
                for (String host : hosts) {
                    JsonObject systemLoad = inventoryManager.getSystemLoad(host);
                    if (systemLoad != null) {
                        inventoryManager.set(host, systemLoad);
                        refreshed++;
                    }
                }
                refreshSpan.setAttribute("systems.refreshed", refreshed);
                refreshSpan.addEvent("Refreshed system load for " + refreshed + " hosts");
            }
        } finally {
            refreshSpan.end();
        }
    }
}
