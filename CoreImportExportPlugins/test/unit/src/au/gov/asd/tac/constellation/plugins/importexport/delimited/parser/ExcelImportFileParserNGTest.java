/*
 * Copyright 2010-2025 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.swing.filechooser.FileFilter;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for ExcelImportFileParser.
 *
 * @author sol695510
 */
public class ExcelImportFileParserNGTest {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of getFileFilter method, of class ExcelImportFileParser.
     *
     * @throws IOException
     */
    @Test
    public void testGetFileFilter() throws IOException {
        System.out.println("testGetFileFilter");

        final String fileChooserDescription = "Excel Files ("
                + FileExtensionConstants.XLS + ", "
                + FileExtensionConstants.XLSX + ")";

        final ExcelImportFileParser instance = new ExcelImportFileParser();
        final FileFilter fileFilter = instance.getFileFilter();

        assertEquals(fileFilter.getDescription(), fileChooserDescription);

        // If file is invalid and does not end with correct extension.
        final File file1 = File.createTempFile("fileInvalid", ".invalid");
        assertEquals(fileFilter.accept(file1), false);

        // If file does not exist.
        final File file2 = new File("/invalidPath/fileXls" + FileExtensionConstants.XLS);
        final File file3 = new File("/invalidPath/fileXlsx" + FileExtensionConstants.XLSX);
        assertEquals(fileFilter.accept(file2), false);
        assertEquals(fileFilter.accept(file3), false);

        // If file exists, is valid and ends with correct extension.
        final File file4 = File.createTempFile("fileXls", FileExtensionConstants.XLS);
        final File file5 = File.createTempFile("fileXlsx", FileExtensionConstants.XLSX);
        assertEquals(fileFilter.accept(file4), true);
        assertEquals(fileFilter.accept(file5), true);

        // If file is a directory.
        final File fileMock = mock(File.class);
        doReturn("directory").when(fileMock).getName();
        doReturn(false).when(fileMock).isFile();
        doReturn(true).when(fileMock).isDirectory();
        assertEquals(fileFilter.accept(fileMock), true);

        Files.deleteIfExists(file1.toPath());
        Files.deleteIfExists(file4.toPath());
        Files.deleteIfExists(file5.toPath());
    }
}
