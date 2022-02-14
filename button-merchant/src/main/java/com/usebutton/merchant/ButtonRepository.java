/*
 * ButtonRepository.java
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

import com.usebutton.core.data.DeviceManager;
import com.usebutton.core.data.Repository;
import com.usebutton.core.data.Task;
import com.usebutton.merchant.module.Features;

import java.util.List;

/**
 * Internal data layer interface.
 */
interface ButtonRepository extends Repository {

    void getPendingLink(DeviceManager deviceManager, Features features,
            Task.Listener<PostInstallLink> listener);

    boolean checkedDeferredDeepLink();

    void updateCheckDeferredDeepLink(boolean checkedDeferredDeepLink);

    void postOrder(Order order, DeviceManager deviceManager, Features features,
            Task.Listener listener);

    void trackActivity(String eventName, List<ButtonProductCompatible> products);

}
