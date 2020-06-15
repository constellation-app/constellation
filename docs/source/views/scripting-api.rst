Scripting View
--------------

Welcome to the Constellation Scripting View! This document will walk you through the API, describing the available graph and plugin operations as well as any other functions available to you.

The Basics
``````````

The Scripting View provides access any open graph in Constellation programmatically via a range of scripting languages (with Python being the preferred language). It is able to access any part of Constellation's internal public API, ie. anything a Constellation developer can access) as well as easy-to-use scripting specific data-structures.

When you open the Scripting View, it will initialise several custom objects for you to make use of in your scripts.

* *graph*: this object represents the current active graph, and allows you to take a copy for reading from using *readableGraph()*, or a copy for writing to using *writableGraph(editName)*. For information on how to use this object, refer to the `javadoc <javadoc/au-gov-asd-tac-constellation-scripting/au/gov/asd/tac/constellation/scripting/graph/SGraph.html>`_.
* *utilities*: this object provides various convenience functions provided by the core developers, for example, *openFile(dirKey)* to activate a Netbeans file open dialog and return the selected file path as a string. If your chosen scripting language is Python, you can use its *dir* function to see what else is available, otherwise refer to the `javadoc <javadoc/au-gov-asd-tac-constellation-scripting/au/gov/asd/tac/constellation/scripting/utilities/ScriptingUtilities.html>`_ for more information on how to use this object.

You can add your own custom objects by extending the ScriptingModule class in the Scripting Module. Speak to your developers for more information on what they have added, or to include your own custom scripting objects.

The Graph
`````````

The current active graph is always readily accessible using the provided *graph* object, which provides a number of convenient methods for reading from and writing to the graph. Under the hood, the graph object is an *SGraph*, which provides access for interrogation and editing of the underlying Constellation graph. The SGraph object, as well as all the objects obtainable from an SGraph are documented in the `API javadoc <javadoc/au-gov-asd-tac-constellation-scripting/index.html>`_.

You can access all other open graphs using the *utilities* object as follows:

* *getOpenGraphs()* will return a map or dictionary of graph name to graph for every currently open graph.

You can also make a copy of a graph using the *utilities* object as follows:

* *copyGraph(graph)* will return an in-memory copy of the provided graph.

Plugins
```````

Every user action in Constellation is designed to be a disposable operation (generally on the graph) called a plugin. The Scripting View provides the ability to run any plugin available to Constellation using the provided *utilities* object. The utilities object provides two ways to run a plugin:

* *executePlugin(graph, pluginName)* allows you to run a plugin by name with default parameter values.
* *executePlugin(graph, pluginName, pluginParameters)* allows you to run a plugin by name with custom parameter values. Parameter values should be provided as a map or dictionary of parameter names as strings to parameter values as strings.

Plugin names and parameters are documented in the *Plugins* tab of the Schema View within Constellation.


.. help-id: au.gov.asd.tac.constellation.views.scripting
