/*
 * Copyright 2010-2021 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.utilities.log.ConnectionLogging;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.utilities.https.HttpsUtilities;
import au.gov.asd.tac.constellation.utilities.log.LogPreferences;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.lang3.StringUtils;

/**
 * REST Client
 *
 * @author arcturus
 */
public abstract class RestClient {
    
    private static final Logger LOGGER = Logger.getLogger(RestClient.class.getName());

    protected static final String HOST = "{HOST}";
    private static final String DASH_STRING = "----\n";

    /**
     * Read the content of the HTTP response.
     *
     * @param conn The HTTPS connection.
     * @return The HTTP response content.
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
        //ConnectionLogger.log(Level.FINE, new String(os.toByteArray()), null);
        return os.toByteArray();
    }

    /**
     * Construct a URL string based on supplied URL and any supplied query
     * parameters.
     * 
     * @param url URL to base generated URL on.
     * @param params Any parameters to add to the URL (ie HTTP GET parameters)
     * @return URL combining base URL and parameters.
     * @throws UnsupportedEncodingException
     * @throws MalformedURLException
     */
    public static URL generateUrl(final String url, final List<Tuple<String, String>> params) throws UnsupportedEncodingException, MalformedURLException {
        // Build the request parameters.
        final StringBuilder query = new StringBuilder();
        if (params != null) {
            for (final Tuple<String, String> param : params) {
                // Ensure the parameter has a non empty key. Rules for parameter names seem quite relaxed in HTTP, as
                // such we will not try and do too much validation
                final String key = URLEncoder.encode(param.getFirst(), StandardCharsets.UTF_8.name()).replace("+", "%20");
                final String value = URLEncoder.encode(param.getSecond(), StandardCharsets.UTF_8.name()).replace("+", "%20");
                if (StringUtils.isNotBlank(key)) {
                    if (query.length() > 0) {
                        query.append('&');
                    }
                    query.append(String.format("%s=%s", key, value));
                } else {
                    LOGGER.info(String.format("Unable to add rest key/value: %s=%s to URL=%s", key, value, url));
                } 
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
    
    /**
     * Manage the creation of a HTTP GET connection.
     * 
     * @param url The URL to base the GET connection on.
     * @param params Any parameters to add to the URL
     * @return HttpsURLConnection object corresponding to the created connection
     * @throws IOException 
     */
    public abstract HttpsURLConnection makeGetConnection(final String url, final List<Tuple<String, String>> params) throws IOException;

    /**
     * Will be run before the GET connection - designed to be overridden as
     * required.
     *
     * @param url The URL to request.
     * @param params Query parameters.
     */
    public void beforeGet(final String url, final List<Tuple<String, String>> params) {
        // DO NOTHING
    }
    
    /**
     * A generic "send request / read response" method.
     * <p>
     * The REST calls each return a body containing a JSON document.
     *
     * @param url The URL to request.
     * @param params Query parameters.
     * @throws IOException
     */
    public void get(final String url, final List<Tuple<String, String>> params) throws IOException {
        logRequest(url, params, null);
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
        logResponse();
    }

    /**
     * Will be run after the GET connection has been disconnected - designed to
     * be overridden as required.
     *
     * @param url The URL to request.
     * @param params Query parameters.
     */
    public void afterGet(final String url, final List<Tuple<String, String>> params) {
        // DO NOTHING
    }
    
    /**
     * Manage the creation of a HTTP POST connection.
     * 
     * @param url The URL to base the POST connection on.
     * @param params Any parameters to add to the URL
     * @return HttpsURLConnection object corresponding to the created connection
     * @throws IOException
     */
    public abstract HttpsURLConnection makePostConnection(final String url, final List<Tuple<String, String>> params) throws IOException;

    /**
     * Will be run before the POST connection - designed to be overridden as
     * required.
     *
     * @param url The URL to request.
     * @param params Query parameters.
     */
    public void beforePost(final String url, final List<Tuple<String, String>> params) {
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
    public void post(final String url, final List<Tuple<String, String>> params) throws IOException {
        logRequest(url, params, null);
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
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        afterPost(url, params);
        logResponse();
    }

    /**
     * Post method similar to {@code post} but has the option to supply a json
     * string that will be posted in the message body.
     *
     * @param url The URL to request
     * @param params A simple key/value pair in which values do not contain
     * Arrays, Sets etc
     * @param json The json string to be posted in the message body
     * @throws IOException
     */
    public void postWithJson(final String url, final List<Tuple<String, String>> params, final String json) throws IOException {
        logRequest(url, params, json);
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
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        afterPost(url, params);
        logResponse();
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
    public void postWithBytes(final String url, final List<Tuple<String, String>> params, final byte[] bytes) throws IOException {
        logRequestBytes(url, params, bytes);
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
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        afterPost(url, params);
        logResponse();
    }

    /**
     * Will be run before the POST connection has been disconnected - designed
     * to be overridden as required.
     *
     * @param url The URL to request.
     * @param params Query parameters.
     */
    public void afterPost(final String url, final List<Tuple<String, String>> params) {
        // DO NOTHING
    }

    /**
     * Generate a json string from a flat Map<String, String> of key and values
     *
     * @param params A Map of key/value pairs
     * @return A json representation of a simple map
     * @throws IOException
     */
    private String generateJsonFromFlatMap(final List<Tuple<String, String>> params) throws IOException {
        final ByteArrayOutputStream json = new ByteArrayOutputStream();
        final JsonFactory jsonFactory = new MappingJsonFactory();
        try (final JsonGenerator jg = jsonFactory.createGenerator(json)) {
            jg.writeStartObject();
            
            for (final Tuple<String, String> param : params) {
                // Ensure the parameter has a non empty key.
                final String key = param.getFirst();
                if (StringUtils.isNotBlank(key)) {
                    jg.writeStringField(key,  param.getSecond());
                }
            }
            jg.writeEndObject();
            jg.flush();
        }

        return json.toString(StandardCharsets.UTF_8.name());
    }
    
    /**
     * Logs the request if ConnectionLogging is enabled
     *
     * @param url The URL to request
     * @param params A simple key/value pair of request parameters
     * @param messageBytes The bytes to be posted in the message body
     *     
     */
    public void logRequestBytes(final String url, final List<Tuple<String, String>> params, final byte[] messageBytes) {
        if (LogPreferences.isConnectionLoggingEnabled()) {
            final StringBuilder sb = new StringBuilder();
            if (messageBytes != null && messageBytes.length > 0) {
                try {
                    sb.append(new String(bytes, StandardCharsets.UTF_8.name()));
                } catch (final UnsupportedEncodingException ex) {
                    sb.append(String.format("(message bytes: length %d)", bytes.length));
                }
                logRequest(url, params, sb.toString());
            } else {
                logRequest(url, params, null);
            }
        }
    }
    
    /**
     * Logs the request if ConnectionLogging is enabled
     *
     * @param url The URL to request
     * @param params A simple key/value pair of request parameters
     * @param messageBody The String to be posted in the message body
     *     
     */
    public void logRequest(final String url, final List<Tuple<String, String>> params, final String messageBody) {
        if (LogPreferences.isConnectionLoggingEnabled()) {
            ConnectionLogging.getInstance().log(Level.FINE, "### Connection Request URL = " + url, null);
            final StringBuilder sb = new StringBuilder();
            for (final Tuple t : params){
                sb.append(t.getFirst()).append(" = ").append(t.getSecond()).append("\n");
            }
            ConnectionLogging.getInstance().log(Level.FINE, "### Connection Request Parameters:\n" + sb.toString(), null);
            if (messageBody != null) {
                ConnectionLogging.getInstance().log(Level.FINE, "### Connection Request Message Body:\n" + messageBody, null);
            }
        }
    }
    
    /**
     * Logs the response if ConnectionLogging is enabled
     *
     */
    public void logResponse() {
        if (LogPreferences.isConnectionLoggingEnabled()) {
            final StringBuilder sb = new StringBuilder();
            sb.append("### Connection Response:\n");
            sb.append(DASH_STRING);
            sb.append(String.format("code    : %d%n", responseCode));
            sb.append(String.format("message : %s%n", responseMessage));
            sb.append(DASH_STRING);
            if (headerFields != null) {
                headerFields.entrySet().stream().forEach(header -> {
                    sb.append(String.format("header  : %s%n", header.getKey()));
                    header.getValue().stream().forEach(v -> sb.append(String.format("        : %s%n", v)));
                });

                sb.append(DASH_STRING);
            }
            if (bytes != null) {
                try {
                    sb.append(new String(bytes, StandardCharsets.UTF_8.name()));
                } catch (final UnsupportedEncodingException ex) {
                    sb.append(String.format("(response bytes: length %d)", bytes.length));
                }
            }
            sb.append(DASH_STRING);
            ConnectionLogging.getInstance().log(Level.FINE, sb.toString(), null);
        }
    }
}
