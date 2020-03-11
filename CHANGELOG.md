# CONSTELLATION Changes

## 2020-03-01 Changes in March 2020
* Fix a logic bug with `GraphRendererDropTarget` preventing graph droppers from every running.

## 2020-02-01 Changes in February 2020
* Fixed a bug which now ensures that overriding a transaction direction using `GraphRecordStoreUtilities.DIRECTED_KEY` persists with the Type.
* Renamed `NodeGraphLabelsEditorFactory` to `VertexGraphLabelsEditorFactory`.
* Renamed `SupporPackageAction` to `SupportPackageAction` to fix a spelling typo.

## 2020-01-01 Changes in January 2020
* Added LabelFontsOptionsPanel to allow setting of fonts rendered on the graph through the UI.
* Added ConstellationLabelFonts interface to allow programmatic specification of default label fonts.

## 2019-12-01 Changes in December 2019
* Added method suppressEvent(boolean, List<>) to PluginParameter which allow setting of properties/options without firing change events.
* Moved CoreUtilities in the Core Functionality module to PreferenceUtilites in the Core Utilities module.
* Renamed ArcgisMap Provider to EsriMapProvider.
* Updated EsriMapProvider to support both regular tile-based services, as well as image export. This can be specified by overriding the new getMapServerType method.

## 2019-11-01 Changes in November 2019
* Remove deprecated jai libraries.

## 2019-10-01 Changes in October 2019
* Added `DevOpsNotificationPlugin` to Core Functionality to track messages from plugins for developers and administrators attention. This is only going to be useful if you have setup a `ConstellationLogger` that sends information to a database or elastic search.
* Fixed a bug with the Restful service caused by multiple servlet libraries used that created a clash.

## 2019-08-01 Changes in August 2019
* Added BrandingUtilities to Core Utilities to maintain the application name "Constellation".
    * You can set the command line argument `constellation.environment` with a label and it will appear in the title. For instance, this could be used to distinguish "Development", "QA" and "Production" versions.
* Added PluginParameters.hasParameter() to the Core Plugin Framework module as a convenient way to check if a parameter exists.
* Fixed a Null Pointer Exception when selecting Circle arrangements.
* Fixed the GitHub url used by Help -> Submit a ticket.
* Removed several unused dependencies, including JOGL, JTS, OpenCSV, Trove4j, JScience, and XML-APIs.
* Renamed ConstellationLogger.ApplicationStart to ConstellationLogger.ApplicationStarted, ConstellationLogger.ApplicationStop to ConstellationLogger.ApplicationStopped, ConstellationLogger.PluginStart to ConstellationLogger.PluginStarted and ConstellationLogger.PluginStop to ConstellationLogger.PluginStopped.
* Updated several dependencies to the latest versions, including Geotools, Jetty, Apache Commons, Jackson, RSyntaxArea, Google Guava, Apache POI, EJML, Processing, Jython, and SwingX.
* Updated ConstellationLogger with new methods viewStarted, viewStopped and viewInfo to support logging of Views.
* Updated DefaultConstellationLogger with a VERBOSE flag to switch between no-op and logging to standard out.
* Updated AbstractTopComponent to log when the view is opened, closed, showing, hidden, activated and deactivated.

## 2019-06-01 Changes in June 2019
* Added a Content.URL attribute to represent a URL link in the ContentConcept.
* Fixed a lot of compile warnings related to Java generics and PluginParameters usage.
* Removed ConstellationSecurityProvider.getPriority as it duplicated functionality of (and conflicted with) the lookup system.
* Removed OldStringUtilities and merged the required methods to StringUtilities.

## 2019-05-01 Changes in May 2019
* Fixed a bug with SchemaVertexTypeUtilities and SchemaTransactionTypeUtilities not respecting overridden types.
* Removed MODIFIED icon from UserInterfaceIconProvider.
* Removed STARS and CONSTELLATION icons from AnalyticIconProvider.
* Updated CHART icon in AnalyticIconProvider.
* Updated RestClient in the Core Utilities module with a minor refactor and support for posting bytes.
* Updated SchemaFactory with getIconSymbol and getIconColor methods to allow for more customisable icons. Graph icons will now be made up of a symbol on top of a colored square background much like how vertices on a graph are represented.
* Updated the font used by the renderer from Arial Unicode MS to Malgun Gothic due to licensing restrictions with the Arial font resulting it from no longer being installed on Windows by default.

## 2019-04-01 Changes in April 2019
* Renamed NodeGraphLabelsAttributeDescription, NodeGraphLabelsAttributeInteraction, and NodeGraphLabelsIOProvider to VertexGraphLabelsAttributeDescription, VertexGraphLabelsAttributeInteraction, and VertexGraphLabelsIOProvider for consistency.
* Updated the SchemaAttribute.ensure() method to create the attribute if it does not exist by default. This fixes a number of plugins that failed if the attribute was not defined.
* Updated SimpleEditPlugin.edit() method to be abstract as it doesn't make sense to have an edit plugin without any editing occurring.

## 2019-03-01 Changes in March 2019
* Added 23 new country flag icons.
* Added Arrange by Node Attribute to combine the function of Arrange by Group and Arrange by Layer into a single plugin.
* Added an updateParameters method to au.gov.asd.tac.constellation.views.histogram.formats.BinFormatter for Histogram View BinFormatters to use.
* Fixed how ConstellationIcon was building and caching icons and images resulting in a major performance improvement and reduced memory usage.
* Updated Ctrl+Backspace to do nothing so that pressing it in a text area on a docked window won't cause it to minimize.

## 2019-02-01 Changes in February 2019
* Added a new interface called DataAccessPreQueryValidation to check before running Data Access View queries.

## 2019-01-01 Changes in January 2019
* Added Enrichment to the DataAccessPluginCoreType class.
* Moved au.gov.asd.tac.constellation.analyticview to au.gov.asd.tac.constellation.views.analyticview.
* Moved au.gov.asd.tac.constellation.attributeeditor to au.gov.asd.tac.constellation.views.attributeeditor.
* Moved au.gov.asd.tac.constellation.conversationview to au.gov.asd.tac.constellation.views.conversationview.
* Moved au.gov.asd.tac.constellation.core to au.gov.asd.tac.constellation.functionality.
* Moved au.gov.asd.tac.constellation.core.dependencies to au.gov.asd.tac.constellation.dependencies.
* Moved au.gov.asd.tac.constellation.dataaccess to au.gov.asd.tac.constellation.views.dataaccess.
* Moved au.gov.asd.tac.constellation.display to au.gov.asd.tac.constellation.visual.opengl.
* Moved au.gov.asd.tac.constellation.find to au.gov.asd.tac.constellation.views.find.
* Moved au.gov.asd.tac.constellation.histogram to au.gov.asd.tac.constellation.views.histogram.
* Moved au.gov.asd.tac.constellation.interactivegraph to au.gov.asd.tac.constellation.graph.interaction.
* Moved au.gov.asd.tac.constellation.mapview to au.gov.asd.tac.constellation.views.mapview.
* Moved au.gov.asd.tac.constellation.qualitycontrol to au.gov.asd.tac.constellation.views.qualitycontrol.
* Moved au.gov.asd.tac.constellation.scatterplot to au.gov.asd.tac.constellation.views.scatterplot.
* Moved au.gov.asd.tac.constellation.schemaview to au.gov.asd.tac.constellation.views.schemaview.
* Moved au.gov.asd.tac.constellation.scripting to au.gov.asd.tac.constellation.views.scripting.
* Moved au.gov.asd.tac.constellation.tableview to au.gov.asd.tac.constellation.views.tableview.
* Moved au.gov.asd.tac.constellation.timeline to au.gov.asd.tac.constellation.views.timeline.
* Moved au.gov.asd.tac.constellation.visualgraph to au.gov.asd.tac.constellation.graph.visual.
* Moved au.gov.asd.tac.constellation.visualsupport to au.gov.asd.tac.constellation.visual.
* Moved au.gov.asd.tac.constellation.webview to au.gov.asd.tac.constellation.views.webview.
* Moved private classes that implemented ParameterValue to public classes to resolve the problem of not being able to set values from a script. These include AnalyticAggregatorParameterValue, SpatialReferenceParameterValue, ElementTypeParameterValue and GraphAttributeParameterValue.
* Renamed Plugin.getOverriddenPlugin to Plugin.getOverriddenPlugins in the Core Plugin Framework module which is a breaking change.
* Updated GraphWriteMethods to include a version of addTransaction that accepts a transaction ID.
* Updated PermanentMergeAction to run in it's own thread (rather than the EDT).
* Updated structure of Merge Transactions Plugin to allow for more merge by types.

