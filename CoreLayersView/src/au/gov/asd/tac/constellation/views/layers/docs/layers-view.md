# Layers View

<table data-border="1">
<caption>Layers View Actions</caption>
<colgroup>
<col style="width: 25%" />
<col style="width: 25%" />
<col style="width: 25%" />
<col style="width: 25%" />
</colgroup>
<thead>
<tr class="header">
<th scope="col"><strong>Constellation Action</strong></th>
<th scope="col"><strong>Keyboard Shortcut</strong></th>
<th scope="col"><strong>User Action</strong></th>
<th scope="col"><strong>Menu Icon</strong></th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Open Layers View</td>
<td>Ctrl + Shift + L</td>
<td>Experimental -&gt; Views -&gt; Layers View</td>
<td><div style="text-align: center">
<img src="../resources/layers-view.png" width="16" height="16" />
</div></td>
</tr>
<tr class="even">
<td>Add New Layer</td>
<td>Ctrl + Alt + L</td>
<td>Click "Add New Layer"</td>
<td></td>
</tr>
<tr class="odd">
<td>Deselect All Layers</td>
<td>Ctrl + Alt + D</td>
<td>Click "Deselect All Layers"</td>
<td></td>
</tr>
<tr class="even">
<td>Toggle On/Off Layer x</td>
<td>Ctrl + Alt + x (x is the corresponding layer number)</td>
<td>Tick/Untick Visibility Box of corresponding layer</td>
<td></td>
</tr>
</tbody>
</table>

Layers View Actions

## Introduction

The Layers View holds a collection of Layers. Each Layer can represent a
static set of elements, or a dynamically calculated set of elements
which match a query criteria.

<div style="text-align: center">

![Layers View](resources/layers-view-example.png)

</div>

## Creating and Using Layers

The two main ways to create and use layers are through the Layers View
window and through the use of shortcut keys (see table at the top of the
page for shortcut details). The maximum amount of layers you can create
is 64.

When a single layer is toggled on, that layer will be displayed on the
graph. When multiple manual layers are selected, it will display
everything on layer x, as well as everything on layer y (i.e. the union
of all selected layers).

## Layer Types

![Layers Context Menu](resources/layers-context.png)

There are two main layer types within the Layers View. The
differentiating factor of the two being the way elements are chosen to
be displayed.

-   *Manual Layer* - A manual layer is a static layer only containing
    elements added via right click context menu (see above image).
-   *Query Layer* - A query layer is a dynamic layer that shows the
    elements represented by the specified Vertex and Transaction
    queries. Since a query layer is dynamic, it will get recalculated if
    the value the layer is concerned about changes. e.g. Layer 1 has a
    vertex query described as Label == 'Vertex \#0'. This will show the
    vertex with that label. If you then change Vertex \#0's label to
    something like 'V\#0', the query will note the change and
    recalculate for the whole graph.

## Query Language

A valid query for a vertex or transaction takes the form of
\[attributeName\] \[operator\] \[value\]. Any attribute name can be used
to query the graph and values must be surrounded with single or double
quotes. e.g. The vertex query Label == 'Vertex \#0' would show all graph
elements whose label is Vertex \#0.

### Operators

Operators can be used to query, edit and compare values. This is the
current list of supported query operators:

<table data-border="1">
<caption>Layers View Query Operators</caption>
<thead>
<tr class="header">
<th scope="col"><strong>Operator</strong></th>
<th scope="col"><strong>Query Representation</strong></th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Equals</td>
<td>==</td>
</tr>
<tr class="even">
<td>Not Equals</td>
<td>!=</td>
</tr>
<tr class="odd">
<td>Greater Than</td>
<td>&gt;</td>
</tr>
<tr class="even">
<td>Less Than</td>
<td>&lt;</td>
</tr>
<tr class="odd">
<td>Greater Than Or Equals</td>
<td>&gt;=</td>
</tr>
<tr class="even">
<td>Less Than Or Equals</td>
<td>&lt;=</td>
</tr>
<tr class="odd">
<td>Assign</td>
<td>=</td>
</tr>
<tr class="even">
<td>And</td>
<td>&amp;</td>
</tr>
<tr class="odd">
<td>Or</td>
<td>|</td>
</tr>
<tr class="even">
<td>XOR</td>
<td>^</td>
</tr>
<tr class="odd">
<td>Not</td>
<td>!</td>
</tr>
<tr class="even">
<td>Add</td>
<td>+</td>
</tr>
<tr class="odd">
<td>Subtract</td>
<td>-</td>
</tr>
<tr class="even">
<td>Multiply</td>
<td>*</td>
</tr>
<tr class="odd">
<td>Divide</td>
<td>/</td>
</tr>
<tr class="even">
<td>Modulo</td>
<td>%</td>
</tr>
<tr class="odd">
<td>Contains</td>
<td>contains</td>
</tr>
<tr class="even">
<td>Starts With</td>
<td>startswith</td>
</tr>
<tr class="odd">
<td>Ends With</td>
<td>endswith</td>
</tr>
<tr class="even">
<td>Or</td>
<td>or</td>
</tr>
<tr class="odd">
<td>And</td>
<td>and</td>
</tr>
<tr class="even">
<td>Equals</td>
<td>equals</td>
</tr>
<tr class="odd">
<td>Not Equals</td>
<td>notequals</td>
</tr>
</tbody>
</table>

Layers View Query Operators

## Nested Queries

The query language also allows for nested queries. This makes querying
complex conditions possible. Nested queries are created by surrounding
internal queries with ( and ).

e.g. Label == 'Vertex \#0&lt;Unknown&gt;' || Label == 'Vertex
\#1&lt;Unknown&gt;' shows elements that satisfy either constraint.

e.g. Label == 'Vertex \#0&lt;Unknown&gt;' & nradius == '1.5' shows
elements that satisfy both constraints.
