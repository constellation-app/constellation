/*
 * Copyright 2010-2020 Australian Signals Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.gov.asd.tac.constellation.utilities.rest;

import au.gov.asd.tac.constellation.utilities.https.HttpsUtilities;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import org.openide.util.Exceptions;

/**
 * REST Client
 *
 * @author arcturus
 */
public abstract class RestClient {

    protected static final String HOST = "{HOST}";

    /**
     * Read the content of the HTTP response.
     *
     * @param conn The HTTPS connection.
     *
     * @return The HTTP response content.
     *
     * @throws IOException
     */
    private static byte[] getBody(final HttpsURLConnection conn, final int code) throws IOException {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final byte[] bytes = new byte[256 * 1024];
        try (final InputStream in = code / 100 == 2 ? HttpsUtilities.getInputStream(conn) : HttpsUtilities.getErrorStream(conn)) {
            if (in != null) {
                while (true) {
                    final int len = in.read(bytes);
                    if (len == -1) {
                        break;
                    }

                    os.write(bytes, 0, len);
                }
            }
        }

        return os.toByteArray();
    }

    public static URL generateUrl(final String url, final Map<String, String> params) throws UnsupportedEncodingException, MalformedURLException {
        // Build the request parameters.
        final StringBuilder query = new StringBuilder();
        if (params != null) {
            for (final Map.Entry<String, String> entry : params.entrySet()) {
                if (query.length() > 0) {
                    query.append('&');
                }

                query.append(String.format(
                        "%s=%s",
                        entry.getKey() == null ? "" : URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name()),
                        entry.getValue() == null ? "" : URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name())
                ));
            }
        }

        return new URL(url + (query.length() > 0 ? "?" + query : ""));
    }

    /**
     * HTTP response code.
     */
    protected int responseCode;

    /**
     * HTTP response message.
     */
    protected String responseMessage;

    /**
     * HTTP response headers.
     */
    protected Map<String, List<String>> headerFields;

    /**
     * HTTP response content.
     */
    protected byte[] bytes;

    public abstract HttpsURLConnection makeGetConnection(final String url, final Map<String, String> params) throws IOException;

    /**
     * Will be run before the GET connection
     *
     * @param url The URL to request.
     * @param params Query parameters.
     */
    public void beforeGet(final String url, final Map<String, String> params) {
        // DO NOTHING
    }

    /**
     * A generic "send request / read response" method.
     * <p>
     * The REST calls each return a body containing a JSON document.
     *
     * @param url The URL to request.
     * @param params Query parameters.
     *
     * @throws IOException
     */
    public void get(final String url, final Map<String, String> params) throws IOException {
        beforeGet(url, params);

        HttpsURLConnection connection = null;
        try {
            connection = makeGetConnection(url, params);

            responseCode = connection.getResponseCode();
            responseMessage = connection.getResponseMessage();
            headerFields = connection.getHeaderFields();

            bytes = null;
            if (Response.isCodeSuccess(responseCode)) {
                bytes = getBody(connection, responseCode);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        afterGet(url, params);
    }

    /**
     * Will be run after the GET connection has been disconnected
     *
     * @param url The URL to request.
     * @param params Query parameters.
     */
    public void afterGet(final String url, final Map<String, String> params) {
        // DO NOTHING
    }

    public abstract HttpsURLConnection makePostConnection(final String url, final Map<String, String> params) throws IOException;

    /**
     * Will be run before the POST connection
     *
     * @param url The URL to request.
     * @param params Query parameters.
     */
    public void beforePost(final String url, final Map<String, String> params) {
        // DO NOTHING
    }

    /**
     * Post method which will convert the {@code params} to a JSON body. At the
     * moment this method only supports a simple JSON object. That is, it does
     * not convert arrays into JSON.
     *
     * @param url The URL to request
     * @param params A simple key/value pair in which values do not contain
     * Arrays, Sets etc
     *
     * @throws IOException
     */
    public void post(final String url, final Map<String, String> params) throws IOException {
        beforePost(url, params);

        HttpsURLConnection connection = null;
        try {
            connection = makePostConnection(url, params);

            try (final BufferedWriter request = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8.name()))) {
                request.write(generateJsonFromFlatMap(params));
                request.flush();
            }

            responseCode = connection.getResponseCode();
            responseMessage = connection.getResponseMessage();
            headerFields = connection.getHeaderFields();

            bytes = null;
            if (Response.isCodeSuccess(responseCode)) {
                bytes = getBody(connection, responseCode);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        afterPost(url, params);
    }

    /**
     * Post method similar to {@code post} but has the option to supply a json
     * string that will be posted in the message body.
     *
     * @param url The URL to request
     * @param params A simple key/value pair in which values do not contain
     * Arrays, Sets etc
     * @param json The json string to be posted in the message body
     *
     * @throws IOException
     */
    public void postWithJson(final String url, final Map<String, String> params, final String json) throws IOException {
        beforePost(url, params);

        HttpsURLConnection connection = null;
        try {
            connection = makePostConnection(url, params);

            try (final BufferedWriter request = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8.name()))) {
                request.write(json);
                request.flush();
            }

            responseCode = connection.getResponseCode();
            responseMessage = connection.getResponseMessage();
            headerFields = connection.getHeaderFields();

            bytes = null;
            if (Response.isCodeSuccess(responseCode)) {
                bytes = getBody(connection, responseCode);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        afterPost(url, params);
    }

    /**
     * Post method similar to {@code post} but has the option to supply a byte
     * array that will be posted in the message body.
     *
     * @param url The URL to request
     * @param params A simple key/value pair in which values do not contain
     * Arrays, Sets etc
     * @param bytes The bytes to be posted in the message body
     *
     * @throws IOException
     */
    public void postWithBytes(final String url, final Map<String, String> params, final byte[] bytes) throws IOException {
        beforePost(url, params);

        HttpsURLConnection connection = null;
        try {
            connection = makePostConnection(url, params);
            try (final DataOutputStream request = new DataOutputStream(connection.getOutputStream())) {
                request.write(bytes);
                request.flush();
            }

            responseCode = connection.getResponseCode();
            responseMessage = connection.getResponseMessage();
            headerFields = connection.getHeaderFields();

            this.bytes = null;
            if (Response.isCodeSuccess(responseCode)) {
                this.bytes = getBody(connection, responseCode);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        afterPost(url, params);
    }

    /**
     * Will be run before the POST connection has been disconnected
     *
     * @param url The URL to request.
     * @param params Query parameters.
     */
    public void afterPost(final String url, final Map<String, String> params) {
        // DO NOTHING
    }

    /**
     * Generate a json string from a flat Map<String, String> of key and values
     *
     * @param params A Map of key/value pairs
     * @return A json representation of a simple map
     *
     * @throws IOException
     */
    private String generateJsonFromFlatMap(final Map<String, String> params) throws IOException {
        final ByteArrayOutputStream json = new ByteArrayOutputStream();
        final JsonFactory jsonFactory = new MappingJsonFactory();
        try (JsonGenerator jg = jsonFactory.createGenerator(json)) {
            jg.writeStartObject();
            for (final Map.Entry<String, String> param : params.entrySet()) {
                jg.writeStringField(param.getKey(), param.getValue());
            }
            jg.writeEndObject();
            jg.flush();
        }

        return json.toString(StandardCharsets.UTF_8.name());
    }

}
