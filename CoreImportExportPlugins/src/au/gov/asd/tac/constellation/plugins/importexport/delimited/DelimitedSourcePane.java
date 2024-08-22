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
package au.gov.asd.tac.constellation.plugins.importexport.delimited;

import au.gov.asd.tac.constellation.plugins.importexport.ImportController;
import au.gov.asd.tac.constellation.plugins.importexport.SourcePane;
import au.gov.asd.tac.constellation.plugins.importexport.delimited.parser.ImportFileParser;
import au.gov.asd.tac.constellation.plugins.importexport.delimited.parser.InputSource;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.utilities.gui.ScreenWindowsHelper;
import au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javax.swing.filechooser.FileFilter;
import org.openide.filesystems.FileChooserBuilder;

/**
 * The SourcePane provides the UI necessary to allow the user to specify where
 * the imported data should come from. This is typically done by selecting a
 * file.
 *
 * @author sirius
 */
public class DelimitedSourcePane extends SourcePane {

    private static final Logger LOGGER = Logger.getLogger(DelimitedSourcePane.class.getName());
    private static final String TITLE = "Import";
    private static final int FILESCROLLPANE_MAX_HEIGHT = 100;
    private static final int FILESCROLLPANE_PREF_HEIGHT = 100;
    private static final int FILEVBOX_SPACING = 10;
    private static final int PREVIEW_LIMIT = 100;

    private final ComboBox<ImportFileParser> importFileParserComboBox;
    private final CheckBox schemaCheckBox;
    private final CheckBox filesIncludeHeadersCheckBox;
    private final ListView<File> fileListView = new ListView<>();
    protected File defaultDirectory = new File(System.getProperty("user.home"));

