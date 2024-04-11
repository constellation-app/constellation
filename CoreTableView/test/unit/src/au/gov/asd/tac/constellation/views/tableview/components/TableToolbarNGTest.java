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
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.tableview.TableViewTopComponent;
import au.gov.asd.tac.constellation.views.tableview.api.ActiveTableReference;
import au.gov.asd.tac.constellation.views.tableview.plugins.UpdateStatePlugin;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewState;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openide.util.HelpCtx;
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
public class TableToolbarNGTest {

    private static final Logger LOGGER = Logger.getLogger(TableToolbarNGTest.class.getName());

    private TableViewTopComponent tableTopComponent;
    private TablePane tablePane;
    private Table table;
    private ActiveTableReference tableService;
    private Graph graph;

    private CopyMenu copyMenu;
    private ExportMenu exportMenu;
    private PreferencesMenu preferencesMenu;
    private ColumnVisibilityContextMenu columnVisibilityContextMenu;
    private ElementTypeContextMenu elementTypeContextMenu;

    private MenuButton copyMenuButton;
    private MenuButton exportMenuButton;
    private MenuButton preferencesMenuButton;
    private ContextMenu contextMenu;

    private TableToolbar tableToolbar;

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
        tableTopComponent = mock(TableViewTopComponent.class);
        tablePane = mock(TablePane.class);
        table = mock(Table.class);
        tableService = mock(ActiveTableReference.class);
        graph = mock(Graph.class);

        copyMenu = mock(CopyMenu.class);
        exportMenu = mock(ExportMenu.class);
        preferencesMenu = mock(PreferencesMenu.class);
        columnVisibilityContextMenu = mock(ColumnVisibilityContextMenu.class);
        elementTypeContextMenu = mock(ElementTypeContextMenu.class);

        copyMenuButton = mock(MenuButton.class);
        exportMenuButton = mock(MenuButton.class);
        preferencesMenuButton = mock(MenuButton.class);
        contextMenu = mock(ContextMenu.class);

        tableToolbar = spy(new TableToolbar(tablePane));

        doReturn(copyMenu).when(tableToolbar).createCopyMenu();
        doReturn(exportMenu).when(tableToolbar).createExportMenu();
        doReturn(preferencesMenu).when(tableToolbar).createPreferencesMenu();
        doReturn(columnVisibilityContextMenu).when(tableToolbar).createColumnVisibilityContextMenu();
        doReturn(elementTypeContextMenu).when(tableToolbar).createElementTypeContextMenu();

        when(tablePane.getTable()).thenReturn(table);
        when(tablePane.getParentComponent()).thenReturn(tableTopComponent);
        when(tablePane.getActiveTableReference()).thenReturn(tableService);

        when(copyMenu.getCopyButton()).thenReturn(copyMenuButton);
        when(exportMenu.getExportButton()).thenReturn(exportMenuButton);
        when(preferencesMenu.getPreferencesButton()).thenReturn(preferencesMenuButton);
        when(columnVisibilityContextMenu.getContextMenu()).thenReturn(contextMenu);
        when(elementTypeContextMenu.getContextMenu()).thenReturn(contextMenu);
        
        when(tableTopComponent.getCurrentGraph()).thenReturn(graph);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void allUIComponentsNullBeforeInit() {
        assertNull(tableToolbar.getToolbar());
        assertNull(tableToolbar.getColumnVisibilityButton());
        assertNull(tableToolbar.getElementTypeButton());
        assertNull(tableToolbar.getHelpButton());
        assertNull(tableToolbar.getExportMenu());
        assertNull(tableToolbar.getCopyMenu());
        assertNull(tableToolbar.getPreferencesMenu());
    }

