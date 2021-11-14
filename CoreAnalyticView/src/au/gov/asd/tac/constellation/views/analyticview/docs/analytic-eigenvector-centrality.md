# Eigenvector Centrality

In graph theory, centrality is any measure that determines how
structurally important a node is within a graph and the degree of a node
is a count of its neighbours.

Eigenvector Centrality is a measure of a node’s importance determined by
the number of neighbours, and the average degree of its neighbours. It
is a variation of Degree Centrality that is less biased towards high
degree nodes.

## Parameters

-   *Transaction Types* - Calculate on only the subgraph of transactions
    of these types
-   *Iterations* - The number of iterations to run before returning a
    result
-   *Epsilon* - The change threshold at which equilibrium can be
    considered reached
-   *Normalise By Max Possible Score* - Normalise calculated scores by
    the maximum possible score
-   *Normalise By Max Available Score* - Normalise calculated scores by
    the maximum calculated score
