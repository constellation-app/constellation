# Compare Graph

## Compare two graphs and show the differences in a new graph

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
<td>Run Compare Graph</td>
<td></td>
<td>Tools -&gt; Compare Graph</td>
<td style="text-align: center;"><img src="../ext/docs/CoreVisualGraph/resources/compareGraph_1.png" alt="Compare Graph Icon" /></td>
</tr>
</tbody>
</table>

Compare Graph does a comparison of two open graphs and shows the
differences in a new graph. An output window will also displayed with
descriptions of the differences. Possible detected differences are
additions, deletions, and attribute value changes.

<br />
<div style="text-align: center">

<img src="../ext/docs/CoreVisualGraph/resources/CompareGraph.png" alt="Compare Graph
Dialog" />

</div>
<br />

In the new graph, each node and transaction will be colored according
to the change detected. An attribute will also be added called Compare
with value set to whatever change was detected (Added, Removed,
Modified, or Unchanged).

## Parameters

-   *Original Graph* - the graph to use as a starting point for the
    comparison
-   *Compare With Graph* - the graph to compare against the original
    graph
-   *Ignore Node Attributes* - node attributes to ignore for the
    comparison
-   *Ignore Transaction Attributes* - transaction attribute to ignore
    for the comparison
-   *Added Color* - the color to indicate an node/transaction addition
    (default is Green)
-   *Removed Color* - the color to indicate a node/transaction removal
    (default is Red)
-   *Changed Color* - the color to indicate a node/transaction
    attribute value change (default is Yellow)
-   *Unchanged Color* - the color to indicate no change to a
    node/transaction (default is Grey)
