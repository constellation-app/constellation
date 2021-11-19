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
package au.gov.asd.tac.constellation.views.schemaview.providers;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.manager.GraphManagerListener;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexTypeUtilities;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author algol
 */
@ServiceProvider(service = SchemaViewNodeProvider.class, position = 100)
public class VertexTypeNodeProvider implements SchemaViewNodeProvider, GraphManagerListener {

    public static final String MIMETYPE = "application/x-constellation-vertextype";
    public static final DataFormat VERTEX_DATA_FORMAT = new DataFormat(MIMETYPE);
    private static final int SMALL_ICON_IMAGE_SIZE = 16;
    private static final int LARGE_ICON_IMAGE_SIZE = 64;

    private final Label schemaLabel;
    private final TreeView<SchemaVertexType> treeView;
    private final List<SchemaVertexType> vertexTypes;
    private final HBox detailsView;
    private final Map<SchemaVertexType, Image> backgroundIcons;
    private final Map<SchemaVertexType, Image> foregroundIcons;
    private final RadioButton startsWithRb;
    private final TextField filterText;

    public VertexTypeNodeProvider() {
        schemaLabel = new Label(SeparatorConstants.HYPHEN);
        schemaLabel.setPadding(new Insets(5));
        treeView = new TreeView<>();
        vertexTypes = new ArrayList<>();
        detailsView = new HBox();
        detailsView.setPadding(new Insets(5));
        backgroundIcons = new HashMap<>();
        foregroundIcons = new HashMap<>();
        startsWithRb = new RadioButton("Starts with");
        filterText = new TextField();

        // A shiny cell factory so the tree nodes show the correct text and graphic.
        treeView.setCellFactory(p -> new TreeCell<SchemaVertexType>() {
            @Override
            protected void updateItem(final SchemaVertexType item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null) {
                    setText(item.getName());
                    if (item.getForegroundIcon() != null) {
                        // Background icon, sized, clipped, colored.
                        final Color color = item.getColor().getJavaFXColor();

                        if (!backgroundIcons.containsKey(item)) {
                            backgroundIcons.put(item, item.getBackgroundIcon().buildImage());
                        }
                        final ImageView bg = new ImageView(backgroundIcons.get(item));
                        bg.setFitWidth(SMALL_ICON_IMAGE_SIZE);
                        bg.setPreserveRatio(true);
                        bg.setSmooth(true);
                        final ImageView clip = new ImageView(bg.getImage());
                        clip.setFitWidth(SMALL_ICON_IMAGE_SIZE);
                        clip.setPreserveRatio(true);
                        clip.setSmooth(true);
                        bg.setClip(clip);
                        final ColorAdjust adjust = new ColorAdjust();
                        adjust.setSaturation(-1);
                        final ColorInput ci = new ColorInput(0, 0, SMALL_ICON_IMAGE_SIZE, SMALL_ICON_IMAGE_SIZE, color);
                        final Blend blend = new Blend(BlendMode.MULTIPLY, adjust, ci);
                        bg.setEffect(blend);

                        // Foreground icon, sized.
                        if (!foregroundIcons.containsKey(item)) {
                            foregroundIcons.put(item, item.getForegroundIcon().buildImage());
                        }
                        final ImageView fg = new ImageView(foregroundIcons.get(item));
                        fg.setFitWidth(SMALL_ICON_IMAGE_SIZE);
                        fg.setPreserveRatio(true);
                        fg.setSmooth(true);
                        fg.setCache(true);

                        // Combine foreground and background icons.
                        final Group iconGroup = new Group(bg, fg);

                        setGraphic(iconGroup);
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
        vertexTypes.clear();
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

            vertexTypes.clear();

            if (graph != null && graph.getSchema() != null && GraphNode.getGraphNode(graph) != null) {
                final SchemaFactory schemaFactory = graph.getSchema().getFactory();
                final Set<Class<? extends SchemaConcept>> concepts = schemaFactory.getRegisteredConcepts();
                schemaLabel.setText(String.format("%s - %s", schemaFactory.getLabel(), GraphNode.getGraphNode(graph).getDisplayName()));
                vertexTypes.addAll(SchemaVertexTypeUtilities.getTypes(concepts));
                Collections.sort(vertexTypes, (final SchemaVertexType a, final SchemaVertexType b) -> a.getName().compareToIgnoreCase(b.getName()));
            } else {
                schemaLabel.setText("No schema available");
            }
            populateTree();
        });
    }

    private VBox addFilter() {
        filterText.setPromptText("Filter Node types");
        final ToggleGroup toggleGroup = new ToggleGroup();
        startsWithRb.setToggleGroup(toggleGroup);
        startsWithRb.setPadding(new Insets(0, 0, 0, 5));
        startsWithRb.setSelected(true);
        final RadioButton containsRadioButton = new RadioButton("Contains");
        containsRadioButton.setToggleGroup(toggleGroup);
        containsRadioButton.setPadding(new Insets(0, 0, 0, 5));

        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> populateTree());

        filterText.textProperty().addListener((observable, oldValue, newValue) -> populateTree());

        final HBox headerBox = new HBox(new Label("Filter: "), filterText, startsWithRb, containsRadioButton);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(5));

        final VBox box = new VBox(schemaLabel, headerBox, treeView);
        VBox.setVgrow(treeView, Priority.ALWAYS);
        return box;
    }

