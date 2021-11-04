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
package au.gov.asd.tac.constellation.utilities.https;

import au.gov.asd.tac.constellation.security.ConstellationSecurityManager;
import au.gov.asd.tac.constellation.utilities.BrandingUtilities;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

/**
 * A HttpsURLConnection connection builder which uses GZIP encoding by default.
 * To retrieve a connection input stream use {@link HttpsUtilities}
 *
 * @author arcturus
 */
public class HttpsConnection {

    private static final Logger LOGGER = Logger.getLogger(HttpsConnection.class.getName());

    public static final String ACCEPT = "Accept";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_FORM = "application/x-www-form-urlencoded;charset=UTF-8";
    public static final String APPLICATION_JSON = "application/json;charset=UTF-8";
    public static final String APPLICATION_XML = "application/xml;charset=UTF-8";
    public static final String IMAGE_PNG = "image/png";
    public static final String CONNECTION = "Connection";
    public static final String KEEP_ALIVE = "keep-alive";

    // messages
    private static final String NO_CERTIFICATE_ERROR = "Failed to create a secure connection. Make sure CONSTELLATION has access to your certificate.";

    private final HttpURLConnection httpConnection;

    /**
     * A {@code HttpsURLConnection} with some sensible default values
     * <p>
     * The defaults are to accept gzip encoding, not use caching and set the
     * user agent to CONSTELLATION
     *
     * @param url The URL
     * @param enableSSL If true an SSL connection will be attempted
     * @throws IOException if an error occurs while connecting.
     * @throws RuntimeException if an SSL connection is requested and no valid
     * security context exists.
     */
    public HttpsConnection(final URL url, final boolean enableSSL) throws IOException {
        this.httpConnection = enableSSL ? (HttpsURLConnection) url.openConnection() : (HttpURLConnection) url.openConnection();

        if (enableSSL) {
            if (ConstellationSecurityManager.getCurrentSecurityContext() == null) {
                throw new RuntimeException(NO_CERTIFICATE_ERROR);
            }
            ((HttpsURLConnection) httpConnection).setSSLSocketFactory(ConstellationSecurityManager.getCurrentSecurityContext().getSSLContext().getSocketFactory());
        }
        httpConnection.setUseCaches(false);
        httpConnection.setRequestProperty("User-Agent", BrandingUtilities.APPLICATION_NAME);
        httpConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
    }

    /**
     * A HTTPS connection with SSL enabled
     *
     * @param url the URL to connect to.
     * @return this HttpsConnection.
     * @throws MalformedURLException if the given URL is malformed.
     * @throws IOException if an error occurs while making the connection.
     */
    public static HttpsConnection withUrl(String url) throws IOException {
        LOGGER.log(Level.FINE, "Connecting to url {0}", url);
        return new HttpsConnection(new URL(url), true);
    }

    /**
     * A HTTPS connection with SSL disabled
     *
     * @param url the URL to connect to.
     * @return this HttpsConnection.
     * @throws MalformedURLException if the given URL is malformed.
     * @throws IOException if an error occurs while making the connection.
     */
    public static HttpsConnection withInsecureUrl(String url) throws IOException {
        LOGGER.log(Level.FINE, "Connecting to url {0}", url);
        return new HttpsConnection(new URL(url), false);
    }

    /**
     * Disable do input
     *
     * @return HttpsConnection
     *
     * @see java.net.URLConnection#setDoInput(boolean)
     */
    public HttpsConnection noInput() {
        httpConnection.setDoInput(false);
        return this;
    }

    /**
     * Enable do output
     *
     * @return HttpsConnection
     *
     * @see java.net.URLConnection#setDoOutput(boolean)
     */
    public HttpsConnection doOutput() {
        httpConnection.setDoOutput(true);
        return this;
    }

    /**
     * Enable use cache
     *
     * @return HttpsConnection
     *
     * @see java.net.URLConnection#setUseCaches(boolean)
     */
    public HttpsConnection useCache() {
        httpConnection.setUseCaches(true);
        return this;
    }

    /**
     * Add a custom request property header
     *
     * @param key the keyword by which the request is known (e.g.,
     * "{@code Accept}").
     * @param value the value associated with it.
     * @return HttpsConnection
     *
     * @see java.net.URLConnection#setRequestProperty(String, String)
     */
    public HttpsConnection addRequestProperty(String key, String value) {
        if (value != null) {
            httpConnection.setRequestProperty(key, value);
        }
        return this;
    }

    /**
     * Sets a specified timeout value, in milliseconds, to be used when opening
     * a communications link to the resource referenced by this URLConnection.
     * If the timeout expires before the connection can be established, a
     * java.net.SocketTimeoutException is raised. A timeout of zero is
     * interpreted as an infinite timeout.
     *
     * @param timeout time in milliseconds
     * @return HttpsConnection
     *
     * @see java.net.URLConnection#setRequestProperty(String, String)
     */
    public HttpsConnection withConnectionTimeout(int timeout) {
        httpConnection.setConnectTimeout(timeout);
        return this;
    }

    /**
     * Sets the read timeout to a specified timeout, in milliseconds. A non-zero
     * value specifies the timeout when reading from Input stream when a
     * connection is established to a resource. If the timeout expires before
     * there is data available for read, a java.net.SocketTimeoutException is
     * raised. A timeout of zero is interpreted as an infinite timeout.
     *
     * @param timeout time in milliseconds
     * @return HttpsConnection
     *
     * @see java.net.URLConnection#setReadTimeout(int)
     */
    public HttpsConnection withReadTimeout(int timeout) {
        httpConnection.setReadTimeout(timeout);
        return this;
    }

