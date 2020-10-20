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
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final List<NotesViewEntry> notesViewEntries;
    private final Map<String, NotesViewEntry> notesMap;
    private GraphReport currentGraphReport;
    
    private final VBox notesViewPaneVBox;
    private final VBox addNoteVBox;
    private final VBox notesListVBox;
    private final ScrollPane notesListScrollPane;
    private Stage editBox;
    
    private final int DEFAULT_SPACING = 5;
    private final String PROMPT_COLOUR = "#909090";
    private final String USER_COLOUR = "#C15A58";
    private final String AUTO_COLOUR = "#588BC1";
    private final String DATETIME_PATTERN = "hh:mm:ss a 'on' dd/MM/yyyy";

    public NotesViewPane(final NotesViewController controller) {
        
        // Create controller.
        notesViewController = controller;
        notesViewEntries = new ArrayList<>();
        notesMap = new HashMap<>();

        // TextField to enter new note title.
        final TextField titleField = new TextField();
        titleField.setPromptText("Title...");
        titleField.setStyle("-fx-prompt-text-fill: " + PROMPT_COLOUR + ";");

        // TextArea to enter new note content.
        final TextArea contentField = new TextArea();
        contentField.setPromptText("Take a note...");
        contentField.setStyle("-fx-prompt-text-fill: " + PROMPT_COLOUR + ";");
        contentField.setWrapText(true);

        // Button to add new note.
        final Button addNoteButton = new Button("Add Note");
        addNoteButton.setOnAction(event -> {
            
            if (currentGraphReport != null) {
                
                if ((titleField.getText().isBlank() && titleField.getText().isEmpty()) 
                        || (contentField.getText().isBlank() && contentField.getText().isEmpty())) {
                    JOptionPane.showMessageDialog(null, "Type in missing fields.", "Invalid Text", JOptionPane.WARNING_MESSAGE);
                } else {
                    final NotesViewEntry newNote = new NotesViewEntry(
                            Long.toString(ZonedDateTime.now().toInstant().toEpochMilli()),
                            titleField.getText(),
                            contentField.getText(),
                            true
                    );

                    titleField.clear();
                    contentField.clear();
                    createNote(newNote);

                    notesViewEntries.add(newNote);
                    controller.writeState();
                    event.consume();
                }
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
        notesViewPaneVBox = new VBox(DEFAULT_SPACING, notesListScrollPane, addNoteVBox);
        notesViewPaneVBox.setAlignment(Pos.BOTTOM_CENTER);
        setCenter(notesViewPaneVBox);
    }
    
    protected void prepareNotesViewPane(final NotesViewController controller, final NotesViewPane pane) {
        controller.readState();
        controller.addAttributes();
    }

    protected void setGraphRecord(final String currentGraphId) {
        currentGraphReport = GraphReportManager.getGraphReport(currentGraphId);
        if (currentGraphId != null && currentGraphReport != null) {
            setPluginReports(currentGraphReport.getPluginReports());
        }
    }

    protected synchronized void setPluginReports(final List<PluginReport> pluginReports) {
        Platform.runLater(() -> {
            pluginReports.forEach((report) -> {
                if (!report.getPluginName().contains("Note")) {
                    // Monitors changes to the plugin report as it executes and finishes.
                    // Affects the output of getMessage() so the note UI updates automatically.
                    report.addPluginReportListener(this);
                    
                    notesViewEntries.add(new NotesViewEntry(
                            Long.toString(report.getStartTime()),
                            report.getPluginName(),
                            report.getMessage(),
                            false
                    ));

                    if (report.getStopTime() != -1) {
                        notesViewController.writeState();
                    }
                }
            });
            
            updateNotes();
        });
    }
    
    protected synchronized void clearPluginReportsOnly() {
        Platform.runLater(() -> {
            notesListVBox.getChildren().removeAll(notesListVBox.getChildren());
            notesViewEntries.removeIf(note -> !note.isUserCreated());
        });
    }

    protected synchronized void setNotes(final List<NotesViewEntry> notesViewEntries) {
        Platform.runLater(() -> {
            this.notesViewEntries.clear();
            notesViewEntries.forEach((entry) -> {
                this.notesViewEntries.add(entry);
            });
            
            updateNotes();
        });
    }

    protected List<NotesViewEntry> getNotes() {
        return Collections.unmodifiableList(notesViewEntries);
    }

    protected synchronized void updateNotes() {
        Platform.runLater(() -> {
            notesListVBox.getChildren().removeAll(notesListVBox.getChildren());
            
            if (CollectionUtils.isNotEmpty(notesViewEntries)) {
                notesViewEntries.sort(Comparator.comparing(NotesViewEntry::getDateTime));
                notesViewEntries.forEach((note) -> {
                    createNote(note);
                });
            }
        });
    }

    protected synchronized void clearNotes() {
        Platform.runLater(() -> {
            notesListVBox.getChildren().removeAll(notesListVBox.getChildren());
            notesViewEntries.clear();
        });
    }    
    
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
            if (notesViewEntries.removeIf(note -> note.getDateTime().equals(newNote.getDateTime()))) {
                notesListVBox.getChildren().remove(noteBody);
                notesViewController.writeState();
            }

            event.consume();
        });

        // Keeps the scroll bar at the bottom?
        Platform.runLater(() -> {
            notesListScrollPane.setVvalue(notesListScrollPane.getVmax()); 
        });
    }

    /**
     * Pop-up window for editing existing user created notes.
     * 
     * @param title
     * @param content 
     */
    private void openEdit(final String title, final String content, final NotesViewEntry noteToEdit) {
        
        editBox = new Stage();
        editBox.getIcons().add(new Image("au/gov/asd/tac/constellation/views/notes/resources/notes-view.png"));
        editBox.setTitle("Edit Note");
        
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
        saveButton.setOnAction(event -> {
            if ((newTitle.getText().isBlank() && newTitle.getText().isEmpty()) 
                    || (newContent.getText().isBlank() && newContent.getText().isEmpty())) {
                JOptionPane.showMessageDialog(null, "Type in missing fields.", "Invalid Text", JOptionPane.WARNING_MESSAGE);
            } else {
                noteToEdit.setNoteTitle(newTitle.getText());
                noteToEdit.setNoteContent(newContent.getText());
                updateNotes();
                notesViewController.writeState();
                closeEdit();
            }
            
            event.consume();
        });
        
        final Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> {
            closeEdit();
            event.consume();
        });
        
        final HBox editNoteHBox = new HBox(DEFAULT_SPACING, saveButton, cancelButton);
        editNoteHBox.setAlignment(Pos.CENTER_RIGHT);
        
        final VBox editNoteVBox = new VBox(DEFAULT_SPACING, newTitle, newContent, editNoteHBox);
        editNoteVBox.setStyle("-fx-padding: 5px;");
        
        final Scene scene = new Scene(editNoteVBox, 250, 200);
        editBox.setScene(scene);
        editBox.show();
    }
    
    protected void closeEdit() {
        Platform.runLater(() -> {
            if (editBox != null) {
                editBox.close();
            }
        });
    }

    // Triggers when plugin reports undergo a change, such as when they go between executing and finishing.
    @Override
    public void pluginReportChanged(PluginReport pluginReport) {
        
        final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
        
        if (!pluginReport.getPluginName().contains("Note")) {
            clearPluginReportsOnly();
            prepareNotesViewPane(notesViewController, this);
            
            if (activeGraph != null) {
                setGraphRecord(activeGraph.getId());
            }
        }
    }

    @Override
    public void addedChildReport(PluginReport parentReport, PluginReport childReport) {
        
    }
}
