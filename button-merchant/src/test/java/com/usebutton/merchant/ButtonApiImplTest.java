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

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
        when(connectionManager.post(eq("/v1/web/deferred-deeplink"), any(JSONObject.class)))
                .thenReturn(response);

        PostInstallLink postInstallLink =
                buttonApi.getPendingLink("valid_application_id", "valid_ifa",
                        true, Collections.<String, String>emptyMap());

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
        ArgumentCaptor<JSONObject> argumentCaptor = ArgumentCaptor.forClass(JSONObject.class);
        NetworkResponse response = new NetworkResponse(200, new JSONObject());
        when(connectionManager.post(eq("/v1/web/deferred-deeplink"), argumentCaptor.capture()))
                .thenReturn(response);

        buttonApi.getPendingLink("valid_application_id", "valid_ifa",
                true, Collections.singletonMap("key", "value"));

        JSONObject body = argumentCaptor.getValue();
        JSONObject signals = body.getJSONObject("signals");

        // request body
        assertEquals("valid_application_id", body.getString("application_id"));
        assertEquals("valid_ifa", body.getString("ifa"));
        assertEquals(true, body.getBoolean("ifa_limited"));
        assertEquals("value", signals.getString("key"));
    }

    @Test(expected = ButtonNetworkException.class)
    public void getPendingLink_returnJSONException_catchException() throws Exception {
        NetworkResponse response = mock(NetworkResponse.class);
        when(response.getBody()).thenThrow(JSONException.class);
        when(connectionManager.post(eq("/v1/web/deferred-deeplink"), any(JSONObject.class)))
                .thenReturn(response);

        buttonApi.getPendingLink("valid_application_id", "valid_ifa", true,
                Collections.<String, String>emptyMap());
    }

    @Test(expected = ButtonNetworkException.class)
    public void getPendingLink_returnsException_catchException() throws Exception {
        when(connectionManager.post(eq("/v1/web/deferred-deeplink"), any(JSONObject.class)))
                .thenThrow(ButtonNetworkException.class);

        buttonApi.getPendingLink("valid_application_id", "valid_ifa", true,
                Collections.<String, String>emptyMap());
    }

    @Test(expected = ButtonNetworkException.class)
    public void postUserActivity_returnsException_catchException() throws Exception {
        when(connectionManager.post(eq("/v1/activity/order"), any(JSONObject.class)))
                .thenThrow(ButtonNetworkException.class);

        Order order = new Order.Builder("123").setCurrencyCode("AUG").build();
        buttonApi.postActivity("valid_application_id", "valid_aid", "valid_ts", order);
    }

    @Test
    public void postUserActivity_validateRequest() throws Exception {
        ArgumentCaptor<JSONObject> argumentCaptor = ArgumentCaptor.forClass(JSONObject.class);
        JSONObject body = new JSONObject("{\"meta\":{\"status\":\"ok\"}}\n");
        NetworkResponse response = new NetworkResponse(200, body);
        when(connectionManager.post(eq("/v1/activity/order"), argumentCaptor.capture()))
                .thenReturn(response);

        Order order = new Order.Builder("123").setAmount(999).setCurrencyCode("AUG").build();
        buttonApi.postActivity("valid_application_id", "valid_aid", "valid_ts", order);

        JSONObject requestBody = argumentCaptor.getValue();

        // request body
        assertEquals("valid_application_id", requestBody.getString("app_id"));
        assertEquals("valid_ts", requestBody.getString("user_local_time"));
        assertEquals("valid_aid", requestBody.getString("btn_ref"));
        assertEquals("123", requestBody.getString("order_id"));
        assertEquals(999, requestBody.getLong("total"));
        assertEquals("AUG", requestBody.getString("currency"));
        assertEquals("merchant-library", requestBody.getString("source"));
    }
}
