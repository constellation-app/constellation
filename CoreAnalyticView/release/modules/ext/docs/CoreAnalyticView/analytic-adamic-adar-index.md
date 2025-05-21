# Adamic-Adar Index

Similarity measures are used to determine how many features two entities
have in common.

See [Resource Allocation Index](../ext/docs/CoreAnalyticView/analytic-resource-allocation-index.md). 
The Adamic-Adar Index analytic calculates the fraction of a "resource" 
that a node can send to another through their common neighbours. It is a 
variation of the Resource Allocation Index in that it is calculated by 
summing 1 divided by the log of the degree of each common neighbour node. 
The Soundarajan-Hopcroft Community Adamic-Adar Index parameter will only 
score pairs of nodes that are both selected (that is, sharing resources 
within the same community).

## Parameters

-   *Transaction Types* - Calculate on only the subgraph of transactions
    of these types
-   *Include Incoming* - Include incoming connections
-   *Include Outgoing* - Include outgoing connections
-   *Include Undirected* - Treat undirected connections as bidirectional
    connections
-   *Minimum Common Features* - Only calculate similarity between nodes
    that share at least this many features
-   *Selected Only* - Calculate using only selected graph elements
-   *Community Adamic-Adar Index Soundarajan-Hopcroft Score* - Only
    calculates score when both nodes are selected