## 2018-12-01 Changes in December 2018
* Added validation check to Date-Time Range Global Parameter in Data Access View.
* Added validation check to numeric parameters in plugins accessed from Data Access View.
* Added plugin CopyCustomMarkersToGraph to generate nodes on your graph from custom markers in the Map View.
* Fixed some performance issues in the conversation view.
* Updated MarkerCache with functions to build and style markers from the graph, allowing this functionality to be used outside of the Map View.
* Updated MarkerUtilities with generateBoundingBox method.
* Updated ConstellationAbstractMarker with getRadius method.

## 2018-11-01 Changes in November 2018
* Moved the getOverriddenPlugin method from the DataAccessPlugin class to the Plugin class. This technically allows the ability for any plugin to be overridden. Note that the current implementation pattern is to call a plugin from a registry and this would need to be modified before plugin calls from PluginExecutor could be overridden.
* Removed MultiScoreResult fromt he analytic view - all score based analytics now use ScoreResult and support multiple scores by default.
* Renamed IconProvider to ConstellationIconProvider
* Renamed GlobalCoreParameters to CoreGlobalParameters
* Renamed all plugin parameter id references from \*_PARAMETER to \*_PARAMETER_ID

## 2018-10-01 Changes in October 2018
* Added the overridenType property to SchemaElementType, and removed it from SchemaVertexType and SchemaTransactionType.
* Fixed a performance issue in SchemaElementType.toString() by pre computing the hierarchy on initialisation. SchemaElementType.getHierachy() is a slow method and was being called too many times. The performance improvement has made it about 1.7 times faster to load, save and work with graphs.
* Fixed the views that have not been upgraded to the new View Framework to have the font size applied to the Top Component on load.
* Updated SchemaElementType.isSubTypeOf to take overridden types into account.

## 2018-09-01 Changes in September 2018
* Added a new plugin to open a url in a browser called OpenInBrowserPlugin that is available in Core Functionality.
* Added a new plugin to send to an email client called SendToEmailClientPlugin that is available in Core Functionality.
* Renamed SchemaAttribute.getFormatContext to SchemaAttribute.getFormat.
* Updated PlaceholderUtilities with support for collapsing placeholders using the standard graph API.

## 2018-08-01 Changes in August 2018
* Added functionality to cache icons.
* Fixed a bug in the Analytic schema factory which was not correctly applying the schema rules to complete the vertex.
* Fixed a memory leak introduced by the FPSRenderable class.
* Fixed a performance issue with the Table View by moving work off the EDT.

## 2018-07-01 Changes in July 2018
* Added AnalyticConcept.VertexType.USER_NAME.
* Added Subdivision enum containing country subdivisions (currently incomplete).
* Added TemporalFormattingUtilties to the Core Utilities module.
* Added an IpAddressUtilities class to the Core Utilities module.
* Fixed a performance issue with JDropDownMenu in the Core Utilities module moving the actionPerformed work into its own thread.
* Fixed the spelling of public constants and comments that incorrectly spelt separator in various modules.
* Renamed AnalyticConcept.VertexType.HOSTNAME to AnalyticConcept.VertexType.HOST_NAME.
* Renamed GeospatialUtilities to Distance and moved HaversineUtilities to Distance.Haversine.
* Renamed ShapeUtilities to Shape, MgrsUtilities to Mgrs and GeohashUtilities to Geohash.
* Updated Country enum to align with the latest version of ISO 3166-1.
* Updated the copyright to Apache Version 2.0 with the Australian Signals Directorate being the License owner.

## 2018-06-01 Changes in June 2018
* Added a RestClient.postWithJson() to allow you to post your own json string in the Core Utilities module.
* Added plugins CreateVertexTypePlugin and CreateTransactionTypePlugin to allow REST clients to create custom types.
* Fixed PluginParameters to use ColorValue instead of Color. This caused a cascade of fixes in other classes.
* Fixed a bug with the spanning tree algorithm which was preventing it from creating a nraduis attribute.
* Renamed ColorValue to ConstellationColor to make it clearer what it should be used for and to avoid conflicts with external classes.
* Renamed TemporalFormatting.DATE_TIME_FORMATTER to TemporalFormatting.UTC_DATE_TIME_FORMATTER and TemporalFormatting.DATE_TIME_WITH_MILLISECONDS_FORMATTER to TemporalFormatting.UTC_DATE_TIME_WITH_MILLISECONDS_FORMATTER in the Core Utilities module. These DateTimeFormatter constants are now build using the DateTimeFormatterBuilder ensuring they convert dates to UTC correctly.
* Updated the support package generation to ignore the heapdump file as it is likely to be several gigabytes in size.

## 2018-05-01 Changes in May 2018
* Added FilterPlugin to the Core Data Access View module.
* Added PasswordParameterType into the Core Plugin Framework module. This change has also removed the capability of StringParameterType to support passwords.
* Added strict DateTimeFormatter's based on DateTimeFormatter.ISO_INSTANT to the TemporalFormatting class in Core Utilities.
* Fixed a performance issue by reducing the amount of "Find State" saves made to the graph as it's causing graph write lock contention for a trivial GUI state that will be already covered when you run a search, switch graphs or save the graph.
* Fixed an issue handling invalid Glyphs.
* Fixed some performance issues with the Map View.
* Removed deprecated methods being PluginGraph.executePluginLater, PluginGraph.executePluginNow and PluginGraph.updateGraph.
* Renamed Plugin.id to Plugin.ID
* Updated JOGL to 2.3.2 and Processing to 3.3.6 to resolve OpenGL issues with the Map view.
* Updated default node and transaction colors in order to ensure overlay colors stand out more.
* Updated default node z2 value.
* Updated password obfuscation to use a service lookup to get the key. To use it implement PasswordKey in the Core Security module.

