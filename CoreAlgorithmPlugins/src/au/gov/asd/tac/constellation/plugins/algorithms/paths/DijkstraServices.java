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
package au.gov.asd.tac.constellation.plugins.algorithms.paths;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains all of the logic for performing shortest paths
 * calculations on a given set of <code>verticesToPath</code>.
 * <p>
 * There are two main functions, <code>queryDistance</code> and
 * <code>queryPath</code>,
 * <p>
 * <code>queryDistance</code> is called first and calculates the shortest
 * distances between nodes on the <code>Graph</code>
 * <p>
 * <code>queryPath</code> is called after <code>queryDistance</code> and
 * calculates the path of the shortest distance between nodes on the
 * <code>Graph</code>
 * <p>
 * It should be noted that all search operations are multi-threaded, and are
 * performed in parallel when there are sufficient resources on the platform.
 *
 * @author procyon
 */
public class DijkstraServices {

    private static final int AVAILABLE_THREADS = Math.max(1, (Runtime.getRuntime().availableProcessors() - 1));
    /**
     * The maximum number of items to assign each thread (until we don't have
     * enough threads anyway)
     */
    private static final int MAX_THRESHOLD_DISTANCE = 10;
    /**
     * The maximum number of items to assign each thread (until we don't have
     * enough threads anyway)
     */
    private static final int MAX_THRESHOLD_PATH = 10;
    private static final String SELECTED = VisualConcept.VertexAttribute.SELECTED.getName();
    private final GraphWriteMethods graph;
    /**
     * A lock object to enforce synchronization; synchronizer does not works on
     * the field but the object assigned to it.
     */
    private final Object lock = new Object();

    /**
     * The order of the collection is important and this is what is used to
     * determine the direction, the first vertex being the source
     */
    private final Map<Integer, ConcurrentHashMap<Integer, Double>> collection = Collections.<Integer, ConcurrentHashMap<Integer, Double>>synchronizedMap(new LinkedHashMap<>());
    private final ConcurrentHashMap<Integer, Set<Integer>> lookupMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, List<ArrayList<Integer>>> paths = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Set<Integer>> visited = new ConcurrentHashMap<>();
    private final List<Integer> selectedVertices;

    private final boolean followDirection;

    private static final Logger LOGGER = Logger.getLogger(DijkstraServices.class.getName());
    
    private static final String THREAD_INTERRUPTED = "Thread was interrupted";

    /**
     * Constructor.
     *
     * @param graph A graph to be modified.
     * @param verticesToPath The vertex ids of interest. Note that the order of
     * the vertices is important when <code>followDirection</code> is True and
     * in this case the first index must source vertex.
     * @param followDirection If true, take note of edge directions. If false,
     * ignore edge directions.
     */
    public DijkstraServices(final GraphWriteMethods graph, final List<Integer> verticesToPath, final boolean followDirection) {
        this.graph = graph;
        this.selectedVertices = verticesToPath;
        this.followDirection = followDirection;
    }

