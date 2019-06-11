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

import android.support.annotation.Nullable;

import java.util.Date;
import java.util.List;

/**
 * Represents an order placed by the user to be tracked using ButtonMerchant.trackOrder(order).
 */
public class Order {

    private String id;
    @Deprecated
    private long amount;
    private String currencyCode;
    @Nullable
    private Date purchaseDate;
    @Nullable
    private List<LineItem> lineItems;
    @Nullable
    private String sourceToken;
    @Nullable
    private String customerOrderId;
    @Nullable
    private Customer customer;

    /**
     * Constructor.
     *
     * @param builder {@link Builder}
     */
    private Order(Builder builder) {
        this.id = builder.id;
        this.amount = builder.amount;
        this.currencyCode = builder.currencyCode;
        this.purchaseDate = builder.purchaseDate;
        this.lineItems = builder.lineItems;
        this.sourceToken = builder.sourceToken;
        this.customerOrderId = builder.customerOrderId;
        this.customer = builder.customer;
    }

    public String getId() {
        return id;
    }

    @Deprecated
    public long getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    @Nullable
    public Date getPurchaseDate() {
        return purchaseDate;
    }

    @Nullable
    public List<LineItem> getLineItems() {
        return lineItems;
    }

    @Nullable
    public String getSourceToken() {
        return sourceToken;
    }

    @Nullable
    public String getCustomerOrderId() {
        return customerOrderId;
    }

    @Nullable
    public Customer getCustomer() {
        return customer;
    }

    /**
     * Builder class for tracking a customer {@link Order}.
     */
    public static class Builder {

        private String id;
        @Deprecated
        private long amount = 0;
        private String currencyCode = "USD";
        @Nullable
        private Date purchaseDate;
        @Nullable
        private List<LineItem> lineItems;
        @Nullable
        private String sourceToken;
        @Nullable
        private String customerOrderId;
        @Nullable
        private Customer customer;

        /**
         * Constructor.
         *
         * @param id The order identifier (required).
         */
        public Builder(String id) {
            this.id = id;
        }

        /**
         * The total order value in pennies (e.g. 3999 for $39.99) or the smallest decimal unit of
         * the currency code. (default is 0)
         */
        @Deprecated
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
         * The time the purchase was made by the user (required)
         */
        public Builder setPurchaseDate(Date purchaseDate) {
            this.purchaseDate = purchaseDate;
            return this;
        }

        /**
         * A list of the line item details that comprise the order (required)
         */
        public Builder setLineItems(List<LineItem> lineItems) {
            this.lineItems = lineItems;
            return this;
        }

        /**
         * The button source token
         */
        public Builder setSourceToken(String sourceToken) {
            this.sourceToken = sourceToken;
            return this;
        }

        /**
         * The customer-facing order id
         */
        public Builder setCustomerOrderId(String customerOrderId) {
            this.customerOrderId = customerOrderId;
            return this;
        }

        /**
         * The customer related to the order
         */
        public Builder setCustomer(Customer customer) {
            this.customer = customer;
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

    /**
     * Represents a line item in the order {@link Order#lineItems}
     */
    public class LineItem {

    }

    /**
     * Represents a customer in the order {@link Order#customer}
     */
    public class Customer {

    }
}
