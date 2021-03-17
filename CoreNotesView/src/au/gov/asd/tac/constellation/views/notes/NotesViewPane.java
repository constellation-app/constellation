/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.notes;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.reporting.GraphReport;
import au.gov.asd.tac.constellation.plugins.reporting.GraphReportManager;
import au.gov.asd.tac.constellation.plugins.reporting.PluginReport;
import au.gov.asd.tac.constellation.views.notes.state.NotesViewEntry;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.swing.JOptionPane;
import org.apache.commons.collections4.CollectionUtils;
import org.controlsfx.control.CheckComboBox;

/**
 * Handles generating UI elements for the Notes View pane and its notes.
 *
 * @author sol695510
 */
public class NotesViewPane extends BorderPane {

    private final NotesViewController notesViewController;
    private final List<NotesViewEntry> notesViewEntries;

    /**
     * A cache of NotesViewEntry datetimes cache to quickly check if a note
     * exists in notesViewEntries. This is a necessary optimisation.
     */
    private final Set<String> notesDateTimeCache;

    private final ObservableList<String> availableFilters;
    private final List<String> selectedFilters;
    private final CheckComboBox filterCheckComboBox;
    private Boolean isSelectedFiltersUpdating = false;

    private final HBox filterNotesHBox;
    private final VBox notesViewPaneVBox;
    private final VBox addNoteVBox;
    private final VBox notesListVBox;
    private final ScrollPane notesListScrollPane;
    private Stage editStage;

    private final int DEFAULT_SPACING = 5;
    private final String PROMPT_COLOUR = "#909090";
    private final String USER_COLOUR = "#C15A58";
    private final String AUTO_COLOUR = "#588BC1";
    private final String DATETIME_PATTERN = "hh:mm:ss a 'on' dd/MM/yyyy"; // TODO: make this a preference so that we can support their local timestamp format instead

    private static final String AUTO_NOTES_FILTER = "Auto Notes";
    private static final String USER_NOTES_FILTER = "User Notes";

    private static final String NOTES_VIEW_ICON = "resources/notes-view.png";

    private final Object LOCK = new Object();

