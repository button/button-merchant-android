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

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
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

            JSONArray lineItemsJson = new JSONArray();
            for (int i = 0; i < order.getLineItems().size(); i++) {
                Order.LineItem lineItem = order.getLineItems().get(i);
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

                lineItemsJson.put(i, lineItemJson);
            }
            if (lineItemsJson.length() > 0) {
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

                if (customer.isNew() != null) {
                    customerJson.put("is_new", customer.isNew());
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
    public Void postActivity(String activityName, List<ButtonProductCompatible> products,
            @Nullable String sourceToken, @Nullable String advertisingId)
            throws ButtonNetworkException {

        try {
            JSONObject requestBody = new JSONObject();
            JSONObject activityBody = new JSONObject();
            requestBody.put("ifa", advertisingId);
            requestBody.put("btn_ref", sourceToken);
            activityBody.put("name", activityName);

            if (!products.isEmpty()) {
                JSONArray productsArray = new JSONArray();
                for (int i = 0; i < products.size(); i++) {
                    ButtonProductCompatible product = products.get(i);
                    List<String> categories = product.getCategories();
                    Map<String, String> attributes = product.getAttributes();
                    JSONObject productJson = new JSONObject();

                    // Convert categories list to JSON array
                    JSONArray categoriesJson = new JSONArray();
                    if (categories != null) {
                        for (int j = 0; j < categories.size(); j++) {
                            categoriesJson.put(j, categories.get(j));
                        }
                        productJson.put("categories", categoriesJson);
                    }

                    // Convert custom attributes map to JSON object
                    JSONObject attributesJson = new JSONObject();
                    if (attributes != null) {
                        for (Map.Entry<String, String> entry : attributes.entrySet()) {
                            attributesJson.putOpt(entry.getKey(), entry.getValue());
                        }
                        productJson.put("attributes", attributesJson);
                    }

                    // Put product data into a JSON object
                    productJson.put("id", product.getId());
                    productJson.put("upc", product.getUpc());
                    productJson.put("name", product.getName());
                    productJson.put("currency", product.getCurrency());
                    productJson.put("value", product.getValue());
                    productJson.put("quantity", product.getQuantity());
                    productJson.put("url", product.getUrl());

                    // Add to JSON array of products
                    productsArray.put(i, productJson);
                }

                // Append products only if available
                activityBody.put("products", productsArray);
            }

            // Append activity data to request body
            requestBody.put("activity_data", activityBody);

            ApiRequest apiRequest = new ApiRequest.Builder(ApiRequest.RequestMethod.POST,
                    "/v1/app/activity")
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
