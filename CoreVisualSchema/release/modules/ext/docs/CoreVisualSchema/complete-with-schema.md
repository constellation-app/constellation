# Complete With Schema

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
<td>Run Complete With Schema</td>
<td>F5</td>
<td>Tools -&gt; Complete With Schema</td>
<td style="text-align: center;"><img src="../ext/docs/CoreVisualSchema/resources/completeWithSchema.png" alt="Complete with Schema Icon" /></td>
</tr>
<tr class="even">
<td>Run Complete with Schema on Selection</td>
<td>Shift + F5</td>
<td>Tools -&gt; Complete With Schema on Selection</td>
<td style="text-align: center;"><img src="../ext/docs/CoreVisualSchema/resources/completeWithSchema.png" alt="Complete with Schema Icon" /></td>
</tr>
</tbody>
</table>

Complete with Schema is a plugin that can be run to update graph attributes to the
match the expected values for the graph's schema. Each graph has a schema which 
defines which attributes are included in that graph. This plugin can be run via 
the Tools menu and is also run by default after using some Data Access Plugins or
running some Analytics. 

More information on the schemas can be found on the 
[Schema](../ext/docs/CoreGraphFramework/schemas.md)
help page. 

## Complete With Schema

When the Complete With Schema plugin is run, attributes on the graph are updated
to match the defined values in that schema. An example of a use case for this plugin 
is when the Type of a Node is changed, running Complete with Schema 
will update the Node's Label, Color and Icon to match the defined attributes for 
that Type in the schema on an Analytic Graph. 

Running Complete With Schema will also update the colors on the graph with the 
current colorblind option if this had been changed.

Example of using the Complete with Schema plugin
<div style="text-align: center">
    <figure style="display: inline-block">
        <img height=400 src="../ext/docs/CoreVisualSchema/resources/analytic_before_schema.png" alt="Before Complete with Schema" />
        <figcaption>Before Complete with Schema</figcaption>
    </figure>
    <figure style="display: inline-block">
        <img height=400 src="../ext/docs/CoreVisualSchema/resources/analytic_after_schema.png" alt="After Complete with Schema" />
        <figcaption>After Complete with Schema</figcaption>
    </figure>
</div>

## Complete With Schema on Selection

Complete With Schema on Selection works the same as Complete With Schema but 
it will only run on the nodes and transactions in the graph that are currently selected. 

Complete With Schema on Selection will also update the selected parts of the graph
with the colors of the current colorblind option. 
