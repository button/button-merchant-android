/*
 * ButtonProductCompatible.java
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

import android.support.annotation.Nullable;

import java.util.List;
import java.util.Map;

/**
 * An interface that defines the product properties that may be provided when reporting user
 * activity.
 */
public interface ButtonProductCompatible {

    /**
     * @return the product identifier
     */
    @Nullable
    String getId();

    /**
     * @return the UPC (Universal Product Code) of the product
     */
    @Nullable
    String getUpc();

    /**
     * @return a flat array of the names of the categories to which the product belongs
     */
    @Nullable
    List<String> getCategories();

    /**
     * @return the name of the product
     */
    @Nullable
    String getName();

    /**
     * @return the ISO-4217 currency code in which the product's value is reported
     */
    @Nullable
    String getCurrency();

    /**
     * @return the value of the order. Includes any discounts, if applicable. Example: 1234 for $12.34
     */
    @Nullable
    Integer getValue();

    /**
     * @return the quantity of the product
     */
    @Nullable
    Integer getQuantity();

    /**
     * @return the URL of the product
     */
    @Nullable
    String getUrl();

    /**
     * @return any additional attributes to be included with the product
     */
    @Nullable
    Map<String, String> getAttributes();
}
