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

import java.util.List;

import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

@Singleton
@Startup
public class SystemLoadRefreshScheduler {

    @Inject
    private InventoryManager inventoryManager;

    // tag::tracer[]
    @Inject
    private Tracer tracer;
    // end::tracer[]

    @Schedule(hour = "*", minute = "*", second = "*/15", persistent = false)
    // tag::refreshSystemLoads[]
    public void refreshSystemLoads() {
        // tag::span[]
        Span span = tracer.spanBuilder("RefreshingSystemLoads").startSpan();
        // end::span[]
        // tag::try[]
        // tag::scope[]
        try (Scope scope = span.makeCurrent()) {
        // end::scope[]
            List<String> hosts = inventoryManager.getHosts();
            // tag::setAttribute1[]
            span.setAttribute("systems.total", hosts.size());
            // end::setAttribute1[]

            int refreshed = 0;
            for (String host : hosts) {
                JsonObject load = inventoryManager.getSystemLoad(host);
                if (load != null) {
                    inventoryManager.set(host, load);
                    refreshed++;
                }
            }

            // tag::setAttribute2[]
            span.setAttribute("systems.refreshed", refreshed);
            // end::setAttribute2[]
            if (hosts.isEmpty()) {
                // tag::addEvent1[]
                span.addEvent("No systems found to refresh");
                // end::addEvent1[]
            } else {
                // tag::addEvent2[]
                span.addEvent("Refreshed system load for " + refreshed + " hosts");
                // end::addEvent2[]
            }
        // tag::finally[]
        } finally {
            // tag::endSpan[]
            span.end();
            // end::endSpan[]
        }
        // end::finally[]
        // end::try[]
    }
    // end::refreshSystemLoads[]
}
