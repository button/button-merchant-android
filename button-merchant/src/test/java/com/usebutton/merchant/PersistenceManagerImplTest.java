/*
 * PersistenceManagerImplTest.java
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

public class PersistenceManagerImplTest {

    @Mock
    private SharedPreferences sharedPreferences;

    @Mock
    private SharedPreferences.Editor editor;

    @Mock
    private Context context;

    private PersistenceManagerImpl persistenceManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(context.getSharedPreferences("button_shared_preferences",
                Context.MODE_PRIVATE)).thenReturn(sharedPreferences);
        when(sharedPreferences.edit()).thenReturn(editor);
        when(editor.putString(anyString(), anyString())).thenReturn(editor);
        when(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor);

        persistenceManager = new PersistenceManagerImpl(context);
    }

    @Test
    public void setSourceToken_persistToSharedPrefs() {
        persistenceManager.setSourceToken("valid_source_token");
        verify(editor).putString(PersistenceManagerImpl.Key.SOURCE_TOKEN, "valid_source_token");
        verify(editor).apply();
    }

    @Test
    public void getSourceToken_returnValidSourceToken() {
        when(sharedPreferences.getString(PersistenceManagerImpl.Key.SOURCE_TOKEN, null)).thenReturn(
                "valid_source_token");
        String sourceToken = persistenceManager.getSourceToken();
        assertEquals("valid_source_token", sourceToken);
    }

    @Test
    public void clear_clearSharedPrefs() {
        when(editor.clear()).thenReturn(editor);

        persistenceManager.clear();

        verify(editor).clear();
        verify(editor).apply();
    }

    @Test
    public void checkedDeferredDeepLink_returnTrue() {
        when(sharedPreferences.getBoolean(PersistenceManagerImpl.Key.CHECKED_DEFERRED_DEEP_LINK,
                false)).thenReturn(true);
        boolean checkedDeferredDeepLink = persistenceManager.checkedDeferredDeepLink();
        assertEquals(true, checkedDeferredDeepLink);
    }

    @Test
    public void updateCheckDeferredDeepLink_persistToSharedPrefs() {
        persistenceManager.updateCheckDeferredDeepLink(true);
        verify(editor).putBoolean(PersistenceManagerImpl.Key.CHECKED_DEFERRED_DEEP_LINK, true);
        verify(editor).apply();
    }
}