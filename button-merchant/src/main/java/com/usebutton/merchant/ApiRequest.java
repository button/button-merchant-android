/*
 * ApiRequest.java
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

import java.util.HashMap;
import java.util.Map;

/**
 * Api request model for {@link ConnectionManager}
 */
class ApiRequest {

    /**
     * Request Method for the api request
     */
    enum RequestMethod {
        POST("POST");

        private final String value;

        RequestMethod(String value) {
            this.value = value;
        }

        String getValue() {
            return value;
        }
    }

    private final RequestMethod requestMethod;
    private final String path;
    private final Map<String, String> headers;
    private final JSONObject body;

    private ApiRequest(Builder builder) {
        this.requestMethod = builder.requestMethod;
        this.path = builder.path;
        this.headers = builder.headers;
        this.body = builder.body;
    }

    RequestMethod getRequestMethod() {
        return requestMethod;
    }

    String getPath() {
        return path;
    }

    Map<String, String> getHeaders() {
        return headers;
    }

    JSONObject getBody() {
        return body;
    }

    /**
     * Constructor
     */
    static class Builder {

        private final RequestMethod requestMethod;
        private final String path;
        private Map<String, String> headers = new HashMap<>();
        private JSONObject body = new JSONObject();

        Builder(RequestMethod requestMethod, String path) {
            this.requestMethod = requestMethod;
            this.path = path;
        }

        Builder addHeader(String key, String value) {
            headers.put(key, value);
            return this;
        }

        Builder setBody(JSONObject body) {
            this.body = body;
            return this;
        }

        ApiRequest build() {
            return new ApiRequest(this);
        }
    }
}
