# Constellation Changes

## Changes in May 2025
-   Updated help page system so that help pages can now be located within module folder of ext/docs to reduce use of redundant folders.
-   Updated `Generator.getOnlineTOCDirectory()` to no longer pass a parameter. It now just calculates relative to the base directory.

## Changes in April 2025
-   Added multichoice type ahead parameter with infrastructure for future input implementations
-   Multichoice input fields replaced with new multichoice type ahead parameter

## Changes in March 2025
-   Moved creation of `toc.md` for offline help to the netbeans-defined user directory.

## Changes in January 2025
-   Removed classes `VideoCreator` and `VideoFrame` from Core Utilities as they are unused. 

## Changes in December 2024
-   Refactored animation framework to update graph attributes and hold write locks for minimal durations to enable graph interction.
-   Created Color Warp Animation.
-   Enhanced Fly through and Direction Indicators Animation.
-   Created Graph Connection Motion Attribute as a META graph element type.
-   Created Animation setting to disable animations for low power machines.
-   Created SetColorValuesOperation to save space on the undo/redo stack.
-   Modified access of `VertexTypeIOProvider.writeTypeObject()` from public to private, reflecting current use and mirroring related classes and functions.
-   Removed `CompositeStatus.getCompositeStatus()` which was unused.

## Changes in November 2024
-   Removed `CreateVertexTypePlugin` and `CreateTransactionTypePlugin` which were unused.
-   Removed `AnalyticSchemaPluginRegistry` as there were no more plugins after above ones were removed.
-   Removed `ImageConcept` which was unused.
-   Removed `ColorblindUtilities.colorNodes()` which was unused. This behaviour is replicated in `VisualSchemaFactory.VisualSchema` with `applyColorblindVertex()` and `applyColorblindTransaction()`.
-   Removed `ColorblindUtilities.setColorRef()` which only had one use. This behaviour is now directly added to where the function was previously used.
-   Renamed `ColorblindUtilities.calcColorBrightness()` to `ColorblindUtilities.calculateColorBrightness()` for readability.
-   Refactored MenuBaseAction to disable graph dependant menu items when primary graph is ambiguous.

## Changes in October 2024
-   Added ability to pass parameters and selected items on graph to PluginReporter to display via `DefaultPluginInteraction`.
-   Added the ability to set of Table View default columns by implementing `TableDefaultColumns` and using lookup.
-   Moved `BrowseContextMenu` from `au.gov.asd.tac.constellation.graph.visual.contextmenu` to `au.gov.asd.tac.constellation.functionality.browser` to group it with other browser functionality.
-   Removed several functions from `VisualGraphUtilities` passing attribute ids as parameters in favour of using existing complimentary functions using default attributes.
-   Removed `BBoxd` as it was unused.
-   Removed `BBoxf.getGraphBoundingBoxMix()` as it was unused.
-   Renamed `getSelectedElements()` in `VisualGraphUtilities` to `getSelectedVertices` to better reflect what the function does.
-   Updated Table View to now default to primary key columns for Show Default Columns and new graphs.

## Changes in September 2024
-   Removed `AddBlazePlugin` in favour of applying defaults to `AddCustomBlazePlugin` (which was already being used by `AddBlazeAction`).
-   Removed `BlazeUtilities.getHTMLColor()` as this is already available through existing functionality `ConstellationColor.fromJavaColor().getHtmlColor()`.
-   Renamed `DeSelectBlazesAction` and `DeSelectBlazesPlugin` to `DeselectBlazesAction` and `DeselectBlazesPlugin`.
-   Updated `BlazeUtilities.colorDialog` to return just a ConstellationColor.
-   Updated both `SavePreset` functions in `BlazeUtilities` to accept a `ConstellationColor` instead of a `java.awt.Color` based on use.

## Changes in July 2024
-   Moved hashmod package from Core Graph Utilities to Core Import Export Plugins as a more appropriate module.
-   Moved `NoGraphPane` from Layers View to the View Framework so that other views can use it.
-   Updated `NoGraphPane` to take two parameters needed for the abstraction.

## Changes in May 2024
-   Removed `FloatArray.clone()` and replaced with a constructor that takes a `FloatArray` object. 
-   Removed `IntArray.clone()` in favour of constructor that takes a `IntArray` object. 
-   Removed `NamedSelection.clone()` and replaced with a constructor that takes a `NamedSelection` object. 
-   Updated the method GetNodeLocation() to getNodeLocation() in class InteractiveGLVisualProcessor.

## Changes in April 2024
-   Removed `graph` parameter from `PermanentMergeTableModel.initialise` as it was unused.

## Changes in March 2024
-   Removed `dateTimeAttr` parameter from `ClusteringManager.InitDimOrHidePlugin` as it was unused.
-   Removed `datetimeAtrr` parameter from `TimelinePanel.initExclusionState` as it was unused.
-   Renamed `exclusionState()`, `setIsShowingSelectedOnly()`, and `setIsShowingNodeLabels()` in `TimelineState` 
    to `getExclusionState()`, `setShowingSelectedOnly()`, and `setShowingNodeLabels()` to follow naming convention.
-   Updated `TimeExtents` to be a record instead of a class.
-   Updated `VideoFrame` to be a record.

## Changes in February 2024
-   Updated the `constellationapplication/netbeans-runner` docker image
    to `21` to include updates to Netbeans, Azul JDK, and other tools used as part of image.

## Changes in December 2023
-   Updated GraphML error messaging for invalid nodes and edges. Transaction Identifiers added as UUIDs if none are found.

## Changes in July 2023
-   Replaced FindView to express the new interface build in JavaFX and removed the old Swing implementation

## Changes in June 2023
-   Changed LookupPluginsTask to implement Supplier<Map<String, Pair<Integer, List<DataAccessPlugin>>>>.
-   Updated return type of `LookupPluginsTask.get()` from Map<String, List<DataAccessPlugin>> to Map<String, Pair<Integer, List<DataAccessPlugin>>>.
-   Updated `plugins` parameter type in `QueryPhasePane.QueryPhasePane()` from Map<String, List<DataAccessPlugin>> to Map<String, Pair<Integer, List<DataAccessPlugin>>>.
-   Updated `PLUGIN_LOAD` member variable type in `DataAccessPaneState` from Map<String, List<DataAccessPlugin>> to Map<String, Pair<Integer, List<DataAccessPlugin>>>.
-   Updated return type of `DataAccessPaneState.getPlugins()` from Map<String, List<DataAccessPlugin>> to Map<String, Pair<Integer, List<DataAccessPlugin>>>.

## Changes in April 2023
-   Added global thread pool class called ConstellationGlobalThreadPool has been created and can be called to generate a new thread.
-   Removed code that created new thread pool every time a new thread was needed.
## Changes in February 2023	

-   Removed unused class `NestedIncircleDrawing` from Core Arrangement Plugins.
-   Removed several unused methods from `PQTree` in Core Arrangement Plugins.
-   Updated `comprisingIds` parameter type in `CompositeUtilities.makeComposite()` from List to Collection.
-   Updated `results` parameter type in `AnalyticResult.setSelectionOnGraph()` from List to Iterable.
-   Updated `results` parameter type in `AnalyticResult.addAll()` from List to Iterable.
-   Updated `keys` parameter type in `GraphTaxonomy.setArrangeRectangularly()` from Set to Iterable.
-   Updated `childNums` parameter type in `PQTree.addLeaves()` from List to Iterable.
-   Updated `includedVertices` and `excludedLinks` parameters type in `GraphSpectrumEmbedder.matrixFromGraph()` from Set to Collection.
-   Updated `circles` parameter type in `BoundingCircle.enclosingCircle()` from List to Iterable.
-   Updated `verticesToConsider` parameter type in `TaxFromNeighbours.getTaxonomy()` from Set to Iterable.

## Changes in October 2022

-   Moved `ApplicationFontOptionsPanel`, `ApplicationFontOptionsPanelController` and
    `ApplicationFontPreferenceKeys` into `ApplicationOptionsPanel`, `ApplicationOptionsPanelController`
    and `ApplicationPrefrenceKeys` respectively. 

-   Moved `AnaglyphicDisplayPanel`, `AnaglyphicDisplayOptionsPanelController` and
    `AnaglyphicDisplayPreferenceKeys` into `GraphOptionsPanel`, `GraphOptionsPanelController`
    and `GraphPreferenceKeys` respectively. 

## Changes in August 2022

-   Moved `ClusteringConcept`, `HierarchicalStateAttributeDescription` and
    `KTrussStateAttributeDescription` from `CoreAlgorithmPlugins` to
    `CoreAnalyticSchema` so that they could be used in an schema updater to 
    update the spelling of 'color' in some attributes changed.

## Changes in July 2022

-   Updated `RestClient` so that `params` are passed as `List<Tuple<String, String>>`
    rather than `Map<String, String>` - this is to allow multiple parameters with
    the same name to be supplied, which is required for some endpoints. This
    change will break existing classes inheriting from `RestClient` if they are
    not modified to reflect the new parameter type.

-   Removed unused classes `FileChooser` from Core Graph File and `DataAccessResultsDirChooser`
    from Core Data Access View. Classes became unused in lieu of refactoring to utilize the class
    `FileChooser` in Core Utilities.

## Changes in March 2022

-   Added abstract classes `AbstractCachedStringIOProvider` and 
    `AbstractUncachedStringIOProvider` classes to Core Graph Framework
    and '`AbstractGraphLabelsIOProvider` class to Core Visual Schema
    and updated multiple IO Provider classes to implement these classes
    to avoid code duplication.

## Changes in February 2022

-   Removed unused classes `DecoratorUtilities` and `LabelUtilities` from Core 
    Visual Schema.

-   Added a new parameter `tabCaption` in `newTab` public methods in `DataAccessTabPane` class 
	to provide the Step tab caption when required. This is used when the user renames the 
	default caption.
	
## Changes in December 2021

-   Changed the return type of `processVertices` and `processTransactions` methods 
    in `ImportJDBCPlugin` and `ImportDelimitedPlugin` classes to return the number
	of imported rows. Added a new parameter `totalRows` in `displaySummaryAlert`
	method of `ImportJDBCPlugin` class. These allow a more meaningful
    summary status message after importing.

## Changes in November 2021

-   Added `netbeans.exception.report.min.level=900` and
    `netbeans.exception.alert.min.level=900` with both set to `900` to make
    sure all `ERROR` and `FATAL` levels will present a dialog box.

-   Removed unused methods in `SelectableLabel` in `ConversationView`.

-   Renamed methods returning a boolean value to start with "is" or "has". This 
    includes methods in `KTrussState` in `CoreAlgorithmPlugins`, `AnalyticResult`
    in `CoreAnalyticView`; `GraphTaxonomy` & `Scatter3dChoiceParameters` in
    `CoreArrangementPlugins`; `FindRule`, `BasicFindPanel` & `ReplacePanel` in
    `CoreFindView`; `HashmodPanel` in `CoreGraphUtilities`; `ToggleGraphVisibilityAction`
    in `CoreInteractiveGraph`; `LayersViewController` & `BitMaskQuerry` in 
    `CoreLayersView`; `LabelFontsOptionsPanel` & `ConstellationLabelFonts` in 
    `CoreOpenGLDisplay`; `ApplicationOptionsPanel` & `DeveloperOptionsPanel` in
    `CorePreferences`; `ProxyOptionsPanel` in `CoreSecuirty`, `VisualAccess` in 
    `CoreUtilities` and `VisualGraphUtilities` in `CoreVisualGraph`.

-   Update the default configuration to always show errors as a dialog message.

-   Updated the way exceptions are displayed to the user. Exceptions thrown in
    the `DefaultPluginEnvironment` are now presented to the user using the
    class `NotifyDescriptor.Exception`. This presents an exception dialog
    when Constellation is ran from the executable.

-   Updated public constructors in `ConversationProvider` and
    `ConversationContributionProvider` to protected to fix code smell that
    abstract classes should not have public constructors. 

