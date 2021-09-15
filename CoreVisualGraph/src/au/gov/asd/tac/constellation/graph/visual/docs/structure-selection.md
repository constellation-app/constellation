# Structure Selection

<table data-border="1">
<caption>Structure Selection Actions</caption>
<thead>
<tr class="header">
<th scope="col">Constellation Action</th>
<th scope="col">Keyboard Shortcut</th>
<th scope="col">User Action</th>
<th style="text-align: center;" scope="col">Menu Icon</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Run Select Singletons</td>
<td></td>
<td>Selection -&gt; Select Singletons</td>
<td style="text-align: center;"><img src="../plugins/select/structure/resources/singleton.png" alt="Select Singletons Icon" /></td>
</tr>
<tr class="even">
<td>Run Select Pendants</td>
<td></td>
<td>Selection -&gt; Select Pendants</td>
<td style="text-align: center;"><img src="../plugins/select/structure/resources/pendant.png" alt="Select Pendants Icon" /></td>
</tr>
<tr class="odd">
<td>Run Select Loops</td>
<td></td>
<td>Selection -&gt; Select Loops</td>
<td style="text-align: center;"><img src="../plugins/select/structure/resources/loop.png" alt="Select Loops Icon" /></td>
</tr>
<tr class="even">
<td>Run Select Sources</td>
<td></td>
<td>Selection -&gt; Select Sources</td>
<td style="text-align: center;"><img src="../plugins/select/structure/resources/source.png" alt="Select Sources Icon" /></td>
</tr>
<tr class="odd">
<td>Run Select Sinks</td>
<td></td>
<td>Selection -&gt; Select Sinks</td>
<td style="text-align: center;"><img src="../plugins/select/structure/resources/sink.png" alt="Select Sinks Icon" /></td>
</tr>
<tr class="even">
<td>Run Select Backbone</td>
<td></td>
<td>Selection -&gt; Select Backbone</td>
<td style="text-align: center;"><img src="../plugins/select/structure/resources/backbone.png" alt="Select Backbone Icon" /></td>
</tr>
</tbody>
</table>

Structure Selection Actions

## Select Singletons

Select Singletons adds all the nodes on your graph with no connected
transactions to the current selection.

After Select Singletons is run (nothing previously selected):

<div style="text-align: center">

![Graph after Select Singletons is
run](resources/SelectSingletonsAfter.png)

</div>

## Select Pendants

Select Pendants adds all the nodes on your graph with only one neighbour
to the current selection.

After Select Pendants is run (nothing previously selected):

<div style="text-align: center">

![Graph after Select Pendants is run](resources/SelectPendantsAfter.png)

</div>

## Select Loops

Select Loops adds all the loops on your graph to the current selection.

After Select Loops is run (nothing previously selected):

<div style="text-align: center">

![Graph after Select Loops is run](resources/SelectLoopsAfter.png)

</div>

## Select Sources

Select Sources adds all the nodes on your graph with only outgoing
transactions to the current selection.

After Select Sources is run (nothing previously selected):

<div style="text-align: center">

![Graph after Select Sources is run](resources/SelectSourcesAfter.png)

</div>

## Select Sinks

Select Sinks adds all the nodes on your graph with only incoming
transactions to the current selection.

After Select Sinks is run (nothing previously selected):

<div style="text-align: center">

![Graph after Select Sinks is run](resources/SelectSinksAfter.png)

</div>

## Select Backbone

Select Backbone adds all the nodes on your graph with more than one
neighbour (excluding loops) and their adjoining transactions to the
current selection..

After Select Backbone is run (nothing previously selected):

<div style="text-align: center">

![Graph after Select Backbone is run](resources/SelectBackboneAfter.png)

</div>
