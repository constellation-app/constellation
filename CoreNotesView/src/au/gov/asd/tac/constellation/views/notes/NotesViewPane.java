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

import au.gov.asd.tac.constellation.plugins.reporting.GraphReport;
import au.gov.asd.tac.constellation.plugins.reporting.PluginReport;
import au.gov.asd.tac.constellation.views.notes.state.NotesViewEntry;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author sol695510
 */
public class NotesViewPane extends BorderPane {

    private final NotesViewController controller;
    private String currentGraphId;
    private GraphReport currentGraphReport;

    //private PluginReport pluginReport;
    private final VBox mainNotesPane;
    private final VBox userNotesPane;
    private final VBox autoNotesPane;

    private static final Insets NOTE_PADDING = new Insets(10, 0, 10, 0);
    private static final int BOX_SPACING = 5;
    private static final String DELETE_BUTTON = "Delete";
    private static final String ADD_BUTTON = "Add New Note";
    private static final String NOTE_TITLE = "Note Title:";
    private static final String NOTE_CONTENT = "Note Content:";

    private final List<NotesViewEntry> notesEntries;
    private final List<PluginReport> pluginReportList;

//    Create buttons and panes to show notes
//    TODO tabs to show both Auto and User generated notes
//    TODO get data about plugin use from PluginReporter
    public NotesViewPane(final NotesViewController controller) {

        notesEntries = new ArrayList<>();
        pluginReportList = new ArrayList<>();

        // create controller
        this.controller = controller;

        // THIS LINE CAUSES AN ERROR RELATING TO THE CONSTRUCTOR!
        //pluginReportList = currentGraphReport.getPluginReports();
        // placeholder label for content example
        final Label userText = new Label("User Notes Here\n");
        final Label autoText = new Label("Auto Notes Here\n");

        // headings for user notes text areas
        final Label noteTitleHeading = new Label(NOTE_TITLE);
        final Label noteContentHeading = new Label(NOTE_CONTENT);

        // Text area to enter note title
        final TextArea titleTextArea = new TextArea();
        titleTextArea.setPrefRowCount(1);
        titleTextArea.setMaxWidth(150);
        HBox.setHgrow(titleTextArea, Priority.ALWAYS);

        // Text area to enter note content
        final TextArea contentTextArea = new TextArea();
        contentTextArea.setPrefRowCount(0);
        contentTextArea.setMaxWidth(150);
        HBox.setHgrow(contentTextArea, Priority.ALWAYS);

        // Button to add a new note
        final Button addButton = new Button(ADD_BUTTON);
        addButton.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(addButton, Priority.ALWAYS);
        addButton.setOnAction(event -> {
            createNote(titleTextArea.getText(), contentTextArea.getText(), LocalDateTime.now().toString(), true);
            controller.writeState();
            event.consume();
        });

        // Adding items used to 'add a new note' to HBox
        final HBox addNoteHBox = new HBox(BOX_SPACING, noteTitleHeading, titleTextArea, noteContentHeading, contentTextArea, addButton);

        // VBoxes used for holding notes of user and plugin generated types
        this.userNotesPane = new VBox(BOX_SPACING, userText);
        this.autoNotesPane = new VBox(BOX_SPACING, autoText);

        // Adding
        this.mainNotesPane = new VBox(BOX_SPACING, userNotesPane, autoNotesPane, addNoteHBox);

        // create layout bindings
        mainNotesPane.prefWidthProperty().bind(this.widthProperty());
        this.setCenter(mainNotesPane);
    }

    public NotesViewController getController() {
        return controller;
    }

    void setGraphRecord(final String currentGraphId) {
        this.currentGraphReport = new GraphReport(currentGraphId);
        // update ui
    }

    public List<NotesViewEntry> getNotes() {
        return Collections.unmodifiableList(notesEntries);
    }

