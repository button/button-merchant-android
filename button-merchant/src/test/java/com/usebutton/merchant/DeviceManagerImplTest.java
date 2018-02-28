/*
 * DeviceManagerImplTest.java
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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.usebutton.merchant.DeviceManagerImpl.ISO_8601;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceManagerImplTest {

    @Mock
    Context context;

    private DeviceManagerImpl deviceManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        deviceManager = new DeviceManagerImpl(context);
    }

    @Test
    public void getScreenSize_hasWindowManager_returnValidSize() {
        WindowManager windowManager = mock(WindowManager.class);
        Display display = mock(Display.class);

        when(context.getSystemService(Context.WINDOW_SERVICE)).thenReturn(windowManager);
        when(windowManager.getDefaultDisplay()).thenReturn(display);

        doAnswer(new Answer<DisplayMetrics>() {
            @Override
            public DisplayMetrics answer(InvocationOnMock invocation) throws Throwable {
                DisplayMetrics displayMetrics = invocation.getArgument(0);
                displayMetrics.widthPixels = 1080;
                displayMetrics.heightPixels = 500;
                return displayMetrics;
            }
        }).when(display).getMetrics(any(DisplayMetrics.class));

        String screenSize = deviceManager.getScreenSize();

        assertEquals("1080x500", screenSize);
    }

    @Test
    public void getScreenSize_doesNotHaveWindowManager_returnUnknown() {
        when(context.getSystemService(Context.WINDOW_SERVICE)).thenReturn(null);

        String screenSize = deviceManager.getScreenSize();

        assertEquals("unknown", screenSize);
    }

    @Test
    public void getSignals_assertRequiredKeys() {
        Map<String, String> signals = deviceManager.getSignals();
        assertTrue(signals.containsKey("timezone"));
        assertTrue(signals.containsKey("os"));
        assertTrue(signals.containsKey("os_version"));
        assertTrue(signals.containsKey("device"));
        assertTrue(signals.containsKey("screen"));
        assertTrue(signals.containsKey("country"));
        assertTrue(signals.containsKey("language"));
    }

    @Test
    public void isOldInstallation_elevenHourInstallation_verifyNotAnOldInstallation()
            throws Exception {
        final long currentTime = System.currentTimeMillis();

        PackageManager packageManager = mock(PackageManager.class);
        PackageInfo packageInfo = mock(PackageInfo.class);
        packageInfo.firstInstallTime = currentTime;

        when(context.getPackageName()).thenReturn("com.usebutton.merchant");
        when(context.getPackageManager()).thenReturn(packageManager);
        when(packageManager.getPackageInfo(eq("com.usebutton.merchant"), anyInt())).thenReturn(
                packageInfo);
        deviceManager.clock = new Getter<Long>() {
            @Override
            public Long get() {
                return currentTime + TimeUnit.HOURS.toMillis(11);
            }
        };

        assertFalse(deviceManager.isOldInstallation());
    }

    @Test
    public void isOldInstallation_thirteenHourInstallation_verifyOldInstallation()
            throws Exception {
        final long currentTime = System.currentTimeMillis();

        PackageManager packageManager = mock(PackageManager.class);
        PackageInfo packageInfo = mock(PackageInfo.class);
        packageInfo.firstInstallTime = currentTime;

        when(context.getPackageName()).thenReturn("com.usebutton.merchant");
        when(context.getPackageManager()).thenReturn(packageManager);
        when(packageManager.getPackageInfo(eq("com.usebutton.merchant"), anyInt())).thenReturn(
                packageInfo);
        deviceManager.clock = new Getter<Long>() {
            @Override
            public Long get() {
                return currentTime + TimeUnit.HOURS.toMillis(13);
            }
        };

        assertTrue(deviceManager.isOldInstallation());
    }

    @Test
    public void isOldInstallation_throwException_verifyNotAnOldInstallation() throws Exception {
        PackageManager packageManager = mock(PackageManager.class);

        when(context.getPackageName()).thenReturn("com.usebutton.merchant");
        when(context.getPackageManager()).thenReturn(packageManager);
        when(packageManager.getPackageInfo(eq("com.usebutton.merchant"), anyInt())).thenThrow(
                PackageManager.NameNotFoundException.class);

        assertFalse(deviceManager.isOldInstallation());
    }

    @Test
    public void getTimeStamp_verifyCorrectDateConversion() {
        final long currentTime = 1523560676528L;
        deviceManager.date = new Getter<Date>() {
            @Override
            public Date get() {
                return new Date(currentTime);
            }
        };

        SimpleDateFormat isoDateFormat = new SimpleDateFormat(ISO_8601, Locale.US);
        assertEquals(deviceManager.getTimeStamp(), isoDateFormat.format(currentTime));
    }
}