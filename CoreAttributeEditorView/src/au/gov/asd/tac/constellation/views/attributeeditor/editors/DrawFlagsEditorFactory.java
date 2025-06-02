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

import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.DrawFlagsAttributeDescription;
import au.gov.asd.tac.constellation.utilities.visual.DrawFlags;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.DefaultGetter;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class DrawFlagsEditorFactory extends AttributeValueEditorFactory<DrawFlags> {

    @Override
    public AbstractEditor<DrawFlags> createEditor(final EditOperation editOperation, final DefaultGetter<DrawFlags> defaultGetter, final ValueValidator<DrawFlags> validator, final String editedItemName, final DrawFlags initialValue) {
        return new DrawFlagsEditor(editOperation, defaultGetter, validator, editedItemName, initialValue);
    }

    @Override
    public String getAttributeType() {
        return DrawFlagsAttributeDescription.ATTRIBUTE_NAME;
    }

    public class DrawFlagsEditor extends AbstractEditor<DrawFlags> {

        private CheckBox drawNodesCheckBox;
        private CheckBox drawConnectionsCheckBox;
        private CheckBox drawNodeLabelsCheckBox;
        private CheckBox drawConnectionLabelsCheckBox;
        private CheckBox drawBlazesCheckBox;

        protected DrawFlagsEditor(final EditOperation editOperation, final DefaultGetter<DrawFlags> defaultGetter, final ValueValidator<DrawFlags> validator, final String editedItemName, final DrawFlags initialValue) {
            super(editOperation, defaultGetter, validator, editedItemName, initialValue);
        }

        @Override
        protected boolean canSet(final DrawFlags value) {
            // Draw flags cannot be null, so prevent null values being set.
            return value != null;
        }

        @Override
        public void updateControlsWithValue(DrawFlags value) {
            // Ensure a null value is translated to an empty/default DrawFlags object
            if (value == null) {
                value = new DrawFlags(false, false, false, false, false);
            }

            drawNodesCheckBox.setSelected(value.drawNodes());
            drawConnectionsCheckBox.setSelected(value.drawConnections());
            drawNodeLabelsCheckBox.setSelected(value.drawNodeLabels());
            drawConnectionLabelsCheckBox.setSelected(value.drawConnectionLabels());
            drawBlazesCheckBox.setSelected(value.drawBlazes());
        }

        @Override
        protected DrawFlags getValueFromControls() {
            return new DrawFlags(drawNodesCheckBox.isSelected(),
                    drawConnectionsCheckBox.isSelected(), drawNodeLabelsCheckBox.isSelected(),
                    drawConnectionLabelsCheckBox.isSelected(), drawBlazesCheckBox.isSelected());
        }

        @Override
        protected Node createEditorControls() {
            final VBox controls = new VBox();
            controls.setFillWidth(true);

            drawNodesCheckBox = new CheckBox("Nodes");
            drawNodesCheckBox.selectedProperty().addListener((v, o, n) -> update());

            drawConnectionsCheckBox = new CheckBox("Connections");
            drawConnectionsCheckBox.selectedProperty().addListener((v, o, n) -> update());

            drawNodeLabelsCheckBox = new CheckBox("Node Labels");
            drawNodeLabelsCheckBox.selectedProperty().addListener((v, o, n) -> update());

            drawConnectionLabelsCheckBox = new CheckBox("Connection Labels");
            drawConnectionLabelsCheckBox.selectedProperty().addListener((v, o, n) -> update());

            drawBlazesCheckBox = new CheckBox("Blazes");
            drawBlazesCheckBox.selectedProperty().addListener((v, o, n) -> update());

            controls.getChildren().addAll(drawNodesCheckBox, drawConnectionsCheckBox, drawNodeLabelsCheckBox, drawConnectionLabelsCheckBox, drawBlazesCheckBox);

            return controls;
        }

        @Override
        public boolean noValueCheckBoxAvailable() {
            return false;
        }
    }
}
