# Induced Subgraph

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
<td>Run Select Half Hop Induced Subgraph</td>
<td></td>
<td>Selection -&gt; Select Half Hop Induced Subgraph</td>
<td style="text-align: center;"><img src="../ext/docs/CoreVisualGraph/src/au/gov/asd/tac/constellation/graph/visual/resources/half_hop_induced_subgraph.png" alt="Select Half Hop Induced Subgraph Icon" /></td>
</tr>
<tr class="even">
<td>Run Select One Hop Induced Subgraph</td>
<td></td>
<td>Selection -&gt; Select One Hop Induced Subgraph</td>
<td style="text-align: center;"><img src="../ext/docs/CoreVisualGraph/src/au/gov/asd/tac/constellation/graph/visual/resources/one_hop_induced_subgraph.png" alt="Select One Hop Induced Subgraph Icon" /></td>
</tr>
</tbody>
</table>

## Half Hop Induced Subgraph

Half Hop Induced Subgraph will add all the transactions whose source and
destination nodes are already selected to the current selection.

## One Hop Induced Subgraph

One Hop Induced Subgraph will add to the current selection everything
the Half Hop Induced Subgraph will add as well as any nodes that have at
least two selected neighbours and the transactions connecting the node
and those neighbours.

Before Any Induced Subgraph Selection is run:

<div style="text-align: center">

<img src="../ext/docs/CoreVisualGraph/src/au/gov/asd/tac/constellation/graph/visual/resources/SelectInducedSubgraphBefore.png" alt="Graph before Induced Subgraph Selection is
run" />

</div>

After Select Half Hop Induced Subgraph is run:

<div style="text-align: center">

<img src="../ext/docs/CoreVisualGraph/src/au/gov/asd/tac/constellation/graph/visual/resources/SelectHalfHopInducedSubgraphAfter.png" alt="Graph after Select Half Hop Induced Subgraph is
run" />

</div>

After Select One Hop Induced Subgraph is run:

<div style="text-align: center">

<img src="../ext/docs/CoreVisualGraph/src/au/gov/asd/tac/constellation/graph/visual/resources/SelectOneHopInducedSubgraphAfter.png" alt="Graph after Select One Hop Induced Subgraph is
run" />

</div>
