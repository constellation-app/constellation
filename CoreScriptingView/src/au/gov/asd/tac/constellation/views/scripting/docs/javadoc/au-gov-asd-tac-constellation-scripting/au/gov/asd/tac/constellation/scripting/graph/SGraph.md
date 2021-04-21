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
-   [[Prev Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SEdge.html "class in au.gov.asd.tac.constellation.views.scripting.graph")
-   [[Next Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SLink.html "class in au.gov.asd.tac.constellation.views.scripting.graph")

```{=html}
<!-- -->
```
-   [Frames](../../../../../../../index.html?au/gov/asd/tac/constellation/scripting/graph/SGraph.html)
-   [No Frames](SGraph.html)

```{=html}
<!-- -->
```
-   [All Classes](../../../../../../../allclasses-noframe.html)

<div>

</div>

<div>

-   Summary: 
-   Nested \| 
-   [Field](#field.summary) \| 
-   [Constr](#constructor.summary) \| 
-   [Method](#method.summary)

```{=html}
<!-- -->
```
-   Detail: 
-   [Field](#field.detail) \| 
-   [Constr](#constructor.detail) \| 
-   [Method](#method.detail)

</div>

[]{#skip.navbar.top}
:::

::: {.header}
::: {.subTitle}
au.gov.asd.tac.constellation.views.scripting.graph
:::

## Class SGraph {#class-sgraph .title title="Class SGraph"}
:::

::: {.contentContainer}
-   java.lang.Object

-   -   au.gov.asd.tac.constellation.views.scripting.graph.SGraph

::: {.description}
-   

    ------------------------------------------------------------------------

    \

        public class SGraph
        extends java.lang.Object

    ::: {.block}
    A representation of a graph for use with scripting.
    :::

    [Author:]{.simpleTagLabel}
    :   algol, cygnus_x-1
:::

::: {.summary}
-   -   []{#field.summary}

        ### Field Summary

        +-----------------------------------+-----------------------------------+
        | Modifier and Type                 | Field and Description             |
        +===================================+===================================+
        | `static au.gov.asd.tac.cons       | `EDGE`                            |
        | tellation.graph.GraphElementType` | ::: {.block}                      |
        |                                   | A reference to the \'Edge\'       |
        |                                   | element type.                     |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `static au.gov.asd.tac.cons       | `GRAPH`                           |
        | tellation.graph.GraphElementType` | ::: {.block}                      |
        |                                   | A reference to the \'Graph\'      |
        |                                   | element type.                     |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `static au.gov.asd.tac.cons       | `LINK`                            |
        | tellation.graph.GraphElementType` | ::: {.block}                      |
        |                                   | A reference to the \'Link\'       |
        |                                   | element type.                     |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `static au.gov.asd.tac.cons       | `TRANSACTION`                     |
        | tellation.graph.GraphElementType` | ::: {.block}                      |
        |                                   | A reference to the                |
        |                                   | \'Transaction\' element type.     |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `static au.gov.asd.tac.cons       | `VERTEX`                          |
        | tellation.graph.GraphElementType` | ::: {.block}                      |
        |                                   | A reference to the \'Vertex\'     |
        |                                   | element type.                     |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+

        : Fields[ ]{.tabEnd}

    ```{=html}
    <!-- -->
    ```
    -   []{#constructor.summary}

        ### Constructor Summary

          Constructor and Description
          ---------------------------------------------------------------------------------------------------------------------------------------------
          `SGraph(au.gov.asd.tac.constellation.graph.Graph graph)` 
          `SGraph(javax.script.ScriptEngine engine,                                                 au.gov.asd.tac.constellation.graph.Graph graph)` 

          : Constructors[ ]{.tabEnd}

    ```{=html}
    <!-- -->
    ```
    -   []{#method.summary}

        ### Method Summary

        +-----------------------------------+-----------------------------------+
        | Modifier and Type                 | Method and Description            |
        +===================================+===================================+
        | `void`                            | `cleanup()`                       |
        |                                   | ::: {.block}                      |
        |                                   | Clean up any graphs created by    |
        |                                   | scripting which remain in memory. |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `javax.script.ScriptEngine`       | `getEngine()`                     |
        |                                   | ::: {.block}                      |
        |                                   | Get the scripting engine          |
        |                                   | currently being used to evaluate  |
        |                                   | scripts.                          |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `au.gov.a                         | `getGraph()`                      |
        | sd.tac.constellation.graph.Graph` | ::: {.block}                      |
        |                                   | Get the actual graph on which     |
        |                                   | scripts are being executed.       |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `SReadableGraph`                  | `readableGraph()`                 |
        |                                   | ::: {.block}                      |
        |                                   | Get a read lock on the graph so   |
        |                                   | that its data can be              |
        |                                   | interrogated.                     |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `void`                            | `setEngine(j                      |
        |                                   | avax.script.ScriptEngine engine)` |
        |                                   | ::: {.block}                      |
        |                                   | Set a new scripting engine to be  |
        |                                   | used for evaluating scripts.      |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `java.lang.String`                | `toString()`                      |
        +-----------------------------------+-----------------------------------+
        | `void`                            | `withReadable                     |
        |                                   | Graph(java.lang.Object callback)` |
        |                                   | ::: {.block}                      |
        |                                   | Evaluate a function against a     |
        |                                   | read lock on the graph with       |
        |                                   | releases handled automatically.   |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `void`                            | `withWritable                     |
        |                                   | Graph(java.lang.Object callback)` |
        |                                   | ::: {.block}                      |
        |                                   | Evaluate a function against a     |
        |                                   | write lock on the graph with      |
        |                                   | commits or rollbacks handled      |
        |                                   | automatically.                    |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `SWritableGraph`                  | `writable                         |
        |                                   | Graph(java.lang.String editName)` |
        |                                   | ::: {.block}                      |
        |                                   | Get a write lock on the graph so  |
        |                                   | that edits can be made to its     |
        |                                   | data.                             |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+

        : [All Methods[ ]{.tabEnd}]{#t0 .activeTableTab}[[Instance
        Methods](javascript:show(2);)[ ]{.tabEnd}]{#t2
        .tableTab}[[Concrete
        Methods](javascript:show(8);)[ ]{.tabEnd}]{#t4 .tableTab}

        -   []{#methods.inherited.from.class.java.lang.Object}

            ### Methods inherited from class java.lang.Object

            `clone, equals, finalize, getClass, hashCode, notify, notifyAll, wait, wait, wait`
:::

::: {.details}
-   -   []{#field.detail}

        ### Field Detail

        []{#GRAPH}

        -   #### GRAPH

                public static final au.gov.asd.tac.constellation.graph.GraphElementType GRAPH

            ::: {.block}
            A reference to the \'Graph\' element type.
            :::

        []{#VERTEX}

        -   #### VERTEX

                public static final au.gov.asd.tac.constellation.graph.GraphElementType VERTEX

            ::: {.block}
            A reference to the \'Vertex\' element type.
            :::

        []{#TRANSACTION}

        -   #### TRANSACTION

                public static final au.gov.asd.tac.constellation.graph.GraphElementType TRANSACTION

            ::: {.block}
            A reference to the \'Transaction\' element type.
            :::

        []{#EDGE}

        -   #### EDGE

                public static final au.gov.asd.tac.constellation.graph.GraphElementType EDGE

            ::: {.block}
            A reference to the \'Edge\' element type.
            :::

        []{#LINK}

        -   #### LINK

                public static final au.gov.asd.tac.constellation.graph.GraphElementType LINK

            ::: {.block}
            A reference to the \'Link\' element type.
            :::

    ```{=html}
    <!-- -->
    ```
    -   []{#constructor.detail}

        ### Constructor Detail

        []{#SGraph-javax.script.ScriptEngine-au.gov.asd.tac.constellation.graph.Graph-}

        -   #### SGraph

                public SGraph(javax.script.ScriptEngine engine,
                              au.gov.asd.tac.constellation.graph.Graph graph)

        []{#SGraph-au.gov.asd.tac.constellation.graph.Graph-}

        -   #### SGraph

                public SGraph(au.gov.asd.tac.constellation.graph.Graph graph)

    ```{=html}
    <!-- -->
    ```
    -   []{#method.detail}

        ### Method Detail

        []{#getEngine--}

        -   #### getEngine

                public javax.script.ScriptEngine getEngine()

            ::: {.block}
            Get the scripting engine currently being used to evaluate
            scripts.
            :::

            [Returns:]{.returnLabel}
            :   the current scripting engine.

        []{#setEngine-javax.script.ScriptEngine-}

        -   #### setEngine

                public void setEngine(javax.script.ScriptEngine engine)

            ::: {.block}
            Set a new scripting engine to be used for evaluating
            scripts.
            :::

            [Parameters:]{.paramLabel}
            :   `engine` - the scripting engine to use.

        []{#getGraph--}

        -   #### getGraph

                public au.gov.asd.tac.constellation.graph.Graph getGraph()

            ::: {.block}
            Get the actual graph on which scripts are being executed.
            :::

            [Returns:]{.returnLabel}
            :   the graph on which scripts are being executed.

        []{#cleanup--}

        -   #### cleanup

                public void cleanup()

            ::: {.block}
            Clean up any graphs created by scripting which remain in
            memory.
            After a script completes, we have no way of reliably knowing
            if the script has called .release(), .commit(), or
            .rollback() on the graphs it used. It is undesirable to
            leave graphs remaining in memory if they are no longer in
            use. To address this issue, we keep track of any graphs used
            by scripting and offer this method to attempt to force them
            out of memory if they still exist.
            :::

        []{#readableGraph--}

        -   #### readableGraph

                public SReadableGraph readableGraph()
                                             throws java.lang.InterruptedException

            ::: {.block}
            Get a read lock on the graph so that its data can be
            interrogated.
            Note: If this method is called in a Python script using the
            \'with\' statement, a context manager will be created for
            you to automatically handle releases as appropriate.
            :::

            [Returns:]{.returnLabel}
            :   a graph with an active read lock.

            [Throws:]{.throwsLabel}
            :   `java.lang.InterruptedException`

        []{#withReadableGraph-java.lang.Object-}

        -   #### withReadableGraph

                public void withReadableGraph(java.lang.Object callback)
                                       throws javax.script.ScriptException,
                                              java.lang.InterruptedException

            ::: {.block}
            Evaluate a function against a read lock on the graph with
            releases handled automatically.
            Note: This method will only work for Python scripts as it
            makes use of Python specific syntax.
            :::

            [Parameters:]{.paramLabel}
            :   `callback` - the function to evaluate.

            [Throws:]{.throwsLabel}
            :   `javax.script.ScriptException`
            :   `java.lang.InterruptedException`

        []{#writableGraph-java.lang.String-}

        -   #### writableGraph

                public SWritableGraph writableGraph(java.lang.String editName)
                                             throws java.lang.InterruptedException

            ::: {.block}
            Get a write lock on the graph so that edits can be made to
            its data.
            Note: If this method is called in a Python script using the
            \'with\' statement, a context manager will be created for
            you to automatically handle commits and rollbacks as
            appropriate.
            :::

            [Parameters:]{.paramLabel}
            :   `editName` - a name for the edit operation.

            [Returns:]{.returnLabel}
            :   a graph with an active write lock.

            [Throws:]{.throwsLabel}
            :   `java.lang.InterruptedException`

        []{#withWritableGraph-java.lang.Object-}

        -   #### withWritableGraph

                public void withWritableGraph(java.lang.Object callback)
                                       throws javax.script.ScriptException,
                                              java.lang.InterruptedException

            ::: {.block}
            Evaluate a function against a write lock on the graph with
            commits or rollbacks handled automatically.
            Note: This method will only work for Python scripts as it
            makes use of Python specific syntax.
            :::

            [Parameters:]{.paramLabel}
            :   `callback` - the function to evaluate.

            [Throws:]{.throwsLabel}
            :   `javax.script.ScriptException`
            :   `java.lang.InterruptedException`

        []{#toString--}

        -   #### toString

                public java.lang.String toString()

            [Overrides:]{.overrideSpecifyLabel}
            :   `toString` in class `java.lang.Object`
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
-   [[Prev Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SEdge.html "class in au.gov.asd.tac.constellation.views.scripting.graph")
-   [[Next Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SLink.html "class in au.gov.asd.tac.constellation.views.scripting.graph")

```{=html}
<!-- -->
```
-   [Frames](../../../../../../../index.html?au/gov/asd/tac/constellation/scripting/graph/SGraph.html)
-   [No Frames](SGraph.html)

```{=html}
<!-- -->
```
-   [All Classes](../../../../../../../allclasses-noframe.html)

<div>

</div>

<div>

-   Summary: 
-   Nested \| 
-   [Field](#field.summary) \| 
-   [Constr](#constructor.summary) \| 
-   [Method](#method.summary)

```{=html}
<!-- -->
```
-   Detail: 
-   [Field](#field.detail) \| 
-   [Constr](#constructor.detail) \| 
-   [Method](#method.detail)

</div>

[]{#skip.navbar.bottom}
:::
