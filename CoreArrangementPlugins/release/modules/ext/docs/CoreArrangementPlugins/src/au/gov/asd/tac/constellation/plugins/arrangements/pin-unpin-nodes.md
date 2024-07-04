# Pin and Unpin Nodes

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
<td>Run Pin Vertex Positions</td>
<td></td>
<td>Arrange -&gt; Pin Vertex Positions</td>
<td style="text-align: center;"><img src="../ext/docs/CoreArrangementPlugins/src/au/gov/asd/tac/constellation/plugins/arrangements/resources/pin.png" alt="Pin Icon" /></td>
</tr>
<tr class="even">
<td>Run Unpin Vertex Positions</td>
<td></td>
<td>Arrange -&gt; Unpin Vertex Positions</td>
<td style="text-align: center;"><img src="../ext/docs/CoreArrangementPlugins/src/au/gov/asd/tac/constellation/plugins/arrangements/resources/unpin.png" alt="Unpin Icon" /></td>
</tr>
</tbody>
</table>
<br />

Arrangements normally work by affecting all selected nodes or, if no nodes are 
selected, all the nodes on the current graph. Should a user want particular nodes 
to not be effected by an arrangement plugin, they can "pin" those nodes.

## Pin
Selected nodes are pinned, meaning they will be ignored (even if selected) when 
an arrangement plugin is run.

## Unpin
Selected nodes are unpinned, setting their "pinned" attribute to false.

<br />
<div style="text-align: center">
    <figure style = "display: inline-block">
        <img height=400 src="../ext/docs/CoreArrangementPlugins/src/au/gov/asd/tac/constellation/plugins/arrangements/resources/unpinnedNode.png" alt="Unpinned" />
        <figcaption>Unpinned Node</figcaption>
    </figure>
    <figure style = "display: inline-block">
        <img height=400 src="../ext/docs/CoreArrangementPlugins/src/au/gov/asd/tac/constellation/plugins/arrangements/resources/pinnedNode.png" alt="Pinned" />
        <figcaption>Pinned Node</figcaption>
    </figure>
</div>

