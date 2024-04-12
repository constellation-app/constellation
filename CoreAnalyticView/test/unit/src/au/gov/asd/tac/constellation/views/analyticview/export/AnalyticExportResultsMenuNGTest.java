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
package au.gov.asd.tac.constellation.views.analyticview.export;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.analyticview.AnalyticViewTopComponent;
import au.gov.asd.tac.constellation.views.analyticview.export.AnalyticExportResultsMenu.ExportMenuItemActionHandler;
import au.gov.asd.tac.constellation.views.analyticview.utilities.AnalyticExportUtilities;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javax.swing.JFileChooser;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.openide.filesystems.FileChooserBuilder;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for AnalyticExportResultsMenu
 *
 * @author Delphinus8821
 */
public class AnalyticExportResultsMenuNGTest {

    private static final Logger LOGGER = Logger.getLogger(AnalyticExportResultsMenuNGTest.class.getName());

    private static final String GRAPH_ID = "graphId";

    private AnalyticExportResultsMenu exportMenu;
    private TableView table;
    private AnalyticViewTopComponent topComponent;
    private final Graph graph = mock(Graph.class);
    private final GraphManager graphManager = spy(GraphManager.class);

    public AnalyticExportResultsMenuNGTest() {
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
        topComponent = mock(AnalyticViewTopComponent.class);
        table = mock(TableView.class);

        exportMenu = new AnalyticExportResultsMenu(table);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void allUIComponentsNullBeforeInit() {
        assertNull(exportMenu.getExportButton());
        assertNull(exportMenu.getExportCsvMenu());
        assertNull(exportMenu.getExportExcelMenu());
    }

    @Test
    public void createExportButtons() throws InterruptedException, PluginException, ExecutionException {
        exportMenu.init();

        assertNotNull(exportMenu.getExportButton());
        assertNotNull(exportMenu.getExportCsvMenu());
        assertNotNull(exportMenu.getExportExcelMenu());

        assertEquals(FXCollections.observableList(List.of(exportMenu.getExportCsvMenu(), exportMenu.getExportExcelMenu())),
                exportMenu.getExportButton().getItems()
        );


        when(topComponent.getCurrentGraph()).thenReturn(graph);
        when(graph.getId()).thenReturn(GRAPH_ID);

        // Export Button
        final ImageView icon = (ImageView) exportMenu.getExportButton().getGraphic();
        assertTrue(isImageEqual(UserInterfaceIconProvider.UPLOAD.buildImage(16), icon.getImage()));
        assertEquals(200.0, exportMenu.getExportButton().getMaxWidth());
        assertEquals(Side.RIGHT, exportMenu.getExportButton().getPopupSide());

        // Export Whole Table as CSV Menu Item
        assertEquals("Export to CSV", exportMenu.getExportCsvMenu().getText());
        verifyExportCSVAction(exportMenu.getExportCsvMenu().getOnAction(), false);
        verifyExportCSVAction(exportMenu.getExportCsvMenu().getOnAction(), true);

        reset(table);

        // Export Whole Table as Excel Menu Item
        assertEquals("Export to Excel", exportMenu.getExportExcelMenu().getText());
        verifyExportExcelAction(exportMenu.getExportExcelMenu().getOnAction(), false);
        verifyExportExcelAction(exportMenu.getExportExcelMenu().getOnAction(), true);

        // The following verifies that the ExportMenuItemActionHandler wont run if
        // the current graph is null
        when(topComponent.getCurrentGraph()).thenReturn(null);

        try (MockedStatic<AnalyticExportUtilities> analyticViewUtilsMockedStatic = Mockito.mockStatic(AnalyticExportUtilities.class)) {
            final ActionEvent actionEvent = mock(ActionEvent.class);
            exportMenu.getExportCsvMenu().getOnAction().handle(actionEvent);
            analyticViewUtilsMockedStatic.verifyNoInteractions();
            verify(actionEvent).consume();
        }
    }

    @Test
    public void exportFileChooser() throws IOException {
        final String fileChooserTitle = "File Chooser Title";
        final String expectedFileExtension = ".json";
        final String fileChooserDescription = "File Filter Description";

        ExportMenuItemActionHandler handler = exportMenu.new ExportMenuItemActionHandler(fileChooserTitle, expectedFileExtension, fileChooserDescription, null);

        final JFileChooser fileChooser = handler.getExportFileChooser().createFileChooser();

        assertEquals(fileChooser.getDialogTitle(), fileChooserTitle);
        assertEquals(fileChooser.getChoosableFileFilters().length, 2);
        assertEquals(fileChooser.getChoosableFileFilters()[0].getDescription(), "All Files");
        assertEquals(fileChooser.getChoosableFileFilters()[1].getDescription(), fileChooserDescription);

        // File does not end with correct extension
        final File tmpFileInvalid = File.createTempFile("test", ".random");
        assertEquals(fileChooser.getChoosableFileFilters()[1].accept(tmpFileInvalid), false);

        // File does not exist
        assertEquals(fileChooser.getChoosableFileFilters()[1].accept(new File("/tmp/test" + expectedFileExtension)), false);

        // File is valid. Exists and ends with correct extension
        final File tmpFileValid = File.createTempFile("test", expectedFileExtension);
        assertEquals(fileChooser.getChoosableFileFilters()[1].accept(tmpFileValid), true);

        Files.deleteIfExists(tmpFileInvalid.toPath());
        Files.deleteIfExists(tmpFileValid.toPath());
    }

    /**
     * Verify that the passed event handler exports to CSV either the whole table or just the selected rows.
     *
     * @param eventHandler the handler to test
     * @param userCancelsRequest true if the user is meant to cancel the export when picking a file in the file chooser
     */
    private void verifyExportCSVAction(final EventHandler<ActionEvent> eventHandler, final boolean userCancelsRequest)
            throws InterruptedException, PluginException, ExecutionException {

        final ExportMenuItemActionHandler exportActionHandler = (ExportMenuItemActionHandler) eventHandler;
        final ExportMenuItemActionHandler spiedExportActionHandler = spy(exportActionHandler);

        final FileChooserBuilder exportFileChooser = mock(FileChooserBuilder.class);
        final File exportFile = userCancelsRequest ? null : new File("test.csv");
        final Optional<File> optionalExportFile = Optional.ofNullable(exportFile);

        doReturn(exportFileChooser).when(spiedExportActionHandler).getExportFileChooser();

        try (final MockedStatic<PluginExecution> pluginExecutionMockedStatic = Mockito.mockStatic(PluginExecution.class);
                final MockedStatic<FileChooser> fileChooserMockedStatic = Mockito.mockStatic(FileChooser.class);
                final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class);
                final MockedStatic<GraphManager> graphManagerMockedStatic = Mockito.mockStatic(GraphManager.class);) {

            graphManagerMockedStatic.when(GraphManager::getDefault).thenReturn(graphManager);
            when(graphManager.getActiveGraph()).thenReturn(graph);
            when(graph.getId()).thenReturn(GRAPH_ID);
            
            // This is added so that the mocked static that we would otherwise be
            // trying to run in the fx thread is actually invoked properly
            platformMockedStatic.when(() -> Platform.runLater(any(Runnable.class))).thenAnswer(iom -> {
                ((Runnable) iom.getArgument(0)).run();
                return null;
            });

            fileChooserMockedStatic.when(() -> FileChooser.openSaveDialog(exportFileChooser))
                    .thenReturn(CompletableFuture.completedFuture(optionalExportFile));

            final ActionEvent actionEvent = mock(ActionEvent.class);

            final PluginExecution pluginExecution = mock(PluginExecution.class);
            pluginExecutionMockedStatic.when(() -> PluginExecution.withPlugin(any(Plugin.class))).thenAnswer(mockitoInvocation -> {
                final AnalyticExportToCsvFilePlugin plugin = (AnalyticExportToCsvFilePlugin) mockitoInvocation.getArgument(0);

                if (exportFile != null) {
                    assertEquals(exportFile.getAbsolutePath(), plugin.getFile().getAbsolutePath());
                } else {
                    assertEquals(exportFile, plugin.getFile());
                }
                assertEquals(table, plugin.getTable());

                return pluginExecution;
            });

            spiedExportActionHandler.handle(actionEvent);

            // Wait for the export job to complete
            spiedExportActionHandler.getLastExport().get();

            fileChooserMockedStatic.verify(() -> FileChooser.openSaveDialog(exportFileChooser));

            if (userCancelsRequest) {
                verifyNoInteractions(pluginExecution);
            } else {
                verify(pluginExecution).executeNow((Graph) null);
            }

            verify(actionEvent).consume();
        }
    }

