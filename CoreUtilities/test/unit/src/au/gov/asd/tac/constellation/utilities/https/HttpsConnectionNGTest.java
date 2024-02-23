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

import au.gov.asd.tac.constellation.security.ConstellationSecurityContext;
import au.gov.asd.tac.constellation.security.ConstellationSecurityManager;
import au.gov.asd.tac.constellation.utilities.BrandingUtilities;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

/**
 * @author groombridge34a
 */
public class HttpsConnectionNGTest {
    
    private static final URL DUMMY_HTTP_URL = getDummyUrl(false);
    private static final URL DUMMY_HTTPS_URL = getDummyUrl(true);

    // returns a URL object with a dummy location
    private static URL getDummyUrl(final boolean https) {
        try {
            return URI.create(https ? "https://dummy/secure" : "http://dummy/insecure").toURL();
        } catch (MalformedURLException e) {
            return null;
        }
    }
    
    /**
     * Can construct an unencrypted HTTP connection.
     * 
     * @throws IOException if the HttpsConnection cannot be constructed
     */
    @Test
    public void testConstructorHttp() throws IOException {     
        final HttpURLConnection httpConn = 
                new HttpsConnection(DUMMY_HTTP_URL, false).getInsecureConnection();
        
        assertSame(httpConn.getURL(), DUMMY_HTTP_URL);
        assertFalse(httpConn.getUseCaches());
        assertEquals(httpConn.getRequestProperty("User-Agent"), 
                BrandingUtilities.APPLICATION_NAME);
        assertEquals(httpConn.getRequestProperty("Accept-Encoding"), 
                "gzip, deflate");
    }
    
    /* Gets a mocked ConstellationSecurityContext that will return a mock 
       SSLContext, that will itself return a mock SSLSocketFactory */
    private ConstellationSecurityContext getMockSecurityContext() {
        final SSLSocketFactory socketFactory = mock(SSLSocketFactory.class);
        final SSLContext sslCtx = mock(SSLContext.class);
        when(sslCtx.getSocketFactory()).thenReturn(socketFactory);
        final ConstellationSecurityContext securityCtx
                = mock(ConstellationSecurityContext.class);
        when(securityCtx.getSSLContext()).thenReturn(sslCtx);
        return securityCtx;
    }
    
    /**
     * Can construct an encrypted HTTPS connection.
     * 
     * @throws IOException if the HttpsConnection cannot be constructed
     */
    @Test
    public void testConstructorHttps() throws IOException {
        try (final MockedStatic<ConstellationSecurityManager> securityMgr 
                = mockStatic(ConstellationSecurityManager.class)) {
            final ConstellationSecurityContext secCtx = getMockSecurityContext();
            securityMgr.when(() -> ConstellationSecurityManager.getCurrentSecurityContext())
                    .thenReturn(secCtx);
            
            final HttpsURLConnection httpsConn = 
                    new HttpsConnection(DUMMY_HTTPS_URL, true).getConnection();
            
            assertSame(
                    httpsConn.getSSLSocketFactory(), 
                    ConstellationSecurityManager.getCurrentSecurityContext()
                            .getSSLContext().getSocketFactory());
            assertSame(httpsConn.getURL(), DUMMY_HTTPS_URL);
            assertFalse(httpsConn.getUseCaches());
            assertEquals(httpsConn.getRequestProperty("User-Agent"), 
                    BrandingUtilities.APPLICATION_NAME);
            assertEquals(httpsConn.getRequestProperty("Accept-Encoding"), 
                    "gzip, deflate");
        }
    }
    
