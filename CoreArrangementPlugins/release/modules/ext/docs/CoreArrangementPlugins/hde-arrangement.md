# High Dimensional Embedding 3D

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
<td>Run HDE 3D Arrangement</td>
<td>Ctrl + Alt + G</td>
<td>Arrange -&gt; HDE 3D</td>
<td style="text-align: center;"><img src="../ext/docs/CoreArrangementPlugins/resources/HDE.png" alt="HDE Icon" /></td>
</tr>
</tbody>
</table>

The HDE 3D arrangement arranges all the "islands" of nodes, by 
using the high dimensional embedding algorithm.

Info about the algorithm can be found in:

D. Harel and Y. Koren, "Graph Drawing by High-Dimensional Embedding", proceedings of Graph Drawing 2002, Volume 2528 of Lecture Notes in Computer Science, pp. 207-219,  Springer Verlag, 2002

Singleton nodes are arranged together in a
grid, similarly for doublets (pairs of nodes only connected to each
other). The way nodes are placed on the graph will look slightly
different each time the arrangement is run.

Example HDE 3D Arrangement:

<div style="text-align: center">
    <figure style = "display: inline-block">
        <img height=400 src="../ext/docs/CoreArrangementPlugins/resources/beforeArrangement.png" alt="Before Arrangement" />
        <figcaption>Before HDE Arrangement</figcaption>
    </figure>
    <figure style = "display: inline-block">
        <img height=400 src="../ext/docs/CoreArrangementPlugins/resources/hdeArrangement.png" alt="Example HDE 3D Arrangement" />
        <figcaption>After HDE Arrangement</figcaption>
    </figure>
</div>