## 2018-04-01 Changes in April 2018
* Added New Nebula into th Experimental->Tools menu
* Added an FPS counter OpenGL renderer that can be enabled from the Debug preference tab.
* Added new MatrixUtilities class for constructing useful matrices from a graph.
* Added simple icon shaders for rendering basic icons on the graph.
* Removed the REST API endpoints /forward (forwarding a request to another HTTP server), /resources (offering embedded web resources), and /static (serving resources from a specified local directory) have been removed.
* Renamed ApplicationPreferenceKeys.DEFAULT_FREEZE_GRAPH_VIEW to ApplicationPreferenceKeys.FREEZE_GRAPH_VIEW_DEFAULT in the Core Preferences module.
* Renamed SharedDrawable.getIconShader to SharedDrawable.getVertexIconShader to differentiate it from SharedDrawable.getSimpleIconShader.
* Updated Core Web Server code so the workings out of the web servlets into separate \*Impl.java classes. This makes the workings transport independent, in preparation for adding a non-HTTP transport. In theory, there should be no change in functionality.
* Updated the Core Web Server module to add a filesystem REST transport.
* Updated the constructor for Decorators to expect its parameters in a clockwise order: north-west, north-east, south-east, south-west.
* Updated the various \*Preference panels to follow and more of an MVC pattern.

## 2018-03-01 Changes in March 2018
* Added FactColorTranslator and FactToSizeTranslator to the Core Analytics View module.
* Added FirstAnalyticPlugin to the Core Analytics View module.
* Added FirstFactAggregator in the Core Analytics View module.
* Added a new attribute called isLabel to SchemaAttribute in the Core Graph Module with the intention of allowing the schema to decide if the attribute should appear as a GraphLabel on a vertex or transaction.
* Added a new isLabel and isDecorator attributes to SchemaAttribute with the intention of allowing the schema to decide if the attribute should appear as a GraphLabel or Decorator on a vertex or transaction.
* Added equivalent method to SchemaAttribute allowing you to compare with an Attribute object.
* Renamed ClusterAggregator to FirstClusterAggregator in the Core Analytics View module.
* Renamed MultiScoreResult.getUniqueNames to MultiScoreResult.getUniqueScoreNames in the Core Analytics View module.
* Renamed MultiScoringAnalyticPlugin to MultiScoreAnalyticPlugin in the Core Analytics View module.
* Renamed PluginRegistry.getPluginNames to PluginRegistry.getPluginClassNames.
* Renamed ScoringAnalyticPlugin to ScoreAnalyticPlugin in the Core Analytics View module.
* Renamed getLabel to getName and getType to GetAttributeType for the Attribute class.
* Renamed the get_pos(g) method to get_nx_pos(g) in the constellation_client provided with the REST API.
* SchemaFactory.ensureAttribute now takes a boolean specifying whether the attribute should be created even if it is not registered to that schema. Similarly, SchemaAttribute.ensure provides this option.

## 2018-02-01 Changes in February 2018
* Fixed memory leaks with the ListeningTopComponent and TimelineTopComponent.
* Renamed COLOR_BLAZE in the CorePluginRegistry to ADD_CUSTOM_BLAZE.
* Updated entries within the Tools and Experimental menu.
* Updated ListeningTopComponent to allow for the update and removal of change handlers.
    * addAttributeChangeHandler was renamed to addAttributeValueChangeHandler.
    * addGlobalChangeHandler, addStructureChangeHandler, addAttributeCountChangeHandler and addAttributeValueChangeHandler now return the Monitor objects associated with that handler.
    * updateGlobalChangeHandler, updateStructureChangeHandler, updateAttributeCountChangeHandler and updateAttributeValueChangeHandler have been added to allow modification to the behaviour of a handler.
    * removeGlobalChangeHandler, removeStructureChangeHandler, removeAttributeCountChangeHandler and removeAttributeValueChangeHandler have been added to allow removal of a handler.
    * removeIgnoredEvent has been added to allow removal of an ignored event.

## 2017-12-01 Changes in December 2017
* Added forward slash (/) to the list of special characters to escape in LuceneUtilities in the Core Utilities module.
* Removed extra FindStatePlugin calls from the ColorCriteriaPanel which will help reduce unnecessary write locks on the graph and reduce overall threads being forked.

## 2017-11-01 Changes in November 2017
* Added MultiplexityAnalytic and WeightAnalytic plugins and analytics.
* Added SnaConcept.Transaction.MULTIPLEXITY to the Core Algorithms module.
* Renamed SnaConcept.GRAPH.GRAPH_DENSITY to SnaConcept.GRAPH.DENSITY in the Core Algorithms module.

## 2017-10-01 Changes in October 2017
* Added GraphNodePluginRegistry in the Core Graph Node module.
* Added JSingleChoiceComboBoxMenu as a single choice alternative to JMultiChoiceComboBoxMenu.
* Added LuceneUtilities to the Core Utilities module.
* Added SeparatorConstants to the Core Utilities module.
* Added more attributes to ContentConcept in the Core Analytic Schema.
* Fixed a bug where the singleton type was not being used when loading a graph.
* Fixed a bug with the WorkflowQueryPlugin which was removing the graph attributes after a batched run completed.
* Moved NewDefaultSchemaGraphAction from the Core Simple Schema module to the Core Graph Node module.
* Moved NewExperimentalSchemaGraphAction from the Core Simple Schema module to the Core Graph Node module.
* Moved NewSchemaGraphAction from the Core Simple Schema module to the Core Graph Node module.
* Moved au.gov.asd.tac.constellation.algorithms.geospatial.Geohash in the Core Utilities module to au.gov.asd.tac.constellation.utilities.geospatial.GeohashUtilities
* Moved au.gov.asd.tac.constellation.algorithms.geospatial.Haversine in the Core Utilities module to au.gov.asd.tac.constellation.utilities.geospatial.HaversineUtilities
* Moved au.gov.asd.tac.constellation.core.opener.SimpleGraphTopComponent to au.gov.asd.tac.constellation.core.visual.SimpleGraphTopComponent.
* Moved au.gov.asd.tac.constellation.core.visual.SaveAsAction in the Core Functionality module to au.gov.asd.tac.constellation.core.save.SaveAsAction.
* Moved au.gov.asd.tac.constellation.graph.file.GraphOpener in the Core Graph File to au.gov.asd.tac.constellation.graph.file.opener.GraphOpener.
* Moved au.gov.asd.tac.constellation.graph.file.autosave.AutosaveUtilities in the Core Graph File module to au.gov.asd.tac.constellation.graph.file.save.AutosaveUtilities.
* Moved au.gov.asd.tac.constellation.schema.simpleschema.plugins.LoadTemplatePlugin from the Core Simple Schema to au.gov.asd.tac.constellation.graph.node.templates.LoadTemplatePlugin in the Core Graph Node module.
* Moved au.gov.asd.tac.constellation.schema.simpleschema.plugins.ManageTemplatesAction from the Core Simple Schema to au.gov.asd.tac.constellation.graph.node.templates.ManageTemplatesAction in the Core Graph Node module.
* Moved au.gov.asd.tac.constellation.schema.simpleschema.plugins.ManageTemplatesPlugin from the Core Simple Schema to au.gov.asd.tac.constellation.graph.node.templates.ManageTemplatesPlugin in the Core Graph Node module.
* Moved au.gov.asd.tac.constellation.schema.simpleschema.plugins.SaveTemplateAction from the Core Simple Schema to au.gov.asd.tac.constellation.graph.node.templates.SaveTemplateAction in the Core Graph Node module.
* Moved au.gov.asd.tac.constellation.schema.simpleschema.plugins.SaveTemplatePlugin from the Core Simple Schema to au.gov.asd.tac.constellation.graph.node.templates.SaveTemplatePlugin in the Core Graph Node module.
* Moved the base package in Core Simple Schema to au.gov.asd.tac.constellation.schema.simpleschema.
* Removed GraphUtilitiesExtra in the Core Graph Utilities module. The GraphUtilitiesExtra.copyGraphToGraph method to CopyGraphUtilities.
* Removed the experimental WordGraphPlugin in the Core Arrangements module.
* Renamed ExtractFromContent to ExtractFromContentPlugin in Core Data Access View.
* Renamed JMultiChoiceDropDownMenu to JMultiChoiceComboBoxMenu.
* Renamed MergeNodes to MergeNodesPlugin in Core Data Access View.
* Renamed MergeTransactions to MergeTransactionsPlugin in Core Data Access View.
* Renamed PluginParameter.setLabel() to PluginParameter.setName() to be more consistent with the remaining API's.
* Renamed RemoveNodes to RemoveNodesPlugin in Core Data Access View.
* Renamed SchemaAttributeUtilities in the CoreGraphUtilities to AttributeUtilities.
* Renamed SelectAll to SelectAllPlugin in Core Data Access View.
* Renamed SelectTopN to SelectTopNPlugin in Core Data Access View.
* Renamed Utilities to GraphObjectUtilities in the Core File module.
* Renamed the Core Simple Schema module to Core Visual Schema.
    * The new package name is au.gov.asd.tac.constellation.schema.visualschema.
    * Renamed SimpleSchemaFactory to VisualSchemaFactory.
    * Renamed SchemaPluginRegistry to VisualSchemaPluginRegistry.
