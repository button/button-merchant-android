/*
 * PostTapApiImpl.java
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

import android.support.annotation.Nullable;
import android.util.Log;

import com.usebutton.core.data.ApiRequest;
import com.usebutton.core.data.ConnectionManager;
import com.usebutton.core.data.CoreApiImpl;
import com.usebutton.core.data.models.NetworkResponse;
import com.usebutton.core.exception.ButtonNetworkException;
import com.usebutton.posttap.data.models.CollectionCampaignData;

import org.json.JSONException;
import org.json.JSONObject;

public class PostTapApiImpl extends CoreApiImpl implements PostTapApi {

    private static final String TAG = PostTapApiImpl.class.getSimpleName();

    public PostTapApiImpl(ConnectionManager connectionManager) {
        super(connectionManager);
    }

    @Nullable
    @Override
    public CollectionCampaignData postCampaignEligibility() throws ButtonNetworkException {
        ApiRequest.RequestMethod method = ApiRequest.RequestMethod.POST;
        String endpoint = "/v1/app/collection-campaign/eligibility";

        try {
            ApiRequest apiRequest = new ApiRequest.Builder(method, endpoint).build();
            NetworkResponse response = connectionManager.executeRequest(apiRequest);
            JSONObject responseBody = response.getBody().optJSONObject("object");
            if (responseBody != null) {
                String templateUrl = responseBody.getString("template_url");
                JSONObject campaign = responseBody.getJSONObject("smscampaign");
                return new CollectionCampaignData(templateUrl, campaign);
            }
            return null;
        } catch (JSONException e) {
            Log.e(TAG, String.format("Error with %s %s", method, endpoint), e);
            throw new ButtonNetworkException(e);
        }
    }

    @Nullable
    @Override
    public String postCampaignEnrollment(String templateUrl, String campaignId, String phoneNumber,
            String timezone) throws ButtonNetworkException {
        ApiRequest.RequestMethod method = ApiRequest.RequestMethod.POST;
        String endpoint = "/v1/app/collection-campaign/enrollment";

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("template", templateUrl);
            requestBody.put("smscampaign_id", campaignId);
            requestBody.put("phone_number", phoneNumber);
            requestBody.put("timezone", timezone);

            ApiRequest apiRequest = new ApiRequest.Builder(method, endpoint)
                    .setBody(requestBody)
                    .build();
            NetworkResponse response = connectionManager.executeRequest(apiRequest);
            JSONObject responseBody = response.getBody().optJSONObject("object");
            if (responseBody != null) {
                return responseBody.optString("enrollment_id");
            }
            return null;
        } catch (JSONException e) {
            Log.e(TAG, String.format("Error with %s %s", method, endpoint), e);
            throw new ButtonNetworkException(e);
        }
    }
}
