# Composite Nodes

<br />
<div style="text-align: center">
    <figure style = "display: inline-block">
        <img height=400 src="../ext/docs/CoreInteractiveGraph/src/au/gov/asd/tac/constellation/graph/interaction/resources/BeforeComposite.png" alt="Before Composite" />
        <figcaption>Before Composite</figcaption>
    </figure>
    <figure style = "display: inline-block">
        <img height=400 src="../ext/docs/CoreInteractiveGraph/src/au/gov/asd/tac/constellation/graph/interaction/resources/AfterComposite.png" alt="After Composite" />
        <figcaption>After Composite</figcaption>
    </figure>
</div>
<br />

A composite node is a node that contains 2 or more nodes. 
It can be expaned to reveal the ndoes contained within.


## Composite Selected Nodes
To create a composite node by selection, first select all the nodes you wish 
to be composited, then either click Tools -&gt; Composite Selected Nodes or 
right-click and select "Composite Selected Nodes".

<br />
<div style="text-align: center">
    <figure>
        <img src="../ext/docs/CoreInteractiveGraph/src/au/gov/asd/tac/constellation/graph/interaction/resources/CompositeSelectedNodes.png" alt="Composite Selected Nodes" />
        <figcaption>From Tool Menu</figcaption>
    </figure>
    <br />
    <figure>
        <img src="../ext/docs/CoreInteractiveGraph/src/au/gov/asd/tac/constellation/graph/interaction/resources/CompositeSelectedNodesFromMenu.png" alt="Composite Selected Nodes From Menu" />
        <figcaption>From Right-Clicking</figcaption>
    </figure>
</div>
<br />

## Composite Correlated Nodes
To create a composite node by correlation, simply click Tools -&gt; Composite 
Correlation Nodes. Two nodes are considered correlated if at least one 
transaction connecting them is of type "Correlation". Multiple nodes chained
by correlation are compsited together.

<br />
<div style="text-align: center">
    <figure>
        <img src="../ext/docs/CoreInteractiveGraph/src/au/gov/asd/tac/constellation/graph/interaction/resources/CompositeCorrelatedNodes.png" alt="Composite Correlated Nodes" />
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
        <img src="../ext/docs/CoreInteractiveGraph/src/au/gov/asd/tac/constellation/graph/interaction/resources/ExpandComposite.png" alt="Expand Composite" />
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
        <img src="../ext/docs/CoreInteractiveGraph/src/au/gov/asd/tac/constellation/graph/interaction/resources/ContractComposite.png" alt="Contract Composite" />
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
        <img src="../ext/docs/CoreInteractiveGraph/src/au/gov/asd/tac/constellation/graph/interaction/resources/DestroyComposite.png" alt="Destroy Composite" />
        <figcaption>From Right-Clicking</figcaption>
    </figure>
</div>
<br />

## Destroy All Composites
A tool to "destroy" all composites on the graph.

<br />
<div style="text-align: center">
    <figure>
        <img src="../ext/docs/CoreInteractiveGraph/src/au/gov/asd/tac/constellation/graph/interaction/resources/DestroyAllComposites.png" alt="Destroy All Composites" />
        <figcaption>From Tool Menu</figcaption>
    </figure>
</div>
<br />