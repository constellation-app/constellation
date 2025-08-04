/*
 * Copyright 2010-2025 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabel;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabels;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.TransactionGraphLabelsAttributeDescription;
import au.gov.asd.tac.constellation.graph.utilities.AttributeUtilities;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.attributeeditor.AttributeEditorDialog;
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
 * Editor Factory for attributes of type graph_labels_transactions
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class TransactionGraphLabelsEditorFactory extends AttributeValueEditorFactory<GraphLabels> {

    @Override
    public AbstractEditor<GraphLabels> createEditor(final EditOperation editOperation, final GraphLabels defaultValue, final ValueValidator<GraphLabels> validator, final String editedItemName, final GraphLabels initialValue) {
        return new GraphLabelsEditor(editOperation, defaultValue, validator, editedItemName, initialValue);
    }

    @Override
    public String getAttributeType() {
        return TransactionGraphLabelsAttributeDescription.ATTRIBUTE_NAME;
    }

    public class GraphLabelsEditor extends AbstractEditor<GraphLabels> {

        private final List<LabelEntry> labels = new ArrayList<>();
        private final VBox labelEntries = new VBox(5);
        private final Button addButton = new Button("", new ImageView(UserInterfaceIconProvider.ADD.buildImage(16)));
        private final List<String> attributeNames = new ArrayList<>();

        protected GraphLabelsEditor(final EditOperation editOperation, final GraphLabels defaultValue, final ValueValidator<GraphLabels> validator, final String editedItemName, final GraphLabels initialValue) {
            super(editOperation, defaultValue, validator, editedItemName, initialValue);
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
            // get all transaction attributes currently in the graph
            attributeNames.addAll(AttributeUtilities.getAttributeNames(GraphElementType.TRANSACTION));
            attributeNames.sort(String::compareTo);

            final Label attrLabel = createLabel("Attribute", 150);
            final Label colorLabel = createLabel("Color", 40);
            final Label sizeLabel = createLabel("Size", 50);
            final Label positionLabel = createLabel("Position", 115);
            final HBox labelTitles = new HBox(attrLabel, colorLabel, sizeLabel, positionLabel);
            
            final VBox labelPaneContent = new VBox(5, labelTitles, labelEntries);
            labelPaneContent.setPadding(new Insets(5));

            final ScrollPane labelsScrollPane = new ScrollPane(labelPaneContent);
            labelsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            labelsScrollPane.setPrefHeight(200);
            labelsScrollPane.setPrefWidth(400);

            addButton.setOnAction(e -> {
                new LabelEntry(labels, labelEntries, attributeNames.isEmpty() ? "" : attributeNames.get(0), ConstellationColor.WHITE, 1);
                addButton.setDisable(labels.size() == GraphLabels.MAX_LABELS);
                update();
            });
            final Label addButtonLabel = new Label("Add Label");
            final FlowPane addPane = new FlowPane(addButtonLabel, addButton);
            addPane.setHgap(10);
            addPane.setAlignment(Pos.CENTER_RIGHT);

            final VBox controls = new VBox(CONTROLS_DEFAULT_VERTICAL_SPACING, 
                    labelsScrollPane, addPane);
            controls.setPrefWidth(400);

            return controls;
        }
        
        private Label createLabel(final String labelText, final double prefWidth) {
            final Label label = new Label(labelText);
            label.setAlignment(Pos.CENTER);
            label.setPrefWidth(prefWidth);
            
            return label;
        }

        private class LabelEntry {

            private final ComboBox<String> attrCombo;
            private final Rectangle colorRect;
            private Color color;
            private final TextField sizeText;
            private final HBox entry;
            private final List<LabelEntry> host;
            private final Pane visualHost;

            public LabelEntry(final List<LabelEntry> host, final Pane visualHost, final String attributeName, final ConstellationColor color, final float size) {
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
                    final Node movingDown = visualHost.getChildren().remove(index - 1);
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

                    final Node movingUp = visualHost.getChildren().remove(index + 1);
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
                    final ColorEditorFactory editorFactory = new ColorEditorFactory();

                    final EditOperation setColorEditOperation = value -> {
                        color = value == null ? ConstellationColor.WHITE.getJavaFXColor() : ((ConstellationColor) value).getJavaFXColor();
                        colorRect.setFill(color);
                        update();
                    };

                    final AbstractEditor<ConstellationColor> editor = editorFactory.createEditor(setColorEditOperation, "label color", ConstellationColor.fromFXColor(color));
                    final AttributeEditorDialog dialog = new AttributeEditorDialog(false, editor);
                    dialog.showDialog();
                };
            }
        }
    }
}
