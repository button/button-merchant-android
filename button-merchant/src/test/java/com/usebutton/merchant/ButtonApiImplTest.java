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

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ButtonApiImplTest {

    private MockWebServer server = new MockWebServer();
    private ButtonApiImpl buttonApi = new ButtonApiImpl();

    @Before
    public void setUp() throws Exception {
        server.start();

        HttpUrl baseUrl = server.url("");
        String url = baseUrl.url().toString();
        buttonApi.BASE_URL = url.substring(0, url.length() - 1);
    }

    @After
    public void tearDown() throws Exception {
        server.close();
    }

    @Test
    public void getPendingLink_returnValidResponse_validatePostInstallLink() throws Exception {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/v1/web/deferred-deeplink") && request.getMethod()
                        .equals("POST")) {
                    return new MockResponse()
                            .setResponseCode(200)
                            .setBody(
                                    "{\"meta\":{\"status\":\"ok\"},\"object\":{\"match\":true,\"id\":\"ddl-6faffd3451edefd3\",\"action\":\"uber://asdfasfasf\",\"attribution\":{\"btn_ref\":\"srctok-afsldkjf29askldfjwe\",\"utm_source\":\"SMS\"}}}");
                }
                return new MockResponse().setResponseCode(404);
            }
        });

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
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));

        buttonApi.getPendingLink("valid_application_id", "valid_ifa",
                true, Collections.singletonMap("key", "value"));

        RecordedRequest recordedRequest = server.takeRequest();
        Headers headers = recordedRequest.getHeaders();
        String body = recordedRequest.getBody().readUtf8();
        JSONObject bodyJson = new JSONObject(body);
        JSONObject signalsJson = bodyJson.getJSONObject("signals");

        // method
        assertEquals("POST", recordedRequest.getMethod());

        // headers
        assertEquals("", headers.get("User-Agent"));
        assertEquals("application/json", headers.get("Accept"));
        assertEquals("application/json", headers.get("Content-Type"));

        // request body
        assertEquals("valid_application_id", bodyJson.getString("application_id"));
        assertEquals("valid_ifa", bodyJson.getString("ifa"));
        assertEquals(true, bodyJson.getBoolean("ifa_limited"));
        assertEquals("value", signalsJson.getString("key"));
    }

    @Test
    public void getPendingLink_returnInvalidResponse_validatePostInstallLink() throws Exception {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                return new MockResponse().setResponseCode(404);
            }
        });

        PostInstallLink postInstallLink =
                buttonApi.getPendingLink("valid_application_id", "valid_ifa", true,
                        Collections.<String, String>emptyMap());

        assertNull(postInstallLink);
    }

    @Test(expected = ButtonNetworkException.class)
    public void getPendingLink_returnInvalidJson_catchException() throws Exception {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/v1/web/deferred-deeplink") && request.getMethod()
                        .equals("POST")) {
                    return new MockResponse()
                            .setResponseCode(200)
                            .setBody("");
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        buttonApi.getPendingLink("valid_application_id", "valid_ifa", true,
                Collections.<String, String>emptyMap());
    }

    @Test(expected = ButtonNetworkException.class)
    public void postUserActivity_returnErrorResponseStatusCodeOver400() throws Exception {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                return new MockResponse().setResponseCode(404);
            }
        });

        Order order = new Order.Builder("123").setCurrencyCode("AUG").build();
        buttonApi.postActivity("valid_application_id", "valid_aid",
                "valid_ifa", true, "valid_ts", order);
    }

    @Test(expected = ButtonNetworkException.class)
    public void postUserActivity_returnErrorMessage() throws Exception {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/v2/session/useractivity") && request.getMethod()
                        .equals("POST")) {
                    return new MockResponse()
                            .setResponseCode(200)
                            .setBody(
                                    "{\"meta\":{\"status\":\"error\"},\"error\":{\"message\":\"Unknown source token\"}}\n");
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        Order order = new Order.Builder("123").setCurrencyCode("AUG").build();
        buttonApi.postActivity("valid_application_id", "valid_aid",
                "valid_ifa", true, "valid_ts", order);
    }

    @Test
    public void postUserActivity_validateRequest() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200)
                .setBody("{\"meta\":{\"status\":\"ok\"}}\n"));

        Order order = new Order.Builder("123").setAmount(999).setCurrencyCode("AUG").build();
        buttonApi.postActivity("valid_application_id", "valid_aid",
                "valid_ifa", true, "valid_ts", order);

        RecordedRequest recordedRequest = server.takeRequest();
        Headers headers = recordedRequest.getHeaders();
        String body = recordedRequest.getBody().readUtf8();
        JSONObject bodyJson = new JSONObject(body);
        JSONObject orderJson = bodyJson.getJSONObject("order");
        JSONObject deviceJson = bodyJson.getJSONObject("device");

        // method
        assertEquals("POST", recordedRequest.getMethod());

        // headers
        assertEquals("", headers.get("User-Agent"));
        assertEquals("application/json", headers.get("Accept"));
        assertEquals("application/json", headers.get("Content-Type"));

        // request body
        assertEquals("valid_application_id", bodyJson.getString("application_id"));
        assertEquals("valid_aid", bodyJson.getString("btn_ref"));
        assertEquals("order-checkout", bodyJson.getString("type"));
        assertEquals("valid_ts", bodyJson.getString("user_local_time"));

        // order body
        assertEquals("123", orderJson.getString("order_id"));
        assertEquals(999, orderJson.getLong("amount"));
        assertEquals("AUG", orderJson.getString("currency_code"));

        // device body
        assertEquals("valid_ifa", deviceJson.getString("ifa"));
        assertEquals(true, deviceJson.getBoolean("ifa_limited"));
    }

    @Test(expected = ButtonNetworkException.class)
    public void postUserActivity_returnInvalidJson_catchException() throws Exception {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/v2/session/useractivity") && request.getMethod()
                        .equals("POST")) {
                    return new MockResponse()
                            .setResponseCode(200)
                            .setBody("");
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        Order order = new Order.Builder("123").setCurrencyCode("AUG").build();
        buttonApi.postActivity("valid_application_id", "valid_aid",
                "valid_ifa", true, "valid_ts", order);
    }
}