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
-   [[Prev Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SAttribute.html "class in au.gov.asd.tac.constellation.views.scripting.graph")
-   [[Next Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SEdge.html "class in au.gov.asd.tac.constellation.views.scripting.graph")

```{=html}
<!-- -->
```
-   [Frames](../../../../../../../index.html?au/gov/asd/tac/constellation/scripting/graph/SCollection.html)
-   [No Frames](SCollection.html)

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

## Class SCollection {#class-scollection .title title="Class SCollection"}
:::

::: {.contentContainer}
-   java.lang.Object

-   -   au.gov.asd.tac.constellation.views.scripting.graph.SCollection

::: {.description}
-   

    ------------------------------------------------------------------------

    \

        public class SCollection
        extends java.lang.Object

    ::: {.block}
    A collection which efficiently stores vertices or transactions for
    use with scripting.
    :::

    [Author:]{.simpleTagLabel}
    :   algol
:::

::: {.summary}
-   -   []{#constructor.summary}

        ### Constructor Summary

          Constructor and Description
          -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
          `SCollection(javax.script.ScriptEngine engine,                                                 au.gov.asd.tac.constellation.graph.GraphReadMethods readableGraph,                                                 au.gov.asd.tac.constellation.graph.GraphElementType elementType,                                                 java.util.BitSet elementIds)` 

          : Constructors[ ]{.tabEnd}

    ```{=html}
    <!-- -->
    ```
    -   []{#method.summary}

        ### Method Summary

        +-----------------------------------+-----------------------------------+
        | Modifier and Type                 | Method and Description            |
        +===================================+===================================+
        | `java.util.BitSet`                | `elementIds()`                    |
        |                                   | ::: {.block}                      |
        |                                   | Get the ids of elements in this   |
        |                                   | collection.                       |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `au.gov.asd.tac.cons              | `elementType()`                   |
        | tellation.graph.GraphElementType` | ::: {.block}                      |
        |                                   | Get the element type of elements  |
        |                                   | in this collection.               |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `boolean`                         | `empty()`                         |
        |                                   | ::: {.block}                      |
        |                                   | Check if this collection is       |
        |                                   | empty.                            |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `SCollection`                     | `f                                |
        |                                   | ilter(java.lang.Object callback)` |
        |                                   | ::: {.block}                      |
        |                                   | Filter this collection using the  |
        |                                   | provided function.                |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `SCollection`                     | `intersection(SCollection other)` |
        |                                   | ::: {.block}                      |
        |                                   | Get the intersection of this      |
        |                                   | collection with another.          |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `int`                             | `size()`                          |
        |                                   | ::: {.block}                      |
        |                                   | Get the size of this collection.  |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `java.lang.String`                | `toString()`                      |
        +-----------------------------------+-----------------------------------+
        | `SCollection`                     | `union(SCollection other)`        |
        |                                   | ::: {.block}                      |
        |                                   | Get the union of this collection  |
        |                                   | with another.                     |
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
-   -   []{#constructor.detail}

        ### Constructor Detail

        []{#SCollection-javax.script.ScriptEngine-au.gov.asd.tac.constellation.graph.GraphReadMethods-au.gov.asd.tac.constellation.graph.GraphElementType-java.util.BitSet-}

        -   #### SCollection

                public SCollection(javax.script.ScriptEngine engine,
                                   au.gov.asd.tac.constellation.graph.GraphReadMethods readableGraph,
                                   au.gov.asd.tac.constellation.graph.GraphElementType elementType,
                                   java.util.BitSet elementIds)

    ```{=html}
    <!-- -->
    ```
    -   []{#method.detail}

        ### Method Detail

        []{#elementType--}

        -   #### elementType

                public au.gov.asd.tac.constellation.graph.GraphElementType elementType()

            ::: {.block}
            Get the element type of elements in this collection.
            :::

            [Returns:]{.returnLabel}
            :   the element type.

        []{#elementIds--}

        -   #### elementIds

                public java.util.BitSet elementIds()

            ::: {.block}
            Get the ids of elements in this collection.
            :::

            [Returns:]{.returnLabel}
            :   the element ids.

        []{#filter-java.lang.Object-}

        -   #### filter

                public SCollection filter(java.lang.Object callback)
                                   throws javax.script.ScriptException

            ::: {.block}
            Filter this collection using the provided function. The
            provided function must only return true or false. Note: This
            method will only work for Python scripts as it makes use of
            Python specific syntax.
            :::

            [Parameters:]{.paramLabel}
            :   `callback` - the function which returns true or false.

            [Returns:]{.returnLabel}
            :   a collection of elements as a
                [`SCollection`](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SCollection.html "class in au.gov.asd.tac.constellation.views.scripting.graph").

            [Throws:]{.throwsLabel}
            :   `javax.script.ScriptException`

        []{#intersection-au.gov.asd.tac.constellation.views.scripting.graph.SCollection-}

        -   #### intersection

                public SCollection intersection(SCollection other)

            ::: {.block}
            Get the intersection of this collection with another.
            :::

            [Parameters:]{.paramLabel}
            :   `other` - the other collection as a
                [`SCollection`](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SCollection.html "class in au.gov.asd.tac.constellation.views.scripting.graph").

            [Returns:]{.returnLabel}
            :   the intersection of this collection and another as a
                [`SCollection`](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SCollection.html "class in au.gov.asd.tac.constellation.views.scripting.graph").

        []{#union-au.gov.asd.tac.constellation.views.scripting.graph.SCollection-}

        -   #### union

                public SCollection union(SCollection other)

            ::: {.block}
            Get the union of this collection with another.
            :::

            [Parameters:]{.paramLabel}
            :   `other` - the other collection as a
                [`SCollection`](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SCollection.html "class in au.gov.asd.tac.constellation.views.scripting.graph").

            [Returns:]{.returnLabel}
            :   the union of this collection and another as a
                [`SCollection`](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SCollection.html "class in au.gov.asd.tac.constellation.views.scripting.graph").

        []{#empty--}

        -   #### empty

                public boolean empty()

            ::: {.block}
            Check if this collection is empty.
            :::

            [Returns:]{.returnLabel}
            :   true if this collection is empty, false otherwise.

        []{#size--}

        -   #### size

                public int size()

            ::: {.block}
            Get the size of this collection.
            :::

            [Returns:]{.returnLabel}
            :   the size of this collection.

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
-   [[Prev Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SAttribute.html "class in au.gov.asd.tac.constellation.views.scripting.graph")
-   [[Next Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SEdge.html "class in au.gov.asd.tac.constellation.views.scripting.graph")

```{=html}
<!-- -->
```
-   [Frames](../../../../../../../index.html?au/gov/asd/tac/constellation/scripting/graph/SCollection.html)
-   [No Frames](SCollection.html)

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
