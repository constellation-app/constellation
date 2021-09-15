# HITS Centrality

In graph theory, centrality is any measure that determines how
structurally important a node is within a graph.

Hyperlink-Induced Topic Search (HITS) Centrality is a link analysis
algorithm that categorises nodes into hubs or authorities.

A hub is a node with incoming transactions from many neighbours and an
authority is a node with incoming transactions from many different hubs.
Authorities hold influential positions in a network.

## Parameters

-   *Transaction Types* - Calculate on only the subgraph of transactions
    of these types
-   *Iterations* - The number of iterations to run before returning a
    result
-   *Epsilon* - The change threshold at which equilibrium can be
    considered reached
-   *Normalise By Max Available Score* - Normalise calculated scores by
    the maximum calculated score
