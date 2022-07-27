/*
 * Copyright 2010-2022 Australian Signals Directorate
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
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;
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
    
    StringBuilder outputStreamString;  // This value is manipulated by the test
                                        // rest interface implementation
    
    public RestClientNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of generateUrl method, of class RestClient.
     */
    @Test
    public void testGenerateUrl_NullParams() throws Exception {
        System.out.println("testGenerateUrl");
        String url = "https://testurl.tst/testEndpoint";
        List<Tuple<String, String>> params = null;
        URL result = RestClient.generateUrl(url, params);
        assertEquals(result, new URL(url));
    }

    /**
     * Test of generateUrl method, of class RestClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testGenerateUrl_Empty() throws Exception {
        System.out.println("testGenerateUrl");
        String url = "https://testurl.tst/testEndpoint";
        List<Tuple<String, String>> params = new ArrayList<>();
        URL result = RestClient.generateUrl(url, params);
        assertEquals(result, new URL(url));
    }

    /**
     * Test of generateUrl method, of class RestClient.
     */
    @Test
    public void testGenerateUrl_InvalidParamName() throws Exception {
        System.out.println("testGenerateUrl");
        String url = "https://testurl.tst/testEndpoint";
        List<Tuple<String, String>> params = new ArrayList<>();
        params.add(new Tuple("", "value"));
        URL result = RestClient.generateUrl(url, params);
        assertEquals(result, new URL(url));
    }

    /**
     * Test of generateUrl method, of class RestClient.
     */
    @Test
    public void testGenerateUrl_ValidParams() throws Exception {
        System.out.println("testGenerateUrl");
        String url = "https://testurl.tst/test  Endpoint";
        List<Tuple<String, String>> params = new ArrayList<>();
        params.add(new Tuple("param 1", "value1 with spaces"));
        params.add(new Tuple("param+2", "value2+with+plusses"));
        URL result = RestClient.generateUrl(url, params);
        assertEquals(result, new URL(String.format("%s?param%201=value2%20with+spaces&param+2=value2+with+plusses", url)));
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
     */
    @Test
    public void testGetInvalidResponseCode() throws Exception {
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
     */
    @Test
    public void testPostInvalidResponseCode() throws Exception {
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
     */
    @Test
    public void testPostWithJson() throws Exception {
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
     */
    @Test
    public void testPostWithBytes_String_Map() throws Exception {
        System.out.println("testPostWithBytes_String_Map - not testing deprecated function");
    }

    /**
     * Test of postWithBytes method, of class RestClient.
     */
    @Test
    public void testPostWithBytes() throws Exception {
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
