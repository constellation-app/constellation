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
package au.gov.asd.tac.constellation.plugins.importexport.delimited;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.plugins.importexport.delimited.model.CellValue;
import au.gov.asd.tac.constellation.plugins.importexport.delimited.model.TableRow;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Callback;

/**
 * A RunPane displays the UI necessary to allow the user to drag and drop
 * attributes onto columns in the table in order to specify which attribute
 * values should be extracted from which columns.
 *
 * @author sirius
 */
public class RunPane extends BorderPane {

    private final ImportController importController;
    private final TableView<TableRow> sampleDataView = new TableView<>();
    private final AttributeList sourceVertexAttributeList, destinationVertexAttributeList, transactionAttributeList;
    public Point2D draggingOffset;
    public AttributeNode draggingAttributeNode;
    private ImportTableColumn mouseOverColumn = null;
    private Rectangle columnRectangle = new Rectangle();

    final TextField filterField;
    private final RowFilter rowFilter = new RowFilter();
    private String filter = "";

    private ObservableList<TableRow> currentRows = FXCollections.observableArrayList();
    private String[] currentColumnLabels = new String[0];

    private static final Image ADD_IMAGE = UserInterfaceIconProvider.ADD.buildImage(16, Color.BLACK);

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
            button.setOnAction((ActionEvent event) -> {
                Attribute attribute = importController.showNewAttributeDialog(attributeList.getAttributeType().getElementType());
                if (attribute != null) {
                    importController.createManualAttribute(attribute);
                }
            });
            button.setTooltip(new Tooltip("Add a new " + attributeList.getAttributeType().getElementType() + " attribute"));
            labelPane.setRight(button);

