/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.testing;

import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser;
import au.gov.asd.tac.constellation.visual.opengl.utilities.SharedDrawable;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.swing.JFileChooser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import org.openide.filesystems.FileChooserBuilder;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for ExportGlyphTexturesAction.
 *
 * @author sol695510
 */
public class ExportGlyphTexturesActionNGTest {

    private static MockedStatic<FileChooser> fileChooserStaticMock;
    private static MockedStatic<SharedDrawable> sharedDrawableStaticMock;

    public ExportGlyphTexturesActionNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        fileChooserStaticMock = mockStatic(FileChooser.class);
        
        fileChooserStaticMock.when(()
                -> FileChooser.createFileChooserBuilderNoFilter(any(String.class)))
                .thenCallRealMethod();
        
        fileChooserStaticMock.when(()
                -> FileChooser.createFileChooserBuilder(any(String.class), any(String.class), any(String.class)))
                .thenCallRealMethod();
        
        sharedDrawableStaticMock = mockStatic(SharedDrawable.class);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        fileChooserStaticMock.close();
        sharedDrawableStaticMock.close();
    }

    /**
     * Test of actionPerformed method, of class ExportGlyphTexturesAction.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testActionPerformed() throws Exception {
        System.out.println("testActionPerformed");

        final ExportGlyphTexturesAction instance = new ExportGlyphTexturesAction();
        final ActionEvent e = null;
        final File file = new File("test.png");
        final Optional<File> optionalFile = Optional.ofNullable(file);

        fileChooserStaticMock.when(()
                -> FileChooser.openSaveDialog(any(FileChooserBuilder.class)))
                .thenReturn(CompletableFuture.completedFuture(optionalFile));

        instance.actionPerformed(e);

        sharedDrawableStaticMock.verify(()
                -> SharedDrawable.exportGlyphTextures(eq(file)), times(1));
    }

    /**
     * Test of getExportGlyphTexturesFileChooser method, of class ExportGlyphTexturesAction.
     *
     * @throws IOException
     */
    @Test
    public void testGetExportGlyphTexturesFileChooser() throws IOException {
        System.out.println("testGetExportGlyphTexturesFileChooser");

        final String fileChooserTitle = "Export Glyph Textures";
        final String fileChooserDescription = "Image Files (" + FileExtensionConstants.PNG + ")";

        final ExportGlyphTexturesAction instance = new ExportGlyphTexturesAction();
        final JFileChooser fileChooser = instance.getExportGlyphTexturesFileChooser().createFileChooser();

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
        assertEquals(fileChooser.getChoosableFileFilters()[0].accept(fileMock), true);

        Files.deleteIfExists(file1.toPath());
        Files.deleteIfExists(file3.toPath());
    }
}
