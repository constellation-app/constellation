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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.ktruss;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.ClusteringConcept;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Execute a k-truss action
 *
 * @author sirius
 */
public class KTruss {

    public static interface KTrussResultHandler {

        public void initialise(final BitSet currentLinksCopy);

        public void recordVertexCluster(final int vxID, final int clusterNum);

        public void recordTransactionCluster(final int txID, final int clusterNum);

        public boolean nextK(final int lastK, final boolean clustersModified, final BitSet currentLinksCopy);

        public void finalise(final int highestK, final BitSet currentLinksCopy);
    }

    public static class KTrussPluginResultHandler implements KTrussResultHandler {

        private final GraphWriteMethods graph;
        private final boolean interactive;
        private final int vertexKTrussAttribute;
        private final int transactionKTrussAttribute;
        private int currentComponentNum;
        // A list of the values of k which caused changes in the graph.
        // That is those values of k for which the graph contained k-1 trusses that were not k-trusses.
        private final List<Integer> significantClusters = new ArrayList<>();

        // These data structures are used to keep track of the nesting of connected components as the value of k increases.
        // This information is used to display nested k-trusses in the KTrussControllerTopComponent
        private final Map<Integer, Integer> nodeToComponent = new HashMap<>();
        private final Map<Integer, Integer> linkToComponent = new HashMap<>();
        private final Map<Integer, Integer> componentTree = new HashMap<>();
        private final Map<Integer, Integer> componentSizes = new HashMap<>();

        // Records the total number of vertices in the graph which lie in a k-truss for some k >= 3.
        private int totalVertsInTrusses = 0;

        public KTrussPluginResultHandler(final GraphWriteMethods graph, final boolean interactive) {
            this.graph = graph;
            this.interactive = interactive;
            vertexKTrussAttribute = ClusteringConcept.VertexAttribute.K_TRUSS_CLUSTER.ensure(graph);
            transactionKTrussAttribute = ClusteringConcept.TransactionAttribute.K_TRUSS_CLUSTER.ensure(graph);
        }

        @Override
        public void initialise(final BitSet currentLinksCopy) {
            currentComponentNum = getComponents(graph, currentLinksCopy, nodeToComponent, linkToComponent, componentTree, componentSizes, 0);
        }

        @Override
        public void recordVertexCluster(final int vxID, final int clusterNum) {
            graph.setIntValue(vertexKTrussAttribute, vxID, clusterNum);
            if (clusterNum != 0) {
                totalVertsInTrusses++;
            }
        }

        @Override
        public void recordTransactionCluster(final int txID, final int clusterNum) {
            graph.setIntValue(transactionKTrussAttribute, txID, clusterNum);
        }

        @Override
        public boolean nextK(final int lastK, final boolean clusterModified, final BitSet currentLinksCopy) {
            if (clusterModified) {
                currentComponentNum = getComponents(graph, currentLinksCopy, nodeToComponent, linkToComponent, componentTree, componentSizes, currentComponentNum);
                significantClusters.add(lastK);
            }
            return true;
        }

        @Override
        public void finalise(final int highestK, final BitSet currentLinksCopy) {

            // Record an array of which values of k caused changes in the graph,
            // ie. those values of k for which the graph contained k-1 trusses that were not k-trusses.
            final boolean[] extantKTrusses = new boolean[highestK + 2];
            Arrays.fill(extantKTrusses, false);
            final Iterator<Integer> iter = significantClusters.iterator();
            while (iter.hasNext()) {
                extantKTrusses[iter.next()] = true;
            }

            // Get the current KTrussState
            final int kTrussStateAttr = ClusteringConcept.MetaAttribute.K_TRUSS_CLUSTERING_STATE.ensure(graph);
            final KTrussState prevKTrussState = (KTrussState) graph.getObjectValue(kTrussStateAttr, 0);

            // Create and set the new KTrussState, respecting the visual options of the previos KTrussState if present
            final KTrussState state;
            if (prevKTrussState != null) {
                state = new KTrussState(prevKTrussState, highestK + 1, extantKTrusses);
            } else {
                state = new KTrussState(highestK + 1, extantKTrusses);
            }

            // Set the information about the connected components of the k-trusses in the KTrussState
            // to facilitate nested k-truss visulisation.
            state.setComponentInformation(nodeToComponent, linkToComponent, componentTree, componentSizes, currentComponentNum, graph.getVertexCount(), totalVertsInTrusses);
            state.strucModificationCount = graph.getStructureModificationCounter();
            state.setInteractive(interactive);
            graph.setObjectValue(kTrussStateAttr, 0, state);

        }

    }

