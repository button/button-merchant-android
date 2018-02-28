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

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GetPendingLinkTaskTest {

    @Mock
    ButtonApi buttonApi;

    @Mock
    DeviceManager deviceManager;

    @Mock
    Task.Listener<PostInstallLink> listener;

    private String applicationId = "valid_application_id";

    private GetPendingLinkTask task;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        task = new GetPendingLinkTask(listener, buttonApi, applicationId, deviceManager);
    }

    @Test
    public void execute_verifyApiCall() throws Exception {
        PostInstallLink postInstallLink = new PostInstallLink(true, "id", null, null);

        Map<String, String> signalsMap = Collections.emptyMap();

        when(deviceManager.getAdvertisingId()).thenReturn("valid_ifa");
        when(deviceManager.getSignals()).thenReturn(signalsMap);
        when(deviceManager.isLimitAdTrackingEnabled()).thenReturn(true);

        when(buttonApi.getPendingLink(applicationId, "valid_ifa", true, signalsMap)).thenReturn(
                postInstallLink);

        assertEquals(postInstallLink, task.execute());
        verify(buttonApi).getPendingLink(applicationId, "valid_ifa",
                true, signalsMap);
    }
}