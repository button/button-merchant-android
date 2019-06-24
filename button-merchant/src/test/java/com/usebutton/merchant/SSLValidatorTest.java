/*
 * SSLValidatorTest.java
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
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.X509TrustManager;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public class SSLValidatorTest {

    private SSLValidator sslValidator;
    private SSLManager sslManager = new MockSSLManager();
    private MockWebServer server = new MockWebServer();
    private X509TrustManagerExtensions extension = new MockTrustExtension(null);
    private Encoder encoder = new Encoder() {
        @Override
        public String encodeBase64ToString(byte[] bytes) {
            return LocalCertificateProvider.hashedKeys[0];
        }
    };

    @Before
    public void setUp() throws Exception {
        TestUtils.setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 17);
        sslValidator = new SSLValidatorImpl(extension, encoder);

        server.useHttps(sslManager.getSecureContext().getSocketFactory(), false);
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                return new MockResponse()
                        .setBody("Sample response")
                        .setResponseCode(200);
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        server.close();
    }

    @Test(expected = IllegalStateException.class)
    public void validator_shouldNotBeAvailableBelowApi17() throws Exception {
        TestUtils.setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 16);
        SSLValidator sslValidator = new SSLValidatorImpl(extension, encoder);
        sslValidator.validatePinning(null, null);
    }

    @Test
    public void getDefault_shouldNotBeAvailableBelowApi17() throws Exception {
        TestUtils.setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 16);
        SSLValidator sslValidator = SSLValidatorImpl.getDefault();
        assertNull(sslValidator);
    }

    @Test
    public void validatePinning_shouldValidateOnPairedPublicKey() throws Exception {
        URL url = server.url("").url();
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setSSLSocketFactory(sslManager.getSecureContext().getSocketFactory());

        sslValidator.validatePinning(conn, sslManager.getCertificateProvider().getPublicKeys());

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        assertEquals(reader.readLine(), "Sample response");
    }

    @Test(expected = SSLPeerUnverifiedException.class)
    public void validatePinning_shouldThrowErrorOnNoPairedPublicKey() throws Exception {
        URL url = server.url("").url();
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setSSLSocketFactory(sslManager.getSecureContext().getSocketFactory());

        Encoder wrongEncoder = new Encoder() {
            @Override
            public String encodeBase64ToString(byte[] bytes) {
                return "nonpairedSHA";
            }
        };

        SSLValidator sslValidator = new SSLValidatorImpl(extension, wrongEncoder);
        sslValidator.validatePinning(conn, sslManager.getCertificateProvider().getPublicKeys());
    }

    /* Private helper methods and classes */

    private class MockTrustExtension extends X509TrustManagerExtensions {

        public MockTrustExtension(X509TrustManager tm) throws IllegalArgumentException {
            super(tm);
        }

        @Override
        public List<X509Certificate> checkServerTrusted(X509Certificate[] chain, String authType,
                String host) throws CertificateException {
            try {
                return (List<X509Certificate>) (List) sslManager.getCertificateProvider()
                        .getChain();
            } catch (Exception e) {
                throw new CertificateException("Could not mock trusted chain");
            }
        }
    }
}