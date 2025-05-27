# Composite Nodes

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
<td>Run Composite Selected Nodes</td>
<td></td>
<td>Tools -&gt; Composite Selected Nodes</td>
<td style="text-align: center;"><img src="../ext/docs/CoreInteractiveGraph/resources/compositeSelectedNodesIcon.png" alt="Composite Selected Nodes Icon" /></td>
</tr>
<tr class="even">
<td>Run Composite Correlated Nodes</td>
<td></td>
<td>Tools -&gt; Composite Correlated Nodes</td>
<td style="text-align: center;"><img src="../ext/docs/CoreInteractiveGraph/resources/compositeCorrelatedNodesIcon.png" alt="Composite Correlated Nodes Icon" /></td>
</tr>
<tr class="odd">
<td>Run Destroy All Composites</td>
<td></td>
<td>Tools -&gt; Destroy All Composites</td>
<td style="text-align: center;"><img src="../ext/docs/CoreInteractiveGraph/resources/destroyCompositeNodes.png" alt="Destroy Composite Nodes Icon" /></td>
</tr>
</tbody>
</table>

<br />
<div style="text-align: center">
    <figure style = "display: inline-block">
        <img height=400 src="../ext/docs/CoreInteractiveGraph/resources/BeforeComposite.png" alt="Before Composite" />
        <figcaption>Before Composite</figcaption>
    </figure>
    <figure style = "display: inline-block">
        <img height=400 src="../ext/docs/CoreInteractiveGraph/resources/AfterComposite.png" alt="After Composite" />
        <figcaption>After Composite</figcaption>
    </figure>
</div>
<br />

A composite node is a node that contains 2 or more nodes. 
It can be expanded to reveal the nodes contained within.


## Composite Selected Nodes
To create a composite node by selection, first select all the nodes you wish 
to be composited, then either click Tools -&gt; Composite Selected Nodes or 
right-click and select "Composite Selected Nodes".

<br />
<div style="text-align: center">
    <figure>
        <img src="../ext/docs/CoreInteractiveGraph/resources/CompositeSelectedNodes.png" alt="Composite Selected Nodes" />
        <figcaption>From Tool Menu</figcaption>
    </figure>
    <br />
    <figure>
        <img src="../ext/docs/CoreInteractiveGraph/resources/CompositeSelectedNodesFromMenu.png" alt="Composite Selected Nodes From Menu" />
        <figcaption>From Right-Clicking</figcaption>
    </figure>
</div>
<br />

## Composite Correlated Nodes
To create a composite node by correlation, simply click Tools -&gt; Composite 
Correlation Nodes. Two nodes are considered correlated if at least one 
transaction connecting them is of type "Correlation". Multiple nodes chained
by correlation are composited together.

<br />
<div style="text-align: center">
    <figure>
        <img src="../ext/docs/CoreInteractiveGraph/resources/CompositeCorrelatedNodes.png" alt="Composite Correlated Nodes" />
        <figcaption>From Tool Menu</figcaption>
    </figure>
</div>
<br />

## Expand Composite
A composite node can be "expanded", revealing the nodes contained within.
This can be done by right-clicking and choosing "Expand Composite".

<br />
<div style="text-align: center">
    <figure>
        <img src="../ext/docs/CoreInteractiveGraph/resources/ExpandComposite.png" alt="Expand Composite" />
        <figcaption>From Right-Clicking</figcaption>
    </figure>
</div>
<br />

## Contract Composite
Right-clicking any one of the expanded nodes and choosing "Contract Composite"
will contract the expanded nodes back into the single composite node.

<br />
<div style="text-align: center">
    <figure>
        <img src="../ext/docs/CoreInteractiveGraph/resources/ContractComposite.png" alt="Contract Composite" />
        <figcaption>From Right-Clicking</figcaption>
    </figure>
</div>
<br />

## Destroy Composite
A composite node can be "destroyed", returning the contained nodes to the graph.
This can be done by right-clicking and choosing "Destroy Composite".

<br />
<div style="text-align: center">
    <figure>
        <img src="../ext/docs/CoreInteractiveGraph/resources/DestroyComposite.png" alt="Destroy Composite" />
        <figcaption>From Right-Clicking</figcaption>
    </figure>
</div>
<br />

## Destroy All Composites
A tool to "destroy" all composites on the graph.

<br />
<div style="text-align: center">
    <figure>
        <img src="../ext/docs/CoreInteractiveGraph/resources/DestroyAllComposites.png" alt="Destroy All Composites" />
        <figcaption>From Tool Menu</figcaption>
    </figure>
</div>
<br />