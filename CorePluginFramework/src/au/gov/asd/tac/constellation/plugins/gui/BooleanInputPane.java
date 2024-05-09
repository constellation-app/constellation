/*
 * Copyright 2010-2024 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Pane;

/**
 * A check box which is the GUI element corresponding to a
 * {@link PluginParameter} of
 * {@link au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType}.
 * <p>
 * Checking and un-checking the box will set the boolean value for the
 * underlying {@link PluginParameter}.
 *
 * @see
 * au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType
 *
 * @author ruby_crucis
 */
public class BooleanInputPane extends Pane {

    private final CheckBox field;
    private static final Logger LOGGER = Logger.getLogger(BooleanInputPane.class.getName());

    public BooleanInputPane(final PluginParameter<BooleanParameterValue> parameter) {
        field = new CheckBox();
        final BooleanParameterValue pv = parameter.getParameterValue();
        field.setSelected(pv.getValue());

        if (parameter.getParameterValue().getGuiInit() != null) {
            parameter.getParameterValue().getGuiInit().init(field);
        }

        field.setDisable(!parameter.isEnabled());
        field.setManaged(parameter.isVisible());
        field.setVisible(parameter.isVisible());
        this.setManaged(parameter.isVisible());
        this.setVisible(parameter.isVisible());

        field.setOnAction(event -> parameter.setBooleanValue(field.isSelected()));

        parameter.addListener((PluginParameter<?> pluginParameter, ParameterChange change) -> Platform.runLater(() -> {
                switch (change) {
                    case VALUE -> {
                        // Don't change the value if it isn't necessary.
                        final boolean param = pluginParameter.getBooleanValue();
                        if (param != field.isSelected()) {
                            field.setSelected(param);
                        }
                    }
                    case ENABLED -> field.setDisable(!pluginParameter.isEnabled());
                    case VISIBLE -> {
                        field.setManaged(parameter.isVisible());
                        field.setVisible(parameter.isVisible());
                        this.setVisible(parameter.isVisible());
                        this.setManaged(parameter.isVisible());
                    }
                    default -> LOGGER.log(Level.FINE, "ignoring parameter change type {0}.", change);
                }
            }));
        getChildren().add(field);
    }
}
