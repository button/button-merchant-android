/*
 * SSLUtils.java
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

package com.usebutton.merchant;

import android.net.http.X509TrustManagerExtensions;
import android.os.Build;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Utility class to validate a network connection against a set of public keys.
 */
final class SSLUtils {

    static void validatePinning(X509TrustManagerExtensions trustManagerExt, Encoder encoder,
            HttpsURLConnection conn, Set<String> validPins)
            throws IOException {

        // Gate keep API level 16 and below
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            throw new IllegalStateException("Secure public key pinning only available on API17+");
        }

        conn.connect();

        String certChainMsg = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            List<X509Certificate> trustedChain = trustedChain(trustManagerExt, conn);
            for (X509Certificate cert : trustedChain) {
                byte[] publicKey = cert.getPublicKey().getEncoded();
                md.update(publicKey, 0, publicKey.length);
                String pin = encoder.encodeBase64ToString(md.digest());
                certChainMsg += "    sha256/" + pin + " : " + cert.getSubjectDN().toString() + "\n";
                if (validPins.contains(pin)) {
                    return;
                }
            }
        } catch (NoSuchAlgorithmException e) {
            throw new SSLException(e);
        }
        throw new SSLPeerUnverifiedException("Certificate pinning failure"
                + "\n\tPeer certificate chain:\n" + certChainMsg);
    }

    private static List<X509Certificate> trustedChain(X509TrustManagerExtensions trustManagerExt,
            HttpsURLConnection conn) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Certificate[] serverCerts = conn.getServerCertificates();
            X509Certificate[] untrustedCerts = Arrays.copyOf(serverCerts,
                    serverCerts.length, X509Certificate[].class);
            String host = conn.getURL().getHost();
            try {
                return trustManagerExt.checkServerTrusted(untrustedCerts,
                        "RSA", host);
            } catch (CertificateException e) {
                throw new SSLException(e);
            }
        }

        throw new IllegalStateException("Secure public key pinning only available on API17+");
    }
}
