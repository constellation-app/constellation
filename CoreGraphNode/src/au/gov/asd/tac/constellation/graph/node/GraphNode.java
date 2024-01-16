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
package au.gov.asd.tac.constellation.graph.node;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.file.GraphDataObject;
import au.gov.asd.tac.constellation.graph.manager.GraphManagerListener;
import au.gov.asd.tac.constellation.utilities.memory.MemoryManager;
import au.gov.asd.tac.constellation.utilities.visual.VisualManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.awt.UndoRedo;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;

/**
 * A NetBeans node used for providing context to actions
 *
 * @author algol
 */
public class GraphNode extends AbstractNode {
    
    private static final Logger LOGGER = Logger.getLogger(GraphNode.class.getName());

    private static final Map<String, GraphNode> GRAPHS = new HashMap<>();
    private static final List<GraphManagerListener> LISTENERS = new ArrayList<>();

    public static void addGraphManagerListener(final GraphManagerListener listener) {
        if (listener != null && !LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    public static void removeGraphManagerListener(final GraphManagerListener listener) {
        LISTENERS.remove(listener);
    }

    /**
     * Return the GraphNode corresponding to the given Graph.
     *
     * @param graph A Graph.
     *
     * @return The GraphNode corresponding to the given Graph.
     */
    public static GraphNode getGraphNode(final Graph graph) {
        return graph != null ? GRAPHS.get(graph.getId()) : null;
    }

    public static GraphNode getGraphNode(final String graphId) {
        return GRAPHS.get(graphId);
    }

    /**
     * Return all currently open graphs.
     *
     * @return All currently open graphs.
     */
    public static Map<String, Graph> getAllGraphs() {
        final Map<String, Graph> graphs = new TreeMap<>();

        for (final Map.Entry<String, GraphNode> entry : GRAPHS.entrySet()) {
            graphs.put(entry.getKey(), entry.getValue().getGraph());
        }

        return Collections.unmodifiableMap(graphs);
    }

    /**
     * Return the graph with the specified id.
     *
     * @param id A unique graph identifier.
     *
     * @return The graph with the specified id, or null if there is no graph
     * with that identifier.
     */
    public static Graph getGraph(final String id) {
        GraphNode graphNode = GRAPHS.get(id);
        return graphNode == null ? null : graphNode.getGraph();
    }

    private final Graph graph;
    private final VisualManager visualManager;
    private GraphDataObject gdo;
    private final TopComponent tc;
    private int busyCount = 0;
    private final UndoRedo.Manager undoRedoManager;

    /**
     * Create a new Node for a Graph.
     * <p>
     * Maintaining a Sheet is not as straight-forward as it seems. The
     * createSheet() method is called only once, so building a new Sheet
     * whenever the graph changes won't work. The trick is to return a Sheet
     * that we keep a reference to, and rebuild that Sheet on a graph property
     * change.
     * <p>
     * There's a further catch: each Property returns a value via a getValue()
     * method, so there's no opportunity to get a single graph read lock to get
     * all of the values at once. Acquiring a read lock for each and every graph
     * attribute seems horrible. Therefore, the property set data is collected
     * in one go (in inspectGraph()), and used to insert a new lot of Set
     * instances into the Sheet (in updateSheet()).
     *
     * @param graph The Graph.
     * @param gdo The GraphDataObject.
     * @param tc The graph's TopComponent.
     * @param visualManager The VisualManager used to respond to the graphs
     * changes to visual attributes and display these via an associated
     * VisualProcessor.
     */
    public GraphNode(final Graph graph, final GraphDataObject gdo, final TopComponent tc, final VisualManager visualManager) {
        this(new InstanceContent(), graph, gdo, tc, visualManager);
    }

    /**
     * Private constructor: recommended pattern for using InstanceContent.
     *
     * @param content This object's Lookup.
     * @param graph The Graph.
     * @param gdo The GraphDataObject.
     * @param visual The visual interface to the graph.
     * @param tc The graph's TopComponent.
     */
    private GraphNode(final InstanceContent content, final Graph graph, final GraphDataObject gdo, final TopComponent tc, final VisualManager visualManager) {
        super(Children.LEAF, new AbstractLookup(content));
        content.add(graph);
        content.add(this);
        this.graph = graph;
        this.visualManager = visualManager;
        this.gdo = gdo;
        this.undoRedoManager = new UndoRedo.Manager();
        this.tc = tc;

        graph.setUndoManager(undoRedoManager);

        GRAPHS.put(graph.getId(), GraphNode.this);

        for (GraphManagerListener listener : LISTENERS) {
            listener.graphOpened(graph);
        }

        MemoryManager.newObject(GraphNode.class);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            MemoryManager.finalizeObject(GraphNode.class);
        } finally {
            super.finalize();
        }
    }

    @Override
    public String getDisplayName() {
        return gdo.getName();
    }

    @Override
    public String getShortDescription() {
        return null;
    }

    /**
     * The graph that this node represents.
     *
     * @return The graph that this node represents.
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * The DataObject that loaded the graph that this node represents.
     *
     * @return The DataObject that loaded the graph that this node represents.
     */
    public GraphDataObject getDataObject() {
        return gdo;
    }

    public void setDataObject(final GraphDataObject gdo) {
        this.gdo = gdo;
    }

    /**
     * Return the GraphVisualiser controlling visualisation of the graph for
     * this graph node. This should be used by plugins and utilities that
     * specifically need reference to the way the graph is being rendered rather
     * than just its attributes. This includes plugins that want to run
     * animations (temporarily altering the rendering of the graph), or things
     * like the ExportToImagePlugin, which needs to ask the renderer to take a
     * snapshot of what it is currently displaying.
     *
     * @return The GraphVisualiser used by this GraphNode.
     */
    public VisualManager getVisualManager() {
        return visualManager;
    }

    /**
     * The TopComponent that is displaying the graph that this node represents.
     *
     * @return The TopComponent that is displaying the graph that this node
     * represents.
     */
    public TopComponent getTopComponent() {
        return tc;
    }

    public synchronized void makeBusy(final boolean busy) {
        if (busy) {
            if (busyCount++ == 0) {
                tc.makeBusy(true);
            }
        } else {
            if (--busyCount == 0) {
                tc.makeBusy(false);
            }
        }
    }

    /**
     * Return the undo/redo manager for this graph.
     *
     * @return The undo/redo manager for this graph.
     */
    public UndoRedo.Manager getUndoRedoManager() {
        return undoRedoManager;
    }

    @Override
    public String getName() {
        return tc.getName();
    }

    /**
     * This node is no longer needed.
     * <p>
     * Call this from TopComponent.componentClosed().
     */
    @Override
    public void destroy() {
        GRAPHS.remove(graph.getId());

        try {
            super.destroy();
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }

        for (GraphManagerListener listener : LISTENERS) {
            try {
                listener.graphClosed(graph);
            } catch (final Exception ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }
    }

    /**
     * Called to provide a Sheet for the property viewer.
     * <p>
     * This is only called once, so we keep a reference to the sheet and update
     * it as required.
     *
     * @return A Sheet for the property viewer.
     */
    @Override
    protected Sheet createSheet() {
        return null;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("au.gov.asd.tac.constellation.graph.attributes");
    }

    @Override
    public String toString() {
        final String fnam = gdo.getPrimaryFile().getPath();

        return String.format("GraphNode[%s]", fnam);
    }

    /**
     * method to generate a new graph name based on a given value and the set of
     * existing graphs
     *
     * @param suggestedName suggested new graph name
     * @return string with new graph name
     */
    public static String getUniqueGraphName(final String suggestedName) {
        // generate list of existing graph names
        final ArrayList<String> list = new ArrayList<>();
        final Iterator<Graph> iter = GraphNode.getAllGraphs().values().iterator();
        while (iter.hasNext()) {
            final GraphNode node = GraphNode.getGraphNode(iter.next());
            if (node != null) {
                list.add(node.getDisplayName());
            }
        }

        // check to see if a graph of the same name already exists - if so, then append a number
        int counter = 0;
        String newName = suggestedName;
        while (list.contains(newName)) {
            counter++;
            newName = suggestedName + counter;
        }

        return newName;
    }

    /**
     * method to identify to return set of IDs
     *
     * @return list of IDs
     */
    public static ArrayList<String> getGraphIDs() {
        final ArrayList<String> list = new ArrayList<>();
        final Iterator<Graph> iter = GraphNode.getAllGraphs().values().iterator();
        while (iter.hasNext()) {
            final GraphNode node = GraphNode.getGraphNode(iter.next());
            if (node != null) {
                list.add(node.getGraph().getId());
            }
        }
        return list;
    }

    /**
     * method to check whether the file name is already used in any opened graph
     * (saved or in-memory)
     */
    public static boolean fileNameExists(final String name) {
        final Iterator<Graph> iter = GraphNode.getAllGraphs().values().iterator();
        while (iter.hasNext()) {
            final GraphNode node = GraphNode.getGraphNode(iter.next());
            if (node != null && node.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
