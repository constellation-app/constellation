/*
 * Copyright 2010-2023 Australian Signals Directorate
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import org.apache.commons.lang3.StringUtils;

/**
 * A text box allowing entry of passwords corresponding to a {@link PluginParameter} of
 * {@link au.gov.asd.tac.constellation.plugins.parameters.types.PasswordParameterType}.
 * <p>
 * Editing the value in the text box will display with "*" and set the value for the
 * underlying {@link PluginParameter}.
 *
 * @author Auriga2
 */
public class PasswordInputPane extends HBox {

    public static final int DEFAULT_WIDTH = 300;
    public static final int INTEGER_WIDTH = 75;

    private final TextInputControl field;
    private final boolean required;
    private static final Logger LOGGER = Logger.getLogger(PasswordInputPane.class.getName());

    public PasswordInputPane(final PluginParameter<?> parameter) {
        required = parameter.isRequired();

        field = new PasswordField();

        field.setPromptText(parameter.getDescription());
        if (parameter.getObjectValue() != null) {
            field.setText(parameter.getStringValue());
        }

        field.setPrefWidth(DEFAULT_WIDTH);

        if (parameter.getParameterValue().getGuiInit() != null) {
            parameter.getParameterValue().getGuiInit().init(field);
        }
        // If parameter is enabled, ensure widget is both enabled and editable.
        field.setEditable(parameter.isEnabled());
        field.setDisable(!parameter.isEnabled());
        field.setManaged(parameter.isVisible());
        field.setVisible(parameter.isVisible());
        this.setManaged(parameter.isVisible());
        this.setVisible(parameter.isVisible());

        final Tooltip tooltip = new Tooltip("");
        tooltip.setStyle("-fx-text-fill: white;");
        field.textProperty().addListener((ov, t, t1) -> {
            final String error = parameter.validateString(field.getText());
            if ((required && StringUtils.isBlank(field.getText())) || error != null) {
                // if error is blank, the situation must be that a required parameter is blank
                tooltip.setText(StringUtils.isNotBlank(error) ? error : "Value is required!");
                field.setTooltip(tooltip);
                field.setId("invalid");
            } else {
                tooltip.setText("");
                field.setTooltip(null);
                field.setId("");
            }

            parameter.setStringValue(field.getText());
        });

        parameter.addListener((pluginParameter, change) -> Platform.runLater(() -> {
                switch (change) {
                    case VALUE -> {
                        // Don't change the value if it isn't necessary.
                        // Setting the text changes the cursor position, which makes it look like text is
                        // being entered right-to-left.
                        final String param = parameter.getStringValue();
                        if (!field.getText().equals(param)) {
                            field.setText(param != null ? param : "");
                        }
                    }
                    case ENABLED -> {
                        // If enabled, then ensure widget is both editable and enabled.
                        field.setEditable(pluginParameter.isEnabled());
                        field.setDisable(!pluginParameter.isEnabled());
                    }
                    case VISIBLE -> {
                        field.setManaged(parameter.isVisible());
                        field.setVisible(parameter.isVisible());
                        this.setVisible(parameter.isVisible());
                        this.setManaged(parameter.isVisible());
                    }
                   default -> LOGGER.log(Level.FINE, "ignoring parameter change type {0}.", change);
                }
            }));
        final HBox fieldHBox = new HBox();
        fieldHBox.setSpacing(2);
        fieldHBox.getChildren().add(field);
        getChildren().add(fieldHBox);
    }
}

