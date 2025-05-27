# Scripting View

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
<th style="text-align: center;">Menu Icon</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Open Scripting View</td>
<td>Ctrl + Shift + X</td>
<td>Views -&gt; Scripting View</td>
<td style="text-align: center"><img src="../ext/docs/CoreScriptingView/resources/scripting_view.png" alt="Scripting View Icon" /></td>
</tr>
</tbody>
</table>

## Introduction

The Scripting View provides access to any open graph in Constellation
programmatically via a range of scripting languages (with Python being
the preferred language). It is able to access any part of
Constellation's internal public API, ie. anything a Constellation
developer can access) as well as easy-to-use scripting specific
data-structures.

<div style="text-align: center">

<img src="../ext/docs/CoreScriptingView/resources/ScriptingView.png" alt="Scripting
View" />

</div>

When you open the Scripting View, it will initialise several custom
objects for you to make use of in your scripts:

-   *graph* - This object represents the current active graph, and
    allows you to take a copy for reading from using "readableGraph()",
    or a copy for writing to using "writableGraph(editName)". For
    information on how to use this object, refer to the
    [javadoc](docs/javadoc/graph/SGraph.md).
-   *utilities* - This object provides various convenience functions
    provided by the core developers (e.g. "openFile(dirKey)" to activate
    a Netbeans file open dialog and return the selected file path as a
    string). If your chosen scripting language is Python, you can use
    its "dir" function to see what else is available, otherwise refer to
    the
    [javadoc](docs/javadoc/utilities/ScriptingUtilities.md)
    for more information on how to use this object.

You can add your own custom objects by extending the ScriptingModule
class in the Scripting Module. Speak to your developers for more
information on what they have added, or to include your own custom
scripting objects.

## The Graph

The current active graph is always readily accessible using the provided
graph object and provides a number of convenient methods for reading
from and writing to the graph. Under the hood, the graph object is an
"SGraph", which provides access for interrogation and editing of the
underlying Constellation graph. The SGraph object, as well as all the
objects obtainable from an SGraph are documented in the [API
javadoc](docs/javadoc/index-all.md).

Using the utilities object, you can access all other open graphs using
"getOpenGraphs()". This function will return a map or dictionary of
graph name to graph for every currently open graph.

The utilities object can also be used to make a copy of graph through
"copyGraph(graph)" This will return an in-memory copy of the provided
graph.

## Plugins

Every user action in Constellation is designed to be a disposable
operation (generally on the graph) called a plugin. The Scripting View
provides the ability to run any plugin available to Constellation using
the provided utilities object. The utilities object provides two ways to
run a plugin:

-   *executePlugin(graph, pluginName)* - Allows you to run a plugin by
    name with default parameter values.
-   *executePlugin(graph, pluginName, pluginParameters)* - Allows you to
    run a plugin by name with custom parameter values. Parameter values
    should be provided as a map or dictionary of parameter names as
    strings to parameter values as strings.

Plugin names and parameters are documented in the Plugins tab of the
Schema View within Constellation.
