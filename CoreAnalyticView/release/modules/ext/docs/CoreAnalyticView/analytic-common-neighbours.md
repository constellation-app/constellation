# Common Neighbours

Similarity measures are used to determine how many features two entities
have in common.

The Common Neighbours analytic calculates the number of common
neighbours between two nodes. The Soundarajan-Hopcroft Common Community
Neighbours parameter will add a bonus of 1 to the score if both nodes
are selected.

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
-   *Common Neighbours Soundarajan-Hopcroft Score* - If both nodes are
    selected, adds bonus of 1 to score
