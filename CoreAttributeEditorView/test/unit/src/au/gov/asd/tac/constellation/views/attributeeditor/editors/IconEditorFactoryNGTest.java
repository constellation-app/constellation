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
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.IconEditorFactory.IconEditor;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.DefaultGetter;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import java.io.File;
import javax.swing.JFileChooser;
import org.mockito.Mockito;
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

        final AbstractEditorFactory.AbstractEditor<ConstellationIcon> instance = new IconEditorFactory().createEditor(
                mock(EditOperation.class),
                Mockito.mock(DefaultGetter.class),
                Mockito.mock(ValueValidator.class),
                "",
                Mockito.mock(ConstellationIcon.class));

        assertEquals(instance.getClass(), IconEditor.class);
    }

    /**
     * Test of getIconEditorFileChooser method, of inner class IconEditor, in
     * class IconEditorFactory.
     */
    @Test
    public void getIconEditorFileChooser() {
        System.out.println("getIconEditorFileChooser");

        final String fileChooserTitle = "Add New Icon(s)";

        final IconEditor instance = (IconEditor) new IconEditorFactory().createEditor(
                mock(EditOperation.class),
                Mockito.mock(DefaultGetter.class),
                Mockito.mock(ValueValidator.class),
                "",
                Mockito.mock(ConstellationIcon.class));

        final JFileChooser fileChooser = instance.getIconEditorFileChooser().createFileChooser();

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
