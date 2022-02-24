/*
 * HttpStatusExceptionTest.java
 *
 * Copyright (c) 2022 Button, Inc. (https://usebutton.com)
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

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class HttpStatusExceptionTest {

    @Test
    public void wasBadRequest_verify() {
        for (int statusCode = 400; statusCode < 500; statusCode++) {
            HttpStatusException httpStatusException = new HttpStatusException("", statusCode);
            assertTrue(httpStatusException.wasBadRequest());
        }
    }

    @Test
    public void wasUnauthorized_verify() {
        HttpStatusException httpStatusException = new HttpStatusException("", 401);
        assertTrue(httpStatusException.wasUnauthorized());
    }

    @Test
    public void wasRateLimited_verify() {
        HttpStatusException httpStatusException = new HttpStatusException("", 429);
        assertTrue(httpStatusException.wasRateLimited());
    }

    @Test
    public void wasServerError_verify() {
        for (int statusCode = 500; statusCode < 600; statusCode++) {
            HttpStatusException httpStatusException = new HttpStatusException("", statusCode);
            assertTrue(httpStatusException.wasServerError());
        }
    }
}