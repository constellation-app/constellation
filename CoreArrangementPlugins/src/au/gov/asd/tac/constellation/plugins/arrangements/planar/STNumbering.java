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
package au.gov.asd.tac.constellation.plugins.arrangements.planar;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author twilight_sparkle
 */
public class STNumbering {

    private final GraphReadMethods graph;

    public final Set<TreeNode> biconnectedComponentRoots = new HashSet<>();
    public final Map<TreeNode, Map<Integer, TreeNode>> vxIDsToTreeNodes = new HashMap<>();
    public final Set<List<TreeNode>> stNumberedBiconnectedComponents = new HashSet<>();

    public static class TreeNode {

        public final int vxID;
        private TreeNode parent;
        private int vLow;
        private int preorder;
        public final Set<TreeNode> children;
        public final Set<TreeNode> descendants;
        public final Set<TreeNode> ancestors;
        public final Set<TreeNode> defactoPendants;
        public final Set<Integer> pendants;
        private Set<TreeNode> componentNeighours = null;

        public TreeNode(final int vxID) {
            this(vxID, null);
        }

        public TreeNode(final int vxID, final TreeNode parent) {
            this.vxID = vxID;
            this.parent = parent;
            children = new HashSet<>();
            descendants = new HashSet<>();
            ancestors = new HashSet<>();
            if (parent != null) {
                ancestors.add(parent);
                ancestors.addAll(parent.ancestors);
            }
            defactoPendants = new HashSet<>();
            pendants = new HashSet<>();
        }

        public int getvLow() {
            return vLow;
        }

        public int getPreorder() {
            return preorder;
        }

    }

    private class NodeResult {

        public final TreeNode node;
        public final int preorder;

        public NodeResult(final TreeNode node, final int preorder) {
            this.node = node;
            this.preorder = preorder;
        }
    }

    public STNumbering(final GraphReadMethods graph) {
        this.graph = graph;
    }

    public void constructAndPreorderBiconnectedComponents() {

        final BitSet unvisitedNodes = new BitSet(graph.getVertexCount());
        unvisitedNodes.set(0, graph.getVertexCount());
        int componentRootPos = 0;

        while (!unvisitedNodes.isEmpty()) {

            componentRootPos = unvisitedNodes.nextSetBit(componentRootPos);
            final int componentRoot = graph.getVertex(componentRootPos);

            if (isPendant(componentRoot) || isSingleton(componentRoot)) {
                unvisitedNodes.clear(componentRootPos);
            } else {
                preorderVertex(unvisitedNodes, componentRoot, null, null, 1);
            }
        }
    }

    public void stNumberBiconnectedComponents() {
        for (final TreeNode t : biconnectedComponentRoots) {

            // The ST Numbering for this component
            final List<TreeNode> stNumberedComponent = new ArrayList<>();

            // Set up the old vertices and edges, and the stack for building an st numbering
            final Set<Integer> oldVxIDs = new HashSet<>();
            final Set<Integer> oldLxIDs = new HashSet<>();
            final Deque<TreeNode> pathfinderStack = new LinkedList<>();
            oldVxIDs.add(t.vxID);
            pathfinderStack.push(t);

            // Note there will only be one child of the root of a biconnected component,
            // but singleton sets still require iteration.
            for (final TreeNode s : t.children) {
                oldVxIDs.add(s.vxID);
                oldLxIDs.add(graph.getLink(t.vxID, s.vxID));
                pathfinderStack.push(s);
            }

            // Process the stack until it is empty
            while (!pathfinderStack.isEmpty()) {

                // Find a path using new edges from the top vertex on the stack
                final TreeNode v = pathfinderStack.pop();
                final List<TreeNode> path = pathfinder(v, t, oldVxIDs, oldLxIDs);

                if (path.isEmpty()) {
                    // If no path was returned, number the vertex
                    stNumberedComponent.add(v);
                } else {
                    // Otherwise push all but the final vertex in path in reverse order onto the stack.
                    for (int i = path.size() - 2; i >= 0; i--) {
                        pathfinderStack.push(path.get(i));
                    }
                }
            }
            stNumberedBiconnectedComponents.add(stNumberedComponent);
        }
    }

    private void setComponentNeighbours(final TreeNode v, final TreeNode root) {
        v.componentNeighours = new HashSet<>();
        for (int i = 0; i < graph.getVertexNeighbourCount(v.vxID); i++) {
            final TreeNode x = vxIDsToTreeNodes.get(root).get(graph.getVertexNeighbour(v.vxID, i));
            if (x != null) {
                v.componentNeighours.add(x);
            }
        }
    }

