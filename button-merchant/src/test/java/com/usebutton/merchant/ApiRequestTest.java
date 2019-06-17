/*
 * ApiRequestTest.java
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

import org.json.JSONObject;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ApiRequestTest {

    @Test
    public void apiRequest_verifyDefaultValues() {
        ApiRequest apiRequest = new ApiRequest.Builder(ApiRequest.RequestMethod.POST, "/")
                .build();

        assertEquals(ApiRequest.RequestMethod.POST, apiRequest.getRequestMethod());
        assertEquals("/", apiRequest.getPath());
        assertNull(apiRequest.getHeaders());
        assertNull(apiRequest.getBody());
    }

    @Test
    public void apiRequest_addHeader_verifyHeaders() {
        ApiRequest apiRequest = new ApiRequest.Builder(ApiRequest.RequestMethod.POST, "/")
                .addHeader("header_key", "header_value")
                .build();

        Map<String, String> headers = apiRequest.getHeaders();
        assertEquals("header_value", headers.get("header_key"));
        assertEquals(1, headers.size());
    }

    @Test
    public void apiRequest_setBody_verifyBody() {
        JSONObject body = new JSONObject();

        ApiRequest apiRequest = new ApiRequest.Builder(ApiRequest.RequestMethod.POST, "/")
                .setBody(body)
                .build();

        assertEquals(body, apiRequest.getBody());
    }
}