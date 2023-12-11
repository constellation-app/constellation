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

import au.gov.asd.tac.constellation.graph.attribute.TimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.DefaultGetter;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import java.time.LocalTime;
import java.time.ZoneOffset;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class TimeEditorFactory extends AttributeValueEditorFactory<LocalTime> {

    @Override
    public AbstractEditor<LocalTime> createEditor(final EditOperation editOperation, final DefaultGetter<LocalTime> defaultGetter, final ValueValidator<LocalTime> validator, final String editedItemName, final LocalTime initialValue) {
        return new TimeEditor(editOperation, defaultGetter, validator, editedItemName, initialValue);
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
        private CheckBox noValueCheckBox;

        protected TimeEditor(final EditOperation editOperation, final DefaultGetter<LocalTime> defaultGetter, final ValueValidator<LocalTime> validator, final String editedItemName, final LocalTime initialValue) {
            super(editOperation, defaultGetter, validator, editedItemName, initialValue);
        }

        @Override
        public void updateControlsWithValue(final LocalTime value) {
            noValueCheckBox.setSelected(false);
            if (value != null) {
                hourSpinner.getValueFactory().setValue(value.getHour());
                minSpinner.getValueFactory().setValue(value.getMinute());
                secSpinner.getValueFactory().setValue(value.getSecond());
                milliSpinner.getValueFactory().setValue(value.getNano() / NANOSECONDS_IN_MILLISECOND);
            }
        }

        @Override
        protected LocalTime getValueFromControls() throws ControlsInvalidException {
            if (noValueCheckBox.isSelected()) {
                return null;
            }
            if (hourSpinner.getValue() == null || minSpinner.getValue() == null
                    || secSpinner.getValue() == null || milliSpinner.getValue() == null) {
                throw new ControlsInvalidException("Time spinners must have numeric values");
            }
            return LocalTime.of(hourSpinner.getValue(), minSpinner.getValue(),
                    secSpinner.getValue(), milliSpinner.getValue() * NANOSECONDS_IN_MILLISECOND);
        }

        @Override
        protected Node createEditorControls() {
            final GridPane controls = new GridPane();
            controls.setAlignment(Pos.CENTER);
            controls.setVgap(CONTROLS_DEFAULT_VERTICAL_SPACING);

            final HBox timeSpinnerContainer = createTimeSpinners();

            noValueCheckBox = new CheckBox(NO_VALUE_LABEL);
            noValueCheckBox.setAlignment(Pos.CENTER);
            noValueCheckBox.selectedProperty().addListener((v, o, n) -> {
                hourSpinner.setDisable(noValueCheckBox.isSelected());
                minSpinner.setDisable(noValueCheckBox.isSelected());
                secSpinner.setDisable(noValueCheckBox.isSelected());
                milliSpinner.setDisable(noValueCheckBox.isSelected());
                update();
            });

            controls.addRow(0, timeSpinnerContainer);
            controls.addRow(1, noValueCheckBox);
            return controls;
        }

        private HBox createTimeSpinners() {
            hourSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23));
            minSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59));
            secSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59));
            milliSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999));
            hourSpinner.getValueFactory().setValue(LocalTime.now(ZoneOffset.UTC).getHour());
            minSpinner.getValueFactory().setValue(LocalTime.now(ZoneOffset.UTC).getMinute());
            secSpinner.getValueFactory().setValue(LocalTime.now(ZoneOffset.UTC).getSecond());
            milliSpinner.getValueFactory().setValue(0);

            final HBox timeSpinnerContainer = new HBox(CONTROLS_DEFAULT_VERTICAL_SPACING);

            final Label hourSpinnerLabel = new Label("hr:");
            hourSpinnerLabel.setId(LABEL);
            hourSpinnerLabel.setLabelFor(hourSpinner);

            final Label minSpinnerLabel = new Label("min:");
            minSpinnerLabel.setId(LABEL);
            minSpinnerLabel.setLabelFor(minSpinner);

            final Label secSpinnerLabel = new Label("sec:");
            secSpinnerLabel.setId(LABEL);
            secSpinnerLabel.setLabelFor(secSpinner);

            final Label milliSpinnerLabel = new Label("ms:");
            milliSpinnerLabel.setId(LABEL);
            milliSpinnerLabel.setLabelFor(milliSpinner);

            hourSpinner.setPrefWidth(NUMBER_SPINNER_WIDTH);
            minSpinner.setPrefWidth(NUMBER_SPINNER_WIDTH);
            secSpinner.setPrefWidth(NUMBER_SPINNER_WIDTH);
            milliSpinner.setPrefWidth(MILLIS_SPINNER_WIDTH);

            hourSpinner.setEditable(true);
            minSpinner.setEditable(true);
            secSpinner.setEditable(true);
            milliSpinner.setEditable(true);

            hourSpinner.valueProperty().addListener((o, n, v) -> update());
            minSpinner.valueProperty().addListener((o, n, v) -> update());
            secSpinner.valueProperty().addListener((o, n, v) -> update());
            milliSpinner.valueProperty().addListener((o, n, v) -> update());

            final VBox hourLabelNode = new VBox(5);
            hourLabelNode.getChildren().addAll(hourSpinnerLabel, hourSpinner);
            final VBox minLabelNode = new VBox(5);
            minLabelNode.getChildren().addAll(minSpinnerLabel, minSpinner);
            final VBox secLabelNode = new VBox(5);
            secLabelNode.getChildren().addAll(secSpinnerLabel, secSpinner);
            final VBox milliLabelNode = new VBox(5);
            milliLabelNode.getChildren().addAll(milliSpinnerLabel, milliSpinner);

            timeSpinnerContainer.getChildren().addAll(hourLabelNode, minLabelNode, secLabelNode, milliLabelNode);

            return timeSpinnerContainer;
        }
    }
}