## Changes in October 2021

-   Added `PluginTags` class to hold all tags as constants for `PluginInfo`.

-   Changed the parameter for takeScreenshot in RecentGraphScreenshotUtilities 
    from filename to filepath.

-   Added `isRequired` in `PluginParameter` with a getter and a setter, which 
    can be used to configure the required plugin parameters to mark as `*required`
    in the swagger.

-   Added a file chooser utility to core utilities. This provides a template
    for opening file choosers. It protects against common mistakes that may
    cause issues on different platforms.

-   Removed the verbose printing of garbage collection by default.

-   Removed the `keepAlive` method from `HttpsConnection` as it is not the 
    method to enable HTTP keep-alive for `HttpURLConnection`. `keepAlive` is 
    turned on by default and is controlled using the `http.keepalive` VM
    argument.

-   Renamed `au.gov.asd.tac.constellation.views.dataaccess.state.DataAccessPreferenceKeys`
    to `au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessPreferenceUtilities`

-   Renamed `tableview2` package to `tableview` now that it is the primary table

-   Removed `RecentFilesWelcomePage` and moved functionality between
    `RecentFiles` and `WelcomeViewPane`

-   Renamed `ShortestPathsFollowDirectionAction` to `DirectedShortestPathsAction`
    to align with the plugin it calls.

-   Renamed constants in `DirectedShortestPathsPlugin` with VERTEX in name to 
    have NODE instead.

-   Updated `DataAccessPluginType` from an abstract class to an interface.

-   Updated the access of some of the constants in `DirectedShortestPathsPlugin` 
    to private since they weren't being used elsewhere. 

## Changes in September 2021

-   Added a preference to choose between viewing the help documentation offline,
    or online.

-   Fixed `setRotationMatrix` in `Matrix44d` as it was previously placing 
    rotation values in the wrong value positions.
    
-   Moved `DataAccessPlugin`, `DataAccessPluginCoreType`, `DataAccessPluginRegistry
    and `DataAccessPluginType` from `au.gov.asd.tac.constellation.views.dataaccess`
    to `au.gov.asd.tac.constellation.views.dataaccess.plugins`.

-   Removed JavaHelp and replaced it with the new help system.

-   Removed unused `localized` parameter from the signature of the `locate()`
    method in `ConstellationInstalledFileLocator`.

-   Removed all the unused `*Action.java` classes from
    `au.gov.asd.tac.constellation.plugins.algorithms.sna`.

-   Renamed `PreferenceUtilites` to `PreferenceUtilities` to fix the typo.

-   Removed unused `localized` parameter from the signature of the `locate()`
    method in `ConstellationInstalledFileLocator`.

-   Updated Core Import Export summary text to provide more information. To
    achieve this, added `definitionName` parameter to `ImportDefinition`
    constructor and paneName parameter to `RunPane` constructor. Updated
    displaySummaryAlert` within `ImportDelimitedPlugin` class to take additional
    parameters. The combination of these changes allows a more meaningful
    summary dialog post delimited file import.

-   Updated `processImport` in `ImportController` to a `void` method given the
    return type `List<File>` previously defined was not being used.

-   Updated `setRotationMatrix` in `Matrix44d` as it was previously placing
    rotation values in the wrong value positions.

## Changes in August 2021

-   Added `updateTagsFiltersAvailable`, `updateSelectedTagsCombo`,
    `updateTagsFilters`, `updateAutoNotesDisplayed` and `getTagsFilters` to
    `NotesViewPane` to control the tags filters used in the check combo box
    to update the Auto Notes filters.

-   Removed `ArrangeByGroupPlugin`, `ArrangeByGroupAction`,
    `ArrangeByLayerPlugin`, and `ArrangeByLayerAction` as Arrange By
    Group and Arrange by Layer are superseded by Arrange by Node Attribute.

-   Update `setFilters` in `NotesViewPane` to include the Auto Notes filters.

-   Update to `readObject` and `writeObject` in `NotesViewStateIoProvider`
    to include Plugin Reporter tags in each Auto Note when they are written
    to the graph file.

## Changes in July 2021

-   Update to Quality Control category names and colors in
    `QualityControlEvent` to be easier to understand.

-   Update to `openEdit`, `updateNotesUI`, `createNote` and the constructor in
    `NotesViewPane` to include variables to hold what graph elements are
    selected and applied to the note. Also included a right click context menu
    option for user created notes.

-   Added `updateSelectedElements`, `addToSelectedElements` and
    `removeFromSelectedElements to `NotesViewPane` to allow for modification of
    the selected elements applied to user notes.

## Changes in June 2021

-   Changed `ImportTableColumn.validate` and `importexport/RunPane.validate`
    functions return type from `void` to `boolean`.

-   Removed `PreferenceUtilites.isGraphViewFrozen()` and related files
    as this feature has been superseded by the Pin nodes feature.
    Plugins no longer need a special check to see if the graph is
    frozen or pinned as this is covered by the arrangement framework.

## Changes in May 2021

-   Update `ProjectUpdater` to sort jars in `project.xml` consistently
    between Windows and Linux.

-   Remove the file type being added to dependency jars as it counts
    towards the limited class path length in Windows.

-   Added `displayAlert()` and `displayLargeAlert()` to NotifyDisplayer
    within `CoreUtilities`. They can be used to display alerts without
    and with `TextArea` elements respectively.

-   Updated `CoreImportExportPlugins` more specifically `Delimited` and
    `JDBC` packages. Common code was put into a common class to remove
    duplication. Many classes now extend the parent class for concrete
    implementations.

-   Removed `QualityControlViewPane.getLookup()` as it was not needed.

-   Removed the file type being added to dependency jars as it counts
    towards the limited class path length in Windows.

-   Updated `ProjectUpdater` to sort jars in `project.xml` consistently
    between Windows and Linux.

## Changes in April 2021

-   Added `FourTuple` to the Core Utilities module.

## Changes in March 2021

-   Added `hasLowLevelTag()` to `PluginReport` classes to check whether
    a plugin has a "LOW LEVEL" tag specified.

-   Added Keyboard Shortcut to Scatter Plot View. Shortcut is
    Ctrl-Shift-O

-   Updated all state reading and writing plugins to have a “LOW LEVEL”
    tag.

## Changes in January 2021

-   Moved a number of classes out of
    `au.gov.asd.tac.constellation.plugins.importexport.delimited` and
    `au.gov.asd.tac.constellation.plugins.importexport.jdbc` into the
    base package to help remove duplicate classes.

-   Converted the Tutorial Page into a What’s New page for displaying
    changes in Constellation.

## Changes in November 2020

-   Added `RecentGraphScreenshotUtilities` to manage taking screenshots
    of graphs to be used by the Welcome tab.

-   Added `createReadAttributeObject()` to `GraphReadMethods`.

-   Added `createWriteAttributeObject()` to `GraphWriteMethods`.

-   Added `createReadObject()` and `createWriteObject()` in
    `AttributeDescription`.

-   Added a number of classes to `CoreGraphFramework` to support the
    layers view.

-   Added `ConnectionGlyphStream`, `ConnectionGlyphSteamContext`,
    `NodeGlyphStream`, `NodeGlyphStreamContext` and `GlyphStreamContext`
    classes.

-   Moved Layers View Shortcuts from
    `au.gov.asd.tac.constellation.views.layers.utilities` to
    `au.gov.asd.tac.constellation.views.layers.shortcut`

-   Removed `setCurrentContext()`, `addGlyph()` and `newLine()` from
    `ConnectionLabelBatcher` and `NodeLabelBatcher`.

-   Removed `LayersViewShortcuts` and added associated functionality to
    `LayersViewPane`.

-   Updated `renderTextAsLigatures()`, `newLine()`, and `addGlyph()`
    from `GraphManager` and `GraphManager.GlyphStream` to take
    additional parameter of type GlyphStreamContext

-   Updated `ConnectionLabelBatcher` and `NodeLabelBatcher` to no longer
    implement `GraphManager.GlyphStream`.

-   Updated `setCurrentConnection()` and `nextParallelConnection()` in
    `ConnectionLabelBatcher` to take an extra parameter of type
    `ConnectionGlyphStreamContext`.

-   Updated `ConnectionLabelBatcher.bufferLabel()` to take extra
    parameters.

-   Updated `NodeLabelBatcher.fillTopLabels()` to take different
    parameters.

-   Updated `bufferTopLabel()` and `bufferBottomLabel()` in
    `NodeLabelBatcher` to take an extra parameter of type
    `NodeGlyphStream`.

-   Updated `getTableData()` and `exportToCsv()` in `TableViewUtilities`
    to take an extra parameter of type `Pagination`.

-   Updated `TableViewUtilities.exportToExcel()` to take additional
    parameters.

-   Updated `TableViewUtilities.ExportToExcelFile.writeRecords()` to
    take additional parameter of type `int`.

-   Updated `LAYER_MASK_SELECTED` and `LAYER_MASK` attributes to be of
    type `Long` instead of `Integer`.

-   Updated constructor for `LayersViewStateWriter`.

-   Updated parameters for `setLayers()` and `updateLayers()` in
    `LayersViewPane`.

## Changes in August 2020

-   Updated `DefaultPluginInteraction` and `PluginParameters` to unfocus
    the Ok button from the plugin swing dialog if there is a multi-line
    string parameter so that enter can be used in the parameter.

-   Added a feature to the Histogram View to copy values from selected
    histogram bars using ctrl+c or a right-click context menu.

-   Added Delimited File Importer to work inside of a view. UI
    improvements and various bugfixes with checkbox changes.

-   Added utility methods to `ConstellationColor` which assist with
    getting inverse colors.

## Changes in July 2020

-   Added `AnalyticSchemaV4UpdateProvider` to upgrade
    `SchemaVertexType`s that have changed.

-   Added utility class `NotifyDisplayer` and static method
    `NotifyDisplayer#display` for use when displaying a
    `NotifyDescriptor` message box.

-   Fixed a bug exporting Glyph Textures to the wrong location if the
    folder path had a period.

-   Updated `QualityControlAutoVetter` to improve performance by using a
    `SimpleReadPlugin` internally.

-   Updated the Quality Control View so that it is multi-threaded and no
    longer runs on the EDT.

-   Removed the Attribute Calculator.

## Changes in June 2020

-   Added `LayerConcept` to group all of the layer mask and layer
    visibility attributes together.

-   Moved the creation of `QUERY_NAME_PARAMETER` and
    `DATETIME_RANGE_PARAMETER` within `CoreGlobalParameters` and can be
    accessed by direct reference;
    i.e. `CoreGlobalParameters.QUERY_NAME_PARAMETERS`.

## Changes in May 2020

-   Added feedback for delimiter import.

-   Added basic support for MacOS.

-   Added `ProjectUpdater` which will manage adding dependencies to the
    `project.xml` file.

    -   The `ivy.xml` file is now located at
        `CoreDependencies/src/ivy.xml`.

    -   The `ivysettings.xml` file is now located at
        `ProjectUpdater/src/ivysettings.xml`.

-   Fixed a label rendering bug on MacOS.

-   Fixed a DPI scaling bug on MacOS and Windows.

-   Fixed a bug effecting the histogram scrolling.

-   Fixed a bug preventing v1 graphs from being open.

-   Moved `ImmutableObjectCache`, `IntHashSet` and `Timer` from the
    Graph Framework module to the Utilities module.

-   Removed deprecated methods from the graph API -
    `Graph#getWritableGraphOnEDT`, `Graph#undo`, `Graph#redo` and
    `GraphWriteMethods#addAttribute`.

-   Removed `GraphUtilites` from the Graph Framework module as it was
    unused.

-   Updated ReadableGraph to allow use with the try-with-resources
    pattern.

-   Updated ImportController’s processImport function to return the list
    of files it has imported.

-   Updated parameter types for `OverviewPanel.setExtentPOV()` from
    longs to doubles.

