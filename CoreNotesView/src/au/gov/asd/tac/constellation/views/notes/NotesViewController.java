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
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import au.gov.asd.tac.constellation.views.notes.state.NotesViewConcept;
import au.gov.asd.tac.constellation.views.notes.state.NotesViewEntry;
import au.gov.asd.tac.constellation.views.notes.state.NotesViewState;
import java.util.List;

/**
 *
 * @author sol695510
 */
public class NotesViewController {

    private final NotesViewTopComponent parent;

    private static final String NOTES_ADD_ATTRIBUTE = "Notes View: Add Required Attributes";
    private static final String NOTES_READ_STATE = "Notes View: Read State";
    private static final String NOTES_WRITE_STATE = "Notes View: Write State";

    public NotesViewController(final NotesViewTopComponent parent) {
        this.parent = parent;
    }

    public String getAddAttributeText() {
        return NOTES_ADD_ATTRIBUTE;
    }

    public String getReadStateText() {
        return NOTES_READ_STATE;
    }

    public String getWriteStateText() {
        return NOTES_WRITE_STATE;
    }

    /**
     * Add attributes required by the Notes View for it to function
     */
    public void addAttributes() {

        final Graph activeGraph = GraphManager.getDefault().getActiveGraph();

        if (activeGraph != null) {
            PluginExecution.withPlugin(new SimpleEditPlugin(NOTES_ADD_ATTRIBUTE) {
                @Override
                public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
                    NotesViewConcept.MetaAttribute.NOTES_VIEW_STATE.ensure(graph);
                }
            }).executeLater(activeGraph);
        }
    }

    /**
     * Reads the graph's notes_view_state attribute and populates the Notes View
     * pane.
     */
    public void readState() {

        final NotesViewPane pane = parent.getContent();
        final Graph graph = GraphManager.getDefault().getActiveGraph();

        if (pane == null || graph == null) {
            return;
        }

        PluginExecution.withPlugin(new NotesViewStateReader(pane)).executeLater(graph);
    }

    /**
     * Executes a plugin to write the current notes to the graph's
     * notes_view_state Attribute.
     */
    public void writeState() {

        final NotesViewPane pane = parent.getContent();
        final Graph graph = GraphManager.getDefault().getActiveGraph();

        if (pane == null || graph == null) {
            return;
        }

        PluginExecution.withPlugin(new NotesViewStateWriter(pane.getNotes())).executeLater(graph);
    }

    /**
     * Read the current state from the graph.
     */
    public static final class NotesViewStateReader extends SimpleReadPlugin {

        private final NotesViewPane pane;

        public NotesViewStateReader(final NotesViewPane pane) {
            this.pane = pane;
        }

        @Override
        public void read(final GraphReadMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {

            if (graph == null) {
                return;
            }

            final int notesViewStateAttributeId = NotesViewConcept.MetaAttribute.NOTES_VIEW_STATE.get(graph);
            if (notesViewStateAttributeId == Graph.NOT_FOUND) {
                return;
            }

            final NotesViewState currentState = graph.getObjectValue(notesViewStateAttributeId, 0);
            if (currentState == null || pane == null) {
                return;
            }

            pane.setNotes(currentState.getNotes());
        }

        @Override
        public String getName() {
            return NOTES_READ_STATE;
        }
    }

    /**
     * Write the current state to the graph.
     */
    private static final class NotesViewStateWriter extends SimpleEditPlugin {

        private final List<NotesViewEntry> notes;

        public NotesViewStateWriter(final List<NotesViewEntry> notes) {
            this.notes = notes;
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            if (graph == null) {
                return;
            }

            final int stateAttributeId = NotesViewConcept.MetaAttribute.NOTES_VIEW_STATE.ensure(graph);
            final NotesViewState currentState = graph.getObjectValue(stateAttributeId, 0);

            final NotesViewState newState = currentState == null ? new NotesViewState() : new NotesViewState(currentState);
            newState.setNotes(notes);

            graph.setObjectValue(stateAttributeId, 0, newState);
        }

        @Override
        protected boolean isSignificant() {
            return false;
        }

        @Override
        public String getName() {
            return NOTES_WRITE_STATE;
        }
    }
}
