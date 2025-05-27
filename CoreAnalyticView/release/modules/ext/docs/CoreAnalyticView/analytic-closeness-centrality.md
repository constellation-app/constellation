# Closeness Centrality

In graph theory, centrality is any measure that determines how
structurally important a node is within a graph and distance is measured
by the number of hops it takes from get from one point to another. The
shortest path distance between two nodes is the path that takes the 
minimum number of hops.

Closeness Centrality is a measure of a node's importance determined by
the average shortest path distance from that node to every other node on
the graph. The lower the average distance to all other nodes, the more
central the position in the network.

An important thing to keep in mind when using Closeness Centrality is
that this method treats each disconnected subgraph in a network as a
separate graph. Be mindful that this will be reflected in the results.
Harmonic Closeness Centrality resolves this issue by modifying the
method for calculating the average shortest path.

## Parameters

-   *Transaction Types* - Calculate on only the subgraph of transactions
    of these types
-   *Harmonic* - Calculate scores using the harmonic mean
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