            setTop(labelPane);
        }
    }

    public RunPane(final ImportController importController) {
        this.importController = importController;

        setMaxHeight(Double.MAX_VALUE);
        setMaxWidth(Double.MAX_VALUE);

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setDividerPositions(0.5);
        setCenter(splitPane);

//        addColumnConstraint(true, HPos.CENTER, Priority.ALWAYS, Double.MAX_VALUE, 300, USE_COMPUTED_SIZE, -1);
//        addRowConstraint(true, VPos.TOP, Priority.ALWAYS, Double.MAX_VALUE, 300, USE_COMPUTED_SIZE, -1);
//        addRowConstraint(true, VPos.TOP, Priority.NEVER, Double.MAX_VALUE, 300, USE_COMPUTED_SIZE, -1);
        filterField = new TextField();
        filterField.setMinHeight(USE_PREF_SIZE);
        filterField.setPromptText("Filter");
        filterField.textProperty().addListener((ObservableValue<? extends String> ov, String oldFilter, String newFilter) -> {
            if (setFilter(newFilter)) {
                filterField.setStyle("-fx-background-color: white;");
            } else {
                filterField.setStyle("-fx-background-color: red;");
            }
        });

        sampleDataView.setMinHeight(100);
        sampleDataView.setPrefHeight(300);
        sampleDataView.setPrefWidth(750);
        sampleDataView.setMaxHeight(Double.MAX_VALUE);

        VBox tableBox = new VBox();
        VBox.setVgrow(sampleDataView, Priority.ALWAYS);
        tableBox.getChildren().addAll(filterField, sampleDataView);

        splitPane.getItems().add(tableBox);

        // add a help place holder
        Text startupHelpText = new Text();
        startupHelpText.setText("1. Click on the green plus icon to add files.\n"
                + "2. Select your destination graph.\n"
                + "3. Drag and drop attributes from the bottom pane onto your columns.\n"
                + "4. Right click an attribute for more options.\n"
                + "5. Click on the Import button to import the data to your destination graph.\n"
                + "6. Save your configuration using Options -> Save.\n\n"
                + "HINT: See all supported attributes using Options -> Show all schema attributes\n"
                + "HINT: Hover over the attribute name for a tooltip.");
        startupHelpText.setStyle("-fx-font-size: 14pt;-fx-fill: grey;");
        sampleDataView.setPlaceholder(startupHelpText);

        sourceVertexAttributeList = new AttributeList(importController, this, AttributeType.SOURCE_VERTEX);
        destinationVertexAttributeList = new AttributeList(importController, this, AttributeType.DESTINATION_VERTEX);
        transactionAttributeList = new AttributeList(importController, this, AttributeType.TRANSACTION);

        AttributeBox sourceVertexScrollPane = new AttributeBox("Source Node Attributes ", sourceVertexAttributeList);
        AttributeBox destinationVertexScrollPane = new AttributeBox("Destination Node Attributes ", destinationVertexAttributeList);
        AttributeBox transactionScrollPane = new AttributeBox("Transaction Attributes ", transactionAttributeList);

        EasyGridPane attributePane = new EasyGridPane();
        attributePane.setMaxWidth(Double.MAX_VALUE);

        attributePane.addColumnConstraint(true, HPos.CENTER, Priority.ALWAYS, Double.MAX_VALUE, 100, USE_COMPUTED_SIZE, -1);
        attributePane.addColumnConstraint(true, HPos.CENTER, Priority.ALWAYS, Double.MAX_VALUE, 100, USE_COMPUTED_SIZE, -1);
        attributePane.addColumnConstraint(true, HPos.CENTER, Priority.ALWAYS, Double.MAX_VALUE, 100, USE_COMPUTED_SIZE, -1);
        attributePane.addRowConstraint(true, VPos.TOP, Priority.ALWAYS, Double.MAX_VALUE, 100, USE_COMPUTED_SIZE, -1);

        attributePane.addRow(0, sourceVertexScrollPane, destinationVertexScrollPane, transactionScrollPane);

        attributePane.setPadding(new Insets(5));
        attributePane.setVgap(5);
        attributePane.setHgap(5);
        attributePane.setAlignment(Pos.TOP_LEFT);

        // A scroll pane to hold the attribute boxes
        final ScrollPane attributeScrollPane = new ScrollPane();
        attributeScrollPane.setContent(attributePane);
        attributeScrollPane.setMaxWidth(Double.MAX_VALUE);
        attributeScrollPane.setPrefHeight(350);
        attributeScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        attributeScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        splitPane.getItems().add(attributeScrollPane);

        columnRectangle.setStyle("-fx-fill: rgba(200, 200, 200, 0.3);");
        columnRectangle.setVisible(false);
        columnRectangle.setManaged(false);
        getChildren().add(columnRectangle);

        setOnMouseDragged((final MouseEvent t) -> {
            handleAttributeMoved(t.getSceneX(), t.getSceneY());
        });

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

    public void handleAttributeMoved(double sceneX, double sceneY) {
        if (draggingAttributeNode != null) {
            Point2D location = sceneToLocal(sceneX, sceneY);

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

            Point2D tableLocation = sampleDataView.sceneToLocal(sceneX, sceneY);

            double offset = 0;
            Set<Node> nodes = sampleDataView.lookupAll(".scroll-bar");
            for (Node node : nodes) {
                if (node instanceof ScrollBar) {
//                    ScrollBarSkin skin = (ScrollBarSkin) node;
//                    Skinnable skinnable = skin.getSkinnable();
                    ScrollBar scrollBar = (ScrollBar) node;
                    if (scrollBar.getOrientation() == Orientation.HORIZONTAL) {
                        offset = scrollBar.getValue();
                        break;
                    }
                }
            }

            double totalWidth = 0;
            mouseOverColumn = null;

            final double cellPadding = 0.5; // ?
            if (tableLocation.getX() >= 0 && tableLocation.getX() <= sampleDataView.getWidth() && tableLocation.getY() >= 0 && tableLocation.getY() <= sampleDataView.getHeight()) {
                double columnLocation = tableLocation.getX() + offset;
                for (TableColumn<TableRow, ?> column : sampleDataView.getColumns()) {
                    totalWidth += column.getWidth() + cellPadding;
                    if (columnLocation < totalWidth) {
                        mouseOverColumn = (ImportTableColumn) column;
                        break;
                    }
                }
            }

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
//        sampleDataView.getColumns().clear();

        int columnIndex = 0;
        for (final String columnLabel : columnLabels) {
            final ImportTableColumn column = new ImportTableColumn(columnLabel, columnIndex);
            column.setCellValueFactory(new Callback<CellDataFeatures<TableRow, CellValue>, ObservableValue<CellValue>>() {
                @Override
                public ObservableValue<CellValue> call(CellDataFeatures<TableRow, CellValue> p) {
                    return p.getValue().getProperty(column.getColumnIndex());
                }
            });
            column.setCellFactory(new Callback<TableColumn<TableRow, CellValue>, TableCell<TableRow, CellValue>>() {
                @Override
                public TableCell<TableRow, CellValue> call(TableColumn<TableRow, CellValue> p) {
                    final ImportTableCell cell = new ImportTableCell();
                    return cell;
                }
            });

            if (columnIndex < savedAttributeNodes.length) {
                column.setAttributeNode(savedAttributeNodes[columnIndex]);
                column.validate(newRows);
            }

            // Show the column heading
            if (columnLabel == null || columnLabel.length() == 0) {
                column.setPrefWidth(50);
            } else {
                column.setPrefWidth(columnLabel.length() * 12.1); // the magic number
                // TODO: need to find a better way to determine the multiplier which
                // seems to be dependant on the number of columns and its width. So this
                // mechanism of blindly multiplying by 12.1 is not the ideal case because
                // it means longer headings get extra padding than they need and smaller
                // headings get just enough.
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

        rowFilter.setColumns(currentColumnLabels = columnLabels);
        sampleDataView.setItems(currentRows = newRows);
        setFilter(filter);
    }

    public void deleteAttribute(Attribute attribute) {
        if (attribute.getElementType() == GraphElementType.VERTEX) {
            sourceVertexAttributeList.deleteAttribute(attribute);
            destinationVertexAttributeList.deleteAttribute(attribute);
        } else {
            transactionAttributeList.deleteAttribute(attribute);
        }
    }

    public void validate(ImportTableColumn column) {
        if (column != null) {
            column.validate(currentRows);
        }
    }

    public boolean setFilter(String filter) {
        this.filter = filter;
        if (filter.isEmpty()) {
            for (TableRow tableRow : currentRows) {
                tableRow.setIncluded(true);
            }
            return true;
        }
        if (rowFilter.setScript(filter)) {
            for (TableRow tableRow : currentRows) {
                tableRow.filter(rowFilter);
            }
            return true;
        } else {
            for (TableRow tableRow : currentRows) {
                tableRow.setIncluded(false);
            }
            return false;
        }
    }

    public ImportDefinition createDefinition() {

        RowFilter rf = rowFilter;
        if (filter == null || filter.isEmpty()) {
            rf = null;
        } else {
            rf.setColumns(currentColumnLabels);
        }

        final ImportDefinition definition = new ImportDefinition(1, rf);

        for (final TableColumn<TableRow, ?> column : sampleDataView.getColumns()) {
            final ImportTableColumn importTableColumn = (ImportTableColumn) column;
            final AttributeNode attributeNode = importTableColumn.getAttributeNode();
            if (attributeNode != null) {
                // We added an artificial column at the beginning of the table ("Row"),
                // so we need to subtract 1 to allow for that offset.
                final ImportAttributeDefinition attributeDefinition = new ImportAttributeDefinition(importTableColumn.getColumnIndex() - 1, attributeNode.getAttribute(), attributeNode.getTranslator(), attributeNode.getDefaultValue(), attributeNode.getTranslatorParameters());
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
        ObservableList<TableColumn<TableRow, ?>> columns = sampleDataView.getColumns();
        List<Attribute> allocatedAttributes = new ArrayList<>(columns.size());
        for (final TableColumn<TableRow, ?> column : columns) {
            ImportTableColumn importTableColumn = (ImportTableColumn) column;
            AttributeNode attributeNode = importTableColumn.getAttributeNode();
            if (attributeNode != null) {
                allocatedAttributes.add(attributeNode.getAttribute());
            }
        }

        return allocatedAttributes;
    }

    public void setDisplayedAttributes(Map<String, Attribute> vertexAttributes, Map<String, Attribute> transactionAttributes, Set<Integer> keys) {
        sourceVertexAttributeList.setDisplayedAttributes(vertexAttributes, keys);
        destinationVertexAttributeList.setDisplayedAttributes(vertexAttributes, keys);
        transactionAttributeList.setDisplayedAttributes(transactionAttributes, keys);
    }

    void update(final ImportDefinition impdef, final Set<Integer> keys) {
        String script = impdef.getRowFilter().getScript();
        if (script == null) {
            script = "";
        }
        filterField.setText(script);
        setFilter(script);

        updateColumns(impdef, sourceVertexAttributeList, AttributeType.SOURCE_VERTEX, keys);
        updateColumns(impdef, destinationVertexAttributeList, AttributeType.DESTINATION_VERTEX, keys);
        updateColumns(impdef, transactionAttributeList, AttributeType.TRANSACTION, keys);
    }

    private void updateColumns(final ImportDefinition impdef, final AttributeList attrList, final AttributeType atype, final Set<Integer> keys) {
        final ObservableList<TableColumn<TableRow, ?>> columns = sampleDataView.getColumns();
        final Map<String, ImportTableColumn> labelToColumn = new HashMap<>();
        columns.stream().forEach((column) -> {
            final ImportTableColumn itc = (ImportTableColumn) column;
            labelToColumn.put(itc.getLabel(), itc);
        });

        final List<ImportAttributeDefinition> elementList = impdef.getDefinitions(atype);
        elementList.stream().forEach((iad) -> {
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
}
