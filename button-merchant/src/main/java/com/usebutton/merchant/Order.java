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
import java.util.Map;

/**
 * Represents an order placed by the user to be tracked using ButtonMerchant.trackOrder(order).
 */
public class Order {

    private String id;
    @Deprecated
    private long amount;
    private String currencyCode;
    private Date purchaseDate;
    private List<LineItem> lineItems;
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

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public List<LineItem> getLineItems() {
        return lineItems;
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
        private Date purchaseDate;
        private List<LineItem> lineItems;
        @Nullable
        private String customerOrderId;
        @Nullable
        private Customer customer;

        /**
         * Constructor.
         *
         * @param id The order identifier (required).
         */
        @Deprecated
        public Builder(String id) {
            this.id = id;
        }

        /**
         * Constructor.
         *
         * @param id The order identifier (required).
         * @param purchaseDate The time the purchase was made by the user (required)
         * @param lineItems A list of the line item details that comprise the order (required)
         */
        public Builder(String id, Date purchaseDate, List<LineItem> lineItems) {
            this.id = id;
            this.purchaseDate = purchaseDate;
            this.lineItems = lineItems;
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
    public static class LineItem {

        private String id;
        private long total;
        @Nullable
        private Integer quantity;
        @Nullable
        private String description;
        @Nullable
        private String sku;
        @Nullable
        private String upc;
        @Nullable
        private List<String> category;
        @Nullable
        private Map<String, String> attributes;

        private LineItem(Builder builder) {
            this.id = builder.id;
            this.total = builder.total;
            this.quantity = builder.quantity;
            this.description = builder.description;
            this.sku = builder.sku;
            this.upc = builder.upc;
            this.category = builder.category;
            this.attributes = builder.attributes;
        }

        public String getId() {
            return id;
        }

        public long getTotal() {
            return total;
        }

        @Nullable
        public Integer getQuantity() {
            return quantity;
        }

        @Nullable
        public String getDescription() {
            return description;
        }

        @Nullable
        public String getSku() {
            return sku;
        }

        @Nullable
        public String getUpc() {
            return upc;
        }

        @Nullable
        public List<String> getCategory() {
            return category;
        }

        @Nullable
        public Map<String, String> getAttributes() {
            return attributes;
        }

        /**
         * Builder class for order line item {@link Order.LineItem}.
         */
        public static class Builder {

            private String id;
            private long total;
            private Integer quantity;
            @Nullable
            private String description;
            @Nullable
            private String sku;
            @Nullable
            private String upc;
            @Nullable
            private List<String> category;
            @Nullable
            private Map<String, String> attributes;

            /**
             * Constructor.
             *
             * @param id The unique identifier for this line item,
             * within the scope of this order. (required)
             * @param total The total price of all items bought in a particular line item
             * (e.g. if 3 bananas were purchased for $3.00 each, total would be 900) (required)
             */
            public Builder(String id, long total) {
                this.id = id;
                this.total = total;
            }

            /**
             * The number of unique units represented by this line item (default is 1)
             */
            public Builder setQuantity(int quantity) {
                this.quantity = quantity;
                return this;
            }

            /**
             * Text describing the line item
             */
            public Builder setDescription(String description) {
                this.description = description;
                return this;
            }

            /**
             * The Stock Keeping Unit of the line item
             */
            public Builder setSku(String sku) {
                this.sku = sku;
                return this;
            }

            /**
             * The Universal Product Code of the line item
             */
            public Builder setUpc(String upc) {
                this.upc = upc;
                return this;
            }

            /**
             * The category of the line item.
             * An ordered list of strings, starting with the topmost (or most general) category.
             */
            public Builder setCategory(List<String> category) {
                this.category = category;
                return this;
            }

            /**
             * A key/value store for strings to specify additional information about a line item
             */
            public Builder setAttributes(Map<String, String> attributes) {
                this.attributes = attributes;
                return this;
            }

            /**
             * Builds and returns an Order line item Object
             *
             * @return {@link Order.LineItem}
             */
            public Order.LineItem build() {
                return new LineItem(this);
            }
        }
    }

    /**
     * Represents a customer in the order {@link Order#customer}
     */
    public static class Customer {

        private String id;
        @Nullable
        private String email;

        private Customer(Builder builder) {
            this.id = builder.id;
            this.email = builder.email;
        }

        public String getId() {
            return id;
        }

        @Nullable
        public String getEmail() {
            return email;
        }

        /**
         * Builder class for order customer {@link Order.Customer}.
         */
        public static class Builder {

            private String id;
            @Nullable
            private String email;

            /**
             * @param id The id for the transacting customer in your system (required)
             */
            public Builder(String id) {
                this.id = id;
            }

            /**
             * The SHA-256 hash of the transacting customer’s lowercase email, as a 64-character
             * hex string.
             *
             * Note: The value of the e-mail address must be converted to lowercase before
             * computing the hash. The hash itself may use uppercase or lowercase hex characters.
             */
            public Builder setEmail(String email) {
                this.email = email;
                return this;
            }

            /**
             * Builds and returns an Order customer Object
             *
             * @return {@link Order.Customer}
             */
            public Order.Customer build() {
                return new Customer(this);
            }
        }
    }
}
