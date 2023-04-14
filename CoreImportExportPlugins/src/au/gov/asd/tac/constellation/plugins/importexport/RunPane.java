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
package au.gov.asd.tac.constellation.plugins.importexport;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.plugins.importexport.model.CellValue;
import au.gov.asd.tac.constellation.plugins.importexport.model.TableRow;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.apache.commons.lang3.StringUtils;

/**
 * A RunPane displays the UI necessary to allow the user to drag and drop
 * attributes onto columns in the table in order to specify which attribute
 * values should be extracted from which columns.
 *
 * @author sirius
 */
public final class RunPane extends BorderPane implements KeyListener {

    private static final int SCROLLPANE_HEIGHT = 450;
    private static final int SCROLLPANE_VIEW_WIDTH = 400;
    private static final int SCROLLPANE_VIEW_HEIGHT = 900;
    private static final int SAMPLEVIEW_HEIGHT = 250;
    private static final int SAMPLEVIEW_MIN_HEIGHT = 130;
    private static final int ATTRIBUTEPANE_PREF_WIDTH = 300;
    private static final int ATTRIBUTEPANE_MIN_WIDTH = 100;
    private static final Insets ATTIRUBTEPANE_PADDING = new Insets(5);
    private static final int ATTRIBUTE_GAP = 5;
    private static final int TABLECOLUMN_PREFWIDTH = 50;
    private static final String FILTER_STYLE = "-fx-background-color: black; -fx-text-fill: white;-fx-prompt-text-fill:grey;";
    private static final String FILTER_STYLE_ALERT = "-fx-background-color: red; -fx-text-fill: black;-fx-prompt-text-fill:grey;";

    private final ImportController importController;
    private final TableView<TableRow> sampleDataView = new TableView<>();
    private final AttributeList sourceVertexAttributeList;
    private final AttributeList destinationVertexAttributeList;
    private final AttributeList transactionAttributeList;
    private String paneName = "";

    private Point2D draggingOffset;
    private AttributeNode draggingAttributeNode;
    private ImportTableColumn mouseOverColumn;
    private Rectangle columnRectangle = new Rectangle();
    private int attributeCount = 0;

    private final TextField filterField;
    private static RowFilter rowFilter;
    private String filter = "";

    private final TextField attributeFilterTextField = new TextField();
    private final EasyGridPane attributePane;
    private static final double ATTRIBUTE_PADDING_HEIGHT = 19.75;

    // find a better way to determine the multiplier which
    // seems to be dependant on the number of columns and its width. So this
    // mechanism of blindly multiplying by 12.1 is not the ideal case because
    // it means longer headings get extra padding than they need and smaller
    // headings get just enough.
    private final static double COLUMN_WIDTH_MULTIPLIER = 12.1;

    private ObservableList<TableRow> currentRows = FXCollections.observableArrayList();
    private String[] currentColumnLabels = new String[0];

    private static final Image ADD_IMAGE = UserInterfaceIconProvider.ADD.buildImage(16, Color.BLACK);

    // made protected purely so that FilterStartUp load can trigger the process for this on startup
    // needs to declared CompletableFuture rather than simply Future so that we can call thenRun() later on
    protected static final CompletableFuture<Void> FILTER_LOAD;

    static {
        FILTER_LOAD = CompletableFuture.supplyAsync(RowFilter::new, Executors.newSingleThreadExecutor())
                .thenAccept(rf -> rowFilter = rf);
    }

    private class AttributeBox extends BorderPane {

