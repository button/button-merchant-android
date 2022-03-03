/*
 * MemoryStoreImpl.java
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
import android.support.annotation.VisibleForTesting;

/**
 * Internal memory store. This class should NOT be subclassed and should instead be shared
 * between dependencies.
 */
public final class MemoryStoreImpl implements MemoryStore {

    private String applicationId;
    private boolean includesIfa = true;

    private static MemoryStore instance;

    public static MemoryStore getInstance() {
        if (instance == null) {
            synchronized (MemoryStore.class) {
                if (instance == null) {
                    instance = new MemoryStoreImpl();
                }
            }
        }
        return instance;
    }

    @VisibleForTesting
    MemoryStoreImpl() {
    }

    @Override
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    @Nullable
    @Override
    public String getApplicationId() {
        return applicationId;
    }

    @Override
    public void setIncludesIfa(boolean includesIfa) {
        this.includesIfa = includesIfa;
    }

    @Override
    public boolean getIncludesIfa() {
        return includesIfa;
    }

    @Override
    public void clearAllData() {
        applicationId = null;
        includesIfa = true;
    }
}