-   Updated the `constellationapplication/netbeans-runner` docker image
    to `11.3.2` to include `python3` so that automation can be done via
    the `build-zip.sh` script in
    `constellation-app/constellation-applications`

## Changes in April 2020

-   Added search feature to Table View Column Selection.

-   Fixed the mouse controls of the Map View to be consistent with the
    graph view.

-   Fixed a bug that caused custom markers to disappear

## Changes in April 2020

-   Fixed the mouse controls of the Map View to be consistent with the
    graph view.

-   Fixed a bug that caused custom markers to disappear

-   Added search feature to Table View Column Selection.

-   Added `functions.sh` to reuse common utility methods. This can be
    used by scripts related to Travis.

-   Added Layers view to the Experimental views tab.

-   Added `RenderablePriority` enum to `GLRenderable` to house the
    constants that sat in that class.

-   Added `VisualPriority` enum to `VisualOperation` to house the
    constants that sat in that class.

-   Added `DoubleAttributeDescription`, `DoubleAttributeInteraction`,
    `DoubleEditorFactory` and `DoubleIOProvider` to support high
    precision numbers in attributes.

-   Added `DoubleObjectAttributeDescription`,
    `DoubleObjectAttributeInteraction`, `DoubleObjectEditorFactory` and
    `DoubleObjectIOProvider` as a nullable alternative to the double
    attribute type.

-   Added `ShortAttributeDescription`, `ShortAttributeInteraction`,
    `ShortEditorFactory` and `ShortIOProvider` to support numbers with
    lower memory usage in attributes.

-   Added `ShortObjectAttributeDescription`,
    `ShortObjectAttributeInteraction`, `ShortObjectEditorFactory` and
    `ShortObjectIOProvider` as a nullable alternative to the short
    attribute type.

-   Added `ByteAttributeDescription`, `ByteAttributeInteraction`,
    `ByteEditorFactory` and `ByteIOProvider` to support numbers with
    lower memory usage in attributes.

-   Added `ByteObjectAttributeDescription`,
    `ByteObjectAttributeInteraction`, `ByteObjectEditorFactory` and
    `ByteObjectIOProvider` as a nullable alternative to the byte
    attribute type.

-   Added `obfuscate()` to `PasswordObfuscator`.

-   Removed the container image to build the NetBeans 8 version of
    Constellation.

