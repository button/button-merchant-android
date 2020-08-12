/*
 * ButtonProductTest.java
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

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ButtonProductTest {

    private ButtonProduct product;

    @Before
    public void setUp() {
        product = new ButtonProduct();
    }

    @Test
    public void testGetSetId() {
        product.setId("test-id");

        assertEquals("test-id", ((ButtonProductCompatible) product).getId());
    }

    @Test
    public void testGetSetUpc() {
        product.setUpc("test-upc");

        assertEquals("test-upc", ((ButtonProductCompatible) product).getUpc());
    }

    @Test
    public void testGetSetCategories() {
        List<String> categories = new ArrayList<>();
        categories.add("category-one");
        categories.add("category-two");
        product.setCategories(categories);

        assertEquals(categories, ((ButtonProductCompatible) product).getCategories());
    }

    @Test
    public void testGetSetName() {
        product.setName("test-name");

        assertEquals("test-name", ((ButtonProductCompatible) product).getName());
    }

    @Test
    public void testGetSetCurrency() {
        product.setCurrency("test-code");

        assertEquals("test-code", ((ButtonProductCompatible) product).getCurrency());
    }

    @Test
    public void testGetSetValue() {
        product.setValue(123);

        assertEquals(new Integer(123), ((ButtonProductCompatible) product).getValue());
    }

    @Test
    public void testGetSetQuantity() {
        product.setQuantity(321);

        assertEquals(new Integer(321), ((ButtonProductCompatible) product).getQuantity());
    }

    @Test
    public void testGetSetUrl() {
        product.setUrl("test-url");

        assertEquals("test-url", ((ButtonProductCompatible) product).getUrl());
    }

    @Test
    public void testGetSetAttributes() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("key", "value");
        product.setAttributes(attributes);

        assertEquals(attributes, ((ButtonProductCompatible) product).getAttributes());
    }
}