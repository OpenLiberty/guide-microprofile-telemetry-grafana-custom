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

import java.util.logging.Logger;

import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

@Singleton
@Startup
public class HealthCheckScheduler {

    private static final Logger logger = Logger.getLogger(HealthCheckScheduler.class.getName());
    
    @Inject
    private InventoryManager inventoryManager;
    
    @Schedule(hour = "*", minute = "*", second = "*/30", persistent = false)
    public void performHealthChecks() {
        int updated = inventoryManager.refreshAllSystemsHealth();
        logger.info("Scheduled health check completed. Updated " + updated + " system(s).");
    }
}
