/*
 * GetPendingLinkTaskTest.java
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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserActivityTaskTest {

    @Mock
    ButtonApi buttonApi;

    @Mock
    DeviceManager deviceManager;

    @Mock
    Task.Listener listener;

    private String applicationId = "valid_application_id";
    private String sourceToken = "valid_source_token";
    private Order order = new Order.Builder("123").build();

    private UserActivityTask task;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        task = new UserActivityTask(listener, buttonApi, applicationId, sourceToken, deviceManager,
                order);
    }

    @Test
    public void execute_verifyApiCall() throws Exception {
        when(deviceManager.getTimeStamp()).thenReturn("valid_ts");

        task.execute();
        verify(buttonApi).postActivity(applicationId, sourceToken, "valid_ts", order);
    }
}