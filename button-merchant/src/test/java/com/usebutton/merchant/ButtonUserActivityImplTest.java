/*
 * ButtonUserActivityImplTest.java
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

import com.usebutton.merchant.module.ButtonUserActivity;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ButtonUserActivityImplTest {

    private ButtonRepository buttonRepository;
    private ButtonUserActivity activityModule;

    @Before
    public void setUp() throws Exception {
        buttonRepository = mock(ButtonRepository.class);
        activityModule = new ButtonUserActivityImpl();
    }

    @Test
    public void productViewed_repositoryUnavailable_shouldQueueActivityEvent() {
        activityModule.productViewed(null);
        activityModule.productViewed(null);

        assertEquals(2, ((ButtonUserActivityImpl) activityModule).queuedActivityEvents.size());
        ((ButtonUserActivityImpl) activityModule).flushQueue(buttonRepository);

        verify(buttonRepository, times(2)).trackActivity(
                eq(ButtonUserActivityImpl.EVENT_PRODUCT_VIEWED),
                ArgumentMatchers.<ButtonProductCompatible>anyList()
        );
        assertEquals(0, ((ButtonUserActivityImpl) activityModule).queuedActivityEvents.size());
    }

    @Test
    public void productViewed_repositoryAvailable_shouldTrackActivity() {
        ((ButtonUserActivityImpl) activityModule).flushQueue(buttonRepository);

        activityModule.productViewed(null);
        activityModule.productViewed(null);
        assertEquals(0, ((ButtonUserActivityImpl) activityModule).queuedActivityEvents.size());

        verify(buttonRepository, times(2)).trackActivity(
                eq(ButtonUserActivityImpl.EVENT_PRODUCT_VIEWED),
                ArgumentMatchers.<ButtonProductCompatible>anyList()
        );
    }

    @Test
    public void productViewed_nullProduct_shouldTrackWithEmptyList() {
        ((ButtonUserActivityImpl) activityModule).flushQueue(buttonRepository);

        activityModule.productViewed(null);

        verify(buttonRepository).trackActivity(
                eq(ButtonUserActivityImpl.EVENT_PRODUCT_VIEWED),
                eq(new ArrayList<ButtonProductCompatible>())
        );
    }

    @Test
    public void productViewed_validProduct_shouldTrackWithProductInList() {
        ButtonProductCompatible product = new ButtonProduct();
        List<ButtonProductCompatible> products = new ArrayList<>();
        products.add(product);
        ((ButtonUserActivityImpl) activityModule).flushQueue(buttonRepository);

        activityModule.productViewed(product);

        verify(buttonRepository).trackActivity(ButtonUserActivityImpl.EVENT_PRODUCT_VIEWED,
                products);
    }

    @Test
    public void productAddedToCart_repositoryUnavailable_shouldQueueActivityEvent() {
        activityModule.productAddedToCart(null);
        activityModule.productAddedToCart(null);

        assertEquals(2, ((ButtonUserActivityImpl) activityModule).queuedActivityEvents.size());
        ((ButtonUserActivityImpl) activityModule).flushQueue(buttonRepository);

        verify(buttonRepository, times(2)).trackActivity(
                eq(ButtonUserActivityImpl.EVENT_ADD_TO_CART),
                ArgumentMatchers.<ButtonProductCompatible>anyList()
        );
        assertEquals(0, ((ButtonUserActivityImpl) activityModule).queuedActivityEvents.size());
    }

    @Test
    public void productAddedToCart_repositoryAvailable_shouldTrackActivity() {
        ((ButtonUserActivityImpl) activityModule).flushQueue(buttonRepository);

        activityModule.productAddedToCart(null);
        activityModule.productAddedToCart(null);
        assertEquals(0, ((ButtonUserActivityImpl) activityModule).queuedActivityEvents.size());

        verify(buttonRepository, times(2)).trackActivity(
                eq(ButtonUserActivityImpl.EVENT_ADD_TO_CART),
                ArgumentMatchers.<ButtonProductCompatible>anyList()
        );
    }

    @Test
    public void productAddedToCart_nullProduct_shouldTrackWithEmptyList() {
        ((ButtonUserActivityImpl) activityModule).flushQueue(buttonRepository);

        activityModule.productAddedToCart(null);

        verify(buttonRepository).trackActivity(
                eq(ButtonUserActivityImpl.EVENT_ADD_TO_CART),
                eq(new ArrayList<ButtonProductCompatible>())
        );
    }

    @Test
    public void productAddedToCart_validProduct_shouldTrackWithProductInList() {
        ButtonProductCompatible product = new ButtonProduct();
        List<ButtonProductCompatible> products = new ArrayList<>();
        products.add(product);
        ((ButtonUserActivityImpl) activityModule).flushQueue(buttonRepository);

        activityModule.productAddedToCart(product);

        verify(buttonRepository).trackActivity(ButtonUserActivityImpl.EVENT_ADD_TO_CART,
                products);
    }

    @Test
    public void cartViewed_repositoryUnavailable_shouldQueueActivityEvent() {
        activityModule.cartViewed(null);
        activityModule.cartViewed(null);

        assertEquals(2, ((ButtonUserActivityImpl) activityModule).queuedActivityEvents.size());
        ((ButtonUserActivityImpl) activityModule).flushQueue(buttonRepository);

        verify(buttonRepository, times(2)).trackActivity(
                eq(ButtonUserActivityImpl.EVENT_CART_VIEWED),
                ArgumentMatchers.<ButtonProductCompatible>anyList()
        );
        assertEquals(0, ((ButtonUserActivityImpl) activityModule).queuedActivityEvents.size());
    }

    @Test
    public void cartViewed_repositoryAvailable_shouldTrackActivity() {
        ((ButtonUserActivityImpl) activityModule).flushQueue(buttonRepository);

        activityModule.cartViewed(null);
        activityModule.cartViewed(null);
        assertEquals(0, ((ButtonUserActivityImpl) activityModule).queuedActivityEvents.size());

        verify(buttonRepository, times(2)).trackActivity(
                eq(ButtonUserActivityImpl.EVENT_CART_VIEWED),
                ArgumentMatchers.<ButtonProductCompatible>anyList()
        );
    }

    @Test
    public void cartViewed_nullProductList_shouldTrackWithEmptyList() {
        ((ButtonUserActivityImpl) activityModule).flushQueue(buttonRepository);

        activityModule.cartViewed(null);

        verify(buttonRepository).trackActivity(
                eq(ButtonUserActivityImpl.EVENT_CART_VIEWED),
                eq(new ArrayList<ButtonProductCompatible>())
        );
    }

    @Test
    public void cartViewed_validProductList_shouldTrackWithProductList() {
        ButtonProductCompatible product = new ButtonProduct();
        List<ButtonProductCompatible> products = new ArrayList<>();
        products.add(product);
        ((ButtonUserActivityImpl) activityModule).flushQueue(buttonRepository);

        activityModule.cartViewed(products);

        verify(buttonRepository).trackActivity(ButtonUserActivityImpl.EVENT_CART_VIEWED,
                products);
    }
}