    /**
     * Verify that the passed event handler exports to Excel either the whole table or just the selected rows.
     *
     * @param eventHandler the handler to test
     * @param userCancelsRequest true if the user is meant to cancel the export when picking a file in the file chooser
     */
    private void verifyExportExcelAction(final EventHandler<ActionEvent> eventHandler, final boolean userCancelsRequest)
            throws InterruptedException, PluginException, ExecutionException {

        final ExportMenuItemActionHandler exportActionHandler = (ExportMenuItemActionHandler) eventHandler;
        final ExportMenuItemActionHandler spiedExportActionHandler = spy(exportActionHandler);

        final FileChooserBuilder exportFileChooser = mock(FileChooserBuilder.class);
        final File exportFile = userCancelsRequest ? null : new File("test.xlsx");
        final Optional<File> optionalExportFile = Optional.ofNullable(exportFile);

        doReturn(exportFileChooser).when(spiedExportActionHandler).getExportFileChooser();

        try (final MockedStatic<PluginExecution> pluginExecutionMockedStatic = Mockito.mockStatic(PluginExecution.class);
                final MockedStatic<FileChooser> fileChooserMockedStatic = Mockito.mockStatic(FileChooser.class);
                final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class);
                final MockedStatic<GraphManager> graphManagerMockedStatic = Mockito.mockStatic(GraphManager.class);) {

            graphManagerMockedStatic.when(GraphManager::getDefault).thenReturn(graphManager);
            when(graphManager.getActiveGraph()).thenReturn(graph);
            when(graph.getId()).thenReturn(GRAPH_ID);
            
            // This is added so that the mocked static that we would otherwise be
            // trying to run in the fx thread is actually invoked properly
            platformMockedStatic.when(() -> Platform.runLater(any(Runnable.class))).thenAnswer(iom -> {
                ((Runnable) iom.getArgument(0)).run();
                return null;
            });

            fileChooserMockedStatic.when(() -> FileChooser.openSaveDialog(exportFileChooser))
                    .thenReturn(CompletableFuture.completedFuture(optionalExportFile));

            final ActionEvent actionEvent = mock(ActionEvent.class);

            final PluginExecution pluginExecution = mock(PluginExecution.class);
            pluginExecutionMockedStatic.when(() -> PluginExecution.withPlugin(any(Plugin.class))).thenAnswer(mockitoInvocation -> {
                final AnalyticExportToExcelFilePlugin plugin = (AnalyticExportToExcelFilePlugin) mockitoInvocation.getArgument(0);

                if (exportFile != null) {
                    assertEquals(exportFile.getAbsolutePath(), plugin.getFile().getAbsolutePath());
                } else {
                    assertEquals(exportFile, plugin.getFile());
                }
                final String expectedSheetName = "Export";
                assertEquals(table, plugin.getTable());
                assertEquals(expectedSheetName, plugin.getSheetName());

                return pluginExecution;
            });     

            spiedExportActionHandler.handle(actionEvent);

            // Wait for the export job to complete
            spiedExportActionHandler.getLastExport().get();

            fileChooserMockedStatic.verify(() -> FileChooser.openSaveDialog(exportFileChooser));

            if (userCancelsRequest) {
                verifyNoInteractions(pluginExecution);
            } else {
                verify(pluginExecution).executeNow((Graph) null);
            }

            verify(actionEvent).consume();
        }
    }

    /**
     * Verifies that two JavaFX images are equal. Unfortunately they don't provide a nice way to do this so we check pixel by pixel.
     *
     * @param firstImage the first image to compare
     * @param secondImage the second image to compare
     * @return true if the images are the same, false otherwise
     */
    private static boolean isImageEqual(Image firstImage, Image secondImage) {
        // Prevent `NullPointerException`
        if (firstImage != null && secondImage == null) {
            return false;
        }

        if (firstImage == null) {
            return secondImage == null;
        }

        // Compare images size
        if (firstImage.getWidth() != secondImage.getWidth()) {
            return false;
        }

        if (firstImage.getHeight() != secondImage.getHeight()) {
            return false;
        }

        // Compare images color
        for (int x = 0; x < firstImage.getWidth(); x++) {
            for (int y = 0; y < firstImage.getHeight(); y++) {
                int firstArgb = firstImage.getPixelReader().getArgb(x, y);
                int secondArgb = secondImage.getPixelReader().getArgb(x, y);

                if (firstArgb != secondArgb) {
                    return false;
                }
            }
        }

        return true;
    }
}
