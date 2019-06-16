/*
 * SSLValidatorImpl.java
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
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.VisibleForTesting;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Helper class to validate a network connection against a set of public keys.
 */
final class SSLValidatorImpl implements SSLValidator {

    private final X509TrustManagerExtensions trustManagerExtensions;
    private final Encoder encoder;

    private static SSLValidatorImpl instance;
    private static GeneralSecurityException pendingException;

    @Nullable
    static SSLValidatorImpl getDefault() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (instance == null) {
                try {
                    TrustManagerFactory trustManagerFactory =
                            TrustManagerFactory.getInstance(
                                    TrustManagerFactory.getDefaultAlgorithm());
                    trustManagerFactory.init((KeyStore) null);
                    X509TrustManager trustManager = null;
                    for (TrustManager tm : trustManagerFactory.getTrustManagers()) {
                        if (tm instanceof X509TrustManager) {
                            trustManager = (X509TrustManager) tm;
                            break;
                        }
                    }
                    X509TrustManagerExtensions extension =
                            new X509TrustManagerExtensions(trustManager);
                    Encoder encoder = new AndroidEncoder();

                    instance = new SSLValidatorImpl(extension, encoder);
                    pendingException = null;
                } catch (KeyStoreException | NoSuchAlgorithmException e) {
                    pendingException = e;
                }
            }

            return instance;
        }

        return null;
    }

    @VisibleForTesting
    SSLValidatorImpl(X509TrustManagerExtensions trustManagerExtensions, Encoder encoder) {
        // Gate keep API level 16 and below
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            throw new IllegalStateException("Secure public key pinning only available on API17+");
        }

        this.trustManagerExtensions = trustManagerExtensions;
        this.encoder = encoder;
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void validatePinning(HttpsURLConnection conn, Set<String> validPins)
            throws IOException, KeyStoreException, NoSuchAlgorithmException {
        if (pendingException != null) {
            if (pendingException instanceof KeyStoreException) {
                throw (KeyStoreException) pendingException;
            }

            if (pendingException instanceof NoSuchAlgorithmException) {
                throw (NoSuchAlgorithmException) pendingException;
            }
        }

        conn.connect();

        String certChainMsg = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            List<X509Certificate> trustedChain = trustedChain(conn);
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

    private List<X509Certificate> trustedChain(HttpsURLConnection conn) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Certificate[] serverCerts = conn.getServerCertificates();
            X509Certificate[] untrustedCerts = Arrays.copyOf(serverCerts,
                    serverCerts.length, X509Certificate[].class);
            String host = conn.getURL().getHost();
            try {
                return trustManagerExtensions.checkServerTrusted(untrustedCerts,
                        "RSA", host);
            } catch (CertificateException e) {
                throw new SSLException(e);
            }
        }

        throw new IllegalStateException("Secure public key pinning only available on API17+");
    }
}