* Updated all plugin parameters to build the parameter name via PluginParameter.buildId() which is declared as a constant ending in _PARAMETER.
* Updated all plugin parameters to make sure they have a name and description.
* Updated ColorValue so that colors can now only be retrieved using one of the getColorValue(...) methods. This is to ensure that named color values are always used where available.

## 2017-09-01 Changes in September 2017
* Added Auth and supporting classes which allows support for a username/password dialog.
* Added BuildIconGraphAction which allows you to construct a graph showcasing all the icons loaded in memory.
* Added FILE_NAME to ContentConcept in the Core Analytic Schema module.
* Added ObfuscatedPassword and supporting classes which allows for obfuscated passwords.
* Added RecordStoreUtilities.fromCsv() to the Core Graph Module.
* Moved all Graph IO classes to the Graph module and removed the Graph IO module.
* Moved au.gov.asd.tac.constellation.schema.analyticschema.concept.ClusteringConcept to au.gov.asd.tac.constellation.algorithms.clustering.ClusteringConcept.
* Moved au.gov.asd.tac.constellation.schema.analyticschema.concept.SnaConcept to au.gov.asd.tac.constellation.algorithms.sna.SnaConcept in the Core Alogorithms module.
* Moved some centrality plugins to au.gov.asd.tac.constellation.algorithms.sna.centrality in th Core Alogorithms module.
* Moved the social network analysis plugins into a new parent package au.gov.asd.tac.constellation.algorithms.sna in the Core Alogorithms module.
* Removed the Core Graph IO, Charts, Networkx, Remote and Integration Testing modules.
* Renamed HierarchicalAttributeDescription in the Core Algorithms module to HierarchicalStateAttributeDescription.
* Renamed the importexport.delimited.parsers package to importexport.delimited.translaor to accurately reflect what it stores
* Updated all modules to now depend on the minimum version used by NetBeans 8.2.
* Updated the ControlsFx library to version 8.40.13 so that it is compatible Java 8u144.
* Updated to NetBeans 8.2 and Java 8u144.

## 2017-08-01 Changes in August 2017
* Added new module Core Web Server to hold all the classes related to managing local web servers and the Constellation REST API.
* Added ConstellationApiServlet to abstract away the idea of checking a secret for each api call.
* Added TypeServlet to allow operations related to types in the Constellation REST API.
* Added SchemaVertexTypeUtilities.getTypeOrBuildNew() and SchemaTransactionTypeUtilities.getTypeOrBuildNew() to the Core Graph module.
* Added an ImageConcept to the Core Analytic Schema module.
* Fixed a bug with types serialisation. Types are now loaded exactly the way they were saved to the graph file by properly serialising types in VertexTypeIOProvider and TransactionTypeIOProvider.
* Renamed all *Popup classes that implement ContextMenuProvider to end in *ContextMenu.
* Removed AnalyticVertexType from the Core Analytic Schema module.
* Removed ApplicationPreferenceKeys.SCRIPTING_LANGUAGE and ApplicationPreferenceKeys.SCRIPTING_LANGUAGE_DEFAULT from the Core Preferences module as we have made a design choice to use Python as our scripting language.
* Removed the topLevelType attribute from SchemaElementType as it can be calculated.
* Updated AnalyticConcept to build types using SchemaVertexType and SchemaTransactionType.
* Updated SchemaElementType by removing the setIncomplete() method which means that Types are now immutable.
* Updated SchemaVertexType and SchemaTransactionType to be final. Any type object has to be either one of these which simplifies types.
* Updated the Content ContentConcept in the Core Analytic Schema module with more attributes.
* Updated the properties map in SchemaElementType to be a Map of String to String so that serialisation is simplified.

## 2017-07-01 Changes in July 2017
* Added ability to make AnalyticPlugin classes hidden from the Analytic View GUI using the isVisible method.
* Added imperial measurements and conversions to the Haversine class.
* Removed Map View v1 packages.
* Removed all networkx analytic plugins for which there are java replacements.
* Renamed CorePluginRegistry.SELECT_ONE_NEIGHBOUR to CorePluginRegistry.SELECT_PENDANTS in Core Functionality module.
* Renamed FxStateIO to FxStateIOProvider in the Core Table View module.
* Renamed GeoUtilities to GeospatialUtilities.
* Renamed ImportExportRegistry in the Core Import Export module to ImportExportPluginRegistry.
* Renamed ShortestPathsFollowDirectionPlugin to DirectedShortestPathsPlugin.
* Renamed TableStateIO to TableStateIOProvider in the Core Table View module.
* Renamed TableStateTransactionIO to TableStateTransactionIOProvider in the Core Table View module.
* Renamed TableStateVertexIO to TableStateVertexIOProvider in the Core Table View module.
* Renamed package au.gov.asd.constellation.utilities.geo to au.gov.asd.constellation.utilities.geospatial.
* Renamed the ChangeSelection plugin in the Data Access View module to SelectAll.
* Updated the Map View to allow for custom overlays by extending the MapOverlay class. The current info and tool overlays have been converted to this framework.
* Updated the SchemaAttributeUtilities class so that any schema attribute lookup requires a GraphElementType to be specified. This is to avoid dealing with conflicts where a vertex and transaction attribute with the same name exist.

