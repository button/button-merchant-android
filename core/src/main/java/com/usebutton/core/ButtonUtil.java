/*
 * ButtonUtil.java
 *
 * Copyright (c) 2019 Button, Inc. (https://usebutton.com)
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

package com.usebutton.core;

import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Util class with helper methods
 */
public final class ButtonUtil {

    private static final String TAG = ButtonUtil.class.getSimpleName();

    private static final String ISO_8601 = "yyyy-MM-dd'T'HH:mm:ssZZZZZ";

    private static final SimpleDateFormat simpleDateFormat =
            new SimpleDateFormat(ISO_8601, Locale.US);

    /**
     * Regex from https://emailregex.com/
     */
    private static final String EMAIL_REGEX = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*"
            + "+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|"
            + "\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\"
            + ".)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)"
            + "\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08"
            + "\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f]"
            + ")+)\\])";

    private static final Pattern EMAIL_REGEX_PATTERN = Pattern.compile(EMAIL_REGEX);

    private static final Pattern APP_ID_PATTERN = Pattern.compile("^app-[0-9a-zA-Z]+$");

    /**
     * @param date date to be formatted
     * @return date formatted in ISO_8601 format
     */
    public static String formatDate(Date date) {
        return simpleDateFormat.format(date);
    }

    /**
     * @param timestamp timestamp to be formatted
     * @return date formatted in ISO_8601 format
     */
    public static String formatTimestamp(long timestamp) {
        return formatDate(new Date(timestamp));
    }

    public static String base64Encode(String value) {
        return Base64.encodeToString(value.getBytes(), Base64.NO_WRAP);
    }

    @Nullable
    public static String sha256Encode(String value) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = value.getBytes();
            messageDigest.update(bytes, 0, bytes.length);
            return encodeHex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error has occurred", e);
        }

        return null;
    }

    public static boolean isValidEmail(String email) {
        return EMAIL_REGEX_PATTERN.matcher(email).matches();
    }

    public static boolean isApplicationIdValid(@Nullable String applicationId) {
        return applicationId != null && APP_ID_PATTERN.matcher(applicationId).matches();
    }

    private static String encodeHex(byte[] digest) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
