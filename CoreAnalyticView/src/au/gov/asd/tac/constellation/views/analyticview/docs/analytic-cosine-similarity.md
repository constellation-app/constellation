# Cosine Similarity

Similarity measures are used to determine how many features two entities
have in common.

Cosine Similarity, in general, is used to determine how similar two
vectors (geometric objects defined by a magnitude and a direction) are
based on the cosine of the angle between them. For a graph, these
vectors are composed of features or behaviours represented by the
network (eg. node A contacted node B, node A is correlated to node B).
This basically results in a high similarity score being assigned to any
pair of nodes which are connected to similar neighbours a similar amount
of times.

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
