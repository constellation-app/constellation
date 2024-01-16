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

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType.MultiChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterValue;
import au.gov.asd.tac.constellation.utilities.gui.MultiChoiceInputField;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.layout.HBox;

/**
 * A drop-down combo box allowing multiple selections, which is the GUI element
 * corresponding to a {@link PluginParameter} of
 * {@link au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType}.
 * <p>
 * Changing the selected items in the drop down will set the checked data for
 * the underlying {@link PluginParameter}.
 *
 * @see
 * au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType
 *
 * @author twinkle2_little
 */
public class MultiChoiceInputPane extends HBox {

    private static final Logger LOGGER = Logger.getLogger(MultiChoiceInputPane.class.getName());
    
    public static final int DEFAULT_WIDTH = 300;

    private final ObservableList<ParameterValue> options = FXCollections.observableArrayList();
    private final MultiChoiceInputField<ParameterValue> field;
    private boolean isAdjusting = false;
    
    public MultiChoiceInputPane(final PluginParameter<MultiChoiceParameterValue> parameter) {
        options.addAll(MultiChoiceParameterType.getOptionsData(parameter));
        field = new MultiChoiceInputField<>(options);
        field.setPromptText(parameter.getDescription());
        if (parameter.getParameterValue().getGuiInit() != null) {
            parameter.getParameterValue().getGuiInit().init(field);
        }
        field.setDisable(!parameter.isEnabled());
        field.setManaged(parameter.isVisible());
        field.setVisible(parameter.isVisible());
        this.setManaged(parameter.isVisible());
        this.setVisible(parameter.isVisible());

        // Set properties before adding listener to ensure unwanted onChanged events do not fire.
        Platform.runLater(() -> {
            @SuppressWarnings("unchecked") // Below cast will always work because getChoicesData returns List<? extends ParameterValue>.
            final List<ParameterValue> checkedItems = (List<ParameterValue>) MultiChoiceParameterType.getChoicesData(parameter);
            checkedItems.stream().forEach(checked -> {
                final BooleanProperty bp = field.getItemBooleanProperty(checked);
                if (bp != null) {
                    bp.setValue(true);
                }
            });

            field.getCheckModel().getCheckedItems().addListener((final ListChangeListener.Change<? extends ParameterValue> c) -> {
                if (!isAdjusting) {
                    MultiChoiceParameterType.setChoicesData(parameter, field.getCheckModel().getCheckedItems());
                }
            });
        });

        parameter.addListener((pluginParameter, change) -> Platform.runLater(() -> {
                @SuppressWarnings("unchecked") //mcPluginParameter is a MultiChoiceParameter
                final PluginParameter<MultiChoiceParameterValue> mcPluginParameter = (PluginParameter<MultiChoiceParameterValue>) pluginParameter;
                switch (change) {
                    case VALUE:
                        isAdjusting = true;
                        field.getCheckModel().clearChecks(); //The order matters here- this should be called before clearing the options.
                        options.clear();
                        options.addAll(MultiChoiceParameterType.getOptionsData(mcPluginParameter));
                        @SuppressWarnings("unchecked") //checkedItems will be list of parameter values
                        final List<ParameterValue> checkedItems = (List<ParameterValue>) MultiChoiceParameterType.getChoicesData(mcPluginParameter);

                        field.getCheckModel().getCheckedItems();
                        checkedItems.forEach(checked -> {
                            field.getCheckModel().check(checked);
                        });
                        
                        // give a visual indicator if a required parameter is empty
                        field.setId(mcPluginParameter.isRequired() && field.getCheckModel().isEmpty() ? "invalid selection" : "");
                        field.setStyle("invalid selection".equals(field.getId()) ? "-fx-color: #8A1D1D" : "");

                        isAdjusting = false;
                        break;
                    case ENABLED:
                        field.setDisable(!pluginParameter.isEnabled());
                        break;
                    case VISIBLE:
                        field.setManaged(parameter.isVisible());
                        field.setVisible(parameter.isVisible());
                        this.setVisible(parameter.isVisible());
                        this.setManaged(parameter.isVisible());
                        break;
                    default:
                        LOGGER.log(Level.FINE, "ignoring parameter change type {0}.", change);
                        break;
                }
            }));

        //field width causes buttons to sit in pane space when available but retract to the same size as buttons if needed.
        field.setPrefWidth(DEFAULT_WIDTH);
        field.setMinWidth(50);
        
        final HBox fieldAndButtons = new HBox();
        fieldAndButtons.setSpacing(2);
        fieldAndButtons.getChildren().addAll(field);
        getChildren().add(fieldAndButtons);
    }
}
