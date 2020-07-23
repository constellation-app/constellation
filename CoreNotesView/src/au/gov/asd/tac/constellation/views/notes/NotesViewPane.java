/*
 * Copyright 2010-2020 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.plugins.reporting.GraphReport;
import au.gov.asd.tac.constellation.plugins.reporting.PluginReport;
import java.util.List;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
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