        public AttributeBox(final String label, final AttributeList attributeList) {
            final BorderPane borderPane = new BorderPane();
            borderPane.setTop(attributeList);

            setMaxHeight(USE_PREF_SIZE);
            setMaxWidth(Double.MAX_VALUE);
            setCenter(borderPane);

            final Label heading = new Label(label);
            heading.setStyle("-fx-font-weight: bold;");

            BorderPane labelPane = new BorderPane();
            labelPane.setPadding(new Insets(1));
            labelPane.setMinWidth(0);
            labelPane.setLeft(heading);

            final Button button = new Button("", new ImageView(ADD_IMAGE));
            button.setOnAction(event -> {
                final NewAttributeDialog dialog = new NewAttributeDialog();
                dialog.setOkButtonAction(event2 -> {
                    dialog.hideDialog();
                    Attribute attribute = new NewAttribute(
                            attributeList.getAttributeType().getElementType(),
                            dialog.getType(),
                            dialog.getLabel(),
                            dialog.getDescription()
                    );

                    importController.createManualAttribute(attribute);
                });

                dialog.showDialog("New Attribute");
            });
            button.setTooltip(new Tooltip("Add a new " + attributeList.getAttributeType().getElementType() + " attribute"));
            labelPane.setRight(button);

            setTop(labelPane);
        }
    }

    public RunPane(final ImportController importController, final String displayText, final String paneName) {
        this.importController = importController;
        this.paneName = paneName;

        setMaxHeight(Double.MAX_VALUE);
        setMaxWidth(Double.MAX_VALUE);

        final VBox configBox = new VBox();
        VBox.setVgrow(configBox, Priority.ALWAYS);

        final ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(configBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        setCenter(scrollPane);

        filterField = new TextField();
        filterField.setFocusTraversable(false);
        filterField.setMinHeight(USE_PREF_SIZE);
        filterField.setStyle(FILTER_STYLE);
        filterField.textProperty().addListener((observable, oldValue, newValue) -> setFilterStyle(newValue));

        filterField.setPromptText("Currently unavailable. The filter will be ready to use shortly");
        FILTER_LOAD.thenRun(() -> filterField.setPromptText("Start typing to search, e.g. first_name==\"NICK\""));

        sampleDataView.setMinHeight(SAMPLEVIEW_MIN_HEIGHT);
        sampleDataView.setPrefHeight(SAMPLEVIEW_HEIGHT);
        sampleDataView.setMaxHeight(Double.MAX_VALUE);
        HBox.setHgrow(filterField, Priority.ALWAYS);

        final HBox filterBox = new HBox(new Label("Filter: "), filterField);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        final VBox tableBox = new VBox(filterBox, sampleDataView);
        VBox.setVgrow(sampleDataView, Priority.ALWAYS);

        configBox.getChildren().add(tableBox);

        // add a help place holder
        // TODO: make this text wrap
        final TextArea startupHelpText = new TextArea();
        startupHelpText.setText(displayText);
        startupHelpText.setStyle("-fx-text-fill: grey;");
        startupHelpText.setWrapText(true);
        startupHelpText.setEditable(false);

        sampleDataView.setPlaceholder(startupHelpText);

        sourceVertexAttributeList = new AttributeList(importController, this, AttributeType.SOURCE_VERTEX);
        destinationVertexAttributeList = new AttributeList(importController, this, AttributeType.DESTINATION_VERTEX);
        transactionAttributeList = new AttributeList(importController, this, AttributeType.TRANSACTION);

        final AttributeBox sourceVertexScrollPane = new AttributeBox("Source Node", sourceVertexAttributeList);
        final AttributeBox destinationVertexScrollPane = new AttributeBox("Destination Node", destinationVertexAttributeList);
        final AttributeBox transactionScrollPane = new AttributeBox("Transaction", transactionAttributeList);

        attributePane = new EasyGridPane();
        attributePane.setMaxWidth(Double.MAX_VALUE);
        attributePane.setPrefSize(ATTRIBUTEPANE_PREF_WIDTH, attributeCount * ATTRIBUTE_PADDING_HEIGHT);
        attributePane.setAlignment(Pos.TOP_CENTER);
        attributePane.setPadding(ATTIRUBTEPANE_PADDING);
        attributePane.setVgap(ATTRIBUTE_GAP);
        attributePane.setHgap(ATTRIBUTE_GAP);

        attributePane.addColumnConstraint(true, HPos.CENTER, Priority.ALWAYS, Double.MAX_VALUE, ATTRIBUTEPANE_MIN_WIDTH,
                USE_COMPUTED_SIZE, -1);
        attributePane.addColumnConstraint(true, HPos.CENTER, Priority.ALWAYS, Double.MAX_VALUE, ATTRIBUTEPANE_MIN_WIDTH,
                USE_COMPUTED_SIZE, -1);
        attributePane.addColumnConstraint(true, HPos.CENTER, Priority.ALWAYS, Double.MAX_VALUE, ATTRIBUTEPANE_MIN_WIDTH,
                USE_COMPUTED_SIZE, -1);
        attributePane.addRowConstraint(true, VPos.TOP, Priority.ALWAYS, Double.MAX_VALUE, ATTRIBUTEPANE_MIN_WIDTH,
                USE_COMPUTED_SIZE, -1);
        attributePane.addRow(0, sourceVertexScrollPane, transactionScrollPane, destinationVertexScrollPane);

        // A scroll pane to hold the attribute boxes
        final ScrollPane attributeScrollPane = new ScrollPane();
        attributeScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        attributeScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        attributeScrollPane.setMaxWidth(Double.MAX_VALUE);
        attributeScrollPane.setContent(attributePane);
        attributeScrollPane.setPrefViewportWidth(SCROLLPANE_VIEW_WIDTH);
        attributeScrollPane.setPrefViewportHeight(SCROLLPANE_VIEW_HEIGHT);
        attributeScrollPane.setFitToWidth(true);
        attributeScrollPane.setPrefHeight(SCROLLPANE_HEIGHT);

        final TitledPane titledAttributePane = new TitledPane("Attributes", attributeScrollPane);
        titledAttributePane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        VBox.setVgrow(titledAttributePane, Priority.ALWAYS);
        titledAttributePane.setMinSize(0, 0);

        attributeFilterTextField.setFocusTraversable(false);
        attributeFilterTextField.setMinHeight(USE_PREF_SIZE);
        attributeFilterTextField.setPromptText("Start typing to search attributes");
        attributeFilterTextField.setStyle(FILTER_STYLE);
        attributeFilterTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            importController.setAttributeFilter(attributeFilterTextField.getText());
            importController.setDestination(null);
        });

