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
package au.gov.asd.tac.constellation.views.attributecalculator.panes;

/**
 *
 * A Number of 'constants' which can be inserted into the calculator's script
 * window and reference either AttributeCalculator utility functions, python
 * inbuilt functions, or python operators. Note that there is no nice way to
 * really automatically generate the list of utility functions, even using
 * reflection, as description strings still need to be written for each method
 * etc. While it would be theoretically possible to cook something up to do
 * this, as these constants are part of the user interface I think manual entry
 * is the better solution (even if slightly prone to mistake) as utility
 * functions can then be added, moved and removed from the insertion menu as
 * desired.
 *
 * @author twilight_sparkle
 */
public enum CalculatorTemplateDescription {

    HAS_NEIGHBOUR(
            "Has Neighbour with Property",
            new String[]{
                "has_neighbour(condition)"
            },
            new String[]{
                "condition - A boolean expression."
            },
            "A boolean (True/False).",
            "Returns True if 'condition' is True for any of the neighbours of a node. Returns False otherwise.",
            new String[]{
                "For the purpose of this functions, a node is not considered a neighbour of itself even if it has a loop."
            }
    ),
    COUNT_NEIGHBOURS(
            "Count Neighbours with Property",
            new String[]{
                "count_neighbours(condition)"
            },
            new String[]{
                "condition - A boolean expression."
            },
            "A non-negative integer.",
            "Returns the number of neighbours of a node for which 'condition' is True.",
            new String[]{
                "For the purpose of this function, a node is not considered a neighbour of itself even if it has a loop."
            }
    ),
    FOR_NEIGHBOURS(
            "For Neighbours",
            new String[]{
                "for_neighbours(calculation)",
                "for_neighbours(condition, calculation)"
            },
            new String[]{
                "condition (optional) - A boolean expression.",
                "calculation - Any expression."
            },
            "A list.",
            "Returns a list of the results of 'calculation' for those neighbours of a node which satisfy 'condition'.",
            new String[]{
                "If 'condition' is not present, the return list contains the results of 'calculation' for all neighbours.",
                "This function will return an empty list if the node has no neighbours, satisfying 'condition' if present, for which 'calculation' has a value.",
                "For the purpose of this functions, a node is not considered a neighbour of itself even if it has a loop."
            }
    ),
    HAS_NODE_AT_DISTANCE(
            "Has Node at Distance with Property",
            new String[]{
                "has_node_at_distance(condition, distance)"
            },
            new String[]{
                "condition - A boolean expression.",
                "distance - A positive integer."
            },
            "A boolean (True/False).",
            "Returns True if 'condition' is True for any node whose minimum distance from the current node is 'distance'. Returns False otherwise.",
            new String[]{}
    ),
    COUNT_NODES_AT_DISTANCE(
            "Count Nodes at Distance with Property",
            new String[]{
                "count_nodes_at_distance(condition, distance)"
            },
            new String[]{
                "condition - A boolean expression",
                "distance - A positive integer."
            },
            "A non-negative integer.",
            "Returns the number of nodes whose minimum distance from the current node is 'distance', for which 'condition' is True.",
            new String[]{}
    ),
    FOR_NODES_AT_DISTANCE(
            "For Nodes at Distance",
            new String[]{
                "for_nodes_at_distance(calculation, distance)",
                "for_nodes_at_distance(condition, calculation, distance)"
            },
            new String[]{
                "condition (optional) - A boolean expression.",
                "calculation - Any expression.",
                "distance - A positive integer."
            },
            "A list.",
            "Returns a list of the results of 'calculation' for those nodes whose minimum distance from the current node is 'distance', which satisfy 'condition'.",
            new String[]{
                "If 'condition' is not present, the return list for a node contains the results of 'calculation' for all nodes at the specified distance.",
                "This function will return an empty list if the node has no nodes at the specified distance, satisfying 'condition' if present, for which 'calculation' has a value."
            }
    ),
    GET_DISTANCES(
            "Get Distances of Nodes with Property",
            new String[]{
                "get_node_distances(condition)"
            },
            new String[]{
                "condition - A boolean expression."
            },
            "A list of non-negative integers, or the list [-1].",
            "Returns a list of the minimum distances from the current node to those nodes for which 'condition' is True.",
            new String[]{
                "If 'condition' is True for the current node, 0 will be in the returned list.",
                "If 'condition' is not True for any node connected to the current node, the returned list be [-1]."
            }
    ),
    HAS_TRANSACTION(
            "Has Transaction with Property",
            new String[]{
                "has_transaction(condition)",
                "has_transaction(condition, node_condition)"
            },
            new String[]{
                "condition - A boolean expression.",
                "node_condition (optional) - A boolean expression."
            },
            "A boolean (True/False).",
            "Evaluates 'condition' for each transaction incident with a node, and if present, evaluates 'node condition' for the transaction's other endpoint.  Returns True if 'condition' is True for any of the transactions and, if present, 'node condition' is also True for the other endpoint of the corresponding transaction. Returns False otherwise.",
            new String[]{}
    ),
    HAS_OUTGOING_TRANSACTION(
            "Has Outgoing Transaction with Property",
            new String[]{
                "has_outgoing_transaction(condition)",
                "has_outgoing_transaction(condition, node_condition)"
            },
            new String[]{
                "condition - A boolean expression.",
                "node_condition (optional) - A boolean expression."
            },
            "A boolean (True/False).",
            "Evaluates 'condition' for each outgoing transaction incident with a node, and if present, evaluates 'node condition' for the transaction's other endpoint.  Returns True if 'condition' is True for any of the transactions and, if present, 'node condition' is also True for the other endpoint of the corresponding transaction. Returns False otherwise.",
            new String[]{}
    ),
    HAS_INCOMING_TRANSACTION(
            "Has Incoming Transaction with Property",
            new String[]{
                "has_incoming_transaction(condition)",
                "has_incoming_transaction(condition, node_condition)"
            },
            new String[]{
                "condition - A boolean expression.",
                "node_condition (optional) - A boolean expression."
            },
            "A boolean (True/False).",
            "Evaluates 'condition' for each incoming transaction incident with a node, and if present, evaluates 'node condition' for the transaction's other endpoint.  Returns True if 'condition' is True for any of the transactions and, if present, 'node condition' is also True for the other endpoint of the corresponding transaction. Returns False otherwise.",
            new String[]{}
    ),
    HAS_UNDIRECTED_TRANSACTION(
            "Has Undirected Transaction with Property",
            new String[]{
                "has_undirected_transaction(condition)",
                "has_undirected_transaction(condition, node_condition)"
            },
            new String[]{
                "condition - A boolean expression.",
                "node_condition (optional) - A boolean expression."
            },
            "A boolean (True/False).",
            "Evaluates 'condition' for each undirected transaction incident with a node, and if present, evaluates 'node condition' for the transaction's other endpoint.  Returns True if 'condition' is True for any of the transactions and, if present, 'node condition' is also True for the other endpoint of the corresponding transaction. Returns False otherwise.",
            new String[]{}
    ),
    HAS_PARALLEL_TRANSACTION(
            "Has Parallel Transaction with Property",
            new String[]{
                "has_parallel_transaction(condition)",},
            new String[]{
                "condition - A boolean expression.",},
            "A boolean (True/False).",
            "Evaluates 'condition' for each transaction parallel (same source and destination nodes, possibly switched) to a transaction. Returns True if 'condition' is True for any of the parallel transactions. Returns False otherwise.",
            new String[]{
                "A transaction is considered to be parallel to itself."
            }
    ),
    COUNT_TRANSACTIONS(
            "Count Transactions with Property",
            new String[]{
                "count_transactions(condition)",
                "count_transactions(condition, node_condition)"
            },
            new String[]{
                "condition - A boolean expression.",
                "node_condition (optional) - A boolean expression."
            },
            "A non-negative integer.",
            "Evaluates 'condition' for each transaction incident with a node, and if present, evaluates 'node condition' for the transaction's other endpoint.  Returns the number of transactions for which 'condition' is True and, if present, for which 'node_condition' is true for the other endpoint.",
            new String[]{}
    ),
    COUNT_OUTGOING_TRANSACTIONS(
            "Count Outgoing Transactions with Property",
            new String[]{
                "count_outgoing_transactions(condition)",
                "count_outgoing_transactions(condition, node_condition)"
            },
            new String[]{
                "condition - A boolean expression.",
                "node_condition (optional) - A boolean expression."
            },
            "A non-negative integer.",
            "Evaluates 'condition' for each outgoing transaction incident with a node, and if present, evaluates 'node condition' for the transaction's other endpoint.  Returns the number of transactions for which 'condition' is True and, if present, for which 'node_condition' is true for the other endpoint.",
            new String[]{}
    ),
    COUNT_INCOMING_TRANSACTIONS(
            "Count Incoming Transactions with Property",
            new String[]{
                "count_incoming_transactions(condition)",
                "count_incoming_transactions(condition, node_condition)"
            },
            new String[]{
                "condition - A boolean expression.",
                "node_condition (optional) - A boolean expression."
            },
            "A non-negative integer.",
            "Evaluates 'condition' for each incoming transaction incident with a node, and if present, evaluates 'node condition' for the transaction's other endpoint.  Returns the number of transactions for which 'condition' is True and, if present, for which 'node_condition' is true for the other endpoint.",
            new String[]{}
    ),
    COUNT_UNDIRECTED_TRANSACTIONS(
            "Count Undirected Transactions with Property",
            new String[]{
                "count_undirected_transactions(condition)",
                "count_undirected_transactions(condition, node_condition)"
            },
            new String[]{
                "condition - A boolean expression.",
                "node_condition (optional) - A boolean expression."
            },
            "A non-negative integer.",
            "Evaluates 'condition' for each undirected transaction incident with a node, and if present, evaluates 'node condition' for the transaction's other endpoint.  Returns the number of transactions for which 'condition' is True and, if present, for which 'node_condition' is true for the other endpoint.",
            new String[]{}
    ),
    COUNT_PARALLEL_TRANSACTIONS(
            "Count Parallel Transactions with Property",
            new String[]{
                "count_parallel_transactions(condition)",},
            new String[]{
                "condition - A boolean expression.",},
            "A non-negative integer.",
            "Evaluates 'condition' for each transaction parallel (same source and destination nodes, possibly switched) to a transaction. Returns the number of parallel transactions for which 'condition' is True.",
            new String[]{
                "A transaction is considered to be parallel to itself."
            }
    ),
    FOR_TRANSACTIONS(
            "For Transactions",
            new String[]{
                "for_transactions(computation)",
                "for_transactions(condition, computation)",
                "for_transactions(condition, node_condition, computation)"
            },
            new String[]{
                "computation - Any expression.",
                "condition (optional) - A boolean expression.",
                "node_condition (optional) - A boolean expression."
            },
            "A list.",
            "If present, evaluates 'condition' for each transaction incident with a node, and also if present, evaluates 'node condition' for the transaction's other endpoint. Returns a list of the results of evaluating 'calculation' for transactions where, if present, 'condition' is True and, if present, for which 'node_condition' is True for the other endpoint.",
            new String[]{
                "If neither condition is present the return list for a node contains the results of evaluation of 'calculation' for all incident transactions."
            }
    ),
    FOR_OUTGOING_TRANSACTIONS(
            "For Outgoing Transactions",
            new String[]{
                "for_outgoing_transactions(computation)",
                "for_outgoing_transactions(condition, computation)",
                "for_outgoing_transactions(condition, node_condition, computation)"
            },
            new String[]{
                "computation - Any expression.",
                "condition (optional) - A boolean expression.",
                "node_condition (optional) - A boolean expression."
            },
            "A list.",
            "If present, evaluates 'condition' for each outgoing transaction incident with a node, and also if present, evaluates 'node condition' for the transaction's other endpoint. Returns a list of the results of evaluating 'calculation' for transactions where, if present, 'condition' is True and, if present, for which 'node_condition' is True for the other endpoint.",
            new String[]{
                "If neither condition is present the return list for a node contains the results of evaluation of 'calculation' for all incident outgoing transactions."
            }
    ),
    FOR_INCOMING_TRANSACTIONS(
            "For Incoming Transactions",
            new String[]{
                "for_incoming_transactions(computation)",
                "for_incoming_transactions(condition, computation)",
                "for_incoming_transactions(condition, node_condition, computation)"
            },
            new String[]{
                "computation - Any expression.",
                "condition (optional) - A boolean expression.",
                "node_condition (optional) - A boolean expression."
            },
            "A list.",
            "If present, evaluates 'condition' for each incoming transaction incident with a node, and also if present, evaluates 'node condition' for the transaction's other endpoint. Returns a list of the results of evaluating 'calculation' for transactions where, if present, 'condition' is True and, if present, for which 'node_condition' is True for the other endpoint.",
            new String[]{
                "If neither condition is present the return list for a node contains the results of evaluation of 'calculation' for all incident incoming transactions."
            }
    ),
    FOR_UNDIRECTED_TRANSACTIONS(
            "For Undirected Transactions",
            new String[]{
                "for_undirected_transactions(computation)",
                "for_undirected_transactions(condition, computation)",
                "for_undirected_transactions(condition, node_condition, computation)"
            },
            new String[]{
                "computation - Any expression.",
                "condition (optional) - A boolean expression.",
                "node_condition (optional) - A boolean expression."
            },
            "A list.",
            "If present, evaluates 'condition' for each undirected transaction incident with a node, and also if present, evaluates 'node condition' for the transaction's other endpoint. Returns a list of the results of evaluating 'calculation' for transactions where, if present, 'condition' is True and, if present, for which 'node_condition' is True for the other endpoint.",
            new String[]{
                "If neither condition is present the return list for a node contains the results of evaluation of 'calculation' for all incident undirected transactions."
            }
    ),
    FOR_PARALLEL_TRANSACTIONS(
            "For Parallel Transactions",
            new String[]{
                "for_parallel_transactions(computation)",
                "for_parallel_transactions(condition, computation)",},
            new String[]{
                "computation - Any expression.",
                "condition (optional) - A boolean expression.",},
            "A list.",
            "Evaluates 'condition' for each transaction parallel (same source and destination nodes, possibly switched) to a transaction. Returns a list of the results of evaluating 'calculation' for parallel transactions where, if present, 'condition' is True.",
            new String[]{
                "If condition is not present the return list for a transaction contains the results of evaluation of 'computation' for all parallel transactions.",
                "A transaction is considered to be parallel to itself."
            }
    ),
    HAS_EDGE(
            "Has Edge with Property",
            new String[]{
                "has_edge(condition)",
                "has_edge(condition, node_condition)"
            },
            new String[]{
                "condition - A boolean expression.",
                "node_condition (optional) - A boolean expression."
            },
            "A boolean (True/False).",
            "Evaluates 'condition' for each edge incident with a node, and if present, evaluates 'node_condition' for the edge's other endpoint. Returns True if 'condition' is True for any edges and, if present, 'node condition' is also True for the other endpoint of the corresponding edge. Returns False otherwise.",
            new String[]{
                "Evaluating any transaction attributes and properties for an edge results in a list of attribute/property values for all the edge's constituent transactions. Hence it is usually necessary to use the python functions 'all' and 'any' around attributes and properties used inside 'condition'."
            }
    ),
    HAS_LINK(
            "Has Link with Property",
            new String[]{
                "has_link(condition)",
                "has_link(condition, node_condition)"
            },
            new String[]{
                "condition - A boolean expression.",
                "node_condition (optional) - A boolean expression."
            },
            "A boolean (True/False).",
            "Evaluates 'condition' for each link incident with a node, and if present, evaluates 'node_condition' for the link's other endpoint. Returns True if 'condition' is True for any links and, if present, 'node condition' is also True for the other endpoint of the corresponding link. Returns False otherwise.",
            new String[]{
                "Evaluating any transaction attributes and properties for a link results in a list of attribute/property values for all the link's constituent transactions. Hence it is usually necessary to use the python functions 'all' and 'any' around attributes and properties used inside 'condition'."
            }
    ),
    COUNT_EDGES(
            "Count Edges with Property",
            new String[]{
                "count_edges(condition)",
                "count_edges(condition, node_condition)"
            },
            new String[]{
                "condition - A boolean expression.",
                "node_condition (optional) - A boolean expression."
            },
            "A non-negative integer.",
            "Evaluates 'condition' for each edge incident with a node, and if present, evaluates 'node_condition' for the edge's other endpoint. Returns the number of edges for which 'condition' is True and, if present, for which 'node_condition' is true for the other endpoint.",
            new String[]{
                "Evaluating any transaction attributes and properties for an edge results in a list of attribute/property values for all the edge's constituent transactions. Hence it is usually necessary to use the python functions 'all' and 'any' around attributes and properties used inside 'condition'."
            }
    ),
    COUNT_LINKS(
            "Count Links with Property",
            new String[]{
                "count_links(condition)",
                "count_links(condition, node_condition)"
            },
            new String[]{
                "condition - A boolean expression.",
                "node_condition (optional) - A boolean expression."
            },
            "A non-negative integer.",
            "Evaluates 'condition' for each link incident with a node, and if present, evaluates 'node_condition' for the link's other endpoint. Returns the number of links for which 'condition' is True and, if present, for which 'node_condition' is true for the other endpoint.",
            new String[]{
                "Evaluating any transaction attributes and properties for a link results in a list of attribute/property values for all the link's constituent transactions. Hence it is usually necessary to use the python functions 'all' and 'any' around attributes and properties used inside 'condition'."
            }
    ),
    FOR_EDGES(
            "For Edges",
            new String[]{
                "for_edges(computation)",
                "for_edges(condition, computation)",
                "for_edges(condition, node_condition, computation)"
            },
            new String[]{
                "condition (optional) - A boolean expression.",
                "node_condition (optional) - A boolean expression.",
                "computation - Any expression."
            },
            "A list.",
            "If present, evaluates 'condition' for each edge incident with a node, and also if present, evaluates 'node condition' for the edge's other endpoint. Returns a list of the results of evaluating 'calculation' for edges where, if present, 'condition' is True and, if present, for which 'node_condition' is True for the other endpoint.",
            new String[]{
                "If neither condition is present the return list for a node contains the results of evaluation of 'calculation' for all incident edges.",
                "Evaluating any transaction attributes and properties for an edge results in a list of attribute/property values for all the edge's constituent transactions. Hence it is usually necessary to use the python functions 'all' and 'any' around attributes and properties used inside 'condition'."
            }
    ),
    FOR_LINKS(
            "For Links",
            new String[]{
                "for_links(computation)",
                "for_links(condition, computation)",
                "for_links(condition, node_condition, computation)"
            },
            new String[]{
                "condition (optional) - A boolean expression.",
                "node_condition (optional) - A boolean expression.",
                "computation - Any expression."
            },
            "A list.",
            "If present, evaluates 'condition' for each link incident with a node, and also if present, evaluates 'node condition' for the link's other endpoint. Returns a list of the results of evaluating 'calculation' for links where, if present, 'condition' is True and, if present, for which 'node_condition' is True for the other endpoint.",
            new String[]{
                "If neither condition is present the return list for a node contains the results of evaluation of 'calculation' for all incident links.",
                "Evaluating any transaction attributes and properties for a link results in a list of attribute/property values for all the link's constituent transactions. Hence it is usually necessary to use the python functions 'all' and 'any' around attributes and properties used inside 'condition'."
            }
    ),
    GRAPH_HAS_NODE(
            "Graph Has Node",
            new String[]{
                "graph_has_node(condition)"
            },
            new String[]{
                "condition - A boolean expression.",},
            "A boolean (True/False).",
            "Evaluates 'condition' for each node in the graph. Returns True if 'condition' is True for any of the nodes. Returns False otherwise.",
            new String[]{}
    ),
    GRAPH_COUNT_NODES(
            "Count Nodes in Graph",
            new String[]{
                "graph_count_nodes(condition)",},
            new String[]{
                "condition - A boolean expression.",},
            "A non-negative integer.",
            "Evaluates 'condition' for each node in the graph. Returns the number of nodes for which 'condition' is True",
            new String[]{}
    ),
    GRAPH_FOR_NODES(
            "For Nodes in Graph",
            new String[]{
                "graph_for_nodes(computation)",
                "graph_for_nodes(condition, computation)"
            },
            new String[]{
                "condition (optional) - A boolean expression.",
                "computation - Any expression."
            },
            "A list.",
            "If present, evaluates 'condition' for each node in the graph. Returns a list of the results of evaluating 'calculation' for nodes where, if present, 'condition' is True.",
            new String[]{
                "If condition is not present, the return list contains the results of evaluation of 'calculation' for all nodes in the graph.",}
    ),
    GRAPH_HAS_TRANSACTION(
            "Graph Has Transaction",
            new String[]{
                "graph_has_transaction(condition)"
            },
            new String[]{
                "condition - A boolean expression.",},
            "A boolean (True/False).",
            "Evaluates 'condition' for each transaction in the graph. Returns True if 'condition' is True for any of the transactions. Returns False otherwise.",
            new String[]{}
    ),
    GRAPH_COUNT_TRANSACTIONS(
            "Count Transactions in Graph",
            new String[]{
                "graph_count_transactions(condition)",},
            new String[]{
                "condition - A boolean expression.",},
            "A non-negative integer.",
            "Evaluates 'condition' for each transaction in the graph. Returns the number of transactions for which 'condition' is True",
            new String[]{}
    ),
    GRAPH_FOR_TRANSACTIONS(
            "For Transactions in Graph",
            new String[]{
                "graph_for_transactions(computation)",
                "graph_for_transactions(condition, computation)"
            },
            new String[]{
                "condition (optional) - A boolean expression.",
                "computation - Any expression."
            },
            "A list.",
            "If present, evaluates 'condition' for each transaction in the graph. Returns a list of the results of evaluating 'calculation' for transactions where, if present, 'condition' is True.",
            new String[]{
                "If condition is not present, the return list contains the results of evaluation of 'calculation' for all transactions in the graph.",}
    ),
    GRAPH_NODE_COUNT(
            "Graph Node Count",
            new String[]{
                "graph_node_count"
            },
            new String[]{},
            "A non-negative integer.",
            "A constant whose value is the total number of the nodes in the graph.",
            new String[]{}
    ),
    GRAPH_LINK_COUNT(
            "Graph Link Count",
            new String[]{
                "graph_link_count"
            },
            new String[]{},
            "A non-negative integer.",
            "A constant whose value is the total number of the links in the graph.",
            new String[]{}
    ),
    GRAPH_EDGE_COUNT(
            "Graph Edge Count",
            new String[]{
                "graph_edge_count"
            },
            new String[]{},
            "A non-negative integer.",
            "A constant whose value is the total number of the edges in the graph.",
            new String[]{}
    ),
    GRAPH_TRANSACTION_COUNT(
            "Graph Transaction Count",
            new String[]{
                "graph_transaction_count"
            },
            new String[]{},
            "A non-negative integer.",
            "A constant whose value is the total number of the transaction in the graph.",
            new String[]{}
    ),
    GRAPH_SELECTED_VERTEX_COUNT(
            "Graph Selected Node Count",
            new String[]{
                "graph_selected_node_count"
            },
            new String[]{},
            "A non-negative integer.",
            "A constant whose value is the number of currently selected nodes in the graph.",
            new String[]{}
    ),
    GRAPH_SELECTED_TRANSACTION_COUNT(
            "Graph Selected Transaction Count",
            new String[]{
                "graph_selected_transaction_count"
            },
            new String[]{},
            "A non-negative integer.",
            "A constant whose value is the number of currently selected transactions in the graph.",
            new String[]{}
    ),
    VERTEX_HAS_LOOP(
            "Has Self as Neighbour",
            new String[]{
                "has_self_as_neighbour"
            },
            new String[]{},
            "A boolean (True/False).",
            "A variable that is True for nodes with loops, and False for all other nodes.",
            new String[]{}
    ),
    VERTEX_NEIGHBOUR_COUNT(
            "Node Neighbour Count",
            new String[]{
                "node_neighbour_count"
            },
            new String[]{},
            "A non-negative integer.",
            "A variable that represents the number of neighbours of a node (not including the node itself).",
            new String[]{}
    ),
    VERTEX_LINK_COUNT(
            "Node Link Count",
            new String[]{
                "node_link_count"
            },
            new String[]{},
            "A non-negative integer.",
            "A variable that represents the number of links incident with a node.",
            new String[]{
                "This differs subtly from Node Neighbour Count, as a loop counts as two links."
            }
    ),
    VERTEX_EDGE_COUNT(
            "Node Edge Count",
            new String[]{
                "node_edge_count"
            },
            new String[]{},
            "A non-negative integer.",
            "A variable that represents the number of edges incident with a node.",
            new String[]{}
    ),
    VERTEX_TRANSACTION_COUNT(
            "Node Transaction Count",
            new String[]{
                "node_transaction_count"
            },
            new String[]{},
            "A non-negative integer.",
            "A variable that represents the number of transactions incident with a node.",
            new String[]{}
    ),
    VERTEX_OUTGOING_TRANSACTION_COUNT(
            "Node Outgoing Transaction Count",
            new String[]{
                "node_outgoing_transaction_count"
            },
            new String[]{},
            "A non-negative integer.",
            "A variable that represents the number of transactions which are outgoing from a node.",
            new String[]{}
    ),
    VERTEX_INCOMING_TRANSACTION_COUNT(
            "Node Incoming Transaction Count",
            new String[]{
                "node_incoming_transaction_count"
            },
            new String[]{},
            "A non-negative integer.",
            "A variable that represents the number of transactions which are incoming to a node.",
            new String[]{}
    ),
    VERTEX_UNDIRECTED_TRANSACTION_COUNT(
            "Node Undirected Transaction Count",
            new String[]{
                "node_undirected_transaction_count"
            },
            new String[]{},
            "A non-negative integer.",
            "A variable that represents the number of undirected transactions incident with a node.",
            new String[]{}
    ),
    SOURCE_HAS_LOOP(
            "Source Has Self as Neighbour",
            new String[]{
                "source_has_self_as_neighbour"
            },
            new String[]{},
            "A boolean (True/False).",
            "A variable that is True for transactions whose source nodes have loops, and False for all other transactions.",
            new String[]{}
    ),
    SOURCE_NEIGHBOUR_COUNT(
            "Source Neighbour Count",
            new String[]{
                "source_neighbour_count"
            },
            new String[]{},
            "A non-negative integer.",
            "A variable that represents the number of neighbours of the source node (not including the node itself) of a transaction.",
            new String[]{}
    ),
    SOURCE_LINK_COUNT(
            "Source Link Count",
            new String[]{
                "source_link_count"
            },
            new String[]{},
            "A non-negative integer.",
            "A variable that represents the number of links incident with the source node of a transaction.",
            new String[]{
                "This differs subtly from Source Neighbour Count, as a loop counts as two links."
            }
    ),
    SOURCE_EDGE_COUNT(
            "Source Edge Count",
            new String[]{
                "source_edge_count"
            },
            new String[]{},
            "A non-negative integer.",
            "A variable that represents the number of edges incident with the source node of a transaction.",
            new String[]{}
    ),
    SOURCE_TRANSACTION_COUNT(
            "Source Transaction Count",
            new String[]{
                "source_transaction_count"
            },
            new String[]{},
            "A non-negative integer.",
            "A variable that represents the number of transactions incident with the source node of a transaction.",
            new String[]{}
    ),
    SOURCE_OUTGOING_TRANSACTION_COUNT(
            "Source Outgoing Transaction Count",
            new String[]{
                "source_outgoing_transaction_count"
            },
            new String[]{},
            "A non-negative integer.",
            "A variable that represents the number of transactions which are outgoing from the source node of a transaction.",
            new String[]{}
    ),
    SOURCE_INCOMING_TRANSACTION_COUNT(
            "Source Incoming Transaction Count",
            new String[]{
                "source_incoming_transaction_count"
            },
            new String[]{},
            "A non-negative integer.",
            "A variable that represents the number of transactions which are incoming to the source node of a transaction.",
            new String[]{}
    ),
    SOURCE_UNDIRECTED_TRANSACTION_COUNT(
            "Source Undirected Transaction Count",
            new String[]{
                "source_undirected_transaction_count"
            },
            new String[]{},
            "A non-negative integer.",
            "A variable that represents the number of undirected transactions incident with the source node of a transaction.",
            new String[]{}
    ),
    DESTINATION_HAS_LOOP(
            "Destination Has Self as Neighbour",
            new String[]{
                "destination_has_self_as_neighbour"
            },
            new String[]{},
            "A boolean (True/False).",
            "A variable that is True for transactions whose destination nodes have loops, and False for all other transactions.",
            new String[]{}
    ),
    DESTINATION_NEIGHBOUR_COUNT(
            "Destination Neighbour Count",
            new String[]{
                "destination_neighbour_count"
            },
            new String[]{},
            "A non-negative integer.",
            "A variable that represents the number of neighbours of the destination node (not including the node itself) of a transaction.",
            new String[]{}
    ),
    DESTINATION_LINK_COUNT(
            "Destination Link Count",
            new String[]{
                "destination_link_count"
            },
            new String[]{},
            "A non-negative integer.",
            "A variable that represents the number of links incident with the destination node of a transaction.",
            new String[]{
                "This differs subtly from Destination Neighbour Count, as a loop counts as two links."
            }
    ),
    DESTINATION_EDGE_COUNT(
            "Destination Edge Count",
            new String[]{
                "destination_edge_count"
            },
            new String[]{},
            "A non-negative integer.",
            "A variable that represents the number of edges incident with the destination node of a transaction.",
            new String[]{}
    ),
    DESTINATION_TRANSACTION_COUNT(
            "Destination Transaction Count",
            new String[]{
                "destination_transaction_count"
            },
            new String[]{},
            "A non-negative integer.",
            "A variable that represents the number of transactions incident with the destination node of a transaction.",
            new String[]{}
    ),
    DESTINATION_OUTGOING_TRANSACTION_COUNT(
            "Destination Outgoing Transaction Count",
            new String[]{
                "destination_outgoing_transaction_count"
            },
            new String[]{},
            "A non-negative integer.",
            "A variable that represents the number of transactions which are outgoing from the destination node of a transaction.",
            new String[]{}
    ),
    DESTINATION_INCOMING_TRANSACTION_COUNT(
            "Destination Incoming Transaction Count",
            new String[]{
                "destination_incoming_transaction_count"
            },
            new String[]{},
            "A non-negative integer.",
            "A variable that represents the number of transactions which are incoming to the destination node of a transaction.",
            new String[]{}
    ),
    DESTINATION_UNDIRECTED_TRANSACTION_COUNT(
            "Destination Undirected Transaction Count",
            new String[]{
                "destination_undirected_transaction_count"
            },
            new String[]{},
            "A non-negative integer.",
            "A variable that represents the number of undirected transactions incident with the destination node of a transaction.",
            new String[]{}
    ),
    LINK_EDGE_COUNT(
            "Link Edge Count",
            new String[]{
                "link_edge_count"
            },
            new String[]{},
            "A non-negative integer.",
            "A variable that represents the number of edges comprised by a link.",
            new String[]{}
    ),
    LINK_TRANSACTION_COUNT(
            "Link Transaction Count",
            new String[]{
                "link_transaction_count"
            },
            new String[]{},
            "A non-negative integer.",
            "A variable that represents the number of transactions comprised by a link.",
            new String[]{}
    ),
    EDGE_TRANSACTION_COUNT(
            "Edge Transaction Count",
            new String[]{
                "edge_transaction_count"
            },
            new String[]{},
            "A non-negative integer.",
            "A variable that represents the number of transactions comprised by an edge.",
            new String[]{}
    ),
    TRANSACTION_DIRECTION(
            "Is Transaction Directed?",
            new String[]{
                "is_transaction_directed"
            },
            new String[]{},
            "A boolean (True/False).",
            "A variable that is True for directed transactions, and False for undirected transactions.",
            new String[]{}
    ),
    OPERATOR_OR(
            "Or",
            new String[]{
                "x or y"
            },
            new String[]{
                "x - A boolean (True/False) expression.",
                "y - A boolean (True/False) expression.",},
            "A value.",
            "If x is False, returns y. Otherwise returns x.",
            new String[]{
                "Since non-boolean types, such as integers, can be interpreted as booleans, 'or' may not always yield a boolean value."
            }
    ),
    OPERATOR_AND(
            "And",
            new String[]{
                "x and y"
            },
            new String[]{
                "x - A boolean (True/False) expression.",
                "y - A boolean (True/False) expression.",},
            "A value.",
            "If x is True, returns y. Otherwise returns x.",
            new String[]{
                "Since non-boolean types, such as integers, can be interpreted as booleans, 'and' may not always yield a boolean value."
            }
    ),
    OPERATOR_NOT(
            "And",
            new String[]{
                "not x"
            },
            new String[]{
                "x - A boolean expression.",},
            "A boolean (True/False).",
            "If x is True, returns False. Otherwise returns True.",
            new String[]{}
    ),
    OPERATOR_EQ(
            "Equal",
            new String[]{
                "x == y"
            },
            new String[]{
                "x - Any value.",
                "y - Any value."
            },
            "A boolean (True/False).",
            "Returns True if x is equal to y. Returns False otherwise.",
            new String[]{}
    ),
    OPERATOR_NEQ(
            "Not Equal",
            new String[]{
                "x != y"
            },
            new String[]{
                "x - Any value.",
                "y - Any value."
            },
            "A boolean (True/False).",
            "Returns False if x is equal to y. Returns True otherwise.",
            new String[]{}
    ),
    OPERATOR_LESS(
            "Less than",
            new String[]{
                "x < y"
            },
            new String[]{
                "x - Any orderable value, e.g., a number.",
                "y - Any orderable value, e.g., a number."
            },
            "A boolean (True/False).",
            "Returns True if x is less than y. Returns False otherwise.",
            new String[]{}
    ),
    OPERATOR_GREAT(
            "Greater than",
            new String[]{
                "x > y"
            },
            new String[]{
                "x - Any orderable value, e.g., a number.",
                "y - Any orderable value, e.g., a number."
            },
            "A boolean (True/False).",
            "Returns True if x is greater than y. Returns False otherwise.",
            new String[]{}
    ),
    OPERATOR_LEQ(
            "Less than or Equal",
            new String[]{
                "x <= y"
            },
            new String[]{
                "x - Any orderable value, e.g., a number.",
                "y - Any orderable value, e.g., a number."
            },
            "A boolean (True/False).",
            "Returns True if x is less than or equal to y. Returns False otherwise.",
            new String[]{}
    ),
    OPERATOR_GEQ(
            "Less than or Equal",
            new String[]{
                "x >= y"
            },
            new String[]{
                "x - Any orderable value, e.g., a number.",
                "y - Any orderable value, e.g., a number."
            },
            "A boolean (True/False).",
            "Returns True if x is greater than or equal to y. Returns False otherwise.",
            new String[]{}
    ),
    OPERATOR_IN(
            "Is contained in",
            new String[]{
                "x in y"
            },
            new String[]{
                "x - Any value",
                "y - Any sequence, or a string."
            },
            "A boolean (True/False).",
            "Returns True if x is contained in y. Returns False otherwise.",
            new String[]{
                "Containment works as expected for sequence types such as lists and sets. If y is a string, then x must also be a string and x is considered to be contained in y if it is a substring of y."
            }
    ),
    OPERATOR_PLUS(
            "Add",
            new String[]{
                "x + y"
            },
            new String[]{
                "x - Any numerical value",
                "y - Any numerical value"
            },
            "A number.",
            "Returns the sum of x and y.",
            new String[]{}
    ),
    OPERATOR_SUB(
            "Subtract",
            new String[]{
                "x - y"
            },
            new String[]{
                "x - Any numerical value",
                "y - Any numerical value"
            },
            "A number.",
            "Returns the difference of x and y.",
            new String[]{}
    ),
    OPERATOR_MUL(
            "Multiply",
            new String[]{
                "x * y"
            },
            new String[]{
                "x - Any numerical value",
                "y - Any numerical value"
            },
            "A number.",
            "Returns the product of x and y.",
            new String[]{}
    ),
    OPERATOR_DIV(
            "Divide",
            new String[]{
                "x / y"
            },
            new String[]{
                "x - Any numerical value",
                "y - Any numerical value"
            },
            "A number.",
            "Returns the quotient of x and y.",
            new String[]{}
    ),
    OPERATOR_POW(
            "To the Power of",
            new String[]{
                "x ** y"
            },
            new String[]{
                "x - Any numerical value",
                "y - Any numerical value"
            },
            "A number.",
            "Returns x to the power of y.",
            new String[]{}
    ),
    OPERATOR_MOD(
            "Modulo",
            new String[]{
                "x % y"
            },
            new String[]{
                "x - Any numerical value",
                "y - Any numerical value"
            },
            "A number.",
            "Returns x modulo y, that is the remainder of x divided by y.",
            new String[]{}
    ),
    FUNCTION_SUM(
            "Sum",
            new String[]{
                "sum(list_of_numbers)"
            },
            new String[]{
                "list_of_numbers - A list of numerical values.",},
            "A number.",
            "Returns the result of summing all values in 'list_of_numbers'.",
            new String[]{}
    ),
    FUNCTION_MAX(
            "Maximum",
            new String[]{
                "max(list_of_numbers)"
            },
            new String[]{
                "list_of_numbers - A list of numerical values.",},
            "A number.",
            "Returns the maximum value from all the values in 'list_of_numbers'.",
            new String[]{}
    ),
    FUNCTION_MIN(
            "Minimum",
            new String[]{
                "min(list_of_numbers)"
            },
            new String[]{
                "list_of_numbers - A list of numerical values.",},
            "A number.",
            "Returns the minimum value from all the values in 'list_of_numbers'.",
            new String[]{}
    ),
    FUNCTION_MEDIAN(
            "Median",
            new String[]{
                "median(list_of_numbers)"
            },
            new String[]{
                "list_of_numbers - A list of numerical values.",},
            "A number.",
            "Returns the median of 'list_of_numbers', that is the middle value when the values are ordered.",
            new String[]{}
    ),
    FUNCTION_MODE(
            "Mode",
            new String[]{
                "mode(list_of_numbers)"
            },
            new String[]{
                "list_of_numbers - A list of numerical values.",},
            "A number.",
            "Returns the mode of 'list_of_numbers', that is the most commonly occuring value.",
            new String[]{}
    ),
    FUNCTION_MEAN(
            "Mean",
            new String[]{
                "mean(list_of_numbers)"
            },
            new String[]{
                "list_of_numbers - A list of numerical values.",},
            "A number.",
            "Returns the mean of 'list_of_numbers', that is the average of all the values.",
            new String[]{}
    ),
    FUNCTION_LOG(
            "Natural Logarithm",
            new String[]{
                "log(number)"
            },
            new String[]{
                "number - A numerical value.",},
            "A number.",
            "Returns the natural logarithm of 'number'.",
            new String[]{}
    ),
    FUNCTION_SQRT(
            "Square Root",
            new String[]{
                "sqrt(number)"
            },
            new String[]{
                "number - A numerical value.",},
            "A number.",
            "Returns the square root of 'number'.",
            new String[]{}
    ),
    FUNCTION_ABS(
            "Absolute Value",
            new String[]{
                "abs(number)"
            },
            new String[]{
                "number - A numerical value.",},
            "A number.",
            "Returns the absolute value of 'number'.",
            new String[]{}
    ),
    FUNCTION_LOWER(
            "To Lower Case",
            new String[]{
                "string.lower()"
            },
            new String[]{
                "string - A string.",},
            "A string.",
            "Returns 'string' in lower case.",
            new String[]{}
    ),
    FUNCTION_UPPER(
            "To Upper Case",
            new String[]{
                "string.upper()"
            },
            new String[]{
                "string - A string.",},
            "A string.",
            "Returns 'string' in lower case.",
            new String[]{}
    ),
    FUNCTION_STARTSWITH(
            "Starts with Prefix",
            new String[]{
                "string.startswith(prefix)"
            },
            new String[]{
                "string - A string.",
                "prefix - A string."
            },
            "A boolean (True/False).",
            "Returns True if 'string' begins with 'prefix'. Returns False otherwise.",
            new String[]{}
    ),
    FUNCTION_ENDSWITH(
            "Ends with Suffix",
            new String[]{
                "string.endswith(suffix)"
            },
            new String[]{
                "string - A string.",
                "suffix - A string."
            },
            "A boolean (True/False).",
            "Returns True if 'string' ends with 'suffix'. Returns False otherwise.",
            new String[]{}
    ),
    FUNCTION_SPLIT(
            "Split on Substring",
            new String[]{
                "string.split(substring)"
            },
            new String[]{
                "string - A string.",
                "substring - A string."
            },
            "A list of strings.",
            "Returns the list of strings which is the result of splitting 'string' wherever 'substring' is found in it.",
            new String[]{
                "If 'substring' is not present, 'string' will be split on space."
            }
    ),
    FUNCTION_SUBSTRING(
            "Get Substring",
            new String[]{
                "string[index_start:index_end]"
            },
            new String[]{
                "string - A string.",
                "index_start - An integer.",
                "index_end - An integer.",},
            "A string.",
            "Returns the substring of 'string' beginning from the character at 'index_start' and ending before the character at 'index_end'.",
            new String[]{
                "If either 'index_start' or 'index_end' is negative, then it is taken as a position from the last, rather than the first, character of 'string'."
            }
    ),
    FUNCTION_STRLEN(
            "Length of String",
            new String[]{
                "len(string)"
            },
            new String[]{
                "string - A string.",},
            "A non-negative integer.",
            "Returns the number of characters in 'string'.",
            new String[]{
                "If 'string' is empty, the return value will be 0."
            }
    ),
    FUNCTION_LISTELEMENT(
            "Get Element",
            new String[]{
                "some_list[index]"
            },
            new String[]{
                "some_list - a list.",
                "index - a number."
            },
            "A value.",
            "Returns the element of 'some_list' at position 'index'.",
            new String[]{}
    ),
    FUNCTION_SUBLIST(
            "Get Element",
            new String[]{
                "some_list[start_index:end_index]"
            },
            new String[]{
                "some_list - a list.",
                "index_start - a number.",
                "index_end - a number."
            },
            "A list.",
            "Returns the subslist of 'some_list' beginning from the element at 'index_start' and ending before the element at 'index_end'.",
            new String[]{
                "If either 'index_start' or 'index_end' is negative, then it is taken as a position from the last, rather than the first, character of 'string'."
            }
    ),
    FUNCTION_COUNT(
            "Count Element",
            new String[]{
                "some_list.count(element)"
            },
            new String[]{
                "some_list - a list.",
                "element - a value."
            },
            "A non-negative integer.",
            "Returns the number of occurrences of 'element' in 'some_list'.",
            new String[]{
                "If 'element' does not occur in 'some_list', the returned value is 0."
            }
    ),
    FUNCTION_LISTINDEX(
            "Get Index",
            new String[]{
                "some_list.index(element)"
            },
            new String[]{
                "some_list - a list.",
                "element - a value."
            },
            "A non-negative integer.",
            "Returns the index of the first occurrence of 'element' in 'some_list'.",
            new String[]{
                "This function will cause an error if 'element' does not occur in 'some_list'."
            }
    ),
    FUNCTION_LISTLEN(
            "Length of List",
            new String[]{
                "len(some_list)"
            },
            new String[]{
                "some_list - a list."
            },
            "A non-negative integer.",
            "Returns the number of elements contained in 'some_list'.",
            new String[]{
                "If 'some_list' is empty, the returned value will be 0."
            }
    ),
    FUNCTION_ALL(
            "Is Every Element True?",
            new String[]{
                "all(some_list)"
            },
            new String[]{
                "some_list - a list of boolean (True/False) values."
            },
            "A boolean (True/False).",
            "Returns True if all the elements of 'some_list' are True. Returns False otherwise.",
            new String[]{}
    ),
    FUNCTION_ANY(
            "Is Any Element True?",
            new String[]{
                "any(some_list)"
            },
            new String[]{
                "some_list - a list of boolean (True/False) values."
            },
            "A boolean (True/False).",
            "Returns True if any of the elements of 'some_list' are True. Returns False otherwise.",
            new String[]{}
    ),
    FUNCTION_SORT(
            "Sort a List",
            new String[]{
                "sorted(some_list)"
            },
            new String[]{
                "some_list - a list of orderable values."
            },
            "A list.",
            "Returns a list containing the same elements as 'some_list', but sorted in ascending order.",
            new String[]{}
    ),
    FUNCTION_DEFINEDVALUES(
            "Get Defined Values (No Nulls)",
            new String[]{
                "defined_values(some_list)"
            },
            new String[]{
                "some_list - a list."
            },
            "A list.",
            "Returns a list containing all the values in 'some_list' which are not null.",
            new String[]{}
    ),
    FUNCTION_MAP(
            "Apply Function to a List",
            new String[]{
                "map(function, some_list)"
            },
            new String[]{
                "function - a function.",
                "some_list - a list."
            },
            "A list.",
            "Returns a list of results of evaulating 'function' for each element in 'some_list'.",
            new String[]{}
    ),
    FUNCTION_BOOL(
            "Convert to Boolean (True/False)",
            new String[]{
                "bool(value)"
            },
            new String[]{
                "value - a value that can be converted to a boolean.",},
            "A boolean (True/False).",
            "Returns the result of converting 'value' to its boolean representation.",
            new String[]{
                "This function will cause an error if 'value' does not have a boolean representation."
            }
    ),
    FUNCTION_INT(
            "Convert to Integer",
            new String[]{
                "int(value)"
            },
            new String[]{
                "value - a value that can be converted to an integer.",},
            "An integer.",
            "Returns the result of converting 'value' to its integer representation.",
            new String[]{
                "This function will cause an error if 'value' does not have an integer representation."
            }
    ),
    FUNCTION_FLOAT(
            "Convert to Float (Decimal Number)",
            new String[]{
                "float(value)"
            },
            new String[]{
                "value - a value that can be converted to a float.",},
            "A float (decimal number).",
            "Returns the result of converting 'value' to its float representation.",
            new String[]{
                "This function will cause an error if 'value' does not have a float representation."
            }
    ),
    FUNCTION_STRING(
            "Convert to String",
            new String[]{
                "str(value)"
            },
            new String[]{
                "value - a value that can be converted to a string.",},
            "A string.",
            "Returns the result of converting 'value' to its string representation.",
            new String[]{
                "This function will cause an error if 'value' does not have a string representation."
            }
    ),
    FUNCTION_LIST(
            "Convert to List",
            new String[]{
                "list(collection)"
            },
            new String[]{
                "collection - a collection that can be converted to a list.",},
            "A list.",
            "Returns the result of converting 'value' to its list representation.",
            new String[]{
                "This function will cause an error if 'value' does not have a list representation."
            }
    ),
    FUNCTION_SET(
            "Convert to Set",
            new String[]{
                "set(collection)"
            },
            new String[]{
                "collection - a collection that can be converted to a set.",},
            "A set.",
            "Returns the result of converting 'value' to its set representation.",
            new String[]{
                "All values are unique in a set, hence this conversion function can be used to get only distinct values from a list.",
                "This function will cause an error if 'value' does not have a set representation."
            }
    ),
    FUNCTION_TIME_YEAR(
            "Get Year",
            new String[]{
                "year(datetime)"
            },
            new String[]{
                "datetime - a temporal value from CONSTELLATION; that is either a time, date or datetime attribute",},
            "An integer.",
            "Returns the year in which 'datetime' occurred.",
            new String[]{}
    ),
    FUNCTION_TIME_MONTH(
            "Get Month",
            new String[]{
                "month(datetime)"
            },
            new String[]{
                "datetime - a temporal value from CONSTELLATION; that is either a time, date or datetime attribute",},
            "An integer.",
            "Returns the month in which 'datetime' occurred.",
            new String[]{}
    ),
    FUNCTION_TIME_DAY(
            "Get Day",
            new String[]{
                "day(datetime)"
            },
            new String[]{
                "datetime - a temporal value from CONSTELLATION; that is either a time, date or datetime attribute",},
            "An integer.",
            "Returns the day in which 'datetime' occurred.",
            new String[]{}
    ),
    FUNCTION_TIME_HOUR(
            "Get Hour",
            new String[]{
                "hour(datetime)"
            },
            new String[]{
                "datetime - a temporal value from CONSTELLATION; that is either a time, date or datetime attribute",},
            "An integer.",
            "Returns the hour in which 'datetime' occurred.",
            new String[]{}
    ),
    FUNCTION_TIME_MIN(
            "Get Minute",
            new String[]{
                "minute(datetime)"
            },
            new String[]{
                "datetime - a temporal value from CONSTELLATION; that is either a time, date or datetime attribute",},
            "An integer.",
            "Returns the minute in which 'datetime' occurred.",
            new String[]{}
    ),
    FUNCTION_TIME_SEC(
            "Get Second",
            new String[]{
                "second(datetime)"
            },
            new String[]{
                "datetime - a temporal value from CONSTELLATION; that is either a time, date or datetime attribute",},
            "An integer.",
            "Returns the second in which 'datetime' occurred.",
            new String[]{}
    ),
    FUNCTION_TIME_WEEKDAY(
            "Get Weekday",
            new String[]{
                "weekday(datetime)"
            },
            new String[]{
                "datetime - a temporal value from CONSTELLATION; that is either a time, date or datetime attribute",},
            "An integer.",
            "Returns the weekday in which 'datetime' occurred.",
            new String[]{}
    ),
    FUNCTION_TIME_MONTH_NAME(
            "Get Month Name",
            new String[]{
                "month_name(datetime)"
            },
            new String[]{
                "datetime - a temporal value from CONSTELLATION; that is either a time, date or datetime attribute",},
            "A string.",
            "Returns the name of the month in which 'datetime' occurred.",
            new String[]{}
    ),
    FUNCTION_TIME_WEEKDAY_NAME(
            "Get Weekday Name",
            new String[]{
                "weekday_name(datetime)"
            },
            new String[]{
                "datetime - a temporal value from CONSTELLATION; that is either a time, date or datetime attribute",},
            "A string.",
            "Returns the name of the weekday in which 'datetime' occurred.",
            new String[]{}
    ),
    FUNCTION_TIME_FROM_DATE(
            "Get Time",
            new String[]{
                "time_from_date(datetime)"
            },
            new String[]{
                "datetime - a temporal value from CONSTELLATION; that is either a time, date or datetime attribute",},
            "A time.",
            "Returns the time component (hours, minutes and seconds) of 'datetime'.",
            new String[]{}
    ),
    FUNCTION_TIME_STRING_FROM_DATE(
            "Get Time as String",
            new String[]{
                "time_string_from_date(datetime)"
            },
            new String[]{
                "datetime - a temporal value from CONSTELLATION; that is either a time, date or datetime attribute",},
            "A string.",
            "Returns the time component as a string (in the format hh:mm:ss) from 'datetime'.",
            new String[]{}
    ),
    FUNCTION_TIME_DATE_AS_STRING(
            "Get Date as String",
            new String[]{
                "date_as_string(datetime)"
            },
            new String[]{
                "datetime - a temporal value from CONSTELLATION; that is either a time, date or datetime attribute",},
            "A string.",
            "Returns (in the format dd/MM/yy hh:mm:ss) a string representation of 'datetime'.",
            new String[]{
                "To retrieve, for example, just the date without the time, you can just take a substring like so: date_as_string(datetime)[0:10]"
            }
    ),
    FUNCTION_TIME_NOW(
            "Current DateTime",
            new String[]{
                "now()"
            },
            new String[]{},
            "A time.",
            "Returns the current datetime as of when the script was run.",
            new String[]{}
    ),
    FUNCTION_TIME_DAYS(
            "Number of Days",
            new String[]{
                "days(time_difference)"
            },
            new String[]{
                "time_difference - a duration, that is the difference between two CONSTELLATION temporal values."
            },
            "An integer.",
            "Returns the total number of days (rounded down) contained within 'time_difference'.",
            new String[]{}
    ),
    FUNCTION_TIME_HOURS(
            "Number of Hours",
            new String[]{
                "hours(time_difference)"
            },
            new String[]{
                "time_difference - a duration, that is the difference between two CONSTELLATION temporal values."
            },
            "An integer.",
            "Returns the total number of hours (rounded down) contained within 'time_difference'.",
            new String[]{}
    ),
    FUNCTION_TIME_MINUTES(
            "Number of Minutes",
            new String[]{
                "minutes(time_difference)"
            },
            new String[]{
                "time_difference - a duration, that is the difference between two CONSTELLATION temporal values."
            },
            "An integer.",
            "Returns the total number of minutes (rounded down) contained within 'time_difference'.",
            new String[]{}
    ),
    FUNCTION_TIME_SECONDS(
            "Number of Seconds",
            new String[]{
                "seconds(time_difference)"
            },
            new String[]{
                "time_difference - a duration, that is the difference between two CONSTELLATION temporal values."
            },
            "An integer.",
            "Returns the total number of seconds (rounded down) contained within 'time_difference'.",
            new String[]{}
    ),;

    public final String templateName;
    public final String[] usage;
    public final String[] arguments;
    public final String returns;
    public final String description;
    public final String[] notes;

    private CalculatorTemplateDescription(String name, String[] usage, String[] arguments, String returns, String description, String[] notes) {
        this.templateName = name;
        this.usage = usage;
        this.arguments = arguments;
        this.returns = returns;
        this.description = description;
        this.notes = notes;
    }

    public static CalculatorTemplateDescription getInstanceFromName(String name) {
        for (CalculatorTemplateDescription c : CalculatorTemplateDescription.values()) {
            if (c.templateName.equals(name)) {
                return c;
            }
        }
        return null;
    }

}
