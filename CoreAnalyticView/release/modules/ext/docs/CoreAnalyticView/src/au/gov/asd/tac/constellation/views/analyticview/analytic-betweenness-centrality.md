# Betweenness Centrality

In graph theory, centrality is any measure that determines how
structurally important a node is within a graph and a shortest path
distance between two nodes is path that takes the minimum number of
hops. The shortest path between two nodes is typically the most likely
route for information to flow through a network.

Betweenness Centrality is a measure of importance calculated by
determining the shortest path between every pair of nodes on the graph,
and then counting the number of shortest paths each node falls on. The
more shortest paths a node falls on, the more information it is likely
to receive.

## Parameters

-   *Transaction Types* - Calculate on only the subgraph of transactions
    of these types
-   *Include Incoming* - Include incoming connections
-   *Include Outgoing* - Include outgoing connections
-   *Include Undirected* - Treat undirected connections as bidirectional
    connections
-   *Normalise By Max Possible Score* - Normalise calculated scores by
    the maximum possible score
-   *Normalise By Max Available Score* - Normalise calculated scores by
    the maximum calculated score
-   *Normalise Connected Components* - Apply normalisation separately
    for each connected component
-   *Selected Only* - Calculate using only selected graph elements
