/*
 * Copyright 2010-2024 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import javafx.scene.layout.BorderPane;
import au.gov.asd.tac.constellation.views.JavaFxTopComponent;
import java.awt.Color;
import java.awt.Image;
import java.io.IOException;
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
import javafx.embed.swing.SwingFXUtils;
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
import javafx.scene.image.WritableImage;
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
    @ActionReference(path = "Menu/Help", position = 945)
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

    public enum SeverityCode {
        SEVERE("SEVERE"),
        WARNING("WARNING"),
        INFO("INFO"),
        FINE("FINE");
        private final String code;

        SeverityCode(final String severityCode) {
            code = severityCode;
        }

        public String getCode() {
            return code;
        }

        public static SeverityCode getSeverityCodeEntry(final String severityCode) {
            for (final SeverityCode sevCode : SeverityCode.values()) {
                if (severityCode.equals(sevCode.getCode())) {
                    return sevCode;
                }
            }
            return null;
        }
    }

    static final boolean DARK_MODE = JavafxStyleManager.isDarkTheme();
    private static final String ALLOW_POPUPS_FMT = "Allow %s Popups";
    private static final String DISPLAY_REPORTS_FMT = "Display %s Reports";

    private final CheckBox severePopCheckBox = new CheckBox(String.format(ALLOW_POPUPS_FMT, SeverityCode.SEVERE.getCode()));
    private final CheckBox severeRepCheckBox = new CheckBox(String.format(DISPLAY_REPORTS_FMT, SeverityCode.SEVERE.getCode()));
    private final CheckBox warningPopCheckBox = new CheckBox(String.format(ALLOW_POPUPS_FMT, SeverityCode.WARNING.getCode()));
    private final CheckBox warningRepCheckBox = new CheckBox(String.format(DISPLAY_REPORTS_FMT, SeverityCode.WARNING.getCode()));
    private final CheckBox infoPopCheckBox = new CheckBox(String.format(ALLOW_POPUPS_FMT, SeverityCode.INFO.getCode()));
    private final CheckBox infoRepCheckBox = new CheckBox(String.format(DISPLAY_REPORTS_FMT, SeverityCode.INFO.getCode()));
    private final CheckBox finePopCheckBox = new CheckBox(String.format(ALLOW_POPUPS_FMT, SeverityCode.FINE.getCode()));
    private final CheckBox fineRepCheckBox = new CheckBox(String.format(DISPLAY_REPORTS_FMT, SeverityCode.FINE.getCode()));

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
    private final List<String> popupFilters = new ArrayList<>();
    private boolean errorReportRunning = true;
    private boolean waitForGracePeriod = false;
    private static final String INACTIVE_BACKGROUND = DARK_MODE ? "black" : "white";

    private Date flashActivatedDate = null;
    private boolean iconFlashing = false;
    private boolean alertStateActive = true;

    private AtomicBoolean updateInProgress = new AtomicBoolean(false);

    private Image alertIcon = null;
    private Image defaultIcon = null;
    private Timer refreshTimer = null;
    private Timer alertTimer = null;
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
                Platform.runLater(() -> {
                    boolean gracePeriodRefresh = false;
                    final Date currentDate = new Date();
                    if (waitForGracePeriod) {
                        final Date resumptionDate = ErrorReportDialogManager.getInstance().getGracePeriodResumptionDate();
                        if (currentDate.after(resumptionDate)) {
                            waitForGracePeriod = false;
                            gracePeriodRefresh = true;
                        }
                    }
                    if (errorReportRunning && updateInProgress != null && !updateInProgress.get()
                            && (gracePeriodRefresh || latestRetrievalDate == null
                            || latestRetrievalDate.before(ErrorReportSessionData.getLastUpdate())
                            || latestRetrievalDate.before(filterUpdateDate)
                            || ErrorReportSessionData.isScreenUpdateRequested())) {

                        previousRetrievalDate = latestRetrievalDate == null ? null : new Date(latestRetrievalDate.getTime());
                        latestRetrievalDate = new Date();
                        boolean flashRequired = true;
                        if (ErrorReportDialogManager.getInstance().getLatestPopupDismissDate() != null && ErrorReportDialogManager.getInstance().getLatestPopupDismissDate().after(ErrorReportSessionData.getLastUpdate())) {
                            flashRequired = false;
                        }
                        updateFilterData();
                        ErrorReportDialogManager.getInstance().updatePopupSettings(getPopupControlValue(), popupFilters);
                        Date topEntryPrevDate = null;
                        if (!sessionErrors.isEmpty()) {
                            topEntryPrevDate = sessionErrors.get(0).getLastDate();
                        }
                        updateSessionErrorsBox(-1);
                        ErrorReportSessionData.requestScreenUpdate(false);
                        if (!sessionErrors.isEmpty() && !gracePeriodRefresh && topEntryPrevDate != null && !sessionErrors.get(0).getLastDate().after(topEntryPrevDate)) {
                            flashRequired = false;
                        }
                        flashErrorIcon(flashRequired);
                    }
                });
            }
        };

        refreshTimer = new Timer();
        refreshTimer.schedule(refreshAction, 840, 480);
    }

    @Override
    protected String createStyle() {
        return DARK_MODE
                ? "resources/error-report-dark.css"
                : "resources/error-report-light.css";
    }

    @Override
    protected BorderPane createContent() {
        final BorderPane outerPane = new BorderPane();
        outerPane.setId("outer-pane");
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
        settingsBox.setPadding(new Insets(4, 0, 0, 0));
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
        severeRepCheckBox.setOnAction((final ActionEvent event) -> {
            filterUpdateDate = new Date();
            updateSettings();
        });
        severeRepCheckBox.setPadding(new Insets(0, 0, 0, 0));
        warningRepCheckBox.setSelected(true);
        warningRepCheckBox.setOnAction((final ActionEvent event) -> {
            filterUpdateDate = new Date();
            updateSettings();
        });
        warningRepCheckBox.setPadding(new Insets(0, 0, 0, 0));
        infoRepCheckBox.setSelected(true);
        infoRepCheckBox.setOnAction((final ActionEvent event) -> {
            filterUpdateDate = new Date();
            updateSettings();
        });
        infoRepCheckBox.setPadding(new Insets(0, 0, 0, 0));
        fineRepCheckBox.setSelected(true);
        fineRepCheckBox.setOnAction((final ActionEvent event) -> {
            filterUpdateDate = new Date();
            updateSettings();
        });
        fineRepCheckBox.setPadding(new Insets(0, 0, 2, 0));

        updateSettings();

        final MenuButton filterControl = new MenuButton("Report Settings ");
        final CustomMenuItem severePopupItem = new CustomMenuItem(severePopCheckBox);
        severePopupItem.setHideOnClick(false);
        filterControl.getItems().add(severePopupItem);

        final CustomMenuItem warningPopupItem = new CustomMenuItem(warningPopCheckBox);
        warningPopupItem.setHideOnClick(false);
        filterControl.getItems().add(warningPopupItem);

        final CustomMenuItem infoPopupItem = new CustomMenuItem(infoPopCheckBox);
        infoPopupItem.setHideOnClick(false);
        filterControl.getItems().add(infoPopupItem);

        final CustomMenuItem finePopupItem = new CustomMenuItem(finePopCheckBox);
        finePopupItem.setHideOnClick(false);
        filterControl.getItems().add(finePopupItem);

        final CustomMenuItem severeReportItem = new CustomMenuItem(severeRepCheckBox);
        severeReportItem.setHideOnClick(false);
        filterControl.getItems().add(severeReportItem);

        final CustomMenuItem warningReportItem = new CustomMenuItem(warningRepCheckBox);
        warningReportItem.setHideOnClick(false);
        filterControl.getItems().add(warningReportItem);

        final CustomMenuItem infoReportItem = new CustomMenuItem(infoRepCheckBox);
        infoReportItem.setHideOnClick(false);
        filterControl.getItems().add(infoReportItem);

        final CustomMenuItem fineReportItem = new CustomMenuItem(fineRepCheckBox);
        fineReportItem.setHideOnClick(false);
        filterControl.getItems().add(fineReportItem);
        filterControl.setMinHeight(26);
        filterControl.setMaxHeight(26);
        
        final MenuButton popupControl = new MenuButton("Popup Mode : 2 ");
        final ToggleGroup popupFrequency = new ToggleGroup();

        final RadioMenuItem neverItem = new RadioMenuItem("0 : Never Show Popups");
        neverItem.setOnAction((final ActionEvent event) -> {
            popupMode = 0;
            popupControl.setText("Popup Mode : 0 ");
        });
        neverItem.setToggleGroup(popupFrequency);

        final RadioMenuItem oneItem = new RadioMenuItem("1 : Show one popup only");
        oneItem.setOnAction((final ActionEvent event) -> {
            popupMode = 1;
            popupControl.setText("Popup Mode : 1 ");
        });
        oneItem.setToggleGroup(popupFrequency);

        final RadioMenuItem oneRedispItem = new RadioMenuItem("2 : Show one popup, redisplayable");
        oneRedispItem.setOnAction((final ActionEvent event) -> {
            popupMode = 2;
            popupControl.setText("Popup Mode : 2 ");
        });
        oneRedispItem.setToggleGroup(popupFrequency);

        final RadioMenuItem multiItem = new RadioMenuItem("3 : Show one popup per source (max 5) ");
        multiItem.setOnAction((final ActionEvent event) -> {
            popupMode = 3;
            popupControl.setText("Popup Mode : 3 ");
        });
        multiItem.setToggleGroup(popupFrequency);

        final RadioMenuItem multiRedispItem = new RadioMenuItem("4 : Show one per source, redisplayable");
        multiRedispItem.setOnAction((final ActionEvent event) -> {
            popupMode = 4;
            popupControl.setText("Popup Mode : 4 ");
        });
        multiRedispItem.setToggleGroup(popupFrequency);

        oneRedispItem.setSelected(true);
        popupControl.getItems().add(neverItem);
        popupControl.getItems().add(oneItem);
        popupControl.getItems().add(oneRedispItem);
        popupControl.getItems().add(multiItem);
        popupControl.getItems().add(multiRedispItem);
        popupControl.setMaxWidth(200);
        popupControl.setMinHeight(26);
        popupControl.setMaxHeight(26);

        final Button clearButton = new Button("Clear All Reports");
        clearButton.setTooltip(new Tooltip("Clear all current error reports"));
        clearButton.setOnAction((final ActionEvent event) -> {
            // remove all matching entries in the data class
            for (final ErrorReportEntry entry : sessionErrors) {
                ErrorReportSessionData.getInstance().removeEntry(entry.getEntryId());
            }
            sessionErrors.clear();
            updateSessionErrorsBox(-1);
        });
        clearButton.setMinHeight(26);
        clearButton.setMaxHeight(26);

        final WritableImage maximizeImage = new WritableImage(22, 16);
        final WritableImage minimizeImage = new WritableImage(22, 16);
        try {
            SwingFXUtils.toFXImage(ImageIO.read(ErrorReportTopComponent.class.getResource("resources/maximize.png")), maximizeImage);
            SwingFXUtils.toFXImage(ImageIO.read(ErrorReportTopComponent.class.getResource("resources/minimize.png")), minimizeImage);
        } catch (final IOException ioex) {
            LOGGER.log(Level.SEVERE, "Error loading image file", ioex);
        }
        final ImageView maximizeButtonImage = new ImageView(maximizeImage);
        final ImageView minimizeButtonImage = new ImageView(minimizeImage);

        final Button maximizeButton = new Button("");
        maximizeButton.setGraphic(maximizeButtonImage);
        maximizeButton.setPadding(new Insets(1, 2, 1, 2));
        maximizeButton.setTooltip(new Tooltip("Maximize All Error Reports"));
        maximizeButton.setOnAction((final ActionEvent event) -> setReportsExpanded(true));
        maximizeButton.setMinHeight(26);
        maximizeButton.setMaxHeight(26);

        final Button minimizeButton = new Button("");
        minimizeButton.setGraphic(minimizeButtonImage);
        minimizeButton.setPadding(new Insets(1, 2, 1, 2));
        minimizeButton.setTooltip(new Tooltip("Minimize All Error Reports"));
        minimizeButton.setOnAction((final ActionEvent event) -> setReportsExpanded(false));
        minimizeButton.setMinHeight(26);
        minimizeButton.setMaxHeight(26);

        final ToolBar controlToolbar = new ToolBar();
        controlToolbar.getItems().addAll(settingsBox, filterControl, popupControl, minimizeButton, maximizeButton, clearButton);
        final HBox toolboxContainer = new HBox();
        toolboxContainer.getChildren().add(controlToolbar);
        toolboxContainer.getChildren().add(new Label("  "));
        GridPane.setHgrow(controlToolbar, Priority.ALWAYS);

        componentBox.getChildren().add(toolboxContainer);

        final BorderPane errorBPane = new BorderPane();
        errorBPane.setStyle("-fx-text-fill: purple; -fx-text-background-color: blue; -fx-background-color: " + (DARK_MODE ? "black" : "white") + ";");
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

    private void updateSettings() {
        final String severeFill = "#c02020";
        final String warningFill = "#b86820";
        final String infoFill = "#a2a200";
        final String fineFill = "#009090";
        final String severeBorder = "#d03030";
        final String warningBorder = "#c87830";
        final String infoBorder = "#b2b200";
        final String fineBorder = "#009c9c";

        final String severeReportInner = severeRepCheckBox.isSelected() ? severeFill : INACTIVE_BACKGROUND;
        final String warningReportInner = warningRepCheckBox.isSelected() ? warningFill : INACTIVE_BACKGROUND;
        final String infoReportInner = infoRepCheckBox.isSelected() ? infoFill : INACTIVE_BACKGROUND;
        final String fineReportInner = fineRepCheckBox.isSelected() ? fineFill : INACTIVE_BACKGROUND;
        final String severePopupInner = severePopCheckBox.isSelected() ? severeFill : INACTIVE_BACKGROUND;
        final String warningPopupInner = warningPopCheckBox.isSelected() ? warningFill : INACTIVE_BACKGROUND;
        final String infoPopupInner = infoPopCheckBox.isSelected() ? infoFill : INACTIVE_BACKGROUND;
        final String finePopupInner = finePopCheckBox.isSelected() ? fineFill : INACTIVE_BACKGROUND;

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

    public void updateFilterData() {
        popupFilters.clear();
        if (severePopCheckBox.isSelected()) {
            popupFilters.add(SeverityCode.SEVERE.getCode());
        }
        if (warningPopCheckBox.isSelected()) {
            popupFilters.add(SeverityCode.WARNING.getCode());
        }
        if (infoPopCheckBox.isSelected()) {
            popupFilters.add(SeverityCode.INFO.getCode());
        }
        if (finePopCheckBox.isSelected()) {
            popupFilters.add(SeverityCode.FINE.getCode());
        }
    }

    private void updateSettingsIcon(final FlowPane settingsPane, final String innerShade, final String borderShade) {
        settingsPane.setStyle("-fx-background-color: " + innerShade + "; -fx-border-color: " + borderShade + ";");
    }

    public void setReportsExpanded(final boolean expandedMode) {
        sessionErrorsBox.getChildren().forEach(tpNode -> ((TitledPane) tpNode).setExpanded(expandedMode));
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
        if (!sessionErrorsBox.getChildren().isEmpty()) {
            for (int i = 0; i < errCount; i++) {
                ErrorReportSessionData.getInstance().updateDisplayedEntryScreenSettings(
                        sessionErrors.get(i).getEntryId(), null, null, ((TitledPane) sessionErrorsBox.getChildren().get(i)).isExpanded(), sessionErrors.get(i).getDialog());
            }
        }
        final ArrayList<String> activeFilters = new ArrayList<>();
        if (severeRepCheckBox.isSelected() || severePopCheckBox.isSelected()) {
            activeFilters.add(SeverityCode.SEVERE.getCode());
        }
        if (warningRepCheckBox.isSelected() || warningPopCheckBox.isSelected()) {
            activeFilters.add(SeverityCode.WARNING.getCode());
        }
        if (infoRepCheckBox.isSelected() || infoPopCheckBox.isSelected()) {
            activeFilters.add(SeverityCode.INFO.getCode());
        }
        if (fineRepCheckBox.isSelected() || finePopCheckBox.isSelected()) {
            activeFilters.add(SeverityCode.FINE.getCode());
        }
        final List<ErrorReportEntry> combinedErrors = ErrorReportSessionData.getInstance().refreshDisplayedErrors(activeFilters);
        sessionErrors.clear();
        hiddenErrors.clear();
        for (final ErrorReportEntry entry : combinedErrors) {
            final SeverityCode entryCode = SeverityCode.getSeverityCodeEntry(entry.getErrorLevel().getName());
            if (entryCode != null) {
                switch (entryCode) {
                    case SEVERE -> {
                        if (severeRepCheckBox.isSelected()) {
                            sessionErrors.add(entry);
                        } else {
                            hiddenErrors.add(entry);
                        }
                    }
                    case WARNING -> {
                        if (warningRepCheckBox.isSelected()) {
                            sessionErrors.add(entry);
                        } else {
                            hiddenErrors.add(entry);
                        }
                    }
                    case INFO -> {
                        if (infoRepCheckBox.isSelected()) {
                            sessionErrors.add(entry);
                        } else {
                            hiddenErrors.add(entry);
                        }
                    }
                    case FINE -> {
                        if (fineRepCheckBox.isSelected()) {
                            sessionErrors.add(entry);
                        } else {
                            hiddenErrors.add(entry);
                        }
                    }
                }
            }
        }
    }

    public void updateSessionErrorsBox(final int insertPos) {
        synchronized (updateInProgress) {
            if (updateInProgress.compareAndSet(false, true)) {
                refreshSessionErrors();
                final Date nextFilterDate = new Date();
                final int errCount = sessionErrors.size();
                // rebuild                
                sessionErrorsBox.getChildren().clear();
                for (int i = 0; i < errCount; i++) {
                    boolean allowPopupDisplay = false;
                    if (errorReportRunning && ((SeverityCode.SEVERE.getCode().equals(sessionErrors.get(i).getErrorLevel().getName()) && severePopCheckBox.isSelected())
                            || (SeverityCode.WARNING.getCode().equals(sessionErrors.get(i).getErrorLevel().getName()) && warningPopCheckBox.isSelected())
                            || (SeverityCode.INFO.getCode().equals(sessionErrors.get(i).getErrorLevel().getName()) && infoPopCheckBox.isSelected())
                            || (SeverityCode.FINE.getCode().equals(sessionErrors.get(i).getErrorLevel().getName()) && finePopCheckBox.isSelected()))) {
                        allowPopupDisplay = true;
                    }
                    sessionErrorsBox.getChildren().add(generateErrorReportTitledPane(sessionErrors.get(i)));
                    if (allowPopupDisplay && sessionErrors.get(i).getLastDate().after(filterUpdateDate)) {
                        ErrorReportDialogManager.getInstance().showErrorDialog(sessionErrors.get(i), false);
                    }
                }

                // check if popup needed on hidden entries ... these entries can only exist with the allow popup checkbox ticked
                if (errorReportRunning) {
                    for (final ErrorReportEntry entry : hiddenErrors) {
                        if (entry.getLastDate().after(filterUpdateDate)) {
                            ErrorReportDialogManager.getInstance().showErrorDialog(entry, false);
                        }
                    }
                }
                final Date resumptionDate = ErrorReportDialogManager.getInstance().getGracePeriodResumptionDate();
                if (resumptionDate == null || nextFilterDate.after(resumptionDate)) {
                    filterUpdateDate = new Date(nextFilterDate.getTime());
                } else {
                    waitForGracePeriod = true;
                }
                updateInProgress.set(false);
            } else {
                LOGGER.log(Level.WARNING, "Unable to set session to: update in progress");
            }
        }
    }

    public boolean isIconFlashing() {
        return iconFlashing;
    }

    /**
     * enable or disable flashing of the alert icon, as well as flashing of the tab label
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
            if (alertTimer != null) {
                alertTimer.cancel();
                updateSettings();
            }
            if (defaultIcon != null) {
                SwingUtilities.invokeLater(() -> setIcon(defaultIcon));
            }
            alertTimer = null;
            return;
        }
        if (sessionErrorsBox.getChildren().isEmpty()) {
            return;
        }
        if (!iconFlashing) {
            iconFlashing = true;
            flashActivatedDate = previousRetrievalDate == null ? new Date() : new Date(previousRetrievalDate.getTime());
            if (alertTimer == null) {
                final TimerTask updateAlerts = new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            if (alertIcon == null) {
                                alertIcon = ImageIO.read(ErrorReportTopComponent.class.getResourceAsStream("resources/error-report-alert.png"));
                            }
                            if (defaultIcon == null) {
                                defaultIcon = ImageIO.read(ErrorReportTopComponent.class.getResourceAsStream("resources/error-report-default.png"));
                            }
                            final List<String> errorReportLevels = new ArrayList<>();
                            final List<String> errorPopupLevels = new ArrayList<>();
                            errorPopupLevels.addAll(ErrorReportDialogManager.getInstance().getActivePopupErrorLevels());
                            errorReportLevels.addAll(getErrorLevelList(true));
                            SwingUtilities.invokeLater(() -> updateFlashingIcons(errorReportLevels, errorPopupLevels));
                        } catch (IOException ex) {
                            LOGGER.log(Level.WARNING, "Error with alerts", ex);
                        }
                    }
                };
                requestAttention(false);
                alertTimer = new Timer();
                alertTimer.schedule(updateAlerts, 250, 750);
            }
        }
    }

    private void updateFlashingIcons(final List<String> errorReportLevels, final List<String> errorPopupLevels) {
        setIcon(alertStateActive ? alertIcon : defaultIcon);
        updateSettings();
        if (errorReportLevels.contains(SeverityCode.SEVERE.getCode())) {
            updateSettingsIcon(severeReportFilter, alertStateActive ? "#ef3030" : INACTIVE_BACKGROUND, alertStateActive ? "#ff5858" : INACTIVE_BACKGROUND);
        }
        if (errorReportLevels.contains(SeverityCode.WARNING.getCode())) {
            updateSettingsIcon(warnReportFilter, alertStateActive ? "#e88830" : INACTIVE_BACKGROUND, alertStateActive ? "#ff9848" : INACTIVE_BACKGROUND);
        }
        if (errorReportLevels.contains(SeverityCode.INFO.getCode())) {
            updateSettingsIcon(infoReportFilter, alertStateActive ? "#dada40" : INACTIVE_BACKGROUND, alertStateActive ? "#eaea58" : INACTIVE_BACKGROUND);
        }
        if (errorReportLevels.contains(SeverityCode.FINE.getCode())) {
            updateSettingsIcon(fineReportFilter, alertStateActive ? "#40d0d0" : INACTIVE_BACKGROUND, alertStateActive ? "#58e0e0" : INACTIVE_BACKGROUND);
        }

        if (errorPopupLevels.contains(SeverityCode.SEVERE.getCode())) {
            updateSettingsIcon(severePopupAllowed, alertStateActive ? "#ef3030" : INACTIVE_BACKGROUND, alertStateActive ? "#ff5858" : INACTIVE_BACKGROUND);
        }
        if (errorPopupLevels.contains(SeverityCode.WARNING.getCode())) {
            updateSettingsIcon(warnPopupAllowed, alertStateActive ? "#e88830" : INACTIVE_BACKGROUND, alertStateActive ? "#ff9848" : INACTIVE_BACKGROUND);
        }
        if (errorPopupLevels.contains(SeverityCode.INFO.getCode())) {
            updateSettingsIcon(infoPopupAllowed, alertStateActive ? "#dada40" : INACTIVE_BACKGROUND, alertStateActive ? "#eaea58" : INACTIVE_BACKGROUND);
        }
        if (errorPopupLevels.contains(SeverityCode.FINE.getCode())) {
            updateSettingsIcon(finePopupAllowed, alertStateActive ? "#40d0d0" : INACTIVE_BACKGROUND, alertStateActive ? "#58e0e0" : INACTIVE_BACKGROUND);
        }
        alertStateActive = !alertStateActive;
    }

    public void removeEntry(final int indexPosition) {
        sessionErrors.remove(indexPosition);
        updateSessionErrorsBox(-1);
    }

    public int getPopupControlValue() {
        return popupMode;
    }

    private List<String> getErrorLevelList(final boolean unAcknowledgedEntriesOnly) {
        final List<String> resultList = new ArrayList<>();
        for (final ErrorReportEntry entry : sessionErrors) {
            if (!resultList.contains(entry.getErrorLevel().getName()) && ((unAcknowledgedEntriesOnly && entry.getLastDate().after(flashActivatedDate)) || !unAcknowledgedEntriesOnly)) {
                resultList.add(entry.getErrorLevel().getName());
            }
        }
        return resultList;
    }

    /**
     * Generate the TitledPane object containing the details for the error report entry.
     *
     * @param entry
     * @return
     */
    public TitledPane generateErrorReportTitledPane(final ErrorReportEntry entry) {
        final TitledPane ttlPane = new TitledPane();
        String backgroundColour = DARK_MODE ? "#4a0000" : "#ff9696";
        String backgroundFadeColour = DARK_MODE ? "#140000" : "#f4f4f4";
        int redBase = 0;
        int redIncrement = 0;
        int greenBase = 0;
        int greenIncrement = 0;
        int blueBase = 0;
        int blueIncrement = 0;

        String alertColour = "#a0a0a0";
        if (entry.getErrorLevel() == Level.SEVERE) {
            alertColour = "#d87070";
            redBase = DARK_MODE ? 100 : 240;
            redIncrement = 16;
            greenBase = DARK_MODE ? 20 : 120;
            greenIncrement = 7;
            blueBase = DARK_MODE ? 20 : 120;
            blueIncrement = 7;
        } else if (entry.getErrorLevel() == Level.WARNING) {
            alertColour = "#c08c60";
            redBase = DARK_MODE ? 95: 250;
            redIncrement = 15;
            greenBase = DARK_MODE ? 50 : 160;
            greenIncrement = 10;
            blueBase = DARK_MODE ? 12 : 80;
            blueIncrement = 5;
        } else if (entry.getErrorLevel() == Level.INFO) {
            alertColour = "#a8a848";
            redBase = DARK_MODE ? 62 : 250;
            redIncrement = 13;
            greenBase = DARK_MODE ? 62 : 210;
            greenIncrement = 13;
            blueBase = DARK_MODE ? 8 : 80;
            blueIncrement = 4;
        } else if (entry.getErrorLevel() == Level.FINE) {
            alertColour = "#42a4a4";
            redBase = DARK_MODE ? 8 : 80;
            redIncrement = 4;
            greenBase = DARK_MODE ? 60 : 220;
            greenIncrement = 11;
            blueBase = DARK_MODE ? 60 : 220;
            blueIncrement = 11;
        }

        int intensityFactor = 1;
        if (entry.getOccurrences() > 999) {
            intensityFactor = 5;
            backgroundColour = DARK_MODE ? "#7c0000" : "#f37e7e";
            backgroundFadeColour = DARK_MODE ? "#240000" : "#fcfcfc";
        } else if (entry.getOccurrences() > 99) {
            intensityFactor = 4;
            backgroundColour = DARK_MODE ? "#6e0000" : "#f68484";
            backgroundFadeColour = DARK_MODE ? "#200000" : "#fafafa";
        } else if (entry.getOccurrences() > 9) {
            intensityFactor = 3;
            backgroundColour = DARK_MODE ? "#600000" : "#f98a8a";
            backgroundFadeColour = DARK_MODE ? "#1c0000" : "#f8f8f8";
        } else if (entry.getOccurrences() > 1) {
            intensityFactor = 2;
            backgroundColour = DARK_MODE ? "#540000" : "#fc9090";
            backgroundFadeColour = DARK_MODE ? "#180000" : "#f6f6f6";
        }
        String areaBackgroundColour = "radial-gradient(radius 100%, " + backgroundColour + " 0%, " + backgroundFadeColour + " 100%)";
        if (!DARK_MODE) {
            intensityFactor = -1 * intensityFactor;
        }

        final String severityColour = "rgb(" + (redBase + intensityFactor * redIncrement) + ","
                + (greenBase + intensityFactor * greenIncrement) + ","
                + (blueBase + intensityFactor * blueIncrement) + ")";

        final BorderPane bdrPane = new BorderPane();
        final VBox vBox = new VBox();
        vBox.setPadding(new Insets(1));
        
        final String textColour = DARK_MODE ? "#c0c0c0" : "#303030";

        final TextArea data = new TextArea(entry.getSummaryHeading() + "\n" + entry.getErrorData());
        data.setStyle("-fx-text-fill: " + textColour + "; -fx-background-color: " + backgroundColour + "; text-area-background: " + areaBackgroundColour + "; -fx-border-color: #505050; -fx-border-width: 2;");
        data.setEditable(false);
        data.setPadding(new Insets(2));
        data.setPrefRowCount(14);
        vBox.getChildren().add(data);

        ttlPane.setText("");
        ttlPane.setContent(vBox);
        ttlPane.setExpanded(entry.isExpanded());
        final HBox hBoxTitle = new HBox();
        final HBox hBoxLeft = new HBox();
        final HBox hBoxRight = new HBox();
        final Label timeLabel = new Label(entry.getTimeText());
        final Label label = new Label(entry.getTrimmedHeading(120));
        label.setTooltip(new Tooltip(label.getText()));
        final Label counterLabel = new Label(" " + Integer.toString(entry.getOccurrences()) + "x ");

        GridPane.setHgrow(label, Priority.ALWAYS);

        final WritableImage popupAllowImage = new WritableImage(16, 16);
        final WritableImage popupBlockImage = new WritableImage(16, 16);
        try {
            SwingFXUtils.toFXImage(ImageIO.read(ErrorReportTopComponent.class.getResource("resources/popupallow" + (DARK_MODE ? "dark" : "light") + ".png")), popupAllowImage);
            SwingFXUtils.toFXImage(ImageIO.read(ErrorReportTopComponent.class.getResource("resources/popupblock" + (DARK_MODE ? "dark" : "light") + ".png")), popupBlockImage);
        } catch (final IOException ioex) {
            LOGGER.log(Level.SEVERE, "Error loading image file", ioex);
        }
        final ImageView allowPopups = new ImageView(popupAllowImage);
        final ImageView blockPopups = new ImageView(popupBlockImage);

        String backgroundTextTitleColour = DARK_MODE ? backgroundFadeColour : "#b8c9db";
        String backgroundStyle = "-fx-background-color: linear-gradient( " + severityColour + " 1% , " + backgroundTextTitleColour + " 30%, " + backgroundTextTitleColour + " 70%, " + severityColour + " 99% );";
        
        final Button blockPopupsButton = new Button("");
        blockPopupsButton.setStyle("-fx-background-color: " + (DARK_MODE ? "404040" : "#A4BAD0") + ";" + " -fx-border-color: #404040");
        blockPopupsButton.setGraphic(entry.isBlockRepeatedPopups() ? blockPopups : allowPopups);
        blockPopupsButton.setTooltip(entry.isBlockRepeatedPopups() ? new Tooltip("Popups Blocked.\nRight click to review exception") : new Tooltip("Popups Allowed.\nRight click to review exception"));
        blockPopupsButton.setPadding(new Insets(2, 2, 2, 2));
        blockPopupsButton.setOnAction((final ActionEvent event) -> {
            entry.setBlockRepeatedPopups(!entry.isBlockRepeatedPopups());
            ErrorReportSessionData.getInstance().updateDisplayedEntryScreenSettings(entry.getEntryId(), null, entry.isBlockRepeatedPopups(), null, null);
            blockPopupsButton.setGraphic(entry.isBlockRepeatedPopups() ? blockPopups : allowPopups);
            blockPopupsButton.setTooltip(entry.isBlockRepeatedPopups() ? new Tooltip("Popups Blocked.\nRight click to review exception") : new Tooltip("Popups Allowed.\nRight click to review exception"));
        });
        final ContextMenu contextMenu = new ContextMenu();
        final MenuItem redisplay = new MenuItem("Redisplay Exception Dialog");
        redisplay.setOnAction((final ActionEvent event) -> ErrorReportDialogManager.getInstance().showDialog(entry, true));
        contextMenu.getItems().add(redisplay);
        blockPopupsButton.setContextMenu(contextMenu);
        
        blockPopupsButton.setMinHeight(18);
        blockPopupsButton.setMinHeight(18);

        final ImageView crossImageHighlight = new ImageView(UserInterfaceIconProvider.CROSS.buildImage(14, new Color(215, 215, 215)));
        final Button dismissButton = new Button("");
        dismissButton.setStyle("-fx-background-color: " + (DARK_MODE ? "404040" : "#91ACC7") + "; -fx-border-color: #606060"); // "#809EBD"
        dismissButton.setGraphic(crossImageHighlight);
        dismissButton.addEventFilter(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
            public void handle(final MouseEvent mouseEvent) {
                dismissButton.setStyle("-fx-background-color: #e84848; -fx-border-color: #c01c1c");
                mouseEvent.consume();
            }
        });
        dismissButton.addEventFilter(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
            public void handle(final MouseEvent mouseEvent) {
                dismissButton.setStyle("-fx-background-color: " + (DARK_MODE ? "404040" : "#91ACC7") + "; -fx-border-color: #606060");
                mouseEvent.consume();
            }
        });
        dismissButton.setPadding(new Insets(3,1,1,1));
        dismissButton.setMinHeight(21);
        dismissButton.setMinHeight(21);
        dismissButton.setOnAction((final ActionEvent event) -> {
            ErrorReportSessionData.getInstance().removeEntry(entry.getEntryId());
            updateSessionErrorsBox(-1);
        });

        label.setStyle("-fx-text-fill: " + textColour + ";");
        timeLabel.setStyle("-fx-text-fill: " + textColour + ";");

        hBoxLeft.setStyle(backgroundStyle);
        hBoxTitle.setStyle(backgroundStyle);
        hBoxRight.setStyle(backgroundStyle);
        counterLabel.setStyle(" -fx-background-color: " + (DARK_MODE ? "black" : "#e0e0e0") + "; -fx-text-background-color: cyan; -fx-text-fill: " + alertColour + " ; -fx-font-weight: bold; -fx-border-color: " + severityColour);
        counterLabel.setTooltip(new Tooltip("Repeated Occurrences"));
        counterLabel.setPadding(new Insets(1, 0, 1, 0));
        timeLabel.setPadding(new Insets(2, 0, 0, 0));
        hBoxLeft.getChildren().add(timeLabel);
        hBoxLeft.getChildren().add(counterLabel);
        hBoxTitle.getChildren().add(label);
        hBoxLeft.setPadding(new Insets(4, 6, 0, 0));
        hBoxTitle.setPadding(new Insets(6, 6, 0, 0));
        hBoxRight.setPadding(new Insets(2, 2, 2, 2));
        hBoxRight.setSpacing(2);
        hBoxRight.getChildren().add(blockPopupsButton);
        hBoxRight.getChildren().add(dismissButton);

        hBoxLeft.setMinHeight(27);
        hBoxLeft.setMinHeight(27);
        hBoxTitle.setMinHeight(27);
        hBoxTitle.setMinHeight(27);
        hBoxRight.setMinHeight(27);
        hBoxRight.setMinHeight(27);
        
        bdrPane.setLeft(hBoxLeft);
        bdrPane.setCenter(hBoxTitle);
        bdrPane.setRight(hBoxRight);
        bdrPane.setStyle("-fx-border-color: grey;");
        bdrPane.prefWidthProperty().bind(scrollPane.widthProperty().subtract(48));
        bdrPane.setPrefHeight(24);

        ttlPane.setGraphic(bdrPane);
        ttlPane.setPadding(new Insets(0, 16, 0, 0));

        return ttlPane;
    }

}