        HBox.setHgrow(attributeFilterTextField, Priority.ALWAYS);
        final HBox attributeFilterBox = new HBox(new Label("Attribute Filter: "), attributeFilterTextField);
        attributeFilterBox.setAlignment(Pos.CENTER_LEFT);

        configBox.getChildren().addAll(attributeFilterBox, titledAttributePane);
        configBox.onKeyPressedProperty().bind(attributePane.onKeyPressedProperty());

        columnRectangle.setStyle("-fx-fill: rgba(200, 200, 200, 0.3);");
        columnRectangle.setVisible(false);
        columnRectangle.setManaged(false);
        RunPane.this.getChildren().add(columnRectangle);

        setOnMouseDragged((final MouseEvent t) -> handleAttributeMoved(t.getSceneX(), t.getSceneY()));

        setOnMouseReleased((final MouseEvent t) -> {
            if (draggingAttributeNode != null) {
                if (mouseOverColumn == null) {
                    draggingAttributeNode.getAttributeList().addAttributeNode(draggingAttributeNode);
                } else {
                    // If the active column currently has an attribute node then return
                    // the attribute node to its list
                    final AttributeNode currentAttributeNode = mouseOverColumn.getAttributeNode();
                    if (currentAttributeNode != null) {
                        currentAttributeNode.getAttributeList().addAttributeNode(currentAttributeNode);
                    }
                    // Drop the AttributeNode onto the column.
                    mouseOverColumn.setAttributeNode(draggingAttributeNode);
                    draggingAttributeNode.setColumn(mouseOverColumn);
                    validate(mouseOverColumn);
                }

                columnRectangle.setVisible(false);
                draggingAttributeNode.setManaged(true);
                draggingAttributeNode = null;
                draggingOffset = null;
                mouseOverColumn = null;
            }
        });
    }

    private void setFilterStyle(final String value) {
        filterField.setStyle(setFilter(value) ? FILTER_STYLE : FILTER_STYLE_ALERT);
    }

    /**
     * Update name associated with this pane. This value is used in
     * ImportDefinition construction to identify the source of the
     * ImportDefinition - ultimately being used when performing import to
     * support an import status dialog.
     *
     * @param paneName Value to set paneName to.
     */
    public void setPaneName(String paneName) {
        this.paneName = paneName;
    }

    public Point2D getDraggingOffset() {
        return draggingOffset;
    }

    public void setDraggingOffset(Point2D draggingOffset) {
        this.draggingOffset = draggingOffset;
    }

    public AttributeNode getDraggingAttributeNode() {
        return draggingAttributeNode;
    }

    public void setDraggingAttributeNode(AttributeNode draggingAttributeNode) {
        this.draggingAttributeNode = draggingAttributeNode;
    }

    public void handleAttributeMoved(double sceneX, double sceneY) {
        if (draggingAttributeNode != null) {
            final Point2D location = sceneToLocal(sceneX, sceneY);

            double x = location.getX() - draggingOffset.getX();
            if (x < 0) {
                x = 0;
            }
            if (x > RunPane.this.getWidth() - draggingAttributeNode.getWidth()) {
                x = RunPane.this.getWidth() - draggingAttributeNode.getWidth();
            }

            double y = location.getY() - draggingOffset.getY();
            if (y < 0) {
                y = 0;
            }
            if (y > RunPane.this.getHeight() - draggingAttributeNode.getHeight()) {
                y = RunPane.this.getHeight() - draggingAttributeNode.getHeight();
            }

            draggingAttributeNode.setLayoutX(x);
            draggingAttributeNode.setLayoutY(y);

            final Point2D tableLocation = sampleDataView.sceneToLocal(sceneX, sceneY);
            mouseOverColumn = null;
            final double offset = getScrollbarOffset();
            final double totalWidth = getTotalWidth(tableLocation, offset);

            if (mouseOverColumn != null) {
                // Allow for the SplitPane left side inset+padding (1+1 hard-coded).
                final double edge = 2;
                columnRectangle.setLayoutX(edge + sampleDataView.getLayoutX() + totalWidth - mouseOverColumn.getWidth() - offset);
                columnRectangle.setLayoutY(sampleDataView.getLayoutY());
                columnRectangle.setWidth(mouseOverColumn.getWidth());
                columnRectangle.setHeight(sampleDataView.getHeight());
                columnRectangle.setVisible(true);
            } else {
                columnRectangle.setVisible(false);
            }
        }
    }

    private double getTotalWidth(final Point2D tableLocation, final double offset) {
        double totalWidth = 0;
        final double cellPadding = 0.5;
        if (tableLocation.getX() >= 0 && tableLocation.getX() <= sampleDataView.getWidth()
                && tableLocation.getY() >= 0 && tableLocation.getY() <= sampleDataView.getHeight()) {
            final double columnLocation = tableLocation.getX() + offset;
            for (final TableColumn<TableRow, ?> column : sampleDataView.getColumns()) {
                totalWidth += column.getWidth() + cellPadding;
                if (columnLocation < totalWidth) {
                    mouseOverColumn = (ImportTableColumn) column;
                    break;
                }
            }
        }
        return totalWidth;
    }

    private double getScrollbarOffset() {
        double offset = 0;
        final Set<Node> nodes = sampleDataView.lookupAll(".scroll-bar");
        for (final Node node : nodes) {
            if (node instanceof ScrollBar) {
                final ScrollBar scrollBar = (ScrollBar) node;
                if (scrollBar.getOrientation() == Orientation.HORIZONTAL) {
                    offset = scrollBar.getValue();
                    break;
                }
            }
        }
        return offset;
    }

    /**
     * Set this RunPane to display the specified column headers and sample data
     * rows.
     *
     * @param columnLabels Column header labels.
     * @param newRows Rows of sample data.
     */
    public void setSampleData(final String[] columnLabels, final ObservableList<TableRow> newRows) {

        // Save any attributes that have already been allocated to columns.
        final ObservableList<TableColumn<TableRow, ?>> tableColumns = sampleDataView.getColumns();
        final AttributeNode[] savedAttributeNodes = new AttributeNode[tableColumns.size()];
        for (int i = 0; i < savedAttributeNodes.length; i++) {
            final ImportTableColumn tableColumn = (ImportTableColumn) tableColumns.get(i);
            savedAttributeNodes[i] = tableColumn.getAttributeNode();
        }

        sampleDataView.getItems().clear();
        sampleDataView.getColumns().clear();

        int columnIndex = 0;
        for (final String columnLabel : columnLabels) {
            final ImportTableColumn column = new ImportTableColumn(columnLabel, columnIndex);
            column.setCellValueFactory((CellDataFeatures<TableRow, CellValue> p) -> p.getValue().getProperty(column.getColumnIndex()));
            column.setCellFactory((TableColumn<TableRow, CellValue> p) -> new ImportTableCell());

            if (columnIndex < savedAttributeNodes.length) {
                column.setAttributeNode(savedAttributeNodes[columnIndex]);
                column.validate(newRows);
            }

            // Show the column heading
            if (StringUtils.isBlank(columnLabel)) {
                column.setPrefWidth(TABLECOLUMN_PREFWIDTH);
            } else {
                column.setPrefWidth(columnLabel.length() * COLUMN_WIDTH_MULTIPLIER);
            }
            sampleDataView.getColumns().add(column);
            columnIndex++;
        }

        while (columnIndex < savedAttributeNodes.length) {
            if (savedAttributeNodes[columnIndex] != null) {
                savedAttributeNodes[columnIndex].getAttributeList().addAttributeNode(savedAttributeNodes[columnIndex]);
            }
            columnIndex++;
        }
        currentColumnLabels = columnLabels;
        if (rowFilter != null) {
            rowFilter.setColumns(currentColumnLabels);
        }
        currentRows = newRows;
        sampleDataView.setItems(currentRows);
        setFilterStyle(filter);
    }

    public void clearFilters() {
        filterField.setText("");
        attributeFilterTextField.clear();
    }

    public void deleteAttribute(final Attribute attribute) {
        if (attribute.getElementType() == GraphElementType.VERTEX) {
            sourceVertexAttributeList.deleteAttribute(attribute);
            destinationVertexAttributeList.deleteAttribute(attribute);
        } else {
            transactionAttributeList.deleteAttribute(attribute);
        }
    }

    public boolean validate(final ImportTableColumn column) {
        // If the validation fails (when the active column doesn't match the attribute node format), return it back to the list
        if (column != null && !column.validate(currentRows)) {
            if (draggingAttributeNode != null) {
                NotifyDisplayer.displayAlert("Delimited Importer", "Attribute mismatch", "Column " + column.getLabel()
                        + " attribute format. Try changing the format by right clicking the attribute.", Alert.AlertType.ERROR);
            }
            column.validate(currentRows);
        }

        return false;
    }

    private boolean setFilter(final String filter) {
        this.filter = filter;
        if (filter.isEmpty()) {
            currentRows.forEach(tableRow -> tableRow.setIncluded(true));
            return true;
        }

        if (rowFilter != null && rowFilter.setScript(filter)) {
            currentRows.forEach(tableRow -> tableRow.filter(rowFilter));
            return true;
        } else {
            currentRows.forEach(tableRow -> tableRow.setIncluded(false));
            return false;
        }
    }

    public ImportDefinition createDefinition(final int firstRow) {

        RowFilter rf = rowFilter;
        if (StringUtils.isBlank(filter)) {
            rf = null;
        } else {
            rf.setColumns(currentColumnLabels);
        }

        final ImportDefinition definition = new ImportDefinition(paneName, firstRow, rf);

        for (final TableColumn<TableRow, ?> column : sampleDataView.getColumns()) {
            final ImportTableColumn importTableColumn = (ImportTableColumn) column;
            final AttributeNode attributeNode = importTableColumn.getAttributeNode();
            if (attributeNode != null) {
                // We added an artificial column at the beginning of the table ("Row"),
                // so we need to subtract 1 to allow for that offset.
                final ImportAttributeDefinition attributeDefinition
                        = new ImportAttributeDefinition(importTableColumn.getColumnIndex() - 1,
                                attributeNode.getAttribute(), attributeNode.getTranslator(),
                                attributeNode.getDefaultValue(), attributeNode.getTranslatorParameters());
                definition.addDefinition(attributeNode.getAttributeList().getAttributeType(), attributeDefinition);
            }
        }

        sourceVertexAttributeList.createDefinition(definition);
        destinationVertexAttributeList.createDefinition(definition);
        transactionAttributeList.createDefinition(definition);

        return definition;
    }

    /**
     * Returns all attributes that have been allocated to a column in this run.
     *
     * @return all attributes that have been allocated to a column in this run.
     */
    public Collection<Attribute> getAllocatedAttributes() {
        final ObservableList<TableColumn<TableRow, ?>> columns = sampleDataView.getColumns();
        final List<Attribute> allocatedAttributes = new ArrayList<>(columns.size());
        for (final TableColumn<TableRow, ?> column : columns) {
            final ImportTableColumn importTableColumn = (ImportTableColumn) column;
            final AttributeNode attributeNode = importTableColumn.getAttributeNode();
            if (attributeNode != null) {
                allocatedAttributes.add(attributeNode.getAttribute());
            }
        }

        return allocatedAttributes;
    }

    public void setDisplayedAttributes(final Map<String, Attribute> vertexAttributes,
            final Map<String, Attribute> transactionAttributes, final Set<Integer> keys) {
        attributeCount = Math.max(vertexAttributes.size(), transactionAttributes.size());
        sourceVertexAttributeList.setDisplayedAttributes(vertexAttributes, keys);
        destinationVertexAttributeList.setDisplayedAttributes(vertexAttributes, keys);
        transactionAttributeList.setDisplayedAttributes(transactionAttributes, keys);
    }

    void update(final ImportDefinition impdef) {
        String script = impdef.getRowFilter().getScript();
        if (script == null) {
            script = "";
        }
        filterField.setText(script);
        setFilterStyle(script);

        updateColumns(impdef, sourceVertexAttributeList, AttributeType.SOURCE_VERTEX);
        updateColumns(impdef, destinationVertexAttributeList, AttributeType.DESTINATION_VERTEX);
        updateColumns(impdef, transactionAttributeList, AttributeType.TRANSACTION);
    }

    private void updateColumns(final ImportDefinition impdef, final AttributeList attrList, final AttributeType atype) {
        final ObservableList<TableColumn<TableRow, ?>> columns = sampleDataView.getColumns();
        final Map<String, ImportTableColumn> labelToColumn = new HashMap<>();
        columns.stream().forEach(column -> {
            final ImportTableColumn itc = (ImportTableColumn) column;
            labelToColumn.put(itc.getLabel(), itc);
        });

        final List<ImportAttributeDefinition> elementList = impdef.getDefinitions(atype);
        elementList.stream().forEach(iad -> {
            final String importLabel = iad.getColumnLabel();
            final ImportTableColumn column = labelToColumn.get(importLabel);

            final AttributeNode attrNode = attrList.getAttributeNode(iad.getAttribute().getName());
            if (attrNode != null) {
                // If the column is null then update the settings which will be
                // reflected on the attribute lists
                attrNode.setTranslator(iad.getTranslator(), iad.getParameters());
                attrNode.setDefaultValue(iad.getDefaultValue());

                if (column != null) {
                    // If the column is not null then assign the attribute to a
                    // column and validate the column
                    column.setAttributeNode(attrNode);
                    attrNode.setColumn(column);
                    attrList.getRunPane().validate(column);
                }
            }
        });
    }

    @Override
    public void keyTyped(final KeyEvent event) {
        // Intentionally blank
    }

    @Override
    public void keyReleased(final KeyEvent event) {
        // Intentionally blank
    }

    @Override
    public void keyPressed(final KeyEvent event) {
        // Intentionally blank
    }

    public void refreshDataView() {
        sampleDataView.refresh();
    }

    public void setAttributePaneHeight() {
        attributePane.setPrefSize(ATTRIBUTEPANE_PREF_WIDTH, attributeCount * ATTRIBUTE_PADDING_HEIGHT);
    }

    /**
     * Check whether this pane has queried data.
     */
    public boolean hasDataQueried() {
        return !currentRows.isEmpty();
    }

    public TableView<TableRow> getSampleDataView() {
        return sampleDataView;
    }
}
