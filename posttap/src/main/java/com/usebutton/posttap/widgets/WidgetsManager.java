/*
 * WidgetsManager.java
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

import android.app.Activity;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.usebutton.core.data.Task;
import com.usebutton.core.data.models.Event;
import com.usebutton.posttap.data.CookieJar;
import com.usebutton.posttap.data.PostTapRepository;
import com.usebutton.posttap.data.models.CollectionCampaign;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class WidgetsManager implements Application.ActivityLifecycleCallbacks,
        WidgetBridge.Listener {

    private static final String TAG = WidgetsManager.class.getSimpleName();

    private static final String WIDGET_WAS_DISPLAYED = "btn_native_widget_display_occurred";
    private static final String WIDGET_WAS_DISMISSED = "btn_native_widget_dismiss_occurred";
    private static final String PHONE_ENROLLED = "btn_native_widget_phone_enrolled";

    private final Map<String, Integer> screenToIdMap = new HashMap<>();
    private final PostTapRepository repository;

    @Nullable private WeakReference<Activity> lastActivityReference;
    @Nullable private CollectionCampaign campaign;
    private WidgetLayout layout = new WidgetLayout();

    public WidgetsManager(Application application, PostTapRepository repository) {
        application.registerActivityLifecycleCallbacks(this);
        this.repository = repository;
    }

    /**
     * Display the widget for the provided collection campaign.
     *
     * @param campaign the campaign associated with the widget
     */
    public void displayWidget(CollectionCampaign campaign) {
        this.campaign = campaign;
        refreshOrDisplayWidget();
    }

    /**
     * Dismiss any currently displayed widget.
     * Please note that dismissing the widget manually like so will *not* emit the dismiss event.
     */
    public void dismissWidget() {
        campaign = null;

        final ViewGroup root = getRootView();
        final WidgetView widgetView = getCurrentWidgetView();
        if (root == null || widgetView == null) return;

        root.post(new Runnable() {
            @Override
            public void run() {
                root.removeView(widgetView);
            }
        });
    }

    @Nullable
    private WidgetView getCurrentWidgetView() {
        Activity activity = getForegroundActivity();
        if (activity == null) return null;
        String screenName = activity.getClass().getSimpleName();
        Integer resId = screenToIdMap.get(screenName);
        return activity.findViewById(resId != null ? resId : 0);
    }

    @Nullable
    private ViewGroup getRootView() {
        Activity activity = getForegroundActivity();
        if (activity == null) return null;
        return activity.findViewById(android.R.id.content);
    }

    private void refreshOrDisplayWidget() {
        if (campaign == null) return; // Widget should only be displayed if a campaign exists

        Activity activity = getForegroundActivity();
        if (activity == null) return;

        String screenName = activity.getClass().getSimpleName();
        Integer resId = screenToIdMap.get(screenName);
        WidgetView widgetView = activity.findViewById(resId != null ? resId : 0);

        if (widgetView != null) {
            widgetView.updateLayout(layout);
            return;
        }

        widgetView = new WidgetView(activity, campaign.getTemplateUrl(), this);
        widgetView.updateLayout(layout);

        ViewGroup root = activity.findViewById(android.R.id.content);
        root.addView(widgetView);

        widgetView.setId(View.generateViewId());
        screenToIdMap.put(screenName, widgetView.getId());
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        // do nothing
    }

    @Override
    public void onActivityStarted(Activity activity) {
        // do nothing
    }

    @Override
    public void onActivityResumed(Activity activity) {
        lastActivityReference = new WeakReference<>(activity);
        refreshOrDisplayWidget();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        // do nothing
    }

    @Override
    public void onActivityStopped(Activity activity) {
        try {
            if (lastActivityReference != null && lastActivityReference.get() == activity) {
                lastActivityReference.clear();
                lastActivityReference = null;
            }
        } catch (Throwable t) {
            Log.e(TAG, "Error clearing last Activity reference", t);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        // do nothing
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        // do nothing
    }

    @Nullable
    public Activity getForegroundActivity() {
        return lastActivityReference != null ? lastActivityReference.get() : null;
    }

    @Override
    public void onMount() {
        WidgetView widgetView = getCurrentWidgetView();
        if (widgetView == null || campaign == null) return;

        // Inject cookies
        widgetView.runWidgetCallback("window.updateCookies",
                "'" + repository.getAllCookies() + "'");

        // Inject campaign
        widgetView.runWidgetCallback("window.updateCampaign", campaign.asJson().toString());
    }

    @Override
    public void onReady(JSONObject initialLayout, int animationDuration) {
        repository.setCookie(WIDGET_WAS_DISPLAYED, "true", 30 * 60);
        onLayoutUpdate(initialLayout, animationDuration);
    }

    @Override
    public void onResetDimensions(int height) {
        layout.setHeight(height); // dimension updates should build upon existing constraints
        layout.setAnimationDuration(WidgetLayout.DEFAULT_ANIMATION_DURATION);

        refreshOrDisplayWidget();
    }

    @Override
    public void onLayoutUpdate(JSONObject layoutJson, int animationDuration) {
        layout = WidgetLayout.fromJson(layoutJson); // layout constraints should be set anew
        layout.setAnimationDuration(animationDuration);

        refreshOrDisplayWidget();
    }

    @Override
    public void onDismiss(int delay) {
        // Set cookie to ensure widget isn't shown again for another 30 days
        repository.setCookie(WIDGET_WAS_DISMISSED, "true", 30 * 24 * 60 * 60);

        // Report the dismiss event once
        if (campaign != null) {
            Event dismissEvent = new Event("btn:web-widget-dismissed", repository.getSourceToken());
            dismissEvent.addProperty("smscampaign_id", campaign.getCampaignId());
            dismissEvent.addProperty("template_url", campaign.getTemplateUrl());
            repository.reportEvent(dismissEvent);
        }

        // Dismiss widget after the requested delay
        WidgetView widgetView = getCurrentWidgetView();
        if (widgetView != null) {
            widgetView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismissWidget();
                }
            }, delay);
        }
    }

    @Override
    public void onSetCookie(String key, @Nullable String value) {
        repository.setCookie(key, value);

        WidgetView widgetView = getCurrentWidgetView();
        if (widgetView == null) return;

        // Sync cookie state back
        widgetView.runWidgetCallback("window.updateCookies",
                "'" + repository.getAllCookies() + "'");
    }

    @Override
    public void onSetCookie(String key, @Nullable String value, long maxAgeInSeconds) {
        repository.setCookie(key, value, maxAgeInSeconds);
    }

    @Override
    public void onReportEvent(String eventName, @Nullable JSONObject eventBody) {
        Event event = new Event(eventName, repository.getSourceToken());
        event.setValue(eventBody);
        repository.reportEvent(event);
    }

    @Override
    public void onPhoneEnroll(String phoneNumber) {
        if (campaign == null) return;
        repository.enrollPhoneNumber(phoneNumber, campaign.getTemplateUrl(),
                campaign.getCampaignId(), new Task.Listener<String>() {
                    @Override
                    public void onTaskComplete(@Nullable String enrollmentId) {
                        repository.setCookie(PHONE_ENROLLED, "true", CookieJar.MAX_AGE);
                    }

                    @Override
                    public void onTaskError(@NonNull Throwable throwable) {
                        Log.e(TAG, "Error enrolling phone number", throwable);
                    }
                });
    }

    @Override
    public void onUrlOpen(String url) {
        Activity activity = getForegroundActivity();
        if (activity != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            try {
                activity.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "Error opening URL: " + url, e);
            }
        }
    }
}
