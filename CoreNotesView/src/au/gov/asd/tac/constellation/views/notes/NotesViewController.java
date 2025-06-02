/*
 * Copyright 2010-2025 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import au.gov.asd.tac.constellation.views.notes.state.NotesViewConcept;
import au.gov.asd.tac.constellation.views.notes.state.NotesViewEntry;
import au.gov.asd.tac.constellation.views.notes.state.NotesViewState;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles reading and writing to the state to save notes to the graph.
 *
 * @author sol695510
 */
public class NotesViewController {

    private final NotesViewTopComponent parent;

    private static final String NOTES_READ_STATE = "Notes View: Read State";
    private static final String NOTES_WRITE_STATE = "Notes View: Write State";

    public NotesViewController(final NotesViewTopComponent parent) {
        this.parent = parent;
    }

    public String getReadStateText() {
        return NOTES_READ_STATE;
    }

    public String getWriteStateText() {
        return NOTES_WRITE_STATE;
    }

    /**
     * Reads the graph's NOTES_VIEW_STATE attribute and populates the Notes View
     * pane.
     *
     * @param graph The Graph to read the state from
     */
    public void readState(final Graph graph) {
        final NotesViewPane pane = parent.getContent();
        if (pane == null || graph == null) {
            return;
        }

        PluginExecution.withPlugin(new NotesViewStateReader(pane)).executeLater(graph);
    }

    /**
     * Executes a plugin to write the current notes to the graph's
     * NOTES_VIEW_STATE attribute.
     *
     * @param graph The Graph to write the state to
     */
    public void writeState(final Graph graph) {
        final NotesViewPane pane = parent.getContent();
        if (pane == null || graph == null) {
            return;
        }

        PluginExecution.withPlugin(new NotesViewStateWriter(pane.getNotes(), pane.getFilters(), pane.getTagsFilters())).executeLater(graph);

    }

    /**
     * Read the current state from the graph.
     */
    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.LOW_LEVEL})
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
                //Try to populate autonotes from the graph report for new graphs where the state is not saved yet
                pane.setGraphReport();
                return;
            }

            final NotesViewState currentState = graph.getObjectValue(notesViewStateAttributeId, 0);
            if (currentState == null || pane == null) {
                return;
            }

            // TODO: A copy of the list is being passed here instead because
            // the Notes View is reading and writing state all the time.
            // Review why the notes view needs to be read and written to so
            // often. If this can be reduced significantly then the chance of
            // reading and writing happening at the same time is reduced and we
            // can simply pass by reference again
            pane.setNotes(new ArrayList<>(currentState.getNotes()));
            pane.setFilters(currentState.getFilters());
        }

        @Override
        public String getName() {
            return NOTES_READ_STATE;
        }
    }

    /**
     * Write the current state to the graph.
     */
    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.LOW_LEVEL})
    private static final class NotesViewStateWriter extends SimpleEditPlugin {

        private final List<NotesViewEntry> notes;
        private final List<String> filters;
        private final List<String> tagsFilters;

        public NotesViewStateWriter(final List<NotesViewEntry> notes, final List<String> filters, final List<String> tagsFilters) {
            this.notes = notes;
            this.filters = filters;
            this.tagsFilters = tagsFilters;
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
            newState.setFilters(filters);
            newState.setTagsFilters(tagsFilters);

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

    public NotesViewTopComponent getParent() {
        return parent;
    }

}
