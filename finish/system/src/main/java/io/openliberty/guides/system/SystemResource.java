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
package io.openliberty.guides.system;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.Calendar;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@ApplicationScoped
@Path("systemLoad")
public class SystemResource {

    private static final OperatingSystemMXBean OS =
        (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    private static final MemoryMXBean MEM =
        ManagementFactory.getMemoryMXBean();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getSystemLoad() {
        long heapMax = MEM.getHeapMemoryUsage().getMax();
        long heapUsed = MEM.getHeapMemoryUsage().getUsed();

        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("time", Calendar.getInstance().getTime().toString());
        builder.add("cpuLoad", Double.valueOf(OS.getCpuLoad() * 100.0));
        builder.add("memoryUsage", Double.valueOf(heapUsed * 100.0 / heapMax));

        JsonObject systemLoad = builder.build();
        return systemLoad;
    }
}
