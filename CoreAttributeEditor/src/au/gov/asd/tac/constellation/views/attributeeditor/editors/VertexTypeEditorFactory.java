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
package au.gov.asd.tac.constellation.views.attributeeditor.editors;

import au.gov.asd.tac.constellation.graph.attribute.VertexTypeAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.SchemaConcept;
import au.gov.asd.tac.constellation.graph.schema.SchemaConceptUtilities;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.SchemaVertexTypeUtilities;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.DefaultGetter;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class VertexTypeEditorFactory extends AttributeValueEditorFactory<SchemaVertexType> {

    @Override
    public AbstractEditor<SchemaVertexType> createEditor(final EditOperation editOperation, final DefaultGetter<SchemaVertexType> defaultGetter, final ValueValidator<SchemaVertexType> validator, final String editedItemName, final SchemaVertexType initialValue) {
        return new VertexTypeEditor(editOperation, defaultGetter, validator, editedItemName, initialValue);
    }

    @Override
    public String getAttributeType() {
        return VertexTypeAttributeDescription.ATTRIBUTE_NAME;
    }

    public class VertexTypeEditor extends AbstractEditor<SchemaVertexType> {

        private ListView<SchemaVertexType> typeList;
        private TextField nameText;
        private boolean selectionIsActive = false;

        protected VertexTypeEditor(final EditOperation editOperation, final DefaultGetter<SchemaVertexType> defaultGetter, final ValueValidator<SchemaVertexType> validator, final String editedItemName, final SchemaVertexType initialValue) {
            super(editOperation, defaultGetter, validator, editedItemName, initialValue);
        }

        @Override
        public void updateControlsWithValue(final SchemaVertexType value) {
            final SchemaVertexType type = value;
            if (typeList.getItems().contains(type)) {
                typeList.getSelectionModel().select(type);
            } else {
                if (type != null) {
                    nameText.setText(type.getName());
                } else {
                    nameText.setText("");
                }
            }
        }

        @Override
        protected SchemaVertexType getValueFromControls() {
            if (typeList.getSelectionModel().getSelectedItem() != null) {
                return typeList.getSelectionModel().getSelectedItem();
            } else {
                return SchemaVertexTypeUtilities.getTypeOrBuildNew(nameText.getText());
            }
        }

        @Override
        protected Node createEditorControls() {
            GridPane controls = new GridPane();
            controls.setAlignment(Pos.CENTER);
            controls.setVgap(CONTROLS_DEFAULT_VERTICAL_SPACING);

            typeList = new ListView<>();
            typeList.setCellFactory(p -> new ListCell<SchemaVertexType>() {
                @Override
                protected void updateItem(final SchemaVertexType item, boolean empty) {
                    super.updateItem(item, empty);
                    if (!empty && item != null) {
                        setText(item.getName());
                    }
                }
            });

            // get all types supported by the current schema
            final List<SchemaVertexType> types = new ArrayList<>();
            if (GraphManager.getDefault().getActiveGraph() != null && GraphManager.getDefault().getActiveGraph().getSchema() != null) {
                final SchemaFactory schemaFactory = GraphManager.getDefault().getActiveGraph().getSchema().getFactory();
                final Set<Class<? extends SchemaConcept>> concepts = new HashSet<>();
                concepts.addAll(schemaFactory.getRegisteredConcepts());
                SchemaConceptUtilities.getChildConcepts(schemaFactory.getRegisteredConcepts())
                        .forEach(childConcept -> concepts.add(childConcept.getClass()));
                types.addAll(SchemaVertexTypeUtilities.getTypes(concepts));
                types.add(SchemaVertexTypeUtilities.getDefaultType());
            }

            Label nameLabel = new Label("Type Name:");
            nameText = new TextField();
            final VBox nameBox = new VBox(10);
            nameBox.getChildren().addAll(nameLabel, nameText);

            nameText.textProperty().addListener(ev -> {
                if (!selectionIsActive) {
                    typeList.getSelectionModel().select(null);
                }
                update();
            });

            Label listLabel = new Label("Schema Types:");
            Collections.sort(types);
            typeList.getItems().addAll(types);

            typeList.getSelectionModel().selectedItemProperty().addListener(ev -> {
                selectionIsActive = true;
                final SchemaVertexType type = typeList.getSelectionModel().getSelectedItem();
                if (type != null) {
                    nameText.setText(type.getName());
                }
                selectionIsActive = false;
            });

            controls.addRow(0, nameBox);
            controls.addRow(1, listLabel);
            controls.addRow(2, typeList);
            return controls;
        }
    }
}
