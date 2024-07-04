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
package au.gov.asd.tac.constellation.views.tableview.components;

import au.gov.asd.tac.constellation.views.tableview.panes.TablePane;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.tableview.api.ActiveTableReference;
import au.gov.asd.tac.constellation.views.tableview.utilities.TableViewUtilities;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
public class CopyMenuNGTest {
    private static final Logger LOGGER = Logger.getLogger(CopyMenuNGTest.class.getName());

    private TablePane tablePane;
    private ActiveTableReference activeTableReference;
    private Table table;
    private Pagination pagination;

    private CopyMenu copyMenu;

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
        tablePane = mock(TablePane.class);
        activeTableReference = mock(ActiveTableReference.class);
        table = mock(Table.class);
        pagination = mock(Pagination.class);

        when(tablePane.getTable()).thenReturn(table);
        when(tablePane.getActiveTableReference()).thenReturn(activeTableReference);
        when(activeTableReference.getPagination()).thenReturn(pagination);

        copyMenu = new CopyMenu(tablePane);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void allUIComponentsNullBeforeInit() {
        assertNull(copyMenu.getCopyButton());
        assertNull(copyMenu.getCopyTableMenu());
        assertNull(copyMenu.getCopyTableSelectionMenu());
    }

    @Test
    public void createCopyButtons() {
        copyMenu.init();

        assertNotNull(copyMenu.getCopyButton());
        assertNotNull(copyMenu.getCopyTableMenu());
        assertNotNull(copyMenu.getCopyTableSelectionMenu());

        assertEquals(
                FXCollections.observableList(
                        List.of(
                                copyMenu.getCopyTableMenu(),
                                copyMenu.getCopyTableSelectionMenu()
                        )
                ),
                copyMenu.getCopyButton().getItems()
        );

        // Copy Button
        final ImageView icon = (ImageView) copyMenu.getCopyButton().getGraphic();
        assertTrue(isImageEqual(UserInterfaceIconProvider.COPY.buildImage(16), icon.getImage()));
        assertEquals(120.0d, copyMenu.getCopyButton().getMaxWidth());
        assertEquals(Side.RIGHT, copyMenu.getCopyButton().getPopupSide());

        // Copy Whole Table Menu Item
        assertEquals("Copy Table", copyMenu.getCopyTableMenu().getText());
        verifyCopyAction(copyMenu.getCopyTableMenu().getOnAction(), false);

        // Copy Selected Rows Menu Item
        assertEquals("Copy Table (Selection)", copyMenu.getCopyTableSelectionMenu().getText());
        verifyCopyAction(copyMenu.getCopyTableSelectionMenu().getOnAction(), true);
    }

    /**
     * Verify that the passed event handler copies to the clipboard either the
     * whole table or just the selected rows.
     *
     * @param eventHandler the handler to test
     * @param expectedCopyOnlySelectedRows true if only the selected rows are
     * expected to be copied, false otherwise
     */
    private void verifyCopyAction(final EventHandler<ActionEvent> eventHandler,
            final boolean expectedCopyOnlySelectedRows) {
        try (MockedStatic<TableViewUtilities> tableViewUtilsMockedStatic
                = Mockito.mockStatic(TableViewUtilities.class)) {

            final ActionEvent actionEvent = mock(ActionEvent.class);

            final TableView<ObservableList<String>> tableView = mock(TableView.class);
            when(table.getTableView()).thenReturn(tableView);

            final String tableData = "Row1Column1,Row1Column2\nRow2Column2,Row2Column2";

            tableViewUtilsMockedStatic.when(() -> TableViewUtilities.getTableData(tableView, pagination,
                    false, expectedCopyOnlySelectedRows)).thenReturn(tableData);

            eventHandler.handle(actionEvent);

            tableViewUtilsMockedStatic.verify(() -> TableViewUtilities.copyToClipboard(tableData));
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
