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
package au.gov.asd.tac.constellation.graph.monitor;

import au.gov.asd.tac.constellation.graph.Graph;

/**
 * An event that is produced when a graph changes in any way. The event holds
 * little information about the change, instead relying on the various
 * modification counters on the graph to communicate what type of change
 * occurred.
 *
 * @author sirius
 */
public class GraphChangeEvent {

    private static long nextID = 0;
    private final long id;
    private GraphChangeEvent next;
    private final Graph graph;
    private final Object editor;
    private final Object description;

    /**
     * Creates a new GraphChangeEvent with a automatically created one-up id.
     *
     * @param previous the change event that occurred immediately previous to
     * this one.
     * @param graph the graph that underwent the change.
     * @param editor an object that represents the editor (usually the plug-in
     * instance)
     * @param description an object that describes the change. This can be used
     * to more efficiently communicate the nature of a change than by examining
     * the modification counters on the graph.
     */
    public GraphChangeEvent(final GraphChangeEvent previous, final Graph graph, final Object editor, final Object description) {
        synchronized (GraphChangeEvent.class) {
            id = nextID++;
        }
        this.graph = graph;
        this.editor = editor;
        this.description = description;

        if (previous != null) {
            previous.next = this;
        }
    }

    /**
     * Creates a new GraphChangeEvent with a specified id.
     *
     * @param id the id of the new GraphChangeEvent.
     * @param previous the change event that occurred immediately previous to
     * this one.
     * @param graph the graph that underwent the change.
     * @param editor an object that represents the editor (usually the plug-in
     * instance)
     * @param description an object that describes the change. This can be used
     * to more efficiently communicate the nature of a change than by examining
     * the modification counters on the graph.
     */
    public GraphChangeEvent(final long id, final GraphChangeEvent previous, final Graph graph, final Object editor, final Object description) {
        this.id = id;
        this.graph = graph;
        this.editor = editor;
        this.description = description;

        if (previous != null) {
            previous.next = this;
        }
    }

    /**
     * Returns the id of this GraphChangeEvent.
     *
     * @return the id of this GraphChangeEvent.
     */
    public long getId() {
        return id;
    }

    /**
     * Returns the graph that underwent the change.
     *
     * @return the graph that underwent the change.
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * Returns an object that represents the editor that caused the change. This
     * is often the instance of the plug-in that acquired the write lock on the
     * graph. A common use of this value is so that a listener can tell if it
     * was the entity that made a particular change on the graph and therefore
     * potentially ignore the change.
     *
     * @return an object that represents the editor that caused the change.
     */
    public Object getEditor() {
        return editor;
    }

    /**
     * Returns an object describing the change that has occurred. This can often
     * be useful to more efficiently communicate the nature of the change than
     * can be done by simply examining the modification counters on the graph.
     * For instance, a description may communicate that a single vertex has
     * changed, saving the listener from examining all vertices for changes. Of
     * course, this relies on the editor and listener cooperating so that they
     * both understand meaning of the description object.
     *
     * @return an object describing the change that has occurred.
     */
    public Object getDescription() {
        return description;
    }

    /**
     * Returns the latest GraphChangeEvent that has been issued for this graph.
     * Often, if graph changes are occurring frequently, a GraphChangeEvent may
     * be out of date by the time a listener processed it. Since no change
     * information is contained in the event, it is usually sufficient to simply
     * examine the last event in the series. In fact, updating a view for every
     * issued GraphChangeEvent can cause a bottleneck in the application and in
     * these instances it is beneficial to simple process the latest event.
     *
     * @return the latest GraphChangeEvent that has been issued for this graph.
     */
    public GraphChangeEvent getLatest() {
        GraphChangeEvent latest = this;
        while (latest.next != null) {
            latest = latest.next;
        }
        return latest;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("GraphChangeEvent[");
        out.append("id = ").append(id);
        out.append(", graph = ").append(graph);
        out.append(", editor = ").append(editor);
        out.append(", description = ").append(description);
        out.append("]");
        return out.toString();
    }
}
