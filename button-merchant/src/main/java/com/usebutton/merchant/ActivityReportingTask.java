/*
 * ActivityReportingTask.java
 *
 * Copyright (c) 2020 Button, Inc. (https://usebutton.com)
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

import com.usebutton.core.data.DeviceManager;
import com.usebutton.core.data.MemoryStore;
import com.usebutton.core.data.Task;

import java.util.List;

/**
 * Asynchronous task used to report user activity to Button.
 */
public class ActivityReportingTask extends Task<Void> {

    private final ButtonApi buttonApi;
    private final DeviceManager deviceManager;
    private final MemoryStore memoryStore;
    private final String activityName;
    private final List<ButtonProductCompatible> products;
    private final String sourceToken;

    public ActivityReportingTask(ButtonApi buttonApi, DeviceManager deviceManager,
            MemoryStore memoryStore, String activityName, List<ButtonProductCompatible> products,
            @Nullable String sourceToken, @Nullable Listener<Void> listener) {
        super(listener);
        this.buttonApi = buttonApi;
        this.deviceManager = deviceManager;
        this.memoryStore = memoryStore;
        this.activityName = activityName;
        this.products = products;
        this.sourceToken = sourceToken;
    }

    @Nullable
    @Override
    protected Void execute() throws Exception {
        String ifa = memoryStore.getIncludesIfa() ? deviceManager.getAdvertisingId() : null;
        return buttonApi.postActivity(activityName, products, sourceToken, ifa);
    }
}
