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
package au.gov.asd.tac.constellation.views.attributeeditor.editors;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabel;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabels;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.VertexGraphLabelsAttributeDescription;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.attributeeditor.AttributeEditorDialog;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.DefaultGetter;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class VertexGraphLabelsEditorFactory extends AttributeValueEditorFactory<GraphLabels> {

    @Override
    public AbstractEditor<GraphLabels> createEditor(final EditOperation editOperation, final DefaultGetter<GraphLabels> defaultGetter, final ValueValidator<GraphLabels> validator, final String editedItemName, final GraphLabels initialValue) {
        return new GraphLabelsEditor(editOperation, defaultGetter, validator, editedItemName, initialValue);
    }

    @Override
    public String getAttributeType() {
        return VertexGraphLabelsAttributeDescription.ATTRIBUTE_NAME;
    }

    public class GraphLabelsEditor extends AbstractEditor<GraphLabels> {

        private final List<LabelEntry> labels = new ArrayList<>();
        private final VBox labelPaneContent = new VBox(5);
        private final VBox labelEntries = new VBox(5);
        private final Button addButton = new Button("", new ImageView(UserInterfaceIconProvider.ADD.buildImage(16)));
        private final List<String> attributeNames = new ArrayList<>();

        protected GraphLabelsEditor(final EditOperation editOperation, final DefaultGetter<GraphLabels> defaultGetter, final ValueValidator<GraphLabels> validator, final String editedItemName, final GraphLabels initialValue) {
            super(editOperation, defaultGetter, validator, editedItemName, initialValue);
        }

        @Override
        public void updateControlsWithValue(final GraphLabels value) {
            labels.clear();
            labelEntries.getChildren().clear();
            if (value != null) {
                value.getLabels().forEach(label -> new LabelEntry(labels, labelEntries, label.getAttributeName(), label.getColor(), label.getSize()));
            }
        }

        @Override
        protected GraphLabels getValueFromControls() throws ControlsInvalidException {
            final List<GraphLabel> data = new ArrayList<>();
            try {
                labels.forEach(label
                        -> data.add(new GraphLabel(label.attrCombo.getSelectionModel().getSelectedItem(), ConstellationColor.fromFXColor(label.color), Float.parseFloat(label.sizeText.getText()))));
            } catch (final NumberFormatException ex) {
                throw new ControlsInvalidException("Non numeric value entered for label size");
            }
            return new GraphLabels(data);
        }

        @Override
        protected Node createEditorControls() {
            // get all vertex attributes currently in the graph
            final ReadableGraph rg = GraphManager.getDefault().getActiveGraph().getReadableGraph();
            try {
                for (int i = 0; i < rg.getAttributeCount(GraphElementType.VERTEX); i++) {
                    attributeNames.add(rg.getAttributeName(rg.getAttribute(GraphElementType.VERTEX, i)));
                }
            } finally {
                rg.release();
            }
            attributeNames.sort(String::compareTo);

            HBox labelTitles = new HBox();
            final Label attrLabel = new Label("Attribute");
            attrLabel.setAlignment(Pos.CENTER);
            attrLabel.setPrefWidth(150);
            final Label colorLabel = new Label("Color");
            colorLabel.setPrefWidth(40);
            colorLabel.setAlignment(Pos.CENTER);
            final Label sizeLabel = new Label("Size");
            sizeLabel.setPrefWidth(50);
            sizeLabel.setAlignment(Pos.CENTER);
            final Label positionLabel = new Label("Position");
            positionLabel.setPrefWidth(115);
            positionLabel.setAlignment(Pos.CENTER);
            labelTitles.getChildren().addAll(attrLabel, colorLabel, sizeLabel, positionLabel);
            labelPaneContent.setPadding(new Insets(5));
            labelPaneContent.getChildren().addAll(labelTitles, labelEntries);

            final ScrollPane labelsScrollPane = new ScrollPane();
            labelsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            labelsScrollPane.setPrefHeight(200);
            labelsScrollPane.setPrefWidth(400);
            labelsScrollPane.setContent(labelPaneContent);

            addButton.setOnAction(e -> {
                new LabelEntry(labels, labelEntries, attributeNames.isEmpty() ? "" : attributeNames.get(0), ConstellationColor.WHITE, 1);
                addButton.setDisable(labels.size() == GraphLabels.MAX_LABELS);
                update();
            });
            final Label addButtonLabel = new Label("Add Label");
            final FlowPane addPane = new FlowPane();
            addPane.setHgap(10);
            addPane.setAlignment(Pos.CENTER_RIGHT);
            addPane.getChildren().addAll(addButtonLabel, addButton);

            final VBox controls = new VBox(10);
            controls.setPrefWidth(400);
            controls.getChildren().addAll(labelsScrollPane, addPane);

            return controls;
        }

        @Override
        public Boolean noValueCheckBoxAvailable() {
            return false;
        }

        private class LabelEntry {

            final ComboBox<String> attrCombo;
            final Rectangle colorRect;
            Color color;
            final TextField sizeText;
            final HBox entry;
            final List<LabelEntry> host;
            final Pane visualHost;

            LabelEntry(final List<LabelEntry> host, final Pane visualHost, final String attributeName, final ConstellationColor color, final float size) {

                attrCombo = new ComboBox<>(FXCollections.observableList(attributeNames));
                attrCombo.setPrefWidth(150);
                attrCombo.getSelectionModel().select(attributeName);
                attrCombo.getSelectionModel().selectedItemProperty().addListener((o, n, v) -> update());

                colorRect = new Rectangle(20, 20);
                this.color = color.getJavaFXColor();
                colorRect.setFill(this.color);
                colorRect.setStroke(Color.LIGHTGREY);
                colorRect.setOnMouseClicked(getChooseColorEventHandler());

                sizeText = new TextField(String.valueOf(size));
                sizeText.setPrefWidth(50);
                sizeText.textProperty().addListener((o, n, v) -> update());

                final Button upButton = new Button("", new ImageView(UserInterfaceIconProvider.CHEVRON_UP.buildImage(16)));
                upButton.setOnAction(e -> {
                    moveUp();
                    update();
                });
                final Button downButton = new Button("", new ImageView(UserInterfaceIconProvider.CHEVRON_DOWN.buildImage(16)));
                downButton.setOnAction(e -> {
                    moveDown();
                    update();
                });
                final Button removeButton = new Button("", new ImageView(UserInterfaceIconProvider.CROSS.buildImage(16)));
                removeButton.setOnAction(e -> {
                    remove();
                    update();
                });

                entry = new HBox(10);
                entry.getChildren().addAll(attrCombo, colorRect, sizeText, upButton, downButton, removeButton);

                this.host = host;
                this.host.add(this);
                this.visualHost = visualHost;
                this.visualHost.getChildren().add(entry);
            }

            private void moveUp() {
                final int index = host.indexOf(this);
                if (index > 0) {
                    host.set(index, host.get(index - 1));
                    host.set(index - 1, this);
                    Node movingDown = visualHost.getChildren().remove(index - 1);
                    final List<Node> hostChildren = visualHost.getChildren();
                    final List<Node> beginning = new ArrayList<>(hostChildren.subList(0, index));
                    final List<Node> end = new ArrayList<>(hostChildren.subList(index, hostChildren.size()));
                    hostChildren.clear();
                    hostChildren.addAll(beginning);
                    hostChildren.add(movingDown);
                    hostChildren.addAll(end);
                }
            }

            private void moveDown() {
                final int index = host.indexOf(this);
                if (index < host.size() - 1) {
                    host.set(index, host.get(index + 1));
                    host.set(index + 1, this);

                    Node movingUp = visualHost.getChildren().remove(index + 1);
                    final List<Node> hostChildren = visualHost.getChildren();
                    final List<Node> beginning = new ArrayList<>(hostChildren.subList(0, index));
                    final List<Node> end = new ArrayList<>(hostChildren.subList(index, hostChildren.size()));
                    hostChildren.clear();
                    hostChildren.addAll(beginning);
                    hostChildren.add(movingUp);
                    hostChildren.addAll(end);
                }
            }

            private void remove() {
                host.remove(this);
                addButton.setDisable(host.size() == GraphLabels.MAX_LABELS);
                visualHost.getChildren().remove(entry);
            }

            private EventHandler<? super MouseEvent> getChooseColorEventHandler() {
                return e -> {
                    final AttributeValueEditorFactory<ConstellationColor> editorFactory = new ColorEditorFactory();

                    final EditOperation setColorEditOperation = value -> {
                        color = value == null ? ConstellationColor.WHITE.getJavaFXColor() : ((ConstellationColor) value).getJavaFXColor();
                        colorRect.setFill(color);
                        update();
                    };

                    final AbstractEditor<ConstellationColor> editor = editorFactory.createEditor(setColorEditOperation, ValueValidator.getAlwaysSucceedValidator(), "label color", ConstellationColor.fromFXColor(color));
                    final AttributeEditorDialog dialog = new AttributeEditorDialog(false, editor);
                    dialog.showDialog();
                };
            }
        }
    }
}
