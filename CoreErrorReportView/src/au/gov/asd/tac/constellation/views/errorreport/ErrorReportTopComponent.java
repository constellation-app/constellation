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

import au.gov.asd.tac.constellation.plugins.gui.MultiChoiceInputPane;
import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType.MultiChoiceParameterValue;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import javafx.scene.layout.BorderPane;
import au.gov.asd.tac.constellation.views.JavaFxTopComponent;
import java.awt.Color;
import java.awt.Image;
import java.io.IOException;
import javafx.scene.input.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
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
import javafx.scene.control.ContextMenu;
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
import org.openide.util.HelpCtx;
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

    /**
     * @return the params
     */
    public PluginParameters getParams() {
        return params;
    }

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

    private static final String BLACK = "black";
    private static final String WHITE = "white";
    private static final String INACTIVE_BACKGROUND = DARK_MODE ? BLACK : WHITE;
    private static final String MODE_TEXT = DARK_MODE ? "dark" : "light";
    private static final String FX_TEXT_FILL = " -fx-text-fill: ";
    private static final String FX_BACKGROUND = " -fx-background-color: ";

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

    private final ImageView helpImage = new ImageView(UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.SKY.getJavaColor()));
    private final Button helpButton = new Button("", helpImage);

    protected final PluginParameters params = new PluginParameters();
    public static final String REPORT_SETTINGS_PARAMETER_ID = PluginParameter.buildId(ErrorReportTopComponent.class, "report_settings");
    public static final String POPUP_REPORT_SETTINGS_PARAMETER_ID = PluginParameter.buildId(ErrorReportTopComponent.class, "popup_report_settings");
        
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

        helpButton.setStyle("-fx-border-color: transparent; -fx-background-color: transparent; -fx-effect: null; ");
        helpButton.setOnAction(event -> new HelpCtx("au.gov.asd.tac.constellation.views.errorreport").display());

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

        updateSettings();
        
        final PluginParameter<MultiChoiceParameterType.MultiChoiceParameterValue> reportSettingOptions = MultiChoiceParameterType.build(REPORT_SETTINGS_PARAMETER_ID);
        reportSettingOptions.setName("Report Settings");
        reportSettingOptions.setDescription("Report Settings");
        MultiChoiceParameterType.setOptions(reportSettingOptions, Arrays.asList(
                SeverityCode.SEVERE.getCode(), SeverityCode.WARNING.getCode(),
                SeverityCode.INFO.getCode(), SeverityCode.FINE.getCode()));

        final List<String> checked = new ArrayList<>();
        checked.add(SeverityCode.SEVERE.getCode());
        checked.add(SeverityCode.WARNING.getCode());
        checked.add(SeverityCode.INFO.getCode());
        checked.add(SeverityCode.FINE.getCode());
        MultiChoiceParameterType.setChoices(reportSettingOptions, checked);
        reportSettingOptions.setEnabled(true);
        
        final PluginParameter<MultiChoiceParameterType.MultiChoiceParameterValue> popupReportSettingOptions = MultiChoiceParameterType.build(POPUP_REPORT_SETTINGS_PARAMETER_ID);
        popupReportSettingOptions.setName("Popup Report Settings");
        popupReportSettingOptions.setDescription("Popup Report Settings");
        MultiChoiceParameterType.setOptions(popupReportSettingOptions, Arrays.asList(
                SeverityCode.SEVERE.getCode(),
                SeverityCode.WARNING.getCode(),
                SeverityCode.INFO.getCode(),
                SeverityCode.FINE.getCode()));
        final List<String> popupChecked = new ArrayList<>();
        popupChecked.add(SeverityCode.SEVERE.getCode());
        popupChecked.add(SeverityCode.WARNING.getCode());
        MultiChoiceParameterType.setChoices(popupReportSettingOptions, popupChecked);
        popupReportSettingOptions.setEnabled(true);
        
        getParams().addParameter(reportSettingOptions);
        getParams().addParameter(popupReportSettingOptions);
        
        getParams().addController(REPORT_SETTINGS_PARAMETER_ID, (masterId, parameters, change) -> {
            if (change == ParameterChange.VALUE) {
                filterUpdateDate = new Date();
                updateSettings();
                updateSessionErrorsBox(-1);
            }
        });
        
        getParams().addController(POPUP_REPORT_SETTINGS_PARAMETER_ID, (masterId, parameters, change) -> {
            if (change == ParameterChange.VALUE) {
                filterUpdateDate = new Date();
                updateSettings();
                updateSessionErrorsBox(-1);
            }
        });
                
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
        final ToolBar controlToolbar2 = new ToolBar();
        final Label reportSettingsLabel = new Label("Report:");
        final Label popupReportSettingsLabel = new Label("Popup:");
        final MultiChoiceInputPane reportSettingPane = new MultiChoiceInputPane(reportSettingOptions);
        reportSettingPane.setPrefWidth(240);
        reportSettingPane.setMaxWidth(240);
        
        final MultiChoiceInputPane popupSettingPane = new MultiChoiceInputPane(popupReportSettingOptions);
        popupSettingPane.setPrefWidth(150);
        popupSettingPane.setMaxWidth(240);
        HBox.setHgrow(popupSettingPane, Priority.ALWAYS);
        
        controlToolbar.getItems().addAll(settingsBox, minimizeButton, maximizeButton, popupControl, clearButton, helpButton);
        controlToolbar2.getItems().addAll(reportSettingsLabel, reportSettingPane, popupReportSettingsLabel, popupSettingPane);
        controlToolbar.setPrefHeight(20);
        controlToolbar2.setPrefHeight(20);
        
        final VBox toolboxContainer = new VBox();
        toolboxContainer.setPrefHeight(20);
        toolboxContainer.setMaxHeight(20);
        toolboxContainer.getChildren().add(controlToolbar);
        toolboxContainer.getChildren().add(controlToolbar2);
        GridPane.setHgrow(toolboxContainer, Priority.ALWAYS);

        componentBox.getChildren().add(toolboxContainer);

        final BorderPane errorBPane = new BorderPane();
        errorBPane.setStyle(FX_BACKGROUND + INACTIVE_BACKGROUND + ";");
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


        List<String> choices = new ArrayList<>();
        List<String> choices2 = new ArrayList<>();
        if (getParams().hasParameter(REPORT_SETTINGS_PARAMETER_ID)) {
            MultiChoiceParameterValue multiChoiceValue = getParams().getMultiChoiceValue(REPORT_SETTINGS_PARAMETER_ID);
            choices = multiChoiceValue.getChoices();
        }
        if (getParams().hasParameter(POPUP_REPORT_SETTINGS_PARAMETER_ID)) {
            MultiChoiceParameterValue multiChoiceValue = getParams().getMultiChoiceValue(POPUP_REPORT_SETTINGS_PARAMETER_ID);
            choices2 = multiChoiceValue.getChoices();
        }
        
        final boolean severeRepIsSelected = choices.contains(SeverityCode.SEVERE.getCode());
        final boolean warningRepIsSelected = choices.contains(SeverityCode.WARNING.getCode());
        final boolean infoRepIsSelected = choices.contains(SeverityCode.INFO.getCode());
        final boolean fineRepIsSelected = choices.contains(SeverityCode.FINE.getCode());
        final boolean severePopupIsSelected = choices2.contains(SeverityCode.SEVERE.getCode());
        final boolean warningPopupIsSelected = choices2.contains(SeverityCode.WARNING.getCode());
        final boolean infoPopupIsSelected = choices2.contains(SeverityCode.INFO.getCode());
        final boolean finePopupIsSelected = choices2.contains(SeverityCode.FINE.getCode());
        
        final String severeReportBorderShade = severeRepIsSelected ? severeFill : INACTIVE_BACKGROUND;
        final String warningReportBorderShade = warningRepIsSelected ? warningFill : INACTIVE_BACKGROUND;
        final String infoReportBorderShade = infoRepIsSelected ? infoFill : INACTIVE_BACKGROUND;
        final String fineReportBorderShade = fineRepIsSelected ? fineFill : INACTIVE_BACKGROUND;
        final String severePopupBorderShade = severePopupIsSelected ? severeFill : INACTIVE_BACKGROUND;
        final String warningPopupBorderShade = warningPopupIsSelected ? warningFill : INACTIVE_BACKGROUND;
        final String infoPopupBorderShade = infoPopupIsSelected ? infoFill : INACTIVE_BACKGROUND;
        final String finePopupBorderShade = finePopupIsSelected ? fineFill : INACTIVE_BACKGROUND;

        
        updateSettingsIcon(severeReportFilter, severeReportBorderShade, severeRepIsSelected ? severeBorder : severeFill);
        updateSettingsIcon(warnReportFilter, warningReportBorderShade, warningRepIsSelected ? warningBorder : warningFill);
        updateSettingsIcon(infoReportFilter, infoReportBorderShade, infoRepIsSelected ? infoBorder : infoFill);
        updateSettingsIcon(fineReportFilter, fineReportBorderShade, fineRepIsSelected ? fineBorder : fineFill);
        updateSettingsIcon(severePopupAllowed, severePopupBorderShade, severePopupIsSelected ? severeBorder : severeFill);
        updateSettingsIcon(warnPopupAllowed, warningPopupBorderShade, warningPopupIsSelected ? warningBorder : warningFill);
        updateSettingsIcon(infoPopupAllowed, infoPopupBorderShade, infoPopupIsSelected ? infoBorder : infoFill);
        updateSettingsIcon(finePopupAllowed, finePopupBorderShade, finePopupIsSelected ? fineBorder : fineFill);
        
        updateFilterData();
    }

    public void updateFilterData() {
        

        List<String> choices = new ArrayList<>();
        List<String> popupChoices = new ArrayList<>();        
        if (getParams().hasParameter(REPORT_SETTINGS_PARAMETER_ID)) {
            MultiChoiceParameterValue multiChoiceValue = getParams().getMultiChoiceValue(REPORT_SETTINGS_PARAMETER_ID);
            choices = multiChoiceValue.getChoices();
        }
       
        if (getParams().hasParameter(POPUP_REPORT_SETTINGS_PARAMETER_ID)) {
            MultiChoiceParameterValue multiChoiceValue = getParams().getMultiChoiceValue(POPUP_REPORT_SETTINGS_PARAMETER_ID);
            popupChoices = multiChoiceValue.getChoices();
        }
        popupFilters.clear();

        final boolean severePopupIsSelected = popupChoices.contains(SeverityCode.SEVERE.getCode());
        final boolean warningPopupIsSelected = popupChoices.contains(SeverityCode.WARNING.getCode());
        final boolean infoPopupIsSelected = popupChoices.contains(SeverityCode.INFO.getCode());
        final boolean finePopupIsSelected = popupChoices.contains(SeverityCode.FINE.getCode());
        
        if (severePopupIsSelected) {
            popupFilters.add(SeverityCode.SEVERE.getCode());
        }
        if (warningPopupIsSelected) {
            popupFilters.add(SeverityCode.WARNING.getCode());
        }
        if (infoPopupIsSelected) {
            popupFilters.add(SeverityCode.INFO.getCode());
        }
        if (finePopupIsSelected) {
            popupFilters.add(SeverityCode.FINE.getCode());
        }
    }

    private void updateSettingsIcon(final FlowPane settingsPane, final String innerShade, final String borderShade) {
        settingsPane.setStyle(FX_BACKGROUND + innerShade + "; -fx-border-color: " + borderShade + ";");
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
                final ErrorReportEntry sessionErr = sessionErrors.get(i);
                ErrorReportSessionData.getInstance().updateDisplayedEntryScreenSettings(sessionErr.getEntryId(), null, null, ((TitledPane) sessionErrorsBox.getChildren().get(i)).isExpanded(), sessionErr.getDialog());
            }
        }
        final ArrayList<String> activeFilters = new ArrayList<>();
        
        
        List<String> choices = new ArrayList<>();
        List<String> popupChoices = new ArrayList<>();
        
        if (getParams().hasParameter(REPORT_SETTINGS_PARAMETER_ID)) {
            MultiChoiceParameterValue multiChoiceValue = getParams().getMultiChoiceValue(REPORT_SETTINGS_PARAMETER_ID);
            choices = multiChoiceValue.getChoices();
        }
        if (getParams().hasParameter(POPUP_REPORT_SETTINGS_PARAMETER_ID)) {
            MultiChoiceParameterValue multiChoiceValue = getParams().getMultiChoiceValue(POPUP_REPORT_SETTINGS_PARAMETER_ID);
            popupChoices = multiChoiceValue.getChoices();
        }
        final boolean severeRepIsSelected = choices.contains(SeverityCode.SEVERE.getCode());
        final boolean warningRepIsSelected = choices.contains(SeverityCode.WARNING.getCode());
        final boolean infoRepIsSelected = choices.contains(SeverityCode.INFO.getCode());
        final boolean fineRepIsSelected = choices.contains(SeverityCode.FINE.getCode());
        final boolean severePopupIsSelected = popupChoices.contains(SeverityCode.SEVERE.getCode());
        final boolean warningPopupIsSelected = popupChoices.contains(SeverityCode.WARNING.getCode());
        final boolean infoPopupIsSelected = popupChoices.contains(SeverityCode.INFO.getCode());
        final boolean finePopupIsSelected = popupChoices.contains(SeverityCode.FINE.getCode());
        
        
        if (severeRepIsSelected || severePopupIsSelected) {
            activeFilters.add(SeverityCode.SEVERE.getCode());
        }
        if (warningRepIsSelected || warningPopupIsSelected) {
            activeFilters.add(SeverityCode.WARNING.getCode());
        }
        if (infoRepIsSelected || infoPopupIsSelected) {
            activeFilters.add(SeverityCode.INFO.getCode());
        }
        if (fineRepIsSelected || finePopupIsSelected) {
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
                        if (severeRepIsSelected) {
                            sessionErrors.add(entry);
                        } else {
                            hiddenErrors.add(entry);
                        }
                    }
                    case WARNING -> {
                        if (warningRepIsSelected) {
                            sessionErrors.add(entry);
                        } else {
                            hiddenErrors.add(entry);
                        }
                    }
                    case INFO -> {
                        if (infoRepIsSelected) {
                            sessionErrors.add(entry);
                        } else {
                            hiddenErrors.add(entry);
                        }
                    }
                    case FINE -> {
                        if (fineRepIsSelected) {
                            sessionErrors.add(entry);
                        } else {
                            hiddenErrors.add(entry);
                        }
                    }
                    default -> {
                        return;
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
                
                // check popup selection
                List<String> choices = new ArrayList<>();
                if (getParams().hasParameter(REPORT_SETTINGS_PARAMETER_ID)) {
                    MultiChoiceParameterValue multiChoiceValue = getParams().getMultiChoiceValue(REPORT_SETTINGS_PARAMETER_ID);
                    choices = multiChoiceValue.getChoices();
                }
                
                List<String> popupChoices = new ArrayList<>();
                if (getParams().hasParameter(POPUP_REPORT_SETTINGS_PARAMETER_ID)) {
                    MultiChoiceParameterValue multiChoiceValue = getParams().getMultiChoiceValue(POPUP_REPORT_SETTINGS_PARAMETER_ID);
                    popupChoices = multiChoiceValue.getChoices();
                }
                final boolean severePopupIsSelected = popupChoices.contains(SeverityCode.SEVERE.getCode());
                final boolean warningPopupIsSelected = popupChoices.contains(SeverityCode.WARNING.getCode());
                final boolean infoPopupIsSelected = popupChoices.contains(SeverityCode.INFO.getCode());
                final boolean finePopupIsSelected = popupChoices.contains(SeverityCode.FINE.getCode());
        
                for (int i = 0; i < errCount; i++) {
                    boolean allowPopupDisplay = false;

                    if (errorReportRunning && ((SeverityCode.SEVERE.getCode().equals(sessionErrors.get(i).getErrorLevel().getName()) && severePopupIsSelected)
                            || (SeverityCode.WARNING.getCode().equals(sessionErrors.get(i).getErrorLevel().getName()) && warningPopupIsSelected)
                            || (SeverityCode.INFO.getCode().equals(sessionErrors.get(i).getErrorLevel().getName()) && infoPopupIsSelected)
                            || (SeverityCode.FINE.getCode().equals(sessionErrors.get(i).getErrorLevel().getName()) && finePopupIsSelected))) {
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
        String backgroundColour;
        String backgroundFadeColour;
        final String textColour;
        final String dismissButtonColor;

        int redBase = 0;
        int redIncrement = 0;
        int greenBase = 0;
        int greenIncrement = 0;
        int blueBase = 0;
        int blueIncrement = 0;
        String alertColour = "#a0a0a0";

        int intensityFactor = 1;
        if (DARK_MODE) {
            backgroundColour = "#4a0000";
            backgroundFadeColour = "#140000";
            textColour = "#c0c0c0";
            dismissButtonColor = "#404040";

            if (entry.getOccurrences() > 999) {
                intensityFactor = 5;
                backgroundColour = "#7c0000";
                backgroundFadeColour = "#240000";
            } else if (entry.getOccurrences() > 99) {
                intensityFactor = 4;
                backgroundColour = "#6e0000";
                backgroundFadeColour = "#200000";
            } else if (entry.getOccurrences() > 9) {
                intensityFactor = 3;
                backgroundColour = "#600000";
                backgroundFadeColour = "#1c0000";
            } else if (entry.getOccurrences() > 1) {
                intensityFactor = 2;
                backgroundColour = "#540000";
                backgroundFadeColour = "#180000";
            }

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

        } else {
            backgroundColour = "#ec9696";
            backgroundFadeColour = "#f2ebeb";
            textColour = "#303030";
            dismissButtonColor = "#89A0B5";

            if (entry.getOccurrences() > 999) {
                intensityFactor = 5;
                backgroundColour = "#dc7676";
                backgroundFadeColour = "#e9dfdf";
            } else if (entry.getOccurrences() > 99) {
                intensityFactor = 4;
                backgroundColour = "#e07e7e";
                backgroundFadeColour = "#eae2e2";
            } else if (entry.getOccurrences() > 9) {
                intensityFactor = 3;
                backgroundColour = "#e48686";
                backgroundFadeColour = "#ece5e5";
            } else if (entry.getOccurrences() > 1) {
                intensityFactor = 2;
                backgroundColour = "#e88e8e";
                backgroundFadeColour = "#eee8e8";
            }

            if (entry.getErrorLevel() == Level.SEVERE) {
                alertColour = "#d87070";
                redBase = 245;
                redIncrement = -10;
                greenBase = 115;
                greenIncrement = -6;
                blueBase = 115;
                blueIncrement = -6;
            } else if (entry.getErrorLevel() == Level.WARNING) {
                alertColour = "#c08c60";
                redBase = 250;
                redIncrement = -15;
                greenBase = 155;
                greenIncrement = -10;
                blueBase = 80;
                blueIncrement = -5;
            } else if (entry.getErrorLevel() == Level.INFO) {
                alertColour = "#a8a848";
                redBase = 250;
                redIncrement = -13;
                greenBase = 210;
                greenIncrement = -13;
                blueBase = 80;
                blueIncrement = -4;
            } else if (entry.getErrorLevel() == Level.FINE) {
                alertColour = "#42a4a4";
                redBase = 80;
                redIncrement = -4;
                greenBase = 220;
                greenIncrement = -11;
                blueBase = 220;
                blueIncrement = -11;
            }

        }
        final String areaBackgroundColour = "radial-gradient(radius 100%, " + backgroundColour + " 0%, " + backgroundFadeColour + " 100%)";

        final String severityColour = "rgb(" + (redBase + intensityFactor * redIncrement) + ","
                + (greenBase + intensityFactor * greenIncrement) + ","
                + (blueBase + intensityFactor * blueIncrement) + ")";

        final BorderPane bdrPane = new BorderPane();
        final VBox vBox = new VBox();
        vBox.setPadding(new Insets(1));

        final TextArea data = new TextArea(entry.getSummaryHeading() + "\n" + entry.getErrorData());
        data.setStyle(FX_TEXT_FILL + textColour + ";" + FX_BACKGROUND + backgroundColour + "; -text-area-background: " + areaBackgroundColour + "; -fx-border-color: #505050; -fx-border-width: 2;");
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
            SwingFXUtils.toFXImage(ImageIO.read(ErrorReportTopComponent.class.getResource("resources/popupallow" + MODE_TEXT + ".png")), popupAllowImage);
            SwingFXUtils.toFXImage(ImageIO.read(ErrorReportTopComponent.class.getResource("resources/popupblock" + MODE_TEXT + ".png")), popupBlockImage);
        } catch (final IOException ioex) {
            LOGGER.log(Level.SEVERE, "Error loading image file", ioex);
        }
        final ImageView allowPopups = new ImageView(popupAllowImage);
        final ImageView blockPopups = new ImageView(popupBlockImage);

        final String backgroundTextTitleColour = backgroundFadeColour;
        final String backgroundStyle = "-fx-background-color: linear-gradient( " + severityColour + " 1% , " + backgroundTextTitleColour + " 35%, " + backgroundTextTitleColour + " 65%, " + severityColour + " 99% );";

        final Button blockPopupsButton = new Button("");
        blockPopupsButton.setStyle(FX_BACKGROUND + (DARK_MODE ? "#404040" : "#aabaca") + ";" + " -fx-border-color: #606060");
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

        blockPopupsButton.setMinHeight(22);
        blockPopupsButton.setMaxHeight(22);

        final ImageView crossImageHighlight = new ImageView(UserInterfaceIconProvider.CROSS.buildImage(14, new Color(215, 215, 215)));
        final Button dismissButton = new Button("");
        dismissButton.setStyle(FX_BACKGROUND + dismissButtonColor + "; -fx-border-color: #606060");
        dismissButton.setGraphic(crossImageHighlight);
        dismissButton.addEventFilter(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent mouseEvent) {
                dismissButton.setStyle("-fx-background-color: #e84848; -fx-border-color: #c03c3c");
                mouseEvent.consume();
            }
        });
        dismissButton.addEventFilter(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent mouseEvent) {
                dismissButton.setStyle(FX_BACKGROUND + dismissButtonColor + "; -fx-border-color: #606060");
                mouseEvent.consume();
            }
        });
        dismissButton.setPadding(new Insets(3, 1, 1, 1));
        dismissButton.setMinHeight(22);
        dismissButton.setMaxHeight(22);
        dismissButton.setOnAction((final ActionEvent event) -> {
            ErrorReportSessionData.getInstance().removeEntry(entry.getEntryId());
            updateSessionErrorsBox(-1);
        });

        label.setStyle(FX_TEXT_FILL + textColour + ";");
        timeLabel.setStyle(FX_TEXT_FILL + textColour + ";");

        hBoxLeft.setStyle(backgroundStyle);
        hBoxTitle.setStyle(backgroundStyle);
        hBoxRight.setStyle(backgroundStyle);
        counterLabel.setStyle(FX_BACKGROUND + (DARK_MODE ? BLACK : "#e0e0e0") + ";" + FX_TEXT_FILL + alertColour + " ; -fx-font-weight: bold; -fx-border-color: " + severityColour);
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
        // ErrorReportFullSuiteNGTest will sometimes have scrollPane = null
        if (scrollPane != null) {
            bdrPane.prefWidthProperty().bind(scrollPane.widthProperty().subtract(48));
        }
        bdrPane.setPrefHeight(24);

        ttlPane.setGraphic(bdrPane);
        ttlPane.setPadding(new Insets(0, 16, 0, 0));

        return ttlPane;
    }

}
