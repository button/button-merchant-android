/*
 * PostTapRepositoryImplTest.java
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

import com.usebutton.core.data.DeviceManager;
import com.usebutton.core.data.MemoryStore;
import com.usebutton.core.data.MemoryStoreImpl;
import com.usebutton.core.data.PersistentStore;
import com.usebutton.core.data.Task;
import com.usebutton.posttap.data.tasks.PhoneEnrollmentTask;
import com.usebutton.posttap.data.tasks.WidgetFetchingTask;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class PostTapRepositoryImplTest {

    @Mock PostTapApi api;
    @Mock DeviceManager deviceManager;
    @Mock PersistentStore persistentStore;
    @Mock ExecutorService executorService;
    @Mock CookieJar cookieJar;
    private final MemoryStore memoryStore = MemoryStoreImpl.getInstance();

    private PostTapRepository repository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        memoryStore.clearAllData();
        repository = new PostTapRepositoryImpl(api, deviceManager, executorService, persistentStore,
                memoryStore, cookieJar);
        repository.setApplicationId("app-xxxxxxxxxxxxxxxx");
    }

    @Test
    public void fetchCollectionCampaign_submitsTask() {
        repository.fetchCollectionCampaign(mock(Task.Listener.class));

        verify(executorService).submit(any(WidgetFetchingTask.class));
    }

    @Test
    public void enrollPhoneNumber_submitsTask() {
        repository.enrollPhoneNumber("+15161237890", mock(Task.Listener.class));

        verify(executorService).submit(any(PhoneEnrollmentTask.class));
    }

    @Test
    public void setCookie_withoutExpiry_providesToCookieJar() {
        repository.setCookie("hello", "world");

        verify(cookieJar).setCookie("hello", "world");
        verifyNoMoreInteractions(cookieJar);
    }

    @Test
    public void setCookie_withExpiry_providesToCookieJar() {
        repository.setCookie("hello", "world", 786);

        verify(cookieJar).setCookie("hello", "world", 786);
        verifyNoMoreInteractions(cookieJar);
    }

    @Test
    public void getCookie_retrievesFromCookieJar() {
        when(cookieJar.getCookie(eq("hello"))).thenReturn("world");

        assertEquals("world", repository.getCookie("hello"));
    }

    @Test
    public void getAllCookies_retrievesFromCookieJar() {
        when(cookieJar.getCookieString()).thenReturn("hello=world;");

        assertEquals("hello=world;", repository.getAllCookies());
    }
}