    /**
     * RuntimeException thrown when attempting to construct an encrypted HTTPS
     * connection without a security context.
     * 
     * @throws IOException if the HttpsConnection cannot be constructed
     */
    @Test(expectedExceptions = {RuntimeException.class}, 
            expectedExceptionsMessageRegExp = "Failed to create a secure "
                    + "connection. Make sure CONSTELLATION has access to "
                    + "your certificate.")
    public void testConstructorHttpsException() throws IOException {
        try (final MockedStatic<ConstellationSecurityManager> securityMgr 
                = mockStatic(ConstellationSecurityManager.class)) {
            securityMgr.when(() -> ConstellationSecurityManager.getCurrentSecurityContext()).thenReturn(null);
            new HttpsConnection(DUMMY_HTTPS_URL, true).getConnection();
        }
    }
    
    /**
     * Can get a HTTPS connection with SSL enabled.
     * 
     * @throws IOException if the HttpsConnection cannot be constructed
     */
    @Test
    public void testWithUrl() throws IOException {
        try (final MockedStatic<ConstellationSecurityManager> securityMgr
                = mockStatic(ConstellationSecurityManager.class)) {
            final ConstellationSecurityContext secCtx = getMockSecurityContext();
            securityMgr.when(
                    ConstellationSecurityManager::getCurrentSecurityContext)
                    .thenReturn(secCtx);
            
            assertEquals(
                    HttpsConnection
                            .withUrl(DUMMY_HTTPS_URL.toString())
                            .getConnection()
                            .getURL().toString(),
                    DUMMY_HTTPS_URL.toString());
        }
    }
    
    /**
     * Can get a HTTPS connection with SSL disabled.
     * 
     * @throws IOException if the HttpsConnection cannot be constructed
     */
    @Test
    public void testWithInsecureUrl() throws IOException {
        assertEquals(
                HttpsConnection
                        .withInsecureUrl(DUMMY_HTTP_URL.toString())
                        .getInsecureConnection()
                        .getURL().toString(),
                DUMMY_HTTP_URL.toString());
    }
    
    /**
     * Can return the HttpsConnection object with doInput disabled.
     * 
     * @throws IOException if the HttpsConnection cannot be constructed
     */
    @Test
    public void testNoInput() throws IOException {
        // get the connection and make sure doInput is true
        final HttpsConnection httpConn = 
                new HttpsConnection(DUMMY_HTTP_URL, false);
        assertTrue(httpConn.getInsecureConnection().getDoInput());

        // set doInput to false and check
        httpConn.noInput();
        assertFalse(httpConn.getInsecureConnection().getDoInput());
    }
    
    /**
     * Can return the HttpsConnection object with doOutput enabled.
     * 
     * @throws IOException if the HttpsConnection cannot be constructed
     */
    @Test
    public void testDoOutput() throws IOException {
        // get the connection and make sure doOutput is false
        final HttpsConnection httpConn = 
                new HttpsConnection(DUMMY_HTTP_URL, false);
        assertFalse(httpConn.getInsecureConnection().getDoOutput());

        // set doOutput to true and check
        httpConn.doOutput();
        assertTrue(httpConn.getInsecureConnection().getDoOutput());
    }
    
    /**
     * Can return the HttpsConnection object with caching enabled.
     * 
     * @throws IOException if the HttpsConnection cannot be constructed
     */
    @Test
    public void testUseCache() throws IOException {
        // get the connection and make sure useCaches is false
        final HttpsConnection httpConn = 
                new HttpsConnection(DUMMY_HTTP_URL, false);
        assertFalse(httpConn.getInsecureConnection().getUseCaches());

        // set useCaches to true and check
        httpConn.useCache();
        assertTrue(httpConn.getInsecureConnection().getUseCaches());
    }
    
    /**
     * Can return the HttpsConnection object with request properties.
     * 
     * @throws IOException if the HttpsConnection cannot be constructed
     */
    @Test
    public void testRequestProperty() throws IOException {
        // get the connection and record the number of request properties
        final HttpsConnection httpConn = 
                new HttpsConnection(DUMMY_HTTP_URL, false);
        final int numProps = httpConn.getInsecureConnection()
                .getRequestProperties().size();

        // adding a request property with a null value does nothing
        final String key = "propKey";
        httpConn.addRequestProperty(key, null);
        assertEquals(
                httpConn.getInsecureConnection().getRequestProperties().size(),
                numProps);

        // add a request property and check
        final String val = "propVal";
        httpConn.addRequestProperty(key, val);
        assertEquals(
                httpConn.getInsecureConnection().getRequestProperties().size(),
                numProps + 1);
        assertEquals(
                httpConn.getInsecureConnection().getRequestProperties().get(key),
                Arrays.asList(val));
    }
    
