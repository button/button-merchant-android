/*
 * MemoryStoreImplTest.java
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

package com.usebutton.core.data;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MemoryStoreImplTest {

    private MemoryStore memoryStore;

    @Before
    public void setUp() throws Exception {
        memoryStore = new MemoryStoreImpl();
    }

    @Test
    public void getApplicationId_verifySetValue() {
        memoryStore.setApplicationId("app-xxxxxxxxxxxxxxxx");
        assertEquals("app-xxxxxxxxxxxxxxxx", memoryStore.getApplicationId());
    }

    @Test
    public void getIncludesIfa_verifySetValue() {
        memoryStore.setIncludesIfa(false);
        assertFalse(memoryStore.getIncludesIfa());
    }

    @Test
    public void getIncludesIfa_verifyDefaultValue() {
        assertTrue(memoryStore.getIncludesIfa());
    }
}