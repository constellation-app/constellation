# Add and Selection Modes

## Add Mode

Select the <img src="../ext/docs/CoreInteractiveGraph/src/au/gov/asd/tac/constellation/graph/interaction/resources/add.png" alt="Add Mode
Icon" />
icon to switch to add mode. This mode allows the user to manually create
nodes and transactions.

<table class="table table-striped">
<colgroup>
<col style="width: 50%" />
<col style="width: 50%" />
</colgroup>
<thead>
<tr class="header">
<th>Graph Action</th>
<th>Mouse Actions</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Create a Node</td>
<td>Left-click on the background</td>
</tr>
<tr class="even">
<td>Create a Transaction</td>
<td><ol>
<li>Left-click on a node</li>
<li>Left-click on another node (or same node for a loop)</li>
</ol></td>
</tr>
<tr class="odd">
<td>Create a Transaction Sequence</td>
<td><ol>
<li>Left-click on a node</li>
<li>Ctrl-left-click on another node (or same node for a loop). A second transaction will automatically be created starting from that node.</li>
<li>Repeat step 2 until final transaction.</li>
<li>Left-click on another node (or same node for a loop)</li>
</ol></td>
</tr>
<tr class="even">
<td>Create a Series of Transactions Starting From the Same Node</td>
<td><ol>
<li>Left-click on a node</li>
<li>Shift-left-click on another node (or same node for a loop). A second transaction will automatically be created starting from the original node.</li>
<li>Repeat step 2 until final transaction.</li>
<li>Left-click on another node (or same node for a loop)</li>
</ol></td>
</tr>
</tbody>
</table>

NOTE: While creating a transaction, you can left-click on the background
or press ESC to abort creating it.

When creating transactions, you can choose whether to generate directed
or undirected transactions by selecting either the <img src="../ext/docs/CoreInteractiveGraph/src/au/gov/asd/tac/constellation/graph/interaction/resources/directed.png" alt="Directed
Transactions Icon" />
icon (for directed) or the <img src="../ext/docs/CoreInteractiveGraph/src/au/gov/asd/tac/constellation/graph/interaction/resources/undirected.png" alt="Undirected Transactions
Icon" />
icon (for undirected). By default, directed will be selected.

## Selection Mode

Select the <img src="../ext/docs/CoreInteractiveGraph/src/au/gov/asd/tac/constellation/graph/interaction/resources/select.png" alt="Selection Mode
Icon" />
icon to switch to selection mode. This mode allows the user to select
elements in the graph and perform graphical operations such as zoom,
pan, and rotate.

Navigating the graph in selection mode using the mouse is done as
follows (In general the left button is for selecting, the middle button
for rotating, and the right button for dragging and panning):

<table class="table table-striped">
<colgroup>
<col style="width: 50%" />
<col style="width: 50%" />
</colgroup>
<thead>
<tr class="header">
<th>Mouse Action</th>
<th>Graph Action</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Left-click on an element</td>
<td>Select a node or transaction.</td>
</tr>
<tr class="even">
<td>Left-click on the background</td>
<td>Set focus on the graph window.</td>
</tr>
<tr class="odd">
<td>Double-left-click on the background</td>
<td>Deselect all elements</td>
</tr>
<tr class="even">
<td>Shift-left-click on an element</td>
<td>Select additional nodes and/or transactions.</td>
</tr>
<tr class="odd">
<td>Ctrl-left-click on an element</td>
<td>Toggle selection of nodes and/or transactions.</td>
</tr>
<tr class="even">
<td>Left-drag</td>
<td>Select multiple nodes and/or transactions with a box selection.</td>
</tr>
<tr class="odd">
<td>Shift-left-drag</td>
<td>Select additional multiple nodes and/or transactions with a box selection.</td>
</tr>
<tr class="even">
<td>Ctrl-left-drag</td>
<td>Toggle selection of multiple nodes and/or transactions with a box selection.</td>
</tr>
<tr class="odd">
<td>Alt-left-drag</td>
<td>Select multiple nodes and/or transactions with a freeform selection.</td>
</tr>
<tr class="even">
<td>Alt-shift-left-drag</td>
<td>Select additional multiple nodes and/or transactions with a freeform selection.</td>
</tr>
<tr class="odd">
<td>Alt-ctrl-left-drag</td>
<td>Toggle selection of multiple nodes and/or transactions with a freeform selection.</td>
</tr>
<tr class="even">
<td>Middle-drag</td>
<td>Rotate the graph.
<ul>
<li>Dragging the mouse up and down the middle of the window will rotate around the X axis.</li>
<li>Dragging the mouse across the middle of the window will rotate around the Y axis.</li>
<li>Dragging the mouse along the edges of the window will rotate around the Z axis.</li>
</ul></td>
</tr>
<tr class="odd">
<td>Right-drag on background</td>
<td>Pan the graph.</td>
</tr>
<tr class="even">
<td>Right-drag on node</td>
<td>Drag the node and other selected nodes.</td>
</tr>
<tr class="odd">
<td>Right-drag on transaction</td>
<td>Pan the graph.</td>
</tr>
</tbody>
</table>

