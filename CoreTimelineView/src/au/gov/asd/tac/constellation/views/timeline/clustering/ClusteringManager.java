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
package au.gov.asd.tac.constellation.views.timeline.clustering;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.attribute.DateAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import au.gov.asd.tac.constellation.utilities.temporal.TemporalConstants;
import au.gov.asd.tac.constellation.views.timeline.TimeExtents;
import au.gov.asd.tac.constellation.views.timeline.TimelineTopComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Clustering Manager
 *
 * @author betelgeuse
 */
public class ClusteringManager {

    final List<TreeLeaf> leaves = new ArrayList<>();
    private TreeElement tree;
    private Set<TreeElement> elementsToDraw = new HashSet<>();
    private Set<TreeElement> elementsToUndim = new HashSet<>();
    private Set<TreeElement> oldElementsToUndim;
    private final Map<Integer, Integer> undimmedVerticesOnGraph = new HashMap<>();
    private Set<TreeElement> elementsToUnhide = new HashSet<>();
    private Set<TreeElement> oldElementsToUnhide;
    private final Map<Integer, Integer> unhiddenVerticesOnGraph = new HashMap<>();

    public TimeExtents generateTree(final GraphReadMethods graph, final String datetimeAttribute, final boolean selectedOnly) {
        final int transactionCount = graph.getTransactionCount();
        final int datetimeAttributeId = graph.getAttribute(GraphElementType.TRANSACTION, datetimeAttribute);
        final int selectedTransAttributeId = VisualConcept.TransactionAttribute.SELECTED.get(graph);
        final int selectedNodeAttributeId = VisualConcept.VertexAttribute.SELECTED.get(graph);

        // If we actually have a the attributes
        if (datetimeAttributeId != Graph.NOT_FOUND && selectedNodeAttributeId != Graph.NOT_FOUND && selectedTransAttributeId != Graph.NOT_FOUND) {
            // Grab all of the transactions off the graph and turn into leaves:
            for (int i = 0; i < transactionCount; i++) {
                final int transactionID = graph.getTransaction(i);
                final String datetimeAttributeType = graph.getAttributeType(datetimeAttributeId);
                final Object datetimeAttributeDefault = graph.getAttributeDefaultValue(datetimeAttributeId);
                final Object datetimeAttributeValue = graph.getObjectValue(datetimeAttributeId, transactionID);

                if (TimelineTopComponent.SUPPORTED_DATETIME_ATTRIBUTE_TYPES.contains(datetimeAttributeType)
                        && datetimeAttributeValue != null && !datetimeAttributeValue.equals(datetimeAttributeDefault)) {
                    final int vertexA = graph.getTransactionSourceVertex(transactionID);
                    final int vertexB = graph.getTransactionDestinationVertex(transactionID);

                    final int lowerY = Math.min(vertexA, vertexB);
                    final int upperY = Math.max(vertexA, vertexB);

                    long transactionValue = graph.getLongValue(datetimeAttributeId, transactionID);

                    // Dates are represented as days since epoch, whereas datetimes are represented as milliseconds since epoch
                    if (datetimeAttributeType.equals(DateAttributeDescription.ATTRIBUTE_NAME)) {
                        transactionValue = transactionValue * TemporalConstants.MILLISECONDS_IN_DAY;
                    }

                    final boolean isSelected = graph.getBooleanValue(selectedTransAttributeId, transactionID);
                    if (isSelected || !selectedOnly) {
                        final boolean nodesSelected = graph.getBooleanValue(selectedNodeAttributeId, vertexA) || graph.getBooleanValue(selectedNodeAttributeId, vertexB);
                        leaves.add(new TreeLeaf(transactionID, transactionValue, isSelected, nodesSelected, lowerY, upperY, Math.min(vertexA, vertexB), Math.max(vertexA, vertexB)));
                    }
                }
            }

            if (leaves.isEmpty()) {
                tree = null;
                return null;
            } else if (leaves.size() == 1) {
                tree = leaves.get(0);
                return new TimeExtents(tree.getLowerTimeExtent(), tree.getUpperTimeExtent());
            }

            Collections.sort(leaves);
            TreeNode lastNode = null;

            final TreeNode[] nodes = new TreeNode[leaves.size() - 1];

            for (int i = 0; i < (leaves.size() - 1); i++) {
                final TreeNode nextNode = new TreeNode(leaves.get(i), leaves.get(i + 1));
                nextNode.previous = lastNode;
                if (lastNode != null) {
                    lastNode.next = nextNode;
                }
                lastNode = nextNode;
                nodes[i] = nextNode;
            }

            Arrays.sort(nodes);

            for (int i = 0; i < nodes.length - 1; i++) {
                final TreeNode highest = nodes[i];

                if (highest.previous != null) {
                    highest.previous.setLastChild(highest);
                    highest.previous.next = highest.next;
                }
                if (highest.next != null) {
                    highest.next.setFirstChild(highest);
                    highest.next.previous = highest.previous;
                }
            }

            tree = nodes[nodes.length - 1];
        } else {
            tree = null;
        }

        return tree != null ? new TimeExtents(tree.getLowerTimeExtent(), tree.getUpperTimeExtent()) : null;
    }

