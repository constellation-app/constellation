Closeness Centrality
--------------------

Closeness Centrality uses shortest paths to determine the importance of a node. For each node on the graph, the distance of each shortest path between that node and every other node on the graph is calculated. Closeness is then the average distance of each shortest path from that node. This can tell us how long it would take for information to spread from a node to the rest of the network.

An important thing to keep in mind when using Closeness Centrality for analysis is that this method of centrality treats each disconnected subgraph in a network as a separate graph. Be mindful that this will be reflected in the results. Harmonic Closeness Centrality resolves this issue by modifying the method for calculating the average shortest path.


.. help-id: au.gov.asd.tac.constellation.plugins.algorithms.centrality.ClosenessCentralityPlugin
