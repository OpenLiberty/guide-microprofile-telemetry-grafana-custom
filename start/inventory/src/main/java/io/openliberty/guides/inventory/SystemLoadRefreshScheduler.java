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

import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;

@Singleton
@Startup
public class SystemLoadRefreshScheduler {

    @Inject
    private InventoryManager inventoryManager;

    @Schedule(hour = "*", minute = "*", second = "*/15", persistent = false)
    public void refreshSystemLoads() {
        for (String host : inventoryManager.getHosts()) {
            JsonObject systemLoad = inventoryManager.getSystemLoad(host);
            inventoryManager.set(host, systemLoad);
        }
    }
}
