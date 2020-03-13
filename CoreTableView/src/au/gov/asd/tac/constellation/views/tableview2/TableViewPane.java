/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.tableview2;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.attribute.interaction.AbstractAttributeInteraction;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.datastructure.ThreeTuple;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.views.tableview2.state.TableViewState;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
//import javafx.scene.text.Font;
//import javafx.tk.FontMetrics;
//import javafx.tk.Toolkit;
import javax.swing.SwingUtilities;
import org.controlsfx.control.table.TableFilter;

/**
 * Table View Pane.
 * 
 * TODO: some javafx classes no are longer supported, fix it.
 *
 * @author elnath
 * @author cygnus_x-1
 */
public final class TableViewPane extends BorderPane {

    private static final Logger LOGGER = Logger.getLogger(TableViewPane.class.getName());

    private static final Object LOCK = new Object();

    private static final String ALL_COLUMNS = "Show All Columns";
    private static final String DEFAULT_COLUMNS = "Show Default Columns";
    private static final String KEY_COLUMNS = "Show Key Columns";
    private static final String NO_COLUMNS = "Show No Columns";
    private static final String COLUMN_VISIBILITY = "Column Visibility";
    private static final String SELECTED_ONLY = "Selected Only";
    private static final String ELEMENT_TYPE = "Element Type";
    private static final String COPY_CELL = "Copy Cell";
    private static final String COPY_ROW = "Copy Row";
    private static final String COPY_COLUMN = "Copy Column";
    private static final String COPY_COLUMN_UNIQUE = "Copy Column (Unique)";
    private static final String COPY_TABLE = "Copy Table";
    private static final String COPY_TABLE_SELECTION = "Copy Table (Selection)";
    private static final String EXPORT_CSV = "Export to CSV";
    private static final String EXPORT_CSV_SELECTION = "Export to CSV (Selection)";
    private static final String EXPORT_XLSX = "Export to Excel";
    private static final String EXPORT_XLSX_SELECTION = "Export to Excel (Selection)";

    private static final ImageView COLUMNS_ICON = new ImageView(UserInterfaceIconProvider.COLUMNS.buildImage(16));
    private static final ImageView SELECTED_VISIBLE_ICON = new ImageView(UserInterfaceIconProvider.VISIBLE.buildImage(16, ConstellationColor.CHERRY.getJavaColor()));
    private static final ImageView ALL_VISIBLE_ICON = new ImageView(UserInterfaceIconProvider.VISIBLE.buildImage(16));
    private static final ImageView VERTEX_ICON = new ImageView(UserInterfaceIconProvider.NODES.buildImage(16));
    private static final ImageView TRANSACTION_ICON = new ImageView(UserInterfaceIconProvider.TRANSACTIONS.buildImage(16));
    private static final ImageView COPY_ICON = new ImageView(UserInterfaceIconProvider.COPY.buildImage(16));
    private static final ImageView EXPORT_ICON = new ImageView(UserInterfaceIconProvider.UPLOAD.buildImage(16));

    private static final int PAD = 20;
    private static final int WIDTH = 120;

    private final TableViewTopComponent parent;
    private final CopyOnWriteArrayList<ThreeTuple<String, Attribute, TableColumn<ObservableList<String>, String>>> columnIndex;
    private final Map<Integer, ObservableList<String>> elementIdToRowIndex;
    private final Map<ObservableList<String>, Integer> rowToElementIdIndex;
    private Change<? extends TableColumn<ObservableList<String>, ?>> lastChange;

    private final TableView<ObservableList<String>> table;
    private TableFilter<ObservableList<String>> filter;
    private final BorderPane progress;

    private Button columnVisibilityButton;
    private ToggleButton selectedOnlyButton;
    private Button elementTypeButton;
    private MenuButton copyButton;
    private MenuButton exportButton;

    private final ReadOnlyObjectProperty<ObservableList<String>> selectedProperty;
    private final ChangeListener<ObservableList<String>> tableSelectionListener;

    private enum UpdateMethod {
        ADD,
        REMOVE,
        REPLACE
    }

    public TableViewPane(final TableViewTopComponent parent) {
        this.parent = parent;
        this.columnIndex = new CopyOnWriteArrayList<>();
        this.elementIdToRowIndex = new HashMap<>();
        this.rowToElementIdIndex = new HashMap<>();
        this.lastChange = null;

        final ToolBar toolbar = initToolbar();
        setLeft(toolbar);

        this.table = new TableView<>();
        table.itemsProperty().addListener((v, o, n) -> table.refresh());
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.setPadding(new Insets(5));
        setCenter(table);

        // TODO: experiment with caching
        table.setCache(false);

        this.progress = new BorderPane();
        final ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(50, 50);
        progress.setCenter(progressIndicator);

        this.tableSelectionListener = (v, o, n) -> {
            if (parent.getCurrentState() != null && !parent.getCurrentState().isSelectedOnly()) {
                TableViewUtilities.copySelectionToGraph(table, rowToElementIdIndex,
                        parent.getCurrentState().getElementType(), parent.getCurrentGraph());
            }
        };
        this.selectedProperty = table.getSelectionModel().selectedItemProperty();
        selectedProperty.addListener(tableSelectionListener);
    }

