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
package it.io.openliberty.guides.inventory;

import jakarta.json.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(OrderAnnotation.class)
public class InventoryEndpointIT {

    private static String url;
    private Client client;

    @BeforeAll
    public static void oneTimeSetup() throws ServletException {
        String port = System.getProperty("inv.http.port");
        url = "http://localhost:" + port + "/inventory/systems";

        try (Client c = ClientBuilder.newClient();
            Response clearResponse = c.target(url).request().delete()) {

            int status = clearResponse.getStatus();
            if (status != Response.Status.OK.getStatusCode()
                && status != Response.Status.NOT_MODIFIED.getStatusCode()) {
                throw new ServletException("Could not clear inventory manager.");
            }
        }
    }

    @BeforeEach
    public void setup() {
        client = ClientBuilder.newClient();
    }

    @AfterEach
    public void teardown() {
        client.close();
    }

    @Test
    @Order(1)
    public void testEmptyInventory() {
        Response response = this.getResponse(url);
        this.assertResponse(url, response);

        JsonObject obj = response.readEntity(JsonObject.class);

        int expected = 0;
        int actual = obj.getInt("total");
        assertEquals(expected, actual,
                "The inventory should be empty on application start but it wasn't");

        response.close();
    }

    @Test
    @Order(2)
    public void testHostRegistration() {
        Response localhostResponse = this.getResponse(url + "/localhost");
        this.assertResponse(url + "/localhost", localhostResponse);

        JsonObject obj = getInventoryJson(1);

        JsonObject localhostObj = obj.getJsonArray("systems").getJsonObject(0);
        assertTrue(localhostObj.get("hostname").toString().contains("localhost"),
            "A host was registered, but it was not localhost");

        JsonObject systemLoadObj = localhostObj.getJsonObject("systemLoad");
        assertNotNull(systemLoadObj, "systemLoad should exist for localhost");
        assertNotNull(systemLoadObj.getJsonNumber("cpuLoad"),
            "cpuLoad should exist in localhost systemLoad");
        assertNotNull(systemLoadObj.getJsonNumber("memoryUsage"),
            "memoryUsage should exist in localhost systemLoad");
        assertNotNull(systemLoadObj.getString("time"),
            "time should exist in localhost systemLoad");

        localhostResponse.close();
    }

    @Test
    @Order(3)
    public void testUnknownHost() {
        Response badResponse = client.target(url + "/unknown")
                               .request(MediaType.APPLICATION_JSON)
                               .get();
        assertEquals(404, badResponse.getStatus(),
                "BadResponse expected status: 404. Response code not as expected.");

        String stringObj = badResponse.readEntity(String.class);
        assertTrue(stringObj.contains("error"),
                "badhostname is not a valid host but it didn't raise an error");

        badResponse.close();

        getInventoryJson(1);
   }

    private Response getResponse(String url) {
        return client.target(url).request().get();
    }

    private void assertResponse(String url, Response response) {
        assertEquals(200, response.getStatus(),
                "Incorrect response code from " + url);
    }

    private JsonObject getInventoryJson(int expectedCount) {
        Response response = this.getResponse(url);
        this.assertResponse(url, response);

        JsonObject obj = response.readEntity(JsonObject.class);
        int actual = obj.getInt("total");
        assertEquals(expectedCount, actual,
            "The inventory should contain " + expectedCount + " host(s).");

        response.close();
        return obj;
    }
}
