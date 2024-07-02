# Export Graph Visualisation to SVG
[More export options...](../ext/docs/CoreImportExportPlugins/src/au/gov/asd/tac/constellation/plugins/importexport/export-from-constellation.md)

This plugin exports Graphs to an SVG file format that represents the graph as viewed from the interactive graph window. 

An SVG file is a form of vectorized image file known as a "Scalar Vector Graphic". SVG images contain graphical information in the form of shapes and lines and are 
subsequently able to produce high quality images at practically any scale. SVG images store their information in a plain text format and is easily manipulatable with a standard text editor.
SVG images may require specialised image viewing software but are also viewable in a standard web browser.  

When this plugin is executed, parameters will automatically reflect the state of the current graph. 
Users may modify these parameters during the export to include, exclude, or adjust information from the export.  

Please note that this export will include high quality representations of visual elements in the graph. If made visible in the export parameters,
assets such as node and transaction labels will be included in the export, regardless of whether they are visible in the Constellation graph visualisation due to distance.


### Parameters
<div style="text-align: center;">
    <a href="../ext/docs/CoreImportExportPlugins/src/au/gov/asd/tac/constellation/plugins/importexport/resources/exportToSVGParameters.png">
        <img src="../ext/docs/CoreImportExportPlugins/src/au/gov/asd/tac/constellation/plugins/importexport/resources/exportToSVGParameters.png" alt="Export to SVG Example"  width="573"/>
    </a>
</div>
<br />

-   *File Location* (Required) - the name and location of the output file
-   *Graph Title* - the title of the graph 
-   *Background Color* - the color of the graph background
-   *Selected Elements* - only export the selected nodes
-   *Show Nodes* - include nodes in the export
-   *Show Connections* - include connections in the export
-   *Show Node Labels* - include node labels in the export
-   *Show Connection Labels* - include connections labels in the export
-   *Show Blazes* - include blazes in the export
-   *Perspective* - set the camera perspective to export the graph from



