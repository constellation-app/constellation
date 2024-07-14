# Tree

<table class="table table-striped">
<colgroup>
<col style="width: 25%" />
<col style="width: 25%" />
<col style="width: 25%" />
<col style="width: 25%" />
</colgroup>
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
<td style="text-align: center;"><img src="../ext/docs/CoreArrangementPlugins/src/au/gov/asd/tac/constellation/plugins/arrangements/resources/arrangeInTree.png" alt="Tree Arrangement Icon" /></td>
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
    <figure style = "display: inline-block">
        <img height=400 src="../ext/docs/CoreArrangementPlugins/src/au/gov/asd/tac/constellation/plugins/arrangements/resources/BeforeTreeArrangement.png" alt="Before Arrangement" />
        <figcaption>Before Tree Arrangement</figcaption>
    </figure>
    <figure style = "display: inline-block">
        <img height=400 src="../ext/docs/CoreArrangementPlugins/src/au/gov/asd/tac/constellation/plugins/arrangements/resources/TreeArrangement.png" alt="Example Tree Arrangement" />
        <figcaption>After Tree Arrangement</figcaption>
    </figure>
</div>