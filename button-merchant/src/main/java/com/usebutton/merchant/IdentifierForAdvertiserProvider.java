/*
 * IdentifierForAdvertiserProvider.java
 *
 * Copyright (c) 2021 Button, Inc. (https://usebutton.com)
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
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

class IdentifierForAdvertiserProvider {

    private static final long IFA_TTL = TimeUnit.HOURS.toMillis(1);
    private final Context context;
    private TtlReference<AdvertisingInfoReflectionProxy> proxyReference;

    /**
     * This class can be used to get Advertising ID from the Google Play Services library via
     * reflection. Will fail silently if not in the classpath, so make sure you have this dependency
     * in your build.gradle:
     * {@code implementation 'com.google.android.gms:play-services-ads:20.0.0}
     */
    IdentifierForAdvertiserProvider(final Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * This call is blocking and should _not_ be run from the main thread.
     * For more info: https://developer.android.com/google/play-services/id.html
     *
     * @return the Advertising ID if available (Google Play Services on classpath and user have not
     * disabled tracking).
     */
    @Nullable
    @WorkerThread
    public String getPrimaryIdentifier() {
        if (isTrackingLimited() || isOnMainThread()) return null;
        return getIdentifierProxy().getTrackingIdentifier(context);
    }

    /**
     * @return true if ad tracking is limited
     */
    public boolean isTrackingLimited() {
        return getIdentifierProxy().isAdTrackingLimited(context);
    }

    private AdvertisingInfoReflectionProxy getIdentifierProxy() {
        if (proxyReference == null || proxyReference.isDead()) {
            proxyReference = new TtlReference<>(new AdvertisingInfoReflectionProxy(), IFA_TTL);
        }
        return proxyReference.get();
    }

    /**
     * Intended to keep a cache of methods and objects accessed via reflection for faster access,
     * but still dynamic enough to get up to date value each time.
     */
    private static class AdvertisingInfoReflectionProxy {

        private static final String CLASS =
                "com.google.android.gms.ads.identifier.AdvertisingIdClient";
        private static final String METHOD_INFO = "getAdvertisingIdInfo";
        private static final String METHOD_TRACKING = "isLimitAdTrackingEnabled";
        private static final String METHOD_IDENTIFIER = "getId";

        private boolean initialized;
        private Method getInfoMethod;
        private Class<?> advertisingClient;

        AdvertisingInfoReflectionProxy() {
            try {
                advertisingClient = Class.forName(CLASS);
                getInfoMethod = advertisingClient.getDeclaredMethod(METHOD_INFO, Context.class);
                initialized = true;
            } catch (Exception e) {
                initialized = false;
            }
        }

        boolean isAdTrackingLimited(final Context context) {
            if (!initialized) return false;
            final Object adInfo;
            try {
                adInfo = getAdInfo(context);
                return (Boolean) adInfo.getClass()
                        .getMethod(METHOD_TRACKING)
                        .invoke(adInfo);
            } catch (Exception e) {
                return false;
            }
        }

        String getTrackingIdentifier(final Context context) {
            if (!initialized) return null;
            final Object adInfo;
            try {
                adInfo = getAdInfo(context);
                return (String) adInfo.getClass()
                        .getMethod(METHOD_IDENTIFIER)
                        .invoke(adInfo);
            } catch (Exception e) {
                return null;
            }
        }

        private Object getAdInfo(final Context context) throws IllegalAccessException,
                InvocationTargetException {
            return getInfoMethod.invoke(advertisingClient, context);
        }
    }

    private static boolean isOnMainThread() {
        return Thread.currentThread() == Looper.getMainLooper().getThread();
    }
}
