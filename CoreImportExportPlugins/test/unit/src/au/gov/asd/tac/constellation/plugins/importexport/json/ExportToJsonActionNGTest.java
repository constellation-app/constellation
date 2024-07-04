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
package au.gov.asd.tac.constellation.plugins.importexport.json;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.importexport.ImportExportPluginRegistry;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.swing.JFileChooser;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.openide.filesystems.FileChooserBuilder;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for ExportToJsonAction.
 *
 * @author sol695510
 */
public class ExportToJsonActionNGTest {

    private static MockedStatic<FileChooser> fileChooserStaticMock;
    private static MockedStatic<PluginExecution> pluginExecutionStaticMock;
    private static PluginExecution withPluginMock;
    private static PluginExecution withParameterMock;
    private static GraphNode contextMock;
    private static Graph graphMock;

    public ExportToJsonActionNGTest() {
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
                -> FileChooser.createFileChooserBuilder(any(String.class)))
                .thenCallRealMethod();
        
        fileChooserStaticMock.when(()
                -> FileChooser.createFileChooserBuilder(any(String.class), any(String.class)))
                .thenCallRealMethod();
        
        fileChooserStaticMock.when(()
                -> FileChooser.createFileChooserBuilder(any(String.class), any(String.class), any(String.class)))
                .thenCallRealMethod();
        
        fileChooserStaticMock.when(()
                -> FileChooser.createFileChooserBuilder(any(String.class), any(String.class), any(String.class), any(Boolean.class)))
                .thenCallRealMethod();

        pluginExecutionStaticMock = mockStatic(PluginExecution.class);
        withPluginMock = mock(PluginExecution.class);
        withParameterMock = mock(PluginExecution.class);
        contextMock = mock(GraphNode.class);
        graphMock = mock(Graph.class);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        fileChooserStaticMock.close();
        pluginExecutionStaticMock.close();
    }

    /**
     * Test of actionPerformed method, of class ExportToJsonAction.
     */
    @Test
    public void testActionPerformed() {
        System.out.println("testActionPerformed");

        final ExportToJsonAction instance = new ExportToJsonAction(contextMock);
        final ActionEvent e = null;

        pluginExecutionStaticMock.when(()
                -> PluginExecution.withPlugin(ImportExportPluginRegistry.EXPORT_JSON))
                .thenReturn(withPluginMock);

        doReturn(withParameterMock).when(withPluginMock).withParameter(any(String.class), any(String.class));

        doReturn(graphMock).when(contextMock).getGraph();

        // If the file ends with the correct file extension.
        final File file1 = new File("file1.json");
        final Optional<File> optionalFile = Optional.ofNullable(file1);

        fileChooserStaticMock.when(()
                -> FileChooser.openSaveDialog(any(FileChooserBuilder.class)))
                .thenReturn(CompletableFuture.completedFuture(optionalFile));

        instance.actionPerformed(e);

        // Check if the plugin executed correctly.
        verify(withPluginMock, times(1)).withParameter(ExportToJsonPlugin.FILE_NAME_PARAMETER_ID, file1.getAbsolutePath());
        verify(withParameterMock, times(1)).executeLater(graphMock);
        verify(contextMock, times(1)).getGraph();

        // If the file ends with the incorrect file extension.
        final File file2 = new File("file2");
        final Optional<File> optionalFile2 = Optional.ofNullable(file2);

        fileChooserStaticMock.when(()
                -> FileChooser.openSaveDialog(any(FileChooserBuilder.class)))
                .thenReturn(CompletableFuture.completedFuture(optionalFile2));

        instance.actionPerformed(e);

        // Check that the correct file extension was added.
        verify(withPluginMock, times(1)).withParameter(ExportToJsonPlugin.FILE_NAME_PARAMETER_ID, file2.getAbsolutePath() + FileExtensionConstants.JSON);
    }

    /**
     * Test of testGetExportToJSONFileChooser method, of class ExportToJsonAction.
     *
     * @throws IOException
     */
    @Test
    public void testGetExportToJSONFileChooser() throws IOException {
        System.out.println("testGetExportToJSONFileChooser");

        final String fileChooserTitle = "Export To JSON";
        final String fileChooserDescription = "JSON Files (" + FileExtensionConstants.JSON + ")";

        final ExportToJsonAction instance = new ExportToJsonAction(contextMock);
        final JFileChooser fileChooser = instance.getExportToJSONFileChooser().createFileChooser();

        // Ensure file chooser is constructed correctly.
        assertEquals(fileChooser.getDialogTitle(), fileChooserTitle);
        assertEquals(fileChooser.getChoosableFileFilters().length, 1);
        assertEquals(fileChooser.getChoosableFileFilters()[0].getDescription(), fileChooserDescription);

        // If file is invalid and does not end with correct extension.
        final File file1 = File.createTempFile("fileInvalid", ".invalid");
        assertEquals(fileChooser.getChoosableFileFilters()[0].accept(file1), false);

        // If file does not exist.
        final File file2 = new File("/invalidPath/fileJson" + FileExtensionConstants.JSON);
        assertEquals(fileChooser.getChoosableFileFilters()[0].accept(file2), false);

        // If file exists, is valid and ends with correct extension.
        final File file3 = File.createTempFile("fileJson", FileExtensionConstants.JSON);
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
