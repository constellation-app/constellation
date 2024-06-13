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
package au.gov.asd.tac.constellation.utilities.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import org.apache.commons.lang3.StringUtils;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Json Utilities Test
 *
 * @author aldebaran30701
 */
public class JsonUtilitiesNGTest {

    private static final JsonFactory FACTORY = new MappingJsonFactory();
    private final Map<String, String> map = new HashMap<>();
    private final String expectedResult = "{\"key1\":\"value1\",\"key2\":\"value2\",\"key3\":\"value3\"}";
    private final String key1 = "key1";
    private final String key2 = "key2";
    private final String key3 = "key3";
    private final String value1 = "value1";
    private final String value2 = "value2";
    private final String value3 = "value3";

    public JsonUtilitiesNGTest() {
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
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
     * Test that the default (private) constructor throws an exception and does nothing.
     * @throws Exception 
     */
    @Test
    public void testDefaultconstructor() throws Exception {
        
        Constructor<JsonUtilities> constructor = JsonUtilities.class.getDeclaredConstructor();
        constructor.setAccessible(true); 
        try {
            constructor.newInstance();
        } catch (InvocationTargetException  e) {
            // Found I couldnt add @Test(expectedExceptions=BLAH) doe to how reflection wrpas exceptions.
            assertEquals(e.getCause().toString(), "java.lang.IllegalStateException: Utility class", "Correct exception");
        }
    }
    
    /**
     * Test calls to JsonUtilities.getTextField for which no default is supplied.
     * @throws Exception
     */
    @Test
    public void testGetTextField_NoDefault() throws Exception {
        ObjectMapper mapper = new ObjectMapper();   
        JsonNode testJson = mapper.readTree("{\"1.k1\":\"1.v1\", \"1.k2\":\"1.v2\", \"1.k3\":{\"2.k1\": \"2.v1\"}}");  

        // Search for missing top level value
        assertEquals(JsonUtilities.getTextField(testJson, "1.Missing"), null, "L1 null returned if not found");

        // Search for existing top level value
        assertEquals(JsonUtilities.getTextField(testJson, "1.k2"), "1.v2", "L1 value returned if found");

        // Search for missing nested value
        assertEquals(JsonUtilities.getTextField(testJson, "1.k3", "2.Missing"), null, "L2 null returned if not found");

        // Search for existing nested value
        assertEquals(JsonUtilities.getTextField(testJson, "1.k3", "2.k1"), "2.v1", "L2 value returned if found"); 
    }
    
    /**
     * Test calls to JsonUtilities.getTextField for which a default is supplied.
     * @throws Exception 
     */
    @Test
    public void testGetTextField_Default() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode testJson = mapper.readTree("{\"1.k1\":\"1.v1\", \"1.k2\":\"1.v2\", \"1.k3\":{\"2.k1\": \"2.v1\"}}");  

        // Search for missing top level value
        assertEquals(JsonUtilities.getTextField("default", testJson, "1.Missing"), "default", "L1 default returned if not found");

        // Search for existing top level value
        assertEquals(JsonUtilities.getTextField("default", testJson, "1.k2"), "1.v2", "L1 value returned if found");

        // Search for missing nested value
        assertEquals(JsonUtilities.getTextField("default", testJson, "1.k3", "2.Missing"), "default", "L2 default returned if not found");

        // Search for existing nested value
        assertEquals(JsonUtilities.getTextField("default", testJson, "1.k3", "2.k1"), "2.v1", "L2 value returned if not found");
    }
    
    /**
     * Test calls to JsonUtilities.getFieldIterator.
     * @throws Exception 
     */
    @Test
    public void testGetFieldIterator() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode testJson = mapper.readTree("{\"1.k1\":\"1.v1\", \"1.k2\":\"1.v2\", \"1.k3\":{\"2.k1\": \"2.v1\", \"2.k2\": \"2.v2\"}}");  
        Iterator<JsonNode> iterator = JsonUtilities.getFieldIterator(testJson, "1.k3");
        ArrayList<String> nodes = new ArrayList<String>();
        while(iterator.hasNext()) {
            nodes.add(iterator.next().toString());  
        }
        assertEquals(nodes.toString(), "[\"2.v1\", \"2.v2\"]", "Populated node iterator matches");

