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

import com.usebutton.merchant.exception.ApplicationIdNotFoundException;
import com.usebutton.merchant.module.Features;

import java.util.ArrayList;
import java.util.concurrent.Executor;

/**
 * ButtonInternalImpl class should implement everything needed for {@link ButtonMerchant}
 * The methods in the ButtonInternalImpl class should never be public because it will only be used
 * by {@link ButtonMerchant}.
 */

final class ButtonInternalImpl implements ButtonInternal {

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

    ButtonInternalImpl(Executor executor) {
        this.attributionTokenListeners = new ArrayList<>();
        this.executor = executor;
    }

    public void configure(ButtonRepository buttonRepository, String applicationId) {
        buttonRepository.setApplicationId(applicationId);
    }

    @Nullable
    @Override
    public String getApplicationId(ButtonRepository buttonRepository) {
        return buttonRepository.getApplicationId();
    }

    @Override
    public void trackIncomingIntent(ButtonRepository buttonRepository, Intent intent) {
        Uri data = intent.getData();
        if (data == null) {
            return;
        }

        String sourceToken = data.getQueryParameter("btn_ref");
        setAttributionToken(buttonRepository, sourceToken);
    }

    @Override
    public void trackOrder(ButtonRepository buttonRepository, DeviceManager manager,
            @NonNull Order order, @Nullable final UserActivityListener listener) {
        if (buttonRepository.getApplicationId() == null) {
            if (listener != null) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        listener.onResult(new ApplicationIdNotFoundException());
                    }
                });
            }

            return;
        }

        /// Create internal class that will forward callback to the UserActivityListener
        Task.Listener taskListener = new Task.Listener() {
            @Override
            public void onTaskComplete(@Nullable Object object) {
                if (listener != null) {
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            listener.onResult(null);
                        }
                    });
                }
            }

            @Override
            public void onTaskError(final Throwable throwable) {
                if (listener != null) {
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            listener.onResult(throwable);
                        }
                    });
                }
            }
        };

        buttonRepository.postUserActivity(manager, order, taskListener);
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
            DeviceManager deviceManager, Features features, final String packageName,
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
}
