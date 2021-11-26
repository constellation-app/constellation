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
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.manager.GraphManagerListener;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConceptUtilities;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author algol
 */
@ServiceProvider(service = SchemaViewNodeProvider.class, position = 0)
public class AttributeNodeProvider implements SchemaViewNodeProvider, GraphManagerListener {

    // icons
    @StaticResource
    private static final String GRAPH_ICON = "au/gov/asd/tac/constellation/views/schemaview/resources/graph.png";
    @StaticResource
    private static final String META_ICON = "au/gov/asd/tac/constellation/views/schemaview/resources/meta.png";
    private Image vertexImage = null;
    private Image transactionImage = null;
    private Image edgeImage = null;
    private Image linkImage = null;
    private Image graphImage = null;
    private Image metaImage = null;

    private final Label schemaLabel;
    private final TableView<AttributeEntry> table;
    private final ObservableList<AttributeEntry> attributeInfo;

    public AttributeNodeProvider() {
        schemaLabel = new Label(SeparatorConstants.HYPHEN);
        schemaLabel.setPadding(new Insets(5));
        attributeInfo = FXCollections.observableArrayList();
        table = new TableView<>();
        table.setItems(attributeInfo);
        table.setPlaceholder(new Label("No schema available"));
    }

    @Override
    public void setContent(final Tab tab) {
        GraphManager.getDefault().addGraphManagerListener(this);
        newActiveGraph(GraphManager.getDefault().getActiveGraph());

        final TextField filterText = new TextField();
        filterText.setPromptText("Filter attribute names");

        final ToggleGroup tg = new ToggleGroup();
        final RadioButton startsWithRb = new RadioButton("Starts with");
        startsWithRb.setToggleGroup(tg);
        startsWithRb.setPadding(new Insets(0, 0, 0, 5));
        startsWithRb.setSelected(true);
        final RadioButton containsRb = new RadioButton("Contains");
        containsRb.setToggleGroup(tg);
        containsRb.setPadding(new Insets(0, 0, 0, 5));

        tg.selectedToggleProperty().addListener((ov, oldValue, newValue) -> filter(table, filterText.getText(), startsWithRb.isSelected()));

        filterText.textProperty().addListener((ov, oldValue, newValue) -> filter(table, newValue, startsWithRb.isSelected()));

        final HBox headerBox = new HBox(new Label("Filter: "), filterText, startsWithRb, containsRb);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(5));

        final VBox box = new VBox(schemaLabel, headerBox, table);
        VBox.setVgrow(table, Priority.ALWAYS);

