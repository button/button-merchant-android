/*
 * TestManager.java
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
import android.support.annotation.VisibleForTesting;

import java.util.List;

/**
 * Internal class responsible for assisting in test automation.
 */
class TestManager {

    @VisibleForTesting static final String TEST_HARNESS_SCHEME = "button-brand-test";
    @VisibleForTesting static final String TEST_HARNESS_PACKAGE = "com.usebutton.brandtest";

    @VisibleForTesting static final String ACTION_QUIT = "quit";
    @VisibleForTesting static final String ACTION_GET_TOKEN = "get-token";
    @VisibleForTesting static final String ACTION_ECHO_TOKEN = "echo";
    @VisibleForTesting static final String ACTION_POST_INSTALL = "test-post-install";
    @VisibleForTesting static final String ACTION_VERSION = "version";
    @VisibleForTesting static final String ACTION_RESPONSE = "action-response";

    private final Context context;
    private final ButtonRepository buttonRepository;
    private final Terminator terminator;

    TestManager(Context context, ButtonRepository buttonRepository, Terminator terminator) {
        this.context = context;
        this.buttonRepository = buttonRepository;
        this.terminator = terminator;
    }

    public void parseIntent(Intent intent) {
        Uri data = intent.getData();
        if (data == null) {
            return;
        }

        String echoParam = data.getQueryParameter("btn_test_echo");
        List<String> segments = data.getPathSegments();
        if (segments != null && segments.size() == 2 && "action".equals(segments.get(0))) {
            String action = segments.get(1);
            switch (action) {
                case ACTION_QUIT:
                    quit();
                    break;
                case ACTION_GET_TOKEN:
                    sendToken(ACTION_GET_TOKEN);
                    break;
                case ACTION_POST_INSTALL:
                    sendPostInstallerResult();
                    break;
                case ACTION_VERSION:
                    sendLibraryVersion();
                    break;
                default:
                    break;
            }
        } else if ("true".equals(echoParam)) {
            sendToken(ACTION_ECHO_TOKEN);
        }
    }

    private void quit() {
        Uri responseDeeplink = new Uri.Builder()
                .scheme(TEST_HARNESS_SCHEME)
                .authority(ACTION_RESPONSE)
                .appendPath(ACTION_QUIT)
                .build();
        boolean successful = submitResult(responseDeeplink);

        if (successful) {
            terminator.terminate();
        }
    }

    private void sendToken(String requestSource) {
        String token = buttonRepository.getSourceToken();
        Uri responseDeeplink = new Uri.Builder()
                .scheme(TEST_HARNESS_SCHEME)
                .authority(ACTION_RESPONSE)
                .appendPath(requestSource)
                .appendQueryParameter("btn_ref", token)
                .build();
        submitResult(responseDeeplink);
    }

    private void sendPostInstallerResult() {
        boolean checked = buttonRepository.checkedDeferredDeepLink();
        Uri responseDeeplink = new Uri.Builder()
                .scheme(TEST_HARNESS_SCHEME)
                .authority(ACTION_RESPONSE)
                .appendPath(ACTION_POST_INSTALL)
                .appendQueryParameter("success", String.valueOf(checked))
                .build();
        submitResult(responseDeeplink);
    }

    private void sendLibraryVersion() {
        Uri responseDeeplink = new Uri.Builder()
                .scheme(TEST_HARNESS_SCHEME)
                .authority(ACTION_RESPONSE)
                .appendPath(ACTION_VERSION)
                .appendQueryParameter("version", BuildConfig.VERSION_NAME)
                .build();
        submitResult(responseDeeplink);
    }

    private boolean submitResult(Uri result) {
        Intent intent = new Intent(Intent.ACTION_VIEW, result);
        intent.setPackage(TEST_HARNESS_PACKAGE);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    /**
     * Internal class responsible for terminating the host application.
     */
    static class Terminator {
        void terminate() {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }
}