    public void queryPaths(final boolean deselectCurrent) throws InterruptedException {
        final int numVertices = selectedVertices.size();

        if (numVertices > 0) {
            final int neededThreadsDistance = Math.min(AVAILABLE_THREADS, (int) Math.ceil((double) numVertices / (double) MAX_THRESHOLD_DISTANCE));
            final int loadPerThreadDistance = (int) Math.ceil((double) numVertices / (double) neededThreadsDistance);

            final CyclicBarrier distanceBarrier = new CyclicBarrier(neededThreadsDistance + 1);

            try {
                final ThreadedQuery[] distanceWorker = new ThreadedQuery[neededThreadsDistance];

                // Create the requisite number of workers:
                for (int i = 0; i < neededThreadsDistance; i++) {
                    final int workloadLBound = i * loadPerThreadDistance;
                    final int workloadUBound = Math.min((numVertices - 1), ((i + 1) * loadPerThreadDistance) - 1);

                    distanceWorker[i] = new ThreadedQuery(distanceBarrier, this, "Distance", i, workloadLBound, workloadUBound);

                    // Start the worker now that it knows its workload:
                    final Thread t = new Thread(distanceWorker[i]);
                    t.start();
                }
            } finally {
                try {
                    distanceBarrier.await();
                } catch (final InterruptedException ex) {
                    LOGGER.log(Level.SEVERE, THREAD_INTERRUPTED, ex);
                    Thread.currentThread().interrupt();
                } catch (final BrokenBarrierException ex) {
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                }
            }
        }

        final int sampleSpaceSize = collection.size();

        if (sampleSpaceSize > 0) {
            final int neededThreadsPath = Math.min(AVAILABLE_THREADS, (int) Math.ceil((double) sampleSpaceSize / (double) MAX_THRESHOLD_PATH));
            final int loadPerThreadPath = (int) Math.ceil((double) sampleSpaceSize / (double) neededThreadsPath);

            final CyclicBarrier pathBarrier = new CyclicBarrier(neededThreadsPath + 1);

            try {
                final ThreadedQuery[] pathWorker = new ThreadedQuery[neededThreadsPath];

                // Create the requisite number of workers:
                for (int i = 0; i < neededThreadsPath; i++) {
                    final int workloadLBound = i * loadPerThreadPath;
                    final int workloadUBound = Math.min((sampleSpaceSize - 1), ((i + 1) * loadPerThreadPath) - 1);

                    pathWorker[i] = new ThreadedQuery(pathBarrier, this, "Path", i, workloadLBound, workloadUBound);

                    // Start the worker now that it knows its workload:
                    final Thread t = new Thread(pathWorker[i]);
                    t.start();
                }
            } finally {
                try {
                    pathBarrier.await();
                } catch (final InterruptedException ex) {
                    LOGGER.log(Level.SEVERE, THREAD_INTERRUPTED, ex);
                    Thread.currentThread().interrupt();
                } catch (final BrokenBarrierException ex) {
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                }
            }
        }
        selectOnGraph(deselectCurrent);
    }

    /**
     * Selects the vertices that lie on each calculated path by
     * <code>queryPath</code>.
     *
     * @param clearSelection <code>true</code> to clear previously selected
     * items on the graph, <code>false</code> to add to them.
     */
    public void selectOnGraph(final boolean clearSelection) {
//        Check if we need to deselect the current selections on the graph
        if (clearSelection) {
            clearSelection();
        }

        //Loop over the map and for each vertex get the paths that it is connected to
        final int vxSelectedAttr = VisualConcept.VertexAttribute.SELECTED.get(graph);

        for (final Map.Entry<Integer, List<ArrayList<Integer>>> vertex : paths.entrySet()) {
            //For each path, get the first vertex and establish which link it corresponds to
            for (final ArrayList<Integer> path : vertex.getValue()) {
                for (int i = 0; i < path.size(); i++) {
                    final int currNode = path.get(i);
                    final int nextNode;

                    //Select Current Node
                    graph.setBooleanValue(vxSelectedAttr, currNode, true);

                    //Handle overflow
                    if (i + 1 == path.size()) {
                        break;
                    } else {
                        nextNode = path.get(i + 1);
                    }

                    //Determine transaction connecting the two vertices
                    final int txSelectedAttr = VisualConcept.TransactionAttribute.SELECTED.get(graph);
                    final int linkId = graph.getLink(currNode, nextNode);
                    //Provide feedback in case of error on link id where the two vertices provided do not
                    //have a valid link between them
                    if (linkId == Graph.NOT_FOUND) {
                        LOGGER.log(Level.SEVERE, "\tERROR ON LINK: {0} -> {1}", new Object[]{currNode, nextNode});
                    }
                    //Get the amount of transactions on the link as an iterator
                    final int txCount = graph.getLinkTransactionCount(linkId);
                    for (int position = 0; position < txCount; position++) {
                        //Select the links on the graph
                        final int tx = graph.getLinkTransaction(linkId, position);
                        graph.setBooleanValue(txSelectedAttr, tx, true);

                    }
                }
            }
        }
    }

