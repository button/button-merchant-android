/*
 * EventTest.java
 *
 * Copyright (c) 2022 Button, Inc. (https://usebutton.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.usebutton.core.data.models;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class EventTest {

    @Test
    public void constructor_shouldConstructEmptyEvent() {
        Event event = new Event("btn:deeplink-opened", "valid_token");

        assertEquals("btn:deeplink-opened", event.getName());
        assertEquals("valid_token", event.getSourceToken());
        assertEquals((new JSONObject()).toString(), event.getEventBody().toString());
        assertNotNull(event.getId());
        assertTrue(event.getTimestamp() > 0);
    }

    @Test
    public void addProperty_shouldAddEventProperty() throws Exception {
        Event event = new Event("btn:deeplink-opened", "valid_token");
        event.addProperty("url", "valid_url");

        assertEquals("valid_url", event.getEventBody().getString("url"));
    }

    @Test
    public void toJson_shouldConvertEventToJsonObject() throws Exception {
        Event event = new Event("btn:deeplink-opened", "valid_token");
        event.addProperty("url", "valid_url");

        JSONObject eventJson = event.toJson();

        assertEquals("btn:deeplink-opened", eventJson.getString("name"));
        assertEquals("valid_token", eventJson.getString("source_token"));
        assertTrue(eventJson.has("value"));
    }
}