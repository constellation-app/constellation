# Timeline

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
<td>Open Timeline</td>
<td>Ctrl + Shift + T</td>
<td>Views -&gt; Timeline</td>
<td><div style="text-align: center">
<img src="../ext/docs/CoreTimelineView/src/au/gov/asd/tac/constellation/views/timeline/resources/timeline.png" width="16" height="16" />
</div></td>
</tr>
</tbody>
</table>

If the graph that you are currently analysing contains temporal data on
its transactions, it can be viewed using the Timeline View. The Timeline
will plot the data from the graph relative to its occurrence in time.

<div style="text-align: center">

<img src="../ext/docs/CoreTimelineView/src/au/gov/asd/tac/constellation/views/timeline/resources/TimelineView.png" alt="Timeline
View" />

</div>

## Temporal Clusters

When timeline events overlap at the current zoom level, these events are
grouped into a temporal cluster. These clusters are represented as
rectangular blocks on the timeline. They are colored blue if none of
the events in the cluster are selected and red if all of the events in
the cluster are selected. If only a portion of events in the cluster are
selected, the block will be both red and blue with the proportion of red
matching the ratio of selected / unselected.

If the Timeline is zoomed in, this large temporal cluster will 'break'
into smaller temporal clusters and potentially distinct events.

## Events

A distinct event is represented as (from top to bottom):

-   Node (Represented as a triangle)
-   Transaction (which can be directed, shown as an arrow, or
    undirected, shown only as a line)
-   Node

If any of the elements (nodes or transaction) are selected on the graph,
they will be shown with a red 'glow'. Clicking on any of these elements
will select it on the graph, and if the 'Control' button is pressed
while a selection is performed, it will be added to the currently
selected elements on the graph.

## Excluded Nodes

Excluded nodes are nodes that are not visible in the current range of
the timeline. The excluded nodes drop-down list at the top of the view
provides three options for how excluded nodes should be visualised on
the graph: Show Excluded Nodes, Dim Excluded Nodes, and Hide Excluded
Nodes.

## Selected Only

When the "Show selected only" toggle button is pressed, the timeline to
only show transactions that are selected on the graph.

## Zoom to Selection

When the "Zoom to Selection" button is pressed, the timeline will zoom
and pan to the smallest time period that includes everything that is
selected.

## Node Labels

If the "Show Node Labels" toggle button is pressed and a node attribute
selected (from the resulting drop-down), then labels will be shown next
to nodes where there is data present. This can be useful for quickly
identifying nodes, or showing an interesting attribute.

## Navigating the Timeline View

### Timeline Window

If the middle button or the secondary button on the mouse is pressed and
held above the Timeline, and the mouse dragged, the Timeline Window will
refresh the beginning and end times relative to the movement of the
mouse.

If the mouse wheel is scrolled, the Timeline Window will increase or
decrease the amount of time covered by the Timeline Window relative to
the scroll direction.

Clicking on the Timeline window with the primary mouse button and
dragging will cause a selection rectangle to be created. Any events
under the selection rectangle upon release will be selected on the
graph.

### Overview Panel

If the primary mouse button is clicked and dragged over the point of
view rectangle of the Overview Panel, the time period shown by the
Timeline Window updates to reflect the position of the point of view
rectangle.

If the primary mouse button is clicked and dragged at the edges of the
point of view rectangle, the time period shown by the Timeline Window
updates to reflect the position of the point of view rectangle.

The Overview Panel can be resized by clicking and dragging the white
portion that separates the timeline window and the overview panel (the
arrowhead should change when you are hovering over the white portion).
