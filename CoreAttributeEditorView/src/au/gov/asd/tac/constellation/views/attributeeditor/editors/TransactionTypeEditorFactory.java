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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.TransactionTypeAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConceptUtilities;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionTypeUtilities;
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
public class TransactionTypeEditorFactory extends AttributeValueEditorFactory<SchemaTransactionType> {

    @Override
    public AbstractEditor<SchemaTransactionType> createEditor(final EditOperation editOperation, final DefaultGetter<SchemaTransactionType> defaultGetter, final ValueValidator<SchemaTransactionType> validator, final String editedItemName, final SchemaTransactionType initialValue) {
        return new TransactionTypeEditor(editOperation, defaultGetter, validator, editedItemName, initialValue);
    }

    @Override
    public String getAttributeType() {
        return TransactionTypeAttributeDescription.ATTRIBUTE_NAME;
    }

    public class TransactionTypeEditor extends AbstractEditor<SchemaTransactionType> {

        private ListView<SchemaTransactionType> typeList;
        private TextField nameText;
        private boolean selectionIsActive = false;

        protected TransactionTypeEditor(final EditOperation editOperation, final DefaultGetter<SchemaTransactionType> defaultGetter, final ValueValidator<SchemaTransactionType> validator, final String editedItemName, final SchemaTransactionType initialValue) {
            super(editOperation, defaultGetter, validator, editedItemName, initialValue);
        }

        @Override
        public void updateControlsWithValue(final SchemaTransactionType value) {
            final SchemaTransactionType type = value;
            if (typeList.getItems().contains(type)) {
                typeList.getSelectionModel().select(type);
            } else {
                nameText.setText(type != null ? type.getName() : "");
            }
        }

        @Override
        protected SchemaTransactionType getValueFromControls() {
            return typeList.getSelectionModel().getSelectedItem() != null
                    ? typeList.getSelectionModel().getSelectedItem()
                    : SchemaTransactionTypeUtilities.getTypeOrBuildNew(nameText.getText());
        }

        @Override
        protected Node createEditorControls() {
            final GridPane controls = new GridPane();
            controls.setAlignment(Pos.CENTER);
            controls.setVgap(CONTROLS_DEFAULT_VERTICAL_SPACING);

            typeList = new ListView<>();
            typeList.setCellFactory(p -> new ListCell<SchemaTransactionType>() {
                @Override
                protected void updateItem(final SchemaTransactionType item, final boolean empty) {
                    super.updateItem(item, empty);
                    if (!empty && item != null) {
                        setText(item.getName());
                    }
                }
            });

            // get all types supported by the current schema
            final List<SchemaTransactionType> types = new ArrayList<>();
            final Graph currentGraph = GraphManager.getDefault().getActiveGraph();
            if (currentGraph != null && currentGraph.getSchema() != null) {
                final SchemaFactory schemaFactory = currentGraph.getSchema().getFactory();
                final Set<Class<? extends SchemaConcept>> concepts = new HashSet<>();
                concepts.addAll(schemaFactory.getRegisteredConcepts());
                SchemaConceptUtilities.getChildConcepts(schemaFactory.getRegisteredConcepts())
                        .forEach(childConcept -> concepts.add(childConcept.getClass()));
                types.addAll(SchemaTransactionTypeUtilities.getTypes(concepts));
                types.add(SchemaTransactionTypeUtilities.getDefaultType());
            }

            final Label nameLabel = new Label("Type Name:");
            nameText = new TextField();
            final VBox nameBox = new VBox(CONTROLS_DEFAULT_VERTICAL_SPACING);
            nameBox.getChildren().addAll(nameLabel, nameText);

            nameText.textProperty().addListener(ev -> {
                if (!selectionIsActive) {
                    typeList.getSelectionModel().select(null);
                }
                update();
            });

            final Label listLabel = new Label("Schema Types:");
            Collections.sort(types);
            typeList.getItems().addAll(types);

            typeList.getSelectionModel().selectedItemProperty().addListener(ev -> {
                selectionIsActive = true;
                final SchemaTransactionType type = typeList.getSelectionModel().getSelectedItem();
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
