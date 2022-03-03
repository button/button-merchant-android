/*
 * ConnectionManagerImpl.java
 *
 * Copyright (c) 2022 Button, Inc. (https://usebutton.com)
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

package com.usebutton.core.data;

import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.usebutton.core.data.models.NetworkResponse;
import com.usebutton.core.exception.ButtonNetworkException;
import com.usebutton.core.exception.HttpStatusException;
import com.usebutton.core.exception.NetworkNotFoundException;

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

public class ConnectionManagerImpl implements ConnectionManager {

    private static final String TAG = ConnectionManagerImpl.class.getSimpleName();

    private static final int CONNECT_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(5);
    private static final int READ_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(15);
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String ENCODING = "UTF-8";

    @VisibleForTesting
    String baseUrl;
    private final String userAgent;
    private final PersistentStore persistentStore;
    private final MemoryStore memoryStore;

    public ConnectionManagerImpl(String baseUrl, String userAgent, PersistentStore persistentStore,
            MemoryStore memoryStore) {
        this.baseUrl = baseUrl;
        this.userAgent = userAgent;
        this.persistentStore = persistentStore;
        this.memoryStore = memoryStore;
    }

    public void updateBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public NetworkResponse executeRequest(ApiRequest request) throws ButtonNetworkException {
        HttpURLConnection urlConnection = null;

        try {
            urlConnection = getConnection(request.getPath());
            urlConnection.setRequestMethod(request.getRequestMethod().getValue());
            urlConnection.setRequestProperty("Content-Type", CONTENT_TYPE_JSON);

            Map<String, String> headers = request.getHeaders();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            JSONObject body = request.getBody() != null ? request.getBody() : new JSONObject();

            // Append necessary information to each request
            body.put("application_id", memoryStore.getApplicationId());
            body.put("session_id", persistentStore.getSessionId());
            body.put("source_token", persistentStore.getSourceToken());

            OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream(),
                    ENCODING);
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
            refreshSessionIfAvailable(responseJson);
            refreshSourceTokenIfAvailable(responseJson);
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

    /**
     * Refreshes the current session if provided in the network response.
     * If a null session is provided, the Library data is cleared.
     *
     * @param responseBody the JSON body of the network response
     */
    private void refreshSessionIfAvailable(@Nullable JSONObject responseBody) {
        if (responseBody == null) return;

        try {
            JSONObject metaJson = responseBody.getJSONObject("meta");
            if (metaJson.has("session_id")) {
                String sessionId = metaJson.optString("session_id", null);
                if (sessionId != null && !sessionId.isEmpty()) {
                    persistentStore.setSessionId(sessionId);
                } else {
                    persistentStore.clearAllData();
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing session data from response body", e);
        }
    }

    /**
     * Refreshes the current source token if provided in the network response.
     * Older source token is retained if no new token was provided.
     *
     * @param responseBody the JSON body of the network response
     */
    private void refreshSourceTokenIfAvailable(@Nullable JSONObject responseBody) {
        if (responseBody == null) return;

        try {
            JSONObject metaJson = responseBody.getJSONObject("meta");
            if (metaJson.has("source_token")) {
                String sourceToken = metaJson.optString("source_token", "");
                if (!sourceToken.isEmpty()) {
                    persistentStore.setSourceToken(sourceToken);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing session data from response body", e);
        }
    }
}