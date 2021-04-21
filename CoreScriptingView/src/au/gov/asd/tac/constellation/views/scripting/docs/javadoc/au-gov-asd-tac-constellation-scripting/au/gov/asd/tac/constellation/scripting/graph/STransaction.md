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
-   [[Prev Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SReadableGraph.html "class in au.gov.asd.tac.constellation.views.scripting.graph")
-   [[Next Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SVertex.html "class in au.gov.asd.tac.constellation.views.scripting.graph")

```{=html}
<!-- -->
```
-   [Frames](../../../../../../../index.html?au/gov/asd/tac/constellation/scripting/graph/STransaction.html)
-   [No Frames](STransaction.html)

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

## Class STransaction {#class-stransaction .title title="Class STransaction"}
:::

::: {.contentContainer}
-   java.lang.Object

-   -   au.gov.asd.tac.constellation.views.scripting.graph.STransaction

::: {.description}
-   

    ------------------------------------------------------------------------

    \

        public class STransaction
        extends java.lang.Object

    ::: {.block}
    A representation of a transaction for use with scripting.
    :::

    [Author:]{.simpleTagLabel}
    :   algol, cygnus_x-1
:::

::: {.summary}
-   -   []{#constructor.summary}

        ### Constructor Summary

          Constructor and Description
          ---------------------------------------------------------------------------------------------------------------------------------
          `STransaction(au.gov.asd.tac.constellation.graph.GraphReadMethods rg,                                                 int id)` 

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
        |                                   | Complete this transaction using   |
        |                                   | the schema of the graph.          |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `SVertex`                         | `destinationVertex()`             |
        |                                   | ::: {.block}                      |
        |                                   | Get the destination vertex of     |
        |                                   | this transaction.                 |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `int`                             | `direction()`                     |
        |                                   | ::: {.block}                      |
        |                                   | Get the direction of this         |
        |                                   | transaction.                      |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `boolean`                         | `equals(java.lang.Object obj)`    |
        +-----------------------------------+-----------------------------------+
        | `int`                             | `hashCode()`                      |
        +-----------------------------------+-----------------------------------+
        | `int`                             | `id()`                            |
        |                                   | ::: {.block}                      |
        |                                   | Get the id of this transaction.   |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `void`                            | `init()`                          |
        |                                   | ::: {.block}                      |
        |                                   | Initialise this transaction using |
        |                                   | the schema of the graph.          |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `SVertex`                         | `otherVertex(SVertex vx)`         |
        +-----------------------------------+-----------------------------------+
        | `SVertex`                         | `sourceVertex()`                  |
        |                                   | ::: {.block}                      |
        |                                   | Get the source vertex of this     |
        |                                   | transaction.                      |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `java.lang.String`                | `toString()`                      |
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

        []{#STransaction-au.gov.asd.tac.constellation.graph.GraphReadMethods-int-}

        -   #### STransaction

                public STransaction(au.gov.asd.tac.constellation.graph.GraphReadMethods rg,
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
            Get the id of this transaction.
            :::

            [Returns:]{.returnLabel}
            :   the id of this transaction.

        []{#init--}

        -   #### init

                public void init()

            ::: {.block}
            Initialise this transaction using the schema of the graph.
            :::

        []{#complete--}

        -   #### complete

                public void complete()

            ::: {.block}
            Complete this transaction using the schema of the graph.
            :::

        []{#direction--}

        -   #### direction

                public int direction()

            ::: {.block}
            Get the direction of this transaction.
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
            Get the source vertex of this transaction.
            :::

            [Returns:]{.returnLabel}
            :   the source vertex as a
                [`SVertex`](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SVertex.html "class in au.gov.asd.tac.constellation.views.scripting.graph").

        []{#destinationVertex--}

        -   #### destinationVertex

                public SVertex destinationVertex()

            ::: {.block}
            Get the destination vertex of this transaction.
            :::

            [Returns:]{.returnLabel}
            :   the destination vertex as a
                [`SVertex`](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SVertex.html "class in au.gov.asd.tac.constellation.views.scripting.graph").

        []{#otherVertex-au.gov.asd.tac.constellation.views.scripting.graph.SVertex-}

        -   #### otherVertex

                public SVertex otherVertex(SVertex vx)

        []{#Z:Z__getitem__-java.lang.Object-}

        -   #### \_\_getitem\_\_

                public java.lang.Object __getitem__(java.lang.Object key)

            ::: {.block}
            Python evaluation of self\[key\].
            :::

            [Parameters:]{.paramLabel}
            :   `key` - an object representing the attribute to query.

            [Returns:]{.returnLabel}
            :   the value for the specified attribute.

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
-   [[Prev Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SReadableGraph.html "class in au.gov.asd.tac.constellation.views.scripting.graph")
-   [[Next Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SVertex.html "class in au.gov.asd.tac.constellation.views.scripting.graph")

```{=html}
<!-- -->
```
-   [Frames](../../../../../../../index.html?au/gov/asd/tac/constellation/scripting/graph/STransaction.html)
-   [No Frames](STransaction.html)

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
