# Uncollide

<table class="table table-striped">
<thead>
<tr class="header">
<th>Constellation Action</th>
<th>User Action</th>
<th style="text-align: center;">Menu Icon</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Run Uncollide</td>
<td>Arrange -&gt; Uncollide</td>
<td style="text-align: center;"><img src="../ext/docs/CoreArrangementPlugins/src/au/gov/asd/tac/constellation/plugins/arrangements/resources/uncollide.png" alt="Uncollide Icon" /></td>
</tr>

<tr class="even">
<td>Run Uncollide 3D</td>
<td>Arrange -&gt; Uncollide 3D</td>
<td style="text-align: center;"><img src="../ext/docs/CoreArrangementPlugins/src/au/gov/asd/tac/constellation/plugins/arrangements/resources/uncollide3D.png" alt="Uncollide 3D Icon" /></td>
</tr>
</tbody>
</table>


Selected nodes are postioned such that they don't "collide" with one another.
Nodes are considered "colliding" if they overlap with one another.
If no nodes are selected, then all nodes on the graph will be affected.

Using uncollide will expand the graph until none of the selected nodes are colliding.
If none of the nodes are colliding the begin with, nothing will happen.

Uncollide will only calculate with respect to the X and Y axis, whereas Uncollide 
3D accounts for all three axis.

<div style="text-align: center">
    <figure style = "display: inline-block">
        <img height=400 src="../ext/docs/CoreArrangementPlugins/src/au/gov/asd/tac/constellation/plugins/arrangements/resources/beforeUncollide.png" alt="Before Uncollide" />
        <figcaption>Before Arrangement</figcaption>
    </figure>
    <figure style = "display: inline-block">
        <img height=400 src="../ext/docs/CoreArrangementPlugins/src/au/gov/asd/tac/constellation/plugins/arrangements/resources/afterUncollide.png" alt="After Uncollide" />
        <figcaption>Example Uncollide</figcaption>
    </figure>
    <figure style = "display: inline-block">
        <img height=400 src="../ext/docs/CoreArrangementPlugins/src/au/gov/asd/tac/constellation/plugins/arrangements/resources/afterUncollide3D.png" alt="After Uncollide 3D" />
        <figcaption>Example Uncollide 3D</figcaption>
    </figure>
</div>

