/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.views.notes;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.plugins.reporting.GraphReport;
import au.gov.asd.tac.constellation.plugins.reporting.PluginReport;
import au.gov.asd.tac.constellation.plugins.reporting.PluginReportListener;
import java.awt.TextArea;
import java.util.List;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 *
 * @author sol695510
 */
public class NotesViewPane extends BorderPane {

    private final NotesViewController controller;
    private String currentGraphId;
    private GraphReport currentGraphReport;
    private List<PluginReport> pluginReportList;
    //private final PluginReport pluginReport;
    private final VBox MainNotesPane;
    private final VBox UserNotesPane;
    private final VBox AutoNotesPane;
    //private final TextArea testText;

//    Create buttons and panes to show notes
//    TODO tabs to show both Auto and User generated notes
//    TODO get data about plugin use from PluginReporter
    
    public NotesViewPane(final NotesViewController controller) {

        // create controller
        this.controller = controller;
        
        // THIS LINE CAUSES AN ERROR RELATING TO THE CONSTRUCTOR!
        //pluginReportList = currentGraphReport.getPluginReports();
        
        // placeholder label for content example
        final Label userText = new Label("User Notes Here\n");
        final Label autoText = new Label("Auto Notes Here\n");      
//        final Label autoText = new Label(pluginReportList.toString());
        
        this.UserNotesPane = new VBox(5, userText);
        this.AutoNotesPane = new VBox(5, autoText);
        this.MainNotesPane = new VBox(5, UserNotesPane, AutoNotesPane);
        
        // add layers grid and options to pane
//        this.UserNotesPane = new VBox(5, visibleText);

        // create layout bindings
//        MainNotesPane.prefWidthProperty().bind(this.widthProperty());

        this.setCenter(MainNotesPane);
    }

    public NotesViewController getController() {
        return controller;
    }

    void setGraphRecord(String currentGraphId) {
        this.currentGraphReport = new GraphReport(currentGraphId);
        // update ui
    }
}
