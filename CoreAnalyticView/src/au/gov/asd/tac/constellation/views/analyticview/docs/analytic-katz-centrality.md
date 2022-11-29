# Katz Centrality

In graph theory, centrality is any measure that determines how
structurally important a node is within a graph.

Katz Centrality computes the influence of a node by calculating the
number of neighbours, and other nodes on the graph that connect to the
node through these immediate neighbours. Connections made with distant
neighbours are penalised by an attenuation factor. It is a variation of
Eigenvector Centrality that incorporates the direction of transactions.

There is major drawback to using Katz Centrality - if a node with high
centrality is pointing to lots of other nodes, they will also get high
centrality. e.g. If you have a node on your graph with over 1000
neighbours and this node has a high centrality, all of those neighbours
would also score highly, which would not be especially useful
analytically. PageRank Centrality, the algorithm used by Google to rank
search results, attempts to resolve this issue by evenly distributing
the centrality a node has to share.

## Parameters

-   *Transaction Types* - Calculate on only the subgraph of transactions
    of these types
-   *Alpha* - The attenuation factor
-   *Beta* - The weight attributed to the immediate neighbourhood
-   *Iterations* - The number of iterations to run before returning a
    result
-   *Epsilon* - The change threshold at which equilibrium can be
    considered reached
-   *Normalise By Max Possible Score* - Normalise calculated scores by
    the maximum possible score
-   *Normalise By Max Available Score* - Normalise calculated scores by
    the maximum calculated score
