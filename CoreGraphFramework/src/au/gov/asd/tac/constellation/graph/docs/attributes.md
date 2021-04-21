## Attributes

Attributes provide the means for the user to associate information with
each node and transaction in a graph. The base application will
automatically create a default set of attributes for the nodes and
transactions. For example, an x, y and z node attributes are
automatically created to support the coordinate position of each node.

Attributes may be created, deleted or modified by the user or by the
plugins in a populated or empty graph.

Each attribute is defined by a name, description, type and a default
value. The set of attribute types include.

<table data-border="1">
<caption>The different types of attributes</caption>
<thead>
<tr class="header">
<th scope="col">Type</th>
<th scope="col">Description</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>boolean</td>
<td>A true or false value.</td>
</tr>
<tr class="even">
<td>color</td>
<td>A color. Some colors have names, other are represented by red/green/blue/alpha values between 0 and 1.</td>
</tr>
<tr class="odd">
<td>date</td>
<td>A date with the representation yyyy-mm-dd.</td>
</tr>
<tr class="even">
<td>datetime</td>
<td>A zoned datetime with the representation yyyy-mm-dd hh:mm:ss Z.</td>
</tr>
<tr class="odd">
<td>float</td>
<td>A floating point number.</td>
</tr>
<tr class="even">
<td>hyperlink</td>
<td>A URL or URI.</td>
</tr>
<tr class="odd">
<td>icon</td>
<td>A small image used for foreground and background pictures on nodes, as well as node decorators.</td>
</tr>
<tr class="even">
<td>integer</td>
<td>An integer number.</td>
</tr>
<tr class="odd">
<td>local_datetime</td>
<td>A datetime with the representation yyyy-mm-dd hh:mm:ss.</td>
</tr>
<tr class="even">
<td>long</td>
<td>A long number.</td>
</tr>
<tr class="odd">
<td>object</td>
<td>A value that doesn't fit any of the other types.</td>
</tr>
<tr class="even">
<td>string</td>
<td>Text.</td>
</tr>
<tr class="odd">
<td>time</td>
<td>A time with representation hh:mm:ss.</td>
</tr>
<tr class="even">
<td>time_zone</td>
<td>A timezone for use with zoned datetimes.</td>
</tr>
</tbody>
</table>

The different types of attributes
