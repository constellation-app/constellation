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
import java.util.List;
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
import org.apache.commons.collections.CollectionUtils;
import org.controlsfx.control.CheckComboBox;

/**
 * Handles generating UI elements for the Notes View pane and its notes.
 * 
 * @author sol695510
 */
public class NotesViewPane extends BorderPane implements PluginReportListener {
    
    private final NotesViewController notesViewController;
    private final List<NotesViewEntry> notesViewEntries;
    
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
    private final String DATETIME_PATTERN = "hh:mm:ss a 'on' dd/MM/yyyy";
    
    /**
     * NotesViewPane constructor.
     * 
     * @param controller 
     */
    public NotesViewPane(final NotesViewController controller) {
        
        notesViewController = controller;
        notesViewEntries = new ArrayList<>();
        
        availableFilters = FXCollections.observableArrayList("User Notes", "Auto Notes");
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
                        updateNotes();
                        controller.writeState();
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
                    notesViewEntries.add(new NotesViewEntry(
                            Long.toString(ZonedDateTime.now().toInstant().toEpochMilli()),
                            titleField.getText(),
                            contentField.getText(),
                            true
                    ));
                    
                    titleField.clear();
                    contentField.clear();
                    updateNotes();
                    controller.writeState();
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
     * Prepares the pane used by the Notes View.
     * 
     * @param controller 
     * @param pane 
     */
    protected synchronized void prepareNotesViewPane(final NotesViewController controller) {
        controller.readState();
        controller.addAttributes();
    }
    
    /**
     * Set the plugin reports that have executed on the current graph report.
     */
    protected synchronized void setGraphReport(final NotesViewController controller) {
        final GraphReport currentGraphReport = GraphReportManager.getGraphReport(GraphManager.getDefault().getActiveGraph().getId());

        if (currentGraphReport != null) {
            // Iterates the list of currently executed plugins.
            currentGraphReport.getPluginReports().forEach(pluginReport -> {
                setPluginReport(pluginReport);
            });
            // Clears duplicates from the list.
            final List<NotesViewEntry> uniqueNotes = clearDuplicates(notesViewEntries);
            notesViewEntries.clear();
            notesViewEntries.addAll(uniqueNotes);
            // Update the Notes View UI.
            updateNotes();
            updateFilters();
            controller.writeState();
        }
    }
    
    /**
     * Adds a plugin report to notesViewEntries as a Notes View Entry object.
     * 
     * @param pluginReport Plugin report to be added.
     */
    protected synchronized void setPluginReport(final PluginReport pluginReport) {
        // Omit plugin reports from the Notes View and Quality Control View.
        if ((!pluginReport.getPluginName().contains("Notes View"))
                && (!pluginReport.getPluginName().contains("Quality Control View"))) {
            // Listener monitors changes to the plugin report as it executes and finishes. Affects the output of getMessage().
            pluginReport.addPluginReportListener(this);

            notesViewEntries.add(new NotesViewEntry(
                    Long.toString(pluginReport.getStartTime()),
                    pluginReport.getPluginName(),
                    pluginReport.getMessage(),
                    false
            ));
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
     * @param notesViewEntries A list of NotesViewEntry objects to add to notesViewEntries.
     */
    protected synchronized void setNotes(final List<NotesViewEntry> notesViewEntries) {
        Platform.runLater(() -> {
            this.notesViewEntries.clear();
            
            notesViewEntries.forEach(entry -> {
                this.notesViewEntries.add(entry);
            });
            
            updateNotes();
        });
    }
    
    /**
     * Sets selectedFilters.
     * 
     * @param selectFilters A list of String objects to add to selectedFilters.
     */
    protected synchronized void setFilters(final List<String> selectedFilters) {
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
    protected synchronized void updateNotes() {
        Platform.runLater(() -> {
            notesListVBox.getChildren().removeAll(notesListVBox.getChildren());
            
            final List<NotesViewEntry> notesToRender = new ArrayList<>();
            
            notesViewEntries.forEach(entry -> {
                // Add user note to render list if "User Note" filter is selected.
                if (selectedFilters.contains("User Notes") && entry.isUserCreated()) {
                    notesToRender.add(entry);
                }
                // Add auto note to render list if "Auto Note" filter is selected.
                if (selectedFilters.contains("Auto Notes") && !entry.isUserCreated()) {
                    notesToRender.add(entry);
                }
            });
            
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
     * Clears UI elements in the Notes View and clears the list of NoteEntry objects.
     * 
     * @param clearOnlyUI True if only the UI elements are intended to be cleared.
     */
    protected void clearNotes(final boolean clearOnlyUI) {
        Platform.runLater(() -> {
            notesListVBox.getChildren().removeAll(notesListVBox.getChildren());
            if (!clearOnlyUI) {
                notesViewEntries.clear();
            }
        });
    }
    
    /**
     * Iterates list of NoteEntry objects and
     * removes objects that share the same dateTime.
     * 
     * @param duplicatedNotes A list with duplicates.
     * @return The given list with duplicates removed.
     */
    private synchronized List<NotesViewEntry> clearDuplicates(final List<NotesViewEntry> duplicatedNotes) {
        final List<NotesViewEntry> uniqueNotes = new ArrayList();
        Collections.reverse(duplicatedNotes);
        
        duplicatedNotes.forEach(report -> {
            boolean isUnique = true;
            
            for (final NotesViewEntry uniqueReport : uniqueNotes) {
                if (report.getDateTime().equals(uniqueReport.getDateTime())) {
                    isUnique = false;
                }
            }
            // Adds only unique notes to the list.
            if (isUnique) {
                uniqueNotes.add(report);
            }
        });
        
        return uniqueNotes;
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
     * @param newNote NoteEntry object used to create a the note UI in the Notes View.
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
            if (notesViewEntries.removeIf(note -> note.getDateTime().equals(newNote.getDateTime()))) {
                updateNotes();
                notesViewController.writeState();
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
                    updateNotes();
                    notesViewController.writeState();
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
     * Convenience method to close the pop-up window for editing user created notes.
     */
    protected void closeEdit() {
        Platform.runLater(() -> {
            if (editStage != null && editStage.isShowing()) {
                editStage.close();
            }
        });
    }
    
    /**
     * Triggers when plugin reports undergo a change, such as when they go between executing and finishing.
     * 
     * @param pluginReport 
     */
    @Override
    public void pluginReportChanged(final PluginReport pluginReport) {
        setPluginReport(pluginReport);
        // Clears duplicates from the list.
        final List<NotesViewEntry> uniqueNotes = clearDuplicates(notesViewEntries);
        notesViewEntries.clear();
        notesViewEntries.addAll(uniqueNotes);
        // Update the Notes View UI.
        updateNotes();
        notesViewController.writeState();
    }
    
    @Override
    public void addedChildReport(final PluginReport parentReport, final PluginReport childReport) {
        // Intentionally left blank.
    }
}
