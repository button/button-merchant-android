/*
 * ButtonUserActivity.java
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

package com.usebutton.merchant.module;

import androidx.annotation.Nullable;

import com.usebutton.merchant.ButtonProductCompatible;

import java.util.List;

/**
 * An interface through which user activities can be reported.
 */
public interface ButtonUserActivity {

    /**
     * Report that the user has viewed a product.
     *
     * @param product the viewed product
     */
    void productViewed(@Nullable ButtonProductCompatible product);

    /**
     * Report that the user has added a product to their cart.
     *
     * @param product the added product
     */
    void productAddedToCart(@Nullable ButtonProductCompatible product);

    /**
     * Report that the user viewed their cart.
     *
     * @param products the products in the cart
     */
    void cartViewed(@Nullable List<ButtonProductCompatible> products);
}
