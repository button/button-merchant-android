/*
 * RepositoryImplTest.java
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

import com.usebutton.core.data.models.Event;
import com.usebutton.core.data.tasks.EventReportingTask;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RepositoryImplTest {

    @Mock CoreApi coreApi;
    @Mock DeviceManager deviceManager;
    @Mock MemoryStore memoryStore;
    @Mock PersistentStore persistentStore;
    @Mock ExecutorService executorService;

    private Repository repository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        repository = new RepositoryImpl(coreApi, deviceManager, executorService, persistentStore,
                memoryStore);
    }

    @Test
    public void setApplicationId_provideToMemoryStore() {
        repository.setApplicationId("app-xxxxxxxxxxxxxxxx");
        verify(memoryStore).setApplicationId("app-xxxxxxxxxxxxxxxx");
    }

    @Test
    public void setApplicationId_submitsPendingTasks() {
        repository.reportEvent(mock(Event.class));
        repository.reportEvent(mock(Event.class));
        repository.reportEvent(mock(Event.class));

        repository.setApplicationId("app-xxxxxxxxxxxxxxxx");

        verify(executorService, times(3)).submit(any(EventReportingTask.class));
    }

    @Test
    public void setApplicationId_clearsPendingTasksAfterSubmission() {
        repository.reportEvent(mock(Event.class));
        repository.reportEvent(mock(Event.class));
        repository.reportEvent(mock(Event.class));

        repository.setApplicationId("app-xxxxxxxxxxxxxxxx");
        repository.setApplicationId("app-xxxxxxxxxxxxxxxx");
        repository.setApplicationId("app-xxxxxxxxxxxxxxxx");
        repository.setApplicationId("app-xxxxxxxxxxxxxxxx");
        repository.setApplicationId("app-xxxxxxxxxxxxxxxx");

        verify(executorService, times(3)).submit(any(EventReportingTask.class));
    }

    @Test
    public void setSourceToken_persistToPersistenceManager() {
        repository.setSourceToken("app-xxxxxxxxxxxxxxxx");
        verify(persistentStore).setSourceToken("app-xxxxxxxxxxxxxxxx");
    }

    @Test
    public void getSourceToken_retrieveFromPersistenceManager() {
        when(persistentStore.getSourceToken()).thenReturn("app-xxxxxxxxxxxxxxxx");

        String sourceToken = repository.getSourceToken();
        assertEquals("app-xxxxxxxxxxxxxxxx", sourceToken);
    }

    @Test
    public void clear_clearPersistentStore() {
        repository.clearAllData();

        verify(persistentStore).clearAllData();
    }

    @Test
    public void reportEvent_configured_executeTask() {
        repository.setApplicationId("app-xxxxxxxxxxxxxxxx");
        repository.reportEvent(mock(Event.class));

        verify(executorService).submit(any(EventReportingTask.class));
    }

    @Test
    public void reportEvent_unConfigured_queueTaskAndExecuteWhenConfigured() {
        repository.reportEvent(mock(Event.class));

        verify(executorService, never()).submit(any(EventReportingTask.class));
        repository.setApplicationId("app-xxxxxxxxxxxxxxxx");

        verify(executorService).submit(any(EventReportingTask.class));
    }
}
