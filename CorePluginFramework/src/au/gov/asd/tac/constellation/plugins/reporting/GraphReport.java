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
package au.gov.asd.tac.constellation.plugins.reporting;

import au.gov.asd.tac.constellation.graph.Graph;
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
    public GraphReport(Graph graph) {
        this(graph.getId());
    }

    /**
     * Creates a new GraphReport for the graph with the specified id.
     *
     * @param graphId the graphId.
     */
    public GraphReport(String graphId) {
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
    public void addNewUndoRedoReport(final UndoRedoReport undoRedoReport) {
        PluginReport p;

        if (undoRedoReport.getGraphId().equals(getGraphId())) {
            //notesViewPane.addNewUndoRedoReport(undoRedoReport, activeGraph);
            if (undoRedoReport.getActionType() == "Undo") {
                p = getMatchingPluginReport(undoRedoReport);
                p.setUndone(true);
            } else { //if (undoRedoReport.getActionType()=="Redo"){
                 p = getMatchingUndonePluginReport(undoRedoReport);
                p.setUndone(false);
            }
            //GraphReportManager.fireNewPluginReport(p);
        }
    }

    private PluginReport getMatchingPluginReport(final UndoRedoReport undoRedoReport) {

        PluginReport n2 = getPluginReports().stream()
                .filter(entry -> undoRedoReport.getActionDescription().equals(entry.getPluginName()) && !entry.getUndone())
                .max(Comparator.comparing(PluginReport::getStartTime)).get();

        return n2;

    }

    private PluginReport getMatchingUndonePluginReport(final UndoRedoReport undoRedoReport) {

        PluginReport n2 = getPluginReports().stream()
                .filter(entry -> undoRedoReport.getActionDescription().equals(entry.getPluginName()) && entry.getUndone())
                .max(Comparator.comparing(PluginReport::getStartTime)).get();

        return n2;

    }
}
