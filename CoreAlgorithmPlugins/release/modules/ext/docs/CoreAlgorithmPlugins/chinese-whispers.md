# Chinese Whispers

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
<td>Run Chinese Whispers</td>
<td></td>
<td>Tools -&gt; Cluster -&gt; Chinese Whispers</td>
<td style="text-align: center;"><img src="../ext/docs/CoreAlgorithmPlugins/resources/chineseWhispers.png" alt="Chinese Whispers Icon" /></td>
</tr>
</tbody>
</table>

Chinese Whispers is a clustering algorithm which forms clusters around the 
strongest classes in the graph.

The algorithm works as follows:

1. Firstly it assigns all nodes in the graph their own class
2. It then iterates through each node in a random order. For each node, it 
changes its class to the one which has the largest sum of edge weights 
connected to that node. If there is a tie for the largest, it randomly chooses
one of the largest.
3. Step 2 is repeated until clusters stabilise.

Because of the random nature of the algorithm, multiple runs of the algorithm 
on the same graph can produce different clusters (depending on the order nodes 
are iterated through, and which classes are selected in the case of ties).

## Constellation Display

Chinese Whispers in Constellation makes use of overlay colors. When the
clustering algorithm has been run, each cluster is assigned a unique
color. Node backgrounds and intra-cluster transactions are colored using
the cluster's color, while inter-cluster transactions are colored dark
grey.

Rather than set the color directly, Chinese Whispers in Constellation
creates a new color attribute "Cluster.ChineseWhispers.Color" and tells 
Constellation to display the graph using the new attribute (via the 
"node\_color\_reference" and "transaction\_color_reference" graph attributes). 
To switch back to the default color attribute, edit the graph attributes via 
the Attribute Editor and click on "Restore Default" followed by Ok.

Before Chinese Whispers is run:
<div style="text-align: center">
<img src="../ext/docs/CoreAlgorithmPlugins/resources/ChineseWhispersBefore.png" alt="Graph before Chinese Whispers clustering applied" />
</div>

After Chinese Whispers is run:
<div style="text-align: center">
<img src="../ext/docs/CoreAlgorithmPlugins/resources/ChineseWhispersAfter.png" alt="Graph after Chinese Whispers clustering applied" />
</div>
<br />

When the clustering algorithm runs, no arrangement is done, so the graph
can look confusing.

The clustering algorithms add an integer attribute "Cluster.ChineseWhispers" 
to the nodes. This attribute has a unique per-cluster value to indicate
which cluster a node belongs to, which other algorithms can later use 
(e.g. [Arrange by Node Attribute](../ext/docs/CoreArrangementPlugins/src/au/gov/asd/tac/constellation/plugins/arrangements/node-attribute.md)).