    public static void run(final GraphWriteMethods graph, final KTrussResultHandler resultHandler) {

        final BitSet vertices = new BitSet();
        vertices.set(0, graph.getVertexCount());
        final BitSet links = new BitSet();
        links.set(0, graph.getLinkCount());

        // Exclude all links that are loops
        for (int linkPosition = 0; linkPosition < graph.getLinkCount(); linkPosition++) {
            final int link = graph.getLink(linkPosition);
            if (graph.getLinkLowVertex(link) == graph.getLinkHighVertex(link)) {
                links.clear(linkPosition);
            }
        }

        // Records whether or not there are graph elements that are present in a k-truss that are not present in a k+1-truss.
        boolean modifiedThisK = false;
        int lastK = 0;
        int currentK = 3;
        resultHandler.initialise((BitSet) links.clone());

        while (true) {
            boolean modified = false;

            // Cull all vertices that do not have enough links
            for (int vertexPosition = vertices.nextSetBit(0); vertexPosition >= 0; vertexPosition = vertices.nextSetBit(vertexPosition + 1)) {
                final int vxID = graph.getVertex(vertexPosition);

                // Count the number of surviving links connected to this vertex
                int survivingLinkCount = 0;
                final int vertexLinkCount = graph.getVertexLinkCount(vxID);
                for (int vertexLinkPosition = 0; vertexLinkPosition < vertexLinkCount; vertexLinkPosition++) {
                    int link = graph.getVertexLink(vxID, vertexLinkPosition);
                    if (links.get(graph.getLinkPosition(link))) {
                        survivingLinkCount++;
                    }
                }

                // If the vertex does not have enough surviving links then cull the vertex and all its links
                if (survivingLinkCount < currentK - 1) {

                    vertices.clear(vertexPosition);
                    modified = true;
                    resultHandler.recordVertexCluster(vxID, lastK);

                    for (int vertexLinkPosition = 0; vertexLinkPosition < vertexLinkCount; vertexLinkPosition++) {
                        final int link = graph.getVertexLink(vxID, vertexLinkPosition);
                        final int linkPosition = graph.getLinkPosition(link);

                        // If this link was still alive then cull it and record its k-truss cluster.
                        if (links.get(linkPosition)) {
                            links.clear(linkPosition);

                            final int transactionCount = graph.getLinkTransactionCount(link);
                            for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                                final int txID = graph.getLinkTransaction(link, transactionPosition);
                                resultHandler.recordTransactionCluster(txID, lastK);
                            }
                        }
                    }
                }
            }

            // Cull all surviving links that do not have enough support
            for (int linkPosition = links.nextSetBit(0); linkPosition >= 0; linkPosition = links.nextSetBit(linkPosition + 1)) {
                final int link = graph.getLink(linkPosition);
                final int lowVertex = graph.getLinkLowVertex(link);
                final int highVertex = graph.getLinkHighVertex(link);

                // Support is determined by counting the number of triangles that the link lies in
                // This is determined by iterating through the neighbours of the lowVertex for the link
                // (ensuring that the link joining the neighbours is still alive)
                // and seeing how many are also neighbours of the highVertex for the link
                int support = 0;
                for (int neighbourPosition = 0; neighbourPosition < graph.getVertexNeighbourCount(lowVertex); neighbourPosition++) {
                    final int lowNeighbour = graph.getVertexNeighbour(lowVertex, neighbourPosition);
                    final int lowNeighbourLink = graph.getLink(lowVertex, lowNeighbour);
                    final int triangleLink = graph.getLink(lowNeighbour, highVertex);
                    if (triangleLink != Graph.NOT_FOUND && links.get(graph.getLinkPosition(triangleLink)) && links.get(graph.getLinkPosition(lowNeighbourLink))) {
                        support++;
                    }
                }

                // If the link lies in less than k - 2 triangles, then cull it and record the k-truss cluster for all of its transactions.
                if (support < currentK - 2) {
                    links.clear(linkPosition);
                    modified = true;

                    final int transactionCount = graph.getLinkTransactionCount(link);
                    for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                        final int txID = graph.getLinkTransaction(link, transactionPosition);
                        resultHandler.recordTransactionCluster(txID, lastK);
                    }
                }
            }

            modifiedThisK |= modified;

