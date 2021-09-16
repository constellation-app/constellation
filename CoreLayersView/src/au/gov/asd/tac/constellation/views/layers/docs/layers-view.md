# Layers View

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
<td>Open Layers View</td>
<td>Ctrl + Shift + L</td>
<td>Experimental -&gt; Views -&gt; Layers View</td>
<td><div style="text-align: center">
<img src="../constellation/CoreLayersView/src/au/gov/asd/tac/constellation/views/layers/docs/resources/layers-view.png" width="16" height="16" />
</div></td>
</tr>
<tr class="even">
<td>Add New Layer</td>
<td>Ctrl + Alt + L</td>
<td>Click "Add New Layer"</td>
<td>N/A</td>
</tr>
<tr class="odd">
<td>Deselect All Layers</td>
<td>Ctrl + Alt + D</td>
<td>Click "Deselect All Layers"</td>
<td>N/A</td>
</tr>
<tr class="even">
<td>Toggle On/Off Layer</td>
<td>Ctrl + Alt + x (x is layer number)</td>
<td>Toggle Visibility Checkbox</td>
<td>N/A</td>
</tr>
</tbody>
</table>

The Layers View holds a collection of Layers. Each Layer can represent a
static set of elements, or a dynamically calculated set of elements
which match a query criteria. The query structure is based on the
expressions framework.  
Each layer has a visibility checkbox which determines if that layer is
toggled. All elements are on the base layer 0. When one or more check
boxes are selected, only the elements that are members of those layers
are visibly displayed. All other elements will have their "visibility"
attribute value toggled to 0.0 (invisible).  
Arrangements on graphs with a visual schema will be ran on the base
layer 0.

<img src../constellation/CoreLayersView/src/au/gov/asd/tac/constellation/views/layers/docs/resources/layers-view-example.png" alt="Fig.1 - Layers view
interface" />
  

## Creating and Using Layers

The two main ways to create and use layers are through the Layers View
window and through the use of shortcut keys (see table at the top of the
page for shortcut details). The maximum amount of layers you can create
is 64.

When a single layer is toggled on, that layer will be displayed on the
graph. When multiple manual layers are selected, it will display
everything on layer x, as well as everything on layer y (i.e. the union
of all selected layers). Combining both query and manual layers will
produce undefined results and should not be used when accuracy of
results is needed.

## Layer Types

<img src="../constellation/CoreLayersView/src/au/gov/asd/tac/constellation/views/layers/docs/resources/layers-context.png" alt="Fig.2 - Right-click context menu used for adding elements to a manual
layer." />

  

There are two main layer types within the Layers View. The
differentiating factor of the two being the way elements are chosen to
be displayed.

### Manual Layer

A manual layer is a static layer only containing elements added via
right click context menu (see above image) or manually set layer_mask
Attribute.

### Query Layer

A query layer is a dynamic layer that shows the elements represented by
the specified Vertex and Transaction queries. Query layers are
dynamically recalculated upon changes to those attributes being queried.
This is to ensure the accuracy of the shown layer. e.g. Layer 1 has a
vertex query described as Label == 'Vertex #0'. This will show the
vertex with that label. If you then change Vertex #0's label to
something like 'V#0', the query will note the change and recalculate for
the whole graph.

NOTE: Layers View does not allow for the use of the assignment operator
= or keyword assign.

Click
[here](../constellation/CoreGraphFramework/src/au/gov/asd/tac/constellation/graph/docs/expressions-framework.md)
for more information on the structure of the expression framework