    public void filterTree(final double pixelsPerTransaction, final long lowerTimeExtent, final long upperTimeExtent) {
        final List<TreeElement> stack = new ArrayList<>();
        elementsToDraw = new HashSet<>();
        if (tree == null) {
            return;
        }
        stack.add(tree);

        while (!stack.isEmpty()) {
            final TreeElement te = stack.remove(stack.size() - 1);
            if (te.getUpperTimeExtent() < lowerTimeExtent || te.getLowerTimeExtent() > upperTimeExtent) {
                continue;
            }

            if (te instanceof TreeLeaf) {
                elementsToDraw.add(te);
            } else {
                final TreeNode node = (TreeNode) te;

                final double gap = (double) node.lastChild.getLowerTimeExtent() - node.firstChild.getUpperTimeExtent();
                if (gap <= pixelsPerTransaction && (node.getLowerTimeExtent() < node.getUpperTimeExtent())) {
                    elementsToDraw.add(node);
                } else {
                    stack.add(node.firstChild);
                    stack.add(node.lastChild);
                }
            }
        }
    }

    public void dimOrHideTree(final long lowerTimeExtent, final long upperTimeExtent, int exclusionState) {
        final List<TreeElement> stack = new ArrayList<>();

        stack.add(tree);

        oldElementsToUndim = elementsToUndim;
        elementsToUndim = new HashSet<>();
        oldElementsToUnhide = elementsToUnhide;
        elementsToUnhide = new HashSet<>();

        if (tree != null) {
            while (!stack.isEmpty()) {
                final TreeElement te = stack.remove(stack.size() - 1);
                if (te.getUpperTimeExtent() < lowerTimeExtent || te.getLowerTimeExtent() > upperTimeExtent) {
                    continue;
                }

                if (te.getUpperTimeExtent() <= upperTimeExtent && te.getLowerTimeExtent() >= lowerTimeExtent) {
                    if (exclusionState == 1) {
                        elementsToUndim.add(te);
                    } else if (exclusionState == 2) {
                        elementsToUnhide.add(te);
                    }
                } else {
                    final TreeNode node = (TreeNode) te;
                    stack.add(node.firstChild);
                    stack.add(node.lastChild);
                }
            }
        }
    }

    public long getLowestObservedTime() {
        return tree.getLowerTimeExtent();
    }

    public long getHighestObservedTime() {
        return tree.getUpperTimeExtent();
    }

    public Set<TreeElement> getElementsToDraw() {
        return Collections.unmodifiableSet(elementsToDraw);
    }

    public void clearTree() {
        tree = null;
        leaves.clear();
    }

    @FunctionalInterface
    public static interface ExclusionStateNotifier {

