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
            return isLimitAdTrackingEnabled() ? null
                    : AdvertisingIdClient.getAdvertisingIdInfo(context).getId();
        } catch (IOException e) {
            Log.e(TAG, "Error has occurred", e);
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.e(TAG, "Error has occurred", e);
        } catch (GooglePlayServicesRepairableException e) {
            Log.e(TAG, "Error has occurred", e);
        }

        return null;
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
        return ButtonUtil.formatDate(date.get());
    }

    @Override
    public boolean isOldInstallation() {
        PackageInfo packageInfo = getPackageInfo();

        return packageInfo != null
                && (packageInfo.firstInstallTime + TimeUnit.HOURS.toMillis(12)) < clock.get();
    }

    @Override
    public String getUserAgent() {
        // $App/$Version ($OS $OSVersion; $HardwareType; $MerchantId/$MerchantVersion;
        // Scale/$screenScale; )

        // "com.usebutton.merchant/1.0.1+12 "
        final StringBuilder sb = new StringBuilder();
        sb.append("com.usebutton.merchant/");
        sb.append(getSdkVersionName());
        sb.append('+');
        sb.append(getSdkVersionCode());
        sb.append(' ');

        // "(Android 4.0.1; "
        sb.append("(Android ");
        sb.append(getAndroidVersionName());
        sb.append("; ");

        // "Samsung Galaxy S5; "
        sb.append(getDeviceManufacturer());
        sb.append(' ');
        sb.append(getDeviceModel());
        sb.append("; ");

        // "com.example.wheelsapp/1.2.3+41; "
        sb.append(getPackageName());
        sb.append('/');
        sb.append(getVersionName());
        sb.append('+');
        sb.append(getVersionCode());
        sb.append("; ");

        // "Scale/2.0;
        sb.append(String.format(Locale.US, "Scale/%.1f; ", getScreenDensity()));

        // en_us)
        final Locale locale = getLocale();
        sb.append(locale.getLanguage()).append('_').append(locale.getCountry().toLowerCase())
                .append(')');

        return sb.toString();
    }

    /**
     * @return true is ad tracking is limited
     */
    @WorkerThread
    private boolean isLimitAdTrackingEnabled()
            throws GooglePlayServicesNotAvailableException, IOException,
            GooglePlayServicesRepairableException {
        return AdvertisingIdClient.getAdvertisingIdInfo(context).isLimitAdTrackingEnabled();
    }

    private String getSdkVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    private int getSdkVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

    private String getAndroidVersionName() {
        return Build.VERSION.RELEASE;
    }

    private String getDeviceManufacturer() {
        return Build.MANUFACTURER;
    }

    private String getDeviceModel() {
        return Build.MODEL;
    }

    @Nullable
    private String getPackageName() {
        PackageInfo packageInfo = getPackageInfo();
        return packageInfo != null ? packageInfo.packageName : null;
    }

    @Nullable
    private String getVersionName() {
        PackageInfo packageInfo = getPackageInfo();
        return packageInfo != null ? packageInfo.versionName : null;
    }

    private int getVersionCode() {
        PackageInfo packageInfo = getPackageInfo();
        return packageInfo != null ? packageInfo.versionCode : -1;
    }

    private float getScreenDensity() {
        return context.getResources().getDisplayMetrics().density;
    }

    private Locale getLocale() {
        return Locale.getDefault();
    }

    @Nullable
    private PackageInfo getPackageInfo() {
        try {
            String packageName = context.getPackageName();
            PackageManager packageManager = context.getPackageManager();
            return packageManager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException ignored) {

        }

        return null;
    }
}
