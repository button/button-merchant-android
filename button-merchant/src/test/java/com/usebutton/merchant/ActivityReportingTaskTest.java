/*
 * ActivityReportingTaskTest.java
 *
 * Copyright (c) 2020 Button, Inc. (https://usebutton.com)
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

import com.usebutton.merchant.module.Features;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ActivityReportingTaskTest {

    @Mock private ButtonApi buttonApi;
    @Mock private DeviceManager deviceManager;
    @Mock private Features features;
    @Mock private Task.Listener<Void> listener;

    private String activityName = "test-activity";
    private String sourceToken = "test-token";
    private List<ButtonProductCompatible> products = new ArrayList<>();

    private ActivityReportingTask task;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        task = new ActivityReportingTask(buttonApi, deviceManager, features, activityName,
                products, sourceToken, listener);
    }

    @Test
    public void execute_includesIfa_hasAdvertisingId_verifyApiCall() throws Exception {
        when(features.getIncludesIfa()).thenReturn(true);
        String advertisingId = "valid_advertising_id";
        when(deviceManager.getAdvertisingId()).thenReturn(advertisingId);

        task.execute();

        verify(buttonApi).postActivity(activityName, products, sourceToken, "valid_advertising_id");
    }

    @Test
    public void execute_includesIfa_nullAdvertisingId_verifyNullAdvertisingId() throws Exception {
        when(features.getIncludesIfa()).thenReturn(true);
        when(deviceManager.getAdvertisingId()).thenReturn(null);

        task.execute();

        verify(buttonApi).postActivity(eq(activityName), eq(products), eq(sourceToken),
                (String) isNull());
    }

    @Test
    public void execute_doesNotIncludesIfa_verifyNullAdvertisingId() throws Exception {
        when(features.getIncludesIfa()).thenReturn(false);
        when(deviceManager.getAdvertisingId()).thenReturn("");

        task.execute();

        verify(buttonApi).postActivity(eq(activityName), eq(products), eq(sourceToken),
                (String) isNull());
    }
}