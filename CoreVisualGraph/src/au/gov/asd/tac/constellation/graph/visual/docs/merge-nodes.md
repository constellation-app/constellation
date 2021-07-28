# Merge Nodes

## Merge all the selected nodes into a single node.

<table data-border="1">
<caption>Merge Nodes Actions</caption>
<thead>
<tr class="header">
<th scope="col">Constellation Action</th>
<th scope="col">Keyboard Shortcut</th>
<th scope="col">User Action</th>
<th style="text-align: center;" scope="col">Menu Icon</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Run Merge Nodes</td>
<td></td>
<td>Tools -&gt; Merge Nodes</td>
<td style="text-align: center;"><img src="../plugins/merge/mergeNodes.png" alt="Merge Nodes Icon" /></td>
</tr>
</tbody>
</table>

Merge Nodes Actions

NOTE: This is a standalone plugin. For the Data Access Plugin, go
[here](/au/gov/asd/tac/constellation/views/dataaccess/docs/merge-nodes.html)

This plugin alters the structure of the graph by deleting all but one of
the nodes and reconnecting all the transactions to the remaining node.
It requires at least two selected nodes in order to work.

<div style="text-align: center">

![Merge Nodes Dialog](resources/PermanentMergeNodes.png)

</div>

## Identifying the Nodes to be Merged

By default, all of the nodes are selected for merging. The selected
nodes are marked in the table by the check box in the left-most column.
Any node can be excluded/included from the merge operation by either
clicking in the checkbox or using the "Include All" or "Exclude All"
actions in the lower left-hand corner of the panel. The total number of
selected nodes is displayed in the lower right-hand corner of the panel.

## Attribute Values of the Merge Node

By default, the final merge node will get a copy of all the attributes
of the first node listed in the table. Clicking an individual cell in
each column will identify the attribute values to be copied to the
merged node. Alternatively, clicking an "ID" value will select all the
attributes values of the given row.
