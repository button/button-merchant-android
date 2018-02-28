/*
 * DeviceManagerImpl.java
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
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.WorkerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Class handles retrieving device data
 */

final class DeviceManagerImpl implements DeviceManager {

    private static final String TAG = DeviceManagerImpl.class.getSimpleName();

    static final String ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZ";

    private final Context context;

    private static DeviceManager deviceManager;

    @VisibleForTesting
    Getter<Long> clock = new Getter<Long>() {
        @Override
        public Long get() {
            return System.currentTimeMillis();
        }
    };

    @VisibleForTesting
    Getter<Date> date = new Getter<Date>() {
        @Override
        public Date get() {
            return new Date();
        }
    };

    static DeviceManager getInstance(Context context) {
        if (deviceManager == null) {
            deviceManager = new DeviceManagerImpl(context);
        }

        return deviceManager;
    }

    @VisibleForTesting
    DeviceManagerImpl(Context context) {
        this.context = context;
    }

    @WorkerThread
    @Nullable
    @Override
    public String getAdvertisingId() {
        try {
            return AdvertisingIdClient.getAdvertisingIdInfo(context).getId();
        } catch (IOException e) {
            Log.e(TAG, "Error has occurred", e);
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.e(TAG, "Error has occurred", e);
        } catch (GooglePlayServicesRepairableException e) {
            Log.e(TAG, "Error has occurred", e);
        }

        return null;
    }

    @WorkerThread
    @Override
    public boolean isLimitAdTrackingEnabled() {
        try {
            return AdvertisingIdClient.getAdvertisingIdInfo(context).isLimitAdTrackingEnabled();
        } catch (IOException e) {
            Log.e(TAG, "Error has occurred", e);
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.e(TAG, "Error has occurred", e);
        } catch (GooglePlayServicesRepairableException e) {
            Log.e(TAG, "Error has occurred", e);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Error has occurred", e);
        }

        return false;
    }

    @VisibleForTesting
    String getScreenSize() {
        WindowManager windowManager =
                (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            Display display = windowManager.getDefaultDisplay();
            final DisplayMetrics out = new DisplayMetrics();
            display.getMetrics(out);
            return String.format(Locale.US, "%dx%d", out.widthPixels, out.heightPixels);
        }

        return "unknown";
    }

    @Override
    public Map<String, String> getSignals() {
        Map<String, String> signalsMap = new HashMap<>();
        signalsMap.put("timezone", Calendar.getInstance().getTimeZone().getID());
        signalsMap.put("os", "android");
        signalsMap.put("os_version", Build.VERSION.RELEASE);
        signalsMap.put("device", String.format("%s %s", Build.MANUFACTURER, Build.MODEL));
        signalsMap.put("screen", getScreenSize());
        final Locale locale = Locale.getDefault();
        signalsMap.put("country", locale.getCountry());
        signalsMap.put("language", locale.getLanguage());

        // TODO: Add Install Referrer
        // signalsJson.put("install_referrer", "");

        return signalsMap;
    }

    @Override
    public String getTimeStamp() {
        return new SimpleDateFormat(ISO_8601, Locale.US).format(date.get());
    }

    @Override
    public boolean isOldInstallation() {
        try {
            String packageName = context.getPackageName();
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            if (packageInfo != null
                    && (packageInfo.firstInstallTime + TimeUnit.HOURS.toMillis(12)) < clock.get()) {
                // More than 12 hours since we were installed
                return true;
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        return false;
    }
}
