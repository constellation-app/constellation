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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.hierarchical;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.ClusteringConcept;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Execute the Fast Newman function which clusters the graph hierarchically by
 * initially placing all vertices in their own cluster and then iteratively
 * merging clusters according to a weight function until the optimal state is
 * reached.
 *
 * @author sirius
 */
public class FastNewman {

    public static void run(final GraphWriteMethods graph, final PluginInteraction interaction, final boolean interactive) throws InterruptedException {
        run(graph, interaction, interactive, new HashSet<>(), AnalyticConcept.VertexAttribute.WEIGHT.getName());
    }

    public static void run(final GraphWriteMethods graph, final PluginInteraction interaction, final boolean interactive, final Set<Integer> initialLinkIds, String weightAttribute) throws InterruptedException {

        final int vertexCount = graph.getVertexCount();
        final int linkCount = graph.getLinkCount();
        final int transactionCount = graph.getTransactionCount();

        final ConstellationColor[] colors = ConstellationColor.createPalette(vertexCount, 0.5f, 0.95f);
        int nextColor = vertexCount - 1;

        final Group[] groups = new Group[graph.getVertexCapacity()];
        final NavigableSet<Link> links = new TreeSet<>();

        int weightAttributeId = Graph.NOT_FOUND;
        if (weightAttribute != null) {
            weightAttributeId = graph.getAttribute(GraphElementType.TRANSACTION, weightAttribute);
        }
        float totalWeight = 0;
        if (weightAttributeId != Graph.NOT_FOUND) {
            for (int p = 0; p < transactionCount; p++) {
                final int transaction = graph.getTransaction(p);
                if (graph.getTransactionSourceVertex(transaction) != graph.getTransactionDestinationVertex(transaction)) {
                    totalWeight += graph.getFloatValue(weightAttributeId, transaction);
                }
            }
        }

        // Once a Group is added to the array, the value of vertex doesn't change,
        // and the Group isn't subsequently moved. Therefore, the index of a Group
        // is it's position. Therefore, the index of groups[i].parent can be found
        // by graph.getVertexPosition(parent.vertex);
        for (int position = 0; position < vertexCount; position++) {
            final int vxId = graph.getVertex(position);

            groups[position] = new Group();
            groups[position].vertex = vxId;
            if (weightAttributeId == Graph.NOT_FOUND) {
                groups[position].weight = (float) graph.getVertexTransactionCount(vxId) / transactionCount;
            } else {
                final int vertexTransactionCount = graph.getVertexTransactionCount(vxId);
                for (int p = 0; p < vertexTransactionCount; p++) {
                    final int transaction = graph.getVertexTransaction(vxId, p);
                    groups[position].weight += graph.getFloatValue(weightAttributeId, transaction);
                }
                groups[position].weight /= totalWeight;
            }
        }

        for (int p = 0; p < linkCount; p++) {
            final int linkId = graph.getLink(p);
            final int highVertex = graph.getLinkHighVertex(linkId);
            final int lowVertex = graph.getLinkLowVertex(linkId);

            if (highVertex != lowVertex) {
                final Link link = new Link();

                // Connect the link to its groups
                link.highGroup = groups[graph.getVertexPosition(highVertex)];
                link.lowGroup = groups[graph.getVertexPosition(lowVertex)];
                link.highGroup.links.put(link.lowGroup, link);
                link.lowGroup.links.put(link.highGroup, link);

                if (weightAttributeId == Graph.NOT_FOUND) {
                    link.weight = (float) graph.getLinkTransactionCount(linkId) / transactionCount;
                } else {
                    final int linkTransactionCount = graph.getLinkTransactionCount(linkId);
                    for (int tp = 0; tp < linkTransactionCount; tp++) {
                        final int transaction = graph.getLinkTransaction(linkId, tp);
                        link.weight += graph.getFloatValue(weightAttributeId, transaction);
                    }
                    link.weight /= totalWeight;
                }
                link.deltaQ = 2f * (link.weight - link.highGroup.weight * link.lowGroup.weight);

                link.initial = initialLinkIds.contains(linkId);
                links.add(link);
            }
        }

        int step = 0;
        float q = 0;

        int maxStep = 0;
        float maxQ = -Float.MAX_VALUE;
        boolean initialising = true;

        while (!links.isEmpty()) {

            interaction.setProgress(step, vertexCount - 1, "Merging groups...", true);

            // Get the link with the highest deltaQ and remove it from the link set
            final Link removedLink = links.first();
            links.remove(removedLink);

            // End initialisation and move onto the first proper step
            if (initialising && !removedLink.initial) {
                initialising = false;
                step++;
            }

            // Update Q
            q += removedLink.deltaQ;

            if (q > maxQ) {
                maxQ = q;
                maxStep = step;
            }

            // Get the parent and child groups for the link
            Group parent = removedLink.highGroup;
            Group child = removedLink.lowGroup;

            // Ensure that the parent has the greater weight
            if (parent.weight < child.weight) {
                final Group temp = parent;
                parent = child;
                child = temp;
            }

            parent.links.remove(child);
            child.links.remove(parent);

            child.color = colors[nextColor--];

            if (parent.singleStep > step) {
                parent.singleStep = step;
            }

            if (child.singleStep > step) {
                child.singleStep = step;
            }

            child.parent = parent;
            child.mergeStep = step;
            parent.weight += child.weight;

            // Process each link that currently connects to the child...
            for (final Entry<Group, Link> childLinkEntry : child.links.entrySet()) {

                final Group otherGroup = childLinkEntry.getKey();
                final Link childLink = childLinkEntry.getValue();
                final Link parentLink = parent.links.get(otherGroup);

                // If the parent has no link to the other vertex then move the child link to the parent
                if (parentLink == null) {

                    // Move the link from the child to the parent
                    if (childLink.highGroup == child) {
                        childLink.highGroup = parent;
                    } else {
                        childLink.lowGroup = parent;
                    }
                    parent.links.put(otherGroup, childLink);
                    otherGroup.links.remove(child);
                    otherGroup.links.put(parent, childLink);

                    // Update the deltaQ for the link
                    links.remove(childLink);
                    childLink.deltaQ = 2 * (childLink.weight - childLink.highGroup.weight * childLink.lowGroup.weight);
                    links.add(childLink);

                    // If the parent also has a link to the other vertex then update the parent link and remove the child link
                } else {

                    // Update the weight and deltaQ for the parent link
                    links.remove(parentLink);
                    parentLink.weight += childLink.weight;
                    parentLink.deltaQ = 2 * (parentLink.weight - parentLink.highGroup.weight * parentLink.lowGroup.weight);
                    links.add(parentLink);

                    // Remove the child link
                    links.remove(childLink);
                    otherGroup.links.remove(child);
                }
            }

            // Clear the child links because they are no longer needed and are consuming memory
            child.links = null;

            // Move on to the next step if we are not doing initialisation any more
            if (!initialising) {
                step++;
            }
        }

        for (int p = 0; p < vertexCount; p++) {
            final int vertex = graph.getVertex(p);
            final Group group = groups[graph.getVertexPosition(vertex)];
            if (group.color == null) {
                group.color = colors[nextColor--];
            }
        }

        final int coiAttr = ClusteringConcept.MetaAttribute.HIERARCHICAL_CLUSTERING_STATE.ensure(graph);
        final HierarchicalState state = new HierarchicalState(step - 1, maxStep, groups, graph.getVertexCapacity());
        state.setStrucModificationCount(graph.getStructureModificationCounter());
        state.setInteractive(interactive);
        graph.setObjectValue(coiAttr, 0, state);
    }

