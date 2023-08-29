/*
 * Copyright 2010-2021 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.reporting.UndoRedoReport;
import au.gov.asd.tac.constellation.graph.reporting.UndoRedoReportListener;
import au.gov.asd.tac.constellation.graph.reporting.UndoRedoReportManager;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.reporting.GraphReportListener;
import au.gov.asd.tac.constellation.plugins.reporting.GraphReportManager;
import au.gov.asd.tac.constellation.plugins.reporting.PluginReport;
import au.gov.asd.tac.constellation.views.JavaFxTopComponent;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    @ActionReference(path = "Menu/Views", position = 900),
    @ActionReference(path = "Shortcuts", name = "CS-A")})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_NotesViewAction",
        preferredID = "NotesViewTopComponent")
@Messages({
    "CTL_NotesViewAction=Notes View",
    "CTL_NotesViewTopComponent=Notes View",
    "HINT_NotesViewTopComponent=Notes View"})
public class NotesViewTopComponent extends JavaFxTopComponent<NotesViewPane> implements GraphReportListener, UndoRedoReportListener {
    private final NotesViewController notesViewController;
    private final NotesViewPane notesViewPane;
    private static final Logger LOGGER = Logger.getLogger(NotesViewTopComponent.class.getName());


    /**
     * NotesViewTopComponent constructor.
     */
    public NotesViewTopComponent() {
        setName(Bundle.CTL_NotesViewTopComponent());
        setToolTipText(Bundle.HINT_NotesViewTopComponent());

        initComponents();

        notesViewController = new NotesViewController(this);
        notesViewPane = new NotesViewPane(notesViewController);

        initContent();

        addAttributeValueChangeHandler(VisualConcept.VertexAttribute.SELECTED, graph -> {
            if (!needsUpdate()) {
                return;
            }
            notesViewPane.updateNotesUI();
        });

        addAttributeValueChangeHandler(VisualConcept.TransactionAttribute.SELECTED, graph -> {
            if (!needsUpdate()) {
                return;
            }
            notesViewPane.updateNotesUI();
        });
    }

    @Override
    protected void handleNewGraph(final Graph graph) {
        if (needsUpdate() && graph != null) {
            notesViewPane.clearNotes();
            notesViewPane.getCreateNewNoteButton().setDisable(false);
            notesViewController.readState(graph);
        }

        if (graph == null) {
            notesViewPane.getCreateNewNoteButton().setDisable(true);
        }

        LOGGER.log(Level.SEVERE, "Handling new graph");
    }


    @Override
    protected void handleGraphClosed(final Graph graph) {
        if (needsUpdate() && graph != null) {
            notesViewPane.clearNotes();
        }
        populateNotes();

        LOGGER.log(Level.SEVERE, "Handling graph closed");
    }

    @Override
    protected void handleComponentOpened() {
        super.handleComponentOpened();
        /**
         * listener is not removed so that plugin reports created when the Notes
         * View is not open will render when it is opened later.
         */
        GraphReportManager.addGraphReportListener(this);
        UndoRedoReportManager.addUndoRedoReportListener(this);
        LOGGER.log(Level.SEVERE, "Handling Component opened");
    }

    private void populateNotes() {
        final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
        if (activeGraph != null) {
            notesViewController.readState(activeGraph);
            notesViewPane.getCreateNewNoteButton().setDisable(false);
        } else {
            notesViewPane.getCreateNewNoteButton().setDisable(true);
        }
    }

    @Override
    protected void handleComponentClosed() {
        super.handleComponentClosed();
        LOGGER.log(Level.SEVERE, "Handling component closed");
    }

    @Override
    protected NotesViewPane createContent() {
        return notesViewPane;
    }

    @Override
    protected String createStyle() {
        return "resources/TimeFilter.css";
    }

    /**
     * Triggers when plugin reports are added or removed.
     *
     * @param pluginReport
     */
    @Override
    public void newPluginReport(final PluginReport pluginReport) {
        final Graph activeGraph = GraphManager.getDefault().getActiveGraph();

        // update the graph report if the new plugin report isn't a low level plugin (which aren't useful as notes)
        if (activeGraph != null && pluginReport.getGraphReport().getGraphId().equals(activeGraph.getId())
                && !pluginReport.hasLowLevelTag()) {
            notesViewPane.setGraphReport(activeGraph);
        }
    }

    /**
     * Triggers when an UndoRedoReport is added.
     *
     * @param undoRedoReport
     */
    @Override
    public void fireNewUndoRedoReport(final UndoRedoReport undoRedoReport) {
        final Graph activeGraph = GraphManager.getDefault().getActiveGraph();

        if (activeGraph != null && undoRedoReport.getGraphId().equals(activeGraph.getId())) {
            notesViewPane.processNewUndoRedoReport(undoRedoReport);
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
