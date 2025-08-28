# K-Truss

## Find highly connected groups of nodes in a graph 

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
<td>Open K-Truss Visibility Panel</td>
<td></td>
<td>Tools -&gt; Cluster -&gt; K-Truss</td>
<td style="text-align: center;"><img src="../ext/docs/CoreAlgorithmPlugins/resources/ktruss.png" alt="K-Truss Icon" /></td>
</tr>
</tbody>
</table>

For some whole number k >= 3, a k-truss is a graph (or part thereof) where 
every transaction lies in at least k-2 triangles. e.g. in a 3-truss every 
transaction lies in at least 1 triangle, while in a 4-truss, every 
transaction lies in at least 2 triangles. There is no upper limit on the 
number of nodes in a k-truss, but it must have at least k nodes, otherwise 
there are not enough triangles. A k-truss captures the idea of a group of 
nodes with a particular level of interconnectivity - the higher the value 
of k is, the greater the interconnection between nodes. Note that a 
k-truss is also a j-truss for all values of j < k.

## In Constellation

The plugin will assign an integer attribute "Cluster.KTruss.Highest" to
each node and transaction. This value will be the largest value of k for
which the node/transaction is in a k-truss. This attribute can be used 
like a cluster attribute, but perhaps more useful is the ability to 
explore the k-trusses in the graph using the K-Truss Visibility panel. 
This panel is opened automatically after running the K-Truss plugin, but 
can be accessed at any time from the menu via Tools -> Cluster -> K-Truss. 
Note that it is disabled for a graph unless the K-Truss plugin has 
previously been run on that graph.

## Using the Visibility Panel

The K-Truss Visibility panel contains a number of features for
visualising k-trusses and the relationships between them. The simplest
and most useful feature is the visibility slider.

### Visibility Slider

The visibility slider ranges over all possible values of k for viewing
k-trusses in the graph. Dragging the slider to a specific value of k
will display only nodes and transactions which are in a k-truss. Nodes
which are not displayed will be either dimmed (default) or hidden. The
slider starts at 3, the lowest possible value of k. There is also an
"all" value which displays the entire graph. The numbers in bold show
where the display will change. For example if 5 is in bold, then there
are 5-trusses in the graph which are not 6-trusses, and hence dragging
the slider from 5 to 6 will change the display. If 5 was not bold, it
would mean that all 5-trusses are also 6-trusses, and hence dragging the
slider from 5 to 6 will not change the display. You should gradually
move the slider to higher values in bold, so as to focus on smaller and
more highly connected parts of the graph.

### Excluded Elements Options

The two radio buttons allow you to dynamically switch between dimming
(default) or hiding the nodes and transactions which would not be
displayed based on the visibility slider's current position. You should
use dimming to get an idea of where k-trusses fit in to the graph as a
whole. Hiding is more useful when you want to only look at the k-trusses
for some value of k.

### Select K-Trusses

This option allows you to select all the nodes and transactions which
are being displayed by the visibility slider. You are then able to use
this selection as you would any other selection. You might find it
useful to open the selection in another graph (Ctrl+U) for closer
analysis of the k-trusses.

### Color Nested K-Trusses

This button allows you to color the k-trusses in your graph with a
different color for each k. This means for example, that if a 4-truss
contains a smaller 5-truss, then the nodes and transactions in the
4-truss but not the 5-truss will be a different color to those in both
the 4-truss and the 5-truss. This button acts as a toggle, allowing you
to return to your original coloring. You should use this feature if you
want to see how more highly connected parts of your graph are nested
inside less highly connected parts.

### Nested K-Trusses Display Panel

Pressing the down arrow beneath the visibility slider on the left will
expand the panel to reveal a display of rectangles arranged in columns
beneath the slider. The columns line up with the numbers on the slider.
The column below the number k has a rectangle for every connected
component of the k-truss in the graph. The height of the rectangles is
proportional to the number of nodes in the connected component they
represent. You can see that as you move to higher values of k, the
number and size of the rectangles decreases. You can use this panel to
visualise how the k-trusses nest inside each other, as well as their
relative sizes. Clicking the up arrow button will hide the panel again.

### Show All Components

This is a toggle button which if set will show connected components in
the "all" column, that is connected components of the entire graph as
well as connected components of the k-trusses. This will rescale all the
rectangles so that their heights are now proportional to the total
number of nodes in the graph, rather than the total number of nodes in
some k-truss. You should only use this option if a large proportion of
the nodes in your graph are in k-trusses. Note that this button is
disabled when the nested trusses display panel is hidden.