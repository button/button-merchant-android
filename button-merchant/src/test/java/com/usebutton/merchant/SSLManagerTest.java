/*
 * SSLManagerTest.java
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

import javax.net.ssl.HttpsURLConnection;
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
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static junit.framework.Assert.assertNotSame;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class SSLManagerTest {

    private static final String[] CERTIFICATE_PATHS = {
            "/raw/ca.pem",
            "/raw/cert_local.pem",
    };

    private MockWebServer server = new MockWebServer();
    private SSLManager sslManager;

    @Before
    public void setUp() {
        sslManager = new SSLManagerImpl(CERTIFICATE_PATHS, "localhost".toCharArray());
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

    @Test
    public void getInstance_shouldReturnNewInstanceOnChangedCertificates() throws Exception {
        String[] chain1 = { "/raw/ca.pem" };
        String[] chain2 = { "/raw/cert_local.pem" };

        SSLManager sslManager1 = SSLManagerImpl.getInstance(chain1);
        SSLManager sslManager2 = SSLManagerImpl.getInstance(chain2);

        assertNotSame(sslManager1, sslManager2);
    }

    @Test
    public void getInstance_shouldReturnNewInstanceOnChangedPassword() throws Exception {
        SSLManager sslManager1 = SSLManagerImpl.getInstance(CERTIFICATE_PATHS, "abc".toCharArray());
        SSLManager sslManager2 = SSLManagerImpl.getInstance(CERTIFICATE_PATHS, "123".toCharArray());

        assertNotSame(sslManager1, sslManager2);
    }

    @Test(expected = IllegalStateException.class)
    public void initialization_shouldAssertMinimumCertificates() throws Exception {
        new SSLManagerImpl(new String[0], null);
    }

    @Test(expected = CertificateException.class)
    public void initialization_shouldCheckCertificatePaths() throws Exception {
        String[] paths = { "/raw/wrong_file.pem" };

        SSLManager sslManager = new SSLManagerImpl(paths, null);
        sslManager.getKeyStore();
    }

    @Test
    public void keyStore_shouldContainCertificates() throws Exception {
        assertTrue(sslManager.getKeyStore().containsAlias("ca"));
        assertTrue(sslManager.getKeyStore().containsAlias("cert_local"));
        assertFalse(sslManager.getKeyStore().containsAlias("cert_example"));
    }

    @Test
    public void keyStore_shouldValidateCertificate() throws Exception {
        Certificate cert = sslManager.getKeyStore().getCertificate("ca");
        assertTrue(cert instanceof X509Certificate);
        assertEquals(cert.getType(), "X.509");
    }

    @Test
    public void context_shouldBeSecure() throws Exception {
        assertEquals(sslManager.getSecureContext().getProtocol(), "TLS");
    }

    @Test
    public void context_shouldPinRequests() throws Exception {
        SSLManager s = new MockSSLManager();
        server.useHttps(s.getSecureContext().getSocketFactory(), false);
        server.start();

        URL url = server.url("").url();

        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setSSLSocketFactory(s.getSecureContext().getSocketFactory());
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        assertEquals(reader.readLine(), "Sample response");
    }
}