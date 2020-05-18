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
 * Tabular RecordStore Test.
 *
 * @author arcturus
 */
public class TabularRecordStoreNGTest {

    TabularRecordStore instance = null;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        instance = new TabularRecordStore();
        instance.add();
        instance.set("key1", "value1");
        instance.set("key2", "value2");
        instance.add();
        instance.set("key1", "value2");
        instance.set("key3", "value3");
        instance.set("key4", "value4");
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getColumn method, of class TabularRecordStore.
     */
    @Test
    public void testGetColumn() {
        final String key = "key1";

        final Object[][] result = instance.getColumn(key);
        assertEquals(result[0][0], "value1");
        assertEquals(result[0][1], "value2");
    }

    /**
     * Test of createColumn method, of class TabularRecordStore.
     */
    @Test
    public void testCreateColumn() {
        final String key = "key1";

        final Object[][] values = new Object[1][TabularRecordStore.BATCH_SIZE];
        values[0][0] = "value1";
        values[0][1] = "value2";

        final TabularRecordStore instance = new TabularRecordStore();
        instance.createColumn(key, values);

        final Object[][] column = instance.getColumn(key);
        assertEquals(column[0][0], "value1");
        assertEquals(column[0][1], "value2");
    }

    /**
     * Test of hasValue method, of class TabularRecordStore.
     */
    @Test
    public void testHasValueUsingValuesArray() {
        final Object[][] values = instance.getColumn("key1");

        boolean result = TabularRecordStore.hasValue(values, 0);
        assertEquals(result, true);
        result = TabularRecordStore.hasValue(values, 1);
        assertEquals(result, true);
        result = TabularRecordStore.hasValue(values, 2);
        assertEquals(result, false);
    }

    /**
     * Test of getValue method, of class TabularRecordStore.
     */
    @Test
    public void testGetValue() {
        final Object[][] values = instance.getColumn("key1");

        String result = TabularRecordStore.getValue(values, 0);
        assertEquals(result, "value1");
        result = TabularRecordStore.getValue(values, 1);
        assertEquals(result, "value2");
        result = TabularRecordStore.getValue(values, 2);
        assertEquals(result, null);
    }

    /**
     * Test of add method, of class TabularRecordStore.
     */
    @Test
    public void testAdd() {
        final TabularRecordStore instance = new TabularRecordStore();
        int expResult = 0;
        int result = instance.add();
        assertEquals(result, expResult);
        expResult = 1;
        result = instance.add();
        assertEquals(result, expResult);
    }

    /**
     * Test of add method, of class TabularRecordStore.
     */
    @Test
    public void testAddRecordStore() {
        final TabularRecordStore instance = new TabularRecordStore();
        assertEquals(instance.size(), 0);

        final RecordStore recordStore = new TabularRecordStore();
        recordStore.add();
        instance.add(recordStore);
        assertEquals(instance.size(), 1);
    }

    @Test
    public void testAddRecordStoreWithDuplicateRecords() {
        final RecordStore recordStore = new TabularRecordStore();
        recordStore.add();
        recordStore.set("key", "value");
        recordStore.add();
        recordStore.set("key", "value");

        final TabularRecordStore instance = new TabularRecordStore();
        instance.add(recordStore);
        assertEquals(instance.size(), 2);
    }

    /**
     * Test of index method, of class TabularRecordStore.
     */
    @Test
    public void testIndex() {
        final TabularRecordStore instance = new TabularRecordStore();
        int result = instance.index();
        assertEquals(result, -1);

        instance.add();
        result = instance.index();
        assertEquals(result, 0);

        instance.add();
        result = instance.index();
        assertEquals(result, 1);
    }

    /**
     * Test of next method, of class TabularRecordStore.
     */
    @Test
    public void testNextWithNoRecords() {
        boolean expResult = false;
        boolean result = instance.next();
        assertEquals(result, expResult);
    }

    @Test
    public void testNextWithSomeRecords() {
        final TabularRecordStore instance = new TabularRecordStore();
        instance.add();
        instance.add();

        boolean result = instance.next();
        assertEquals(result, false);

        instance.reset();
        result = instance.next();
        assertEquals(result, true);
        result = instance.next();
        assertEquals(result, true);
        result = instance.next();
        assertEquals(result, false);
    }

    /**
     * Test of reset method, of class TabularRecordStore.
     */
    @Test
    public void testReset() {
        final TabularRecordStore instance = new TabularRecordStore();
        instance.add();

        assertEquals(instance.index(), 0);
        instance.reset();
        assertEquals(instance.index(), -1);
    }

    /**
     * Test of close method, of class TabularRecordStore.
     */
    @Test
    public void testClose() {
        final TabularRecordStore instance = new TabularRecordStore();

        instance.add();
        assertEquals(instance.size(), 1);
        instance.close();
        assertEquals(instance.size(), 1); // fyi the implementation does not empty the size
    }

