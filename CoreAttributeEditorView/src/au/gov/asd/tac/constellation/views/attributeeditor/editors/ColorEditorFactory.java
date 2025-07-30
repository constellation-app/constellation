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
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.ColorAttributeDescription;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class ColorEditorFactory extends AttributeValueEditorFactory<ConstellationColor> {

    @Override
    public AbstractEditor<ConstellationColor> createEditor(final EditOperation editOperation, final ConstellationColor defaultValue, final ValueValidator<ConstellationColor> validator, final String editedItemName, final ConstellationColor initialValue) {
        return new ColorEditor(editOperation, defaultValue, validator, editedItemName, initialValue);
    }

    @Override
    public String getAttributeType() {
        return ColorAttributeDescription.ATTRIBUTE_NAME;
    }

    public class ColorEditor extends AbstractEditor<ConstellationColor> {

        private ComboBox<ConstellationColor> colorCombo;
        private ColorPicker picker;

        protected ColorEditor(final EditOperation editOperation, final ConstellationColor defaultValue, final ValueValidator<ConstellationColor> validator, final String editedItemName, final ConstellationColor initialValue) {
            super(editOperation, defaultValue, validator, editedItemName, initialValue, true);
        }

        @Override
        public void updateControlsWithValue(final ConstellationColor value) {
            if (value != null) {
                picker.setValue(value.getJavaFXColor());
            }
        }

        @Override
        protected ConstellationColor getValueFromControls() {
            if (colorCombo.getValue() != null) {
                return colorCombo.getValue();
            }

            return ConstellationColor.fromFXColor(picker.getValue());
        }

        @Override
        protected Node createEditorControls() {
            final Label namedLabel = new Label("Named:");
            final ObservableList<ConstellationColor> namedColors = FXCollections.observableArrayList(ConstellationColor.NAMED_COLOR_LIST);
            colorCombo = new ComboBox<>(namedColors);
            namedLabel.setLabelFor(colorCombo);
            colorCombo.valueProperty().addListener((o, oldValue, newValue) -> {
                if (newValue != null && !newValue.equals(oldValue)) {
                    picker.setValue(newValue.getJavaFXColor());
                }
            });
            
            final Callback<ListView<ConstellationColor>, ListCell<ConstellationColor>> cellFactory = (final ListView<ConstellationColor> p) -> new ListCell<>() {
                @Override
                protected void updateItem(final ConstellationColor item, final boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        final Rectangle r = new Rectangle(12, 12, item.getJavaFXColor());
                        r.setStroke(Color.BLACK);
                        setText(item.getName());
                        setGraphic(r);
                    }
                }
            };
            colorCombo.setCellFactory(cellFactory);
            colorCombo.setButtonCell(cellFactory.call(null));

            final Label pickerLabel = new Label("Pallete:");
            picker = new ColorPicker();
            pickerLabel.setLabelFor(picker);
            picker.valueProperty().addListener((o, oldValue, newValue) -> {
                if (newValue != null && !newValue.equals(oldValue)) {
                    boolean foundNamedColor = false;
                    for (final ConstellationColor c : ConstellationColor.NAMED_COLOR_LIST) {
                        final Color fxc = c.getJavaFXColor();
                        if (newValue.equals(fxc)) {
                            colorCombo.setValue(c);
                            foundNamedColor = true;
                            break;
                        }
                    }

                    if (!foundNamedColor) {
                        colorCombo.setValue(null);
                    }
                }

                update();
            });
            
            final GridPane controls = new GridPane();
            controls.setAlignment(Pos.CENTER);
            controls.setVgap(CONTROLS_DEFAULT_VERTICAL_SPACING);
            controls.setHgap(CONTROLS_DEFAULT_HORIZONTAL_SPACING);

            controls.addRow(0, namedLabel, colorCombo);
            controls.addRow(1, pickerLabel, picker);
            return controls;
        }
    }
}
