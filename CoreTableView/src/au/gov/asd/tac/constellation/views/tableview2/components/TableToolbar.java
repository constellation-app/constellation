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

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.tableview2.TableViewTopComponent;
import au.gov.asd.tac.constellation.views.tableview2.plugins.UpdateStatePlugin;
import au.gov.asd.tac.constellation.views.tableview2.service.TableService;
import au.gov.asd.tac.constellation.views.tableview2.state.TableViewState;
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
    
    private final TableViewTopComponent tableTopComponent;
    private final TableViewPane tablePane;
    private final Table table;
    
    private final TableService tableService;
    
    /**
     * Creates a new table tool bar.
     *
     * @param tableTopComponent the top component that the table is embedded into
     * @param tablePane
     * @param table the table that the tool bar will be attached to
     * @param tableService the table service associated to the table
     */
    public TableToolbar(final TableViewTopComponent tableTopComponent,
                        final TableViewPane tablePane,
                        final Table table,
                        final TableService tableService) {
        this.tableTopComponent = tableTopComponent;
        this.tablePane = tablePane;
        this.table = table;
        this.tableService = tableService;
    }
    
    /**
     * Initializes the export menu. Until this method is called, all menu UI components
     * will be null.
     */
    public void init() {
        columnVisibilityButton = createButton(COLUMNS_ICON, COLUMN_VISIBILITY, e -> {
            final ColumnVisibilityContextMenu columnVisibilityMenu
                    = createColumnVisibilityContextMenu();
            columnVisibilityMenu.getContextMenu()
                    .show(columnVisibilityButton, Side.RIGHT, 0, 0);
            
            e.consume();
        });
        
        // TODO Initial icon here should be based on state if it exists??
        selectedOnlyButton = createToggleButton(ALL_VISIBLE_ICON, SELECTED_ONLY, e -> {
            if (tableTopComponent.getCurrentState() != null) {
                tableService.getSelectedOnlySelectedRows().clear();
                
                final TableViewState newState = new TableViewState(tableTopComponent.getCurrentState());
                newState.setSelectedOnly(!tableTopComponent.getCurrentState().isSelectedOnly());
                
                selectedOnlyButton.setGraphic(
                        newState.isSelectedOnly() ? SELECTED_VISIBLE_ICON : ALL_VISIBLE_ICON 
                );
                
                PluginExecution.withPlugin(
                        new UpdateStatePlugin(newState)
                ).executeLater(tableTopComponent.getCurrentGraph());
            }
            
            e.consume();
        });
        
        // TODO Initial icon here should be based on state if it exists??
        elementTypeButton = createButton(TRANSACTION_ICON, ELEMENT_TYPE, e -> {
            if (tableTopComponent.getCurrentState() != null) {
                final TableViewState newState = new TableViewState(tableTopComponent.getCurrentState());
                
                newState.setElementType(tableTopComponent.getCurrentState().getElementType() == GraphElementType.TRANSACTION
                        ? GraphElementType.VERTEX : GraphElementType.TRANSACTION);
                
                elementTypeButton.setGraphic(
                        newState.getElementType() == GraphElementType.TRANSACTION
                            ? TRANSACTION_ICON : VERTEX_ICON
                );
                
                PluginExecution.withPlugin(
                        new UpdateStatePlugin(newState)
                ).executeLater(tableTopComponent.getCurrentGraph());
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
                getElementTypeButton().setGraphic(state.getElementType() == GraphElementType.TRANSACTION
                        ? TRANSACTION_ICON : VERTEX_ICON);
            }
        });
    }
    
    /**
     * Gets the tool bar UI component that will be added to the table and contains
     * all the other UI buttons etc that are created and added to it.
     *
     * @return the table tool bar
     */
    public ToolBar getToolbar() {
        return toolbar;
    }

    /**
     * Gets the column visibility button on the tool bar. This button will, when clicked
     * generate a context menu with more options to select from.
     *
     * @return the column visibility button on the tool bar
     * @see ColumnVisibilityContextMenu
     */
    public Button getColumnVisibilityButton() {
        return columnVisibilityButton;
    }

    /**
     * Gets the "Element Type" button from the tool bar that toggles
     * between the currently displayed element types.
     * <p/>
     * The table displays either nodes or edges. This button is what toggles
     * between the two.
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
     * When "Selected Only Mode" is <b>ON</b>, selection in the table does not effect
     * selection in the graph and vice versa. When "Selected Only Mode" is <b>OFF</b>
     * then selection in the table effects selection in the graph and vice versa.
     *
     * @return the "Selected Only Mode" toggle button
     */
    public ToggleButton getSelectedOnlyButton() {
        return selectedOnlyButton;
    }

    /**
     * Gets the {@link ExportMenu} associated to the tool bar. The export menu
     * will allow for the export of the table data into different formats.
     *
     * @return the export menu on the tool bar
     * @see ExportMenu
     */
    public ExportMenu getExportMenu() {
        return exportMenu;
    }

    /**
     * Gets the {@link CopyMenu} associated to the tool bar. The copy menu will
     * allow for the loading of CSV table data into the OS clipboard.
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
        final CopyMenu newCopyMenu = new CopyMenu(table, tableService.getPagination());
        newCopyMenu.init();
        
        return newCopyMenu;
    }
    
    /**
     * Creates a new {@link ExportMenu} and initializes it.
     *
     * @return the new export menu
     */
    protected ExportMenu createExportMenu() {
        final ExportMenu newExportMenu = new ExportMenu(tableTopComponent, table, tableService);
        newExportMenu.init();
        
        return newExportMenu;
    }
    
    /**
     * Creates a new {@link PreferencesMenu} and initializes it.
     *
     * @return the new preferences menu
     */
    protected PreferencesMenu createPreferencesMenu() {
        final PreferencesMenu newPreferencesMenu = new PreferencesMenu(tableTopComponent,
                tablePane, table, tableService);
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
     * Creates a new {@link ColumnVisibilityContextMenu} and initializes it.
     *
     * @return the new column visibility context menu
     */
    protected ColumnVisibilityContextMenu createColumnVisibilityContextMenu() {
        final ColumnVisibilityContextMenu newColumnVisibilityMenu
                    = new ColumnVisibilityContextMenu(tableTopComponent, table, tableService);
        newColumnVisibilityMenu.init();
        
        return newColumnVisibilityMenu;
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