    private List<TreeNode> pathfinder(final TreeNode v, final TreeNode t, final Set<Integer> oldVxIDs, final Set<Integer> oldLxIDs) {
        final List<TreeNode> path = new ArrayList<>();

        // Check for 'new' cycle edges between v and its ancestors.
        for (final Iterator<TreeNode> iter = v.ancestors.iterator(); iter.hasNext();) {
            final TreeNode w = iter.next();
            final int v_w = graph.getLink(v.vxID, w.vxID);
            if (v_w == Graph.NOT_FOUND || oldLxIDs.contains(v_w)) {
                iter.remove();
            } else {
                oldLxIDs.add(v_w);
                iter.remove();
                path.add(v);
                path.add(w);
                return path;
            }
        }

        // Check for 'new' tree edges between v and its children
        for (final Iterator<TreeNode> iter = v.children.iterator(); iter.hasNext();) {
            TreeNode w = iter.next();
            final int v_w = graph.getLink(v.vxID, w.vxID);
            if (oldLxIDs.contains(v_w)) {
                iter.remove();
            } else {
                oldLxIDs.add(v_w);
                iter.remove();
                path.add(v);
                path.add(w);
                while (!oldVxIDs.contains(w.vxID)) {
                    if (w.componentNeighours == null) {
                        setComponentNeighbours(w, t);
                    }
                    for (final Iterator<TreeNode> neighbIter = w.componentNeighours.iterator(); neighbIter.hasNext();) {
                        final TreeNode x = neighbIter.next();
                        final int w_x = graph.getLink(w.vxID, x.vxID);
                        if (oldLxIDs.contains(w_x)) {
                            neighbIter.remove();
                        } else if (!path.contains(x) && (x.preorder == w.vLow || x.vLow == w.vLow)) {
                            oldVxIDs.add(w.vxID);
                            oldLxIDs.add(w_x);
                            neighbIter.remove();
                            path.add(x);
                            w = x;
                            break;
                        }
                    }
                }
                return path;
            }
        }

        // Check for 'new' cycle edges between v and its descendants (excluding children).
        for (final Iterator<TreeNode> iter = v.descendants.iterator(); iter.hasNext();) {
            TreeNode w = iter.next();
            final int v_w = graph.getLink(v.vxID, w.vxID);
            if (v_w != Graph.NOT_FOUND && !oldLxIDs.contains(v_w)) {
                oldLxIDs.add(v_w);
                iter.remove();
                path.add(v);
                path.add(w);
                while (!oldVxIDs.contains(w.vxID)) {
                    final TreeNode x = w.parent;
                    final int w_x = graph.getLink(w.vxID, x.vxID);
                    oldVxIDs.add(w.vxID);
                    oldLxIDs.add(w_x);
                    path.add(x);
                    w = x;
                }
                return path;
            }
        }

        // Return an empty path if no applicable new edges were found.
        return path;
    }

