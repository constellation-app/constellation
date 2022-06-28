# The Constellation Graph Model

The diagram below shows the components of a graph:

<div style="text-align: center">

<img src="../constellation/CoreGraphFramework/src/au/gov/asd/tac/constellation/graph/docs/resources/graph-model.png" alt="Graph
Model" />

</div>

A transaction represents an interaction between two nodes; it can be
directed (from node 1 to node 2 or vise-versa) or undirected. There can
be multiple transactions between two nodes.

An edge is the collection of transactions in a single direction between
two nodes. Because there are only three directions (as described above),
there can only be up to three edges between two nodes.

A link is the collection of edges (and therefore transactions) between
two nodes. There can be no more than one link between two nodes. A link
has no direction.
