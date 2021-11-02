# The Graph Window

The graph window provides a 3D visual display of a graph.

<div style="text-align: center">

<img src="../constellation/CoreFunctionality/src/au/gov/asd/tac/constellation/functionality/docs/resources/GraphView.png" width="573" height="378" alt="The Graph Window" />

</div>

## Navigating The Graph

You can navigate the graph using the mouse and keyboard keys (refer
[here](../constellation/CoreInteractiveGraph/src/au/gov/asd/tac/constellation/graph/interaction/docs/add-and-selection-modes.md)
for the mouse controls). Menus can be used to manipulate the graph. Some
menus have keys assigned to them by default, but you can change the key
assignments using Tools -> Options -> Keymap.

## Graph Sidebar

Each graph display has a sidebar containing some commonly used actions:

<table class="table table-striped">
<colgroup>
<col style="width: 50%" />
<col style="width: 50%" />
</colgroup>
<thead>
<tr class="header">
<th>Icon</th>
<th>Action</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td><img src="../constellation/CoreFunctionality/src/au/gov/asd/tac/constellation/functionality/docs/resources/2d.png" alt="2D Rotation Icon" /></td>
<td>Two Dimensional rotation. Only allow rotation around the Z-Axis<br />
Useful for 2D graphs (all nodes have same z value)</td>
</tr>
<tr class="even">
<td><img src="../constellation/CoreFunctionality/src/au/gov/asd/tac/constellation/functionality/docs/resources/3d.png" alt="3D Rotation Icon" /></td>
<td>Three Dimensional rotation. Allow rotation around X, Y, and Z axes.</td>
</tr>
<tr class="odd">
<td><img src="../constellation/CoreFunctionality/src/au/gov/asd/tac/constellation/functionality/docs/resources/links.png" alt="Draw Links Icon" /></td>
<td>Draw links.</td>
</tr>
<tr class="even">
<td><img src="../constellation/CoreFunctionality/src/au/gov/asd/tac/constellation/functionality/docs/resources/edges.png" alt="Draw Edges Icon" /></td>
<td>Draw edges.</td>
</tr>
<tr class="odd">
<td><img src="../constellation/CoreFunctionality/src/au/gov/asd/tac/constellation/functionality/docs/resources/transactions.png" alt="Draw Transactions Icon" /></td>
<td>Draw transactions.</td>
</tr>
<tr class="even">
<td><img src="../constellation/CoreFunctionality/src/au/gov/asd/tac/constellation/functionality/docs/resources/nodes.png" alt="Draw Nodes Icon" /></td>
<td>Draw/Don't draw nodes.</td>
</tr>
<tr class="odd">
<td><img src="../constellation/CoreFunctionality/src/au/gov/asd/tac/constellation/functionality/docs/resources/connections.png" alt="Draw Connections Icon" /></td>
<td>Draw/Don't draw connections.</td>
</tr>
<tr class="even">
<td><img src="../constellation/CoreFunctionality/src/au/gov/asd/tac/constellation/functionality/docs/resources/node_labels.png" alt="Draw Labels on Nodes Icon" /></td>
<td>Draw/Don't draw labels on nodes.</td>
</tr>
<tr class="odd">
<td><img src="../constellation/CoreFunctionality/src/au/gov/asd/tac/constellation/functionality/docs/resources/connection_labels.png" alt="Draw Labels on Connections Icon" /></td>
<td>Draw/Don't draw labels on connections.</td>
</tr>
<tr class="even">
<td><img src="../constellation/CoreFunctionality/src/au/gov/asd/tac/constellation/functionality/docs/resources/blazes.png" alt="Draw Blazes Icon" /></td>
<td>Draw/Don't draw blazes.</td>
</tr>
<tr class="odd">
<td><img src="../constellation/CoreFunctionality/src/au/gov/asd/tac/constellation/functionality/docs/resources/visible.png" alt="Ignore Visibility Threshold Icon" /></td>
<td>Show all nodes, connections, blazes and labels. With this setting, the Graph Visibility Threshold is ignored.</td>
</tr>
<tr class="even">
<td><img src="../constellation/CoreFunctionality/src/au/gov/asd/tac/constellation/functionality/docs/resources/hidden.png" alt="Enforce Visibility Threshold Icon" /></td>
<td>Hide all nodes, connections, blazes and labels when the Graph Visibility Threshold is reached.<br />
Visualising a large graph will have an impact on performance so this helps reduce the load on the graphics card. You can still use the histogram and table view to clean up your data before you enable graph visualisation.</td>
</tr>
<tr class="odd">
<td><img src="../constellation/CoreFunctionality/src/au/gov/asd/tac/constellation/functionality/docs/resources/expand.png" alt="Expand Composite Nodes Icon" /></td>
<td>Expand a composite node back to nodes and transactions</td>
</tr>
<tr class="even">
<td><img src="../constellation/CoreFunctionality/src/au/gov/asd/tac/constellation/functionality/docs/resources/contract.png" alt="Contract Composite Nodes Icon" /></td>
<td>Contract selected nodes into a composite node</td>
</tr>
<tr class="odd">
<td><img src="../constellation/CoreFunctionality/src/au/gov/asd/tac/constellation/functionality/docs/resources/draw_mode.png" alt="Draw Mode Icon" /></td>
<td>Draw Mode</td>
</tr>
<tr class="even">
<td><img src="../constellation/CoreFunctionality/src/au/gov/asd/tac/constellation/functionality/docs/resources/select_mode.png" alt="Select Mode Icon" /></td>
<td>Select Mode</td>
</tr>
<tr class="odd">
<td><img src="../constellation/CoreFunctionality/src/au/gov/asd/tac/constellation/functionality/docs/resources/directed.png" alt="Draw Directed Transactions Icon" /></td>
<td>Draw Directed Transactions</td>
</tr>
<tr class="even">
<td><img src="../constellation/CoreFunctionality/src/au/gov/asd/tac/constellation/functionality/docs/resources/undirected.png" alt="Draw Undirected Transactions Icon" /></td>
<td>Draw Undirected Transactions</td>
</tr>
</tbody>
</table>

When links and edges are drawn and the underlying transactions have
different colors, grey is used. When transactions are drawn (each
transaction is drawn with its own line), transactions are drawn
individually up to a limit (default limit is eight). If there are more
than eight transactions between two nodes, they will be drawn as the
corresponding edge.

Text can be drawn above and below nodes: use the graph-level attributes
"node\_labels\_bottom" and "node\_labels\_top" to determine what text is
drawn. Similarly for transactions using the attribute
"transaction\_labels".
