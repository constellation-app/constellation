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
package au.gov.asd.tac.constellation.views.notes;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.VisualGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.visual.plugins.select.ChangeSelectionPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.select.SelectionMode;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.parameters.types.ElementTypeParameterValue;
import au.gov.asd.tac.constellation.plugins.reporting.GraphReport;
import au.gov.asd.tac.constellation.plugins.reporting.GraphReportManager;
import au.gov.asd.tac.constellation.plugins.reporting.PluginReport;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.font.FontUtilities;
import au.gov.asd.tac.constellation.utilities.gui.MultiChoiceInputField;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.notes.state.NotesViewEntry;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Window;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.CheckComboBox;
import org.openide.util.HelpCtx;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

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
    private final MultiChoiceInputField filterSelectionMultiChoiceInput;
    private MultiChoiceInputField autoFilterCheckComboBox;
    private boolean isSelectedFiltersUpdating = false;
    private boolean isAutoSelectedFiltersUpdating = false;

    private final VBox notesListVBox;
    private final ScrollPane notesListScrollPane;
    private static final double NOTE_HEIGHT = 157.0;
    private static final int DEFAULT_SPACING = 5;
    private static final int EDIT_SPACING = 10;
    private static final String SHOW_MORE = "Show more";
    private static final String SHOW_LESS = "Show less";
    private static final String USER_COLOR = "#942483";
    private static final String AUTO_COLOR = "#1c5aa6";
    private static String USER_CHOSEN_COLOUR = USER_COLOR;
    private static final String DATETIME_PATTERN = "hh:mm:ss a 'on' dd/MM/yyyy"; // TODO: make this a preference so that we can support their local timestamp format instead.

    private static final String AUTO_NOTES_FILTER = "Auto Notes";
    private static final String USER_NOTES_FILTER = "User Notes";
    private static final String SELECTED_FILTER = "Selected";

    private static final String PADDING_BG_COLOUR_STYLE = "-fx-padding: 5px; -fx-background-color: ";
    private static final String BG_RADIUS_STYLE = "; -fx-background-radius: 10 10 10 10;";

    private static final Object LOCK = new Object();

    private final String fontStyle = String.format("-fx-font-size:%d;", FontUtilities.getApplicationFontSize());
    private static final String BOLD_STYLE = "-fx-font-weight: bold;";

    private final List<Integer> nodesSelected = new ArrayList<>();
    private final List<Integer> transactionsSelected = new ArrayList<>();
    private final List<String> tagsUpdater = new ArrayList<>();
    private ObservableList<String> tagsFiltersList;
    private final List<String> tagsSelectedFiltersList = new ArrayList<>();

    private final DateTimeRangePicker dateTimeRangePicker = new DateTimeRangePicker();
    private final Button createNewNoteButton = new Button();
    private boolean creatingFirstNote = true;
    private final NewNotePane newNotePane;
    private int noteID = 0;
    private final Map<Integer, String> previouseColourMap = new HashMap<>();

    public static final Logger LOGGER = Logger.getLogger(NotesViewPane.class.getName());

    /**
     * NotesViewPane constructor.
     *
     * @param controller
     */
    public NotesViewPane(final NotesViewController controller) {
        notesViewController = controller;
        notesViewEntries = new ArrayList<>();
        notesDateTimeCache = new HashSet<>();

        availableFilters = FXCollections.observableArrayList(USER_NOTES_FILTER, AUTO_NOTES_FILTER, SELECTED_FILTER);
        selectedFilters = new ArrayList<>();
        selectedFilters.add(USER_NOTES_FILTER); // Only user notes are selected by default.

        // CheckComboBox to select and deselect various filters for note rendering.
        filterSelectionMultiChoiceInput = new MultiChoiceInputField(availableFilters);
        filterSelectionMultiChoiceInput.setTitle("Select a filter...");
        filterSelectionMultiChoiceInput.setMinWidth(165);
        filterSelectionMultiChoiceInput.setMaxWidth(165);
        filterSelectionMultiChoiceInput.setStyle(String.format("-fx-font-size:%d;", FontUtilities.getApplicationFontSize()));
        filterSelectionMultiChoiceInput.getCheckModel().getCheckedItems().addListener((final ListChangeListener.Change event) -> {
            if (!isSelectedFiltersUpdating) {
                setFilters(filterSelectionMultiChoiceInput.getCheckModel().getCheckedItems());

                final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
                if (activeGraph != null) {
                    updateNotesUI();
                    controller.writeState(activeGraph);
                }
            }
            final ObservableList<String> filters = filterSelectionMultiChoiceInput.getCheckModel().getCheckedItems();
            final String checkedFilters = String.join(", ", filters);
            filterSelectionMultiChoiceInput.setTitle(filters.isEmpty()? "Select a filter..." : checkedFilters);
        });
        
        notesViewEntries.forEach(entry -> {
            if (!entry.isUserCreated()) {

                final List<String> tags = entry.getTags();
                for (final String tag : tags) {
                    if (!tagsUpdater.contains(tag)) {
                        tagsUpdater.add(tag);
                    }
                }
            }
        });

        tagsFiltersList = FXCollections.observableArrayList(tagsUpdater);

        // CheckComboBox for the Auto Note filters.
        autoFilterCheckComboBox = new MultiChoiceInputField(tagsFiltersList);
        autoFilterCheckComboBox.setStyle(String.format("-fx-font-size:%d;", FontUtilities.getApplicationFontSize()));
        autoFilterCheckComboBox.getCheckModel().getCheckedItems().addListener((final ListChangeListener.Change event) -> {
            if (!isAutoSelectedFiltersUpdating) {
                updateSelectedTagsCombo(autoFilterCheckComboBox.getCheckModel().getCheckedItems());

                final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
                if (activeGraph != null) {
                    updateNotesUI();
                    controller.writeState(activeGraph);
                }
            }
        });
        autoFilterCheckComboBox.setTitle("Select tag(s)...");
        autoFilterCheckComboBox.setDisable(true);

        // Set whether or not a time filter should even be applied
        dateTimeRangePicker.getClearButton().setOnAction(event -> {
            dateTimeRangePicker.setActive(false);
            dateTimeRangePicker.disableAll(false);
            final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
            if (activeGraph != null) {
                updateNotesUI();
                controller.writeState(activeGraph);
            }
            event.consume();
        });

        // Hide/show notes based on their entry time
        dateTimeRangePicker.getApplyButton().setOnAction(event -> {
            dateTimeRangePicker.setActive(true);
            dateTimeRangePicker.disableAll(true);
            dateTimeRangePicker.showClearButton();
            final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
            if (activeGraph != null) {
                updateNotesUI();
                controller.writeState(activeGraph);
            }
            event.consume();
        });

        final Button helpButton = new Button("", new ImageView(UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.BLUEBERRY.getJavaColor())));
        helpButton.paddingProperty().set(new Insets(2, 0, 0, 0));
        helpButton.setTooltip(new Tooltip("Display help for Notes View"));
        helpButton.setOnAction(event -> new HelpCtx(NotesViewTopComponent.class.getName()).display());
        // Get rid of the ugly button look so the icon stands alone.
        helpButton.setStyle("-fx-border-color: transparent;-fx-background-color: transparent;");

        // FlowPane to store control items used to filter notes.
        final ToolBar toolBar = new ToolBar();
        toolBar.getItems().addAll(createNewNoteButton, filterSelectionMultiChoiceInput, autoFilterCheckComboBox, dateTimeRangePicker.getTimeFilterMenu(), helpButton);
        // Create the actual node that allows user to add new notes
        newNotePane = new NewNotePane(USER_CHOSEN_COLOUR);

        // Button to trigger pop-up window to make a new note
        createNewNoteButton.setText("Create Note");
        createNewNoteButton.setStyle(String.format("-fx-font-size:%d;", FontUtilities.getApplicationFontSize()));
        createNewNoteButton.setStyle("-fx-text-fill: #00FF00;");

        createNewNoteButton.setOnAction(event -> {
            if (creatingFirstNote) {
                newNotePane.setParent(this.getScene().getWindow());
                creatingFirstNote = false;
            }
            newNotePane.showPopUp(this.getScene().getWindow());
        });

        // Event handler to add new note
        newNotePane.getAddButtion().setOnAction(event -> {
            final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
            if (activeGraph != null) {
                if (newNotePane.getTitleField().getText().isBlank()
                        || newNotePane.getContentField().getText().isBlank()) {
                    newNotePane.closePopUp();
                    final Window w = this.getScene().getWindow();
                    final List<Screen> screens = Screen.getScreensForRectangle(w.getX(), w.getY(), w.widthProperty().get(), w.heightProperty().get());

                    final int popUpWidth = 300;
                    final int popUpHeight = 150;

                    final int xPos = (int) (screens.get(0).getVisualBounds().getMinX() + screens.get(0).getVisualBounds().getWidth() / 2) - popUpWidth / 2;
                    final int yPos = (int) (screens.get(0).getVisualBounds().getMinY() + screens.get(0).getVisualBounds().getHeight() / 2) - popUpHeight / 2;

                    final Alert warningAlert = new Alert(AlertType.WARNING);
                    warningAlert.setTitle("Invalid Text");
                    warningAlert.setContentText("Type in missing fields.");
                    warningAlert.setX(xPos);
                    warningAlert.setY(yPos);
                    warningAlert.setWidth(popUpWidth);
                    warningAlert.setHeight(popUpHeight);
                    final Optional o = warningAlert.showAndWait();
                    newNotePane.showPopUp(this.getScene().getWindow());
                } else {
                    synchronized (LOCK) {
                        notesViewEntries.add(new NotesViewEntry(
                                Long.toString(ZonedDateTime.now().toInstant().toEpochMilli()),
                                newNotePane.getTitleField().getText(),
                                newNotePane.getContentField().getText(),
                                true,
                                !newNotePane.isApplySelected(),
                                newNotePane.getUserChosenColour()
                        ));
                        if (newNotePane.isApplySelected()) {
                            LOGGER.log(Level.SEVERE, "Selecting nodes to link to note");
                            // Get selected nodes from the graph.
                            final List<Integer> selectedNodes = new ArrayList<>();
                            // Get selected transactions from the graph.
                            final List<Integer> selectedTransactions = new ArrayList<>();

                            final ReadableGraph rg = activeGraph.getReadableGraph();
                            try {
                                // Add selected nodes.
                                final int vxSelectedAttr = rg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());
                                if (vxSelectedAttr != Graph.NOT_FOUND) {
                                    final int vxCount = rg.getVertexCount();
                                    for (int position = 0; position < vxCount; position++) {
                                        final int vxId = rg.getVertex(position);
                                        if (rg.getBooleanValue(vxSelectedAttr, vxId)) {
                                            selectedNodes.add(vxId);
                                        }
                                    }
                                }

                                // Add selected transactions.
                                final int txSelectedAttr = rg.getAttribute(GraphElementType.TRANSACTION, VisualConcept.TransactionAttribute.SELECTED.getName());
                                if (txSelectedAttr != Graph.NOT_FOUND) {
                                    final int txCount = rg.getTransactionCount();
                                    for (int position = 0; position < txCount; position++) {
                                        final int txId = rg.getTransaction(position);
                                        if (rg.getBooleanValue(txSelectedAttr, txId)) {
                                            selectedTransactions.add(txId);
                                        }
                                    }
                                }

                                // If there are no selected nodes or transactions on the graph, set the graph attribute to true.
                                if (selectedNodes.isEmpty() && selectedTransactions.isEmpty()) {
                                    notesViewEntries.get(notesViewEntries.size() - 1).setGraphAttribute(true);
                                } else {
                                    // Add selected nodes to the note entry.
                                    notesViewEntries.get(notesViewEntries.size() - 1).setNodesSelected(selectedNodes);
                                    // Add selected transactions to the node entry.
                                    notesViewEntries.get(notesViewEntries.size() - 1).setTransactionsSelected(selectedTransactions);
                                }

                            } finally {
                                rg.release();
                            }
                        }
                    }
                    newNotePane.clearTextFields();
                    newNotePane.closePopUp();
                    updateNotesUI();
                    controller.writeState(activeGraph);
                    event.consume();
                }
            }
        });

        // VBox in a ScrollPane for holding expanding list of user and plugin generated notes.
        notesListVBox = new VBox(DEFAULT_SPACING);
        notesListVBox.setAlignment(Pos.BOTTOM_CENTER);
        notesListScrollPane = new ScrollPane();
        notesListScrollPane.setContent(notesListVBox);
        notesListScrollPane.setStyle(fontStyle + "-fx-padding: 5px; -fx-background-color: transparent;");
        notesListScrollPane.setFitToWidth(true);

        setTop(toolBar);
        setCenter(notesListScrollPane);
    }

    /**
     * Set the plugin reports that have executed on the current graph report.
     */
    protected void setGraphReport(final Graph graph, final NotesViewController controller) {
        if (graph != null) {
            final GraphReport currentGraphReport = GraphReportManager.getGraphReport(graph.getId());

            if (currentGraphReport != null) {
                // Iterates the list of currently executed plugins.
                currentGraphReport.getPluginReports().forEach(pluginReport -> {
                    // Omit low level plugins which are not useful as notes.
                    if (!pluginReport.hasLowLevelTag()) {
                        addPluginReport(pluginReport);
                    }
                });

                SwingUtilities.invokeLater(() -> {
                    final TopComponent tc = WindowManager.getDefault().findTopComponent(NotesViewTopComponent.class.getSimpleName());
                    if (tc != null && tc.isOpened()) {
                        // Update the Notes View UI.
                        updateNotesUI();
                        updateFilters();
                    }
                });

                controller.writeState(graph);
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
                    false,
                    false,
                    "#ffffff"
            );

            final String[] tags = pluginReport.getTags();
            final List<String> tagsList = new ArrayList<>();
            for (final String tag : tags) {
                tagsList.add(tag);
            }
            note.setTags(tagsList);

            /**
             * Listener monitors changes to the plugin report as it executes and
             * finishes. Affects the output of getMessage().
             */
            pluginReport.addPluginReportListener(note);

            synchronized (LOCK) {
                addNote(note);
            }
            updateTagsFiltersAvailable();
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
        synchronized (LOCK) {
            this.notesViewEntries.clear();
            notesViewEntries.forEach(this::addNote);
        }

        SwingUtilities.invokeLater(() -> {
            final TopComponent tc = WindowManager.getDefault().findTopComponent(NotesViewTopComponent.class.getSimpleName());
            if (tc != null && tc.isOpened()) {
                updateNotesUI();
            }
        });
    }

    /**
     * Sets selectedFilters.
     *
     * @param selectFilters A list of String objects to add to selectedFilters.
     */
    protected void setFilters(final List<String> selectedFilters) {
        if (this.selectedFilters.contains(SELECTED_FILTER) && (selectedFilters.contains(USER_NOTES_FILTER) || selectedFilters.contains(AUTO_NOTES_FILTER))) {
            this.selectedFilters.clear();

            selectedFilters.forEach(filter -> {
                if (!filter.equals(SELECTED_FILTER)) {
                    this.selectedFilters.add(filter);
                }
            });
        } else {
            this.selectedFilters.clear();
            selectedFilters.forEach(filter -> {
                if (selectedFilters.contains(SELECTED_FILTER)) {
                    this.selectedFilters.add(SELECTED_FILTER);
                } else {
                    this.selectedFilters.add(filter);
                }
            });
        }
        if (this.selectedFilters.contains(AUTO_NOTES_FILTER)) {
            Platform.runLater(() -> autoFilterCheckComboBox.setDisable(false));
            updateTagsFiltersAvailable();
        } else {
            Platform.runLater(() -> autoFilterCheckComboBox.setDisable(true));
        }
        updateFilters();
    }

    /**
     * Updates the UI of the notes currently being displayed in the Notes View.
     */
    protected synchronized void updateNotesUI() {
        final List<NotesViewEntry> notesToRender = new ArrayList<>();
        updateSelectedElements();

        synchronized (LOCK) {
            notesViewEntries.forEach(entry -> {
                if (dateTimeRangePicker.isActive()) {
                    // Get date time of entry in proper format
                    final String dateFormat = new SimpleDateFormat(DATETIME_PATTERN).format(new Date(Long.parseLong(entry.getDateTime())));

                    // Extract date components
                    final String[] dateTimeComponents = dateFormat.split(" ");
                    final String time = dateTimeComponents[0];
                    final String date = dateTimeComponents[3];

                    // Split time into hour, minute and day
                    final String[] timeComponents = time.split(":");
                    int hour = Integer.parseInt(timeComponents[0]);
                    final int min = Integer.parseInt(timeComponents[1]);
                    final int sec = Integer.parseInt(timeComponents[2]);

                    if ("pm".equals(dateTimeComponents[1]) && hour < 12) {
                        hour = 12 + hour;
                    } else if ("am".equals(dateTimeComponents[1]) && hour > 11) {
                        hour = 0;
                    }

                    // Split date into day, month and year
                    final String[] dateComponents = date.split("/");
                    final int day = Integer.parseInt(dateComponents[0]);
                    final int month = Integer.parseInt(dateComponents[1]);
                    final int year = Integer.parseInt(dateComponents[2]);

                    // Convert time of notes to user specified time zone
                    final ZonedDateTime entryTime = ZonedDateTime.of(year, month, day, hour, min, sec, 0, ZoneId.of(ZoneId.systemDefault().getId()));

                    entry.setShowing(dateTimeRangePicker.checkIsWithinRange(entryTime));

                } else {
                    entry.setShowing(true);
                }

                // Add note to render list if its respective filter is selected.
                if ((selectedFilters.contains(USER_NOTES_FILTER) && entry.isUserCreated() && entry.getShowing())) {
                    notesToRender.add(entry);

                } else if (selectedFilters.contains(AUTO_NOTES_FILTER) && !entry.isUserCreated() && entry.getShowing()) {
                    if (updateAutoNotesDisplayed(entry)) {
                        notesToRender.add(entry);
                    }

                } else if (selectedFilters.contains(SELECTED_FILTER) && entry.isUserCreated() && entry.getShowing()) {
                    // If no nodes or transactions are selected, show notes applied to the whole graph.
                    if (entry.isGraphAttribute()) {
                        notesToRender.add(entry);
                    }
                    // Show notes related to the selected nodes.
                    for (final int node : nodesSelected) {
                        if (entry.getNodesSelected() != null && entry.getNodesSelected().contains(node) && !notesToRender.contains(entry)) {
                            notesToRender.add(entry);
                        }
                    }
                    // Shows notes related to the selected transactions.
                    for (final int transaction : transactionsSelected) {
                        if (entry.getTransactionsSelected() != null && entry.getTransactionsSelected().contains(transaction)
                                && !notesToRender.contains(entry)) {
                            notesToRender.add(entry);
                        }
                    }
                }

            });
        }

        Platform.runLater(() -> {
            boolean foundNoteInEdit = false;
            for (final NotesViewEntry entry : notesToRender) {
                if (entry.getEditMode()) {
                    foundNoteInEdit = true;
                    break;
                }
            }

            if (!foundNoteInEdit) {
                notesListVBox.getChildren().removeAll(notesListVBox.getChildren());

                if (CollectionUtils.isNotEmpty(notesToRender)) {
                    notesToRender.sort(Comparator.comparing(NotesViewEntry::getDateTime));
                    notesToRender.forEach(note -> {
                        note.setEditMode(note.checkIfWasInEditMode());
                        createNote(note);
                    });
                }
                notesListScrollPane.applyCss();
                notesListScrollPane.layout();
                // Keeps the scroll bar at the bottom?
                notesListScrollPane.setVvalue(notesListScrollPane.getVmax());
            }
        });
    }

    /**
     * Updates the UI of the filters currently being selected in the Notes View.
     */
    protected synchronized void updateFilters() {
        Platform.runLater(() -> {
            isSelectedFiltersUpdating = true;

            filterSelectionMultiChoiceInput.getCheckModel().clearChecks();
            selectedFilters.forEach(filter -> filterSelectionMultiChoiceInput.getCheckModel().check(filter));

            isSelectedFiltersUpdating = false;
            updateTagFilters();
        });
    }

    /**
     * Check if the PluginReport was already added.
     *
     * @param pluginReport The PluginReport to add.
     *
     * @return True if plugin report was already added, False otherwise.
     */
    private boolean isExistingNote(final PluginReport pluginReport) {
        final String startTime = Long.toString(pluginReport.getStartTime());
        return notesDateTimeCache.contains(startTime);
    }

    /**
     * A convenient method to add a note to the various lists that are used to
     * track them.
     *
     * @param note A new NoteViewEntry to be added.
     */
    private void addNote(final NotesViewEntry note) {
        notesViewEntries.add(note);
        notesDateTimeCache.add(note.getDateTime());
    }

    /**
     * Clears UI elements in the Notes View.
     */
    protected void clearNotes() {
        Platform.runLater(() -> notesListVBox.getChildren().removeAll(notesListVBox.getChildren()));
        synchronized (LOCK) {
            notesViewEntries.forEach(note -> {
                if (note.getEditMode()) {
                    note.setEditMode(false);
                    note.setWasInEditMode(true);
                } else {
                    note.setWasInEditMode(false);
                }
            });
            notesViewEntries.clear();
            notesDateTimeCache.clear();
        }
    }

    /**
     * Takes a NoteEntry object and creates the UI for it in the Notes View.
     *
     * @param newNote NoteEntry object used to create a the note UI in the Notes
     * View.
     */
    private void createNote(final NotesViewEntry newNote) {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Not processing on the JavaFX Application Thread");
        }

        if (newNote.getID() < 0) {
            newNote.setID(++noteID);
        }

        if (!previouseColourMap.containsKey(newNote.getID())) {
            previouseColourMap.put(newNote.getID(), newNote.getNodeColour());
        }

        // Define dateTime label
        final Label dateTimeLabel = new Label((new SimpleDateFormat(DATETIME_PATTERN).format(new Date(Long.parseLong(newNote.getDateTime())))));
        dateTimeLabel.setWrapText(true);
        dateTimeLabel.setStyle(BOLD_STYLE + fontStyle);
        dateTimeLabel.setMinWidth(185);
        dateTimeLabel.setMaxWidth(185);
        dateTimeLabel.setPadding(new Insets(0, 0, 0, 0));
        // Define title text box
        final TextField titleText = new TextField(newNote.getNoteTitle());
        titleText.setStyle(BOLD_STYLE);
        titleText.setOnKeyTyped(event -> newNote.setTempTitle(titleText.getText()));

        // Define title label
        final Label titleLabel = new Label(newNote.getNoteTitle());
        titleLabel.setWrapText(true);
        titleLabel.setStyle(BOLD_STYLE + fontStyle);
        titleLabel.setStyle("-fx-font-color: #FFFFFF;");

        // Define content label
        final Label contentLabel = new Label(newNote.getNoteContent());
        contentLabel.setWrapText(true);
        contentLabel.setMinWidth(50);
        contentLabel.setAlignment(Pos.TOP_LEFT);

        // Define content text area
        final TextArea contentTextArea = new TextArea(newNote.getNoteContent());
        contentTextArea.setWrapText(true);
        contentTextArea.positionCaret(contentTextArea.getText() == null ? 0 : contentTextArea.getText().length());
        contentTextArea.setOnKeyTyped(event -> newNote.setTempContent(contentTextArea.getText()));

        if (newNote.checkIfWasInEditMode()) {
            titleText.setText(newNote.getTempTitle());
            contentTextArea.setText(newNote.getTempContent());
        }

        final VBox noteInformation;

        // Define selection label
        String selectionLabelText = "";
        final Label selectionLabel = new Label(selectionLabelText);

        // If the note is user created add the selection details.
        if (newNote.isUserCreated()) {
            if (newNote.isGraphAttribute()) {
                selectionLabelText = "Note linked to: the graph.";
            } else {
                selectionLabelText = "Note linked to: ";
                if (newNote.getNodesSelected().size() == 1) {
                    selectionLabelText += newNote.getNodesSelected().size() + " node, ";
                } else {
                    selectionLabelText += newNote.getNodesSelected().size() + " nodes, ";
                }
                if (newNote.getTransactionsSelected().size() == 1) {
                    selectionLabelText += newNote.getTransactionsSelected().size() + " transaction. ";
                } else {
                    selectionLabelText += newNote.getTransactionsSelected().size() + " transactions. ";
                }
            }
            selectionLabel.setText(selectionLabelText);
            selectionLabel.setWrapText(true);
            selectionLabel.setStyle("-fx-font-weight: bold; -fx-font-style: italic; " + fontStyle);
            selectionLabel.setMinWidth(300);
            selectionLabel.setAlignment(Pos.CENTER_RIGHT);

            // If the note to be created is in edit mode, ensure it is created
            // with the correct java fx elements
            noteInformation = new VBox(DEFAULT_SPACING, newNote.getEditMode() ? titleText : titleLabel,
                    newNote.getEditMode() ? contentTextArea : contentLabel);

            HBox.setHgrow(noteInformation, Priority.ALWAYS);
        } else {
            // If the note to be created is in edit mode, ensure it is created
            // with the correct java fx elements
            noteInformation = new VBox(DEFAULT_SPACING, newNote.getEditMode() ? titleText : titleLabel,
                    newNote.getEditMode() ? contentTextArea : contentLabel);
            HBox.setHgrow(noteInformation, Priority.ALWAYS);
        }

        // Define buttons (edit, save, add, renove, delete)
        final Button editTextButton = new Button("Edit");
        editTextButton.setMinWidth(92);
        editTextButton.setStyle(String.format("-fx-font-size:%d;", FontUtilities.getApplicationFontSize()));

        final Button saveTextButton = new Button("Save");
        saveTextButton.setMinWidth(92);
        saveTextButton.setStyle(String.format("-fx-font-size:%d;", FontUtilities.getApplicationFontSize()));

        final Button deleteButton = new Button("Delete Note");
        deleteButton.setMinWidth(92);
        deleteButton.setStyle(String.format("-fx-font-size:%d;", FontUtilities.getApplicationFontSize()));

        final Button cancelButton = new Button("Cancel");
        cancelButton.setMinWidth(92);
        cancelButton.setStyle(String.format("-fx-font-size:%d;", FontUtilities.getApplicationFontSize()));

        final HBox editScreenButtons = new HBox(DEFAULT_SPACING, saveTextButton, cancelButton);
        editScreenButtons.setAlignment(Pos.CENTER);

        // If the note to be created is in edit mode, ensure it is created with
        // the correct java fx elements
        final HBox noteButtons;
        final Region gap = new Region();
        final Region gap2 = new Region();
        final Region topGap = new Region();

        gap.setPrefWidth(615);
        gap2.setPrefWidth(650);
        topGap.setPrefWidth(650);
        topGap.setMinWidth(10);
        gap.setMinWidth(10);
        gap2.setMinWidth(10);

        if (newNote.getNodeColour().isBlank()) {
            newNote.setNodeColour(USER_COLOR);
        }

        HBox.setHgrow(dateTimeLabel, Priority.NEVER);
        HBox.setHgrow(editScreenButtons, Priority.ALWAYS);

        final ColorPicker colourPicker = new ColorPicker(ConstellationColor.fromHtmlColor(newNote.getNodeColour()).getJavaFXColor());
        colourPicker.setMinWidth(100);
        colourPicker.setMaxWidth(100);
        HBox.setHgrow(colourPicker, Priority.NEVER);
        final Button showMoreButton = new Button(SHOW_MORE);
        showMoreButton.setMinWidth(100);
        showMoreButton.setMaxWidth(100);
        showMoreButton.setVisible(false);
        if (newNote.getEditMode()) {
            noteButtons = new HBox(EDIT_SPACING, colourPicker, gap2, editScreenButtons);
            newNote.setEditMode(true);
        } else {
            newNote.setEditMode(false);
            noteButtons = new HBox(DEFAULT_SPACING, showMoreButton, gap, editTextButton, deleteButton);
        }

        HBox.setHgrow(gap, Priority.ALWAYS);
        HBox.setHgrow(gap2, Priority.ALWAYS);

        noteButtons.setAlignment(Pos.CENTER_RIGHT);

        final HBox noteTop = new HBox(dateTimeLabel, topGap, selectionLabel);
        noteTop.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(topGap, Priority.ALWAYS);

        final VBox noteBody = newNote.isUserCreated() ? new VBox(DEFAULT_SPACING, noteTop, noteInformation, noteButtons) : new VBox(DEFAULT_SPACING, dateTimeLabel, noteInformation);
        noteBody.prefWidthProperty().bind(this.widthProperty());
        noteBody.setMinWidth(500);
        noteBody.setMaxHeight(Double.MAX_VALUE);

        noteBody.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (obs.getValue().doubleValue() >= NOTE_HEIGHT - 10) {
                if (!showMoreButton.isVisible()) {
                    showMoreButton.setVisible(true);
                    showMoreButton.setText(SHOW_MORE);
                    noteBody.setMaxHeight(NOTE_HEIGHT - 5);
                }
            } else if (showMoreButton.isVisible()) {
                showMoreButton.setVisible(false);
            }
        });

        if (newNote.isUserCreated()) {
            noteBody.setStyle(PADDING_BG_COLOUR_STYLE + newNote.getNodeColour() + BG_RADIUS_STYLE);
            notesListVBox.getChildren().add(noteBody);
        } else {
            noteBody.setStyle(PADDING_BG_COLOUR_STYLE + AUTO_COLOR + BG_RADIUS_STYLE);
            notesListVBox.getChildren().add(noteBody);
        }

        noteBody.prefWidthProperty().bind(this.widthProperty());

        showMoreButton.setOnAction(event -> {
            if (showMoreButton.getText().equals(SHOW_MORE)) {
                contentLabel.setText(newNote.getNoteContent());
                noteBody.setMaxHeight(Double.MAX_VALUE);
                showMoreButton.setText(SHOW_LESS);
            } else if (showMoreButton.getText().equals(SHOW_LESS)) {
                contentLabel.setText(newNote.getNoteContent());
                noteBody.setMaxHeight(NOTE_HEIGHT - 5);
                showMoreButton.setText(SHOW_MORE);
            }
        });

        // Change colour of note to whatever user sleects
        colourPicker.setOnAction(event -> {
            final Color col = colourPicker.getValue();
            noteBody.setStyle(PADDING_BG_COLOUR_STYLE + ConstellationColor.fromFXColor(col).getHtmlColor() + BG_RADIUS_STYLE);
            newNote.setNodeColour(ConstellationColor.fromFXColor(col).getHtmlColor());
        });

        if (newNote.isUserCreated()) {
            // Add a right click context menu to user notes.
            final MenuItem selectOnGraphMenuItem = new MenuItem("Select on Graph");
            selectOnGraphMenuItem.setOnAction(event -> {
                final BitSet elementIdsTx = new BitSet();
                final BitSet elementIdsVx = new BitSet();

                final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
                if (newNote.isGraphAttribute()) {
                    // Select all elements with right click menu if the user note is applied to the whole graph.
                    final ReadableGraph rg = activeGraph.getReadableGraph();
                    try {
                        final int vxCount = rg.getVertexCount();
                        for (int position = 0; position < vxCount; position++) {
                            final int vxId = rg.getVertex(position);
                            elementIdsVx.set(vxId);
                        }
                        final int txCount = rg.getTransactionCount();
                        for (int position = 0; position < txCount; position++) {
                            final int txId = rg.getTransaction(position);
                            elementIdsTx.set(txId);
                        }
                    } finally {
                        rg.release();
                    }
                } else {
                    // Select the specific nodes and/or transactions applied to the note.
                    // Add nodes that are selected to the note.
                    final int nodesLength = newNote.getNodesSelected().size();
                    final List<Integer> selectedNodes = newNote.getNodesSelected();
                    for (int i = 0; i < nodesLength; i++) {
                        elementIdsVx.set(selectedNodes.get(i));
                    }

                    // Add transactions that are selected to the note.
                    final int transactionsLength = newNote.getTransactionsSelected().size();
                    final List<Integer> selectedTransactions = newNote.getTransactionsSelected();
                    for (int i = 0; i < transactionsLength; i++) {
                        elementIdsTx.set(selectedTransactions.get(i));
                    }
                }

                PluginExecution.withPlugin(VisualGraphPluginRegistry.CHANGE_SELECTION)
                        .withParameter(ChangeSelectionPlugin.ELEMENT_BIT_SET_PARAMETER_ID, elementIdsTx)
                        .withParameter(ChangeSelectionPlugin.ELEMENT_TYPE_PARAMETER_ID, new ElementTypeParameterValue(GraphElementType.TRANSACTION))
                        .withParameter(ChangeSelectionPlugin.SELECTION_MODE_PARAMETER_ID, SelectionMode.REPLACE)
                        .executeLater(activeGraph);

                PluginExecution.withPlugin(VisualGraphPluginRegistry.CHANGE_SELECTION)
                        .withParameter(ChangeSelectionPlugin.ELEMENT_BIT_SET_PARAMETER_ID, elementIdsVx)
                        .withParameter(ChangeSelectionPlugin.ELEMENT_TYPE_PARAMETER_ID, new ElementTypeParameterValue(GraphElementType.VERTEX))
                        .withParameter(ChangeSelectionPlugin.SELECTION_MODE_PARAMETER_ID, SelectionMode.REPLACE)
                        .executeLater(activeGraph);
            });
            final MenuItem addOnGraphMenuItem = new MenuItem("Add Selected");
            addOnGraphMenuItem.setOnAction(event -> {
                // Save the current text in the text fields so they are not reset on updateNotesUI
                newNote.setNoteTitle(titleText.getText());
                newNote.setNoteContent(contentTextArea.getText());

                addToSelectedElements(newNote);
                final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
                if (activeGraph != null) {
                    updateNotesUI();
                    notesViewController.writeState(activeGraph);
                }
            });

            final MenuItem removeOnGraphMenuItem = new MenuItem("Remove Selected");
            removeOnGraphMenuItem.setOnAction(event -> {
                // Save the current text in the text fields so they are not reset on updateNotesUI
                newNote.setNoteTitle(titleText.getText());
                newNote.setNoteContent(contentTextArea.getText());

                removeFromSelectedElements(newNote);
                final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
                if (activeGraph != null) {
                    updateNotesUI();
                    notesViewController.writeState(activeGraph);
                }
            });

            if (newNote.getNodesSelected() != null && newNote.getTransactionsSelected() != null && newNote.getNodesSelected().isEmpty() && newNote.getTransactionsSelected().isEmpty()) {
                addOnGraphMenuItem.disableProperty().set(true);
                removeOnGraphMenuItem.disableProperty().set(true);
            }

            // Context menu is only added to user created notes.
            final ContextMenu contextMenu = new ContextMenu();
            contextMenu.getItems().addAll(selectOnGraphMenuItem, addOnGraphMenuItem, removeOnGraphMenuItem);

            noteBody.setOnContextMenuRequested(event -> contextMenu.show(this, event.getScreenX(), event.getScreenY()));
        }

        deleteButton.setOnAction(event -> {
            final Alert deleteAlert = new Alert(Alert.AlertType.CONFIRMATION);
            deleteAlert.setHeaderText("Delete Note");
            deleteAlert.setContentText("Are you sure you want to delete \"" + titleLabel.getText() + "\"?");

            deleteAlert.showAndWait();
            if (deleteAlert.getResult() == ButtonType.OK) {
                if (previouseColourMap.containsKey(newNote.getID())) {
                    previouseColourMap.remove(newNote.getID());
                }
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
            }
            deleteAlert.close();
        });

        // Edit button activates editable text boxs for title and label
        editTextButton.setOnAction(event -> {
            newNote.setTempTitle(newNote.getNoteTitle());
            newNote.setTempContent(newNote.getNoteContent());
            noteButtons.getChildren().removeAll(showMoreButton, gap, editTextButton, deleteButton);
            noteButtons.getChildren().addAll(colourPicker, gap2, editScreenButtons);
            noteButtons.setSpacing(EDIT_SPACING);

            noteInformation.getChildren().removeAll(titleLabel, contentLabel);
            titleText.setText(titleLabel.getText());
            contentTextArea.setText(contentLabel.getText());
            newNote.setNoteTitle(titleLabel.getText());
            newNote.setNoteContent(contentLabel.getText());
            noteInformation.getChildren().addAll(titleText, contentTextArea);
            newNote.setEditMode(true);

        });

        cancelButton.setOnAction(cancelEvent -> {
            final String currentColour = previouseColourMap.get(newNote.getID());
            colourPicker.setValue(ConstellationColor.fromHtmlColor(currentColour).getJavaFXColor());
            noteButtons.getChildren().removeAll(colourPicker, gap2, editScreenButtons);
            noteButtons.getChildren().addAll(showMoreButton, gap, editTextButton, deleteButton);
            noteButtons.setSpacing(DEFAULT_SPACING);

            titleText.setText(titleLabel.getText());
            contentTextArea.setText(contentLabel.getText());

            noteInformation.getChildren().removeAll(titleText, contentTextArea);
            noteInformation.getChildren().addAll(titleLabel, contentLabel);

            newNote.setEditMode(false);
            newNote.setWasInEditMode(false);
            noteBody.setStyle(PADDING_BG_COLOUR_STYLE
                    + currentColour + BG_RADIUS_STYLE);
            newNote.setNodeColour(currentColour);
        });

        // Save button deactivates editable text boxs for title and label
        saveTextButton.setOnAction(event -> {
            // Check if either the title or content text boxs are empty
            if (StringUtils.isBlank(titleText.getText()) || StringUtils.isBlank(contentTextArea.getText())) {
                JOptionPane.showMessageDialog(null, "Type in missing fields.", "Invalid Text", JOptionPane.WARNING_MESSAGE);
            } else {
                newNote.setEditMode(false);
                titleLabel.setText(titleText.getText());
                contentLabel.setText(contentTextArea.getText());

                newNote.setNoteTitle(titleText.getText());
                newNote.setNoteContent(contentTextArea.getText());
                previouseColourMap.replace(newNote.getID(), newNote.getNodeColour());

                noteInformation.getChildren().removeAll(titleText, contentTextArea);
                noteButtons.getChildren().removeAll(colourPicker, gap2, editScreenButtons);

                noteInformation.getChildren().addAll(titleLabel, contentLabel);
                noteButtons.getChildren().addAll(showMoreButton, gap, editTextButton, deleteButton);
                noteButtons.setSpacing(DEFAULT_SPACING);


                newNote.setWasInEditMode(false);

                synchronized (LOCK) {
                    final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
                    if (activeGraph != null) {
                        updateNotesUI();
                        notesViewController.writeState(activeGraph);
                    }
                }

            }
        });
    }

    /**
     * Updates the arrays of what nodes and transactions are currently selected.
     */
    public void updateSelectedElements() {
        nodesSelected.clear();
        transactionsSelected.clear();

        final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
        final ReadableGraph rg = activeGraph.getReadableGraph();
        try {
            // Get all currently selected nodes.
            final int vxSelectedAttr = rg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());
            if (vxSelectedAttr != Graph.NOT_FOUND) {
                final int vxCount = rg.getVertexCount();
                for (int position = 0; position < vxCount; position++) {
                    final int vxId = rg.getVertex(position);
                    if (rg.getBooleanValue(vxSelectedAttr, vxId)) {
                        nodesSelected.add(vxId);
                    }
                }
            }

            // Get all currently selected transactions.
            final int txSelectedAttr = rg.getAttribute(GraphElementType.TRANSACTION, VisualConcept.TransactionAttribute.SELECTED.getName());
            if (txSelectedAttr != Graph.NOT_FOUND) {
                final int txCount = rg.getTransactionCount();
                for (int position = 0; position < txCount; position++) {
                    final int txId = rg.getTransaction(position);
                    if (rg.getBooleanValue(txSelectedAttr, txId)) {
                        transactionsSelected.add(txId);
                    }
                }
            }
        } finally {
            rg.release();
        }
    }

    /**
     * Add what is currently selected on the graph to the note's selected
     * elements.
     */
    public void addToSelectedElements(final NotesViewEntry noteToEdit) {
        updateSelectedElements();

        if (!nodesSelected.isEmpty()) {
            if (noteToEdit.isGraphAttribute()) {
                noteToEdit.setGraphAttribute(false);
            }
            final List<Integer> originalNodes = noteToEdit.getNodesSelected();
            for (final int node : nodesSelected) {
                if (!originalNodes.contains(node)) {
                    originalNodes.add(node);
                }
            }
            noteToEdit.setNodesSelected(originalNodes);
        }

        if (!transactionsSelected.isEmpty()) {
            if (noteToEdit.isGraphAttribute()) {
                noteToEdit.setGraphAttribute(false);
            }
            final List<Integer> originalTransactions = noteToEdit.getTransactionsSelected();
            for (final int transaction : transactionsSelected) {
                if (!originalTransactions.contains(transaction)) {
                    originalTransactions.add(transaction);
                }
            }
            noteToEdit.setTransactionsSelected(originalTransactions);
        }
    }

    /**
     * Remove what is currently selected on the graph from the note's selected
     * elements.
     */
    public void removeFromSelectedElements(final NotesViewEntry noteToEdit) {
        updateSelectedElements();

        if (!nodesSelected.isEmpty()) {
            final List<Integer> originalNodes = noteToEdit.getNodesSelected();
            for (final int node : nodesSelected) {
                if (originalNodes.contains(node)) {
                    final int index = originalNodes.indexOf(node);
                    originalNodes.remove(index);
                }
            }
            noteToEdit.setNodesSelected(originalNodes);
        }

        if (!transactionsSelected.isEmpty()) {
            final List<Integer> originalTransactions = noteToEdit.getTransactionsSelected();
            for (final int transaction : transactionsSelected) {
                if (originalTransactions.contains(transaction)) {
                    final int index = originalTransactions.indexOf(transaction);
                    originalTransactions.remove(index);
                }
            }
            noteToEdit.setTransactionsSelected(originalTransactions);
        }

        if (noteToEdit.getNodesSelected().isEmpty() && noteToEdit.getTransactionsSelected().isEmpty()) {
            noteToEdit.setGraphAttribute(true);
        }
    }

    /**
     * Updates the tags filters array with what tags are currently available.
     */
    public void updateTagsFiltersAvailable() {
        notesViewEntries.forEach(entry -> {
            if (!entry.isUserCreated()) {
                final List<String> tags = entry.getTags();
                for (final String tag : tags) {
                    if (!tagsUpdater.contains(tag)) {
                        tagsUpdater.add(tag);
                    }
                }
            }
        });

        tagsFiltersList = FXCollections.observableArrayList(tagsUpdater);
        Platform.runLater(() -> {
            autoFilterCheckComboBox.getItems().clear();
            autoFilterCheckComboBox.getItems().addAll(tagsFiltersList);
        });
    }

    /**
     * Updates what tags filters are currently selected.
     *
     * @param selectedTagsFilters
     */
    public void updateSelectedTagsCombo(final List<String> selectedTagsFilters) {
        this.tagsSelectedFiltersList.clear();
        selectedTagsFilters.forEach(filter -> this.tagsSelectedFiltersList.add(filter));
        updateTagFilters();
    }

    /**
     * Updates UI with what tags filters are selected.
     */
    public void updateTagFilters() {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Not processing on the JavaFX Application Thread");
        }

        isAutoSelectedFiltersUpdating = true;

        autoFilterCheckComboBox.getCheckModel().clearChecks();
        tagsSelectedFiltersList.forEach(filter -> autoFilterCheckComboBox.getCheckModel().check(filter));

        isAutoSelectedFiltersUpdating = false;
    }

    /**
     * Decides what Auto Notes are shown depending on what filters are selected.
     *
     * @param entry
     * @return boolean
     */
    public boolean updateAutoNotesDisplayed(final NotesViewEntry entry) {
        if (tagsSelectedFiltersList.isEmpty()) {
            return true;
        }
        for (final String filter : tagsSelectedFiltersList) {
            if (entry.getTags().contains(filter)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns an unmodifiable view backed by tagsSelectedFiltersList.
     *
     * @return Unmodifiable view backed by tagsSelectedFiltersList.
     */
    protected List<String> getTagsFilters() {
        return Collections.unmodifiableList(tagsSelectedFiltersList);
    }

    public Button getCreateNewNoteButton() {
        return createNewNoteButton;
    }

}
