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

import java.util.Properties;
import java.util.logging.Logger;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@RequestScoped
@Path("properties")
public class SystemResource {

    private static final Logger LOGGER =
        Logger.getLogger(SystemResource.class.getName());

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Properties getProperties() {
        // tag::log[]
        LOGGER.info("Received request to fetch system properties.");
        // end::log[]
        return System.getProperties();
    }
}
