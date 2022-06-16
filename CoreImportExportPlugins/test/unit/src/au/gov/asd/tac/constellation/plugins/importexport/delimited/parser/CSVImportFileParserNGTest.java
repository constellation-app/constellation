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
package au.gov.asd.tac.constellation.plugins.importexport.delimited.parser;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.mockito.Mockito;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author sol695510
 */
public class CSVImportFileParserNGTest {

    private static InputSource inputSourceMock;
    private static PluginParameters pluginParametersMock;
    private static CSVParser CSVParserMock;
    private static Iterator<CSVRecord> iteratorMock;
    private static CSVRecord CSVRecordMock;
    private static File directoryMock;

    public CSVImportFileParserNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        inputSourceMock = Mockito.mock(InputSource.class);
        pluginParametersMock = Mockito.mock(PluginParameters.class);
        CSVParserMock = Mockito.mock(CSVParser.class);
        iteratorMock = Mockito.mock(Iterator.class);
        CSVRecordMock = Mockito.mock(CSVRecord.class);
        directoryMock = Mockito.mock(File.class);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of parse method, of class CSVImportFileParser.
     *
     * @throws IOException
     */
    @Test
    public void testParse() throws IOException {
        System.out.println("testParse");

        final CSVImportFileParser instance = spy(new CSVImportFileParser());
        doCallRealMethod().when(instance).parse(Mockito.any(InputSource.class), Mockito.any(PluginParameters.class));

        // When the CSV file is empty.
        doReturn(CSVParserMock).when(instance).getCSVParser(inputSourceMock);
        doReturn(iteratorMock).when(CSVParserMock).iterator();

        final List<String[]> expResult1 = new ArrayList<>();
        final List<String[]> result1 = instance.parse(inputSourceMock, pluginParametersMock);

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
        final List<String[]> result2 = instance.parse(inputSourceMock, pluginParametersMock);

        assertEquals(result2, expResult2);
    }

    /**
     * Test of preview method, of class CSVImportFileParser.
     *
     * @throws IOException
     */
    @Test
    public void testPreview() throws IOException {
        System.out.println("testPreview");

        final CSVImportFileParser instance = spy(new CSVImportFileParser());
        doCallRealMethod().when(instance).preview(Mockito.any(InputSource.class), Mockito.any(PluginParameters.class), Mockito.anyInt());

        // When the CSV file is empty.
        doReturn(CSVParserMock).when(instance).getCSVParser(inputSourceMock);
        doReturn(iteratorMock).when(CSVParserMock).iterator();

        // The limit value is irrelevant in this case.
        final SecureRandom rand = new SecureRandom();
        final int limit = rand.nextInt(10) + 1;

        final List<String[]> expResult1 = new ArrayList<>();
        final List<String[]> result1 = instance.preview(inputSourceMock, pluginParametersMock, limit);

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
        final List<String[]> result2 = instance.preview(inputSourceMock, pluginParametersMock, 0);

        assertEquals(result2, expResult2);

        // When there are 4 CSV records to be parsed in the file and the limit is 2
        doReturn(true, true, true, true, false).when(iteratorMock).hasNext();
        doReturn(CSVRecordMock, CSVRecordMock, CSVRecordMock, CSVRecordMock).when(iteratorMock).next();

        explist.add(line);

        // Only 2 records should be returned by preview().
        final List<String[]> expResult3 = explist;
        final List<String[]> result3 = instance.preview(inputSourceMock, pluginParametersMock, 2);

        assertEquals(result3, expResult3);
    }

    /**
     * Test of getFileFilter method, of class CSVImportFileParser.
     *
     * @throws IOException
     */
    @Test
    public void testGetFileFilter() throws IOException {
        System.out.println("testGetFileFilter");

        final String fileChooserDescription = "CSV Files (" + FileExtensionConstants.COMMA_SEPARATED_VALUE + ")";

        final CSVImportFileParser instance = new CSVImportFileParser();
        final FileFilter fileFilter = instance.getFileFilter();

        assertEquals(fileFilter.getDescription(), fileChooserDescription);

        // If file is invalid and does not end with correct extension.
        final File file1 = File.createTempFile("fileInvalid", ".invalid");
        assertEquals(fileFilter.accept(file1), false);

        // If file does not exist.
        final File file2 = new File("/invalidPath/fileCsv" + FileExtensionConstants.COMMA_SEPARATED_VALUE);
        assertEquals(fileFilter.accept(file2), false);

        // If file exists, is valid and ends with correct extension.
        final File file3 = File.createTempFile("fileCsv", FileExtensionConstants.COMMA_SEPARATED_VALUE);
        assertEquals(fileFilter.accept(file3), true);

        // If file is a directory.
        doReturn("directory").when(directoryMock).getName();
        doReturn(false).when(directoryMock).isFile();
        doReturn(true).when(directoryMock).isDirectory();
        assertEquals(fileFilter.accept(directoryMock), true);

        Files.deleteIfExists(file1.toPath());
        Files.deleteIfExists(file3.toPath());
    }
}
