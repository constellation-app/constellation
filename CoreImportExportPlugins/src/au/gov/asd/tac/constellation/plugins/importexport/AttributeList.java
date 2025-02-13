/*
 * Copyright 2010-2024 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.plugins.importexport.translator.AttributeTranslator;
import au.gov.asd.tac.constellation.plugins.importexport.translator.DefaultAttributeTranslator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

/**
 * An AttributeList is a UI element that can hold all the available attributes
 * for a given graph element. At present, three of these are used to hold the
 * source node, destination node and transaction attributes in the UI and
 * provide a place from which the user can drag these attributes onto columns.
 * Attributes are represented with in the AttributeList by
 * {@link AttributeNode}s.
 *
 * @author sirius
 */
public class AttributeList extends VBox {

    private static final int MIN_WIDTH = 50;
    private static final int MIN_HEIGHT = 100;
    private static final int VBOX_SPACING = 1;
    private static final Insets VBOX_PADDING = new Insets(2);

    private final RunPane runPane;
    protected final ImportController importController;
    private final AttributeType attributeType;
    private final Map<String, AttributeNode> attributeNodes;
    private Set<Integer> keys;

    public AttributeList(final ImportController importController, final RunPane runPane, final AttributeType attributeType) {
        this.runPane = runPane;

        this.importController = importController;
        this.attributeType = attributeType;
        attributeNodes = new HashMap<>();
        keys = new HashSet<>();

        setAlignment(Pos.CENTER);
        setMinSize(MIN_WIDTH, MIN_HEIGHT);
        setSpacing(VBOX_SPACING);
        setPadding(VBOX_PADDING);
        setFillWidth(true);

        setAlignment(Pos.TOP_CENTER);
    }

    public AttributeType getAttributeType() {
        return attributeType;
    }

    public RunPane getRunPane() {
        return runPane;
    }

    public AttributeNode getAttributeNode(final String label) {
        return attributeNodes.get(label);
    }

    private void createAttribute(final Attribute attribute) {

        final AttributeNode attributeNode = new AttributeNode(this, attribute, keys.contains(attribute.getId()));

        attributeNodes.put(attribute.getName(), attributeNode);

        attributeNode.setOnMousePressed((final MouseEvent t) -> {
            if (t.isPrimaryButtonDown()) {
                runPane.setDraggingOffset(new Point2D(t.getX(), t.getY()));
                final Point2D location = runPane.sceneToLocal(t.getSceneX(), t.getSceneY());
                // If the attribute node is currently assigned to a column then remove it.
                final ImportTableColumn currentColumn = attributeNode.getColumn();
                if (currentColumn != null) {
                    currentColumn.setAttributeNode(null);
                    runPane.validate(currentColumn);
                }
                attributeNode.setColumn(null);
                // Replicates user clicking the attribute
                if (!runPane.getChildren().contains(attributeNode)) {
                    runPane.getChildren().add(attributeNode);
                }
                attributeNode.setManaged(false);
                attributeNode.setLayoutX(location.getX() - runPane.getDraggingOffset().getX());
                attributeNode.setLayoutY(location.getY() - runPane.getDraggingOffset().getY());
                runPane.setDraggingAttributeNode(attributeNode);
                runPane.handleAttributeMoved(t.getSceneX(), t.getSceneY());
            }
        });

        // Add the new attributeNode to the list in the correct place
        // to maintain the sorted order.
        final ObservableList<Node> children = getChildren();
        for (int i = 0; i < children.size(); i++) {
            if (attributeNode.compareTo((AttributeNode) children.get(i)) <= 0) {
                children.add(i, attributeNode);
                return;
            }
        }

        children.add(attributeNode);
    }

    public void deleteAttributeNode(final AttributeNode attributeNode) {
        final ImportTableColumn currentColumn = attributeNode.getColumn();
        if (currentColumn != null) {
            currentColumn.setAttributeNode(null);
            runPane.validate(currentColumn);
        }
        attributeNode.setColumn(null);
        importController.deleteAttribute(attributeNode.getAttribute());
    }

    public void deleteAttribute(final Attribute attribute) {
        final AttributeNode attributeNode = attributeNodes.remove(attribute.getName());
        if (attributeNode != null) {
            final ImportTableColumn column = attributeNode.getColumn();
            if (column == null) {
                getChildren().remove(attributeNode);
            } else {
                column.setAttributeNode(null);
            }
        }
    }

    public void addAttributeNode(final AttributeNode attributeNode) {
        final ObservableList<Node> children = getChildren();
        for (int i = 0; i < children.size(); i++) {
            if (attributeNode.compareTo((AttributeNode) children.get(i)) <= 0) {
                children.add(i, attributeNode);
                attributeNode.setColumn(null);
                return;
            }
        }
        children.add(attributeNode);
        attributeNode.setColumn(null);
    }

    public void setDisplayedAttributes(final Map<String, Attribute> attributes, final Set<Integer> keySet) {

        this.keys = keySet;

        // Remove all attributes that no longer exist.
        final Iterator<Entry<String, AttributeNode>> i = attributeNodes.entrySet().iterator();
        while (i.hasNext()) {
            final Entry<String, AttributeNode> entry = i.next();
            if (!attributes.containsKey(entry.getKey())) {
                final AttributeNode attributeNode = entry.getValue();
                final ImportTableColumn column = attributeNode.getColumn();
                if (column == null) {
                    getChildren().remove(attributeNode);
                } else {
                    column.setAttributeNode(null);
                }
                i.remove();
            }
        }

        attributes.values().forEach(attribute -> {
            final AttributeNode attributeNode = attributeNodes.get(attribute.getName());
            if (attributeNode == null) {
                createAttribute(attribute);
            } else {
                attributeNode.setAttribute(attribute, keys.contains(attribute.getId()));

                // Show the default value
                attributeNode.updateDefaultValue();
            }
        });
    }

    /**
     * Create an {@code ImportDefinition} from attributes modified on the
     * attribute list
     *
     * @param importDefinition the {@link ImportDefinition} that will hold the
     * new {@link ImportAttributeDefinition}s.
     */
    public void createDefinition(final ImportDefinition importDefinition) {
        attributeNodes.values().stream().filter(attributeNode -> (attributeNode.getColumn() == null)).forEachOrdered(attributeNode -> {
            final String defaultValue = attributeNode.getDefaultValue();
            final AttributeTranslator translator = attributeNode.getTranslator();
            if (defaultValue != null || (translator != null && !(translator instanceof DefaultAttributeTranslator))) {
                final ImportAttributeDefinition attributeDefinition = new ImportAttributeDefinition(defaultValue,
                        attributeNode.getAttribute(), attributeNode.getTranslator(),
                        attributeNode.getTranslatorParameters());
                importDefinition.addDefinition(attributeType, attributeDefinition);
            }
        });
    }
}
