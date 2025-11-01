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
package io.openliberty.guides.inventory.model;

import jakarta.json.JsonObject;

public class SystemData {

    private final String hostname;
    private JsonObject systemLoad;

    public SystemData(String hostname, JsonObject systemLoad) {
        this.hostname = hostname;
        this.systemLoad = systemLoad;
    }

    public String getHostname() {
        return hostname;
    }

    public JsonObject getSystemLoad() {
        return systemLoad;
    }

    public void setSystemLoad(JsonObject systemLoad) {
        this.systemLoad = systemLoad;
    }
}
