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
package au.gov.asd.tac.constellation.plugins.reporting;

import au.gov.asd.tac.constellation.graph.Graph;
import static au.gov.asd.tac.constellation.graph.locking.LockingManager.UNDO;
import au.gov.asd.tac.constellation.graph.reporting.UndoRedoReport;
import au.gov.asd.tac.constellation.graph.reporting.UndoRedoReportListener;
import au.gov.asd.tac.constellation.graph.reporting.UndoRedoReportManager;
import au.gov.asd.tac.constellation.plugins.Plugin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A GraphReport holds all plugin reports for a particular graph. All
 * {@link PluginReport}s for a given graph are stored in an ordered list, sorted
 * on the datetime on which the plugin was run.
 *
 * @author sirius
 */
public class GraphReport implements UndoRedoReportListener {

    public final String graphId;

    // A list holding all the plugin reports for this graph
    // Plus an unmodifiable version to provide to clients
    private final List<PluginReport> pluginReports = Collections.synchronizedList(new ArrayList<>());
    private final List<PluginReport> uPluginReports = Collections.unmodifiableList(pluginReports);

    // A set holding all the plugin tags that should be visible in the UI
    // A plugin will be visible in the UI if is has at least one of these tags
    // Plus an unmodifiable version to provide to clients
    private final Set<String> tags = new TreeSet<>();
    private final Set<String> uTags = Collections.unmodifiableSet(tags);

    /**
     * Creates a new GraphReport for the specified graph.
     *
     * @param graph the graph
     */
    public GraphReport(final Graph graph) {
        this(graph.getId());
    }

    /**
     * Creates a new GraphReport for the graph with the specified id.
     *
     * @param graphId the graphId.
     */
    public GraphReport(final String graphId) {
        this.graphId = graphId;
        UndoRedoReportManager.addUndoRedoReportListener(this);
    }

    /**
     * Returns the graphId of the graph associated with this GraphReport.
     *
     * @return the graphId of the graph associated with this GraphReport.
     */
    public String getGraphId() {
        return graphId;
    }

    /**
     * Add a new plugin report, possibly causing it to be recorded and listeners
     * notified.
     * <p>
     * If the tags on the plugin cause this report to be ignored, it will not be
     * recorded, and listeners will not be notified.
     *
     * @param plugin A plugin for which a PluginReport will be created.
     *
     * @return A new PluginReport.
     */
    public PluginReport addPluginReport(Plugin plugin) {
        final PluginReport pluginReport = new PluginReport(this, plugin);
        pluginReports.add(pluginReport);
        GraphReportManager.fireNewPluginReport(pluginReport);

        return pluginReport;
    }

    /**
     * Adds all the tags of the specified {@link PluginReport} to the set of
     * tags recognized by this GraphReport.
     *
     * @param pluginReport the specified PluginReport.
     * @see GraphReport#getUTags()
     */
    public void includeTags(PluginReport pluginReport) {
        tags.addAll(Arrays.asList(pluginReport.getTags()));
    }

    /**
     * Returns an unmodifiable list of all {@link PluginReport}s held by this
     * GraphReport sorted by the datetime the plugin was executed.
     *
     * @return an unmodifiable list of all {@link PluginReport}s held by this
     * GraphReport sorted by the datetime the plugin was executed.
     */
    public List<PluginReport> getPluginReports() {
        return uPluginReports;
    }

    /**
     * Returns the union of all tags seen on any {@link PluginReport} sent to
     * this GraphReport.
     *
     * @return the union of all tags seen on any {@link PluginReport} sent to
     * this GraphReport.
     */
    public Set<String> getUTags() {
        return uTags;
    }

    /**
     * Triggers when an UndoRedoReport is added. Update the relevant plugin
     * report.
     *
     * @param undoRedoReport
     */
    @Override
    public void fireNewUndoRedoReport(final UndoRedoReport undoRedoReport) {
        if (hasMatchingPluginReport(undoRedoReport)) {
            getMatchingPluginReport(undoRedoReport).setUndone(undoRedoReport.getActionType() == UNDO);
        }
    }

    private PluginReport getMatchingPluginReport(final UndoRedoReport undoRedoReport) {
        if (undoRedoReport.getActionType() == UNDO) {
            return getPluginReports().stream()
                    .filter(entry -> undoRedoReport.getActionDescription().equals(entry.getPluginName())
                    && !entry.isUndone() && entry.getGraphReport().getGraphId().equals(undoRedoReport.getGraphId()))
                    .max(Comparator.comparing(PluginReport::getStartTime)).get();

        } else {
            return getPluginReports().stream()
                    .filter(entry -> undoRedoReport.getActionDescription().equals(entry.getPluginName())
                    && entry.isUndone() && entry.getGraphReport().getGraphId().equals(undoRedoReport.getGraphId()))
                    .min(Comparator.comparing(PluginReport::getStartTime)).get();
        }
    }

        /**
     * Check if the action in the UndoRedoReport has a matching PluginReport already
     * added for the original execution category. This is to ignore the undo redo reports
     * for activities on the graph that are not run by a plugin such as Drag, Zoom.
     *
     * @param undoRedoReport The UndoRedoReport to check.
     *
     * @return True if there's any PluginReport with the action in the UndoRedoReport
     */
    private boolean hasMatchingPluginReport(final UndoRedoReport undoRedoReport) {
        return getPluginReports().stream().anyMatch(entry
                -> undoRedoReport.getActionDescription().equals(entry.getPluginName())
                && entry.getGraphReport().getGraphId().equals(undoRedoReport.getGraphId()));
    }
}
