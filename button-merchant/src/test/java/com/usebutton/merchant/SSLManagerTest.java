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
import java.security.cert.X509Certificate;
import java.util.Set;

import static junit.framework.Assert.assertNotSame;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class SSLManagerTest {

    private MockWebServer server = new MockWebServer();
    private CertificateProvider provider = new LocalCertificateProvider();
    private SSLManager sslManager;

    @Before
    public void setUp() {
        sslManager = new SSLManagerImpl(provider, "localhost".toCharArray());
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
    public void getInstance_shouldReturnNewInstanceOnChangedPassword() throws Exception {
        SSLManager sslManager1 = SSLManagerImpl.getInstance(provider, "abc".toCharArray());
        SSLManager sslManager2 = SSLManagerImpl.getInstance(provider, "123".toCharArray());

        assertNotSame(sslManager1, sslManager2);
    }

    @Test(expected = IllegalStateException.class)
    public void initialization_shouldAssertMinimumCertificates() throws Exception {
        CertificateProvider provider = new CertificateProvider() {
            @Override
            String[] getCertificates() {
                return new String[0];
            }

            @Override
            Set<String> getPublicKeys() {
                return null;
            }

            @Override
            String getProviderName() {
                return null;
            }
        };
        new SSLManagerImpl(provider, null);
    }

    @Test
    public void keyStore_shouldContainCertificates() throws Exception {
        assertTrue(sslManager.getKeyStore().containsAlias("localhost_0"));
        assertTrue(sslManager.getKeyStore().containsAlias("localhost_1"));
    }

    @Test
    public void keyStore_shouldValidateCertificate() throws Exception {
        Certificate cert = sslManager.getKeyStore().getCertificate("localhost_0");
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