-   Removed `getSearchString()`,
    canBeImported()`and`ordering()`from`AttributeDescription\` and any
    implementing classes.

-   Removed `datetime` parameter from `makeDateTimesEven()` in
    `ZonedDateTimeAxis` as this was not needed.

-   Removed `boxBlurF()` and `boxBlurFF()` from `GaussianBlur` as their
    implementation was simple enough to be added straight to where they
    called from.

-   Renamed `getTags()` in `GraphReport` to `getUTags()` to match field
    the function was getting.

-   Renamed `getChildReports()` in `PluginReport` to
    `getUChildReports()` to match field the function was getting.

-   Renamed `equal()` in `NativeAttributeType` to `equalValue()` to
    avoid confusion with `Object.equals()`.

-   Renamed `PasswordKey` to `PasswordSecret` and added `getIV()` to the
    class.

-   Renamed `DefaultPasswordKey` to `DefaultPasswordSecret` to mirror
    above change.

-   Updated the container used to build Constellation on Travis to
    `11.3.1` which fixes the issue of no code coverage being reported in
    SonarQube.

-   Updated the java source detected by SonarQube to check for Java 11.

-   Updated `build.xml` and `.travis\build-zip.sh` with support for
    MacOSX and a temporary hardcoding of version numbers.

-   Updated `deobfuscate()` in `PasswordDeobfuscator` to now return a
    String instead of a CharSequence.

## Changes in March 2020

-   Added `AnalyticSchemaPluginRegistry` to Core Analytic Schema

-   Added new module Core Attribute Calculator to separate it from the
    Scripting View.

-   Added new module Core Named Selections to break it out of Core
    Functionality.

-   Added new module Core Plugin Reporter to separate it from the plugin
    framework.

-   Added new module Core View Framework containing
    `AbstractTopComponent` and other related classes.

-   Added `VisualGraphPluginRegistry` to Core Visual Graph

-   Fixed a logic bug with `GraphRendererDropTarget` preventing graph
    droppers from every running.

-   Moved `AnalyticIconProvider` to
    `au.gov.asd.tac.constellation.utilities.icon`.

-   Moved a number of plugins out of Core Functionality into other
    modules to better reflect their purpose.

-   Moved `AttributeSelectionPanel` to Core Graph Utilities module.

-   Moved `BBoxf` and `BBoxd` to the Core Visual Graph module.

-   Moved `CharacterIconProvider` to
    `au.gov.asd.tac.constellation.utilities.icon`.

-   Moved `ConstellationColor` to
    `au.gov.asd.tac.constellation.utilities.color`.

-   Moved `ConstellationIcon` to
    `au.gov.asd.tac.constellation.utilities.icon`.

-   Moved `ConstellationViewsConcept` to
    `au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept`.

-   Moved `DragAction` to Core Visual Graph module.

-   Moved `SchemaAttribute` to
    `au.gov.asd.tac.constellation.graph.schema.attribute`.

-   Moved `SchemaConcept` to
    `au.gov.asd.tac.constellation.graph.schema.concept`.

-   Moved `SchemaTransactionType` to
    `au.gov.asd.tac.constellation.graph.schema.type`.

-   Moved `SchemaVertexType` to
    `au.gov.asd.tac.constellation.graph.schema.type`.

-   Moved `SimpleGraphOpener` and `SimpleGraphTopComponent` to the Core
    Graph Node module.

-   Moved `UserInterfaceIconProvider` to
    `au.gov.asd.tac.constellation.utilities.icon`.

-   Moved `VisualConcept` to the Core Visual Schema module located at
    `au.gov.asd.tac.constellation.graph.schema.visual.concept`.

-   Moved `VisualGraphOpener` and `VisualGraphTopComponent` to the Core
    Interactive Graph module.

-   Moved `VisualManager` to
    `au.gov.asd.tac.constellation.utilities.visual`.

-   Removed the `build-zip` stage from Travis as it wasn’t being used.

-   Removed the Core Visual Support module by merging it with Core
    Utilities.

-   Renamed base package of Core Algorithms to
    `au.gov.asd.tac.constellation.plugins.algorithms`.

-   Renamed base package of Core Analytic Schema to
    `au.gov.asd.tac.constellation.graph.schema.analytic`.

-   Renamed base package of Core Arrangements to
    `au.gov.asd.tac.constellation.plugins.arrangements`.

-   Renamed base package of Core Import Export to
    `au.gov.asd.tac.constellation.plugins.importexport`.

-   Renamed base package of Core Plugin Framework to
    `au.gov.asd.tac.constellation.plugins`.

-   Renamed base package of Core Visual Schema to
    `au.gov.asd.tac.constellation.graph.schema.visual`.

-   Renamed `Decorators` to `VertexDecorators` and moved to Core Visual
    Schema module.

-   Renamed `InteractivePluginRegsitry` to
    `InteractiveGraphPluginRegistry`.

-   Renamed `IoProgressHandle` to `HandleIoProgress`.

-   Updated Core Analytic Schema with all attribute classes relevant to
    it.

-   Updated Core Visual Schema with all attribute classes relevant to
    it.

-   Updated the Core Web Server module with a complete rewrite regarding
    adding REST services.

-   Updated the `README.MD` instructions to explain the NetBeans 11
    installation workaround.

-   Updated the REST API with a major refactor.

-   Updated the Travis run image to use NetBeans 11.3 and include the
    workaround for NetBeans 11.

## Changes in February 2020

-   Fixed a bug which now ensures that overriding a transaction
    direction using `GraphRecordStoreUtilities.DIRECTED_KEY` persists
    with the Type.

-   Updated JOGL to 2.4.0 to assist in migration to JDK11. The new JOGL
    jars are hosted as third-party dependencies on GitHub until
    available on maven.

-   Renamed `NodeGraphLabelsEditorFactory` to
    `VertexGraphLabelsEditorFactory`.

-   Renamed `SupporPackageAction` to `SupportPackageAction` to fix a
    spelling typo.

## Changes in January 2020

-   Added `LabelFontsOptionsPanel` to allow setting of fonts rendered on
    the graph through the UI.

-   Added `ConstellationLabelFonts` interface to allow programmatic
    specification of default label fonts.

## Changes in December 2019

-   Added method `suppressEvent(boolean, List<>)` to `PluginParameter`
    which allow setting of properties/options without firing change
    events.

-   Moved `CoreUtilities` in the Core Functionality module to
    `PreferenceUtilites` in the Core Utilities module.

-   Renamed `ArcgisMap` Provider to `EsriMapProvider`.

-   Updated `EsriMapProvider` to support both regular tile-based
    services, as well as image export. This can be specified by
    overriding the new `getMapServerType` method.

## Changes in November 2019

-   Remove deprecated jai libraries.

## Changes in October 2019

-   Added `DevOpsNotificationPlugin` to Core Functionality to track
    messages from plugins for developers and administrators attention.
    This is only going to be useful if you have setup a
    `ConstellationLogger` that sends information to a database or
    elastic search.

-   Fixed a bug with the Restful service caused by multiple servlet
    libraries used that created a clash.

## Changes in August 2019

-   Added `BrandingUtilities` to Core Utilities to maintain the
    application name “Constellation”.

    -   You can set the command line argument
        `constellation.environment` with a label and it will appear in
        the title. For instance, this could be used to distinguish
        “Development”, “QA” and “Production” versions.

-   Added `PluginParameters.hasParameter()` to the Core Plugin Framework
    module as a convenient way to check if a parameter exists.

-   Fixed a Null Pointer Exception when selecting Circle arrangements.

-   Fixed the `GitHub` url used by Help -&gt; Submit a Ticket.

-   Removed several unused dependencies, including JOGL, JTS, `OpenCSV,`
    Trove4j, `JScience,` and `XML-APIs`.

-   Renamed `ConstellationLogger.ApplicationStart` to
    `ConstellationLogger.ApplicationStarted,`
    `ConstellationLogger.ApplicationStop` to
    `ConstellationLogger.ApplicationStopped,`
    `ConstellationLogger.PluginStart` to
    `ConstellationLogger.PluginStarted` and
    `ConstellationLogger.PluginStop` to
    `ConstellationLogger.PluginStopped`.

-   Updated several dependencies to the latest versions, including
    Geotools, Jetty, Apache Commons, Jackson, `RSyntaxArea,` Google
    Guava, Apache POI, EJML, Processing, Jython, and `SwingX`.

-   Updated `ConstellationLogger` with new methods `viewStarted,`
    `viewStopped` and `viewInfo` to support logging of Views.

-   Updated `DefaultConstellationLogger` with a VERBOSE flag to switch
    between no-op and logging to standard out.

-   Updated `AbstractTopComponent` to log when the view is opened,
    closed, showing, hidden, activated and deactivated.

## Changes in June 2019

-   Added a `Content.URL` attribute to represent a URL link in the
    `ContentConcept`.

-   Fixed a lot of compile warnings related to Java generics and
    `PluginParameters` usage.

-   Removed `ConstellationSecurityProvider.getPriority` as it duplicated
    functionality of (and conflicted with) the lookup system.

-   Removed `OldStringUtilities` and merged the required methods to
    `StringUtilities`.

## Changes in May 2019

-   Fixed a bug with `SchemaVertexTypeUtilities` and
    `SchemaTransactionTypeUtilities` not respecting overridden types.

-   Removed MODIFIED icon from `UserInterfaceIconProvider`.

-   Removed STARS and CONSTELLATION icons from `AnalyticIconProvider`.

-   Updated CHART icon in `AnalyticIconProvider`.

-   Updated `RestClient` in the Core Utilities module with a minor
    refactor and support for posting bytes.

-   Updated `SchemaFactory` with `getIconSymbol` and `getIconColor`
    methods to allow for more customisable icons. Graph icons will now
    be made up of a symbol on top of a colored square background much
    like how vertices on a graph are represented.

-   Updated the font used by the renderer from Arial Unicode MS to
    Malgun Gothic due to licensing restrictions with the Arial font
    resulting it from no longer being installed on Windows by default.

## Changes in April 2019

-   Renamed `NodeGraphLabelsAttributeDescription,`
    `NodeGraphLabelsAttributeInteraction,` and
    `NodeGraphLabelsIOProvider` to
    `VertexGraphLabelsAttributeDescription,`
    `VertexGraphLabelsAttributeInteraction,` and
    `VertexGraphLabelsIOProvider` for consistency.

-   Updated the `SchemaAttribute.ensure()` method to create the
    attribute if it does not exist by default. This fixes a number of
    plugins that failed if the attribute was not defined.

-   Updated `SimpleEditPlugin.edit()` method to be abstract as it
    doesn’t make sense to have an edit plugin without any editing
    occurring.

## Changes in March 2019

-   Added 23 new country flag icons.

-   Added Arrange by Node Attribute to combine the function of Arrange
    by Group and Arrange by Layer into a single plugin.

-   Added an `updateParameters` method to
    `au.gov.asd.tac.constellation.views.histogram.formats.BinFormatter`
    for Histogram View `BinFormatters` to use.

-   Fixed how `ConstellationIcon` was building and caching icons and
    images resulting in a major performance improvement and reduced
    memory usage.

-   Updated `Ctrl+Backspace` to do nothing so that pressing it in a text
    area on a docked window won’t cause it to minimize.

## Changes in February 2019

-   Added a new interface called `DataAccessPreQueryValidation` to check
    before running Data Access View queries.

## Changes in January 2019

-   Added Enrichment to the `DataAccessPluginCoreType` class.

-   Moved `au.gov.asd.tac.constellation.analyticview` to
    `au.gov.asd.tac.constellation.views.analyticview`.

-   Moved `au.gov.asd.tac.constellation.attributeeditor` to
    `au.gov.asd.tac.constellation.views.attributeeditor`.

-   Moved `au.gov.asd.tac.constellation.conversationview` to
    `au.gov.asd.tac.constellation.views.conversationview`.

-   Moved `au.gov.asd.tac.constellation.core` to
    `au.gov.asd.tac.constellation.functionality`.

-   Moved `au.gov.asd.tac.constellation.core.dependencies` to
    `au.gov.asd.tac.constellation.dependencies`.

-   Moved `au.gov.asd.tac.constellation.dataaccess` to
    `au.gov.asd.tac.constellation.views.dataaccess`.

-   Moved `au.gov.asd.tac.constellation.display` to
    `au.gov.asd.tac.constellation.visual.opengl`.

-   Moved `au.gov.asd.tac.constellation.find` to
    `au.gov.asd.tac.constellation.views.find`.

-   Moved `au.gov.asd.tac.constellation.histogram` to
    `au.gov.asd.tac.constellation.views.histogram`.

-   Moved `au.gov.asd.tac.constellation.interactivegraph` to
    `au.gov.asd.tac.constellation.graph.interaction`.

-   Moved `au.gov.asd.tac.constellation.mapview` to
    `au.gov.asd.tac.constellation.views.mapview`.

-   Moved `au.gov.asd.tac.constellation.qualitycontrol` to
    `au.gov.asd.tac.constellation.views.qualitycontrol`.

-   Moved `au.gov.asd.tac.constellation.scatterplot` to
    `au.gov.asd.tac.constellation.views.scatterplot`.

-   Moved `au.gov.asd.tac.constellation.schemaview` to
    `au.gov.asd.tac.constellation.views.schemaview`.

-   Moved `au.gov.asd.tac.constellation.scripting` to
    `au.gov.asd.tac.constellation.views.scripting`.

-   Moved `au.gov.asd.tac.constellation.tableview` to
    `au.gov.asd.tac.constellation.views.tableview`.

-   Moved `au.gov.asd.tac.constellation.timeline` to
    `au.gov.asd.tac.constellation.views.timeline`.

-   Moved `au.gov.asd.tac.constellation.visualgraph` to
    `au.gov.asd.tac.constellation.graph.visual`.

-   Moved `au.gov.asd.tac.constellation.visualsupport` to
    `au.gov.asd.tac.constellation.visual`.

-   Moved `au.gov.asd.tac.constellation.webview` to
    `au.gov.asd.tac.constellation.views.webview`.

-   Moved private classes that implemented `ParameterValue` to public
    classes to resolve the problem of not being able to set values from
    a script. These include `AnalyticAggregatorParameterValue,`
    `SpatialReferenceParameterValue,` `ElementTypeParameterValue` and
    `GraphAttributeParameterValue`.

-   Renamed `Plugin.getOverriddenPlugin` to
    `Plugin.getOverriddenPlugins` in the Core Plugin Framework module
    which is a breaking change.

-   Updated `GraphWriteMethods` to include a version of `addTransaction`
    that accepts a transaction ID.

-   Updated `PermanentMergeAction` to run in it’s own thread (rather
    than the EDT).

-   Updated structure of Merge Transactions Plugin to allow for more
    merge by types.

## Changes in December 2018

-   Added validation check to `Date-Time` Range Global Parameter in Data
    Access View.

-   Added validation check to numeric parameters in plugins accessed
    from Data Access View.

-   Added plugin `CopyCustomMarkersToGraph` to generate nodes on your
    graph from custom markers in the Map View.

-   Fixed some performance issues in the conversation view.

-   Updated `MarkerCache` with functions to build and style markers from
    the graph, allowing this functionality to be used outside of the Map
    View.

-   Updated `MarkerUtilities` with `generateBoundingBox` method.

-   Updated `ConstellationAbstractMarker` with `getRadius` method.

## Changes in November 2018

-   Moved the `getOverriddenPlugin` method from the `DataAccessPlugin`
    class to the Plugin class. This technically allows the ability for
    any plugin to be overridden. Note that the current implementation
    pattern is to call a plugin from a registry and this would need to
    be modified before plugin calls from `PluginExecutor` could be
    overridden.

-   Removed `MultiScoreResult` fromt he analytic view - all score based
    analytics now use `ScoreResult` and support multiple scores by
    default.

-   Renamed `IconProvider` to `ConstellationIconProvider`

-   Renamed `GlobalCoreParameters` to `CoreGlobalParameters`

-   Renamed all plugin parameter id references from `\*_PARAMETER` to
    `\*_PARAMETER_ID`

## Changes in October 2018

-   Added the `overridenType` property to `SchemaElementType,` and
    removed it from `SchemaVertexType` and `SchemaTransactionType`.

-   Fixed a performance issue in `SchemaElementType.toString()` by pre
    computing the hierarchy on initialisation.
    `SchemaElementType.getHierachy()` is a slow method and was being
    called too many times. The performance improvement has made it about
    1.7 times faster to load, save and work with graphs.

-   Fixed the views that have not been upgraded to the new View
    Framework to have the font size applied to the Top Component on
    load.

-   Updated `SchemaElementType.isSubTypeOf` to take overridden types
    into account.

## Changes in September 2018

-   Added a new plugin to open a url in a browser called
    `OpenInBrowserPlugin` that is available in Core Functionality.

-   Added a new plugin to send to an email client called
    `SendToEmailClientPlugin` that is available in Core Functionality.

-   Renamed `SchemaAttribute.getFormatContext` to
    `SchemaAttribute.getFormat`.

-   Updated `PlaceholderUtilities` with support for collapsing
    placeholders using the standard graph API.

## Changes in August 2018

-   Added functionality to cache icons.

-   Fixed a bug in the Analytic schema factory which was not correctly
    applying the schema rules to complete the vertex.

-   Fixed a memory leak introduced by the `FPSRenderable` class.

-   Fixed a performance issue with the Table View by moving work off the
    EDT.

## Changes in July 2018

-   Added `AnalyticConcept.VertexType.USER_NAME`.

-   Added Subdivision enum containing country subdivisions (currently
    incomplete).

-   Added `TemporalFormattingUtilties` to the Core Utilities module.

-   Added an `IpAddressUtilities` class to the Core Utilities module.

-   Fixed a performance issue with `JDropDownMenu` in the Core Utilities
    module moving the `actionPerformed` work into its own thread.

-   Fixed the spelling of public constants and comments that incorrectly
    spelt separator in various modules.

-   Renamed `AnalyticConcept.VertexType.HOSTNAME` to
    `AnalyticConcept.VertexType.HOST_NAME`.

-   Renamed `GeospatialUtilities` to Distance and moved
    `HaversineUtilities` to `Distance.Haversine`.

-   Renamed `ShapeUtilities` to Shape, `MgrsUtilities` to Mgrs and
    `GeohashUtilities` to Geohash.

-   Updated Country enum to align with the latest version of ISO 3166-1.

-   Updated the copyright to Apache Version 2.0 with the Australian
    Signals Directorate being the License owner.

## Changes in June 2018

-   Added a `RestClient.postWithJson()` to allow you to post your own
    json string in the Core Utilities module.

-   Added plugins `CreateVertexTypePlugin` and
    `CreateTransactionTypePlugin` to allow REST clients to create custom
    types.

-   Fixed `PluginParameters` to use `ColorValue` instead of Color. This
    caused a cascade of fixes in other classes.

-   Fixed a bug with the spanning tree algorithm which was preventing it
    from creating a nraduis attribute.

-   Renamed `ColorValue` to `ConstellationColor` to make it clearer what
    it should be used for and to avoid conflicts with external classes.

-   Renamed `TemporalFormatting.DATE_TIME_FORMATTER` to
    `TemporalFormatting.UTC_DATE_TIME_FORMATTER` and
    `TemporalFormatting.DATE_TIME_WITH_MILLISECONDS_FORMATTER` to
    `TemporalFormatting.UTC_DATE_TIME_WITH_MILLISECONDS_FORMATTER` in
    the Core Utilities module. These `DateTimeFormatter` constants are
    now build using the `DateTimeFormatterBuilder` ensuring they convert
    dates to UTC correctly.

-   Updated the support package generation to ignore the heapdump file
    as it is likely to be several gigabytes in size.

## Changes in May 2018

-   Added `FilterPlugin` to the Core Data Access View module.

-   Added `PasswordParameterType` into the Core Plugin Framework module.
    This change has also removed the capability of `StringParameterType`
    to support passwords.

-   Added strict `DateTimeFormatter's` based on
    `DateTimeFormatter.ISO_INSTANT` to the `TemporalFormatting` class in
    Core Utilities.

-   Fixed a performance issue by reducing the amount of “Find State”
    saves made to the graph as it’s causing graph write lock contention
    for a trivial GUI state that will be already covered when you run a
    search, switch graphs or save the graph.

-   Fixed an issue handling invalid Glyphs.

-   Fixed some performance issues with the Map View.

-   Removed deprecated methods being `PluginGraph.executePluginLater,`
    `PluginGraph.executePluginNow` and `PluginGraph.updateGraph`.

-   Renamed Plugin.id to `Plugin.ID`

-   Updated JOGL to 2.3.2 and Processing to 3.3.6 to resolve `OpenGL`
    issues with the Map view.

-   Updated default node and transaction colors in order to ensure
    overlay colors stand out more.

-   Updated default node z2 value.

-   Updated password obfuscation to use a service lookup to get the key.
    To use it implement `PasswordKey` in the Core Security module.

## Changes in April 2018

-   Added New Nebula into th `Experimental->Tools` menu

-   Added an FPS counter `OpenGL` renderer that can be enabled from the
    Debug preference tab.

-   Added new `MatrixUtilities` class for constructing useful matrices
    from a graph.

-   Added simple icon shaders for rendering basic icons on the graph.

-   Removed the REST API endpoints /forward (forwarding a request to
    another HTTP server), /resources (offering embedded web resources),
    and /static (serving resources from a specified local directory)
    have been removed.

