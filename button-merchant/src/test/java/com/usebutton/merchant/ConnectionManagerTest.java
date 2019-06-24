/*
 * ConnectionManagerTest.java
 *
 * Copyright (c) 2019 Button, Inc. (https://usebutton.com)
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

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class ConnectionManagerTest {

    private static final String VALID_UA = "valid_user_agent";

    private ConnectionManager connectionManager;
    private MockWebServer server = new MockWebServer();

    @Before
    public void setUp() throws Exception {
        server.start();

        HttpUrl baseUrl = server.url("");
        String url = baseUrl.url().toString();
        url = url.substring(0, url.length() - 1);

        connectionManager = new ConnectionManagerImpl(url, VALID_UA);
    }

    @After
    public void tearDown() throws Exception {
        server.close();
    }

    @Test(expected = ButtonNetworkException.class)
    public void post_invalidUrl_catchException() throws Exception {
        String url = "invalid_link";
        connectionManager = new ConnectionManagerImpl(url, VALID_UA);
        connectionManager.post("/test", null);
    }

    @Test(expected = ButtonNetworkException.class)
    public void post_status400_catchException() throws Exception {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                int code = 400 + new Random().nextInt(112);
                return new MockResponse().setResponseCode(code);
            }
        });
        connectionManager.post("/", null);
    }

    @Test
    public void post_shouldHaveCorrectHeaders() throws Exception {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                JSONObject res;
                try {
                    res = new JSONObject();
                    res.put("agent", request.getHeader("User-Agent"));
                    res.put("data", request.getHeader("Accept"));
                } catch (JSONException e) {
                    return new MockResponse().setResponseCode(500);
                }

                return new MockResponse()
                        .setResponseCode(200)
                        .setBody(res.toString());
            }
        });

        NetworkResponse response = connectionManager.post("/", null);
        assertEquals(response.getBody().optString("agent"), VALID_UA);
        assertEquals(response.getBody().optString("data"), "application/json");
    }

    @Test
    public void post_shouldReturnRequestAsResponse() throws Exception {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(request.getBody().inputStream()));

                JSONObject res;
                try {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    res = new JSONObject(response.toString());
                } catch (JSONException | IOException e) {
                    return new MockResponse().setResponseCode(500);
                }

                return new MockResponse()
                        .setResponseCode(200)
                        .setBody(res.toString());
            }
        });
        JSONObject body = new JSONObject();
        body.put("key", "val");
        body.put("abc", "def");
        body.put("123", "456");
        NetworkResponse response = connectionManager.post("/", body);

        assertEquals(response.getStatusCode(), 200);
        assertEquals(response.getBody().optString("key"), "val");
        assertEquals(response.getBody().optString("abc"), "def");
        assertEquals(response.getBody().optString("123"), "456");
    }

    @Test(expected = ButtonNetworkException.class)
    public void post_brokenResponse_catchException() throws Exception {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                return new MockResponse().setBody("}{");
            }
        });

        connectionManager.post("/", null);
    }
}