/*
 * WidgetLayout.java
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

package com.usebutton.posttap.widgets;

import android.support.annotation.Nullable;

import org.json.JSONObject;

public final class WidgetLayout {

    public static final int DEFAULT_ANIMATION_DURATION = 650;

    @Nullable private Integer leading;
    @Nullable private Integer top;
    @Nullable private Integer trailing;
    @Nullable private Integer bottom;

    @Nullable private Integer centerX;
    @Nullable private Integer centerY;

    @Nullable private Integer width;
    @Nullable private Integer height;

    private int animationDuration = DEFAULT_ANIMATION_DURATION;

    @Nullable
    public Integer getLeading() {
        return leading;
    }

    public void setLeading(@Nullable Integer leading) {
        this.leading = leading;
    }

    @Nullable
    public Integer getTop() {
        return top;
    }

    public void setTop(@Nullable Integer top) {
        this.top = top;
    }

    @Nullable
    public Integer getTrailing() {
        return trailing;
    }

    public void setTrailing(@Nullable Integer trailing) {
        this.trailing = trailing;
    }

    @Nullable
    public Integer getBottom() {
        return bottom;
    }

    public void setBottom(@Nullable Integer bottom) {
        this.bottom = bottom;
    }

    @Nullable
    public Integer getCenterX() {
        return centerX;
    }

    public void setCenterX(@Nullable Integer centerX) {
        this.centerX = centerX;
    }

    @Nullable
    public Integer getCenterY() {
        return centerY;
    }

    public void setCenterY(@Nullable Integer centerY) {
        this.centerY = centerY;
    }

    @Nullable
    public Integer getWidth() {
        return width;
    }

    public void setWidth(@Nullable Integer width) {
        this.width = width;
    }

    @Nullable
    public Integer getHeight() {
        return height;
    }

    public void setHeight(@Nullable Integer height) {
        this.height = height;
    }

    public int getAnimationDuration() {
        return animationDuration;
    }

    public void setAnimationDuration(int animationDuration) {
        this.animationDuration = animationDuration;
    }

    public static WidgetLayout fromJson(JSONObject json) {
        WidgetLayout layout = new WidgetLayout();
        if (json == null) return layout;
        if (json.has("leading")) layout.setLeading(json.optInt("leading"));
        if (json.has("top")) layout.setTop(json.optInt("top"));
        if (json.has("trailing")) layout.setTrailing(json.optInt("trailing"));
        if (json.has("bottom")) layout.setBottom(json.optInt("bottom"));
        if (json.has("centerX")) layout.setCenterX(json.optInt("centerX"));
        if (json.has("centerY")) layout.setCenterY(json.optInt("centerY"));
        if (json.has("width")) layout.setWidth(json.optInt("width"));
        if (json.has("height")) layout.setHeight(json.optInt("height"));
        return layout;
    }
}