        public void exclusionStateNotify(final long vxModCount, final long txModCount);
    }

    @PluginInfo(tags = {PluginTags.MODIFY})
    public final class InitDimOrHidePlugin extends SimplePlugin {

        final long lowerTimeExtent;
        final long upperTimeExtent;
        final int exclusionState;
        final ExclusionStateNotifier exclusionStateNotifier;

        public InitDimOrHidePlugin(final long lowerTimeExtent, final long upperTimeExtent, 
                final int exclusionState, final ExclusionStateNotifier exclusionStateNotifier) {
            this.lowerTimeExtent = lowerTimeExtent;
            this.upperTimeExtent = upperTimeExtent;
            this.exclusionState = exclusionState;
            this.exclusionStateNotifier = exclusionStateNotifier;
        }

        @Override
        public String getName() {
            return "Timeline: Initialise Dimming or Hiding";
        }

        @Override
        protected void execute(final PluginGraphs graph, final PluginInteraction interaction,
                final PluginParameters parameters) throws InterruptedException {
            final WritableGraph wg = graph.getGraph().getWritableGraph("Initialising dimming or hiding from timeline.", false, this);
            try {
                final int vertDimAttr = VisualConcept.VertexAttribute.DIMMED.ensure(wg);
                final int transDimAttr = VisualConcept.TransactionAttribute.DIMMED.ensure(wg);
                final int vertHideAttr = VisualConcept.VertexAttribute.VISIBILITY.ensure(wg);
                final int transHideAttr = VisualConcept.TransactionAttribute.VISIBILITY.ensure(wg);

                elementsToUndim = null;
                elementsToUnhide = null;
                dimOrHideTree(lowerTimeExtent, upperTimeExtent, exclusionState);

                final Set<Integer> transactionsToUndim = new HashSet<>();
                final Set<Integer> transactionsToUnhide = new HashSet<>();

                undimmedVerticesOnGraph.clear();
                unhiddenVerticesOnGraph.clear();

                final List<TreeElement> stack = new ArrayList<>();
                if (exclusionState == 1) {
                    for (final TreeElement te : elementsToUndim) {
                        if (te instanceof TreeLeaf leaf) {
                            transactionsToUndim.add(leaf.getId());
                            final Integer countA = undimmedVerticesOnGraph.get(leaf.vertexIdA);
                            final Integer countB = undimmedVerticesOnGraph.get(leaf.vertexIdB);
                            undimmedVerticesOnGraph.put(leaf.vertexIdA, countA == null ? 1 : countA + 1);
                            undimmedVerticesOnGraph.put(leaf.vertexIdB, countB == null ? 1 : countB + 1);
                        } else {
                            stack.add(te);
                            while (!stack.isEmpty()) {
                                final TreeElement element = stack.remove(stack.size() - 1);
                                
                                if (element instanceof TreeLeaf leaf) {
                                    transactionsToUndim.add(leaf.getId());

                                    final Integer countA = undimmedVerticesOnGraph.get(leaf.vertexIdA);
                                    final Integer countB = undimmedVerticesOnGraph.get(leaf.vertexIdB);
                                    undimmedVerticesOnGraph.put(leaf.vertexIdA, countA == null ? 1 : countA + 1);
                                    undimmedVerticesOnGraph.put(leaf.vertexIdB, countB == null ? 1 : countB + 1);
                                } else {
                                    final TreeNode node = (TreeNode) element;
                                    stack.add(node.firstChild);
                                    stack.add(node.lastChild);
                                }
                            }
                        }
                    }
                } else if (exclusionState == 2) {
                    for (final TreeElement te : elementsToUnhide) {
                        if (te instanceof TreeLeaf leaf) {
                            transactionsToUnhide.add(leaf.getId());
                            final Integer countA = unhiddenVerticesOnGraph.get(leaf.vertexIdA);
                            final Integer countB = unhiddenVerticesOnGraph.get(leaf.vertexIdB);
                            unhiddenVerticesOnGraph.put(leaf.vertexIdA, countA == null ? 1 : countA + 1);
                            unhiddenVerticesOnGraph.put(leaf.vertexIdB, countB == null ? 1 : countB + 1);
                        } else {
                            stack.add(te);
                            while (!stack.isEmpty()) {
                                final TreeElement element = stack.remove(stack.size() - 1);

                                if (element instanceof TreeLeaf leaf) {
                                    transactionsToUnhide.add(leaf.getId());

                                    final Integer countA = unhiddenVerticesOnGraph.get(leaf.vertexIdA);
                                    final Integer countB = unhiddenVerticesOnGraph.get(leaf.vertexIdB);
                                    unhiddenVerticesOnGraph.put(leaf.vertexIdA, countA == null ? 1 : countA + 1);
                                    unhiddenVerticesOnGraph.put(leaf.vertexIdB, countB == null ? 1 : countB + 1);
                                } else {
                                    final TreeNode node = (TreeNode) element;

                                    stack.add(node.firstChild);
                                    stack.add(node.lastChild);
                                }
                            }
                        }
                    }
                }

                for (int pos = 0; pos < wg.getTransactionCount(); pos++) {
                    int txID = wg.getTransaction(pos);
                    wg.setBooleanValue(transDimAttr, txID, !transactionsToUndim.contains(txID) && exclusionState == 1);
                    wg.setIntValue(transHideAttr, txID, (!transactionsToUnhide.contains(txID) && exclusionState == 2) ? 0 : 1);
                }
                for (int pos = 0; pos < wg.getVertexCount(); pos++) {
                    int vxID = wg.getVertex(pos);
                    wg.setBooleanValue(vertDimAttr, vxID, !undimmedVerticesOnGraph.containsKey(vxID) && exclusionState == 1);
                    wg.setIntValue(vertHideAttr, vxID, (!unhiddenVerticesOnGraph.containsKey(vxID) && exclusionState == 2) ? 0 : 1);
                }
                exclusionStateNotifier.exclusionStateNotify(wg.getValueModificationCounter(vertDimAttr), wg.getValueModificationCounter(transDimAttr));
            } finally {
                wg.commit();
            }
        }
    }

