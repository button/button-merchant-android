/*
 * ButtonProduct.java
 *
 * Copyright (c) 2020 Button, Inc. (https://usebutton.com)
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

import androidx.annotation.Nullable;

import java.util.List;
import java.util.Map;

/**
 * A concrete implementation of the {@link ButtonProductCompatible} interface.
 */
public class ButtonProduct implements ButtonProductCompatible {

    @Nullable private String id;
    @Nullable private String upc;
    @Nullable private List<String> categories;
    @Nullable private String name;
    @Nullable private String currency;
    @Nullable private Integer value;
    @Nullable private Integer quantity;
    @Nullable private String url;
    @Nullable private Map<String, String> attributes;

    @Override
    @Nullable
    public String getId() {
        return id;
    }

    @Override
    @Nullable
    public String getUpc() {
        return upc;
    }

    @Override
    @Nullable
    public List<String> getCategories() {
        return categories;
    }

    @Override
    @Nullable
    public String getName() {
        return name;
    }

    @Override
    @Nullable
    public String getCurrency() {
        return currency;
    }

    @Override
    @Nullable
    public Integer getValue() {
        return value;
    }

    @Override
    @Nullable
    public Integer getQuantity() {
        return quantity;
    }

    @Override
    @Nullable
    public String getUrl() {
        return url;
    }

    @Override
    @Nullable
    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

    public void setUpc(@Nullable String upc) {
        this.upc = upc;
    }

    public void setCategories(@Nullable List<String> categories) {
        this.categories = categories;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public void setCurrency(@Nullable String currency) {
        this.currency = currency;
    }

    public void setValue(@Nullable Integer value) {
        this.value = value;
    }

    public void setQuantity(@Nullable Integer quantity) {
        this.quantity = quantity;
    }

    public void setUrl(@Nullable String url) {
        this.url = url;
    }

    public void setAttributes(@Nullable Map<String, String> attributes) {
        this.attributes = attributes;
    }
}
