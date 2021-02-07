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
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import au.gov.asd.tac.constellation.views.notes.state.NotesViewConcept;
import au.gov.asd.tac.constellation.views.notes.state.NotesViewEntry;
import au.gov.asd.tac.constellation.views.notes.state.NotesViewState;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Handles reading and writing to the state to save notes to the graph.
 *
 * @author sol695510
 */
public class NotesViewController {

    private final NotesViewTopComponent parent;

    private static final String NOTES_ADD_ATTRIBUTE = "Notes View: Add Required Attributes";
    private static final String NOTES_READ_STATE = "Notes View: Read State";
    private static final String NOTES_WRITE_STATE = "Notes View: Write State";

    private final ScheduledExecutorService scheduledExecutorService;
    private ScheduledFuture<?> scheduledFuture;
    private static List<Future<?>> futures = new ArrayList();
    private static final Logger LOGGER = Logger.getLogger(NotesViewController.class.getName());

    public NotesViewController(final NotesViewTopComponent parent) {
        this.parent = parent;
        this.scheduledExecutorService = Executors.newScheduledThreadPool(1);
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
     * Add attributes required by the Notes View for it to function.
     */
    public void addAttributes(final Graph graph) {
        final int notesViewState;

        if (graph != null) {
            try (ReadableGraph readableGraph = graph.getReadableGraph()) {
                notesViewState = NotesViewConcept.MetaAttribute.NOTES_VIEW_STATE.get(readableGraph);
            }

            if (notesViewState == Graph.NOT_FOUND) {
                PluginExecution.withPlugin(new SimpleEditPlugin(NOTES_ADD_ATTRIBUTE) {
                    @Override
                    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
                        NotesViewConcept.MetaAttribute.NOTES_VIEW_STATE.ensure(graph);
                    }
                }).executeLater(graph);
            }
        }
    }

    /**
     * Reads the graph's NOTES_VIEW_STATE attribute and populates the Notes View
     * pane.
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
     */
    public void writeState(final String caller) {
        LOGGER.info(caller);
        final NotesViewPane pane = parent.getContent();
        final Graph graph = GraphManager.getDefault().getActiveGraph();

        if (pane == null || graph == null) {
            return;
        }

        Future<?> executeLater = PluginExecution.withPlugin(new NotesViewStateWriter(pane.getNotes(), pane.getFilters())).executeLater(graph);
//        final Thread thread = new Thread("Table View: Update Table") {
//            @Override
//            public void run() {
//                if (scheduledFuture != null) {
//                    for (Future<?> future : futures) {
//                        future.cancel(true);
//                    }
//                    scheduledFuture.cancel(true);
//                }
//
//                scheduledFuture = scheduledExecutorService.schedule(() -> {
//                    try{
//                    Future<?> executeLater = PluginExecution.withPlugin(new NotesViewStateWriter(pane.getNotes(), pane.getFilters())).executeLater(graph);
//                    futures.add(executeLater);
//                    } catch(Exception ex){
//                        LOGGER.info("@@@ Exception Thrown");
//                    }
//                }, 0, TimeUnit.MILLISECONDS);
//            }
//        };
//        thread.start();

    }

    /**
     * Read the current state from the graph.
     */
    @PluginInfo(tags = {"LOW LEVEL"})
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
    @PluginInfo(tags = {"LOW LEVEL"})
    private static final class NotesViewStateWriter extends SimpleEditPlugin {

        private final List<NotesViewEntry> notes;
        private final List<String> filters;

        public NotesViewStateWriter(final List<NotesViewEntry> notes, final List<String> filters) {
            this.notes = notes;
            this.filters = filters;
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