    private void populateTree() {
        final TreeItem<SchemaVertexType> root = createNode(null);
        treeView.setRoot(root);
    }

    private boolean isFilterMatchCurrentNodeOrAnyChildren(SchemaVertexType treeItem) {
        return StringUtils.isNotBlank(filterText.getText()) && (isFilterMatchText(treeItem.getName())
                || isFilterMatchAnyProperty(treeItem) || isFilterMatchAnyChildNodes(treeItem));
    }

    private boolean isFilterMatchAnyChildNodes(SchemaVertexType treeItem) {
        boolean found = false;
        final List<SchemaVertexType> children = vertexTypes.stream().filter(vertexType
                -> vertexType.getSuperType() == treeItem && vertexType != treeItem).collect(Collectors.toList());

        for (SchemaVertexType child : children) {
            found = isFilterMatchCurrentNodeOrAnyChildren(child);
            if (found) {
                break;
            }
        }
        return found;
    }

    private boolean isFilterMatchAnyProperty(SchemaVertexType treeItem) {
        return isFilterMatchText(treeItem.getName())
                || isFilterMatchText(treeItem.getDescription())
                || isFilterMatchText(treeItem.getColor().getName())
                || isFilterMatchText(treeItem.getForegroundIcon().getName())
                || isFilterMatchText(Objects.toString(treeItem.getValidationRegex(), ""))
                || isFilterMatchText(Objects.toString(treeItem.getDetectionRegex(), ""))
                || isFilterMatchText(treeItem.getHierachy())
                || !(treeItem.getProperties().keySet().isEmpty())
                && treeItem.getProperties().keySet().stream().anyMatch(property
                        -> property != null && isFilterMatchText(property)
                );
    }

    private boolean isFilterMatchText(final String propertyValue) {
        final String filterInputText = filterText.getText().toLowerCase();
        return (StringUtils.isNotBlank(filterText.getText()) && StringUtils.isNotBlank(propertyValue))
                && (startsWithRb.isSelected() ? propertyValue.toLowerCase().startsWith(filterInputText)
                : propertyValue.toLowerCase().contains(filterInputText));

    }

