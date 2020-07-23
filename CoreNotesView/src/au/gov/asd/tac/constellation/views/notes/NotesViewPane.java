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

/**
 *
 * @author sol695510
 */
public class NotesViewPane extends BorderPane {

    private final NotesViewController controller;
    private String currentGraphId;
    private GraphReport currentGraphReport;

    //private final PluginReport pluginReport;
    private final VBox mainNotesPane;
    private final VBox userNotesPane;
    private final VBox autoNotesPane;
    //private final TextArea testText;

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
//        final Label autoText = new Label(pluginReportList.toString());

        // headings for text areas
        final Label noteTitleHeading = new Label("Note Title:");
        final Label noteContentHeading = new Label("Note Content:");

        // Text area to enter new note
        final TextArea titleTextArea = new TextArea();
        titleTextArea.setPrefRowCount(1);
        titleTextArea.setMaxWidth(150);
        HBox.setHgrow(titleTextArea, Priority.ALWAYS);

        final TextArea contentTextArea = new TextArea();
        contentTextArea.setPrefRowCount(0);
        contentTextArea.setMaxWidth(150);
        HBox.setHgrow(contentTextArea, Priority.ALWAYS);

        final Button addButton = new Button("Add New Note");
        addButton.setAlignment(Pos.CENTER_RIGHT);
        addButton.setOnAction(event -> {
            addNote(titleTextArea.getText(), contentTextArea.getText());
            event.consume();
        });
        HBox.setHgrow(addButton, Priority.ALWAYS);
        HBox addNoteHBox = new HBox(5, noteTitleHeading, titleTextArea, noteContentHeading, contentTextArea, addButton);

        this.userNotesPane = new VBox(5);
        this.autoNotesPane = new VBox(5);
        this.mainNotesPane = new VBox(5, userText, userNotesPane, autoText, autoNotesPane, addNoteHBox);

        // add layers grid and options to pane
//        this.UserNotesPane = new VBox(5, visibleText);
        // create layout bindings
//        MainNotesPane.prefWidthProperty().bind(this.widthProperty());
        this.setCenter(mainNotesPane);
    }

    public NotesViewController getController() {
        return controller;
    }

    void setGraphRecord(String currentGraphId) {
        this.currentGraphReport = new GraphReport(currentGraphId);
        // update ui
    }

    public List<NotesViewEntry> getNotes() {
        return Collections.unmodifiableList(notesEntries);
    }

    private void addNote(final String title, final String content) {
        final NotesViewEntry note = new NotesViewEntry(true, LocalDateTime.now().toString(), title, content);
        notesEntries.add(note);
        controller.writeState();

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
            VBox noteVBox = new VBox(5, noteTitleLabel, noteContentLabel, noteTimestampLabel);
            noteVBox.setPadding(new Insets(10, 0, 10, 0));
            userNotesPane.getChildren().add(noteVBox);
        // Auto generated note
        } else {
            final Label noteTitleLabel = new Label(title);
            final Label noteContentLabel = new Label(content);
            final Label noteTimestampLabel = new Label(timestamp);
            VBox noteVBox = new VBox(5, noteTitleLabel, noteContentLabel, noteTimestampLabel);
            noteVBox.setPadding(new Insets(10, 0, 10, 0));
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
            //controller.writeState(); // save state on update
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
            // and remove all notes before refreshing.
            if (notes != null) {
                userNotesPane.getChildren().removeAll();
            }
            if (pluginReports != null) {
                autoNotesPane.getChildren().removeAll();
            }

            if (notes != null) {
                // and finally create a UI note for each of the new notes in the list
                for (final NotesViewEntry note : sortByTimestamp(notes)) {
                    createNote(note.getNoteTitle(), note.getNoteContent(), note.getTimestamp().toString(), true);
                }
            }

            if (pluginReports != null) {
                // Plugin Report creation - pluginReports might be all sorted. if not make a sort method
                for (final PluginReport pluginReport : pluginReports) {
                    //TODO: these will need to be changed to something more meaningful
                    createNote(pluginReport.getPluginName(), pluginReport.getMessage(), String.valueOf(pluginReport.getStartTime()), false);
                }
            }

        }
    }

    public synchronized void clearContents() {
        Platform.runLater(() -> {
            this.notesEntries.clear();
            userNotesPane.getChildren().clear();
            this.pluginReportList.clear();
            autoNotesPane.getChildren().clear();
        });
    }

    private List<NotesViewEntry> sortByTimestamp(final List<NotesViewEntry> notes) {
        //loop all notes and reorder by timestamp
        // TODO: currently unimplemented
        return notes;
    }

}
