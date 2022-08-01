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

import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.swing.JFileChooser;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for HashmodPanel.
 *
 * @author sol695510
 */
public class HashmodPanelNGTest {

    public HashmodPanelNGTest() {
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
     * Test of getHasmodFileChooser method, of class HashmodPanel.
     *
     * @throws IOException
     */
    @Test
    public void testGetHasmodFileChooser() throws IOException {
        System.out.println("testGetHasmodFileChooser");

        final String fileChooserTitle = "Select a CSV for the Hashmod";
        final String fileChooserDescription = "CSV Files (" + FileExtensionConstants.COMMA_SEPARATED_VALUE + ")";

        final HashmodPanel instance = new HashmodPanel(mock(Hashmod.class));
        final JFileChooser fileChooser = instance.getHasmodFileChooser().createFileChooser();

        // Ensure file chooser is constructed correctly.
        assertEquals(fileChooser.getDialogTitle(), fileChooserTitle);
        assertEquals(fileChooser.getChoosableFileFilters().length, 1);
        assertEquals(fileChooser.getChoosableFileFilters()[0].getDescription(), fileChooserDescription);

        // If file is invalid and does not end with correct extension.
        final File file1 = File.createTempFile("fileInvalid", ".invalid");
        assertEquals(fileChooser.getChoosableFileFilters()[0].accept(file1), false);

        // If files do not exist.
        final File file2 = new File("/invalidPath/fileCsv" + FileExtensionConstants.COMMA_SEPARATED_VALUE);
        assertEquals(fileChooser.getChoosableFileFilters()[0].accept(file2), false);

        // If files exist, are valid and end with correct extensions.
        final File file3 = File.createTempFile("fileCsv", FileExtensionConstants.COMMA_SEPARATED_VALUE);
        assertEquals(fileChooser.getChoosableFileFilters()[0].accept(file3), true);

        // If file is a directory.
        final File fileMock = mock(File.class);
        doReturn("directory").when(fileMock).getName();
        doReturn(false).when(fileMock).isFile();
        doReturn(true).when(fileMock).isDirectory();
        assertEquals(fileChooser.getChoosableFileFilters()[0].accept(fileMock), true);

        Files.deleteIfExists(file1.toPath());
        Files.deleteIfExists(file3.toPath());
    }
}
