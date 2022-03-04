/*
 * WidgetView.java
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.usebutton.core.ButtonUtil;
import com.usebutton.posttap.R;

import static android.support.constraint.ConstraintSet.BOTTOM;
import static android.support.constraint.ConstraintSet.LEFT;
import static android.support.constraint.ConstraintSet.PARENT_ID;
import static android.support.constraint.ConstraintSet.RIGHT;
import static android.support.constraint.ConstraintSet.TOP;

@SuppressLint("ViewConstructor")
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
class WidgetView extends ConstraintLayout {

    @SuppressLint({ "SetJavaScriptEnabled", "AddJavascriptInterface" })
    WidgetView(Context context, String templateUrl, WidgetBridge.Listener listener) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.view_widget, this, true);

        WebView webView = findViewById(R.id.btn_widget_webview);

        WebSettings settings = webView.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptEnabled(true);

        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.addJavascriptInterface(new WidgetBridge(listener), "ButtonPostTapAndroid");
        webView.loadUrl(templateUrl);

        // Enable transparency within the WebView
        webView.setBackgroundColor(Color.argb(1, 255, 255, 255));
    }

    /**
     * Destructively updates the layout of the widget i.e. prior positions and dimensions will
     * not be preserved.
     *
     * @param layout the new layout
     */
    public void updateLayout(final WidgetLayout layout) {
        post(new Runnable() {
            @Override
            public void run() {
                setLayout(layout);
            }
        });
    }

    /**
     * Evaluate a JavaScript function within the widget context, with some optional arguments.
     *
     * @param callbackFunctionName the JS function name
     * @param args optional arguments
     */
    public void runWidgetCallback(final String callbackFunctionName, @Nullable final String args) {
        final WebView webView = findViewById(R.id.btn_widget_webview);
        webView.post(new Runnable() {
            @Override
            public void run() {
                String message = String.format("%s(%s)", callbackFunctionName, args);
                webView.evaluateJavascript(message, null);
            }
        });
    }

    /**
     * Destructively updates the layout of the widget i.e. prior positions and constraints will
     * not be preserved.
     *
     * @param layout the new layout
     */
    private void setLayout(WidgetLayout layout) {
        ConstraintSet set = new ConstraintSet();

        if (layout.getHeight() != null) {
            set.constrainHeight(R.id.btn_widget_webview, toPixel(layout.getHeight()));
        }

        if (layout.getWidth() != null) {
            set.constrainWidth(R.id.btn_widget_webview, toPixel(layout.getWidth()));
        }

        if (layout.getTop() != null) {
            set.connect(R.id.btn_widget_webview, TOP, PARENT_ID, TOP, toPixel(layout.getTop()));
        }

        if (layout.getBottom() != null) {
            set.connect(R.id.btn_widget_webview, BOTTOM, PARENT_ID, BOTTOM,
                    toPixel(layout.getBottom()));
        }

        if (layout.getLeading() != null) {
            set.connect(R.id.btn_widget_webview, LEFT, PARENT_ID, LEFT,
                    toPixel(layout.getLeading()));
        }

        if (layout.getTrailing() != null) {
            set.connect(R.id.btn_widget_webview, RIGHT, PARENT_ID, RIGHT,
                    toPixel(layout.getTrailing()));
        }

        if (layout.getCenterX() != null) {
            set.connect(R.id.btn_widget_webview, RIGHT, PARENT_ID, RIGHT);
            set.connect(R.id.btn_widget_webview, LEFT, PARENT_ID, LEFT);
            set.setTranslationX(R.id.btn_widget_webview, toPixel(layout.getCenterX()));
        }

        if (layout.getCenterY() != null) {
            set.connect(R.id.btn_widget_webview, TOP, PARENT_ID, TOP);
            set.connect(R.id.btn_widget_webview, BOTTOM, PARENT_ID, BOTTOM);
            set.setTranslationY(R.id.btn_widget_webview, toPixel(layout.getCenterY()));
        }

        AutoTransition transition = new AutoTransition();
        transition.setDuration(layout.getAnimationDuration());
        transition.setInterpolator(new AccelerateDecelerateInterpolator());
        TransitionManager.beginDelayedTransition(this, transition);

        set.applyTo(this);
    }

    private int toPixel(int dp) {
        return (int) ButtonUtil.dpToPixel(getResources(), dp);
    }
}
