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
package au.gov.asd.tac.constellation.views.attributeeditor.editors;

import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.icon.AnalyticIconProvider;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory.AbstractEditor;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.IconEditorFactory.IconEditor;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for IconEditorFactory.
 *
 * @author sol695510
 * @author antares
 */
public class IconEditorFactoryNGTest {

    private static final Logger LOGGER = Logger.getLogger(IconEditorFactoryNGTest.class.getName());
    
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
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
        }
    }

    /**
     * Test of createEditor method, of class IconEditorFactory.
     */
    @Test
    public void testCreateEditor() {
        System.out.println("testCreateEditor");

        final IconEditorFactory instance = new IconEditorFactory();
        final AbstractEditor<ConstellationIcon> result = instance.createEditor("Test", null, null, null, null);
        // could be different abstract editors for the ConstellationIcon type but we want to make sure it's the right one
        assertTrue(result instanceof IconEditor);
    }
    
    /**
     * Test of updateControlsWithValue method, of class IconEditor.
     */
    @Test
    public void testUpdateControlsWithValue() {
        System.out.println("updateControlsWithValue");
        
        final IconEditorFactory instance = new IconEditorFactory();
        final IconEditor editor = instance.new IconEditor("Test", null, null, null, null);
        
        // need to run in order for editor controls to be instantiated
        editor.createEditorControls();
        
        // default values from instantiation
        assertNull(editor.getListSelection());
        
        editor.updateControlsWithValue(AnalyticIconProvider.GITHUB);
        
        assertEquals(editor.getListSelection(), "Internet.Github");
    }
    
    /**
     * Test of getValueFromControls method, of class IconEditor.
     */
    @Test
    public void testGetValueFromControls() {
        System.out.println("getValueFromControls");
        
        final IconEditorFactory instance = new IconEditorFactory();
        final IconEditor editor = instance.new IconEditor("Test", null, null, null, null);
        
        // need to run in order for editor controls to be instantiated
        editor.createEditorControls();
        
        final ConstellationIcon icon = AnalyticIconProvider.GITHUB;
        editor.updateControlsWithValue(icon);
        
        assertEquals(editor.getValueFromControls(), icon);
    }

    /**
     * Test of getIconEditorFileChooser method, of class IconEditor.
     *
     * @throws IOException
     */
    @Test
    public void getIconEditorFileChooser() throws IOException {
        System.out.println("getIconEditorFileChooser");

        final String fileChooserTitle = "Add New Icon(s)";
        final String fileChooserDescription = "Image Files (*" + FileExtensionConstants.JPG + ";*" + FileExtensionConstants.GIF + ";*" + FileExtensionConstants.PNG + ")";

        final IconEditorFactory instance = new IconEditorFactory();
        final IconEditor editor = instance.new IconEditor("Test", null, null, null, null);

        final JFileChooser fileChooser = editor.getIconEditorFileChooser().createFileChooser();

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
     * Test of getIconEditorFolderChooser method, of class IconEditor.
     */
    @Test
    public void getIconEditorFolderChooser() {
        System.out.println("getIconEditorFolderChooser");

        final String fileChooserTitle = "Add New Icon(s)";

        final IconEditorFactory instance = new IconEditorFactory();
        final IconEditor editor = instance.new IconEditor("Test", null, null, null, null);

        final JFileChooser fileChooser = editor.getIconEditorFolderChooser().createFileChooser();

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
