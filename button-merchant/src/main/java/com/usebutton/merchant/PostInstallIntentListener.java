/*
 * PostInstallIntentListener.java
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
import android.support.annotation.Nullable;

/**
 * Callbacks for post-install intent handler.
 */
public interface PostInstallIntentListener {
    /**
     * This callback is used to notify the application that the request to check for a post-app
     * install deep link is complete. If a deep link was found, it will be returned here. If an
     * error was encountered while making the request, it will optionally be returned here.
     *
     * @param intent if a post-install link was found it will be returned here in the intent data.
     * The intent can be started to navigate the user to the appropriate location in the app. If no
     * post-install intent was found, this param will be {@code null}.
     * @param t if an error was encountered while making the request it will be returned in this
     * param, otherwise {@code null}.
     */
    void onResult(@Nullable Intent intent, @Nullable Throwable t);
}
