# Layer by Time

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
<td>Run Layer by Time</td>
<td></td>
<td>Arrange -&gt; Layer by Time</td>
<td style="text-align: center;"><img src="../ext/docs/CoreArrangementPlugins/resources/layerByTime.png" alt="Layer by Time Icon" /></td>
</tr>
</tbody>
</table>

Layer by Time is an analytic using the Z-axis or 3D capability of
Constellation. It uses a transaction attribute of type datetime to split
the data in the graph into a series of time bins. Select a range using
the First Datetime and Last Datetime selection boxes, or use the default
values of the earliest and latest timestamps in the current graph data.

**WARNING: Using the "Layer by Time" feature will add information
(duplicate nodes and transactions) to your graph. To preserve your
original data, make a copy of your graph before running the plugin.**

There are two options for defining the layers: "Nodes as Layers" and
"Transactions as Layers". The default option of "Nodes as Layers" (i.e.
"Transactions as Layers" unticked) creates layers containing both nodes
and transactions, with each layer representing a different time bin. It
also adds guidelines to your graph to help with tracking the same node
across multiple time layers. The "Transactions as Layers" option bins
the data in the same way, but puts transactions on a separate layer
(between their source and destination nodes). Experiment with both
options to see which one works best for you.

<div style="text-align: center">
    <img height=600 src="../ext/docs/CoreArrangementPlugins/resources/layer_by_time_parameters_gui_screenshot.png" alt="Layer by Time Interface" />
</div>
<br />
<br />

Example:

<div style="text-align: center">
    <figure style = "display: inline-block">
        <img height=400 src="../ext/docs/CoreArrangementPlugins/resources/BeforeLayer.png" alt="Before" />
        <figcaption>Before</figcaption>
    </figure>
    <figure style = "display: inline-block">
        <img height=400 src="../ext/docs/CoreArrangementPlugins/resources/AfterLayer.png" alt="After" />
        <figcaption>After</figcaption>
    </figure>
</div>