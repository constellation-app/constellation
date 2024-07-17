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
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameterController;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.apache.commons.lang3.StringUtils;

/**
 * A button which is the GUI element corresponding to a {@link PluginParameter}
 * of
 * {@link au.gov.asd.tac.constellation.plugins.parameters.types.ActionParameterType}.
 * <p>
 * This button will fire a {@link ParameterChange} for the underlying
 * {@link PluginParameter}. Plugins using this should call
 * {@link au.gov.asd.tac.constellation.plugins.parameters.PluginParameters#addController(String, PluginParameterController) }
 * to the parameter inside
 * {@link au.gov.asd.tac.constellation.plugins.Plugin#createParameters} so that
 * the desired action will be performed after the ParameterChange is fired.
 *
 * @see
 * au.gov.asd.tac.constellation.plugins.parameters.types.ActionParameterType
 * @author algol
 */
public class ActionInputPane extends Pane {

    private final Button field;
    private static final Logger LOGGER = Logger.getLogger(ActionInputPane.class.getName());

    public ActionInputPane(final PluginParameter<?> parameter) {
        field = new Button();
        String label = parameter.getStringValue();
        if (StringUtils.isBlank(label)) {
            label = parameter.getName();
        }
        field.setText(label);

        String icon = parameter.getIcon();
        if (icon != null) {
            ImageView img = new ImageView(icon);
            field.setGraphic(img);
        }
        field.setManaged(parameter.isVisible());
        field.setVisible(parameter.isVisible());
        this.setManaged(parameter.isVisible());
        this.setVisible(parameter.isVisible());

        field.setOnAction(event -> 
            parameter.fireNoChange());

        parameter.addListener((final PluginParameter<?> pluginParameter, final ParameterChange change) -> Platform.runLater(() -> {
                switch (change) {
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
