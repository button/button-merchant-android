/*
 * ButtonInternalImpl.java
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

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.usebutton.merchant.exception.ApplicationIdNotFoundException;
import com.usebutton.merchant.module.Features;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ButtonInternalImpl class should implement everything needed for {@link ButtonMerchant}
 * The methods in the ButtonInternalImpl class should never be public because it will only be used
 * by {@link ButtonMerchant}.
 */

final class ButtonInternalImpl implements ButtonInternal {

    private static final String TAG = ButtonMerchant.class.getSimpleName();

    /**
     * A list of {@link ButtonMerchant.AttributionTokenListener}. All listeners will be notified of
     * a token change via the
     * {@link ButtonMerchant.AttributionTokenListener#onAttributionTokenChanged(String)} in the
     * {@link ButtonInternalImpl#setAttributionToken(ButtonRepository, String)} method.
     */
    @VisibleForTesting
    ArrayList<ButtonMerchant.AttributionTokenListener> attributionTokenListeners;

    /**
     * An {@link Executor} that ensures callbacks are run on the main thread.
     *
     * @see MainThreadExecutor
     */
    private final Executor executor;

    /**
     * A thread-safe flag to indicate if we've received a valid source token from a direct deeplink
     */
    private final AtomicBoolean hasReceivedDirectDeeplink = new AtomicBoolean();

    ButtonInternalImpl(Executor executor) {
        this.attributionTokenListeners = new ArrayList<>();
        this.executor = executor;
    }

    public void configure(ButtonRepository buttonRepository, String applicationId) {
        if (!ButtonUtil.isApplicationIdValid(applicationId)) {
            Log.e(TAG, "Application ID [" + applicationId + "] is not valid. "
                    + "You can find your Application ID in the dashboard by logging in at"
                    + " https://app.usebutton.com/");
        }

        buttonRepository.setApplicationId(applicationId);
    }

    @Nullable
    @Override
    public String getApplicationId(ButtonRepository buttonRepository) {
        return buttonRepository.getApplicationId();
    }

    @Override
    public void trackIncomingIntent(ButtonRepository buttonRepository, DeviceManager deviceManager,
            Features features, Intent intent) {
        Uri data = intent.getData();
        if (data == null) {
            return;
        }

        String sourceToken = data.getQueryParameter("btn_ref");
        if (sourceToken != null && !sourceToken.isEmpty()) {
            setAttributionToken(buttonRepository, sourceToken);
            hasReceivedDirectDeeplink.set(true);
        }

        reportDeeplinkOpenEvent(buttonRepository, deviceManager, features, data);
    }

    @Nullable
    @Override
    public String getAttributionToken(ButtonRepository buttonRepository) {
        return buttonRepository.getSourceToken();
    }

    @Override
    public void addAttributionTokenListener(ButtonRepository buttonRepository,
            @NonNull ButtonMerchant.AttributionTokenListener listener) {
        attributionTokenListeners.add(listener);
    }

    @Override
    public void removeAttributionTokenListener(ButtonRepository buttonRepository,
            @NonNull ButtonMerchant.AttributionTokenListener listener) {
        attributionTokenListeners.remove(listener);
    }

    @Override
    public void clearAllData(ButtonRepository buttonRepository) {
        buttonRepository.clear();
    }

