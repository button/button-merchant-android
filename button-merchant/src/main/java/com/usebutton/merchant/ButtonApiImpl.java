/*
 * ButtonApiImpl.java
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

import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.usebutton.merchant.exception.ButtonNetworkException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Button API endpoint request implementations.
 */
final class ButtonApiImpl implements ButtonApi {

    private static final String TAG = ButtonApiImpl.class.getSimpleName();

    private static final int CONNECT_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(5);
    private static final int READ_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(15);
    private static final String CONTENT_TYPE_JSON = "application/json";

    private final String userAgent;
    private final SSLManager sslManager;

    @VisibleForTesting
    String baseUrl = "https://api.usebutton.com";

    private static ButtonApi buttonApi;

    static ButtonApi getInstance(String userAgent, SSLManager sslManager) {
        if (buttonApi == null) {
            buttonApi = new ButtonApiImpl(userAgent, sslManager);
        }

        return buttonApi;
    }

    @VisibleForTesting
    ButtonApiImpl(String userAgent, SSLManager sslManager) {
        this.userAgent = userAgent;
        this.sslManager = sslManager;
    }

    @Nullable
    @WorkerThread
    @Override
    public PostInstallLink getPendingLink(String applicationId, String ifa,
            boolean limitAdTrackingEnabled, Map<String, String> signalsMap) throws
            ButtonNetworkException {
        HttpsURLConnection urlConnection = null;

        try {
            // create request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("application_id", applicationId);
            requestBody.put("ifa", ifa);
            requestBody.put("ifa_limited", limitAdTrackingEnabled);
            requestBody.put("signals", new JSONObject(signalsMap));

            // setup url connection
            final URL url = new URL(baseUrl + "/v1/web/deferred-deeplink");
            urlConnection = (HttpsURLConnection) url.openConnection();
            initializeUrlConnection(urlConnection);

            // write request body
            final OutputStreamWriter writer =
                    new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8");
            writer.write(requestBody.toString());
            writer.close();

            Log.d(TAG, "Request Body: " + requestBody);
            Log.d(TAG, "Response Code: " + urlConnection.getResponseCode());
            // check if it's successful
            if (urlConnection.getResponseCode() < 400) {
                // read response body
                final InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                final BufferedReader reader =
                        new BufferedReader(new InputStreamReader(in, "UTF-8"));
                final StringBuilder responseString = new StringBuilder();
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseString.append(line);
                    }
                } finally {
                    reader.close();
                }

                // parse response body
                JSONObject responseJson = new JSONObject(responseString.toString());
                responseJson = responseJson.optJSONObject("object");
                if (responseJson != null) {
                    boolean match = responseJson.getBoolean("match");
                    String id = responseJson.getString("id");
                    String action = responseJson.getString("action");
                    PostInstallLink.Attribution attribution = null;

                    JSONObject attributionJson = responseJson.optJSONObject("attribution");
                    if (attributionJson != null) {
                        String btnRef = attributionJson.getString("btn_ref");
                        String utmSource = attributionJson.optString("utm_source", null);
                        attribution = new PostInstallLink.Attribution(btnRef, utmSource);
                    }

                    return new PostInstallLink(match, id, action, attribution);
                }
            } else {
                String message =
                        "Unsuccessful Request. HTTP StatusCode: " + urlConnection.getResponseCode();
                Log.e(TAG, message);
                throw new ButtonNetworkException(message);
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException has occurred", e);
            throw new ButtonNetworkException(e);
        } catch (IOException e) {
            Log.e(TAG, "IOException has occurred", e);
            throw new ButtonNetworkException(e);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException has occurred", e);
            throw new ButtonNetworkException(e);
        } catch (CertificateException | KeyStoreException
                | NoSuchAlgorithmException | KeyManagementException e) {
            Log.e(TAG, e.getClass().getSimpleName() + " has occurred", e);
            throw new ButtonNetworkException(e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return null;
    }

    private void initializeUrlConnection(HttpsURLConnection urlConnection) throws
            CertificateException, KeyStoreException, NoSuchAlgorithmException, IOException,
            KeyManagementException {
        SSLContext sslContext = sslManager.getSecureContext();
        urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
        urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
        urlConnection.setReadTimeout(READ_TIMEOUT);
        urlConnection.setRequestProperty("User-Agent", getUserAgent());
        urlConnection.setRequestProperty("Accept", CONTENT_TYPE_JSON);
        urlConnection.setRequestProperty("Content-Type", CONTENT_TYPE_JSON);
        urlConnection.setRequestMethod("POST");
        urlConnection.setDoOutput(true);
    }

    @Override
    public Void postActivity(String applicationId, String sourceToken, String timestamp,
            Order order) throws ButtonNetworkException {
        HttpsURLConnection urlConnection = null;

        try {
            // create request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("app_id", applicationId);
            requestBody.put("user_local_time", timestamp);
            requestBody.put("btn_ref", sourceToken);
            requestBody.put("order_id", order.getId());
            requestBody.put("total", order.getAmount());
            requestBody.put("currency", order.getCurrencyCode());
            requestBody.put("source", "merchant-library");

            // setup url connection
            final URL url = new URL(baseUrl + "/v1/activity/order");
            urlConnection = (HttpsURLConnection) url.openConnection();
            initializeUrlConnection(urlConnection);

            // write request body
            final OutputStreamWriter writer =
                    new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8");
            writer.write(requestBody.toString());
            writer.close();

            // check if it's successful
            if (urlConnection.getResponseCode() < 400) {
                // read response body
                final InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                final BufferedReader reader =
                        new BufferedReader(new InputStreamReader(in, "UTF-8"));
                final StringBuilder responseString = new StringBuilder();
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseString.append(line);
                    }
                } finally {
                    reader.close();
                }

                // parse response body
                JSONObject responseJson = new JSONObject(responseString.toString());

                String status = responseJson.optJSONObject("meta").optString("status");
                if (status.equals("error")) {
                    String error = responseJson.optJSONObject("error").optString("message");
                    throw new ButtonNetworkException(error);
                }
            } else {
                String message =
                        "Unsuccessful Request. HTTP StatusCode: " + urlConnection.getResponseCode();
                Log.e(TAG, message);
                throw new ButtonNetworkException(message);
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException has occurred", e);
            throw new ButtonNetworkException(e);
        } catch (IOException e) {
            Log.e(TAG, "IOException has occurred", e);
            throw new ButtonNetworkException(e);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException has occurred", e);
            throw new ButtonNetworkException(e);
        } catch (CertificateException | KeyStoreException
                | NoSuchAlgorithmException | KeyManagementException e) {
            Log.e(TAG, e.getClass().getSimpleName() + " has occurred", e);
            throw new ButtonNetworkException(e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return null;
    }

    private String getUserAgent() {
        return userAgent;
    }
}