    private ToolBar initToolbar() {
        this.columnVisibilityButton = new Button();
        columnVisibilityButton.setGraphic(COLUMNS_ICON);
        columnVisibilityButton.setMaxWidth(WIDTH);
        columnVisibilityButton.setPadding(new Insets(5));
        columnVisibilityButton.setTooltip(new Tooltip(COLUMN_VISIBILITY));
        columnVisibilityButton.setOnAction(e -> {
            final ContextMenu contextMenu = initColumnVisibilityContextMenu();
            contextMenu.show(columnVisibilityButton, Side.RIGHT, 0, 0);
            e.consume();
        });

        this.selectedOnlyButton = new ToggleButton();
        selectedOnlyButton.setGraphic(ALL_VISIBLE_ICON);
        selectedOnlyButton.setMaxWidth(WIDTH);
        selectedOnlyButton.setPadding(new Insets(5));
        selectedOnlyButton.setTooltip(new Tooltip(SELECTED_ONLY));
        selectedOnlyButton.setOnAction(e -> {
            selectedOnlyButton.setGraphic(selectedOnlyButton.getGraphic().equals(SELECTED_VISIBLE_ICON) ? ALL_VISIBLE_ICON : SELECTED_VISIBLE_ICON);
            if (parent.getCurrentState() != null) {
                final TableViewState newState = new TableViewState(parent.getCurrentState());
                newState.setSelectedOnly(!parent.getCurrentState().isSelectedOnly());
                PluginExecution.withPlugin(new TableViewUtilities.UpdateStatePlugin(newState)).executeLater(parent.getCurrentGraph());
            }
            e.consume();
        });

        this.elementTypeButton = new Button();
        elementTypeButton.setGraphic(TRANSACTION_ICON);
        elementTypeButton.setMaxWidth(WIDTH);
        elementTypeButton.setPadding(new Insets(5));
        elementTypeButton.setTooltip(new Tooltip(ELEMENT_TYPE));
        elementTypeButton.setOnAction(e -> {
            elementTypeButton.setGraphic(elementTypeButton.getGraphic().equals(VERTEX_ICON) ? TRANSACTION_ICON : VERTEX_ICON);
            if (parent.getCurrentState() != null) {
                final TableViewState newState = new TableViewState(parent.getCurrentState());
                newState.setElementType(parent.getCurrentState().getElementType() == GraphElementType.TRANSACTION
                        ? GraphElementType.VERTEX : GraphElementType.TRANSACTION);
                PluginExecution.withPlugin(new TableViewUtilities.UpdateStatePlugin(newState)).executeLater(parent.getCurrentGraph());
            }
            e.consume();
        });

        this.copyButton = new MenuButton();
        copyButton.setGraphic(COPY_ICON);
        copyButton.setMaxWidth(WIDTH);
        copyButton.setPopupSide(Side.RIGHT);
        final MenuItem copyTableMenu = new MenuItem(COPY_TABLE);
        copyTableMenu.setOnAction(e -> {
            final String data = TableViewUtilities.getTableData(table, false, false);
            TableViewUtilities.copyToClipboard(data);
            e.consume();
        });
        final MenuItem copyTableSelectionMenu = new MenuItem(COPY_TABLE_SELECTION);
        copyTableSelectionMenu.setOnAction(e -> {
            final String selectedData = TableViewUtilities.getTableData(table, false, true);
            TableViewUtilities.copyToClipboard(selectedData);
            e.consume();
        });
        copyButton.getItems().addAll(copyTableMenu, copyTableSelectionMenu);

        this.exportButton = new MenuButton();
        exportButton.setGraphic(EXPORT_ICON);
        exportButton.setMaxWidth(WIDTH);
        exportButton.setPopupSide(Side.RIGHT);
        final MenuItem exportCsvItem = new MenuItem(EXPORT_CSV);
        exportCsvItem.setOnAction(e -> {
            if (parent.getCurrentGraph() != null) {
                TableViewUtilities.exportToCsv(table, false);
            }
            e.consume();
        });
        final MenuItem exportCsvSelectionItem = new MenuItem(EXPORT_CSV_SELECTION);
        exportCsvSelectionItem.setOnAction(e -> {
            if (parent.getCurrentGraph() != null) {
                TableViewUtilities.exportToCsv(table, true);
            }
            e.consume();
        });
        final MenuItem exportExcelItem = new MenuItem(EXPORT_XLSX);
        exportExcelItem.setOnAction(e -> {
            if (parent.getCurrentGraph() != null) {
                TableViewUtilities.exportToExcel(table, false, parent.getCurrentGraph().getId());
            }
            e.consume();
        });
        final MenuItem exportExcelSelectionItem = new MenuItem(EXPORT_XLSX_SELECTION);
        exportExcelSelectionItem.setOnAction(e -> {
            if (parent.getCurrentGraph() != null) {
                TableViewUtilities.exportToExcel(table, true, parent.getCurrentGraph().getId());
            }
            e.consume();
        });
        exportButton.getItems().addAll(exportCsvItem, exportCsvSelectionItem,
                exportExcelItem, exportExcelSelectionItem);

        final ToolBar toolbar = new ToolBar(columnVisibilityButton, selectedOnlyButton,
                elementTypeButton, new Separator(), copyButton, exportButton);
        toolbar.setOrientation(Orientation.VERTICAL);
        toolbar.setPadding(new Insets(5));

        return toolbar;
    }

