/*
 * TtlReferenceTest.java
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

import com.usebutton.core.functional.TimeProvider;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TtlReferenceTest {

    @Test
    public void get_expired_shouldReturnNull() {
        String testObject = "Hello World";
        TestTimeProvider timeProvider = new TestTimeProvider();
        TtlReference<String> reference = new TtlReference<>(timeProvider, testObject, 5);
        timeProvider.setTime(6);

        assertNull(reference.get());
    }

    @Test
    public void get_live_shouldReturnValue() {
        String testObject = "Hello World";
        TestTimeProvider timeProvider = new TestTimeProvider();
        TtlReference<String> reference = new TtlReference<>(timeProvider, testObject, 5);
        timeProvider.setTime(4);

        assertEquals(testObject, reference.get());
    }

    @Test
    public void isDead_expired_shouldReturnTrue() {
        String testObject = "Hello World";
        TestTimeProvider timeProvider = new TestTimeProvider();
        TtlReference<String> reference = new TtlReference<>(timeProvider, testObject, 5);
        timeProvider.setTime(6);

        assertTrue(reference.isDead());
    }

    @Test
    public void isDead_live_shouldReturnFalse() {
        String testObject = "Hello World";
        TestTimeProvider timeProvider = new TestTimeProvider();
        TtlReference<String> reference = new TtlReference<>(timeProvider, testObject, 5);
        timeProvider.setTime(4);

        assertFalse(reference.isDead());
    }

    private static class TestTimeProvider implements TimeProvider {

        private long time;

        @Override
        public long getTimeInMs() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }
    }
}