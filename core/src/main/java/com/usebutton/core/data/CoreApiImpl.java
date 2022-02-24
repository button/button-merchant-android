/*
 * CoreApiImpl.java
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

package com.usebutton.core.data;

import android.support.annotation.Nullable;
import android.util.Log;

import com.usebutton.core.ButtonUtil;
import com.usebutton.core.data.models.Event;
import com.usebutton.core.exception.ButtonNetworkException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

public class CoreApiImpl implements CoreApi {

    private static final String TAG = CoreApiImpl.class.getSimpleName();

    protected final ConnectionManager connectionManager;

    public CoreApiImpl(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Void postEvents(List<Event> events, @Nullable String advertisingId)
            throws ButtonNetworkException {
        try {
            JSONArray eventStream = new JSONArray();
            for (int i = 0; i < events.size(); i++) {
                JSONObject eventJson = events.get(i).toJson();
                eventStream.put(i, eventJson);
            }

            JSONObject requestBody = new JSONObject();
            requestBody.put("ifa", advertisingId);
            requestBody.put("current_time", ButtonUtil.formatDate(new Date()));
            requestBody.put("events", eventStream);

            ApiRequest apiRequest = new ApiRequest.Builder(ApiRequest.RequestMethod.POST,
                    "/v1/app/events")
                    .setBody(requestBody)
                    .build();
            connectionManager.executeRequest(apiRequest);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating request body", e);
            throw new ButtonNetworkException(e);
        }

        return null;
    }
}
