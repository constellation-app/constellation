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
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.tableview.TableViewTopComponent;
import au.gov.asd.tac.constellation.views.tableview.api.ActiveTableReference;
import au.gov.asd.tac.constellation.views.tableview.plugins.UpdateStatePlugin;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewState;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import org.openide.util.HelpCtx;

/**
 * Creates the UI components that will be added to the table tool bar.
 *
 * @author formalhaunt
 */
public class TableToolbar {

    private static final String TABLE_TOP_COMPONENT_CLASS_NAME = TableViewTopComponent.class.getName();

    private static final String COLUMN_VISIBILITY = "Column Visibility";
    private static final String ELEMENT_TYPE = "Element Type";
    private static final String SELECTED_ONLY = "Selected Only";
    private static final String HELP = "Display help for Table View";

    private static final ImageView COLUMNS_ICON = new ImageView(UserInterfaceIconProvider.COLUMNS.buildImage(16));
    private static final ImageView SELECTED_VISIBLE_ICON = new ImageView(UserInterfaceIconProvider.VISIBLE.buildImage(16, ConstellationColor.CHERRY.getJavaColor()));
    private static final ImageView ALL_VISIBLE_ICON = new ImageView(UserInterfaceIconProvider.VISIBLE.buildImage(16));
    private static final ImageView VERTEX_ICON = new ImageView(UserInterfaceIconProvider.NODES.buildImage(16));
    private static final ImageView TRANSACTION_ICON = new ImageView(UserInterfaceIconProvider.TRANSACTIONS.buildImage(16));
    private static final ImageView LINK_ICON = new ImageView(UserInterfaceIconProvider.LINKS.buildImage(16));
    private static final ImageView HELP_ICON = new ImageView(UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.BLUEBERRY.getJavaColor()));

    private static final int WIDTH = 120;

    private ToolBar toolbar;

    private Button columnVisibilityButton;
    private Button elementTypeButton;
    private Button helpButton;

    private ToggleButton selectedOnlyButton;

    private ExportMenu exportMenu;
    private CopyMenu copyMenu;
    private PreferencesMenu preferencesMenu;

    private final TablePane tablePane;

    /**
     * Creates a new table tool bar.
     *
     * @param tablePane the pane that contains the table
     */
    public TableToolbar(final TablePane tablePane) {
        this.tablePane = tablePane;
    }

    /**
     * Initializes the export menu. Until this method is called, all menu UI components will be null.
     */
    public void init() {
        columnVisibilityButton = createButton(COLUMNS_ICON, COLUMN_VISIBILITY, e -> {
            final ColumnVisibilityContextMenu columnVisibilityMenu
                    = createColumnVisibilityContextMenu();
            columnVisibilityMenu.getContextMenu()
                    .show(columnVisibilityButton, Side.RIGHT, 0, 0);

            e.consume();
        });

        selectedOnlyButton = createToggleButton(getSelectedOnlyInitialIcon(), SELECTED_ONLY, e -> {
            if (getTableViewTopComponent().getCurrentState() != null) {
                getActiveTableReference().getSelectedOnlySelectedRows().clear();

                final TableViewState newState = new TableViewState(getTableViewTopComponent().getCurrentState());
                newState.setSelectedOnly(!getTableViewTopComponent().getCurrentState().isSelectedOnly());

                selectedOnlyButton.setGraphic(
                        newState.isSelectedOnly() ? SELECTED_VISIBLE_ICON : ALL_VISIBLE_ICON
                );

                PluginExecution.withPlugin(
                        new UpdateStatePlugin(newState)
                ).executeLater(getTableViewTopComponent().getCurrentGraph());
            }

            e.consume();
        });

        elementTypeButton = createButton(getElementTypeInitialIcon(), ELEMENT_TYPE, e -> {
            if (getTableViewTopComponent().getCurrentState() != null) {
                final TableViewState newState = new TableViewState(getTableViewTopComponent().getCurrentState());

                newState.setElementType(getTableViewTopComponent().getCurrentState().getElementType() == GraphElementType.TRANSACTION
                        ? GraphElementType.VERTEX : getTableViewTopComponent().getCurrentState().getElementType()
                        == GraphElementType.VERTEX ? GraphElementType.LINK : GraphElementType.TRANSACTION);

                elementTypeButton.setGraphic(
                        newState.getElementType() == GraphElementType.TRANSACTION
                        ? TRANSACTION_ICON : newState.getElementType() == GraphElementType.VERTEX ? VERTEX_ICON : LINK_ICON);

                PluginExecution.withPlugin(
                        new UpdateStatePlugin(newState)
                ).executeLater(getTableViewTopComponent().getCurrentGraph());
            }

            e.consume();
        });

        copyMenu = createCopyMenu();
        exportMenu = createExportMenu();
        preferencesMenu = createPreferencesMenu();

        helpButton = createButton(HELP_ICON, HELP, e -> {
            getHelpContext().display();

            e.consume();
        });

        toolbar = new ToolBar(columnVisibilityButton, selectedOnlyButton,
                elementTypeButton, new Separator(), copyMenu.getCopyButton(),
                exportMenu.getExportButton(), preferencesMenu.getPreferencesButton(),
                helpButton);

        toolbar.setOrientation(Orientation.VERTICAL);
        toolbar.setPadding(new Insets(5));
    }

    /**
     * Update the toolbar using the state.
     *
     * @param state the current table view state
     */
    public void updateToolbar(final TableViewState state) {
        Platform.runLater(() -> {
            if (state != null) {
                getSelectedOnlyButton().setSelected(state.isSelectedOnly());
                getSelectedOnlyButton().setGraphic(state.isSelectedOnly()
                        ? SELECTED_VISIBLE_ICON : ALL_VISIBLE_ICON);
                getElementTypeButton().setGraphic(
                        state.getElementType() == GraphElementType.TRANSACTION
                        ? TRANSACTION_ICON : state.getElementType() == GraphElementType.VERTEX ? VERTEX_ICON : LINK_ICON);

            }
        });
    }

    /**
     * Gets the tool bar UI component that will be added to the table and contains all the other UI buttons etc that are created and added to it.
     *
     * @return the table tool bar
     */
    public ToolBar getToolbar() {
        return toolbar;
    }

    /**
     * Gets the column visibility button on the tool bar. This button will, when clicked generate a context menu with more options to select from.
     *
     * @return the column visibility button on the tool bar
     * @see ColumnVisibilityContextMenu
     */
    public Button getColumnVisibilityButton() {
        return columnVisibilityButton;
    }

    /**
     * Gets the "Element Type" button from the tool bar that toggles between the currently displayed element types.
     * <p/>
     * The table displays either nodes or edges. This button is what toggles between the two.
     *
     * @return the element type button on the tool bar
     */
    public Button getElementTypeButton() {
        return elementTypeButton;
    }

    /**
     * Gets the help button on the tool bar.
     *
     * @return the help button on the tool bar
     */
    public Button getHelpButton() {
        return helpButton;
    }

    /**
     * Gets the "Selected Only Mode" toggle button on the tool bar.
     * <p/>
     * When "Selected Only Mode" is <b>ON</b>, selection in the table does not effect selection in the graph and vice versa. This is because the contents of the table is only what is selected in the graph and of the active table element type.
     * <p/>
     * When "Selected Only Mode" is <b>OFF</b> then selection in the table effects selection in the graph and vice versa because the contents of the table is all elements of the active table element type in the graph.
     *
     * @return the "Selected Only Mode" toggle button
     */
    public ToggleButton getSelectedOnlyButton() {
        return selectedOnlyButton;
    }

    /**
     * Gets the {@link ExportMenu} associated to the tool bar. The export menu will allow for the export of the table data into different formats.
     *
     * @return the export menu on the tool bar
     * @see ExportMenu
     */
    public ExportMenu getExportMenu() {
        return exportMenu;
    }

    /**
     * Gets the {@link CopyMenu} associated to the tool bar. The copy menu will allow for the loading of CSV table data into the OS clipboard.
     *
     * @return the copy menu on the tool bar
     * @see CopyMenu
     */
    public CopyMenu getCopyMenu() {
        return copyMenu;
    }

    /**
     * Gets the {@link PreferencesMenu} associated to the tool bar.
     *
     * @return the preference menu on the tool bar
     * @see PreferencesMenu
     */
    public PreferencesMenu getPreferencesMenu() {
        return preferencesMenu;
    }

    /**
     * Creates a new {@link CopyMenu} and initializes it.
     *
     * @return the new copy menu
     */
    protected CopyMenu createCopyMenu() {
        final CopyMenu newCopyMenu = new CopyMenu(tablePane);
        newCopyMenu.init();

        return newCopyMenu;
    }

    /**
     * Creates a new {@link ExportMenu} and initializes it.
     *
     * @return the new export menu
     */
    protected ExportMenu createExportMenu() {
        final ExportMenu newExportMenu = new ExportMenu(tablePane);
        newExportMenu.init();

        return newExportMenu;
    }

    /**
     * Creates a new {@link PreferencesMenu} and initializes it.
     *
     * @return the new preferences menu
     */
    protected PreferencesMenu createPreferencesMenu() {
        final PreferencesMenu newPreferencesMenu = new PreferencesMenu(tablePane);
        newPreferencesMenu.init();

        return newPreferencesMenu;
    }

    /**
     * Creates a new help context for the tool bar.
     *
     * @return the new help context
     */
    protected HelpCtx getHelpContext() {
        return new HelpCtx(TABLE_TOP_COMPONENT_CLASS_NAME);
    }

    /**
     * Gets the initial icon for the element type button. If the current state is null or has the element type set to {@link GraphElementType#VERTEX} then the {@link #VERTEX_ICON} will be returned, other wise the {@link #TRANSACTION_ICON} will be returned.
     *
     * @return the initial icon to place on the element type button
     */
    protected ImageView getElementTypeInitialIcon() {
        return getTableViewTopComponent().getCurrentState() != null
                && getTableViewTopComponent().getCurrentState().getElementType() == GraphElementType.VERTEX
                ? VERTEX_ICON : TRANSACTION_ICON;
    }

    /**
     * Gets the initial icon for the selected only button. If the current state is null or has the selected only flag set to true then the {@link #SELECTED_VISIBLE_ICON} will be returned, otherwise the {@link #ALL_VISIBLE_ICON} will be returned.
     *
     * @return the initial icon to place on the selected only button
     */
    protected ImageView getSelectedOnlyInitialIcon() {
        return getTableViewTopComponent().getCurrentState() != null
                && getTableViewTopComponent().getCurrentState().isSelectedOnly()
                ? SELECTED_VISIBLE_ICON : ALL_VISIBLE_ICON;
    }

    /**
     * Creates a new {@link ColumnVisibilityContextMenu} and initializes it.
     *
     * @return the new column visibility context menu
     */
    protected ColumnVisibilityContextMenu createColumnVisibilityContextMenu() {
        final ColumnVisibilityContextMenu newColumnVisibilityMenu
                = new ColumnVisibilityContextMenu(getTable());
        newColumnVisibilityMenu.init();

        return newColumnVisibilityMenu;
    }

    /**
     * Convenience method for accessing the active table reference.
     *
     * @return the active table reference
     */
    private ActiveTableReference getActiveTableReference() {
        return tablePane.getActiveTableReference();
    }

    /**
     * Convenience method for accessing the table view top component.
     *
     * @return the table view top component
     */
    private TableViewTopComponent getTableViewTopComponent() {
        return tablePane.getParentComponent();
    }

    /**
     * Convenience method for accessing the table.
     *
     * @return the table
     */
    private Table getTable() {
        return tablePane.getTable();
    }

    /**
     * Creates a button.
     *
     * @param icon the icon to place on the button
     * @param tooltip the tool tip text to associate with the button
     * @param eventHandler the action handler for when the button is pressed
     * @return the created button
     */
    private Button createButton(final ImageView icon,
            final String tooltip,
            final EventHandler<ActionEvent> eventHandler) {
        final Button button = new Button();

        button.setGraphic(icon);
        button.setMaxWidth(WIDTH);
        button.setPadding(new Insets(5));
        button.setTooltip(new Tooltip(tooltip));
        button.setOnAction(eventHandler);

        return button;
    }

    /**
     * Creates a toggle button.
     *
     * @param icon the icon to place on the button
     * @param tooltip the tool tip text to associate with the button
     * @param eventHandler the action handler for when the button is pressed
     * @return the created toggle button
     */
    private ToggleButton createToggleButton(final ImageView icon,
            final String tooltip,
            final EventHandler<ActionEvent> eventHandler) {
        final ToggleButton button = new ToggleButton();

        button.setGraphic(icon);
        button.setMaxWidth(WIDTH);
        button.setPadding(new Insets(5));
        button.setTooltip(new Tooltip(tooltip));
        button.setOnAction(eventHandler);

        return button;
    }
}
