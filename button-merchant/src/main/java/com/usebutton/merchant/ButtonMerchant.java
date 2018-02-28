/*
 * ButtonMerchant.java
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Primary entry point for the Button merchant library.
 */
public final class ButtonMerchant {

    private ButtonMerchant() {

    }

    @VisibleForTesting
    static ButtonInternal buttonInternal = new ButtonInternalImpl();

    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Configures {@link ButtonMerchant} with your application Id.
     * Note: Get your application Id from from the [Button Dashboard](https://app.usebutton.com)
     *
     * @param context {@code Context}
     * @param applicationId Your applicationId (required)
     */
    public static void configure(@NonNull Context context, @NonNull String applicationId) {
        buttonInternal.configure(getButtonRepository(context), applicationId);
    }

    /**
     * Checks the passed {@code Intent} for a Button attribution and if present stores the token.
     *
     * @param context a {@link Context) instance that can be used to access app resources like SharedPreferences.
     * @param intent An intent that has entered your app from a third party source.
     */
    public static void trackIncomingIntent(@NonNull Context context, @NonNull Intent intent) {
        buttonInternal.trackIncomingIntent(getButtonRepository(context), intent);
    }

    /**
     * Tracks orders
     *
     * @param order {@link Order}
     * @param userActivityListener {@link UserActivityListener}
     */
    public static void trackOrder(@NonNull Context context, @NonNull Order order,
            @Nullable UserActivityListener userActivityListener) {
        buttonInternal.trackOrder(getButtonRepository(context), getDeviceManager(context), order,
                userActivityListener);
    }

    /**
     * The `attributionToken` from the last inbound Button attributed {@link Intent}.
     *
     * For attribution to work correctly, you must:
     * -Always access this token directly—*never cache it*.
     * -Never manage the lifecycle of this token—Button manages the token validity window server-side.
     * -Always include this value when reporting orders to your order API
     *
     * @return the last tracked Button attribution token.
     **/
    @Nullable
    public static String getAttributionToken(@NonNull Context context) {
        return buttonInternal.getAttributionToken(getButtonRepository(context));
    }

    /**
     * Registers attribution token listener.
     *
     * @param context {@link Context}
     * @param listener {@link AttributionTokenListener}
     */
    public static void addAttributionTokenListener(@NonNull Context context,
            @NonNull AttributionTokenListener listener) {
        buttonInternal.addAttributionTokenListener(getButtonRepository(context),
                listener);
    }

    /**
     * Unregisters attribution token listener.
     *
     * @param context {@link Context}
     * @param listener {@link AttributionTokenListener}
     */
    public static void removeAttributionTokenListener(@NonNull Context context,
            @NonNull AttributionTokenListener listener) {
        buttonInternal.removeAttributionTokenListener(getButtonRepository(context),
                listener);
    }

    /**
     * Discards the current session and all persisted data.
     */
    public static void clearAllData(@NonNull Context context) {
        buttonInternal.clearAllData(getButtonRepository(context));
    }

    /**
     * Checks to see if the user visited a url prior to installing your app.
     *
     * If a url is found, {@link PostInstallIntentListener#onPostInstallIntent(Intent)} will be called on the listener you passed and you
     * are responsible for navigating the user to the relevant content in your app. If a url is not found
     * or an error occurs, {@link PostInstallIntentListener#onNoPostInstallIntent(Throwable)}  will be called on your listener and you can continue
     * with your normal launch sequence.
     *
     * This method checks for a post-install url exactly *one time* after a user has installed your app.
     * Subsequent calls will result in a call to {@link PostInstallIntentListener#onNoPostInstallIntent(Throwable)} on your listener. You do not need to wait
     * for the listener before continuing with your normal launch sequence but you should be prepared to handle
     * an intent if a post-install url is found.
     *
     * @param context context
     * @param listener The listener for be notified when a post install url is found.
     */
    public static void handlePostInstallIntent(@NonNull Context context, @NonNull
            PostInstallIntentListener listener) {
        buttonInternal.handlePostInstallIntent(getButtonRepository(context), listener,
                context.getPackageName(), getDeviceManager(context));
    }

    private static ButtonRepository getButtonRepository(Context context) {
        PersistenceManager persistenceManager =
                PersistenceManagerImpl.getInstance(context.getApplicationContext());
        ButtonApi buttonApi = ButtonApiImpl.getInstance();

        return ButtonRepositoryImpl.getInstance(buttonApi, persistenceManager, executorService);
    }

    private static DeviceManager getDeviceManager(Context context) {
        return DeviceManagerImpl.getInstance(context.getApplicationContext());
    }

    /**
     * An interface to receive callbacks each time the attribution token value has been updated.
     */
    public interface AttributionTokenListener {
        void onAttributionTokenChanged(@NonNull String token);
    }
}
