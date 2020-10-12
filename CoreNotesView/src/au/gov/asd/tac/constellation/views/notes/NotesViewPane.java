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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import org.apache.commons.collections.CollectionUtils;

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
    private final String DATETIME_PATTERN = "hh:mm:ss a 'on' dd/MM/yyyy";

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
                final NotesViewEntry newNote = new NotesViewEntry(
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATETIME_PATTERN)),
                    titleField.getText(),
                    contentField.getText(),
                    true
                );
                
                noteEntries.add(newNote);
                createNote(newNote);
                
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
//            final List<NotesViewEntry> noteEntriesCopy = new ArrayList();
            noteEntries.forEach((entry) -> {
                this.noteEntries.add(new NotesViewEntry(entry));
//                notesViewController.writeState();
            });
            updateNoteEntries(this.noteEntries, null);
        });
    }

    public synchronized void setPluginReports(final List<PluginReport> pluginReports) {
        Platform.runLater(() -> {
            this.pluginReports.clear();
//            final List<PluginReport> pluginReportsCopy = new ArrayList();
            pluginReports.forEach((report) -> {
                if (!report.getPluginName().contains("Note")) {
                    //this.pluginReports.add(report);
//                    final NotesViewEntry newNote = new NotesViewEntry(
//                        (new SimpleDateFormat(DATETIME_PATTERN).format(new Date(report.getStartTime()))),
//                        report.getPluginName(),
//                        report.getMessage(),
//                        false
//                    );
                    this.pluginReports.add(report);
//                notesViewController.writeState();
                }
            });
            updateNoteEntries(null, this.pluginReports);
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
            
            List<NotesViewEntry> notesToRender = new ArrayList<>();
            
            // Clear the UI first.
            notesListVBox.getChildren().removeAll(notesListVBox.getChildren());
            
            // TODO: PluginReports should update the items in NoteEntries then the UI should update from what's in NoteEntries.
            
            // Meant to be for user notes?
            if (this.noteEntries != null && !this.noteEntries.isEmpty()) {
                for (final NotesViewEntry note : this.noteEntries) {
                    notesToRender.add(note);
//                    final NotesViewEntry newNote = new NotesViewEntry(
//                        note.getDateTime(),
//                        note.getNoteTitle(),
//                        note.getNoteContent(),
//                        true
//                    );
                    
                    //noteEntries.add(note);
                    //createNote(note);
//                    notesViewController.writeState();
                }
            }
            // Meant to be for auto notes?
            if (this.pluginReports != null && !this.pluginReports.isEmpty()) {
                for (final PluginReport pluginReport : this.pluginReports) {
                    if (!pluginReport.getPluginName().contains("Note")) {
                        pluginReport.addPluginReportListener(this);
                        
                        final NotesViewEntry newNote = new NotesViewEntry(
                            (new SimpleDateFormat(DATETIME_PATTERN).format(new Date(pluginReport.getStartTime()))),
                            pluginReport.getPluginName(),
                            pluginReport.getMessage(),
                            false
                        );
                        
//                        this.noteEntries.add(newNote);
                        notesToRender.add(newNote);
                        //createNote(newNote);
                    }
                }
            }
            if(CollectionUtils.isNotEmpty(notesToRender)){
                //notesToRender.sort(c);
                for(final NotesViewEntry entry : notesToRender){
                    createNote(entry);
                }
                //
            }
            //notesViewController.writeState();
        }
    }

    public synchronized void clearNotes() {
        Platform.runLater(() -> {
            this.noteEntries.clear();
//            notesListVBox.getChildren().removeIf(note -> note instanceof VBox);
            //this.pluginReports.clear();
//            notesListVBox.getChildren().removeIf(report -> report instanceof VBox);
            notesListVBox.getChildren().removeAll(notesListVBox.getChildren());
        });
    }

    private List<NotesViewEntry> sortNotesByTimestamp(final List<NotesViewEntry> notes) {
        // TODO: Loop all notes and reorder by timestamp to use in updateNotes, currently unimplemented.
        final List sortedNotes = new ArrayList<NotesViewEntry>();
        
        return sortedNotes;
    }
    
    
    public void createNote(final NotesViewEntry newNote) {
        
        final String noteColour = newNote.isUserCreated() ? USER_COLOUR : AUTO_COLOUR;
        
        final Label dateTimeLabel = new Label(newNote.getDateTime());
        dateTimeLabel.setWrapText(true);
        
        final Label titleLabel = new Label(newNote.getNoteTitle());
        titleLabel.setWrapText(true);
        
        final Label contentLabel = new Label(newNote.getNoteContent());
        contentLabel.setWrapText(true);
        
        final VBox noteInformation = new VBox(DEFAULT_SPACING, dateTimeLabel, titleLabel, contentLabel);
        HBox.setHgrow(noteInformation, Priority.ALWAYS);
        
        final Button editButton = new Button("Edit");
        editButton.setMinWidth(55);
        editButton.setOnAction(event -> {
            editNote(newNote.getNoteTitle(), newNote.getNoteContent());
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
            
            if (noteEntries.removeIf(note -> note.getNoteId() == newNote.getNoteId())) {
                notesListVBox.getChildren().remove(noteBody);
                notesViewController.writeState();
            }

            event.consume();
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
        
        // TODO: write state when editing
        final Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> {
            
            event.consume();
        });
        
        final Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> {
            editStage.close();
            event.consume();
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
            if(editStage != null){
                editStage.close();
            }
            
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
