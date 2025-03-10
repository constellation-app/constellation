/*
 * Copyright 2010-2024 Australian Signals Directorate
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class ResponseNGTest {
    
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
     * Test of isSuccess method, of class Response.
     * @throws java.io.IOException
     */
    @Test
    public void testIsSuccess() throws IOException {
        System.out.println("isSuccess");

        final Response instance1 = new ResponseImpl(200, "", null, null);
        final Response instance2 = new ResponseImpl(300, "", null, null);
        final Response instance3 = new ResponseImpl(400, "", null, null);

        assertTrue(instance1.isSuccess());
        assertFalse(instance2.isSuccess());
        assertTrue(instance3.isSuccess());
    }

    /**
     * Test of isCodeSuccess method, of class Response.
     */
    @Test
    public void testIsCodeSuccess() {
        System.out.println("isCodeSuccess");

        assertTrue(Response.isCodeSuccess(200));
        assertFalse(Response.isCodeSuccess(300));
        assertTrue(Response.isCodeSuccess(400));
    }

    /**
     * Test of isSuccessWithJson method, of class Response.
     * @throws java.io.IOException
     */
    @Test
    public void testIsSuccessWithJson() throws IOException {
        System.out.println("isSuccessWithJson");

        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode json = mapper.createObjectNode().put("test", "mytest");

        final Response instance1 = new ResponseImpl(200, "", null, json.toString().getBytes());
        final Response instance2 = new ResponseImpl(200, "", null, null, false);
        final Response instance3 = new ResponseImpl(300, "", null, null);

        assertTrue(instance1.isSuccessWithJson());
        assertFalse(instance2.isSuccessWithJson());
        assertFalse(instance3.isSuccessWithJson());
    }

    /**
     * Test of getSaveResponseFilename method, of class Response.
     * @throws java.io.IOException
     */
    @Test
    public void testGetSaveResponseFilename() throws IOException {
        System.out.println("getSaveResponseFilename");

        final Response instance1 = new ResponseImpl(200, "", null, null);
        final Response instance2 = new ResponseSaveImpl(200, "", null, null);

        assertNull(instance1.getSaveResponseFilename());
        assertEquals(instance2.getSaveResponseFilename(), "test");
    }

    /**
     * Test of getRootNode method, of class Response.
     * @throws java.io.IOException
     */
    @Test
    public void testGetRootNode() throws IOException {
        System.out.println("getRootNode");

        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode json = mapper.createObjectNode().put("test", "mytest");

        final Response instance = new ResponseImpl(200, "", null, null);

        final JsonNode result = instance.getRootNode(mapper, json.toString().getBytes());
        assertEquals(result.toString(), "{\"test\":\"mytest\"}");
    }

    /**
     * Test of getLogMessage method, of class Response.
     * @throws java.io.IOException
     */
    @Test
    public void testGetLogMessage() throws IOException {
        System.out.println("getLogMessage");

        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode json1 = mapper.createObjectNode().put("test", "mytest");
        final ObjectNode json2 = mapper.createObjectNode().put("logMessage", "my log message!");

        final Response instance1 = new ResponseImpl(200, "test1", null, null);
        final Response instance2 = new ResponseImpl(200, "test2", null, json1.toString().getBytes());
        final Response instance3 = new ResponseImpl(200, "test3", null, json2.toString().getBytes());

        assertEquals(instance1.getLogMessage(), "Invalid response 200: test1" + System.getProperty("line.separator") 
                + "null" + System.getProperty("line.separator"));
        assertEquals(instance2.getLogMessage(), "Invalid response 200: test2" + System.getProperty("line.separator") 
                + "[123, 34, 116, 101, 115, 116, 34, 58, 34, 109, 121, 116, 101, 115, 116, 34, 125]" 
                + System.getProperty("line.separator"));
        assertEquals(instance3.getLogMessage(), "my log message!");
    }

    /**
     * Test of toString method, of class Response. No headers, bytes, or json
     * @throws java.io.IOException
     */
    @Test
    public void testToString() throws IOException {
        System.out.println("toString");

        final Response instance = new ResponseImpl(200, "test", null, null);

        final String expString = new StringBuilder("[ResponseImpl")
                .append(System.getProperty("line.separator"))
                .append("----\n")
                .append("code    : 200")
                .append(System.getProperty("line.separator"))
                .append("message : test")
                .append(System.getProperty("line.separator"))
                .append("----\n----\n]\n")
                .toString();

        assertEquals(instance.toString(), expString);
    }

    /**
     * Test of toString method, of class Response. Headers in response
     * @throws java.io.IOException
     */
    @Test
    public void testToStringHeaders() throws IOException {
        System.out.println("toStringHeaders");

        final Map<String, List<String>> headers = new HashMap<>();
        headers.put("testHeader", Arrays.asList("value1", "value2"));

        final Response instance = new ResponseImpl(200, "test", headers, null);

        final String expString = new StringBuilder("[ResponseImpl")
                .append(System.getProperty("line.separator"))
                .append("----\n")
                .append("code    : 200")
                .append(System.getProperty("line.separator"))
                .append("message : test")
                .append(System.getProperty("line.separator"))
                .append("----\n")
                .append("header  : testHeader")
                .append(System.getProperty("line.separator"))
                .append("        : value1")
                .append(System.getProperty("line.separator"))
                .append("        : value2")
                .append(System.getProperty("line.separator"))
                .append("----\n")
                .append("----\n]\n")
                .toString();

        assertEquals(instance.toString(), expString);
    }

    /**
     * Test of toString method, of class Response. Bytes (not json) included
     * @throws java.io.IOException
     */
    @Test
    public void testToStringBytes() throws IOException {
        System.out.println("toStringBytes");

        final Response instance = new ResponseImpl(200, "test", null, "not json".getBytes(), false);

        final String expString = new StringBuilder("[ResponseImpl")
                .append(System.getProperty("line.separator"))
                .append("----\n")
                .append("code    : 200")
                .append(System.getProperty("line.separator"))
                .append("message : test")
                .append(System.getProperty("line.separator"))
                .append("----\n")
                .append("not json")
                .append("----\n]\n")
                .toString();

        assertEquals(instance.toString(), expString);
    }

    /**
     * Test of toString method, of class Response. Bytes that are json included
     * @throws java.io.IOException
     */
    @Test
    public void testToStringJson() throws IOException {
        System.out.println("toStringJson");

        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode jsonObject = mapper.createObjectNode().put("test", "mytest");

        final Response instance = new ResponseImpl(200, "test", null, jsonObject.toString().getBytes());

        final String expString = new StringBuilder("[ResponseImpl")
                .append(System.getProperty("line.separator"))
                .append("----\n")
                .append("code    : 200")
                .append(System.getProperty("line.separator"))
                .append("message : test")
                .append(System.getProperty("line.separator"))
                .append("----\n")
                .append("{")
                .append(System.getProperty("line.separator"))
                .append("  \"test\" : \"mytest\"")
                .append(System.getProperty("line.separator"))
                .append("}\n")
                .append("----\n]\n")
                .toString();

        assertEquals(instance.toString(), expString);
    }

    /**
     * Test of jsonToString method, of class Response.
     * @throws java.io.IOException
     */
    @Test
    public void testJsonToString() throws IOException {
        System.out.println("jsonToString");

        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode json1 = mapper.createObjectNode();
        final ObjectNode json2 = mapper.createObjectNode().put("test", "mytest");
        final ArrayNode json3 = mapper.createArrayNode();
        final ArrayNode json4 = mapper.createArrayNode().add("test");

        final String result1 = Response.jsonToString(null);
        final String result2 = Response.jsonToString(json1);
        final String result3 = Response.jsonToString(json2);
        final String result4 = Response.jsonToString(json3);
        final String result5 = Response.jsonToString(json4);

        final String expResult3 = new StringBuilder()
                .append("{")
                .append(System.getProperty("line.separator"))
                .append("  \"test\" : \"mytest\"")
                .append(System.getProperty("line.separator"))
                .append("}")
                .toString();

        assertEquals(result1, "null");
        assertEquals(result2, "{ }");
        assertEquals(result3, expResult3);
        assertEquals(result4, "[ ]");
        assertEquals(result5, "[ \"test\" ]");
    }

    private class ResponseImpl extends Response {

        public ResponseImpl(final int code, final String message, final Map<String, List<String>> headers, 
                final byte[] bytes) throws IOException {
            super(code, message, headers, bytes);
        }

        public ResponseImpl(final int code, final String message, final Map<String, List<String>> headers, 
                final byte[] bytes, final boolean isJson) throws IOException {
            super(code, message, headers, bytes, isJson);
        }

    }

    private class ResponseSaveImpl extends Response {

        public ResponseSaveImpl(final int code, final String message, final Map<String, List<String>> headers, 
                final byte[] bytes) throws IOException {
            super(code, message, headers, bytes);
        }

        @Override
        public String getSaveResponseFilename() {
            return "test";
        }
    }
}