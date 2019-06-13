/*
 * ButtonApiImpl.java
 *
 * Copyright (c) 2018 Button, Inc. (https://usebutton.com)
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
import android.support.annotation.VisibleForTesting;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.usebutton.merchant.exception.ButtonNetworkException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Button API endpoint request implementations.
 */
final class ButtonApiImpl implements ButtonApi {

    private static final String TAG = ButtonApiImpl.class.getSimpleName();
    private static ButtonApi buttonApi;

    private final ConnectionManager connectionManager;

    static ButtonApi getInstance(ConnectionManager connectionManager) {
        if (buttonApi == null) {
            buttonApi = new ButtonApiImpl(connectionManager);
        }

        return buttonApi;
    }

    @VisibleForTesting
    ButtonApiImpl(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Nullable
    @WorkerThread
    @Override
    public PostInstallLink getPendingLink(String applicationId, String ifa,
            boolean limitAdTrackingEnabled, Map<String, String> signalsMap) throws
            ButtonNetworkException {

        try {
            // Create request body
            JSONObject request = new JSONObject();
            request.put("application_id", applicationId);
            request.put("ifa", ifa);
            request.put("ifa_limited", limitAdTrackingEnabled);
            request.put("signals", new JSONObject(signalsMap));

            // Execute POST request and parse response
            NetworkResponse response = connectionManager.post("/v1/web/deferred-deeplink", request);
            JSONObject responseBody = response.getBody().optJSONObject("object");
            if (responseBody != null) {
                boolean match = responseBody.getBoolean("match");
                String id = responseBody.getString("id");
                String action = responseBody.getString("action");
                PostInstallLink.Attribution attribution = null;

                JSONObject attributionJson = responseBody.optJSONObject("attribution");
                if (attributionJson != null) {
                    String btnRef = attributionJson.getString("btn_ref");
                    String utmSource = attributionJson.optString("utm_source", null);
                    attribution = new PostInstallLink.Attribution(btnRef, utmSource);
                }

                return new PostInstallLink(match, id, action, attribution);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error creating request body", e);
            throw new ButtonNetworkException(e);
        }

        return null;
    }

    @Override
    public Void postActivity(String applicationId, String sourceToken, String timestamp,
            Order order) throws ButtonNetworkException {

        try {
            // Create request body
            JSONObject request = new JSONObject();
            request.put("app_id", applicationId);
            request.put("user_local_time", timestamp);
            request.put("btn_ref", sourceToken);
            request.put("order_id", order.getId());
            request.put("total", order.getAmount());
            request.put("currency", order.getCurrencyCode());
            request.put("source", "merchant-library");

            // Execute POST request and parse response
            connectionManager.post("/v1/activity/order", request);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating request body", e);
            throw new ButtonNetworkException(e);
        }

        return null;
    }
}
