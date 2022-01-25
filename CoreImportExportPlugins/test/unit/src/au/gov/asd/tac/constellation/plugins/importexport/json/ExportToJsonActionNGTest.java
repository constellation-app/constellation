/*
 * Copyright 2010-2021 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.importexport.ImportExportPluginRegistry;
import au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import org.openide.filesystems.FileChooserBuilder;
import org.testfx.api.FxToolkit;
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

    private static final Logger LOGGER = Logger.getLogger(ExportToJsonActionNGTest.class.getName());

    private static MockedStatic<FileChooser> fileChooserStaticMock;
    private static MockedStatic<PluginExecution> pluginExecutionStaticMock;
    private static GraphNode context;

    public ExportToJsonActionNGTest() {
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
        context = Mockito.mock(GraphNode.class);
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

        final ExportToJsonAction instance = new ExportToJsonAction(context);
        final ActionEvent e = null;

        final String title = "Export to JSON";
        final File savedDirectory = FileChooser.DEFAULT_DIRECTORY;
        final FileNameExtensionFilter filter = FileChooser.JSON_FILE_FILTER;

        final File file = new File("test.json");
        final Optional<File> optionalFile = Optional.ofNullable(file);

        fileChooserStaticMock.when(()
                -> FileChooser.getBaseFileChooserBuilder(
                        title,
                        savedDirectory,
                        filter))
                .thenCallRealMethod();

        fileChooserStaticMock.when(()
                -> FileChooser.openSaveDialog(Mockito.any(FileChooserBuilder.class)))
                .thenReturn(CompletableFuture.completedFuture(optionalFile));

        instance.actionPerformed(e);

        pluginExecutionStaticMock.verify(()
                -> PluginExecution.withPlugin(ImportExportPluginRegistry.EXPORT_JSON), times(1));
    }
}
