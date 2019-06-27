/*
 * ConnectionManagerImpl.java
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
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.usebutton.merchant.exception.ButtonNetworkException;
import com.usebutton.merchant.exception.HttpStatusException;
import com.usebutton.merchant.exception.NetworkNotFoundException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Network helper class that abstracts away the initiation, protection, and error handling of new
 * HTTP connections. Responses from the network calls are parsed to a {@link NetworkResponse}.
 *
 * @see NetworkResponse
 */
final class ConnectionManagerImpl implements ConnectionManager {

    private static final String TAG = ConnectionManagerImpl.class.getSimpleName();

    private static ConnectionManager instance;

    private static final int CONNECT_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(5);
    private static final int READ_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(15);
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String ENCODING = "UTF-8";

    private final String baseUrl;
    private final String userAgent;

    static ConnectionManager getInstance(String baseUrl, String userAgent) {
        if (instance == null) {
            instance = new ConnectionManagerImpl(baseUrl, userAgent);
        }

        return instance;
    }

    @VisibleForTesting
    ConnectionManagerImpl(String baseUrl, String userAgent) {
        this.baseUrl = baseUrl;
        this.userAgent = userAgent;
    }

    @Override
    public NetworkResponse executeRequest(@NonNull ApiRequest request)
            throws ButtonNetworkException {
        HttpURLConnection urlConnection = null;

        try {
            urlConnection = getConnection(request.getPath());
            urlConnection.setRequestMethod(request.getRequestMethod().getValue());
            urlConnection.setRequestProperty("Content-Type", CONTENT_TYPE_JSON);

            Map<String, String> headers = request.getHeaders();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            JSONObject body = request.getBody();
            OutputStreamWriter writer =
                    new OutputStreamWriter(urlConnection.getOutputStream(), ENCODING);
            writer.write(body.toString());
            writer.close();

            int responseCode = urlConnection.getResponseCode();
            Log.d(TAG, "Request Body: " + body);
            Log.d(TAG, "Response Code: " + responseCode);

            if (responseCode >= 400) {
                String message = "Unsuccessful Request. HTTP StatusCode: " + responseCode;
                Log.e(TAG, message);
                throw new HttpStatusException(message, responseCode);
            }

            JSONObject responseJson = readResponseBody(urlConnection);
            return new NetworkResponse(responseCode, responseJson);
        } catch (IOException e) {
            Log.e(TAG, "Error has occurred", e);
            throw new NetworkNotFoundException(e);
        } catch (JSONException e) {
            Log.e(TAG, "Error has occurred", e);
            throw new ButtonNetworkException(e.getClass().getSimpleName() + " has occurred");
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private HttpURLConnection getConnection(String path)
            throws IOException {
        URL url = new URL(baseUrl + path);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
        urlConnection.setReadTimeout(READ_TIMEOUT);
        urlConnection.setRequestProperty("User-Agent", userAgent);
        urlConnection.setRequestProperty("Accept", CONTENT_TYPE_JSON);
        urlConnection.setDoOutput(true);

        return urlConnection;
    }

    private static JSONObject readResponseBody(HttpURLConnection connection)
            throws IOException, JSONException {
        InputStream in = new BufferedInputStream(connection.getInputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, ENCODING));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return new JSONObject(response.toString());
    }
}
