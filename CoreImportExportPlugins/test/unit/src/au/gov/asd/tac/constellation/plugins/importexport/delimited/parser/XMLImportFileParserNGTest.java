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
package au.gov.asd.tac.constellation.plugins.importexport.delimited.parser;

import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.swing.filechooser.FileFilter;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for XMLImportFileParser.
 *
 * @author sol695510
 */
public class XMLImportFileParserNGTest {

//    private static final Logger LOGGER = Logger.getLogger(XMLImportFileParser.class.getName());
    private static File fileMock;

    public XMLImportFileParserNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
//        if (!FxToolkit.isFXApplicationThreadRunning()) {
//            FxToolkit.registerPrimaryStage();
//        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
//        try {
//            FxToolkit.cleanupStages();
//        } catch (TimeoutException ex) {
//            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
//        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        fileMock = Mockito.mock(File.class);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of parse method, of class XMLImportFileParser.
     */
    @Test
    public void testParse() throws Exception {
        System.out.println("testParse");

    }

    /**
     * Test of preview method, of class XMLImportFileParser.
     */
    @Test
    public void testPreview() throws Exception {
        System.out.println("testPreview");

    }

    /**
     * Test of getFileFilter method, of class XMLImportFileParser.
     *
     * @throws IOException
     */
    @Test
    public void testGetFileFilter() throws IOException {
        System.out.println("testGetFileFilter");

        final String fileChooserDescription = "XML Files (" + FileExtensionConstants.XML + ")";

        final XMLImportFileParser instance = new XMLImportFileParser();
        final FileFilter fileFilter = instance.getFileFilter();

        assertEquals(fileFilter.getDescription(), fileChooserDescription);

        // If file is invalid and does not end with correct extension.
        final File file1 = File.createTempFile("fileInvalid", ".invalid");
        assertEquals(fileFilter.accept(file1), false);

        // If file does not exist.
        final File file2 = new File("/invalidPath/fileXml" + FileExtensionConstants.XML);
        assertEquals(fileFilter.accept(file2), false);

        // If file exists, is valid and ends with correct extension.
        final File file3 = File.createTempFile("fileXml", FileExtensionConstants.XML);
        assertEquals(fileFilter.accept(file3), true);

        // If file is a directory.
        doReturn("directory").when(fileMock).getName();
        doReturn(false).when(fileMock).isFile();
        doReturn(true).when(fileMock).isDirectory();
        assertEquals(fileFilter.accept(fileMock), true);

        Files.deleteIfExists(file1.toPath());
        Files.deleteIfExists(file3.toPath());
    }
}