## 2017-06-01 Changes in June 2017
* Fixed a bug detecting graphics card compatibility.
* Moved all *AttributeUpdateProvider classes in 'au.gov.asd.tac.constellation.graph.io.versioning' to 'au.gov.asd.tac.constellation.graph.io.providers.compatability'.
* Removed the @Deprecated tag for classes in *compatibility packages. These classes will need to remain to support backward compatibility and can not be deprecated. To avoid them being used a comment has been added at the top of each class.
* Renamed AttrListAttributeDesciption to AttrListAttributeDesciptionV0 in the Core Graph module.
* Renamed AttrListIOProvider to AttrListIOProviderV0 in the Core Graph IO module.
* Renamed GraphLabel to GraphLabelV0 in the Core Visual Graph module.
* Renamed GraphLabelsAndDecorators to GraphLabelsAndDecoratorsV0 in the Core Visual Graph module.
* Renamed GraphLabelsAndDecoratorsIOProvider to GraphLabelsAndDecoratorsIOProviderV0 in the Core Visual Graph module.
* Renamed LabelsAttributeDescription to LabelsAttributeDescriptionV0 in the Core Visual Graph module.
* Renamed package 'au.gov.asd.tac.constellation.schema.analyticschema.update' to 'au.gov.asd.tac.constellation.schema.analyticschema.compatibility'.
* Renamed package 'au.gov.asd.tac.constellation.schema.simpleschema.update' to 'au.gov.asd.tac.constellation.schema.simpleschema.compatibility'.

## 2017-05-01 Changes in May 2017
* Added RawData.isEmpty().
* Added ScoringAnalyticPlugin and MultiScoringAnalyticPlugin to simplify the addition of analytics to the Analytic View for the case where the analytic simply needs to run a plugin and read the resulting scores.
* Added SelectTopN to the Data Access View module to select the top n nodes based on the transaction count and type.
* Added TextPluginInteraction in the Core Plugin Framework module that will be useful in unit tests.
* Added a MultiScoreResult result type to the Analytic View, as well as aggregators and translators allowing calculations and visualisation. This result type is designed to support any analytic which produces multiple scores as output.
* Added ability to override the foreground icon associated with a type by adding custom icons named the full hierarchy of that type.
* Fixed bugs with the SimpleSchemaV*UpdateProvider classes that caused the wrong icons to be set.
* Moved the DEFAULT_FONT constant from the FontUtilities class in Core Visual Support to ApplicationPreferenceKeys in Core Preferences.
* Removed DebugUtilities from the Core Preferences module as the convention is to use different java.util.logging.Logger levels.
* Removed StringBuilderOutputStream due to its implementation being fundamentally wrong. Using ByteArrayOutputStream and toString(StandardCharsets.UTF_8.name()) is a better approach.
* Removed the representation() method in SchemaElementType and replaced it with the getHierarchy method which is now called by the toString() method in SchemaElementType.
    * When setting types in RecordStore objects, you should no longer use getName() it now returns the name of the type. Given toString() has been overridden, it will return the type's hierarchy as a string.
* Renamed 'getChoices' (and similarly named methods) to 'getOptions' and 'getChecked' to 'getChoices' in the MultiChoiceParameterType class.
* Renamed 'getChoices' (and similarly named methods) to 'getOptions' in the SingleChoiceParameterType class.
* Renamed GenericTopComponent to AbstractTopComponent in the Core Functionality module.
* Renamed package 'au.gov.asd.tac.constellation.core.composites' to 'au.gov.asd.tac.constellation.core.composite'.
* Renamed the VisualConcept.VertexAttribute.UNIQUEID attribute to VisualConcept.VertexAttribute.IDENTIFIER.
* Updated XmlUtilities to optionally handle XML namespaces. Namespaces can be enabled by setting the 'namespaceAware' flag in the constructor for XmlUtilities, and then you can use any method suffixed with 'NS' to take advantage of namespaces.
* Updated all font references in CSS files to use em instead of px so that fonts scale based on the screen resolution.
* Updated how schema element types are stored in a .star file, where instead of the type name, the entire type hierarchy is now used. This is a backwards compatible change.
* Updated the DataAccessPane to factor in the category position along with the plugin's position which ensures that the favourites category is sorted similarly.
* Updated the implementation of the resolveVertex and resolveTransaction methods in the AnalyticSchema class to allow for hierarchical names specified by multiple types separated by the "." character.
* Updated the SimpleSchemaV*UpdateProvider and AnalyticSchemaV*UpdateProvider classes by rearranging the logic so that each update provider is now responsible to manage the update of the concept it uses first. That is for example, SimpleSchema*UpdateProvider handle's the label attribute whilst the AnalyticSchema*UpdateProvider handles updates to the Type attribute.
    * The update provider no longer runs a Complete With Schema
    * The responsibility of the UpdateProviders is to only handle mandatory changes that would otherwise break the graph to said change. It will be up to the graph's schema rules to update all elements on an old graph to be up to date with the latest look and feel. This can be done via Tools -> Complete with Schema or F5.

## 2017-04-01 Changes in April 2017
* Added GeoUtilities.dmsToDd to convert a Degrees-Minute-Seconds formatted geospatial coordinate to Decimal Degrees.
* Added PrimaryKeyUtilities to the Core Graph Utilities module.
* Added SplitNodesPlugin in the Core Data Access module.
* Added a compare graph feature available from the Tools menu.
* Added a new module called Core Integration Testing.
* Moved GeoUtilities from the Core Algorithms module to Core Utilities.
* Moved Tuple to the Core Utilities module.
* Removed the Core Charts module.

## 2017-03-01 Changes in March 2017
* Added DownloadImageUtilities to the Core Utilities module.
* Added HttpsUtilities.readErrorStreamAndThrow in the Core Utilities module to make reporting errors from services consistent.
* Added IntegerObjectAttributeInteraction, LongAttributeInteraction and LongObjectAttributeInteraction classes so that the corresponding attribute types are properly represented in the user interface.
* Added LongObjectAttributeDescription class for completeness.
* Added a "Start Jupyter notebook" capability.
* Added the ability to add plugins to your favourites in the Data Access View.
* Fixed a bug in ConsolidatedDialog that would not deselect correctly.
* Fixed a major performance issue where table view state updates where running on the EDT causing CONSTELLATION to lock up.
* Fixed a serious bug when extending Types and calling VertexTypeAttributeDescription.getString() causing a mis-representation of the type as a string. This was evident when retrieving the type value from a RecordStore. This is a temporary fix until the Types design is reviewed and possibly overhauled.
* Fixed bugs with quick search, advanced find and recent search queries.
* Renamed AbstractCalculatorUtility to AbstractCalculatorUtilities in the Core Scripting module.
* Renamed ClipboardUtility to ClipboardUtilities in the Core Functionality module.
* Renamed ConsolidatedMultipleMatchesDialog to ConsolidatedDialog in the Core Functionality module.
* Renamed DebugUtility to DebugUtilities in the Core Preferences module.
* Renamed GraphUtil to GraphUtilities in the Core Arrangements module.
* Renamed SimpleStringUtilities in the Core Utilities module to StringUtilities and added new functionality.
* Renamed StringUtilities in the Core Utilities module to OldStringUtilities and deprecated it.
* Updated HttpsConnection in the Core Utilities module to use the UTF-8 charset by default for application/json and application/xml content types.
* Updated all references to UTF-8 to use StandardCharsets.UTF_8.
* Renamed ArrangementRegistry to ArrangementPluginRegistry for consistency with other plugin registry classes.
* Added ability to set custom context menu items on the SeletableLabel class used in the Conversation View.
* Moved resources for rendering pill buttons in JavaFX from the conversation view module to the visual support module so they can be used anywhere in the application.
* Renamed AlgorithmsRegistry to AlgorithmPluginRegistry for consistency with other plugin registry classes.
* Renamed references to plugins which extend NetworkxCentralityPlugin, NetworkxImportancePlugin and NetworkxSimilarityPlugin in the AlgorithmPluginRegistry. They are all now appended with '_NX' to differentiate them from java implementations of the same algorithms.