    @Override
    public void setContent(final Tab tab) {
        GraphManager.getDefault().addGraphManagerListener(this);
        final VBox filterBox = addFilter();

        treeView.setShowRoot(false);
        treeView.setOnDragDetected(event -> {
            final Dragboard db = treeView.startDragAndDrop(TransferMode.COPY);
            final ClipboardContent content = new ClipboardContent();
            final TreeItem<SchemaVertexType> selectedItem = treeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                final SchemaVertexType vxtype = selectedItem.getValue();
                final String value = String.format("Type:%s", vxtype.getName());
                content.put(VERTEX_DATA_FORMAT, value);
                content.putString(String.format("%s=%s", SchemaVertexType.class.getSimpleName(), value));
            } else {
                content.putString("");
            }

            db.setContent(content);
            event.consume();
        });
        treeView.getSelectionModel().selectedItemProperty().addListener(event -> {
            detailsView.getChildren().clear();

            // Display the relevant fg and colored bg icons.
            final TreeItem<SchemaVertexType> item = treeView.getSelectionModel().getSelectedItem();
            if (item != null) {
                final SchemaVertexType vertexType = item.getValue();
                final Color color = vertexType.getColor().getJavaFXColor();

                // Background icon, sized, clipped, colored.
                final ImageView bg = new ImageView(backgroundIcons.get(vertexType));
                bg.setFitWidth(LARGE_ICON_IMAGE_SIZE);
                bg.setPreserveRatio(true);
                bg.setSmooth(true);
                final ImageView clip = new ImageView(bg.getImage());
                clip.setFitWidth(LARGE_ICON_IMAGE_SIZE);
                clip.setPreserveRatio(true);
                clip.setSmooth(true);
                bg.setClip(clip);
                final ColorAdjust adjust = new ColorAdjust();
                adjust.setSaturation(-1);
                final ColorInput ci = new ColorInput(0, 0, LARGE_ICON_IMAGE_SIZE, LARGE_ICON_IMAGE_SIZE, color);
                final Blend blend = new Blend(BlendMode.MULTIPLY, adjust, ci);
                bg.setEffect(blend);

                // Foreground icon, sized.
                final ImageView fg = new ImageView(foregroundIcons.get(vertexType));
                fg.setFitWidth(LARGE_ICON_IMAGE_SIZE);
                fg.setPreserveRatio(true);
                fg.setSmooth(true);

                // Combine foreground and background icons.
                final Group iconGroup = new Group(bg, fg);

                final GridPane grid = new GridPane();
                grid.setMaxWidth(detailsView.widthProperty().doubleValue());
                grid.setPadding(new Insets(0, 0, 0, 5));
                grid.setHgap(2);
                final ColumnConstraints col0 = new ColumnConstraints();
                col0.setHgrow(Priority.ALWAYS);
                final ColumnConstraints col1 = new ColumnConstraints();
                col1.setPercentWidth(75);
                grid.getColumnConstraints().addAll(col0, col1);

                final Label nameLabel = new Label(vertexType.getName());
                nameLabel.setWrapText(true);
                grid.add(boldLabel("Name:"), 0, 0);
                grid.add(nameLabel, 1, 0);

                final Label descriptionLabel = new Label(vertexType.getDescription());
                descriptionLabel.setWrapText(true);
                grid.add(boldLabel("Description:"), 0, 1);
                grid.add(descriptionLabel, 1, 1);

                final Label colorLabel = new Label(vertexType.getColor().toString());
                colorLabel.setWrapText(true);
                grid.add(boldLabel("Color:"), 0, 2);
                grid.add(colorLabel, 1, 2);

                final Label foregroundIconLabel = new Label(vertexType.getForegroundIcon().getName());
                foregroundIconLabel.setWrapText(true);
                grid.add(boldLabel("Foreground Icon:"), 0, 3);
                grid.add(foregroundIconLabel, 1, 3);

                final Label backgroundIconLabel = new Label(vertexType.getBackgroundIcon().getName());
                backgroundIconLabel.setWrapText(true);
                grid.add(boldLabel("Background Icon:"), 0, 4);
                grid.add(backgroundIconLabel, 1, 4);

                if (vertexType.getValidationRegex() != null) {
                    final Label validationLabel = new Label(vertexType.getValidationRegex().toString());
                    validationLabel.setWrapText(true);
                    grid.add(boldLabel("Validation Regex:"), 0, 5);
                    grid.add(validationLabel, 1, 5);
                }

                if (vertexType.getDetectionRegex() != null) {
                    final Label detectionLabel = new Label(vertexType.getDetectionRegex().toString());
                    detectionLabel.setWrapText(true);
                    grid.add(boldLabel("Detection Regex:"), 0, 6);
                    grid.add(detectionLabel, 1, 6);
                }

                final Label hierarchyLabel = new Label(vertexType.getHierachy());
                hierarchyLabel.setWrapText(true);
                grid.add(boldLabel("Hierarchy:"), 0, 7);
                grid.add(hierarchyLabel, 1, 7);

                int gridPosition = 7;
                for (String property : vertexType.getProperties().keySet()) {
                    final Object propertyValue = vertexType.getProperty(property);
                    if (propertyValue != null) {
                        gridPosition++;
                        Label propertyLabel = new Label(propertyValue.toString());
                        propertyLabel.setWrapText(true);
                        grid.add(boldLabel(property + SeparatorConstants.COLON), 0, gridPosition);
                        grid.add(propertyLabel, 1, gridPosition);
                    }
                }
                for (final Node child : grid.getChildren()) {
                    final Integer column = GridPane.getColumnIndex(child);
                    final Integer row = GridPane.getRowIndex(child);
                    if ((column > 0 && row != null && child instanceof Label) && (isFilterMatchText(((Label) child).getText()))) {
                        child.getStyleClass().add("schemaview-highlight-blue");
                    }
                }
                detailsView.getChildren().addAll(iconGroup, grid);
            }
        });

