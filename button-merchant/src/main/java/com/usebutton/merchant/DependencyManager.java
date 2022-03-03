/*
 * DependencyManager.java
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

package com.usebutton.merchant;

import android.app.Application;

import com.usebutton.core.data.ConnectionManager;
import com.usebutton.core.data.ConnectionManagerImpl;
import com.usebutton.core.data.DeviceManager;
import com.usebutton.core.data.DeviceManagerImpl;
import com.usebutton.core.data.IdentifierForAdvertiserProvider;
import com.usebutton.core.data.MemoryStore;
import com.usebutton.core.data.MemoryStoreImpl;
import com.usebutton.core.data.PersistentStore;
import com.usebutton.core.data.PersistentStoreImpl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class DependencyManager {

    private Application application;
    private ExecutorService executorService;
    private PersistentStore persistentStore;
    private DeviceManager deviceManager;
    private ConnectionManager connectionManager;
    private ButtonApi buttonApi;
    private ButtonRepository buttonRepository;

    private static DependencyManager instance;

    public static DependencyManager getInstance() {
        if (instance == null) {
            instance = new DependencyManager();
        }
        return instance;
    }

    void updateApplicationContext(Application application) {
        this.application = application;
    }

    ExecutorService getExecutorService() {
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }
        return executorService;
    }

    MemoryStore getMemoryStore() {
        return MemoryStoreImpl.getInstance();
    }

    PersistentStore getPersistentStore() {
        if (persistentStore == null) {
            persistentStore = new PersistentStoreImpl(application);
        }
        return persistentStore;
    }

    DeviceManager getDeviceManager() {
        if (deviceManager == null) {
            deviceManager = new DeviceManagerImpl(
                    application,
                    new IdentifierForAdvertiserProvider(application)
            );
        }
        return deviceManager;
    }

    ConnectionManager getConnectionManager() {
        if (connectionManager == null) {
            connectionManager = new ConnectionManagerImpl(
                    ButtonMerchant.BASE_URL,
                    getDeviceManager().getUserAgent(),
                    getPersistentStore(),
                    getMemoryStore()
            );
        }
        return connectionManager;
    }

    ButtonApi getButtonApi() {
        if (buttonApi == null) {
            buttonApi = new ButtonApiImpl(getConnectionManager());
        }
        return buttonApi;
    }

    ButtonRepository getButtonRepository() {
        if (buttonRepository == null) {
            buttonRepository = new ButtonRepositoryImpl(
                    getButtonApi(),
                    getDeviceManager(),
                    getExecutorService(),
                    getPersistentStore(),
                    getMemoryStore()
            );
        }
        return buttonRepository;
    }
}
