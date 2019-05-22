/*
 * ButtonInternalImplTest.java
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

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.usebutton.merchant.exception.ApplicationIdNotFoundException;
import com.usebutton.merchant.exception.ButtonNetworkException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.Executor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ButtonInternalImplTest {

    private ButtonInternalImpl buttonInternal;
    private Executor executor;

    @Before
    public void setUp() {
        executor = new TestMainThreadExecutor();
        buttonInternal = new ButtonInternalImpl(executor);
    }

    @Test
    public void configure_saveApplicationIdInMemory() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);

        buttonInternal.configure(buttonRepository, "valid_application_id");
        verify(buttonRepository).setApplicationId("valid_application_id");
    }

    @Test
    public void getApplicationId_retrieveFromRepository() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        when(buttonRepository.getApplicationId()).thenReturn("valid_application_id");

        String applicationId = buttonInternal.getApplicationId(buttonRepository);
        assertEquals("valid_application_id", applicationId);
    }

    @Test
    public void trackIncomingIntent_withValidData_persistUrl() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        Intent intent = mock(Intent.class);
        Uri uri = mock(Uri.class);
        when(intent.getData()).thenReturn(uri);
        when(uri.getQueryParameter("btn_ref")).thenReturn("valid_source_token");

        buttonInternal.trackIncomingIntent(buttonRepository, intent);
        verify(buttonRepository).setSourceToken("valid_source_token");
    }

    @Test
    public void trackIncomingIntent_withNullIntentData_doNotPersist() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        Intent intent = mock(Intent.class);
        when(intent.getData()).thenReturn(null);

        buttonInternal.trackIncomingIntent(buttonRepository, intent);

        verify(buttonRepository, never()).setSourceToken(anyString());
    }

    @Test
    public void trackOrder_withValidParams_NonNullListener() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);

        when(buttonRepository.getApplicationId()).thenReturn("valid_id");
        buttonInternal.trackOrder(buttonRepository, mock(DeviceManager.class), mock(Order.class),
                mock(UserActivityListener.class));

        verify(buttonRepository).postUserActivity(any(DeviceManager.class), any(Order.class),
                any(Task.Listener.class));
    }

    @Test
    public void trackOrder_nullApplicationId() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        UserActivityListener listener = mock(UserActivityListener.class);

        when(buttonRepository.getApplicationId()).thenReturn(null);
        buttonInternal.trackOrder(buttonRepository, mock(DeviceManager.class), mock(Order.class),
                listener);

        verify(listener).onResult(any(ApplicationIdNotFoundException.class));
        verify(buttonRepository, never()).postUserActivity(any(DeviceManager.class),
                any(Order.class),
                any(Task.Listener.class));
    }

    @Test
    public void trackOrder_nullApplicationIdAndListener() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);

        when(buttonRepository.getApplicationId()).thenReturn(null);
        buttonInternal.trackOrder(buttonRepository, mock(DeviceManager.class), mock(Order.class),
                null);

        verify(buttonRepository, never()).postUserActivity(any(DeviceManager.class),
                any(Order.class),
                any(Task.Listener.class));
    }

    @Test
    public void trackOrder_validateUserActivityListenerOnSuccess() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        final UserActivityListener listener = mock(UserActivityListener.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final Task.Listener listener1 = invocation.getArgument(2);
                listener1.onTaskComplete(null);
                return null;
            }
        }).when(buttonRepository)
                .postUserActivity(any(DeviceManager.class), any(Order.class),
                        any(Task.Listener.class));

        when(buttonRepository.getApplicationId()).thenReturn("valid_id");

        buttonInternal.trackOrder(buttonRepository, mock(DeviceManager.class), mock(Order.class),
                listener);
        verify(listener).onResult(null);
    }

    @Test
    public void trackOrder_validateUserActivityListenerOnError() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        final UserActivityListener listener = mock(UserActivityListener.class);
        final Throwable throwable = new ButtonNetworkException("");

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final Task.Listener listener1 = invocation.getArgument(2);
                listener1.onTaskError(throwable);
                return null;
            }
        }).when(buttonRepository)
                .postUserActivity(any(DeviceManager.class), any(Order.class),
                        any(Task.Listener.class));

        when(buttonRepository.getApplicationId()).thenReturn("valid_id");

        buttonInternal.trackOrder(buttonRepository, mock(DeviceManager.class), mock(Order.class),
                listener);
        verify(listener).onResult(throwable);
    }

    @Test
    public void setAttributionToken_ShouldNotifyListenersWithToken() {
        String validToken = "valid_source_token";
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        Intent intent = mock(Intent.class);
        Uri uri = mock(Uri.class);
        when(intent.getData()).thenReturn(uri);
        when(uri.getQueryParameter("btn_ref")).thenReturn(validToken);

        // Add Listener
        ButtonMerchant.AttributionTokenListener listener =
                mock(ButtonMerchant.AttributionTokenListener.class);
        buttonInternal.addAttributionTokenListener(buttonRepository, listener);
        buttonInternal.trackIncomingIntent(buttonRepository, intent);

        verify(buttonRepository).setSourceToken(validToken);
        verify(listener).onAttributionTokenChanged(validToken);
    }

    @Test
    public void setAttributionToken_shouldNotNotifyListenersOrSetTokenWithNull() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        Intent intent = mock(Intent.class);
        Uri uri = mock(Uri.class);
        when(intent.getData()).thenReturn(uri);
        when(uri.getQueryParameter("btn_ref")).thenReturn(null);

        // Add Listener
        ButtonMerchant.AttributionTokenListener listener =
                mock(ButtonMerchant.AttributionTokenListener.class);
        buttonInternal.addAttributionTokenListener(buttonRepository, listener);
        buttonInternal.trackIncomingIntent(buttonRepository, intent);

        verify(buttonRepository, never()).setSourceToken(null);
        verify(listener, never()).onAttributionTokenChanged(null);
    }

    @Test
    public void setAttributionToken_shouldNotNotifyListenersOrSetTokenWithEmptyString() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        Intent intent = mock(Intent.class);
        Uri uri = mock(Uri.class);
        when(intent.getData()).thenReturn(uri);
        when(uri.getQueryParameter("btn_ref")).thenReturn("");

        // Add Listener
        ButtonMerchant.AttributionTokenListener listener =
                mock(ButtonMerchant.AttributionTokenListener.class);
        buttonInternal.addAttributionTokenListener(buttonRepository, listener);
        buttonInternal.trackIncomingIntent(buttonRepository, intent);

        verify(buttonRepository, never()).setSourceToken("");
        verify(listener, never()).onAttributionTokenChanged("");
    }

    @Test
    public void addAndRemoveAttributionToken_verifyInternalList() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        ButtonMerchant.AttributionTokenListener listener =
                mock(ButtonMerchant.AttributionTokenListener.class);

        buttonInternal.addAttributionTokenListener(buttonRepository, listener);
        assertTrue(buttonInternal.attributionTokenListeners.contains(listener));
        buttonInternal.removeAttributionTokenListener(buttonRepository, listener);
        assertFalse(buttonInternal.attributionTokenListeners.contains(listener));
    }

    @Test
    public void getAttributionToken_returnPersistedToken() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        when(buttonRepository.getSourceToken()).thenReturn("valid_source_token");

        String attributionToken = buttonInternal.getAttributionToken(buttonRepository);
        assertEquals("valid_source_token", attributionToken);
    }

    @Test
    public void clearAllData_clearPersistenceManager() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);

        buttonInternal.clearAllData(buttonRepository);

        verify(buttonRepository).clear();
    }

    @Test
    public void handlePostInstallIntent_returnValidPostInstallLink_setSourceToken() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        PostInstallIntentListener postInstallIntentListener = mock(PostInstallIntentListener.class);
        DeviceManager deviceManager = mock(DeviceManager.class);

        when(buttonRepository.getApplicationId()).thenReturn("valid_application_id");

        buttonInternal.handlePostInstallIntent(buttonRepository, postInstallIntentListener,
                "com.usebutton.merchant",
                deviceManager);

        ArgumentCaptor<GetPendingLinkTask.Listener> listenerArgumentCaptor =
                ArgumentCaptor.forClass(GetPendingLinkTask.Listener.class);
        verify(buttonRepository).getPendingLink(listenerArgumentCaptor.capture(),
                eq(deviceManager));

        PostInstallLink.Attribution attribution =
                new PostInstallLink.Attribution("valid_source_token", "SMS");
        PostInstallLink postInstallLink =
                new PostInstallLink(true, "ddl-6faffd3451edefd3", "uber://asdfasfasf",
                        attribution);

        listenerArgumentCaptor.getValue().onTaskComplete(postInstallLink);

        verify(postInstallIntentListener).onResult(any(Intent.class), (Throwable) isNull());
        verify(buttonRepository).setSourceToken("valid_source_token");
    }

    @Test
    public void handlePostInstallIntent_returnInvalidPostInstallLink_doNotSetSourceToken() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        PostInstallIntentListener postInstallIntentListener = mock(PostInstallIntentListener.class);
        DeviceManager deviceManager = mock(DeviceManager.class);

        when(buttonRepository.getApplicationId()).thenReturn("valid_application_id");

        buttonInternal.handlePostInstallIntent(buttonRepository, postInstallIntentListener,
                "com.usebutton.merchant",
                deviceManager);

        ArgumentCaptor<GetPendingLinkTask.Listener> listenerArgumentCaptor =
                ArgumentCaptor.forClass(GetPendingLinkTask.Listener.class);
        verify(buttonRepository).getPendingLink(listenerArgumentCaptor.capture(),
                eq(deviceManager));

        PostInstallLink postInstallLink =
                new PostInstallLink(false, "ddl-6faffd3451edefd3", null, null);

        listenerArgumentCaptor.getValue().onTaskComplete(postInstallLink);

        verify(postInstallIntentListener).onResult(null, null);
        verify(buttonRepository, never()).setSourceToken(anyString());
    }

    @Test
    public void handlePostInstallIntent_nullApplicationId_throwException() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        PostInstallIntentListener postInstallIntentListener = mock(PostInstallIntentListener.class);
        DeviceManager deviceManager = mock(DeviceManager.class);

        when(buttonRepository.getApplicationId()).thenReturn(null);

        buttonInternal.handlePostInstallIntent(buttonRepository, postInstallIntentListener,
                "com.usebutton.merchant",
                deviceManager);

        verify(buttonRepository, never()).getPendingLink(any(Task.Listener.class),
                any(DeviceManager.class));
        verify(buttonRepository, never()).setSourceToken(anyString());
        verify(postInstallIntentListener).onResult((Intent) isNull(),
                any(ApplicationIdNotFoundException.class));
    }

    @Test
    public void handlePostInstallIntent_throwButtonNetworkException() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        PostInstallIntentListener postInstallIntentListener = mock(PostInstallIntentListener.class);
        DeviceManager deviceManager = mock(DeviceManager.class);

        when(buttonRepository.getApplicationId()).thenReturn("valid_application_id");

        buttonInternal.handlePostInstallIntent(buttonRepository, postInstallIntentListener,
                "com.usebutton.merchant",
                deviceManager);

        ArgumentCaptor<GetPendingLinkTask.Listener> listenerArgumentCaptor =
                ArgumentCaptor.forClass(GetPendingLinkTask.Listener.class);
        verify(buttonRepository).getPendingLink(listenerArgumentCaptor.capture(),
                eq(deviceManager));

        listenerArgumentCaptor.getValue().onTaskError(new ButtonNetworkException(""));

        verify(postInstallIntentListener).onResult((Intent) isNull(),
                any(ButtonNetworkException.class));
    }

    @Test
    public void handlePostInstallIntent_newInstallationAndDidNotCheckDeferredDeepLink_updateCheckDeferredDeepLink() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        PostInstallIntentListener postInstallIntentListener = mock(PostInstallIntentListener.class);
        DeviceManager deviceManager = mock(DeviceManager.class);

        when(buttonRepository.getApplicationId()).thenReturn("valid_application_id");
        when(deviceManager.isOldInstallation()).thenReturn(false);
        when(buttonRepository.checkedDeferredDeepLink()).thenReturn(false);

        buttonInternal.handlePostInstallIntent(buttonRepository, postInstallIntentListener,
                "com.usebutton.merchant",
                deviceManager);

        verify(buttonRepository).updateCheckDeferredDeepLink(true);
        verify(buttonRepository).getPendingLink(any(Task.Listener.class), any(DeviceManager.class));
    }

    @Test
    public void handlePostInstallIntent_oldInstallation_doNotUpdateCheckDeferredDeepLink_verifyCallback() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        PostInstallIntentListener postInstallIntentListener = mock(PostInstallIntentListener.class);
        DeviceManager deviceManager = mock(DeviceManager.class);

        when(buttonRepository.getApplicationId()).thenReturn("valid_application_id");
        when(deviceManager.isOldInstallation()).thenReturn(true);
        when(buttonRepository.checkedDeferredDeepLink()).thenReturn(false);

        buttonInternal.handlePostInstallIntent(buttonRepository, postInstallIntentListener,
                "com.usebutton.merchant",
                deviceManager);

        verify(postInstallIntentListener).onResult(null, null);
        verify(buttonRepository, never()).updateCheckDeferredDeepLink(anyBoolean());
    }

    @Test
    public void handlePostInstallIntent_checkedDeferredDeepLink_doNotUpdateCheckDeferredDeepLink_verifyCallback() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        PostInstallIntentListener postInstallIntentListener = mock(PostInstallIntentListener.class);
        DeviceManager deviceManager = mock(DeviceManager.class);

        when(buttonRepository.getApplicationId()).thenReturn("valid_application_id");
        when(deviceManager.isOldInstallation()).thenReturn(false);
        when(buttonRepository.checkedDeferredDeepLink()).thenReturn(true);

        buttonInternal.handlePostInstallIntent(buttonRepository, postInstallIntentListener,
                "com.usebutton.merchant",
                deviceManager);

        verify(postInstallIntentListener).onResult((Intent) isNull(), (Throwable) isNull());
        verify(buttonRepository, never()).updateCheckDeferredDeepLink(anyBoolean());
    }

    private class TestMainThreadExecutor implements Executor {

        @Override
        public void execute(@NonNull Runnable command) {
            command.run();
        }
    }
}
