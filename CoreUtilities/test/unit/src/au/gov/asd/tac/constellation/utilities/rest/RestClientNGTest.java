/*
 * Copyright 2010-2025 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.utilities.log.ConnectionLogging;
import au.gov.asd.tac.constellation.utilities.log.LogPreferences;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author serpens24
 */
public class RestClientNGTest {
    
    private StringBuilder outputStreamString;  // This value is manipulated by the test rest interface implementation
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of generateUrl method, of class RestClient.
     * @throws java.io.UnsupportedEncodingException
     * @throws java.net.MalformedURLException
     */
    @Test
    public void testGenerateUrl_NullParams() throws UnsupportedEncodingException, MalformedURLException {
        System.out.println("testGenerateUrl");
        String url = "https://testurl.tst/testEndpoint";
        List<Tuple<String, String>> params = null;
        URL result = RestClient.generateUrl(url, params);
        assertEquals(result, URI.create(url).toURL());
    }

    /**
     * Test of generateUrl method, of class RestClient.
     * @throws java.io.UnsupportedEncodingException
     * @throws java.net.MalformedURLException
     */
    @Test
    public void testGenerateUrl_Empty() throws UnsupportedEncodingException, MalformedURLException  {
        System.out.println("testGenerateUrl");
        String url = "https://testurl.tst/testEndpoint";
        List<Tuple<String, String>> params = new ArrayList<>();
        URL result = RestClient.generateUrl(url, params);
        assertEquals(result, URI.create(url).toURL());
    }

    /**
     * Test of generateUrl method, of class RestClient.
     * @throws java.io.UnsupportedEncodingException
     * @throws java.net.MalformedURLException
     */
    @Test
    public void testGenerateUrl_InvalidParamName() throws UnsupportedEncodingException, MalformedURLException {
        System.out.println("testGenerateUrl");
        String url = "https://testurl.tst/testEndpoint";
        List<Tuple<String, String>> params = new ArrayList<>();
        params.add(new Tuple("", "value"));
        URL result = RestClient.generateUrl(url, params);
        assertEquals(result, URI.create(url).toURL());
    }

    /**
     * Test of generateUrl method, of class RestClient.
     * @throws java.io.UnsupportedEncodingException
     * @throws java.net.MalformedURLException
     */
    @Test
    public void testGenerateUrl_ValidParams() throws UnsupportedEncodingException, MalformedURLException {
        System.out.println("testGenerateUrl");
        String url = "https://testurl.tst/testEndpoint";
        List<Tuple<String, String>> params = new ArrayList<>();
        params.add(new Tuple("param 1", "value1 with spaces"));
        params.add(new Tuple("param+2", "value2+with+plusses"));
        URL result = RestClient.generateUrl(url, params);
        assertEquals(result, URI.create(String.format("%s?param%%201=value1%%20with%%20spaces&param%%2B2=value2%%2Bwith%%2Bplusses", url)).toURL());
    }

    /**
     * Test of beforeGet method, of class RestClient.
     */
    @Test
    public void testBeforeGet() {
        System.out.println("testBeforeGet");
        String url = "";
        List<Tuple<String, String>> params = null;
        RestClient instance = new RestClientImpl(0, "", null);
        instance.beforeGet(url, params);
        // Method does nothing - designed to be overridden, test really just
        // checks it can be called
    }

    /**
     * Test of get method, of class RestClient.
     * @throws java.io.IOException
     */
    @Test
    public void testGetInvalidResponseCode() throws IOException {
        System.out.println("testGet");
        String url = "";
        // Stubbed RestClientImpl uses params to provide class params
        List<Tuple<String, String>> params = new ArrayList<>();
        Map<String, List<String>> headerFields = new HashMap<>();
        headerFields.put("hdr1", new ArrayList<>());
        RestClientImpl instance = new RestClientImpl(501, "message", headerFields);
        instance.get(url, params);
        assertEquals(instance.getResponseCode(), 501);
        assertEquals(instance.getResponseMessage(), "message");
        assertEquals(instance.getHeaderFields().size(), headerFields.size());
        assertTrue(instance.getHeaderFields().containsKey("hdr1"));
        assertNull(instance.getBytes());
        // Note: cant see easy way to test valid response code as call to getBody would fail
    }

    /**
     * Test of afterGet method, of class RestClient.
     */
    @Test
    public void testAfterGet() {
        System.out.println("testAfterGet");
        String url = "";
        List<Tuple<String, String>> params = null;
        RestClient instance = new RestClientImpl(0, "", null);
        instance.afterGet(url, params);
        // Method does nothing - designed to be overridden, test really just
        // checks it can be called
    }

    /**
     * Test of beforePost method, of class RestClient.
     */
    @Test
    public void testBeforePost() {
        System.out.println("testBeforePost");
        String url = "";
        List<Tuple<String, String>> params = null;
        RestClient instance = new RestClientImpl(0, "", null);
        instance.beforePost(url, params);
        // Method does nothing - designed to be overridden, test really just
        // checks it can be called
    }