    @Test
    public void createToolbarButtons() {
        tableToolbar.init();

        assertNotNull(tableToolbar.getToolbar());
        assertNotNull(tableToolbar.getColumnVisibilityButton());
        assertNotNull(tableToolbar.getElementTypeButton());
        assertNotNull(tableToolbar.getHelpButton());
        assertNotNull(tableToolbar.getExportMenu());
        assertNotNull(tableToolbar.getCopyMenu());
        assertNotNull(tableToolbar.getPreferencesMenu());

        // No equality in separator so we have to pull it out and place it back in
        final Optional<Node> separator = tableToolbar.getToolbar().getItems().stream()
                .filter(Separator.class::isInstance)
                .findFirst();

        assertTrue(separator.isPresent());

        assertEquals(
                FXCollections.observableList(
                        List.of(
                                tableToolbar.getColumnVisibilityButton(),
                                tableToolbar.getSelectedOnlyButton(),
                                tableToolbar.getElementTypeButton(),
                                separator.get(),
                                tableToolbar.getCopyMenu().getCopyButton(),
                                tableToolbar.getExportMenu().getExportButton(),
                                tableToolbar.getPreferencesMenu().getPreferencesButton(),
                                tableToolbar.getHelpButton()
                        )
                ),
                tableToolbar.getToolbar().getItems()
        );
        assertEquals(Orientation.VERTICAL, tableToolbar.getToolbar().getOrientation());
        assertEquals(new Insets(5), tableToolbar.getToolbar().getPadding());

        // Column Visibility Button
        buttonChecks(
                tableToolbar.getColumnVisibilityButton(),
                UserInterfaceIconProvider.COLUMNS.buildImage(16),
                "Column Visibility"
        );

        columnVisibilityButtonActionCheck();

        // Selected Only Mode Button
        toggleButtonChecks(
                tableToolbar.getSelectedOnlyButton(),
                UserInterfaceIconProvider.VISIBLE.buildImage(16),
                "Selected Only"
        );

        selectedOnlyModeActionChecks(true, UserInterfaceIconProvider.VISIBLE.buildImage(16));
        selectedOnlyModeActionChecks(false, UserInterfaceIconProvider.VISIBLE.buildImage(16, ConstellationColor.CHERRY.getJavaColor()));

        // Element Type Button
        buttonChecks(
                tableToolbar.getElementTypeButton(),
                UserInterfaceIconProvider.TRANSACTIONS.buildImage(16),
                "Element Type"
        );
        
        elementTypeActionCheck();

        // Help Button
        buttonChecks(
                tableToolbar.getHelpButton(),
                UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.WHITE.getJavaColor()),
                "Display help for Table View"
        );