        Platform.runLater(() -> tab.setContent(box));
    }

    private void filter(final TableView<AttributeEntry> table, final String newValue, final boolean st) {
        if (newValue.isEmpty()) {
            table.setItems(attributeInfo);
            table.scrollTo(0);
            table.getSelectionModel().clearSelection();
        } else {
            final ObservableList<AttributeEntry> items = FXCollections.observableArrayList();
            attributeInfo.stream().forEach(si -> {
                final String nameLc = si.attr.getName().toLowerCase();
                final boolean found = st ? StringUtils.startsWithIgnoreCase(nameLc, newValue) : StringUtils.containsIgnoreCase(nameLc, newValue);
                if (found) {
                    items.add(si);
                }
            });

            table.setItems(items);
        }
    }

    @Override
    public void discardNode() {
        GraphManager.getDefault().removeGraphManagerListener(this);
        table.getColumns().clear();
        attributeInfo.clear();
    }

    @Override
    public String getText() {
        return "Attributes";
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
            table.getColumns().clear();

            if (graph != null && graph.getSchema() != null && GraphNode.getGraphNode(graph) != null) {
                schemaLabel.setText(String.format("%s - %s", graph.getSchema().getFactory().getLabel(), GraphNode.getGraphNode(graph).getDisplayName()));
                buildTable(graph);
            } else {
                schemaLabel.setText(SeparatorConstants.HYPHEN);
                table.setPlaceholder(new Label("No schema available"));
            }
        });
    }

    private void buildTable(final Graph graph) {
        attributeInfo.clear();
        attributeInfo.addAll(getSchemaInfo(graph));

        vertexImage = vertexImage != null ? vertexImage : UserInterfaceIconProvider.NODES.buildImage(16);
        transactionImage = transactionImage != null ? transactionImage : UserInterfaceIconProvider.TRANSACTIONS.buildImage(16);
        edgeImage = edgeImage != null ? edgeImage : UserInterfaceIconProvider.EDGES.buildImage(16);
        linkImage = linkImage != null ? linkImage : UserInterfaceIconProvider.LINKS.buildImage(16);
        graphImage = graphImage != null ? graphImage : new Image(GRAPH_ICON);
        metaImage = metaImage != null ? metaImage : new Image(META_ICON);

        final TableColumn<AttributeEntry, Label> labelCol = new TableColumn<>("Attribute Name");
        labelCol.setCellValueFactory(p -> {
            final Label label = new Label(p.getValue().attr.getName());
            if (p.getValue().keyIx >= 0) {
                label.setStyle("-fx-background-color: #8a1d1d; -fx-text-fill: white;");
            }

            final GraphElementType et = p.getValue().attr.getElementType();
            switch (et) {
                case VERTEX:
                    label.setGraphic(new ImageView(vertexImage));
                    break;
                case TRANSACTION:
                    label.setGraphic(new ImageView(transactionImage));
                    break;
                case EDGE:
                    label.setGraphic(new ImageView(edgeImage));
                    break;
                case LINK:
                    label.setGraphic(new ImageView(linkImage));
                    break;
                case GRAPH:
                    label.setGraphic(new ImageView(graphImage));
                    break;
                case META:
                    label.setGraphic(new ImageView(metaImage));
                    break;
                default:
                    break;
            }

            return new SimpleObjectProperty<>(label);
        });
        labelCol.setComparator((a, b) -> a.getText().compareTo(b.getText()));

        final TableColumn<AttributeEntry, String> dtypeCol = new TableColumn<>("Data Type");
        dtypeCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().attr.getAttributeType()));

        final TableColumn<AttributeEntry, String> etypeCol = new TableColumn<>("Element Type");
        etypeCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().attr.getElementType().getShortLabel()));

        final TableColumn<AttributeEntry, String> descrCol = new TableColumn<>("Description");
        descrCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().attr.getDescription()));

        final TableColumn<AttributeEntry, Label> defaultCol = new TableColumn<>("Default Value");
        defaultCol.setCellValueFactory(p -> {
            final Object defaultValue = p.getValue().attr.getDefault();
            final String value = defaultValue != null ? defaultValue.toString() : "null";
            final Label label = new Label(value);
            if (defaultValue == null) {
                label.setStyle("-fx-text-fill: grey;");
            }

            return new SimpleObjectProperty<>(label);
        });

        final TableColumn<AttributeEntry, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().category));

        final TableColumn<AttributeEntry, Label> keyCol = new TableColumn<>("Key");
        keyCol.setMinWidth(50);
        keyCol.setCellValueFactory(p -> {
            final int keyIx = p.getValue().keyIx;
            final Label label = new Label(keyIx >= 0 ? Integer.toString(keyIx) : "");

            return new SimpleObjectProperty<>(label);
        });
        keyCol.setComparator((a, b) -> {
            final int sa = a.getText().isEmpty() ? Integer.MAX_VALUE : Integer.valueOf(a.getText());
            final int sb = b.getText().isEmpty() ? Integer.MAX_VALUE : Integer.valueOf(b.getText());

            return Integer.compare(sa, sb);
        });

        table.getColumns().addAll(labelCol, dtypeCol, descrCol, etypeCol, defaultCol, categoryCol, keyCol);
        table.getSortOrder().clear();
        table.getSortOrder().add(labelCol);
    }

    private List<AttributeEntry> getSchemaInfo(final Graph graph) {
        final List<AttributeEntry> attrs = new ArrayList<>();
        final Schema schema = graph.getSchema();
        final SchemaFactory factory = schema.getFactory();

        // Get the keys for each element type.
        final Map<GraphElementType, List<String>> elementKeys = new HashMap<>();
        for (final GraphElementType et : new GraphElementType[]{GraphElementType.VERTEX, GraphElementType.TRANSACTION}) {
            final List<SchemaAttribute> keys = factory.getKeyAttributes(et);
            final List<String> keyLabels = keys.stream().map(key -> key.getName()).collect(Collectors.toList());
            elementKeys.put(et, keyLabels);
        }

        for (final GraphElementType et : GraphElementType.values()) {
            final Map<String, SchemaAttribute> attrsMap = factory.getRegisteredAttributes(et);
            attrsMap.values().stream().forEach(schemaAttribute -> {
                final int keyIx = elementKeys.containsKey(schemaAttribute.getElementType()) ? elementKeys.get(schemaAttribute.getElementType()).indexOf(schemaAttribute.getName()) : -1;
                final Collection<SchemaConcept> concepts = SchemaConceptUtilities.getAttributeConcepts(schemaAttribute);
                final StringBuilder categories = new StringBuilder();
                for (SchemaConcept concept : concepts) {
                    categories.append(concept.getName()).append(",");
                }
                if (categories.length() > 0) {
                    categories.deleteCharAt(categories.length() - 1);
                }
                attrs.add(new AttributeEntry(schemaAttribute, categories.toString(), keyIx));
            });
        }

        return attrs;
    }

    private static class AttributeEntry {

        final SchemaAttribute attr;
        final String category;
        final int keyIx;

        AttributeEntry(final SchemaAttribute attr, final String category, final int keyIx) {
            this.attr = attr;
            this.category = category;
            this.keyIx = keyIx;
        }
    }
}