        final VBox contentBox = new VBox(schemaLabel, filterBox, treeView, detailsView);
        VBox.setVgrow(treeView, Priority.ALWAYS);
        detailsView.prefHeightProperty().bind(contentBox.heightProperty().multiply(0.4));
        final StackPane contentNode = new StackPane(contentBox);

        newActiveGraph(GraphManager.getDefault().getActiveGraph());

        Platform.runLater(() -> tab.setContent(contentNode));
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
     * @param vxtype
     * @return
     */
    private TreeItem<SchemaVertexType> createNode(final SchemaVertexType vxtype) {
        return new TreeItem<SchemaVertexType>(vxtype) {
            // We cache whether the vertextype is a leaf or not.
            private boolean isLeaf;

            // We do the children and leaf testing only once, and then set these
            // booleans to false so that we do not check again during this
            // run.
            private boolean isFirstTimeChildren = true;
            private boolean isFirstTimeLeaf = true;

            @Override
            public ObservableList<TreeItem<SchemaVertexType>> getChildren() {
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
                        final boolean foundChild = vertexTypes.stream()
                                .anyMatch(vertexType -> vertexType.getSuperType() == getValue() && vertexType != getValue());
                        isLeaf = !foundChild;
                    }
                }

                return isLeaf;
            }

            private ObservableList<TreeItem<SchemaVertexType>> buildChildren(final TreeItem<SchemaVertexType> item) {
                final SchemaVertexType value = item.getValue();
                final ObservableList<TreeItem<SchemaVertexType>> children = FXCollections.observableArrayList();
                if (value == null) {
                    // Null is a special marker for the single root node.
                    // Any vertextype that points to itself is in the root layer.
                    for (final SchemaVertexType vertexType : vertexTypes) {
                        if ((vertexType.getSuperType() == vertexType) && (isFilterMatchCurrentNodeOrAnyChildren(vertexType) || filterText.getText().isEmpty())) {
                            children.add(createNode(vertexType));
                        }
                    }
                } else {
                    for (final SchemaVertexType vertexType : vertexTypes) {
                        if ((vertexType.getSuperType() == value && vertexType != value) && (isFilterMatchCurrentNodeOrAnyChildren(vertexType) || filterText.getText().isEmpty())) {
                            children.add(createNode(vertexType));
                        }
                    }
                }
                return children;
            }
        };
    }

    @Override
    public String getText() {
        return "Node Types";
    }
}
