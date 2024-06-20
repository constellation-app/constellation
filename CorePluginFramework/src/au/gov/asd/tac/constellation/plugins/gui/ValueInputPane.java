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
import static au.gov.asd.tac.constellation.plugins.parameters.ParameterChange.ENABLED;
import static au.gov.asd.tac.constellation.plugins.parameters.ParameterChange.VALUE;
import static au.gov.asd.tac.constellation.plugins.parameters.ParameterChange.VISIBLE;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameterListener;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldConstants.TextType;
import au.gov.asd.tac.constellation.utilities.gui.RecentValue.RecentValueUtility;
import au.gov.asd.tac.constellation.utilities.gui.field.TextInputField;
import au.gov.asd.tac.constellation.plugins.parameters.RecentParameterValues;
import au.gov.asd.tac.constellation.plugins.parameters.RecentValuesChangeEvent;
import au.gov.asd.tac.constellation.plugins.parameters.RecentValuesListener;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.textfield.TextFields;

/**
 * A text box allowing entry of single line text, multiple line text
 * corresponding to a {@link PluginParameter} of
 * {@link au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType}.
 * <p>
 * Editing the value in the text box will set the string value for the
 * underlying {@link PluginParameter}.
 *
 * @see
 * au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType
 *
 * check the is label, i think this feature is just used when a parameter value is uneditable and shown as plan text label, why does this need to be a label....
 * @author ruby_crucis
 */
public class ValueInputPane extends ParameterInputPane<StringParameterValue, String> {

    private static final Logger LOGGER = Logger.getLogger(ValueInputPane.class.getName());

    public ValueInputPane(final PluginParameter<StringParameterValue> parameter) {
        this(parameter, 1);
    }

    /**
     * Primary constructor
     *
     * @param parameter parameter to link to value
     * @param defaultWidth default width (in pixels)
     * @param suggestedHeight suggested hight (in lines)
     */
    public ValueInputPane(final PluginParameter<StringParameterValue> parameter, Integer suggestedHeight) {
        super(suggestedHeight == null || suggestedHeight <= 1 ? new TextInputField(TextType.SINGLELINE, parameter.getId()) : new TextInputField(TextType.MULTILINE, parameter.getId()), parameter);
        
//        final boolean isLabel = StringParameterType.isLabel(parameter);
//        if (isLabel) {
//            field = null;
//            recentValuesCombo = null;
//            recentValueSelectionListener = null;
//            final Label l = new Label(parameter.getStringValue().replace(SeparatorConstants.NEWLINE, " "));
//            l.setWrapText(true);
//            l.setPrefWidth(defaultWidth);
//            getChildren().add(l);
//            parameter.addListener((pluginParameter, change) -> Platform.runLater(() -> {
//                    switch (change) {
//                        case VALUE -> {
//                            // Don't change the value if it isn't necessary.
//                            // Setting the text changes the cursor position, which makes it look like text is
//                            // being entered right-to-left.
//                            final String param = parameter.getStringValue();
//                            if (!l.getText().equals(param)) {
//                                l.setText(param);
//                            }
//                        }
//                        case VISIBLE -> {
//                            l.setManaged(parameter.isVisible());
//                            l.setVisible(parameter.isVisible());
//                            this.setVisible(parameter.isVisible());
//                            this.setManaged(parameter.isVisible());
//                        }
//                        default -> {
//                            // do nothing
//                        }
//                    }
//                }));
//        } else {

            
//            if (parameter.getParameterValue().getGuiInit() != null) {
//                parameter.getParameterValue().getGuiInit().init(field);
//            }

            StringParameterValue pv = (StringParameterValue) parameter.getParameterValue();
            if (parameter.getObjectValue() != null) {
                this.setFieldValue(pv.get());
            }
            
            if (suggestedHeight == null){
                this.setFieldLines(1);
            } else {
                this.setFieldLines(suggestedHeight);
            }
        }

    @Override
    public ChangeListener getFieldChangeListener(PluginParameter<StringParameterValue> parameter) {
        return (ChangeListener<String>) (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue != null) {
                parameter.setStringValue(getFieldValue());
            }
        };
    }
    
    @Override
    public PluginParameterListener getPluginParameterListener() {
       return (PluginParameter<?> parameter, ParameterChange change) -> Platform.runLater(() -> {
            if (parameter.getParameterValue() instanceof StringParameterValue pv){
                switch (change) {
                    case VALUE -> {
                        // Don't change the value if it isn't necessary.
                        if (pv.get().equals(getFieldValue())){
                            setFieldValue(pv.get());
                        }
                    }
                    case ENABLED -> updateFieldEnablement();
                    case VISIBLE -> updateFieldVisability();
                    default -> LOGGER.log(Level.FINE, "ignoring parameter change type {0}.", change);
                }
            }
        });
    }
}