/*
 * CookieJar.java
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

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CookieJar {

    public static final long MAX_AGE = 60 * 60 * 24 * 365;

    private final Map<String, String> sessionCookies;
    private final SharedPreferences sharedPreferences;

    public CookieJar(Context context) {
        sessionCookies = new HashMap<>();
        this.sharedPreferences = context.getSharedPreferences("button_posttap_cookies",
                Context.MODE_PRIVATE);
    }

    public void setCookie(String key, String value) {
        sessionCookies.put(key, value);
    }

    public void setCookie(String key, String value, long maxAgeInSeconds) {
        sharedPreferences.edit().putString(key, value).apply();
        sharedPreferences.edit()
                .putLong("expires_" + key, System.currentTimeMillis() + (maxAgeInSeconds * 1000))
                .apply();
    }

    @Nullable
    public String getCookie(String key) {
        if (sessionCookies.containsKey(key) && sessionCookies.get(key) != null) {
            return sessionCookies.get(key);
        }

        if (!hasCookieExpired(key)) {
            return sharedPreferences.getString(key, null);
        }

        return null;
    }

    public String getCookieString() {
        StringBuilder sb = new StringBuilder();
        Map<String, ?> data = sharedPreferences.getAll();
        for (String key : data.keySet()) {
            if (!key.startsWith("expires_")
                    && !hasCookieExpired(key)
                    && data.containsKey(key)
                    && data.get(key) != null) {
                sb.append(key);
                sb.append("=");
                sb.append(data.get(key));
                sb.append("; ");
            }
        }
        for (String key : sessionCookies.keySet()) {
            if (sessionCookies.containsKey(key) && sessionCookies.get(key) != null) {
                sb.append(key);
                sb.append("=");
                sb.append(sessionCookies.get(key));
                sb.append("; ");
            }
        }
        return sb.toString().trim();
    }

    private boolean hasCookieExpired(String key) {
        if (sharedPreferences.contains(key)) {
            long expires = sharedPreferences.getLong("expires_" + key, System.currentTimeMillis());
            if (expires > System.currentTimeMillis()) {
                return false;
            } else {
                sharedPreferences.edit().remove(key).apply();
                sharedPreferences.edit().remove("expires_" + key).apply();
            }
        }

        return true;
    }
}
