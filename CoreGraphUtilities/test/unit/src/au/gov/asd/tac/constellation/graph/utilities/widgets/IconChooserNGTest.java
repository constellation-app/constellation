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
package au.gov.asd.tac.constellation.graph.utilities.widgets;

import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
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
 * Test class for IconChooser.
 *
 * @author sol695510
 */
public class IconChooserNGTest {

    public IconChooserNGTest() {
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
     * Test of getIconMap method, of class IconChooser.
     */
    @Test
    public void testGetIconMap() {
        System.out.println("testGetIconMap");
    }

    /**
     * Test of getSelectedIconName method, of class IconChooser.
     */
    @Test
    public void testGetSelectedIconName() {
        System.out.println("testGetSelectedIconName");
    }

    /**
     * Test of isIconAdded method, of class IconChooser.
     */
    @Test
    public void testIsIconAdded() {
        System.out.println("testIsIconAdded");
    }

    /**
     * Test of valueChanged method, of class IconChooser.
     */
    @Test
    public void testValueChanged_TreeSelectionEvent() {
        System.out.println("testValueChanged_TreeSelectionEvent");
    }

    /**
     * Test of valueChanged method, of class IconChooser.
     */
    @Test
    public void testValueChanged_ListSelectionEvent() {
        System.out.println("testValueChanged_ListSelectionEvent");
    }

    /**
     * Test of getAddIconFileChooser method, of class IconChooser.
     *
     * @throws IOException
     */
    @Test
    public void testGetAddIconFileChooser() throws IOException {
        System.out.println("testGetAddIconFileChooser");

        final String fileChooserTitle = "Add icons";
        final String fileChooserDescription = "Graph Icon";

        final Set<ConstellationIcon> icons = new HashSet<>();
        final String selectedIconName = "";

        final IconChooser instance = new IconChooser(icons, selectedIconName);
        final JFileChooser fileChooser = instance.getAddIconFileChooser().createFileChooser();

        // Ensure file chooser is constructed correctly.
        assertEquals(fileChooser.getDialogTitle(), fileChooserTitle);
        assertEquals(fileChooser.getChoosableFileFilters().length, 1);
        assertEquals(fileChooser.getChoosableFileFilters()[0].getDescription(), fileChooserDescription);

        // If file is invalid and does not end with correct extension.
        final File file1 = File.createTempFile("icon.fileInvalid", ".invalid");
        assertEquals(fileChooser.getChoosableFileFilters()[0].accept(file1), false);

        // If files do not exist.
        final File file2 = new File("/invalidPath/icon.filePng" + FileExtensionConstants.PNG);
        final File file3 = new File("/invalidPath/icon.fileJpg" + FileExtensionConstants.JPG);
        assertEquals(fileChooser.getChoosableFileFilters()[0].accept(file2), false);
        assertEquals(fileChooser.getChoosableFileFilters()[0].accept(file3), false);

        // If files exist, are valid and end with correct extensions.
        final File file4 = File.createTempFile("icon.filePng", FileExtensionConstants.PNG);
        final File file5 = File.createTempFile("icon.fileJpg", FileExtensionConstants.JPG);
        assertEquals(fileChooser.getChoosableFileFilters()[0].accept(file4), true);
        assertEquals(fileChooser.getChoosableFileFilters()[0].accept(file5), true);

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
