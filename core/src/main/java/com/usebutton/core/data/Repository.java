/*
 * Repository.java
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

import android.support.annotation.Nullable;

import com.usebutton.core.data.models.Event;

/**
 * Interface to the repository layer for Button related services.
 */
public interface Repository {

    /**
     * Set the application ID associated with the current application.
     * Should be in the format `app-xxxxxxxxxxxxxxxx`.
     *
     * @param applicationId the Partner application ID
     */
    void setApplicationId(String applicationId);

    /**
     * Retrieve the application ID associated with the current application.
     *
     * @return the Partner application ID
     */
    @Nullable
    String getApplicationId();

    /**
     * Set the source token for the current shopping journey.
     * Should be in the format `srctok-xxxxxxxxxxxxxxxx`.
     *
     * @param sourceToken the source token for this shopping journey.
     */
    void setSourceToken(String sourceToken);

    /**
     * Retrieve the source token for the current shopping journey.
     *
     * @return the source token for this shopping journey
     */
    @Nullable
    String getSourceToken();

    /**
     * Report an event to Button.
     *
     * @param event the event to report
     * @see Event
     */
    void reportEvent(Event event);

    /**
     * Clear any cached and/or persisted data.
     */
    void clearAllData();

    /**
     * Submit the provided {@link Task} for execution.
     *
     * @param task the task to execute
     * @param forceSubmit if true, task is executed even if app has not yet configured
     */
    void submitTask(Task<?> task, boolean forceSubmit);
}
