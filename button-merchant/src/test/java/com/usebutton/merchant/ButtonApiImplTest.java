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

import com.usebutton.core.ButtonUtil;
import com.usebutton.core.data.ApiRequest;
import com.usebutton.core.data.ConnectionManager;
import com.usebutton.core.data.models.Event;
import com.usebutton.core.data.models.NetworkResponse;
import com.usebutton.core.exception.ButtonNetworkException;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
    public void postOrder_validateMultipleLineItems() throws Exception {
        Order.LineItem lineItem1 = new Order.LineItem.Builder("valid_item_id_1", 100)
                .setAttributes(Collections.singletonMap("valid_key_1", "valid_value_1"))
                .setCategory(Collections.singletonList("valid_line_item_category_1"))
                .setUpc("valid_line_item_upc_1")
                .setSku("valid_line_item_sku_1")
                .setDescription("valid_line_item_description_1")
                .setQuantity(5)
                .build();
        Order.LineItem lineItem2 = new Order.LineItem.Builder("valid_item_id_2", 200)
                .setAttributes(Collections.singletonMap("valid_key_2", "valid_value_2"))
                .setCategory(Collections.singletonList("valid_line_item_category_2"))
                .setUpc("valid_line_item_upc_2")
                .setSku("valid_line_item_sku_2")
                .setDescription("valid_line_item_description_2")
                .setQuantity(10)
                .build();
        List<Order.LineItem> lineItems = new ArrayList<>();
        lineItems.add(lineItem1);
        lineItems.add(lineItem2);
        Order order = new Order.Builder("123", new Date(), lineItems).build();

        buttonApi.postOrder(order, "valid_application_id",
                "valid_source_token", "valid_advertising_id");

        ArgumentCaptor<ApiRequest> argumentCaptor = ArgumentCaptor.forClass(ApiRequest.class);
        verify(connectionManager).executeRequest(argumentCaptor.capture());
        ApiRequest apiRequest = argumentCaptor.getValue();

        JSONObject requestBody = apiRequest.getBody();
        JSONArray lineItemsJsonArray = requestBody.getJSONArray("line_items");
        assertEquals(2, lineItemsJsonArray.length());

        JSONObject lineItem1Json = lineItemsJsonArray.getJSONObject(0);
        assertEquals(lineItem1.getId(), lineItem1Json.getString("identifier"));
        assertEquals(lineItem1.getQuantity(), lineItem1Json.getInt("quantity"));
        assertEquals(lineItem1.getTotal(), lineItem1Json.getLong("total"));
        assertEquals(lineItem1.getUpc(), lineItem1Json.getString("upc"));
        assertEquals(lineItem1.getDescription(), lineItem1Json.getString("description"));
        assertEquals(lineItem1.getSku(), lineItem1Json.getString("sku"));
        JSONArray category1Json = lineItem1Json.getJSONArray("category");
        assertEquals(lineItem1.getCategory().size(), category1Json.length());
        for (int i = 0; i < lineItem1.getCategory().size(); i++) {
            assertEquals(lineItem1.getCategory().get(i), category1Json.getString(i));
        }
        JSONObject attributes1Json = lineItem1Json.getJSONObject("attributes");
        assertEquals(lineItem1.getAttributes().size(), attributes1Json.length());
        for (Map.Entry<String, String> attribute : lineItem1.getAttributes().entrySet()) {
            assertEquals(attribute.getValue(), attributes1Json.getString(attribute.getKey()));
        }

        JSONObject lineItem2Json = lineItemsJsonArray.getJSONObject(1);
        assertEquals(lineItem2.getId(), lineItem2Json.getString("identifier"));
        assertEquals(lineItem2.getQuantity(), lineItem2Json.getInt("quantity"));
        assertEquals(lineItem2.getTotal(), lineItem2Json.getLong("total"));
        assertEquals(lineItem2.getUpc(), lineItem2Json.getString("upc"));
        assertEquals(lineItem2.getDescription(), lineItem2Json.getString("description"));
        assertEquals(lineItem2.getSku(), lineItem2Json.getString("sku"));
        JSONArray category2Json = lineItem2Json.getJSONArray("category");
        assertEquals(lineItem2.getCategory().size(), category2Json.length());
        for (int i = 0; i < lineItem2.getCategory().size(); i++) {
            assertEquals(lineItem2.getCategory().get(i), category2Json.getString(i));
        }
        JSONObject attributes2Json = lineItem2Json.getJSONObject("attributes");
        assertEquals(lineItem2.getAttributes().size(), attributes2Json.length());
        for (Map.Entry<String, String> attribute : lineItem2.getAttributes().entrySet()) {
            assertEquals(attribute.getValue(), attributes2Json.getString(attribute.getKey()));
        }
    }

    @Test
    public void postOrder_validateCustomer() throws Exception {
        String customerId = "valid_customer_id";
        String customerEmail = "valid_customer_email";
        Order.Customer customer = new Order.Customer.Builder(customerId)
                .setEmail(customerEmail)
                .setIsNew(true)
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
        assertTrue(customerJson.getBoolean("is_new"));
    }

    @Test
    public void postOrder_shouldNotIncludeMissingCustomerProperties() throws Exception {
        String customerId = "valid_customer_id";
        Order.Customer customer = new Order.Customer.Builder(customerId).build();

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
        assertFalse(customerJson.has("email_sha256"));
        assertFalse(customerJson.has("is_new"));
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
    public void postActivity_validateRequest() throws Exception {
        ArgumentCaptor<ApiRequest> argumentCaptor = ArgumentCaptor.forClass(ApiRequest.class);

        buttonApi.postActivity("test-activity", Collections.<ButtonProductCompatible>emptyList(),
                null, null);
        verify(connectionManager).executeRequest(argumentCaptor.capture());
        ApiRequest apiRequest = argumentCaptor.getValue();

        assertEquals("/v1/app/activity", apiRequest.getPath());
        assertEquals(ApiRequest.RequestMethod.POST, apiRequest.getRequestMethod());
    }

    @Test
    public void postActivity_noProducts_shouldPostActivity() throws Exception {
        ArgumentCaptor<ApiRequest> argumentCaptor = ArgumentCaptor.forClass(ApiRequest.class);

        buttonApi.postActivity("test-activity", Collections.<ButtonProductCompatible>emptyList(),
                "test-token", null);
        verify(connectionManager).executeRequest(argumentCaptor.capture());
        ApiRequest apiRequest = argumentCaptor.getValue();
        JSONObject requestBody = apiRequest.getBody();
        JSONObject activityBody = requestBody.getJSONObject("activity_data");

        assertEquals("test-activity", activityBody.getString("name"));
        assertFalse(activityBody.has("products"));
        assertEquals("test-token", requestBody.getString("btn_ref"));
        assertFalse(requestBody.has("ifa"));
    }

    @Test
    public void postActivity_multipleProducts_shouldPostActivity() throws Exception {
        ArgumentCaptor<ApiRequest> argumentCaptor = ArgumentCaptor.forClass(ApiRequest.class);
        final ButtonProductCompatible productOne = new ButtonProduct() {{
            setId("one");
        }};
        final ButtonProductCompatible productTwo = new ButtonProduct() {{
            setId("two");
        }};

        buttonApi.postActivity("test-activity", new ArrayList<ButtonProductCompatible>() {{
            add(productOne);
            add(productTwo);
        }}, "test-token", null);
        verify(connectionManager).executeRequest(argumentCaptor.capture());
        ApiRequest apiRequest = argumentCaptor.getValue();
        JSONObject requestBody = apiRequest.getBody();
        JSONObject activityBody = requestBody.getJSONObject("activity_data");

        assertEquals("test-activity", activityBody.getString("name"));
        assertEquals(2, activityBody.getJSONArray("products").length());
        assertEquals("one", activityBody.getJSONArray("products").getJSONObject(0).getString("id"));
        assertEquals("two", activityBody.getJSONArray("products").getJSONObject(1).getString("id"));
        assertEquals("test-token", requestBody.getString("btn_ref"));
        assertFalse(requestBody.has("ifa"));
    }

    @Test
    public void postActivity_withEmptyProductInfo_shouldPostActivity() throws Exception {
        ArgumentCaptor<ApiRequest> argumentCaptor = ArgumentCaptor.forClass(ApiRequest.class);
        ButtonProductCompatible product = new ButtonProduct();

        buttonApi.postActivity("test-activity", Collections.singletonList(product), null, null);
        verify(connectionManager).executeRequest(argumentCaptor.capture());
        ApiRequest apiRequest = argumentCaptor.getValue();
        JSONObject requestBody = apiRequest.getBody();
        JSONObject activityBody = requestBody.getJSONObject("activity_data");

        assertEquals("test-activity", activityBody.getString("name"));
        assertEquals(1, activityBody.getJSONArray("products").length());
        assertEquals(0, activityBody.getJSONArray("products").getJSONObject(0).length());
        assertFalse(requestBody.has("btn_ref"));
        assertFalse(requestBody.has("ifa"));
    }

    @Test
    public void postActivity_withCompleteProductInfo_shouldPostActivity() throws Exception {
        ArgumentCaptor<ApiRequest> argumentCaptor = ArgumentCaptor.forClass(ApiRequest.class);
        List<String> categories = new ArrayList<>();
        categories.add("cat-one");
        categories.add("cat-two");
        Map<String, String> attributes = new HashMap<>();
        attributes.put("attr-one", "1");
        attributes.put("attr-two", "2");
        final ButtonProduct product = new ButtonProduct();
        product.setId("test-id");
        product.setUpc("test-upc");
        product.setCategories(categories);
        product.setName("test-name");
        product.setCurrency("test-curr");
        product.setQuantity(2);
        product.setValue(1234);
        product.setUrl("test-url");
        product.setAttributes(attributes);

        buttonApi.postActivity("test-activity", new ArrayList<ButtonProductCompatible>() {{
            add(product);
        }}, "test-token", "test-ifa");
        verify(connectionManager).executeRequest(argumentCaptor.capture());
        ApiRequest apiRequest = argumentCaptor.getValue();
        JSONObject requestBody = apiRequest.getBody();
        JSONObject activityBody = requestBody.getJSONObject("activity_data");

        assertEquals("test-ifa", requestBody.getString("ifa"));
        assertEquals("test-token", requestBody.getString("btn_ref"));
        assertEquals("test-activity", activityBody.getString("name"));
        assertEquals(1, activityBody.getJSONArray("products").length());
        JSONObject productJson = activityBody.getJSONArray("products").getJSONObject(0);
        assertEquals("test-id", productJson.getString("id"));
        assertEquals("test-upc", productJson.getString("upc"));
        assertEquals("test-name", productJson.getString("name"));
        assertEquals("test-curr", productJson.getString("currency"));
        assertEquals("test-url", productJson.getString("url"));
        assertEquals(2, productJson.getInt("quantity"));
        assertEquals(1234, productJson.getInt("value"));
        assertEquals(2, productJson.getJSONArray("categories").length());
        assertEquals("cat-one", productJson.getJSONArray("categories").getString(0));
        assertEquals("cat-two", productJson.getJSONArray("categories").getString(1));
        assertEquals(2, productJson.getJSONObject("attributes").length());
        assertEquals("1", productJson.getJSONObject("attributes").getString("attr-one"));
        assertEquals("2", productJson.getJSONObject("attributes").getString("attr-two"));
    }

    @Test
    public void postEvents_singleEvent_shouldReportCorrectly() throws Exception {
        ArgumentCaptor<ApiRequest> argumentCaptor = ArgumentCaptor.forClass(ApiRequest.class);
        List<Event> events = new ArrayList<>();
        Event event = new Event("btn:deeplink-opened", "valid_token");
        event.addProperty("url", "valid_url");
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
        Event event = new Event("btn:deeplink-opened", "valid_token");
        event.addProperty("url", "valid_url");
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
        Event event = new Event("btn:deeplink-opened", "valid_token");
        event.addProperty("url", "valid_url");
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
        Event event = new Event("btn:deeplink-opened", "valid_token");
        events.add(event);

        buttonApi.postEvents(events, null);

        verify(connectionManager).executeRequest(argumentCaptor.capture());
        ApiRequest apiRequest = argumentCaptor.getValue();
        JSONObject requestBody = apiRequest.getBody();

        assertTrue(requestBody.has("current_time"));
    }
}
