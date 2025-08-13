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

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.openliberty.guides.system.health.SystemReadinessCheck;

@Path("unhealthy")
public class UnhealthyResource {
    @GET
    public String unhealthy() {
        SystemReadinessCheck.setUnhealthy();
        return "System is now unhealthy...\n";
    }
}
