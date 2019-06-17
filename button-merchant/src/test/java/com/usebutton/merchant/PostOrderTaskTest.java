/*
 * PostOrderTaskTest.java
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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PostOrderTaskTest {

    @Mock
    private ButtonApi buttonApi;

    @Mock
    private DeviceManager deviceManager;

    @Mock
    private Task.Listener listener;

    @Mock
    private Order order;

    private String applicationId = "valid_application_id";
    private String sourceToken = "valid_source_token";

    private PostOrderTask task;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        task = new PostOrderTask(listener, buttonApi, order, applicationId, sourceToken,
                deviceManager);
    }

    @Test
    public void execute_limitAdTrackingFalse_verifyApiCall() throws Exception {
        String advertisingId = "valid_advertising_id";
        when(deviceManager.getAdvertisingId()).thenReturn(advertisingId);
        when(deviceManager.isLimitAdTrackingEnabled()).thenReturn(false);

        task.execute();

        verify(buttonApi).postOrder(order, applicationId, sourceToken, advertisingId);
    }

    @Test
    public void execute_limitAdTrackingTrue_verifyNullAdvertisingId() throws Exception {
        when(deviceManager.isLimitAdTrackingEnabled()).thenReturn(true);

        task.execute();

        verify(buttonApi).postOrder(eq(order), eq(applicationId), eq(sourceToken), (String) isNull());
    }
}