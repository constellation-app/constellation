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
package au.gov.asd.tac.constellation.graph.utilities.planes;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.utilities.planes.PlaneManagerTopComponent.ImportPlanePlugin;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.openide.filesystems.FileChooserBuilder;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for PlaneManagerTopComponent.
 *
 * @author sol695510
 */
public class PlaneManagerTopComponentNGTest {

    private static final Logger LOGGER = Logger.getLogger(PlaneManagerTopComponentNGTest.class.getName());

    private static MockedStatic<FileChooser> fileChooserStaticMock;
    private static MockedStatic<PluginExecution> pluginExecutionStaticMock;
    private static PluginExecution withPluginMock;

    public PlaneManagerTopComponentNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        fileChooserStaticMock = Mockito.mockStatic(FileChooser.class);
        pluginExecutionStaticMock = Mockito.mockStatic(PluginExecution.class);
        withPluginMock = Mockito.mock(PluginExecution.class);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        fileChooserStaticMock.close();
        pluginExecutionStaticMock.close();
    }

    /**
     * Test of importPlaneActionPerformed method, of class
     * PlaneManagerTopComponent.
     */
    @Test
    public void testImportPlaneActionPerformed() {
        System.out.println("testImportPlaneActionPerformed");

        final PlaneManagerTopComponent instance = new PlaneManagerTopComponent();
        final ActionEvent e = null;
        final Graph graph = null;

        pluginExecutionStaticMock.when(()
                -> PluginExecution.withPlugin(Mockito.any(ImportPlanePlugin.class)))
                .thenReturn(withPluginMock);

        final File file = new File("file1.png");
        final Optional<File> optionalFile = Optional.ofNullable(file);

        fileChooserStaticMock.when(()
                -> FileChooser.openOpenDialog(Mockito.any(FileChooserBuilder.class)))
                .thenReturn(CompletableFuture.completedFuture(optionalFile));

        instance.importPlaneActionPerformed(e);

        verify(withPluginMock, times(1)).executeLater(graph);
    }

    /**
     * Test of resultChanged method, of class PlaneManagerTopComponent.
     */
    @Test
    public void testResultChanged() {
        System.out.println("testResultChanged");
    }

    /**
     * Test of componentOpened method, of class PlaneManagerTopComponent.
     */
    @Test
    public void testComponentOpened() {
        System.out.println("testComponentOpened");
    }

    /**
     * Test of componentClosed method, of class PlaneManagerTopComponent.
     */
    @Test
    public void testComponentClosed() {
        System.out.println("testComponentClosed");
    }

    /**
     * Test of writeProperties method, of class PlaneManagerTopComponent.
     */
    @Test
    public void testWriteProperties() {
        System.out.println("testWriteProperties");
    }

    /**
     * Test of readProperties method, of class PlaneManagerTopComponent.
     */
    @Test
    public void testReadProperties() {
        System.out.println("testReadProperties");
    }

    /**
     * Test of graphChanged method, of class PlaneManagerTopComponent.
     */
    @Test
    public void testGraphChanged() {
        System.out.println("testGraphChanged");
    }

    /**
     * Test of getPlaneFileChooser method, of class PlaneManagerTopComponent.
     *
     * @throws IOException
     */
    @Test
    public void testGetPlaneFileChooser() throws IOException {
        System.out.println("testGetPlaneFileChooser");

        final String fileChooserTitle = "Import plane";
        final String fileChooserDescription = "Image Files ("
                + FileExtensionConstants.PNG + ", "
                + FileExtensionConstants.JPG + ")";

        final PlaneManagerTopComponent instance = new PlaneManagerTopComponent();
        final JFileChooser fileChooser = instance.getPlaneFileChooser().createFileChooser();

        // Ensure file chooser is constructed correctly.
        assertEquals(fileChooser.getDialogTitle(), fileChooserTitle);
        assertEquals(fileChooser.getChoosableFileFilters().length, 1);
        assertEquals(fileChooser.getChoosableFileFilters()[0].getDescription(), fileChooserDescription);

        // If file is invalid and does not end with correct extension.
        final File file1 = File.createTempFile("fileInvalid", ".invalid");
        assertEquals(fileChooser.getChoosableFileFilters()[0].accept(file1), false);

        // If files do not exist.
        final File file2 = new File("/invalidPath/filePng" + FileExtensionConstants.PNG);
        final File file3 = new File("/invalidPath/fileJpg" + FileExtensionConstants.JPG);
        assertEquals(fileChooser.getChoosableFileFilters()[0].accept(file2), false);
        assertEquals(fileChooser.getChoosableFileFilters()[0].accept(file3), false);

        // If files exist, are valid and end with correct extensions.
        final File file4 = File.createTempFile("filePng", FileExtensionConstants.PNG);
        final File file5 = File.createTempFile("fileJpg", FileExtensionConstants.JPG);
        assertEquals(fileChooser.getChoosableFileFilters()[0].accept(file4), true);
        assertEquals(fileChooser.getChoosableFileFilters()[0].accept(file5), true);

        // If file is a directory.
        final File fileMock = mock(File.class);
        doReturn("directory").when(fileMock).getName();
        doReturn(false).when(fileMock).isFile();
        doReturn(true).when(fileMock).isDirectory();
        assertEquals(fileChooser.getChoosableFileFilters()[0].accept(fileMock), true);

        Files.deleteIfExists(file1.toPath());
        Files.deleteIfExists(file4.toPath());
        Files.deleteIfExists(file5.toPath());
    }
}