    /**
     * Set a host name verifier
     *
     * @param hostnameVerifier the host name verifier.
     * @return this HttpsConnection.
     *
     * @see
     * javax.net.ssl.HttpsURLConnection#setHostnameVerifier(javax.net.ssl.HostnameVerifier)
     */
    public HttpsConnection withHostnameVerifier(HostnameVerifier hostnameVerifier) {
        ((HttpsURLConnection) httpConnection).setHostnameVerifier(hostnameVerifier);
        return this;
    }

    /**
     * Set the request property to accept JSON
     *
     * @return this HttpsConnection.
     */
    public HttpsConnection acceptJson() {
        httpConnection.setRequestProperty(HttpsConnection.ACCEPT, HttpsConnection.APPLICATION_JSON);
        return this;
    }

    /**
     * Set the request property to accept XML
     *
     * @return this HttpsConnection.
     */
    public HttpsConnection acceptXml() {
        httpConnection.setRequestProperty(HttpsConnection.ACCEPT, HttpsConnection.APPLICATION_XML);
        return this;
    }

    /**
     * Set the request property to accept PNG
     *
     * @return HttpsConnection
     */
    public HttpsConnection acceptPng() {
        httpConnection.setRequestProperty(HttpsConnection.ACCEPT, HttpsConnection.IMAGE_PNG);
        return this;
    }

    /**
     * Set the request property to accept the JSON Content-Type
     *
     * @return HttpsConnection
     */
    public HttpsConnection withJsonContentType() {
        httpConnection.setRequestProperty(HttpsConnection.CONTENT_TYPE, HttpsConnection.APPLICATION_JSON);
        return this;
    }

    /**
     * Set the request property to accept the XML Content-Type
     *
     * @return HttpsConnection
     */
    public HttpsConnection withXmlContentType() {
        httpConnection.setRequestProperty(HttpsConnection.CONTENT_TYPE, HttpsConnection.APPLICATION_XML);
        return this;
    }

    /**
     * Send an insecure post request
     *
     * @return the underlying HttpURLConnection.
     * @throws ProtocolException if the connection does not support POST.
     * @throws IOException if an error occurs while connecting.
     */
    public HttpURLConnection insecurePost() throws IOException {
        httpConnection.setRequestMethod("POST");
        httpConnection.connect();
        return httpConnection;
    }

    /**
     * Send a post request
     *
     * @return the underlying HttpsURLConnection.
     * @throws ProtocolException if the connection does not support POST.
     * @throws IOException if an error occurs while connecting.
     */
    public HttpsURLConnection post() throws IOException {
        httpConnection.setRequestMethod("POST");
        httpConnection.connect();
        return ((HttpsURLConnection) httpConnection);
    }

    /**
     * Send an insecure get request
     *
     * @return the underlying HttpURLConnection.
     * @throws ProtocolException if the connection does not support GET.
     * @throws IOException if an error occurs while connecting.
     */
    public HttpURLConnection insecureGet() throws IOException {
        httpConnection.setRequestMethod("GET");
        httpConnection.connect();
        return httpConnection;
    }

    /**
     * Send a get request
     *
     * @return the underlying HttpsURLConnection.
     * @throws ProtocolException if the connection does not support GET.
     * @throws IOException if an error occurs while connecting.
     */
    public HttpsURLConnection get() throws IOException {
        httpConnection.setRequestMethod("GET");
        httpConnection.connect();
        return ((HttpsURLConnection) httpConnection);
    }

    /**
     * Send an insecure put reqeust
     *
     * @return the underlying HttpURLConnection.
     * @throws ProtocolException if the connection does not support PUT.
     * @throws IOException if an error occurs while connecting.
     */
    public HttpURLConnection insecurePut() throws IOException {
        httpConnection.setRequestMethod("PUT");
        httpConnection.connect();
        return httpConnection;
    }

    /**
     * Send a put request
     *
     * @return the underlying HttpsURLConnection.
     * @throws ProtocolException if the connection does not support PUT.
     * @throws IOException if an error occurs while connecting.
     */
    public HttpsURLConnection put() throws IOException {
        httpConnection.setRequestMethod("PUT");
        httpConnection.connect();
        return ((HttpsURLConnection) httpConnection);
    }

    /**
     * Send a delete request
     *
     * @return the underlying HttpsURLConnection.
     * @throws ProtocolException if the connection does not support DELETE.
     * @throws IOException if an error occurs while connecting.
     */
    public HttpsURLConnection delete() throws IOException {
        httpConnection.setRequestMethod("DELETE");
        httpConnection.connect();
        return ((HttpsURLConnection) httpConnection);
    }

    /**
     * Should you need to get a handle to the HttpURLConnection to perform
     * custom changes you can get the connection and make the connect request
     * yourself
     *
     * @return the underlying HttpURLConnection.
     */
    public HttpURLConnection getInsecureConnection() {
        return httpConnection;
    }

    /**
     * Should you need to get a handle to the HttpsURLConnection to perform
     * custom changes you can get the connection and make the connect request
     * yourself
     *
     * @return the underlying HttpsURLConnection.
     */
    public HttpsURLConnection getConnection() {
        return ((HttpsURLConnection) httpConnection);
    }
}
