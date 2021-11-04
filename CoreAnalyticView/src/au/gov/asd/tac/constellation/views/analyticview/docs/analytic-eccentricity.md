# Eccentricity

In graph theory, distance is measured by the number of hops it takes to
get from one point to another. The shortest path distance between two
nodes is path that takes the minimum number of hops.

The eccentricity of a node is the shortest path distance between it and
the furthest node. It is a measure that can be used to determine whether
a node is located in the middle of a network (low eccentricity), or on
the outskirts of a network (high eccentricity).

## Parameters

-   *Transaction Types* - Calculate on only the subgraph of transactions
    of these types
-   *Include Incoming* - Include incoming connections
-   *Include Outgoing* - Include outgoing connections
-   *Include Undirected* - Treat undirected connections as bidirectional
    connections
-   *Normalise By Max Available Score* - Normalise calculated scores by
    the maximum calculated score
-   *Normalise Connected Components* - Apply normalisation separately
    for each connected component
