/*
 * Order.java
 *
 * Copyright (c) 2018 Button, Inc. (https://usebutton.com)
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

import android.support.annotation.NonNull;

/**
 * Represents an order placed by the user to be tracked using ButtonMerchant.trackOrder(order).
 */
public class Order {
    @NonNull
    private String id;
    private long amount;
    private String currencyCode;

    /**
     * Constructor.
     *
     * @param builder {@link Builder}
     */
    private Order(Builder builder) {
        this.id = builder.id;
        this.amount = builder.amount;
        this.currencyCode = builder.currencyCode;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public long getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     * Builder class for tracking a customer {@link Order}.
     */
    public static class Builder {
        @NonNull
        private String id;
        private long amount = 0;
        private String currencyCode = "USD";

        /**
         * Constructor.
         *
         * @param id The order identifier (required).
         */
        public Builder(@NonNull String id) {
            this.id = id;
        }

        /**
         * The total order value in pennies (e.g. 3999 for $39.99) or the smallest decimal unit of
         * the currency code. (default is 0)
         */
        public Builder setAmount(long amount) {
            this.amount = amount;
            return this;
        }

        /**
         * The ISO 4217 currency code. (default is USD).
         */
        public Builder setCurrencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
            return this;
        }

        /**
         * Builds and returns an Order Object
         *
         * @return {@link Order}
         */
        public Order build() {
            return new Order(this);
        }
    }
}