    /**
     * Test of post method, of class RestClient.
     * @throws java.io.IOException
     */
    @Test
    public void testPostInvalidResponseCode() throws IOException {
        System.out.println("testPostInvalidResponseCode");
        String url = "";
        List<Tuple<String, String>> params = new ArrayList<>();
        params.add(new Tuple<>("AAA", "BBB"));
        params.add(new Tuple<>(null, "CCC"));
        params.add(new Tuple<>(" ", "DDD"));
        Map<String, List<String>> headerFields = new HashMap<>();
        headerFields.put("hdr1", new ArrayList<>());
        RestClientImpl instance = new RestClientImpl(501, "message", headerFields);
        outputStreamString = new StringBuilder();
        instance.post(url, params);
        assertEquals(instance.getResponseCode(), 501);
        assertEquals(instance.getResponseMessage(), "message");
        assertEquals(instance.getHeaderFields().size(), headerFields.size());
        assertTrue(instance.getHeaderFields().containsKey("hdr1"));
        assertNull(instance.getBytes());
        assertEquals(outputStreamString.toString(), "{\"AAA\":\"BBB\"}");
    }

    /**
     * Test of postWithJson method, of class RestClient.
     * @throws java.io.IOException
     */
    @Test
    public void testPostWithJson() throws IOException {
        System.out.println("testPostWithJson");
        String url = "";
        List<Tuple<String, String>> params = new ArrayList<>();
        params.add(new Tuple<>("AAA", "BBB"));
        params.add(new Tuple<>(null, "CCC"));
        params.add(new Tuple<>(" ", "DDD"));
        String json = "{\"jsonText\":\"I am JSON\"}";
        Map<String, List<String>> headerFields = new HashMap<>();
        headerFields.put("hdr1", new ArrayList<>());
        RestClientImpl instance = new RestClientImpl(501, "message", headerFields);
        outputStreamString = new StringBuilder();
        instance.postWithJson(url, params, json);
        assertEquals(instance.getResponseCode(), 501);
        assertEquals(instance.getResponseMessage(), "message");
        assertEquals(instance.getHeaderFields().size(), headerFields.size());
        assertTrue(instance.getHeaderFields().containsKey("hdr1"));
        assertNull(instance.getBytes());
        assertEquals(outputStreamString.toString(), json);
    }

    /**
     * Test of postWithBytes method, of class RestClient.
     * @throws java.io.IOException
     */
    @Test
    public void testPostWithBytes() throws IOException {
        System.out.println("testPostWithBytes");
        String url = "";
        List<Tuple<String, String>> params = new ArrayList<>();
        params.add(new Tuple<>("AAA", "BBB"));
        params.add(new Tuple<>(null, "CCC"));
        params.add(new Tuple<>(" ", "DDD"));
        String byteString = "Sample Bytes From String";
        byte[] bytes = byteString.getBytes();
        Map<String, List<String>> headerFields = new HashMap<>();
        headerFields.put("hdr1", new ArrayList<>());
        RestClientImpl instance = new RestClientImpl(501, "message", headerFields);
        outputStreamString = new StringBuilder();
        instance.postWithBytes(url, params, bytes);
        assertEquals(instance.getResponseCode(), 501);
        assertEquals(instance.getResponseMessage(), "message");
        assertEquals(instance.getHeaderFields().size(), headerFields.size());
        assertTrue(instance.getHeaderFields().containsKey("hdr1"));
        assertNull(instance.getBytes());
        assertEquals(outputStreamString.toString(), byteString);
    }

    /**
     * Test of afterPost method, of class RestClient.
     */
    @Test
    public void testAfterPost() {
        System.out.println("testAfterPost");
        String url = "";
        List<Tuple<String, String>> params = null;
        RestClient instance = new RestClientImpl(0, "", null);
        instance.afterPost(url, params);
        // Method does nothing - designed to be overridden, test really just
        // checks it can be called
    }
    
