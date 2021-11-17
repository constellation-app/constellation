# Attributes

Attributes provide the means for the user to associate information with
each node and transaction in a graph. The base application will
automatically create a default set of attributes for the nodes and
transactions. e.g. x, y and z node attributes are automatically created
on an analytic graph to support the coordinate position of each node.

Attributes on a graph may be created, deleted or modified by either the
user or by plugins that are run.

Each attribute is defined by a name, description, type and a default
value. The set of attribute types that are commonly used include:

<table class="table table-striped">
<thead>
<tr class="header">
<th><strong>Type</strong></th>
<th><strong>Description</strong></th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>boolean</td>
<td>A true or false value</td>
</tr>
<tr class="even">
<td>boolean_or_null</td>
<td>A boolean value or no value</td>
</tr>
<tr class="odd">
<td>byte</td>
<td>A byte number</td>
</tr>
<tr class="even">
<td>byte_or_null</td>
<td>A byte value or no value</td>
</tr>
<tr class="odd">
<td>color</td>
<td>A colour. Some colours have names, other are represented by red/green/blue/alpha values between 0 and 1</td>
</tr>
<tr class="even">
<td>date</td>
<td>A date with the representation yyyy-mm-dd</td>
</tr>
<tr class="odd">
<td>datetime</td>
<td>A zoned datetime with the representation yyyy-mm-dd hh:mm:ss Z</td>
</tr>
<tr class="even">
<td>double</td>
<td>A double-precision floating point number</td>
</tr>
<tr class="odd">
<td>double_or_null</td>
<td>A double value or no value</td>
</tr>
<tr class="even">
<td>float</td>
<td>A floating point number</td>
</tr>
<tr class="odd">
<td>float_or_null</td>
<td>A float value or no value</td>
</tr>
<tr class="even">
<td>hyperlink</td>
<td>A URL or URI</td>
</tr>
<tr class="odd">
<td>icon</td>
<td>A small image used for foreground and background pictures on nodes, as well as node decorators</td>
</tr>
<tr class="even">
<td>integer</td>
<td>An integer number</td>
</tr>
<tr class="odd">
<td>integer_or_null</td>
<td>An integer value or no value</td>
</tr>
<tr class="even">
<td>local_datetime</td>
<td>A datetime with the representation yyyy-mm-dd hh:mm:ss</td>
</tr>
<tr class="odd">
<td>long</td>
<td>A long number</td>
</tr>
<tr class="even">
<td>long_or_null</td>
<td>A long value or no value</td>
</tr>
<tr class="odd">
<td>object</td>
<td>A value that doesn't fit any of the other types</td>
</tr>
<tr class="even">
<td>short</td>
<td>A short number</td>
</tr>
<tr class="odd">
<td>short_or_null</td>
<td>A short value or no value</td>
</tr>
<tr class="even">
<td>string</td>
<td>Text</td>
</tr>
<tr class="odd">
<td>time</td>
<td>A time with representation hh:mm:ss</td>
</tr>
<tr class="even">
<td>time_zone</td>
<td>A timezone for use with zoned datetimes</td>
</tr>
</tbody>
</table>

There is also a number of other attribute types you will find but have
generally been designed for a specific attribute (usually the type has a
similar name to the attribute it is for):

-   blaze
-   composite\_node\_state
-   connection\_mode
-   draw\_flags
-   graph\_labels\_nodes
-   graph\_labels\_transactions
-   layer\_name
-   line\_style
-   raw
-   transaction\_attribute\_name
-   transaction\_type
-   vertex\_attribute\_name
-   vertex\_type
