/*
 * PostTapApiImplTest.java
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

package com.usebutton.posttap.data;

import com.usebutton.core.data.ApiRequest;
import com.usebutton.core.data.ConnectionManager;
import com.usebutton.core.data.models.NetworkResponse;
import com.usebutton.posttap.data.models.CollectionCampaign;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PostTapApiImplTest {

    private ConnectionManager connectionManager;
    private PostTapApi api;

    @Before
    public void setUp() {
        connectionManager = mock(ConnectionManager.class);
        api = new PostTapApiImpl(connectionManager);
    }

    @Test
    public void postCampaignEligibility_verifyRequestAndResponse() throws Exception {
        JSONObject body = new JSONObject("\n"
                + "{\n"
                + "  \"meta\": { \"status\": \"ok\" },\n"
                + "  \"object\": {\n"
                + "     \"smscampaign\": { \n"
                + "        \"smscampaign_id\": \"smscampaign-12345\", \n"
                + "        \"inapp_style\": { \n"
                + "           \"template\": \"https://cdn.usebutton.com/xxxxxxxxx.html\" \n"
                + "        }, \n"
                + "     }, \n"
                + "     \"check_widget_eligibility\": true\n"
                + "  }\n"
                + "}");
        NetworkResponse response = new NetworkResponse(200, body);
        ArgumentCaptor<ApiRequest> requestCaptor = ArgumentCaptor.forClass(ApiRequest.class);
        when(connectionManager.executeRequest(requestCaptor.capture())).thenReturn(response);

        CollectionCampaign campaign = api.postCampaignEligibility();

        ApiRequest request = requestCaptor.getValue();
        assertEquals(ApiRequest.RequestMethod.POST, request.getRequestMethod());
        assertEquals("/v1/app/collection-campaign/eligibility", request.getPath());

        JSONObject requestBody = request.getBody();
        assertNotNull(requestBody);

        assertNotNull(campaign);
        assertEquals("https://cdn.usebutton.com/xxxxxxxxx.html", campaign.getTemplateUrl());
        assertEquals("smscampaign-12345", campaign.getCampaignId());
        assertNotNull(campaign.asJson());
        assertNotNull(campaign.getStyles());
        assertNull(campaign.getExperimentalValues());
    }

    @Test
    public void postCampaignEnrollment_verifyRequestAndResponse() throws Exception {
        JSONObject body = new JSONObject("\n"
                + "{\n"
                + "  \"meta\": { \"status\": \"ok\" },\n"
                + "  \"object\": {\n"
                + "     \"enrollment_id\": \"enroll-54321\",\n"
                + "     \"check_widget_eligibility\": true\n"
                + "  }\n"
                + "}");
        NetworkResponse response = new NetworkResponse(200, body);
        ArgumentCaptor<ApiRequest> requestCaptor = ArgumentCaptor.forClass(ApiRequest.class);
        when(connectionManager.executeRequest(requestCaptor.capture())).thenReturn(response);

        String enrollmentId = api.postCampaignEnrollment(
                "https://cdn.usebutton.com/test.html",
                "smscampaign-xxxxxxxxxxxxxxxx",
                "+15161237890",
                "America/New_York"
        );

        ApiRequest request = requestCaptor.getValue();
        assertEquals(ApiRequest.RequestMethod.POST, request.getRequestMethod());
        assertEquals("/v1/app/collection-campaign/enrollment", request.getPath());

        JSONObject requestBody = request.getBody();
        assertNotNull(requestBody);
        assertEquals("https://cdn.usebutton.com/test.html", requestBody.getString("template"));
        assertEquals("smscampaign-xxxxxxxxxxxxxxxx", requestBody.getString("smscampaign_id"));
        assertEquals("+15161237890", requestBody.getString("phone_number"));
        assertEquals("America/New_York", requestBody.getString("timezone"));

        assertEquals("enroll-54321", enrollmentId);
    }
}
