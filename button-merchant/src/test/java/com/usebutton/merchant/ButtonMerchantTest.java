/*
 * ButtonMerchantTest.java
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

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ButtonMerchantTest {

    @Mock
    private ButtonInternal buttonInternal;

    @Mock
    private Context context;

    @Mock
    private PackageManager packageManager;

    @Mock
    private PackageInfo packageInfo;

    @Mock
    private DisplayMetrics displayMetrics;

    @Mock
    private Resources resources;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ButtonMerchant.buttonInternal = buttonInternal;

        when(context.getApplicationContext()).thenReturn(context);
        when(context.getPackageManager()).thenReturn(packageManager);
        when(packageManager.getPackageInfo(anyString(), anyInt())).thenReturn(packageInfo);
        when(context.getResources()).thenReturn(resources);
        when(resources.getDisplayMetrics()).thenReturn(displayMetrics);
    }

    @Test
    public void configure_verifyButtonInternal() {
        ButtonMerchant.configure(context, "valid_application_id");
        verify(buttonInternal).configure(any(ButtonRepository.class), eq("valid_application_id"));
    }

    @Test
    public void trackIncomingIntent_verifyButtonInternal() {
        ButtonMerchant.trackIncomingIntent(context, mock(Intent.class));
        verify(buttonInternal).trackIncomingIntent(any(ButtonRepository.class), any(Intent.class));
    }

    @Test
    public void trackOrder_verifyButtonInternal() {
        ButtonMerchant.trackOrder(context, mock(Order.class), mock(UserActivityListener.class));
        verify(buttonInternal).trackOrder(any(ButtonRepository.class), any(DeviceManager.class),
                any(Order.class), any(UserActivityListener.class));
    }

    @Test
    public void getAttributionToken_verifyButtonInternal() {
        ButtonMerchant.getAttributionToken(context);
        verify(buttonInternal).getAttributionToken(any(ButtonRepository.class));
    }

    @Test
    public void addAttributionTokenListener_verifyButtonInternal() {
        ButtonMerchant.AttributionTokenListener listener = mock(
                ButtonMerchant.AttributionTokenListener.class);

        ButtonMerchant.addAttributionTokenListener(context, listener);
        verify(buttonInternal).addAttributionTokenListener(any(ButtonRepository.class),
                eq(listener));
    }

    @Test
    public void removeAttributionTokenListener_verifyButtonInternal() {
        ButtonMerchant.removeAttributionTokenListener(context, mock(
                ButtonMerchant.AttributionTokenListener.class));

        verify(buttonInternal).removeAttributionTokenListener(any(ButtonRepository.class), any(
                ButtonMerchant.AttributionTokenListener.class));
    }

    @Test
    public void clearAllData_verifyButtonInternal() {
        ButtonMerchant.clearAllData(context);

        verify(buttonInternal).clearAllData(any(ButtonRepository.class));
    }

    @Test
    public void handlePostInstallIntent_verifyButtonInternal() {
        when(context.getPackageName()).thenReturn("com.usebutton.merchant");
        PostInstallIntentListener postInstallIntentListener = mock(PostInstallIntentListener.class);

        ButtonMerchant.handlePostInstallIntent(context, postInstallIntentListener);

        verify(buttonInternal).handlePostInstallIntent(any(ButtonRepository.class), eq(
                postInstallIntentListener), anyString(), any(DeviceManager.class));
    }
}