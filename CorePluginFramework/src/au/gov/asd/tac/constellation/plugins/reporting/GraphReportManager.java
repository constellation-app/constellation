/*
 * Copyright 2010-2019 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.manager.GraphManagerListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author sirius
 */
public class GraphReportManager {

    private static final String IGNORED_TAGS_KEY = "reportManagerIgnoredTags";

    private static final List<GraphReportListener> LISTENERS = new ArrayList<>();

    private static final Map<String, GraphReport> GRAPH_REPORTS = Collections.synchronizedMap(new HashMap<>());

    /**
     * Tags that will be ignored (not recorded) by GraphReport instances.
     */
    private static final Set<String> IGNORED_TAGS;

    static {
        // Determine the ignored tags *before* we set up the graph reports.
        final Preferences prefs = NbPreferences.forModule(GraphReportManager.class);
        final String filteredTagString = prefs.get(IGNORED_TAGS_KEY, "LOW LEVEL").trim();
        IGNORED_TAGS = new HashSet<>();
        if (!filteredTagString.isEmpty()) {
            IGNORED_TAGS.addAll(Arrays.asList(filteredTagString.split("\t", 0)));
        }

        GraphManager.getDefault().addGraphManagerListener(new GraphManagerListener() {

            @Override
            public void graphOpened(Graph graph) {
                GRAPH_REPORTS.put(graph.getId(), new GraphReport(graph));
            }

            @Override
            public void graphClosed(Graph graph) {
                GRAPH_REPORTS.remove(graph.getId());
            }

            @Override
            public void newActiveGraph(Graph graph) {
            }
        });

        for (Graph graph : GraphManager.getDefault().getAllGraphs().values()) {
            GRAPH_REPORTS.put(graph.getId(), new GraphReport(graph));
        }
    }

    public static synchronized void addGraphReportListener(GraphReportListener listener) {
        if (listener != null && !LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    public static synchronized void removeGraphReportListener(GraphReportListener listener) {
        LISTENERS.remove(listener);
    }

    static synchronized void fireNewPluginReport(PluginReport pluginReport) {
        LISTENERS.stream().forEach((listener) -> {
            listener.newPluginReport(pluginReport);
        });
    }

    public static GraphReport getGraphReport(String graphId) {
        return GRAPH_REPORTS.get(graphId);
    }

    /**
     * Set the tags that will be ignored (not recorded) by GraphReport. A
     * {@link PluginReport} will be ignored if all of its tags are present in
     * the this collection of ignored tags. If any of a {@link PluginReport}s
     * tags are not in this list, it will not be ignored. When a
     * {@link PluginReport} is ignored, it is completely discarded as if it
     * never occurred.
     * <p>
     * The tags will be remembered in user preferences so they persist between
     * sessions.
     *
     * @param tags Tags to be ignored.
     * @see GraphReportManager#getIgnoredTags()
     */
    public static synchronized void setIgnoredTags(final Collection<String> tags) {
        IGNORED_TAGS.clear();
        IGNORED_TAGS.addAll(tags);

        // Save the new tags.
        final StringBuilder b = new StringBuilder();
        IGNORED_TAGS.stream().forEach(tag -> {
            if (b.length() > 0) {
                b.append("\t");
            }
            b.append(tag);
        });

        final Preferences prefs = NbPreferences.forModule(GraphReportManager.class);
        prefs.put(IGNORED_TAGS_KEY, b.toString());

    }

    /**
     * Get the tags that are being ignored (not recorded) by GraphReport. A
     * {@link PluginReport} will be ignored if all of its tags are present in
     * the this collection of ignored tags. If any of a {@link PluginReport}s
     * tags are not in this list, it will not be ignored. When a
     * {@link PluginReport} is ignored, it is completely discarded as if it
     * never occurred. This is different
     * <p>
     * The tags will be remembered in user preferences so they persist between
     * sessions.
     *
     * @return The tags that are being ignored (not recorded) by GraphReport.
     * @see GraphReportManager#setIgnoredTags(java.util.Collection)
     */
    public static synchronized Set<String> getIgnoredTags() {
        final TreeSet<String> tags = new TreeSet<>();
        tags.addAll(IGNORED_TAGS);

        return tags;
    }

    /**
     * Returns true if a plugin with the specified tags should be ignored,
     * meaning that it will not be recorded and will never be visible in the UI.
     * This is different to simply being hidden
     *
     * @param tags An array of tags, typically from Plugin.getTags().
     *
     * @return True if all tags are to be ignored.
     */
    public static synchronized boolean isIgnored(final String[] tags) {
        for (final String tag : tags) {
            if (!IGNORED_TAGS.contains(tag)) {
                return false;
            }
        }

        return true;
    }
}