    /**
     * Can return the HttpsConnection object with a connection timeout set.
     * 
     * @throws IOException if the HttpsConnection cannot be constructed
     */
    @Test
    public void testWithConnectionTimeout() throws IOException {
        // get the connection and make sure the connection timeout is zero
        final HttpsConnection httpConn = 
                new HttpsConnection(DUMMY_HTTP_URL, false);
        assertEquals(httpConn.getInsecureConnection().getConnectTimeout(), 0);

        // set connection timeout and check
        final int timeout = 12345;
        httpConn.withConnectionTimeout(timeout);
        assertEquals(
                httpConn.getInsecureConnection().getConnectTimeout(), 
                timeout);
    }
    
    /**
     * Can return the HttpsConnection object with a read timeout set.
     * 
     * @throws IOException if the HttpsConnection cannot be constructed
     */
    @Test
    public void testWithReadTimeout() throws IOException {
        // get the connection and make sure the read timeout is zero
        final HttpsConnection httpConn = 
                new HttpsConnection(DUMMY_HTTP_URL, false);
        assertEquals(httpConn.getInsecureConnection().getReadTimeout(), 0);

        // set read timeout and check
        final int timeout = 54321;
        httpConn.withReadTimeout(timeout);
        assertEquals(httpConn.getInsecureConnection().getReadTimeout(), timeout);
    }
    
    // hostname verifier for testing purposes
    private static final HostnameVerifier HOSTNAME_VERIFIER = 
            (String hostname, SSLSession session) -> {
        throw new UnsupportedOperationException("Not supported yet.");
    };
    
    /**
     * Can return the HttpsConnection object with a custom hostname verifier.
     * 
     * @throws IOException if the HttpsConnection cannot be constructed
     */
    @Test
    public void testHostnameVerifier() throws IOException {
        try (final MockedStatic<ConstellationSecurityManager> securityMgr
                = mockStatic(ConstellationSecurityManager.class)) {
            final ConstellationSecurityContext secCtx = getMockSecurityContext();
            securityMgr.when(
                    ConstellationSecurityManager::getCurrentSecurityContext)
                    .thenReturn(secCtx);
            
            // get the connection, set a hostname verifier and check
            final HttpsConnection httpsConn = 
                    HttpsConnection.withUrl(DUMMY_HTTPS_URL.toString());            
            httpsConn.withHostnameVerifier(HOSTNAME_VERIFIER);
            assertSame(
                    httpsConn.getConnection().getHostnameVerifier(), 
                    HOSTNAME_VERIFIER);
        }
    }
    
    /**
     * Can return the HttpsConnection object with the accept header set to JSON.
     * 
     * @throws IOException if the HttpsConnection cannot be constructed
     */
    @Test
    public void testAcceptJson() throws IOException {
        // get the connection and make sure the accept header is null
        final HttpsConnection httpConn = 
                new HttpsConnection(DUMMY_HTTP_URL, false);
        assertEquals(httpConn.getInsecureConnection()
                .getRequestProperty(HttpsConnection.ACCEPT), null);

        // set accept to JSON and check
        httpConn.acceptJson();
        assertEquals(
                httpConn.getInsecureConnection()
                        .getRequestProperty(HttpsConnection.ACCEPT),
                HttpsConnection.APPLICATION_JSON);
    }
    
