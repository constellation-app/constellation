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
package au.gov.asd.tac.constellation.graph;

/**
 * The GraphElementType enum defines the types of elements present in a graph.
 *
 * There are several functions that are relevant to several element types (such
 * as setting attribute values, selection, deletion etc) and it is useful to
 * process elements in an element-type-agnostic way. GraphElementType provides a
 * collection of useful methods that allow this to happen.
 *
 * @author sirius
 */
public enum GraphElementType {

    /**
     * Metadata; one element.
     */
    META(GraphElementType.META_LABEL) {
        @Override
        public int getElementCount(final GraphReadMethods graph) {
            return 1;
        }

        @Override
        public int getElement(final GraphReadMethods graph, final int position) {
            return 0;
        }

        @Override
        public int getElementCapacity(final GraphReadMethods graph) {
            return 1;
        }

        @Override
        public void removeElement(final GraphWriteMethods graph, final int element) {
            // Override required for implementation
        }

        @Override
        public int getTransactionCount(final GraphReadMethods graph, final int element) {
            return graph.getTransactionCount();
        }

        @Override
        public int getTransaction(final GraphReadMethods graph, final int element, final int position) {
            return graph.getTransaction(position);
        }

        @Override
        public int getSourceVertex(final GraphReadMethods graph, final int element) {
            return Graph.NOT_FOUND;
        }

        @Override
        public int getDestinationVertex(final GraphReadMethods graph, final int element) {
            return Graph.NOT_FOUND;
        }

        @Override
        public int getOtherVertex(final GraphReadMethods graph, final int element, final int vxId) {
            return Graph.NOT_FOUND;
        }

        @Override
        public long getUID(final GraphReadMethods graph, final int element) {
            return 0L;
        }

        @Override
        public boolean isSelected(final GraphReadMethods graph, final int element, final int selectedAttribute) {
            return true;
        }

        @Override
        public GraphElementType getSelectionElementType() {
            return GraphElementType.META;
        }

        @Override
        public boolean elementExists(final GraphReadMethods graph, final int element) {
            return element == 0;
        }

        @Override
        public void completeWithSchema(final GraphWriteMethods graph, final int element) {
            // Override required for implementation
        }

        @Override
        public boolean canBeSelected() {
            return false;
        }
    },
    /**
     * Graph data; one element.
     */
    GRAPH(GraphElementType.GRAPH_LABEL) {
        @Override
        public int getElementCount(final GraphReadMethods graph) {
            return 1;
        }

        @Override
        public int getElement(final GraphReadMethods graph, final int position) {
            return 0;
        }

        @Override
        public int getElementCapacity(final GraphReadMethods graph) {
            return 1;
        }

        @Override
        public void removeElement(final GraphWriteMethods graph, final int element) {
            // Override required for implementation
        }

        @Override
        public int getTransactionCount(final GraphReadMethods graph, final int element) {
            return graph.getTransactionCount();
        }

        @Override
        public int getTransaction(final GraphReadMethods graph, final int element, final int position) {
            return graph.getTransaction(position);
        }

        @Override
        public int getSourceVertex(final GraphReadMethods graph, final int element) {
            return Graph.NOT_FOUND;
        }

        @Override
        public int getDestinationVertex(final GraphReadMethods graph, final int element) {
            return Graph.NOT_FOUND;
        }

        @Override
        public int getOtherVertex(final GraphReadMethods graph, final int element, final int vxId) {
            return Graph.NOT_FOUND;
        }

        @Override
        public long getUID(final GraphReadMethods graph, final int element) {
            return 0L;
        }

        @Override
        public boolean isSelected(final GraphReadMethods graph, final int element, final int selectedAttribute) {
            return true;
        }

        @Override
        public GraphElementType getSelectionElementType() {
            return GraphElementType.GRAPH;
        }

        @Override
        public boolean elementExists(final GraphReadMethods graph, final int element) {
            return element == 0;
        }

        @Override
        public void completeWithSchema(final GraphWriteMethods graph, final int element) {
            if (graph.getSchema() != null) {
                final int vertexCount = graph.getVertexCount();
                for (int i = 0; i < vertexCount; i++) {
                    int vertex = graph.getVertex(i);
                    graph.getSchema().completeVertex(graph, vertex);
                }

                final int transactionCount = graph.getTransactionCount();
                for (int i = 0; i < transactionCount; i++) {
                    int transaction = graph.getTransaction(i);
                    graph.getSchema().completeTransaction(graph, transaction);
                }
            }
        }

        @Override
        public boolean canBeSelected() {
            return false;
        }
    },
    /**
     * Vertices.
     */
    VERTEX(GraphElementType.VERTEX_LABEL) {
        @Override
        public int getElementCount(final GraphReadMethods graph) {
            return graph.getVertexCount();
        }

        @Override
        public int getElement(final GraphReadMethods graph, final int position) {
            return graph.getVertex(position);
        }

        @Override
        public int getElementCapacity(final GraphReadMethods graph) {
            return graph.getVertexCapacity();
        }

        @Override
        public void removeElement(final GraphWriteMethods graph, final int element) {
            graph.removeVertex(element);
        }

        @Override
        public int getTransactionCount(final GraphReadMethods graph, final int element) {
            return graph.getVertexTransactionCount(element);
        }

        @Override
        public int getTransaction(final GraphReadMethods graph, final int element, final int position) {
            return graph.getVertexTransaction(element, position);
        }

        @Override
        public int getSourceVertex(final GraphReadMethods graph, final int element) {
            return element;
        }

        @Override
        public int getDestinationVertex(final GraphReadMethods graph, final int element) {
            return element;
        }

        @Override
        public int getOtherVertex(final GraphReadMethods graph, final int element, final int vxId) {
            return element;
        }

        @Override
        public long getUID(final GraphReadMethods graph, final int element) {
            return graph.getVertexUID(element);
        }

        @Override
        public boolean isSelected(final GraphReadMethods graph, final int element, final int selectedAttribute) {
            return graph.getBooleanValue(selectedAttribute, element);
        }

        @Override
        public GraphElementType getSelectionElementType() {
            return GraphElementType.VERTEX;
        }

        @Override
        public boolean elementExists(final GraphReadMethods graph, final int element) {
            return graph.vertexExists(element);
        }

        @Override
        public void completeWithSchema(final GraphWriteMethods graph, final int element) {
            if (graph.getSchema() != null) {
                graph.getSchema().completeVertex(graph, element);
            }
        }

        @Override
        public boolean canBeSelected() {
            return true;
        }
    },
    /**
     * Links.
     */
    LINK(GraphElementType.LINK_LABEL) {

        @Override
        public String getShortLabel() {
            return LINK_SHORT_LABEL;
        }

        @Override
        public int getElementCount(final GraphReadMethods graph) {
            return graph.getLinkCount();
        }

        @Override
        public int getElement(final GraphReadMethods graph, final int position) {
            return graph.getLink(position);
        }

        @Override
        public int getElementCapacity(final GraphReadMethods graph) {
            return graph.getLinkCapacity();
        }

        @Override
        public void removeElement(final GraphWriteMethods graph, final int element) {
            // Override required for implementation
        }

        @Override
        public int getTransactionCount(final GraphReadMethods graph, int element) {
            return graph.getLinkTransactionCount(element);
        }

        @Override
        public int getTransaction(final GraphReadMethods graph, final int element, final int position) {
            return graph.getLinkTransaction(element, position);
        }

        @Override
        public int getSourceVertex(final GraphReadMethods graph, final int element) {
            return graph.getLinkHighVertex(element);
        }

        @Override
        public int getDestinationVertex(final GraphReadMethods graph, final int element) {
            return graph.getLinkLowVertex(element);
        }

        @Override
        public int getOtherVertex(final GraphReadMethods graph, final int element, final int vxId) {
            final int otherId = getSourceVertex(graph, element);
            return otherId == vxId ? getDestinationVertex(graph, element) : otherId;
        }

        @Override
        public long getUID(final GraphReadMethods graph, final int element) {
            return graph.getLinkUID(element);
        }

        @Override
        public boolean isSelected(final GraphReadMethods graph, final int element, final int selectedAttribute) {
            int transactionCount = graph.getLinkTransactionCount(element);
            for (int i = 0; i < transactionCount; i++) {
                int transaction = graph.getLinkTransaction(element, i);
                if (graph.getBooleanValue(selectedAttribute, transaction)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public GraphElementType getSelectionElementType() {
            return GraphElementType.TRANSACTION;
        }

        @Override
        public boolean elementExists(final GraphReadMethods graph, final int element) {
            return graph.linkExists(element);
        }

        @Override
        public void completeWithSchema(final GraphWriteMethods graph, final int element) {
            if (graph.getSchema() != null) {
                final int transactionCount = graph.getLinkTransactionCount(element);
                for (int i = 0; i < transactionCount; i++) {
                    int transaction = graph.getLinkTransaction(element, i);
                    graph.getSchema().completeTransaction(graph, transaction);
                }
            }
        }

        @Override
        public boolean canBeSelected() {
            return true;
        }
    },
    /**
     * Edges.
     */
    EDGE(GraphElementType.EDGE_LABEL) {

        @Override
        public String getShortLabel() {
            return EDGE_SHORT_LABEL;
        }

        @Override
        public int getElementCount(final GraphReadMethods graph) {
            return graph.getEdgeCount();
        }

        @Override
        public int getElement(final GraphReadMethods graph, final int position) {
            return graph.getEdge(position);
        }

        @Override
        public int getElementCapacity(final GraphReadMethods graph) {
            return graph.getEdgeCapacity();
        }

        @Override
        public void removeElement(final GraphWriteMethods graph, final int element) {
            // Override required for implementation
        }

        @Override
        public int getTransactionCount(final GraphReadMethods graph, final int element) {
            return graph.getEdgeTransactionCount(element);
        }

        @Override
        public int getTransaction(final GraphReadMethods graph, final int element, final int position) {
            return graph.getEdgeTransaction(element, position);
        }

        @Override
        public int getSourceVertex(final GraphReadMethods graph, final int element) {
            return graph.getEdgeSourceVertex(element);
        }

        @Override
        public int getDestinationVertex(final GraphReadMethods graph, final int element) {
            return graph.getEdgeDestinationVertex(element);
        }

        @Override
        public int getOtherVertex(final GraphReadMethods graph, final int element, final int vxId) {
            final int otherId = getSourceVertex(graph, element);
            return otherId == vxId ? getDestinationVertex(graph, element) : otherId;
        }

        @Override
        public long getUID(final GraphReadMethods graph, final int element) {
            return graph.getEdgeUID(element);
        }

        @Override
        public boolean isSelected(final GraphReadMethods graph, final int element, final int selectedAttribute) {
            int transactionCount = graph.getEdgeTransactionCount(element);
            for (int i = 0; i < transactionCount; i++) {
                int transaction = graph.getEdgeTransaction(element, i);
                if (graph.getBooleanValue(selectedAttribute, transaction)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public GraphElementType getSelectionElementType() {
            return GraphElementType.TRANSACTION;
        }

        @Override
        public boolean elementExists(final GraphReadMethods graph, final int element) {
            return graph.edgeExists(element);
        }

        @Override
        public void completeWithSchema(final GraphWriteMethods graph, final int element) {
            if (graph.getSchema() != null) {
                final int transactionCount = graph.getEdgeTransactionCount(element);
                for (int i = 0; i < transactionCount; i++) {
                    int transaction = graph.getEdgeTransaction(element, i);
                    graph.getSchema().completeTransaction(graph, transaction);
                }
            }
        }

        @Override
        public boolean canBeSelected() {
            return true;
        }
    },
    /**
     * Transactions.
     */
    TRANSACTION(GraphElementType.TRANSACTION_LABEL) {

        @Override
        public String getShortLabel() {
            return TRANSACTION_SHORT_LABEL;
        }

        @Override
        public int getElementCount(final GraphReadMethods graph) {
            return graph.getTransactionCount();
        }

        @Override
        public int getElement(final GraphReadMethods graph, final int position) {
            return graph.getTransaction(position);
        }

        @Override
        public int getElementCapacity(final GraphReadMethods graph) {
            return graph.getTransactionCapacity();
        }

        @Override
        public void removeElement(final GraphWriteMethods graph, final int element) {
            graph.removeTransaction(element);
        }

        @Override
        public int getTransactionCount(final GraphReadMethods graph, final int element) {
            return 1;
        }

        @Override
        public int getTransaction(final GraphReadMethods graph, final int element, final int position) {
            return element;
        }

        @Override
        public int getSourceVertex(final GraphReadMethods graph, final int element) {
            return graph.getTransactionSourceVertex(element);
        }

        @Override
        public int getDestinationVertex(final GraphReadMethods graph, final int element) {
            return graph.getTransactionDestinationVertex(element);
        }

        @Override
        public int getOtherVertex(final GraphReadMethods graph, final int element, final int vxId) {
            final int otherId = getSourceVertex(graph, element);
            return otherId == vxId ? getDestinationVertex(graph, element) : otherId;
        }

        @Override
        public long getUID(final GraphReadMethods graph, final int element) {
            return graph.getTransactionUID(element);
        }

        @Override
        public boolean isSelected(final GraphReadMethods graph, final int element, final int selectedAttribute) {
            return graph.getBooleanValue(selectedAttribute, element);
        }

        @Override
        public GraphElementType getSelectionElementType() {
            return GraphElementType.TRANSACTION;
        }

        @Override
        public boolean elementExists(final GraphReadMethods graph, final int element) {
            return graph.transactionExists(element);
        }

        @Override
        public void completeWithSchema(final GraphWriteMethods graph, final int element) {
            if (graph.getSchema() != null) {
                graph.getSchema().completeTransaction(graph, element);
            }
        }

        @Override
        public boolean canBeSelected() {
            return true;
        }
    };

    private final String label;

    private static final String META_LABEL = "Metadata";
    private static final String GRAPH_LABEL = "Graph";
    private static final String VERTEX_LABEL = "Node";
    private static final String LINK_LABEL = "Link (full transaction merging)";
    private static final String EDGE_LABEL = "Edge (transactions merged by direction)";
    private static final String TRANSACTION_LABEL = "Transaction (no merging)";

    private static final String LINK_SHORT_LABEL = "Link";
    private static final String EDGE_SHORT_LABEL = "Edge";
    private static final String TRANSACTION_SHORT_LABEL = "Transaction";

    /**
     * Default constructor.
     *
     * @param label String representation of GraphElementType.
     */
    GraphElementType(final String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    /**
     * Return the String representation of this GraphElementType.
     *
     * @return the String representation of this GraphElementType
     */
    public String getLabel() {
        return label;
    }

    public String getShortLabel() {
        return label;
    }

    /**
     * Returns the number of elements of this type that are currently present in
     * the graph.
     *
     * @param graph the graph containing the elements.
     *
     * @return the number of elements of this type that are currently present in
     * the graph.
     */
    public abstract int getElementCount(final GraphReadMethods graph);

    /**
     * Returns the element based on its position.
     *
     * @param graph the graph containing the elements.
     * @param position the position of the element that will be returned.
     *
     * @return the element based on its position.
     */
    public abstract int getElement(final GraphReadMethods graph, final int position);

    /**
     * Returns the capacity of the graph to hold this type of element.
     *
     * @param graph the graph containing the elements.
     *
     * @return the capacity of the graph to hold this type of element.
     */
    public abstract int getElementCapacity(final GraphReadMethods graph);

    /**
     * Removes an element of this type from the graph.
     *
     * @param graph the graph containing the element.
     * @param element the id of the element to remove.
     */
    public abstract void removeElement(final GraphWriteMethods graph, final int element);

    /**
     * Returns the number of transactions that this element represents.
     *
     * @param graph the graph containing the element.
     * @param element the id of the element.
     *
     * @return the number of transactions that this element represents.
     */
    public abstract int getTransactionCount(final GraphReadMethods graph, final int element);

    /**
     * Returns the transaction that holds the specified position in relation to
     * this element.
     *
     * @param graph the graph containing the element.
     * @param element the id of the element.
     * @param position the position of the transaction to return.
     *
     * @return the transaction that holds the specified position in relation to
     * this element.
     */
    public abstract int getTransaction(final GraphReadMethods graph, final int element, final int position);

    /**
     * Returns the source vertex for this element.
     *
     * @param graph the graph containing the element.
     * @param element the id of the element.
     *
     * @return the source vertex for this element.
     */
    public abstract int getSourceVertex(final GraphReadMethods graph, final int element);

    /**
     * Returns the destination vertex for this element.
     *
     * @param graph the graph containing the element.
     * @param element the id of the element.
     *
     * @return the destination vertex for this element.
     */
    public abstract int getDestinationVertex(final GraphReadMethods graph, final int element);

    /**
     * Returns the other vertex for this element.
     *
     * @param graph the graph containing the element.
     * @param element the id of the element.
     * @param vxId the id of the vertex that should not be returned.
     *
     * @return the other vertex for this element.
     */
    public abstract int getOtherVertex(final GraphReadMethods graph, final int element, final int vxId);

    /**
     * Returns the UID for this element.
     *
     * @param graph the graph containing the element.
     * @param element the id of the element.
     *
     * @return the UID for this element.
     */
    public abstract long getUID(final GraphReadMethods graph, final int element);

    /**
     * Returns true if this element type can be selected in the graph.
     *
     * @return true if this element type can be selected in the graph.
     */
    public abstract boolean canBeSelected();

    /**
     * Returns if this element should be considered selected. For transactions
     * and vertices this depends on the value of their selected attributes. For
     * edges and links, it depends on the selection of their contained
     * transactions.
     *
     * @param graph the graph containing the element.
     * @param element the id of the element.
     * @param selectedAttribute a boolean attribute that holds the selection
     * information.
     *
     * @return true if this element should be considered selected.
     */
    public abstract boolean isSelected(final GraphReadMethods graph, final int element, final int selectedAttribute);

    /**
     * Returns the element type that actually holds selection information for
     * this element type.
     *
     * @return the element type that actually holds selection information for
     * this element type.
     */
    public abstract GraphElementType getSelectionElementType();

    /**
     * Returns true if an element of this type with the specified id exists in
     * the graph. If this method returns true, then all other methods that
     * expect an element id as a parameter (and operate on the same element
     * type) are guaranteed to give well defined results. If this method returns
     * false, it is illegal to call any of these methods and the results of
     * doing so are undefined. In general, most element ids are gained from
     * querying the graph, meaning that it is already clear that a candidate
     * element id exists. This means that this method is hardly ever required.
     *
     * @param graph the graph to check.
     * @param element the id of the element.
     * @return true if an element of this type with the specified id exists in
     * the graph.
     */
    public abstract boolean elementExists(final GraphReadMethods graph, final int element);

    /**
     * Causes the schema to complete the element.
     *
     * @param graph the graph containing the element.
     * @param element the id of the element.
     */
    public abstract void completeWithSchema(final GraphWriteMethods graph, final int element);

    /**
     * Returns the GraphElementType constant with the specified name.
     * <p>
     * This differs from valueOf() in that the string must (case-insensitively)
     * match the equivalent label of a GraphElementType constant, rather than
     * the String value of the constant itself.
     *
     * @param label A String representing a GraphElementType value.
     *
     * @return A GraphElementType enum constant.
     */
    public static GraphElementType getValue(final String label) {
        if (label.equalsIgnoreCase(META_LABEL)) {
            return META;
        } else if (label.equalsIgnoreCase(VERTEX_LABEL)) {
            return VERTEX;
        } else if (label.equalsIgnoreCase(LINK_LABEL) || label.equalsIgnoreCase(LINK_SHORT_LABEL)) {
            return LINK;
        } else if (label.equalsIgnoreCase(EDGE_LABEL) || label.equalsIgnoreCase(EDGE_SHORT_LABEL)) {
            return EDGE;
        } else if (label.equalsIgnoreCase(TRANSACTION_LABEL) || label.equalsIgnoreCase(TRANSACTION_SHORT_LABEL)) {
            return TRANSACTION;
        } else if (label.equalsIgnoreCase(GRAPH_LABEL)) {
            return GRAPH;
        } else {
            throw new IllegalArgumentException("Label given is not a valid graph element type!");
        }
    }
}
