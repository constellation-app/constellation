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
<th>Menu Icon</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Run Complete With Schema</td>
<td>F5</td>
<td>Tools -&gt; Complete With Schema</td>
<td>
<img src="../ext/docs/CoreVisualSchema/src/au/gov/asd/tac/constellation/graph/schema/visual/resources/completeWithSchema.png" width="16" height="16" />
</td>
</tr>
</tbody>
</table>

Complete with Schema is a plugin that can be run to update graph attributes to the
match the expected values for the graph's schema. Each graph has a schema which 
defines which attributes are included in that graph. This plugin can be run via 
the Tools menu and is also run by default after using some Data Access Plugins or
running some Analytics. 

## Schemas

### Visual Schema

The base level schema in Constellation is the Visual Schema which only includes 
visual attributes such as Identifier, Label, Color and the coordinates of the 
object on the graph. This schema is used when a Visual Graph <img src="../ext/docs/CoreVisualSchema/src/au/gov/asd/tac/constellation/graph/schema/visual/resources/visual_graph.png" alt="Visual Graph" />
is created.

Example of a Visual Graph 
<div style="text-align: center">
<img height=400 src="../ext/docs/CoreVisualSchema/src/au/gov/asd/tac/constellation/graph/schema/visual/resources/visual_graph_example.png" alt="Visual Graph Example" />
</div>

<br />

### Analytic Schema

The other commonly used schema in Constellation is the Analytic Schema. This schema 
adds attributes such as Type, Source and Raw. This schema is used when a Analytic
Graph <img src="../ext/docs/CoreVisualSchema/src/au/gov/asd/tac/constellation/graph/schema/visual/resources/analytic_graph.png" alt="Analytic Graph" /> 
is created.

Example of an Analytic Graph 
<div style="text-align: center">
<img height=400 src="../ext/docs/CoreVisualSchema/src/au/gov/asd/tac/constellation/graph/schema/visual/resources/analytic_graph_example.png" alt="Analytic Graph Example" />
</div>
<br />

### Other Attributes

Other attributes can be added to a schema through the Attribute Editor. These can 
include Constellation defined attributes or custom attributes that can be user created.
<br />

More information on the schemas can be found on the Schema help page under 
Graph -> Graph Overview -> Schemas.

## Complete With Schema

When the Complete With Schema plugin is run, attributes on the graph are updated
to match the defined values in that schema. An example of a use case for this plugin 
is when the Type of a Node is changed, running Complete with Schema 
will update the Node's Label, Color and Icon to match the defined attributes for 
that Type in the schema on an Analytic Graph. 

Example of using the Complete with Schema plugin
<div style="text-align: center">
    <figure style="display: inline-block">
        <img height=400 src="../ext/docs/CoreVisualSchema/src/au/gov/asd/tac/constellation/graph/schema/visual/resources/analytic_before_schema.png" alt="Before Complete with Schema" />
        <figcaption>Before Complete with Schema</figcaption>
    </figure>
    <figure style="display: inline-block">
        <img height=400 src="../ext/docs/CoreVisualSchema/src/au/gov/asd/tac/constellation/graph/schema/visual/resources/analytic_after_schema.png" alt="After Complete with Schema" />
        <figcaption>After Complete with Schema</figcaption>
    </figure>
</div>