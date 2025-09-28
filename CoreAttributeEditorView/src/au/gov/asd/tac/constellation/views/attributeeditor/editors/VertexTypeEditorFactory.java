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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.VertexTypeAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConceptUtilities;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexTypeUtilities;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.openide.util.lookup.ServiceProvider;

/**
 * Editor Factory for attributes of type vertex_type
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class VertexTypeEditorFactory extends AttributeValueEditorFactory<SchemaVertexType> {

    @Override
    public AbstractEditor<SchemaVertexType> createEditor(final String editedItemName, final EditOperation editOperation, final ValueValidator<SchemaVertexType> validator, final SchemaVertexType defaultValue, final SchemaVertexType initialValue) {
        return new VertexTypeEditor(editedItemName, editOperation, validator, defaultValue, initialValue);
    }

    @Override
    public String getAttributeType() {
        return VertexTypeAttributeDescription.ATTRIBUTE_NAME;
    }

    public class VertexTypeEditor extends AbstractEditor<SchemaVertexType> {

        private ListView<SchemaVertexType> typeList;
        private TextField nameText;
        private boolean selectionIsActive = false;

        protected VertexTypeEditor(final String editedItemName, final EditOperation editOperation, final ValueValidator<SchemaVertexType> validator, final SchemaVertexType defaultValue, final SchemaVertexType initialValue) {
            super(editedItemName, editOperation, validator, defaultValue, initialValue);
        }

        @Override
        public void updateControlsWithValue(final SchemaVertexType value) {
            if (typeList.getItems().contains(value)) {
                typeList.getSelectionModel().select(value);
            } else {
                nameText.setText(value != null ? value.getName() : "");
            }
        }

        @Override
        protected SchemaVertexType getValueFromControls() {
            return typeList.getSelectionModel().getSelectedItem() != null
                    ? typeList.getSelectionModel().getSelectedItem()
                    : SchemaVertexTypeUtilities.getTypeOrBuildNew(nameText.getText());
        }

        @Override
        protected Node createEditorControls() {
            final Label nameLabel = new Label("Type Name:");
            final VBox nameBox = new VBox(CONTROLS_DEFAULT_VERTICAL_SPACING, nameLabel, nameText);
            nameText = new TextField();
            nameText.textProperty().addListener(ev -> {
                if (!selectionIsActive) {
                    typeList.getSelectionModel().select(null);
                }
                update();
            });
            
            // get all types supported by the current schema
            final List<SchemaVertexType> types = new ArrayList<>();
            final Graph currentGraph = GraphManager.getDefault().getActiveGraph();
            if (currentGraph != null && currentGraph.getSchema() != null) {
                final SchemaFactory schemaFactory = currentGraph.getSchema().getFactory();
                final Set<Class<? extends SchemaConcept>> concepts = new HashSet<>();
                concepts.addAll(schemaFactory.getRegisteredConcepts());
                SchemaConceptUtilities.getChildConcepts(schemaFactory.getRegisteredConcepts())
                        .forEach(childConcept -> concepts.add(childConcept.getClass()));
                types.addAll(SchemaVertexTypeUtilities.getTypes(concepts));
                types.add(SchemaVertexTypeUtilities.getDefaultType());
            }
            
            final Label listLabel = new Label("Schema Types:");
            Collections.sort(types);
            typeList = new ListView<>();
            typeList.getItems().addAll(types);
            typeList.setCellFactory(p -> new ListCell<>() {
                @Override
                protected void updateItem(final SchemaVertexType item, final boolean empty) {
                    super.updateItem(item, empty);
                    if (!empty && item != null) {
                        setText(item.getName());
                    }
                }
            });

            typeList.getSelectionModel().selectedItemProperty().addListener(ev -> {
                selectionIsActive = true;
                final SchemaVertexType type = typeList.getSelectionModel().getSelectedItem();
                if (type != null) {
                    nameText.setText(type.getName());
                }
                selectionIsActive = false;
            });
            
            final VBox controls = new VBox(CONTROLS_DEFAULT_VERTICAL_SPACING, 
                    nameBox, listLabel, typeList);
            controls.setAlignment(Pos.CENTER);
            
            return controls;
        }
    }
}
