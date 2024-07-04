# Jaccard Index

Similarity measures are used to determine how many features two entities
have in common.

The Jaccard Index is the ratio of common neighbours between two nodes
compared to the total number of neighbours of both nodes. The more
neighbours two nodes have in common, the more similar they are. The
features used to determine similarity with the Jaccard Index are a
node's neighbours.

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
