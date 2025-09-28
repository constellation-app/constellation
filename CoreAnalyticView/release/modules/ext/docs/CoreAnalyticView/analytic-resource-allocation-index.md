# Resource Allocation Index

Similarity measures are used to determine how many features two entities
have in common.

The Resource Allocation Index analytic calculates the fraction of a
"resource" that a node can send to another through their common
neighbours. It is calculated by summing 1 divided by the degree of each
common neighbour node. The Soundarajan-Hopcroft Community Resource
Allocation parameter will only score pairs of nodes that are both
selected (that is, sharing resources within the same community).

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
-   *Community Resource Allocation Soundarajan-Hopcroft Score* - Only
    calculates score when both nodes are selected
