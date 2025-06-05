# Ratio of Reciprocity

Ratio of Reciprocity is a simple ratio of the number of incoming
transactions to the number of outgoing transactions. This produces an
importance score where lower scores indicate more one-sided
relationships, and higher scores indicate more reciprocal relationships.
You can use this to compare the neighbours of a node and rank them in
order from most reciprocated to most one-sided.

## Parameters

-   *Transaction Types* - Calculate on only the subgraph of transactions
    of these types
-   *Include Undirected* - Treat undirected connections as bidirectional
    connections
-   *Normalise By Max Available Score* - Normalise calculated scores by
    the maximum calculated score