-   Renamed `ApplicationPreferenceKeys.DEFAULT_FREEZE_GRAPH_VIEW` to
    `ApplicationPreferenceKeys.FREEZE_GRAPH_VIEW_DEFAULT` in the Core
    Preferences module.

-   Renamed `SharedDrawable.getIconShader` to
    `SharedDrawable.getVertexIconShader` to differentiate it from
    `SharedDrawable.getSimpleIconShader`.

-   Updated Core Web Server code so the workings out of the web servlets
    into separate `\*Impl.java` classes. This makes the workings
    transport independent, in preparation for adding a `non-HTTP`
    transport. In theory, there should be no change in functionality.

-   Updated the Core Web Server module to add a filesystem REST
    transport.

-   Updated the constructor for Decorators to expect its parameters in a
    clockwise order: north-west, north-east, south-east, south-west.

-   Updated the various `\*Preference` panels to follow and more of an
    MVC pattern.

## Changes in March 2018

-   Added `FactColorTranslator` and `FactToSizeTranslator` to the Core
    Analytics View module.

-   Added `FirstAnalyticPlugin` to the Core Analytics View module.

-   Added `FirstFactAggregator` in the Core Analytics View module.

-   Added a new attribute called `isLabel` to `SchemaAttribute` in the
    Core Graph Module with the intention of allowing the schema to
    decide if the attribute should appear as a `GraphLabel` on a vertex
    or transaction.

-   Added a new `isLabel` and `isDecorator` attributes to
    `SchemaAttribute` with the intention of allowing the schema to
    decide if the attribute should appear as a `GraphLabel` or Decorator
    on a vertex or transaction.

-   Added equivalent method to `SchemaAttribute` allowing you to compare
    with an Attribute object.

-   Renamed `ClusterAggregator` to `FirstClusterAggregator` in the Core
    Analytics View module.

-   Renamed `MultiScoreResult.getUniqueNames` to
    `MultiScoreResult.getUniqueScoreNames` in the Core Analytics View
    module.

-   Renamed `MultiScoringAnalyticPlugin` to `MultiScoreAnalyticPlugin`
    in the Core Analytics View module.

-   Renamed `PluginRegistry.getPluginNames` to
    `PluginRegistry.getPluginClassNames`.

-   Renamed `ScoringAnalyticPlugin` to `ScoreAnalyticPlugin` in the Core
    Analytics View module.

-   Renamed `getLabel` to `getName` and `getType` to `GetAttributeType`
    for the Attribute class.

-   Renamed the get\_pos(g) method to get\_nx\_pos(g) in the
    constellation\_client provided with the REST API.

-   `SchemaFactory.ensureAttribute` now takes a boolean specifying
    whether the attribute should be created even if it is not registered
    to that schema. Similarly, `SchemaAttribute.ensure` provides this
    option.

## Changes in February 2018

-   Fixed memory leaks with the `ListeningTopComponent` and
    `TimelineTopComponent`.

-   Renamed `COLOR_BLAZE` in the `CorePluginRegistry` to
    `ADD_CUSTOM_BLAZE`.

-   Updated entries within the Tools and Experimental menu.

-   Updated `ListeningTopComponent` to allow for the update and removal
    of change handlers.

    -   `addAttributeChangeHandler` was renamed to
        `addAttributeValueChangeHandler`.

    -   `addGlobalChangeHandler,` `addStructureChangeHandler,`
        `addAttributeCountChangeHandler` and
        `addAttributeValueChangeHandler` now return the Monitor objects
        associated with that handler.

    -   `updateGlobalChangeHandler,` `updateStructureChangeHandler,`
        `updateAttributeCountChangeHandler` and
        `updateAttributeValueChangeHandler` have been added to allow
        modification to the behaviour of a handler.

    -   `removeGlobalChangeHandler,` `removeStructureChangeHandler,`
        `removeAttributeCountChangeHandler` and
        `removeAttributeValueChangeHandler` have been added to allow
        removal of a handler.

    -   `removeIgnoredEvent` has been added to allow removal of an
        ignored event.

## Changes in December 2017

-   Added forward slash (/) to the list of special characters to escape
    in `LuceneUtilities` in the Core Utilities module.

-   Removed extra `FindStatePlugin` calls from the `ColorCriteriaPanel`
    which will help reduce unnecessary write locks on the graph and
    reduce overall threads being forked.

## Changes in November 2017

-   Added `MultiplexityAnalytic` and `WeightAnalytic` plugins and
    analytics.

-   Added `SnaConcept.Transaction.MULTIPLEXITY` to the Core Algorithms
    module.

-   Renamed `SnaConcept.GRAPH.GRAPH_DENSITY` to
    `SnaConcept.GRAPH.DENSITY` in the Core Algorithms module.

## Changes in October 2017

-   Added `GraphNodePluginRegistry` in the Core Graph Node module.

-   Added `JSingleChoiceComboBoxMenu` as a single choice alternative to
    `JMultiChoiceComboBoxMenu`.

-   Added `LuceneUtilities` to the Core Utilities module.

-   Added `SeparatorConstants` to the Core Utilities module.

-   Added more attributes to `ContentConcept` in the Core Analytic
    Schema.

-   Fixed a bug where the singleton type was not being used when loading
    a graph.

-   Fixed a bug with the `WorkflowQueryPlugin` which was removing the
    graph attributes after a batched run completed.

-   Moved `NewDefaultSchemaGraphAction` from the Core Simple Schema
    module to the Core Graph Node module.

-   Moved `NewExperimentalSchemaGraphAction` from the Core Simple Schema
    module to the Core Graph Node module.

-   Moved `NewSchemaGraphAction` from the Core Simple Schema module to
    the Core Graph Node module.

-   Moved `au.gov.asd.tac.constellation.algorithms.geospatial.Geohash`
    in the Core Utilities module to
    `au.gov.asd.tac.constellation.utilities.geospatial.GeohashUtilities`

-   Moved `au.gov.asd.tac.constellation.algorithms.geospatial.Haversine`
    in the Core Utilities module to
    `au.gov.asd.tac.constellation.utilities.geospatial.HaversineUtilities`

-   Moved
    `au.gov.asd.tac.constellation.core.opener.SimpleGraphTopComponent`
    to
    `au.gov.asd.tac.constellation.core.visual.SimpleGraphTopComponent`.

-   Moved `au.gov.asd.tac.constellation.core.visual.SaveAsAction` in the
    Core Functionality module to
    `au.gov.asd.tac.constellation.core.save.SaveAsAction`.

-   Moved `au.gov.asd.tac.constellation.graph.file.GraphOpener` in the
    Core Graph File to
    `au.gov.asd.tac.constellation.graph.file.opener.GraphOpener`.

-   Moved
    `au.gov.asd.tac.constellation.graph.file.autosave.AutosaveUtilities`
    in the Core Graph File module to
    `au.gov.asd.tac.constellation.graph.file.save.AutosaveUtilities`.

-   Moved
    `au.gov.asd.tac.constellation.schema.simpleschema.plugins.LoadTemplatePlugin`
    from the Core Simple Schema to
    `au.gov.asd.tac.constellation.graph.node.templates.LoadTemplatePlugin`
    in the Core Graph Node module.

-   Moved
    `au.gov.asd.tac.constellation.schema.simpleschema.plugins.ManageTemplatesAction`
    from the Core Simple Schema to
    `au.gov.asd.tac.constellation.graph.node.templates.ManageTemplatesAction`
    in the Core Graph Node module.

-   Moved
    `au.gov.asd.tac.constellation.schema.simpleschema.plugins.ManageTemplatesPlugin`
    from the Core Simple Schema to
    `au.gov.asd.tac.constellation.graph.node.templates.ManageTemplatesPlugin`
    in the Core Graph Node module.

-   Moved
    `au.gov.asd.tac.constellation.schema.simpleschema.plugins.SaveTemplateAction`
    from the Core Simple Schema to
    `au.gov.asd.tac.constellation.graph.node.templates.SaveTemplateAction`
    in the Core Graph Node module.

-   Moved
    `au.gov.asd.tac.constellation.schema.simpleschema.plugins.SaveTemplatePlugin`
    from the Core Simple Schema to
    `au.gov.asd.tac.constellation.graph.node.templates.SaveTemplatePlugin`
    in the Core Graph Node module.

-   Moved the base package in Core Simple Schema to
    `au.gov.asd.tac.constellation.schema.simpleschema`.

-   Removed `GraphUtilitiesExtra` in the Core Graph Utilities module.
    The `GraphUtilitiesExtra.copyGraphToGraph` method to
    `CopyGraphUtilities`.

-   Removed the experimental `WordGraphPlugin` in the Core Arrangements
    module.

-   Renamed `ExtractFromContent` to `ExtractFromContentPlugin` in Core
    Data Access View.

-   Renamed `JMultiChoiceDropDownMenu` to `JMultiChoiceComboBoxMenu`.

-   Renamed `MergeNodes` to `MergeNodesPlugin` in Core Data Access View.

-   Renamed `MergeTransactions` to `MergeTransactionsPlugin` in Core
    Data Access View.

-   Renamed `PluginParameter.setLabel()` to `PluginParameter.setName()`
    to be more consistent with the remaining `API's`.

-   Renamed `RemoveNodes` to `RemoveNodesPlugin` in Core Data Access
    View.

-   Renamed `SchemaAttributeUtilities` in the `CoreGraphUtilities` to
    `AttributeUtilities`.

-   Renamed `SelectAll` to `SelectAllPlugin` in Core Data Access View.

-   Renamed `SelectTopN` to `SelectTopNPlugin` in Core Data Access View.

-   Renamed Utilities to `GraphObjectUtilities` in the Core File module.

-   Renamed the Core Simple Schema module to Core Visual Schema.

    -   The new package name is
        `au.gov.asd.tac.constellation.schema.visualschema`.

-   Renamed `SimpleSchemaFactory` to `VisualSchemaFactory`.

-   Renamed `SchemaPluginRegistry` to `VisualSchemaPluginRegistry`.

-   Updated all plugin parameters to build the parameter name via
    `PluginParameter.buildId()` which is declared as a constant ending
    in `_PARAMETER`.

-   Updated all plugin parameters to make sure they have a name and
    description.

-   Updated `ColorValue` so that colors can now only be retrieved using
    one of the `getColorValue(...)` methods. This is to ensure that
    named color values are always used where available.

## Changes in September 2017

-   Added Auth and supporting classes which allows support for a
    username/password dialog.

-   Added `BuildIconGraphAction` which allows you to construct a graph
    showcasing all the icons loaded in memory.

-   Added `FILE_NAME` to `ContentConcept` in the Core Analytic Schema
    module.

-   Added `ObfuscatedPassword` and supporting classes which allows for
    obfuscated passwords.

-   Added `RecordStoreUtilities.fromCsv()` to the Core Graph Module.

-   Moved all Graph IO classes to the Graph module and removed the Graph
    IO module.

-   Moved
    `au.gov.asd.tac.constellation.schema.analyticschema.concept.ClusteringConcept`
    to
    `au.gov.asd.tac.constellation.algorithms.clustering.ClusteringConcept`.

-   Moved
    `au.gov.asd.tac.constellation.schema.analyticschema.concept.SnaConcept`
    to `au.gov.asd.tac.constellation.algorithms.sna.SnaConcept` in the
    Core Alogorithms module.

-   Moved some centrality plugins to
    `au.gov.asd.tac.constellation.algorithms.sna.centrality` in th Core
    Alogorithms module.

-   Moved the social network analysis plugins into a new parent package
    `au.gov.asd.tac.constellation.algorithms.sna` in the Core
    Alogorithms module.

-   Removed the Core Graph IO, Charts, Networkx, Remote and Integration
    Testing modules.

-   Renamed `HierarchicalAttributeDescription` in the Core Algorithms
    module to `HierarchicalStateAttributeDescription`.

-   Renamed the importexport.delimited.parsers package to
    importexport.delimited.translaor to accurately reflect what it
    stores

-   Updated all modules to now depend on the minimum version used by
    `NetBeans` 8.2.

