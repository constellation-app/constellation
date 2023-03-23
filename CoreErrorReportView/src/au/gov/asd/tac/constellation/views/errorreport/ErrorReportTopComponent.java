/*
 * Copyright 2010-2023 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.errorreport;

import au.gov.asd.tac.constellation.utilities.icon.CharacterIconProvider;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import javafx.scene.layout.BorderPane;
import au.gov.asd.tac.constellation.views.JavaFxTopComponent;
import java.awt.Color;
import java.awt.Image;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javafx.scene.input.MouseEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import org.controlsfx.control.CheckComboBox;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author OrionsGuardian
 */
/**
 * The ErrorReportTopComponent provides the UI for the entire error report.
 */
@TopComponent.Description(
        preferredID = "ErrorReportTopComponent",
        iconBase = "au/gov/asd/tac/constellation/views/errorreport/resources/error-report-default.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "rightSlidingSide",
        openAtStartup = true
)
@ActionID(
        category = "Window",
        id = "au.gov.asd.tac.constellation.views.errorreport.ErrorReportTopComponent"
)
@ActionReferences({
    @ActionReference(path = "Menu/Views", position = 475),
    @ActionReference(path = "Shortcuts", name = "CS-R")
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_ErrorReportAction",
        preferredID = "ErrorReportTopComponent"
)
@NbBundle.Messages({
    "CTL_ErrorReportAction=Error Report",
    "CTL_ErrorReportTopComponent=Error Report",
    "HINT_ErrorReportTopComponent=Error Report"
})

public class ErrorReportTopComponent extends JavaFxTopComponent<BorderPane> {

    private static final Logger LOGGER = Logger.getLogger(ErrorReportTopComponent.class.getName());
    
    private List<ErrorReportEntry> sessionErrors = new ArrayList<>();
    static VBox sessionErrorsBox = new VBox();
    private ComboBox<String> popupControl;
    private CheckComboBox<String> filterControl;
    private Date filterUpdateDate = new Date();

    private boolean iconFlashing = false;
    private BorderPane errorBPane = null;

    private AtomicBoolean updateInProgress = new AtomicBoolean(false);

    private Timer refreshTimer = null;
    private Date latestRetrievalDate = null;

    ErrorReportTopComponent() {
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 300, Short.MAX_VALUE)
        );
        setName(Bundle.CTL_ErrorReportTopComponent());
        setToolTipText(Bundle.HINT_ErrorReportTopComponent());
        initContent();
        final TimerTask refreshAction = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    public void run() {
                        if (updateInProgress != null && !updateInProgress.get()) {
                            if (latestRetrievalDate == null || latestRetrievalDate.before(ErrorReportSessionData.lastUpdate) || latestRetrievalDate.before(filterUpdateDate) || ErrorReportSessionData.screenUpdateRequested) {
                                latestRetrievalDate = new Date();
                                boolean flashRequired = true;
                                if (ErrorReportDialogManager.getInstance().getLatestPopupDismissDate() != null && ErrorReportDialogManager.getInstance().getLatestPopupDismissDate().after(ErrorReportSessionData.lastUpdate)) {
                                    flashRequired = false;
                                }
                                ErrorReportDialogManager.getInstance().updatePopupMode(getPopupControlValue());
                                updateSessionErrorsBox(-1);
                                ErrorReportSessionData.screenUpdateRequested = false;
                                flashErrorIcon(flashRequired);
                            }
                        }
                    }
                });
            }
        };

        refreshTimer = new Timer();
        refreshTimer.schedule(refreshAction, 4000, 400);
    }

    @Override
    protected String createStyle() {
        return "resources/error-report.css";
    }

    @Override
    protected BorderPane createContent() {
        final BorderPane outerPane = new BorderPane();
        final VBox componentBox = new VBox();

        final ObservableList<String> filterOptions = FXCollections.observableArrayList();
        filterOptions.add("SEVERE");
        filterOptions.add("WARNING");
        filterOptions.add("INFO");
        filterOptions.add("FINE");
        filterControl = new CheckComboBox<>(filterOptions);
        filterControl.getCheckModel().check(0);
        filterControl.setMaxWidth(200);

        filterControl.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
            public void onChanged(ListChangeListener.Change<? extends String> c) {
                 while(c.next()) {
                     // update the screen with latest selections
                 }
                 LOGGER.info("\n-------");
                 for (String filterEntry : filterControl.getCheckModel().getCheckedItems()) {
                    LOGGER.info(filterEntry);
                 }
                 filterUpdateDate = new Date();
                 LOGGER.info("\n=======");
            }
        });
                
        final ObservableList<String> popupOptions = FXCollections.observableArrayList();
        popupOptions.add("Never Show Popups");
        popupOptions.add("Show one popup only");
        popupOptions.add("Show one popup, redisplayable");
        popupOptions.add("Show one popup per source");
        popupOptions.add("Show one per source, redisplayable");
        popupControl = new ComboBox<>(popupOptions);
        popupControl.getSelectionModel().select(1);
        popupControl.setMaxWidth(200);
        
        final Button clearButton = new Button("Clear All Reports");
        clearButton.setTooltip(new Tooltip("Clear all current error reports"));
        clearButton.setOnAction((final ActionEvent event) -> {
            // remove all matching entries in the data class
            int errCount = sessionErrors.size();
            for (int i = 0; i < errCount; i++) {
                ErrorReportSessionData.getInstance().removeEntry(sessionErrors.get(i).getEntryId());
            }
            sessionErrors.clear();
            updateSessionErrorsBox(-1);
        });

        final ImageView expandButtonImage = new ImageView(CharacterIconProvider.CHAR_002B.buildImage(15, Color.WHITE));
        final Button expandButton = new Button("");
        expandButton.setGraphic(expandButtonImage);
        expandButton.setTooltip(new Tooltip("Expand All Error Reports"));
        expandButton.setOnAction((final ActionEvent event) -> {
            setReportsExpanded(true);
        });

        final ImageView shrinkButtonImage = new ImageView(CharacterIconProvider.CHAR_002D.buildImage(15, Color.WHITE));
        final Button shrinkButton = new Button("");
        shrinkButton.setGraphic(shrinkButtonImage);
        shrinkButton.setTooltip(new Tooltip("Shrink All Error Reports"));
        shrinkButton.setOnAction((final ActionEvent event) -> {
            setReportsExpanded(false);
        });

        final ToolBar controlToolbar = new ToolBar();
        controlToolbar.getItems().addAll(filterControl, popupControl, shrinkButton, expandButton, clearButton);
        final HBox toolboxContainer = new HBox();
        toolboxContainer.getChildren().add(controlToolbar);
        toolboxContainer.getChildren().add(new Label("  "));
        GridPane.setHgrow(controlToolbar, Priority.ALWAYS);

        componentBox.getChildren().add(toolboxContainer);

        errorBPane = new BorderPane();
        errorBPane.setStyle("-fx-text-fill: purple; -fx-text-background-color: blue; -fx-background-color: black;");
        sessionErrorsBox.setPadding(new Insets(0, 0, 0, 0));
        sessionErrorsBox.setSpacing(2);

        errorBPane.addEventFilter(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
            public void handle(final MouseEvent mouseEvent) {
                flashErrorIcon(false);
                mouseEvent.consume();
            }
        });

        errorBPane.setTop(sessionErrorsBox);
        componentBox.getChildren().add(errorBPane);
        outerPane.setTop(componentBox);

        return outerPane;
    }

    @Override
    protected ScrollPane.ScrollBarPolicy getVerticalScrollPolicy() {
        return ScrollPane.ScrollBarPolicy.ALWAYS;
    }

    public void setReportsExpanded(final boolean expandedMode) {
        final int errCount = sessionErrors.size();
        if (sessionErrorsBox.getChildren().size() > 0) {
            for (int i = 0; i < errCount; i++) {
                ((TitledPane) sessionErrorsBox.getChildren().get(i)).setExpanded(expandedMode);
            }
        }
    }

    public ErrorReportEntry findActiveEntryWithId(final double id) {
        for (final ErrorReportEntry activeEntry : sessionErrors) {
            if (activeEntry.getEntryId() == id) {
                return activeEntry;
            }
        }
        return null;
    }

    public void refreshSessionErrors() {
        // save expanded states
        final int errCount = sessionErrors.size();
        if (sessionErrorsBox.getChildren().size() > 0) {
            for (int i = 0; i < errCount; i++) {
                ErrorReportSessionData.getInstance().updateDisplayedEntryScreenSettings(
                        sessionErrors.get(i).getEntryId(), null, null, ((TitledPane) sessionErrorsBox.getChildren().get(i)).isExpanded());
            }
        }
        final ArrayList<String> activeFilters = new ArrayList<>();
        activeFilters.addAll(filterControl.getCheckModel().getCheckedItems());
        sessionErrors = ErrorReportSessionData.getInstance().refreshDisplayedErrors(activeFilters);
    }

    public void updateSessionErrorsBox(final int insertPos) {
        // get current snapshot of errors
        boolean sessionUpdated = false;
        while (!sessionUpdated) {
            if (updateInProgress.compareAndSet(false, true)) {
                refreshSessionErrors();
                final int errCount = sessionErrors.size();
                // rebuild                
                sessionErrorsBox.getChildren().clear();
                for (int i = 0; i < errCount; i++) {
                    final int errorLevelIndex = filterControl.getCheckModel().getItemIndex(sessionErrors.get(i).getErrorLevel().getName());
                    boolean allowDisplay = filterControl.getCheckModel().isChecked(errorLevelIndex);
                    sessionErrorsBox.getChildren().add(generateErrorReportTitledPane(sessionErrors.get(i)));
                    if (allowDisplay && sessionErrors.get(i).getLastDate().after(filterUpdateDate)) {
                        ErrorReportDialogManager.getInstance().showErrorDialog(sessionErrors.get(i));
                    }
                    LOGGER.info("\n ###### Displaying error with level = " + sessionErrors.get(i).getErrorLevel().getName());
                }
                sessionUpdated = true;
                updateInProgress.set(false);
            } else {
                try {
                    Thread.sleep(180);
                } catch (final InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    public boolean isIconFlashing() {
        return iconFlashing;
    }

    /**
     * enable or disable flashing of the alert icon, as well as flashing of the tab label
     * @param enabled 
     */
    public void flashErrorIcon(final boolean enabled) {
        if (!enabled) {
            iconFlashing = false;
            return;
        }
        if (sessionErrorsBox.getChildren().isEmpty()) {
            return;
        }
        if (!iconFlashing) {
            iconFlashing = true;
            try {
                final Image alertIcon = ImageIO.read(ErrorReportTopComponent.class.getResourceAsStream("resources/error-report-alert.png"));
                final Image defaultIcon = ImageIO.read(ErrorReportTopComponent.class.getResourceAsStream("resources/error-report-default.png"));
                final Thread flasher = new Thread(() -> {
                    int countDown = 3;
                    requestAttention(true);
                    while (isIconFlashing()) {
                        try {
                            countDown--;
                            if (countDown <= 0) {
                                countDown = 3;
                                requestAttention(true);
                            }
                            SwingUtilities.invokeAndWait(() -> {
                                setIcon(alertIcon);
                            });
                            Thread.sleep(750);
                            SwingUtilities.invokeAndWait(() -> {
                                setIcon(defaultIcon);
                            });
                            Thread.sleep(650);
                        } catch (final InvocationTargetException | InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
                flasher.start();
            } catch (final IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public void removeEntry(final int indexPosition) {
        sessionErrors.remove(indexPosition);
        updateSessionErrorsBox(-1);
    }

    public int getPopupControlValue() {
        return popupControl.getSelectionModel().getSelectedIndex();
    }

    /**
     * Generate the TitledPane object containing the details for the error report entry.
     * @param entry
     * @return 
     */
    public TitledPane generateErrorReportTitledPane(final ErrorReportEntry entry) {
        final TitledPane ttlPane = new TitledPane();
        String backgroundColour = "#4a0000";
        String areaBackgroundColour = "radial-gradient(radius 100%, #4a0000 0%, #140000 100%)";
        if (entry.getOccurrences() > 999) {
            backgroundColour = "#7c0000";
            areaBackgroundColour = "radial-gradient(radius 100%, #7c0000 0%, #240000 100%)";
        } else if (entry.getOccurrences() > 99) {
            backgroundColour = "#6e0000";
            areaBackgroundColour = "radial-gradient(radius 100%, #6e0000 0%, #200000 100%)";
        } else if (entry.getOccurrences() > 9) {
            backgroundColour = "#600000";
            areaBackgroundColour = "radial-gradient(radius 100%, #600000 0%, #1c0000 100%)";
        } else if (entry.getOccurrences() > 1) {
            backgroundColour = "#540000";
            areaBackgroundColour = "radial-gradient(radius 100%, #540000 0%, #180000 100%)";
        }

        String alertColour = "#a0a0a0";
        String severityColour = "#808080";
        if (entry.getErrorLevel() == Level.SEVERE) {
            alertColour = "#d87878";
            severityColour = "#a03030";
        } else if (entry.getErrorLevel() == Level.WARNING) {
            alertColour = "#c08860";
            severityColour = "#905828";
        } else if (entry.getErrorLevel() == Level.INFO) {
            alertColour = "#a8a848";
            severityColour = "#787820";
        } else if (entry.getErrorLevel() == Level.FINE) {
            alertColour = "#48b0b0";
            severityColour = "#207878";
        }
        
        final BorderPane bdrPane = new BorderPane();
        final VBox vBox = new VBox();
        vBox.setPadding(new Insets(1));

        final TextArea data = new TextArea(entry.getSummaryHeading() + "\n" + entry.getErrorData());
        data.setStyle("-fx-text-fill: #c0c0c0; -fx-background-color: " + backgroundColour + "; text-area-background: " + areaBackgroundColour + "; -fx-border-color: #505050; -fx-border-width: 2;"); //  + backgroundColour
        data.setEditable(false);
        data.setPadding(new Insets(2));
        data.setPrefRowCount(14);
        vBox.getChildren().add(data);

        ttlPane.setText("");
        ttlPane.setContent(vBox);
        ttlPane.setExpanded(entry.getExpanded());
        final HBox hBoxTitle = new HBox();
        final HBox hBoxLeft = new HBox();
        final HBox hBoxRight = new HBox();
        final Label timeLabel = new Label(entry.getTimeText());
        final Label label = new Label(entry.getTrimmedHeading(120));
        label.setTooltip(new Tooltip(label.getText()));
        final Label counterLabel = new Label(" " + Integer.toString(entry.getOccurrences()) + "x ");

        GridPane.setHgrow(label, Priority.ALWAYS);

        final ImageView shieldImageHighlight = new ImageView(UserInterfaceIconProvider.LOCK.buildImage(15, Color.ORANGE.darker()));
        final ImageView visibleImageHighlight = new ImageView(UserInterfaceIconProvider.UNLOCK.buildImage(15, Color.CYAN.darker()));
        final Button blockPopupsButton = new Button("");
        blockPopupsButton.setStyle("-fx-background-color: linear-gradient( " + severityColour + " 1% , " + backgroundColour + " 28%, " + backgroundColour + " 72%, " + severityColour + " 99% ); -fx-border-color: #404040");
        blockPopupsButton.setGraphic(entry.isBlockRepeatedPopups() ? shieldImageHighlight : visibleImageHighlight);
        blockPopupsButton.setTooltip(entry.isBlockRepeatedPopups() ? new Tooltip("Popups Blocked") : new Tooltip("Popups Allowed"));
        blockPopupsButton.setPadding(new Insets(3, 2, 1, 2));
        blockPopupsButton.setOnAction((final ActionEvent event) -> {
            entry.setBlockRepeatedPopups(!entry.isBlockRepeatedPopups());
            blockPopupsButton.setGraphic(entry.isBlockRepeatedPopups() ? shieldImageHighlight : visibleImageHighlight);
            blockPopupsButton.setTooltip(entry.isBlockRepeatedPopups() ? new Tooltip("Popups Blocked") : new Tooltip("Popups Allowed"));
        });

        final ImageView crossImageHighlight = new ImageView(UserInterfaceIconProvider.CROSS.buildImage(13, Color.LIGHT_GRAY));
        final Button dismissButton = new Button("");
        dismissButton.setStyle("-fx-background-color: #505050; -fx-border-color: #404040");
        dismissButton.setGraphic(crossImageHighlight);
        dismissButton.addEventFilter(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
            public void handle(final MouseEvent mouseEvent) {
                dismissButton.setStyle("-fx-background-color: #e02828; -fx-border-color: #c01c1c");
                mouseEvent.consume();
            }
        });
        dismissButton.addEventFilter(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
            public void handle(final MouseEvent mouseEvent) {
                dismissButton.setStyle("-fx-background-color: #505050; -fx-border-color: #404040");
                mouseEvent.consume();
            }
        });
        dismissButton.setPadding(new Insets(2));
        dismissButton.setOnAction((final ActionEvent event) -> {
            ErrorReportSessionData.getInstance().removeEntry(entry.getEntryId());
            updateSessionErrorsBox(-1);
        });

        label.setStyle("-fx-text-fill: #c0c0c0;");
        timeLabel.setStyle("-fx-text-fill: #c0c0c0;");
        
        hBoxLeft.setStyle("-fx-background-color: linear-gradient( " + severityColour + " 1% , " + backgroundColour + " 28%, " + backgroundColour + " 72%, " + severityColour + " 99% );");
        hBoxTitle.setStyle("-fx-background-color: linear-gradient( " + severityColour + " 1% , " + backgroundColour + " 28%, " + backgroundColour + " 72%, " + severityColour + " 99% );");
        hBoxRight.setStyle("-fx-background-color: linear-gradient( " + severityColour + " 1% , " + backgroundColour + " 28%, " + backgroundColour + " 72%, " + severityColour + " 99% );");
        counterLabel.setStyle(" -fx-background-color: black; -fx-text-background-color: cyan; -fx-text-fill: " + alertColour + " ; -fx-font-weight: bold; -fx-border-color: " + severityColour);
        counterLabel.setTooltip(new Tooltip("Repeated occurrences"));
        counterLabel.setPadding(new Insets(1,0,1,0));
        timeLabel.setPadding(new Insets(2,0,0,0));
        hBoxLeft.getChildren().add(timeLabel);
        hBoxLeft.getChildren().add(counterLabel);
        hBoxTitle.getChildren().add(label);
        hBoxLeft.setPadding(new Insets(2, 6, 1, 0));
        hBoxTitle.setPadding(new Insets(4, 6, 1, 0));
        hBoxRight.setPadding(new Insets(1, 1, 1, 6));
        hBoxRight.setSpacing(2);
        hBoxRight.getChildren().add(blockPopupsButton);
        hBoxRight.getChildren().add(dismissButton);

        bdrPane.setLeft(hBoxLeft);
        bdrPane.setCenter(hBoxTitle);
        bdrPane.setRight(hBoxRight);
        bdrPane.setPadding(new Insets(0, 0, 0, 0));
        bdrPane.setStyle("-fx-border-color:grey");

        bdrPane.prefWidthProperty().bind(scrollPane.widthProperty().subtract(48));

        ttlPane.setGraphic(bdrPane);
        ttlPane.setPadding(new Insets(0, 16, 0, 0));

        return ttlPane;
    }

}
