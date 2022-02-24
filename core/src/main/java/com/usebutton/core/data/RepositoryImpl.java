/*
 * RepositoryImpl.java
 *
 * Copyright (c) 2022 Button, Inc. (https://usebutton.com)
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

package com.usebutton.core.data;

import android.support.annotation.Nullable;
import android.util.Log;

import com.usebutton.core.ButtonUtil;
import com.usebutton.core.data.models.Event;
import com.usebutton.core.data.tasks.EventReportingTask;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

public class RepositoryImpl implements Repository {

    private static final String TAG = Repository.class.getSimpleName();

    protected final CoreApi coreApi;
    protected final DeviceManager deviceManager;
    protected final ExecutorService executorService;
    protected final PersistentStore persistentStore;
    protected final MemoryStore memoryStore;

    private boolean isConfigured;
    private final List<Task<?>> pendingTasks = new CopyOnWriteArrayList<>();

    protected RepositoryImpl(CoreApi coreApi, DeviceManager deviceManager,
            ExecutorService executorService,
            PersistentStore persistentStore, MemoryStore memoryStore) {
        this.coreApi = coreApi;
        this.deviceManager = deviceManager;
        this.executorService = executorService;
        this.persistentStore = persistentStore;
        this.memoryStore = memoryStore;
    }

    @Override
    public void setApplicationId(String applicationId) {
        // Only cache the application ID if it is valid
        // However, we would still let the network requests proceed instead of failing silently
        // on the client-side.
        if (ButtonUtil.isApplicationIdValid(applicationId)) {
            isConfigured = true;
            memoryStore.setApplicationId(applicationId);

            for (Task<?> task : pendingTasks) {
                executorService.submit(task);
            }
            pendingTasks.clear();
        }
    }

    @Nullable
    @Override
    public String getApplicationId() {
        return memoryStore.getApplicationId();
    }

    @Override
    public void setSourceToken(String sourceToken) {
        persistentStore.setSourceToken(sourceToken);
    }

    @Nullable
    @Override
    public String getSourceToken() {
        return persistentStore.getSourceToken();
    }

    @Override
    public void reportEvent(final Event event) {
        EventReportingTask task = new EventReportingTask(coreApi, deviceManager, memoryStore,
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

        submitTask(task, false);
    }

    @Override
    public void clearAllData() {
        persistentStore.clearAllData();
    }

    @Override
    public void submitTask(Task<?> task, boolean forceSubmit) {
        if (isConfigured || forceSubmit) {
            executorService.submit(task);
        } else {
            Log.d(TAG, "Application ID unavailable! Queueing Task.");
            pendingTasks.add(task);
        }
    }
}
