/*
 * CollectionCampaign.java
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

package com.usebutton.posttap.data.models;

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class CollectionCampaign {

    private static final String TAG = CollectionCampaign.class.getSimpleName();

    private final JSONObject campaign;
    private final String campaignId;
    private final String templateUrl;
    private final JSONObject styles;
    private final JSONObject experimentalValues;

    private CollectionCampaign(JSONObject campaign, String campaignId, String templateUrl,
            JSONObject styles, @Nullable JSONObject experimentalValues) {
        this.campaign = campaign;
        this.campaignId = campaignId;
        this.templateUrl = templateUrl;
        this.styles = styles;
        this.experimentalValues = experimentalValues;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public String getTemplateUrl() {
        return templateUrl;
    }

    public JSONObject getStyles() {
        return styles;
    }

    @Nullable
    public JSONObject getExperimentalValues() {
        return experimentalValues;
    }

    public JSONObject asJson() {
        return campaign;
    }

    @Nullable
    public static CollectionCampaign fromJson(JSONObject json) {
        try {
            String campaignId = json.getString("smscampaign_id");
            JSONObject styles = json.getJSONObject("inapp_style");
            String templateUrl = styles.getString("template");
            JSONObject expValues = json.optJSONObject("experimental_values");
            return new CollectionCampaign(
                    json,
                    campaignId,
                    templateUrl,
                    styles,
                    expValues
            );
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing campaign", e);
            return null;
        }
    }
}