-   Updated the `ControlsFx` library to version 8.40.13 so that it is
    compatible Java 8u144.

-   Updated to `NetBeans` 8.2 and Java 8u144.

## Changes in August 2017

-   Added new module Core Web Server to hold all the classes related to
    managing local web servers and the Constellation REST API.

-   Added `ConstellationApiServlet` to abstract away the idea of
    checking a secret for each api call.

-   Added `TypeServlet` to allow operations related to types in the
    Constellation REST API.

-   Added `SchemaVertexTypeUtilities.getTypeOrBuildNew()` and
    `SchemaTransactionTypeUtilities.getTypeOrBuildNew()` to the Core
    Graph module.

-   Added an `ImageConcept` to the Core Analytic Schema module.

-   Fixed a bug with types serialisation. Types are now loaded exactly
    the way they were saved to the graph file by properly serialising
    types in `VertexTypeIOProvider` and `TransactionTypeIOProvider`.

-   Renamed all `*Popup` classes that implement `ContextMenuProvider` to
    end in `*ContextMenu`.

-   Removed `AnalyticVertexType` from the Core Analytic Schema module.

-   Removed `ApplicationPreferenceKeys.SCRIPTING_LANGUAGE` and
    `ApplicationPreferenceKeys.SCRIPTING_LANGUAGE_DEFAULT` from the Core
    Preferences module as we have made a design choice to use Python as
    our scripting language.

-   Removed the `topLevelType` attribute from `SchemaElementType` as it
    can be calculated.

-   Updated `AnalyticConcept` to build types using `SchemaVertexType`
    and `SchemaTransactionType`.

-   Updated `SchemaElementType` by removing the `setIncomplete()` method
    which means that Types are now immutable.

-   Updated `SchemaVertexType` and `SchemaTransactionType` to be final.
    Any type object has to be either one of these which simplifies
    types.

-   Updated the Content `ContentConcept` in the Core Analytic Schema
    module with more attributes.

-   Updated the properties map in `SchemaElementType` to be a Map of
    String to String so that serialisation is simplified.

## Changes in July 2017

-   Added ability to make `AnalyticPlugin` classes hidden from the
    Analytic View GUI using the `isVisible` method.

-   Added imperial measurements and conversions to the Haversine class.

-   Removed Map View v1 packages.

-   Removed all networkx analytic plugins for which there are java
    replacements.

-   Renamed `CorePluginRegistry.SELECT_ONE_NEIGHBOUR` to
    `CorePluginRegistry.SELECT_PENDANTS` in Core Functionality module.

-   Renamed `FxStateIO` to `FxStateIOProvider` in the Core Table View
    module.

-   Renamed `GeoUtilities` to `GeospatialUtilities`.

-   Renamed `ImportExportRegistry` in the Core Import Export module to
    `ImportExportPluginRegistry`.

-   Renamed `ShortestPathsFollowDirectionPlugin` to
    `DirectedShortestPathsPlugin`.

-   Renamed `TableStateIO` to `TableStateIOProvider` in the Core Table
    View module.

-   Renamed `TableStateTransactionIO` to
    `TableStateTransactionIOProvider` in the Core Table View module.

-   Renamed `TableStateVertexIO` to `TableStateVertexIOProvider` in the
    Core Table View module.

-   Renamed package `au.gov.asd.constellation.utilities.geo` to
    `au.gov.asd.constellation.utilities.geospatial`.

-   Renamed the `ChangeSelection` plugin in the Data Access View module
    to `SelectAll`.

-   Updated the Map View to allow for custom overlays by extending the
    `MapOverlay` class. The current info and tool overlays have been
    converted to this framework.

-   Updated the `SchemaAttributeUtilities` class so that any schema
    attribute lookup requires a `GraphElementType` to be specified. This
    is to avoid dealing with conflicts where a vertex and transaction
    attribute with the same name exist.

## Changes in June 2017

-   Fixed a bug detecting graphics card compatibility.

-   Moved all `*AttributeUpdateProvider` classes in
    ‘au.gov.asd.tac.constellation.graph.io.versioning’ to
    ‘au.gov.asd.tac.constellation.graph.io.providers.compatability’.

-   Removed the `@Deprecated` tag for classes in \*compatibility
    packages. These classes will need to remain to support backward
    compatibility and can not be deprecated. To avoid them being used a
    comment has been added at the top of each class.

-   Renamed `AttrListAttributeDesciption` to
    `AttrListAttributeDesciptionV0` in the Core Graph module.

-   Renamed `AttrListIOProvider` to `AttrListIOProviderV0` in the Core
    Graph IO module.

-   Renamed `GraphLabel` to `GraphLabelV0` in the Core Visual Graph
    module.

-   Renamed `GraphLabelsAndDecorators` to `GraphLabelsAndDecoratorsV0`
    in the Core Visual Graph module.

-   Renamed `GraphLabelsAndDecoratorsIOProvider` to
    `GraphLabelsAndDecoratorsIOProviderV0` in the Core Visual Graph
    module.

-   Renamed `LabelsAttributeDescription` to
    `LabelsAttributeDescriptionV0` in the Core Visual Graph module.

-   Renamed package
    ‘au.gov.asd.tac.constellation.schema.analyticschema.update’ to
    ‘au.gov.asd.tac.constellation.schema.analyticschema.compatibility’.

-   Renamed package
    ‘au.gov.asd.tac.constellation.schema.simpleschema.update’ to
    ‘au.gov.asd.tac.constellation.schema.simpleschema.compatibility’.

## Changes in May 2017

-   Added `RawData.isEmpty()`.

-   Added `ScoringAnalyticPlugin` and `MultiScoringAnalyticPlugin` to
    simplify the addition of analytics to the Analytic View for the case
    where the analytic simply needs to run a plugin and read the
    resulting scores.

-   Added `SelectTopN` to the Data Access View module to select the top
    n nodes based on the transaction count and type.

-   Added `TextPluginInteraction` in the Core Plugin Framework module
    that will be useful in unit tests.

-   Added a `MultiScoreResult` result type to the Analytic View, as well
    as aggregators and translators allowing calculations and
    visualisation. This result type is designed to support any analytic
    which produces multiple scores as output.

-   Added ability to override the foreground icon associated with a type
    by adding custom icons named the full hierarchy of that type.

-   Fixed bugs with the `SimpleSchemaV*UpdateProvider` classes that
    caused the wrong icons to be set.

-   Moved the `DEFAULT_FONT` constant from the `FontUtilities` class in
    Core Visual Support to `ApplicationPreferenceKeys` in Core
    Preferences.

-   Removed `DebugUtilities` from the Core Preferences module as the
    convention is to use different `java.util.logging.Logger` levels.

-   Removed `StringBuilderOutputStream` due to its implementation being
    fundamentally wrong. Using `ByteArrayOutputStream` and
    `toString(StandardCharsets.UTF_8.name())` is a better approach.

-   Removed the representation() method in `SchemaElementType` and
    replaced it with the `getHierarchy` method which is now called by
    the `toString()` method in `SchemaElementType`.

    -   When setting types in `RecordStore` objects, you should no
        longer use `getName()` it now returns the name of the type.
        Given `toString()` has been overridden, it will return the
        type’s hierarchy as a string.

-   Renamed `'getChoices'` (and similarly named methods) to
    `'getOptions'` and `'getChecked'` to `'getChoices'` in the
    `MultiChoiceParameterType` class.

-   Renamed `'getChoices'` (and similarly named methods) to
    `'getOptions'` in the `SingleChoiceParameterType` class.

-   Renamed `GenericTopComponent` to `AbstractTopComponent` in the Core
    Functionality module.

-   Renamed package ‘au.gov.asd.tac.constellation.core.composites’ to
    ‘au.gov.asd.tac.constellation.core.composite’.

-   Renamed the `VisualConcept.VertexAttribute.UNIQUEID` attribute to
    `VisualConcept.VertexAttribute.IDENTIFIER`.

-   Updated `XmlUtilities` to optionally handle XML namespaces.
    Namespaces can be enabled by setting the `'namespaceAware'` flag in
    the constructor for `XmlUtilities,` and then you can use any method
    suffixed with ‘NS’ to take advantage of namespaces.

-   Updated all font references in CSS files to use em instead of px so
    that fonts scale based on the screen resolution.

-   Updated how schema element types are stored in a .star file, where
    instead of the type name, the entire type hierarchy is now used.
    This is a backwards compatible change.

-   Updated the `DataAccessPane` to factor in the category position
    along with the plugin’s position which ensures that the favourites
    category is sorted similarly.

-   Updated the implementation of the `resolveVertex` and
    `resolveTransaction` methods in the `AnalyticSchema` class to allow
    for hierarchical names specified by multiple types separated by the
    “.” character.

-   Updated the `SimpleSchemaV*UpdateProvider` and
    `AnalyticSchemaV*UpdateProvider` classes by rearranging the logic so
    that each update provider is now responsible to manage the update of
    the concept it uses first. That is for example,
    `SimpleSchema*UpdateProvider` handle’s the label attribute whilst
    the `AnalyticSchema*UpdateProvider` handles updates to the Type
    attribute.

    -   The update provider no longer runs a Complete With Schema

    -   The responsibility of the `UpdateProviders` is to only handle
        mandatory changes that would otherwise break the graph to said
        change. It will be up to the graph’s schema rules to update all
        elements on an old graph to be up to date with the latest look
        and feel. This can be done via Tools -&gt; Complete with Schema
        or F5.

## Changes in April 2017

-   Added `GeoUtilities.dmsToDd` to convert a `Degrees-Minute-Seconds`
    formatted geospatial coordinate to Decimal Degrees.

-   Added `PrimaryKeyUtilities` to the Core Graph Utilities module.

-   Added `SplitNodesPlugin` in the Core Data Access module.

-   Added a compare graph feature available from the Tools menu.

-   Added a new module called Core Integration Testing.

-   Moved `GeoUtilities` from the Core Algorithms module to Core
    Utilities.

-   Moved Tuple to the Core Utilities module.

-   Removed the Core Charts module.

## Changes in March 2017

-   Added `DownloadImageUtilities` to the Core Utilities module.

-   Added `HttpsUtilities.readErrorStreamAndThrow` in the Core Utilities
    module to make reporting errors from services consistent.

-   Added `IntegerObjectAttributeInteraction,`
    `LongAttributeInteraction` and `LongObjectAttributeInteraction`
    classes so that the corresponding attribute types are properly
    represented in the user interface.

-   Added `LongObjectAttributeDescription` class for completeness.

-   Added a “Start Jupyter notebook” capability.

-   Added the ability to add plugins to your favourites in the Data
    Access View.

-   Fixed a bug in `ConsolidatedDialog` that would not deselect
    correctly.

-   Fixed a major performance issue where table view state updates where
    running on the EDT causing CONSTELLATION to lock up.

-   Fixed a serious bug when extending Types and calling
    `VertexTypeAttributeDescription.getString()` causing a
    mis-representation of the type as a string. This was evident when
    retrieving the type value from a `RecordStore`. This is a temporary
    fix until the Types design is reviewed and possibly overhauled.

-   Fixed bugs with quick search, advanced find and recent search
    queries.

-   Renamed `AbstractCalculatorUtility` to `AbstractCalculatorUtilities`
    in the Core Scripting module.

-   Renamed `ClipboardUtility` to `ClipboardUtilities` in the Core
    Functionality module.

-   Renamed `ConsolidatedMultipleMatchesDialog` to `ConsolidatedDialog`
    in the Core Functionality module.

-   Renamed `DebugUtility` to `DebugUtilities` in the Core Preferences
    module.

-   Renamed `GraphUtil` to `GraphUtilities` in the Core Arrangements
    module.

-   Renamed `SimpleStringUtilities` in the Core Utilities module to
    `StringUtilities` and added new functionality.

-   Renamed `StringUtilities` in the Core Utilities module to
    `OldStringUtilities` and deprecated it.

