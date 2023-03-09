/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.views.errorreport;

import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.utilities.icon.CharacterIconProvider;
import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import au.gov.asd.tac.constellation.views.JavaFxTopComponent;
import java.awt.Color;
import java.awt.Image;
import javafx.scene.input.MouseEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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

public class ErrorReportTopComponent extends JavaFxTopComponent<BorderPane>  { // implements GraphReportListener

    public static final Logger LOGGER = Logger.getLogger(ErrorReportTopComponent.class.getName());

    private List<ErrorReportEntry> sessionErrors = new ArrayList<>();
    static VBox sessionErrorsBox = new VBox();
    private ComboBox<String> popupControl;

    private boolean iconFlashing = false;
    private BorderPane erbp = null;
    
    private AtomicBoolean updateInProgress = new AtomicBoolean(false);
    
    private Timer refreshTimer = null;
    private Date latestRetrievalDate = null;
    
    private int popupMode = 1;
    
    ErrorReportTopComponent(){
        LOGGER.info("\n\n -- create error report component ! ");
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
        TimerTask refreshAction = new TimerTask(){
            @Override
            public void run() {
                Platform.runLater(new Runnable(){
                    public void run() {
                        if(updateInProgress != null && !updateInProgress.get()) {                            
                            if (latestRetrievalDate == null || latestRetrievalDate.before(ErrorReportSessionData.lastUpdate) || ErrorReportSessionData.screenUpdateRequested) {
                                latestRetrievalDate = new Date();
                                flashErrorIcon(true);
                                ErrorReportDialogManager.getInstance().updatePopupMode(getPopupControlValue());
                                updateSessionErrorsBox(-1);
                                ErrorReportSessionData.screenUpdateRequested = false;
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
        LOGGER.info("\n\n -- create style ");
        return "resources/error-report.css";
    }

    @Override
    protected BorderPane createContent() {
        LOGGER.info("\n\n -- create content ");
        BorderPane outerPane = new BorderPane();
        VBox componentBox = new VBox();
        
        Label popupLabel = new Label("Show Error Popups: ");
        ObservableList<String> popupOptions = FXCollections.observableArrayList();
        popupOptions.add("Never");
        popupOptions.add("One only");
        popupOptions.add("One only, redisplayable");
        popupOptions.add("One per source");
        popupOptions.add("One per source, redisplayable");
        popupControl = new ComboBox<>(popupOptions);
        popupControl.getSelectionModel().select(1);
        
        Button clearButton = new Button("Clear All Reports");
        clearButton.setTooltip(new Tooltip("Clear all current error reports"));
        clearButton.setOnAction((ActionEvent event) -> {
            // remove all matching entries in the data class
            int errCount = sessionErrors.size();
            for (int i = 0; i < errCount; i++){
                ErrorReportSessionData.getInstance().removeEntry(sessionErrors.get(i).getEntryId());
            }
            // clear local data
            sessionErrors.clear();
            updateSessionErrorsBox(-1);
        });


        final ImageView expandButtonImage = new ImageView(CharacterIconProvider.CHAR_002B.buildImage(15, Color.WHITE));
        final Button expandButton = new Button("");
        expandButton.setGraphic(expandButtonImage);
        expandButton.setTooltip(new Tooltip("Expand All Error Reports"));
        expandButton.setOnAction((ActionEvent event) -> {
            setReportsExpanded(true);
        });

        final ImageView shrinkButtonImage = new ImageView(CharacterIconProvider.CHAR_002D.buildImage(15, Color.WHITE));
        final Button shrinkButton = new Button("");
        shrinkButton.setGraphic(shrinkButtonImage);
        shrinkButton.setTooltip(new Tooltip("Shrink All Error Reports"));
        shrinkButton.setOnAction((ActionEvent event) -> {
            setReportsExpanded(false);
        });

        
        ToolBar controlToolbar = new ToolBar();
        controlToolbar.getItems().addAll(popupLabel, popupControl, shrinkButton, expandButton, clearButton);
        HBox toolboxContainer = new HBox();
        toolboxContainer.getChildren().add(controlToolbar);
        toolboxContainer.getChildren().add(new Label("  "));
        GridPane.setHgrow(controlToolbar, Priority.ALWAYS);
        
        
        componentBox.getChildren().add(toolboxContainer);
        
        erbp = new BorderPane();
        erbp.setStyle("-fx-text-fill: purple; -fx-text-background-color: blue; -fx-background-color: black;");
        sessionErrorsBox.setPadding(new Insets(0,0,0,0));
        sessionErrorsBox.setSpacing(2);
                
        erbp.addEventFilter(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
                            public void handle(final MouseEvent mouseEvent) {
                                LOGGER.info("\n\n -- handle mouse event : " + mouseEvent);
                                flashErrorIcon(false);
                                mouseEvent.consume();
                            }
                        });
        
        erbp.setTop(sessionErrorsBox);
        componentBox.getChildren().add(erbp);
        outerPane.setTop(componentBox);
        
        return outerPane;
    }
    
    @Override
    protected ScrollPane.ScrollBarPolicy getVerticalScrollPolicy() {
        return ScrollPane.ScrollBarPolicy.ALWAYS;
    }    
    
    public void setReportsExpanded(boolean expandedMode){
        int errCount = sessionErrors.size();
        if (sessionErrorsBox.getChildren().size() > 0) {
            for (int i=0; i<errCount; i++) {
                ((TitledPane)sessionErrorsBox.getChildren().get(i)).setExpanded(expandedMode);
            }      
        }
    }
    
    public ErrorReportEntry findActiveEntryWithId(double id){
        for (ErrorReportEntry activeEntry: sessionErrors) {
            if (activeEntry.getEntryId() == id) {
                return activeEntry;
            }
        }        
        return null;
    }
    
    public void refreshSessionErrors(){
        // save expanded states
        int errCount = sessionErrors.size();
        if (sessionErrorsBox.getChildren().size() > 0) {
            for (int i=0; i<errCount; i++) {
                ErrorReportSessionData.getInstance().updateDisplayedEntryScreenSettings(
                        sessionErrors.get(i).getEntryId(), null, null, ((TitledPane)sessionErrorsBox.getChildren().get(i)).isExpanded());
            }      
        }
        sessionErrors = ErrorReportSessionData.getInstance().refreshDisplayedErrors();
    }
    
    public void updateSessionErrorsBox(int insertPos){
        // get current snapshot of errors
        boolean sessionUpdated = false;
        while(!sessionUpdated) {
            if (updateInProgress.compareAndSet(false, true)) {
                refreshSessionErrors();
                int errCount = sessionErrors.size();
                // rebuild                
                sessionErrorsBox.getChildren().clear();
                for (int i=0; i<errCount; i++) {
                    sessionErrorsBox.getChildren().add(generateErrorReportTitledPane(sessionErrors.get(i)));
                    ErrorReportDialogManager.getInstance().showErrorDialog(sessionErrors.get(i));
                }

                sessionUpdated = true;
                updateInProgress.set(false);
            } else {
                try {
                    Thread.sleep(180);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    
    public boolean isIconFlashing(){
        return iconFlashing;
    }
    
    public void flashErrorIcon(boolean enabled){
        
        if (sessionErrorsBox.getChildren().isEmpty()) return;
        
        if (enabled && !iconFlashing) {
            iconFlashing = true;
            try {
                //LOGGER.info(" - - - - - alertIcon : " + ErrorReportTopComponent.class.getResource("resources/error-report-alert.png").getPath());
                final Image alertIcon = ImageIO.read(ErrorReportTopComponent.class.getResourceAsStream("resources/error-report-alert.png"));
                final Image defaultIcon = ImageIO.read(ErrorReportTopComponent.class.getResourceAsStream("resources/error-report-default.png"));
                Thread flasher = new Thread(() -> {
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
                        } catch (InvocationTargetException | InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
                flasher.start();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            iconFlashing = enabled;
        }
    }
    
    public void removeEntry(int indexPosition){
        sessionErrors.remove(indexPosition);
        updateSessionErrorsBox(-1);
    }
    
    public int getPopupControlValue(){
        return popupControl.getSelectionModel().getSelectedIndex();
    }
    
    public TitledPane generateErrorReportTitledPane(ErrorReportEntry ere){
        TitledPane tp = new TitledPane();
        String alertColour = "#f0b0b0";
        String backgroundColour = "#4a0000";
        String areaBackgroundColour ="linear-gradient(to bottom right, #4a0000, #100000)";//28
        if (ere.getOccurrences() > 999) {
            alertColour = "#d06868";
            backgroundColour = "#7c0000";
            areaBackgroundColour ="linear-gradient(to bottom right, #7c0000, #240000)";//54
        } else if (ere.getOccurrences() > 99) {
            alertColour = "#e07878";
            backgroundColour = "#6e0000";
            areaBackgroundColour ="linear-gradient(to bottom right, #6e0000, #1f0000)";//48
        } else if (ere.getOccurrences() > 9) {
            alertColour = "#f08080";
            backgroundColour = "#600000";
            areaBackgroundColour ="linear-gradient(to bottom right, #600000, #1a0000)";//3c
        } else if (ere.getOccurrences() > 1) {
            alertColour = "#f09898";
            backgroundColour = "#540000";
            areaBackgroundColour ="linear-gradient(to bottom right, #540000, #150000)"; //30
        }

        BorderPane bop = new BorderPane();
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(1));
        
        TextArea data = new TextArea(ere.getErrorData());
        data.setStyle("-fx-text-fill: #c0c0c0; -fx-background-color: " + backgroundColour + "; text-area-background: " + areaBackgroundColour + "; -fx-border-color: #505050; -fx-border-width: 2;"); //  + backgroundColour
        data.setEditable(false);
        data.setPadding(new Insets(2));
        data.setPrefRowCount(14);        
        vBox.getChildren().add(data);
        
        tp.setText("");      
        tp.setContent(vBox);    
        tp.setExpanded(ere.getExpanded());           
        HBox hBoxTitle = new HBox();
        HBox hBoxLeft = new HBox();
        HBox hBoxRight = new HBox();
        Label timeLabel = new Label(ere.getTimeText());
        Label label = new Label(ere.getHeading());
        label.setTooltip(new Tooltip(label.getText()));
        Label counterLabel = new Label(" " + Integer.toString(ere.getOccurrences()) + "x ");
        
        GridPane.setHgrow(label, Priority.ALWAYS);
        
        final ImageView shieldImageHighlight = new ImageView(UserInterfaceIconProvider.LOCK.buildImage(15, Color.ORANGE.darker()));
        final ImageView visibleImageHighlight = new ImageView(UserInterfaceIconProvider.UNLOCK.buildImage(15, Color.CYAN.darker()));
        final Button blockPopupsButton = new Button("");
        blockPopupsButton.setStyle("-fx-background-color:" + backgroundColour +"; -fx-border-color: #404040");
        blockPopupsButton.setGraphic(ere.isBlockRepeatedPopups() ? shieldImageHighlight : visibleImageHighlight);
        blockPopupsButton.setTooltip(ere.isBlockRepeatedPopups() ? new Tooltip("Popups Blocked") : new Tooltip("Popups Allowed"));
        blockPopupsButton.setPadding(new Insets(3,2,1,2));
        blockPopupsButton.setOnAction((ActionEvent event) -> {
            ere.setBlockRepeatedPopups(!ere.isBlockRepeatedPopups());
            blockPopupsButton.setGraphic(ere.isBlockRepeatedPopups() ? shieldImageHighlight : visibleImageHighlight);
            blockPopupsButton.setTooltip(ere.isBlockRepeatedPopups() ? new Tooltip("Popups Blocked") : new Tooltip("Popups Allowed"));
        });

        final ImageView crossImageHighlight = new ImageView(UserInterfaceIconProvider.CROSS.buildImage(13, Color.LIGHT_GRAY));
        final Button dismissButton = new Button("");
        dismissButton.setStyle("-fx-background-color: #505050; -fx-border-color: #404040");
        dismissButton.setGraphic(crossImageHighlight);
        dismissButton.addEventFilter(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
                            public void handle(final MouseEvent mouseEvent) {
                                LOGGER.info("\n\n -- dismiss button handle mouse event : " + mouseEvent);
                                dismissButton.setStyle("-fx-background-color: #e02828; -fx-border-color: #c01c1c");
                                mouseEvent.consume();
                            }
                        });
        dismissButton.addEventFilter(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
                            public void handle(final MouseEvent mouseEvent) {
                                LOGGER.info("\n\n -- dismiss button handle mouse event : " + mouseEvent);
                                dismissButton.setStyle("-fx-background-color: #505050; -fx-border-color: #404040");
                                mouseEvent.consume();
                            }
                        });
        dismissButton.setPadding(new Insets(2));
        dismissButton.setOnAction((ActionEvent event) -> {
            ErrorReportSessionData.getInstance().removeEntry(ere.getEntryId());
            updateSessionErrorsBox(-1);
        });

        label.setStyle("-fx-text-fill: #c0c0c0;");
        timeLabel.setStyle("-fx-text-fill: #c0c0c0;");
        hBoxLeft.setStyle("-fx-background-color: " + backgroundColour + ";");
        hBoxTitle.setStyle("-fx-background-color: " + backgroundColour + ";");
        hBoxRight.setStyle("-fx-background-color: " + backgroundColour + ";");
        
        counterLabel.setStyle(" -fx-background-color: black; -fx-text-background-color: cyan; -fx-text-fill: " + alertColour + "; -fx-font-weight: bold");
        counterLabel.setTooltip(new Tooltip("Repeated occurrences"));
        hBoxLeft.getChildren().add(timeLabel);
        hBoxLeft.getChildren().add(counterLabel);
        hBoxTitle.getChildren().add(label);
        hBoxLeft.setPadding(new Insets(4,6,1,0));
        hBoxTitle.setPadding(new Insets(4,6,1,0));
        hBoxRight.setPadding(new Insets(1,1,1,6));
        hBoxRight.setSpacing(2);
        hBoxRight.getChildren().add(blockPopupsButton);
        hBoxRight.getChildren().add(dismissButton);
        
        bop.setLeft(hBoxLeft);
        bop.setCenter(hBoxTitle);
        bop.setRight(hBoxRight);
        bop.setPadding(new Insets(0,0,0,0));
        bop.setStyle("-fx-border-color:grey");
        
        bop.prefWidthProperty().bind(scrollPane.widthProperty().subtract(48));

        tp.setGraphic(bop);
        tp.setPadding(new Insets(0,16,0,0));
        
        return tp;
    }
        
}