    /**
     * NotesViewPane constructor.
     *
     * @param controller
     */
    public NotesViewPane(final NotesViewController controller) {

        notesViewController = controller;
        notesViewEntries = new ArrayList<>();
        notesDateTimeCache = new HashSet<>();

        availableFilters = FXCollections.observableArrayList(USER_NOTES_FILTER, AUTO_NOTES_FILTER);
        selectedFilters = new ArrayList<>(availableFilters); // By default all filters are selected.

        // CheckComboBox to select and deselect various filters for note rendering.
        filterCheckComboBox = new CheckComboBox(availableFilters);
        filterCheckComboBox.setTitle("Select a filter...");
        filterCheckComboBox.getCheckModel().getCheckedItems().addListener(new ListChangeListener() {
            @Override
            public void onChanged(final ListChangeListener.Change event) {
                if (!isSelectedFiltersUpdating) {

                    setFilters(filterCheckComboBox.getCheckModel().getCheckedItems());

                    final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
                    if (activeGraph != null) {
                        updateNotesUI();
                        controller.writeState(activeGraph);
                    }
                }
            }
        });

        // VBox to store control items used to filter notes.
        filterNotesHBox = new HBox(DEFAULT_SPACING, filterCheckComboBox);
        filterNotesHBox.setAlignment(Pos.CENTER_LEFT);
        filterNotesHBox.setStyle("-fx-padding: 5px;");

        // TextField to enter new note title.
        final TextField titleField = new TextField();
        titleField.setPromptText("Type a title...");
        titleField.setStyle("-fx-prompt-text-fill: " + PROMPT_COLOUR + ";");

        // TextArea to enter new note content.
        final TextArea contentField = new TextArea();
        contentField.setPromptText("Type a note...");
        contentField.setStyle("-fx-prompt-text-fill: " + PROMPT_COLOUR + ";");
        contentField.setWrapText(true);
        contentField.setOnKeyPressed(key -> {
            // If tab is typed and shift isn't being held dowm.
            if (key.getCode() == KeyCode.TAB && !key.isShiftDown()) {
                // Backspace any tabs typed.
                contentField.fireEvent(new KeyEvent(null, null, KeyEvent.KEY_PRESSED, "", "", KeyCode.BACK_SPACE, false, false, false, false));
                // Move focus to the next UI element.
                contentField.getParent().getChildrenUnmodifiable().get(contentField.getParent().getChildrenUnmodifiable().indexOf(contentField) + 1).requestFocus();
            }
        });

        // Button to add new note.
        final Button addNoteButton = new Button("Add Note");
        addNoteButton.setOnAction(event -> {
            final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
            if (activeGraph != null) {
                if ((titleField.getText().isBlank() && titleField.getText().isEmpty())
                        || (contentField.getText().isBlank() && contentField.getText().isEmpty())) {
                    JOptionPane.showMessageDialog(null, "Type in missing fields.", "Invalid Text", JOptionPane.WARNING_MESSAGE);
                } else {
                    synchronized (LOCK) {
                        notesViewEntries.add(new NotesViewEntry(
                                Long.toString(ZonedDateTime.now().toInstant().toEpochMilli()),
                                titleField.getText(),
                                contentField.getText(),
                                true
                        ));
                    }

                    titleField.clear();
                    contentField.clear();
                    updateNotesUI();
                    controller.writeState(activeGraph);
                    event.consume();
                }
            }
        });

        // VBox to store control items used to add new note.
        addNoteVBox = new VBox(DEFAULT_SPACING, titleField, contentField, addNoteButton);
        addNoteVBox.setAlignment(Pos.CENTER_RIGHT);
        addNoteVBox.setStyle("-fx-padding: 5px;");
        addNoteVBox.setMinHeight(200);

        // VBox in a ScrollPane for holding expanding list of user and plugin generated notes.
        notesListVBox = new VBox(DEFAULT_SPACING);
        notesListVBox.setAlignment(Pos.BOTTOM_CENTER);
        notesListScrollPane = new ScrollPane();
        notesListScrollPane.setContent(notesListVBox);
        notesListScrollPane.setStyle("-fx-padding: 5px; -fx-background-color: transparent;");
        notesListScrollPane.setFitToWidth(true);
        VBox.setVgrow(notesListScrollPane, Priority.ALWAYS);

        // Main Notes View Pane VBox.
        notesViewPaneVBox = new VBox(DEFAULT_SPACING, filterNotesHBox, notesListScrollPane, addNoteVBox);
        notesViewPaneVBox.setAlignment(Pos.BOTTOM_CENTER);
        setCenter(notesViewPaneVBox);
    }

    /**
     * Set the plugin reports that have executed on the current graph report.
     */
    protected void setGraphReport(final NotesViewController controller) {
        final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
        final GraphReport currentGraphReport = GraphReportManager.getGraphReport(activeGraph.getId());

        if (currentGraphReport != null) {
            // Iterates the list of currently executed plugins.
            currentGraphReport.getPluginReports().forEach(pluginReport -> {
                // omit low level plugins which are not useful as notes
                if (!pluginReport.hasLowLevelTag()) {
                    addPluginReport(pluginReport);
                }
            });

            // Update the Notes View UI.
            if (activeGraph != null) {
                updateNotesUI();
                updateFilters();
                controller.writeState(activeGraph);
            }
        }
    }

    /**
     * Adds a plugin report to notesViewEntries as a Notes View Entry object.
     *
     * @param pluginReport Plugin report to be added.
     */
    protected void addPluginReport(final PluginReport pluginReport) {
        if (!isExistingNote(pluginReport)) {
            final NotesViewEntry note = new NotesViewEntry(
                    Long.toString(pluginReport.getStartTime()),
                    pluginReport.getPluginName(),
                    pluginReport.getMessage(),
                    false
            );

            /**
             * Listener monitors changes to the plugin report as it executes and
             * finishes. Affects the output of getMessage().
             */
            pluginReport.addPluginReportListener(note);

            synchronized (LOCK) {
                addNote(note);
            }
        }
    }

    /**
     * Returns an unmodifiable view backed by notesViewEntries.
     *
     * @return Unmodifiable view backed by notesViewEntries.
     */
    protected List<NotesViewEntry> getNotes() {
        return Collections.unmodifiableList(notesViewEntries);
    }

    /**
     * Returns an unmodifiable view backed by selectedFilters.
     *
     * @return Unmodifiable view backed by selectedFilters.
     */
    protected List<String> getFilters() {
        return Collections.unmodifiableList(selectedFilters);
    }