    private ContextMenu initColumnVisibilityContextMenu() {
        final ContextMenu cm = new ContextMenu();

        final CustomMenuItem allColumns = new CustomMenuItem(new Label(ALL_COLUMNS));
        allColumns.setHideOnClick(false);
        allColumns.setOnAction(e -> {
            updateVisibleColumns(parent.getCurrentGraph(), parent.getCurrentState(),
                    columnIndex, UpdateMethod.REPLACE);
            e.consume();
        });

        final CustomMenuItem defaultColumns = new CustomMenuItem(new Label(DEFAULT_COLUMNS));
        defaultColumns.setHideOnClick(false);
        defaultColumns.setOnAction(e -> {
            updateVisibleColumns(parent.getCurrentGraph(), parent.getCurrentState(),
                    columnIndex.stream()
                            .filter(columnTuple -> Character.isUpperCase(columnTuple.getSecond().getName().charAt(0)))
                            .collect(Collectors.toList()), UpdateMethod.REPLACE);
            e.consume();
        });

        final CustomMenuItem keyColumns = new CustomMenuItem(new Label(KEY_COLUMNS));
        keyColumns.setHideOnClick(false);
        keyColumns.setOnAction(e -> {
            final Set<GraphAttribute> keyAttributes = new HashSet<>();
            final ReadableGraph readableGraph = parent.getCurrentGraph().getReadableGraph();
            try {
                final int[] vertexKeys = readableGraph.getPrimaryKey(GraphElementType.VERTEX);
                for (int vertexKey : vertexKeys) {
                    keyAttributes.add(new GraphAttribute(readableGraph, vertexKey));
                }
                final int[] transactionKeys = readableGraph.getPrimaryKey(GraphElementType.TRANSACTION);
                for (int transactionKey : transactionKeys) {
                    keyAttributes.add(new GraphAttribute(readableGraph, transactionKey));
                }
            } finally {
                readableGraph.release();
            }
            updateVisibleColumns(parent.getCurrentGraph(), parent.getCurrentState(),
                    columnIndex.stream()
                            .filter(columnTuple -> keyAttributes.stream()
                            .anyMatch(keyAttribute -> keyAttribute.equals(columnTuple.getSecond())))
                            .collect(Collectors.toList()), UpdateMethod.REPLACE);
            e.consume();
        });

        final CustomMenuItem noColumns = new CustomMenuItem(new Label(NO_COLUMNS));
        noColumns.setHideOnClick(false);
        noColumns.setOnAction(e -> {
            columnIndex.forEach(columnTuple -> {
                columnTuple.getThird().setVisible(false);
            });
            updateVisibleColumns(parent.getCurrentGraph(), parent.getCurrentState(),
                    Collections.emptyList(), UpdateMethod.REPLACE);
            e.consume();
        });

        cm.getItems().addAll(allColumns, defaultColumns, keyColumns, noColumns, new SeparatorMenuItem());

        columnIndex.forEach(columnTuple -> {
            final CheckBox columnCheckbox = new CheckBox(columnTuple.getThird().getText());
            columnCheckbox.selectedProperty().bindBidirectional(columnTuple.getThird().visibleProperty());
            columnCheckbox.setOnAction(e -> {
                updateVisibleColumns(parent.getCurrentGraph(), parent.getCurrentState(), Arrays.asList(columnTuple),
                        ((CheckBox) e.getSource()).isSelected() ? UpdateMethod.ADD : UpdateMethod.REMOVE);
                e.consume();
            });

            final CustomMenuItem columnVisibility = new CustomMenuItem(columnCheckbox);
            columnVisibility.setHideOnClick(false);

            cm.getItems().add(columnVisibility);
        });

        return cm;
    }

