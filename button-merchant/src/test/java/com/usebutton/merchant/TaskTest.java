/*
 * TaskTest.java
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

import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class TaskTest {

    @Test
    public void run_verifyListenerCallback() {
        Task.Listener<String> listener = mock(Task.Listener.class);

        Task<String> task = new Task<String>(listener) {
            @Override
            String execute() {
                return "test";
            }
        };

        task.run();

        verify(listener).onTaskComplete("test");
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void run_throwException_verifyListenerCallback() {
        Task.Listener<String> listener = mock(Task.Listener.class);

        Task<String> task = new Task<String>(listener) {
            @Nullable
            @Override
            String execute() throws Exception {
                throw new RuntimeException();
            }
        };

        task.run();

        verify(listener).onTaskError(any(RuntimeException.class));
        verifyNoMoreInteractions(listener);
    }

}