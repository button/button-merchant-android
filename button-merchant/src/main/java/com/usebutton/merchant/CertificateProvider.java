/*
 * CertificateProvider.java
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Abstract class that provides public keys and certificates for a specific provider.
 */
public abstract class CertificateProvider {

    public List<Certificate> getChain() throws CertificateException, UnsupportedEncodingException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        List<Certificate> certs = new ArrayList<>();
        for (String certText : getCertificates()) {
            InputStream in = new ByteArrayInputStream(certText.getBytes("UTF-8"));
            X509Certificate cert = (X509Certificate) cf.generateCertificate(in);
            certs.add(cert);
        }

        return certs;
    }

    /**
     * Return PEM formatted X.509 certificates
     *
     * @return certificate strings
     */
    abstract String[] getCertificates();

    /**
     * Return the SHA-256 hashed unique public keys associated with this provider
     *
     * @return public keys
     */
    abstract Set<String> getPublicKeys();

    /**
     * Return the singular name of the certificate provider (e.g. the Certificate Authority)
     *
     * @return certificate provider name
     */
    abstract String getProviderName();

    @Override
    public int hashCode() {
        return Arrays.hashCode(getCertificates());
    }
}
