# Export From Constellation

<table class="table table-striped">
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
<td>Export to PNG</td>
<td></td>
<td>File -&gt; Export -&gt; To Screenshot Image...</td>
<td style="text-align: center;"><img src="../ext/docs/CoreImportExportPlugins/src/au/gov/asd/tac/constellation/plugins/importexport/resources/exportToImage.png" alt="Export to Screenshot Image Icon" /></td>
</tr>
<tr class="even">
<td>Export to SVG</td>
<td></td>
<td>File -&gt; Export -&gt; To SVG...</td>
<td style="text-align: center;"><img src="../ext/docs/CoreImportExportPlugins/src/au/gov/asd/tac/constellation/plugins/importexport/resources/exportToSVG.png" alt="Export to SVG Image Icon" /></td>
</tr>
<tr class="odd">
<td>Export to JSON</td>
<td></td>
<td>File -&gt; Export -&gt; To JSON...</td>
<td style="text-align: center;"><img src="../ext/docs/CoreImportExportPlugins/src/au/gov/asd/tac/constellation/plugins/importexport/resources/exportToJSON.png" alt="Export to JSON Icon" /></td>
</tr>
<tr class="even">
<td>Export to GeoJSON</td>
<td></td>
<td>File -&gt; Export -&gt; To GeoJSON...</td>
<td style="text-align: center;"><img src="../ext/docs/CoreImportExportPlugins/src/au/gov/asd/tac/constellation/plugins/importexport/resources/exportToGeoJSON.png" alt="Export to GeoJSON Icon" /></td>
</tr>
<tr class="odd">
<td>Export to GeoPackage</td>
<td></td>
<td>File -&gt; Export -&gt; To GeoPackage...</td>
<td style="text-align: center;"><img src="../ext/docs/CoreImportExportPlugins/src/au/gov/asd/tac/constellation/plugins/importexport/resources/exportToGeoPackage.png" alt="Export to GeoPackage Icon" /></td>
</tr>
<tr class="even">
<td>Export to Shapefile</td>
<td></td>
<td>File -&gt; Export -&gt; To Shapefile...</td>
<td style="text-align: center;"><img src="../ext/docs/CoreImportExportPlugins/src/au/gov/asd/tac/constellation/plugins/importexport/resources/exportToShapeFile.png" alt="Export to Shapefile Icon" /></td>
</tr>
<tr class="odd">
<td>Export to KML</td>
<td></td>
<td>File -&gt; Export -&gt; To KML...</td>
<td style="text-align: center;"><img src="../ext/docs/CoreImportExportPlugins/src/au/gov/asd/tac/constellation/plugins/importexport/resources/exportToKML.png" alt="Export to KML Icon" /></td>
</tr>
</tbody>
</table>

Constellation provides a number of options for exporting a graph. Some
of these will present dialog boxes and details on those are further down
this page:

-   *Screenshot Image* - Export a screenshot of the graph window as a
    .png
-   *SVG Image* - Export a screenshot of the graph window as a
    .svg
-   *JSON* - Export a JSON representation of the graph (described in
    detail
    [here](../ext/docs/CoreGraphFramework/src/au/gov/asd/tac/constellation/graph/constellation-file-format.md))
-   *GeoJSON* - Export a GeoJSON representation of the graph
-   *GeoPackage* - Export the graph as a GeoPackage file
-   *Shapefile* - Export the graph as a Shapefile file
-   *KML* - Export the graph as a KML file

## Export to SVG

### Parameters

-   *Output File* - the name and location of the output file
-   *Graph Title* - the title of the graph
-   *Background Color* - the color of the graph background
-   *Selected Nodes Only* - only export the selected nodes
-   *Show Connections* - include connections in the export
-   *Show Node Labels* - include node labels in the export
-   *Show Connection Labels* - include connections labels in the export
-   *Perspective* - set the camera perspective to export the graph from

## Export to GeoJSON

### Parameters

-   *Output File* - the name and location of the output file
-   *Element Type* - the graph element type
-   *Attributes* - the list of attributes to include in the export
-   *Selected Only* - only export the selected elements

## Export to GeoPackage

### Parameters

-   *Output File* - the name and location of the output file
-   *Spatial Reference* - the spatial reference to use for the
    geopackage
-   *Element Type* - the graph element type
-   *Attributes* - the list of attributes to include in the export
-   *Selected Only* - only export the selected elements

## Export to Shapefile

### Parameters

-   *Output File* - the name and location of the output file
-   *Spatial Reference* - the spatial reference to use for the
    geopackage
-   *Element Type* - the graph element type
-   *Attributes* - the list of attributes to include in the export
-   *Selected Only* - only export the selected elements
-   *Geometry Type* - the geometry type to export

## Export to KML

### Parameters

-   *Output File* - the name and location of the output file
-   *Element Type* - the graph element type
-   *Attributes* - the list of attributes to include in the export
-   *Selected Only* - only export the selected elements

## Export to CSV and XLSX

Constellation also provides options to export to CSV and Excel (XLSX)
via the Table View. Refer
[here](../ext/docs/CoreTableView/src/au/gov/asd/tac/constellation/views/tableview/table-view.md)
for details.
