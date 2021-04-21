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
-   [[Prev Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SCollection.html "class in au.gov.asd.tac.constellation.views.scripting.graph")
-   [[Next Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SGraph.html "class in au.gov.asd.tac.constellation.views.scripting.graph")

```{=html}
<!-- -->
```
-   [Frames](../../../../../../../index.html?au/gov/asd/tac/constellation/scripting/graph/SEdge.html)
-   [No Frames](SEdge.html)

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

## Class SEdge {#class-sedge .title title="Class SEdge"}
:::

::: {.contentContainer}
-   java.lang.Object

-   -   au.gov.asd.tac.constellation.views.scripting.graph.SEdge

::: {.description}
-   

    ------------------------------------------------------------------------

    \

        public class SEdge
        extends java.lang.Object

    ::: {.block}
    A representation of an edge for use with scripting.
    :::

    [Author:]{.simpleTagLabel}
    :   cygnus_x-1
:::

::: {.summary}
-   -   []{#constructor.summary}

        ### Constructor Summary

          Constructor and Description
          -------------------------------------------------------------------------------------------------------------------------------------
          `SEdge(au.gov.asd.tac.constellation.graph.GraphReadMethods readableGraph,                                                 int id)` 

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
        | `SVertex`                         | `destinationVertex()`             |
        |                                   | ::: {.block}                      |
        |                                   | Get the destination vertex of     |
        |                                   | this edge.                        |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `int`                             | `direction()`                     |
        |                                   | ::: {.block}                      |
        |                                   | Get the direction of this edge.   |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `boolean`                         | `equals(java.lang.Object obj)`    |
        +-----------------------------------+-----------------------------------+
        | `int`                             | `hashCode()`                      |
        +-----------------------------------+-----------------------------------+
        | `int`                             | `id()`                            |
        |                                   | ::: {.block}                      |
        |                                   | Get the id of this edge.          |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `SVertex`                         | `otherVertex(SVertex vertex)`     |
        |                                   | ::: {.block}                      |
        |                                   | Get the other vertex of this edge |
        |                                   | given one end.                    |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `SVertex`                         | `sourceVertex()`                  |
        |                                   | ::: {.block}                      |
        |                                   | Get the source vertex of this     |
        |                                   | edge.                             |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `java.lang.String`                | `toString()`                      |
        +-----------------------------------+-----------------------------------+
        | `int`                             | `transactionCount()`              |
        |                                   | ::: {.block}                      |
        |                                   | Get the number of transactions    |
        |                                   | this edge represents.             |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `SEdgeTransactionIterator`        | `transactions()`                  |
        |                                   | ::: {.block}                      |
        |                                   | Get an iterator over transactions |
        |                                   | this edge represents.             |
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

        []{#SEdge-au.gov.asd.tac.constellation.graph.GraphReadMethods-int-}

        -   #### SEdge

                public SEdge(au.gov.asd.tac.constellation.graph.GraphReadMethods readableGraph,
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
            Get the id of this edge.
            :::

            [Returns:]{.returnLabel}
            :   the id of this edge.

        []{#direction--}

        -   #### direction

                public int direction()

            ::: {.block}
            Get the direction of this edge.
            :::

            [Returns:]{.returnLabel}
            :   0 if the direction is \'uphill\' (the id of the source
                vertex is lower than the id of the destination vertex),
                1 if the direction is \'downhill\' (the id of the source
                vertex is higher than the id of the destination vertex),
                or 2 if the direction is \'flat\' (the source id and
                destination id are equal, ie. a loop).

        []{#sourceVertex--}

        -   #### sourceVertex

                public SVertex sourceVertex()

            ::: {.block}
            Get the source vertex of this edge.
            :::

            [Returns:]{.returnLabel}
            :   the source vertex as a
                [`SVertex`](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SVertex.html "class in au.gov.asd.tac.constellation.views.scripting.graph").

        []{#destinationVertex--}

        -   #### destinationVertex

                public SVertex destinationVertex()

            ::: {.block}
            Get the destination vertex of this edge.
            :::

            [Returns:]{.returnLabel}
            :   the destination vertex as a
                [`SVertex`](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SVertex.html "class in au.gov.asd.tac.constellation.views.scripting.graph").

        []{#otherVertex-au.gov.asd.tac.constellation.views.scripting.graph.SVertex-}

        -   #### otherVertex

                public SVertex otherVertex(SVertex vertex)

            ::: {.block}
            Get the other vertex of this edge given one end.
            :::

            [Parameters:]{.paramLabel}
            :   `vertex` - the vertex at one end of this edge.

            [Returns:]{.returnLabel}
            :   the vertex at the other end of this edge as a
                [`SVertex`](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SVertex.html "class in au.gov.asd.tac.constellation.views.scripting.graph").

        []{#transactionCount--}

        -   #### transactionCount

                public int transactionCount()

            ::: {.block}
            Get the number of transactions this edge represents.
            :::

            [Returns:]{.returnLabel}
            :   the number of transactions.

        []{#transactions--}

        -   #### transactions

                public SEdgeTransactionIterator transactions()

            ::: {.block}
            Get an iterator over transactions this edge represents.
            :::

            [Returns:]{.returnLabel}
            :   a transaction iterator.

        []{#Z:Z__getitem__-java.lang.Object-}

        -   #### \_\_getitem\_\_

                public java.lang.Object __getitem__(java.lang.Object key)

            ::: {.block}
            Python evaluation of self\[key\].
            :::

            [Parameters:]{.paramLabel}
            :   `key` - an object representing the attribute to query.

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
            :   `key` - an object representing the attribute to set.
            :   `value` - the new value for the specified attribute.

            [Returns:]{.returnLabel}
            :   the value that was set.

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
-   [[Prev Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SCollection.html "class in au.gov.asd.tac.constellation.views.scripting.graph")
-   [[Next Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SGraph.html "class in au.gov.asd.tac.constellation.views.scripting.graph")

```{=html}
<!-- -->
```
-   [Frames](../../../../../../../index.html?au/gov/asd/tac/constellation/scripting/graph/SEdge.html)
-   [No Frames](SEdge.html)

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
