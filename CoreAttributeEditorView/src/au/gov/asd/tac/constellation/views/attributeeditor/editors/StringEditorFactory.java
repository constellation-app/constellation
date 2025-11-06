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

import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.utilities.text.SpellCheckingTextArea;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import org.openide.util.lookup.ServiceProvider;

/**
 * Editor Factory for attributes of type string
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class StringEditorFactory extends AttributeValueEditorFactory<String> {

    @Override
    public AbstractEditor<String> createEditor(final String editedItemName, final EditOperation editOperation, final ValueValidator<String> validator, final String defaultValue, final String initialValue) {
        return new StringEditor(editedItemName, editOperation, validator, defaultValue, initialValue);
    }

    @Override
    public String getAttributeType() {
        return StringAttributeDescription.ATTRIBUTE_NAME;
    }

    public class StringEditor extends AbstractEditor<String> {
        
        private SpellCheckingTextArea spellCheckingTextArea;

        protected StringEditor(final String editedItemName, final EditOperation editOperation, final ValueValidator<String> validator, final String defaultValue, final String initialValue) {
            super(editedItemName, editOperation, validator, defaultValue, initialValue, true);
        }

        @Override
        public void updateControlsWithValue(final String value) {
            if (value != null) {                
                spellCheckingTextArea.setText(value);
            }
        }

        @Override
        protected String getValueFromControls() {
            return spellCheckingTextArea.getText().isBlank() ? null : spellCheckingTextArea.getText();
        }

        @Override
        protected Node createEditorControls() {
            spellCheckingTextArea = new SpellCheckingTextArea(false);
            spellCheckingTextArea.setWrapText(true);
            spellCheckingTextArea.textProperty().addListener((o, n, v) -> update());
            
            final VBox controls = new VBox(spellCheckingTextArea);
            controls.setAlignment(Pos.CENTER);
            
            return controls;
        }
    }
}
