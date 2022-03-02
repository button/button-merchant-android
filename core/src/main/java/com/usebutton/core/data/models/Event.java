/*
 * Event.java
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

import android.support.annotation.Nullable;
import android.util.Log;

import com.usebutton.core.ButtonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/**
 * Data class representing an event.
 */
public class Event {

    private static final String TAG = Event.class.getSimpleName();

    private final UUID id;
    private final long timestamp;
    private final String name;
    @Nullable
    private final String sourceToken;
    private JSONObject eventBody;

    public Event(String name, @Nullable String sourceToken) {
        this.id = UUID.randomUUID();
        this.timestamp = System.currentTimeMillis();
        this.name = name;
        this.sourceToken = sourceToken;
        this.eventBody = new JSONObject();
    }

    public void addProperty(String key, @Nullable String value) {
        try {
            eventBody.put(key, value);
        } catch (JSONException e) {
            Log.e(TAG, String.format("Error adding property [%s] to event [%s]", key, name), e);
        }
    }

    public void setValue(JSONObject eventBody) {
        this.eventBody = eventBody;
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

    @Nullable
    public String getSourceToken() {
        return sourceToken;
    }

    public JSONObject getEventBody() {
        return eventBody;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("source_token", sourceToken);
        json.put("time", ButtonUtil.formatTimestamp(timestamp));
        json.put("uuid", id.toString());
        eventBody.put("promotion_source_token", sourceToken);
        json.put("value", eventBody);
        return json;
    }
}
