# Effective Resistance

Otherwise known as the Resistance Distance, this is a measure of the
resistance that would be held by each link if it were an electrical
network. This is achieved by replacing each link in the graph with a 1
ohm resistor (or if the weighted parameter is set to true, an n ohm
resistor, where n is the weight of that link) and using Kirchoff's
current/voltage laws to measure the resistance at each junction. These
resistances highlight which paths are easy or difficult to traverse, and
in general nodes which lie on easily traversable paths can be easily
reached.

## Parameters

-   *Transaction Types* - Calculate on only the subgraph of transactions
    of these types
-   *Weighted* - Account for the weight of links
-   *Normalise By Max Available Score* - Normalise calculated scores by
    the maximum calculated score
