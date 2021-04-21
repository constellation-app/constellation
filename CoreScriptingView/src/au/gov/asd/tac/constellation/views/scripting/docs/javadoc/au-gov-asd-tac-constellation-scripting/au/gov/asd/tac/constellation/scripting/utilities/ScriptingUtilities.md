<div>

JavaScript is disabled on your browser.

</div>

::: {.topNav}
[]{#navbar.top}

::: {.skipNav}
[Skip navigation links](#skip.navbar.top "Skip navigation links")
:::

[]{#navbar.top.firstrow}

-   [Overview](../../../../../../../overview-summary.html)
-   [Package](package-summary.html)
-   Class
-   [Tree](package-tree.html)
-   [Deprecated](../../../../../../../deprecated-list.html)
-   [Index](../../../../../../../index-all.html)
-   [Help](../../../../../../../help-doc.html)

::: {.aboutLanguage}
au.gov.asd.tac.constellation.views.scripting 1.0
:::
:::

::: {.subNav}
-   Prev Class
-   Next Class

```{=html}
<!-- -->
```
-   [Frames](../../../../../../../index.html?au/gov/asd/tac/constellation/scripting/utilities/ScriptingUtilities.html)
-   [No Frames](ScriptingUtilities.html)

```{=html}
<!-- -->
```
-   [All Classes](../../../../../../../allclasses-noframe.html)

<div>

</div>

<div>

-   Summary: 
-   Nested \| 
-   Field \| 
-   [Constr](#constructor.summary) \| 
-   [Method](#method.summary)

```{=html}
<!-- -->
```
-   Detail: 
-   Field \| 
-   [Constr](#constructor.detail) \| 
-   [Method](#method.detail)

</div>

[]{#skip.navbar.top}
:::

::: {.header}
::: {.subTitle}
au.gov.asd.tac.constellation.views.scripting.utilities
:::

## Class ScriptingUtilities {#class-scriptingutilities .title title="Class ScriptingUtilities"}
:::

::: {.contentContainer}
-   java.lang.Object

-   -   au.gov.asd.tac.constellation.views.scripting.utilities.ScriptingUtilities

::: {.description}
-   

    All Implemented Interfaces:
    :   au.gov.asd.tac.constellation.views.scripting.ScriptingModule

    ------------------------------------------------------------------------

    \

        public class ScriptingUtilities
        extends java.lang.Object
        implements au.gov.asd.tac.constellation.views.scripting.ScriptingModule

    ::: {.block}
    Core scripting utilities.
    :::

    [Author:]{.simpleTagLabel}
    :   cygnus_x-1
:::

::: {.summary}
-   -   []{#constructor.summary}

        ### Constructor Summary

          Constructor and Description
          -----------------------------
          `ScriptingUtilities()` 

          : Constructors[ ]{.tabEnd}

    ```{=html}
    <!-- -->
    ```
    -   []{#method.summary}

        ### Method Summary

        +-----------------------------------+-----------------------------------+
        | Modifier and Type                 | Method and Description            |
        +===================================+===================================+
        | `SGraph`                          | `copyGraph(SGraph graph)`         |
        |                                   | ::: {.block}                      |
        |                                   | Create an in-memory copy of the   |
        |                                   | given graph.                      |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `void`                            | `execut                           |
        |                                   | ePlugin(SGraph graph,             |
        |                                   |                                   |
        |                                   |     java.lang.String pluginName)` |
        |                                   | ::: {.block}                      |
        |                                   | Lookup a plugin by name and       |
        |                                   | execute it with default parameter |
        |                                   | values.                           |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `void`                            | `executePlugin(SGraph g           |
        |                                   | raph,                             |
        |                                   |                      java.lang.St |
        |                                   | ring pluginName,                  |
        |                                   |                                 j |
        |                                   | ava.util.Map<java.lang.String,jav |
        |                                   | a.lang.String> pluginParameters)` |
        |                                   | ::: {.block}                      |
        |                                   | Lookup a plugin by name and       |
        |                                   | execute it with custom parameter  |
        |                                   | values.                           |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `java.lang.String`                | `getName()`                       |
        +-----------------------------------+-----------------------------------+
        | `java.u                           | `getOpenGraphs()`                 |
        | til.Map<java.lang.String,SGraph>` | ::: {.block}                      |
        |                                   | Provide a map of graph name to    |
        |                                   | graph for every graph currently   |
        |                                   | open in Constellation.            |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `java.lang.String`                | `o                                |
        |                                   | penFile(java.lang.String dirKey)` |
        |                                   | ::: {.block}                      |
        |                                   | Provide a NetBeans open file      |
        |                                   | dialog box that remembers where   |
        |                                   | it was last opened.               |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+

        : [All Methods[ ]{.tabEnd}]{#t0 .activeTableTab}[[Instance
        Methods](javascript:show(2);)[ ]{.tabEnd}]{#t2
        .tableTab}[[Concrete
        Methods](javascript:show(8);)[ ]{.tabEnd}]{#t4 .tableTab}

        -   []{#methods.inherited.from.class.java.lang.Object}

            ### Methods inherited from class java.lang.Object

            `clone, equals, finalize, getClass, hashCode, notify, notifyAll, toString, wait, wait, wait`
:::

::: {.details}
-   -   []{#constructor.detail}

        ### Constructor Detail

        []{#ScriptingUtilities--}

        -   #### ScriptingUtilities

                public ScriptingUtilities()

    ```{=html}
    <!-- -->
    ```
    -   []{#method.detail}

        ### Method Detail

        []{#getName--}

        -   #### getName

                public java.lang.String getName()

            [Specified by:]{.overrideSpecifyLabel}
            :   `getName` in
                interface `au.gov.asd.tac.constellation.views.scripting.ScriptingModule`

        []{#openFile-java.lang.String-}

        -   #### openFile

                public java.lang.String openFile(java.lang.String dirKey)

            ::: {.block}
            Provide a NetBeans open file dialog box that remembers where
            it was last opened.
            :::

            [Parameters:]{.paramLabel}
            :   `dirKey` - A key to where the dialog box was last
                opened.

            [Returns:]{.returnLabel}
            :   A string containing a file name, or null if the user
                selected Cancel.

        []{#getOpenGraphs--}

        -   #### getOpenGraphs

                public java.util.Map<java.lang.String,SGraph> getOpenGraphs()

            ::: {.block}
            Provide a map of graph name to graph for every graph
            currently open in Constellation.
            :::

            [Returns:]{.returnLabel}
            :   a map of graph name to graph as a
                [`SGraph`](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SGraph.html "class in au.gov.asd.tac.constellation.views.scripting.graph").

        []{#copyGraph-au.gov.asd.tac.constellation.views.scripting.graph.SGraph-}

        -   #### copyGraph

                public SGraph copyGraph(SGraph graph)

            ::: {.block}
            Create an in-memory copy of the given graph.
            :::

            [Returns:]{.returnLabel}
            :   a copy of the given graph as an
                [`SGraph`](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SGraph.html "class in au.gov.asd.tac.constellation.views.scripting.graph").

        []{#executePlugin-au.gov.asd.tac.constellation.views.scripting.graph.SGraph-java.lang.String-}

        -   #### executePlugin

                public void executePlugin(SGraph graph,
                                          java.lang.String pluginName)

            ::: {.block}
            Lookup a plugin by name and execute it with default
            parameter values.
            :::

            [Parameters:]{.paramLabel}
            :   `graph` - The graph on which to execute the plugin.
            :   `pluginName` - The name of the plugin to execute.

        []{#executePlugin-au.gov.asd.tac.constellation.views.scripting.graph.SGraph-java.lang.String-java.util.Map-}

        -   #### executePlugin

                public void executePlugin(SGraph graph,
                                          java.lang.String pluginName,
                                          java.util.Map<java.lang.String,java.lang.String> pluginParameters)

            ::: {.block}
            Lookup a plugin by name and execute it with custom parameter
            values.
            :::

            [Parameters:]{.paramLabel}
            :   `graph` - The graph on which to execute the plugin.
            :   `pluginName` - The name of the plugin you wish to
                execute.
            :   `pluginParameters` - A map of parameters to their values
                for use with the plugin you wish to execute.
:::
:::

::: {.bottomNav}
[]{#navbar.bottom}

::: {.skipNav}
[Skip navigation links](#skip.navbar.bottom "Skip navigation links")
:::

[]{#navbar.bottom.firstrow}

-   [Overview](../../../../../../../overview-summary.html)
-   [Package](package-summary.html)
-   Class
-   [Tree](package-tree.html)
-   [Deprecated](../../../../../../../deprecated-list.html)
-   [Index](../../../../../../../index-all.html)
-   [Help](../../../../../../../help-doc.html)

::: {.aboutLanguage}
au.gov.asd.tac.constellation.views.scripting 1.0
:::
:::

::: {.subNav}
-   Prev Class
-   Next Class

```{=html}
<!-- -->
```
-   [Frames](../../../../../../../index.html?au/gov/asd/tac/constellation/scripting/utilities/ScriptingUtilities.html)
-   [No Frames](ScriptingUtilities.html)

```{=html}
<!-- -->
```
-   [All Classes](../../../../../../../allclasses-noframe.html)

<div>

</div>

<div>

-   Summary: 
-   Nested \| 
-   Field \| 
-   [Constr](#constructor.summary) \| 
-   [Method](#method.summary)

```{=html}
<!-- -->
```
-   Detail: 
-   Field \| 
-   [Constr](#constructor.detail) \| 
-   [Method](#method.detail)

</div>

[]{#skip.navbar.bottom}
:::
