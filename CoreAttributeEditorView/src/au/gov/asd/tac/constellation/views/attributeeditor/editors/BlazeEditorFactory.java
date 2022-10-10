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
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.BlazeAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.Blaze;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.DefaultGetter;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.util.Callback;
import org.openide.util.lookup.ServiceProvider;

/**
 * Blaze Editor Factory
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class BlazeEditorFactory extends AttributeValueEditorFactory<Blaze> {

    @Override
    public AbstractEditor<Blaze> createEditor(final EditOperation editOperation, final DefaultGetter<Blaze> defaultGetter, final ValueValidator<Blaze> validator, final String editedItemName, final Blaze initialValue) {
        return new BlazeEditor(editOperation, defaultGetter, validator, editedItemName, initialValue);
    }

    @Override
    public String getAttributeType() {
        return BlazeAttributeDescription.ATTRIBUTE_NAME;
    }

    public class BlazeEditor extends AbstractEditor<Blaze> {

        private CheckBox noValueCheckBox;
        private TextField angleTextField;
        private ColorPicker picker;

        protected BlazeEditor(final EditOperation editOperation, final DefaultGetter<Blaze> defaultGetter, final ValueValidator<Blaze> validator, final String editedItemName, final Blaze initialValue) {
            super(editOperation, defaultGetter, validator, editedItemName, initialValue);
        }

        @Override
        public void updateControlsWithValue(final Blaze value) {
            noValueCheckBox.setSelected(value == null);
            if (value != null) {
                angleTextField.setText(String.valueOf(value.getAngle()));
                picker.setValue(value.getColor().getJavaFXColor());
            }
        }

        @Override
        protected Blaze getValueFromControls() throws ControlsInvalidException {
            if (noValueCheckBox.isSelected()) {
                return null;
            }

            try {
                final int angle = Integer.parseInt(angleTextField.getText());
                if (angle >= 360) {
                    throw new ControlsInvalidException("Blaze angle must be in range [0, 360)");
                }
                return new Blaze(angle, ConstellationColor.fromFXColor(picker.getValue()));
            } catch (final NumberFormatException ex) {
                throw new ControlsInvalidException("Blaze angle must be a number.");
            }
        }

        @Override
        protected Node createEditorControls() {
            final GridPane controls = new GridPane();
            controls.setAlignment(Pos.CENTER);
            controls.setVgap(CONTROLS_DEFAULT_VERTICAL_SPACING);
            controls.setHgap(CONTROLS_DEFAULT_HORIZONTAL_SPACING);

            final Label angleLabel = new Label("Angle:");
            final HBox angleHBox = new HBox();
            angleTextField = new TextField();
            angleTextField.textProperty().addListener((v, o, n) -> update());
            final Button northButton = new Button("N");
            northButton.setOnAction(e -> angleTextField.setText("0"));
            final Button northEastButton = new Button("NE");
            northEastButton.setOnAction(e -> angleTextField.setText("45"));
            final Button eastButton = new Button("E");
            eastButton.setOnAction(e -> angleTextField.setText("90"));
            final Button southEastButton = new Button("SE");
            southEastButton.setOnAction(e -> angleTextField.setText("135"));
            final Button southButton = new Button("S");
            southButton.setOnAction(e -> angleTextField.setText("180"));
            final Button southWestButton = new Button("SW");
            southWestButton.setOnAction(e -> angleTextField.setText("225"));
            final Button westButton = new Button("W");
            westButton.setOnAction(e -> angleTextField.setText("270"));
            final Button northWestButton = new Button("NW");
            northWestButton.setOnAction(e -> angleTextField.setText("315"));
            angleHBox.getChildren().addAll(northButton, northEastButton, eastButton, southEastButton, southButton, southWestButton, westButton, northWestButton);
            angleLabel.setLabelFor(angleHBox);

            final Label colorLabel = new Label("Color");
            colorLabel.setFont(Font.font(Font.getDefault().getFamily(), FontPosture.ITALIC, 14));
            final Separator separator = new Separator();
            final VBox colorSeparator = new VBox(colorLabel, separator);

            final Label namedLabel = new Label("Named:");
            final ObservableList<ConstellationColor> namedColors = FXCollections.observableArrayList();
            for (final ConstellationColor c : ConstellationColor.NAMED_COLOR_LIST) {
                namedColors.add(c);
            }
            final ComboBox<ConstellationColor> colorCombo = new ComboBox<>(namedColors);
            final Callback<ListView<ConstellationColor>, ListCell<ConstellationColor>> cellFactory = (final ListView<ConstellationColor> p) -> new ListCell<ConstellationColor>() {
                @Override
                protected void updateItem(final ConstellationColor item, boolean empty) {
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
            namedLabel.setLabelFor(colorCombo);

            colorCombo.valueProperty().addListener((o, oldValue, newValue) -> {
                if (newValue != null && !newValue.equals(oldValue)) {
                    picker.setValue(newValue.getJavaFXColor());
                }
            });

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

            noValueCheckBox = new CheckBox(NO_VALUE_LABEL);
            noValueCheckBox.setAlignment(Pos.CENTER);
            noValueCheckBox.selectedProperty().addListener((v, o, n) -> {
                angleHBox.getChildren().forEach(button -> button.setDisable(noValueCheckBox.isSelected()));
                angleTextField.setDisable(noValueCheckBox.isSelected());
                colorCombo.setDisable(noValueCheckBox.isSelected());
                picker.setDisable(noValueCheckBox.isSelected());
                update();
            });

            controls.addRow(0, angleLabel, angleTextField);
            controls.add(angleHBox, 0, 1, 2, 1);
            controls.add(colorSeparator, 0, 2, 2, 1);
            controls.addRow(3, namedLabel, colorCombo);
            controls.addRow(4, pickerLabel, picker);
            controls.addRow(5, noValueCheckBox);
            return controls;
        }
    }
}
