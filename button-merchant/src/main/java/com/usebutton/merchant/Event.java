/*
 * Event.java
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

import androidx.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;

/**
 * Internal class used to encapsulate event data.
 */
class Event {

    private static final String TAG = Event.class.getSimpleName();

    /**
     * Button event names.
     */
    enum Name {
        DEEPLINK_OPENED("btn:deeplink-opened");

        private final String eventName;

        Name(String eventName) {
            this.eventName = eventName;
        }

        @Override
        public String toString() {
            return eventName;
        }
    }

    /**
     * Button event properties
     */
    enum Property {
        URL("url");

        private final String propertyName;

        Property(String propertyName) {
            this.propertyName = propertyName;
        }

        @Override
        public String toString() {
            return propertyName;
        }
    }

    /**
     * Event source
     */
    enum Source {
        BUTTON("button"),
        CUSTOM("custom");

        private final String sourceName;

        Source(String sourceName) {
            this.sourceName = sourceName;
        }

        @Override
        public String toString() {
            return sourceName;
        }
    }

    private final UUID id;
    private final long timestamp;
    private final String name;
    private final Source source;
    @Nullable
    private final String sourceToken;
    private final JSONObject eventBody;

    /**
     * Creates a Button sourced event.
     *
     * @param name Known event name
     * @param sourceToken Nullable attribution token
     */
    Event(Name name, @Nullable String sourceToken) {
        this(name.eventName, Source.BUTTON, sourceToken, null);
    }

    /**
     * Creates a custom sourced event (e.g. brand reported).
     *
     * @param name Custom event name
     * @param sourceToken Nullable attribution token
     * @param properties Optional properties
     */
    Event(String name, @Nullable String sourceToken, @Nullable Map<String, String> properties) {
        this(name, Source.CUSTOM, sourceToken, properties);
    }

    private Event(String name, Source source, @Nullable String sourceToken,
                  @Nullable Map<String, String> properties) {
        this.id = UUID.randomUUID();
        this.timestamp = System.currentTimeMillis();
        this.name = name;
        this.source = source;
        this.sourceToken = sourceToken;
        this.eventBody = new JSONObject();
        if (properties != null) {
            for (Map.Entry<String, String> prop : properties.entrySet()) {
                addProperty(prop.getKey(), prop.getValue());
            }
        }
    }

    void addProperty(Property key, @Nullable String value) {
        this.addProperty(key.propertyName, value);
    }

    private void addProperty(String key, @Nullable String value) {
        try {
            eventBody.put(key, value);
        } catch (JSONException e) {
            Log.e(TAG, String.format("Error adding property [%s] to event [%s]", key, name), e);
        }
    }

    public UUID getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getName() {
        return name;
    }

    public Source getSource() {
        return source;
    }

    @Nullable
    public String getSourceToken() {
        return sourceToken;
    }

    public JSONObject getEventBody() {
        return eventBody;
    }

    JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("source", source.sourceName);
        json.put("source_token", sourceToken);
        json.put("time", ButtonUtil.formatTimestamp(timestamp));
        json.put("uuid", id.toString());
        json.put("value", eventBody);
        if (source == Source.CUSTOM && eventBody.length() > 0) {
            JSONObject value = new JSONObject();
            value.put("extra", eventBody);
            json.put("value", value);
        }
        return json;
    }
}
