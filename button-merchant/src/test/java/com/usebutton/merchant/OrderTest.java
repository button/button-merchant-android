/*
 * OrderTest.java
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

import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class OrderTest {

    @Test
    public void shouldBeDefaultValues() {
        Order order = new Order.Builder("123").build();

        assertEquals(order.getId(), "123");
        assertEquals(order.getAmount(), 0);
        assertEquals(order.getCurrencyCode(), "USD");
        assertNull(order.getPurchaseDate());
        assertNull(order.getLineItems());
        assertNull(order.getSourceToken());
        assertNull(order.getCustomerOrderId());
        assertNull(order.getCustomer());
    }

    @Test
    public void shouldSetAmount() {
        Order order = new Order.Builder("123").setAmount(1000).build();

        assertEquals(order.getAmount(), 1000);
    }

    @Test
    public void shouldSetCurrencyCode() {
        Order order = new Order.Builder("123").setCurrencyCode("AUG").build();

        assertEquals(order.getCurrencyCode(), "AUG");
    }

    @Test
    public void shouldSetPurchaseDate() {
        Date purchaseDate = new Date();
        Order order = new Order.Builder("123").setPurchaseDate(purchaseDate).build();

        assertEquals(order.getPurchaseDate(), purchaseDate);
    }

    @Test
    public void shouldSetLineItems() {
        List<Order.LineItem> lineItems = Collections.emptyList();
        Order order = new Order.Builder("123").setLineItems(lineItems).build();

        assertEquals(order.getLineItems(), lineItems);
    }

    @Test
    public void shouldSetSourceToken() {
        String sourceToken = "valid_source_token";
        Order order = new Order.Builder("123").setSourceToken(sourceToken).build();

        assertEquals(order.getSourceToken(), sourceToken);
    }

    @Test
    public void shouldSetCustomerOrderId() {
        String customerOrderId = "valid_customer_order_id";
        Order order = new Order.Builder("123").setCustomerOrderId(customerOrderId).build();

        assertEquals(order.getCustomerOrderId(), customerOrderId);
    }

    @Test
    public void shouldSetCustomer() {
        Order.Customer customer = mock(Order.Customer.class);
        Order order = new Order.Builder("123").setCustomer(customer).build();

        assertEquals(order.getCustomer(), customer);
    }
}
