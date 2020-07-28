/*
 * ButtonInternal.java
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.usebutton.merchant.module.Features;

/**
 * Internal implementation of the {@link ButtonMerchant} interface.
 */
interface ButtonInternal {

    void configure(ButtonRepository buttonRepository, String applicationId);

    @Nullable
    String getApplicationId(ButtonRepository buttonRepository);

    void trackIncomingIntent(ButtonRepository buttonRepository, DeviceManager deviceManager,
            Features features, Intent intent);

    @Nullable
    String getAttributionToken(ButtonRepository buttonRepository);

    void addAttributionTokenListener(ButtonRepository buttonRepository, @NonNull
            ButtonMerchant.AttributionTokenListener listener);

    void removeAttributionTokenListener(ButtonRepository buttonRepository, @NonNull
            ButtonMerchant.AttributionTokenListener listener);

    void clearAllData(ButtonRepository buttonRepository);

    void handlePostInstallIntent(ButtonRepository buttonRepository, DeviceManager deviceManager,
            Features features, String packageName, PostInstallIntentListener listener);

    void reportOrder(ButtonRepository buttonRepository, DeviceManager deviceManager,
            Features features, Order order, @Nullable OrderListener orderListener);
}
