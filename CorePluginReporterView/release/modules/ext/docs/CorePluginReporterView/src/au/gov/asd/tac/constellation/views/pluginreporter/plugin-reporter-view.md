# Plugin Reporter

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
<td>Open Plugin Reporter</td>
<td>Ctrl + Shift + P</td>
<td>Views -&gt; Plugin Reporter</td>
<td><div style="text-align: center">
<img src="../ext/docs/CorePluginReporterView/src/au/gov/asd/tac/constellation/views/pluginreporter/resources/plugin-reporter.png" width="16" height="16" />
</div></td>
</tr>
</tbody>
</table>

The Plugin Reporter stores a history of any plugin run on the graph
currently in focus.

<div style="text-align: center">

<img src="../ext/docs/CorePluginReporterView/src/au/gov/asd/tac/constellation/views/pluginreporter/resources/PluginReporter.png" alt="Plugin
Reporter" />

</div>

A plugin report will be created whenever a plugin is run, and added to
the Plugin Reporter. These reports will change their color and message
based on the current status of the plugin they represent.

-   Green indicates the plugin is currently running.
    <div style="text-align: center">

    <img src="../ext/docs/CorePluginReporterView/src/au/gov/asd/tac/constellation/views/pluginreporter/resources/PluginReportGreen.png" alt="Running Plugin
    Report" />

    </div>
-   Blue indicates that the plugin was cancelled.
    <div style="text-align: center">

    <img src="../ext/docs/CorePluginReporterView/src/au/gov/asd/tac/constellation/views/pluginreporter/resources/PluginReportBlue.png" alt="Cancelled Plugin
    Report" />

    </div>
-   Orange indicates that an anticipated error occurred.
    <div style="text-align: center">

    <img src="../ext/docs/CorePluginReporterView/src/au/gov/asd/tac/constellation/views/pluginreporter/resources/PluginReportOrange.png" alt="Anticipated Error Plugin
    Report" />

    </div>
-   Red indicates that an unexpected error occurred.
    <div style="text-align: center">

    <img src="../ext/docs/CorePluginReporterView/src/au/gov/asd/tac/constellation/views/pluginreporter/resources/PluginReportRed.png" alt="Unexpected Error Plugin
    Report" />

    </div>
-   Grey indicates that the plugin finished successfully.
    <div style="text-align: center">

    <img src="../ext/docs/CorePluginReporterView/src/au/gov/asd/tac/constellation/views/pluginreporter/resources/PluginReportGrey.png" alt="Successful Plugin
    Report" />

    </div>

The Plugin Reporter allows filtering of the plugin history using a
tag-based system. You can filter on plugin reports by their tags using
the "Filter" drop down menu. NOTE: You will only be able to filter tags
which are present in plugins that have already been executed. The list
of tags you can filter on may include:

-   *General* - Plugins which don't fit into any specific category.
-   *Create* - Plugins which create graphs, nodes, and transactions.
-   *Modify* - Plugins that change the graph in some way.
-   *Delete* - Plugins that delete things from the graph.
-   *Search* - Plugins that search the graph.
-   *Select* - Plugins which change what is selected on the graph.
-   *View* - Plugins the change the camera view of the graph.
-   *Import* - Plugins which add data from other sources to the graph.
    Generally found in the Data Access View.
-   *Export* - Plugins which export data from Constellation into other
    sources
-   *Clean* - Plugins which clean up the existing data on your graph.
    Found in the Data Access View.
-   *Enrich* - Plugins which add more information to the existing nodes
    and transactions on your graph. Found in the Data Access View.
-   *Extend* - Plugins which add more information with new nodes and
    transactions to your graph. Found in Data Access View.
-   *Utility* - Plugins which perform utility functions.
-   *Experimental* - Plugins which perform experimental features in
    Constellation.
-   *Developer* - Plugins designed for use by Constellation developers.
-   *Analytic* - Plugins which perform some analytic calculation based
    on information on the graph. Found in the Analytic View
-   *Low Level* - Plugins which are generally hidden from the user.
-   *Welcome* - Plugins which are accessed from the Welcome Page.

The "Clear" button will clear all currently displayed plugin reports,
and only display new plugin reports for the active graph.

The "Show All" button will make the Plugin Reporter display all plugin
reports for the active graph.
