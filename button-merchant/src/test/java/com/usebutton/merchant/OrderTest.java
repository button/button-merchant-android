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
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class OrderTest {

    @Test
    public void order_idConstructor_verifyDefaultValues() {
        Order order = new Order.Builder("123").build();

        assertEquals(order.getId(), "123");
        assertEquals(order.getAmount(), 0);
        assertEquals(order.getCurrencyCode(), "USD");
        assertNull(order.getPurchaseDate());
        assertNull(order.getLineItems());
        assertNull(order.getCustomerOrderId());
        assertNull(order.getCustomer());
    }

    @Test
    public void order_idPurchaseDateLineItemConstructor_verifyDefaultValues() {
        String id = "123";
        Date purchaseDate = new Date();
        List<Order.LineItem> lineItems = Collections.emptyList();

        Order order = new Order.Builder(id, purchaseDate, lineItems).build();

        assertEquals(order.getId(), id);
        assertEquals(order.getAmount(), 0);
        assertEquals(order.getCurrencyCode(), "USD");
        assertEquals(order.getPurchaseDate(), purchaseDate);
        assertEquals(order.getLineItems(), lineItems);
        assertNull(order.getCustomerOrderId());
        assertNull(order.getCustomer());
    }

    @Test
    public void order_setAmount_verify() {
        Order order = new Order.Builder("123")
                .setAmount(1000)
                .build();

        assertEquals(order.getAmount(), 1000);
    }

    @Test
    public void order_setCurrencyCode_verify() {
        Order order = new Order.Builder("123", new Date(),
                Collections.<Order.LineItem>emptyList())
                .setCurrencyCode("AUG")
                .build();

        assertEquals(order.getCurrencyCode(), "AUG");
    }

    @Test
    public void order_setCustomerOrderId_verify() {
        String customerOrderId = "valid_customer_order_id";
        Order order = new Order.Builder("123", new Date(),
                Collections.<Order.LineItem>emptyList())
                .setCustomerOrderId(customerOrderId)
                .build();

        assertEquals(order.getCustomerOrderId(), customerOrderId);
    }

    @Test
    public void order_setCustomer_verify() {
        Order.Customer customer = mock(Order.Customer.class);
        Order order = new Order.Builder("123", new Date(),
                Collections.<Order.LineItem>emptyList())
                .setCustomer(customer)
                .build();

        assertEquals(order.getCustomer(), customer);
    }

    @Test
    public void orderLineItem_verifyDefaultValues() {
        Order.LineItem lineItem = new Order.LineItem.Builder("123").build();

        assertEquals(lineItem.getId(), "123");
        assertEquals(lineItem.getTotal(), 0);
        assertEquals(lineItem.getQuantity(), 0);
        assertNull(lineItem.getDescription());
        assertNull(lineItem.getSku());
        assertNull(lineItem.getUpc());
        assertNull(lineItem.getCategory());
        assertNull(lineItem.getAttributes());
    }

    @Test
    public void orderLineItem_setTotal_verify() {
        long total = 1000;
        Order.LineItem lineItem = new Order.LineItem.Builder("123")
                .setTotal(total)
                .build();

        assertEquals(lineItem.getTotal(), total);
    }

    @Test
    public void orderLineItem_setQuantity_verify() {
        int quantity = 5;
        Order.LineItem lineItem = new Order.LineItem.Builder("123")
                .setQuantity(quantity)
                .build();

        assertEquals(lineItem.getQuantity(), quantity);
    }

    @Test
    public void orderLineItem_setDescription_verify() {
        String description = "valid_description";
        Order.LineItem lineItem = new Order.LineItem.Builder("123")
                .setDescription(description)
                .build();

        assertEquals(lineItem.getDescription(), description);
    }

    @Test
    public void orderLineItem_setSku_verify() {
        String sku = "valid_sku";
        Order.LineItem lineItem = new Order.LineItem.Builder("123")
                .setSku(sku)
                .build();

        assertEquals(lineItem.getSku(), sku);
    }

    @Test
    public void orderLineItem_setUpc_verify() {
        String upc = "valid_upc";
        Order.LineItem lineItem = new Order.LineItem.Builder("123")
                .setUpc(upc)
                .build();

        assertEquals(lineItem.getUpc(), upc);
    }

    @Test
    public void orderLineItem_setCategory_verify() {
        List<String> category = Collections.singletonList("valid_category");
        Order.LineItem lineItem = new Order.LineItem.Builder("123")
                .setCategory(category)
                .build();

        assertEquals(lineItem.getCategory(), category);
    }

    @Test
    public void orderLineItem_setAttributes_verify() {
        Map<String, String> attributes =
                Collections.singletonMap("valid_attribute_key", "valid_attribute_value");
        Order.LineItem lineItem = new Order.LineItem.Builder("123")
                .setAttributes(attributes)
                .build();

        assertEquals(lineItem.getAttributes(), attributes);
    }
}
