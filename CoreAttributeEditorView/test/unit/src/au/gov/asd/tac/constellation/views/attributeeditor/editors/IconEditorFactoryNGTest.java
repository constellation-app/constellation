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
package au.gov.asd.tac.constellation.views.attributeeditor.editors;

import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory.AbstractEditor;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.IconEditorFactory.IconEditor;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.DefaultGetter;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
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
 * Test class for IconEditorFactory.
 *
 * @author sol695510
 */
public class IconEditorFactoryNGTest {

    public IconEditorFactoryNGTest() {
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
     * Test of createEditor method, of class IconEditorFactory.
     */
    @Test
    public void testCreateEditor() {
        System.out.println("testCreateEditor");

        final AbstractEditor<ConstellationIcon> instance = new IconEditorFactory().createEditor(
                mock(EditOperation.class),
                mock(DefaultGetter.class),
                mock(ValueValidator.class),
                "",
                mock(ConstellationIcon.class));

        assertEquals(instance.getClass(), IconEditor.class);
    }

    /**
     * Test of getIconEditorFileChooser method, of inner class IconEditor, of
     * class IconEditorFactory.
     *
     * @throws IOException
     */
    @Test
    public void getIconEditorFileChooser() throws IOException {
        System.out.println("getIconEditorFileChooser");

        final String fileChooserTitle = "Add New Icon(s)";
        final String fileChooserDescription = "Image Files (*" + FileExtensionConstants.JPG + ";*" + FileExtensionConstants.GIF + ";*" + FileExtensionConstants.PNG + ")";

        final IconEditor instance = (IconEditor) new IconEditorFactory().createEditor(
                mock(EditOperation.class),
                mock(DefaultGetter.class),
                mock(ValueValidator.class),
                "",
                mock(ConstellationIcon.class));

        final JFileChooser fileChooser = instance.getIconEditorFileChooser().createFileChooser();

        // Ensure file chooser is constructed correctly.
        assertEquals(fileChooser.getDialogTitle(), fileChooserTitle);
        assertEquals(fileChooser.getChoosableFileFilters().length, 1);
        assertEquals(fileChooser.getChoosableFileFilters()[0].getDescription(), fileChooserDescription);

        // If file is invalid and does not end with correct extension.
        final File file1 = File.createTempFile("fileInvalid", ".invalid");
        assertEquals(fileChooser.getChoosableFileFilters()[0].accept(file1), false);

        // If file does not exist.
        final File file2 = new File("/invalidPath/filePng" + FileExtensionConstants.PNG);
        assertEquals(fileChooser.getChoosableFileFilters()[0].accept(file2), false);

        // If file exists, is valid and ends with correct extension.
        final File file3 = File.createTempFile("filePng", FileExtensionConstants.PNG);
        assertEquals(fileChooser.getChoosableFileFilters()[0].accept(file3), true);

        // If file is a directory.
        final File fileMock = mock(File.class);
        doReturn("directory").when(fileMock).getName();
        doReturn(false).when(fileMock).isFile();
        doReturn(true).when(fileMock).isDirectory();
        assertEquals(fileChooser.accept(fileMock), true);

        Files.deleteIfExists(file1.toPath());
        Files.deleteIfExists(file3.toPath());
    }

    /**
     * Test of getIconEditorFolderChooser method, of inner class IconEditor, of
     * class IconEditorFactory.
     */
    @Test
    public void getIconEditorFolderChooser() {
        System.out.println("getIconEditorFolderChooser");

        final String fileChooserTitle = "Add New Icon(s)";

        final IconEditor instance = (IconEditor) new IconEditorFactory().createEditor(
                mock(EditOperation.class),
                mock(DefaultGetter.class),
                mock(ValueValidator.class),
                "",
                mock(ConstellationIcon.class));

        final JFileChooser fileChooser = instance.getIconEditorFolderChooser().createFileChooser();

        // Ensure file chooser is constructed correctly.
        assertEquals(fileChooser.getDialogTitle(), fileChooserTitle);
        assertEquals(fileChooser.getChoosableFileFilters().length, 0);

        // If file is a directory.
        final File fileMock = mock(File.class);
        doReturn("directory").when(fileMock).getName();
        doReturn(false).when(fileMock).isFile();
        doReturn(true).when(fileMock).isDirectory();
        assertEquals(fileChooser.accept(fileMock), true);
    }
}
