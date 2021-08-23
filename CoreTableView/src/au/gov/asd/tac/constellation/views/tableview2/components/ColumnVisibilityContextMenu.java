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

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.utilities.datastructure.ThreeTuple;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.tableview2.TableViewTopComponent;
import au.gov.asd.tac.constellation.views.tableview2.service.TableService;
import au.gov.asd.tac.constellation.views.tableview2.UpdateMethod;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

/**
 *
 * @author formalhaunt
 */
public class ColumnVisibilityContextMenu {
    private static final String SPLIT_SOURCE = "Source";
    private static final String SPLIT_DESTINATION = "Destination";
    private static final String SPLIT_TRANSACTION = "Transaction";
    
    private static final String ALL_COLUMNS = "Show All Columns";
    private static final String DEFAULT_COLUMNS = "Show Default Columns";
    private static final String KEY_COLUMNS = "Show Key Columns";
    private static final String NO_COLUMNS = "Show No Columns";
    
    private static final String FILTER_CAPTION = "Filter:";
    
    private static final ImageView SPLIT_SOURCE_ICON = new ImageView(UserInterfaceIconProvider.MENU.buildImage(16));
    private static final ImageView SPLIT_DESTINATION_ICON = new ImageView(UserInterfaceIconProvider.MENU.buildImage(16));
    private static final ImageView SPLIT_TRANSACTION_ICON = new ImageView(UserInterfaceIconProvider.MENU.buildImage(16));
    
    private static final int WIDTH = 120;
    
    private final TableViewTopComponent parent;
    private final Table table;
    
    private final TableService tableService;
    
    private ContextMenu contextMenu = new ContextMenu();
    
    private MenuButton splitSourceButton;
    private MenuButton splitDestinationButton;
    private MenuButton splitTransactionButton;
    
    private CustomMenuItem allColumns;
    private CustomMenuItem defaultColumns;
    private CustomMenuItem keyColumns;
    private CustomMenuItem noColumns;
    
    private CustomMenuItem columnFilterSource;
    private CustomMenuItem columnFilterDestination;
    private CustomMenuItem columnFilterTransaction;
    
    public ColumnVisibilityContextMenu(final TableViewTopComponent parent,
                                       final Table table,
                                       final TableService tableService) {
        this.parent = parent;
        this.table = table;
        this.tableService = tableService;
    }
    
    public void init() {
        contextMenu = new ContextMenu();
        
        splitSourceButton = createMenuButton(SPLIT_SOURCE, SPLIT_SOURCE_ICON);
        splitDestinationButton = createMenuButton(SPLIT_DESTINATION, SPLIT_DESTINATION_ICON);
        splitTransactionButton = createMenuButton(SPLIT_TRANSACTION, SPLIT_TRANSACTION_ICON);
        
        allColumns = createCustomMenuItem(ALL_COLUMNS, false, e -> {
            tableService.updateVisibleColumns(parent.getCurrentGraph(), parent.getCurrentState(),
                    extractColumnAttributes(table.getColumnIndex()), UpdateMethod.REPLACE);
            e.consume();
        });
        
        defaultColumns = createCustomMenuItem(DEFAULT_COLUMNS, false, e -> {
            tableService.updateVisibleColumns(parent.getCurrentGraph(), parent.getCurrentState(),
                    extractColumnAttributes(table.getColumnIndex().stream()
                            .filter(columnTuple -> Character.isUpperCase(columnTuple.getSecond().getName().charAt(0)))
                            .collect(Collectors.toList())), UpdateMethod.REPLACE);
            e.consume();
        });
        
        keyColumns = createCustomMenuItem(KEY_COLUMNS, false, e -> {
            if (parent.getCurrentGraph() != null) {
                final Set<GraphAttribute> keyAttributes = new HashSet<>();
                final ReadableGraph readableGraph = parent.getCurrentGraph().getReadableGraph();
                try {
                    final int[] vertexKeys = readableGraph.getPrimaryKey(GraphElementType.VERTEX);
                    for (final int vertexKey : vertexKeys) {
                        keyAttributes.add(new GraphAttribute(readableGraph, vertexKey));
                    }
                    final int[] transactionKeys = readableGraph.getPrimaryKey(GraphElementType.TRANSACTION);
                    for (final int transactionKey : transactionKeys) {
                        keyAttributes.add(new GraphAttribute(readableGraph, transactionKey));
                    }
                } finally {
                    readableGraph.release();
                }
                tableService.updateVisibleColumns(parent.getCurrentGraph(), parent.getCurrentState(),
                        extractColumnAttributes(table.getColumnIndex().stream()
                                .filter(columnTuple -> keyAttributes.stream()
                                .anyMatch(keyAttribute -> keyAttribute.equals(columnTuple.getSecond())))
                                .collect(Collectors.toList())), UpdateMethod.REPLACE);
                e.consume();
            }
        });
        
        noColumns = createCustomMenuItem(NO_COLUMNS, false, e -> {
            table.getColumnIndex().forEach(columnTuple -> {
                columnTuple.getThird().setVisible(false);
            });
            tableService.updateVisibleColumns(parent.getCurrentGraph(), parent.getCurrentState(),
                    Collections.emptyList(), UpdateMethod.REPLACE);
            e.consume();
        });
        
        contextMenu.getItems().addAll(allColumns, defaultColumns, keyColumns,
                noColumns, new SeparatorMenuItem());
        
        final List<CustomMenuItem> columnCheckboxesSource = new ArrayList<>();
        final List<CustomMenuItem> columnCheckboxesDestination = new ArrayList<>();
        final List<CustomMenuItem> columnCheckboxesTransaction = new ArrayList<>();
        
        columnFilterSource = createColumnFilterMenuItem(columnCheckboxesSource);
        columnFilterDestination = createColumnFilterMenuItem(columnCheckboxesDestination);
        columnFilterTransaction = createColumnFilterMenuItem(columnCheckboxesTransaction);

        splitSourceButton.getItems().add(columnFilterSource);
        splitDestinationButton.getItems().add(columnFilterDestination);
        splitTransactionButton.getItems().add(columnFilterTransaction);
        
        table.getColumnIndex().forEach(columnTuple -> {
            final String columnHeading = columnTuple.getFirst();
            if (null != columnHeading) {
                switch (columnHeading) {
                    case GraphRecordStoreUtilities.SOURCE:
                        columnCheckboxesSource.add(getColumnVisibility(columnTuple));
                        break;
                    case GraphRecordStoreUtilities.DESTINATION:
                        columnCheckboxesDestination.add(getColumnVisibility(columnTuple));
                        break;
                    case GraphRecordStoreUtilities.TRANSACTION:
                        columnCheckboxesTransaction.add(getColumnVisibility(columnTuple));
                        break;
                    default:
                        break;
                }
            }
        });
        
        addCustomMenu(splitSourceButton, columnCheckboxesSource);
        addCustomMenu(splitDestinationButton, columnCheckboxesDestination);
        addCustomMenu(splitTransactionButton, columnCheckboxesTransaction);
    }
    