-   Updated `HttpsConnection` in the Core Utilities module to use the
    UTF-8 charset by default for application/json and application/xml
    content types.

-   Updated all references to UTF-8 to use `StandardCharsets.UTF_8`.

-   Renamed `ArrangementRegistry` to `ArrangementPluginRegistry` for
    consistency with other plugin registry classes.

-   Added ability to set custom context menu items on the
    `SeletableLabel` class used in the Conversation View.

-   Moved resources for rendering pill buttons in `JavaFX` from the
    conversation view module to the visual support module so they can be
    used anywhere in the application.

-   Renamed `AlgorithmsRegistry` to `AlgorithmPluginRegistry` for
    consistency with other plugin registry classes.

-   Renamed references to plugins which extend
    `NetworkxCentralityPlugin,` `NetworkxImportancePlugin` and
    `NetworkxSimilarityPlugin` in the `AlgorithmPluginRegistry`. They
    are all now appended with `'_NX'` to differentiate them from java
    implementations of the same algorithms.

## Changes in February 2017

-   Added `CreateCompositesFromDominantNodesPlugin`.

-   Added `SetDrawFlagPlugin`.

-   Added `SetVibilityAboveThresholdPlugin` which can be used to toggle
    the visiblity status instead.

-   Added `VisibleAboveThresholdPlugin` which will update the draw flags
    based on whether the count of nodes has exceeded the threshold.

-   Added a new module called Core Graph Utilities.

-   Added
    `au.gov.asd.tac.constellation.graph.utilities.io.CopyGraphUtilities`.

-   Added
    `au.gov.asd.tac.constellation.graph.utilities.io.SaveGraphUtilities`.
    The method `saveGraphToTemporaryDirectory` can be used to save
    graphs in unit tests.

-   Added
    `au.gov.asd.tac.constellation.utilities.io.StringBuilderOutputStream`
    as a alternative to `java.io.StringWriter` which uses
    `StringBuilder` instead of `StringBuffer` as the underlying
    implementation. Using `StringBuilderOutputStream` in say
    `com.fasterxml.jackson.core.JsonFactory.createGenerator()` can avoid
    an `OutOfMemoryError`.

-   Fixed a bug where the graph visibility above threshold was no longer
    working.

-   Fixed a major bug with
    `GraphRecordStoreUtilities.getSelectedVerticesBatches()` where the
    selected nodes were not correctly being added to the Recordstore.

-   Moved
    `au.gov.asd.tac.constellation.schema.analyticschema.utilities.SubgraphUtilties`
    to `au.gov.asd.tac.constellation.graph.utilities.SubgraphUtilties`

-   Moved the `DeleteAllPlugin` and `DeleteSelectionPlugin` to
    `au.gov.asd.tac.constellation.core.delete`.

-   Moved the Dim plugins to `au.gov.asd.tac.constellation.core.dim` in
    the Core Functionality module.

-   Moved the `PermanentMergePlugin` to
    `au.gov.asd.tac.constellation.core.merge` in the Core Functionality
    module.

-   Removed `ArrangeInComponentGridPlugin` which was a duplicate of
    `ArrangeComponentsInGridPlugin`

-   Removed `ToggleVisibleAboveThresholdPlugin`.

-   Removed undo() and redo() methods from Graph. `GraphCommits` are
    tied to `UndoableEdit` objects, which are managed by a Swing
    `UndoManager`. The manager may call undo() or redo() on these edit
    objects at will, noting that they run asynchornously, because the
    EDT may not get a lock on the graph. No Future&lt;?&gt; to wait upon
    is available for these methods, meaning that it no longer makes
    sense to interact with the undo manager from the Graph in a
    programmatic way. Reimplementing these methods is desirable, but
    would require that something internal to CONSTELLATION code also
    keeps track of the `UndoableEdit` objects and has some way of
    knowing when a given edit has completed its undo/redo method.

-   Renamed `CorePluginRegistry.COPY_SELECTED_ELEMENTS` to
    `CorePluginRegistry.COPY_TO_NEW_GRAPH`.

-   Renamed `HLGraph` in the Core Graph module to `SGraph`.

-   Renamed `HLReadableGraph` in the Core Graph module to
    `SReadableGraph`.

-   Renamed `HLWritableGraph` in the Core Graph module to
    `SWritableGraph`.

-   Renamed composites package in the Core Functionality module to
    composite.

-   Renamed dialogs package in the Core Functionality module to dialog.

-   Renamed selection package in Core Arrangements to select.

-   Renamed the drawing package in the Core Functionality module to
    draw.

-   Renamed the view package in the Core Functionality module to
    display.

-   Updated the “visual\_state” attribute to be an attribute called
    “camera”, of type “camera”, that is a GRAPH rather than META
    attribute. A `SchemaUpdateProvider` and an `AttributeUpdateProvider`
    will automatically make this change when old graphs are loaded. To
    access the camera attribute, use `VisualConcept.Graph.CAMERA`.

    -   `GraphVisualAccess.getX(vertex)` already uses this pattern. Note
        that as with any default values in the `VisualDefault` class,
        these x,y,z defaults need to be respected when interacting with
        the `CONSTELLATION's` visualisation framework. Higher level
        plugins/views are free to do whatever makes sense in the absence
        of x,y,z attibutes (for example arrangements would probably just
        throw a plugin exception).

    -   In the absence of x, y, or z vertex attributes, the default
        visual values of these attributes are now considered to be equal
        to the id of the vertex. The correct pattern to get an x, y, z
        value in the application for visualisation purposes is:

    ``` java
    final int `xAttribute` = `VisualConcept.Vertex.X.get(readableGraph);`
    final float x = `xAttribute` != `Graph.NOT_FOUND` ? `readableGraph.getFloatValue(vertexId)` : `VisualDefaults.getDefaultX(vertexId);`
    ```

-   Updated `GraphVisualAccess` explicitly checks for null values of the
    Camera attribute on the graph and returns
    `VisualDefaults.DEFAULT_CAMERA` instead. Note that the Camera
    shouldn’t ever really be null, but as it is possible to set it
    programmatically (through scripting view), it is considered safer
    and more convenient to prevent the Visualisation framework itself
    from having to deal with null cameras.

## Changes in January 2017

### Major Changes to Rendering and Event Handling

-   `COSNTELLATION's` visualistion and event handling framework has been
    reimplemented from the ground up. Performance, stability,
    correctness, extensability, and the design concept underlying it
    have all been greatly improved. The user experience should remain
    mostly the same.

-   There is now a separation between three main components that were
    previously highly coupled: event handling, visualisation, and the
    graph. The class `VisualManager` handles all communication between
    these components.

-   `VisualProcessor` is an abstract base class for any component that
    provides a visualisation of the graph.

    -   `GLVisualProcessor` is an implementation which models the old
        open GL renderer.

    -   A lot of high level stuff in the GL Renderer has been rewritten
        as well, but low level stuff like the `Vector/Matrix` utility
        classes and the shaders themselves are mostly the same.

    -   A simpler lightweight renderer (using Swing for example) could
        be implemented with relative ease as a subclass of
        `VisualProcessor`.

-   `VisualAccess` is an interface that `VisualProcessors` must now use
    to access the graph. They are not allowed to have reference to the
    graph directly.

    -   `GraphVisualAccess` is the default implementation for
        CONSTELLATION graphs.

    -   Theoretically `VisualProcessors` could visualise other
        ‘graph-like’ data structures if they implement `VisualAccess`

-   `InteractionEventHandler` is an interface for responding to mouse
    and keyboard gestures (as generated by AWT) on a CONSTELLATION
    graph.

    -   `DefaultInteractionEventHandler` is the default implementation
        in CONSTELLATION. It contains code that performs a similar
        function to the event handling code that used to be in
        `GraphRenderer`.

    -   `VisualAnnotator` and `VisualInteraction` are two interfaces
        that `InteractionEventHandler` depends on in order to help it
        translate gestures into graph actions/visualisations.

    -   `InteractiveGLVisualProcessor` extends `GLVisualProcessor` to
        satisfy the `VisualAnnotator` and `VisualInteraction` interfaces
        so that the `DefaultInteractionEventHandler` can respond to
        gestures on CONSTELLATION graphs rendered in `OpenGL`.