## 2017-02-01 Changes in February 2017
* Added CreateCompositesFromDominantNodesPlugin.
* Added SetDrawFlagPlugin.
* Added SetVibilityAboveThresholdPlugin which can be used to toggle the visiblity status instead.
* Added VisibleAboveThresholdPlugin which will update the draw flags based on whether the count of nodes has exceeded the threshold.
* Added a new module called Core Graph Utilities.
* Added au.gov.asd.tac.constellation.graph.utilities.io.CopyGraphUtilities.
* Added au.gov.asd.tac.constellation.graph.utilities.io.SaveGraphUtilities. The method saveGraphToTemporaryDirectory can be used to save graphs in unit tests.
* Added au.gov.asd.tac.constellation.utilities.io.StringBuilderOutputStream as a alternative to java.io.StringWriter which uses StringBuilder instead of StringBuffer as the underlying implementation. Using StringBuilderOutputStream in say com.fasterxml.jackson.core.JsonFactory.createGenerator() can avoid an OutOfMemoryError.
* Fixed a bug where the graph visibility above threshold was no longer working.
* Fixed a major bug with GraphRecordStoreUtilities.getSelectedVerticesBatches() where the selected nodes were not correctly being added to the Recordstore.
* Moved au.gov.asd.tac.constellation.schema.analyticschema.utilities.SubgraphUtilties to au.gov.asd.tac.constellation.graph.utilities.SubgraphUtilties
* Moved the DeleteAllPlugin and DeleteSelectionPlugin to au.gov.asd.tac.constellation.core.delete.
* Moved the Dim plugins to au.gov.asd.tac.constellation.core.dim in the Core Functionality module.
* Moved the PermanentMergePlugin to au.gov.asd.tac.constellation.core.merge in the Core Functionality module.
* Removed ArrangeInComponentGridPlugin which was a duplicate of ArrangeComponentsInGridPlugin
* Removed ToggleVisibleAboveThresholdPlugin.
* Removed undo() and redo() methods from Graph. GraphCommits are tied to UndoableEdit objects, which are managed by a Swing UndoManager. The manager may call undo() or redo() on these edit objects at will, noting that they run asynchornously, because the EDT may not get a lock on the graph. No Future<?> to wait upon is available for these methods, meaning that it no longer makes sense to interact with the undo manager from the Graph in a programmatic way. Reimplementing these methods is desirable, but would require that something internal to CONSTELLATION code also keeps track of the UndoableEdit objects and has some way of knowing when a given edit has completed its undo/redo method.
* Renamed CorePluginRegistry.COPY_SELECTED_ELEMENTS to CorePluginRegistry.COPY_TO_NEW_GRAPH.
* Renamed HLGraph in the Core Graph module to SGraph.
* Renamed HLReadableGraph in the Core Graph module to SReadableGraph.
* Renamed HLWritableGraph in the Core Graph module to SWritableGraph.
* Renamed composites package in the Core Functionality module to composite.
* Renamed dialogs package in the Core Functionality module to dialog.
* Renamed selection package in Core Arrangements to select.
* Renamed the drawing package in the Core Functionality module to draw.
* Renamed the view package in the Core Functionality module to display.
* Updated the "visual_state" attribute to be an attribute called "camera", of type "camera", that is a GRAPH rather than META attribute. A SchemaUpdateProvider and an AttributeUpdateProvider will automatically make this change when old graphs are loaded. To access the camera attribute, use VisualConcept.Graph.CAMERA.
    * GraphVisualAccess.getX(vertex) already uses this pattern. Note that as with any default values in the VisualDefault class, these x,y,z defaults need to be respected when interacting with the CONSTELLATION's visualisation framework. Higher level plugins/views are free to do whatever makes sense in the absence of x,y,z attibutes (for example arrangements would probably just throw a plugin exception).
    * In the absence of x, y, or z vertex attributes, the default visual values of these attributes are now considered to be equal to the id of the vertex. The correct pattern to get an x, y, z value in the application for visualisation purposes is:
```java
final int xAttribute = VisualConcept.Vertex.X.get(readableGraph);
final float x = xAttribute != Graph.NOT_FOUND ? readableGraph.getFloatValue(vertexId) : VisualDefaults.getDefaultX(vertexId);
```
* Updated GraphVisualAccess explicitly checks for null values of the Camera attribute on the graph and returns VisualDefaults.DEFAULT_CAMERA instead. Note that the Camera shouldn't ever really be null, but as it is possible to set it programmatically (through scripting view), it is considered safer and more convenient to prevent the Visualisation framework itself from having to deal with null cameras.

## 2017-01-01 Changes in January 2017
### Major Changes to Rendering and Event Handling
* COSNTELLATION's visualistion and event handling framework has been reimplemented from the ground up. Performance, stability, correctness, extensability, and the design concept underlying it have all been greatly improved. The user experience should remain mostly the same.
* There is now a separation between three main components that were previously highly coupled: event handling, visualisation, and the graph. The class VisualManager handles all communication between these components.
* VisualProcessor is an abstract base class for any component that provides a visualisation of the graph.
    * GLVisualProcessor is an implementation which models the old open GL renderer.
    * A lot of high level stuff in the GL Renderer has been rewritten as well, but low level stuff like the Vector/Matrix utility classes and the shaders themselves are mostly the same.
    * A simpler lightweight renderer (using Swing for example) could be implemented with relative ease as a subclass of VisualProcessor.
* VisualAccess is an interface that VisualProcessors must now use to access the graph. They are not allowed to have reference to the graph directly.
    * GraphVisualAccess is the default implementation for CONSTELLATION graphs.
    * Theoretically VisualProcessors could visualise other 'graph-like' data structures if they implement VisualAccess
* InteractionEventHandler is an interface for responding to mouse and keyboard gestures (as generated by AWT) on a CONSTELLATION graph.
    * DefaultInteractionEventHandler is the default implementation in CONSTELLATION. It contains code that performs a similar function to the event handling code that used to be in GraphRenderer.
    * VisualAnnotator and VisualInteraction are two interfaces that InteractionEventHandler depends on in order to help it translate gestures into graph actions/visualisations.
    * InteractiveGLVisualProcessor extends GLVisualProcessor to satisfy the VisualAnnotator and VisualInteraction interfaces so that the DefaultInteractionEventHandler can respond to gestures on CONSTELLATION graphs rendered in OpenGL.
