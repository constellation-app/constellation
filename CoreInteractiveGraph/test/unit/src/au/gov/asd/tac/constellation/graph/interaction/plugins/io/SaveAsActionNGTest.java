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
package au.gov.asd.tac.constellation.graph.interaction.plugins.io;

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
 * Test class for SaveAsAction.
 *
 * @author sol695510
 */
public class SaveAsActionNGTest {

    public SaveAsActionNGTest() {
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
     * Test of create method, of class SaveAsAction.
     */
    @Test
    public void testCreate() {
        System.out.println("testCreate");
    }

    /**
     * Test of isEnabled method, of class SaveAsAction.
     */
    @Test
    public void testIsEnabled() {
        System.out.println("testIsEnabled");
    }

    /**
     * Test of isSaved method, of class SaveAsAction.
     */
    @Test
    public void testIsSaved() {
        System.out.println("testIsSaved");
    }

    /**
     * Test of actionPerformed method, of class SaveAsAction.
     */
    @Test
    public void testActionPerformed() {
        System.out.println("testActionPerformed");
    }

    /**
     * Test of isFileInUse method, of class SaveAsAction.
     */
    @Test
    public void testIsFileInUse() {
        System.out.println("testIsFileInUse");
    }

    /**
     * Test of createContextAwareInstance method, of class SaveAsAction.
     */
    @Test
    public void testCreateContextAwareInstance() {
        System.out.println("testCreateContextAwareInstance");
    }

    /**
     * Test of addPropertyChangeListener method, of class SaveAsAction.
     */
    @Test
    public void testAddPropertyChangeListener() {
        System.out.println("testAddPropertyChangeListener");
    }

    /**
     * Test of removePropertyChangeListener method, of class SaveAsAction.
     */
    @Test
    public void testRemovePropertyChangeListener() {
        System.out.println("testRemovePropertyChangeListener");
    }

    /**
     * Test of _isEnabled method, of class SaveAsAction.
     */
    @Test
    public void test_isEnabled() {
        System.out.println("test_isEnabled");
    }

    /**
     * Test of getSaveFileChooser method, of class SaveAsAction.
     *
     * @throws IOException
     */
    @Test
    public void testGetSaveFileChooser() throws IOException {
        System.out.println("testGetSaveFileChooser");

        final String fileChooserTitle = "Save As";
        final String fileChooserDescription = "Constellation Files (" + FileExtensionConstants.STAR + ")";

        final SaveAsAction instance = new SaveAsAction();
        final JFileChooser fileChooser = instance.getSaveFileChooser().createFileChooser();

        // Ensure file chooser is constructed correctly.
        assertEquals(fileChooser.getDialogTitle(), fileChooserTitle);
        assertEquals(fileChooser.getChoosableFileFilters().length, 1);
        assertEquals(fileChooser.getChoosableFileFilters()[0].getDescription(), fileChooserDescription);

        // If file is invalid and does not end with correct extension.
        final File file1 = File.createTempFile("fileInvalid", ".invalid");
        assertEquals(fileChooser.getChoosableFileFilters()[0].accept(file1), false);

        // If files do not exist.
        final File file2 = new File("/invalidPath/fileStar" + FileExtensionConstants.STAR);
        assertEquals(fileChooser.getChoosableFileFilters()[0].accept(file2), false);

        // If files exist, are valid and end with correct extensions.
        final File file3 = File.createTempFile("fileStar", FileExtensionConstants.STAR);
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
