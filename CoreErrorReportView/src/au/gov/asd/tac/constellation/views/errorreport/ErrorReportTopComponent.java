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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
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
    private List<ErrorReportEntry> hiddenErrors = new ArrayList<>();
    static VBox sessionErrorsBox = new VBox();
    private MenuButton popupControl;
    private final CheckBox severePopCheckBox = new CheckBox("Allow SEVERE Popups");
    private final CheckBox severeRepCheckBox = new CheckBox("Display SEVERE Reports");
    private final CheckBox warningPopCheckBox = new CheckBox("Allow WARNING Popups");
    private final CheckBox warningRepCheckBox = new CheckBox("Display WARNING Reports");
    private final CheckBox infoPopCheckBox = new CheckBox("Allow INFO Popups");
    private final CheckBox infoRepCheckBox = new CheckBox("Display INFO Reports");
    private final CheckBox finePopCheckBox = new CheckBox("Allow FINE Popups");
    private final CheckBox fineRepCheckBox = new CheckBox("Display FINE Reports");

    private final FlowPane severePopupAllowed = new FlowPane();
    private final FlowPane warnPopupAllowed = new FlowPane();
    private final FlowPane infoPopupAllowed = new FlowPane();
    private final FlowPane finePopupAllowed = new FlowPane();                
    private final FlowPane severeReportFilter = new FlowPane();
    private final FlowPane warnReportFilter = new FlowPane();
    private final FlowPane infoReportFilter = new FlowPane();
    private final FlowPane fineReportFilter = new FlowPane();

    private Date filterUpdateDate = new Date();
    private int popupMode = 2;
    private final ArrayList<String> popupFilters = new ArrayList<>();
    private boolean errorReportRunning = true;
    private boolean waitForGracePeriod = false;

    private Date flashActivatedDate = null;
    private boolean iconFlashing = false;
    private BorderPane errorBPane = null;

    private AtomicBoolean updateInProgress = new AtomicBoolean(false);

    private Timer refreshTimer = null;
    private Date latestRetrievalDate = null;
    private Date previousRetrievalDate = null;

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
                        boolean gracePeriodRefresh = false;
                        final Date currentDate = new Date();
                        if (waitForGracePeriod) {
                            final Date resumptionDate = ErrorReportDialogManager.getInstance().getGracePeriodResumptionDate();
                            if (currentDate.after(resumptionDate)) {
                                waitForGracePeriod = false;
                                gracePeriodRefresh = true;
                            }
                        }
                        if (errorReportRunning && updateInProgress != null && !updateInProgress.get()) {
                            if (gracePeriodRefresh || latestRetrievalDate == null || latestRetrievalDate.before(ErrorReportSessionData.lastUpdate) || latestRetrievalDate.before(filterUpdateDate) || ErrorReportSessionData.screenUpdateRequested) {
                                previousRetrievalDate = latestRetrievalDate == null ? null : new Date(latestRetrievalDate.getTime());
                                latestRetrievalDate = new Date();
                                boolean flashRequired = true;
                                if (ErrorReportDialogManager.getInstance().getLatestPopupDismissDate() != null && ErrorReportDialogManager.getInstance().getLatestPopupDismissDate().after(ErrorReportSessionData.lastUpdate)) {
                                    flashRequired = false;
                                }
                                updateFilterData();
                                ErrorReportDialogManager.getInstance().updatePopupSettings(getPopupControlValue(), popupFilters);
                                Date topEntryPrevDate = null;
                                if (!sessionErrors.isEmpty()) {
                                    topEntryPrevDate = sessionErrors.get(0).getLastDate();
                                }
                                updateSessionErrorsBox(-1);
                                ErrorReportSessionData.screenUpdateRequested = false;
                                if (!sessionErrors.isEmpty()) {
                                    if (topEntryPrevDate != null && !sessionErrors.get(0).getLastDate().after(topEntryPrevDate)) {
                                        flashRequired = false;
                                    }
                                }
                                flashErrorIcon(flashRequired);
                            }
                        }
                    }
                });
            }
        };

        refreshTimer = new Timer();
        refreshTimer.schedule(refreshAction, 840, 480);
    }

    @Override
    protected String createStyle() {
        return "resources/error-report.css";
    }

    @Override
    protected BorderPane createContent() {
        final BorderPane outerPane = new BorderPane();
        final VBox componentBox = new VBox();
        
        final VBox settingsBox = new VBox();
        final HBox popupSettings = new HBox();
        
        popupSettings.getChildren().addAll(severePopupAllowed, warnPopupAllowed, infoPopupAllowed, finePopupAllowed);
        settingsBox.getChildren().addAll(popupSettings, severeReportFilter, warnReportFilter, infoReportFilter, fineReportFilter);
        popupSettings.setSpacing(1);
        settingsBox.setSpacing(1);
        
        severePopupAllowed.setPrefSize(5, 5);
        severePopupAllowed.setPadding(new Insets(0));
        warnPopupAllowed.setPrefSize(5, 5);
        warnPopupAllowed.setPadding(new Insets(0));
        infoPopupAllowed.setPrefSize(5, 5);
        infoPopupAllowed.setPadding(new Insets(0));
        finePopupAllowed.setPrefSize(5, 5);
        finePopupAllowed.setPadding(new Insets(0));
        
        severeReportFilter.setPrefSize(20, 3);
        severeReportFilter.setPadding(new Insets(0));
        warnReportFilter.setPrefSize(20, 3);
        warnReportFilter.setPadding(new Insets(0));
        infoReportFilter.setPrefSize(20, 3);
        infoReportFilter.setPadding(new Insets(0));
        fineReportFilter.setPrefSize(20, 3);
        fineReportFilter.setPadding(new Insets(0));

        settingsBox.setPrefSize(23, 21);
        settingsBox.setPadding(new Insets(1,0,0,0));
        settingsBox.addEventFilter(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
            public void handle(final MouseEvent mouseEvent) {
                flashErrorIcon(false);
                mouseEvent.consume();
            }
        });
        
        severePopCheckBox.setSelected(true);
        severePopCheckBox.setOnAction((final ActionEvent event) -> updateSettings());
        severePopCheckBox.setPadding(new Insets(0, 0, 0, 0));
        warningPopCheckBox.setSelected(true);
        warningPopCheckBox.setOnAction((final ActionEvent event) -> updateSettings());
        warningPopCheckBox.setPadding(new Insets(0, 0, 0, 0));        
        infoPopCheckBox.setSelected(false);
        infoPopCheckBox.setOnAction((final ActionEvent event) -> updateSettings());
        infoPopCheckBox.setPadding(new Insets(0, 0, 0, 0));
        finePopCheckBox.setSelected(false);
        finePopCheckBox.setOnAction((final ActionEvent event) -> updateSettings());
        finePopCheckBox.setPadding(new Insets(0, 0, 8, 0));
        
        severeRepCheckBox.setSelected(true);
        severeRepCheckBox.setOnAction((final ActionEvent event) -> {filterUpdateDate = new Date(); updateSettings();});
        severeRepCheckBox.setPadding(new Insets(0, 0, 0, 0));
        warningRepCheckBox.setSelected(true);
        warningRepCheckBox.setOnAction((final ActionEvent event) -> {filterUpdateDate = new Date(); updateSettings();});
        warningRepCheckBox.setPadding(new Insets(0, 0, 0, 0));
        infoRepCheckBox.setSelected(true);
        infoRepCheckBox.setOnAction((final ActionEvent event) -> {filterUpdateDate = new Date(); updateSettings();});
        infoRepCheckBox.setPadding(new Insets(0, 0, 0, 0));        
        fineRepCheckBox.setSelected(true);
        fineRepCheckBox.setOnAction((final ActionEvent event) -> {filterUpdateDate = new Date(); updateSettings();});
        fineRepCheckBox.setPadding(new Insets(0, 0, 2, 0));

        updateSettings();        
        
        MenuButton filterControl = new MenuButton("Report Settings ");
        CustomMenuItem severityItem = new CustomMenuItem(severePopCheckBox);
        severityItem.setHideOnClick(false);
        filterControl.getItems().add(severityItem);
        severityItem = new CustomMenuItem(warningPopCheckBox);
        severityItem.setHideOnClick(false);
        filterControl.getItems().add(severityItem);
        severityItem = new CustomMenuItem(infoPopCheckBox);
        severityItem.setHideOnClick(false);
        filterControl.getItems().add(severityItem);
        severityItem = new CustomMenuItem(finePopCheckBox);
        severityItem.setHideOnClick(false);
        filterControl.getItems().add(severityItem);
                
        severityItem = new CustomMenuItem(severeRepCheckBox);
        severityItem.setHideOnClick(false);
        filterControl.getItems().add(severityItem);
        severityItem = new CustomMenuItem(warningRepCheckBox);
        severityItem.setHideOnClick(false);
        filterControl.getItems().add(severityItem);
        severityItem = new CustomMenuItem(infoRepCheckBox);
        severityItem.setHideOnClick(false);
        filterControl.getItems().add(severityItem);
        severityItem = new CustomMenuItem(fineRepCheckBox);
        severityItem.setHideOnClick(false);
        filterControl.getItems().add(severityItem);

        ToggleGroup popupFrequency = new ToggleGroup();
        
        final RadioMenuItem neverItem = new RadioMenuItem("0 : Never Show Popups");
        neverItem.setOnAction((final ActionEvent event) -> {popupMode = 0; popupControl.setText("Popup Mode : 0 ");} );
        neverItem.setToggleGroup(popupFrequency);
        final RadioMenuItem oneItem = new RadioMenuItem("1 : Show one popup only");
        oneItem.setOnAction((final ActionEvent event) -> {popupMode = 1; popupControl.setText("Popup Mode : 1 ");} );
        oneItem.setToggleGroup(popupFrequency);
        final RadioMenuItem oneRedispItem = new RadioMenuItem("2 : Show one popup, redisplayable");
        oneRedispItem.setOnAction((final ActionEvent event) -> {popupMode = 2; popupControl.setText("Popup Mode : 2 ");} );
        oneRedispItem.setToggleGroup(popupFrequency);
        final RadioMenuItem multiItem = new RadioMenuItem("3 : Show one popup per source (max 5) ");
        multiItem.setOnAction((final ActionEvent event) -> {popupMode = 3; popupControl.setText("Popup Mode : 3 ");} );
        multiItem.setToggleGroup(popupFrequency);
        final RadioMenuItem multiRedispItem = new RadioMenuItem("4 : Show one per source, redisplayable");
        multiRedispItem.setOnAction((final ActionEvent event) -> {popupMode = 4; popupControl.setText("Popup Mode : 4 ");} );
        multiRedispItem.setToggleGroup(popupFrequency);
        oneRedispItem.setSelected(true);
        popupControl = new MenuButton("Popup Mode : 2 ");
        popupControl.getItems().add(neverItem);
        popupControl.getItems().add(oneItem);
        popupControl.getItems().add(oneRedispItem);
        popupControl.getItems().add(multiItem);
        popupControl.getItems().add(multiRedispItem);
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
        controlToolbar.getItems().addAll(settingsBox, filterControl, popupControl, shrinkButton, expandButton, clearButton);
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

    /**
     * This view will control popups while it is open
     */
    @Override
    protected void handleComponentOpened() {
        errorReportRunning = true;
        ErrorReportDialogManager.getInstance().setErrorReportRunning(errorReportRunning);
    }

    /**
     * Return popup control to the ErrorReportDialogManager when this view is closed
     */
    @Override
    protected void handleComponentClosed() {
        errorReportRunning = false;
        updateFilterData();
        ErrorReportDialogManager.getInstance().updatePopupSettings(getPopupControlValue(), popupFilters);
        ErrorReportDialogManager.getInstance().setErrorReportRunning(errorReportRunning);
    }
    
    private void updateSettings(){
        final String severeFill = "#c02020";
        final String warningFill = "#b86820";
        final String infoFill = "#a2a200";
        final String fineFill = "#009090";
        final String severeBorder = "#d03030";
        final String warningBorder = "#c87830";
        final String infoBorder = "#b2b200";
        final String fineBorder = "#009c9c";
        
        final String severeReportInner = severeRepCheckBox.isSelected() ? severeFill : "black";
        final String warningReportInner = warningRepCheckBox.isSelected() ? warningFill : "black";
        final String infoReportInner = infoRepCheckBox.isSelected() ? infoFill : "black";
        final String fineReportInner = fineRepCheckBox.isSelected() ? fineFill : "black";
        final String severePopupInner = severePopCheckBox.isSelected() ? severeFill : "black";
        final String warningPopupInner = warningPopCheckBox.isSelected() ? warningFill : "black";
        final String infoPopupInner = infoPopCheckBox.isSelected() ? infoFill : "black";
        final String finePopupInner = finePopCheckBox.isSelected() ? fineFill : "black";
        
        updateSettingsIcon(severeReportFilter, severeReportInner, severeRepCheckBox.isSelected() ? severeBorder : severeFill);
        updateSettingsIcon(warnReportFilter, warningReportInner, warningRepCheckBox.isSelected() ? warningBorder : warningFill);
        updateSettingsIcon(infoReportFilter, infoReportInner, infoRepCheckBox.isSelected() ? infoBorder : infoFill);
        updateSettingsIcon(fineReportFilter, fineReportInner, fineRepCheckBox.isSelected() ? fineBorder : fineFill);
        updateSettingsIcon(severePopupAllowed, severePopupInner, severePopCheckBox.isSelected() ? severeBorder : severeFill);
        updateSettingsIcon(warnPopupAllowed, warningPopupInner, warningPopCheckBox.isSelected() ? warningBorder : warningFill);
        updateSettingsIcon(infoPopupAllowed, infoPopupInner, infoPopCheckBox.isSelected() ? infoBorder : infoFill);
        updateSettingsIcon(finePopupAllowed, finePopupInner, finePopCheckBox.isSelected() ? fineBorder : fineFill);      
        
        updateFilterData();
    }
    
    public void updateFilterData(){
        popupFilters.clear();
        if (severePopCheckBox.isSelected()) {
            popupFilters.add("SEVERE");
        }
        if (warningPopCheckBox.isSelected()) {
            popupFilters.add("WARNING");
        }
        if (infoPopCheckBox.isSelected()) {
            popupFilters.add("INFO");
        }
        if (finePopCheckBox.isSelected()) {
            popupFilters.add("FINE");
        }
    }
    
    private void updateSettingsIcon(final FlowPane settingsPane, final String innerShade, final String borderShade){
        settingsPane.setStyle("-fx-background-color: " + innerShade + "; -fx-border-color: " + borderShade + ";");
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
        if (severeRepCheckBox.isSelected() || severePopCheckBox.isSelected()) {
            activeFilters.add("SEVERE");
        }
        if (warningRepCheckBox.isSelected() || warningPopCheckBox.isSelected()) {
            activeFilters.add("WARNING");
        }
        if (infoRepCheckBox.isSelected() || infoPopCheckBox.isSelected()) {
            activeFilters.add("INFO");
        }
        if (fineRepCheckBox.isSelected() || finePopCheckBox.isSelected()) {
            activeFilters.add("FINE");
        }
        final ArrayList<ErrorReportEntry> combinedErrors = ErrorReportSessionData.getInstance().refreshDisplayedErrors(activeFilters);
        sessionErrors.clear();
        hiddenErrors.clear();
        for (ErrorReportEntry entry : combinedErrors) {
            switch (entry.getErrorLevel().getName()) {
                case "SEVERE":  if (severeRepCheckBox.isSelected()) {
                                    sessionErrors.add(entry);
                                } else {
                                    hiddenErrors.add(entry);
                                }
                                break;
                case "WARNING":  if (warningRepCheckBox.isSelected()) {
                                    sessionErrors.add(entry);
                                } else {
                                    hiddenErrors.add(entry);
                                }
                                break;
                case "INFO":  if (infoRepCheckBox.isSelected()) {
                                    sessionErrors.add(entry);
                                } else {
                                    hiddenErrors.add(entry);
                                }
                                break;
                case "FINE":  if (fineRepCheckBox.isSelected()) {
                                    sessionErrors.add(entry);
                                } else {
                                    hiddenErrors.add(entry);
                                }
                                break;
                default:    break;                                    
            }
        }
    }

    public void updateSessionErrorsBox(final int insertPos) {
        // get current snapshot of errors
        boolean sessionUpdated = false;
        while (!sessionUpdated) {
            if (updateInProgress.compareAndSet(false, true)) {
                refreshSessionErrors();
                final Date nextFilterDate = new Date();
                final int errCount = sessionErrors.size();
                // rebuild                
                sessionErrorsBox.getChildren().clear();
                for (int i = 0; i < errCount; i++) {
                    boolean allowPopupDisplay = false;
                    if (errorReportRunning && (("SEVERE".equals(sessionErrors.get(i).getErrorLevel().getName()) && severePopCheckBox.isSelected())
                            || ("WARNING".equals(sessionErrors.get(i).getErrorLevel().getName()) && warningPopCheckBox.isSelected())
                            || ("INFO".equals(sessionErrors.get(i).getErrorLevel().getName()) && infoPopCheckBox.isSelected())
                            || ("FINE".equals(sessionErrors.get(i).getErrorLevel().getName()) && finePopCheckBox.isSelected()))) {
                        allowPopupDisplay = true;
                    }
                    sessionErrorsBox.getChildren().add(generateErrorReportTitledPane(sessionErrors.get(i)));
                    if (allowPopupDisplay && sessionErrors.get(i).getLastDate().after(filterUpdateDate)) {
                        ErrorReportDialogManager.getInstance().showErrorDialog(sessionErrors.get(i));
                    }
                }
                
                // check if popup needed on hidden entries ... these entries can only exist with the allow popup checkbox ticked
                if (errorReportRunning) {
                    for (final ErrorReportEntry entry : hiddenErrors) {
                        if (entry.getLastDate().after(filterUpdateDate)) {
                            ErrorReportDialogManager.getInstance().showErrorDialog(entry);
                        }
                    }
                }
                final Date resumptionDate = ErrorReportDialogManager.getInstance().getGracePeriodResumptionDate();
                if (resumptionDate == null || nextFilterDate.after(resumptionDate)) {
                    filterUpdateDate = new Date(nextFilterDate.getTime());
                } else {
                    waitForGracePeriod = true;
                }
                sessionUpdated = true;
                updateInProgress.set(false);
            } else {
                try {
                    // session is being updated on another thread
                    Thread.sleep(360);
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
     * enable or disable flashing of the alert icon, as well as flashing of the
     * tab label
     *
     * @param enabled
     */
    public void flashErrorIcon(final boolean enabled) {
        if (!enabled) {
            iconFlashing = false;
            if (waitForGracePeriod) {
                filterUpdateDate = new Date();
                waitForGracePeriod = false;
            }
            cancelRequestAttention();
            return;
        }
        if (sessionErrorsBox.getChildren().isEmpty()) {
            return;
        }
        if (!iconFlashing) {
            iconFlashing = true;
            flashActivatedDate = previousRetrievalDate == null ? new Date() : new Date(previousRetrievalDate.getTime());
            try {
                final Image alertIcon = ImageIO.read(ErrorReportTopComponent.class.getResourceAsStream("resources/error-report-alert.png"));
                final Image defaultIcon = ImageIO.read(ErrorReportTopComponent.class.getResourceAsStream("resources/error-report-default.png"));
                final Thread flasher = new Thread(() -> {
                    final ArrayList<String> errorReportLevels = new ArrayList<>();
                    final ArrayList<String> errorPopupLevels = new ArrayList<>();
                    requestAttention(false);
                    while (isIconFlashing()) {
                        errorPopupLevels.clear();
                        errorPopupLevels.addAll(ErrorReportDialogManager.getInstance().getActivePopupErrorLevels());
                        errorReportLevels.clear();
                        errorReportLevels.addAll(getErrorLevelList(true));
                        try {
                            SwingUtilities.invokeAndWait(() -> {
                                setIcon(alertIcon);
                                updateSettings();
                                if (errorReportLevels.contains("SEVERE")) {
                                    updateSettingsIcon(severeReportFilter, "#ef3030", "#ff5858");                                     
                                }
                                if (errorReportLevels.contains("WARNING")) {
                                    updateSettingsIcon(warnReportFilter, "#e88830", "#ff9848");
                                }
                                if (errorReportLevels.contains("INFO")) {
                                    updateSettingsIcon(infoReportFilter, "#dada40", "#eaea58");
                                }
                                if (errorReportLevels.contains("FINE")) {
                                    updateSettingsIcon(fineReportFilter, "#40d0d0", "#58e0e0");
                                }
                                
                                if (errorPopupLevels.contains("SEVERE")) {
                                    updateSettingsIcon(severePopupAllowed, "#ef3030", "#ff5858");                                        
                                }
                                if (errorPopupLevels.contains("WARNING")) {
                                    updateSettingsIcon(warnPopupAllowed, "#e88830", "#ff9848");
                                }
                                if (errorPopupLevels.contains("INFO")) {
                                    updateSettingsIcon(infoPopupAllowed, "#dada40", "#eaea58");
                                }
                                if (errorPopupLevels.contains("FINE")) {
                                    updateSettingsIcon(finePopupAllowed, "#40d0d0", "#58e0e0");
                                }
                            });
                            Thread.sleep(750);
                            SwingUtilities.invokeAndWait(() -> {
                                setIcon(defaultIcon);
                                if (errorReportLevels.contains("SEVERE")) {
                                    updateSettingsIcon(severeReportFilter, "black", "black");                                     
                                }
                                if (errorReportLevels.contains("WARNING")) {
                                    updateSettingsIcon(warnReportFilter, "black", "black");
                                }
                                if (errorReportLevels.contains("INFO")) {
                                    updateSettingsIcon(infoReportFilter, "black", "black");
                                }
                                if (errorReportLevels.contains("FINE")) {
                                    updateSettingsIcon(fineReportFilter, "black", "black");
                                }
                                
                                if (errorPopupLevels.contains("SEVERE")) {
                                    updateSettingsIcon(severePopupAllowed, "black", "black");
                                }
                                if (errorPopupLevels.contains("WARNING")) {
                                    updateSettingsIcon(warnPopupAllowed, "black", "black");
                                }
                                if (errorPopupLevels.contains("INFO")) {
                                    updateSettingsIcon(infoPopupAllowed, "black", "black");
                                }
                                if (errorPopupLevels.contains("FINE")) {
                                    updateSettingsIcon(finePopupAllowed, "black", "black");
                                }
                            });
                            Thread.sleep(650);
                        } catch (final InvocationTargetException | InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    updateSettings();
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
        return popupMode;
    }

    private ArrayList<String> getErrorLevelList(final boolean unAcknowledgedEntriesOnly) {
        final ArrayList<String> resultList = new ArrayList<>();
        for (ErrorReportEntry entry : sessionErrors) {
            if (!resultList.contains(entry.getErrorLevel().getName())) {
                if (unAcknowledgedEntriesOnly && entry.getLastDate().after(flashActivatedDate)) {
                    resultList.add(entry.getErrorLevel().getName());
                } else if (!unAcknowledgedEntriesOnly) {
                    resultList.add(entry.getErrorLevel().getName());
                }
            }
        }
        return resultList;
    }
    
    /**
     * Generate the TitledPane object containing the details for the error
     * report entry.
     *
     * @param entry
     * @return
     */
    public TitledPane generateErrorReportTitledPane(final ErrorReportEntry entry) {
        final TitledPane ttlPane = new TitledPane();
        String backgroundColour = "#4a0000";
        String areaBackgroundColour = "radial-gradient(radius 100%, #4a0000 0%, #140000 100%)";
        int redBase = 0, redIncrement = 0;
        int greenBase = 0, greenIncrement = 0;
        int blueBase = 0, blueIncrement = 0;

        String alertColour = "#a0a0a0";
        if (entry.getErrorLevel() == Level.SEVERE) {
            alertColour = "#d87070";
            redBase = 100;
            redIncrement = 16;
            greenBase = 20;
            greenIncrement = 7;
            blueBase = 20;
            blueIncrement = 7;
        } else if (entry.getErrorLevel() == Level.WARNING) {
            alertColour = "#c08c60";
            redBase = 95;
            redIncrement = 15;
            greenBase = 50;
            greenIncrement = 10;
            blueBase = 12;
            blueIncrement = 5;
        } else if (entry.getErrorLevel() == Level.INFO) {
            alertColour = "#a8a848";
            redBase = 62;
            redIncrement = 13;
            greenBase = 62;
            greenIncrement = 13;
            blueBase = 8;
            blueIncrement = 4;
        } else if (entry.getErrorLevel() == Level.FINE) {
            alertColour = "#42a4a4";
            redBase = 8;
            redIncrement = 4;
            greenBase = 60;
            greenIncrement = 11;
            blueBase = 60;
            blueIncrement = 11;
        }

        int intensityFactor = 1;
        if (entry.getOccurrences() > 999) {
            intensityFactor = 5;
            backgroundColour = "#7c0000";
            areaBackgroundColour = "radial-gradient(radius 100%, #7c0000 0%, #240000 100%)";
        } else if (entry.getOccurrences() > 99) {
            intensityFactor = 4;
            backgroundColour = "#6e0000";
            areaBackgroundColour = "radial-gradient(radius 100%, #6e0000 0%, #200000 100%)";
        } else if (entry.getOccurrences() > 9) {
            intensityFactor = 3;
            backgroundColour = "#600000";
            areaBackgroundColour = "radial-gradient(radius 100%, #600000 0%, #1c0000 100%)";
        } else if (entry.getOccurrences() > 1) {
            intensityFactor = 2;
            backgroundColour = "#540000";
            areaBackgroundColour = "radial-gradient(radius 100%, #540000 0%, #180000 100%)";
        }

        String severityColour = "rgb(" + (redBase + intensityFactor * redIncrement) + ","
                + (greenBase + intensityFactor * greenIncrement) + ","
                + (blueBase + intensityFactor * blueIncrement) + ")";

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
        blockPopupsButton.setStyle("-fx-background-color: linear-gradient( " + severityColour + " 1% , " + backgroundColour + " 30%, " + backgroundColour + " 70%, " + severityColour + " 99% ); -fx-border-color: #404040");
        blockPopupsButton.setGraphic(entry.isBlockRepeatedPopups() ? shieldImageHighlight : visibleImageHighlight);
        blockPopupsButton.setTooltip(entry.isBlockRepeatedPopups() ? new Tooltip("Popups Blocked.\nRight click to review exception") : new Tooltip("Popups Allowed.\nRight click to review exception"));
        blockPopupsButton.setPadding(new Insets(3, 2, 1, 2));
        blockPopupsButton.setOnAction((final ActionEvent event) -> {
            entry.setBlockRepeatedPopups(!entry.isBlockRepeatedPopups());
            blockPopupsButton.setGraphic(entry.isBlockRepeatedPopups() ? shieldImageHighlight : visibleImageHighlight);
            blockPopupsButton.setTooltip(entry.isBlockRepeatedPopups() ? new Tooltip("Popups Blocked.\nRight click to review exception") : new Tooltip("Popups Allowed.\nRight click to review exception"));
        });
        ContextMenu contextMenu = new ContextMenu();
        MenuItem redisplay = new MenuItem("Redisplay Exception Dialog");
        redisplay.setOnAction((final ActionEvent event) -> {
            ErrorReportDialogManager.getInstance().showDialog(entry, true);
        });
        contextMenu.getItems().add(redisplay);
        blockPopupsButton.setContextMenu(contextMenu);
        
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

        hBoxLeft.setStyle("-fx-background-color: linear-gradient( " + severityColour + " 1% , " + backgroundColour + " 30%, " + backgroundColour + " 70%, " + severityColour + " 99% );");
        hBoxTitle.setStyle("-fx-background-color: linear-gradient( " + severityColour + " 1% , " + backgroundColour + " 30%, " + backgroundColour + " 70%, " + severityColour + " 99% );");
        hBoxRight.setStyle("-fx-background-color: linear-gradient( " + severityColour + " 1% , " + backgroundColour + " 30%, " + backgroundColour + " 70%, " + severityColour + " 99% );");
        counterLabel.setStyle(" -fx-background-color: black; -fx-text-background-color: cyan; -fx-text-fill: " + alertColour + " ; -fx-font-weight: bold; -fx-border-color: " + severityColour);
        counterLabel.setTooltip(new Tooltip("Repeated Occurrences"));
        counterLabel.setPadding(new Insets(1, 0, 1, 0));
        timeLabel.setPadding(new Insets(2, 0, 0, 0));
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