    public DelimitedSourcePane(final DelimitedImportController importController) {
        super(importController);

        final Label fileLabel = new Label("Files:");
        GridPane.setConstraints(fileLabel, 0, 1, 1, 1, HPos.LEFT, VPos.TOP);

        fileListView.setMinHeight(0);
        fileListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        fileListView.setMaxWidth(Double.MAX_VALUE);
        fileListView.setMinWidth(0);

        // listener calls the controller to trigger the open config page and the
        // enable import button. this will be triggered when a file is in
        // the imported files list
        fileListView.itemsProperty().addListener((observable, oldValue, newValue) -> {
            importController.openConfigPane(!newValue.isEmpty());
            importController.disableButton(newValue.isEmpty());
        });

        final ScrollPane fileScrollPane = new ScrollPane();
        fileScrollPane.setMaxHeight(FILESCROLLPANE_MAX_HEIGHT);
        fileScrollPane.setMaxWidth(Double.MAX_VALUE);
        fileScrollPane.setPrefHeight(FILESCROLLPANE_PREF_HEIGHT);
        fileScrollPane.setFitToWidth(true);
        fileScrollPane.setFitToHeight(true);
        fileScrollPane.setMinWidth(0);
        fileScrollPane.setContent(fileListView);
        fileScrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        fileScrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        GridPane.setConstraints(fileScrollPane, 1, 1);

        final VBox fileButtonBox = new VBox();
        fileButtonBox.setFillWidth(true);
        fileButtonBox.setSpacing(FILEVBOX_SPACING);
        GridPane.setConstraints(fileButtonBox, 2, 1);

        final Button fileAddBtn = new Button("", new ImageView(UserInterfaceIconProvider.ADD_ALTERNATE.buildImage(40,
                ConstellationColor.EMERALD.getJavaColor())));
        final Button fileRemBtn = new Button("", new ImageView(UserInterfaceIconProvider.REMOVE_ALTERNATE.buildImage(40,
                ConstellationColor.CHERRY.getJavaColor())));
        fileRemBtn.setDisable(true);

        fileButtonBox.getChildren().addAll(fileAddBtn, fileRemBtn);

        getChildren().addAll(fileLabel, fileScrollPane, fileButtonBox);

        fileListView.setOnMouseClicked((MouseEvent t) -> fileRemBtn.setDisable(fileListView.getSelectionModel().getSelectedItems().isEmpty()));

        fileListView.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends File> c) -> {
            final ObservableList<File> allFiles = fileListView.getItems();
            final ObservableList<File> selectedFiles = fileListView.getSelectionModel().getSelectedItems();
            importController.setFiles(allFiles, selectedFiles.isEmpty() ? null : selectedFiles.get(0));
        });

        fileAddBtn.setOnAction((ActionEvent t) -> addFile(importController));

        fileRemBtn.setOnAction((ActionEvent t) -> {
            final ObservableList<File> selectedFiles = fileListView.getSelectionModel().getSelectedItems();
            final ObservableList<File> allFiles = fileListView.getItems();
            final ObservableList<File> files = FXCollections.observableArrayList(allFiles);
            files.removeAll(selectedFiles);
            fileListView.setItems(files);
            importController.setFiles(files, null);
            DelimitedSourcePane.this.importFileParserComboBox.setDisable(!files.isEmpty());
        });

        final Label destinationLabel = new Label("Destination:");
        updateDestinationGraphCombo();

        // IMPORT FILE PARSER
        final Label importFileParserLabel = new Label("File Parser:");

        final ObservableList<ImportFileParser> parsers = FXCollections.observableArrayList();
        parsers.addAll(ImportFileParser.getParsers().values());
        importFileParserComboBox = new ComboBox<>();
        importFileParserComboBox.setItems(parsers);
        importFileParserComboBox.getSelectionModel().selectFirst();
        importFileParserComboBox.setOnAction((final ActionEvent t)
                -> importController.setImportFileParser(importFileParserComboBox.getSelectionModel().getSelectedItem()));

        //SCHEMA
        final Label schemaLabel = new Label("Initialise With Schema:");
        schemaCheckBox = new CheckBox();
        schemaCheckBox.setSelected(importController.isSchemaInitialised());
        schemaCheckBox.setOnAction((final ActionEvent event) -> importController.setSchemaInitialised(schemaCheckBox.isSelected()));

        //INSERT COLUMN HEADERS FOR FILES WITH MISSING HEADERS
        final Label insertHeadersLabel = new Label("Files Include Headers:");
        filesIncludeHeadersCheckBox = new CheckBox();
        filesIncludeHeadersCheckBox.setSelected(importController.isFilesIncludeHeadersEnabled());
        filesIncludeHeadersCheckBox.setOnAction(t -> importController.setfilesIncludeHeaders(filesIncludeHeadersCheckBox.isSelected()));

        final ToolBar optionsBox = new ToolBar();
        optionsBox.setMinWidth(0);
        GridPane.setConstraints(optionsBox, 0, 0, 3, 1);
        optionsBox.getItems().addAll(destinationLabel, graphComboBox, importFileParserLabel, importFileParserComboBox,
                schemaLabel, schemaCheckBox, insertHeadersLabel, filesIncludeHeadersCheckBox);
        getChildren().add(optionsBox);
    }

    private void addFile(final DelimitedImportController importController) {
        final ImportFileParser parser = DelimitedSourcePane.this.importFileParserComboBox.getSelectionModel().getSelectedItem();

        if (parser != null) {
            final FileFilter fileFilter = parser.getFileFilter();

            if (fileFilter != null) {
                FileChooser.openMultiDialog(getDelimitedImportFileChooser().setFileFilter(fileFilter)).thenAccept(optionalFiles -> optionalFiles.ifPresent(newFiles -> Platform.runLater(() -> {
                    DelimitedSourcePane.this.importFileParserComboBox.setDisable(true);

                    final ObservableList<File> files = FXCollections.observableArrayList(fileListView.getItems());
                    final StringBuilder sb = new StringBuilder();
                    final String alertText = "The following files could not be parsed and have been excluded from the import set:\n";
                    sb.append(alertText);

                    for (final File file : newFiles) {
                        // prevent adding the same file again
                        if (fileAlreadyAdded(file)) {
                            continue;
                        }

                        // Attempt to parse/preview, if a failure is detected don't add the file to the set of files to import.
                        try {
                            parser.preview(new InputSource(file), null, PREVIEW_LIMIT);
                            files.add(file);
                        } catch (final IOException ex) {
                            // Append the name of each file that could not be imported.
                            sb.append("\n");
                            sb.append(file.getName());
                            LOGGER.log(Level.WARNING, "Unable to parse the file {0}, excluding from import set.", new Object[]{file.toString()});
                            LOGGER.log(Level.WARNING, ex.toString());
                        }
                    }

                    // If file names have been appended to sb, then some files could not be imported, so notify user.
                    if (!sb.toString().equals(alertText)) {
                        NotifyDisplayer.displayAlert("Import from File", "Invalid file(s) found", sb.toString(), Alert.AlertType.WARNING, ScreenWindowsHelper.getMainWindowCentrePoint());
                    }

                    fileListView.setItems(files);

                    if (!fileListView.getSelectionModel().isEmpty() && !files.isEmpty()) {
                        fileListView.getSelectionModel().select(files.get(0));
                        fileListView.requestFocus();
                    }
                    if (files.isEmpty()){
                        DelimitedSourcePane.this.importFileParserComboBox.setDisable(false);
                    }

                    final ObservableList<File> selectedFiles = fileListView.getSelectionModel().getSelectedItems();

                    importController.setFiles(files, selectedFiles.isEmpty() ? null : selectedFiles.get(0));
                    importController.validateFileStructure(newFiles);
                })));
            }
        }
    }

    private boolean fileAlreadyAdded(final File newFile) {
        final ObservableList<File> files = fileListView.getItems();

        for (final File file : files) {
            try {
                if (file.getCanonicalPath().equals(newFile.getCanonicalPath())) {
                    return true;
                }
            } catch (final IOException ex) {
                LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        return false;
    }

    /**
     * Allow a file to be removed from fileListView. This would be triggered by
     * code in InputController if the file was found to be missing or invalid -
     * these checks are triggered when a new file is selected in the
     * fileListView.
     *
     * @param file The file to remove.
     */
    public void removeFile(final File file) {
        final ObservableList<File> files = fileListView.getItems();
        files.remove(file);
        fileListView.setItems(files);

        if (files.isEmpty()) {
            DelimitedImportController delimitedImportController = (DelimitedImportController) importController;
            delimitedImportController.clearFilters();
        }
    }

    /**
     * Creates a new file chooser.
     *
     * @return the created file chooser.
     */
    public FileChooserBuilder getDelimitedImportFileChooser() {
        return new FileChooserBuilder(TITLE)
                .setTitle(TITLE)
                .setAcceptAllFileFilterUsed(false)
                .setFilesOnly(true);
    }

    /**
     * The import controller has been modified: update the GUI to match.
     *
     * @param importController The ImportController.
     */
    @Override
    public void update(final ImportController importController) {
        graphComboBox.getItems().stream()
                .filter(importDestination -> importController.getDestination().toString().equals(importDestination.toString()))
                .findAny()
                .ifPresent(graphComboBox.getSelectionModel()::select);

        importFileParserComboBox.getSelectionModel().select(((DelimitedImportController) importController).getImportFileParser());
        schemaCheckBox.setSelected(importController.isSchemaInitialised());
        filesIncludeHeadersCheckBox.setSelected(((DelimitedImportController) importController).isFilesIncludeHeadersEnabled());

        importController.updateDisplayedAttributes();
    }
}
