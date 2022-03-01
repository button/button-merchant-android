/*
 * WidgetFetchingTask.java
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

package com.usebutton.posttap.data.tasks;

import android.support.annotation.Nullable;

import com.usebutton.core.data.Task;
import com.usebutton.posttap.data.PostTapApi;
import com.usebutton.posttap.data.models.CollectionCampaignData;

public final class WidgetFetchingTask extends Task<CollectionCampaignData> {

    private final PostTapApi api;

    public WidgetFetchingTask(PostTapApi api,
            @Nullable Listener<CollectionCampaignData> listener) {
        super(listener);
        this.api = api;
    }

    @Nullable
    @Override
    protected CollectionCampaignData execute() throws Exception {
        return api.postCampaignEligibility();
    }
}