    /**
     * Test to confirm that the request and response data is sent to 
     * the Logger when the Connection Logging option has been enabled.
     * 
     * @throws java.io.IOException
     */
    @Test
    public void testConnectionLogging() throws IOException {
        System.out.println("testConnectionLogging");

        final MockedStatic<LogPreferences> logPrefs = Mockito.mockStatic(LogPreferences.class);
        logPrefs.when(LogPreferences::isConnectionLoggingEnabled).thenReturn(true);        
        final MockedStatic<ConnectionLogging> conLoggingStatic = Mockito.mockStatic(ConnectionLogging.class);
        final ConnectionLogging conLogging = Mockito.mock(ConnectionLogging.class);
        conLoggingStatic.when(ConnectionLogging::getInstance).thenReturn(conLogging);
        final StringBuilder outputLog = new StringBuilder();
        final Answer testAnswer = (Answer) (final InvocationOnMock iom) -> {
            outputLog.append(iom.getArgument(1).toString()).append("\n");
            return null;            
        };
        doAnswer(testAnswer).when(conLogging).log(Mockito.any(), Mockito.anyString(), Mockito.any());
        
        final String url = "SAMPLE URL";
        final List<Tuple<String, String>> params = new ArrayList<>();
        params.add(new Tuple<>("AAA", "BBB"));
        params.add(new Tuple<>(null, "CCC"));
        params.add(new Tuple<>(" ", "DDD"));
        final String requestBody = "Message Body: Sample Bytes From String";
        final byte[] requestBytes = requestBody.getBytes();
        final Map<String, List<String>> headerFields = new HashMap<>();
        headerFields.put("hdr1", new ArrayList<>());
        final String responseMessage = "Mocked Response";
        final RestClientImpl instance = new RestClientImpl(200, responseMessage, headerFields);
        outputStreamString = new StringBuilder();
        instance.postWithBytes(url, params, requestBytes);

        // Check that the request body has been sent to the log file
        assertTrue(outputLog.toString().contains(requestBody));
        // Check that the response message has been sent to the log file
        assertTrue(outputLog.toString().contains(responseMessage));
    }
        
    /**
     * Implementation of a Rest implementation of a REST client for testing
     * purposes which makes use of Mockito to mock the actual connection.
     * Basically, this class extends RestCLient and uses Mockito to mimic actual
     * connection operations.
     * A constructor is provided to let calling test construct it with
     * precanned responseCode, ResponseMessage, and header values which
     * Mockito uses in mocks to return to calling test.
     */
    public class RestClientImpl extends RestClient {
        // Values to be returned by Mockito for mocked HttpsURLConnection
        // objecxt calls to getResponseCode, getResponseMessage, and
        // getHeaderFields
        int mockedResponseCode = 0;
        String mockedResponseMessage = null;
        Map<String, List<String>> mockedHeaders  = null;
        
        /**
         * Constructor - allows calling test to set mocked connection return
         * values.
         * @param responseCode Response code for mocked connection to return
         * @param responseMessage Response msg for mocked connection to return
         * @param headers Headers for mocked connection to return
         */
        public RestClientImpl(final int responseCode, final String responseMessage, final Map<String, List<String>> headers) {
            mockedResponseCode = responseCode;
            mockedResponseMessage = responseMessage;
            mockedHeaders = headers;
        }

        /**
         * Test implementation of makeGetConnection. Uses mockito to mock
         * several key functions (getResponseCode, getResponseMessage, and
         * getHeaderFields which use the values requested by test constructor
         * as return value.
         * @param url
         * @param params
         * @return
         * @throws IOException 
         */
        @Override
        public HttpsURLConnection makeGetConnection(String url, List<Tuple<String, String>> params) throws IOException {
            HttpsURLConnection mockConnection = Mockito.mock(HttpsURLConnection.class);
            when(mockConnection.getResponseCode()).thenReturn(mockedResponseCode);
            when(mockConnection.getResponseMessage()).thenReturn(mockedResponseMessage);
            when(mockConnection.getHeaderFields()).thenReturn(mockedHeaders);
            return mockConnection;
        }

        /**
         * Test implementation of makePostConnection. Uses mockito to mock
         * several key functions (getResponseCode, getResponseMessage, and
         * getHeaderFields which use the values requested by test constructor
         * as return value.
         * Mocks getOutputStream which implements an output stream which writes
         * the stream to a test class member variable which can be later read to
         * confirm its contents.
         * @param url
         * @param params
         * @return
         * @throws IOException 
         */
        @Override
        public HttpsURLConnection makePostConnection(String url, List<Tuple<String, String>> params) throws IOException {
            HttpsURLConnection mockConnection = Mockito.mock(HttpsURLConnection.class);
            when(mockConnection.getResponseCode()).thenReturn(mockedResponseCode);
            when(mockConnection.getResponseMessage()).thenReturn(mockedResponseMessage);
            when(mockConnection.getHeaderFields()).thenReturn(mockedHeaders);
            when(mockConnection.getOutputStream()).thenReturn(new OutputStream() {
                    @Override
                    public void write(int b) throws IOException {
                        outputStreamString.append((char) b);
                    }
                }
            );
            return mockConnection;
        }

        /**
         * Returns the response code value of the rest client implementation.
         * @return response code value of the rest client implementation.
         */
        public int getResponseCode() {
            return this.responseCode;
        }

        /**
         * Returns the response message value of the rest client implementation.
         * @return response message value of the rest client implementation.
         */
        public String getResponseMessage() {
            return this.responseMessage;
        }

        /**
         * Returns the header fields of the rest client implementation.
         * @return header fields value of the rest client implementation.
         */
        public Map<String, List<String>> getHeaderFields() {
            return this.headerFields;
        }

        /**
         * Returns the bytes value of the rest client implementation.
         * @return bytes value of the rest client implementation.
         */
        public byte[] getBytes() {
            return this.bytes;
        }
    }
}
