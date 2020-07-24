/*
 * ButtonRepositoryImplTest.java
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

import com.usebutton.merchant.module.Features;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class ButtonRepositoryImplTest {

    @Mock
    ButtonApi buttonApi;

    @Mock
    PersistenceManager persistenceManager;

    @Mock
    ExecutorService executorService;

    private ButtonRepositoryImpl buttonRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        buttonRepository = new ButtonRepositoryImpl(buttonApi, persistenceManager, executorService);
    }

    @Test
    public void setApplicationId_cacheInMemory() {
        buttonRepository.setApplicationId("valid_application_id");
        assertEquals("valid_application_id", buttonRepository.getApplicationId());
        verifyZeroInteractions(persistenceManager);
    }

    @Test
    public void setApplicationId_provideToButtonApi() {
        buttonRepository.setApplicationId("valid_application_id");
        verify(buttonApi).setApplicationId("valid_application_id");
    }

    @Test
    public void getApplicationId_returnValidAppId() {
        buttonRepository.setApplicationId("valid_application_id");
        assertEquals("valid_application_id", buttonRepository.getApplicationId());
    }

    @Test
    public void setSourceToken_persistToPersistenceManager() {
        buttonRepository.setSourceToken("valid_source_token");
        verify(persistenceManager).setSourceToken("valid_source_token");
    }

    @Test
    public void getSourceToken_retrieveFromPersistenceManager() {
        when(persistenceManager.getSourceToken()).thenReturn("valid_source_token");

        String sourceToken = buttonRepository.getSourceToken();
        assertEquals("valid_source_token", sourceToken);
    }

    @Test
    public void clear_clearPersistenceManager() {
        buttonRepository.clear();

        verify(persistenceManager).clear();
    }

    @Test
    public void getPendingLink_executeTask() {
        buttonRepository.getPendingLink(mock(DeviceManager.class), mock(Features.class),
                mock(GetPendingLinkTask.Listener.class));

        verify(executorService).submit(any(GetPendingLinkTask.class));
    }

    @Test
    public void postUserActivity_executeTask() {
        buttonRepository.postUserActivity(mock(DeviceManager.class), mock(Order.class),
                mock(Task.Listener.class));

        verify(executorService).submit(any(UserActivityTask.class));
    }

    @Test
    public void checkedDeferredDeepLink_retrieveFromPersistenceManager() {
        when(persistenceManager.checkedDeferredDeepLink()).thenReturn(true);

        boolean checkedDeferredDeepLink = buttonRepository.checkedDeferredDeepLink();
        assertEquals(true, checkedDeferredDeepLink);
    }

    @Test
    public void updateCheckDeferredDeepLink_persistToPersistenceManager() {
        buttonRepository.updateCheckDeferredDeepLink(true);
        verify(persistenceManager).updateCheckDeferredDeepLink(true);
    }

    @Test
    public void postOrder_executeTask() {
        buttonRepository.postOrder(mock(Order.class), mock(DeviceManager.class),
                mock(Features.class), mock(Task.Listener.class));

        verify(executorService).submit(any(PostOrderTask.class));
    }
}