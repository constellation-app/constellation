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
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType.FileParameterValue;
import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputField;
import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputField.TextType;
import au.gov.asd.tac.constellation.utilities.gui.field.FileInputField;
import java.io.File;
import javafx.application.Platform;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import org.apache.commons.lang3.StringUtils;


/**
 * A text-box and file chooser that together allows the selection or manual
 * entry of a number files, which is the GUI element corresponding to a
 * {@link PluginParameter} of
 * {@link au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType}.
 * <p>
 * Entering file names manually or making a selection with the file chooser will
 * update the object value of the underlying {@link PluginParameter}.
 *
 * @see au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType
 *
 * @author ruby_crucis
 */
public class FileInputPane extends HBox {

    public static final int DEFAULT_WIDTH = 300;
    public static final File DEFAULT_DIRECTORY = new File(System.getProperty("user.home"));
    private final ConstellationInputField field;
    private final boolean required;

    public FileInputPane(final PluginParameter<FileParameterValue> parameter) {
        this(parameter, DEFAULT_WIDTH, null);
    }

    public FileInputPane(final PluginParameter<FileParameterValue> parameter, final int defaultWidth) {
        this(parameter, defaultWidth, null);
    }

    /**
     * Primary constructor
     *
     * @param parameter parameter to link to value
     * @param defaultWidth default width (in pixels)
     * @param suggestedHeight suggested hight (in lines)
     */
    public FileInputPane(final PluginParameter<FileParameterValue> parameter, final int defaultWidth, Integer suggestedHeight) {
        if (suggestedHeight == null) {
            suggestedHeight = 1;
        }
        
        required = parameter.isRequired();

        final FileParameterValue paramaterValue = parameter.getParameterValue();

        if (suggestedHeight > 1) {
            field = new FileInputField(paramaterValue.getKind(), TextType.MULTILINE);//TextArea();
            field.setWrapText(true);
            field.setPrefRowCount(suggestedHeight);
        } else {
            field = new FileInputField(paramaterValue.getKind());
        }

        ((FileInputField) field).setFileFilter(FileParameterType.getFileFilters(parameter));
        ((FileInputField) field).setAcceptAll(FileParameterType.isAcceptAllFileFilterUsed(parameter));
        
        if (parameter.getParameterValue().getGuiInit() != null) {
            parameter.getParameterValue().getGuiInit().init(field);
        }

        field.setDisable(!parameter.isEnabled());
        field.setVisible(parameter.isVisible());
        field.setManaged(parameter.isVisible());
        this.setManaged(parameter.isVisible());
        this.setVisible(parameter.isVisible());
        
        field.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.DELETE) {
                final IndexRange selection = field.getSelection();
                if (selection.getLength() == 0) {
                    field.deleteNextChar();
                } else {
                    field.deleteText(selection);
                }
                event.consume();
            } else if (event.isShortcutDown() && event.isShiftDown() && (event.getCode() == KeyCode.RIGHT)) {
                field.selectNextWord();
                event.consume();
            } else if (event.isShortcutDown() && event.isShiftDown() && (event.getCode() == KeyCode.LEFT)) {
                field.selectPreviousWord();
                event.consume();
            } else if (event.isShortcutDown() && (event.getCode() == KeyCode.RIGHT)) {
                field.nextWord();
                event.consume();
            } else if (event.isShortcutDown() && (event.getCode() == KeyCode.LEFT)) {
                field.previousWord();
                event.consume();
            } else if (event.isShiftDown() && (event.getCode() == KeyCode.RIGHT)) {
                field.selectForward();
                event.consume();
            } else if (event.isShiftDown() && (event.getCode() == KeyCode.LEFT)) {
                field.selectBackward();
                event.consume();
            } else if (event.isShortcutDown() && (event.getCode() == KeyCode.A)) {
                field.selectAll();
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                event.consume();
            } else {
                // Do nothing
            }
        });

        field.setPromptText(parameter.getDescription());
        if (parameter.getObjectValue() != null) {
            field.setText(parameter.getStringValue());
        }

        field.setEditable(true);
        field.setPrefWidth(defaultWidth);

        final Tooltip tooltip = new Tooltip("");
        
        // Looks for changes to the input field
        // Triggers a change to the parameter
        field.textProperty().addListener((observableValue, oldValue, newValue) -> {            
            
            // As the change is happening in the field, the parameter object will not have updated its error value yet
            final String error = parameter.validateString(newValue);
            if ((required && StringUtils.isBlank(newValue)) || error != null) {
                tooltip.setText(StringUtils.isNotBlank(error) ? error : "File is required!");
                field.setTooltip(tooltip);
                field.setId("invalid");
            } else {
                tooltip.setText("");
                field.setTooltip(null);
                field.setId("");
            }
            parameter.setStringValue(field.getText());
        });

        // Looks for changes to the plugin parameter
        // Can be triggered by a change from the application or a change from the respective input field
        // Can trigger a change to the input field which will cause this listner to be triggered a second time.
        parameter.addListener((pluginParameter, change) -> {
            Platform.runLater(() -> {
                switch (change) {
                    case VALUE -> {
                        // Do not retrigger the fieled listner if this event was triggered by the field listner.
                        final String param = parameter.getStringValue();
                        if (!field.getText().equals(param)) {
                            field.setText(param);
                        }
                    }
                    case ENABLED -> field.setDisable(!pluginParameter.isEnabled());
                    case VISIBLE -> {
                        field.setManaged(parameter.isVisible());
                        field.setVisible(parameter.isVisible());
                        this.setVisible(parameter.isVisible());
                        this.setManaged(parameter.isVisible());
                    }
                    default -> {
                    }
                }
            });
        });

        final HBox fieldAndAddButton = new HBox();
        fieldAndAddButton.setSpacing(2);
        fieldAndAddButton.getChildren().addAll(field);
        getChildren().add(fieldAndAddButton);
    }
}
