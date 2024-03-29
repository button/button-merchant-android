/*
 * EventTest.java
 *
 * Copyright (c) 2020 Button, Inc. (https://usebutton.com)
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

package com.usebutton.merchant;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

public class EventTest {

    @Test
    public void constructor_knownName_shouldConstructEmptyButtonEvent() {
        Event event = new Event(Event.Name.DEEPLINK_OPENED, "valid_token");

        assertEquals(Event.Name.DEEPLINK_OPENED.toString(), event.getName());
        assertEquals(Event.Source.BUTTON, event.getSource());
        assertEquals("valid_token", event.getSourceToken());
        assertEquals((new JSONObject()).toString(), event.getEventBody().toString());
        assertNotNull(event.getId());
        assertTrue(event.getTimestamp() > 0);
    }

    @Test
    public void addProperty_shouldAddEventProperty() throws Exception {
        Event event = new Event(Event.Name.DEEPLINK_OPENED, "valid_token");
        event.addProperty(Event.Property.URL, "valid_url");

        assertEquals("valid_url", event.getEventBody().getString(Event.Property.URL.toString()));
    }

    @Test
    public void toJson_shouldConvertEventToJsonObject() throws Exception {
        Event event = new Event(Event.Name.DEEPLINK_OPENED, "valid_token");
        event.addProperty(Event.Property.URL, "valid_url");

        JSONObject eventJson = event.toJson();

        assertEquals(Event.Name.DEEPLINK_OPENED.toString(), eventJson.getString("name"));
        assertEquals(Event.Source.BUTTON, event.getSource());
        assertEquals("valid_token", eventJson.getString("source_token"));
        assertTrue(eventJson.has("value"));
    }

    @Test
    public void constructor_stringName_shouldConstructCustomEvent() throws Exception {
        Event event = new Event("custom-event", "valid_token",
                Collections.singletonMap("key", "value"));

        assertEquals("custom-event", event.getName());
        assertEquals(Event.Source.CUSTOM, event.getSource());
        assertEquals("valid_token", event.getSourceToken());
        assertEquals("{\"key\":\"value\"}", event.getEventBody().toString());
        assertNotNull(event.getId());
        assertTrue(event.getTimestamp() > 0);


    }

    @Test
    public void toJson_shouldConvertCustomEventToJsonObject() throws Exception {
        Event event = new Event("custom-event", "valid_token",
                Collections.singletonMap("key", "value"));

        JSONObject eventJson = event.toJson();
        assertEquals("custom-event", eventJson.getString("name"));
        assertEquals("custom", eventJson.getString("source"));
        assertEquals("valid_token", eventJson.getString("source_token"));
        assertEquals("{\"extra\":{\"key\":\"value\"}}", eventJson.getJSONObject("value").toString());
        assertNotNull(eventJson.getString("uuid"));
        assertNotNull(eventJson.getString("time"));

        event = new Event("custom-event", "valid_token", null);
        eventJson = event.toJson();
        assertEquals("{}", eventJson.getJSONObject("value").toString());
    }
}