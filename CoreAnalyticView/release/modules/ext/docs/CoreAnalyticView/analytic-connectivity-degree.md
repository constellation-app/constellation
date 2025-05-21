# Connectivity Degree

In graph theory, a component is a group of nodes that are completely
connected and a node's component size is the number of nodes in its
component.

The connectivity degree of a node is the number of components that node
holds together. Deleting the node from the graph will result in a
disconnected graph made up of a number of components equal to the
connectivity degree.

## Parameters

-   *Transaction Types* - Calculate on only the subgraph of transactions
    of these types
-   *Include Incoming* - Include incoming connections
-   *Include Outgoing* - Include outgoing connections
-   *Include Undirected* - Treat undirected connections as bidirectional
    connections
-   *Ignore Singletons* - Singletons are not treated as graph components
-   *Normalise Score* - Score should indicate the number of components
    on the graph if the vertex was deleted. Otherwise, it indicates how
    many components are added if the vertex was deleted
