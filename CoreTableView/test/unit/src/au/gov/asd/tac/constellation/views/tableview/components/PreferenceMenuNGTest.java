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

import au.gov.asd.tac.constellation.views.tableview.panes.TablePane;
import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.tableview.TableViewTopComponent;
import au.gov.asd.tac.constellation.views.tableview.api.ActiveTableReference;
import au.gov.asd.tac.constellation.views.tableview.api.Column;
import au.gov.asd.tac.constellation.views.tableview.api.UpdateMethod;
import au.gov.asd.tac.constellation.views.tableview.api.UserTablePreferences;
import au.gov.asd.tac.constellation.views.tableview.io.TableViewPreferencesIoProvider;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewState;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Side;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Pagination;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.commons.lang3.tuple.ImmutablePair;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;
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
 public class PreferenceMenuNGTest extends ConstellationTest {
    private static final Logger LOGGER = Logger.getLogger(PreferenceMenuNGTest.class.getName());

    private TableViewTopComponent tableViewTopComponent;
    private TablePane tablePane;
    private Table table;
    private ActiveTableReference activeTableReference;

    private PreferencesMenu preferencesMenu;

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
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
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

        preferencesMenu = spy(new PreferencesMenu(tablePane));
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void allUIComponentsNullBeforeInit() {
        assertNull(preferencesMenu.getPreferencesButton());
        assertNull(preferencesMenu.getSavePreferencesMenu());
        assertNull(preferencesMenu.getLoadPreferencesMenu());
        assertNull(preferencesMenu.getPageSizeMenu());
    }

    @Test
    public void createExportButtons() throws InterruptedException {
        preferencesMenu.init();

        assertNotNull(preferencesMenu.getPreferencesButton());
        assertNotNull(preferencesMenu.getSavePreferencesMenu());
        assertNotNull(preferencesMenu.getLoadPreferencesMenu());
        assertNotNull(preferencesMenu.getPageSizeMenu());

        assertEquals(
                FXCollections.observableList(
                        List.of(
                                preferencesMenu.getPageSizeMenu(),
                                preferencesMenu.getSavePreferencesMenu(),
                                preferencesMenu.getLoadPreferencesMenu()
                        )
                ),
                preferencesMenu.getPreferencesButton().getItems()
        );

        // Preferences Button
        final ImageView icon = (ImageView) preferencesMenu.getPreferencesButton().getGraphic();
        assertTrue(isImageEqual(UserInterfaceIconProvider.SETTINGS.buildImage(16), icon.getImage()));
        assertEquals(120.0d, preferencesMenu.getPreferencesButton().getMaxWidth());
        assertEquals(Side.RIGHT, preferencesMenu.getPreferencesButton().getPopupSide());

        // Page Size Menu
        assertEquals("Set Page Size", preferencesMenu.getPageSizeMenu().getText());
        assertEquals(4, preferencesMenu.getPageSizeMenu().getItems().size());

        Map.of(
                0, "100",
                1, "250",
                2, "500",
                3, "1000"
        ).entrySet().stream()
                .forEach(entry -> {
                    verifyPageSizeRadioButton(
                            ((RadioMenuItem) preferencesMenu.getPageSizeMenu().getItems().get(entry.getKey())),
                            entry.getValue()
                    );
                });

        // Save Preferences
        assertEquals("Save Table Preferences", preferencesMenu.getSavePreferencesMenu().getText());

        verifySavePreferencesAction(preferencesMenu.getSavePreferencesMenu(), false, false, true);
        verifySavePreferencesAction(preferencesMenu.getSavePreferencesMenu(), false, true, false);
        verifySavePreferencesAction(preferencesMenu.getSavePreferencesMenu(), true, false, false);

        // Load Preferences
        assertEquals("Load Table Preferences...", preferencesMenu.getLoadPreferencesMenu().getText());

        verifyLoadPreferencesAction(preferencesMenu.getLoadPreferencesMenu(), true);
        verifyLoadPreferencesAction(preferencesMenu.getLoadPreferencesMenu(), false);
    }

    @Test
    public void loadPreferencesCurrentStateNull() {
        try (
                final MockedStatic<TableViewPreferencesIoProvider> tablePrefIOUtilsMockedStatic
                = Mockito.mockStatic(TableViewPreferencesIoProvider.class);) {
            preferencesMenu.loadPreferences();

            tablePrefIOUtilsMockedStatic.verifyNoInteractions();
        }
    }

    @Test
    public void loadPreferencesColumnOrderIsEmpty() {
        try (
                final MockedStatic<TableViewPreferencesIoProvider> tablePrefIOUtilsMockedStatic
                = Mockito.mockStatic(TableViewPreferencesIoProvider.class);) {
            final TableViewState tableViewState = new TableViewState();
            tableViewState.setElementType(GraphElementType.VERTEX);

            when(tableViewTopComponent.getCurrentState()).thenReturn(tableViewState);

            final UserTablePreferences currentTablePreferences = new UserTablePreferences();
            currentTablePreferences.setMaxRowsPerPage(42);

            when(activeTableReference.getUserTablePreferences()).thenReturn(currentTablePreferences);

            final UserTablePreferences loadedTablePreferences = new UserTablePreferences();
            loadedTablePreferences.setColumnOrder(Collections.emptyList());

            tablePrefIOUtilsMockedStatic.when(() -> TableViewPreferencesIoProvider
                    .getPreferences(GraphElementType.VERTEX)).thenReturn(loadedTablePreferences);

            preferencesMenu.loadPreferences();

            verify(activeTableReference, times(0)).saveSortDetails(anyString(), any(TableColumn.SortType.class));
            verify(activeTableReference, times(0)).updateVisibleColumns(any(Graph.class), any(TableViewState.class),
                    any(List.class), any(UpdateMethod.class));
        }
    }

    @Test
    public void loadPreferences() {
        try (
                final MockedStatic<TableViewPreferencesIoProvider> tablePrefIOUtilsMockedStatic
                = Mockito.mockStatic(TableViewPreferencesIoProvider.class);) {
            final TableViewState tableViewState = new TableViewState();
            tableViewState.setElementType(GraphElementType.VERTEX);

            final Graph graph = mock(Graph.class);

            when(tableViewTopComponent.getCurrentState()).thenReturn(tableViewState);
            when(tableViewTopComponent.getCurrentGraph()).thenReturn(graph);

            // These are the existing table userTablePreferences
            final UserTablePreferences currentTablePreferences = new UserTablePreferences();
            currentTablePreferences.setMaxRowsPerPage(42);

            // There are 4 columns in the table. Set them up and all required variables
            // that describe them
            when(activeTableReference.getUserTablePreferences()).thenReturn(currentTablePreferences);

            final TableView<ObservableList<String>> tableView = mock(TableView.class);

            when(table.getTableView()).thenReturn(tableView);

            final TableColumn<ObservableList<String>, String> column1 = mock(TableColumn.class);
            final TableColumn<ObservableList<String>, String> column2 = mock(TableColumn.class);
            final TableColumn<ObservableList<String>, String> column3 = mock(TableColumn.class);
            final TableColumn<ObservableList<String>, String> column4 = mock(TableColumn.class);

            when(column1.getText()).thenReturn("Column1");
            when(column2.getText()).thenReturn("Column2");
            when(column3.getText()).thenReturn("Column3");
            when(column4.getText()).thenReturn("Column4");

            when(tableView.getColumns()).thenReturn(
                    FXCollections.observableList(List.of(column1, column2, column3, column4)));

            final Attribute attribute1 = mock(Attribute.class);
            final Attribute attribute2 = mock(Attribute.class);
            final Attribute attribute3 = mock(Attribute.class);
            final Attribute attribute4 = mock(Attribute.class);

            final CopyOnWriteArrayList<Column> columnIndex = new CopyOnWriteArrayList<>();
            columnIndex.add(new Column("type1", attribute1, column1));
            columnIndex.add(new Column("type2", attribute2, column2));
            columnIndex.add(new Column("type3", attribute3, column3));
            columnIndex.add(new Column("type4", attribute4, column4));

            when(table.getColumnIndex()).thenReturn(columnIndex);

            // The loaded userTablePreferences specifies 3 of the 4 columns in the table
            final UserTablePreferences loadedTablePreferences = new UserTablePreferences();
            loadedTablePreferences.setColumnOrder(List.of("Column1", "Column2", "Column4"));
            loadedTablePreferences.setSortByColumn(ImmutablePair.of("Column2", TableColumn.SortType.DESCENDING));
            loadedTablePreferences.setMaxRowsPerPage(150);

            when(activeTableReference.updateVisibleColumns(
                    any(Graph.class),
                    any(TableViewState.class),
                    any(List.class),
                    eq(UpdateMethod.REPLACE)
            )).thenReturn(CompletableFuture.completedFuture(null));

            // Create page size option menu items, with one matching the pages size
            // in the loaded prefs above
            final ToggleGroup toggleGroup = mock(ToggleGroup.class);

            final RadioMenuItem pageSizeOption1 = mock(RadioMenuItem.class);
            final RadioMenuItem pageSizeOption2 = mock(RadioMenuItem.class);

            when(pageSizeOption1.getText()).thenReturn("100");
            when(pageSizeOption2.getText()).thenReturn("150");

            when(preferencesMenu.getPageSizeToggle()).thenReturn(toggleGroup);
            when(toggleGroup.getToggles()).thenReturn(
                    FXCollections.observableList(List.of(pageSizeOption1, pageSizeOption2)));

            // Return the loaded userTablePreferences when the load call is made
            tablePrefIOUtilsMockedStatic.when(() -> TableViewPreferencesIoProvider
                    .getPreferences(GraphElementType.VERTEX)).thenReturn(loadedTablePreferences);

            // Start the test
            preferencesMenu.loadPreferences();

            verify(column1).setVisible(true);
            verify(column2).setVisible(true);
            verify(column3, times(0)).setVisible(anyBoolean());
            verify(column4).setVisible(true);

            verify(activeTableReference).saveSortDetails("Column2", TableColumn.SortType.DESCENDING);
            verify(activeTableReference).updateVisibleColumns(
                    same(graph),
                    same(tableViewState),
                    eq(List.of(
                            Tuple.create("type1", attribute1),
                            Tuple.create("type2", attribute2),
                            Tuple.create("type4", attribute4)
                    )),
                    eq(UpdateMethod.REPLACE));

            verify(pageSizeOption1, times(0)).setSelected(true);
            verify(pageSizeOption2, times(1)).setSelected(true);
            assertEquals(150, currentTablePreferences.getMaxRowsPerPage());
        }
    }

    /**
     * Verifies that when the load preferences button is clicked, the load
     * preferences method is called and the tables pagination is updated with
     * any necessary changes. If the current active graph is null, then no
     * preferences will be loaded.
     *
     * @param loadPreferencesMenu the load userTablePreferences menu
     * @param isActiveGraphNull true if the active graph is null, false
     * otherwise
     * @throws InterruptedException if there is a an issue waiting for the
     * JavaFX thread work to complete
     */
    private void verifyLoadPreferencesAction(final MenuItem loadPreferencesMenu,
            final boolean isActiveGraphNull) throws InterruptedException {
        clearInvocations(preferencesMenu, activeTableReference, tablePane);

        try (
                final MockedStatic<GraphManager> graphManagerMockedStatic
                = Mockito.mockStatic(GraphManager.class);) {

            final GraphManager graphManager = mock(GraphManager.class);
            graphManagerMockedStatic.when(GraphManager::getDefault).thenReturn(graphManager);

            final ActionEvent actionEvent = mock(ActionEvent.class);

            if (isActiveGraphNull) {
                when(graphManager.getActiveGraph()).thenReturn(null);

                loadPreferencesMenu.getOnAction().handle(actionEvent);

                verify(preferencesMenu, times(0)).loadPreferences();
            } else {
                final UserTablePreferences userTablePreferences = new UserTablePreferences();
                userTablePreferences.setMaxRowsPerPage(42);

                final Graph graph = mock(Graph.class);
                final Pagination pagination = mock(Pagination.class);

                when(graphManager.getActiveGraph()).thenReturn(graph);

                when(activeTableReference.getUserTablePreferences()).thenReturn(userTablePreferences);
                when(activeTableReference.getPagination()).thenReturn(pagination);

                doNothing().when(preferencesMenu).loadPreferences();

                loadPreferencesMenu.getOnAction().handle(actionEvent);

                final CountDownLatch latch = new CountDownLatch(1);
                Platform.runLater(() -> latch.countDown());
                latch.await();

                verify(activeTableReference).updatePagination(42, tablePane);
                verify(preferencesMenu).loadPreferences();
            }
            verify(actionEvent).consume();
        }
    }

    /**
     * Verifies that when the save userTablePreferences button is pressed then
     * it will call out to
     * {@link TableViewPreferencesIoProvider#savePreferences(GraphElementType, TableView, int)}.
     * If certain values are not set, then it will not save the preferences and
     * just return.
     * <p/>
     * There is a lot of mock setup for this so the code tries to re-use as much
     * of that as possible which is why its a little weird.
     *
     * @param savePreferencesMenu the save userTablePreferences menu
     * @param isTableViewColumnsEmpty true if when
     * {@link TableView#getColumns()} is called it should return an empty list,
     * false otherwise
     * @param isActivGraphNull true if when
     * {@link GraphManager#getActiveGraph()} is called it should return null,
     * false otherwise
     * @param expectSavePrefsCalled true if it is expected that
     * {@link TableViewPreferencesIoProvider#savePreferences(GraphElementType, TableView, int)}
     * should have been called, false otherwise
     */
    private void verifySavePreferencesAction(final MenuItem savePreferencesMenu,
            final boolean isTableViewColumnsEmpty,
            final boolean isActivGraphNull,
            final boolean expectSavePrefsCalled) {
        try (
                final MockedStatic<TableViewPreferencesIoProvider> tablePrefsIOUtilsMockedStatic
                = Mockito.mockStatic(TableViewPreferencesIoProvider.class);
                final MockedStatic<GraphManager> graphManagerMockedStatic
                = Mockito.mockStatic(GraphManager.class);) {

            final TableView<ObservableList<String>> tableView = mock(TableView.class);
            when(table.getTableView()).thenReturn(tableView);

            final TableViewState tableViewState = new TableViewState();
            tableViewState.setElementType(GraphElementType.VERTEX);
            when(tableViewTopComponent.getCurrentState()).thenReturn(tableViewState);

            final UserTablePreferences userTablePreferences = new UserTablePreferences();
            userTablePreferences.setMaxRowsPerPage(42);
            when(activeTableReference.getUserTablePreferences()).thenReturn(userTablePreferences);

            final GraphManager graphManager = mock(GraphManager.class);
            graphManagerMockedStatic.when(GraphManager::getDefault).thenReturn(graphManager);

            final Graph graph = mock(Graph.class);

            final ActionEvent actionEvent = mock(ActionEvent.class);

            if (isTableViewColumnsEmpty) {
                when(tableView.getColumns()).thenReturn(FXCollections.observableArrayList());
            } else {
                when(tableView.getColumns())
                        .thenReturn(FXCollections.observableArrayList(mock(TableColumn.class)));
            }

            if (isActivGraphNull) {
                when(graphManager.getActiveGraph()).thenReturn(null);
            } else {
                when(graphManager.getActiveGraph()).thenReturn(graph);
            }

            savePreferencesMenu.getOnAction().handle(actionEvent);

            if (expectSavePrefsCalled) {
                tablePrefsIOUtilsMockedStatic.verify(() -> TableViewPreferencesIoProvider
                        .savePreferences(GraphElementType.VERTEX, tableView, 42));
            } else {
                tablePrefsIOUtilsMockedStatic.verifyNoInteractions();
            }

            verify(actionEvent).consume();
        }
    }

    /**
     * Verifies the page size menu items have been constructed correctly and
     * perform the correct actions when clicked.
     *
     * @param radioButton a page size radio button
     * @param expectedTitle the page size that the radio button will trigger as
     * a string
     */
    private void verifyPageSizeRadioButton(final RadioMenuItem radioButton,
            final String expectedTitle) {
        assertEquals(expectedTitle, radioButton.getText());
        assertSame(preferencesMenu.getPageSizeToggle(), radioButton.getToggleGroup());

        if (expectedTitle.equals("500")) {
            assertTrue(radioButton.isSelected());
        } else {
            assertFalse(radioButton.isSelected());
        }

        try {
            verifyPageSizeAction(radioButton, Integer.parseInt(expectedTitle));
        } catch (InterruptedException ie) {
            fail("Something went wrong while waiting for the latch to "
                    + "confirm work in the JavaFX thread had completed.");
        }
    }

    /**
     * Verifies that when a page size menu item is clicked, the preferences will
     * be updated as well as the pagination and the table will be refreshed. If
     * the page size that was clicked is the current page size, then no actions
     * should be taken.
     *
     * @param pageSizeMenuItem a page size menu item
     * @param pageSize the page size that the menu item will trigger
     * @throws InterruptedException if there is a an issue waiting for the
     * JavaFX thread work to complete
     */
    private void verifyPageSizeAction(final MenuItem pageSizeMenuItem,
            final int pageSize) throws InterruptedException {
        clearInvocations(activeTableReference, tablePane);

        final ActionEvent actionEvent = mock(ActionEvent.class);
        final Pagination pagination = mock(Pagination.class);

        final UserTablePreferences userTablePreferences = new UserTablePreferences();
        userTablePreferences.setMaxRowsPerPage(42);

        when(activeTableReference.getUserTablePreferences()).thenReturn(userTablePreferences);
        when(activeTableReference.getPagination()).thenReturn(pagination);

        pageSizeMenuItem.getOnAction().handle(actionEvent);
        pageSizeMenuItem.getOnAction().handle(actionEvent);

        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> latch.countDown());
        latch.await();

        // The action was called twice but the update should only happen once as
        // it was the same page size. No change for the second action
        assertEquals(pageSize, userTablePreferences.getMaxRowsPerPage());
        verify(activeTableReference).updatePagination(pageSize, tablePane);
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
