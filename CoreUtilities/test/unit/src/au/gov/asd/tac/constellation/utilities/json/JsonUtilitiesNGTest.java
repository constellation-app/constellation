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
package au.gov.asd.tac.constellation.utilities.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections.MapUtils;
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
        assertEquals(JsonUtilities.getStringAsMap(FACTORY, null), MapUtils.EMPTY_MAP);

        // Test empty String
        final String emptyString = StringUtils.EMPTY;
        assertEquals(JsonUtilities.getStringAsMap(FACTORY, emptyString), MapUtils.EMPTY_MAP);

        // Test full String
        assertEquals(JsonUtilities.getStringAsMap(FACTORY, expectedResult), map);
    }
}
