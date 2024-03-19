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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * RecordStore Utilities Test.
 *
 * @author algol
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class RecordStoreUtilitiesNGTest extends ConstellationTest {

    public RecordStoreUtilitiesNGTest() {
    }

    @Test
    public void testToCsvEmpty() {
        try {
            final File file = new File("test1.csv");
            final RecordStore recordStore = new GraphRecordStore();

            RecordStoreUtilities.toCsv(recordStore, new FileOutputStream(file));

            assertTrue(file.isFile());
        } catch (FileNotFoundException ex) {
            Assert.fail(ex.getLocalizedMessage());
        }
    }

    @Test
    public void testToCsvEmptyRow() {
        try {
            final File file = new File("test2.csv");

            final RecordStore recordStore = new GraphRecordStore();
            recordStore.add();
            RecordStoreUtilities.toCsv(recordStore, new FileOutputStream(file));

            assertTrue(file.isFile());
        } catch (FileNotFoundException ex) {
            Assert.fail(ex.getLocalizedMessage());
        }
    }

    @Test
    public void testToCsvWithOneResult() {
        try {
            final File file = new File("test3.csv");
            final RecordStore recordStore = new GraphRecordStore();
            recordStore.add();
            recordStore.set("key", "value");
            RecordStoreUtilities.toCsv(recordStore, new FileOutputStream(file));

            final BufferedReader in = new BufferedReader(new FileReader(file));
            String line = in.readLine();
            assertEquals(line, "key");
            line = in.readLine();
            assertEquals(line, "value");
        } catch (FileNotFoundException ex) {
            Assert.fail(ex.getLocalizedMessage());
        } catch (IOException ex) {
            Assert.fail(ex.getLocalizedMessage());
        }
    }

    @Test
    public void testToCsvWithManyResult() {
        try {
            final File file = new File("test4.csv");
            final RecordStore recordStore = new GraphRecordStore();
            recordStore.add();
            recordStore.set("key1", "value1");
            recordStore.set("key2", "value2");
            RecordStoreUtilities.toCsv(recordStore, new FileOutputStream(file));

            final BufferedReader in = new BufferedReader(new FileReader(file));
            String line = in.readLine();
            assertEquals(line, "key1,key2");
            line = in.readLine();
            assertEquals(line, "value1,value2");
        } catch (FileNotFoundException ex) {
            Assert.fail(ex.getLocalizedMessage());
        } catch (IOException ex) {
            Assert.fail(ex.getLocalizedMessage());
        }
    }

    @Test
    public void testFromCsv() {
        try {
            final StringBuilder sb = new StringBuilder();
            sb.append("source.identifier,source.type,destination.identifier,destination.type\n");
            sb.append("foo,Person,bar,Person\n");
            sb.append("foo2,Person,bar2,Person\n");

            RecordStore actualRecordStore;
            try (final InputStream is = new ByteArrayInputStream(sb.toString().getBytes())) {
                actualRecordStore = RecordStoreUtilities.fromCsv(is);
            }

            final RecordStore expectedRecordStore = new GraphRecordStore();
            expectedRecordStore.add();
            expectedRecordStore.set("source.identifier", "foo");
            expectedRecordStore.set("source.type", "Person");
            expectedRecordStore.set("destination.identifier", "bar");
            expectedRecordStore.set("destination.type", "Person");
            expectedRecordStore.add();
            expectedRecordStore.set("source.identifier", "foo2");
            expectedRecordStore.set("source.type", "Person");
            expectedRecordStore.set("destination.identifier", "bar2");
            expectedRecordStore.set("destination.type", "Person");

            assertEquals(actualRecordStore, expectedRecordStore);
        } catch (IOException ex) {
            Assert.fail(ex.getLocalizedMessage());
        }
    }

}
