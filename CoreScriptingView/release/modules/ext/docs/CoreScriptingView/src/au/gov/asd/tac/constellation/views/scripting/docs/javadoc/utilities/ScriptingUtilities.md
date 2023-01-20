<div>

JavaScript is disabled on your browser.

</div>

<div class="topNav">

<span id="navbar.top"></span>

<div class="skipNav">

[Skip navigation links](#skip.navbar.top "Skip navigation links")

</div>

<span id="navbar.top.firstrow"></span>

-   [Overview](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/overview-summary.md)
-   [Package](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/utilities/package-summary.md)
-   Class
-   [Tree](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/utilities/package-tree.md)
-   [Deprecated](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/deprecated-list.md)
-   [Index](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/index-all.md)
-   [Help](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/help-doc.md)

<div class="aboutLanguage">

au.gov.asd.tac.constellation.views.scripting 1.0

</div>

</div>

<div class="subNav">

-   Prev Class
-   Next Class

<!-- -->

-   [Frames](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/utilities/ScriptingUtilities.md)
-   [No Frames](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/utilities/ScriptingUtilities.md)

<!-- -->

-   [All Classes](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/allclasses-noframe.md)

<div>

</div>

<div>

-   Summary: 
-   Nested | 
-   Field | 
-   [Constr](#constructor.summary) | 
-   [Method](#method.summary)

<!-- -->

-   Detail: 
-   Field | 
-   [Constr](#constructor.detail) | 
-   [Method](#method.detail)

</div>

<span id="skip.navbar.top"></span>

</div>

<div class="header">

<div class="subTitle">

au.gov.asd.tac.constellation.views.scripting.utilities

</div>

## Class ScriptingUtilities

</div>

<div class="contentContainer">

-   java.lang.Object

-   -   au.gov.asd.tac.constellation.views.scripting.utilities.ScriptingUtilities

<div class="description">

-   All Implemented Interfaces:  
    au.gov.asd.tac.constellation.views.scripting.ScriptingModule

    ------------------------------------------------------------------------

      

        public class ScriptingUtilities
        extends java.lang.Object
        implements au.gov.asd.tac.constellation.views.scripting.ScriptingModule

    <div class="block">

    Core scripting utilities.

    </div>

    <span class="simpleTagLabel">Author:</span>  
    cygnus_x-1

</div>

<div class="summary">

-   -   <span id="constructor.summary"></span>

        ### Constructor Summary

        <table class="memberSummary" data-border="0" data-cellpadding="3" data-cellspacing="0" data-summary="Constructor Summary table, listing constructors, and an explanation">
        <caption><span>Constructors</span><span class="tabEnd"> </span></caption>
        <thead>
        <tr class="header">
        <th class="colOne" scope="col">Constructor and Description</th>
        </tr>
        </thead>
        <tbody>
        <tr class="odd altColor">
        <td class="colOne"><code>ScriptingUtilities()</code> </td>
        </tr>
        </tbody>
        </table>

        Constructors<span class="tabEnd"> </span>

    <!-- -->

    -   <span id="method.summary"></span>

        ### Method Summary

        <table class="memberSummary" data-border="0" data-cellpadding="3" data-cellspacing="0" data-summary="Method Summary table, listing methods, and an explanation">
        <caption><span id="t0" class="activeTableTab"><span>All Methods</span><span class="tabEnd"> </span></span><span id="t2" class="tableTab"><span><a href="javascript:show(2);">Instance Methods</a></span><span class="tabEnd"> </span></span><span id="t4" class="tableTab"><span><a href="javascript:show(8);">Concrete Methods</a></span><span class="tabEnd"> </span></span></caption>
        <colgroup>
        <col style="width: 50%" />
        <col style="width: 50%" />
        </colgroup>
        <thead>
        <tr class="header">
        <th class="colFirst" scope="col">Modifier and Type</th>
        <th class="colLast" scope="col">Method and Description</th>
        </tr>
        </thead>
        <tbody>
        <tr id="i0" class="odd altColor">
        <td class="colFirst"><code>SGraph</code></td>
        <td class="colLast"><code>copyGraph(SGraph graph)</code>
        <div class="block">
        Create an in-memory copy of the given graph.
        </div></td>
        </tr>
        <tr id="i1" class="even rowColor">
        <td class="colFirst"><code>void</code></td>
        <td class="colLast"><code>executePlugin(SGraph graph,                                                 java.lang.String pluginName)</code>
        <div class="block">
        Lookup a plugin by name and execute it with default parameter values.
        </div></td>
        </tr>
        <tr id="i2" class="odd altColor">
        <td class="colFirst"><code>void</code></td>
        <td class="colLast"><code>executePlugin(SGraph graph,                                                 java.lang.String pluginName,                                                 java.util.Map&lt;java.lang.String,java.lang.String&gt; pluginParameters)</code>
        <div class="block">
        Lookup a plugin by name and execute it with custom parameter values.
        </div></td>
        </tr>
        <tr id="i3" class="even rowColor">
        <td class="colFirst"><code>java.lang.String</code></td>
        <td class="colLast"><code>getName()</code> </td>
        </tr>
        <tr id="i4" class="odd altColor">
        <td class="colFirst"><code>java.util.Map&lt;java.lang.String,SGraph&gt;</code></td>
        <td class="colLast"><code>getOpenGraphs()</code>
        <div class="block">
        Provide a map of graph name to graph for every graph currently open in Constellation.
        </div></td>
        </tr>
        <tr id="i5" class="even rowColor">
        <td class="colFirst"><code>java.lang.String</code></td>
        <td class="colLast"><code>openFile(java.lang.String dirKey)</code>
        <div class="block">
        Provide a NetBeans open file dialog box that remembers where it was last opened.
        </div></td>
        </tr>
        </tbody>
        </table>

        <span id="t0" class="activeTableTab">All Methods<span
        class="tabEnd"> </span></span><span id="t2"
        class="tableTab">[Instance Methods](javascript:show(2);)<span
        class="tabEnd"> </span></span><span id="t4"
        class="tableTab">[Concrete Methods](javascript:show(8);)<span
        class="tabEnd"> </span></span>

        -   <span
            id="methods.inherited.from.class.java.lang.Object"></span>

            ### Methods inherited from class java.lang.Object

            `clone, equals, finalize, getClass, hashCode, notify, notifyAll, toString, wait, wait, wait`

</div>

<div class="details">

-   -   <span id="constructor.detail"></span>

        ### Constructor Detail

        <span id="ScriptingUtilities--"></span>

        -   #### ScriptingUtilities

                public ScriptingUtilities()

    <!-- -->

    -   <span id="method.detail"></span>

        ### Method Detail

        <span id="getName--"></span>

        -   #### getName

                public java.lang.String getName()

            <span class="overrideSpecifyLabel">Specified by:</span>  
            `getName` in
            interface `au.gov.asd.tac.constellation.views.scripting.ScriptingModule`

        <span id="openFile-java.lang.String-"></span>

        -   #### openFile

                public java.lang.String openFile(java.lang.String dirKey)

            <div class="block">

            Provide a NetBeans open file dialog box that remembers where
            it was last opened.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `dirKey` - A key to where the dialog box was last opened.

            <span class="returnLabel">Returns:</span>  
            A string containing a file name, or null if the user
            selected Cancel.

        <span id="getOpenGraphs--"></span>

        -   #### getOpenGraphs

                public java.util.Map<java.lang.String,SGraph> getOpenGraphs()

            <div class="block">

            Provide a map of graph name to graph for every graph
            currently open in Constellation.

            </div>

            <span class="returnLabel">Returns:</span>  
            a map of graph name to graph as a
            [`SGraph`](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/graph/SGraph.md "class in au.gov.asd.tac.constellation.views.scripting.graph").

        <span
        id="copyGraph-au.gov.asd.tac.constellation.views.scripting.graph.SGraph-"></span>

        -   #### copyGraph

                public SGraph copyGraph(SGraph graph)

            <div class="block">

            Create an in-memory copy of the given graph.

            </div>

            <span class="returnLabel">Returns:</span>  
            a copy of the given graph as an
            [`SGraph`](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/graph/SGraph.md "class in au.gov.asd.tac.constellation.views.scripting.graph").

        <span
        id="executePlugin-au.gov.asd.tac.constellation.views.scripting.graph.SGraph-java.lang.String-"></span>

        -   #### executePlugin

                public void executePlugin(SGraph graph,
                                          java.lang.String pluginName)

            <div class="block">

            Lookup a plugin by name and execute it with default
            parameter values.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `graph` - The graph on which to execute the plugin.

            `pluginName` - The name of the plugin to execute.

        <span
        id="executePlugin-au.gov.asd.tac.constellation.views.scripting.graph.SGraph-java.lang.String-java.util.Map-"></span>

        -   #### executePlugin

                public void executePlugin(SGraph graph,
                                          java.lang.String pluginName,
                                          java.util.Map<java.lang.String,java.lang.String> pluginParameters)

            <div class="block">

            Lookup a plugin by name and execute it with custom parameter
            values.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `graph` - The graph on which to execute the plugin.

            `pluginName` - The name of the plugin you wish to execute.

            `pluginParameters` - A map of parameters to their values for
            use with the plugin you wish to execute.

</div>

</div>

<div class="bottomNav">

<span id="navbar.bottom"></span>

<div class="skipNav">

[Skip navigation links](#skip.navbar.bottom "Skip navigation links")

</div>

<span id="navbar.bottom.firstrow"></span>

-   [Overview](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/overview-summary.md)
-   [Package](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/utilities/package-summary.md)
-   Class
-   [Tree](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/utilities/package-tree.md)
-   [Deprecated](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/deprecated-list.md)
-   [Index](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/index-all.md)
-   [Help](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/help-doc.md)

<div class="aboutLanguage">

au.gov.asd.tac.constellation.views.scripting 1.0

</div>

</div>

<div class="subNav">

-   Prev Class
-   Next Class

<!-- -->

-   [Frames](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/utilities/ScriptingUtilities.md)
-   [No Frames](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/utilities/ScriptingUtilities.md)

<!-- -->

-   [All Classes](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/allclasses-noframe.md)

<div>

</div>

<div>

-   Summary: 
-   Nested | 
-   Field | 
-   [Constr](#constructor.summary) | 
-   [Method](#method.summary)

<!-- -->

-   Detail: 
-   Field | 
-   [Constr](#constructor.detail) | 
-   [Method](#method.detail)

</div>

<span id="skip.navbar.bottom"></span>

</div>
