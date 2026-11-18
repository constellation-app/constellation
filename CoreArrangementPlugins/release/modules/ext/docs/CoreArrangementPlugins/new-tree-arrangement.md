# New Tree Arrangement

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
<td>New Trees Arranger</td>
<td></td>
<td>Experimental -&gt; Arrangements -&gt; New Trees Arranger</td>
<td style="text-align: center;"><img src="../ext/docs/CoreArrangementPlugins/resources/arrangeInTree.png" alt="Tree Arrangement Icon" /></td>
</tr>
</tbody>
</table>

The "New Trees Arrangement" arranges all the nodes in tree-like structures with
"root" nodes arranged by proximity and then "leaf" nodes placed around
their root node circularly. Singleton nodes are arranged together in a
grid, similarly for doublets (pairs of nodes only connected to each
other). The way nodes are placed on the graph may look slightly
different each time the arrangement is run.

The root nodes are determined by which node has the greatest sum of neighbours and neighbour's neighbours.

**Options:**
- Split into Trees: The graph will be split into multiple subgraphs, each of which is a tree. If unchecked the whole graph will be treated as one big tree
- Dim or Hide Cross-subgraph Transactions: If chosen to split into trees, transactions that connect different trees can be dimmed, hidden or left unchanged
- Color Subgraphs: Each tree wil be assigned a random color, every node in the tree will be assigned that color. Does not require graph to be split into trees
- Distance Between Layers: The distance between the different layers of the tree

**Example New Tree Arrangement:**

<div style="text-align: center">
    <figure style = "display: inline-block">
        <img height=400 src="../ext/docs/CoreArrangementPlugins/resources/BeforeNewTreeArrangement.png" alt="Before Arrangement" />
        <figcaption>Before Tree Arrangement</figcaption>
    </figure>
    <figure style = "display: inline-block">
        <img height=400 src="../ext/docs/CoreArrangementPlugins/resources/NewTreeArrangement.png" alt="Example Tree Arrangement Split into Trees" />
        <figcaption>After Tree Arrangement (Split into Trees)</figcaption>
    </figure>
<figure style = "display: inline-block">
        <img height=400 src="../ext/docs/CoreArrangementPlugins/resources/NewTreeArrangementNoSplit.png" alt="Example Tree Arrangement" />
        <figcaption>After Tree Arrangement</figcaption>
    </figure>
</div>