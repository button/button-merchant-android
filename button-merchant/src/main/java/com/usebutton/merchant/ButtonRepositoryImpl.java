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
import android.support.annotation.VisibleForTesting;

import com.usebutton.merchant.module.Features;

import java.util.concurrent.ExecutorService;

/**
 * Class handles retrieving data from memory, api, and disk
 */

final class ButtonRepositoryImpl implements ButtonRepository {

    private final ButtonApi buttonApi;
    private final PersistenceManager persistenceManager;
    private final ExecutorService executorService;

    private String applicationId;

    private static ButtonRepository buttonRepository;

    static ButtonRepository getInstance(ButtonApi buttonApi, PersistenceManager persistenceManager,
            ExecutorService executorService) {
        if (buttonRepository == null) {
            buttonRepository = new ButtonRepositoryImpl(buttonApi, persistenceManager,
                    executorService);
        }

        return buttonRepository;
    }

    @VisibleForTesting
    ButtonRepositoryImpl(ButtonApi buttonApi, PersistenceManager persistenceManager,
            ExecutorService executorService) {
        this.buttonApi = buttonApi;
        this.persistenceManager = persistenceManager;
        this.executorService = executorService;
    }

    @Override
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
        buttonApi.setApplicationId(applicationId);
    }

    @Nullable
    @Override
    public String getApplicationId() {
        return applicationId;
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
    public void postUserActivity(DeviceManager deviceManager, Order order, Task.Listener listener) {
        executorService.submit(
                new UserActivityTask(listener, buttonApi, getApplicationId(), getSourceToken(),
                        deviceManager, order));
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
}
