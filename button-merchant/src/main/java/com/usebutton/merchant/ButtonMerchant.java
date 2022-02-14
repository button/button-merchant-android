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

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.usebutton.core.data.MainThreadExecutor;
import com.usebutton.merchant.module.ButtonUserActivity;
import com.usebutton.merchant.module.Features;

import java.util.concurrent.Executor;

/**
 * Primary entry point for the Button merchant library.
 */
public final class ButtonMerchant {

    private ButtonMerchant() {

    }

    private static final Executor executor = new MainThreadExecutor();
    @VisibleForTesting
    static ButtonInternal buttonInternal = new ButtonInternalImpl(executor);
    @VisibleForTesting
    static ButtonUserActivity activity = ButtonUserActivityImpl.getInstance();

    static final String BASE_URL = "https://mobileapi.usebutton.com";
    static final String FMT_BASE_URL_APP_ID = "https://%s.mobileapi.usebutton.com";
    private static final DependencyManager dependencyManager = DependencyManager.getInstance();

    /**
     * Configures {@link ButtonMerchant} with your application Id.
     * Note: Get your application Id from from the [Button Dashboard](https://app.usebutton.com)
     *
     * @param context {@code Context}
     * @param applicationId Your applicationId (required)
     */
    public static void configure(@NonNull Context context, @NonNull String applicationId) {
        dependencyManager.updateApplicationContext((Application) context.getApplicationContext());
        buttonInternal.configure(dependencyManager.getButtonRepository(),
                dependencyManager.getConnectionManager(), applicationId);
        ((ButtonUserActivityImpl) activity()).flushQueue(dependencyManager.getButtonRepository());
    }

    /**
     * Checks the passed {@code Intent} for a Button attribution and if present stores the token.
     *
     * @param context a {@link Context) instance that can be used to access app resources like
     * SharedPreferences.
     * @param intent An intent that has entered your app from a third party source.
     */
    public static void trackIncomingIntent(@NonNull Context context, @NonNull Intent intent) {
        TestManager testManager = new TestManager(context, dependencyManager.getButtonRepository(),
                new TestManager.Terminator());
        dependencyManager.updateApplicationContext((Application) context.getApplicationContext());
        buttonInternal.trackIncomingIntent(testManager, dependencyManager.getButtonRepository(),
                dependencyManager.getDeviceManager(), features(), intent);
    }

    /**
     * Tracks orders
     *
     * @param order {@link Order}
     * @param userActivityListener {@link UserActivityListener}
     *
     * @deprecated This method is deprecated and will be removed in a future version.
     * It is safe to remove your usage of this method.
     */
    @Deprecated
    public static void trackOrder(@NonNull Context context, @NonNull Order order,
            @Nullable UserActivityListener userActivityListener) {
        if (userActivityListener != null) {
            Throwable error = new Throwable("trackOrder(~) is no longer supported."
                    + " You can safely remove your usage of this method.");
            userActivityListener.onResult(error);
        }
    }

    /**
     * Reports an order to Button
     *
     * @param context a {@link Context) instance that can be used to access app resources like
     * SharedPreferences.
     * @param order Your {@link Order} object to be reported
     * @param orderListener A nullable {@link OrderListener} interface
     *
     * @see <a href="https://developer.usebutton.com/guides/merchants/android/report-orders-to-
button#report-orders-to-buttons-order-api">Reporting Orders to Button</a>
     */
    public static void reportOrder(@NonNull Context context, @NonNull Order order,
            @Nullable OrderListener orderListener) {
        dependencyManager.updateApplicationContext((Application) context.getApplicationContext());
        buttonInternal.reportOrder(
                dependencyManager.getButtonRepository(),
                dependencyManager.getDeviceManager(),
                FeaturesImpl.getInstance(dependencyManager.getMemoryStore()),
                order,
                orderListener
        );
    }

    /**
     * The {@code attributionToken} from the last inbound Button attributed {@link Intent}.
     *
     * For attribution to work correctly, you must:
     * <ul>
     * <li>Always access this token directly—*never cache it*.</li>
     * <li>Never manage the lifecycle of this token—Button manages the token validity window
     * server-side.</li>
     * <li>Always include this value when reporting orders to your order API.</li>
     * </ul>
     *
     * @return the last tracked Button attribution token.
     **/
    @Nullable
    public static String getAttributionToken(@NonNull Context context) {
        dependencyManager.updateApplicationContext((Application) context.getApplicationContext());
        return buttonInternal.getAttributionToken(dependencyManager.getButtonRepository());
    }

    /**
     * Registers attribution token listener.
     *
     * @param context {@link Context}
     * @param listener {@link AttributionTokenListener}
     */
    public static void addAttributionTokenListener(@NonNull Context context,
            @NonNull AttributionTokenListener listener) {
        dependencyManager.updateApplicationContext((Application) context.getApplicationContext());
        buttonInternal.addAttributionTokenListener(dependencyManager.getButtonRepository(),
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
        dependencyManager.updateApplicationContext((Application) context.getApplicationContext());
        buttonInternal.removeAttributionTokenListener(dependencyManager.getButtonRepository(),
                listener);
    }

    /**
     * Discards the current session and all persisted data.
     */
    public static void clearAllData(@NonNull Context context) {
        dependencyManager.updateApplicationContext((Application) context.getApplicationContext());
        buttonInternal.clearAllData(dependencyManager.getButtonRepository());
    }

    /**
     * Checks to see if the user visited a url prior to installing your app.
     *
     * If a url is found, {@link PostInstallIntentListener#onResult(Intent, Throwable)} will be
     * called on the listener you passed and you are responsible for navigating the user to the
     * relevant content in your app. If a url is not found or an error occurs,
     * {@link PostInstallIntentListener#onResult(Intent, Throwable)} will be called on your
     * listener and you can continue with your normal launch sequence.
     *
     * This method checks for a post-install url exactly *one time* after a user has installed your
     * app. Subsequent calls will result in a call to
     * {@link PostInstallIntentListener#onResult(Intent, Throwable)} on your listener. You do
     * not need to wait for the listener before continuing with your normal launch sequence but you
     * should be prepared to handle an intent if a post-install url is found.
     *
     * @param context context
     * @param listener The listener for be notified when a post install url is found.
     */
    public static void handlePostInstallIntent(@NonNull Context context, @NonNull
            PostInstallIntentListener listener) {
        dependencyManager.updateApplicationContext((Application) context.getApplicationContext());
        buttonInternal.handlePostInstallIntent(
                dependencyManager.getButtonRepository(),
                dependencyManager.getDeviceManager(),
                FeaturesImpl.getInstance(dependencyManager.getMemoryStore()),
                context.getPackageName(),
                listener
        );
    }

    /**
     * An interface through which library features can be enabled/disabled.
     *
     * @return Button features API
     */
    public static Features features() {
        return FeaturesImpl.getInstance(dependencyManager.getMemoryStore());
    }

    /**
     * An interface through which user activities can be reported.
     *
     * @return Button user activity API
     */
    public static ButtonUserActivity activity() {
        return activity;
    }

    /**
     * An interface to receive callbacks each time the attribution token value has been updated.
     */
    public interface AttributionTokenListener {
        void onAttributionTokenChanged(@NonNull String token);
    }
}
