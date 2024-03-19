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
package au.gov.asd.tac.constellation.views.tableview.components;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.tableview.TableViewTopComponent;
import au.gov.asd.tac.constellation.views.tableview.api.ActiveTableReference;
import au.gov.asd.tac.constellation.views.tableview.api.UserTablePreferences;
import au.gov.asd.tac.constellation.views.tableview.components.ExportMenu.ExportMenuItemActionHandler;
import au.gov.asd.tac.constellation.views.tableview.panes.TablePane;
import au.gov.asd.tac.constellation.views.tableview.plugins.ExportToCsvFilePlugin;
import au.gov.asd.tac.constellation.views.tableview.plugins.ExportToExcelFilePlugin;
import au.gov.asd.tac.constellation.views.tableview.utilities.TableViewUtilities;
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
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.Pagination;
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
 *
 * @author formalhaunt
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class ExportMenuNGTest extends ConstellationTest {
    private static final Logger LOGGER = Logger.getLogger(ExportMenuNGTest.class.getName());

    private static final String GRAPH_ID = "graphId";

    private TableViewTopComponent tableViewTopComponent;
    private TablePane tablePane;
    private Table table;
    private ActiveTableReference activeTableReference;

    private ExportMenu exportMenu;

    public ExportMenuNGTest() {
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
        tableViewTopComponent = mock(TableViewTopComponent.class);
        tablePane = mock(TablePane.class);
        table = mock(Table.class);
        activeTableReference = mock(ActiveTableReference.class);

        when(tablePane.getTable()).thenReturn(table);
        when(tablePane.getParentComponent()).thenReturn(tableViewTopComponent);
        when(tablePane.getActiveTableReference()).thenReturn(activeTableReference);

        exportMenu = new ExportMenu(tablePane);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void allUIComponentsNullBeforeInit() {
        assertNull(exportMenu.getExportButton());
        assertNull(exportMenu.getExportCsvMenu());
        assertNull(exportMenu.getExportCsvSelectionMenu());
        assertNull(exportMenu.getExportExcelMenu());
        assertNull(exportMenu.getExportExcelSelectionMenu());
    }

    @Test
    public void createExportButtons() throws InterruptedException, PluginException, ExecutionException {
        exportMenu.init();

        assertNotNull(exportMenu.getExportButton());
        assertNotNull(exportMenu.getExportCsvMenu());
        assertNotNull(exportMenu.getExportCsvSelectionMenu());
        assertNotNull(exportMenu.getExportExcelMenu());
        assertNotNull(exportMenu.getExportExcelSelectionMenu());

        assertEquals(
                FXCollections.observableList(
                        List.of(
                                exportMenu.getExportCsvMenu(),
                                exportMenu.getExportCsvSelectionMenu(),
                                exportMenu.getExportExcelMenu(),
                                exportMenu.getExportExcelSelectionMenu()
                        )
                ),
                exportMenu.getExportButton().getItems()
        );

        final Graph graph = mock(Graph.class);
        when(tableViewTopComponent.getCurrentGraph()).thenReturn(graph);
        when(graph.getId()).thenReturn(GRAPH_ID);

        // Export Button
        final ImageView icon = (ImageView) exportMenu.getExportButton().getGraphic();
        assertTrue(isImageEqual(UserInterfaceIconProvider.UPLOAD.buildImage(16), icon.getImage()));
        assertEquals(120.0d, exportMenu.getExportButton().getMaxWidth());
        assertEquals(Side.RIGHT, exportMenu.getExportButton().getPopupSide());

        // Export Whole Table as CSV Menu Item
        assertEquals("Export to CSV", exportMenu.getExportCsvMenu().getText());
        verifyExportCSVAction(exportMenu.getExportCsvMenu().getOnAction(), false, false);
        verifyExportCSVAction(exportMenu.getExportCsvMenu().getOnAction(), true, false);

        reset(activeTableReference, table);

        // Export Selected Rows as CSV Menu Item
        assertEquals("Export to CSV (Selection)", exportMenu.getExportCsvSelectionMenu().getText());
        verifyExportCSVAction(exportMenu.getExportCsvSelectionMenu().getOnAction(), false, true);
        verifyExportCSVAction(exportMenu.getExportCsvSelectionMenu().getOnAction(), true, true);

        reset(activeTableReference, table);

        // Export Whole Table as Excel Menu Item
        assertEquals("Export to Excel", exportMenu.getExportExcelMenu().getText());
        verifyExportExcelAction(exportMenu.getExportExcelMenu().getOnAction(), false, false);
        verifyExportExcelAction(exportMenu.getExportExcelMenu().getOnAction(), true, false);

        reset(activeTableReference, table);

        // Export Selected Rows as Excel Menu Item
        assertEquals("Export to Excel (Selection)", exportMenu.getExportExcelSelectionMenu().getText());
        verifyExportExcelAction(exportMenu.getExportExcelSelectionMenu().getOnAction(), false, true);
        verifyExportExcelAction(exportMenu.getExportExcelSelectionMenu().getOnAction(), true, true);

        reset(activeTableReference, table);

        // The following verifies that the ExportMenuItemActionHandler wont run if
        // the current graph is null
        when(tableViewTopComponent.getCurrentGraph()).thenReturn(null);

        try (MockedStatic<TableViewUtilities> tableViewUtilsMockedStatic
                = Mockito.mockStatic(TableViewUtilities.class)) {
            final ActionEvent actionEvent = mock(ActionEvent.class);
            exportMenu.getExportCsvMenu().getOnAction().handle(actionEvent);
            tableViewUtilsMockedStatic.verifyNoInteractions();
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
     * Verify that the passed event handler exports to CSV either the whole
     * table or just the selected rows.
     *
     * @param eventHandler the handler to test
     * @param expectedCopyOnlySelectedRows true if only the selected rows are
     *     expected to be exported, false otherwise
     * @param userCancelsRequest true if the user is meant to cancel the export when
     *     picking a file in the file chooser
     */
    private void verifyExportCSVAction(final EventHandler<ActionEvent> eventHandler,
                                       final boolean userCancelsRequest,
                                       final boolean expectedExportOnlySelectedRows) throws InterruptedException, PluginException, ExecutionException {

        final ExportMenuItemActionHandler exportActionHandler = (ExportMenuItemActionHandler) eventHandler;

        final ExportMenuItemActionHandler spiedExportActionHandler = spy(exportActionHandler);

        final FileChooserBuilder exportFileChooser = mock(FileChooserBuilder.class);
        final File exportFile = userCancelsRequest ? null : new File("test.csv");
        final Optional<File> optionalExportFile = Optional.ofNullable(exportFile);

        doReturn(exportFileChooser).when(spiedExportActionHandler).getExportFileChooser();

        try (
                final MockedStatic<PluginExecution> pluginExecutionMockedStatic =
                        Mockito.mockStatic(PluginExecution.class);
                final MockedStatic<FileChooser> fileChooserMockedStatic = 
                        Mockito.mockStatic(FileChooser.class);
                final MockedStatic<Platform> platformMockedStatic =
                        Mockito.mockStatic(Platform.class);
        ) {            
            // This is added so that the mocked static that we would otherwise be 
            // trying to run in the fx thread is actually invoked properly
            platformMockedStatic.when(() -> Platform.runLater(any(Runnable.class)))
                    .thenAnswer(iom -> {
                        ((Runnable) iom.getArgument(0)).run();
                        return null;
                    });

            fileChooserMockedStatic.when(() -> FileChooser.openSaveDialog(exportFileChooser))
                    .thenReturn(CompletableFuture.completedFuture(optionalExportFile));
            
            final ActionEvent actionEvent = mock(ActionEvent.class);

            final Pagination pagination = mock(Pagination.class);
            final TableView<ObservableList<String>> tableView = mock(TableView.class);

            when(activeTableReference.getPagination()).thenReturn(pagination);
            when(table.getTableView()).thenReturn(tableView);

            final PluginExecution pluginExecution = mock(PluginExecution.class);
            pluginExecutionMockedStatic.when(() -> PluginExecution.withPlugin(any(Plugin.class)))
                    .thenAnswer(mockitoInvocation -> {
                        final ExportToCsvFilePlugin plugin
                                = (ExportToCsvFilePlugin) mockitoInvocation.getArgument(0);

                        if (exportFile != null) {
                            assertEquals(exportFile.getAbsolutePath(), plugin.getFile().getAbsolutePath());                           
                        } else {
                            assertEquals(exportFile, plugin.getFile());                                                      
                        }
                        assertEquals(pagination, plugin.getPagination());
                        assertEquals(tableView, plugin.getTable());
                        assertEquals(expectedExportOnlySelectedRows, plugin.isSelectedOnly());

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
     * Verify that the passed event handler exports to Excel either the whole
     * table or just the selected rows.
     *
     * @param eventHandler the handler to test
     * @param expectedCopyOnlySelectedRows true if only the selected rows are
     *     expected to be exported, false otherwise
     * @param userCancelsRequest true if the user is meant to cancel the export when
     *     picking a file in the file chooser
     */
    private void verifyExportExcelAction(final EventHandler<ActionEvent> eventHandler,
                                         final boolean userCancelsRequest,
                                         final boolean expectedExportOnlySelectedRows) throws InterruptedException, PluginException, ExecutionException {

        final ExportMenuItemActionHandler exportActionHandler = (ExportMenuItemActionHandler) eventHandler;

        final ExportMenuItemActionHandler spiedExportActionHandler = spy(exportActionHandler);

        final FileChooserBuilder exportFileChooser = mock(FileChooserBuilder.class);
        final File exportFile = userCancelsRequest ? null : new File("test.xlsx");
        final Optional<File> optionalExportFile = Optional.ofNullable(exportFile);

        doReturn(exportFileChooser).when(spiedExportActionHandler).getExportFileChooser();

        try (
                final MockedStatic<PluginExecution> pluginExecutionMockedStatic =
                        Mockito.mockStatic(PluginExecution.class);
                final MockedStatic<FileChooser> fileChooserMockedStatic = 
                        Mockito.mockStatic(FileChooser.class);
                final MockedStatic<Platform> platformMockedStatic =
                        Mockito.mockStatic(Platform.class);
        ) {
            // This is added so that the mocked static that we would otherwise be 
            // trying to run in the fx thread is actually invoked properly
            platformMockedStatic.when(() -> Platform.runLater(any(Runnable.class)))
                    .thenAnswer(iom -> {
                        ((Runnable) iom.getArgument(0)).run();
                        return null;
                    });
            
            fileChooserMockedStatic.when(() -> FileChooser.openSaveDialog(exportFileChooser))
                    .thenReturn(CompletableFuture.completedFuture(optionalExportFile));
            
            final ActionEvent actionEvent = mock(ActionEvent.class);

            final Pagination pagination = mock(Pagination.class);
            final TableView<ObservableList<String>> tableView = mock(TableView.class);

            final int maxRowsPerPage = 42;

            final UserTablePreferences userTablePreferences = new UserTablePreferences();
            userTablePreferences.setMaxRowsPerPage(maxRowsPerPage);

            when(activeTableReference.getPagination()).thenReturn(pagination);
            when(table.getTableView()).thenReturn(tableView);
            when(activeTableReference.getUserTablePreferences()).thenReturn(userTablePreferences);

            final PluginExecution pluginExecution = mock(PluginExecution.class);
            pluginExecutionMockedStatic.when(() -> PluginExecution.withPlugin(any(Plugin.class)))
                    .thenAnswer(mockitoInvocation -> {
                        final ExportToExcelFilePlugin plugin
                                = (ExportToExcelFilePlugin) mockitoInvocation.getArgument(0);

                        if (exportFile != null) {
                            assertEquals(exportFile.getAbsolutePath(), plugin.getFile().getAbsolutePath());                           
                        } else {
                            assertEquals(exportFile, plugin.getFile());                                                      
                        }
                        assertEquals(pagination, plugin.getPagination());
                        assertEquals(tableView, plugin.getTable());
                        assertEquals(42, plugin.getRowsPerPage());
                        assertEquals(GRAPH_ID, plugin.getSheetName());
                        assertEquals(expectedExportOnlySelectedRows, plugin.isSelectedOnly());

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
     * Verifies that two JavaFX images are equal. Unfortunately they don't
     * provide a nice way to do this so we check pixel by pixel.
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
