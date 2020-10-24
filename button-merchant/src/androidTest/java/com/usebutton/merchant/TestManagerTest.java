/*
 * TestManagerTest.java
 *
 * Copyright (c) 2020 Button, Inc. (https://usebutton.com)
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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class TestManagerTest {

    private Context context;
    private ButtonRepository buttonRepository;
    private TestManager.Terminator terminator;
    private TestManager testManager;
    private ArgumentCaptor<Intent> intentCaptor;

    @Before
    public void setUp() {
        context = mock(Context.class, RETURNS_MOCKS);
        buttonRepository = mock(ButtonRepository.class);
        terminator = mock(TestManager.Terminator.class);
        testManager = new TestManager(context, buttonRepository, terminator);
        intentCaptor = ArgumentCaptor.forClass(Intent.class);
    }

    @Test
    public void parseIntent_unrecognized_shouldNotSendResponse() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("test://hello/world"));

        testManager.parseIntent(intent);

        verifyZeroInteractions(context);
    }

    @Test
    public void parseIntent_unsupported_shouldNotSendResponse() {
        Intent intent = createIntentForAction("unknown");

        testManager.parseIntent(intent);

        verifyZeroInteractions(context);
    }

    @Test
    public void parseIntent_quit_harnessExists_shouldQuitApp() {
        Intent intent = createIntentForAction(TestManager.ACTION_QUIT);

        testManager.parseIntent(intent);

        verifyBaseResponseStructure(TestManager.ACTION_QUIT);
        verify(terminator).terminate();
    }

    @Test
    public void parseIntent_quit_harnessDoesNotExist_shouldNotQuitApp() {
        Intent intent = createIntentForAction(TestManager.ACTION_QUIT);
        doThrow(ActivityNotFoundException.class).when(context).startActivity(any(Intent.class));

        testManager.parseIntent(intent);

        verify(terminator, never()).terminate();
    }

    @Test
    public void parseIntent_getToken_nullToken_shouldSendTokenInResponse() {
        Intent intent = createIntentForAction(TestManager.ACTION_GET_TOKEN);
        when(buttonRepository.getSourceToken()).thenReturn(null);

        testManager.parseIntent(intent);

        Uri data = verifyBaseResponseStructure(TestManager.ACTION_GET_TOKEN);
        assertEquals("null", data.getQueryParameter("btn_ref"));
    }

    @Test
    public void parseIntent_getToken_validToken_shouldSendTokenInResponse() {
        Intent intent = createIntentForAction(TestManager.ACTION_GET_TOKEN);
        when(buttonRepository.getSourceToken()).thenReturn("test-token");

        testManager.parseIntent(intent);

        Uri data = verifyBaseResponseStructure(TestManager.ACTION_GET_TOKEN);
        assertEquals("test-token", data.getQueryParameter("btn_ref"));
    }

    @Test
    public void parseIntent_postInstall_shouldSendResultInResponse() {
        Intent intent = createIntentForAction(TestManager.ACTION_POST_INSTALL);
        when(buttonRepository.checkedDeferredDeepLink()).thenReturn(true);

        testManager.parseIntent(intent);

        Uri data = verifyBaseResponseStructure(TestManager.ACTION_POST_INSTALL);
        assertEquals("true", data.getQueryParameter("success"));
    }

    @Test
    public void parseIntent_version_shouldSendVersionInResponse() {
        Intent intent = createIntentForAction(TestManager.ACTION_VERSION);

        testManager.parseIntent(intent);

        Uri data = verifyBaseResponseStructure(TestManager.ACTION_VERSION);
        assertEquals(BuildConfig.VERSION_NAME, data.getQueryParameter("version"));
    }

    @Test
    public void parseIntent_echo_true_shouldSendTokenInResponse() {
        Uri uri = Uri.parse("test://this/is/different?with_different=params&btn_test_echo=true");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        when(buttonRepository.getSourceToken()).thenReturn("test-token");

        testManager.parseIntent(intent);

        Uri data = verifyBaseResponseStructure(TestManager.ACTION_ECHO_TOKEN);
        assertEquals("test-token", data.getQueryParameter("btn_ref"));
    }

    @Test
    public void parseIntent_echo_false_shouldNotSendResponse() {
        Uri uri = Uri.parse("test://this/is/different?with_different=params&btn_test_echo=false");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        when(buttonRepository.getSourceToken()).thenReturn("test-token");

        testManager.parseIntent(intent);

        verifyZeroInteractions(context);
    }

    private static Intent createIntentForAction(String action) {
        Uri uri = Uri.parse(String.format("test://home/action/%s", action));
        return new Intent(Intent.ACTION_VIEW, uri);
    }

    private Uri verifyBaseResponseStructure(String actionType) {
        verify(context).startActivity(intentCaptor.capture());

        Intent intent = intentCaptor.getValue();
        assertNotNull(intent);
        assertEquals(TestManager.TEST_HARNESS_PACKAGE, intent.getPackage());
        assertEquals(Intent.FLAG_ACTIVITY_SINGLE_TOP, intent.getFlags());

        Uri data = intent.getData();
        assertNotNull(data);
        assertEquals(TestManager.TEST_HARNESS_SCHEME, data.getScheme());
        assertEquals(TestManager.ACTION_RESPONSE, data.getAuthority());
        assertEquals("/" + actionType, data.getPath());

        return data;
    }
}