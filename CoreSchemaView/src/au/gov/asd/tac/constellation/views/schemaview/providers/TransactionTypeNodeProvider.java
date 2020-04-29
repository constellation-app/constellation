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
package au.gov.asd.tac.constellation.views.schemaview.providers;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.manager.GraphManagerListener;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionTypeUtilities;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author algol
 */
@ServiceProvider(service = SchemaViewNodeProvider.class, position = 200)
public class TransactionTypeNodeProvider implements SchemaViewNodeProvider, GraphManagerListener {

    public static final String MIMETYPE = "application/x-constellation-transactiontype";
    public static final DataFormat TRANSACTION_TYPE = new DataFormat(MIMETYPE);
    private static final int ICON_IMAGE_SIZE = 16;

    private final Label schemaLabel;
    private final TreeView<SchemaTransactionType> treeView;
    private final ArrayList<SchemaTransactionType> transactionTypes;
    private final HBox detailsView;

    public TransactionTypeNodeProvider() {
        schemaLabel = new Label(SeparatorConstants.HYPHEN);
        schemaLabel.setPadding(new Insets(5));
        treeView = new TreeView<>();
        transactionTypes = new ArrayList<>();
        detailsView = new HBox();
        detailsView.setPadding(new Insets(5));

        // A shiny cell factory so the tree nodes show the correct text and graphic.
        treeView.setCellFactory(p -> new TreeCell<SchemaTransactionType>() {
            @Override
            protected void updateItem(final SchemaTransactionType item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null) {
                    setText(item.getName());
                    if (item.getColor() != null) {
                        final Rectangle icon = getSquare(item.getColor().getJavaFXColor());
                        icon.setSmooth(true);
                        icon.setCache(true);
                        setGraphic(icon);
                    }
                } else {
                    setGraphic(null);
                    setText(null);
                }
            }
        });
    }

    @Override
    public void discardNode() {
        GraphManager.getDefault().removeGraphManagerListener(this);
        transactionTypes.clear();
    }

    @Override
    public void graphOpened(final Graph graph) {
        newActiveGraph(graph);
    }

    @Override
    public void graphClosed(final Graph graph) {
        newActiveGraph(null);
    }

    @Override
    public void newActiveGraph(final Graph graph) {
        // TODO: if the old graph and the new graph have the same schema, don't recalculate.
        Platform.runLater(() -> {
            detailsView.getChildren().clear();
            final Label nameLabel = new Label("No type selected");
            detailsView.getChildren().add(nameLabel);
            
            transactionTypes.clear();

            if (graph != null && graph.getSchema() != null && GraphNode.getGraphNode(graph) != null) {
                final SchemaFactory schemaFactory = graph.getSchema().getFactory();
                final Set<Class<? extends SchemaConcept>> concepts = schemaFactory.getRegisteredConcepts();
                schemaLabel.setText(String.format("%s - %s", schemaFactory.getLabel(), GraphNode.getGraphNode(graph).getDisplayName()));
                transactionTypes.addAll(SchemaTransactionTypeUtilities.getTypes(concepts));
                Collections.sort(transactionTypes, (final SchemaTransactionType a, final SchemaTransactionType b) -> a.getName().compareToIgnoreCase(b.getName()));

            } else {
                schemaLabel.setText("No schema available");
            }

            final TreeItem<SchemaTransactionType> root = createNode(null);
            treeView.setRoot(root);
        });
    }

    @Override
    public void setContent(final Tab tab) {
        GraphManager.getDefault().addGraphManagerListener(this);

        treeView.setShowRoot(false);
        treeView.getSelectionModel().selectedItemProperty().addListener(event -> {
            detailsView.getChildren().clear();

            // Display the relevant fg and colored bg icons.
            final TreeItem<SchemaTransactionType> item = treeView.getSelectionModel().getSelectedItem();
            if (item != null) {
                final SchemaTransactionType transactionType = item.getValue();
                final Rectangle colorRectangle = getSquare(transactionType.getColor().getJavaFXColor());

                final GridPane grid = new GridPane();
                grid.setPadding(new Insets(0, 0, 0, 5));
                grid.setHgap(2);
                final ColumnConstraints col0 = new ColumnConstraints();
                col0.setHgrow(Priority.ALWAYS);
                final ColumnConstraints col1 = new ColumnConstraints();
                col1.setPercentWidth(75);
                grid.getColumnConstraints().addAll(col0, col1);

                Label nameLabel = new Label(transactionType.getName());
                nameLabel.setWrapText(true);
                grid.add(boldLabel("Name:"), 0, 0);
                grid.add(nameLabel, 1, 0);

                Label descriptionLabel = new Label(transactionType.getDescription());
                descriptionLabel.setWrapText(true);
                grid.add(boldLabel("Description:"), 0, 1);
                grid.add(descriptionLabel, 1, 1);

                Label colorLabel = new Label(transactionType.getColor().toString());
                colorLabel.setWrapText(true);
                grid.add(boldLabel("Color:"), 0, 2);
                grid.add(colorLabel, 1, 2);

                Label styleLabel = new Label(transactionType.getStyle().toString());
                styleLabel.setWrapText(true);
                grid.add(boldLabel("Style:"), 0, 3);
                grid.add(styleLabel, 1, 3);

                Label directedLabel = new Label(transactionType.isDirected().toString());
                directedLabel.setWrapText(true);
                grid.add(boldLabel("Directed:"), 0, 4);
                grid.add(directedLabel, 1, 4);

                Label hierarchyLabel = new Label(transactionType.getHierachy());
                hierarchyLabel.setWrapText(true);
                grid.add(boldLabel("Hierarchy:"), 0, 5);
                grid.add(hierarchyLabel, 1, 5);

                int gridPosition = 5;
                for (String property : transactionType.getProperties().keySet()) {
                    final Object propertyValue = transactionType.getProperty(property);
                    if (propertyValue != null) {
                        gridPosition++;
                        Label propertyLabel = new Label(propertyValue.toString());
                        propertyLabel.setWrapText(true);
                        grid.add(boldLabel(property + ":"), 0, gridPosition);
                        grid.add(propertyLabel, 1, gridPosition);
                    }
                }

                detailsView.getChildren().addAll(colorRectangle, grid);
            }
        });

        final VBox contentBox = new VBox(schemaLabel, treeView, detailsView);
        VBox.setVgrow(treeView, Priority.ALWAYS);
        detailsView.prefHeightProperty().bind(contentBox.heightProperty().multiply(0.4));
        final StackPane contentNode = new StackPane(contentBox);

        newActiveGraph(GraphManager.getDefault().getActiveGraph());

        Platform.runLater(() -> {
            tab.setContent(contentNode);
        });
    }

    /**
     * A Label containing bold text.
     *
     * @param text
     * @return
     */
    private static Node boldLabel(final String text) {
        final Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold;");
        label.setMinWidth(Region.USE_PREF_SIZE);
        label.setMaxWidth(Region.USE_PREF_SIZE);
        return label;
    }

    /**
     * Recursively create a tree of vertex types.
     * <p>
     * getSuperType() points to the parent. If getSuperType() points to itself,
     * the vertex type is a root.
     *
     * @param txtype
     * @return
     */
    private TreeItem<SchemaTransactionType> createNode(final SchemaTransactionType txtype) {
        final TreeItem<SchemaTransactionType> ti = new TreeItem<SchemaTransactionType>(txtype) {
            // We cache whether the vertextype is a leaf or not.
            private boolean isLeaf;

            // We do the children and leaf testing only once, and then set these
            // booleans to false so that we do not check again during this
            // run.
            private boolean isFirstTimeChildren = true;
            private boolean isFirstTimeLeaf = true;

            @Override
            public ObservableList<TreeItem<SchemaTransactionType>> getChildren() {
                if (isFirstTimeChildren) {
                    isFirstTimeChildren = false;

                    super.getChildren().setAll(buildChildren(this));
                }

                return super.getChildren();
            }

            /**
             * A vertextype is not a leaf if another vertextype refers to it as
             * a supertype.
             *
             * @return
             */
            @Override
            public boolean isLeaf() {
                if (isFirstTimeLeaf) {
                    isFirstTimeLeaf = false;

                    isLeaf = false;
                    if (getValue() != null) {
                        final boolean foundChild = transactionTypes.stream().anyMatch(tt -> tt.getSuperType() == getValue() && tt != getValue());
                        isLeaf = !foundChild;
                    }
                }

                return isLeaf;
            }

            private ObservableList<TreeItem<SchemaTransactionType>> buildChildren(final TreeItem<SchemaTransactionType> item) {
                final SchemaTransactionType value = item.getValue();
                final ObservableList<TreeItem<SchemaTransactionType>> children = FXCollections.observableArrayList();
                if (value == null) {
                    // Null is a special marker for the single root node.
                    // Any vertextype that points to itself is in the root layer.
                    for (final SchemaTransactionType tt : transactionTypes) {
                        if (tt.getSuperType() == tt) {
                            children.add(createNode(tt));
                        }
                    }
                } else {
                    for (final SchemaTransactionType tt : transactionTypes) {
                        if (tt.getSuperType() == value && tt != value) {
                            children.add(createNode(tt));
                        }
                    }
                }

                return children;
            }
        };

        if (txtype != null) {
            ti.setGraphic(getSquare(txtype.getColor().getJavaFXColor()));
        }

        return ti;
    }

    private static Rectangle getSquare(final Color color) {
        final Rectangle r = new Rectangle(ICON_IMAGE_SIZE, ICON_IMAGE_SIZE);
        r.setFill(color);

        return r;
    }

    @Override
    public String getText() {
        return "Transaction Types";
    }
}
