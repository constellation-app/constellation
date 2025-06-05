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
<td style="text-align: center;"><img src="../ext/docs/CoreAlgorithmPlugins/resources/hierarchical.png" alt="Hierarchical Clustering Icon" /></td>
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

<br />
<div style="text-align: center">
<img src="../ext/docs/CoreAlgorithmPlugins/resources/clusterPanel.png" width="1000" />
</div>
<br />

### Adjusting Number of Clusters

The arrows on the right side move the marker along the clustering display, 
allowing for the number of clusters to be chosen. 
The optimal clustering button will adjust the number of clusters such that clustering is optimal.

### Shortest Path

The <img src="../ext/docs/CoreAlgorithmPlugins/resources/shortestpaths.png" width="16" height="16" />
button will select the shortest path between the centers of all clusters

### Excluding Elements

Elements that are not part of clustering, such as unnecessary nodes or transactions, can have their visibilty altered.
They can be either shown, dimmed or hidden with the respective radio buttons located on the left of the Hierarchical Clusters panel.
Single nodes can also be excluded by selecting the checkbox labeled "Exclude Single Nodes".

## Constellation Display

Hierarchical Clustering in Constellation makes use of overlay colors. When the
clustering algorithm has been run, each cluster is assigned a unique
color. Node backgrounds and intra-cluster transactions are colored using
the cluster's color, while inter-cluster transactions are colored dark
grey when the Excluded Elements is set to dimmed.

Rather than set the color directly, Hierarchical Clustering in Constellation
creates a new color attribute "Cluster.Hierarchical.Color" and tells 
Constellation to display the graph using the new attribute (via the 
"node\_color\_reference" and "transaction\_color\_reference" graph attributes). 
To switch back to the default color attribute, edit the graph attributes via 
the Attribute Editor and click on "Restore Default" followed by Ok.

Before Hierarchical Clustering is run:

<div style="text-align: center">
<img width=1000 src="../ext/docs/CoreAlgorithmPlugins/resources/clusteringBefore.png" alt="Before Hierarchical Clustering" />
</div>
<br />

After Hierarchical Clustering is run:

<div style="text-align: center">
<img width=1000 src="../ext/docs/CoreAlgorithmPlugins/resources/hierarchicalAfter.png" alt="After Hierarchical Clustering" />
</div>

