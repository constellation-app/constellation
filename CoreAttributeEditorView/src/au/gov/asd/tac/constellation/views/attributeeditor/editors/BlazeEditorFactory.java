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
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.BlazeAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.Blaze;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
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
import javafx.util.Callback;
import org.openide.util.lookup.ServiceProvider;

/**
 * Editor Factory for attributes of type blaze
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class BlazeEditorFactory extends AttributeValueEditorFactory<Blaze> {

    @Override
    public AbstractEditor<Blaze> createEditor(final String editedItemName, final EditOperation editOperation, final ValueValidator<Blaze> validator, final Blaze defaultValue, final Blaze initialValue) {
        return new BlazeEditor(editedItemName, editOperation, validator, defaultValue, initialValue);
    }

    @Override
    public String getAttributeType() {
        return BlazeAttributeDescription.ATTRIBUTE_NAME;
    }

    public class BlazeEditor extends AbstractEditor<Blaze> {

        private TextField angleTextField;
        private ColorPicker picker;

        protected BlazeEditor(final String editedItemName, final EditOperation editOperation, final ValueValidator<Blaze> validator, final Blaze defaultValue, final Blaze initialValue) {
            super(editedItemName, editOperation, validator, defaultValue, initialValue, true);
        }

        @Override
        public void updateControlsWithValue(final Blaze value) {
            if (value != null) {
                angleTextField.setText(String.valueOf(value.getAngle()));
                picker.setValue(value.getColor().getJavaFXColor());
            }
        }

        @Override
        protected Blaze getValueFromControls() throws ControlsInvalidException {
            try {
                final int angle = Integer.parseInt(angleTextField.getText());

                if (angle >= 360) {
                    throw new ControlsInvalidException("Blaze angle must be between 0 and 360");
                }

                return new Blaze(angle, ConstellationColor.fromFXColor(picker.getValue()));
            } catch (final NumberFormatException ex) {
                throw new ControlsInvalidException("Blaze angle must be a number.");
            }
        }

        @Override
        protected Node createEditorControls() {
            // create the angle controls
            final Label angleLabel = new Label("Angle:");
            angleTextField = new TextField();
            angleTextField.textProperty().addListener((v, o, n) -> update());
            
            final Button northButton = createAngleButton("N", "0");
            final Button northEastButton = createAngleButton("NE", "45");
            final Button eastButton = createAngleButton("E", "90");
            final Button southEastButton = createAngleButton("SE", "135");
            final Button southButton = createAngleButton("S", "180");
            final Button southWestButton = createAngleButton("SW", "225");
            final Button westButton = createAngleButton("W", "270");
            final Button northWestButton = createAngleButton("NW", "315");
            
            final HBox angleHBox = new HBox(northButton, northEastButton, eastButton, southEastButton, 
                    southButton, southWestButton, westButton, northWestButton);
            angleLabel.setLabelFor(angleHBox);

            // create the color controls
            final Label colorLabel = new Label("Color");
            final Separator separator = new Separator();
            final VBox colorSeparator = new VBox(colorLabel, separator);

            final Label namedLabel = new Label("Named:");
            final ObservableList<ConstellationColor> namedColors = FXCollections.observableArrayList(ConstellationColor.NAMED_COLOR_LIST);
            final ComboBox<ConstellationColor> colorCombo = new ComboBox<>(namedColors);
            namedLabel.setLabelFor(colorCombo);
            colorCombo.valueProperty().addListener((o, oldValue, newValue) -> {
                if (newValue != null && !newValue.equals(oldValue)) {
                    picker.setValue(newValue.getJavaFXColor());
                }
            });
            
            final Callback<ListView<ConstellationColor>, ListCell<ConstellationColor>> cellFactory = p -> new ListCell<>() {
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
            
            final GridPane controls = new GridPane(CONTROLS_DEFAULT_HORIZONTAL_SPACING, CONTROLS_DEFAULT_VERTICAL_SPACING);
            controls.setAlignment(Pos.CENTER);

            controls.addRow(0, angleLabel, angleTextField);
            controls.add(angleHBox, 0, 1, 2, 1);
            controls.add(colorSeparator, 0, 2, 2, 1);
            controls.addRow(3, namedLabel, colorCombo);
            controls.addRow(4, pickerLabel, picker);
            return controls;
        }
        
        /**
         * Creates a button for altering the angle text field
         * 
         * @param buttonLabel the label of the button
         * @param angle the angle which will be applied to the text field when clicked
         * @return the newly created button
         */
        private Button createAngleButton(final String buttonLabel, final String angle) {
            final Button button = new Button(buttonLabel);
            button.setOnAction(e -> angleTextField.setText(angle));
            return button;
        }
    }
}
