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
package au.gov.asd.tac.constellation.plugins.gui;

import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.HBox;
import org.controlsfx.control.SearchableComboBox;

/**
 * A drop-down combo box which is the GUI element corresponding to a
 * {@link PluginParameter} of
 * {@link au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType}.
 * <p>
 * Selecting an item from the drop-down will set the choice data for the
 * underlying {@link PluginParameter}.
 *
 * @see
 * au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType
 *
 * @author ruby_crucis
 */
public class SingleChoiceInputPane extends HBox {

    public static final int DEFAULT_WIDTH = 300;

    private final SearchableComboBox<ParameterValue> field;
    private boolean initialRun = true;

    private static final Logger LOGGER = Logger.getLogger(SingleChoiceInputPane.class.getName());

    public SingleChoiceInputPane(final PluginParameter<SingleChoiceParameterValue> parameter) {
        field = new SearchableComboBox<>();
        field.setPromptText(parameter.getDescription());
        field.setItems(FXCollections.observableList(SingleChoiceParameterType.getOptionsData(parameter)));
        final ParameterValue initialValue = parameter.getParameterValue();
        if (initialValue.getObjectValue() != null) {
            field.getSelectionModel().select(initialValue);
        }

        field.setPrefWidth(DEFAULT_WIDTH);
        field.setDisable(!parameter.isEnabled());
        field.setManaged(parameter.isVisible());
        field.setVisible(parameter.isVisible());
        this.setManaged(parameter.isVisible());
        this.setVisible(parameter.isVisible());

        if (parameter.getParameterValue().getGuiInit() != null) {
            parameter.getParameterValue().getGuiInit().init(field);
        }

        field.setOnAction((final ActionEvent t) -> {
            SingleChoiceParameterType.setChoiceData(parameter, field.getSelectionModel().getSelectedItem());
        });

        parameter.addListener((final PluginParameter<?> scParameter, final ParameterChange change) -> {
            Platform.runLater(() -> {
                assert(scParameter.getParameterValue() instanceof SingleChoiceParameterValue);
                final SingleChoiceParameterValue scParameterValue = (SingleChoiceParameterValue) scParameter.getParameterValue();
                switch (change) {
                    case VALUE:
                        // Don't change the value if it isn't necessary.
                        final List<ParameterValue> param = scParameterValue.getOptionsData();
                        final ParameterValue value = field.getSelectionModel().getSelectedItem();

                        //Checks that the currently selected value is in the new parameters list
                        if (!param.contains(value)) {
                            field.getSelectionModel().select(scParameterValue.getChoiceData());
                        }
                        break;
                    case PROPERTY:
                        final ObservableList<ParameterValue> options = FXCollections.observableArrayList();
                        EventHandler<ActionEvent> handler = field.getOnAction();
                        field.setOnAction(null);

                        options.setAll(scParameterValue.getOptionsData());
                        field.setItems(options);
                        field.setOnAction(handler);

                        if (initialRun){
                            // This is a workaround to fix dynamically changing drop downs.
                            // Otherwise when the Constellation is loaded for the first time,
                            // such lists wouldn't populate until clicked twice on the arrow.
                            // E.g. `Type Category` drop down in `Select Top N` plugin
                            field.show();
                            field.hide();
                            field.requestFocus();
                            initialRun = false;
                        }

                        // Only keep the value if it's in the new choices.
                        if (options.contains(scParameterValue.getChoiceData())) {
                            field.getSelectionModel().select(scParameter.getSingleChoice());
                        } else {
                            field.getSelectionModel().clearSelection();
                        }

                        break;


                    case ENABLED:
                        field.setDisable(!scParameter.isEnabled());
                        break;
                    case VISIBLE:
                        field.setManaged(scParameter.isVisible());
                        field.setVisible(scParameter.isVisible());
                        this.setVisible(scParameter.isVisible());
                        this.setManaged(scParameter.isVisible());
                        break;
                    default:
                        LOGGER.log(Level.FINE, "ignoring parameter change type {0}.", change);
                        break;
                }
            });
        });

        getChildren().add(field);
    }
}
