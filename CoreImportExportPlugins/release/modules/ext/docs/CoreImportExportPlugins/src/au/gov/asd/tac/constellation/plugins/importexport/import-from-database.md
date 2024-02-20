# Import From Database

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
<td>Open Database Importer</td>
<td></td>
<td>File -&gt; Import -&gt; From Database...</td>
<td style="text-align: center;"><img src="../ext/docs/CoreImportExportPlugins/src/au/gov/asd/tac/constellation/plugins/importexport/resources/jdbc_import.png" alt="Import From Database Icon" /></td>
</tr>
</tbody>
</table>

## Introduction

The "Import from Database" window allows you to import data from a
database using a JDBC Connection.

Full Example:
<div style="text-align: center">
    <img src="../ext/docs/CoreImportExportPlugins/src/au/gov/asd/tac/constellation/plugins/importexport/resources/ImportDatabaseFullExample.png" alt="Database Importer Connection
    example with all details" />
</div>
<br/>



## High Level Workflow Steps

The following is a high level workflow an analyst can follow (explained
in detail later on):

1.  Add the relevant JDBC Driver via "Manage Connections" -&gt; "Drivers"
    tab. These drivers will be specific to the database that you are
    connecting to. You only need one driver for each database type e.g.
    MySQL, PostgresQL, SQLite Example drivers:

    -   MySQL -
        [mysql-connector-java-8.0.23.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.23/mysql-connector-java-8.0.23.jar)
    -   SQLite -
        [sqlite-jdbc-3.36.0.3.jar](https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.36.0.3/sqlite-jdbc-3.36.0.3.jar)
    -   Postgresql -
        [postgresql-42.7.1.jar](https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.1/postgresql-42.7.1.jar)

    <div style="text-align: center">
        <img src="../ext/docs/CoreImportExportPlugins/src/au/gov/asd/tac/constellation/plugins/importexport/resources/DBDriversExample.png" alt="Database
drivers example" />

    </div>

2.  Add the Connection details in "Manage Connections" -&gt; "Connections"
    tab. The connection will be an instance to a specific database.

    -   *Connection name* - user specified name for the connection
    -   *Driver* - Pick the database driver needed to connect to the db
    -   *Connection String* - It will contain the database type, host, port,
        and name of the database in the format `jdbc:[database
        name]://[host]:[port]/[database name]`
    -   e.g. `jdbc:mysql://localhost:3306/employees` for mysql or
        `jdbc:postgresql://localhost:5432/test` for postgres.
    -   If the username and password are not required, leave them blank.

        <img src="../ext/docs/CoreImportExportPlugins/src/au/gov/asd/tac/constellation/plugins/importexport/resources/DBConnections.png" alt="Database Importer Connection example" />
3.  Select the connection from the "Connection" drop-down in the main
    Import window.
4.  Enter the username and password if the connection requires them.
5.  Enter the SQL Query and Click the "Query" button to retrieve data.
6.  Select your destination graph.
7.  Drag and drop attributes from the bottom pane onto your columns (Shown in the image below).
8.  Right click an attribute for more options.
9.  Click on the "Import" button to import the data to your destination
    graph.

    <img src="../ext/docs/CoreImportExportPlugins/src/au/gov/asd/tac/constellation/plugins/importexport/resources/dragging_attributes_jdbc_import.png" alt="Database
    Importer" />

Hints:

-   See all supported attributes using Options -> Show all schema
    attributes
-   Hover over the attribute name for a tooltip.
-   Filter in the Configuration Pane by adding searches e.g. first_name=="Nick".


## Add and Remove Connections to Import Queries From

Once you select a connection and submit a query, the "Configuration"
section will display the first 100 rows as a preview.

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
<img src="../ext/docs/CoreImportExportPlugins/src/au/gov/asd/tac/constellation/plugins/importexport/resources/plus_black.png" alt="Add Attribute Icon"/> and selecting a type and
entering a label.

## Directed and Undirected Transactions

By default, transactions are created as directed. In the Transaction
Attributes column there is a pseudo-attribute called "\_\_directed\_\_"
(different from "directed") which can be used to create directed and
undirected transactions as required. The "\_\_directed\_\_"
pseudo-attribute is a boolean that will cause a transaction to be
directed when its value is "true" (case insensitive), and undirected
otherwise.

## Importing Data

Once your configuration is finished, import the entire query by clicking
on the Import button located at the bottom right hand corner.

NOTE: The import view does not disappear after clicking import. This is
so that you don't have to re-configure from the beginning. Once you are
satisfied with the configuration, you have the ability to save your
settings explained in the next section.

## Save and Load Import Templates

An import configuration can be saved as a template, and loaded at a
later time, using the "Options" menu in the top left corner.

When a template is saved, the import definition name is used to name the
saved query. If a query of that name has already been saved, you will be
asked if you want to overwrite it. Templates are saved in the directory
"&lt;HOME_DIRECTORY&gt;/.CONSTELLATION/ImportDatabase". (The name of the file in
which the template is saved is encoded so it doesn't clash with file
system limitations.) Files in this directory can be deleted using your
favourite file management utility.

Before loading a template, select the query you want to import. The
loaded template then attempts to match the column names it knows with
the column names in the current query. If a template is loaded before
any queries are selected, there are no columns to match against.

When you select "Load...", you will be presented with a list of saved
templates. Select one from the list and select OK. The template will be
loaded and will appear as it was when it was saved.

## Share Templates

To share the template do the following:

1.  Click on Help -> User Directory
2.  Navigate to the folder called "ImportDatabase"
3.  Give the appropriate .json file to a recipient
4.  Upon receiving the file, they can follow these steps to save that
    .json file inside the "ImportDatabase" directory

NOTE: The .json filename should not change!
