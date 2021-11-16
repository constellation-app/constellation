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
package au.gov.asd.tac.constellation.utilities.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
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
     * Test calls to JsonUtilities.getTextField for which no default is supplied.
     */
    @Test
    public void testGetTextField_NoDefault() {
        try {
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
        } catch (JsonProcessingException e) {
            // This would throw generating the JSON, which is not under test
        }  
    }
    
    /**
     * Test calls to JsonUtilities.getTextField for which a default is supplied.
     */
    @Test
    public void testGetTextField_Default() {
        try {
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
        } catch (JsonProcessingException e) {
            // This would throw generating the JSON, which is not under test
        }  
    }
    
    /**
     * Test calls to JsonUtilities.getFieldIterator.
     */
    @Test
    public void testGetFieldIterator() {
        try {
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
          } catch (JsonProcessingException e) {
            // This would throw generating the JSON, which is not under test
        }       
    }
    
    /**
     * Test calls to JsonUtilities.getTextFieldIterator.
     */
    @Test
    public void testGetTextFieldIterator() {
        try {
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
            
          } catch (JsonProcessingException e) {
            // This would throw generating the JSON, which is not under test
        }       
    }
    
    /**
     * Test calls to JsonUtilities.getIntegerField for which no default is supplied.
     */
    @Test
    public void testGetIntegerField_NoDefault() {
        try {
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
        } catch (JsonProcessingException e) {
            // This would throw generating the JSON, which is not under test
        }  
    }

    /**
     * Test calls to JsonUtilities.getIntegerFieldIterator.
     */
    @Test
    public void testGetIntegerFieldIterator() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode testJson = mapper.readTree("{\"1.k1\":\"aaa\", \"1.k2\": 12, \"1.k3\":{\"2.k1\": 21}}"); 
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
            
          } catch (JsonProcessingException e) {
            // This would throw generating the JSON, which is not under test
        }       
    }
    
    /**
     * Test calls to JsonUtilities.getIntegerField for which a default is supplied.
     */
    @Test
    public void testGetIntegerField_Default() {
        try {
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
        } catch (JsonProcessingException e) {
            // This would throw generating the JSON, which is not under test
        }  
    }
    
    /**
     * Test calls to JsonUtilities.getIntegerField for which no default is supplied.
     */
    @Test
    public void testGetLongField_NoDefault() {
        try {
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
        } catch (JsonProcessingException e) {
            // This would throw generating the JSON, which is not under test
        }  
    }
    
    /**
     * Test calls to JsonUtilities.getIntegerField for which a default is supplied.
     */
    @Test
    public void testGetLongField_Default() {
        try {
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
        } catch (JsonProcessingException e) {
            // This would throw generating the JSON, which is not under test
        }  
    }
    
    /**
     * Test calls to JsonUtilities.getDoubleField for which no default is supplied.
     */
    @Test
    public void testGetDoubleField_NoDefault() {
        try {
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
        } catch (JsonProcessingException e) {
            // This would throw generating the JSON, which is not under test
        }  
    }
    
    /**
     * Test calls to JsonUtilities.getDoubleField for which a default is supplied.
     */
    @Test
    public void testGetDoubleField_Default() {
        try {
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
        } catch (JsonProcessingException e) {
            // This would throw generating the JSON, which is not under test
        }  
    }
     
    /**
     * Test calls to JsonUtilities.getBooleanField for which no default is supplied.
     */
    @Test
    public void testGetBooleanField_NoDefault() {
        try {
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
        } catch (JsonProcessingException e) {
            // This would throw generating the JSON, which is not under test
        }  
    }
    
    /**
     * Test calls to JsonUtilities.getBooleanField for which a default is supplied.
     */
    @Test
    public void testGetBooleanField_Default() {
        try {
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
        } catch (JsonProcessingException e) {
            // This would throw generating the JSON, which is not under test
        }  
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
