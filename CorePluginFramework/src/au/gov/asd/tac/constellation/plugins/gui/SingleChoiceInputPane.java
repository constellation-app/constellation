/*
 * Copyright 2010-2020 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.plugins.parameters.RecentParameterValues;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;

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

    private final ComboBox<ParameterValue> field;
    
    private String parameterId;

    // Keep track of entered characters so the user can enter the prefixes of choices to get there quicker.
    private String prefix;
    private static final Logger LOGGER = Logger.getLogger(SingleChoiceInputPane.class.getName());

    public SingleChoiceInputPane(final PluginParameter<SingleChoiceParameterValue> parameter) {
        field = new ComboBox<>();
        field.setPromptText(parameter.getDescription());
        field.setItems(FXCollections.observableList(SingleChoiceParameterType.getOptionsData(parameter)));
        final ParameterValue initialValue = parameter.getParameterValue();
        if (initialValue.getObjectValue() != null) {
            field.getSelectionModel().select(initialValue);
        }
        field.setEditable(false);
        field.setPrefWidth(DEFAULT_WIDTH);
        field.setDisable(!parameter.isEnabled());
        field.setManaged(parameter.isVisible());
        field.setVisible(parameter.isVisible());
        this.setManaged(parameter.isVisible());
        this.setVisible(parameter.isVisible());

        if (parameter.getParameterValue().getGuiInit() != null) {
            parameter.getParameterValue().getGuiInit().init(field);
        }

        prefix = "";

        // When moving in or out of the field, clear the prefix.
        field.focusedProperty().addListener(listener -> {
            prefix = "";
        });

        // When a character is typed, modify the prefix and find the matching option.
        field.setOnKeyTyped(event -> {
            final int c = (int) event.getCharacter().charAt(0);
            if (c == 8) {
                // Delete a character.
                if (prefix.length() > 0) {
                    prefix = prefix.substring(0, prefix.length() - 1);
                }
            } else if (c == 127) {
                // Delete the entire prefix.
                prefix = "";
            } else if (!Character.isISOControl(c)) {
                // No control characters (TAB in particular).
                // Add the new character to the end of the prefix.
                prefix += event.getCharacter().toLowerCase();
            }

            // Find the matching option and select it.
            final ObservableList<ParameterValue> items = field.getItems();
            for (int ix = 0; ix < items.size(); ix++) {
                final ParameterValue item = items.get(ix);
                if (item.toString().toLowerCase().startsWith(prefix)) {
                    field.getSelectionModel().clearAndSelect(ix);
                    break;
                }
            }
        });

        field.setOnAction((final ActionEvent t) -> {
            final ParameterValue pv = field.getSelectionModel().getSelectedItem();
            SingleChoiceParameterType.setChoiceData(parameter, field.getSelectionModel().getSelectedItem());
        });

        parameter.addListener((final PluginParameter<?> scParameter, final ParameterChange change) -> {
            Platform.runLater(() -> {
                assert(scParameter.getParameterValue() instanceof SingleChoiceParameterValue);
                final SingleChoiceParameterValue scParameterValue = (SingleChoiceParameterValue) scParameter.getParameterValue();
                switch (change) {
                    case VALUE:
                        // Don't change the value if it isn't necessary.
                        final ParameterValue param = scParameterValue.getChoiceData();
                        final ParameterValue value = field.getSelectionModel().getSelectedItem();
                        if (!Objects.equals(value, param)) {
                            field.getSelectionModel().select(param);
                        }
                        break;
                    case PROPERTY:
                        final List<ParameterValue> options = scParameterValue.getOptionsData();
                        EventHandler<ActionEvent> handler = field.getOnAction();
                        field.setOnAction(null);
                        field.setItems(FXCollections.observableList(options));
                        field.setOnAction(handler);

                        // Only keep the value if it's in the new choices.
                        if (options.contains(scParameterValue.getChoiceData())) {
                            field.getSelectionModel().select(scParameter.getSingleChoice());
                        } else {
                            field.getSelectionModel().clearSelection();
                        }

                        break;
                    case ENABLED:
                        field.setDisable(scParameter.isEnabled());
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
        parameterId = parameter.getId();
        List<String> singleChoiceRecentValues = RecentParameterValues.getRecentValues(parameterId);
        if (singleChoiceRecentValues != null) {
            SingleChoiceParameterType.setChoiceData(parameter, new StringParameterValue(singleChoiceRecentValues.get(0)));
        }
    }
}
