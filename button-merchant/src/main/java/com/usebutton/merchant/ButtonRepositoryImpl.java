/*
 * ButtonRepositoryImpl.java
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

import android.support.annotation.Nullable;
import android.util.Log;

import com.usebutton.core.data.DeviceManager;
import com.usebutton.core.data.MemoryStore;
import com.usebutton.core.data.PersistentStore;
import com.usebutton.core.data.RepositoryImpl;
import com.usebutton.core.data.Task;
import com.usebutton.core.data.ThreadManager;
import com.usebutton.merchant.module.Features;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Class handles retrieving data from memory, api, and disk
 */

final class ButtonRepositoryImpl extends RepositoryImpl implements ButtonRepository {

    private static final String TAG = ButtonRepositoryImpl.class.getSimpleName();

    private final ButtonApi buttonApi;

    ButtonRepositoryImpl(ButtonApi buttonApi, DeviceManager deviceManager,
            ExecutorService executorService, PersistentStore persistentStore,
            MemoryStore memoryStore) {
        super(buttonApi, deviceManager, executorService, persistentStore, memoryStore);
        this.buttonApi = buttonApi;
    }

    @Override
    public void getPendingLink(DeviceManager deviceManager, Features features,
            Task.Listener<PostInstallLink> listener) {
        GetPendingLinkTask getPendingLinkTask = new GetPendingLinkTask(buttonApi, deviceManager,
                memoryStore, getApplicationId(), listener);
        submitTask(getPendingLinkTask, true);
    }

    @Override
    public boolean checkedDeferredDeepLink() {
        return persistentStore.checkedDeferredDeepLink();
    }

    @Override
    public void updateCheckDeferredDeepLink(boolean checkedDeferredDeepLink) {
        persistentStore.updateCheckDeferredDeepLink(checkedDeferredDeepLink);
    }

    @Override
    public void postOrder(Order order, DeviceManager deviceManager, Features features,
            Task.Listener listener) {
        PostOrderTask task = new PostOrderTask(listener, buttonApi, order, getApplicationId(),
                getSourceToken(), deviceManager, memoryStore, new ThreadManager());
        submitTask(task, true);
    }

    @Override
    public void trackActivity(final String eventName, List<ButtonProductCompatible> products) {
        ActivityReportingTask task = new ActivityReportingTask(buttonApi, deviceManager,
                memoryStore, eventName, products, getSourceToken(), new Task.Listener<Void>() {
            @Override
            public void onTaskComplete(@Nullable Void object) {
                // ignored
            }

            @Override
            public void onTaskError(Throwable throwable) {
                Log.e(TAG, String.format("Error reporting user activity [%s]", eventName),
                        throwable);
            }
        });

        submitTask(task, false);
    }

}
