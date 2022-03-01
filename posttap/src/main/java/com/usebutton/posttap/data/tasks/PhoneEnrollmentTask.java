/*
 * PhoneEnrollmentTask.java
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

import java.util.TimeZone;

public class PhoneEnrollmentTask extends Task<String> {

    private final PostTapApi api;
    private final String templateUrl;
    private final String campaignId;
    private final String phoneNumber;
    private final TimeZone timeZone;

    public PhoneEnrollmentTask(PostTapApi api, String templateUrl, String campaignId,
            String phoneNumber, TimeZone timeZone, @Nullable Listener<String> listener) {
        super(listener);
        this.api = api;
        this.templateUrl = templateUrl;
        this.campaignId = campaignId;
        this.phoneNumber = phoneNumber;
        this.timeZone = timeZone;
    }

    @Nullable
    @Override
    protected String execute() throws Exception {
        return api.postCampaignEnrollment(templateUrl, campaignId, phoneNumber, timeZone.getID());
    }
}
