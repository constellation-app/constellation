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
import au.gov.asd.tac.constellation.plugins.parameters.types.ColorParameterType.ColorParameterValue;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;

/**
 * A colour picker which is the GUI element corresponding to a
 * {@link PluginParameter} of
 * {@link au.gov.asd.tac.constellation.plugins.parameters.types.ColorParameterType}.
 * <p>
 * Picking a colour will set the colour value for the underlying
 * {@link PluginParameter}.
 *
 * @see au.gov.asd.tac.constellation.plugins.parameters.types.ColorParameterType
 * @author algol
 */
public class ColorInputPane extends Pane {

    private final ColorPicker field;
    private final ComboBox<ConstellationColor> namedCombo;
    private static final Logger LOGGER = Logger.getLogger(ColorInputPane.class.getName());

    public ColorInputPane(final PluginParameter<ColorParameterValue> parameter) {
        field = new ColorPicker();
        namedCombo = makeNamedCombo();
        final HBox hbox = new HBox(field, namedCombo);

        field.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                final Color opaque = Color.color(newValue.getRed(), newValue.getGreen(), newValue.getBlue());
                if (!opaque.equals(oldValue)) {
                    boolean foundNamedColor = false;
                    for (final ConstellationColor c : ConstellationColor.NAMED_COLOR_LIST) {
                        final Color fxc = c.getJavaFXColor();
                        if (opaque.equals(fxc)) {
                            namedCombo.setValue(c);
                            foundNamedColor = true;
                            break;
                        }
                    }

                    if (!foundNamedColor) {
                        namedCombo.setValue(null);
                    }
                }
            }
        });

        final ColorParameterValue pv = parameter.getParameterValue();

        field.setValue(pv.get().getJavaFXColor());

        if (parameter.getParameterValue().getGuiInit() != null) {
            parameter.getParameterValue().getGuiInit().init(hbox);
        }

        field.setDisable(!parameter.isEnabled());
        field.setManaged(parameter.isVisible());
        field.setVisible(parameter.isVisible());
        this.setManaged(parameter.isVisible());
        this.setVisible(parameter.isVisible());

        namedCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                field.setValue(newValue.getJavaFXColor());
                parameter.setColorValue(ConstellationColor.fromFXColor(field.getValue()));
            }
        });

        field.setOnAction(event -> parameter.setColorValue(ConstellationColor.fromFXColor(field.getValue())));

        parameter.addListener((PluginParameter<?> pluginParameter, ParameterChange change) -> Platform.runLater(() -> {
                switch (change) {
                    case VALUE:
                        // Don't change the value if it isn't necessary.
                        final ConstellationColor param = pluginParameter.getColorValue();
                        if (!param.equals(field.getValue())) {
                            field.setValue(param.getJavaFXColor());
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

        getChildren().add(hbox);
    }

    private ComboBox<ConstellationColor> makeNamedCombo() {
        final ObservableList<ConstellationColor> namedColors = FXCollections.observableArrayList();
        for (final ConstellationColor c : ConstellationColor.NAMED_COLOR_LIST) {
            namedColors.add(c);
        }
        final ComboBox<ConstellationColor> namedCombo = new ComboBox<>(namedColors);
        namedCombo.setValue(ConstellationColor.WHITE);
        final Callback<ListView<ConstellationColor>, ListCell<ConstellationColor>> cellFactory = (final ListView<ConstellationColor> p) -> new ListCell<ConstellationColor>() {
            @Override
            protected void updateItem(final ConstellationColor item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    final Rectangle r = new Rectangle(12, 12, item.getJavaFXColor());
                    r.setStroke(Color.BLACK);
                    setText(item.getName());
                    setGraphic(r);
                }
            }
        };
        namedCombo.setCellFactory(cellFactory);
        namedCombo.setButtonCell(cellFactory.call(null));

        return namedCombo;
    }
}
