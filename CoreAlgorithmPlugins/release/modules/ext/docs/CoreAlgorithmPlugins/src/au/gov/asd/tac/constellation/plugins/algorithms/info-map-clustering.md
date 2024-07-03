# Info Map Clustering

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
<th>Menu Icon</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Run Info Map Cluster</td>
<td></td>
<td>Tools -&gt; Cluster -&gt; Info Map</td>
<td>
<img src="../ext/docs/CoreAlgorithmPlugins/src/au/gov/asd/tac/constellation/plugins/algorithms/resources/infoMap.png" width="16" height="16" />
</td>
</tr>
<tr class="even">
<td>Open Info Map Cluster Options</td>
<td></td>
<td>Tools -&gt; Cluster -&gt; Info Map...</td>
<td>
<img src="../ext/docs/CoreAlgorithmPlugins/src/au/gov/asd/tac/constellation/plugins/algorithms/resources/infoMapOptions.png" width="16" height="16" />
</td>
</tr>
</tbody>
</table>

Info Map is a network clustering algorithm based on the Map Equation. 
The Info Map algorithm clusters groups of nodes into communities 
based on the flow of the network. 

More information about the Map Equation can be found <a href="https://www.mapequation.org/publications.html#Rosvall-Axelsson-Bergstrom-2009-Map-equation">here</a>.

When the Info Map... option is chosen from the Tools -> Cluster menu, an options 
panel opens, allowing for the algorithm to be customised. 

<div style="text-align: center">
<img src="../ext/docs/CoreAlgorithmPlugins/src/au/gov/asd/tac/constellation/plugins/algorithms/resources/infoMapOptionsPanel.png" alt="Info Map Options Pane" />
</div>

## Constellation Display

Info Map Clustering in Constellation makes use of overlay colors. When the
clustering algorithm has been run, each cluster is assigned a unique
color. Node backgrounds and intra-cluster transactions are colored using
the cluster's color, while inter-cluster transactions are colored dark
grey.

Rather than set the color directly, Info Map Clustering in Constellation
creates a new color attribute "Cluster.Infomap.Color" and tells 
Constellation to display the graph using the new attribute (via the 
"node\_color\_reference" and "transaction\_color_reference" graph attributes). 
To switch back to the default color attribute, edit the graph attributes via 
the Attribute Editor and click on "Restore Default" followed by Ok.
 

<div style="text-align: center">
    <figure style="display: inline-block">
        <img height=500 src="../ext/docs/CoreAlgorithmPlugins/src/au/gov/asd/tac/constellation/plugins/algorithms/resources/infoMapBefore.png" alt="Before Info Map Clustering" />
        <figcaption>Before Info Map Clustering</figcaption>
    </figure>
    <figure style="display: inline-block">
        <img height=500 src="../ext/docs/CoreAlgorithmPlugins/src/au/gov/asd/tac/constellation/plugins/algorithms/resources/infoMapAfter.png" alt="After Info Map Clustering" />
        <figcaption>After Info Map Clustering</figcaption>
    </figure>
</div>