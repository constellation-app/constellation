/*
 * Copyright 2010-2020 Australian Signals Directorate. All Rights Reserved.
 * 
 * NOTICE: All information contained herein remains the property of the
 * Australian Signals Directorate. The intellectual and technical concepts 
 * contained herein are proprietary to the Australian Signals Directorate 
 * and are protected by copyright law. Dissemination of this information or 
 * reproduction of this material is strictly forbidden unless prior written 
 * permission is obtained from the Australian Signals Directorate.
 */
package au.gov.asd.tac.constellation.views.attributeeditor.editors;

import au.gov.asd.tac.constellation.graph.attribute.IntegerObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import static au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory.AbstractEditor.CONTROLS_DEFAULT_VERTICAL_SPACING;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.DefaultGetter;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class IntegerObjectEditorFactory extends AttributeValueEditorFactory<Integer> {

    @Override
    public AbstractEditor<Integer> createEditor(final EditOperation editOperation, final DefaultGetter<Integer> defaultGetter, final ValueValidator<Integer> validator, final String editedItemName, final Integer initialValue) {
        return new IntegerObjectEditor(editOperation, defaultGetter, validator, editedItemName, initialValue);
    }

    @Override
    public String getAttributeType() {
        return IntegerObjectAttributeDescription.ATTRIBUTE_NAME;
    }

    public class IntegerObjectEditor extends AbstractEditor<Integer> {

        private TextField numberField;
        private CheckBox noValueCheckBox;

        protected IntegerObjectEditor(final EditOperation editOperation, final DefaultGetter<Integer> defaultGetter, final ValueValidator<Integer> validator, final String editedItemName, final Integer initialValue) {
            super(editOperation, defaultGetter, validator, editedItemName, initialValue);
        }

        @Override
        public void updateControlsWithValue(final Integer value) {
            noValueCheckBox.setSelected(value == null);
            if (value != null) {
                numberField.setText(String.valueOf(value));
            }
        }

        @Override
        protected Integer getValueFromControls() throws ControlsInvalidException {
            if (noValueCheckBox.isSelected()) {
                return null;
            }
            try {
                return Integer.parseInt(numberField.getText());
            } catch (final NumberFormatException ex) {
                throw new ControlsInvalidException("Entered value is not an integer.");
            }
        }

        @Override
        protected Node createEditorControls() {
            GridPane controls = new GridPane();
            controls.setAlignment(Pos.CENTER);
            controls.setVgap(CONTROLS_DEFAULT_VERTICAL_SPACING);

            numberField = new TextField();
            numberField.textProperty().addListener((o, n, v) -> {
                update();
            });

            noValueCheckBox = new CheckBox(NO_VALUE_LABEL);
            noValueCheckBox.setAlignment(Pos.CENTER);
            noValueCheckBox.selectedProperty().addListener((v, o, n) -> {
                numberField.setDisable(noValueCheckBox.isSelected());
                update();
            });

            controls.addRow(0, numberField);
            controls.addRow(1, noValueCheckBox);
            return controls;
        }
    }
}
