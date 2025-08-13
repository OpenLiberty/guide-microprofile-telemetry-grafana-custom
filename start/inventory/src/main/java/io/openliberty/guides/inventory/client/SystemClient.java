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
package io.openliberty.guides.inventory.client;

import java.net.URI;
import java.util.Properties;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

public class SystemClient implements AutoCloseable {

    private static final String PROTOCOL = "http";
    private static final String SYSTEM_PROPERTIES = "/system/properties";
    private static final String SYSTEM_HEALTH = "/health";

    private String hostname;
    private int port;
    private Client client;

    public void init(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    private String buildUrl(String path) {
        try {
            URI uri = new URI(PROTOCOL, null, hostname, port, path, null, null);
            return uri.toString();
        } catch (Exception e) {
            // tag::out1[]
            System.err.println("URISyntaxException while building system service URL: "
                    + e.getMessage());
            // end::out1[]
            return null;
        }
    }

    private Builder buildClientBuilder(String urlString) {
        try {
            this.client = ClientBuilder.newClient();
            Builder builder = client.target(urlString).request();
            return builder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        } catch (Exception e) {
            // tag::out2[]
            System.err.println("Exception while creating REST client builder: "
                    + e.getMessage());
            // end::out2[]
            return null;
        }
    }

    public Properties getProperties() {
        String url = buildUrl(SYSTEM_PROPERTIES);
        Builder builder = buildClientBuilder(url);
        if (builder == null) return null;
        try {
            Response response = builder.get();
            // tag::out3[]
            System.out.println("Received response with status: " + response.getStatus());
            // end::out3[]
            if (response.getStatus() == Status.OK.getStatusCode()) {
                return response.readEntity(Properties.class);
            } else {
                // tag::out4[]
                System.out.println("Response Status is not OK.");
                // end::out4[]
            }
        } catch (RuntimeException e) {
            // tag::out5[]
            System.err.println("Runtime exception while invoking system service: "
                    + e.getMessage());
            // end::out5[]
        } catch (Exception e) {
            // tag::out6[]
            System.err.println("Unexpected exception while processing system service request: "
                    + e.getMessage());
            // end::out6[]
        }
        return null;
    }

    public String getHealth() {
        String url = buildUrl(SYSTEM_HEALTH);
        Builder builder = buildClientBuilder(url);
        if (builder == null) return "ERROR";
        try {
            Response response = builder.get();
            int statusCode = response.getStatus();
            if (statusCode == Status.OK.getStatusCode()) {
                return "UP";
            } else if (statusCode == Status.SERVICE_UNAVAILABLE.getStatusCode()) {
                return "DOWN";
            } else {
                return "ERROR";
            }
        } catch (Exception e) {
            // tag::out7[]
            System.err.println("Unexpected exception while processing system service request: "
                    + e.getMessage());
            // end::out7[]
        }
        return "ERROR";
    }

    @Override
    public void close() {
        if (client != null) {
            client.close();
            // tag::out8[]
            System.out.println("SystemClient HTTP client closed.");
            // end::out8[]
        }
    }
}