    private void updateVisibleColumns(final Graph graph, final TableViewState state,
            final List<ThreeTuple<String, Attribute, TableColumn<ObservableList<String>, String>>> columns, final UpdateMethod updateState) {
        if (graph != null && state != null) {
            final TableViewState newState = new TableViewState(state);

            final List<Tuple<String, Attribute>> columnAttributes = new ArrayList<>();
            switch (updateState) {
                case ADD:
                    if (newState.getColumnAttributes() != null) {
                        columnAttributes.addAll(newState.getColumnAttributes());
                    }
                    columnAttributes.addAll(columns.stream()
                            .map(columnTuple -> Tuple.create(columnTuple.getFirst(), columnTuple.getSecond()))
                            .collect(Collectors.toList()));
                    break;
                case REMOVE:
                    if (newState.getColumnAttributes() != null) {
                        columnAttributes.addAll(newState.getColumnAttributes());
                    }
                    columnAttributes.removeAll(columns.stream()
                            .map(columnTuple -> Tuple.create(columnTuple.getFirst(), columnTuple.getSecond()))
                            .collect(Collectors.toList()));
                    break;
                case REPLACE:
                    columnAttributes.addAll(columns.stream()
                            .map(columnTuple -> Tuple.create(columnTuple.getFirst(), columnTuple.getSecond()))
                            .collect(Collectors.toList()));
                    break;
            }

            newState.setColumnAttributes(columnAttributes);
            PluginExecution.withPlugin(new TableViewUtilities.UpdateStatePlugin(newState)).executeLater(graph);
        }
    }

    private ContextMenu initRightClickContextMenu(final TableCell<ObservableList<String>, String> cell) {
        final ContextMenu cm = new ContextMenu();

        final MenuItem copyCell = new MenuItem(COPY_CELL);
        copyCell.setOnAction(e -> {
            final String cellData = cell.getItem();
            TableViewUtilities.copyToClipboard(cellData);
            e.consume();
        });

        final MenuItem copyRow = new MenuItem(COPY_ROW);
        copyRow.setOnAction(e -> {
            final String rowData = ((ObservableList<String>) cell.getTableRow().getItem()).stream()
                    .reduce((cell1, cell2) -> cell1 + SeparatorConstants.COMMA + cell2).get();
            TableViewUtilities.copyToClipboard(rowData);
            e.consume();
        });

        final MenuItem copyColumn = new MenuItem(COPY_COLUMN);
        copyColumn.setOnAction(e -> {
            final String columnData = table.getItems().stream()
                    .map(item -> cell.getTableColumn().getCellObservableValue(item).getValue())
                    .reduce((cell1, cell2) -> cell1 + SeparatorConstants.COMMA + cell2).get();
            TableViewUtilities.copyToClipboard(columnData);
            e.consume();
        });

        final MenuItem copyColumnUnique = new MenuItem(COPY_COLUMN_UNIQUE);
        copyColumnUnique.setOnAction(e -> {
            final String uniqueColumnData = table.getItems().stream()
                    .map(item -> cell.getTableColumn().getCellObservableValue(item).getValue())
                    .collect(Collectors.toSet()).stream()
                    .reduce((cell1, cell2) -> cell1 + SeparatorConstants.COMMA + cell2).get();
            TableViewUtilities.copyToClipboard(uniqueColumnData);
            e.consume();
        });

        cm.getItems().addAll(copyCell, copyRow, copyColumn, copyColumnUnique);

        return cm;
    }

