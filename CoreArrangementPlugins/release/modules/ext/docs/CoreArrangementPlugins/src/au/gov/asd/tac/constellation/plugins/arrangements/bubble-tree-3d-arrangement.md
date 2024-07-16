# Bubble Tree 3D

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
<td>Run Bubble Tree 3D Arrangement</td>
<td>Ctrl + Alt + T</td>
<td>Arrange -&gt; Bubble Tree 3D</td>
<td style="text-align: center;"><img src="../ext/docs/CoreArrangementPlugins/src/au/gov/asd/tac/constellation/plugins/arrangements/resources/arrangeInTree3D.png" alt="Bubble Tree 3D Arrangement Icon" /></td>
</tr>
</tbody>
</table>

Similar to the [tree arrangement](tree.md) plugin, the bubble tree 3D arrangement 
arranges all the nodes in tree-like structures with "root" nodes and "leaf" 
nodes placed around their respective root nodes.

Singleton nodes are arranged together in a
grid. Doublets are arranged in a similar fashion. Doublets are pairs of nodes 
only connected to each other. The way nodes are placed on the graph will look 
slightly different each time the arrangement is run.

The root nodes are determined by iteratively removing all the nodes with
only one neighbour until you are left with a set of nodes where each has
more than one neighbour.


Example Bubble Tree 3D Arrangement:

<div style="text-align: center">
    <figure style = "display: inline-block">
        <img height=400 src="../ext/docs/CoreArrangementPlugins/src/au/gov/asd/tac/constellation/plugins/arrangements/resources/beforeArrangement.png" alt="Before Arrangement" />
        <figcaption>Before Bubble Tree Arrangement</figcaption>
    </figure>
    <figure style = "display: inline-block">
        <img height=400 src="../ext/docs/CoreArrangementPlugins/src/au/gov/asd/tac/constellation/plugins/arrangements/resources/bubbleTreeArrangement.png" alt="Example Bubble Tree Arrangement" />
        <figcaption>After Bubble Tree Arrangement</figcaption>
    </figure>
</div>