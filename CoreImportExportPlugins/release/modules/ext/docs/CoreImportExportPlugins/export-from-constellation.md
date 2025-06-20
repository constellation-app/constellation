# Export From Constellation

Constellation provides a number of options for exporting graphical information. Some
of these export options may provide options to customise the export. 
Others will simply request a destination file location. 

Exporting from Constellation can be broken into two groups, **Visualisation Exports** and **Data Exports**. 

## Visualisation Exports
Visualisation exports, export the graph as viewed from the Constellation Interactive Graph. These exports will typicaly only contain visual information 
relevant to the visual aspects of the graph, but may contain some other identifying information.

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
<th>User Action</th>
<th style="text-align: center;">Menu Icon</th>
<th style="text-align: center;">More Help</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Export to PNG</td>
<td>File -&gt; Export -&gt; To Screenshot Image...</td>
<td style="text-align: center;"><img src="../ext/docs/CoreImportExportPlugins/resources/exportToImage.png" alt="Export to Screenshot Image Icon" /></td>
<td style="text-align: center;"><a href="../ext/docs/CoreImportExportPlugins/export-to-png.md">More Help</a></td>
</tr>
<tr class="even">
<td>Export to SVG</td>
<td>File -&gt; Export -&gt; To SVG...</td>
<td style="text-align: center;"><img src="../ext/docs/CoreImportExportPlugins/resources/exportToSVG.png" alt="Export to SVG Image Icon" /></td>
<td style="text-align: center;"><a href="../ext/docs/CoreImportExportPlugins/export-to-svg.md">More Help</a></td>
</tr>
</tbody>
</table>

## Data Exports
Data exports will export the entirety of the graph in a format that preserves the data used for analysis and visualisation in constellation.  
These formats typically contain rich information about the element being represented in the graph and are easily transferable into other platforms. 

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
<th>User Action</th>
<th style="text-align: center;">Menu Icon</th>
<th style="text-align: center;">More Help</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Export to JSON</td>
<td>File -&gt; Export -&gt; To JSON...</td>
<td style="text-align: center;"><img src="../ext/docs/CoreImportExportPlugins/resources/exportToJSON.png" alt="Export to JSON Icon" /></td>
<td style="text-align: center;"><a href="../ext/docs/CoreImportExportPlugins/export-to-json.md">More Help</a></td>
</tr>
<tr class="even">
<td>Export to GeoJSON</td>
<td>File -&gt; Export -&gt; To GeoJSON...</td>
<td style="text-align: center;"><img src="../ext/docs/CoreImportExportPlugins/resources/exportToGeoJSON.png" alt="Export to GeoJSON Icon" /></td>
<td style="text-align: center;"><a href="../ext/docs/CoreImportExportPlugins/export-to-geojson.md">More Help</a></td>
</tr>
<tr class="odd">
<td>Export to GeoPackage</td>
<td>File -&gt; Export -&gt; To GeoPackage...</td>
<td style="text-align: center;"><img src="../ext/docs/CoreImportExportPlugins/resources/exportToGeoPackage.png" alt="Export to GeoPackage Icon" /></td>
<td style="text-align: center;"><a href="../ext/docs/CoreImportExportPlugins/export-to-geopackage.md">More Help</a></td>
</tr>
<tr class="even">
<td>Export to Shapefile</td>
<td>File -&gt; Export -&gt; To Shapefile...</td>
<td style="text-align: center;"><img src="../ext/docs/CoreImportExportPlugins/resources/exportToShapeFile.png" alt="Export to Shapefile Icon" /></td>
<td style="text-align: center;"><a href="../ext/docs/CoreImportExportPlugins/export-to-shapefile.md">More Help</a></td>
</tr>
<tr class="odd">
<td>Export to KML</td>
<td>File -&gt; Export -&gt; To KML...</td>
<td style="text-align: center;"><img src="../ext/docs/CoreImportExportPlugins/resources/exportToKML.png" alt="Export to KML Icon" /></td>
<td style="text-align: center;"><a href="../ext/docs/CoreImportExportPlugins/export-to-kml.md">More Help</a></td>
</tr>
<tr class="even">
<td>Export to CSV</td>
<td>Views -&gt; Table View -&gt; <img src="../ext/docs/CoreImportExportPlugins/resources/TableExport.png" alt="Table Export" /> -&gt; Export To CSV</td>
<td style="text-align: center;"><img src="../ext/docs/CoreImportExportPlugins/resources/table-view.png" alt="Table View Icon" /></td>
<td style="text-align: center;"><a href="../ext/docs/CoreImportExportPlugins/export-to-csv.md">More Help</a></td>
</tr>
<tr class="odd">
<td>Export to Excel (XLSX)</td>
<td>Views -&gt; Table View -&gt; <img src="../ext/docs/CoreImportExportPlugins/resources/TableExport.png" alt="Table Export" /> -&gt; Export To Excel (XLSX)</td>
<td style="text-align: center;"><img src="../ext/docs/CoreImportExportPlugins/resources/table-view.png" alt="Table View Icon" /></td>
<td style="text-align: center;"><a href="../ext/docs/CoreImportExportPlugins/export-to-xlsx.md">More Help</a></td>
</tr>
</tbody>
</table>

-   *Screenshot Image* - Export a screenshot of the graph window as a
    .png
-   *SVG Image* - Export a screenshot of the graph window as a
    .svg
-   *JSON* - Export a JSON representation of the graph (described in
    detail
    [here](../ext/docs/CoreGraphFramework/constellation-file-format.md))
-   *GeoJSON* - Export a GeoJSON representation of the graph
-   *GeoPackage* - Export the graph as a GeoPackage file
-   *Shapefile* - Export the graph as a Shapefile file
-   *KML* - Export the graph as a KML file
-   *CSV* - Export the graph as a CSV file. Refer
    [here](../ext/docs/CoreTableView/table-view.md)
    for details
-   *XLSX* - Export the graph as an XLSX (Excel Spreadsheet) file. Refer
    [here](../ext/docs/CoreTableView/table-view.md)
    for details