    /**
     * Update the whole table using the graph.
     *
     * @param graph the graph to retrieve data from.
     * @param state the current table view state.
     */
    public void updateTable(final Graph graph, final TableViewState state) {
        final Thread thread = new Thread("Table View: Update Table") {
            @Override
            public void run() {
                updateToolbar(state);
                if (graph != null) {
                    updateColumns(graph, state);
                    updateData(graph, state);
                    updateSelection(graph, state);
                } else {
                    Platform.runLater(() -> {
                        table.getColumns().clear();
                    });
                }
            }
        };
        thread.start();
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

    /**
     * Update the columns in the table using the graph and state.
     * <p>
     * Note that column references are reused where possible to ensure certain
     * toolbar/menu operations to work correctly.
     * <p>
     * The entire method is synchronized so it should be thread safe and keeps
     * the locking logic simpler. Maybe this method could be broken out further.
     *
     * @param graph the graph to retrieve data from.
     * @param state the current table view state.
     */
    public void updateColumns(final Graph graph, final TableViewState state) {
        synchronized (LOCK) {
            if (graph != null && state != null) {

                if (Platform.isFxApplicationThread()) {
                    throw new IllegalStateException("Attempting to process on the JavaFX Application Thread");
                }

                if (SwingUtilities.isEventDispatchThread()) {
                    throw new IllegalStateException("Attempting to process on the EDT");
                }

                // clear current columnIndex, but cache the column objects for reuse
                final Map<String, TableColumn<ObservableList<String>, String>> columnReferenceMap = columnIndex.stream()
                        .collect(Collectors.toMap(columnTuple -> columnTuple.getThird().getText(),
                                c -> c.getThird(),
                                (e1, e2) -> e1
                        ));
                columnIndex.clear();

                // update columnIndex based on graph attributes
                final ReadableGraph readableGraph = graph.getReadableGraph();
                try {
                    final int sourceAttributeCount = readableGraph.getAttributeCount(GraphElementType.VERTEX);
                    for (int sourceAttributePosition = 0; sourceAttributePosition < sourceAttributeCount; sourceAttributePosition++) {
                        final int sourceAttributeId = readableGraph.getAttribute(GraphElementType.VERTEX, sourceAttributePosition);
                        final String sourceAttributeName = GraphRecordStoreUtilities.SOURCE + readableGraph.getAttributeName(sourceAttributeId);
                        final TableColumn<ObservableList<String>, String> column = columnReferenceMap.containsKey(sourceAttributeName)
                                ? columnReferenceMap.get(sourceAttributeName) : new TableColumn<>(sourceAttributeName);
                        columnIndex.add(ThreeTuple.create(GraphRecordStoreUtilities.SOURCE, new GraphAttribute(readableGraph, sourceAttributeId), column));
                    }

                    if (state.getElementType() == GraphElementType.TRANSACTION) {
                        final int transactionAttributeCount = readableGraph.getAttributeCount(GraphElementType.TRANSACTION);
                        for (int transactionAttributePosition = 0; transactionAttributePosition < transactionAttributeCount; transactionAttributePosition++) {
                            final int transactionAttributeId = readableGraph.getAttribute(GraphElementType.TRANSACTION, transactionAttributePosition);
                            final String transactionAttributeName = GraphRecordStoreUtilities.TRANSACTION + readableGraph.getAttributeName(transactionAttributeId);
                            final TableColumn<ObservableList<String>, String> column = columnReferenceMap.containsKey(transactionAttributeName)
                                    ? columnReferenceMap.get(transactionAttributeName) : new TableColumn<>(transactionAttributeName);
                            columnIndex.add(ThreeTuple.create(GraphRecordStoreUtilities.TRANSACTION, new GraphAttribute(readableGraph, transactionAttributeId), column));
                        }

                        final int destinationAttributeCount = readableGraph.getAttributeCount(GraphElementType.VERTEX);
                        for (int destinationAttributePosition = 0; destinationAttributePosition < destinationAttributeCount; destinationAttributePosition++) {
                            final int destinationAttributeId = readableGraph.getAttribute(GraphElementType.VERTEX, destinationAttributePosition);
                            final String destinationAttributeName = GraphRecordStoreUtilities.DESTINATION + readableGraph.getAttributeName(destinationAttributeId);
                            final TableColumn<ObservableList<String>, String> column = columnReferenceMap.containsKey(destinationAttributeName)
                                    ? columnReferenceMap.get(destinationAttributeName) : new TableColumn<>(destinationAttributeName);
                            columnIndex.add(ThreeTuple.create(GraphRecordStoreUtilities.DESTINATION, new GraphAttribute(readableGraph, destinationAttributeId), column));
                        }
                    }
                } finally {
                    readableGraph.release();
                }

                // if there are no visible columns specified, write the key columns to the state
                if (state.getColumnAttributes() == null) {
                    final ContextMenu columnVisibilityMenu = initColumnVisibilityContextMenu();
                    final MenuItem keyColumns = columnVisibilityMenu.getItems().get(2);
                    keyColumns.fire();
                    return;
                }

                // sort columns in columnIndex by state, prefix and attribute name
                columnIndex.sort((columnTuple1, columnTuple2) -> {
                    final int c1Index = state.getColumnAttributes().indexOf(Tuple.create(columnTuple1.getFirst(), columnTuple1.getSecond()));
                    final int c2Index = state.getColumnAttributes().indexOf(Tuple.create(columnTuple2.getFirst(), columnTuple2.getSecond()));
                    final String c1Type = columnTuple1.getFirst();
                    final String c2Type = columnTuple2.getFirst();

                    if (c1Index != -1 && c2Index != -1) {
                        return Integer.compare(c1Index, c2Index);
                    } else if (c1Index != -1 && c2Index == -1) {
                        return -1;
                    } else if (c1Index == -1 && c2Index != -1) {
                        return 1;
                    } else if (c1Type.equals(GraphRecordStoreUtilities.SOURCE) && c2Type.equals(GraphRecordStoreUtilities.TRANSACTION)
                            || c1Type.equals(GraphRecordStoreUtilities.SOURCE) && c2Type.equals(GraphRecordStoreUtilities.DESTINATION)
                            || c1Type.equals(GraphRecordStoreUtilities.TRANSACTION) && c2Type.equals(GraphRecordStoreUtilities.DESTINATION)) {
                        return -1;
                    } else if (c1Type.equals(GraphRecordStoreUtilities.DESTINATION) && c2Type.equals(GraphRecordStoreUtilities.TRANSACTION)
                            || c1Type.equals(GraphRecordStoreUtilities.DESTINATION) && c2Type.equals(GraphRecordStoreUtilities.SOURCE)
                            || c1Type.equals(GraphRecordStoreUtilities.TRANSACTION) && c2Type.equals(GraphRecordStoreUtilities.SOURCE)) {
                        return 1;
                    } else {
                        final String c1Name = columnTuple1.getSecond().getName();
                        final String c2Name = columnTuple2.getSecond().getName();
                        return c1Name.compareTo(c2Name);
                    }
                });

                // style and format columns in columnIndex
//                final Font defaultFont = Font.getDefault();
//                final FontMetrics fontMetrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(defaultFont);
                columnIndex.forEach(columnTuple -> {
                    final TableColumn<ObservableList<String>, String> column = columnTuple.getThird();

                    // set the columns widths based on the length of their text
//                    final String columnText = column.getText();
//                    final float prefWidth = columnText == null
//                            ? 0 : fontMetrics.computeStringWidth(columnText);
//                    column.setPrefWidth(prefWidth + PAD);

                    // assign cells to columns
                    column.setCellValueFactory(cellData -> {
                        final int cellIndex = table.getColumns().indexOf(cellData.getTableColumn());
                        if (cellIndex < cellData.getValue().size()) {
                            return new SimpleStringProperty(cellData.getValue().get(cellIndex));
                        } else {
                            return null;
                        }
                    });

                    // assign values and styles to cells
                    column.setCellFactory(cellColumn -> {
                        return new TableCell<ObservableList<String>, String>() {
                            @Override
                            public void updateItem(final String item, final boolean empty) {
                                super.updateItem(item, empty);
                                if (!empty) {
                                    // set text in cell and style if it is null
                                    this.getStyleClass().remove("null-value");
                                    if (item != null) {
                                        this.setText(item);
                                    } else {
                                        this.setText("<No Value>");
                                        this.getStyleClass().add("null-value");
                                    }

                                    // color cell based on the attribute it represents
                                    this.getStyleClass().remove("element-source");
                                    this.getStyleClass().remove("element-transaction");
                                    this.getStyleClass().remove("element-destination");
                                    final String columnPrefix = columnIndex.stream()
                                            .filter(columnTuple -> columnTuple.getThird().equals(cellColumn))
                                            .map(columnTuple -> columnTuple.getFirst())
                                            .findFirst().orElse("");
                                    switch (columnPrefix) {
                                        case GraphRecordStoreUtilities.SOURCE:
                                            this.getStyleClass().add("element-source");
                                            break;
                                        case GraphRecordStoreUtilities.TRANSACTION:
                                            this.getStyleClass().add("element-transaction");
                                            break;
                                        case GraphRecordStoreUtilities.DESTINATION:
                                            this.getStyleClass().add("element-destination");
                                            break;
                                    }

                                    // enable context menu on right-click
                                    this.setOnMouseClicked(me -> {
                                        if (me.getButton() == MouseButton.SECONDARY) {
                                            final ContextMenu contextMenu = initRightClickContextMenu(this);
                                            contextMenu.show(table, me.getScreenX(), me.getScreenY());
                                        }
                                    });
                                }
                            }
                        };
                    });
                });

                Platform.runLater(() -> {
                    selectedProperty.removeListener(tableSelectionListener);

                    columnReferenceMap.forEach((columnName, column) -> column.setGraphic(null));

                    // set column visibility in columnIndex based on the state
                    columnIndex.forEach(columnTuple -> {
                        columnTuple.getThird().setVisible(state.getColumnAttributes().stream()
                                .anyMatch(a -> a.getFirst().equals(columnTuple.getFirst())
                                && a.getSecond().equals(columnTuple.getSecond())));
                    });

                    // add columns to table
                    table.getColumns().clear();
                    table.getColumns().addAll(columnIndex.stream().map(t -> t.getThird()).collect(Collectors.toList()));

                    // sort data if the column ordering changes
                    table.getColumns().addListener((final Change<? extends TableColumn<ObservableList<String>, ?>> change) -> {
                        if (lastChange == null || !lastChange.equals(change)) {
                            while (change.next()) {
                                if (change.wasReplaced() && change.getRemovedSize() == change.getAddedSize()) {
                                    final List<Integer> newIndices = change.getRemoved().stream()
                                            .map(i -> change.getAddedSubList().indexOf(i))
                                            .collect(Collectors.toList());
                                    table.getItems().forEach(item -> {
                                        final List<String> copy = new ArrayList<>(item);
                                        for (int i = 0; i < copy.size(); i++) {
                                            final String element = copy.get(i);
                                            final int elementIndex = newIndices.get(i);
                                            item.set(elementIndex, element);
                                        }
                                    });

                                    final List<TableColumn<ObservableList<String>, String>> columnIndexColumns
                                            = columnIndex.stream()
                                                    .map(ci -> ci.getThird())
                                                    .collect(Collectors.toList());
                                    final List<ThreeTuple<String, Attribute, TableColumn<ObservableList<String>, String>>> orderedColumns
                                            = change.getAddedSubList().stream()
                                                    .map(c -> columnIndex.get(columnIndexColumns.indexOf(c)))
                                                    .filter(c -> (parent.getCurrentState().getColumnAttributes().contains(Tuple.create(c.getFirst(), c.getSecond()))))
                                                    .collect(Collectors.toList());
                                    updateVisibleColumns(parent.getCurrentGraph(), parent.getCurrentState(), orderedColumns, UpdateMethod.REPLACE);
                                }
                            }
                            lastChange = change;
                        }
                    });

                    selectedProperty.addListener(tableSelectionListener);
                });
            }
        }
    }

    /**
     * Update the data in the table using the graph and state.
     * <p>
     * The entire method is synchronized so it should be thread safe and keeps
     * the locking logic simpler. Maybe this method could be broken out further.
     *
     * @param graph the graph to retrieve data from.
     * @param state the current table view state.
     */
    public void updateData(final Graph graph, final TableViewState state) {
        synchronized (LOCK) {
            if (graph != null && state != null) {

                if (Platform.isFxApplicationThread()) {
                    throw new IllegalStateException("Attempting to process on the JavaFX Application Thread");
                }

                if (SwingUtilities.isEventDispatchThread()) {
                    throw new IllegalStateException("Attempting to process on the EDT");
                }

                // set progress indicator
                Platform.runLater(() -> {
                    setCenter(progress);
                });

                // update data on a new thread so as to not interrupt the progress indicator
                elementIdToRowIndex.clear();
                rowToElementIdIndex.clear();

                // build table data based on attribute values on the graph
                final List<ObservableList<String>> rows = new ArrayList<>();
                final ReadableGraph readableGraph = graph.getReadableGraph();
                try {
                    if (state.getElementType() == GraphElementType.TRANSACTION) {
                        final int selectedAttributeId = VisualConcept.TransactionAttribute.SELECTED.get(readableGraph);
                        final int transactionCount = readableGraph.getTransactionCount();
                        for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                            final int transactionId = readableGraph.getTransaction(transactionPosition);
                            final boolean isSelected = selectedAttributeId != Graph.NOT_FOUND ? readableGraph.getBooleanValue(selectedAttributeId, transactionId) : false;
                            if (!state.isSelectedOnly() || isSelected) {
                                final ObservableList<String> rowData = FXCollections.observableArrayList();
                                columnIndex.forEach(columnTuple -> {
                                    final int attributeId = readableGraph.getAttribute(columnTuple.getSecond().getElementType(), columnTuple.getSecond().getName());
                                    final AbstractAttributeInteraction<?> interaction = AbstractAttributeInteraction.getInteraction(columnTuple.getSecond().getAttributeType());
                                    final Object attributeValue;
                                    switch (columnTuple.getFirst()) {
                                        case GraphRecordStoreUtilities.SOURCE:
                                            final int sourceVertexId = readableGraph.getTransactionSourceVertex(transactionId);
                                            attributeValue = readableGraph.getObjectValue(attributeId, sourceVertexId);
                                            break;
                                        case GraphRecordStoreUtilities.TRANSACTION:
                                            attributeValue = readableGraph.getObjectValue(attributeId, transactionId);
                                            break;
                                        case GraphRecordStoreUtilities.DESTINATION:
                                            final int destinationVertexId = readableGraph.getTransactionDestinationVertex(transactionId);
                                            attributeValue = readableGraph.getObjectValue(attributeId, destinationVertexId);
                                            break;
                                        default:
                                            attributeValue = null;
                                    }
                                    final String displayableValue = interaction.getDisplayText(attributeValue);
                                    rowData.add(displayableValue);
                                });
                                elementIdToRowIndex.put(transactionId, rowData);
                                rowToElementIdIndex.put(rowData, transactionId);
                                rows.add(rowData);
                            }
                        }
                    } else {
                        final int selectedAttributeId = VisualConcept.VertexAttribute.SELECTED.get(readableGraph);
                        final int vertexCount = readableGraph.getVertexCount();
                        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
                            final int vertexId = readableGraph.getVertex(vertexPosition);
                            final boolean isSelected = selectedAttributeId != Graph.NOT_FOUND ? readableGraph.getBooleanValue(selectedAttributeId, vertexId) : false;
                            if (!state.isSelectedOnly() || isSelected) {
                                final ObservableList<String> rowData = FXCollections.observableArrayList();
                                columnIndex.forEach(columnTuple -> {
                                    final int attributeId = readableGraph.getAttribute(columnTuple.getSecond().getElementType(), columnTuple.getSecond().getName());
                                    final AbstractAttributeInteraction<?> interaction = AbstractAttributeInteraction.getInteraction(columnTuple.getSecond().getAttributeType());
                                    final Object attributeValue = readableGraph.getObjectValue(attributeId, vertexId);
                                    final String displayableValue = interaction.getDisplayText(attributeValue);
                                    rowData.add(displayableValue);
                                });
                                elementIdToRowIndex.put(vertexId, rowData);
                                rowToElementIdIndex.put(rowData, vertexId);
                                rows.add(rowData);
                            }
                        }
                    }
                } finally {
                    readableGraph.release();
                }

                final CountDownLatch updateDataLatch = new CountDownLatch(1);

                Platform.runLater(() -> {
                    selectedProperty.removeListener(tableSelectionListener);

                    // add table data to table
                    table.setItems(FXCollections.observableArrayList(rows));
                    setCenter(table);

                    // add user defined filter to the table
                    filter = TableFilter.forTableView(table).lazy(true).apply();
                    filter.setSearchStrategy((t, u) -> {
                        try {
                            return u.toLowerCase().startsWith(t.toLowerCase());
                        } catch (Exception ex) {
                            return false;
                        }
                    });
                    filter.getFilteredList().predicateProperty().addListener((v, o, n) -> table.refresh());
                    updateDataLatch.countDown();

                    selectedProperty.addListener(tableSelectionListener);
                });

                try {
                    updateDataLatch.await();
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.WARNING, "InterruptedException encountered while updating table data", ex);
                }
            }
        }
    }

    /**
     * Update the table selection using the graph and state.
     * <p>
     * The entire method is synchronized so it should be thread safe and keeps
     * the locking logic simpler. Maybe this method could be broken out further.
     *
     * @param graph the graph to read selection from.
     * @param state the current table view state.
     */
    public void updateSelection(final Graph graph, final TableViewState state) {
        synchronized (LOCK) {
            if (graph != null && state != null) {

                if (Platform.isFxApplicationThread()) {
                    throw new IllegalStateException("Attempting to process on the JavaFX Application Thread");
                }

                if (SwingUtilities.isEventDispatchThread()) {
                    throw new IllegalStateException("Attempting to process on the EDT");
                }

                // get graph selection
                if (!state.isSelectedOnly()) {
                    final List<Integer> selectedIds = new ArrayList<>();
                    final ReadableGraph readableGraph = graph.getReadableGraph();
                    try {
                        final boolean isVertex = state.getElementType() == GraphElementType.VERTEX;
                        final int selectedAttributeId = isVertex
                                ? VisualConcept.VertexAttribute.SELECTED.get(readableGraph)
                                : VisualConcept.TransactionAttribute.SELECTED.get(readableGraph);
                        final int elementCount = isVertex
                                ? readableGraph.getVertexCount()
                                : readableGraph.getTransactionCount();
                        for (int elementPosition = 0; elementPosition < elementCount; elementPosition++) {
                            final int elementId = isVertex
                                    ? readableGraph.getVertex(elementPosition)
                                    : readableGraph.getTransaction(elementPosition);
                            final boolean isSelected = selectedAttributeId != Graph.NOT_FOUND ? readableGraph.getBooleanValue(selectedAttributeId, elementId) : false;
                            if (isSelected) {
                                selectedIds.add(elementId);
                            }
                        }
                    } finally {
                        readableGraph.release();
                    }

                    // update table selection
                    final int[] selectedIndices = selectedIds.stream().map(id -> elementIdToRowIndex.get(id))
                            .map(row -> table.getItems().indexOf(row)).mapToInt(i -> i).toArray();

                    Platform.runLater(() -> {
                        selectedProperty.removeListener(tableSelectionListener);
                        table.getSelectionModel().clearSelection();
                        if (!selectedIds.isEmpty()) {
                            table.getSelectionModel().selectIndices(selectedIndices[0], selectedIndices);
                        }
                        selectedProperty.addListener(tableSelectionListener);
                    });
                }
            }
        }
    }
}