    /**
     * Iterates over the entire graph and removes and sets all instances of the
     * selected attribute to false.
     */
    private void clearSelection() {
        final int selectedVertexAttr = VisualConcept.VertexAttribute.SELECTED.get(graph);
        final int selectedLinkAttr = graph.getAttribute(GraphElementType.LINK, SELECTED);
        final int selectedEdgeAttr = graph.getAttribute(GraphElementType.EDGE, SELECTED);
        final int selectedTranAttr = VisualConcept.TransactionAttribute.SELECTED.get(graph);
        // Unselect everything:
        if (selectedVertexAttr != Graph.NOT_FOUND) {
            for (int i = 0; i < graph.getVertexCount(); i++) {
                graph.setBooleanValue(selectedVertexAttr, i, false);
            }
        }
        if (selectedLinkAttr != Graph.NOT_FOUND) {
            for (int i = 0; i < graph.getLinkCount(); i++) {
                graph.setBooleanValue(selectedLinkAttr, i, false);
            }
        }
        if (selectedEdgeAttr != Graph.NOT_FOUND) {
            for (int i = 0; i < graph.getEdgeCount(); i++) {
                graph.setBooleanValue(selectedEdgeAttr, i, false);
            }
        }
        if (selectedTranAttr != Graph.NOT_FOUND) {
            for (int i = 0; i < graph.getTransactionCount(); i++) {
                graph.setBooleanValue(selectedTranAttr, i, false);
            }
        }
    }

    // TODO: it looks like getNeighboursIgnoreDirection and getNeighboursFollowDirection are run twice...check and optimise!
    /**
     * Get the neighbours of the specified vertex, ignoring the direction of the
     * edges between them.
     *
     * @param neighbours A List to hold the neighbour ids.
     * @param rg The graph.
     * @param vxId The vertex of interest.
     */
    private static void getNeighboursIgnoreDirection(final List<Integer> neighbours, final GraphReadMethods rg, final int vxId) {
        for (int position = 0; position < rg.getVertexNeighbourCount(vxId); position++) {
            neighbours.add(rg.getVertexNeighbour(vxId, position));
        }
    }

    /**
     * Get the neighbours of the specified vertex that are connected by an
     * outgoing directed edge.
     *
     * @param neighbours A List to hold the neighbour ids.
     * @param rg The graph.
     * @param vxId The vertex of interest.
     */
    private static void getNeighboursFollowDirection(final List<Integer> neighbours, final GraphReadMethods rg, final int vxId) {
        for (int position = 0; position < rg.getVertexEdgeCount(vxId, Graph.OUTGOING); position++) {
            final int edgeId = rg.getVertexEdge(vxId, Graph.OUTGOING, position);
            neighbours.add(rg.getEdgeDestinationVertex(edgeId));
        }
    }

    /**
     * Private class responsible for handling queries in a threaded manner.
     * <p>
     * Each thread is assigned an instance of this internal class.
     */
    private class ThreadedQuery implements Runnable {

        private final CyclicBarrier barrier;
        private final DijkstraServices parent;
        private final int threadID;
        private final String type;
        private int workloadLBound = -1;
        private int workloadUBound = -1;

        /*
         * Constructor for performing distance and path calculations
         */
        public ThreadedQuery(final CyclicBarrier barrier, final DijkstraServices parent, final String type,
                final int threadID, final int workloadLBound, final int workloadUBound) {
            this.barrier = barrier;
            this.parent = parent;
            this.type = type;
            this.threadID = threadID;
            this.workloadLBound = workloadLBound;
            this.workloadUBound = workloadUBound;
        }