-   Updates to a `VisualProcessor` from something like the event handler
    arrive in the form of `VisualChange` objects that are sent to a
    `VisualManager` (wrapped in `VisualOperations)`.

    -   The `VisualChange/VisualOperation` framework’s purpose is to
        allow components to update the visualisation of a graph
        efficiently and without having to commit their changes first.

    -   The previous `GraphRenderer` achieved this, but only by being
        highly coupled with a Graph, and it had lots of logical flaws in
        its processing model. The new framework is rigorous and stable
        by separating data from its visualisation.

    -   Whilst the event handler is the primary client of this model,
        other components could be written to take advantage of it.

    -   Animation has been rewritten to use this model. Previously they
        directly maniuplated specific buffers on the GL context, which
        meant they were highly coupled with the renderer, and could not
        be used by alterate visualisations.

-   A few features have been removed or now behave slightly differently:

    -   Animations are now slower on large graphs. They may be able to
        be optimised in the future but this is considered low priority.

    -   Some experimental animations have been removed.

    -   You can no longer interact with an animation. This may also be
        fixed in the future, but it would be require a decent amount of
        work and is currently low priority.

    -   Direction indicators no longer move when the mouse cursor moves
        (only when you rotate/pan the graph). They should be
        re-implemented as an animation, which would require a reasonably
        simple expansion of the animation framework to cater for
        utilising non-graph visual operations.

    -   Lines and nodes are no longer `Anti-Aliased`. The old method,
        however, was slow, deprecated and caused artifacts when AA was
        turned off in the graphics card settings. Graphs now render
        correctly regardless of the AA settings on the card (although
        currently enabling AA gives no visual improvement). `AAing` may
        be implemented in the future by multisampling the graph texture.

    -   On large graphs, the renderer will update slightly later than
        the rest of the views (when creating nodes etc.). The display
        always took (at least) this long to update, but other views used
        to wait. This may be tricky to fix, but is not considered
        critical.

    -   Note that whilst the graph will display that it is busy and
        prevent interaction during a write lock, the same will not occur
        during read locks. When a read lock is in progress, events will
        be queued and the display will not be updated until the read
        lock is released at which point all queued events will be
        processed in quick succession so that the event handler ‘catches
        up’. This effect can be seen by running “Experimental &gt; Five
        Second Read Lock” and then trying to rotate/pan the graph. While
        this looks bad in this instance, in practice due to the brevity
        of read locks (but also their potential to be obtained at any
        point in time during the possession of a write lock), holding up
        but not terminating the event handler’s main loop is the most
        sensible course of action.

-   Fixed all known memory leaks.

-   Fixed failing unit tests.

-   Fixed some dialogs fading to the back.

-   Fixed the Plugin Reporter from causing `JavaFX` to use up all the
    memory by removing `PluginReporterPane's` after they reach
    `MAXIMUM_REPORT_PANES`.

-   Fixed various bugs.

-   Renamed the Country utility class package name from countries to
    geo.

-   Reviewed and finalised icon set.

-   Updated the format when saving a graph file to use json objects for
    the color and the decorations attribute.

-   Double escaping strings in the decorations no longer occur.

## Changes in November 2016

-   Added `JMultiChoiceComboBox` class to the Core Utilities module.
    This class provides a Swing alternative to `JavaFX's`
    `MultiChoiceComboBox`.

-   Added `MarkerCache` class which manages markers on the Map View and
    can be looked up. This grants the Data Access View the ability to
    query locations from the Map View.

-   Added REST API functionality for `RecordStore,` static files and
    icons.

-   Added the ability to blend a color with an icon image on creation.

-   Added the ability to delete a vertex or transaction from a
    `GraphRecordStore` by setting the `DELETE_KEY` key.

-   Fixed the bug in `ValueInputPanes` which was causing old entry to
    appear after the text was modified.

    -   The recent values combo was updated when a plugin was run,
        causing its selection model to update, which in turn caused the
        parameter it was managing recent values for to change right at
        the point of plugin execution. Temporarily disabling the
        listener from Recent Values -&gt; Parameter whilst inside the
        `RecentValues` updating listener solved the problem.

-   Removed `FreezeGraphViewPreferencePlugin` because it is only a
    preference change and does not need to be a plugin.

-   Renamed get `GraphReadMethods.getAttributeLabel()` to
    `GraphReadMethods.getAttributeName()` for consistency.

-   Updated `GraphRecordStoreUtilities.getVertices()` to return
    singleton vertices.

-   Updated Python REST client which can now be downloaded.

-   Updated the majority of built-in icons available to Constellation to
    have a more flat, iconic style with transparent backgrounds. This
    makes the core icon set consistent and allows us to make better use
    of background icons for analytic purposes.

## Changes in October 2016

-   Added `LogAction` to show the CONSTELLATION logs to the user.

-   Added a “Support Package” menu item under help which zips the
    CONSTELLATION logs.

-   Added a `DataAccessState` object which currently saves the String
    parameter values in the “Gobal Parameters” section of the Data
    Access View when you press Go. This parameters are saved with the
    graph so that they can be loaded when the graph opens.

-   Added an option for `RecordStoreServlet` to return JSON suitable for
    `Pandas.DataFrame.from_items()`. The `DataFrame` JSON parser is much
    faster.

-   Added missing type qualifiers in `GraphRecordStoreUtilities`
    methods.

-   Added support back for attr\_list attribute type which were found in
    legacy graphs. The attr\_list attribute is converted to the
    currently supported Graph attributes in
    `SimpleSchemaV1UpdateProvider`.

-   Modified the `WritableGraph.flush()` method to take a boolean
    parameter indicating whether or not `GraphChangeListeners` will be
    notified.

    -   The use case for this is performing a series of quick updates on
        the graph that new `ReadableGraphs` need to be able to see, but
        that views don’t need to respond to - for example animations,
        event handling, etc. In this case, all views will still respond
        to the series of changes when commit() is called at the end of
        the `WritableGraph's` lifecycle.

-   Renamed `ParameterIO` to `ParameterIOUtilities`.

-   Updated `ConstellationDialog` to dispose when the `hideDialog` is
    called which should free up resources.

## Changes in September 2016

-   Added a new Map View based on the third party libraries `'Unfolding`
    Maps’ and `'Processing'`.

    -   Maps are rendered using `OpenGL` through Processing allowing for
        greater performance and flexibility in what can be drawn.

    -   Provides two-way interaction between the graph and the map by
        making use of the new generic top component framework.

    -   Provides ability to add custom maps by extending the
        `MapProvider` class.

    -   Provides an information overlay which displays the location of
        the mouse pointer, the current zoom level and a scale bar.

    -   Provides ability to place map providers in debug mode by
        overriding `MapProvider.isDebug`. This will extend the
        information overlay to also provide debug information.

    -   Provides support for point, line, polygon and multi markers.
        Shape-based markers can be added to the graph by inserting
        `GeoJSON` into the `'Geo.Shape'` attribute. You can build
        `GeoJSON` for `'Geohash'` type nodes using `Geohash.getGeoJSON`.

    -   Currently limited to rendering maps in the Web Mercator
        projection only.

-   Added `SchemaElementType` class as common base class for the
    `SchemaVertexType` and `SchemaTransactionType` classes.

-   Added a REST API. Initially this allows a `RecordStore` to be added
    to the active graph.

-   Added a `ScriptingAction` lookup to allow more actions to be added
    to the Scripting view Actions drop-down menu.

-   Fixed various bugs.

-   Renamed `FeedbackHandler` to `SupportHandler` and changed the menu
    name from “Feedback…” to “Support”

-   Renamed `IconManager.getIconObjects` to `IconManager.getIcons`.

-   Updated `SaveResultsFileWriter.writeRecordStore` to be memory
    efficient which fixes a `java.lang.OutOfMemoryError` exception and
    removed `SaveResultsFileWriter.write`.

-   Updated plugins to provide their own arrangements after being run by
    overriding `RecordStoreQueryPlugin.completionArrangement()`. By
    default any `RecordStoreQueryPlugin` will now finish with an Grid
    arrangement (which is fast). This is a breaking change which
    replaces `RecordStoreQueryPlugin.arrangeOnCompletion()`.

-   Updated some Alert dialogs to use the Netbeans `DialogDisplayer` API
    to enforce modality.

-   Updated the internal web server to listen on only the loopback
    address instead of all addresses, and has a default port instead of
    being dynamically assigned.

## Changes in August 2016

-   Added Analytic View v2 framework.

    -   Any plugin can now be used as an analytic using the
        `AnalyticPlugin` class.

    -   Pre-canned questions are now extensible using the
        `AnalyticQuestion` class.

    -   Both internal and graph visualisations are now extensible using
        the `InternalVisualisation` and `GraphVisualisation` classes
        respectively.

    -   Note that adding new analytics may require the definition of a
        new result type using the `AnalyticResult` class, as well as the
        construction of an `AnalyticTranslator` class to translate
        between your result and any visualisations you wish to enable.

-   Added `IntegerObjectAttributeDescription` to handle integer type
    attributes which could also be null.

-   Added generic `JavaFxTopComponent` and `ListeningTopComponent`
    classes to abstract away the creation of new views in CONSTELLATION.

-   Assigned names to common threads to assist with debugging.

-   Fixed various bugs.

-   Fixed various performance enhancements to type lookups.

-   Improved the type hierarchy used by the Analytic dominance
    calculator.

-   Moved `au.gov.asd.tac.constellation.graph.GraphChangeEvent` to
    `au.gov.asd.tac.constellation.graph.monitor.GraphChangeEvent`.

-   Moved `au.gov.asd.tac.constellation.graph.GraphChangeListener` to
    `au.gov.asd.tac.constellation.graph.monitor.GraphChangeListener`.

-   Moved utilities for interacting with schemas from the
    `SchemaFactory` class to the `SchemaFactoryUtilities` class.

-   Removed `SchemaFactory.getPosition` in favour of using a
    `ServiceProvider` position annotation.

-   Removed the `CoreExtFx` module.

-   Renamed `SimpleSchemaFactory.SIMPLE_SCHEMA_NAME` to
    `SimpleSchemaFactory.SIMPLE_SCHEMA_ID`.

-   Renamed arrow icons from UP to `ARROW_UP,` DOWN to `ARROW_DOWN` etc.

-   Replaced `ControlsFX` dialogs with `JDK's` Alert class.

-   Updated module short and long descriptions.

-   Updated platform to use Java8u92 and Netbeans 8.0.2

-   Updated regular expressions used for the Phone Number, Email `IPv6`
    Address, Country and Geohash types.

-   Updated various menu item positions.

## Changes in July 2016

-   Added “Templates” which allow users to save multiple custom
    visualisations. Templates are essentially constellation files in
    disguise - however only the graph attributes are saved, no graph
    element data.

    -   Menu items (including icons) have been added to allow easy
        creation of graphs from templates, saving templates, and
        management of templates.

    -   Management of templates allows templates to be deleted, and also
        set as the default graph to open when the user selects New Graph
        (or hits `control+N)`.

-   Added `HttpsUrlConnection` class, a builder pattern to create a
    `HttpsUrlConnection` with sensible defaults like using GZIP
    compression and the user agent string set to ‘CONSTELLATION’.

-   Added `HttpsUtilities` class, a utility class to safely retrieve
    streams from a `HttpsUrlConnection`.

-   Added `ObjectAttributeDescriptions` class which allows you to
    quickly define an attribute description for any attribute backed by
    a class extending Object.

-   Added a lot of Javadocs and fixed Javadoc warnings.

-   Added org.apache.poi and org.apache.commons.lang as dependencies.

-   Added the `GraphLabels` and Decorators classes for specifying labels
    and decorators on a graph.

-   Added the ability for graph attributes to provide an attribute
    merger so that you can decide what happens when attributes merge.

-   Fixed memory leak in Data Access View.

-   Fixed minor bugs relating to attributes, including correctly saving
    and loading default values for icon attributes, and fixing the
    previously non-functioning ‘set default value’ option when
    creating/modifying a new attribute in the attribute editor.

-   Fixed various bugs.

-   Moved some preferences from the `ApplicationPreferenceKeys` to
    `DebuggingPreferenceKeys`.

-   Removed ability to set default values for visual attributes through
    preferences in favour of the new template system. The changes have
    been explained in a what’s new entry to avoid user confusion.

-   Removed bespoke editors such as “Edit &gt; Labels” in favour of
    using the streamlined approach provided by the attribute editor.

-   Removed the `CorePluginGuide` module.

-   Renamed `VisualConcept.TransactionAttribute.COLOR_REFERENCE` to
    `VisualConcept.GraphAttribute.TRANSACTION_COLOR_REFERENCE`.

-   Renamed `VisualConcept.VertexAttribute.COLOR_REFERENCE` to
    `VisualConcept.GraphAttribute.NODE_COLOR_REFERENCE`.

-   Renamed all attribute name constants
    e.g. `HierarchicalState.ATTR_NAME` renamed to
    `HierarchicalState.ATTRIBUTE_NAME`.

-   Renamed package
    `au.gov.asd.tac.constellation.attributeeditor.handler` to
    `au.gov.asd.tac.constellation.attributeeditor.editors`.

-   Renamed package
    `au.gov.asd.tac.constellation.attributeeditor.handlerimplementation`
    to
    `au.gov.asd.tac.constellation.attributeeditor.editors.operations`.

-   Renamed the `PreferenceKeys` class to `ApplicationPreferenceKeys`.

-   Renamed the Rule class to `QualityControlRule`.

-   Updated `StringUtilities` class to streamline the serialisation of
    the reworked visual attributes such as labels and decorators.

-   Updated `UpdateProvider` class to convert from old graphs to new
    graphs have been included (meaning that the old graph labels and
    decorators framework still exists in a compatibility package).

-   Updated various menu item positions.

-   Updated visual attributes (and visual properties previously not
    exposed as attributes) so they can be used as regular graph
    attributes.

## Changes in June 2016

-   Fixed a dormant graph locking bug.

-   Fixed various bugs.

-   Improved Schema API.

    -   The way in which graph schemas are created and controlled within
        CONSTELLATION has been overhauled, resulting in a more
        consolidated, and overall simpler API.

    -   The most notable change is the introduction of “Schema Concepts”
        which collect related attributes, vertex types and transaction
        types into conceptual groups which can then be registered to a
        schema. Schema concepts will replace “Attribute Providers”,
        “Vertex Type Providers”, and “Transaction Type Providers” and
        are now hierarchical by default, making it easier to extend an
        existing concept.

    -   Other changes include the simplification of “Schema” and “Schema
        Factory”, and new utility classes for interacting with schema
        concepts, vertex types and transaction types. In addition to
        this, we also now have a new convention where schemas should no
        longer extend each other, but rather inheritance should be
        limited to schema concepts.

-   Improved performance of `IconManager` and added new icons,
    `BAGEL_BLUE,` `BAGEL_GREY` and CHART.

-   Improved the Scripting View API by adding support for LINK and EDGE
    types.

-   Moved the `WhatsNewProvider` from
    `au.gov.asd.tac.constellation.core.tutorial.whatsnew` to
    `au.gov.asd.tac.constellation.core.whatsnew`.

-   Updated `CoreImportExport` now using Apache Commons CSV to parse CSV
    files.

## Changes in May 2016

-   Added a new module called `CoreMapView` which contains the Map View
    framework.

-   Added versioning to attribute description classes.

-   Fixed various bugs.

-   Fixed a dormant graphics bug.

-   Improved the `Rule.executeRule` method by forcing it to take a copy
    of the graph.

-   Renamed the `ResetPlugin` class to `ResetViewPlugin`.

## Change in February 2016


-   Added an icons API so that developers can add custom icons
    programmatically using the `IconProvider` class.
