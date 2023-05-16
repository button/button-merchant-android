/*
 * PersistenceManagerImpl.java
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
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

/**
 * Class handles persisting anything in the {@link SharedPreferences}
 */

final class PersistenceManagerImpl implements PersistenceManager {

    private static PersistenceManager persistenceManager;

    static PersistenceManager getInstance(Context context) {
        if (persistenceManager == null) {
            persistenceManager = new PersistenceManagerImpl(context);
        }

        return persistenceManager;
    }

    private final SharedPreferences sharedPreferences;

    @VisibleForTesting
    PersistenceManagerImpl(Context context) {
        sharedPreferences = context.getSharedPreferences("button_shared_preferences",
                Context.MODE_PRIVATE);
    }

    @Override
    public void setSessionId(String sessionId) {
        sharedPreferences.edit().putString(Key.SESSION_ID, sessionId).apply();
    }

    @Nullable
    @Override
    public String getSessionId() {
        return sharedPreferences.getString(Key.SESSION_ID, null);
    }

    @Override
    public void setSourceToken(String sourceToken) {
        sharedPreferences.edit().putString(Key.SOURCE_TOKEN, sourceToken).apply();
    }

    @Nullable
    @Override
    public String getSourceToken() {
        return sharedPreferences.getString(Key.SOURCE_TOKEN, null);
    }

    @Override
    public void clear() {
        sharedPreferences.edit().clear().apply();
    }

    @Override
    public boolean checkedDeferredDeepLink() {
        return sharedPreferences.getBoolean(Key.CHECKED_DEFERRED_DEEP_LINK, false);
    }

    @Override
    public void updateCheckDeferredDeepLink(boolean checkedDeferredDeepLink) {
        sharedPreferences.edit().putBoolean(Key.CHECKED_DEFERRED_DEEP_LINK,
                checkedDeferredDeepLink).apply();
    }

    /**
     * Class contains all of the keys for the shared preferences
     */
    static final class Key {

        private static final String PREFIX = "btn_";

        static final String SESSION_ID = PREFIX + "session_id";

        static final String SOURCE_TOKEN = PREFIX + "source_token";

        static final String CHECKED_DEFERRED_DEEP_LINK = PREFIX + "checked_deferred_deep_link";
    }
}
