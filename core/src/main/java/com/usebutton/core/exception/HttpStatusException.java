/*
 * HttpStatusException.java
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

package com.usebutton.core.exception;

/**
 * Network error with status code encountered when communicating with the Button API
 */
public class HttpStatusException extends ButtonNetworkException {
    private final int statusCode;

    public HttpStatusException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * @return true if status code falls in the range indicating bad requests. (400-499)
     * http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
     */
    public boolean wasBadRequest() {
        return statusCode >= 400 && statusCode < 500;
    }

    /**
     * @return if the exception was due to the server responding with a 401 (Unauthorized).
     * http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
     */
    public boolean wasUnauthorized() {
        return statusCode == 401;
    }

    /**
     * @return if the exception was due to the server responding with a 429 (Rate Limited).
     * http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
     */
    public boolean wasRateLimited() {
        return statusCode == 429;
    }

    /**
     * @return true if status code falls in the range indicating server errors. (500-599)
     * http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
     */
    public boolean wasServerError() {
        return statusCode >= 500 && statusCode < 600;
    }
}
