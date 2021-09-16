# Map View

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
<td>Open Map View</td>
<td>Ctrl + Shift + M</td>
<td>Views -&gt; Map View</td>
<td><div style="text-align: center">
<img src="../constellation/CoreMapView/src/au/gov/asd/tac/constellation/views/mapview/docs/resources/map-view.png" width="16" height="16" />
</div></td>
</tr>
</tbody>
</table>

## Introduction

The Map View provides a visualisation of geospatial data in a
Constellation graph as well as features for basic geospatial analysis.

<div style="text-align: center">

<img src="../constellation/CoreMapView/src/au/gov/asd/tac/constellation/views/mapview/docs/resources/MapView.png" alt="Map
View" />

</div>

## Base Maps

Base maps are provided by local or network tile servers, with the
default map being a simple built-in map consisting only of country
borders. The Map View can be extended to source its base map data from a
custom location (refer to the Developer Guide for more information on
how to do this). You can switch between available base maps using the
<img src="../constellation/CoreMapView/src/au/gov/asd/tac/constellation/views/mapview/docs/resources/MapBaseMapsMenu.png" alt="Base Maps
Menu" />
menu.

## Layers

Layers can be rendered on top of the map in order to provide additional
visualisations for analytic purposes. Layers can be switched on and off
using the <img src="../constellation/CoreMapView/src/au/gov/asd/tac/constellation/views/mapview/docs/resources/MapLayersMenu.png" alt="Layers
Menu" />
menu.

## Overlays

Overlays can be rendered on top of the map in order to provide
additional information or features to the Map View. Overlays can be
switched on and off using the <img src="../constellation/CoreMapView/src/au/gov/asd/tac/constellation/views/mapview/docs/resources/MapOverlayMenu.png" alt="Overlay
Menu" />
menu.

## Zooming

You can zoom to markers or custom locations on the map using the <img src="../constellation/CoreMapView/src/au/gov/asd/tac/constellation/views/mapview/docs/resources/MapZoomMenu.png" alt="Zoom
Menu" />
menu.

## Marker Types

The Map View is capable of rendering points, lines, polygons and
multi-polygons to represent geospatial data on a graph. By default, it
will read from the "Geo.Latitude" and "Geo.Longitude" attributes for
point data, and the "Geo.Shape" attribute for line, polygon and
multi-polygon data, where it will expect to find a GeoJSON object
representing the shape as a Feature Collection. The Map View can also
render clusters (calculated using marker centroids). All of these marker
types can be switched on and off using the <img src="../constellation/CoreMapView/src/au/gov/asd/tac/constellation/views/mapview/docs/resources/MapMarkerVisibilityMenu.png" alt="Marker Visibility
Menu" />
menu.

## Marker Customisation

You customise markers with colours and labels using data on the graph.
This can be achieved by selecting options in the <img src="../constellation/CoreMapView/src/au/gov/asd/tac/constellation/views/mapview/docs/resources/MapMarkerColoursMenu.png" alt="Marker Colours
Menu" />
and <img src="../constellation/CoreMapView/src/au/gov/asd/tac/constellation/views/mapview/docs/resources/MapMarkerLabelsMenu.png" alt="Marker Labels
Menu" />
menus.

## Exporting Data

Geospatial data can be exported to a range of open data formats using
the <img src="../constellation/CoreMapView/src/au/gov/asd/tac/constellation/views/mapview/docs/resources/MapExportMenu.png" alt="Export
Menu" />
menu.
