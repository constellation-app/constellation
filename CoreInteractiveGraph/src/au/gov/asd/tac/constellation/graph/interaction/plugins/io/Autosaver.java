/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.interaction.plugins.io;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 * Perform autosaves of open graphs.
 *
 * @author algol
 */
public final class Autosaver implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(Autosaver.class.getName());
    
    private static final String AUTOSAVE_THREAD_NAME = "Autosave";
    private static final RequestProcessor REQUEST_PROCESSOR = new RequestProcessor(Autosaver.class);

    private static final Map<String, Long> LAST_AUTOSAVE = new HashMap<>();

    /**
     * Schedule a new autosave.
     * <p>
     * A new task is always scheduled, regardless of whether autosave is enabled
     * or not. It is up to the task to determine whether autosave is enabled or
     * not and do the required work.
     *
     * @param delayMinutes The delay (in minutes) until the next autosave.
     */
    public static void schedule(final int delayMinutes) {
        int dm = delayMinutes;
        if (dm <= 0) {
            final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
            final int autosaveSchedule = prefs.getInt(ApplicationPreferenceKeys.AUTOSAVE_SCHEDULE, ApplicationPreferenceKeys.AUTOSAVE_SCHEDULE_DEFAULT);
            dm = autosaveSchedule;
        }

        REQUEST_PROCESSOR.schedule(new Autosaver(), dm, TimeUnit.MINUTES);
    }

    @Override
    public void run() {
        StatusDisplayer.getDefault().setStatusText(String.format("Auto saving %s at %s...", "", new Date()));

        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final boolean autosaveEnabled = prefs.getBoolean(ApplicationPreferenceKeys.AUTOSAVE_ENABLED, ApplicationPreferenceKeys.AUTOSAVE_ENABLED_DEFAULT);
        if (autosaveEnabled) {
            runAutosave();
        }

        final int autosaveSchedule = prefs.getInt(ApplicationPreferenceKeys.AUTOSAVE_SCHEDULE, ApplicationPreferenceKeys.AUTOSAVE_SCHEDULE_DEFAULT);
        final int delayMinutes = autosaveSchedule;
        schedule(delayMinutes);
    }

    /**
     * Do an autosave on all extant graphs.
     */
    public static void runAutosave() {
        // We want to save graphs sequentially: if we save them concurrently, then CPU will be used serialising them all at once,
        // and I/O bandwidth will be used saving them all at once, which may not be good on a network drive.
        // We need to get off the EDT; since we aren't operating on any particular graph, we'll just use a Thread,
        // rather than relying on the framework.
        new Thread() {
            @Override
            public void run() {
                setName(AUTOSAVE_THREAD_NAME);
                final List<String> graphIds = GraphNode.getGraphIDs();
                for (String id : graphIds) {
                    final Graph graph = GraphNode.getGraph(id);

                    // Get the modification count
                    Long lastAutosaveModificationCounter = LAST_AUTOSAVE.get(id);

                    long newAutosaveModificationCounter;
                    final ReadableGraph rg = graph.getReadableGraph();
                    try {
                        newAutosaveModificationCounter = rg.getGlobalModificationCounter();
                    } finally {
                        rg.release();
                    }

                    if (lastAutosaveModificationCounter == null || lastAutosaveModificationCounter != newAutosaveModificationCounter) {
                        LAST_AUTOSAVE.put(id, newAutosaveModificationCounter);

                        try {
                            PluginExecution.withPlugin(InteractiveGraphPluginRegistry.AUTOSAVE_GRAPH).executeNow(graph);
                        } catch (final InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            LOGGER.log(Level.WARNING, "Autosave was interrupted");
                        } catch (final PluginException ex) {
                            LOGGER.log(Level.WARNING, "Error occurred during autosave");
                        }
                    }
                }
            }
        }.start();
    }
}
