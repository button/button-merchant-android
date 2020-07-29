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
import com.usebutton.merchant.module.Features;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.Executor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
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

        buttonInternal.configure(buttonRepository, "app-abcdef1234567890");

        verify(buttonRepository).setApplicationId("app-abcdef1234567890");
    }

    @Test
    public void configure_invalidAppId_shouldNotConfigure() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);

        buttonInternal.configure(buttonRepository, "invalid_application_id");

        verifyZeroInteractions(buttonRepository);
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
        Uri.Builder builder = mock(Uri.Builder.class);
        when(intent.getData()).thenReturn(uri);
        when(uri.buildUpon()).thenReturn(builder);
        when(uri.buildUpon().clearQuery()).thenReturn(builder);
        when(builder.build()).thenReturn(uri);
        when(uri.getQueryParameter("btn_ref")).thenReturn("valid_source_token");

        buttonInternal.trackIncomingIntent(buttonRepository, mock(DeviceManager.class),
                mock(Features.class), intent);

        verify(buttonRepository).setSourceToken("valid_source_token");
    }

    @Test
    public void trackIncomingIntent_withNullIntentData_doNotPersist() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        Intent intent = mock(Intent.class);
        when(intent.getData()).thenReturn(null);

        buttonInternal.trackIncomingIntent(buttonRepository, mock(DeviceManager.class),
                mock(Features.class), intent);

        verify(buttonRepository, never()).setSourceToken(anyString());
    }

    @Test
    public void setAttributionToken_ShouldNotifyListenersWithToken() {
        String validToken = "valid_source_token";
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        Intent intent = mock(Intent.class);
        Uri uri = mock(Uri.class);
        Uri.Builder builder = mock(Uri.Builder.class);
        when(intent.getData()).thenReturn(uri);
        when(uri.buildUpon()).thenReturn(builder);
        when(uri.buildUpon().clearQuery()).thenReturn(builder);
        when(builder.build()).thenReturn(uri);
        when(uri.getQueryParameter("btn_ref")).thenReturn(validToken);

        // Add Listener
        ButtonMerchant.AttributionTokenListener listener =
                mock(ButtonMerchant.AttributionTokenListener.class);
        buttonInternal.addAttributionTokenListener(buttonRepository, listener);
        buttonInternal.trackIncomingIntent(buttonRepository, mock(DeviceManager.class),
                mock(Features.class), intent);

        verify(buttonRepository).setSourceToken(validToken);
        verify(listener).onAttributionTokenChanged(validToken);
    }

    @Test
    public void setAttributionToken_shouldNotNotifyListenersOrSetTokenWithNull() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        Intent intent = mock(Intent.class);
        Uri uri = mock(Uri.class);
        Uri.Builder builder = mock(Uri.Builder.class);
        when(intent.getData()).thenReturn(uri);
        when(uri.buildUpon()).thenReturn(builder);
        when(uri.buildUpon().clearQuery()).thenReturn(builder);
        when(builder.build()).thenReturn(uri);
        when(uri.getQueryParameter("btn_ref")).thenReturn(null);

        // Add Listener
        ButtonMerchant.AttributionTokenListener listener =
                mock(ButtonMerchant.AttributionTokenListener.class);
        buttonInternal.addAttributionTokenListener(buttonRepository, listener);
        buttonInternal.trackIncomingIntent(buttonRepository, mock(DeviceManager.class),
                mock(Features.class), intent);

        verify(buttonRepository, never()).setSourceToken(null);
        verify(listener, never()).onAttributionTokenChanged(null);
    }

    @Test
    public void setAttributionToken_shouldNotNotifyListenersOrSetTokenWithEmptyString() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        Intent intent = mock(Intent.class);
        Uri uri = mock(Uri.class);
        Uri.Builder builder = mock(Uri.Builder.class);
        when(intent.getData()).thenReturn(uri);
        when(uri.buildUpon()).thenReturn(builder);
        when(uri.buildUpon().clearQuery()).thenReturn(builder);
        when(builder.build()).thenReturn(uri);
        when(uri.getQueryParameter("btn_ref")).thenReturn("");

        // Add Listener
        ButtonMerchant.AttributionTokenListener listener =
                mock(ButtonMerchant.AttributionTokenListener.class);
        buttonInternal.addAttributionTokenListener(buttonRepository, listener);
        buttonInternal.trackIncomingIntent(buttonRepository, mock(DeviceManager.class),
                mock(Features.class), intent);

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
        Features features = mock(Features.class);

        when(buttonRepository.getApplicationId()).thenReturn("valid_application_id");

        buttonInternal.handlePostInstallIntent(buttonRepository, deviceManager,
                features,"com.usebutton.merchant", postInstallIntentListener);

        ArgumentCaptor<GetPendingLinkTask.Listener> listenerArgumentCaptor =
                ArgumentCaptor.forClass(GetPendingLinkTask.Listener.class);
        verify(buttonRepository).getPendingLink(eq(deviceManager), eq(features),
                listenerArgumentCaptor.capture());

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
        Features features = mock(Features.class);

        when(buttonRepository.getApplicationId()).thenReturn("valid_application_id");

        buttonInternal.handlePostInstallIntent(buttonRepository, deviceManager,
                features, "com.usebutton.merchant", postInstallIntentListener);

        ArgumentCaptor<GetPendingLinkTask.Listener> listenerArgumentCaptor =
                ArgumentCaptor.forClass(GetPendingLinkTask.Listener.class);
        verify(buttonRepository).getPendingLink(eq(deviceManager), eq(features),
                listenerArgumentCaptor.capture());

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
        Features features = mock(Features.class);

        when(buttonRepository.getApplicationId()).thenReturn(null);

        buttonInternal.handlePostInstallIntent(buttonRepository, deviceManager, features,
                "com.usebutton.merchant", postInstallIntentListener);

        verify(buttonRepository, never()).getPendingLink(any(DeviceManager.class),
                any(Features.class), any(Task.Listener.class));
        verify(buttonRepository, never()).setSourceToken(anyString());
        verify(postInstallIntentListener).onResult((Intent) isNull(),
                any(ApplicationIdNotFoundException.class));
    }

    @Test
    public void handlePostInstallIntent_throwButtonNetworkException() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        PostInstallIntentListener postInstallIntentListener = mock(PostInstallIntentListener.class);
        DeviceManager deviceManager = mock(DeviceManager.class);
        Features features = mock(Features.class);

        when(buttonRepository.getApplicationId()).thenReturn("valid_application_id");

        buttonInternal.handlePostInstallIntent(buttonRepository, deviceManager,
                features, "com.usebutton.merchant", postInstallIntentListener);

        ArgumentCaptor<GetPendingLinkTask.Listener> listenerArgumentCaptor =
                ArgumentCaptor.forClass(GetPendingLinkTask.Listener.class);
        verify(buttonRepository).getPendingLink(eq(deviceManager), eq(features),
                listenerArgumentCaptor.capture());

        listenerArgumentCaptor.getValue().onTaskError(new ButtonNetworkException(""));

        verify(postInstallIntentListener).onResult((Intent) isNull(),
                any(ButtonNetworkException.class));
    }

    @Test
    public void handlePostInstallIntent_newInstallationAndDidNotCheckDeferredDeepLink_updateCheckDeferredDeepLink() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        PostInstallIntentListener postInstallIntentListener = mock(PostInstallIntentListener.class);
        DeviceManager deviceManager = mock(DeviceManager.class);
        Features features = mock(Features.class);

        when(buttonRepository.getApplicationId()).thenReturn("valid_application_id");
        when(deviceManager.isOldInstallation()).thenReturn(false);
        when(buttonRepository.checkedDeferredDeepLink()).thenReturn(false);

        buttonInternal.handlePostInstallIntent(buttonRepository, deviceManager, features,
                "com.usebutton.merchant", postInstallIntentListener);

        verify(buttonRepository).updateCheckDeferredDeepLink(true);
        verify(buttonRepository).getPendingLink(any(DeviceManager.class), any(Features.class),
                any(Task.Listener.class));
    }

    @Test
    public void handlePostInstallIntent_oldInstallation_doNotUpdateCheckDeferredDeepLink_verifyCallback() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        PostInstallIntentListener postInstallIntentListener = mock(PostInstallIntentListener.class);
        DeviceManager deviceManager = mock(DeviceManager.class);
        Features features = mock(Features.class);

        when(buttonRepository.getApplicationId()).thenReturn("valid_application_id");
        when(deviceManager.isOldInstallation()).thenReturn(true);
        when(buttonRepository.checkedDeferredDeepLink()).thenReturn(false);

        buttonInternal.handlePostInstallIntent(buttonRepository, deviceManager, features,
                "com.usebutton.merchant", postInstallIntentListener);

        verify(postInstallIntentListener).onResult(null, null);
        verify(buttonRepository, never()).updateCheckDeferredDeepLink(anyBoolean());
    }

    @Test
    public void handlePostInstallIntent_checkedDeferredDeepLink_doNotUpdateCheckDeferredDeepLink_verifyCallback() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        PostInstallIntentListener postInstallIntentListener = mock(PostInstallIntentListener.class);
        DeviceManager deviceManager = mock(DeviceManager.class);
        Features features = mock(Features.class);

        when(buttonRepository.getApplicationId()).thenReturn("valid_application_id");
        when(deviceManager.isOldInstallation()).thenReturn(false);
        when(buttonRepository.checkedDeferredDeepLink()).thenReturn(true);

        buttonInternal.handlePostInstallIntent(buttonRepository, deviceManager, features,
                "com.usebutton.merchant", postInstallIntentListener);

        verify(postInstallIntentListener).onResult((Intent) isNull(), (Throwable) isNull());
        verify(buttonRepository, never()).updateCheckDeferredDeepLink(anyBoolean());
    }

    @Test
    public void handlePostInstallIntent_previouslySetTokenFromDirectDeeplink_shouldNotPersistTokenNorProvideDeferredDeeplink() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        PostInstallIntentListener postInstallIntentListener = mock(PostInstallIntentListener.class);
        DeviceManager deviceManager = mock(DeviceManager.class);
        Features features = mock(Features.class);
        Intent intent = mock(Intent.class);
        Uri link = mock(Uri.class);
        Uri.Builder builder = mock(Uri.Builder.class);
        when(link.buildUpon()).thenReturn(builder);
        when(link.buildUpon().clearQuery()).thenReturn(builder);
        when(builder.build()).thenReturn(link);
        when(link.getQueryParameter("btn_ref")).thenReturn("valid_token");
        when(intent.getData()).thenReturn(link);
        when(buttonRepository.getApplicationId()).thenReturn("valid_application_id");
        ArgumentCaptor<GetPendingLinkTask.Listener<PostInstallLink>> listenerArgumentCaptor =
                ArgumentCaptor.forClass(GetPendingLinkTask.Listener.class);
        PostInstallLink.Attribution attribution =
                new PostInstallLink.Attribution("valid_source_token", "SMS");
        PostInstallLink postInstallLink =
                new PostInstallLink(true, "ddl-6faffd3451edefd3", "uber://asdfasfasf",
                        attribution);

        buttonInternal.trackIncomingIntent(buttonRepository, deviceManager, features, intent);
        buttonInternal.handlePostInstallIntent(buttonRepository, deviceManager,
                features, "com.usebutton.merchant", postInstallIntentListener);

        verify(buttonRepository).getPendingLink(eq(deviceManager), eq(features),
                listenerArgumentCaptor.capture());
        listenerArgumentCaptor.getValue().onTaskComplete(postInstallLink);

        verify(postInstallIntentListener).onResult((Intent) isNull(), (Throwable) isNull());
        verify(buttonRepository, never()).setSourceToken("valid_source_token");
    }

    @Test
    public void reportOrder_nullApplicationId_verifyException() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        when(buttonRepository.getApplicationId()).thenReturn(null);
        OrderListener orderListener = mock(OrderListener.class);

        buttonInternal.reportOrder(buttonRepository, mock(DeviceManager.class),
                mock(Features.class), mock(Order.class), orderListener);

        verify(orderListener).onResult(any(ApplicationIdNotFoundException.class));
    }

    @Test
    public void reportOrder_hasApplicationId_verifyPostOrder() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        when(buttonRepository.getApplicationId()).thenReturn("valid_application_id");
        DeviceManager deviceManager = mock(DeviceManager.class);
        Features features = mock(Features.class);
        Order order = mock(Order.class);
        OrderListener orderListener = mock(OrderListener.class);

        buttonInternal.reportOrder(buttonRepository, deviceManager, features, order, orderListener);

        verify(buttonRepository).postOrder(eq(order), eq(deviceManager), eq(features),
                any(Task.Listener.class));
    }

    @Test
    public void reportOrder_onTaskComplete_verifyCallback() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        when(buttonRepository.getApplicationId()).thenReturn("valid_application_id");
        OrderListener orderListener = mock(OrderListener.class);

        buttonInternal.reportOrder(buttonRepository, mock(DeviceManager.class),
                mock(Features.class), mock(Order.class), orderListener);

        ArgumentCaptor<Task.Listener> argumentCaptor = ArgumentCaptor.forClass(Task.Listener.class);
        verify(buttonRepository).postOrder(any(Order.class), any(DeviceManager.class),
                any(Features.class), argumentCaptor.capture());
        argumentCaptor.getValue().onTaskComplete(null);
        verify(orderListener).onResult((Throwable) isNull());
    }

    @Test
    public void reportOrder_onTaskError_verifyCallback() {
        ButtonRepository buttonRepository = mock(ButtonRepository.class);
        when(buttonRepository.getApplicationId()).thenReturn("valid_application_id");
        OrderListener orderListener = mock(OrderListener.class);

        buttonInternal.reportOrder(buttonRepository, mock(DeviceManager.class),
                mock(Features.class), mock(Order.class), orderListener);

        ArgumentCaptor<Task.Listener> argumentCaptor = ArgumentCaptor.forClass(Task.Listener.class);
        verify(buttonRepository).postOrder(any(Order.class), any(DeviceManager.class),
                any(Features.class), argumentCaptor.capture());
        Exception exception = mock(Exception.class);
        argumentCaptor.getValue().onTaskError(exception);
        verify(orderListener).onResult(exception);
    }

    private class TestMainThreadExecutor implements Executor {

        @Override
        public void execute(@NonNull Runnable command) {
            command.run();
        }
    }
}
