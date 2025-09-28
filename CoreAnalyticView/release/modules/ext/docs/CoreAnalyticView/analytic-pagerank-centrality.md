# Pagerank Centrality

In graph theory, centrality is any measure that determines how
structurally important a node is within a graph.

Pagerank Centrality is a measure of a node's importance calculated by
counting the number and quality of incoming transactions. The assumption
is that important nodes are more likely to receive transactions from
other nodes.

## Parameters

-   *Transaction Types* - Calculate on only the subgraph of transactions
    of these types
-   *Include Undirected* - Treat undirected connections as bidirectional
    connections
-   *Damping Factor* - The damping factor to apply at each iteration
-   *Iterations* - The number of iterations to run before returning a
    result
-   *Epsilon* - The change threshold at which equilibrium can be
    considered reached
-   *Normalise By Max Available Score* - Normalise calculated scores by
    the maximum calculated score