* Updates to a VisualProcessor from something like the event handler arrive in the form of VisualChange objects that are sent to a VisualManager (wrapped in VisualOperations).
    * The VisualChange/VisualOperation framework's purpose is to allow components to update the visualisation of a graph efficiently and without having to commit their changes first.
    * The previous GraphRenderer achieved this, but only by being highly coupled with a Graph, and it had lots of logical flaws in its processing model. The new framework is rigorous and stable by separating data from its visualisation.
    * Whilst the event handler is the primary client of this model, other components could be written to take advantage of it.
    * Animation has been rewritten to use this model. Previously they directly maniuplated specific buffers on the GL context, which meant they were highly coupled with the renderer, and could not be used by alterate visualisations.
* A few features have been removed or now behave slightly differently:
    * Animations are now slower on large graphs. They may be able to be optimised in the future but this is considered low priority.
    * Some experimental animations have been removed.
    * You can no longer interact with an animation. This may also be fixed in the future, but it would be require a decent amount of work and is currently low priority.
    * Direction indicators no longer move when the mouse cursor moves (only when you rotate/pan the graph). They should be re-implemented as an animation, which would require a reasonably simple expansion of the animation framework to cater for utilising non-graph visual operations.
    * Lines and nodes are no longer Anti-Aliased. The old method, however, was slow, deprecated and caused artifacts when AA was turned off in the graphics card settings. Graphs now render correctly regardless of the AA settings on the card (although currently enabling AA gives no visual improvement). AAing may be implemented in the future by multisampling the graph texture.
    * On large graphs, the renderer will update slightly later than the rest of the views (when creating nodes etc.). The display always took (at least) this long to update, but other views used to wait. This may be tricky to fix, but is not considered critical.
    * Note that whilst the graph will display that it is busy and prevent interaction during a write lock, the same will not occur during read locks. When a read lock is in progress, events will be queued and the display will not be updated until the read lock is released at which point all queued events will be processed in quick succession so that the event handler 'catches up'. This effect can be seen by running "Experimental > Five Second Read Lock" and then trying to rotate/pan the graph. While this looks bad in this instance, in practice due to the brevity of read locks (but also their potential to be obtained at any point in time during the possession of a write lock), holding up but not terminating the event handler's main loop is the most sensible course of action.
* Fixed all known memory leaks.
* Fixed failing unit tests.
* Fixed some dialogs fading to the back.
* Fixed the Plugin Reporter from causing JavaFX to use up all the memory by removing PluginReporterPane's after they reach MAXIMUM_REPORT_PANES.
* Fixed various bugs.
* Renamed the Country utility class package name from countries to geo.
* Reviewed and finalised icon set.
* Updated the format when saving a graph file to use json objects for the color and the decorations attribute.
    * Double escaping strings in the decorations no longer occur.

## 2016-11-01 Changes in November 2016
* Added JMultiChoiceComboBox class to the Core Utilities module. This class provides a Swing alternative to JavaFX's MultiChoiceComboBox.
* Added MarkerCache class which manages markers on the Map View and can be looked up. This grants the Data Access View the ability to query locations from the Map View.
* Added REST API functionality for RecordStore, static files and icons.
* Added the ability to blend a color with an icon image on creation.
* Added the ability to delete a vertex or transaction from a GraphRecordStore by setting the DELETE_KEY key.
* Fixed the bug in ValueInputPanes which was causing old entry to appear after the text was modified.
    * The recent values combo was updated when a plugin was run, causing its selection model to update, which in turn caused the parameter it was managing recent values for to change right at the point of plugin execution. Temporarily disabling the listener from Recent Values -> Parameter whilst inside the RecentValues updating listener solved the problem.
* Removed FreezeGraphViewPreferencePlugin because it is only a preference change and does not need to be a plugin.
* Renamed get GraphReadMethods.getAttributeLabel() to GraphReadMethods.getAttributeName() for consistency.
* Updated GraphRecordStoreUtilities.getVertices() to return singleton vertices.
* Updated Python REST client which can now be downloaded.
* Updated the majority of built-in icons available to Constellation to have a more flat, iconic style with transparent backgrounds. This makes the core icon set consistent and allows us to make better use of background icons for analytic purposes.

## 2016-10-01 Changes in October 2016
* Added LogAction to show the CONSTELLATION logs to the user.
* Added a "Support Package" menu item under help which zips the CONSTELLATION logs.
* Added a DataAccessState object which currently saves the String parameter values in the "Gobal Parameters" section of the Data Access View when you press Go. This parameters are saved with the graph so that they can be loaded when the graph opens.
* Added an option for RecordStoreServlet to return JSON suitable for Pandas.DataFrame.from_items(). The DataFrame JSON parser is much faster.
* Added missing type qualifiers in GraphRecordStoreUtilities methods.
* Added support back for attr_list attribute type which were found in legacy graphs. The attr_list attribute is converted to the currently supported Graph attributes in SimpleSchemaV1UpdateProvider.
* Modified the WritableGraph.flush() method to take a boolean parameter indicating whether or not GraphChangeListeners will be notified.
    * The use case for this is performing a series of quick updates on the graph that new ReadableGraphs need to be able to see, but that views don't need to respond to - for example animations, event handling, etc. In this case, all views will still respond to the series of changes when commit() is called at the end of the WritableGraph's lifecycle.
* Renamed ParameterIO to ParameterIOUtilities.
* Updated ConstellationDialog to dispose when the hideDialog is called which should free up resources.

## 2016-09-01 Changes in September 2016
* Added a new Map View based on the third party libraries 'Unfolding Maps' and 'Processing'.
    * Maps are rendered using OpenGL through Processing allowing for greater performance and flexibility in what can be drawn.
    * Provides two-way interaction between the graph and the map by making use of the new generic top component framework.
    * Provides ability to add custom maps by extending the MapProvider class.
    * Provides an information overlay which displays the location of the mouse pointer, the current zoom level and a scale bar.
    * Provides ability to place map providers in debug mode by overriding MapProvider.isDebug. This will extend the information overlay to also provide debug information.
    * Provides support for point, line, polygon and multi markers. Shape-based markers can be added to the graph by inserting GeoJSON into the 'Geo.Shape' attribute. You can build GeoJSON for 'Geohash' type nodes using Geohash.getGeoJSON.
    * Currently limited to rendering maps in the Web Mercator projection only.
* Added SchemaElementType class as common base class for the SchemaVertexType and SchemaTransactionType classes.
* Added a REST API. Initially this allows a RecordStore to be added to the active graph.
* Added a ScriptingAction lookup to allow more actions to be added to the Scripting view Actions drop-down menu.
* Fixed various bugs.
* Renamed FeedbackHandler to SupportHandler and changed the menu name from "Feedback..." to "Support"
* Renamed IconManager.getIconObjects to IconManager.getIcons.
* Updated SaveResultsFileWriter.writeRecordStore to be memory efficient which fixes a java.lang.OutOfMemoryError exception and removed SaveResultsFileWriter.write.
* Updated plugins to provide their own arrangements after being run by overriding RecordStoreQueryPlugin.completionArrangement(). By default any RecordStoreQueryPlugin will now finish with an Grid arrangement (which is fast). This is a breaking change which replaces RecordStoreQueryPlugin.arrangeOnCompletion().
* Updated some Alert dialogs to use the Netbeans DialogDisplayer API to enforce modality.
* Updated the internal web server to listen on only the loopback address instead of all addresses, and has a default port instead of being dynamically assigned.

