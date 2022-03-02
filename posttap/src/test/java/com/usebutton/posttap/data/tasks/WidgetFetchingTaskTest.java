/*
 * WidgetFetchingTaskTest.java
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

import com.usebutton.core.data.Task;
import com.usebutton.posttap.data.PostTapApi;
import com.usebutton.posttap.data.models.CollectionCampaign;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WidgetFetchingTaskTest {

    @Mock private PostTapApi api;
    @Mock private Task.Listener<CollectionCampaign> listener;
    @Mock private CollectionCampaign campaignData;

    private WidgetFetchingTask task;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(api.postCampaignEligibility()).thenReturn(campaignData);
        task = new WidgetFetchingTask(api, listener);
    }

    @Test
    public void execute_verifyApiCall() throws Exception {
        task.execute();

        verify(api).postCampaignEligibility();
    }
}