        @Override
        public void run() {
            try {
                switch (type) {
                    case "Distance":
                        Thread.currentThread().setName("Find.FindServices.Thread.Distance." + threadID);
                        queryDistance();
                        break;
                    case "Path":
                        Thread.currentThread().setName("Find.FindServices.Thread.Path." + threadID);
                        queryPath();
                        break;
                    //Not Handled
                    default:
                        break;
                }
            } catch (final InterruptedException ex) {
                LOGGER.log(Level.SEVERE, THREAD_INTERRUPTED, ex);
                Thread.currentThread().interrupt();
            } finally {
                // This thread is now done, so wait for all others to finish:
                try {
                    barrier.await();
                } catch (final InterruptedException ex) {
                    LOGGER.log(Level.SEVERE, THREAD_INTERRUPTED);
                    Thread.currentThread().interrupt();
                } catch (final BrokenBarrierException ex) {
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
                }
            }
        }

        /**
         * Calculates the shortest distances between nodes on the
         * <code>Graph</code> and saves its results in it's parent
         * <code>collection</code>.
         */
        private void queryDistance() throws InterruptedException {
            //Get a list of all the vertices in the graph
            final List<Integer> vertices = Collections.synchronizedList(new ArrayList<>());
            synchronized (vertices) {
                for (int i = 0; i < graph.getVertexCount(); i++) {
                    vertices.add(graph.getVertex(i));
                }
            }

            //Loop over each vertex that is selected on the graph and determine the vertices
            //connected to it and their distances
            for (final int vertex : Collections.synchronizedList(parent.selectedVertices)) {
                //Collection of distances for the current vertex
                final ConcurrentHashMap<Integer, Double> distances = new ConcurrentHashMap<>();

                //Holds number of hops from the current vertex to connected vertices
                final ConcurrentHashMap<Tuple<Integer, Integer>, Double> weights = null; // Map: (source,dest) -> edge weight

                //Begin Dijkstra's Algorithm
                final FibonacciHeap<Integer> priorityQueue = new FibonacciHeap<>();
                synchronized (priorityQueue) {
                    final ConcurrentHashMap<Integer, FibonacciHeap.Entry<Integer>> entries = new ConcurrentHashMap<>();

                    synchronized (entries) {
                        for (final int node : Collections.synchronizedList(vertices)) {
                            entries.put(node, priorityQueue.enqueue(node, Double.POSITIVE_INFINITY));
                        }

                        priorityQueue.decreaseKey(entries.get(vertex), 0.0);

                        while (!priorityQueue.isEmpty()) {
                            if (Thread.interrupted()) {
                                throw new InterruptedException();
                            }

                            final FibonacciHeap.Entry<Integer> curr = priorityQueue.dequeueMin();

                            synchronized (curr) {
                                if (curr.getPriority() == Double.POSITIVE_INFINITY) {// if the dist is inf should be ok to break cos must be in different connected component else would be less than inf.
                                    break;
                                }

                                distances.put(curr.getValue(), curr.getPriority());

                                final List<Integer> neighbours = Collections.synchronizedList(new ArrayList<>());

                                synchronized (neighbours) {
                                    final int id = curr.getValue();
                                    if (followDirection) {
                                        getNeighboursFollowDirection(neighbours, graph, id);
                                    } else {
                                        getNeighboursIgnoreDirection(neighbours, graph, id);
                                    }

                                    for (final int n : neighbours) {
                                        if (Collections.synchronizedMap(distances).containsKey(n)) {
                                            continue;
                                        } else if (n == vertex) {
                                            break;
                                        } else {
                                            // Do nothing
                                        }
                                        final double pathCost = curr.getPriority() + getWeight(curr.getValue(), n, weights);
                                        final FibonacciHeap.Entry<Integer> neigh = entries.get(n);
                                        if (pathCost < neigh.getPriority()) {
                                            priorityQueue.decreaseKey(neigh, pathCost);
                                            final Set<Integer> tmp = Collections.synchronizedSet(new HashSet<>());
                                            synchronized (tmp) {
                                                Collections.synchronizedSet(tmp).add(curr.getValue());
                                                if (parent.lookupMap.containsKey(n)) {
                                                    parent.lookupMap.get(n).addAll(tmp);
                                                } else {
                                                    parent.lookupMap.put(n, tmp);
                                                }
                                            }
                                        } else if (pathCost == neigh.getPriority()) {
                                            incMapSet(parent.lookupMap, n, curr.getValue());
                                        } else {
                                            // Do nothing
                                        }
                                    }
                                }
                            }
                        }
                    }
                    //End Dijkstra's Algorithm

                    //Determine whether the vertex is connected to other selected nodes
                    //If it does, add them to the collection
                    int count = 0;
                    synchronized (distances) {
                        for (final int vertexEntry : Collections.synchronizedList(parent.selectedVertices)) {
                            if (distances.containsKey(vertexEntry) && vertexEntry != vertex) {
                                count++;
                            }
                        }
                        if (count > 0) {
                            synchronized (parent.collection) {
                                parent.collection.put(vertex, new ConcurrentHashMap<>(distances));
                            }
                        }
                    }
                }
            }
        }

