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
package au.gov.asd.tac.constellation.views.tableview2.components;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.tableview2.TableViewTopComponent;
import au.gov.asd.tac.constellation.views.tableview2.utils.TableViewUtilities;
import au.gov.asd.tac.constellation.views.tableview2.components.ExportMenu.ExportMenuItemActionHandler;
import au.gov.asd.tac.constellation.views.tableview2.plugins.ExportToCsvFilePlugin;
import au.gov.asd.tac.constellation.views.tableview2.plugins.ExportToExcelFilePlugin;
import au.gov.asd.tac.constellation.views.tableview2.api.ActiveTableReference;
import au.gov.asd.tac.constellation.views.tableview2.api.TablePreferences;
import java.io.File;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class ExportMenuNGTest {
    private static final String GRAPH_ID = "graphId";
    
    private TableViewTopComponent tableTopComponent;
    private TablePane tablePane;
    private Table table;
    private ActiveTableReference tableService;
    
    private ExportMenu exportMenu;
    
    public ExportMenuNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        FxToolkit.registerPrimaryStage();
        FxToolkit.showStage();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        FxToolkit.hideStage();
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        tableTopComponent = mock(TableViewTopComponent.class);
        tablePane = mock(TablePane.class);
        table = mock(Table.class);
        tableService = mock(ActiveTableReference.class);
        
        when(tablePane.getTable()).thenReturn(table);
        when(tablePane.getParentComponent()).thenReturn(tableTopComponent);
        when(tablePane.getActiveTableReference()).thenReturn(tableService);
        
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
    public void createExportButtons() throws InterruptedException, PluginException {
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
        when(tableTopComponent.getCurrentGraph()).thenReturn(graph);
        when(graph.getId()).thenReturn(GRAPH_ID);
        
        // Export Button
        final ImageView icon = (ImageView) exportMenu.getExportButton().getGraphic();
        assertTrue(isImageEqual(UserInterfaceIconProvider.UPLOAD.buildImage(16), icon.getImage()));
        assertEquals(120.0d, exportMenu.getExportButton().getMaxWidth());
        assertEquals(Side.RIGHT, exportMenu.getExportButton().getPopupSide());
        
        // Export Whole Table as CSV Menu Item
        assertEquals("Export to CSV", exportMenu.getExportCsvMenu().getText());
        verifyExportCSVAction(exportMenu.getExportCsvMenu().getOnAction(), false);
        
        reset(tableService, table);
        
        // Export Selected Rows as CSV Menu Item
        assertEquals("Export to CSV (Selection)", exportMenu.getExportCsvSelectionMenu().getText());
        verifyExportCSVAction(exportMenu.getExportCsvSelectionMenu().getOnAction(), true);
        
        reset(tableService, table);
        
        // Export Whole Table as Excel Menu Item
        assertEquals("Export to Excel", exportMenu.getExportExcelMenu().getText());
        verifyExportExcelAction(exportMenu.getExportExcelMenu().getOnAction(), false);
        
        reset(tableService, table);
        
        // Export Selected Rows as Excel Menu Item
        assertEquals("Export to Excel (Selection)", exportMenu.getExportExcelSelectionMenu().getText());
        verifyExportExcelAction(exportMenu.getExportExcelSelectionMenu().getOnAction(), true);
        
        reset(tableService, table);
        
        // The following verifies that the ExportMenuItemActionHandler wont run if
        // the current graph is null
        when(tableTopComponent.getCurrentGraph()).thenReturn(null);
        
        try (MockedStatic<TableViewUtilities> tableViewUtilsMockedStatic
                = Mockito.mockStatic(TableViewUtilities.class)) {
            final ActionEvent actionEvent = mock(ActionEvent.class);
            exportMenu.getExportCsvMenu().getOnAction().handle(actionEvent);
            tableViewUtilsMockedStatic.verifyNoInteractions();
            verify(actionEvent).consume();
        }
        
        
    }
    
    /**
     * Verify that the passed event handler exports to CSV either the whole
     * table or just the selected rows.
     *
     * @param eventHandler the handler to test
     * @param expectedCopyOnlySelectedRows true if only the selected rows are expected
     *     to be exported, false otherwise
     */
    private void verifyExportCSVAction(final EventHandler<ActionEvent> eventHandler,
                                       final boolean expectedExportOnlySelectedRows) throws InterruptedException, PluginException {
        
        final ExportMenuItemActionHandler exportActionHandler = (ExportMenuItemActionHandler) eventHandler;
        
        final ExportMenuItemActionHandler spiedExportActionHandler = spy(exportActionHandler);
        
        final ExportFileChooser exportFileChooser = mock(ExportFileChooser.class);
        final File exportFile = new File("test.csv");
        
        doReturn(exportFileChooser).when(spiedExportActionHandler).getExportFileChooser();
        doReturn(exportFile).when(exportFileChooser).openExportFileChooser();
        
        try (MockedStatic<PluginExecution> pluginExecutionMockedStatic
                = Mockito.mockStatic(PluginExecution.class)) {

            final ActionEvent actionEvent = mock(ActionEvent.class);
            
            final Pagination pagination = mock(Pagination.class);
            final TableView<ObservableList<String>> tableView = mock(TableView.class);
            
            when(tableService.getPagination()).thenReturn(pagination);
            when(table.getTableView()).thenReturn(tableView);
            
            final PluginExecution pluginExecution = mock(PluginExecution.class);
            pluginExecutionMockedStatic.when(() -> PluginExecution.withPlugin(any(Plugin.class)))
                    .thenAnswer(mockitoInvocation -> {
                        final ExportToCsvFilePlugin plugin
                                = (ExportToCsvFilePlugin) mockitoInvocation.getArgument(0);
                        
                        assertEquals(exportFile, plugin.getFile());
                        assertEquals(pagination, plugin.getPagination());
                        assertEquals(tableView, plugin.getTable());
                        assertEquals(expectedExportOnlySelectedRows, plugin.isSelectedOnly());
                        
                        return pluginExecution;
                    });
            
            
            
            spiedExportActionHandler.handle(actionEvent);
            
            verify(exportFileChooser).openExportFileChooser();
            verify(pluginExecution).executeNow((Graph) null);
            
            verify(actionEvent).consume();
        }
    }
    
    /**
     * Verify that the passed event handler exports to Excel either the whole
     * table or just the selected rows.
     *
     * @param eventHandler the handler to test
     * @param expectedCopyOnlySelectedRows true if only the selected rows are expected
     *     to be exported, false otherwise
     */
    private void verifyExportExcelAction(final EventHandler<ActionEvent> eventHandler,
                                         final boolean expectedExportOnlySelectedRows) throws InterruptedException, PluginException {
        
        final ExportMenuItemActionHandler exportActionHandler = (ExportMenuItemActionHandler) eventHandler;
        
        final ExportMenuItemActionHandler spiedExportActionHandler = spy(exportActionHandler);
        
        final ExportFileChooser exportFileChooser = mock(ExportFileChooser.class);
        final File exportFile = new File("test.xlsx");
        
        doReturn(exportFileChooser).when(spiedExportActionHandler).getExportFileChooser();
        doReturn(exportFile).when(exportFileChooser).openExportFileChooser();
        
        try (MockedStatic<PluginExecution> pluginExecutionMockedStatic
                = Mockito.mockStatic(PluginExecution.class)) {

            final ActionEvent actionEvent = mock(ActionEvent.class);
            
            final Pagination pagination = mock(Pagination.class);
            final TableView<ObservableList<String>> tableView = mock(TableView.class);
            
            final int maxRowsPerPage = 42;
            
            final TablePreferences tablePreferences = new TablePreferences();
            tablePreferences.setMaxRowsPerPage(maxRowsPerPage);
            
            when(tableService.getPagination()).thenReturn(pagination);
            when(table.getTableView()).thenReturn(tableView);
            when(tableService.getTablePreferences()).thenReturn(tablePreferences);
            
            final PluginExecution pluginExecution = mock(PluginExecution.class);
            pluginExecutionMockedStatic.when(() -> PluginExecution.withPlugin(any(Plugin.class)))
                    .thenAnswer(mockitoInvocation -> {
                        final ExportToExcelFilePlugin plugin
                                = (ExportToExcelFilePlugin) mockitoInvocation.getArgument(0);
                        
                        assertEquals(exportFile, plugin.getFile());
                        assertEquals(pagination, plugin.getPagination());
                        assertEquals(tableView, plugin.getTable());
                        assertEquals(42, plugin.getRowsPerPage());
                        assertEquals(GRAPH_ID, plugin.getSheetName());
                        assertEquals(expectedExportOnlySelectedRows, plugin.isSelectedOnly());
                        
                        return pluginExecution;
                    });
            
            spiedExportActionHandler.handle(actionEvent);
            
            verify(exportFileChooser).openExportFileChooser();
            verify(pluginExecution).executeNow((Graph) null);
            
            verify(actionEvent).consume();
        }
    }
    
    /**
     * Verifies that two JavaFX images are equal. Unfortunately they don't provide
     * a nice way to do this so we check pixel by pixel.
     *
     * @param firstImage the first image to compare
     * @param secondImage the second image to compare
     * @return true if the images are the same, false otherwise
     */
    private static boolean isImageEqual(Image firstImage, Image secondImage) {
        // Prevent `NullPointerException`
        if(firstImage != null && secondImage == null) {
            return false;
        }
        
        if(firstImage == null) {
            return secondImage == null;
        }

        // Compare images size
        if(firstImage.getWidth() != secondImage.getWidth()) {
            return false;
        }
        
        if(firstImage.getHeight() != secondImage.getHeight()) {
            return false;
        }

        // Compare images color
        for(int x = 0; x < firstImage.getWidth(); x++){
            for(int y = 0; y < firstImage.getHeight(); y++){
                int firstArgb = firstImage.getPixelReader().getArgb(x, y);
                int secondArgb = secondImage.getPixelReader().getArgb(x, y);

                if(firstArgb != secondArgb) return false;
            }
        }

        return true;
    }
}
