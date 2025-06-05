# Structure Selection

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
<td>Run Select Singletons</td>
<td></td>
<td>Selection -&gt; Select Singletons</td>
<td style="text-align: center;"><img src="../ext/docs/CoreVisualGraph/resources/singleton.png" alt="Select Singletons Icon" /></td>
</tr>
<tr class="even">
<td>Run Select Pendants</td>
<td></td>
<td>Selection -&gt; Select Pendants</td>
<td style="text-align: center;"><img src="../ext/docs/CoreVisualGraph/resources/pendant.png" alt="Select Pendants Icon" /></td>
</tr>
<tr class="odd">
<td>Run Select Loops</td>
<td></td>
<td>Selection -&gt; Select Loops</td>
<td style="text-align: center;"><img src="../ext/docs/CoreVisualGraph/resources/loop.png" alt="Select Loops Icon" /></td>
</tr>
<tr class="even">
<td>Run Select Sources</td>
<td></td>
<td>Selection -&gt; Select Sources</td>
<td style="text-align: center;"><img src="../ext/docs/CoreVisualGraph/resources/source.png" alt="Select Sources Icon" /></td>
</tr>
<tr class="odd">
<td>Run Select Sinks</td>
<td></td>
<td>Selection -&gt; Select Sinks</td>
<td style="text-align: center;"><img src="../ext/docs/CoreVisualGraph/resources/sink.png" alt="Select Sinks Icon" /></td>
</tr>
<tr class="even">
<td>Run Select Backbone</td>
<td></td>
<td>Selection -&gt; Select Backbone</td>
<td style="text-align: center;"><img src="../ext/docs/CoreVisualGraph/resources/backbone.png" alt="Select Backbone Icon" /></td>
</tr>
</tbody>
</table>

## Select Singletons

Select Singletons adds all the nodes on your graph with no connected
transactions to the current selection.

After Select Singletons is run (nothing previously selected):

<div style="text-align: center">

<img src="../ext/docs/CoreVisualGraph/resources/SelectSingletonsAfter.png" alt="Graph after Select Singletons is
run" />

</div>

## Select Pendants

Select Pendants adds all the nodes on your graph with only one neighbour
to the current selection.

After Select Pendants is run (nothing previously selected):

<div style="text-align: center">

<img src="../ext/docs/CoreVisualGraph/resources/SelectPendantsAfter.png" alt="Graph after Select Pendants is
run" />

</div>

## Select Loops

Select Loops adds all the loops on your graph to the current selection.

After Select Loops is run (nothing previously selected):

<div style="text-align: center">

<img src="../ext/docs/CoreVisualGraph/resources/SelectLoopsAfter.png" alt="Graph after Select Loops is
run" />

</div>

## Select Sources

Select Sources adds all the nodes on your graph with only outgoing
transactions to the current selection.

After Select Sources is run (nothing previously selected):

<div style="text-align: center">

<img src="../ext/docs/CoreVisualGraph/resources/SelectSourcesAfter.png" alt="Graph after Select Sources is
run" />

</div>

## Select Sinks

Select Sinks adds all the nodes on your graph with only incoming
transactions to the current selection.

After Select Sinks is run (nothing previously selected):

<div style="text-align: center">

<img src="../ext/docs/CoreVisualGraph/resources/SelectSinksAfter.png" alt="Graph after Select Sinks is
run" />

</div>

## Select Backbone

Select Backbone adds all the nodes on your graph with more than one
neighbour (excluding loops) and their adjoining transactions to the
current selection..

After Select Backbone is run (nothing previously selected):

<div style="text-align: center">

<img src="../ext/docs/CoreVisualGraph/resources/SelectBackboneAfter.png" alt="Graph after Select Backbone is
run" />

</div>
