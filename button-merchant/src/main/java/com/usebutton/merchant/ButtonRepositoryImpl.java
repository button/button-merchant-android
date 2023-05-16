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

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import android.util.Log;

import com.usebutton.merchant.module.Features;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

/**
 * Class handles retrieving data from memory, api, and disk
 */

final class ButtonRepositoryImpl implements ButtonRepository {

    private static final String TAG = ButtonRepository.class.getSimpleName();

    private final ButtonApi buttonApi;
    private final DeviceManager deviceManager;
    private final Features features;
    private final PersistenceManager persistenceManager;
    private final ExecutorService executorService;

    private static ButtonRepository buttonRepository;
    private boolean isConfigured;
    private List<Task<?>> pendingTasks = new CopyOnWriteArrayList<>();

    static ButtonRepository getInstance(ButtonApi buttonApi, DeviceManager deviceManager,
            Features features, PersistenceManager persistenceManager,
            ExecutorService executorService) {
        if (buttonRepository == null) {
            buttonRepository = new ButtonRepositoryImpl(buttonApi, deviceManager, features,
                    persistenceManager, executorService);
        }

        return buttonRepository;
    }

    @VisibleForTesting
    ButtonRepositoryImpl(ButtonApi buttonApi, DeviceManager deviceManager, Features features,
            PersistenceManager persistenceManager, ExecutorService executorService) {
        this.buttonApi = buttonApi;
        this.deviceManager = deviceManager;
        this.features = features;
        this.persistenceManager = persistenceManager;
        this.executorService = executorService;
    }

    @Override
    public void setApplicationId(String applicationId) {
        isConfigured = true;
        buttonApi.setApplicationId(applicationId);

        for (Task<?> task : pendingTasks) {
            executorService.submit(task);
        }
        pendingTasks.clear();
    }

    @Nullable
    @Override
    public String getApplicationId() {
        return buttonApi.getApplicationId();
    }

    @Override
    public void setSourceToken(String sourceToken) {
        persistenceManager.setSourceToken(sourceToken);
    }

    @Nullable
    @Override
    public String getSourceToken() {
        return persistenceManager.getSourceToken();
    }

    @Override
    public void clear() {
        persistenceManager.clear();
    }

    @Override
    public void getPendingLink(DeviceManager deviceManager, Features features,
            Task.Listener<PostInstallLink> listener) {
        GetPendingLinkTask getPendingLinkTask =
                new GetPendingLinkTask(buttonApi, deviceManager, features, getApplicationId(),
                        listener);

        executorService.submit(getPendingLinkTask);
    }

    @Override
    public boolean checkedDeferredDeepLink() {
        return persistenceManager.checkedDeferredDeepLink();
    }

    @Override
    public void updateCheckDeferredDeepLink(boolean checkedDeferredDeepLink) {
        persistenceManager.updateCheckDeferredDeepLink(checkedDeferredDeepLink);
    }

    @Override
    public void postOrder(Order order, DeviceManager deviceManager, Features features,
            Task.Listener listener) {
        executorService.submit(
                new PostOrderTask(listener, buttonApi, order, getApplicationId(),
                        getSourceToken(), deviceManager, features, new ThreadManager()));
    }

    @Override
    public void trackActivity(final String eventName, List<ButtonProductCompatible> products) {
        ActivityReportingTask task = new ActivityReportingTask(buttonApi, deviceManager, features,
                eventName, products, getSourceToken(), new Task.Listener<Void>() {
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

        invokeIfConfigured(task);
    }

    @Override
    public void reportEvent(DeviceManager deviceManager, Features features, final Event event) {
        EventReportingTask task = new EventReportingTask(buttonApi, deviceManager, features,
                Collections.singletonList(event), new Task.Listener<Void>() {
            @Override
            public void onTaskComplete(@Nullable Void object) {
                // ignored
            }

            @Override
            public void onTaskError(Throwable throwable) {
                Log.e(TAG, String.format("Error reporting event [%s]", event.getName()), throwable);
            }
        });

        invokeIfConfigured(task);
    }

    /**
     * If the Merchant Library has been configured, the provided {@link Task} is submitted
     * immediately. Otherwise, it queued up to be invoked once the Library is configured.
     *
     * @param task the task to submit
     */
    private void invokeIfConfigured(Task<?> task) {
        if (isConfigured) {
            executorService.submit(task);
        } else {
            Log.d(TAG, "Application ID unavailable! Queueing Task.");
            pendingTasks.add(task);
        }
    }
}
