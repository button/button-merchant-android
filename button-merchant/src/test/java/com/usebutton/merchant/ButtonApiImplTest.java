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

public class ButtonApiImplTest {

    private static final String APPLICATION_ID = "app-124567890abcdef";
    private final String userAgent = "valid_user_agent";

    private MockWebServer server = new MockWebServer();
    private ButtonApiImpl buttonApi = new ButtonApiImpl(userAgent);

    @Before
    public void setUp() throws Exception {
        server.start();

        HttpUrl baseUrl = server.url("");
        String url = baseUrl.url().toString();
        buttonApi.baseUrl = url.substring(0, url.length() - 1);
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
                buttonApi.getPendingLink(APPLICATION_ID, "valid_ifa",
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

        buttonApi.getPendingLink(APPLICATION_ID, "valid_ifa",
                true, Collections.singletonMap("key", "value"));

        RecordedRequest recordedRequest = server.takeRequest();
        Headers headers = recordedRequest.getHeaders();
        String body = recordedRequest.getBody().readUtf8();
        JSONObject bodyJson = new JSONObject(body);
        JSONObject signalsJson = bodyJson.getJSONObject("signals");

        // method
        assertEquals("POST", recordedRequest.getMethod());

        // headers
        assertEquals(userAgent, headers.get("User-Agent"));
        assertEquals("application/json", headers.get("Accept"));
        assertEquals("application/json", headers.get("Content-Type"));

        // request body
        assertEquals(APPLICATION_ID, bodyJson.getString("application_id"));
        assertEquals("valid_ifa", bodyJson.getString("ifa"));
        assertEquals(true, bodyJson.getBoolean("ifa_limited"));
        assertEquals("value", signalsJson.getString("key"));
    }

    @Test(expected = ButtonNetworkException.class)
    public void getPendingLink_returnErrorResponseStatusCodeOver400_catchException()
            throws Exception {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                return new MockResponse().setResponseCode(404);
            }
        });

        buttonApi.getPendingLink(APPLICATION_ID, "valid_ifa", true,
                Collections.<String, String>emptyMap());
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

        buttonApi.getPendingLink(APPLICATION_ID, "valid_ifa", true,
                Collections.<String, String>emptyMap());
    }

    @Test(expected = ButtonNetworkException.class)
    public void getPendingLink_invalidUrl_catchException() throws Exception {
        buttonApi.baseUrl = "invalid_url";

        buttonApi.getPendingLink(APPLICATION_ID, "valid_ifa", true,
                Collections.<String, String>emptyMap());
    }

    @Test(expected = ButtonNetworkException.class)
    public void postUserActivity_returnErrorResponseStatusCodeOver400_catchException()
            throws Exception {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                return new MockResponse().setResponseCode(404);
            }
        });

        Order order = new Order.Builder("123").setCurrencyCode("AUG").build();
        buttonApi.postActivity(APPLICATION_ID, "valid_aid", "valid_ts", order);
    }

    @Test(expected = ButtonNetworkException.class)
    public void postUserActivity_returnErrorMessage_catchException() throws Exception {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/v1/activity/order") && request.getMethod()
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
        buttonApi.postActivity(APPLICATION_ID, "valid_aid", "valid_ts", order);
    }

    @Test(expected = ButtonNetworkException.class)
    public void postUserActivity_invalidUrl_catchException() throws Exception {
        buttonApi.baseUrl = "invalid_url";

        Order order = new Order.Builder("123").setCurrencyCode("AUG").build();
        buttonApi.postActivity(APPLICATION_ID, "valid_aid", "valid_ts", order);
    }

    @Test
    public void postUserActivity_validateRequest() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200)
                .setBody("{\"meta\":{\"status\":\"ok\"}}\n"));

        Order order = new Order.Builder("123").setAmount(999).setCurrencyCode("AUG").build();
        buttonApi.postActivity(APPLICATION_ID, "valid_aid", "valid_ts", order);

        RecordedRequest recordedRequest = server.takeRequest();
        Headers headers = recordedRequest.getHeaders();
        String body = recordedRequest.getBody().readUtf8();
        JSONObject requestBodyJson = new JSONObject(body);
        // method
        assertEquals("POST", recordedRequest.getMethod());

        // headers
        assertEquals(userAgent, headers.get("User-Agent"));
        assertEquals("application/json", headers.get("Accept"));
        assertEquals("application/json", headers.get("Content-Type"));

        // request body
        assertEquals(APPLICATION_ID, requestBodyJson.getString("app_id"));
        assertEquals("valid_ts", requestBodyJson.getString("user_local_time"));
        assertEquals("valid_aid", requestBodyJson.getString("btn_ref"));
        assertEquals("123", requestBodyJson.getString("order_id"));
        assertEquals(999, requestBodyJson.getLong("total"));
        assertEquals("AUG", requestBodyJson.getString("currency"));
        assertEquals("merchant-library", requestBodyJson.getString("source"));
    }

    @Test(expected = ButtonNetworkException.class)
    public void postUserActivity_returnInvalidJson_catchException() throws Exception {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/v1/activity/order") && request.getMethod()
                        .equals("POST")) {
                    return new MockResponse()
                            .setResponseCode(200)
                            .setBody("");
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        Order order = new Order.Builder("123").setCurrencyCode("AUG").build();
        buttonApi.postActivity(APPLICATION_ID, "valid_aid", "valid_ts", order);
    }

    @Test()
    public void getBaseUrl_validateApplicationIdInUrl() {
        // Reinitialize the buttonApi variable to reset the baseUrl back to the original.
        buttonApi = new ButtonApiImpl(userAgent);

        final String expectedUrl = "https://" +  APPLICATION_ID + ".mobileapi.usebutton.com";
        final String actualUrl = buttonApi.getBaseUrl(APPLICATION_ID);

        assertEquals(expectedUrl, actualUrl);
    }
}
