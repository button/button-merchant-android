/*
 * SSLManager.java
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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Network helper class that provides {@link KeyStore}s and {@link SSLContext} for use in secure
 * socket communications.
 */
class SSLManager {

    private KeyStore keyStore;
    private SSLContext sslContext;
    private Exception pendingException;

    private final char[] password;

    SSLManager(@NonNull String[] certificates, @Nullable char[] password) {
        if (certificates.length < 1) {
            throw new IllegalStateException(
                    "Must provide at least one certificate to pin network connections to!");
        }
        this.password = password;
        init(certificates);
    }

    KeyStore getKeyStore()
            throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException,
            KeyManagementException {
        checkExceptions();
        return keyStore;
    }

    SSLContext getSecureContext()
            throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException,
            KeyManagementException {
        checkExceptions();
        return sslContext;
    }

    KeyStore getKeyStore(String[] certPaths)
            throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, password);

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        for (String certPath : certPaths) {
            Pattern p = Pattern.compile("(?<=raw/).*?(?=.pem|.jks|.crt)");
            Matcher m = p.matcher(certPath);
            if (!m.find()) {
                continue;
            }

            String certName = m.group();
            InputStream ca = this.getClass().getResourceAsStream(certPath);
            X509Certificate certificate = (X509Certificate) cf.generateCertificate(ca);
            keyStore.setCertificateEntry(certName, certificate);
        }

        return keyStore;
    }

    private void init(String[] certPaths) {
        try {
            keyStore = getKeyStore(certPaths);

            String kmfAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(kmfAlgorithm);
            kmf.init(keyStore, password);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(kmfAlgorithm);
            trustManagerFactory.init(keyStore);

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(kmf.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            sslContext = context;
            pendingException = null;
        } catch (GeneralSecurityException | IOException e) {
            pendingException = e;
        }
    }

    private void checkExceptions()
            throws CertificateException, KeyStoreException, NoSuchAlgorithmException,
            KeyManagementException, IOException {
        if (pendingException != null) {
            if (pendingException instanceof CertificateException) {
                throw (CertificateException) pendingException;
            }
            if (pendingException instanceof KeyStoreException) {
                throw (KeyStoreException) pendingException;
            }
            if (pendingException instanceof NoSuchAlgorithmException) {
                throw (NoSuchAlgorithmException) pendingException;
            }
            if (pendingException instanceof KeyManagementException) {
                throw (KeyManagementException) pendingException;
            }
            if (pendingException instanceof IOException) {
                throw (IOException) pendingException;
            }
        }
    }
}
