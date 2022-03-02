/*
 * PostTapRepositoryImpl.java
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

package com.usebutton.posttap.data;

import android.support.annotation.Nullable;

import com.usebutton.core.data.DeviceManager;
import com.usebutton.core.data.MemoryStore;
import com.usebutton.core.data.PersistentStore;
import com.usebutton.core.data.RepositoryImpl;
import com.usebutton.core.data.Task;
import com.usebutton.posttap.data.models.CollectionCampaign;
import com.usebutton.posttap.data.tasks.PhoneEnrollmentTask;
import com.usebutton.posttap.data.tasks.WidgetFetchingTask;

import java.util.TimeZone;
import java.util.concurrent.ExecutorService;

public class PostTapRepositoryImpl extends RepositoryImpl implements PostTapRepository {

    private final PostTapApi api;
    private final CookieJar cookieJar;

    public PostTapRepositoryImpl(PostTapApi api, DeviceManager deviceManager,
            ExecutorService executorService, PersistentStore persistentStore,
            MemoryStore memoryStore, CookieJar cookieJar) {
        super(api, deviceManager, executorService, persistentStore, memoryStore);
        this.api = api;
        this.cookieJar = cookieJar;
    }

    @Override
    public void fetchCollectionCampaign(@Nullable Task.Listener<CollectionCampaign> listener) {
        WidgetFetchingTask task = new WidgetFetchingTask(api, listener);
        submitTask(task, false);
    }

    @Override
    public void enrollPhoneNumber(String phoneNumber, @Nullable Task.Listener<String> listener) {
        PhoneEnrollmentTask task = new PhoneEnrollmentTask(api, "TODO", "TODO", phoneNumber,
                TimeZone.getDefault(), listener);
        submitTask(task, false);
    }

    @Override
    public void setCookie(String key, @Nullable String value) {
        cookieJar.setCookie(key, value);
    }

    @Override
    public void setCookie(String key, @Nullable String value, long maxAgeInSeconds) {
        cookieJar.setCookie(key, value, maxAgeInSeconds);
    }

    @Nullable
    @Override
    public String getCookie(String key) {
        return cookieJar.getCookie(key);
    }

    @Override
    public String getAllCookies() {
        return cookieJar.getCookieString();
    }
}
