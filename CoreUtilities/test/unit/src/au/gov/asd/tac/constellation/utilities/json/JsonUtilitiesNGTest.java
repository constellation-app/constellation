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
import org.apache.commons.lang3.StringUtils;
import static org.testng.Assert.assertEquals;
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
    public void testgetTextField_NoDefault() {
        try {
            ObjectMapper mapper = new ObjectMapper();   
            JsonNode testJson = mapper.readTree("{\"1.k1\":\"1.v1\", \"1.k2\":\"1.v2\", \"1.k3\":{\"2.k1\": \"2.v1\"}}");  
            
            // Search for missing top level value
            assertEquals(JsonUtilities.getTextField(testJson, "1.Missing"), null, "Null returned if not found");
            
            // Search for existing top level value
            assertEquals(JsonUtilities.getTextField(testJson, "1.k2"), "1.v2", "Value returned if found");
            
            // Search for missing nested value
            assertEquals(JsonUtilities.getTextField(testJson, "1.k3", "2.Missing"), null, "Null returned if not found");
            
            // Search for existing nested value
            assertEquals(JsonUtilities.getTextField(testJson, "1.k3", "2.k1"), "2.v1", "Null returned if not found");  
        } catch (JsonProcessingException e) {
            // This would throw generating the JSON, which is not under test
        }  
    }
    
    /**
     * Test calls to JsonUtilities.getTextField for which a default is supplied.
     */
    @Test
    public void testgetTextField_Default() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode testJson = mapper.readTree("{\"1.k1\":\"1.v1\", \"1.k2\":\"1.v2\", \"1.k3\":{\"2.k1\": \"2.v1\"}}");  
            
            // Search for missing top level value
            assertEquals(JsonUtilities.getTextField("default", testJson, "1.Missing"), "default", "Null returned if not found");
            
            // Search for existing top level value
            assertEquals(JsonUtilities.getTextField("default", testJson, "1.k2"), "1.v2", "Value returned if found");
            
            // Search for missing nested value
            assertEquals(JsonUtilities.getTextField("default", testJson, "1.k3", "2.Missing"), "default", "Null returned if not found");
            
            // Search for existing nested value
            assertEquals(JsonUtilities.getTextField("default", testJson, "1.k3", "2.k1"), "2.v1", "Null returned if not found");  
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
            nodes = new ArrayList<String>();
            while(iterator.hasNext()) {
                nodes.add(iterator.next().toString());  
            }
            assertEquals(nodes.toString(), "[]", "Empty node iterator matches");        
            
            
            iterator = JsonUtilities.getFieldIterator(testJson, "1.Missing");
            nodes = new ArrayList<String>();
            while(iterator.hasNext()) {
                nodes.add(iterator.next().toString());  
            }
            assertEquals(nodes.toString(), "[]", "Missing node iterator matches");  
          } catch (JsonProcessingException e) {
            // This would throw generating the JSON, which is not under test
        }       
    }
    
    /**
     * Test calls to JsonUtilities.getFieldIterator.
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
            
            iterator = JsonUtilities.getTextFieldIterator(testJson, "1.k1");
            nodes = new ArrayList<String>();
            while(iterator.hasNext()) {
                nodes.add(iterator.next());  
            }
            assertEquals(nodes.toString(), "[]", "Empty node iterator matches");        
            
            
            iterator = JsonUtilities.getTextFieldIterator(testJson, "1.Missing");
            nodes = new ArrayList<String>();
            while(iterator.hasNext()) {
                nodes.add(iterator.next());  
            }
            assertEquals(nodes.toString(), "[]", "Missing node iterator matches");  
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
