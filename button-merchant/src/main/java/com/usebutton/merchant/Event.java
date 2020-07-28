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

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Internal class used to encapsulate event data.
 */
class Event {

    private static final String TAG = Event.class.getSimpleName();

    /**
     * Supported event names.
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
     * Supported event properties
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

    private final Name name;
    @Nullable
    private final String sourceToken;
    private final JSONObject eventBody;

    Event(Name name, @Nullable String sourceToken) {
        this.name = name;
        this.sourceToken = sourceToken;
        this.eventBody = new JSONObject();
    }

    void addProperty(Property key, @Nullable String value) {
        try {
            eventBody.put(key.propertyName, value);
        } catch (JSONException e) {
            Log.e(TAG, String.format("Error adding property [%s] to event [%s]", key, name), e);
        }
    }

    public Name getName() {
        return name;
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
        json.put("name", name.eventName);
        json.put("promotion_source_token", sourceToken);
        json.put("value", eventBody);
        return json;
    }
}