    /**
     * Sets notesViewEntries.
     *
     * @param notesViewEntries A list of NotesViewEntry objects to add to
     * notesViewEntries.
     */
    protected void setNotes(final List<NotesViewEntry> notesViewEntries) {
        Platform.runLater(() -> {
            synchronized (LOCK) {
                this.notesViewEntries.clear();

                notesViewEntries.forEach(note -> {
                    addNote(note);
                });
            }

            updateNotesUI();
        });
    }

    /**
     * Sets selectedFilters.
     *
     * @param selectFilters A list of String objects to add to selectedFilters.
     */
    protected void setFilters(final List<String> selectedFilters) {
        Platform.runLater(() -> {
            this.selectedFilters.clear();

            selectedFilters.forEach(filter -> {
                this.selectedFilters.add(filter);
            });

            updateFilters();
        });
    }

    /**
     * Updates the UI of the notes currently being displayed in the Notes View.
     */
    protected synchronized void updateNotesUI() {
        Platform.runLater(() -> {
            notesListVBox.getChildren().removeAll(notesListVBox.getChildren());

            final List<NotesViewEntry> notesToRender = new ArrayList<>();

            synchronized (LOCK) {
                notesViewEntries.forEach(entry -> {
                    // Add note to render list if its respective filter is selected.
                    if ((selectedFilters.contains(USER_NOTES_FILTER) && entry.isUserCreated())
                            || (selectedFilters.contains(AUTO_NOTES_FILTER) && !entry.isUserCreated())) {
                        notesToRender.add(entry);
                    }
                });
            }

            if (CollectionUtils.isNotEmpty(notesToRender)) {
                notesToRender.sort(Comparator.comparing(NotesViewEntry::getDateTime));
                notesToRender.forEach(note -> {
                    createNote(note);
                });
            }

            // Keeps the scroll bar at the bottom?
            notesListScrollPane.setVvalue(notesListScrollPane.getVmax());
        });
    }

    /**
     * Updates the UI of the filters currently being selected in the Notes View.
     */
    protected synchronized void updateFilters() {
        Platform.runLater(() -> {
            isSelectedFiltersUpdating = true;

            filterCheckComboBox.getCheckModel().clearChecks();

            selectedFilters.forEach(filter -> {
                filterCheckComboBox.getCheckModel().check(filter);
            });

            isSelectedFiltersUpdating = false;
        });
    }

    /**
     * Check if the PluginReport was already added
     *
     * @param pluginReport The PluginReport to add
     *
     * @return True if plugin report was already added, False otherwise
     */
    private boolean isExistingNote(final PluginReport pluginReport) {
        final String startTime = Long.toString(pluginReport.getStartTime());
        return notesDateTimeCache.contains(startTime);
    }

    /**
     * A convenient method to add a note to the various lists that are used to
     * track them.
     *
     * @param note A new NoteViewEntry to be added
     */
    private void addNote(final NotesViewEntry note) {
        notesViewEntries.add(note);
        notesDateTimeCache.add(note.getDateTime());
    }

    /**
     * Clears UI elements in the Notes View and clears the list of NoteEntry
     * objects.
     */
    protected void clearAllNotes() {
        Platform.runLater(() -> {
            notesListVBox.getChildren().removeAll(notesListVBox.getChildren());
        });

        synchronized (LOCK) {
            notesViewEntries.clear();
            notesDateTimeCache.clear();
        }
    }

    /**
     * Adds all available filters to selectedFilters.
     */
    protected void selectAllFilters() {
        setFilters(availableFilters);
    }

