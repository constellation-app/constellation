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
-   [[Prev Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/STransaction.html "class in au.gov.asd.tac.constellation.views.scripting.graph")
-   [[Next Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SWritableGraph.html "class in au.gov.asd.tac.constellation.views.scripting.graph")

```{=html}
<!-- -->
```
-   [Frames](../../../../../../../index.html?au/gov/asd/tac/constellation/scripting/graph/SVertex.html)
-   [No Frames](SVertex.html)

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
au.gov.asd.tac.constellation.views.scripting.graph
:::

## Class SVertex {#class-svertex .title title="Class SVertex"}
:::

::: {.contentContainer}
-   java.lang.Object

-   -   au.gov.asd.tac.constellation.views.scripting.graph.SVertex

::: {.description}
-   

    ------------------------------------------------------------------------

    \

        public class SVertex
        extends java.lang.Object

    ::: {.block}
    A representation of a vertex for use with scripting.
    :::

    [Author:]{.simpleTagLabel}
    :   algol, cygnus_x-1
:::

::: {.summary}
-   -   []{#constructor.summary}

        ### Constructor Summary

          Constructor and Description
          ---------------------------------------------------------------------------------------------------------------------------------
          `SVertex(au.gov.asd.tac.constellation.graph.GraphReadMethods hlgraph,                                                 int id)` 

          : Constructors[ ]{.tabEnd}

    ```{=html}
    <!-- -->
    ```
    -   []{#method.summary}

        ### Method Summary

        +-----------------------------------+-----------------------------------+
        | Modifier and Type                 | Method and Description            |
        +===================================+===================================+
        | `java.lang.Object`                | `_                                |
        |                                   | _getitem__(java.lang.Object key)` |
        |                                   | ::: {.block}                      |
        |                                   | Python evaluation of self\[key\]. |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `java.lang.Object`                | `__setit                          |
        |                                   | em__(java.lang.Object key,        |
        |                                   |                                   |
        |                                   |          java.lang.Object value)` |
        |                                   | ::: {.block}                      |
        |                                   | Python assignment to self\[key\]. |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `void`                            | `complete()`                      |
        |                                   | ::: {.block}                      |
        |                                   | Complete this vertex using the    |
        |                                   | schema of the graph.              |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `int`                             | `edgeCount()`                     |
        |                                   | ::: {.block}                      |
        |                                   | Get the number of edges connected |
        |                                   | to this vertex.                   |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `SVertexEdgeIterator`             | `edges()`                         |
        |                                   | ::: {.block}                      |
        |                                   | Get an iterator over edges        |
        |                                   | attached to this vertex.          |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `boolean`                         | `equals(java.lang.Object obj)`    |
        +-----------------------------------+-----------------------------------+
        | `int`                             | `hashCode()`                      |
        +-----------------------------------+-----------------------------------+
        | `int`                             | `id()`                            |
        |                                   | ::: {.block}                      |
        |                                   | Get the id of this vertex.        |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `void`                            | `init()`                          |
        |                                   | ::: {.block}                      |
        |                                   | Initialise this vertex using the  |
        |                                   | schema of the graph.              |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `int`                             | `linkCount()`                     |
        |                                   | ::: {.block}                      |
        |                                   | Get the number of links connected |
        |                                   | to this vertex.                   |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `SVertexLinkIterator`             | `links()`                         |
        |                                   | ::: {.block}                      |
        |                                   | Get an iterator over links        |
        |                                   | attached to this vertex.          |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `java.lang.String`                | `toString()`                      |
        +-----------------------------------+-----------------------------------+
        | `int`                             | `transactionCount()`              |
        |                                   | ::: {.block}                      |
        |                                   | Get the number of transactions    |
        |                                   | connected to this vertex.         |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `SVertexTransactionIterator`      | `transactions()`                  |
        |                                   | ::: {.block}                      |
        |                                   | Get an iterator over transactions |
        |                                   | attached to this vertex.          |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+

        : [All Methods[ ]{.tabEnd}]{#t0 .activeTableTab}[[Instance
        Methods](javascript:show(2);)[ ]{.tabEnd}]{#t2
        .tableTab}[[Concrete
        Methods](javascript:show(8);)[ ]{.tabEnd}]{#t4 .tableTab}

        -   []{#methods.inherited.from.class.java.lang.Object}

            ### Methods inherited from class java.lang.Object

            `clone, finalize, getClass, notify, notifyAll, wait, wait, wait`
:::

::: {.details}
-   -   []{#constructor.detail}

        ### Constructor Detail

        []{#SVertex-au.gov.asd.tac.constellation.graph.GraphReadMethods-int-}

        -   #### SVertex

                public SVertex(au.gov.asd.tac.constellation.graph.GraphReadMethods hlgraph,
                               int id)

    ```{=html}
    <!-- -->
    ```
    -   []{#method.detail}

        ### Method Detail

        []{#id--}

        -   #### id

                public int id()

            ::: {.block}
            Get the id of this vertex.
            :::

            [Returns:]{.returnLabel}
            :   the id of this vertex.

        []{#init--}

        -   #### init

                public void init()

            ::: {.block}
            Initialise this vertex using the schema of the graph.
            :::

        []{#complete--}

        -   #### complete

                public void complete()

            ::: {.block}
            Complete this vertex using the schema of the graph.
            :::

        []{#transactionCount--}

        -   #### transactionCount

                public int transactionCount()

            ::: {.block}
            Get the number of transactions connected to this vertex.
            :::

            [Returns:]{.returnLabel}
            :   the number of transactions connected to this vertex.

        []{#transactions--}

        -   #### transactions

                public SVertexTransactionIterator transactions()

            ::: {.block}
            Get an iterator over transactions attached to this vertex.
            :::

            [Returns:]{.returnLabel}
            :   a transaction iterator.

        []{#edgeCount--}

        -   #### edgeCount

                public int edgeCount()

            ::: {.block}
            Get the number of edges connected to this vertex.
            :::

            [Returns:]{.returnLabel}
            :   the number of edges connected to this vertex.

        []{#edges--}

        -   #### edges

                public SVertexEdgeIterator edges()

            ::: {.block}
            Get an iterator over edges attached to this vertex.
            :::

            [Returns:]{.returnLabel}
            :   a edge iterator.

        []{#linkCount--}

        -   #### linkCount

                public int linkCount()

            ::: {.block}
            Get the number of links connected to this vertex.
            :::

            [Returns:]{.returnLabel}
            :   the number of links connected to this vertex.

        []{#links--}

        -   #### links

                public SVertexLinkIterator links()

            ::: {.block}
            Get an iterator over links attached to this vertex.
            :::

            [Returns:]{.returnLabel}
            :   a link iterator.

        []{#Z:Z__getitem__-java.lang.Object-}

        -   #### \_\_getitem\_\_

                public java.lang.Object __getitem__(java.lang.Object key)

            ::: {.block}
            Python evaluation of self\[key\].
            :::

            [Parameters:]{.paramLabel}
            :   `key` - an object representing the attribute to be
                queried.

            [Returns:]{.returnLabel}
            :   the value of the specified attribute.

        []{#Z:Z__setitem__-java.lang.Object-java.lang.Object-}

        -   #### \_\_setitem\_\_

                public java.lang.Object __setitem__(java.lang.Object key,
                                                    java.lang.Object value)

            ::: {.block}
            Python assignment to self\[key\].
            :::

            [Parameters:]{.paramLabel}
            :   `key` - an object representing the attribute to be set.
            :   `value` - the new value for the specified attribute.

            [Returns:]{.returnLabel}
            :   the set value.

        []{#hashCode--}

        -   #### hashCode

                public int hashCode()

            [Overrides:]{.overrideSpecifyLabel}
            :   `hashCode` in class `java.lang.Object`

        []{#equals-java.lang.Object-}

        -   #### equals

                public boolean equals(java.lang.Object obj)

            [Overrides:]{.overrideSpecifyLabel}
            :   `equals` in class `java.lang.Object`

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
-   [[Prev Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/STransaction.html "class in au.gov.asd.tac.constellation.views.scripting.graph")
-   [[Next Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SWritableGraph.html "class in au.gov.asd.tac.constellation.views.scripting.graph")

```{=html}
<!-- -->
```
-   [Frames](../../../../../../../index.html?au/gov/asd/tac/constellation/scripting/graph/SVertex.html)
-   [No Frames](SVertex.html)

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
