# Hierarchical Clustering

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
<td>Run Hierarchical Cluster</td>
<td></td>
<td>Tools -&gt; Cluster -&gt; Hierarchical</td>
<td style="text-align: center;"><img src="../ext/docs/CoreAlgorithmPlugins/src/au/gov/asd/tac/constellation/plugins/algorithms/resources/hierarchical.png" alt="Hierarchical Clustering Icon" /></td>
</tr>
</tbody>
</table>

Hierarchical clustering, as the name suggests, forms clusters on the graph in 
a hierarchical manner. When the Hierarchical Clustering plugin is executed, a 
Fast Newman algorithm is run which clusters the graph hierarchically by initially 
placing all nodes in their own cluster and then iteratively merging clusters 
according to a weight function until the optimal state is reached.

To run the Hierarchical Clusters algorithnm, open the Hierarchical Clusters 
panel from Tools -> Cluster -> Hierarchical. To apply the changes from the clustering
to the graph click the Cluster button and then Toggle Interactive: Disabled. This
will change the color of the graph elements to match the clusters. 

The arrows on the right side allow for the number of clusters to be modified and the 
<img src="../ext/docs/CoreAlgorithmPlugins/src/au/gov/asd/tac/constellation/plugins/algorithms/resources/shortestpaths.png" width="16" height="16" />
button will indicate the Shortest Path between cluster centres. 

## Constellation Display

Hierarchical Clustering in Constellation makes use of overlay colors. When the
clustering algorithm has been run, each cluster is assigned a unique
color. Node backgrounds and intra-cluster transactions are colored using
the cluster's color, while inter-cluster transactions are colored dark
grey when the Excluded Elements is set to dimmed.

Rather than set the color directly, Hierarchical Clustering in Constellation
creates a new color attribute "Cluster.Hierarchical.Color" and tells 
Constellation to display the graph using the new attribute (via the 
"node\_color\_reference" and "transaction\_color_reference" graph attributes). 
To switch back to the default color attribute, edit the graph attributes via 
the Attribute Editor and click on "Restore Default" followed by Ok.

Before Hierarchical Clustering is run:

<div style="text-align: center">
<img height=500 src="../ext/docs/CoreAlgorithmPlugins/src/au/gov/asd/tac/constellation/plugins/algorithms/resources/clusteringBefore.png" alt="Before Hierarchical Clustering" />
</div>
<br />

After Hierarchical Clustering is run:

<div style="text-align: center">
<img height=550 src="../ext/docs/CoreAlgorithmPlugins/src/au/gov/asd/tac/constellation/plugins/algorithms/resources/hierarchicalAfter.png" alt="After Hierarchical Clustering" />
</div>

