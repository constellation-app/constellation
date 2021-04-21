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
-   [[Prev Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SGraph.html "class in au.gov.asd.tac.constellation.views.scripting.graph")
-   [[Next Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SReadableGraph.html "class in au.gov.asd.tac.constellation.views.scripting.graph")

```{=html}
<!-- -->
```
-   [Frames](../../../../../../../index.html?au/gov/asd/tac/constellation/scripting/graph/SLink.html)
-   [No Frames](SLink.html)

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

## Class SLink {#class-slink .title title="Class SLink"}
:::

::: {.contentContainer}
-   java.lang.Object

-   -   au.gov.asd.tac.constellation.views.scripting.graph.SLink

::: {.description}
-   

    ------------------------------------------------------------------------

    \

        public class SLink
        extends java.lang.Object

    ::: {.block}
    A representation of a link for use with scripting.
    :::

    [Author:]{.simpleTagLabel}
    :   cygnus_x-1
:::

::: {.summary}
-   -   []{#constructor.summary}

        ### Constructor Summary

          Constructor and Description
          --------------------------------------------------------------------------------------------------------------------------
          `SLink(au.gov.asd.tac.constellation.graph.GraphReadMethods rg,                                                 int id)` 

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
        | `int`                             | `edgeCount()`                     |
        |                                   | ::: {.block}                      |
        |                                   | Get the number of edges this link |
        |                                   | represents.                       |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `SLinkEdgeIterator`               | `edges()`                         |
        |                                   | ::: {.block}                      |
        |                                   | Get an iterator over edges this   |
        |                                   | link represents.                  |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `boolean`                         | `equals(java.lang.Object obj)`    |
        +-----------------------------------+-----------------------------------+
        | `int`                             | `hashCode()`                      |
        +-----------------------------------+-----------------------------------+
        | `SVertex`                         | `highVertex()`                    |
        |                                   | ::: {.block}                      |
        |                                   | Get the vertex the end of this    |
        |                                   | link with the higher id.          |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `int`                             | `id()`                            |
        |                                   | ::: {.block}                      |
        |                                   | Get the id of this link.          |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `SVertex`                         | `lowVertex()`                     |
        |                                   | ::: {.block}                      |
        |                                   | Get the vertex the end of this    |
        |                                   | link with the lower id.           |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `SVertex`                         | `otherVertex(SVertex vertex)`     |
        |                                   | ::: {.block}                      |
        |                                   | Get the other vertex of this link |
        |                                   | given one end.                    |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `java.lang.String`                | `toString()`                      |
        +-----------------------------------+-----------------------------------+
        | `int`                             | `transactionCount()`              |
        |                                   | ::: {.block}                      |
        |                                   | Get the number of transactions    |
        |                                   | this link represents.             |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `SLinkTransactionIterator`        | `transactions()`                  |
        |                                   | ::: {.block}                      |
        |                                   | Get an iterator over transactions |
        |                                   | this link represents.             |
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

        []{#SLink-au.gov.asd.tac.constellation.graph.GraphReadMethods-int-}

        -   #### SLink

                public SLink(au.gov.asd.tac.constellation.graph.GraphReadMethods rg,
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
            Get the id of this link.
            :::

            [Returns:]{.returnLabel}
            :   the id of this link.

        []{#highVertex--}

        -   #### highVertex

                public SVertex highVertex()

            ::: {.block}
            Get the vertex the end of this link with the higher id.
            :::

            [Returns:]{.returnLabel}
            :   a vertex as a
                [`SVertex`](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SVertex.html "class in au.gov.asd.tac.constellation.views.scripting.graph").

        []{#lowVertex--}

        -   #### lowVertex

                public SVertex lowVertex()

            ::: {.block}
            Get the vertex the end of this link with the lower id.
            :::

            [Returns:]{.returnLabel}
            :   a vertex as a
                [`SVertex`](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SVertex.html "class in au.gov.asd.tac.constellation.views.scripting.graph").

        []{#otherVertex-au.gov.asd.tac.constellation.views.scripting.graph.SVertex-}

        -   #### otherVertex

                public SVertex otherVertex(SVertex vertex)

            ::: {.block}
            Get the other vertex of this link given one end.
            :::

            [Parameters:]{.paramLabel}
            :   `vertex` - the vertex at one end of this link.

            [Returns:]{.returnLabel}
            :   the vertex at the other end of this link as a
                [`SVertex`](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SVertex.html "class in au.gov.asd.tac.constellation.views.scripting.graph").

        []{#transactionCount--}

        -   #### transactionCount

                public int transactionCount()

            ::: {.block}
            Get the number of transactions this link represents.
            :::

            [Returns:]{.returnLabel}
            :   the number of transactions.

        []{#transactions--}

        -   #### transactions

                public SLinkTransactionIterator transactions()

            ::: {.block}
            Get an iterator over transactions this link represents.
            :::

            [Returns:]{.returnLabel}
            :   a transaction iterator.

        []{#edgeCount--}

        -   #### edgeCount

                public int edgeCount()

            ::: {.block}
            Get the number of edges this link represents.
            :::

            [Returns:]{.returnLabel}
            :   the number of edges.

        []{#edges--}

        -   #### edges

                public SLinkEdgeIterator edges()

            ::: {.block}
            Get an iterator over edges this link represents.
            :::

            [Returns:]{.returnLabel}
            :   an edge iterator.

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
-   [[Prev Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SGraph.html "class in au.gov.asd.tac.constellation.views.scripting.graph")
-   [[Next Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SReadableGraph.html "class in au.gov.asd.tac.constellation.views.scripting.graph")

```{=html}
<!-- -->
```
-   [Frames](../../../../../../../index.html?au/gov/asd/tac/constellation/scripting/graph/SLink.html)
-   [No Frames](SLink.html)

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