            // If the graph has not been modified (no links or vertices culled) then increase the value of k.
            if (!modified) {
                if (vertices.isEmpty() || !resultHandler.nextK(currentK, modifiedThisK, (BitSet) links.clone())) {
                    resultHandler.finalise(lastK, (BitSet) links.clone());
                    break;
                }
                modifiedThisK = false;
                lastK = currentK++;
            }
        }
    }

    // Calculates the connected components of the k-trusses in the graph as k increases.
    // Apart from links which is a clone of the links remaining in the graph that serves only as an in parameter,
    // all parameters are in/out parameters which need to be managed by the KTruss method itself.
    // nodeToComponent and linkToComponent record the smallest components that each node and link lies in
    // componentTree records the heirarchy of nested components
    // componentSizes records the number of nodes in each componenent
    // currentComponentNum keeps track of the total number of components
    private static int getComponents(final GraphWriteMethods graph, final BitSet links, final Map<Integer, Integer> nodeToComponent, final Map<Integer, Integer> linkToComponent, final Map<Integer, Integer> componentTree, final Map<Integer, Integer> componentSizes, int currentComponentNum) {
        // For each link remaining in the graph find all links connected to it, record them and their end vertices as belonging to the same component, and clear them.
        for (int linkPosition = links.nextSetBit(0); linkPosition >= 0; linkPosition = links.nextSetBit(linkPosition + 1)) {
            getComponentsHopper(graph, links, nodeToComponent, linkToComponent, componentTree, currentComponentNum, linkPosition);
            int componentCounter = 0;
            for (final Map.Entry<Integer, Integer> entry : nodeToComponent.entrySet()) {
                if (entry.getValue() == currentComponentNum) {
                    componentCounter++;
                }
            }
            componentSizes.put(currentComponentNum, componentCounter);
            currentComponentNum++;
        }
        return currentComponentNum;
    }

    // Helper method for getComponents which uses recursion to 'hop out one'.
    // Iterates through the links adjacent of a given link, adding them to the same componenent, clearing them, and then calling itself recursively,
    // until it reaches a link with no adjacent links that haven't already been cleared.
    private static void getComponentsHopper(final GraphWriteMethods graph, final BitSet links, final Map<Integer, Integer> nodeToComponent, final Map<Integer, Integer> linkToComponent, final Map<Integer, Integer> componentTree, final int currentComponentNum, final int initialLinkPosition) {

        final Deque<Integer> linksToHopFrom = new LinkedList<>();
        linksToHopFrom.add(initialLinkPosition);
        links.clear(initialLinkPosition);

        while (!linksToHopFrom.isEmpty()) {

            final int linkPosition = linksToHopFrom.pop();
            final int link = graph.getLink(linkPosition);
            final int lowVertex = graph.getLinkLowVertex(link);
            final int highVertex = graph.getLinkHighVertex(link);

            // If the current link is already in a component, record the current component as being nested inside the link's previous component.
            if (linkToComponent.get(link) != null) {
                componentTree.put(currentComponentNum, linkToComponent.get(link));
                // If the current link is not in a component, record the current component as being it's own parent (ie. not nested inside any other components)
            } else {
                componentTree.put(currentComponentNum, currentComponentNum);
            }
            // Record the current link and its two end nodes as belonging to the current component, then clear the link
            linkToComponent.put(link, currentComponentNum);
            nodeToComponent.put(lowVertex, currentComponentNum);
            nodeToComponent.put(highVertex, currentComponentNum);

            // Iterate through the links adjacent to this link's low vertex, adding them to the stack if they haven't already been
            for (int i = 0; i < graph.getVertexLinkCount(lowVertex); i++) {
                final int neighbourLink = graph.getVertexLink(lowVertex, i);
                final int neighbourLinkPosition = graph.getLinkPosition(neighbourLink);
                if (links.get(neighbourLinkPosition)) {

                    links.clear(neighbourLinkPosition);
                    linksToHopFrom.push(neighbourLinkPosition);
                }
            }
            // Iterate through the links adjacent to this link's high vertex, calling this method recursively to record the component for and hop out from these links
            for (int i = 0; i < graph.getVertexLinkCount(highVertex); i++) {
                final int neighbourLink = graph.getVertexLink(highVertex, i);
                final int neighbourLinkPosition = graph.getLinkPosition(neighbourLink);
                if (links.get(neighbourLinkPosition)) {
                    links.clear(neighbourLinkPosition);
                    linksToHopFrom.push(neighbourLinkPosition);

                }
            }
        }
    }
}
