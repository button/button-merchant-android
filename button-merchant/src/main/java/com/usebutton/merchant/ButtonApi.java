/*
 * ButtonApi.java
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
import androidx.annotation.WorkerThread;

import com.usebutton.merchant.exception.ButtonNetworkException;

import java.util.List;
import java.util.Map;

/**
 * Button API endpoints.
 */
interface ButtonApi {

    void setApplicationId(String applicationId);

    @Nullable
    String getApplicationId();

    @Nullable
    @WorkerThread
    PostInstallLink getPendingLink(String applicationId, @Nullable String advertisingId,
            Map<String, String> signalsMap)
            throws ButtonNetworkException;

    @Nullable
    @WorkerThread
    Void postOrder(Order order, String applicationId, String sourceToken,
            @Nullable String advertisingId) throws ButtonNetworkException;

    @Nullable
    @WorkerThread
    Void postActivity(String activityName, List<ButtonProductCompatible> products,
            @Nullable String sourceToken, @Nullable String advertisingId)
            throws ButtonNetworkException;

    @Nullable
    @WorkerThread
    Void postEvents(List<Event> events, @Nullable String advertisingId)
            throws ButtonNetworkException;
}
