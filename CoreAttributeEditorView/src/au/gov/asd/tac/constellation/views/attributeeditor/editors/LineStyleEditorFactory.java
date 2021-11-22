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

import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.LineStyleAttributeDescription;
import au.gov.asd.tac.constellation.utilities.visual.LineStyle;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.DefaultGetter;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class LineStyleEditorFactory extends AttributeValueEditorFactory<LineStyle> {

    @Override
    public AbstractEditor<LineStyle> createEditor(final EditOperation editOperation, final DefaultGetter<LineStyle> defaultGetter, final ValueValidator<LineStyle> validator, final String editedItemName, final LineStyle initialValue) {
        return new LineStyleEditor(editOperation, defaultGetter, validator, editedItemName, initialValue);
    }

    @Override
    public String getAttributeType() {
        return LineStyleAttributeDescription.ATTRIBUTE_NAME;
    }

    public class LineStyleEditor extends AbstractEditor<LineStyle> {

        private ComboBox<LineStyle> lineStyleComboBox;

        protected LineStyleEditor(final EditOperation editOperation, final DefaultGetter<LineStyle> defaultGetter, final ValueValidator<LineStyle> validator, final String editedItemName, final LineStyle initialValue) {
            super(editOperation, defaultGetter, validator, editedItemName, initialValue);
        }

        @Override
        protected boolean canSet(final LineStyle value) {
            // As LineStyle is an enum, we want one of its constants, not null.
            return value != null;
        }

        @Override
        public void updateControlsWithValue(final LineStyle value) {
            lineStyleComboBox.getSelectionModel().select(value);
        }

        @Override
        protected LineStyle getValueFromControls() {
            return lineStyleComboBox.getValue();
        }

        @Override
        protected Node createEditorControls() {
            final GridPane controls = new GridPane();
            controls.setAlignment(Pos.CENTER);
            controls.setHgap(CONTROLS_DEFAULT_HORIZONTAL_SPACING);

            final Label lineStyleLabel = new Label("Line Style:");
            final ObservableList<LineStyle> lineStyles = FXCollections.observableArrayList(LineStyle.values());
            lineStyleComboBox = new ComboBox<>(lineStyles);
            final Callback<ListView<LineStyle>, ListCell<LineStyle>> cellFactory = (final ListView<LineStyle> p) -> new ListCell<LineStyle>() {
                @Override
                protected void updateItem(final LineStyle item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setText(item.name());
                    }
                }
            };
            lineStyleComboBox.setCellFactory(cellFactory);
            lineStyleComboBox.setButtonCell(cellFactory.call(null));
            lineStyleLabel.setLabelFor(lineStyleComboBox);
            lineStyleComboBox.getSelectionModel().selectedItemProperty().addListener((o, n, v) -> update());

            controls.addRow(0, lineStyleLabel, lineStyleComboBox);
            return controls;
        }
    }
}
