/*
 * WidgetBridge.java
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

package com.usebutton.posttap.widgets;

import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.JavascriptInterface;

import org.json.JSONException;
import org.json.JSONObject;

class WidgetBridge {

    private static final String TAG = WidgetBridge.class.getSimpleName();

    private final WidgetBridge.Listener listener;

    WidgetBridge(Listener listener) {
        this.listener = listener;
    }

    @JavascriptInterface
    public void onMessageReceived(String message) {
        if (message == null || message.isEmpty()) return;

        try {
            JSONObject messageJson = new JSONObject(message);
            String name = messageJson.getString("name");
            JSONObject arguments = messageJson.optJSONObject("arguments");
            if (arguments == null) arguments = new JSONObject();

            switch (name) {
                case "Button.web.onMount":
                    listener.onMount();
                    break;
                case "Button.web.onReady":
                    int duration = arguments.optInt("animationDuration", 650);
                    JSONObject initialLayout = arguments.getJSONObject("layout");
                    listener.onReady(initialLayout, duration);
                    break;
                case "Button.web.resetDimensions":
                    int height = arguments.getInt("height");
                    listener.onResetDimensions(height);
                    break;
                case "Button.web.updateLayout":
                    int animationDuration = arguments.optInt("animationDuration", 650);
                    JSONObject layout = arguments.getJSONObject("layout");
                    listener.onLayoutUpdate(layout, animationDuration);
                    break;
                case "Button.web.dismissWidget":
                    int delay = arguments.optInt("delay");
                    listener.onDismiss(delay);
                    break;
                case "Button.web.setCookie":
                    String key = arguments.getString("key");
                    String value = arguments.optString("value", null);
                    if (arguments.has("maxAgeInSeconds")) {
                        listener.onSetCookie(key, value, arguments.getLong("maxAgeInSeconds"));
                    } else {
                        listener.onSetCookie(key, value);
                    }
                    break;
                case "Button.web.collectPhoneNumber":
                    String phoneNumber = arguments.getString("phoneNumber");
                    listener.onPhoneEnroll(phoneNumber);
                    break;
                case "Button.web.reportEvent":
                    String eventName = arguments.getString("eventName");
                    JSONObject eventBody = arguments.optJSONObject("eventBody");
                    listener.onReportEvent(eventName, eventBody);
                    break;
                case "Button.web.openURL":
                    String url = arguments.getString("url");
                    listener.onUrlOpen(url);
                    break;
                default:
                    Log.w(TAG, "Unknown message received: " + name);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing incoming message", e);
        }
    }

    interface Listener {
        void onMount();

        void onReady(JSONObject initialLayout, int animationDuration);

        void onResetDimensions(int height);

        void onLayoutUpdate(JSONObject layout, int animationDuration);

        void onDismiss(int delay);

        void onSetCookie(String key, @Nullable String value);

        void onSetCookie(String key, @Nullable String value, long maxAgeInSeconds);

        void onReportEvent(String eventName, @Nullable JSONObject eventBody);

        void onPhoneEnroll(String phoneNumber);

        void onUrlOpen(String url);
    }
}
