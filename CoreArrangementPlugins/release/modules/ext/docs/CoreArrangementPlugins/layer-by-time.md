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

After running the arrangement, a new graph is created containing the results of Layer by Time.

There are two options for defining the layers: "Nodes as Layers" and
"Transactions as Layers". The default option of "Nodes as Layers" (i.e.
"Transactions as Layers" unticked) creates layers containing both nodes
and transactions, with each layer representing a different time bin. It
also adds guidelines to your graph to help with tracking the same node
across multiple time layers. The "Transactions as Layers" option bins
the data in the same way, but puts transactions on a separate layer
(between their source and destination nodes). Experiment with both
options to see which one works best for you.

**Options:**
- Date-time Attribute: The attribute from the graph's tranactions that will used to order the results
- Date Range: The range of time to include in layering
- Layer date-times by: The method by which to layer the graph into. Choices are "intervals" or "bins"
- Intervals: The number of intervals to layer the graph int. This option is disabled if "bins" is chosen for the "Layer date-times by" paramter
- Unit: The unit of time in which to layer the graph by
- Transactions as layers: Whether to use transactions as layers. Uses nodes if left unticked
- Keep transaction colors: If ticked, transactions will retain their color. If unticked, color will be determined by a gradient across all layers
- Draw transaction guide layers: If ticked indicator lines will connect nodes that appear across mutliple layers
- Arrange result in 2D: If ticked, the results of Layer by Time will be arranged in a compact, 2-dimensional arrangement

**Options (only available if "Arrange results in 2D" is ticked):**
- Limit rows or columns: A fixed number of rows or columns can be chosen
- Number of rows/columns: The desired number of rows or columns to display the results in
- Distance between rows: The distance between rows of layers. Minimum of 0 units
- Distance between columns: The distance between columns of layers. Minimum of 0 units.
- Distance between nodes in layer: The distance between each node inside a layer. Minimum of 0 units.
- Direction to arrange: Options are "Left to Right" and "Top to Bottom". "Left to right" will place newer layers (by default) **to the right** of previous layers, then start a new row if the column limit is reached. "Top to bottom" will place newer layers (by default) **below** previous layers, then start a new column when the row limit is reached.
- Oldest or newest time first: "Oldest" will place layers in chronological order, "Newest" will place layers in reverse-chronological order


<div style="text-align: center">
    <img height=600 src="../ext/docs/CoreArrangementPlugins/resources/layer_by_time_parameters_gui_screenshot.png" alt="Layer by Time Interface" />
</div>
<br />
<br />

**Example:**

<div style="text-align: center">
    <figure style = "display: inline-block">
        <img height=400 src="../ext/docs/CoreArrangementPlugins/resources/BeforeLayer.png" alt="Before" />
        <figcaption>Before</figcaption>
    </figure>
    <figure style = "display: inline-block">
        <img height=400 src="../ext/docs/CoreArrangementPlugins/resources/AfterLayer.png" alt="After" />
        <figcaption>After</figcaption>
    </figure>
    <figure style = "display: inline-block">
        <img height=400 src="../ext/docs/CoreArrangementPlugins/resources/AfterLayer2D.png" alt="After (2D Option)" />
        <figcaption>After (2D Option)</figcaption>
    </figure>
</div>