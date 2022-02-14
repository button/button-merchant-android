/*
 * PersistentStoreImplTest.java
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

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PersistentStoreImplTest {

    @Mock
    private SharedPreferences sharedPreferences;

    @Mock
    private SharedPreferences.Editor editor;

    @Mock
    private Context context;

    private PersistentStore persistenceManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(context.getSharedPreferences("button_shared_preferences",
                Context.MODE_PRIVATE)).thenReturn(sharedPreferences);
        when(sharedPreferences.edit()).thenReturn(editor);
        when(editor.putString(anyString(), anyString())).thenReturn(editor);
        when(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor);

        persistenceManager = new PersistentStoreImpl(context);
    }

    @Test
    public void setSessionId_persistToSharedPrefs() {
        persistenceManager.setSessionId("valid_session_id");
        verify(editor).putString(PersistentStoreImpl.Key.SESSION_ID, "valid_session_id");
        verify(editor).apply();
    }

    @Test
    public void getSessionId_returnValidSessionId() {
        when(sharedPreferences.getString(PersistentStoreImpl.Key.SESSION_ID, null))
                .thenReturn("valid_session_id");
        String sessionId = persistenceManager.getSessionId();
        assertEquals("valid_session_id", sessionId);
    }

    @Test
    public void setSourceToken_persistToSharedPrefs() {
        persistenceManager.setSourceToken("valid_source_token");
        verify(editor).putString(PersistentStoreImpl.Key.SOURCE_TOKEN, "valid_source_token");
        verify(editor).apply();
    }

    @Test
    public void getSourceToken_returnValidSourceToken() {
        when(sharedPreferences.getString(PersistentStoreImpl.Key.SOURCE_TOKEN, null)).thenReturn(
                "valid_source_token");
        String sourceToken = persistenceManager.getSourceToken();
        assertEquals("valid_source_token", sourceToken);
    }

    @Test
    public void clear_clearSharedPrefs() {
        when(editor.clear()).thenReturn(editor);

        persistenceManager.clearAllData();

        verify(editor).clear();
        verify(editor).apply();
    }

    @Test
    public void checkedDeferredDeepLink_returnTrue() {
        when(sharedPreferences.getBoolean(PersistentStoreImpl.Key.CHECKED_DEFERRED_DEEP_LINK,
                false)).thenReturn(true);
        boolean checkedDeferredDeepLink = persistenceManager.checkedDeferredDeepLink();
        assertEquals(true, checkedDeferredDeepLink);
    }

    @Test
    public void updateCheckDeferredDeepLink_persistToSharedPrefs() {
        persistenceManager.updateCheckDeferredDeepLink(true);
        verify(editor).putBoolean(PersistentStoreImpl.Key.CHECKED_DEFERRED_DEEP_LINK, true);
        verify(editor).apply();
    }
}