    /**
     * Test of hasValue method, of class TabularRecordStore.
     */
    @Test
    public void testHasValueUsingKeyThatExists() {
        final String key = "key1";

        final boolean expResult = true;
        final boolean result = instance.hasValue(key);
        assertEquals(result, expResult);
    }

    @Test
    public void testHasValueUsingKeyThatDoesNotExist() {
        final String key = "foo";

        final boolean expResult = false;
        final boolean result = instance.hasValue(key);
        assertEquals(result, expResult);
    }

    /**
     * Test of hasValue method, of class TabularRecordStore.
     */
    @Test
    public void testHasValueUsingKeyAndIndex() {
        final int record = 0;
        final String key = "key1";

        final boolean expResult = true;
        final boolean result = instance.hasValue(record, key);
        assertEquals(result, expResult);
    }

    /**
     * Test of get method, of class TabularRecordStore.
     */
    @Test
    public void testGetUsingValidKey() {
        final String key = "key1";

        final TabularRecordStore instance = new TabularRecordStore();
        instance.add();
        instance.set("key1", "value1");
        instance.set("key2", "value2");

        final String expResult = "value1";
        final String result = instance.get(key);
        assertEquals(result, expResult);
    }

    @Test
    public void testGetUsingInvalidKey() {
        final String key = "foo";

        final TabularRecordStore instance = new TabularRecordStore();
        instance.add();
        instance.set("key1", "value1");
        instance.set("key2", "value2");

        final String expResult = null;
        final String result = instance.get(key);
        assertEquals(result, expResult);
    }

    /**
     * Test of get method, of class TabularRecordStore.
     */
    @Test
    public void testGetUsingValidKeyAndIndex() {
        final int record = 0;
        final String key = "key1";
        final String expResult = "value1";
        final String result = instance.get(record, key);
        assertEquals(result, expResult);
    }

    /**
     * Test of set method, of class TabularRecordStore.
     */
    @Test
    public void testSetUsingKeyAndValue() {
        final String key = "key1";
        final String value = "value1";

        final TabularRecordStore instance = new TabularRecordStore();
        instance.add();
        instance.set(key, value);

        assertEquals(instance.get(key), value);
    }

    /**
     * Test of set method, of class TabularRecordStore.
     */
    @Test
    public void testSetUsingKeyValueAndIndex() {
        final int record = 0;
        final String key = "key1";
        final String value = "value1";

        instance.add();
        instance.set(record, key, value);

        assertEquals(instance.get(record, key), value);
    }

    /**
     * Test of values method, of class TabularRecordStore.
     */
    @Test
    public void testValuesFromFirstIndex() {
        final TabularRecordStore instance = new TabularRecordStore();
        instance.add();
        instance.set("key1", "value1");
        instance.set("key2", "value2");

        final List<String> expResult = new ArrayList<>();
        expResult.add("value1");
        expResult.add("value2");

        final List<String> result = instance.values();
        assertEquals(result, expResult);
    }

    @Test
    public void testValuesWithColumnMissingValue() {
        final List<String> expResult = new ArrayList<>();
        expResult.add("value2");
        expResult.add(null);
        expResult.add("value3");
        expResult.add("value4");

        final List<String> result = instance.values();
        assertEquals(result, expResult);
    }

    /**
     * Test of values method, of class TabularRecordStore.
     */
    @Test
    public void testValuesUsingIndex() {
        final int record = 1;

        final List<String> expResult = new ArrayList<>();
        expResult.add("value2");
        expResult.add(null);
        expResult.add("value3");
        expResult.add("value4");

        final List<String> result = instance.values(record);
        assertEquals(result, expResult);
    }

    /**
     * Test of keys method, of class TabularRecordStore.
     */
    @Test
    public void testKeys() {
        final List<String> expResult = new ArrayList<>();
        expResult.add("key1");
        expResult.add("key2");
        expResult.add("key3");
        expResult.add("key4");

        final List<String> result = instance.keys();

        assertEquals(result, expResult);
    }

    /**
     * Test of getAll method, of class TabularRecordStore.
     */
    @Test
    public void testGetAll() {
        String key = "key1";

        final List<String> expResult = new ArrayList<>();
        expResult.add("value1");
        expResult.add("value2");

        final List<String> result = instance.getAll(key);
        assertEquals(result, expResult);
    }

    /**
     * Test of size method, of class TabularRecordStore.
     */
    @Test
    public void testSize() {
        final int expResult = 2;
        final int result = instance.size();
        assertEquals(result, expResult);
    }

    /**
     * Test of toString method, of class TabularRecordStore.
     */
    @Test
    public void testToString() {
        final String expResult = "Record Store with 2 rows and 4 columns.";
        final String result = instance.toString();
        assertEquals(result, expResult);
    }

    /**
     * Test of toStringVerbose method, of class TabularRecordStore.
     */
    @Test
    public void testToStringVerbose() {
        final String expResult = "key1 = value1, key2 = value2\nkey1 = value2, key3 = value3, key4 = value4\n";
        final String result = instance.toStringVerbose();
        assertEquals(result, expResult);
    }
}