    public static class Group {

        private int vertex;
        private float weight = 0.0f;
        private Group parent = null;
        private int mergeStep = Integer.MAX_VALUE;
        private Map<Group, Link> links = new HashMap<>();
        private ConstellationColor color;
        private int singleStep = Integer.MAX_VALUE;

        public int getVertex() {
            return vertex;
        }

        public void setVertex(final int vertex) {
            this.vertex = vertex;
        }

        public Group getParent() {
            return parent;
        }

        public void setParent(final Group parent) {
            this.parent = parent;
        }

        public int getMergeStep() {
            return mergeStep;
        }

        public void setMergeStep(final int mergeStep) {
            this.mergeStep = mergeStep;
        }

        public ConstellationColor getColor() {
            return color;
        }

        public void setColor(final ConstellationColor color) {
            this.color = color;
        }

        public int getSingleStep() {
            return singleStep;
        }

        public void setSingleStep(final int singleStep) {
            this.singleStep = singleStep;
        }
    }

    private static class Link implements Comparable<Link> {

        private Group highGroup;
        private Group lowGroup;
        private float weight = 0.0f;
        private float deltaQ;
        private boolean initial = false;

        @Override
        public int compareTo(final Link o) {
            if (initial && !o.initial) {
                return -1;
            } else if (!initial && o.initial) {
                return 1;
            } else if (deltaQ > o.deltaQ) {
                return -1;
            } else if (deltaQ < o.deltaQ) {
                return 1;
            } else {
                return hashCode() - o.hashCode();
            }
        }
    }
}
