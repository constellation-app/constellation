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
package au.gov.asd.tac.constellation.views.attributecalculator.plugins;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.views.attributecalculator.panes.CalculatorTemplateDescription;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author sirius
 */
public enum CalculatorVariable {

    GRAPH_VERTEX_COUNT(null, "graph_node_count", "Graph Node Count", CalculatorTemplateDescription.GRAPH_NODE_COUNT, "Graph Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
            return graph.getVertexCount();
        }
    },
    GRAPH_LINK_COUNT(null, "graph_link_count", "Graph Link Count", CalculatorTemplateDescription.GRAPH_LINK_COUNT, "Graph Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
            return graph.getLinkCount();
        }
    },
    GRAPH_EDGE_COUNT(null, "graph_edge_count", "Graph Edge Count", CalculatorTemplateDescription.GRAPH_EDGE_COUNT, "Graph Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
            return graph.getEdgeCount();
        }
    },
    GRAPH_TRANSACTION_COUNT(null, "graph_transaction_count", "Graph Transaction Count", CalculatorTemplateDescription.GRAPH_TRANSACTION_COUNT, "Graph Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
            return graph.getTransactionCount();
        }
    },
    GRAPH_SELECTED_VERTEX_COUNT(null, "graph_selected_node_count", "Graph Selected Node Count", CalculatorTemplateDescription.GRAPH_SELECTED_VERTEX_COUNT, "Graph Properties") {
        private int selectedVertexCount = 0;

        @Override
        public void init(GraphReadMethods graph) {
            selectedVertexCount = 0;
            int selectedAttr = VisualConcept.VertexAttribute.SELECTED.get(graph);
            if (selectedAttr != Graph.NOT_FOUND) {
                int vertexCount = graph.getVertexCount();
                for (int i = 0; i < vertexCount; i++) {
                    int vertex = graph.getVertex(i);
                    if (graph.getBooleanValue(selectedAttr, vertex)) {
                        selectedVertexCount++;
                    }
                }
            }
        }

        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
            return selectedVertexCount;
        }
    },
    GRAPH_SELECTED_TRANSACTION_COUNT(null, "graph_selected_transaction_count", "Graph Selected Transaction Count", CalculatorTemplateDescription.GRAPH_SELECTED_TRANSACTION_COUNT, "Graph Properties") {
        private int selectedTranactionCount = 0;

        @Override
        public void init(GraphReadMethods graph) {
            selectedTranactionCount = 0;
            int selectedAttr = VisualConcept.TransactionAttribute.SELECTED.get(graph);
            if (selectedAttr != Graph.NOT_FOUND) {
                int transactionCount = graph.getTransactionCount();
                for (int i = 0; i < transactionCount; i++) {
                    int transaction = graph.getTransaction(i);
                    if (graph.getBooleanValue(selectedAttr, transaction)) {
                        selectedTranactionCount++;
                    }
                }
            }
        }

        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
            return selectedTranactionCount;
        }
    },
    VERTEX_HAS_LOOP(GraphElementType.VERTEX, "has_self_as_neighbour", "Has Self as Neighbour", CalculatorTemplateDescription.VERTEX_HAS_LOOP, "Node Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
            for (int i = 0; i < graph.getVertexNeighbourCount(element); i++) {
                if (graph.getVertexNeighbour(element, i) == element) {
                    return true;
                }
            }
            return false;
        }
    },
    VERTEX_NEIGHBOUR_COUNT(GraphElementType.VERTEX, "node_neighbour_count", "Node Neighbour Count", CalculatorTemplateDescription.VERTEX_NEIGHBOUR_COUNT, "Node Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
            int loopAdjustment = 0;
            for (int i = 0; i < graph.getVertexNeighbourCount(element); i++) {
                if (graph.getVertexNeighbour(element, i) == element) {
                    loopAdjustment = -2;
                    break;
                }
            }
            return graph.getVertexLinkCount(element) + loopAdjustment;
        }
    },
    VERTEX_LINK_COUNT(GraphElementType.VERTEX, "node_link_count", "Node Link Count", CalculatorTemplateDescription.VERTEX_LINK_COUNT, "Node Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
            return graph.getVertexLinkCount(element);
        }
    },
    VERTEX_EDGE_COUNT(GraphElementType.VERTEX, "node_edge_count", "Node Edge Count", CalculatorTemplateDescription.VERTEX_EDGE_COUNT, "Node Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
            return graph.getVertexEdgeCount(element);
        }
    },
    VERTEX_TRANSACTION_COUNT(GraphElementType.VERTEX, "node_transaction_count", "Node Transaction Count", CalculatorTemplateDescription.VERTEX_TRANSACTION_COUNT, "Node Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
            return graph.getVertexTransactionCount(element);
        }
    },
    VERTEX_OUTGOING_TRANSACTION_COUNT(GraphElementType.VERTEX, "node_out_transaction_count", "Node Outgoing Transaction Count", CalculatorTemplateDescription.VERTEX_OUTGOING_TRANSACTION_COUNT, "Node Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
            return graph.getVertexTransactionCount(element, Graph.OUTGOING);
        }
    },
    VERTEX_INCOMING_TRANSACTION_COUNT(GraphElementType.VERTEX, "node_in_transaction_count", "Node Incoming Transaction Count", CalculatorTemplateDescription.VERTEX_INCOMING_TRANSACTION_COUNT, "Node Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
            return graph.getVertexTransactionCount(element, Graph.INCOMING);
        }
    },
    VERTEX_UNDIRECTED_TRANSACTION_COUNT(GraphElementType.VERTEX, "node_undir_transaction_count", "Node Undirected Transaction Count", CalculatorTemplateDescription.VERTEX_UNDIRECTED_TRANSACTION_COUNT, "Node Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
            return graph.getVertexTransactionCount(element, Graph.UNDIRECTED);
        }
    },
    SOURCE_HAS_LOOP(GraphElementType.TRANSACTION, "source_has_self_as_neighbour", "Source Has Self as Neighbour", CalculatorTemplateDescription.SOURCE_HAS_LOOP, "Source Node Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
            final int vertID;
            if (elementType.equals(GraphElementType.TRANSACTION)) {
                vertID = graph.getTransactionSourceVertex(element);
            } else if (elementType.equals(GraphElementType.EDGE)) {
                vertID = graph.getEdgeSourceVertex(element);
            } else {
                vertID = graph.getLinkLowVertex(element);
            }
            for (int i = 0; i < graph.getVertexNeighbourCount(vertID); i++) {
                if (graph.getVertexNeighbour(vertID, i) == vertID) {
                    return true;
                }
            }
            return false;
        }
    },
    SOURCE_NEIGHBOUR_COUNT(GraphElementType.TRANSACTION, "source_neighbour_count", "Source Neighbour Count", CalculatorTemplateDescription.SOURCE_NEIGHBOUR_COUNT, "Source Node Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
            int loopAdjustment = 0;
            final int vertID;
            if (elementType.equals(GraphElementType.TRANSACTION)) {
                vertID = graph.getTransactionSourceVertex(element);
            } else if (elementType.equals(GraphElementType.EDGE)) {
                vertID = graph.getEdgeSourceVertex(element);
            } else {
                vertID = graph.getLinkLowVertex(element);
            }
            for (int i = 0; i < graph.getVertexNeighbourCount(vertID); i++) {
                if (graph.getVertexNeighbour(vertID, i) == vertID) {
                    loopAdjustment = -2;
                    break;
                }
            }
            return graph.getVertexLinkCount(vertID) + loopAdjustment;
        }
    },
    SOURCE_LINK_COUNT(GraphElementType.TRANSACTION, "source_link_count", "Source Link Count", CalculatorTemplateDescription.SOURCE_LINK_COUNT, "Source Node Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
//                    checkElementType(elementType);
            if (elementType.equals(GraphElementType.TRANSACTION)) {
                return graph.getVertexLinkCount(graph.getTransactionSourceVertex(element));
            } else if (elementType.equals(GraphElementType.EDGE)) {
                return graph.getVertexLinkCount(graph.getEdgeSourceVertex(element));
            } else {
                return graph.getVertexLinkCount(graph.getLinkLowVertex(element));
            }
        }
    },
    SOURCE_EDGE_COUNT(GraphElementType.TRANSACTION, "source_edge_count", "Source Edge Count", CalculatorTemplateDescription.SOURCE_EDGE_COUNT, "Source Node Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
//                    checkElementType(elementType);
            if (elementType.equals(GraphElementType.TRANSACTION)) {
                return graph.getVertexEdgeCount(graph.getTransactionSourceVertex(element));
            } else if (elementType.equals(GraphElementType.EDGE)) {
                return graph.getVertexEdgeCount(graph.getEdgeSourceVertex(element));
            } else {
                return graph.getVertexEdgeCount(graph.getLinkLowVertex(element));
            }
        }
    },
    SOURCE_TRANSACTION_COUNT(GraphElementType.TRANSACTION, "source_transaction_count", "Source Transaction Count", CalculatorTemplateDescription.SOURCE_TRANSACTION_COUNT, "Source Node Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
//                    checkElementType(elementType);
            if (elementType.equals(GraphElementType.TRANSACTION)) {
                return graph.getVertexTransactionCount(graph.getTransactionSourceVertex(element));
            } else if (elementType.equals(GraphElementType.EDGE)) {
                return graph.getVertexTransactionCount(graph.getEdgeSourceVertex(element));
            } else {
                return graph.getVertexTransactionCount(graph.getLinkLowVertex(element));
            }
        }
    },
    SOURCE_OUTGOING_TRANSACTION_COUNT(GraphElementType.TRANSACTION, "source_out_transaction_count", "Source Outgoing Transaction Count", CalculatorTemplateDescription.SOURCE_OUTGOING_TRANSACTION_COUNT, "Source Node Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
//                    checkElementType(elementType);
            if (elementType.equals(GraphElementType.TRANSACTION)) {
                return graph.getVertexTransactionCount(graph.getTransactionSourceVertex(element), Graph.OUTGOING);
            } else if (elementType.equals(GraphElementType.EDGE)) {
                return graph.getVertexTransactionCount(graph.getEdgeSourceVertex(element), Graph.OUTGOING);
            } else {
                return graph.getVertexTransactionCount(graph.getLinkLowVertex(element), Graph.OUTGOING);
            }
        }
    },
    SOURCE_INCOMING_TRANSACTION_COUNT(GraphElementType.TRANSACTION, "source_in_transaction_count", "Source Incoming Transaction Count", CalculatorTemplateDescription.SOURCE_INCOMING_TRANSACTION_COUNT, "Source Node Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
//                    checkElementType(elementType);
            if (elementType.equals(GraphElementType.TRANSACTION)) {
                return graph.getVertexTransactionCount(graph.getTransactionSourceVertex(element), Graph.INCOMING);
            } else if (elementType.equals(GraphElementType.EDGE)) {
                return graph.getVertexTransactionCount(graph.getEdgeSourceVertex(element), Graph.INCOMING);
            } else {
                return graph.getVertexTransactionCount(graph.getLinkLowVertex(element), Graph.INCOMING);
            }
        }
    },
    SOURCE_UNDIRECTED_TRANSACTION_COUNT(GraphElementType.TRANSACTION, "source_undir_transaction_count", "Source Undirected Transaction Count", CalculatorTemplateDescription.SOURCE_UNDIRECTED_TRANSACTION_COUNT, "Source Node Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
//                    checkElementType(elementType);
            if (elementType.equals(GraphElementType.TRANSACTION)) {
                return graph.getVertexTransactionCount(graph.getTransactionSourceVertex(element), Graph.UNDIRECTED);
            } else if (elementType.equals(GraphElementType.EDGE)) {
                return graph.getVertexTransactionCount(graph.getEdgeSourceVertex(element), Graph.UNDIRECTED);
            } else {
                return graph.getVertexTransactionCount(graph.getLinkLowVertex(element), Graph.UNDIRECTED);
            }
        }
    },
    DESTINATION_HAS_LOOP(GraphElementType.TRANSACTION, "dest_has_self_as_neighbour", "Destination Has Self as Neighbour", CalculatorTemplateDescription.DESTINATION_HAS_LOOP, "Destination Node Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
            final int vertID;
            if (elementType.equals(GraphElementType.TRANSACTION)) {
                vertID = graph.getTransactionDestinationVertex(element);
            } else if (elementType.equals(GraphElementType.EDGE)) {
                vertID = graph.getEdgeDestinationVertex(element);
            } else {
                vertID = graph.getLinkHighVertex(element);
            }
            for (int i = 0; i < graph.getVertexNeighbourCount(vertID); i++) {
                if (graph.getVertexNeighbour(vertID, i) == vertID) {
                    return true;
                }
            }
            return false;
        }
    },
    DESTINATION_NEIGHBOUR_COUNT(GraphElementType.TRANSACTION, "dest_neighbour_count", "Destination Neighbour Count", CalculatorTemplateDescription.DESTINATION_NEIGHBOUR_COUNT, "Destination Node Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
            int loopAdjustment = 0;
            final int vertID;
            if (elementType.equals(GraphElementType.TRANSACTION)) {
                vertID = graph.getTransactionDestinationVertex(element);
            } else if (elementType.equals(GraphElementType.EDGE)) {
                vertID = graph.getEdgeDestinationVertex(element);
            } else {
                vertID = graph.getLinkHighVertex(element);
            }
            for (int i = 0; i < graph.getVertexNeighbourCount(vertID); i++) {
                if (graph.getVertexNeighbour(vertID, i) == vertID) {
                    loopAdjustment = -2;
                    break;
                }
            }
            return graph.getVertexLinkCount(vertID) + loopAdjustment;
        }
    },
    DESTINATION_LINK_COUNT(GraphElementType.TRANSACTION, "dest_link_count", "Destination Link Count", CalculatorTemplateDescription.DESTINATION_LINK_COUNT, "Destination Node Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
//                    checkElementType(elementType);
            if (elementType.equals(GraphElementType.TRANSACTION)) {
                return graph.getVertexLinkCount(graph.getTransactionDestinationVertex(element));
            } else if (elementType.equals(GraphElementType.EDGE)) {
                return graph.getVertexLinkCount(graph.getEdgeDestinationVertex(element));
            } else {
                return graph.getVertexLinkCount(graph.getLinkHighVertex(element));
            }
        }
    },
    DESTINATION_EDGE_COUNT(GraphElementType.TRANSACTION, "dest_edge_count", "Destination Edge Count", CalculatorTemplateDescription.DESTINATION_EDGE_COUNT, "Destination Node Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
//                    checkElementType(elementType);
            if (elementType.equals(GraphElementType.TRANSACTION)) {
                return graph.getVertexEdgeCount(graph.getTransactionDestinationVertex(element));
            } else if (elementType.equals(GraphElementType.EDGE)) {
                return graph.getVertexEdgeCount(graph.getEdgeDestinationVertex(element));
            } else {
                return graph.getVertexEdgeCount(graph.getLinkHighVertex(element));
            }
        }
    },
    DESTINATION_TRANSACTION_COUNT(GraphElementType.TRANSACTION, "dest_transaction_count", "Destination Transaction Count", CalculatorTemplateDescription.DESTINATION_TRANSACTION_COUNT, "Destination Node Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
//                    checkElementType(elementType);
            if (elementType.equals(GraphElementType.TRANSACTION)) {
                return graph.getVertexTransactionCount(graph.getTransactionDestinationVertex(element));
            } else if (elementType.equals(GraphElementType.EDGE)) {
                return graph.getVertexTransactionCount(graph.getEdgeDestinationVertex(element));
            } else {
                return graph.getVertexTransactionCount(graph.getLinkHighVertex(element));
            }
        }
    },
    DESTINATION_OUTGOING_TRANSACTION_COUNT(GraphElementType.TRANSACTION, "dest_out_transaction_count", "Destination Outgoing Transaction Count", CalculatorTemplateDescription.DESTINATION_OUTGOING_TRANSACTION_COUNT, "Destination Node Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
//                    checkElementType(elementType);
            if (elementType.equals(GraphElementType.TRANSACTION)) {
                return graph.getVertexTransactionCount(graph.getTransactionDestinationVertex(element), Graph.OUTGOING);
            } else if (elementType.equals(GraphElementType.EDGE)) {
                return graph.getVertexTransactionCount(graph.getEdgeDestinationVertex(element), Graph.OUTGOING);
            } else {
                return graph.getVertexTransactionCount(graph.getLinkHighVertex(element), Graph.OUTGOING);
            }
        }
    },
    DESTINATION_INCOMING_TRANSACTION_COUNT(GraphElementType.TRANSACTION, "dest_in_transaction_count", "Destination Incoming Transaction Count", CalculatorTemplateDescription.DESTINATION_INCOMING_TRANSACTION_COUNT, "Destination Node Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
//                    checkElementType(elementType);
            if (elementType.equals(GraphElementType.TRANSACTION)) {
                return graph.getVertexTransactionCount(graph.getTransactionDestinationVertex(element), Graph.INCOMING);
            } else if (elementType.equals(GraphElementType.EDGE)) {
                return graph.getVertexTransactionCount(graph.getEdgeDestinationVertex(element), Graph.INCOMING);
            } else {
                return graph.getVertexTransactionCount(graph.getLinkHighVertex(element), Graph.INCOMING);
            }
        }
    },
    DESTINATION_UNDIRECTED_TRANSACTION_COUNT(GraphElementType.TRANSACTION, "dest_undir_transaction_count", "Destination Undirected Transaction Count", CalculatorTemplateDescription.DESTINATION_UNDIRECTED_TRANSACTION_COUNT, "Destination Node Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
//                    checkElementType(elementType);
            if (elementType.equals(GraphElementType.TRANSACTION)) {
                return graph.getVertexTransactionCount(graph.getTransactionDestinationVertex(element), Graph.UNDIRECTED);
            } else if (elementType.equals(GraphElementType.EDGE)) {
                return graph.getVertexTransactionCount(graph.getEdgeDestinationVertex(element), Graph.UNDIRECTED);
            } else {
                return graph.getVertexTransactionCount(graph.getLinkHighVertex(element), Graph.UNDIRECTED);
            }
        }
    },
    LINK_EDGE_COUNT(GraphElementType.TRANSACTION, "link_edge_count", "Link Edge Count", CalculatorTemplateDescription.LINK_EDGE_COUNT, "Transaction/Edge/Link Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
//                    checkElementType(elementType);
            if (elementType.equals(GraphElementType.TRANSACTION)) {
                return graph.getLinkEdgeCount(graph.getTransactionLink(element));
            } else if (elementType.equals(GraphElementType.EDGE)) {
                return graph.getLinkEdgeCount(graph.getEdgeLink(element));
            } else {
                return graph.getLinkEdgeCount(element);
            }
        }
    },
    LINK_TRANSACTION_COUNT(GraphElementType.TRANSACTION, "link_transaction_count", "Link Transaction Count", CalculatorTemplateDescription.LINK_TRANSACTION_COUNT, "Transaction/Edge/Link Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
//                    checkElementType(elementType);
            if (elementType.equals(GraphElementType.TRANSACTION)) {
                return graph.getLinkTransactionCount(graph.getTransactionLink(element));
            } else if (elementType.equals(GraphElementType.EDGE)) {
                return graph.getLinkTransactionCount(graph.getEdgeLink(element));
            } else {
                return graph.getLinkTransactionCount(element);
            }
        }
    },
    EDGE_TRANSACTION_COUNT(GraphElementType.TRANSACTION, "edge_transaction_count", "Edge Transaction Count", CalculatorTemplateDescription.EDGE_TRANSACTION_COUNT, "Transaction/Edge/Link Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
//                    checkElementType(elementType);
            if (elementType.equals(GraphElementType.TRANSACTION)) {
                return graph.getEdgeTransactionCount(graph.getTransactionEdge(element));
            } else if (elementType.equals(GraphElementType.EDGE)) {
                return graph.getEdgeTransactionCount(element);
            } else {
                List<Integer> edgeTransactionCounts = new LinkedList<>();
                for (int i = 0; i < graph.getLinkEdgeCount(element); i++) {
                    edgeTransactionCounts.add(graph.getEdgeTransactionCount(graph.getLinkEdge(element, i)));
                }
                return edgeTransactionCounts;
            }
        }
    },
    TRANSACTION_DIRECTION(GraphElementType.TRANSACTION, "is_transaction_directed", "Is Transaction Directed?", CalculatorTemplateDescription.TRANSACTION_DIRECTION, "Transaction/Edge/Link Properties") {
        @Override
        public Object getValue(GraphReadMethods graph, GraphElementType elementType, int element) {
//                    checkElementType(elementType);
            if (elementType.equals(GraphElementType.TRANSACTION)) {
                return graph.getTransactionDirection(element) != Graph.UNDIRECTED;
            } else if (elementType.equals(GraphElementType.EDGE)) {
                List<Boolean> transactionsDirected = new LinkedList<>();
                for (int i = 0; i < graph.getEdgeTransactionCount(element); i++) {
                    transactionsDirected.add(graph.getTransactionDirection(graph.getEdgeTransaction(element, i)) != Graph.UNDIRECTED);
                }
                return transactionsDirected;
            } else {
                List<Boolean> transactionsDirected = new LinkedList<>();
                for (int i = 0; i < graph.getLinkTransactionCount(element); i++) {
                    transactionsDirected.add(graph.getTransactionDirection(graph.getLinkTransaction(element, i)) != Graph.UNDIRECTED);
                }
                return transactionsDirected;
            }
        }
    },;

    private final GraphElementType elementType;
    private final String variableName;
    private final String variableLabel;
    private final CalculatorTemplateDescription description;
    private final String[] directory;
    private final int[] selectionIndices;

    private static int[] getDefaultSelectionIndex(String name) {
        int[] selIndices = {name.length()};
        return selIndices;
    }

    private CalculatorVariable(GraphElementType elementType, String variableName, String variableLabel, CalculatorTemplateDescription description, String... directory) {
        this(elementType, variableName, variableLabel, description, getDefaultSelectionIndex(variableName), directory);
    }

    private CalculatorVariable(GraphElementType elementType, String variableName, String variableLabel, CalculatorTemplateDescription description, int[] selectionIndices, String... directory) {
        this.elementType = elementType;
        this.variableName = variableName;
        this.variableLabel = variableLabel;
        this.description = description;
        this.selectionIndices = selectionIndices;
        this.directory = directory;
    }

    public static CalculatorVariable getGRAPH_VERTEX_COUNT() {
        return GRAPH_VERTEX_COUNT;
    }

    public GraphElementType getElementType() {
        return elementType;
    }

    public String getVariableName() {
        return variableName;
    }

    public String getVariableLabel() {
        return variableLabel;
    }

    public CalculatorTemplateDescription getDescription() {
        return description;
    }

    public String[] getDirectory() {
        return directory;
    }

    public int[] getSelectionIndices() {
        return selectionIndices;
    }

    public String getDirectoryString() {
        StringBuilder directoryString = new StringBuilder();
        for (String s : directory) {
            directoryString.append(s);
        }
        return directoryString.toString();
    }

    public abstract Object getValue(final GraphReadMethods graph, final GraphElementType elementType, final int element);

//    public void checkElementType(final GraphElementType elementType) {
//        if (this.elementType == GraphElementType.VERTEX && elementType != GraphElementType.VERTEX) {
//            throw new RuntimeException("Attribute Calculator Error: using node variable in transaction context");
//        } else if (this.elementType != GraphElementType.VERTEX && elementType == GraphElementType.VERTEX) {
//            throw new RuntimeException("Attribute Calculator Error: using transaction variable in node context");
//        }
//    }
    public void init(GraphReadMethods graph) {
    }
}
