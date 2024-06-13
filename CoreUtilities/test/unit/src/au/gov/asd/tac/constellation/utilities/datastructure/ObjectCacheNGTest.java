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
package au.gov.asd.tac.constellation.utilities.datastructure;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class ObjectCacheNGTest {

    private static final String KEY1 = "key1";
    private static final String KEY2 = "key2";
    private static final String VALUE1 = "value1";
    private static final String VALUE2 = "value2";
    private static final String VALUE3 = "value3";

    private ObjectCache<String, String> objectCache;

    public ObjectCacheNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        objectCache = new ObjectCache();
        objectCache.add(KEY1, VALUE1);
        objectCache.add(KEY1, VALUE2);
        objectCache.add(KEY2, VALUE3);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void size() {
        assertEquals(objectCache.size(), 2);
    }

    @Test
    public void contains() {
        assertTrue(objectCache.contains(KEY1));
        assertFalse(objectCache.contains("random key"));
    }

    @Test
    public void keys() {
        assertEquals(objectCache.keys(), Set.of(KEY1, KEY2));
    }

    @Test
    public void values() {
        assertEquals(objectCache.values(), Set.of(VALUE1, VALUE2, VALUE3));
    }

    @Test
    public void get() {
        assertEquals(objectCache.get(KEY1), Set.of(VALUE1, VALUE2));
        assertEquals(objectCache.get("Random Key"), null);
    }

    @Test
    public void getRandom() {
        final List<String> possibleValues = List.of(VALUE1, VALUE2);
        assertTrue(possibleValues.contains(objectCache.getRandom(KEY1)));

        assertEquals(objectCache.getRandom(KEY2), VALUE3);
    }

    @Test
    public void remove() {
        assertEquals(objectCache.get(KEY1), Set.of(VALUE1, VALUE2));
        objectCache.remove(KEY1);
        assertEquals(objectCache.get(KEY1), null);
    }

    @Test
    public void clear() {
        assertEquals(objectCache.size(), 2);
        objectCache.clear();
        assertEquals(objectCache.size(), 0);
    }

    @Test
    public void forEach() {
        final Set<String> actions = new HashSet<>();
        objectCache.forEach((key, values)
                -> actions.addAll(
                        values.stream()
                                .map(value -> key + "::" + value)
                                .collect(Collectors.toSet())
                )
        );

        assertEquals(actions, Set.of(
                KEY1 + "::" + VALUE1,
                KEY1 + "::" + VALUE2,
                KEY2 + "::" + VALUE3
        ));
    }

    @Test
    public void testToString() {
        assertEquals(objectCache.toString(), "{key1=[value2, value1], key2=[value3]}");
    }
}
