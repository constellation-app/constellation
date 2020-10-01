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
import au.gov.asd.tac.constellation.plugins.reporting.PluginReportListener;
import au.gov.asd.tac.constellation.views.notes.state.NotesViewEntry;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.swing.JOptionPane;

/**
 *
 * @author sol695510
 */
public class NotesViewPane extends BorderPane implements PluginReportListener {

    private final NotesViewController notesViewController;
    private GraphReport currentGraphReport;
    
    private final VBox mainNotesVBox;
    private final VBox addNoteVBox;
    private final VBox notesListVBox;
    private final ScrollPane notesListScrollPane;
    private Stage editStage;
    
    private final int DEFAULT_SPACING = 5;
    private final String PROMPT_COLOUR = "#909090";
    private final String USER_COLOUR = "#C15A58";
    private final String AUTO_COLOUR = "#588BC1";
    private final String DATETIME_PATTERN = "hh:mm:ss a dd/MM/yyyy";

    private final List<PluginReport> pluginReports;
    private final List<NotesViewEntry> noteEntries;

    public NotesViewPane(final NotesViewController controller) {
        
        pluginReports = new ArrayList<>();
        noteEntries = new ArrayList<>();
        
        // Create controller.
        this.notesViewController = controller;

        // TextField to enter new note title.
        final TextField titleField = new TextField();
        titleField.setPromptText("Title");
        titleField.setStyle("-fx-prompt-text-fill: " + PROMPT_COLOUR + ";");

        // TextArea to enter new note content.
        final TextArea contentField = new TextArea();
        contentField.setPromptText("Take a note...");
        contentField.setStyle("-fx-prompt-text-fill: " + PROMPT_COLOUR + ";");
        contentField.setWrapText(true);

        // Button to add new note.
        final Button addNoteButton = new Button("Add Note");
        addNoteButton.setOnAction(event -> {
            if (titleField.getText().isBlank()
                && titleField.getText().isEmpty()
                && contentField.getText().isBlank()
                && contentField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Enter a title and type a note.", "Invalid Text", JOptionPane.WARNING_MESSAGE);
            } else {
                createNote(
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATETIME_PATTERN)),
                    titleField.getText(),
                    contentField.getText(),
                    true
                );
                titleField.clear();
                contentField.clear();
                controller.writeState();
                event.consume();
            }
        });

        // VBox to store control items used to add new note.
        addNoteVBox = new VBox(DEFAULT_SPACING, titleField, contentField, addNoteButton);
        addNoteVBox.setStyle("-fx-padding: 5px;");
        addNoteVBox.setMinHeight(200);

        // VBox in a ScrollPane in a VBox for holding expanding list of user and plugin generated notes.
        notesListVBox = new VBox(DEFAULT_SPACING);
        notesListVBox.setAlignment(Pos.BOTTOM_CENTER);
        notesListScrollPane = new ScrollPane();
        notesListScrollPane.setContent(notesListVBox);
        notesListScrollPane.setStyle("-fx-padding: 5px; -fx-background-color: transparent;");
        notesListScrollPane.setFitToWidth(true);
        VBox.setVgrow(notesListScrollPane, Priority.ALWAYS);
        
        // Main Notes View Pane VBox.
        mainNotesVBox = new VBox(DEFAULT_SPACING, notesListScrollPane, addNoteVBox);
        mainNotesVBox.setAlignment(Pos.BOTTOM_CENTER);
        this.setCenter(mainNotesVBox);
    }
    
    public NotesViewController getController() {
        return notesViewController;
    }
    
    public void prepareNotesViewPane(final NotesViewController controller, NotesViewPane pane) {
        pane.clearNotes();
        controller.readState();
        controller.addAttributes();
    }

    public void setGraphRecord(final String currentGraphId) {
        this.currentGraphReport = GraphReportManager.getGraphReport(currentGraphId);
        if (currentGraphId != null && currentGraphReport != null) {
            setPluginReports(currentGraphReport.getPluginReports());
        }
    }

    public List<NotesViewEntry> getNoteEntries() {
        return Collections.unmodifiableList(noteEntries);
    }

    public synchronized void setNoteEntries(final List<NotesViewEntry> noteEntries) {
        Platform.runLater(() -> {
            this.noteEntries.clear();
            final List<NotesViewEntry> noteEntriesCopy = new ArrayList();
            noteEntries.forEach((note) -> {
                noteEntriesCopy.add(new NotesViewEntry(note));
            });
            updateNoteEntries(noteEntriesCopy, null);
        });
    }

    public synchronized void setPluginReports(final List<PluginReport> pluginReports) {
        Platform.runLater(() -> {
            this.pluginReports.clear();
            final List<PluginReport> pluginReportsCopy = new ArrayList();
            pluginReports.forEach((report) -> {
                pluginReportsCopy.add(report);
            });
            updateNoteEntries(null, pluginReportsCopy);
        });
    }

    /**
     * When a parameter is null, it signifies that only a partial refresh is
     * occurring this method will then remove any entries from the pane
     * responsible for holding that type of note. This allows for full
     * refreshing of one pane or the other.
     *
     * @param noteEntries
     * @param pluginReports
     */
    private void updateNoteEntries(final List<NotesViewEntry> noteEntries, final List<PluginReport> pluginReports) {
        synchronized (this) {
            
            // Clear the UI first.
            notesListVBox.getChildren().removeAll(notesListVBox.getChildren());
            
            // TODO: PluginReports should update the items in NoteEntries then the UI should update from what's in NoteEntries.
            
            // Meant to be for user notes?
            if (noteEntries != null && !noteEntries.isEmpty()) {
                for (final NotesViewEntry note : noteEntries) {
                    createNote(
                        note.getDateTime(),
                        note.getNoteTitle(),
                        note.getNoteContent(),
                        true
                    );
                }
            }
            // Meant to be for auto notes?
            if (pluginReports != null && !pluginReports.isEmpty()) {
                for (final PluginReport pluginReport : pluginReports) {
                    if (!pluginReport.getPluginName().contains("Note")) {
                        pluginReport.addPluginReportListener(this);
                        createNote(
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATETIME_PATTERN)),
                            pluginReport.getPluginName(),
                            pluginReport.getMessage() +
                            "\nStart: " + new Date(pluginReport.getStartTime()) +
                            "\nStop: " + new Date(pluginReport.getStopTime()),
                            false
                        );
                    }
                }
            }
        }
    }

    public synchronized void clearNotes() {
        Platform.runLater(() -> {
            this.noteEntries.clear();
//            notesListVBox.getChildren().removeIf(note -> note instanceof VBox);
            this.pluginReports.clear();
//            notesListVBox.getChildren().removeIf(report -> report instanceof VBox);
            notesListVBox.getChildren().removeAll(notesListVBox.getChildren());
        });
    }

    private List<NotesViewEntry> sortNotesByTimestamp(final List<NotesViewEntry> notes) {
        // TODO: Loop all notes and reorder by timestamp to use in updateNotes, currently unimplemented.
        final List sortedNotes = new ArrayList<NotesViewEntry>();
        
        return sortedNotes;
    }
    
    public void createNote(final String dateTime, final String title, final String content, final boolean userCreated) {
        
        final String noteColour = userCreated ? USER_COLOUR : AUTO_COLOUR;
        
        final Label dateTimeLabel = new Label(dateTime);
        dateTimeLabel.setWrapText(true);
        
        final Label titleLabel = new Label(title);
        titleLabel.setWrapText(true);
        
        final Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        
        final VBox noteInformation = new VBox(DEFAULT_SPACING, dateTimeLabel, titleLabel, contentLabel);
        HBox.setHgrow(noteInformation, Priority.ALWAYS);
        
        final Button editButton = new Button("Edit");
        editButton.setMinWidth(55);
        editButton.setOnAction(e -> {
            editNote(title, content);
            e.consume();
        });
        
        final Button deleteButton = new Button("Delete");
        deleteButton.setMinWidth(55);
        
        final VBox noteButtons = new VBox(DEFAULT_SPACING, editButton, deleteButton);
        noteButtons.setAlignment(Pos.CENTER);
        
        final HBox noteBody = userCreated ? new HBox(DEFAULT_SPACING, noteInformation, noteButtons) : new HBox(DEFAULT_SPACING, noteInformation);
        noteBody.setStyle("-fx-padding: 5px; -fx-background-color: " + noteColour + "; -fx-background-radius: 10 10 10 10;");
        
        final NotesViewEntry newNote = new NotesViewEntry(dateTime, title, content, userCreated);
        noteEntries.add(newNote);
        notesListVBox.getChildren().add(noteBody);
        
        deleteButton.setOnAction(e -> {
            
            if (noteEntries.removeIf(note -> note.getNoteId() == newNote.getNoteId())) {
                notesListVBox.getChildren().remove(noteBody);
            }
            
            notesViewController.writeState();
            e.consume();
        });

        // Keeps scroll bar at the bottom.
        notesListScrollPane.setVvalue(notesListScrollPane.getVmax());
    }

    /**
     * Pop-up window for editing existing user created notes.
     * 
     * @param title
     * @param content 
     */
    public void editNote(final String title, final String content) {
        
        editStage = new Stage();
        editStage.getIcons().add(new Image("au/gov/asd/tac/constellation/views/notes/resources/notes-view.png"));
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
        
        final Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            
            e.consume();
        });
        
        final Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> {
            editStage.close();
            e.consume();
        });
        
        final HBox editNoteHBox = new HBox(DEFAULT_SPACING, saveButton, cancelButton);
        editNoteHBox.setAlignment(Pos.CENTER_RIGHT);
        final VBox editNoteVBox = new VBox(DEFAULT_SPACING, newTitle, newContent, editNoteHBox);
        editNoteVBox.setStyle("-fx-padding: 5px;");
        
        final Scene scene = new Scene(editNoteVBox, 250, 200);
        editStage.setScene(scene);
        editStage.show();
    }
    
    public void closeEditNote() {
        Platform.runLater(() -> {
            editStage.close();
        });
    }

    @Override
    public void pluginReportChanged(PluginReport pluginReport) {
        final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
        
        if (!pluginReport.getPluginName().contains("Note")) {
            this.prepareNotesViewPane(notesViewController, this);
            if (activeGraph != null) {
                this.setGraphRecord(activeGraph.getId());
            }
        }
    }

    @Override
    public void addedChildReport(PluginReport parentReport, PluginReport childReport) {
        
    }
}