    // TODO: Adding "LOW LEVEL" to omit this plugin flooding the Notes View with
    // this call every time a selection is made and the timeline view is open.
    // Review why this plugin is called so oftern and see if there is a need for
    // this to even be a plugin if there is no direct user interaction required
    // manually or programatically. If not then perhaps this should not be a
    // plugin but rather a function call.
    @PluginInfo(tags = {PluginTags.LOW_LEVEL, PluginTags.MODIFY})
    public final class UpdateDimOrHidePlugin extends SimplePlugin {

        final long lowerTimeExtent;
        final long upperTimeExtent;
        final int exclusionState;
        final ExclusionStateNotifier exclusionStateNotifier;

        public UpdateDimOrHidePlugin(final long lowerTimeExtent, final long upperTimeExtent, final int exclusionState, final ExclusionStateNotifier exclusionStateNotifier) {
            this.lowerTimeExtent = lowerTimeExtent;
            this.upperTimeExtent = upperTimeExtent;
            this.exclusionState = exclusionState;
            this.exclusionStateNotifier = exclusionStateNotifier;
        }

        @Override
        public String getName() {
            return "Timeline: Update Dimming or Hiding";
        }

        @Override
        protected void execute(final PluginGraphs graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
            final Set<Integer> verticesToBeUndimmed = new HashSet<>();
            final Set<Integer> verticesToBeDimmed = new HashSet<>();
            final Set<Integer> verticesToBeUnhidden = new HashSet<>();
            final Set<Integer> verticesToBeHidden = new HashSet<>();

            final WritableGraph wg = graph.getGraph().getWritableGraph(getName(), false, this);
            try {
                dimOrHideTree(lowerTimeExtent, upperTimeExtent, exclusionState);

                final int vertDimAttr = VisualConcept.VertexAttribute.DIMMED.ensure(wg);
                final int transDimAttr = VisualConcept.TransactionAttribute.DIMMED.ensure(wg);
                final int vertHideAttr = VisualConcept.VertexAttribute.VISIBILITY.ensure(wg);
                final int transHideAttr = VisualConcept.TransactionAttribute.VISIBILITY.ensure(wg);

                final List<TreeElement> stack = new ArrayList<>();

                if (exclusionState == 1) {
                    for (final TreeElement te : oldElementsToUndim) {
                        if (!elementsToUndim.contains(te)) {
                            if (te instanceof TreeLeaf leaf) {
                                final int countA = undimmedVerticesOnGraph.get(leaf.vertexIdA) - 1;
                                final int countB = undimmedVerticesOnGraph.get(leaf.vertexIdB) - 1;

                                if (countA == 0) {
                                    verticesToBeDimmed.add(leaf.vertexIdA);
                                    undimmedVerticesOnGraph.remove(leaf.vertexIdA);
                                } else {
                                    undimmedVerticesOnGraph.put(leaf.vertexIdA, countA);
                                }
                                if (countB == 0) {
                                    verticesToBeDimmed.add(leaf.vertexIdB);
                                    undimmedVerticesOnGraph.remove(leaf.vertexIdB);
                                } else {
                                    undimmedVerticesOnGraph.put(leaf.vertexIdB, countB);
                                }

                                wg.setBooleanValue(transDimAttr, leaf.getId(), true);
                                wg.setIntValue(transHideAttr, leaf.getId(), 1);
                            } else {
                                stack.add(te);
                                while (!stack.isEmpty()) {
                                    TreeElement element = stack.remove(stack.size() - 1);

                                    if (element instanceof TreeLeaf leaf) {
                                        int countA = undimmedVerticesOnGraph.get(leaf.vertexIdA) - 1;
                                        int countB = undimmedVerticesOnGraph.get(leaf.vertexIdB) - 1;

                                        if (countA == 0) {
                                            verticesToBeDimmed.add(leaf.vertexIdA);
                                            undimmedVerticesOnGraph.remove(leaf.vertexIdA);
                                        } else {
                                            undimmedVerticesOnGraph.put(leaf.vertexIdA, countA);
                                        }
                                        if (countB == 0) {
                                            verticesToBeDimmed.add(leaf.vertexIdB);
                                            undimmedVerticesOnGraph.remove(leaf.vertexIdB);
                                        } else {
                                            undimmedVerticesOnGraph.put(leaf.vertexIdB, countB);
                                        }

                                        wg.setBooleanValue(transDimAttr, leaf.getId(), true);
                                        wg.setIntValue(transHideAttr, leaf.getId(), 1);
                                    } else {
                                        TreeNode node = (TreeNode) element;

                                        stack.add(node.firstChild);
                                        stack.add(node.lastChild);
                                    }
                                }
                            }
                        }
                    }

                    for (final TreeElement te : elementsToUndim) {
                        if (!oldElementsToUndim.contains(te)) {
                            if (te instanceof TreeLeaf leaf) {
                                wg.setBooleanValue(transDimAttr, leaf.getId(), false);
                                wg.setIntValue(transHideAttr, leaf.getId(), 1);

                                final Integer countA = undimmedVerticesOnGraph.get(leaf.vertexIdA);
                                final Integer countB = undimmedVerticesOnGraph.get(leaf.vertexIdB);
                                if (countA == null) {
                                    undimmedVerticesOnGraph.put(leaf.vertexIdA, 1);
                                    verticesToBeUndimmed.add(leaf.vertexIdA);
                                } else {
                                    undimmedVerticesOnGraph.put(leaf.vertexIdA, countA + 1);
                                }
                                if (countB == null) {
                                    undimmedVerticesOnGraph.put(leaf.vertexIdB, 1);
                                    verticesToBeUndimmed.add(leaf.vertexIdB);
                                } else {
                                    undimmedVerticesOnGraph.put(leaf.vertexIdB, countB + 1);
                                }
                            } else {
                                stack.add(te);
                                while (!stack.isEmpty()) {
                                    TreeElement element = stack.remove(stack.size() - 1);

                                    if (element instanceof TreeLeaf leaf) {
                                        wg.setBooleanValue(transDimAttr, leaf.getId(), false);
                                        wg.setIntValue(transHideAttr, leaf.getId(), 1);

                                        final Integer countA = undimmedVerticesOnGraph.get(leaf.vertexIdA);
                                        final Integer countB = undimmedVerticesOnGraph.get(leaf.vertexIdB);
                                        if (countA == null) {
                                            undimmedVerticesOnGraph.put(leaf.vertexIdA, 1);
                                            verticesToBeUndimmed.add(leaf.vertexIdA);
                                        } else {
                                            undimmedVerticesOnGraph.put(leaf.vertexIdA, countA + 1);
                                        }
                                        if (countB == null) {
                                            undimmedVerticesOnGraph.put(leaf.vertexIdB, 1);
                                            verticesToBeUndimmed.add(leaf.vertexIdB);
                                        } else {
                                            undimmedVerticesOnGraph.put(leaf.vertexIdB, countB + 1);
                                        }
                                    } else {
                                        TreeNode node = (TreeNode) element;

                                        stack.add(node.firstChild);
                                        stack.add(node.lastChild);
                                    }
                                }
                            }
                        }
                    }
                } else if (exclusionState == 2) {
                    for (final TreeElement te : oldElementsToUnhide) {
                        if (!elementsToUnhide.contains(te)) {
                            if (te instanceof TreeLeaf leaf) {
                                final int countA = unhiddenVerticesOnGraph.get(leaf.vertexIdA) - 1;
                                final int countB = unhiddenVerticesOnGraph.get(leaf.vertexIdB) - 1;

                                if (countA == 0) {
                                    verticesToBeHidden.add(leaf.vertexIdA);
                                    unhiddenVerticesOnGraph.remove(leaf.vertexIdA);
                                } else {
                                    unhiddenVerticesOnGraph.put(leaf.vertexIdA, countA);
                                }
                                if (countB == 0) {
                                    verticesToBeHidden.add(leaf.vertexIdB);
                                    unhiddenVerticesOnGraph.remove(leaf.vertexIdB);
                                } else {
                                    unhiddenVerticesOnGraph.put(leaf.vertexIdB, countB);
                                }

                                wg.setIntValue(transHideAttr, leaf.getId(), 0);
                                wg.setBooleanValue(transDimAttr, leaf.getId(), false);
                            } else {
                                stack.add(te);
                                while (!stack.isEmpty()) {
                                    final TreeElement element = stack.remove(stack.size() - 1);

                                    if (element instanceof TreeLeaf leaf) {
                                        final int countA = unhiddenVerticesOnGraph.get(leaf.vertexIdA) - 1;
                                        final int countB = unhiddenVerticesOnGraph.get(leaf.vertexIdB) - 1;

                                        if (countA == 0) {
                                            verticesToBeHidden.add(leaf.vertexIdA);
                                            unhiddenVerticesOnGraph.remove(leaf.vertexIdA);
                                        } else {
                                            unhiddenVerticesOnGraph.put(leaf.vertexIdA, countA);
                                        }
                                        if (countB == 0) {
                                            verticesToBeHidden.add(leaf.vertexIdB);
                                            unhiddenVerticesOnGraph.remove(leaf.vertexIdB);
                                        } else {
                                            unhiddenVerticesOnGraph.put(leaf.vertexIdB, countB);
                                        }

                                        wg.setIntValue(transHideAttr, leaf.getId(), 0);
                                        wg.setBooleanValue(transDimAttr, leaf.getId(), false);
                                    } else {
                                        final TreeNode node = (TreeNode) element;

                                        stack.add(node.firstChild);
                                        stack.add(node.lastChild);
                                    }
                                }
                            }
                        }
                    }

                    for (final TreeElement te : elementsToUnhide) {
                        if (!oldElementsToUnhide.contains(te)) {
                            if (te instanceof TreeLeaf leaf) {
                                wg.setIntValue(transHideAttr, leaf.getId(), 1);
                                wg.setBooleanValue(transDimAttr, leaf.getId(), false);

                                final Integer countA = unhiddenVerticesOnGraph.get(leaf.vertexIdA);
                                final Integer countB = unhiddenVerticesOnGraph.get(leaf.vertexIdB);
                                if (countA == null) {
                                    unhiddenVerticesOnGraph.put(leaf.vertexIdA, 1);
                                    verticesToBeUnhidden.add(leaf.vertexIdA);
                                } else {
                                    unhiddenVerticesOnGraph.put(leaf.vertexIdA, countA + 1);
                                }
                                if (countB == null) {
                                    unhiddenVerticesOnGraph.put(leaf.vertexIdB, 1);
                                    verticesToBeUnhidden.add(leaf.vertexIdB);
                                } else {
                                    unhiddenVerticesOnGraph.put(leaf.vertexIdB, countB + 1);
                                }
                            } else {
                                stack.add(te);
                                while (!stack.isEmpty()) {
                                    final TreeElement element = stack.remove(stack.size() - 1);

                                    if (element instanceof TreeLeaf leaf) {
                                        wg.setIntValue(transHideAttr, leaf.getId(), 1);
                                        wg.setBooleanValue(transDimAttr, leaf.getId(), false);

                                        final Integer countA = unhiddenVerticesOnGraph.get(leaf.vertexIdA);
                                        final Integer countB = unhiddenVerticesOnGraph.get(leaf.vertexIdB);
                                        if (countA == null) {
                                            unhiddenVerticesOnGraph.put(leaf.vertexIdA, 1);
                                            verticesToBeUnhidden.add(leaf.vertexIdA);
                                        } else {
                                            unhiddenVerticesOnGraph.put(leaf.vertexIdA, countA + 1);
                                        }
                                        if (countB == null) {
                                            unhiddenVerticesOnGraph.put(leaf.vertexIdB, 1);
                                            verticesToBeUnhidden.add(leaf.vertexIdB);
                                        } else {
                                            unhiddenVerticesOnGraph.put(leaf.vertexIdB, countB + 1);
                                        }
                                    } else {
                                        final TreeNode node = (TreeNode) element;

                                        stack.add(node.firstChild);
                                        stack.add(node.lastChild);
                                    }
                                }
                            }
                        }
                    }
                }

                for (final Integer vertexId : verticesToBeUndimmed) {
                    if (!verticesToBeDimmed.contains(vertexId)) {
                        wg.setBooleanValue(vertDimAttr, vertexId, false);
                        wg.setIntValue(vertHideAttr, vertexId, 1);
                    }
                }
                for (final Integer vertexId : verticesToBeDimmed) {
                    if (!verticesToBeUndimmed.contains(vertexId)) {
                        wg.setBooleanValue(vertDimAttr, vertexId, true);
                        wg.setIntValue(vertHideAttr, vertexId, 1);
                    }
                }

                for (final Integer vertexId : verticesToBeUnhidden) {
                    if (!verticesToBeHidden.contains(vertexId)) {
                        wg.setIntValue(vertHideAttr, vertexId, 1);
                        wg.setBooleanValue(vertDimAttr, vertexId, false);
                    }
                }
                for (final Integer vertexId : verticesToBeHidden) {
                    if (!verticesToBeUnhidden.contains(vertexId)) {
                        wg.setIntValue(vertHideAttr, vertexId, 0);
                        wg.setBooleanValue(vertDimAttr, vertexId, false);
                    }
                }
                exclusionStateNotifier.exclusionStateNotify(wg.getValueModificationCounter(vertDimAttr), wg.getValueModificationCounter(transDimAttr));
            } finally {
                wg.commit();
            }
        }
    }
}