    @Override
    public void handlePostInstallIntent(final ButtonRepository buttonRepository,
            final DeviceManager deviceManager, final Features features, final String packageName,
            final PostInstallIntentListener listener) {

        if (buttonRepository.getApplicationId() == null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    listener.onResult(null, new ApplicationIdNotFoundException());
                }
            });
            return;
        }

        if (deviceManager.isOldInstallation() || buttonRepository.checkedDeferredDeepLink()) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    listener.onResult(null, null);
                }
            });
            return;
        }

        buttonRepository.updateCheckDeferredDeepLink(true);
        buttonRepository.getPendingLink(deviceManager, features,
                new Task.Listener<PostInstallLink>() {
                    @Override
                    public void onTaskComplete(@Nullable PostInstallLink postInstallLink) {
                        if (hasReceivedDirectDeeplink.get()) {
                            executor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onResult(null, null);
                                }
                            });
                            return;
                        }

                        if (postInstallLink != null
                                && postInstallLink.isMatch()
                                && postInstallLink.getAction() != null) {
                            final Intent deepLinkIntent =
                                    new Intent(Intent.ACTION_VIEW,
                                            Uri.parse(postInstallLink.getAction()));
                            deepLinkIntent.setPackage(packageName);

                            PostInstallLink.Attribution attribution =
                                    postInstallLink.getAttribution();
                            if (attribution != null) {
                                setAttributionToken(buttonRepository, attribution.getBtnRef());
                            }

                            executor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onResult(deepLinkIntent, null);
                                }
                            });
                            return;
                        }

                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                listener.onResult(null, null);
                            }
                        });
                    }

                    @Override
                    public void onTaskError(final Throwable throwable) {
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                listener.onResult(null, throwable);
                            }
                        });
                    }
                });
    }

    @Override
    public void reportOrder(ButtonRepository buttonRepository, DeviceManager deviceManager,
            Features features, Order order, @Nullable final OrderListener orderListener) {

        if (buttonRepository.getApplicationId() == null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if (orderListener != null) {
                        orderListener.onResult(new ApplicationIdNotFoundException());
                    }
                }
            });
            return;
        }

        buttonRepository.postOrder(order, deviceManager, features, new Task.Listener() {
            @Override
            public void onTaskComplete(@Nullable Object object) {
                if (orderListener != null) {
                    orderListener.onResult(null);
                }
            }

            @Override
            public void onTaskError(Throwable throwable) {
                if (orderListener != null) {
                    orderListener.onResult(throwable);
                }
            }
        });
    }

    /**
     * Sets the attribution token in the {@link ButtonRepository} and updates all
     * {@link ButtonInternalImpl#attributionTokenListeners} if the token has changed.
     *
     * @param buttonRepository {@link ButtonRepository}
     * @param attributionToken The attributionToken.
     */
    private void setAttributionToken(ButtonRepository buttonRepository,
            final String attributionToken) {
        if (attributionToken != null && !attributionToken.isEmpty()) {

            // check if the sourceToken has changed
            if (!attributionToken.equals(getAttributionToken(buttonRepository))) {
                // notify all listeners that the attributionToken has changed
                for (final ButtonMerchant.AttributionTokenListener listener
                        : attributionTokenListeners) {
                    if (listener != null) {
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                listener.onAttributionTokenChanged(attributionToken);
                            }
                        });
                    }
                }
            }

            buttonRepository.setSourceToken(attributionToken);
        }
    }

    /**
     * Report the deeplink open event.
     *
     * This method should generally only be called when a direct deeplink is received. It will
     * report the base URL with *only* Button-associated query parameters.
     *
     * @param link the deeplink URL
     */
    private void reportDeeplinkOpenEvent(ButtonRepository buttonRepository,
            DeviceManager deviceManager, Features features, @Nullable Uri link) {
        if (link == null) {
            return;
        }

        // Strip the deeplink URL, keeping only the base URL and Button-associated query params
        Uri.Builder strippedLinkBuilder = link.buildUpon().clearQuery();
        Set<String> params = link.getQueryParameterNames();
        for (String param : params) {
            if (param.startsWith("btn_")
                    || "from_landing".equalsIgnoreCase(param)
                    || "from_tracking".equalsIgnoreCase(param)) {

                strippedLinkBuilder.appendQueryParameter(param, link.getQueryParameter(param));
            }
        }
        String strippedLink = strippedLinkBuilder.build().toString();

        // Construct deeplink open event
        String sourceToken = getAttributionToken(buttonRepository);
        Event deeplinkEvent = new Event(Event.Name.DEEPLINK_OPENED, sourceToken);
        deeplinkEvent.addProperty(Event.Property.URL, strippedLink);

        // Report event
        buttonRepository.reportEvent(deviceManager, features, deeplinkEvent);
    }
}
