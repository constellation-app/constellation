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
import au.gov.asd.tac.constellation.views.tableview2.TableViewUtilities;
import au.gov.asd.tac.constellation.views.tableview2.service.PreferenceService;
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
 *
 * @author formalhaunt
 */
public class TableToolbar {
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
    private final PreferenceService preferenceService;
    
    public TableToolbar(final TableViewTopComponent tableTopComponent,
                        final TableViewPane tablePane,
                        final Table table,
                        final TableService tableService,
                        final PreferenceService preferenceService) {
        this.tableTopComponent = tableTopComponent;
        this.tablePane = tablePane;
        this.table = table;
        this.tableService = tableService;
        this.preferenceService = preferenceService;
    }
    
    public void init() {
        columnVisibilityButton = createButton(COLUMNS_ICON, COLUMN_VISIBILITY, e -> {
            final ColumnVisibilityContextMenu columnVisibilityMenu
                    = new ColumnVisibilityContextMenu(tableTopComponent, table, tableService);
            columnVisibilityMenu.init();
            columnVisibilityMenu.getContextMenu()
                    .show(columnVisibilityButton, Side.RIGHT, 0, 0);
            
            e.consume();
        });
        
        selectedOnlyButton = createToggleButton(ALL_VISIBLE_ICON, SELECTED_ONLY, e -> {
            selectedOnlyButton.setGraphic(
                    selectedOnlyButton.getGraphic().equals(SELECTED_VISIBLE_ICON) 
                            ? ALL_VISIBLE_ICON : SELECTED_VISIBLE_ICON
            );
            if (tableTopComponent.getCurrentState() != null) {
                tableService.getSelectedOnlySelectedRows().clear();
                
                final TableViewState newState = new TableViewState(tableTopComponent.getCurrentState());
                newState.setSelectedOnly(!tableTopComponent.getCurrentState().isSelectedOnly());
                
                PluginExecution.withPlugin(
                        new TableViewUtilities.UpdateStatePlugin(newState)
                ).executeLater(tableTopComponent.getCurrentGraph());
            }
            
            e.consume();
        });
        
        elementTypeButton = createButton(TRANSACTION_ICON, ELEMENT_TYPE, e -> {
            elementTypeButton.setGraphic(
                    elementTypeButton.getGraphic().equals(VERTEX_ICON) 
                            ? TRANSACTION_ICON : VERTEX_ICON
            );
            
            if (tableTopComponent.getCurrentState() != null) {
                final TableViewState newState = new TableViewState(tableTopComponent.getCurrentState());
                newState.setElementType(tableTopComponent.getCurrentState().getElementType() == GraphElementType.TRANSACTION
                        ? GraphElementType.VERTEX : GraphElementType.TRANSACTION);
                
                PluginExecution.withPlugin(
                        new TableViewUtilities.UpdateStatePlugin(newState)
                ).executeLater(tableTopComponent.getCurrentGraph());
            }
            
            e.consume();
        });
        
        // Copy Button Menu
        copyMenu = new CopyMenu(table, tableService.getPagination());
        copyMenu.init();
        
        // Export Button Menu
        exportMenu = new ExportMenu(tableTopComponent, table, tableService.getPagination(),
                preferenceService);
        exportMenu.init();
        
        // Preferences Button Menu
        preferencesMenu = new PreferencesMenu(tableTopComponent, tablePane, table,
                tableService, preferenceService);
        preferencesMenu.init();
        
        helpButton = createButton(HELP_ICON, HELP, e -> {
            new HelpCtx(TableViewTopComponent.class.getName()).display();
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
     * @param state the current table view state.
     */
    public void updateToolbar(final TableViewState state) {
        Platform.runLater(() -> {
            if (state != null) {
                selectedOnlyButton.setSelected(state.isSelectedOnly());
                selectedOnlyButton.setGraphic(state.isSelectedOnly()
                        ? SELECTED_VISIBLE_ICON : ALL_VISIBLE_ICON);
                elementTypeButton.setGraphic(state.getElementType() == GraphElementType.TRANSACTION
                        ? TRANSACTION_ICON : VERTEX_ICON);
            }
        });
    }
    
    public ToolBar getToolbar() {
        return toolbar;
    }

    public Button getColumnVisibilityButton() {
        return columnVisibilityButton;
    }

    public Button getElementTypeButton() {
        return elementTypeButton;
    }

    public Button getHelpButton() {
        return helpButton;
    }

    public ToggleButton getSelectedOnlyButton() {
        return selectedOnlyButton;
    }

    public ExportMenu getExportMenu() {
        return exportMenu;
    }

    public CopyMenu getCopyMenu() {
        return copyMenu;
    }

    public PreferencesMenu getPreferencesMenu() {
        return preferencesMenu;
    }
    
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