## 2016-08-01 Changes in August 2016
* Added Analytic View v2 framework.
    * Any plugin can now be used as an analytic using the AnalyticPlugin class.
    * Pre-canned questions are now extensible using the AnalyticQuestion class.
    * Both internal and graph visualisations are now extensible using the InternalVisualisation and GraphVisualisation classes respectively.
    * Note that adding new analytics may require the definition of a new result type using the AnalyticResult class, as well as the construction of an AnalyticTranslator class to translate between your result and any visualisations you wish to enable.
* Added IntegerObjectAttributeDescription to handle integer type attributes which could also be null.
* Added generic JavaFxTopComponent and ListeningTopComponent classes to abstract away the creation of new views in CONSTELLATION.
* Assigned names to common threads to assist with debugging.
* Fixed various bugs.
* Fixed various performance enhancements to type lookups.
* Improved the type hierarchy used by the Analytic dominance calculator.
* Moved au.gov.asd.tac.constellation.graph.GraphChangeEvent to au.gov.asd.tac.constellation.graph.monitor.GraphChangeEvent.
* Moved au.gov.asd.tac.constellation.graph.GraphChangeListener to au.gov.asd.tac.constellation.graph.monitor.GraphChangeListener.
* Moved utilities for interacting with schemas from the SchemaFactory class to the SchemaFactoryUtilities class.
* Removed SchemaFactory.getPosition in favour of using a ServiceProvider position annotation.
* Removed the CoreExtFx module.
* Renamed SimpleSchemaFactory.SIMPLE_SCHEMA_NAME to SimpleSchemaFactory.SIMPLE_SCHEMA_ID.
* Renamed arrow icons from UP to ARROW_UP, DOWN to ARROW_DOWN etc.
* Replaced ControlsFX dialogs with JDK's Alert class.
* Updated module short and long descriptions.
* Updated platform to use Java8u92 and Netbeans 8.0.2
* Updated regular expressions used for the Phone Number, Email IPv6 Address, Country and Geohash types.
* Updated various menu item positions.

## 2016-07-01 Changes in July 2016
* Added "Templates" which allow users to save multiple custom visualisations. Templates are essentially constellation files in disguise - however only the graph attributes are saved, no graph element data.
    * Menu items (including icons) have been added to allow easy creation of graphs from templates, saving templates, and management of templates.
    * Management of templates allows templates to be deleted, and also set as the default graph to open when the user selects New Graph (or hits control+N).
* Added HttpsUrlConnection class, a builder pattern to create a HttpsUrlConnection with sensible defaults like using GZIP compression and the user agent string set to 'CONSTELLATION'.
* Added HttpsUtilities class, a utility class to safely retrieve streams from a HttpsUrlConnection.
* Added ObjectAttributeDescriptions class which allows you to quickly define an attribute description for any attribute backed by a class extending Object.
* Added a lot of Javadocs and fixed Javadoc warnings.
* Added org.apache.poi and org.apache.commons.lang as dependencies.
* Added the GraphLabels and Decorators classes for specifying labels and decorators on a graph.
* Added the ability for graph attributes to provide an attribute merger so that you can decide what happens when attributes merge.
* Fixed memory leak in Data Access View.
* Fixed minor bugs relating to attributes, including correctly saving and loading default values for icon attributes, and fixing the previously non-functioning 'set default value' option when creating/modifying a new attribute in the attribute editor.
* Fixed various bugs.
* Moved some preferences from the ApplicationPreferenceKeys to DebuggingPreferenceKeys.
* Removed ability to set default values for visual attributes through preferences in favour of the new template system. The changes have been explained in a what's new entry to avoid user confusion.
* Removed bespoke editors such as "Edit > Labels" in favour of using the streamlined approach provided by the attribute editor.
* Removed the CorePluginGuide module.
* Renamed VisualConcept.TransactionAttribute.COLOR_REFERENCE to VisualConcept.GraphAttribute.TRANSACTION_COLOR_REFERENCE.
* Renamed VisualConcept.VertexAttribute.COLOR_REFERENCE to VisualConcept.GraphAttribute.NODE_COLOR_REFERENCE.
* Renamed all attribute name constants e.g. HierarchicalState.ATTR_NAME renamed to HierarchicalState.ATTRIBUTE_NAME.
* Renamed package au.gov.asd.tac.constellation.attributeeditor.handler to au.gov.asd.tac.constellation.attributeeditor.editors.
* Renamed package au.gov.asd.tac.constellation.attributeeditor.handlerimplementation to au.gov.asd.tac.constellation.attributeeditor.editors.operations.
* Renamed the PreferenceKeys class to ApplicationPreferenceKeys.
* Renamed the Rule class to QualityControlRule.
* Updated StringUtilities class to streamline the serialisation of the reworked visual attributes such as labels and decorators.
* Updated UpdateProvider class to convert from old graphs to new graphs have been included (meaning that the old graph labels and decorators framework still exists in a compatibility package).
* Updated various menu item positions.
* Updated visual attributes (and visual properties previously not exposed as attributes) so they can be used as regular graph attributes.

## 2016-06-01 Changes in June 2016
* Fixed a dormant graph locking bug.
* Fixed various bugs.
* Improved Schema API.
    * The way in which graph schemas are created and controlled within CONSTELLATION has been overhauled, resulting in a more consolidated, and overall simpler API.
    * The most notable change is the introduction of "Schema Concepts" which collect related attributes, vertex types and transaction types into conceptual groups which can then be registered to a schema. Schema concepts will replace "Attribute Providers", "Vertex Type Providers", and "Transaction Type Providers" and are now hierarchical by default, making it easier to extend an existing concept.
    * Other changes include the simplification of "Schema" and "Schema Factory", and new utility classes for interacting with schema concepts, vertex types and transaction types. In addition to this, we also now have a new convention where schemas should no longer extend each other, but rather inheritance should be limited to schema concepts.
* Improved performance of IconManager and added new icons, BAGEL_BLUE, BAGEL_GREY and CHART.
* Improved the Scripting View API by adding support for LINK and EDGE types.
* Moved the WhatsNewProvider from au.gov.asd.tac.constellation.core.tutorial.whatsnew to au.gov.asd.tac.constellation.core.whatsnew.
* Updated CoreImportExport now using Apache Commons CSV to parse CSV files.

## 2016-05-01 Changes in May 2016
* Added a new module called CoreMapView which contains the Map View framework.
* Added versioning to attribute description classes.
* Fixed various bugs.
* Fixed a dormant graphics bug.
* Improved the Rule.executeRule method by forcing it to take a copy of the graph.
* Renamed the ResetPlugin class to ResetViewPlugin.

## 2016-02-01 Change in February 2016
* Added an icons API so that developers can add custom icons programmatically using the IconProvider class.
