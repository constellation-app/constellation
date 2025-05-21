# Paths Layers

The paths layers in the Map View will connect markers together to
indicate paths. Paths are rendered using colored lines, with a lighter
color indicating the beginning of the path and a darker color
representing the end. Lines are offset slightly so that lines in
opposite directions are not drawn on top of each other. The thickness of
a line can be used to represent a weight.

## Entity Paths

Entity paths represent how nodes on the graph travel between locations.
These transactions can be of any type (not just Location). Paths are
weighted by the number of times the node travels between the two
endpoint locations.

## Location Paths

Location paths represent paths between location nodes that are directly
connected. These transactions can be of any type (not just Location) but
must be directed. Paths are weighted by the number of transactions
between the two endpoint locations. If the maximum number of
transactions in a single direction between locations is greater than the
maximum line width, the widths will be scaled to the maximum line width.

<div style="text-align: center">

<img src="../ext/docs/CoreMapView/resources/mapview-layers-paths.png" alt="Location Paths
Layer" />

</div>
