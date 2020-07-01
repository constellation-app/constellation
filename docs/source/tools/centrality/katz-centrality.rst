Katz Centrality
---------------

Katz Centrality expands on Eigenvector Centrality by taking into account the number of ways to travel between two nodes, rather than just the single directed transaction between them. There is major drawback to using Katz Centrality - if a node with high centrality is pointing to lots of other nodes, they will also get high centrality Imagine if you had a node on your graph with over 1000 neighbours. If this node has a high centrality, all of those neighbours would also score highly, which would not be especially useful analytically.

PageRank Centrality, the algorithm used by Google to rank search results, attempts to resolve this issue. Using Pagerank Centrality, instead of a high degree node giving a large centrality to all its neighbours, it evenly distributes the centrality it has to share.


.. help-id: au.gov.asd.tac.constellation.plugins.algorithms.centrality.KatzCentralityPlugin
