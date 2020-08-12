/*
 * ButtonApiImplTest.java
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

import com.usebutton.merchant.exception.ButtonNetworkException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ButtonApiImplTest {

    @Mock
    private ConnectionManager connectionManager;

    @InjectMocks
    private ButtonApiImpl buttonApi;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void setApplicationId_shouldProvideToConnectionManager() {
        buttonApi.setApplicationId("valid_application_id");

        verify(connectionManager).setApplicationId("valid_application_id");
    }

    @Test
    public void getPendingLink_returnValidResponse_validatePostInstallLink() throws Exception {
        JSONObject body = new JSONObject(
                "{\"meta\":{\"status\":\"ok\"},\"object\":{\"match\":true,\"id\":\"ddl-6faffd3451edefd3\",\"action\":\"uber://asdfasfasf\",\"attribution\":{\"btn_ref\":\"srctok-afsldkjf29askldfjwe\",\"utm_source\":\"SMS\"}}}");
        NetworkResponse response = new NetworkResponse(200, body);
        when(connectionManager.executeRequest(any(ApiRequest.class))).thenReturn(response);

        PostInstallLink postInstallLink =
                buttonApi.getPendingLink("valid_application_id", "valid_ifa",
                        Collections.<String, String>emptyMap());

        assertNotNull(postInstallLink);
        assertEquals(true, postInstallLink.isMatch());
        assertEquals("ddl-6faffd3451edefd3", postInstallLink.getId());
        assertEquals("uber://asdfasfasf", postInstallLink.getAction());
        PostInstallLink.Attribution attribution = postInstallLink.getAttribution();
        assertNotNull(attribution);
        assertEquals("srctok-afsldkjf29askldfjwe", attribution.getBtnRef());
        assertEquals("SMS", attribution.getUtmSource());
    }

    @Test
    public void getPendingLink_validateRequest() throws Exception {
        ArgumentCaptor<ApiRequest> argumentCaptor = ArgumentCaptor.forClass(ApiRequest.class);
        NetworkResponse response = new NetworkResponse(200, new JSONObject());
        when(connectionManager.executeRequest(argumentCaptor.capture()))
                .thenReturn(response);

        buttonApi.getPendingLink("valid_application_id", "valid_ifa",
                Collections.singletonMap("key", "value"));

        ApiRequest apiRequest = argumentCaptor.getValue();
        JSONObject body = apiRequest.getBody();
        JSONObject signals = body.getJSONObject("signals");

        assertEquals(apiRequest.getRequestMethod(), ApiRequest.RequestMethod.POST);
        assertEquals(apiRequest.getPath(), "/v1/app/deferred-deeplink");

        // request body
        assertEquals("valid_application_id", body.getString("application_id"));
        assertEquals("valid_ifa", body.getString("ifa"));
        assertEquals("value", signals.getString("key"));
    }

    @Test(expected = ButtonNetworkException.class)
    public void getPendingLink_returnJSONException_catchException() throws Exception {
        NetworkResponse response = mock(NetworkResponse.class);
        when(response.getBody()).thenThrow(JSONException.class);
        when(connectionManager.executeRequest(any(ApiRequest.class))).thenReturn(response);

        buttonApi.getPendingLink("valid_application_id", "valid_ifa",
                Collections.<String, String>emptyMap());
    }

    @Test(expected = ButtonNetworkException.class)
    public void getPendingLink_returnsException_catchException() throws Exception {
        when(connectionManager.executeRequest(any(ApiRequest.class)))
                .thenThrow(ButtonNetworkException.class);

        buttonApi.getPendingLink("valid_application_id", "valid_ifa",
                Collections.<String, String>emptyMap());
    }

    @Test
    public void postOrder_validateRequestMethodAndPath() throws Exception {
        Order order = new Order.Builder("123", new Date(), Collections.<Order.LineItem>emptyList())
                .build();

        buttonApi.postOrder(order, "valid_application_id",
                "valid_source_token", "valid_advertising_id");

        ArgumentCaptor<ApiRequest> argumentCaptor = ArgumentCaptor.forClass(ApiRequest.class);
        verify(connectionManager).executeRequest(argumentCaptor.capture());
        ApiRequest apiRequest = argumentCaptor.getValue();

        assertEquals(ApiRequest.RequestMethod.POST, apiRequest.getRequestMethod());
        assertEquals("/v1/app/order", apiRequest.getPath());
    }

    @Test
    public void postOrder_validateHeaders() throws Exception {
        Order order = new Order.Builder("123", new Date(), Collections.<Order.LineItem>emptyList())
                .build();

        String applicationId = "valid_application_id";
        buttonApi.postOrder(order, applicationId,
                "valid_source_token", "valid_advertising_id");

        ArgumentCaptor<ApiRequest> argumentCaptor = ArgumentCaptor.forClass(ApiRequest.class);
        verify(connectionManager).executeRequest(argumentCaptor.capture());
        ApiRequest apiRequest = argumentCaptor.getValue();


        assertEquals("Basic " + ButtonUtil.base64Encode(applicationId + ":"),
                apiRequest.getHeaders().get("Authorization"));
    }

    @Test
    public void postOrder_validateOrder() throws Exception {
        String orderId = "valid_order_id";
        Date purchaseDate = new Date();
        String currencyCode = "valid_currency_code";
        String customerOrderId = "valid_customer_order_id";

        Order order = new Order.Builder(orderId, purchaseDate,
                Collections.<Order.LineItem>emptyList())
                .setCurrencyCode(currencyCode)
                .setCustomerOrderId(customerOrderId)
                .build();

        String sourceToken = "valid_source_token";
        String advertisingId = "valid_advertising_id";

        buttonApi.postOrder(order, "valid_application_id",
                sourceToken, advertisingId);

        ArgumentCaptor<ApiRequest> argumentCaptor = ArgumentCaptor.forClass(ApiRequest.class);
        verify(connectionManager).executeRequest(argumentCaptor.capture());
        ApiRequest apiRequest = argumentCaptor.getValue();

        JSONObject requestBody = apiRequest.getBody();
        assertEquals(currencyCode, requestBody.getString("currency"));
        assertEquals(sourceToken, requestBody.getString("btn_ref"));
        assertEquals(orderId, requestBody.getString("order_id"));
        assertEquals(ButtonUtil.formatDate(purchaseDate),
                requestBody.getString("purchase_date"));
        assertEquals(customerOrderId, requestBody.getString("customer_order_id"));
        assertEquals(advertisingId, requestBody.getString("advertising_id"));

        assertNull(requestBody.optJSONArray("line_items"));
        assertNull(requestBody.optJSONObject("customer"));
    }

    @Test
    public void postOrder_validateLineItem() throws Exception {
        String lineItemId = "valid_line_item_id";
        long lineItemTotal = 100;
        Map<String, String> lineItemAttributes =
                Collections.singletonMap("valid_key", "valid_value");
        List<String> lineItemCategory = Collections.singletonList("valid_line_item_category");
        String lineItemUpc = "valid_line_item_upc";
        String lineItemSku = "valid_line_item_sku";
        String lineItemDescription = "valid_line_item_description";
        int lineItemQuantity = 5;
        Order.LineItem lineItem = new Order.LineItem.Builder(lineItemId, lineItemTotal)
                .setAttributes(lineItemAttributes)
                .setCategory(lineItemCategory)
                .setUpc(lineItemUpc)
                .setSku(lineItemSku)
                .setDescription(lineItemDescription)
                .setQuantity(lineItemQuantity)
                .build();

        Order order = new Order.Builder("123", new Date(), Collections.singletonList(lineItem))
                .build();

        buttonApi.postOrder(order, "valid_application_id",
                "valid_source_token", "valid_advertising_id");

        ArgumentCaptor<ApiRequest> argumentCaptor = ArgumentCaptor.forClass(ApiRequest.class);
        verify(connectionManager).executeRequest(argumentCaptor.capture());
        ApiRequest apiRequest = argumentCaptor.getValue();

        JSONObject requestBody = apiRequest.getBody();
        JSONArray lineItemsJsonArray = requestBody.getJSONArray("line_items");
        assertEquals(1, lineItemsJsonArray.length());

        JSONObject lineItemJson = lineItemsJsonArray.getJSONObject(0);

        assertEquals(lineItemId, lineItemJson.getString("identifier"));
        assertEquals(lineItemQuantity, lineItemJson.getInt("quantity"));
        assertEquals(lineItemTotal, lineItemJson.getLong("total"));
        assertEquals(lineItemUpc, lineItemJson.getString("upc"));
        assertEquals(lineItemDescription, lineItemJson.getString("description"));
        assertEquals(lineItemSku, lineItemJson.getString("sku"));

        JSONArray categoryJson = lineItemJson.getJSONArray("category");
        assertEquals(lineItemCategory.size(), categoryJson.length());
        assertEquals(lineItemCategory.get(0), categoryJson.getString(0));

        JSONObject attributesJson = lineItemJson.getJSONObject("attributes");
        assertEquals(lineItemAttributes.size(), attributesJson.length());
        for (Map.Entry<String, String> attribute : lineItemAttributes.entrySet()) {
            assertEquals(attribute.getValue(), attributesJson.getString(attribute.getKey()));
        }
    }

    @Test
    public void postOrder_validateCustomer() throws Exception {
        String customerId = "valid_customer_id";
        String customerEmail = "valid_customer_email";
        Order.Customer customer = new Order.Customer.Builder(customerId)
                .setEmail(customerEmail)
                .build();

        Order order = new Order.Builder("123", new Date(),
                Collections.<Order.LineItem>emptyList())
                .setCustomer(customer)
                .build();

        buttonApi.postOrder(order, "valid_application_id",
                "valid_source_token", "valid_advertising_id");

        ArgumentCaptor<ApiRequest> argumentCaptor = ArgumentCaptor.forClass(ApiRequest.class);
        verify(connectionManager).executeRequest(argumentCaptor.capture());
        ApiRequest apiRequest = argumentCaptor.getValue();

        JSONObject requestBody = apiRequest.getBody();
        JSONObject customerJson = requestBody.getJSONObject("customer");
        assertEquals(customerId, customerJson.getString("id"));
        assertEquals(customerEmail, customerJson.getString("email_sha256"));
    }

    @Test(expected = ButtonNetworkException.class)
    public void postOrder_returnsException_catchException() throws Exception {
        when(connectionManager.executeRequest(any(ApiRequest.class)))
                .thenThrow(ButtonNetworkException.class);

        Order order = new Order.Builder("valid_order_id", new Date(),
                Collections.<Order.LineItem>emptyList())
                .build();

        buttonApi.postOrder(order, "valid_application_id",
                "valid_source_token", "valid_advertising_id");
    }

    @Test
    public void postOrder_validEmail_verifyShaEmail() throws Exception {
        String customerId = "valid_customer_id";
        String customerEmail = "customer@usebutton.com";
        Order.Customer customer = new Order.Customer.Builder(customerId)
                .setEmail(customerEmail)
                .build();

        Order order = new Order.Builder("123", new Date(),
                Collections.<Order.LineItem>emptyList())
                .setCustomer(customer)
                .build();


        buttonApi.postOrder(order, "valid_application_id",
                "valid_source_token", "valid_advertising_id");

        ArgumentCaptor<ApiRequest> argumentCaptor = ArgumentCaptor.forClass(ApiRequest.class);
        verify(connectionManager).executeRequest(argumentCaptor.capture());
        ApiRequest apiRequest = argumentCaptor.getValue();

        JSONObject requestBody = apiRequest.getBody();
        JSONObject customerJson = requestBody.getJSONObject("customer");
        assertEquals(ButtonUtil.sha256Encode(customerEmail.toLowerCase()),
                customerJson.getString("email_sha256"));
    }

    @Test
    public void postOrder_invalidEmail_verifyPassthrough() throws Exception {
        String customerId = "valid_customer_id";
        String customerEmail = "customer";
        Order.Customer customer = new Order.Customer.Builder(customerId)
                .setEmail(customerEmail)
                .build();

        Order order = new Order.Builder("123", new Date(),
                Collections.<Order.LineItem>emptyList())
                .setCustomer(customer)
                .build();


        buttonApi.postOrder(order, "valid_application_id",
                "valid_source_token", "valid_advertising_id");

        ArgumentCaptor<ApiRequest> argumentCaptor = ArgumentCaptor.forClass(ApiRequest.class);
        verify(connectionManager).executeRequest(argumentCaptor.capture());
        ApiRequest apiRequest = argumentCaptor.getValue();

        JSONObject requestBody = apiRequest.getBody();
        JSONObject customerJson = requestBody.getJSONObject("customer");
        assertEquals(customerEmail, customerJson.getString("email_sha256"));
    }

    @Test
    public void postEvents_singleEvent_shouldReportCorrectly() throws Exception {
        ArgumentCaptor<ApiRequest> argumentCaptor = ArgumentCaptor.forClass(ApiRequest.class);
        List<Event> events = new ArrayList<>();
        Event event = new Event(Event.Name.DEEPLINK_OPENED, "valid_token");
        event.addProperty(Event.Property.URL, "valid_url");
        events.add(event);

        buttonApi.postEvents(events, null);

        verify(connectionManager).executeRequest(argumentCaptor.capture());
        ApiRequest apiRequest = argumentCaptor.getValue();
        JSONObject requestBody = apiRequest.getBody();
        JSONArray eventsJson = requestBody.getJSONArray("events");
        JSONObject eventJson = eventsJson.getJSONObject(0);

        assertEquals(1, eventsJson.length());
        assertEquals(eventJson.toString(), event.toJson().toString());
    }

    @Test
    public void postEvents_multipleEvents_shouldReportCorrectly() throws Exception {
        ArgumentCaptor<ApiRequest> argumentCaptor = ArgumentCaptor.forClass(ApiRequest.class);
        List<Event> events = new ArrayList<>();
        Event event = new Event(Event.Name.DEEPLINK_OPENED, "valid_token");
        event.addProperty(Event.Property.URL, "valid_url");
        events.add(event);
        events.add(event);

        buttonApi.postEvents(events, null);

        verify(connectionManager).executeRequest(argumentCaptor.capture());
        ApiRequest apiRequest = argumentCaptor.getValue();
        JSONObject requestBody = apiRequest.getBody();
        JSONArray eventsJson = requestBody.getJSONArray("events");
        JSONObject eventOneJson = eventsJson.getJSONObject(0);
        JSONObject eventTwoJson = eventsJson.getJSONObject(1);

        assertEquals(2, eventsJson.length());
        assertEquals(eventOneJson.toString(), event.toJson().toString());
        assertEquals(eventTwoJson.toString(), event.toJson().toString());
    }

    @Test
    public void postEvents_validIfa_shouldIncludeIfa() throws Exception {
        ArgumentCaptor<ApiRequest> argumentCaptor = ArgumentCaptor.forClass(ApiRequest.class);
        List<Event> events = new ArrayList<>();
        Event event = new Event(Event.Name.DEEPLINK_OPENED, "valid_token");
        event.addProperty(Event.Property.URL, "valid_url");
        events.add(event);

        buttonApi.postEvents(events, "valid_ifa");

        verify(connectionManager).executeRequest(argumentCaptor.capture());
        ApiRequest apiRequest = argumentCaptor.getValue();
        JSONObject requestBody = apiRequest.getBody();

        assertEquals(requestBody.getString("ifa"), "valid_ifa");
    }

    @Test
    public void postEvents_shouldIncludeTimestamp() throws Exception {
        ArgumentCaptor<ApiRequest> argumentCaptor = ArgumentCaptor.forClass(ApiRequest.class);
        List<Event> events = new ArrayList<>();
        Event event = new Event(Event.Name.DEEPLINK_OPENED, "valid_token");
        events.add(event);

        buttonApi.postEvents(events, null);

        verify(connectionManager).executeRequest(argumentCaptor.capture());
        ApiRequest apiRequest = argumentCaptor.getValue();
        JSONObject requestBody = apiRequest.getBody();

        assertTrue(requestBody.has("current_time"));
    }
}
