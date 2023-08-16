/*
 * GetPendingLinkTask.java
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

import androidx.annotation.Nullable;

import com.usebutton.merchant.module.Features;

/**
 * Class handles getting the pending link from the backend when a user is going from web to native
 * mobile.
 */
final class GetPendingLinkTask extends Task<PostInstallLink> {

    private final ButtonApi buttonApi;
    private final DeviceManager deviceManager;
    private final Features features;
    private final String applicationId;

    GetPendingLinkTask(ButtonApi buttonApi, DeviceManager deviceManager, Features features,
            String applicationId, Listener<PostInstallLink> listener) {
        super(listener);
        this.buttonApi = buttonApi;
        this.deviceManager = deviceManager;
        this.features = features;
        this.applicationId = applicationId;
    }

    @Nullable
    @Override
    PostInstallLink execute() throws Exception {
        String advertisingId = features.getIncludesIfa() ? deviceManager.getAdvertisingId() : null;
        return buttonApi.getPendingLink(applicationId, advertisingId, deviceManager.getSignals());
    }
}
