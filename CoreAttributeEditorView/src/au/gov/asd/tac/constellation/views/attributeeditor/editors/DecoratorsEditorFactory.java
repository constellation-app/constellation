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
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.visual.VertexDecorators;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.DecoratorsAttributeDescription;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.DefaultGetter;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class DecoratorsEditorFactory extends AttributeValueEditorFactory<VertexDecorators> {

    @Override
    public AbstractEditor<VertexDecorators> createEditor(final EditOperation editOperation, final DefaultGetter<VertexDecorators> defaultGetter, final ValueValidator<VertexDecorators> validator, final String editedItemName, final VertexDecorators initialValue) {
        return new DecoratorsEditor(editOperation, defaultGetter, validator, editedItemName, initialValue);
    }

    @Override
    public String getAttributeType() {
        return DecoratorsAttributeDescription.ATTRIBUTE_NAME;
    }

    private static final String NO_DECORATOR = "<None>";

    public class DecoratorsEditor extends AbstractEditor<VertexDecorators> {

        ComboBox<String> nwCombo;
        ComboBox<String> neCombo;
        ComboBox<String> seCombo;
        ComboBox<String> swCombo;

        protected DecoratorsEditor(final EditOperation editOperation, final DefaultGetter<VertexDecorators> defaultGetter, final ValueValidator<VertexDecorators> validator, final String editedItemName, final VertexDecorators initialValue) {
            super(editOperation, defaultGetter, validator, editedItemName, initialValue);
        }

        @Override
        protected boolean canSet(final VertexDecorators value) {
            // Decorators cannot be null, so prevent null values being set.
            return value != null;
        }

        @Override
        public void updateControlsWithValue(VertexDecorators value) {
            // Ensure a null value is translated to an empty/default VertexDecorators object
            if (value == null) {
                value = new VertexDecorators(null, null, null, null);
            }

            setDecoratorChoice(nwCombo, value.getNorthWestDecoratorAttribute());
            setDecoratorChoice(neCombo, value.getNorthEastDecoratorAttribute());
            setDecoratorChoice(seCombo, value.getSouthEastDecoratorAttribute());
            setDecoratorChoice(swCombo, value.getSouthWestDecoratorAttribute());
        }

        @Override
        protected VertexDecorators getValueFromControls() throws ControlsInvalidException {
            return new VertexDecorators(getDecoratorChoice(nwCombo), getDecoratorChoice(neCombo),
                    getDecoratorChoice(seCombo), getDecoratorChoice(swCombo));
        }

        @Override
        protected Node createEditorControls() {
            // get all vertex attributes currently in the graph
            final List<String> attributeNames = new ArrayList<>();
            final ReadableGraph rg = GraphManager.getDefault().getActiveGraph().getReadableGraph();
            try {
                for (int i = 0; i < rg.getAttributeCount(GraphElementType.VERTEX); i++) {
                    attributeNames.add(rg.getAttributeName(rg.getAttribute(GraphElementType.VERTEX, i)));
                }
            } finally {
                rg.release();
            }
            Collections.sort(attributeNames);
            attributeNames.add(0, NO_DECORATOR);

            final Label nwLabel = new Label("NW:");
            final Label neLabel = new Label("NE:");
            final Label seLabel = new Label("SE:");
            final Label swLabel = new Label("SW:");
            nwCombo = new ComboBox<>(FXCollections.observableList(attributeNames));
            nwCombo.getSelectionModel().selectedItemProperty().addListener((o, n, v) -> update());
            neCombo = new ComboBox<>(FXCollections.observableList(attributeNames));
            neCombo.getSelectionModel().selectedItemProperty().addListener((o, n, v) -> update());
            seCombo = new ComboBox<>(FXCollections.observableList(attributeNames));
            seCombo.getSelectionModel().selectedItemProperty().addListener((o, n, v) -> update());
            swCombo = new ComboBox<>(FXCollections.observableList(attributeNames));
            swCombo.getSelectionModel().selectedItemProperty().addListener((o, n, v) -> update());

            final GridPane controls = new GridPane();
            controls.getColumnConstraints().add(new ColumnConstraints(50));
            controls.setVgap(CONTROLS_DEFAULT_VERTICAL_SPACING);
            controls.addRow(0, nwLabel, nwCombo);
            controls.addRow(3, neLabel, neCombo);
            controls.addRow(2, seLabel, seCombo);
            controls.addRow(1, swLabel, swCombo);

            return controls;
        }

        @Override
        public boolean noValueCheckBoxAvailable() {
            return false;
        }

        private void setDecoratorChoice(final ComboBox<String> comboBox, final String choice) {
            comboBox.getSelectionModel().select(choice == null ? NO_DECORATOR : choice);
        }

        private String getDecoratorChoice(final ComboBox<String> comboBox) {
            return comboBox.getSelectionModel().getSelectedItem().equals(NO_DECORATOR) ? null : comboBox.getSelectionModel().getSelectedItem();
        }
    }
}
