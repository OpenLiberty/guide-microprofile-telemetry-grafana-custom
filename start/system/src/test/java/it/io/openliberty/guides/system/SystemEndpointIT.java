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
package it.io.openliberty.guides.system;

import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SystemEndpointIT {

    @Test
    public void testGetSystemLoad() {
        String port = System.getProperty("sys.http.port");
        String url = "http://localhost:" + port + "/system/systemLoad";

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(url);
        Response response = target.request().get();

        assertEquals(200, response.getStatus(),
                "Incorrect response code from " + url);

        JsonObject jsonObj = response.readEntity(JsonObject.class);

        assertTrue(jsonObj.containsKey("time"),
            "Response should contain time");
        assertTrue(jsonObj.containsKey("cpuLoad"),
            "Response should contain cpuLoad");
        assertTrue(jsonObj.containsKey("memoryUsage"),
            "Response should contain memoryUsage");

        assertNotNull(jsonObj.getString("time"), "time is null");
        assertNotNull(jsonObj.getJsonNumber("cpuLoad"), "cpuLoad is null");
        assertNotNull(jsonObj.getJsonNumber("memoryUsage"), "memoryUsage is null");

        response.close();
        client.close();
    }
}