        /**
         * Calculates the paths that the shortest distances take between nodes
         * on the <code>Graph</code> and saves its results in it's parent
         * <code>paths</code>.
         */
        private void queryPath() throws InterruptedException {
            //Use each vertex in our collection as a pivot point and look at the rest of the graph according to it
            for (int workPackage = workloadLBound; workPackage <= workloadUBound; workPackage++) {
                final int pivottedVertex = (Integer) parent.collection.keySet().toArray()[workPackage];

                if (!followDirection || (followDirection && pivottedVertex == selectedVertices.get(0))) {
                    //Set each selected vertex as a target
                    for (final int vertex : parent.collection.get(pivottedVertex).keySet()) {
                        //Check to make sure this vertex is selected and we haven't found the path before, otherwise skip it
                        if (!parent.selectedVertices.contains(vertex) || pivottedVertex == vertex
                                || (parent.visited.containsKey(pivottedVertex) && parent.visited.get(pivottedVertex).contains(vertex)) 
                                || (parent.visited.containsKey(vertex) && parent.visited.get(vertex).contains(pivottedVertex))) {
                            continue;
                        }

                        if (Thread.currentThread().isInterrupted()) {
                            throw new InterruptedException();
                        }

                        //Check if there is a distance between the two nodes
                        synchronized (parent.visited) {
                            if (parent.collection.get(pivottedVertex).get(vertex).intValue() > 0) {
                                //Housekeeping, add the route to a list of calculated routes
                                if (parent.visited.containsKey(pivottedVertex)) {
                                    parent.visited.get(pivottedVertex).add(vertex);
                                } else if (parent.visited.containsKey(vertex)) {
                                    parent.visited.get(vertex).add(pivottedVertex);
                                } else {
                                    final Set<Integer> set = Collections.synchronizedSet(new HashSet<>());
                                    synchronized (set) {
                                        set.add(vertex);
                                        parent.visited.put(pivottedVertex, set);
                                    }
                                }
                            }

                            if (pivottedVertex != vertex) {
                                //Create localised variables to aid in recursive calling of findPath
                                final List<Integer> localPath = Collections.synchronizedList(new ArrayList<>()); //Contains the current path, this is appended to resulting in the final path
                                synchronized (localPath) {
                                    final List<ArrayList<Integer>> shortestPaths = Collections.synchronizedList(new ArrayList<>()); //Contains a running list of localPaths that we have found
                                    synchronized (shortestPaths) {
                                        //Send off our variables to findPath which will recursively visit neighbouring nodes and determine the paths.
                                        //This call populates shortestPaths - Also NOTE: we are originating from our target back to our pivot in congruence with the way our map works ie -> 15=[14,13] vertex 15 is connected to 14 and 13
                                        findPath(pivottedVertex, vertex, parent.collection.get(pivottedVertex).get(vertex).intValue(), localPath, shortestPaths); // We can ignore the returned list as it returns to our shortestPath list
                                    }
                                    //Now we have a list of the shortest paths from the vertex to our connected target vertex we need to inject our target vertex into the path as a starting point
                                    for (final ArrayList<Integer> list : shortestPaths) {
                                        synchronized (list) {
                                            list.add(0, vertex);
                                        }
                                    }

                                    //Need to add our new paths to the combined collection for all of our vertices to return
                                    //Bit of housekeeping to check if we already have an index for our vertex, if we do we just need to append it to what is currently there
                                    synchronized (parent.paths) {
                                        if (parent.paths.containsKey(vertex)) {
                                            parent.paths.get(vertex).addAll(shortestPaths);
                                        } else {
                                            parent.paths.put(vertex, shortestPaths);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        /**
         * Returns a sequence of integers representing the shortest path between
         * two vertices.
         * <p>
         * Recursive function which calculates the path between the target and
         * previous nodes by visiting each neighboring node and determining if
         * the distance to the target correlates with the shortest distance.
         *
         * @param target The integer value of the destination vertex.
         * @param previous The integer value of the originating vertex.
         * @param count The shortest amount of hops between the target and
         * previous vertices.
         * @param path The current path being created (is appended on each
         * recursion if a new vertex is encountered on the shortest path).
         * @param shortestPaths List containing the paths found.
         */
        private List<ArrayList<Integer>> findPath(final int target, final int previous, int count, final List<Integer> path, final List<ArrayList<Integer>> shortestPaths) throws InterruptedException {
            synchronized (lock) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                //If count is 0 we should be at the target
                if (previous == target && count == 0) {
                    synchronized (lock) {
                        shortestPaths.add(new ArrayList<>(path));
                        path.remove(path.indexOf(previous));
                    }
                    count++;
                } else if (count == 0) {
                    //If we are at 0 and not at the target we have reached a dead end
                    //Not the shortest path
                } else {
                    //Make sure the originating vertex has an entry in our map so we can transverse it
                    if (parent.lookupMap.containsKey(previous)) {
                        //We need to get all the neigbouring vertices that belong to our current vertex
                        for (final int currentNode : parent.lookupMap.get(previous)) {
                            //Check that we are heading in the right direction, ie. our current vertex should have a distance of our original count - 1 otherwise its not the right path
                            if (parent.collection.get(target).containsKey(currentNode) && parent.collection.get(target).get(currentNode).intValue() == (count - 1)) {
                                //Add our vertex to the current path and recursively call findPath using this vertex as the originating, decrement count
                                synchronized (lock) {
                                    path.add(currentNode);
                                    findPath(target, currentNode, (count - 1), path, shortestPaths);
                                    //Reduncancy check - if node still exists in the path IE. did not match we need to remove the entry
                                    if (path.contains(currentNode)) {
                                        path.remove(path.lastIndexOf(currentNode));
                                    }
                                }
                            }
                        }
                    } else {
                        //TODO: need to catch error here
                        LOGGER.log(Level.SEVERE, "ERROR: {0} in pursuit of {1}", new Object[]{previous, target});
                    }
                }
            }
            return shortestPaths;
        }

        /**
         * Helper function used to create and retrieve a weight corresponding to
         * a distance
         */
        private synchronized double getWeight(final Integer source, final Integer dest, final Map<Tuple<Integer, Integer>, Double> weights) {
            if (weights == null) {
                return 1;
            }
            return weights.get(new Tuple<>(source, dest));
        }

        private synchronized void incMapSet(final Map<Integer, Set<Integer>> map, final Integer key, final Integer val) {
            Set<Integer> tmp = Collections.synchronizedSet(new HashSet<>());
            synchronized (tmp) {
                if (map.containsKey(key)) {
                    tmp = map.get(key);
                }
                tmp.add(val);
                map.put(key, tmp);
            }
        }
    }
}
