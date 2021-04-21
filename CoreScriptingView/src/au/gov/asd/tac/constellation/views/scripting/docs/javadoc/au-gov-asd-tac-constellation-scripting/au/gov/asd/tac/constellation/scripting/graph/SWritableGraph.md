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
-   [[Prev Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SVertex.html "class in au.gov.asd.tac.constellation.views.scripting.graph")
-   Next Class

```{=html}
<!-- -->
```
-   [Frames](../../../../../../../index.html?au/gov/asd/tac/constellation/scripting/graph/SWritableGraph.html)
-   [No Frames](SWritableGraph.html)

```{=html}
<!-- -->
```
-   [All Classes](../../../../../../../allclasses-noframe.html)

<div>

</div>

<div>

-   Summary: 
-   Nested \| 
-   [Field](#fields.inherited.from.class.au.gov.asd.tac.constellation.views.scripting.graph.SReadableGraph) \| 
-   Constr \| 
-   [Method](#method.summary)

```{=html}
<!-- -->
```
-   Detail: 
-   Field \| 
-   Constr \| 
-   [Method](#method.detail)

</div>

[]{#skip.navbar.top}
:::

::: {.header}
::: {.subTitle}
au.gov.asd.tac.constellation.views.scripting.graph
:::

## Class SWritableGraph {#class-swritablegraph .title title="Class SWritableGraph"}
:::

::: {.contentContainer}
-   java.lang.Object

-   -   [au.gov.asd.tac.constellation.views.scripting.graph.SReadableGraph](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SReadableGraph.html "class in au.gov.asd.tac.constellation.views.scripting.graph")

    -   -   au.gov.asd.tac.constellation.views.scripting.graph.SWritableGraph

::: {.description}
-   

    ------------------------------------------------------------------------

    \

        public class SWritableGraph
        extends SReadableGraph

    ::: {.block}
    A representation of a graph write lock for use with scripting.
    :::

    [Author:]{.simpleTagLabel}
    :   algol, cygnus_x-1
:::

::: {.summary}
-   -   []{#field.summary}

        ### Field Summary

        -   []{#fields.inherited.from.class.au.gov.asd.tac.constellation.views.scripting.graph.SReadableGraph}

            ### Fields inherited from class au.gov.asd.tac.constellation.views.scripting.graph.[SReadableGraph](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SReadableGraph.html "class in au.gov.asd.tac.constellation.views.scripting.graph")

            `graph, readableGraph`

    ```{=html}
    <!-- -->
    ```
    -   []{#method.summary}

        ### Method Summary

        +-----------------------------------+-----------------------------------+
        | Modifier and Type                 | Method and Description            |
        +===================================+===================================+
        | `SWritableGraph`                  | `__enter__()`                     |
        |                                   | ::: {.block}                      |
        |                                   | The entry point for the Python    |
        |                                   | context manager.                  |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `void`                            | `__exit__(java.lang.Obje          |
        |                                   | ct exc_type,                      |
        |                                   |                             java. |
        |                                   | lang.Object exc_value,            |
        |                                   |                                   |
        |                                   |      java.lang.Object traceback)` |
        |                                   | ::: {.block}                      |
        |                                   | The exit point for the Python     |
        |                                   | context manager.                  |
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
        | `SAttribute`                      | `addAttri                         |
        |                                   | bute(au.gov.asd.tac.constellation |
        |                                   | .graph.GraphElementType elementTy |
        |                                   | pe,                               |
        |                                   |                    java.lang.Stri |
        |                                   | ng attributeType,                 |
        |                                   |                                   |
        |                                   | java.lang.String name,            |
        |                                   |                                   |
        |                                   |      java.lang.String description |
        |                                   | ,                                 |
        |                                   |                  java.lang.Object |
        |                                   |  defaultValue,                    |
        |                                   |                               jav |
        |                                   | a.lang.String attributeMergerId)` |
        |                                   | ::: {.block}                      |
        |                                   | Add a new attribute to the graph. |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `STransaction`                    | `addTransactio                    |
        |                                   | n(SVertex sourceVertex,           |
        |                                   |                                   |
        |                                   |       SVertex destinationVertex,  |
        |                                   |                                   |
        |                                   |                boolean directed)` |
        |                                   | ::: {.block}                      |
        |                                   | Add a transaction to the graph.   |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `SVertex`                         | `addVertex()`                     |
        |                                   | ::: {.block}                      |
        |                                   | Add a vertex to the graph.        |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `void`                            | `commit()`                        |
        |                                   | ::: {.block}                      |
        |                                   | Commit changes made to the graph  |
        |                                   | using this write lock, then       |
        |                                   | release the lock.                 |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `SAttribute`                      | `ensureAttribute(au.g             |
        |                                   | ov.asd.tac.constellation.graph.Gr |
        |                                   | aphElementType elementType,       |
        |                                   |                                   |
        |                                   |           java.lang.String name)` |
        |                                   | ::: {.block}                      |
        |                                   | Get a schema attribute from the   |
        |                                   | graph if it exists, otherwise     |
        |                                   | create it.                        |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `protected au.gov.asd.tac.c       | `getWritableGraph()`              |
        | onstellation.graph.WritableGraph` | ::: {.block}                      |
        |                                   | Get the actual write lock on the  |
        |                                   | graph that this object            |
        |                                   | represents.                       |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `void`                            | `mergeVertices(int leadVertex,    |
        |                                   |                                   |
        |                                   |                 int mergeVertex)` |
        |                                   | ::: {.block}                      |
        |                                   | Merge two vertices together.      |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `void`                            | `m                                |
        |                                   | ergeVertices(int leadVertex,      |
        |                                   |                                   |
        |                                   |            SCollection vertices)` |
        |                                   | ::: {.block}                      |
        |                                   | Merge more than two vertices      |
        |                                   | together.                         |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `void`                            | `                                 |
        |                                   | removeAttribute(int attributeId)` |
        |                                   | ::: {.block}                      |
        |                                   | Remove an attribute from the      |
        |                                   | graph.                            |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `void`                            | `re                               |
        |                                   | moveTransaction(int transaction)` |
        |                                   | ::: {.block}                      |
        |                                   | Remove a transaction from the     |
        |                                   | graph.                            |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `void`                            | `removeTransa                     |
        |                                   | ctions(SCollection transactions)` |
        |                                   | ::: {.block}                      |
        |                                   | Remove a collection of            |
        |                                   | transactions from the graph.      |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `void`                            | `removeVertex(int vertexId)`      |
        |                                   | ::: {.block}                      |
        |                                   | Remove a vertex from the graph.   |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `void`                            | `remo                             |
        |                                   | veVertices(SCollection vertices)` |
        |                                   | ::: {.block}                      |
        |                                   | Remove a collection of vertices   |
        |                                   | from the graph.                   |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `void`                            | `rollback()`                      |
        |                                   | ::: {.block}                      |
        |                                   | Rollback changes made to the      |
        |                                   | graph using this write lock, then |
        |                                   | release the lock.                 |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `java.lang.String`                | `toString()`                      |
        +-----------------------------------+-----------------------------------+

        : [All Methods[ ]{.tabEnd}]{#t0 .activeTableTab}[[Instance
        Methods](javascript:show(2);)[ ]{.tabEnd}]{#t2
        .tableTab}[[Concrete
        Methods](javascript:show(8);)[ ]{.tabEnd}]{#t4 .tableTab}

        -   []{#methods.inherited.from.class.au.gov.asd.tac.constellation.views.scripting.graph.SReadableGraph}

            ### Methods inherited from class au.gov.asd.tac.constellation.views.scripting.graph.[SReadableGraph](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SReadableGraph.html "class in au.gov.asd.tac.constellation.views.scripting.graph")

            `__getitem__, attribute, attribute, attributeCount, attributeModificationCounter, attributes, edge, edgeCount, edges, filterTransactions, filterVertices, getReadableGraph, globalModificationCounter, hasAttribute, hasEdge, hasLink, hasTransaction, hasVertex, link, link, linkCount, links, release, structureModificationCounter, transaction, transactionCount, transactions, valueModificationCounter, vertex, vertexCount, vertices, withTransactions, withVertices`

        ```{=html}
        <!-- -->
        ```
        -   []{#methods.inherited.from.class.java.lang.Object}

            ### Methods inherited from class java.lang.Object

            `clone, equals, finalize, getClass, hashCode, notify, notifyAll, wait, wait, wait`
:::

::: {.details}
-   -   []{#method.detail}

        ### Method Detail

        []{#getWritableGraph--}

        -   #### getWritableGraph

                protected au.gov.asd.tac.constellation.graph.WritableGraph getWritableGraph()

            ::: {.block}
            Get the actual write lock on the graph that this object
            represents.
            :::

            [Returns:]{.returnLabel}
            :   the write lock on the graph.

        []{#commit--}

        -   #### commit

                public void commit()

            ::: {.block}
            Commit changes made to the graph using this write lock, then
            release the lock. This or
            [`rollback()`](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SWritableGraph.html#rollback--)
            should be called immediately after you finish writing to the
            graph.
            :::

        []{#rollback--}

        -   #### rollback

                public void rollback()

            ::: {.block}
            Rollback changes made to the graph using this write lock,
            then release the lock. This or
            [`commit()`](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SWritableGraph.html#commit--)
            should be called immediately after you finish writing to the
            graph.
            :::

        []{#addAttribute-au.gov.asd.tac.constellation.graph.GraphElementType-java.lang.String-java.lang.String-java.lang.String-java.lang.Object-java.lang.String-}

        -   #### addAttribute

                public SAttribute addAttribute(au.gov.asd.tac.constellation.graph.GraphElementType elementType,
                                               java.lang.String attributeType,
                                               java.lang.String name,
                                               java.lang.String description,
                                               java.lang.Object defaultValue,
                                               java.lang.String attributeMergerId)

            ::: {.block}
            Add a new attribute to the graph.
            :::

            [Parameters:]{.paramLabel}
            :   `elementType` - the element type of the attribute.
            :   `attributeType` - the type of the attribute.
            :   `name` - the name of the attribute.
            :   `description` - a description of the attribute.
            :   `defaultValue` - the default value for the attribute.
            :   `attributeMergerId` - the merger the attribute should
                use.

            [Returns:]{.returnLabel}
            :   the new attribute as a
                [`SAttribute`](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SAttribute.html "class in au.gov.asd.tac.constellation.views.scripting.graph").

        []{#ensureAttribute-au.gov.asd.tac.constellation.graph.GraphElementType-java.lang.String-}

        -   #### ensureAttribute

                public SAttribute ensureAttribute(au.gov.asd.tac.constellation.graph.GraphElementType elementType,
                                                  java.lang.String name)

            ::: {.block}
            Get a schema attribute from the graph if it exists,
            otherwise create it.
            :::

            [Parameters:]{.paramLabel}
            :   `elementType` - the element type of the attribute.
            :   `name` - the name of the attribute.

            [Returns:]{.returnLabel}
            :   the attribute as a
                [`SAttribute`](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SAttribute.html "class in au.gov.asd.tac.constellation.views.scripting.graph").

        []{#removeAttribute-int-}

        -   #### removeAttribute

                public void removeAttribute(int attributeId)

            ::: {.block}
            Remove an attribute from the graph.
            :::

            [Parameters:]{.paramLabel}
            :   `attributeId` - the attribute id.

        []{#addVertex--}

        -   #### addVertex

                public SVertex addVertex()

            ::: {.block}
            Add a vertex to the graph.
            :::

            [Returns:]{.returnLabel}
            :   the new vertex as a
                [`SVertex`](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SVertex.html "class in au.gov.asd.tac.constellation.views.scripting.graph").

        []{#removeVertex-int-}

        -   #### removeVertex

                public void removeVertex(int vertexId)

            ::: {.block}
            Remove a vertex from the graph.
            :::

            [Parameters:]{.paramLabel}
            :   `vertexId` - the vertex id.

        []{#removeVertices-au.gov.asd.tac.constellation.views.scripting.graph.SCollection-}

        -   #### removeVertices

                public void removeVertices(SCollection vertices)

            ::: {.block}
            Remove a collection of vertices from the graph.
            :::

            [Parameters:]{.paramLabel}
            :   `vertices` - the vertices to remove as a
                [`SCollection`](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SCollection.html "class in au.gov.asd.tac.constellation.views.scripting.graph").

        []{#mergeVertices-int-int-}

        -   #### mergeVertices

                public void mergeVertices(int leadVertex,
                                          int mergeVertex)

            ::: {.block}
            Merge two vertices together.
            :::

            [Parameters:]{.paramLabel}
            :   `leadVertex` - the id of the consuming vertex.
            :   `mergeVertex` - the id of the consumed vertex.

        []{#mergeVertices-int-au.gov.asd.tac.constellation.views.scripting.graph.SCollection-}

        -   #### mergeVertices

                public void mergeVertices(int leadVertex,
                                          SCollection vertices)

            ::: {.block}
            Merge more than two vertices together.
            :::

            [Parameters:]{.paramLabel}
            :   `leadVertex` - the id of the consuming vertex.
            :   `vertices` - the consumed vertices as a
                [`SCollection`](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SCollection.html "class in au.gov.asd.tac.constellation.views.scripting.graph").

        []{#addTransaction-au.gov.asd.tac.constellation.views.scripting.graph.SVertex-au.gov.asd.tac.constellation.views.scripting.graph.SVertex-boolean-}

        -   #### addTransaction

                public STransaction addTransaction(SVertex sourceVertex,
                                                   SVertex destinationVertex,
                                                   boolean directed)

            ::: {.block}
            Add a transaction to the graph.
            :::

            [Parameters:]{.paramLabel}
            :   `sourceVertex` - the id of the source vertex.
            :   `destinationVertex` - the id of the destination vertex.
            :   `directed` - should the transaction be directed?

            [Returns:]{.returnLabel}
            :   the new transaction as a
                [`STransaction`](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/STransaction.html "class in au.gov.asd.tac.constellation.views.scripting.graph").

        []{#removeTransaction-int-}

        -   #### removeTransaction

                public void removeTransaction(int transaction)

            ::: {.block}
            Remove a transaction from the graph.
            :::

            [Parameters:]{.paramLabel}
            :   `transaction` - the id of the transaction.

        []{#removeTransactions-au.gov.asd.tac.constellation.views.scripting.graph.SCollection-}

        -   #### removeTransactions

                public void removeTransactions(SCollection transactions)

            ::: {.block}
            Remove a collection of transactions from the graph.
            :::

            [Parameters:]{.paramLabel}
            :   `transactions` - the transaction to remove as a
                [`STransaction`](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/STransaction.html "class in au.gov.asd.tac.constellation.views.scripting.graph").

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

        []{#Z:Z__enter__--}

        -   #### \_\_enter\_\_

                public SWritableGraph __enter__()

            ::: {.block}
            The entry point for the Python context manager.
            :::

            [Overrides:]{.overrideSpecifyLabel}
            :   `__enter__` in class `SReadableGraph`

            [Returns:]{.returnLabel}
            :   a write lock on the graph.

        []{#Z:Z__exit__-java.lang.Object-java.lang.Object-java.lang.Object-}

        -   #### \_\_exit\_\_

                public void __exit__(java.lang.Object exc_type,
                                     java.lang.Object exc_value,
                                     java.lang.Object traceback)

            ::: {.block}
            The exit point for the Python context manager.
            :::

            [Overrides:]{.overrideSpecifyLabel}
            :   `__exit__` in class `SReadableGraph`

            [Parameters:]{.paramLabel}
            :   `exc_type` - the exception type.
            :   `exc_value` - the exception value.
            :   `traceback` - the exception traceback.

        []{#toString--}

        -   #### toString

                public java.lang.String toString()

            [Overrides:]{.overrideSpecifyLabel}
            :   `toString` in class `SReadableGraph`
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
-   [[Prev Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SVertex.html "class in au.gov.asd.tac.constellation.views.scripting.graph")
-   Next Class

```{=html}
<!-- -->
```
-   [Frames](../../../../../../../index.html?au/gov/asd/tac/constellation/scripting/graph/SWritableGraph.html)
-   [No Frames](SWritableGraph.html)

```{=html}
<!-- -->
```
-   [All Classes](../../../../../../../allclasses-noframe.html)

<div>

</div>

<div>

-   Summary: 
-   Nested \| 
-   [Field](#fields.inherited.from.class.au.gov.asd.tac.constellation.views.scripting.graph.SReadableGraph) \| 
-   Constr \| 
-   [Method](#method.summary)

```{=html}
<!-- -->
```
-   Detail: 
-   Field \| 
-   Constr \| 
-   [Method](#method.detail)

</div>

[]{#skip.navbar.bottom}
:::
