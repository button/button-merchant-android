/*
 * TtlReference.java
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

package com.usebutton.core.data;

import android.os.SystemClock;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.usebutton.core.functional.TimeProvider;

/**
 * This class is a convenience class for objects that we want to expire after a certain amount
 * of time. When the ttl has elapsed against the time provider, {@link #get()} will return null.
 *
 * @param <T> type of object we're holding on to
 */
class TtlReference<T> {

    public static final TimeProvider REALTIME_MILLIS_PROVIDER = new TimeProvider() {
        @Override
        public long getTimeInMs() {
            return SystemClock.elapsedRealtime();
        }
    };

    private final T object;
    private final long timeOfDeath;
    private final TimeProvider timeProvider;

    /**
     *
     * @param object the object to maintain a reference to
     * @param ttl positive number of ms object should be accessible
     */
    TtlReference(@NonNull T object, @IntRange(from = 1) long ttl) {
        this(REALTIME_MILLIS_PROVIDER, object, ttl);
    }

    @VisibleForTesting
    TtlReference(@NonNull TimeProvider timeProvider, @NonNull T object,
            @IntRange(from = 1) long ttl) {
        this.timeProvider = timeProvider;
        this.object = object;
        this.timeOfDeath = timeProvider.getTimeInMs() + ttl;
    }

    /**
     * @return object or null if ttl has elapsed
     */
    public T get() {
        if (isDead()) {
            return null;
        }
        return object;
    }

    /**
     * @return true when ttl has elapsed
     */
    public boolean isDead() {
        return timeProvider.getTimeInMs() > timeOfDeath;
    }
}
