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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
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

    @Override
    public void setApplicationId(String applicationId) {
        connectionManager.setApplicationId(applicationId);
    }

    @Nullable
    @Override
    public String getApplicationId() {
        return connectionManager.getApplicationId();
    }

    @Nullable
    @WorkerThread
    @Override
    public PostInstallLink getPendingLink(String applicationId, @Nullable String advertisingId,
            Map<String, String> signalsMap) throws
            ButtonNetworkException {

        try {
            // Create request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("application_id", applicationId);
            requestBody.put("ifa", advertisingId);
            requestBody.put("signals", new JSONObject(signalsMap));

            ApiRequest apiRequest = new ApiRequest.Builder(ApiRequest.RequestMethod.POST,
                    "/v1/app/deferred-deeplink")
                    .setBody(requestBody)
                    .build();

            // Execute POST request and parse response
            NetworkResponse response = connectionManager.executeRequest(apiRequest);
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

    @Nullable
    @Override
    public Void postOrder(Order order, String applicationId, String sourceToken,
            @Nullable String advertisingId) throws ButtonNetworkException {

        try {
            // Create request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("currency", order.getCurrencyCode());
            requestBody.put("btn_ref", sourceToken);
            requestBody.put("order_id", order.getId());
            requestBody.put("purchase_date", ButtonUtil.formatDate(order.getPurchaseDate()));
            requestBody.put("customer_order_id", order.getCustomerOrderId());
            requestBody.put("advertising_id", advertisingId);

            for (Order.LineItem lineItem : order.getLineItems()) {
                JSONArray lineItemsJson = new JSONArray();
                JSONObject lineItemJson = new JSONObject();

                List<String> lineItemCategory = lineItem.getCategory();
                if (lineItemCategory != null) {
                    JSONArray categoryJson = new JSONArray();
                    for (String category : lineItemCategory) {
                        categoryJson.put(category);
                    }

                    lineItemJson.put("category", categoryJson);
                }

                lineItemJson.put("identifier", lineItem.getId());
                lineItemJson.put("quantity", lineItem.getQuantity());
                lineItemJson.put("total", lineItem.getTotal());

                Map<String, String> lineItemAttributes = lineItem.getAttributes();
                if (lineItemAttributes != null) {
                    JSONObject attributesJson = new JSONObject();
                    for (Map.Entry<String, String> entry : lineItemAttributes.entrySet()) {
                        attributesJson.put(entry.getKey(), entry.getValue());
                    }

                    lineItemJson.put("attributes", attributesJson);
                }

                lineItemJson.put("upc", lineItem.getUpc());
                lineItemJson.put("description", lineItem.getDescription());
                lineItemJson.put("sku", lineItem.getSku());

                lineItemsJson.put(lineItemJson);
                requestBody.put("line_items", lineItemsJson);
            }

            Order.Customer customer = order.getCustomer();
            if (customer != null) {
                JSONObject customerJson = new JSONObject();
                customerJson.put("id", customer.getId());

                String email = customer.getEmail();
                if (email != null) {
                    if (ButtonUtil.isValidEmail(email)) {
                        email = ButtonUtil.sha256Encode(email.toLowerCase());
                    }
                    customerJson.put("email_sha256", email);
                }

                requestBody.put("customer", customerJson);
            }

            applicationId = ButtonUtil.base64Encode(applicationId + ":");
            ApiRequest apiRequest = new ApiRequest.Builder(ApiRequest.RequestMethod.POST,
                    "/v1/app/order")
                    .addHeader("Authorization", String.format("Basic %s", applicationId))
                    .setBody(requestBody)
                    .build();

            connectionManager.executeRequest(apiRequest);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating request body", e);
            throw new ButtonNetworkException(e);
        }

        return null;
    }

    @Nullable
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