    public ContextMenu getContextMenu() {
        return contextMenu;
    }
    
    private List<Tuple<String, Attribute>> extractColumnAttributes(final ThreeTuple<String, Attribute, TableColumn<ObservableList<String>, String>> column) {
        return extractColumnAttributes(List.of(column));
    }
    
    private List<Tuple<String, Attribute>> extractColumnAttributes(final List<ThreeTuple<String, Attribute, TableColumn<ObservableList<String>, String>>> columns) {
        return columns.stream()
                .map(columnTuple
                        -> Tuple.create(
                                columnTuple.getFirst(),
                                columnTuple.getSecond())
                )
                .collect(Collectors.toList());
    }
    
    private CustomMenuItem getColumnVisibility(final ThreeTuple<String, Attribute, TableColumn<ObservableList<String>, String>> columnTuple) {
        final CheckBox columnCheckbox = new CheckBox(columnTuple.getThird().getText());
        columnCheckbox.selectedProperty().bindBidirectional(columnTuple.getThird().visibleProperty());
        
        columnCheckbox.setOnAction(e -> {
            tableService.updateVisibleColumns(
                    parent.getCurrentGraph(),
                    parent.getCurrentState(),
                    extractColumnAttributes(columnTuple),
                    ((CheckBox) e.getSource()).isSelected() ? UpdateMethod.ADD : UpdateMethod.REMOVE
            );
            e.consume();
        });

        final CustomMenuItem columnVisibility = new CustomMenuItem(columnCheckbox);
        columnVisibility.setHideOnClick(false);
        columnVisibility.setId(columnTuple.getThird().getText());
        
        return columnVisibility;
    }
    
    private void addCustomMenu(final MenuButton button,
                               final List<CustomMenuItem> columnCheckboxes) {
        if (!columnCheckboxes.isEmpty()) {
            button.getItems().addAll(columnCheckboxes);
            
            final CustomMenuItem menuItem = new CustomMenuItem(button);
            menuItem.setHideOnClick(false);
            
            contextMenu.getItems().add(menuItem);
        }
    }
    
    private CustomMenuItem createColumnFilterMenuItem(final List<CustomMenuItem> columnCheckboxes) {
        final Label label = new Label(FILTER_CAPTION);
        final TextField textField = new TextField();
        final HBox box = new HBox();
        
        box.getChildren().addAll(label, textField);
        
        final CustomMenuItem menuItem = new CustomMenuItem(box);
        menuItem.setHideOnClick(false);
        
        textField.setOnKeyReleased(
                new ColumnFilterKeyReleasedEventHandler(textField, columnCheckboxes));
        
        return menuItem;
    }
    
    private CustomMenuItem createCustomMenuItem(final String title,
                                                final boolean hideOnclick,
                                                final EventHandler<ActionEvent> handler) {
        final CustomMenuItem menuItem = new CustomMenuItem(new Label(title));
        
        menuItem.setHideOnClick(hideOnclick);
        menuItem.setOnAction(handler);
        
        return menuItem;
    }
    
    private MenuButton createMenuButton(final String title, final ImageView icon) {
        final MenuButton button = new MenuButton();
        
        button.setText(title);
        button.setGraphic(icon);
        button.setMaxWidth(WIDTH);
        button.setPopupSide(Side.RIGHT);
        
        return button;
    }
    
    class ColumnFilterKeyReleasedEventHandler implements EventHandler<KeyEvent> {
        private final TextField textField;
        private final List<CustomMenuItem> columnCheckboxes;
        
        public ColumnFilterKeyReleasedEventHandler(final TextField textField,
                                                   final List<CustomMenuItem> columnCheckboxes) {
            this.textField = textField;
            this.columnCheckboxes = columnCheckboxes;
        }
        
        @Override
        public void handle(KeyEvent event) {
            final String filterTerm = textField.getText().toLowerCase().trim();
            columnCheckboxes.forEach(item -> {
                final String columnName = item.getId().toLowerCase();
                item.setVisible(filterTerm.isBlank() || columnName.contains(filterTerm));
            });
            event.consume();
        }
        
    }
}
