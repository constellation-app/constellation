/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.views.notes;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author sol695510
 */
public class NotesViewPane extends BorderPane {

    private final NotesViewController controller;
    private final VBox layersViewPane;

    public NotesViewPane(final NotesViewController controller) {

        // create controller
        this.controller = controller;

        // placeholder label for content example
        final Label visibleText = new Label("This should be visible");

        // add layers grid and options to pane
        this.layersViewPane = new VBox(5, visibleText);

        // create layout bindings
        layersViewPane.prefWidthProperty().bind(this.widthProperty());

        this.setCenter(layersViewPane);
    }

    public NotesViewController getController() {
        return controller;
    }

}
