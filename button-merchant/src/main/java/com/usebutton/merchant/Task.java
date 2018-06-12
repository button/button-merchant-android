/*
 * Task.java
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

import android.support.annotation.Nullable;

/**
 * Class that handles running the api request
 *
 * @param <T> type that the task returns
 */
abstract class Task<T> implements Runnable {

    @Nullable
    private final Listener<T> listener;

    Task(@Nullable Listener<T> listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            T object = execute();

            if (listener != null) {
                listener.onTaskComplete(object);
            }

        } catch (Exception e) {
            if (listener != null) {
                listener.onTaskError(e);
            }
        }
    }

    @Nullable
    abstract T execute() throws Exception;

    /**
     * Internal task callbacks.
     *
     * @param <T> task response type.
     */
    interface Listener<T> {
        void onTaskComplete(@Nullable T object);
        void onTaskError(Throwable throwable);
    }
}
