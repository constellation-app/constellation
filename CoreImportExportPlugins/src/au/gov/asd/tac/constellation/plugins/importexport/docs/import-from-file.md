# Import From File

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
<td>Open Delimited File Importer</td>
<td></td>
<td>File -&gt; Import -&gt; From File...</td>
<td style="text-align: center;"><img src="../constellation/CoreImportExportPlugins/src/au/gov/asd/tac/constellation/plugins/importexport/docs/resources/importDelimited.png" alt="Import From File Icon" /></td>
</tr>
</tbody>
</table>

## Introduction

The "Import From File" window allows you to import data from the
following sources:

-   Comma-separated values (CSV)
-   Tab-separated values (TSV)
-   Excel
-   XML
-   JSON

<div style="text-align: center">

<img src="../constellation/CoreImportExportPlugins/src/au/gov/asd/tac/constellation/plugins/importexport/docs/resources/DelimitedFileImporter.png" alt="Delimited File
Importer" />

</div>

## High Level Workflow Steps

The following is a high level workflow an analyst can follow (explained
in detail later on):

1.  Select the file type you want to import with File Parser
2.  Click on the <img src="../constellation/CoreImportExportPlugins/src/au/gov/asd/tac/constellation/plugins/importexport/docs/resources/plus_coloured.png" alt="Add File
    Button" />
    to add files.
3.  Select your destination graph.
4.  Drag and drop attributes from the bottom pane onto your columns.
5.  Right click an attribute for more options.
6.  Click on the Import button to import the data to your destination
    graph.
7.  Save your template using Options -> Save.
8.  Share the template

Hints:

-   See all supported attributes using Options -> Show all schema
    attributes
-   Hover over the attribute name for a tooltip.
-   Filter in the Configuration Pane by adding searches of the form
    \<column_name> =="\<search text>" E.g. first_name=="Nick".

## Add and Remove Files to Import

Add files to import using the <img src="../constellation/CoreImportExportPlugins/src/au/gov/asd/tac/constellation/plugins/importexport/docs/resources/plus_coloured.png" alt="Add File
Button" />
and remove a file using the <img src="../constellation/CoreImportExportPlugins/src/au/gov/asd/tac/constellation/plugins/importexport/docs/resources/minus_coloured.png" alt="Remove File
Button" />.
Once a file has been selected, the full name will appear. If the entire
file location is not visible, extend the window to see the full name.

Once you add a file, the Configuration section will display the first
100 rows of the file as a preview. If you have imported multiple files,
selecting the file will update the preview.

With the "Initialise With Schema" option enabled, new nodes and
transactions added to the graph will follow the rules governed by the
schema. If this is not enabled, nodes imported will not have attributes
such as label and icon defined.

With the "Files Include Headers" option enabled, the importer will treat
the first row of the file as a header and it not be imported. The header
row is then displayed as the column names in the preview. With the
option disabled, the importer will import every row including the first
row. The preview in this case will add some auto-generated column names
(these won't affect the file or the import).

## Select the Destination Graph

Select the destination graph from the drop-down list. Once selected, the
attributes in the configuration pane update depending on what attributes
are supported by the schema of that graph.

## Apply Attributes

Applying an attribute to the column is simple as dragging and dropping
an attribute onto a column in the preview.

By default, not all attributes available to the selected schema are
visible because there can be an overwhelming number of attributes
depending on the schema. To see all possible attributes you can apply to
a graph go to Options -> Show all schema attributes.

If you have a specific format for example the DateTime, then you can
right click on the DateTime attribute and select the format from the
drop down list or enter your own.

If you want to create your own attributes then you can by clicking on
<img src="../constellation/CoreImportExportPlugins/src/au/gov/asd/tac/constellation/plugins/importexport/docs/resources/plus_black.png" alt="Add Attribute
Icon" />
and selecting a type and entering a label.

## Directed and Undirected Transactions

By default, transactions are created as directed. In the Transaction
Attributes column there is a pseudo-attribute called "\_\_directed\_\_"
(different from "directed") which can be used to create directed and
undirected transactions as required. The "\_\_directed\_\_"
pseudo-attribute is a boolean that will cause a transaction to be
directed when its value is "true" (case insensitive), and undirected
otherwise.

## Import Data

Once your configuration is finished, import the entire file with the
format you have applied by clicking on the Import button located at the
bottom right hand corner.

NOTE: The import window does not disappear after clicking import. This
is so that you don't have to re-configure from the beginning. Once you
are satisfied with the configuration, you have the ability to save your
settings explained in the next section.

## Save and Load Import Templates

An import configuration can be saved as a template, and loaded at a
later time, using the "Options" menu in the top left corner.

When a template is saved, the import definition name is used to name the
saved query. If a query of that name has already been saved, you will be
asked if you want to overwrite it. Templates are saved in the directory
"\<HOME_DIRECTORY>/.CONSTELLATION/ImportDelimited". (The name of the
file in which the template is saved is encoded so it doesn't clash with
file system limitations.) Files in this directory can be deleted using
your favourite file management utility.

Before loading a template, select the files you want to import. The
loaded template then attempts to match the column names it knows with
the column names in the current files. If a template is loaded before
any files are selected, there are no columns to match against.

When you select "Load...", you will be presented with a list of saved
templates. Select one from the list and select OK. The template will be
loaded and will appear as it was when it was saved.

## Share Templates

To share the template do the following:

1.  Click on Help -> User Directory
2.  Navigate to the folder called "ImportDelimited"
3.  Give the appropriate .json file to a recipient
4.  Upon receiving the file, they can follow these steps to save that
    .json file inside the "ImportDelimited" directory

NOTE: The .json filename should not change!