    /**
     * Can return the HttpsConnection object with the accept header set to XML.
     * 
     * @throws IOException if the HttpsConnection cannot be constructed
     */
    @Test
    public void testAcceptXml() throws IOException {
        // get the connection and make sure the accept header is null
        final HttpsConnection httpConn = 
                new HttpsConnection(DUMMY_HTTP_URL, false);
        assertEquals(httpConn.getInsecureConnection()
                .getRequestProperty(HttpsConnection.ACCEPT), null);

        // set accept to XML and check
        httpConn.acceptXml();
        assertEquals(
                httpConn.getInsecureConnection()
                        .getRequestProperty(HttpsConnection.ACCEPT),
                HttpsConnection.APPLICATION_XML);
    }
    
    /**
     * Can return the HttpsConnection object with the accept header set to PNG.
     * 
     * @throws IOException if the HttpsConnection cannot be constructed
     */
    @Test
    public void testAcceptPng() throws IOException {
        // get the connection and make sure the accept header is null
        final HttpsConnection httpConn = 
                new HttpsConnection(DUMMY_HTTP_URL, false);
        assertEquals(httpConn.getInsecureConnection()
                .getRequestProperty(HttpsConnection.ACCEPT), null);

        // set accept to PNG and check
        httpConn.acceptPng();
        assertEquals(
                httpConn.getInsecureConnection()
                        .getRequestProperty(HttpsConnection.ACCEPT),
                HttpsConnection.IMAGE_PNG);
    }
    
    /**
     * Can return the HttpsConnection object with the content type set to JSON.
     * 
     * @throws IOException if the HttpsConnection cannot be constructed
     */
    @Test
    public void testWithJsonContentType() throws IOException {
        // get the connection and make sure the content type is null
        final HttpsConnection httpConn = 
                new HttpsConnection(DUMMY_HTTP_URL, false);
        assertEquals(httpConn.getInsecureConnection()
                .getRequestProperty(HttpsConnection.CONTENT_TYPE), null);

        // set content type to JSON and check
        httpConn.withJsonContentType();
        assertEquals(
                httpConn.getInsecureConnection()
                        .getRequestProperty(HttpsConnection.CONTENT_TYPE),
                HttpsConnection.APPLICATION_JSON);
    }
    
    /**
     * Can return the HttpsConnection object with the content type set to XML.
     * 
     * @throws IOException if the HttpsConnection cannot be constructed
     */
    @Test
    public void testWithXmlContentType() throws IOException {
        // get the connection and make sure the content type is null
        final HttpsConnection httpConn = 
                new HttpsConnection(DUMMY_HTTP_URL, false);
        assertEquals(httpConn.getInsecureConnection()
                .getRequestProperty(HttpsConnection.CONTENT_TYPE), null);

        // set content type to XML and check
        httpConn.withXmlContentType();
        assertEquals(
                httpConn.getInsecureConnection()
                        .getRequestProperty(HttpsConnection.CONTENT_TYPE),
                HttpsConnection.APPLICATION_XML);
    }
    
    /**
     * Can make insecure connections using POST, GET and PUT methods.
     * 
     * @throws IOException if the HttpsConnection cannot be constructed
     */
    @Test
    public void testInsecureCalls() throws IOException {
        // mock the connect call
        final HttpURLConnection httpUrlConn = 
                mock(HttpURLConnection.class, CALLS_REAL_METHODS);
        doNothing().when(httpUrlConn).connect();
        final URL url = mock(URL.class);
        when(url.openConnection()).thenReturn(httpUrlConn);
        
        /* get the connection and make sure the request method is not POST, 
           GET or PUT */
        final HttpsConnection httpConn = new HttpsConnection(url, false);
        httpConn.getInsecureConnection().setRequestMethod("OPTIONS");
        
        // POST
        httpConn.insecurePost();
        assertEquals(
                httpConn.getInsecureConnection().getRequestMethod(), 
                "POST");
        verify(httpUrlConn, times(1)).connect();
        
        // GET
        httpConn.insecureGet();
        assertEquals(
                httpConn.getInsecureConnection().getRequestMethod(), 
                "GET");
        verify(httpUrlConn, times(2)).connect();
        
        // PUT
        httpConn.insecurePut();
        assertEquals(
                httpConn.getInsecureConnection().getRequestMethod(), 
                "PUT");
        verify(httpUrlConn, times(3)).connect();
    }
    
