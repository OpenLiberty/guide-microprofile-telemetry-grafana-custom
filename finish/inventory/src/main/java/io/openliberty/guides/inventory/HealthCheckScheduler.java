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

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;

@Singleton
@Startup
public class HealthCheckScheduler {

    private static final Logger LOGGER =
        Logger.getLogger(HealthCheckScheduler.class.getName());

    @Inject
    private InventoryManager inventoryManager;

    // tag::meter[]
    @Inject
    Meter meter;
    // end::meter[]

    private DoubleHistogram schedulerDuration;
    private LongCounter schedulerRunCounter;

    @PostConstruct
    public void init() {
        // tag::counterBuilder[]
        schedulerRunCounter =
            // tag::schedulerRunCounter[]
            meter.counterBuilder("inventory.health.check.scheduler.runs")
            // end::schedulerRunCounter[]
                 .setDescription("Total number of scheduler executions")
                 .setUnit("1")
                 .build();
        // end::counterBuilder[]

        // tag::histogramBuilder[]
        schedulerDuration =
            // tag::schedulerDuration[]
            meter.histogramBuilder("inventory.health.check.scheduler.duration")
            // end::schedulerDuration[]
                 .setDescription("Duration of scheduled health check runs")
                 .setUnit("s")
                 .build();
        // end::histogramBuilder[]
    }

    // tag::scheduleAnnotation[]
    @Schedule(hour = "*", minute = "*", second = "*/30", persistent = false)
    // end::scheduleAnnotation[]
    // tag::performHealthChecks[]
    public void performHealthChecks() {
        // tag::start[]
        long start = System.nanoTime();
        // end::start[]

        int updated = inventoryManager.refreshAllSystemsHealth();

        // tag::duration[]
        double duration = (System.nanoTime() - start) / 1_000_000_000.0;
        // end::duration[]

        schedulerDuration.record(duration);
        schedulerRunCounter.add(1);

        LOGGER.info(
            "Scheduled health check completed. Updated " + updated + " system(s).");
    }
    // end::performHealthChecks[]
}
