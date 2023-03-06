/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.views.errorreport;

import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
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
    private ScrollPane errorScrollPane = new ScrollPane();
    static VBox sessionErrorsBox = new VBox();
    private ComboBox<String> popupControl;

    //private static ErrorReportTopComponent instance = null;
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
                            if (latestRetrievalDate == null || latestRetrievalDate.before(ErrorReportSessionData.lastUpdate)) {
                                latestRetrievalDate = new Date();
                                flashErrorIcon(true);
                                ErrorReportDialogManager.getInstance().updatePopupMode(getPopupControlValue());
                                updateSessionErrorsBox(-1);    
                            }
                        }
                    }                    
                });
            }            
        };
        
        refreshTimer = new Timer();
        refreshTimer.schedule(refreshAction, 10000, 400);
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

        
        ToolBar controlToolbar = new ToolBar();
        controlToolbar.getItems().addAll(popupLabel, popupControl,clearButton);
        componentBox.getChildren().add(controlToolbar);
        
        erbp = new BorderPane();
        erbp.setStyle("-fx-text-fill: purple; -fx-text-background-color: blue; -fx-background-color: black;");
        sessionErrorsBox.setPadding(new Insets(1,1,1,1));
        sessionErrorsBox.setSpacing(2);
        
        erbp.addEventFilter(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
                            public void handle(final MouseEvent mouseEvent) {
                                LOGGER.info("\n\n -- handle mouse event : " + mouseEvent);
                                flashErrorIcon(false);
                                mouseEvent.consume();
                            }
                        });
        
        erbp.setTop(sessionErrorsBox);
        errorScrollPane.setContent(erbp);
        errorScrollPane.setFitToHeight(true);
        errorScrollPane.setFitToWidth(true);
        errorScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        errorScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        componentBox.getChildren().add(errorScrollPane);
        outerPane.setTop(componentBox);
        
        return outerPane;
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
                sessionErrors.get(i).setExpanded(((TitledPane)sessionErrorsBox.getChildren().get(i)).isExpanded());
            }      
        }

        if (sessionErrors.isEmpty()) {
            sessionErrors = ErrorReportSessionData.getInstance().getSessionErrorsCopy();
        } else {
            List<ErrorReportEntry> oldSessionErrors = new ArrayList<>();
            //oldSessionErrors.addAll(sessionErrors);
            for (ErrorReportEntry testEntry: sessionErrors) {
                LOGGER.info("\n -- REFRESH : add old entry : " + testEntry);
                oldSessionErrors.add(testEntry);
            }
            sessionErrors = ErrorReportSessionData.getInstance().getSessionErrorsCopy();

            for (ErrorReportEntry testEntry: sessionErrors) {
                LOGGER.info("\n -- REFRESH : active entry : " + testEntry);
            }
            // update local data with new incoming data
            for (ErrorReportEntry oldEntry: oldSessionErrors) {
                ErrorReportEntry activeEntry = findActiveEntryWithId(oldEntry.getEntryId());
                if (activeEntry != null) {
                    // update active entry with old entry's data
                    activeEntry.setExpanded(oldEntry.getExpanded());
                    if (oldEntry.getLastPopupDate() != null) {
                        activeEntry.setLastPopupDate(new Date(oldEntry.getLastPopupDate().getTime()));
                    }
                    LOGGER.info("\n -- REFRESH : active entry updated : " + activeEntry);
                }
            }            
        }
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
    
    void generateSampleErrorMessages(){
        String errorMessage = "ABC Exception on line 007 ...\nat somewhere.someclass.method(line 007)\nat somewhereelse.anotherclass.badmethod(line 666)";
        ErrorReportEntry rep1 = new ErrorReportEntry("test1", errorMessage, 99999);
        String errorMessage2 = "ABC Exception on line 007b ...\nat somewhere.someclass.method(line 007)\nat somewhereelse.anotherclass.badmethod(line 666)";
        ErrorReportEntry rep2 = new ErrorReportEntry("test2", errorMessage2, 99998);
        String errorMessage3 = "ABC Exception on line 007c ...\nat somewhere.someclass.method(line 007)\nat somewhereelse.anotherclass.badmethod(line 666)";
        ErrorReportEntry rep3 = new ErrorReportEntry("test3", errorMessage3, 99997);
        String errorMessage4 = "ABC Exception on line 007b ...\nat somewhere.someclass.method(line 007)\nat somewhereelse.anotherclass.badmethod(line 666)";
        ErrorReportEntry rep4 = new ErrorReportEntry("test4", errorMessage4, 99996);
        
        ErrorReportSessionData.getInstance().storeSessionError(rep1);
        ErrorReportSessionData.getInstance().storeSessionError(rep2);
        ErrorReportSessionData.getInstance().storeSessionError(rep3);
        ErrorReportSessionData.getInstance().storeSessionError(rep4);        
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
                    int countDown = 10;
                    requestAttention(true);
                    while (isIconFlashing()) {                            
                        try {
                            countDown--;
                            if (countDown <= 0) {
                                countDown = 10;
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
        String alertColour = "#d0d0d0";
        if (ere.getOccurrences() > 999) {
            alertColour = "#d06060";
        } else if (ere.getOccurrences() > 99) {
            alertColour = "#e07878";
        } else if (ere.getOccurrences() > 9) {
            alertColour = "#f09090";
        } else if (ere.getOccurrences() > 1) {
            alertColour = "#f0b0b0";
        }

        BorderPane bop = new BorderPane();
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(1,1,1,1));
        TextArea data = new TextArea(ere.getErrorData());
        data.setStyle("-fx-text-fill: #c0c0c0; -fx-text-background-color: brown; -fx-background-color: #505050;");
        data.setEditable(false);
        vBox.getChildren().add(data);
        
        tp.setText("");      
        tp.setContent(vBox);    
        tp.setExpanded(ere.getExpanded());           
        HBox hBoxTitle = new HBox();
        HBox hBoxLeft = new HBox();
        Label timeLabel = new Label(ere.getTimeText());
        Label label = new Label(ere.getHeading());
        label.setTooltip(new Tooltip(label.getText()));
        Label counterLabel = new Label(" " + Integer.toString(ere.getOccurrences()) + "x ");
        
        GridPane.setHgrow(label, Priority.ALWAYS);
        
        //final Color deepRed = new Color(220,40,40);
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
        dismissButton.setPrefSize(14, 14);
        dismissButton.setMaxSize(14, 14);
        dismissButton.setOnAction((ActionEvent event) -> {
            ErrorReportSessionData.getInstance().removeEntry(ere.getEntryId());
            updateSessionErrorsBox(-1);
        });

        label.setStyle("-fx-text-fill: " + alertColour + ";");
        timeLabel.setStyle("-fx-text-fill: " + alertColour + ";");
        counterLabel.setStyle(" -fx-background-color: " + alertColour + "; -fx-text-background-color: cyan; -fx-text-fill: black;");
        counterLabel.setTooltip(new Tooltip("Repeated occurrences"));
        hBoxLeft.getChildren().add(timeLabel);
        hBoxLeft.getChildren().add(counterLabel);
        hBoxTitle.getChildren().add(label);
        hBoxLeft.setPadding(new Insets(4,6,0,0));
        hBoxTitle.setPadding(new Insets(4,6,0,0));

        bop.setLeft(hBoxLeft);
        bop.setCenter(hBoxTitle);
        bop.setRight(dismissButton);
        bop.setPadding(new Insets(0,0,1,0));
        bop.setStyle("-fx-border-color:grey");
        
        bop.prefWidthProperty().bind(errorScrollPane.widthProperty().subtract(36));

        tp.setGraphic(bop);
        tp.setPadding(new Insets(0));
        
        return tp;
    }
        
}