        iterator = JsonUtilities.getFieldIterator(testJson, "1.k1");
        assertEquals(iterator.hasNext(), false, "Iterator.hasNext() returns false for empty node");
        try {
            JsonNode nextNode = iterator.next();
            fail("NoSuchElementException not thrown for empty node");
        } catch (NoSuchElementException nse) {
        }

        iterator = JsonUtilities.getFieldIterator(testJson, "1.Missing");
        assertEquals(iterator.hasNext(), false, "Iterator.hasNext() returns false for missing node");
        try {
            JsonNode nextNode = iterator.next();
            fail("NoSuchElementException not thrown for missing node");
        } catch (NoSuchElementException nse) {
        }      
    }
    
    /**
     * Test calls to JsonUtilities.getTextFieldIterator.
     * @throws Exception 
     */
    @Test
    public void testGetTextFieldIterator() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode testJson = mapper.readTree("{\"1.k1\":\"1.v1\", \"1.k2\":\"1.v2\", \"1.k3\":{\"2.k1\": \"2.v1\", \"2.k2\": \"2.v2\"}}");  
        Iterator<String> iterator = JsonUtilities.getTextFieldIterator(testJson, "1.k3");
        ArrayList<String> nodes = new ArrayList<String>();
        while(iterator.hasNext()) {
            nodes.add(iterator.next());  
        }
        assertEquals(nodes.toString(), "[2.v1, 2.v2]", "Populated node iterator matches");
        try {
            String nextNode = iterator.next();
            fail("NoSuchElementException not thrown at end of iteration");
        } catch (NoSuchElementException nse) {
        }

        iterator = JsonUtilities.getTextFieldIterator(testJson, "1.k1");
        assertEquals(iterator.hasNext(), false, "Iterator.hasNext() returns false for empty node");
        try {
            String nextNode = iterator.next();
            fail("NoSuchElementException not thrown for empty node");
        } catch (NoSuchElementException nse) {
        }

        iterator = JsonUtilities.getTextFieldIterator(testJson, "1.Missing");
        assertEquals(iterator.hasNext(), false, "Iterator.hasNext() returns false for missing node");
        try {
            String nextNode = iterator.next();
            fail("NoSuchElementException not thrown for missing node");
        } catch (NoSuchElementException nse) {
        }     
    }
    
    /**
     * Test calls to JsonUtilities.getIntegerField for which no default is supplied.
     * @throws Exception 
     */
    @Test
    public void testGetIntegerField_NoDefault() throws Exception {
        ObjectMapper mapper = new ObjectMapper();   
        JsonNode testJson = mapper.readTree("{\"1.k1\":\"aaa\", \"1.k2\": 12, \"1.k3\":{\"2.k1\": 21}}");  

        // Search for missing top level value
        assertEquals(JsonUtilities.getIntegerField(testJson, "1.Missing"), 0, "0 returned if not found");

        // Search for existing top level value with invalid value type
        assertEquals(JsonUtilities.getIntegerField(99, testJson, "1.k1"), 0, "L1 value returned if found with invalid value");

        // Search for existing top level value
        assertEquals(JsonUtilities.getIntegerField(testJson, "1.k2"), 12, "L1 value returned if found");

        // Search for missing nested value
        assertEquals(JsonUtilities.getIntegerField(testJson, "1.k3", "2.Missing"), 0, "0 returned if not found");

        // Search for existing nested value
        assertEquals(JsonUtilities.getIntegerField(testJson, "1.k3", "2.k1"), 21, "L2 value returned if found");
    }

    /**
     * Test calls to JsonUtilities.getIntegerField for which a default is supplied.
     * @throws Exception 
     */
    @Test
    public void testGetIntegerField_Default() throws Exception {
        ObjectMapper mapper = new ObjectMapper();   
        JsonNode testJson = mapper.readTree("{\"1.k1\":\"aaa\", \"1.k2\": 12, \"1.k3\":{\"2.k1\": 21}}");  

        // Search for missing top level value
        assertEquals(JsonUtilities.getIntegerField(99, testJson, "1.Missing"), 99, "L1 default returned if not found");

        // Search for existing top level value with invalid value type
        assertEquals(JsonUtilities.getIntegerField(99, testJson, "1.k1"), 0, "L1 value returned if found with invalid value");

        // Search for existing top level value
        assertEquals(JsonUtilities.getIntegerField(99, testJson, "1.k2"), 12, "L1 value returned if found");

        // Search for missing nested value
        assertEquals(JsonUtilities.getIntegerField(99, testJson, "1.k3", "2.Missing"), 99, "L2 default returned if not found");

        // Search for existing nested value
        assertEquals(JsonUtilities.getIntegerField(99, testJson, "1.k3", "2.k1"), 21, "L2 value returned if found");  
    }
    
    /**
     * Test calls to JsonUtilities.getIntegerFieldIterator.
     * @throws Exception 
     */
    @Test
    public void testGetIntegerFieldIterator() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode testJson = mapper.readTree("{\"1.k1\":\"aaa\", \"1.k2\": 12, \"1.k3\":{\"2.k1\": 21, \"2.k2\": 22}}"); 
        Iterator<Integer> iterator = JsonUtilities.getIntegerFieldIterator(testJson, "1.k3");
        ArrayList<Integer> nodes = new ArrayList<Integer>();
        while(iterator.hasNext()) {
            nodes.add(iterator.next());  
        }
        assertEquals(nodes.toString(), "[21, 22]", "Populated node iterator matches");
        try {
            Integer nextNode = iterator.next();
            fail("NoSuchElementException not thrown at end of iteration");
        } catch (NoSuchElementException nse) {
        }

        iterator = JsonUtilities.getIntegerFieldIterator(testJson, "1.k1");
        assertEquals(iterator.hasNext(), false, "Iterator.hasNext() returns false for empty node");
        try {
            Integer nextNode = iterator.next();
            fail("NoSuchElementException not thrown for empty node");
        } catch (NoSuchElementException nse) {
        }

        iterator = JsonUtilities.getIntegerFieldIterator(testJson, "1.Missing");
        assertEquals(iterator.hasNext(), false, "Iterator.hasNext() returns false for missing node");
        try {
            Integer nextNode = iterator.next();
            fail("NoSuchElementException not thrown for missing node");
        } catch (NoSuchElementException nse) {
        }      
    }
    
    /**
     * Test calls to JsonUtilities.getIntegerField for which no default is supplied.
     * @throws Exception 
     */
    @Test
    public void testGetLongField_NoDefault() throws Exception {
        ObjectMapper mapper = new ObjectMapper();   
        JsonNode testJson = mapper.readTree("{\"1.k1\":\"aaa\", \"1.k2\": 12.1, \"1.k3\":{\"2.k1\": 21.1}}");  

        // Search for missing top level value
        assertEquals(JsonUtilities.getLongField(testJson, "1.Missing"), 0, "Null returned if not found");

        // Search for existing top level value with invalid value type
        assertEquals(JsonUtilities.getLongField(99, testJson, "1.k1"), 0, "L1 value returned if found with invalid value");

        // Search for existing top level value
        assertEquals(JsonUtilities.getLongField(testJson, "1.k2"), 12, "L1 value returned if found");

        // Search for missing nested value
        assertEquals(JsonUtilities.getLongField(testJson, "1.k3", "2.Missing"), 0, "Null returned if not found");

        // Search for existing nested value
        assertEquals(JsonUtilities.getLongField(testJson, "1.k3", "2.k1"), 21, "L2 value returned if found");
    }
    
    /**
     * Test calls to JsonUtilities.getIntegerField for which a default is supplied.
     * @throws Exception 
     */
    @Test
    public void testGetLongField_Default() throws Exception {
        ObjectMapper mapper = new ObjectMapper();   
        JsonNode testJson = mapper.readTree("{\"1.k1\":\"aaa\", \"1.k2\": 12.1, \"1.k3\":{\"2.k1\": 21.1}}");  

        // Search for missing top level value
        assertEquals(JsonUtilities.getLongField(99, testJson, "1.Missing"), 99, "L1 default returned if not found");

        // Search for existing top level value with invalid value type
        assertEquals(JsonUtilities.getLongField(99, testJson, "1.k1"), 0, "L1 value returned if found with invalid value");

        // Search for existing top level value
        assertEquals(JsonUtilities.getLongField(99, testJson, "1.k2"), 12, "L1 value returned if found");

        // Search for missing nested value
        assertEquals(JsonUtilities.getLongField(99, testJson, "1.k3", "2.Missing"), 99, "L2 default returned if not found");

        // Search for existing nested value
        assertEquals(JsonUtilities.getLongField(99, testJson, "1.k3", "2.k1"), 21, "L2 value returned if found"); 
    }
    
    /**
     * Test calls to JsonUtilities.getDoubleField for which no default is supplied.
     * @throws Exception 
     */
    @Test
    public void testGetDoubleField_NoDefault() throws Exception {
        ObjectMapper mapper = new ObjectMapper();   
        JsonNode testJson = mapper.readTree("{\"1.k1\":\"aaa\", \"1.k2\": 12.1, \"1.k3\":{\"2.k1\": 21.1}}");  

        // Search for missing top level value
        assertEquals(JsonUtilities.getDoubleField(testJson, "1.Missing"), 0.0, "Null returned if not found");

        // Search for existing top level value with invalid value type
        assertEquals(JsonUtilities.getDoubleField(99.9, testJson, "1.k1"), 0.0, "L1 value returned if found with invalid value");

        // Search for existing top level value
        assertEquals(JsonUtilities.getDoubleField(testJson, "1.k2"), 12.1, "L1 value returned if found");

        // Search for missing nested value
        assertEquals(JsonUtilities.getDoubleField(testJson, "1.k3", "2.Missing"), 0.0, "Null returned if not found");

        // Search for existing nested value
        assertEquals(JsonUtilities.getDoubleField(testJson, "1.k3", "2.k1"), 21.1, "L2 value returned if found"); 
    }
    
    /**
     * Test calls to JsonUtilities.getDoubleField for which a default is supplied.
     * @throws Exception 
     */
    @Test
    public void testGetDoubleField_Default() throws Exception {
        ObjectMapper mapper = new ObjectMapper();   
        JsonNode testJson = mapper.readTree("{\"1.k1\":\"aaa\", \"1.k2\": 12.1, \"1.k3\":{\"2.k1\": 21.1}}");  

        // Search for missing top level value
        assertEquals(JsonUtilities.getDoubleField(99.9, testJson, "1.Missing"), 99.9, "L1 default returned if not found");

        // Search for existing top level value with invalid value type
        assertEquals(JsonUtilities.getDoubleField(99.9, testJson, "1.k1"), 0.0, "L1 value returned if found with invalid value");

        // Search for existing top level value
        assertEquals(JsonUtilities.getDoubleField(99.9, testJson, "1.k2"), 12.1, "L1 value returned if found");

        // Search for missing nested value
        assertEquals(JsonUtilities.getDoubleField(99.9, testJson, "1.k3", "2.Missing"), 99.9, "L2 default returned if not found");

        // Search for existing nested value
        assertEquals(JsonUtilities.getDoubleField(99.9, testJson, "1.k3", "2.k1"), 21.1, "L2 value returned if found"); 
    }
     
    /**
     * Test calls to JsonUtilities.getBooleanField for which no default is supplied.
     * @throws Exception 
     */
    @Test
    public void testGetBooleanField_NoDefault() throws Exception {
        ObjectMapper mapper = new ObjectMapper();   
        JsonNode testJson = mapper.readTree("{\"1.k1\":\"aaa\", \"1.k2\": true, \"1.k3\":{\"2.k1\": true}}");  

        // Search for missing top level value
        assertEquals(JsonUtilities.getBooleanField(testJson, "1.Missing"), false, "L1 false returned if not found");

        // Search for existing top level value with invalid value type
        assertEquals(JsonUtilities.getBooleanField(true, testJson, "1.k1"), false, "L1 value returned if found with invalid value");

        // Search for existing top level value
        assertEquals(JsonUtilities.getBooleanField(testJson, "1.k2"), true, "L1 value returned if found");

        // Search for missing nested value
        assertEquals(JsonUtilities.getBooleanField(testJson, "1.k3", "2.Missing"), false, "L2 false returned if not found");

        // Search for existing nested value
        assertEquals(JsonUtilities.getBooleanField(testJson, "1.k3", "2.k1"), true, "L2 value returned if found");  
    }
    
    /**
     * Test calls to JsonUtilities.getBooleanField for which a default is supplied.
     * @throws Exception 
     */
    @Test
    public void testGetBooleanField_Default() throws Exception {
        ObjectMapper mapper = new ObjectMapper();   
        JsonNode testJson = mapper.readTree("{\"1.k1\":\"aaa\", \"1.k2\": false, \"1.k3\":{\"2.k1\": false}}");  

        // Search for missing top level value
        assertEquals(JsonUtilities.getBooleanField(true, testJson, "1.Missing"), true, "L1 default returned if not found");

        // Search for existing top level value with invalid value type
        assertEquals(JsonUtilities.getBooleanField(true, testJson, "1.k1"), false, "L1 value returned if found with invalid value");

        // Search for existing top level value
        assertEquals(JsonUtilities.getBooleanField(true, testJson, "1.k2"), false, "L1 value returned if found");

        // Search for missing nested value
        assertEquals(JsonUtilities.getBooleanField(true, testJson, "1.k3", "2.Missing"), true, "L2 default returned if not found");

        // Search for existing nested value
        assertEquals(JsonUtilities.getBooleanField(true, testJson, "1.k3", "2.k1"), false, "L2 value returned if found"); 
    }

    /**
     * Test calls to JsonUtilities.getIntegerFieldIterator.
     * @throws Exception 
     */
    @Test
    public void testGetBooleanFieldIterator() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode testJson = mapper.readTree("{\"1.k1\":\"aaa\", \"1.k2\": true, \"1.k3\":{\"2.k1\": true, \"2.k2\": false}}"); 
        Iterator<Boolean> iterator = JsonUtilities.getBooleanFieldIterator(testJson, "1.k3");
        ArrayList<Boolean> nodes = new ArrayList<Boolean>();
        while(iterator.hasNext()) {
            nodes.add(iterator.next());  
        }
        assertEquals(nodes.toString(), "[true, false]", "Populated node iterator matches");
        try {
            Boolean nextNode = iterator.next();
            fail("NoSuchElementException not thrown at end of iteration");
        } catch (NoSuchElementException nse) {
        }

        iterator = JsonUtilities.getBooleanFieldIterator(testJson, "1.k1");
        assertEquals(iterator.hasNext(), false, "Iterator.hasNext() returns false for empty node");
        try {
            Boolean nextNode = iterator.next();
            fail("NoSuchElementException not thrown for empty node");
        } catch (NoSuchElementException nse) {
        }

        iterator = JsonUtilities.getBooleanFieldIterator(testJson, "1.Missing");
        assertEquals(iterator.hasNext(), false, "Iterator.hasNext() returns false for missing node");
        try {
            Boolean nextNode = iterator.next();
            fail("NoSuchElementException not thrown for missing node");
        } catch (NoSuchElementException nse) {
        }     
    }

    /**
     * Test calls to JsonUtilities.getGetTextValue.
     * @throws Exception 
     */
    @Test
    public void testGetChildNode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();   
        JsonNode testJson = mapper.readTree("{\"1.k1\":\"aaa\", \"1.k2\": false, \"1.k3\":1.1, \"1.k4\": {\"2.k1\": \"nest1\", \"2.k2\": \"nest2\"}, \"1.k5\": [{\"l1\": \"list1\"},2,3,4]}");  

        assertEquals(JsonUtilities.getChildNode(testJson, "missing"), null, "Missing node");
        JsonNode childNode = JsonUtilities.getChildNode(testJson, "1.k4");
        assertEquals(JsonUtilities.getTextValue("2.k1", childNode), "nest1", "Get text value of string");      
    }
    
    /**
     * Test calls to JsonUtilities.getGetTextValue.
     * @throws Exception 
     */
    @Test
    public void testGetTextValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();   
        JsonNode testJson = mapper.readTree("{\"1.k1\":\"aaa\", \"1.k2\": false, \"1.k3\":1.1, \"1.k4\": {\"2.k1\": \"nest1\", \"2.k2\": \"nest2\"}, \"1.k5\": [{\"l1\": \"list1\"},2,3,4]}");  
        assertEquals(JsonUtilities.getTextValue("1.k1", testJson), "aaa", "Get text value of string");
        assertEquals(JsonUtilities.getTextValue("1.k2", testJson), "false", "Get text value of boolean");
        assertEquals(JsonUtilities.getTextValue("1.k3", testJson), "1.1", "Get text value of numerical");
        assertEquals(JsonUtilities.getTextValue("missing", testJson), null, "Node doesn't have attribute");
    }
    
    /**
     * Test calls to JsonUtilities.getGetTextValues.
     * @throws Exception 
     */
    @Test
    public void testGetTextValues() throws Exception {
        ObjectMapper mapper = new ObjectMapper();   
        JsonNode testJson = mapper.readTree("{\"1.k1\":\"aaa\", \"1.k2\": false, \"1.k3\":1.1, \"1.k4\": {\"2.k1\": \"nest1\", \"2.k2\": \"nest2\"}, \"1.k5\": [{\"l1\": \"list1\"},2,3,4]}");  

        assertEquals(JsonUtilities.getTextValues("1.k4", testJson, ":"), "nest1:nest2", "Print child dictionary");
        assertEquals(JsonUtilities.getTextValues("1.k5", testJson, ":"), "{\"l1\":\"list1\"}:2:3:4", "Print child list");

        assertEquals(JsonUtilities.getTextValues("missing", testJson, ":"), null, "Node doesn't exist");       
    }
     
    /**
     * Test calls to JsonUtilities.getGetTextValue., \"12\": 3
     * @throws Exception 
     */
    @Test
    public void testGetTextValueOfFirstSubElement() throws Exception {
        ObjectMapper mapper = new ObjectMapper();   
        JsonNode testJson = mapper.readTree("{\"1.k1\":\"aaa\", \"1.k2\": false, \"1.k3\":1.1, \"1.k4\": {\"2.k1\": \"nest1\", \"2.k2\": \"nest2\"}, \"1.k5\": [{\"1.sub\": \"subvalue\"}], \"1.k6\": [{\"1.sub\": 55}]}");  
        assertEquals(JsonUtilities.getTextValueOfFirstSubElement("missing", "1.sub", testJson), null, "Attribute doesnt exist");
        assertEquals(JsonUtilities.getTextValueOfFirstSubElement("1.k3", "1.sub", testJson), null, "Attribute exists but holds numbert");
        assertEquals(JsonUtilities.getTextValueOfFirstSubElement("1.k4", "1.sub", testJson), null, "Attribute exists but holds dictionary");
        assertEquals(JsonUtilities.getTextValueOfFirstSubElement("1.k5", "1.sub", testJson), "subvalue", "1st element is a string");
        assertEquals(JsonUtilities.getTextValueOfFirstSubElement("1.k6", "1.sub", testJson), "55", "1st element is numerical");
        assertEquals(JsonUtilities.getTextValueOfFirstSubElement("1.k6", "missing", testJson), null, "1st element is missing");      
    }
    
    /**
     * Test calls to JsonUtilities.getGetTextValue when null
     * @throws Exception 
     */
    @Test
    public void testGetNodeTextWhenValueIsNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();   
        JsonNode testJson = mapper.readTree("{\"key\":null}");
        String text = JsonUtilities.getNodeText(testJson.get("key"));
        assertEquals(text, null);
    }
    
    /**
     * Test calls to JsonUtilities.PrettyPrint.
     */
    @Test
    public void testPrettyPrint() {
        String expected = "{\n  \"1.k1\" : \"aaa\",\n  \"1.k2\" : 12,\n  \"1.k3\" : {\n    \"2.k1\" : false\n  }\n}".replace("\n", System.lineSeparator());
        assertEquals(JsonUtilities.prettyPrint("{\"1.k1\":\"aaa\",\"1.k2\":12,\"1.k3\":{\"2.k1\":false}}"), expected, "Pretty print JSON");
        assertEquals(JsonUtilities.prettyPrint("This is not JSON"), "This is not JSON", "Pretty print non-JSON");
    }
    
    /**
     * Test of getMapAsString method, of class JsonUtilities.
     */
    @Test
    public void testGetMapAsString() {
        // Test null
        assertEquals(JsonUtilities.getMapAsString(FACTORY, null), StringUtils.EMPTY);

        // Test empty map
        final Map<String, String> emptyMap = new HashMap<>();
        assertEquals(JsonUtilities.getMapAsString(FACTORY, emptyMap), StringUtils.EMPTY);

        // Test null map
        final Map<String, String> emptyNullMap = new HashMap<>();
        emptyNullMap.put(null, null);
        emptyNullMap.put(null, null);
        emptyNullMap.put(null, null);
        assertEquals(JsonUtilities.getMapAsString(FACTORY, emptyMap), StringUtils.EMPTY);

        // Test full map
        assertEquals(JsonUtilities.getMapAsString(FACTORY, map), expectedResult);
    }

    /**
     * Test of getStringAsMap method, of class JsonUtilities.
     */
    @Test
    public void testGetStringAsMap() {
        // Test null
        assertEquals(JsonUtilities.getStringAsMap(FACTORY, null), Collections.emptyMap());

        // Test empty String
        final String emptyString = StringUtils.EMPTY;
        assertEquals(JsonUtilities.getStringAsMap(FACTORY, emptyString), Collections.emptyMap());

        // Test full String
        assertEquals(JsonUtilities.getStringAsMap(FACTORY, expectedResult), map);
    }
}
