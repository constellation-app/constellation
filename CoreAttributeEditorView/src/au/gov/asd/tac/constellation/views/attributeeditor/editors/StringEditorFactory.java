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
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
        
        private TextArea textArea;

        protected StringEditor(final String editedItemName, final EditOperation editOperation, final ValueValidator<String> validator, final String defaultValue, final String initialValue) {
            super(editedItemName, editOperation, validator, defaultValue, initialValue, true);
        }

        @Override
        public void updateControlsWithValue(final String value) {
            if (value != null) {
                textArea.setText(value);
            }
        }

        @Override
        protected String getValueFromControls() {
            return textArea.getText().isBlank() ? null : textArea.getText();
        }

        @Override
        protected Node createEditorControls() {
            textArea = new TextArea();
            textArea.setWrapText(true);
            textArea.textProperty().addListener((o, n, v) -> update());
            textArea.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                if (e.getCode() == KeyCode.DELETE) {
                    final IndexRange selection = textArea.getSelection();
                    if (selection.getLength() == 0) {
                        textArea.deleteNextChar();
                    } else {
                        textArea.deleteText(selection);
                    }
                    e.consume();
                } else if (e.isShortcutDown() && e.isShiftDown() && (e.getCode() == KeyCode.RIGHT)) {
                    textArea.selectNextWord();
                    e.consume();
                } else if (e.isShortcutDown() && e.isShiftDown() && (e.getCode() == KeyCode.LEFT)) {
                    textArea.selectPreviousWord();
                    e.consume();
                } else if (e.isShortcutDown() && (e.getCode() == KeyCode.RIGHT)) {
                    textArea.nextWord();
                    e.consume();
                } else if (e.isShortcutDown() && (e.getCode() == KeyCode.LEFT)) {
                    textArea.previousWord();
                    e.consume();
                } else if (e.isShiftDown() && (e.getCode() == KeyCode.RIGHT)) {
                    textArea.selectForward();
                    e.consume();
                } else if (e.isShiftDown() && (e.getCode() == KeyCode.LEFT)) {
                    textArea.selectBackward();
                    e.consume();
                } else if (e.isShortcutDown() && (e.getCode() == KeyCode.A)) {
                    textArea.selectAll();
                    e.consume();
                } else if (e.getCode() == KeyCode.ESCAPE) {
                    e.consume();
                }
            });
            
            final VBox controls = new VBox(textArea);
            controls.setAlignment(Pos.CENTER);
            
            return controls;
        }
    }
}