    /**
     * Takes a NoteEntry object and creates the UI for it in the Notes View.
     *
     * @param newNote NoteEntry object used to create a the note UI in the Notes
     * View.
     */
    private void createNote(final NotesViewEntry newNote) {

        final String noteColour = newNote.isUserCreated() ? USER_COLOUR : AUTO_COLOUR;

        final Label dateTimeLabel = new Label((new SimpleDateFormat(DATETIME_PATTERN).format(new Date(Long.parseLong(newNote.getDateTime())))));
        dateTimeLabel.setWrapText(true);
        dateTimeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15;");

        final Label titleLabel = new Label(newNote.getNoteTitle());
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15;");

        final Label contentLabel = new Label(newNote.getNoteContent());
        contentLabel.setWrapText(true);

        final VBox noteInformation = new VBox(DEFAULT_SPACING, dateTimeLabel, titleLabel, contentLabel);
        HBox.setHgrow(noteInformation, Priority.ALWAYS);

        final Button editButton = new Button("Edit");
        editButton.setMinWidth(55);
        editButton.setOnAction(event -> {
            openEdit(newNote.getNoteTitle(), newNote.getNoteContent(), newNote);
            event.consume();
        });

        final Button deleteButton = new Button("Delete");
        deleteButton.setMinWidth(55);

        final VBox noteButtons = new VBox(DEFAULT_SPACING, editButton, deleteButton);
        noteButtons.setAlignment(Pos.CENTER);

        final HBox noteBody = newNote.isUserCreated() ? new HBox(DEFAULT_SPACING, noteInformation, noteButtons) : new HBox(DEFAULT_SPACING, noteInformation);
        noteBody.setStyle("-fx-padding: 5px; -fx-background-color: " + noteColour + "; -fx-background-radius: 10 10 10 10;");
        notesListVBox.getChildren().add(noteBody);

        deleteButton.setOnAction(event -> {
            synchronized (LOCK) {
                if (notesViewEntries.removeIf(note -> note.getDateTime().equals(newNote.getDateTime()))) {
                    notesDateTimeCache.remove(newNote.getDateTime());

                    final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
                    if (activeGraph != null) {
                        updateNotesUI();
                        notesViewController.writeState(activeGraph);
                    }
                }
            }

            event.consume();
        });
    }

    /**
     * Pop-up window for editing user created notes.
     *
     * @param title
     * @param content
     * @param noteToEdit
     */
    private void openEdit(final String title, final String content, final NotesViewEntry noteToEdit) {
        Platform.runLater(() -> {
            editStage = new Stage();
            editStage.getIcons().add(new Image(NotesViewPane.class.getResourceAsStream(NOTES_VIEW_ICON)));
            editStage.setTitle("Edit Note");

            final TextField newTitle = new TextField(title);
            newTitle.setPromptText("Edit title...");
            newTitle.setStyle("-fx-prompt-text-fill: " + PROMPT_COLOUR + ";");
            newTitle.setText(title);

            final TextArea newContent = new TextArea(content);
            newContent.setPromptText("Edit note...");
            newContent.setStyle("-fx-prompt-text-fill: " + PROMPT_COLOUR + ";");
            newContent.setText(content);
            newContent.setWrapText(true);
            newContent.setOnKeyPressed(key -> {
                // If tab is typed and shift isn't being held dowm.
                if (key.getCode() == KeyCode.TAB && !key.isShiftDown()) {
                    // Backspace any tabs typed.
                    newContent.fireEvent(new KeyEvent(null, null, KeyEvent.KEY_PRESSED, "", "", KeyCode.BACK_SPACE, false, false, false, false));
                    // Move focus to the next UI element.
                    newContent.getParent().getChildrenUnmodifiable().get(newContent.getParent().getChildrenUnmodifiable().indexOf(newContent) + 1).requestFocus();
                }
            });

            final Button saveButton = new Button("Save");
            saveButton.setOnAction(event -> {
                if ((newTitle.getText().isBlank() && newTitle.getText().isEmpty())
                        || (newContent.getText().isBlank() && newContent.getText().isEmpty())) {
                    JOptionPane.showMessageDialog(null, "Type in missing fields.", "Invalid Text", JOptionPane.WARNING_MESSAGE);
                } else {
                    noteToEdit.setNoteTitle(newTitle.getText());
                    noteToEdit.setNoteContent(newContent.getText());

                    final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
                    if (activeGraph != null) {
                        updateNotesUI();
                        notesViewController.writeState(activeGraph);
                    }

                    closeEdit();
                }

                event.consume();
            });

            final VBox editNoteVBox = new VBox(DEFAULT_SPACING, newTitle, newContent, saveButton);
            editNoteVBox.setAlignment(Pos.CENTER_RIGHT);
            editNoteVBox.setStyle("-fx-padding: 5px;");

            final Scene scene = new Scene(editNoteVBox, 250, 200);
            editStage.setScene(scene);
            editStage.show();
        });
    }

    /**
     * Convenience method to close the pop-up window for editing user created
     * notes.
     */
    protected void closeEdit() {
        Platform.runLater(() -> {
            if (editStage != null && editStage.isShowing()) {
                editStage.close();
            }
        });
    }
}