    /**
     * Can make secure connections using POST, GET, PUT and DELETE methods.
     * 
     * @throws IOException if the HttpsConnection cannot be constructed
     */
    @Test
    public void testSecureCalls() throws IOException {
        try (final MockedStatic<ConstellationSecurityManager> securityMgr
                = mockStatic(ConstellationSecurityManager.class)) {
            // mock the security context
            final ConstellationSecurityContext secCtx = getMockSecurityContext();
            securityMgr.when(ConstellationSecurityManager::getCurrentSecurityContext)
                    .thenReturn(secCtx);
            
            // mock the connect call
            final HttpsURLConnection httpsUrlConn = 
                    mock(HttpsURLConnection.class, CALLS_REAL_METHODS);
            doNothing().when(httpsUrlConn).connect();
            final URL url = mock(URL.class);
            when(url.openConnection()).thenReturn(httpsUrlConn);

            /* get the connection and make sure the request method is not POST, 
               GET, PUT or DELETE */
            final HttpsConnection httpsConn = new HttpsConnection(url, false);
            httpsConn.getConnection().setRequestMethod("OPTIONS");

            // POST
            httpsConn.post();
            assertEquals(
                    httpsConn.getConnection().getRequestMethod(),
                    "POST");
            verify(httpsUrlConn, times(1)).connect();
            
            // GET
            httpsConn.get();
            assertEquals(
                    httpsConn.getConnection().getRequestMethod(),
                    "GET");
            verify(httpsUrlConn, times(2)).connect();
            
            // PUT
            httpsConn.put();
            assertEquals(
                    httpsConn.getConnection().getRequestMethod(),
                    "PUT");
            verify(httpsUrlConn, times(3)).connect();
            
            // DELETE
            httpsConn.delete();
            assertEquals(
                    httpsConn.getConnection().getRequestMethod(),
                    "DELETE");
            verify(httpsUrlConn, times(4)).connect();
        }
    }
    
    /**
     * Can get access to the wrapped insecure HttpURLConnection.
     * 
     * @throws IOException if the HttpsConnection cannot be constructed
     */
    @Test
    public void testGetInsecureConnection() throws IOException {
        // mock URL connection
        final HttpURLConnection httpUrlConn = mock(HttpURLConnection.class);
        final URL url = mock(URL.class);
        when(url.openConnection()).thenReturn(httpUrlConn);
        
        /* create a new HttpConnection and check it is using the expected 
           URL connection */
        assertSame(
                new HttpsConnection(url, false).getInsecureConnection(),
                httpUrlConn);
    }
    
    /**
     * Can get access to the wrapped secure HttpsURLConnection.
     * 
     * @throws IOException if the HttpsConnection cannot be constructed
     */
    @Test
    public void testGetSecureConnection() throws IOException {
        try (final MockedStatic<ConstellationSecurityManager> securityMgr
                = mockStatic(ConstellationSecurityManager.class)) {
            // mock the security context and URL connection
            final ConstellationSecurityContext secCtx = getMockSecurityContext();
            securityMgr.when(ConstellationSecurityManager::getCurrentSecurityContext)
                    .thenReturn(secCtx);

            final HttpsURLConnection httpsUrlConn = mock(HttpsURLConnection.class);
            final URL url = mock(URL.class);
            when(url.openConnection()).thenReturn(httpsUrlConn);
            
        /* create a new HttpConnection and check it is using the expected 
           URL connection */
        assertSame(
                new HttpsConnection(url, true).getConnection(),
                httpsUrlConn);
        }
    }
}
