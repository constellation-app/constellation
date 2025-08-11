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

import au.gov.asd.tac.constellation.graph.attribute.HyperlinkAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import java.net.URI;
import java.net.URISyntaxException;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.openide.util.lookup.ServiceProvider;

/**
 * Editor Factory for attributes of type hyperlink
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class HyperlinkEditorFactory extends AttributeValueEditorFactory<URI> {

    @Override
    public AbstractEditor<URI> createEditor(final String editedItemName, final EditOperation editOperation, final ValueValidator<URI> validator, final URI defaultValue, final URI initialValue) {
        return new HyperlinkEditor(editedItemName, editOperation, validator, defaultValue, initialValue);
    }

    @Override
    public String getAttributeType() {
        return HyperlinkAttributeDescription.ATTRIBUTE_NAME;
    }

    public class HyperlinkEditor extends AbstractEditor<URI> {

        private TextField textField;

        protected HyperlinkEditor(final String editedItemName, final EditOperation editOperation, final ValueValidator<URI> validator, final URI defaultValue, final URI initialValue) {
            super(editedItemName, editOperation, validator, defaultValue, initialValue, true);
        }

        @Override
        public void updateControlsWithValue(final URI value) {
            if (value != null) {
                textField.setText(String.valueOf(value));
            }
        }

        @Override
        protected URI getValueFromControls() throws ControlsInvalidException {
            try {
                return new URI(textField.getText());
            } catch (final URISyntaxException ex) {
                throw new ControlsInvalidException("Entered value is not a valid URL.");
            }
        }

        @Override
        protected Node createEditorControls() {
            textField = new TextField();
            textField.textProperty().addListener((o, n, v) -> update());
            
            final VBox controls = new VBox(textField);
            controls.setAlignment(Pos.CENTER);
            
            return controls;
        }
    }
}
