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
Real-world networks have a complex topology comprising many elements often 
structured into communities. Revealing these communities helps researchers 
uncover the organizational and functional structure of the system that the network 
represents. The Info Map algorithm clusters groups of nodes into communities 
based on the flow of the network. 

More information about the Map Equation can be found <a href="https://www.mapequation.org/publications.html#Rosvall-Axelsson-Bergstrom-2009-Map-equation">here</a>.

When the Info Map... option is chosen from the Tools -> Cluster menu, an options 
panel opens, allowing for the algorithm to be customised. 
 
<div style="text-align: center">
<img src="../ext/docs/CoreAlgorithmPlugins/src/au/gov/asd/tac/constellation/plugins/algorithms/resources/infoMapOptionsPanel.png" alt="Info Map Options Pane" />
</div>


Before Info Map Clustering is run:

<div style="text-align: center">
<img src="../ext/docs/CoreAlgorithmPlugins/src/au/gov/asd/tac/constellation/plugins/algorithms/resources/clusteringBefore.png" alt="Before Info Map Clustering" />
</div>
<br />

After Info Map Clustering is run:

<div style="text-align: center">
<img src="../ext/docs/CoreAlgorithmPlugins/src/au/gov/asd/tac/constellation/plugins/algorithms/resources/infoMapAfter.png" alt="After Info Map Clustering" />
</div>

