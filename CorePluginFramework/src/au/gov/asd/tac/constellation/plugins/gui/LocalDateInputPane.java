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
import au.gov.asd.tac.constellation.plugins.parameters.types.LocalDateParameterType.LocalDateParameterValue;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

/**
 * A Date Picker, which is the GUI element corresponding to a
 * {@link PluginParameter} of
 * {@link au.gov.asd.tac.constellation.plugins.parameters.types.LocalDateParameterType}.
 * <p>
 * Selecting a date will set the local date value of the underlying
 * {@link PluginParameter}.
 *
 * @see
 * au.gov.asd.tac.constellation.plugins.parameters.types.LocalDateParameterType
 *
 * @author algol
 */
public class LocalDateInputPane extends Pane {

    private static final String PATTERN = "yyyy-MM-dd";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final Logger LOGGER = Logger.getLogger(LocalDateInputPane.class.getName());

    private final DatePicker field;

    public LocalDateInputPane(final PluginParameter<LocalDateParameterValue> parameter) {
        field = new DatePicker();
        final LocalDateParameterValue pv = parameter.getParameterValue();

        field.setPromptText(PATTERN);
        field.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(final LocalDate date) {
                return date != null ? DATE_FORMATTER.format(date) : "";
            }

            @Override
            public LocalDate fromString(final String s) {
                return StringUtils.isNotBlank(s) ? LocalDate.parse(s, DATE_FORMATTER) : null;
            }
        });

        field.setValue(pv.get());

        if (parameter.getParameterValue().getGuiInit() != null) {
            parameter.getParameterValue().getGuiInit().init(field);
        }

        field.setDisable(!parameter.isEnabled());
        field.setVisible(parameter.isVisible());
        field.setManaged(parameter.isVisible());
        this.setManaged(parameter.isVisible());
        this.setVisible(parameter.isVisible());

        field.setOnAction(event -> parameter.setLocalDateValue(field.getValue()));

        parameter.addListener((PluginParameter<?> pluginParameter, ParameterChange change) -> Platform.runLater(() -> {
                switch (change) {
                    case VALUE:
                        // Don't change the value if it isn't necessary.
                        final LocalDate param = pluginParameter.getLocalDateValue();
                        if (!param.equals(field.getValue())) {
                            field.setValue(param);
                        }
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

        getChildren().add(field);
    }
}
