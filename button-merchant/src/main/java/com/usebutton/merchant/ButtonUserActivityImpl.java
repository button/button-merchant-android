/*
 * ButtonUserActivityImpl.java
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

import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.usebutton.merchant.module.ButtonUserActivity;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class ButtonUserActivityImpl implements ButtonUserActivity {

    @VisibleForTesting static final String EVENT_PRODUCT_VIEWED = "product-viewed";
    @VisibleForTesting static final String EVENT_ADD_TO_CART = "add-to-cart";
    @VisibleForTesting static final String EVENT_CART_VIEWED = "cart-viewed";

    private static ButtonUserActivity activity;

    @Nullable
    private ButtonRepository buttonRepository;
    // TODO: Move logic to ButtonRepository
    @VisibleForTesting
    List<Event> queuedActivityEvents = new CopyOnWriteArrayList<>();

    static ButtonUserActivity getInstance() {
        if (activity == null) {
            activity = new ButtonUserActivityImpl();
        }
        return activity;
    }

    @Override
    public void productViewed(@Nullable ButtonProductCompatible product) {
        Event event = new Event(
                EVENT_PRODUCT_VIEWED,
                product != null ? Collections.singletonList(product)
                        : Collections.<ButtonProductCompatible>emptyList()
        );

        trackOrQueueEvent(event);
    }

    @Override
    public void productAddedToCart(@Nullable ButtonProductCompatible product) {
        Event event = new Event(
                EVENT_ADD_TO_CART,
                product != null ? Collections.singletonList(product)
                        : Collections.<ButtonProductCompatible>emptyList()
        );

        trackOrQueueEvent(event);
    }

    @Override
    public void cartViewed(@Nullable List<ButtonProductCompatible> products) {
        Event event = new Event(
                EVENT_CART_VIEWED,
                products != null ? products : Collections.<ButtonProductCompatible>emptyList()
        );

        trackOrQueueEvent(event);
    }

    private void trackOrQueueEvent(Event event) {
        if (buttonRepository != null) {
            buttonRepository.trackActivity(event.name, event.products);
        } else {
            queuedActivityEvents.add(event);
        }
    }

    void flushQueue(ButtonRepository buttonRepository) {
        this.buttonRepository = buttonRepository;

        for (Event event : queuedActivityEvents) {
            buttonRepository.trackActivity(event.name, event.products);
        }
        queuedActivityEvents.clear();
    }

    private static class Event {
        private final String name;
        private final List<ButtonProductCompatible> products;

        public Event(String name, List<ButtonProductCompatible> products) {
            this.name = name;
            this.products = products;
        }
    }
}
