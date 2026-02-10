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
package au.gov.asd.tac.constellation.plugins.gui;

import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameterListener;
import au.gov.asd.tac.constellation.plugins.parameters.types.ColorParameterType.ColorParameterValue;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.gui.field.ColorInput;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputListener;

/**
 * A color picker which is the GUI element corresponding to a
 * {@link PluginParameter} of
 * {@link au.gov.asd.tac.constellation.plugins.parameters.types.ColorParameterType}.
 * <p>
 * Picking a color will set the color value for the underlying
 * {@link PluginParameter}.
 *
 * @see au.gov.asd.tac.constellation.plugins.parameters.types.ColorParameterType
 * @author algol
 * @author capricornunicorn123
 */
public final class ColorInputPane extends ParameterInputPane<ColorParameterValue, ConstellationColor> {

    private static final Logger LOGGER = Logger.getLogger(ColorInputPane.class.getName());
    
    public ColorInputPane(final PluginParameter<ColorParameterValue> parameter) {
        super(new ColorInput(), parameter);
        
        // Set the initial Field value
        setFieldValue(parameter.getParameterValue().get());
    }

    @Override
    public final ConstellationInputListener getFieldChangeListener(PluginParameter<ColorParameterValue> parameter) {
        return (ConstellationInputListener<ConstellationColor>) (ConstellationColor newValue) -> {
            parameter.setColorValue(newValue);
        };
    }
    
    @Override 
    public final PluginParameterListener getPluginParameterListener() {
        return (PluginParameter<?> parameter, ParameterChange change) -> {
            Platform.runLater(() -> {
                switch (change) {
                    case VALUE -> {
                        // Don't change the value if it isn't necessary.
                        final ConstellationColor param = parameter.getColorValue();
                        if (getInputReference().isValid()){
                            if (param != null && !param.equals(getInputReference().getValue())) {
                                getInputReference().setValue(param);
                            }
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

        getChildren().add(hbox);
    }

    private ComboBox<ConstellationColor> makeNamedCombo() {
        final ObservableList<ConstellationColor> namedColors = FXCollections.observableArrayList();
        for (final ConstellationColor c : ConstellationColor.NAMED_COLOR_LIST) {
            namedColors.add(c);
        }
        final ComboBox<ConstellationColor> namedComboBox = new ComboBox<>(namedColors);
        namedComboBox.setValue(ConstellationColor.WHITE);
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
        namedComboBox.setCellFactory(cellFactory);
        namedComboBox.setButtonCell(cellFactory.call(null));

        return namedComboBox;
    }
}
