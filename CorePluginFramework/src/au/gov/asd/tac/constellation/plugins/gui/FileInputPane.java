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

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType;
import static au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType.FileParameterKind.OPEN;
import static au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType.FileParameterKind.OPEN_MULTIPLE;
import static au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType.FileParameterKind.OPEN_MULTIPLE_OBSCURED;
import static au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType.FileParameterKind.OPEN_OBSCURED;
import static au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType.FileParameterKind.SAVE;
import static au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType.FileParameterKind.SAVE_OBSCURED;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType.FileParameterValue;
import au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser.ExtensionFilter;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.lang3.StringUtils;
import org.openide.filesystems.FileChooserBuilder;

/**
 * A text-box and file chooser that together allows the selection or manual entry of a number files, which is the GUI
 * element corresponding to a {@link PluginParameter} of
 * {@link au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType}.
 * <p>
 * Entering file names manually or making a selection with the file chooser will update the object value of the
 * underlying {@link PluginParameter}.
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
    private final boolean required;
    private static final Logger LOGGER = Logger.getLogger(FileInputPane.class.getName());

    public FileInputPane(final PluginParameter<FileParameterValue> parameter) {
        this(parameter, DEFAULT_WIDTH, null, null);
    }

    public FileInputPane(final PluginParameter<FileParameterValue> parameter, final int defaultWidth) {
        this(parameter, defaultWidth, null, null);
    }

    /**
     * Primary constructor
     *
     * @param parameter parameter to link to value
     * @param defaultWidth default width (in pixels)
     * @param suggestedHeight suggested hight (in lines)
     */
    public FileInputPane(final PluginParameter<FileParameterValue> parameter, final int defaultWidth, Integer suggestedHeight) {
        this(parameter, defaultWidth, suggestedHeight, null);
    }

    /**
     * Primary constructor
     *
     * @param parameter parameter to link to value
     * @param defaultWidth default width (in pixels)
     * @param suggestedHeight suggested hight (in lines)
     * @param fileExtension the file extension to filter file dialog
     */
    public FileInputPane(final PluginParameter<FileParameterValue> parameter, final int defaultWidth, Integer suggestedHeight, final String fileExtension) {
        if (suggestedHeight == null) {
            suggestedHeight = 1;
        }

        required = parameter.isRequired();

        final FileParameterValue paramaterValue = parameter.getParameterValue();
        fileAddButton = new Button(paramaterValue.getKind().toString());
        fileAddButton.setOnAction(event -> handleButtonOnAction(paramaterValue, parameter, fileExtension));

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

        field.addEventFilter(KeyEvent.KEY_PRESSED, event -> handleEventFilter(event, field));

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
        parameter.addListener((pluginParameter, change)
                -> Platform.runLater(() -> {
                    switch (change) {
                        case VALUE -> {
                            // Do not retrigger the fieled listner if this event was triggered by the field listner.
                            final String param = parameter.getStringValue();
                            if (!field.getText().equals(param)) {
                                field.setText(param);
                            }
                        }
                        case ENABLED ->
                            field.setDisable(!pluginParameter.isEnabled());
                        case VISIBLE -> {
                            field.setManaged(parameter.isVisible());
                            field.setVisible(parameter.isVisible());
                            this.setVisible(parameter.isVisible());
                            this.setManaged(parameter.isVisible());
                        }
                        default -> {
                            // do nothing
                        }
                    }
                }));

        final HBox fieldAndAddButton = new HBox();
        fieldAndAddButton.setSpacing(2);
        fieldAndAddButton.getChildren().addAll(field, fileAddButton);
        getChildren().add(fieldAndAddButton);
    }

    // Public for testing
    public void handleButtonOnAction(final FileParameterValue parameterValue, final PluginParameter<FileParameterValue> parameter, final String fileExtension) {
        final List<File> files = new ArrayList<>();
        final CompletableFuture<Void> dialogFuture;
        switch (parameterValue.getKind()) {
            case OPEN, OPEN_OBSCURED ->
                dialogFuture = FileChooser.openOpenDialog(getFileChooser(parameter, "Open", fileExtension)).thenAccept(optionalFile -> optionalFile.ifPresent(openFile -> {
                    if (openFile != null) {
                        files.add(openFile);
                    }
                }));
            case OPEN_MULTIPLE, OPEN_MULTIPLE_OBSCURED ->
                dialogFuture = FileChooser.openMultiDialog(getFileChooser(parameter, "Open File(s)", fileExtension)).thenAccept(optionalFile -> optionalFile.ifPresent(openFiles -> {
                    if (openFiles != null) {
                        files.addAll(openFiles);
                    }
                }));
            case SAVE, SAVE_OBSCURED ->
                dialogFuture = FileChooser.openSaveDialog(getFileChooser(parameter, "Save", fileExtension)).thenAccept(optionalFile -> optionalFile.ifPresent(saveFile -> {
                    if (saveFile != null) {
                        //Save files may have been typed by the user and an extension may not have been specified.
                        final String fnam = saveFile.getAbsolutePath();
                        final String expectedExtension = FileParameterType.getFileFilters(parameter).getExtensions().get(0);
                        if (!fnam.toLowerCase().endsWith(expectedExtension)) {
                            saveFile = new File(fnam + expectedExtension);
                        }
                        files.add(saveFile);
                    }
                }));
            default -> {
                dialogFuture = null;
                LOGGER.log(Level.FINE, "ignoring file selection type {0}.", parameterValue.getKind());
            }
        }

        // As the dialog windows are completed on another thread 
        // the execution of this method must wait until the thread has finnished executing.
        if (dialogFuture != null) {
            try {
                dialogFuture.get();
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
            } catch (final ExecutionException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
            }
        }

        if (!files.isEmpty()) {
            parameter.setObjectValue(files);
        }
    }

    public static void handleEventFilter(final KeyEvent event, final TextInputControl field) {
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
    }

    /**
     * Creates a FileChooser for the Parameter If an extension filter has not been specified, all file types will be
     * accepted by default.
     *
     * @param parameter
     * @param title
     * @return
     */
    private FileChooserBuilder getFileChooser(final PluginParameter<FileParameterValue> parameter, final String title) {
        final ExtensionFilter extensionFilter = FileParameterType.getFileFilters(parameter);

        FileChooserBuilder fileChooserBuilder = FileChooser.createFileChooserBuilder(title)
                .setAcceptAllFileFilterUsed(extensionFilter == null || FileParameterType.isAcceptAllFileFilterUsed(parameter));

        final boolean warnOverwrite = FileParameterType.isWarnOverwriteUsed(parameter);
        if (extensionFilter != null) {
            for (final String extension : extensionFilter.getExtensions()) {
                if (warnOverwrite) {
                    FileChooser.setWarnOverwrite(fileChooserBuilder, extension);
                }

                // Add a file filter for all registered exportable file types.
                fileChooserBuilder = fileChooserBuilder.addFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(final File file) {
                        final String name = file.getName();
                        return (file.isFile() && StringUtils.endsWithIgnoreCase(name, extension)) || file.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return extensionFilter.getDescription();
                    }
                });
            }
        }
        return fileChooserBuilder;
    }

    /**
     * Creates a FileChooser for the Parameter If an extension filter has not been specified, all file types will be
     * accepted by default.
     *
     * @param parameter
     * @param title
     * @return
     */
    private FileChooserBuilder getFileChooser(final PluginParameter<FileParameterValue> parameter, final String title, final String fileExtension) {
        final FileChooserBuilder fcb;

        if (fileExtension != null) {
            fcb = FileChooser.createFileChooserBuilder(title, fileExtension)
                    .setAcceptAllFileFilterUsed(FileParameterType.isAcceptAllFileFilterUsed(parameter));
        } else {
            fcb = getFileChooser(parameter, title);
        }

        if (FileParameterType.isWarnOverwriteUsed(parameter) && FileParameterType.getFileFilters(parameter) != null) {
            for (final String extension : FileParameterType.getFileFilters(parameter).getExtensions()) {
                FileChooser.setWarnOverwrite(fcb, extension);
            }
        }

        return fcb;
    }

    /**
     * Used only in testing
     *
     * @return fileAddButton
     */
    public Button getFileAddButton() {
        return fileAddButton;
    }
}
