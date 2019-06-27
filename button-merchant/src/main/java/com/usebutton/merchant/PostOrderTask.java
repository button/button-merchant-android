/*
 * PostOrderTask.java
 *
 * Copyright (c) 2019 Button, Inc. (https://usebutton.com)
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

import android.support.annotation.Nullable;

import com.usebutton.merchant.exception.ButtonNetworkException;
import com.usebutton.merchant.exception.HttpStatusException;
import com.usebutton.merchant.exception.NetworkNotFoundException;

/**
 * Asynchronous task used to report order to the Button API.
 */
class PostOrderTask extends Task {

    private final ButtonApi buttonApi;
    private final String applicationId;
    private final String sourceToken;
    private final Order order;
    private final DeviceManager deviceManager;

    private Getter<Double> retryDelayInterval = new Getter<Double>() {
        @Override
        public Double get() {
            return Math.pow(2, retryCount) * 100;
        }
    };

    static final int MAX_RETRIES = 4;

    private int retryCount = 0;

    PostOrderTask(@Nullable Listener listener, ButtonApi buttonApi, Order order,
            String applicationId, String sourceToken, DeviceManager deviceManager) {
        super(listener);
        this.buttonApi = buttonApi;
        this.order = order;
        this.applicationId = applicationId;
        this.sourceToken = sourceToken;
        this.deviceManager = deviceManager;
    }

    @Nullable
    @Override
    Void execute() throws Exception {
        String advertisingId = deviceManager.isLimitAdTrackingEnabled()
                ? null : deviceManager.getAdvertisingId();

        // loop and execute postOrder until max retries is met or non case exception is met
        while (true) {
            try {
                return buttonApi.postOrder(order, applicationId, sourceToken, advertisingId);
            } catch (ButtonNetworkException exception) {
                if (!shouldRetry(exception)) {
                    throw exception;
                }

                retryCount++;
            }
        }
    }

    /**
     * @param e exception thrown by api request
     * @return true if should retry
     */
    private boolean shouldRetry(ButtonNetworkException e) throws InterruptedException {
        if (retryCount >= MAX_RETRIES) {
            return false;
        }

        double delay = retryDelayInterval.get();
        if (e instanceof HttpStatusException) {
            HttpStatusException httpStatusException =
                    (HttpStatusException) e;
            if (httpStatusException.wasRateLimited() || httpStatusException.wasServerError()) {
                Thread.sleep((long) delay);
                return true;
            }
        }

        if (e instanceof NetworkNotFoundException) {
            Thread.sleep((long) delay);
            return true;
        }

        return false;
    }
}
