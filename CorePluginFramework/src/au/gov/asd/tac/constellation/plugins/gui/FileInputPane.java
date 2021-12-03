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
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType.FileParameterKind;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType.FileParameterValue;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

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
    private final Button fileAddButton;
    private final TextInputControl field;
    private static final Logger LOGGER = Logger.getLogger(FileInputPane.class.getName());

    public FileInputPane(final PluginParameter<FileParameterValue> parameter) {
        this(parameter, DEFAULT_WIDTH, null);
    }

    public FileInputPane(final PluginParameter<FileParameterValue> parameter, int defaultWidth) {
        this(parameter, defaultWidth, null);
    }

    /**
     * Primary constructor
     *
     * @param parameter parameter to link to value
     * @param defaultWidth default width (in pixels)
     * @param suggestedHeight suggested hight (in lines)
     */
    public FileInputPane(final PluginParameter<FileParameterValue> parameter, int defaultWidth, Integer suggestedHeight) {
        if (suggestedHeight == null) {
            suggestedHeight = 1;
        }

        final FileParameterValue paramaterValue = parameter.getParameterValue();
        fileAddButton = new Button(paramaterValue.getKind() == FileParameterKind.SAVE ? "Save" : "Open");
        fileAddButton.setOnAction((ActionEvent event) -> {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(DEFAULT_DIRECTORY);
            final ExtensionFilter filter = FileParameterType.getFileFilters(parameter);
            if (filter != null) {
                fileChooser.getExtensionFilters().add(filter);
            }

            final List<File> files = new ArrayList<>();
            switch (paramaterValue.getKind()) {
                case OPEN:
                    final File openFile = fileChooser.showOpenDialog(getScene().getWindow());
                    if (openFile != null) {
                        files.add(openFile);
                    }
                    break;
                case OPEN_MULTIPLE:
                    final List<File> multipleOpenFiles = fileChooser.showOpenMultipleDialog(getScene().getWindow());
                    if (multipleOpenFiles != null) {
                        files.addAll(multipleOpenFiles);
                    }
                    break;
                case SAVE:
                    final File saveFile = fileChooser.showSaveDialog(getScene().getWindow());
                    if (saveFile != null) {
                        files.add(saveFile);
                    }
                    break;
                default:
                    LOGGER.log(Level.FINE, "ignoring file selection type {0}.", paramaterValue.getKind());
                    break;
            }

            if (!files.isEmpty()) {
                parameter.setObjectValue(files);
            }

        });

        if (suggestedHeight > 1) {
            field = new TextArea();
            ((TextArea) field).setWrapText(true);
            ((TextArea) field).setPrefRowCount(suggestedHeight);
        } else {
            field = new TextField();
        }

        if (parameter.getParameterValue().getGuiInit() != null) {
            parameter.getParameterValue().getGuiInit().init(field);
        }

        field.setDisable(!parameter.isEnabled());
        field.setVisible(parameter.isVisible());
        field.setManaged(parameter.isVisible());
        this.setManaged(parameter.isVisible());
        this.setVisible(parameter.isVisible());

        field.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            if (event.getCode() == KeyCode.DELETE) {
                IndexRange selection = field.getSelection();
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
        tooltip.setStyle("-fx-text-fill: black;");
        field.textProperty().addListener((observableValue, oldValue, newValue) -> {
            // Validation
            String error = parameter.validateString(field.getText());
            if (error != null) {
                tooltip.setText(error);
                field.setTooltip(tooltip);
                field.setId("invalid");
            } else {
                tooltip.setText("");
                field.setTooltip(null);
                field.setId("");
            }
            parameter.setStringValue(field.getText());
        });

        parameter.addListener((pluginParameter, change) -> {
            Platform.runLater(() -> {
                switch (change) {
                    case VALUE:
                        // Don't change the value if it isn't necessary.
                        final String param = parameter.getStringValue();
                        if (!field.getText().equals(param)) {
                            field.setText(param);
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
                        break;
                }
            });
        });

        HBox fieldAndAddButton = new HBox();
        fieldAndAddButton.setSpacing(2);
        fieldAndAddButton.getChildren().addAll(field, fileAddButton);
        getChildren().add(fieldAndAddButton);
    }
}
