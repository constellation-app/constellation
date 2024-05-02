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
package au.gov.asd.tac.constellation.graph.utilities.hashmod;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for HashmodCSVImportFileParser.
 *
 * @author sol695510
 */
public class HashmodCSVImportFileParserNGTest {

    private static HashmodInputSource hashmodInputSourceMock;
    private static PluginParameters pluginParametersMock;
    private static CSVParser CSVParserMock;
    private static Iterator<CSVRecord> iteratorMock;
    private static CSVRecord CSVRecordMock;

    public HashmodCSVImportFileParserNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        hashmodInputSourceMock = mock(HashmodInputSource.class);
        pluginParametersMock = mock(PluginParameters.class);
        CSVParserMock = mock(CSVParser.class);
        iteratorMock = mock(Iterator.class);
        CSVRecordMock = mock(CSVRecord.class);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of parse method, of class HashmodCSVImportFileParser.
     *
     * @throws IOException
     */
    @Test
    public void testParse() throws IOException {
        System.out.println("testParse");

        final HashmodCSVImportFileParser instance = spy(new HashmodCSVImportFileParser());
        doCallRealMethod().when(instance).parse(any(HashmodInputSource.class));

        // When the CSV file is empty.
        doReturn(CSVParserMock).when(instance).getCSVParser(hashmodInputSourceMock);
        doReturn(iteratorMock).when(CSVParserMock).iterator();

        final List<String[]> expResult1 = new ArrayList<>();
        final List<String[]> result1 = instance.parse(hashmodInputSourceMock);

        assertEquals(result1, expResult1);

        // When there are CSV records to be parsed in the file.
        doReturn(true, true, false).when(iteratorMock).hasNext();
        doReturn(CSVRecordMock, CSVRecordMock).when(iteratorMock).next();
        doReturn(1).when(CSVRecordMock).size();
        doReturn("test").when(CSVRecordMock).get(0);

        final String[] line = new String[1];
        line[0] = "test";

        final List<String[]> list = new ArrayList<>();
        list.add(line);
        list.add(line);

        final List<String[]> expResult2 = list;
        final List<String[]> result2 = instance.parse(hashmodInputSourceMock);

        assertThat(result2).usingRecursiveComparison().isEqualTo(expResult2);
    }

    /**
     * Test of preview method, of class HashmodCSVImportFileParser.
     *
     * @throws IOException
     */
    @Test
    public void testPreview() throws IOException {
        System.out.println("testPreview");

        final HashmodCSVImportFileParser instance = spy(new HashmodCSVImportFileParser());
        doCallRealMethod().when(instance).preview(any(HashmodInputSource.class), anyInt());

        // When the CSV file is empty.
        doReturn(CSVParserMock).when(instance).getCSVParser(hashmodInputSourceMock);
        doReturn(iteratorMock).when(CSVParserMock).iterator();

        // The limit value is irrelevant in this case.
        final SecureRandom rand = new SecureRandom();
        final int limit = rand.nextInt(10) + 1;

        final List<String[]> expResult1 = new ArrayList<>();
        final List<String[]> result1 = instance.preview(hashmodInputSourceMock, limit);

        assertEquals(result1, expResult1);

        // When there are 2 CSV records to be parsed in the file and the limit is 0.
        doReturn(true, true, false).when(iteratorMock).hasNext();
        doReturn(CSVRecordMock, CSVRecordMock).when(iteratorMock).next();
        doReturn(1).when(CSVRecordMock).size();
        doReturn("test").when(CSVRecordMock).get(0);

        final String[] line = new String[1];
        line[0] = "test";

        final List<String[]> explist = new ArrayList<>();
        explist.add(line);

        // Only 1 record should be returned by preview().
        final List<String[]> expResult2 = explist;
        final List<String[]> result2 = instance.preview(hashmodInputSourceMock, 0);

        assertThat(result2).usingRecursiveComparison().isEqualTo(expResult2);

        // When there are 4 CSV records to be parsed in the file and the limit is 2
        doReturn(true, true, true, true, false).when(iteratorMock).hasNext();
        doReturn(CSVRecordMock, CSVRecordMock, CSVRecordMock, CSVRecordMock).when(iteratorMock).next();

        explist.add(line);

        // Only 2 records should be returned by preview().
        final List<String[]> expResult3 = explist;
        final List<String[]> result3 = instance.preview(hashmodInputSourceMock, 2);

        assertThat(result3).usingRecursiveComparison().isEqualTo(expResult3);
    }
}