    private NodeResult preorderVertex(final BitSet unvisitedNodes, final int vxID, final TreeNode parent, TreeNode currentRoot, int currentPreorder) {

        // Set the vertex as visited
        unvisitedNodes.clear(graph.getVertexPosition(vxID));

        // Construct the tree node for this vertex
        final TreeNode t = new TreeNode(vxID, parent);

        // Set the tree node as a root if it has no parent
        if (parent == null) {
            biconnectedComponentRoots.add(t);
            currentRoot = t;
            final Map<Integer, TreeNode> componentIDToTreeNodeMap = new HashMap<>();
            componentIDToTreeNodeMap.put(vxID, t);
            vxIDsToTreeNodes.put(t, componentIDToTreeNodeMap);
        } else {
            vxIDsToTreeNodes.get(currentRoot).put(vxID, t);
        }

        // Initialise preorder and vLow values for this vertex
        t.preorder = currentPreorder;
        t.vLow = currentPreorder;

        int nextPreorder = currentPreorder + 1;
        TreeNode n;
        for (int i = 0; i < graph.getVertexNeighbourCount(vxID); i++) {

            final int neighbourID = graph.getVertexNeighbour(vxID, i);

            // loop case
            if (neighbourID == vxID) {
                // pendant case - add as a pendant but doesn't need its own TreeNode
            } else if (isPendant(neighbourID)) {
                t.pendants.add(neighbourID);
                // unvisited neighbour case
            } else if (unvisitedNodes.get(graph.getVertexPosition(neighbourID))) {
                // preorder this vertex for depth first traversal.
                final NodeResult nr = preorderVertex(unvisitedNodes, neighbourID, t, currentRoot, nextPreorder);
                n = nr.node;
                nextPreorder = nr.preorder + 1;

                // update this vertice's vLow if this child has a lower vLow
                if (n.vLow < t.vLow) {
                    t.vLow = n.vLow;
                }

                // we have detected an articulation point
                if ((n.vLow == t.preorder && t.preorder != 1)
                        || (t.preorder == 1 && !t.children.isEmpty())) {

                    // check whether n is a 'defacto pendant', that is all children are pendants and it is a child of the articulation point
                    if (n.children.isEmpty()) {
                        t.children.remove(n);
                        t.defactoPendants.add(n);
                        vxIDsToTreeNodes.get(currentRoot).remove(neighbourID);
                        // Revert the next preorder back to the one after the current preorder
                        nextPreorder = currentPreorder + 1;
                        continue;
                    }

                    // Make a new TreeRoot and add it to the relevant maps
                    final TreeNode newRoot = new TreeNode(vxID, null);
                    final Map<Integer, TreeNode> componentIDToTreeNodeMap = new HashMap<>();
                    componentIDToTreeNodeMap.put(vxID, newRoot);
                    vxIDsToTreeNodes.put(newRoot, componentIDToTreeNodeMap);
                    biconnectedComponentRoots.add(newRoot);

                    // Make the offending neighbour a child of the new root instead.
                    t.children.remove(n);
                    newRoot.children.add(n);
                    newRoot.descendants.add(n);
                    newRoot.descendants.addAll(n.descendants);
                    n.parent = newRoot;

                    // Update the preorder and vLow values of newRoot and the subtree of which it is the root.
                    newRoot.preorder = 1;
                    newRoot.vLow = 1;
                    updatePreorderForNewComponent(n, t.preorder, currentPreorder, newRoot, currentRoot);

                    // Revert the next preorder back to the one after the current preorder
                    nextPreorder = currentPreorder + 1;
                } else {
                    // If the curerntly processed neighbour was not an articulartion point, update the current preorder.
                    currentPreorder = nr.preorder;
                    // Add this neighbour to the current vertice's children and descendants
                    t.children.add(n);
                    t.descendants.addAll(n.children);
                    t.descendants.addAll(n.descendants);
                }

                // visited neighbour case
            } else {
                // Retrieve the TreeNode corresponding to this neighbour.
                n = vxIDsToTreeNodes.get(currentRoot).get(neighbourID);

                // the neighbour has been visited but is a pendant (no tree node exists for it), then skip it.
                if (n == null) {
                    continue;
                }

                // update this vertice's vLow if this non-descendant neighbour has a lower preorder
                if (n.preorder < t.vLow) {
                    t.vLow = n.preorder;
                }
            }
        }

        return new NodeResult(t, currentPreorder);
    }

    private boolean isPendant(final int vxID) {
        final int numNeighbours = graph.getVertexNeighbourCount(vxID);
        final boolean hasLoop = graph.getLink(vxID, vxID) != Graph.NOT_FOUND;
        return (numNeighbours == 1 && !hasLoop) || (numNeighbours == 2 && hasLoop);
    }

    private boolean isSingleton(final int vxID) {
        final int numNeighbours = graph.getVertexNeighbourCount(vxID);
        final boolean hasLoop = graph.getLink(vxID, vxID) != Graph.NOT_FOUND;
        return (numNeighbours == 0) || (numNeighbours == 1 && hasLoop);
    }

    private void updatePreorderForNewComponent(final TreeNode toAmend, final int articulationPreorder, final int currentPreorder, final TreeNode newRoot, final TreeNode currentRoot) {
        vxIDsToTreeNodes.get(currentRoot).remove(toAmend.vxID);
        vxIDsToTreeNodes.get(newRoot).put(toAmend.vxID, toAmend);
        toAmend.preorder -= (currentPreorder - 1);
        toAmend.vLow = toAmend.vLow == articulationPreorder ? 1 : toAmend.vLow - (currentPreorder - 1);
        for (final TreeNode child : toAmend.children) {
            updatePreorderForNewComponent(child, articulationPreorder, currentPreorder, newRoot, currentRoot);
        }
    }

}
