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
package au.gov.asd.tac.constellation.graph.processing;

import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Graph RecordStore Test.
 *
 * @author arcturus
 */
public class GraphRecordStoreNGTest {

    public GraphRecordStoreNGTest() {
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
     * Test of getColumn method, of class GraphRecordStore.
     */
    @Test
    public void testGetColumn() {
        final String key = "key1";

        final GraphRecordStore instance = new GraphRecordStore();
        instance.add();
        instance.set("key1", "value1");
        instance.set("key2", "value2");
        instance.add();
        instance.set("key1", "value2");
        instance.set("key3", "value3");
        instance.set("key4", "value4");

        final Object[][] result = instance.getColumn(key);
        assertEquals("value1", result[0][0]);
        assertEquals("value2", result[0][1]);
    }

    /**
     * Test of createColumn method, of class GraphRecordStore.
     */
    @Test
    public void testCreateColumn() {
        final String key = "key1";

        final Object[][] values = new Object[1][GraphRecordStore.BATCH_SIZE];
        values[0][0] = "value1";
        values[0][1] = "value2";

        final GraphRecordStore instance = new GraphRecordStore();
        instance.createColumn(key, values);

        final Object[][] column = instance.getColumn(key);
        assertEquals("value1", column[0][0]);
        assertEquals("value2", column[0][1]);
    }

    /**
     * Test of set method, of class GraphRecordStore.
     */
    @Test
    public void testSet() {
        final int record = 0;
        final String key = "key1";
        final String value = "value1";

        final GraphRecordStore instance = new GraphRecordStore();
        instance.add();
        instance.set(record, key, value);

        assertEquals(value, instance.get(record, key));
    }

    /**
     * Test of values method, of class GraphRecordStore.
     */
    @Test
    public void testValues() {
        final int record = 1;

        final GraphRecordStore instance = new GraphRecordStore();
        instance.add();
        instance.set("key1", "value1");
        instance.set("key2", "value2");
        instance.add();
        instance.set("key1", "value2");
        instance.set("key3", "value3");
        instance.set("key4", "value4");

        final List<String> expResult = new ArrayList<>();
        expResult.add("value2");
        expResult.add(null);
        expResult.add("value3");
        expResult.add("value4");

        final List<String> result = instance.values(record);
        assertEquals(result, expResult);
    }

    /**
     * Test of add method, of class GraphRecordStore.
     */
    @Test
    public void testAdd() {
        final GraphRecordStore recordStore = new GraphRecordStore();
        recordStore.add();
        recordStore.set("key", "value");

        final GraphRecordStore instance = new GraphRecordStore();
        assertEquals(instance.size(), 0);

        instance.add(recordStore);
        assertEquals(instance.size(), 1);
        assertEquals(instance.get(0, "key"), "value");

        final GraphRecordStore recordStore2 = new GraphRecordStore();
        recordStore2.add();
        recordStore2.set("key2", "value2");
        instance.add(recordStore2);
        assertEquals(instance.size(), 2);
        assertEquals(instance.get(0, "key"), "value");
        assertEquals(instance.get(1, "key2"), "value2");
    }

    @Test
    public void testAddWithTheSameRecordStore() {
        final GraphRecordStore recordStore = new GraphRecordStore();
        recordStore.add();
        recordStore.set("key", "value");

        final GraphRecordStore instance = new GraphRecordStore();
        assertEquals(instance.size(), 0);

        instance.add(recordStore);
        assertEquals(instance.size(), 1);
        assertEquals(instance.get(0, "key"), "value");

        instance.add(recordStore);
        assertEquals(instance.size(), 2);
        assertEquals(instance.get(0, "key"), "value");
        assertEquals(instance.get(1, "key"), "value");
    }

    /**
     * Test of keysWithType method, of class GraphRecordStore.
     */
    @Test
    public void testKeysWithStringType() {
        final GraphRecordStore instance = new GraphRecordStore();
        instance.add();
        instance.set("foo", "bar");

        final List<String> expResult = new ArrayList<>();
        expResult.add("foo<string>");

        final List<String> result = instance.keysWithType();
        assertEquals(result, expResult);
    }

    /**
     * Test of keysWithType method, of class GraphRecordStore.
     */
    @Test
    public void testKeysWithBooleanType() {
        final GraphRecordStore instance = new GraphRecordStore();
        instance.add();
        instance.set("foo<boolean>", "bar");

        final List<String> expResult = new ArrayList<>();
        expResult.add("foo<boolean>");

        final List<String> result = instance.keysWithType();
        assertEquals(result, expResult);
    }

    /**
     * Test of toStringVerbose method, of class GraphRecordStore.
     */
    @Test
    public void testToStringVerbose() {
        final GraphRecordStore instance = new GraphRecordStore();
        instance.add();
        instance.set("key1", "value1");
        instance.set("key2", "value2");
        instance.add();
        instance.set("key1", "value2");
        instance.set("key3", "value3");
        instance.set("key4", "value4");

        final String expResult = "key1<string> = value1, key2<string> = value2\nkey1<string> = value2, key3<string> = value3, key4<string> = value4\n";
        final String result = instance.toStringVerbose();
        assertEquals(result, expResult);
    }

}
