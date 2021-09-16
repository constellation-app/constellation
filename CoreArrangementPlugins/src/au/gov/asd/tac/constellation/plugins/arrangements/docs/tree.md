# Tree

<table class="table table-striped">
<thead>
<tr class="header">
<th>Constellation Action</th>
<th>Keyboard Shortcut</th>
<th>User Action</th>
<th style="text-align: center;">Menu Icon</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Run Tree Arrangement</td>
<td>Ctrl + T</td>
<td>Arrange -&gt; Trees</td>
<td style="text-align: center;"><img src="../constellation/CoreArrangementPlugins/src/au/gov/asd/tac/constellation/plugins/arrangements/docs/resources/arrangeInTree.png" alt="Tree Arrangement Icon" /></td>
</tr>
</tbody>
</table>

The tree arrangement arranges all the nodes in tree-like structures with
"root" nodes arranged by proximity and then "leaf" nodes placed around
their root node circularly. Singleton nodes are arranged together in a
grid, similarly for doublets (pairs of nodes only connected to each
other). The way nodes are placed on the graph will look slightly
different each time the arrangement is run.

The root nodes are determined by iteratively removing all the nodes with
only one neighbour until you are left with a set of nodes where each has
more than one neighbour.

Example Tree Arrangement:

<div style="text-align: center">

<img src="../constellation/CoreArrangementPlugins/src/au/gov/asd/tac/constellation/plugins/arrangements/docs/resources/TreeArrangement.png" alt="Example Tree
Arrangement" />

</div>
