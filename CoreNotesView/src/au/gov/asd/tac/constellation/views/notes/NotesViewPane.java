/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.views.notes;

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
    private final HBox MainNotesPane;
    private final VBox UserNotesPane;
    private final VBox AutoNotesPane;

//    Create buttons and panes to show notes
//    TODO tabs to show both Auto and User generated notes
//    TODO get data about plugin use from PluginReporter
    
    public NotesViewPane(final NotesViewController controller) {

        // create controller
        this.controller = controller;

        // placeholder label for content example
        final Label userText = new Label("User Notes Here");
        final Label autoText = new Label("Auto Notes Here");
        
        this.UserNotesPane = new VBox(5, userText);
        this.AutoNotesPane = new VBox(5, autoText);
        this.MainNotesPane = new HBox(5, UserNotesPane, AutoNotesPane);
        
        // add layers grid and options to pane
//        this.UserNotesPane = new VBox(5, visibleText);

        // create layout bindings
//        MainNotesPane.prefWidthProperty().bind(this.widthProperty());

        this.setCenter(MainNotesPane);
    }

    public NotesViewController getController() {
        return controller;
    }

}
