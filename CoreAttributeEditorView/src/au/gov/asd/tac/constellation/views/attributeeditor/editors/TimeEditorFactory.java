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

import au.gov.asd.tac.constellation.graph.attribute.TimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import java.time.LocalTime;
import java.time.ZoneOffset;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.openide.util.lookup.ServiceProvider;

/**
 * Editor Factory for attributes of type time
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class TimeEditorFactory extends AttributeValueEditorFactory<LocalTime> {

    @Override
    public AbstractEditor<LocalTime> createEditor(final String editedItemName, final EditOperation editOperation, final ValueValidator<LocalTime> validator, final LocalTime defaultValue, final LocalTime initialValue) {
        return new TimeEditor(editedItemName, editOperation, validator, defaultValue, initialValue);
    }

    @Override
    public String getAttributeType() {
        return TimeAttributeDescription.ATTRIBUTE_NAME;
    }

    public class TimeEditor extends AbstractEditor<LocalTime> {

        private static final int NUMBER_SPINNER_WIDTH = 55;
        private static final int MILLIS_SPINNER_WIDTH = 60;

        private static final int NANOSECONDS_IN_MILLISECOND = 1000000;

        private static final String LABEL = "label";

        private Spinner<Integer> hourSpinner;
        private Spinner<Integer> minSpinner;
        private Spinner<Integer> secSpinner;
        private Spinner<Integer> milliSpinner;

        protected TimeEditor(final String editedItemName, final EditOperation editOperation, final ValueValidator<LocalTime> validator, final LocalTime defaultValue, final LocalTime initialValue) {
            super(editedItemName, editOperation, validator, defaultValue, initialValue, true);
        }
        
        protected Integer getHourValue() {
            return hourSpinner.getValue();
        }
        
        protected Integer getMinValue() {
            return minSpinner.getValue();
        }
        
        protected Integer getSecValue() {
            return secSpinner.getValue();
        }
        
        protected Integer getMilliValue() {
            return milliSpinner.getValue();
        }

        @Override
        public void updateControlsWithValue(final LocalTime value) {
            if (value != null) {
                hourSpinner.getValueFactory().setValue(value.getHour());
                minSpinner.getValueFactory().setValue(value.getMinute());
                secSpinner.getValueFactory().setValue(value.getSecond());
                milliSpinner.getValueFactory().setValue(value.getNano() / NANOSECONDS_IN_MILLISECOND);
            }
        }

        @Override
        protected LocalTime getValueFromControls() throws ControlsInvalidException {
            if (hourSpinner.getValue() == null || minSpinner.getValue() == null
                    || secSpinner.getValue() == null || milliSpinner.getValue() == null) {
                throw new ControlsInvalidException("Time spinners must have numeric values");
            }

            return LocalTime.of(hourSpinner.getValue(), minSpinner.getValue(),
                    secSpinner.getValue(), milliSpinner.getValue() * NANOSECONDS_IN_MILLISECOND);
        }

        @Override
        protected Node createEditorControls() {
            final HBox timeSpinnerContainer = createTimeSpinners();
            
            final VBox controls = new VBox(timeSpinnerContainer);
            controls.setAlignment(Pos.CENTER);
            
            return controls;
        }
        
        private HBox createTimeSpinners() {
            hourSpinner = createTimeSpinner(23, LocalTime.now(ZoneOffset.UTC).getHour(), NUMBER_SPINNER_WIDTH);
            final Label hourSpinnerLabel = createLabel("Hour:", hourSpinner);
            
            minSpinner = createTimeSpinner(59, LocalTime.now(ZoneOffset.UTC).getMinute(), NUMBER_SPINNER_WIDTH);
            final Label minSpinnerLabel = createLabel("Minute:", minSpinner);
            
            secSpinner = createTimeSpinner(59, LocalTime.now(ZoneOffset.UTC).getSecond(), NUMBER_SPINNER_WIDTH);
            final Label secSpinnerLabel = createLabel("Second:", secSpinner);
            
            milliSpinner = createTimeSpinner(999, 0, MILLIS_SPINNER_WIDTH);
            final Label milliSpinnerLabel = createLabel("Millis:", milliSpinner);
            
            final VBox hourLabelNode = new VBox(5, hourSpinnerLabel, hourSpinner);
            final VBox minLabelNode = new VBox(5, minSpinnerLabel, minSpinner);
            final VBox secLabelNode = new VBox(5, secSpinnerLabel, secSpinner);
            final VBox milliLabelNode = new VBox(5, milliSpinnerLabel, milliSpinner);

            return new HBox(CONTROLS_DEFAULT_HORIZONTAL_SPACING, 
                    hourLabelNode, minLabelNode, secLabelNode, milliLabelNode);
        }
        
        /**
         * Creates a spinner for a measurement of time, for the editor
         * 
         * @param maxValue The maximum value on the spinner
         * @param initialValue The initial value on the spinner
         * @param spinnerWidth The preferred width of the spinner
         * @return The newly created spinner object
         */
        private Spinner<Integer> createTimeSpinner(final int maxValue, final int initialValue, final int spinnerWidth) {
            final Spinner<Integer> timeSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, maxValue));
            timeSpinner.getValueFactory().setValue(initialValue);
            timeSpinner.setPrefWidth(spinnerWidth);
            timeSpinner.setEditable(true);
            timeSpinner.valueProperty().addListener((o, n, v) -> update());
            
            return timeSpinner;
        }
        
        /**
         * Creates a label associated with the given time spinner
         * 
         * @param labelText The label text
         * @param associatedObject The object to set the label for
         * @return The newly created label
         */
        private Label createLabel(final String labelText, final Control associatedObject) {
            final Label spinnerLabel = new Label(labelText);
            spinnerLabel.setId(LABEL);
            spinnerLabel.setLabelFor(associatedObject);
            
            return spinnerLabel;
        }
    }
}
