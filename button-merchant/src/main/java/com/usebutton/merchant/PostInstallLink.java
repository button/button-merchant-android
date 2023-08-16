/*
 * PostInstallLink.java
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

import androidx.annotation.Nullable;

/**
 * Internal model for post-install link server response.
 */
final class PostInstallLink {

    private boolean match;
    private String id;

    @Nullable
    private String action;

    @Nullable
    private Attribution attribution;

    PostInstallLink(boolean match, String id, @Nullable String action,
            @Nullable Attribution attribution) {
        this.match = match;
        this.id = id;
        this.action = action;
        this.attribution = attribution;
    }

    boolean isMatch() {
        return match;
    }

    String getId() {
        return id;
    }

    @Nullable
    String getAction() {
        return action;
    }

    @Nullable
    Attribution getAttribution() {
        return attribution;
    }

    /**
     * Attribution and referrer tokens.
     */
    static final class Attribution {
        private String btnRef;
        @Nullable
        private String utmSource;

        Attribution(String btnRef, @Nullable String utmSource) {
            this.btnRef = btnRef;
            this.utmSource = utmSource;
        }

        String getBtnRef() {
            return btnRef;
        }

        @Nullable
        String getUtmSource() {
            return utmSource;
        }
    }
}