    private void createNote(final String title, final String content, final String timestamp, final boolean isUserNote) {
        // creates a note in the UI
        // make title element
        // make content element
        // make timestamp element
        // differentiate from User and Auto notes?
        if (isUserNote) {
            final Label noteTitleLabel = new Label(title);
            final Label noteContentLabel = new Label(content);
            final Label noteTimestampLabel = new Label(timestamp);
            final Button remove = new Button(DELETE_BUTTON);
            remove.setOnAction(event -> {
                // delete this note
                if (StringUtils.isNotBlank(timestamp)) {
                    notesEntries.removeIf(note -> timestamp.equals(note.getTimestamp()));
                    final List<NotesViewEntry> newNotesEntries = List.copyOf(notesEntries);
                    userNotesPane.getChildren().removeIf(note -> note instanceof VBox);
                    for (final NotesViewEntry note : sortByTimestamp(newNotesEntries)) {
                        createNote(note.getNoteTitle(), note.getNoteContent(), note.getTimestamp().toString(), true);
                    }
                }
                controller.writeState();
                setNotes(notesEntries);
                event.consume();
            });
            HBox.setHgrow(remove, Priority.ALWAYS);
            final HBox deleteHBox = new HBox(BOX_SPACING, remove);
            final VBox noteVBox = new VBox(BOX_SPACING, noteTitleLabel, noteContentLabel, noteTimestampLabel, deleteHBox);
            noteVBox.setPadding(NOTE_PADDING);
            notesEntries.add(new NotesViewEntry(isUserNote, timestamp, title, content));
            userNotesPane.getChildren().add(noteVBox);
        // Auto generated note
        } else {
            final Label noteTitleLabel = new Label(title);
            final Label noteContentLabel = new Label(content);
            final Label noteTimestampLabel = new Label(timestamp);
            final VBox noteVBox = new VBox(BOX_SPACING, noteTitleLabel, noteContentLabel, noteTimestampLabel);
            noteVBox.setPadding(NOTE_PADDING);
            // add entry into auto notes list / pluginreports?
            autoNotesPane.getChildren().add(noteVBox);
        }
    }

    public synchronized void setNotes(final List<NotesViewEntry> notes) {
        Platform.runLater(() -> {
            this.notesEntries.clear();
            final List<NotesViewEntry> notesCopy = new ArrayList();
            notes.forEach((note) -> {
                notesCopy.add(new NotesViewEntry(note));
            });
            updateNotes(notesCopy, null);
        });
    }

    public synchronized void setReports(final List<PluginReport> pluginReports) {
        Platform.runLater(() -> {
            this.pluginReportList.clear();
            final List<PluginReport> reportsCopy = new ArrayList();
            pluginReports.forEach((report) -> {
                reportsCopy.add(report);
            });
            updateNotes(null, reportsCopy);
        });
    }

    /**
     * When a parameter is null, it signifies that only a partial refresh is
     * occurring this method will then remove any entries from the pane
     * responsible for holding that type of note. This allows for full
     * refreshing of one pane or the other.
     *
     * @param notes
     * @param pluginReports
     */
    private void updateNotes(final List<NotesViewEntry> notes, final List<PluginReport> pluginReports) {
        synchronized (this) {
            // this code should iterate the UI structure which contains the notes entries
            // and clear notes before refreshing.
            if (CollectionUtils.isNotEmpty(notes)) {
                userNotesPane.getChildren().removeIf(note -> note instanceof VBox);
                // and finally create a UI note for each of the new notes in the list
                for (final NotesViewEntry note : sortByTimestamp(notes)) {
                    createNote(note.getNoteTitle(), note.getNoteContent(), note.getTimestamp().toString(), true);
                }
            }
            if (CollectionUtils.isNotEmpty(pluginReports)) {
                autoNotesPane.getChildren().removeIf(report -> report instanceof VBox);
                // Plugin Report creation - pluginReports might be all sorted. if not make a sort method
                for (final PluginReport pluginReport : pluginReports) {
                    //TODO: these will need to be changed to something more meaningful
                    createNote(pluginReport.getPluginName(), pluginReport.getMessage(), String.valueOf(pluginReport.getStartTime()), false);
                }
            }
        }
    }

    /**
     * Clears the pane of items within stored lists and pane children which are
     * VBoxes ready for a new graph or graph closed.
     */
    public synchronized void clearContents() {
        Platform.runLater(() -> {
            this.notesEntries.clear();
            userNotesPane.getChildren().removeIf(note -> note instanceof VBox);
            this.pluginReportList.clear();
            autoNotesPane.getChildren().removeIf(report -> report instanceof VBox);
        });
    }

    private List<NotesViewEntry> sortByTimestamp(final List<NotesViewEntry> notes) {
        //loop all notes and reorder by timestamp
        // TODO: currently unimplemented
        return notes;
    }

}
