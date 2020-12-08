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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.reporting.GraphReportListener;
import au.gov.asd.tac.constellation.plugins.reporting.GraphReportManager;
import au.gov.asd.tac.constellation.plugins.reporting.PluginReport;
import au.gov.asd.tac.constellation.views.JavaFxTopComponent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * UI component associated with the Notes View.
 * 
 * @author sol695510
 */
@TopComponent.Description(
        preferredID = "NotesViewTopComponent",
        iconBase = "au/gov/asd/tac/constellation/views/notes/resources/notes-view.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(
        mode = "explorer",
        openAtStartup = false)
@ActionID(
        category = "Window",
        id = "au.gov.asd.tac.constellation.views.notes.NotesViewTopComponent")
@ActionReferences({
    @ActionReference(path = "Menu/Experimental/Views", position = 500),
    @ActionReference(path = "Shortcuts", name = "CS-N")})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_NotesViewAction",
        preferredID = "NotesViewTopComponent")
@Messages({
    "CTL_NotesViewAction=Notes View",
    "CTL_NotesViewTopComponent=Notes View",
    "HINT_NotesViewTopComponent=Notes View"})

public class NotesViewTopComponent extends JavaFxTopComponent<NotesViewPane> implements GraphReportListener {
    
    private final NotesViewController notesViewController;
    private final NotesViewPane notesViewPane;

    /**
     * Creates a new NotesViewTopComponent.
     */
    public NotesViewTopComponent() {
        
        setName(Bundle.CTL_NotesViewTopComponent());
        setToolTipText(Bundle.HINT_NotesViewTopComponent());
        
        initComponents();
        
        notesViewController = new NotesViewController(this);
        notesViewPane = new NotesViewPane(notesViewController);
        
        initContent();
    }
    
    @Override
    protected void handleNewGraph(final Graph graph) {

        if (needsUpdate() && graph != null) {
            notesViewPane.selectAllFilters();
            notesViewPane.clearNotes();
            notesViewPane.prepareNotesViewPane(notesViewController);
        }
    }

    @Override
    protected void handleGraphOpened(final Graph graph) {

        if (needsUpdate() && graph != null) {
            notesViewPane.selectAllFilters();
            notesViewPane.clearNotes();
            notesViewPane.prepareNotesViewPane(notesViewController);
        }
    }

    @Override
    protected void handleGraphClosed(final Graph graph) {

        if (needsUpdate() && graph != null) {
            notesViewPane.clearNotes();
            notesViewPane.prepareNotesViewPane(notesViewController);
            notesViewPane.closeEdit();
        }
    }

    @Override
    protected void handleComponentOpened() {
        
        GraphReportManager.addGraphReportListener(this);
        Graph activeGraph = GraphManager.getDefault().getActiveGraph();
        
        if (activeGraph != null) {
            notesViewPane.clearNotes();
            notesViewPane.prepareNotesViewPane(notesViewController);
            // Ensures plugin reports created while the Notes View is not "open" will appear when it is opened later.
            notesViewPane.setGraphReport(notesViewController);
        }
    }

    @Override
    protected void handleComponentClosed() {
        
        GraphReportManager.removeGraphReportListener(this);
        notesViewPane.clearNotes();
        notesViewPane.prepareNotesViewPane(notesViewController);
        notesViewPane.closeEdit();
    }

    @Override
    protected String createStyle() {
        return "resources/notes-view.css";
    }

    @Override
    protected NotesViewPane createContent() {
        return notesViewPane;
    }

    // Triggers when plugin reports are added or removed.
    @Override
    public void newPluginReport(PluginReport pluginReport) {
        // Omit plugin reports from the Notes View and Quality Control View.
        if (!pluginReport.getPluginName().contains("Notes View")) {
            if (!pluginReport.getPluginName().contains("Quality Control View")) {
                notesViewPane.prepareNotesViewPane(notesViewController);
                notesViewPane.setGraphReport(notesViewController);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        setLayout(new java.awt.BorderLayout());

    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