        helpContextButtonActionCheck();
    }

    @Test
    public void updateToolbar() throws InterruptedException {
        buttonChangeChecksOnToolbarUpdate(
                true,
                GraphElementType.TRANSACTION,
                UserInterfaceIconProvider.VISIBLE.buildImage(16, ConstellationColor.CHERRY.getJavaColor()),
                UserInterfaceIconProvider.TRANSACTIONS.buildImage(16)
        );

        buttonChangeChecksOnToolbarUpdate(
                false,
                GraphElementType.VERTEX,
                UserInterfaceIconProvider.VISIBLE.buildImage(16),
                UserInterfaceIconProvider.NODES.buildImage(16)
        );
    }

    @Test
    public void getElementTypeInitialIcon() {
        final TableViewState state = new TableViewState();

        when(tableTopComponent.getCurrentState()).thenReturn(state);

        state.setElementType(GraphElementType.META);
        assertTrue(isImageEqual(UserInterfaceIconProvider.TRANSACTIONS.buildImage(16),
                tableToolbar.getElementTypeInitialIcon().getImage()));

        state.setElementType(GraphElementType.TRANSACTION);
        assertTrue(isImageEqual(UserInterfaceIconProvider.TRANSACTIONS.buildImage(16),
                tableToolbar.getElementTypeInitialIcon().getImage()));

        state.setElementType(GraphElementType.VERTEX);
        assertTrue(isImageEqual(UserInterfaceIconProvider.NODES.buildImage(16),
                tableToolbar.getElementTypeInitialIcon().getImage()));

        when(tableTopComponent.getCurrentState()).thenReturn(null);

        assertTrue(isImageEqual(UserInterfaceIconProvider.TRANSACTIONS.buildImage(16),
                tableToolbar.getElementTypeInitialIcon().getImage()));
    }

    @Test
    public void getSelectedOnlyInitialIcon() {
        final TableViewState state = new TableViewState();

        when(tableTopComponent.getCurrentState()).thenReturn(state);

        state.setSelectedOnly(true);
        assertTrue(isImageEqual(UserInterfaceIconProvider.VISIBLE.buildImage(16, ConstellationColor.CHERRY.getJavaColor()),
                tableToolbar.getSelectedOnlyInitialIcon().getImage()));

        state.setSelectedOnly(false);
        assertTrue(isImageEqual(UserInterfaceIconProvider.VISIBLE.buildImage(16),
                tableToolbar.getSelectedOnlyInitialIcon().getImage()));

        when(tableTopComponent.getCurrentState()).thenReturn(null);

        assertTrue(isImageEqual(UserInterfaceIconProvider.VISIBLE.buildImage(16),
                tableToolbar.getSelectedOnlyInitialIcon().getImage()));
    }

    /**
     * Verifies that when the update tool bar method is called, the icons on the
     * selected only button and element type buttons are changed. Also verifies
     * the selected state of the selected only toggle button is changed.
     *
     * @param newSelectedOnlyState the new selected only state, true if it is
     * on, false otherwise
     * @param newElementType the new element type to be displayed in the table
     * @param expectedSelectedOnlyIcon the icon expected to be on the selected
     * only button once update is called
     * @param expectedElementTypeIcon the icon expected to be on the element
     * type button once the update it called
     */
    private void buttonChangeChecksOnToolbarUpdate(final boolean newSelectedOnlyState,
            final GraphElementType newElementType,
            final Image expectedSelectedOnlyIcon,
            final Image expectedElementTypeIcon) throws InterruptedException {
        final TableViewState state = new TableViewState();

        final ArgumentCaptor<ImageView> selectedOnlyCaptor = ArgumentCaptor.forClass(ImageView.class);
        final ArgumentCaptor<ImageView> elementTypeCaptor = ArgumentCaptor.forClass(ImageView.class);

        final ToggleButton selectedOnlyButton = mock(ToggleButton.class);
        final Button elementTypeButton = mock(Button.class);

        when(tableToolbar.getSelectedOnlyButton()).thenReturn(selectedOnlyButton);
        when(tableToolbar.getElementTypeButton()).thenReturn(elementTypeButton);

        state.setSelectedOnly(newSelectedOnlyState);
        state.setElementType(newElementType);

        tableToolbar.updateToolbar(state);

        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> latch.countDown());
        latch.await();

        verify(selectedOnlyButton).setSelected(newSelectedOnlyState);
        verify(selectedOnlyButton).setGraphic(selectedOnlyCaptor.capture());
        assertTrue(isImageEqual(
                expectedSelectedOnlyIcon,
                selectedOnlyCaptor.getValue().getImage()
        ));

        verify(elementTypeButton).setGraphic(elementTypeCaptor.capture());
        assertTrue(isImageEqual(
                expectedElementTypeIcon,
                elementTypeCaptor.getValue().getImage()
        ));
    }

    /**
     * Verifies that buttons on the tool bar are created correctly.
     *
     * @param button the button to check
     * @param expectedIcon the expected image to be present on the button
     * @param expectedToolTip the expected tool tip to be associated with the
     * button
     */
    private void buttonChecks(final Button button,
            final Image expectedIcon,
            final String expectedToolTip) {
        final ImageView buttonIcon = (ImageView) button.getGraphic();
        assertTrue(isImageEqual(expectedIcon, buttonIcon.getImage()));
        assertEquals(120.0d, button.getMaxWidth());
        assertEquals(new Insets(5), button.getPadding());
        assertEquals(expectedToolTip, button.getTooltip().getText());
    }

    /**
     * Verifies that toggle buttons on the tool bar are created correctly.
     *
     * @param toggleButton the toggle button to check
     * @param expectedIcon the expected image to be present on the toggle button
     * @param expectedToolTip the expected tool tip to be associated with the
     * toggle button
     */
    private void toggleButtonChecks(final ToggleButton toggleButton,
            final Image expectedIcon,
            final String expectedToolTip) {
        final ImageView buttonIcon = (ImageView) toggleButton.getGraphic();
        assertTrue(isImageEqual(expectedIcon, buttonIcon.getImage()));
        assertEquals(120.0d, toggleButton.getMaxWidth());
        assertEquals(new Insets(5), toggleButton.getPadding());
        assertEquals(expectedToolTip, toggleButton.getTooltip().getText());
    }

    /**
     * Verifies that when the column visibility button is clicked, the
     * {@link ColumnVisibilityContextMenu} is created, initialized and shown.
     */
    private void columnVisibilityButtonActionCheck() {
        final ActionEvent actionEvent = mock(ActionEvent.class);

        tableToolbar.getColumnVisibilityButton().getOnAction().handle(actionEvent);

        verify(contextMenu).show(tableToolbar.getColumnVisibilityButton(), Side.RIGHT, 0, 0);
        verify(actionEvent).consume();
    }

    /**
     * When the selected only mode button is pressed, the table switches between
     * "Selected Only Mode" ON and OFF. This verifies that as the button is
     * pressed that transition between ON and OFF happens and the update state
     * plugin is executed triggering the required changes.
     *
     * @param selectedOnlyModeInitialState the initial status of the "Selected
     * Only Mode", the expected status after the button is pressed will be the
     * inverse
     * @param expectedNewIcon the new image that is expected to be on the button
     * after it was clicked
     */
    private void selectedOnlyModeActionChecks(final boolean selectedOnlyModeInitialState,
            final Image expectedNewIcon) {
        try (MockedStatic<PluginExecution> pluginExecutionMockedStatic
                = Mockito.mockStatic(PluginExecution.class)) {
            final PluginExecution pluginExecution = mock(PluginExecution.class);
            final ActionEvent actionEvent = mock(ActionEvent.class);

            final TableViewState tableViewState = new TableViewState();
            tableViewState.setSelectedOnly(selectedOnlyModeInitialState);

            when(tableTopComponent.getCurrentState()).thenReturn(tableViewState);

            final ArgumentCaptor<UpdateStatePlugin> captor
                    = ArgumentCaptor.forClass(UpdateStatePlugin.class);

            pluginExecutionMockedStatic.when(() -> PluginExecution
                    .withPlugin(captor.capture())).thenReturn(pluginExecution);

            tableToolbar.getSelectedOnlyButton().getOnAction().handle(actionEvent);

            final UpdateStatePlugin updatePlugin = captor.getValue();

            final ImageView buttonIcon = (ImageView) tableToolbar.getSelectedOnlyButton().getGraphic();
            assertTrue(isImageEqual(expectedNewIcon, buttonIcon.getImage()));

            assertEquals(!selectedOnlyModeInitialState, updatePlugin.getTableViewState().isSelectedOnly());
            verify(pluginExecution).executeLater(graph);
            verify(actionEvent).consume();
        }
    }

    /**
     * When the element type button is pressed the tables state is switched
     * between VERTEX and TRANSACTION. This verifies that as the button is
     * pressed that transition happens and the update state plugin is executed
     * triggering the required changes. The buttons icon should also change to
     * the element type now set in the state.
     *
     * @param elementTypeInitialState the initial element type in the state
     * before the button is pressed
     * @param elementTypeEndState the expected element type in the state after
     * the button is pressed
     * @param expectedNewIcon the expected image to be now on the element type
     * change button
     */
    private void elementTypeActionCheck() {
        final ActionEvent actionEvent = mock(ActionEvent.class);

        tableToolbar.getElementTypeButton().getOnAction().handle(actionEvent);
        
        System.out.println("contextMenu:");
        System.out.println(contextMenu.toString());


        verify(contextMenu).show(tableToolbar.getElementTypeButton(), Side.RIGHT, 0, 0);
        verify(actionEvent).consume();
    }

    /**
     * Verifies that the help button will display the {@link HelpCtx}.
     */
    private void helpContextButtonActionCheck() {
        final ActionEvent actionEvent = mock(ActionEvent.class);

        final HelpCtx helpContext = mock(HelpCtx.class);

        when(tableToolbar.getHelpContext()).thenReturn(helpContext);

        tableToolbar.getHelpButton().getOnAction().handle(actionEvent);

        verify(helpContext).display();
        verify(actionEvent).consume();
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
