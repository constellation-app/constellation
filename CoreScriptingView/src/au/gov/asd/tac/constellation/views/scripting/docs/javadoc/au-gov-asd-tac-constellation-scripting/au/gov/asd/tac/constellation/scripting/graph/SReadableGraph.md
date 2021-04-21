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
-   [[Prev Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SLink.html "class in au.gov.asd.tac.constellation.views.scripting.graph")
-   [[Next Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/STransaction.html "class in au.gov.asd.tac.constellation.views.scripting.graph")

```{=html}
<!-- -->
```
-   [Frames](../../../../../../../index.html?au/gov/asd/tac/constellation/scripting/graph/SReadableGraph.html)
-   [No Frames](SReadableGraph.html)

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
-   Constr \| 
-   [Method](#method.summary)

```{=html}
<!-- -->
```
-   Detail: 
-   [Field](#field.detail) \| 
-   Constr \| 
-   [Method](#method.detail)

</div>

[]{#skip.navbar.top}
:::

::: {.header}
::: {.subTitle}
au.gov.asd.tac.constellation.views.scripting.graph
:::

## Class SReadableGraph {#class-sreadablegraph .title title="Class SReadableGraph"}
:::

::: {.contentContainer}
-   java.lang.Object

-   -   au.gov.asd.tac.constellation.views.scripting.graph.SReadableGraph

::: {.description}
-   

    Direct Known Subclasses:
    :   [SWritableGraph](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SWritableGraph.html "class in au.gov.asd.tac.constellation.views.scripting.graph")

    ------------------------------------------------------------------------

    \

        public class SReadableGraph
        extends java.lang.Object

    ::: {.block}
    A representation of a read lock on a graph for use with scripting.
    :::

    [Author:]{.simpleTagLabel}
    :   algol, cygnus_x-1
:::

::: {.summary}
-   -   []{#field.summary}

        ### Field Summary

          Modifier and Type                                              Field and Description
          -------------------------------------------------------------- -----------------------
          `protected SGraph`                                             `graph` 
          `protected au.gov.asd.tac.constellation.graph.ReadableGraph`   `readableGraph` 

          : Fields[ ]{.tabEnd}

    ```{=html}
    <!-- -->
    ```
    -   []{#method.summary}

        ### Method Summary

        +-----------------------------------+-----------------------------------+
        | Modifier and Type                 | Method and Description            |
        +===================================+===================================+
        | `protected SReadableGraph`        | `__enter__()`                     |
        |                                   | ::: {.block}                      |
        |                                   | The entry point for the Python    |
        |                                   | context manager.                  |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `protected void`                  | `__exit__(java.lang.Obje          |
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
        | `protected java.lang.Object`      | `_                                |
        |                                   | _getitem__(java.lang.Object key)` |
        |                                   | ::: {.block}                      |
        |                                   | Python assignment to self\[key\]. |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `SAttribute`                      | `attribute(au.g                   |
        |                                   | ov.asd.tac.constellation.graph.Gr |
        |                                   | aphElementType elementType,       |
        |                                   |                                   |
        |                                   |           java.lang.String name)` |
        |                                   | ::: {.block}                      |
        |                                   | Get an attribute from the graph   |
        |                                   | by name.                          |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `SAttribute`                      | `attribute(int attributeId)`      |
        |                                   | ::: {.block}                      |
        |                                   | Get an attribute from the graph   |
        |                                   | by id.                            |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `int`                             | `attributeCount                   |
        |                                   | (au.gov.asd.tac.constellation.gra |
        |                                   | ph.GraphElementType elementType)` |
        |                                   | ::: {.block}                      |
        |                                   | Get the number of attributes on   |
        |                                   | the graph for a specific element  |
        |                                   | type.                             |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `long`                            | `attributeModificationCounter()`  |
        |                                   | ::: {.block}                      |
        |                                   | Get a count representing the      |
        |                                   | number of modifications that have |
        |                                   | occurred which modified           |
        |                                   | attributes on the graph.          |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `SAttributeIterator`              | `attributes                       |
        |                                   | (au.gov.asd.tac.constellation.gra |
        |                                   | ph.GraphElementType elementType)` |
        |                                   | ::: {.block}                      |
        |                                   | Get an iterator over attributes   |
        |                                   | of the specified element type.    |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `SEdge`                           | `edge(int edgeId)`                |
        |                                   | ::: {.block}                      |
        |                                   | Get a edge on the graph by id.    |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `int`                             | `edgeCount()`                     |
        |                                   | ::: {.block}                      |
        |                                   | Get the number of edges on the    |
        |                                   | graph.                            |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `SEdgeIterator`                   | `edges()`                         |
        |                                   | ::: {.block}                      |
        |                                   | Get an iterator over edges.       |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `SCollection`                     | `filterTransac                    |
        |                                   | tions(java.lang.Object callback)` |
        |                                   | ::: {.block}                      |
        |                                   | Evaluate a function against each  |
        |                                   | transaction on the graph in order |
        |                                   | to gather a filtered collection.  |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `SCollection`                     | `filterVer                        |
        |                                   | tices(java.lang.Object callback)` |
        |                                   | ::: {.block}                      |
        |                                   | Evaluate a function against each  |
        |                                   | vertex on the graph in order to   |
        |                                   | gather a filtered collection.     |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `protected au.gov.asd.tac.c       | `getReadableGraph()`              |
        | onstellation.graph.ReadableGraph` | ::: {.block}                      |
        |                                   | Get the actual read lock on the   |
        |                                   | graph that this object            |
        |                                   | represents.                       |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `long`                            | `globalModificationCounter()`     |
        |                                   | ::: {.block}                      |
        |                                   | Get a count representing the      |
        |                                   | number of modifications that have |
        |                                   | occurred globally on the graph.   |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `boolean`                         | `hasAttribute(au.g                |
        |                                   | ov.asd.tac.constellation.graph.Gr |
        |                                   | aphElementType elementType,       |
        |                                   |                                   |
        |                                   |           java.lang.String name)` |
        |                                   | ::: {.block}                      |
        |                                   | Check if the specified attribute  |
        |                                   | exists on the graph.              |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `boolean`                         | `hasEdge(int edgeId)`             |
        |                                   | ::: {.block}                      |
        |                                   | Check if the specified edge       |
        |                                   | exists on the graph.              |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `boolean`                         | `hasLink(int linkId)`             |
        |                                   | ::: {.block}                      |
        |                                   | Check if the specified link       |
        |                                   | exists on the graph.              |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `boolean`                         | `h                                |
        |                                   | asTransaction(int transactionId)` |
        |                                   | ::: {.block}                      |
        |                                   | Check if the specified            |
        |                                   | transaction exists on the graph.  |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `boolean`                         | `hasVertex(int vertexId)`         |
        |                                   | ::: {.block}                      |
        |                                   | Check if the specified vertex     |
        |                                   | exists on the graph.              |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `SLink`                           | `link(int linkId)`                |
        |                                   | ::: {.block}                      |
        |                                   | Get a link on the graph by id.    |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `SLink`                           | `lin                              |
        |                                   | k(SVertex sourceVertex,           |
        |                                   |                                   |
        |                                   |       SVertex destinationVertex)` |
        |                                   | ::: {.block}                      |
        |                                   | Get a link on the graph by its    |
        |                                   | endpoints.                        |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `int`                             | `linkCount()`                     |
        |                                   | ::: {.block}                      |
        |                                   | Get the number of links on the    |
        |                                   | graph.                            |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `SLinkIterator`                   | `links()`                         |
        |                                   | ::: {.block}                      |
        |                                   | Get an iterator over links.       |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `void`                            | `release()`                       |
        |                                   | ::: {.block}                      |
        |                                   | Release the read lock on the      |
        |                                   | graph.                            |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `long`                            | `structureModificationCounter()`  |
        |                                   | ::: {.block}                      |
        |                                   | Get a count representing the      |
        |                                   | number of modifications that have |
        |                                   | occurred which modified the       |
        |                                   | structure of the graph.           |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `java.lang.String`                | `toString()`                      |
        +-----------------------------------+-----------------------------------+
        | `STransaction`                    | `transaction(int transactionId)`  |
        |                                   | ::: {.block}                      |
        |                                   | Get a transaction on the graph by |
        |                                   | id.                               |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `int`                             | `transactionCount()`              |
        |                                   | ::: {.block}                      |
        |                                   | Get the number of transactions on |
        |                                   | the graph.                        |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `STransactionIterator`            | `transactions()`                  |
        |                                   | ::: {.block}                      |
        |                                   | Get an iterator over              |
        |                                   | transactions.                     |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `long`                            | `valueModificationCount           |
        |                                   | er(au.gov.asd.tac.constellation.g |
        |                                   | raph.GraphElementType type,       |
        |                                   |                                   |
        |                                   |           java.lang.String name)` |
        |                                   | ::: {.block}                      |
        |                                   | Get a count representing the      |
        |                                   | number of modifications that have |
        |                                   | occurred to the value of a single |
        |                                   | attribute on the graph.           |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `SVertex`                         | `vertex(int vertexId)`            |
        |                                   | ::: {.block}                      |
        |                                   | Get a vertex on the graph by id.  |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `int`                             | `vertexCount()`                   |
        |                                   | ::: {.block}                      |
        |                                   | Get the number of vertices on the |
        |                                   | graph.                            |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `SVertexIterator`                 | `vertices()`                      |
        |                                   | ::: {.block}                      |
        |                                   | Get an iterator over vertices.    |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `void`                            | `withTransac                      |
        |                                   | tions(java.lang.Object callback)` |
        |                                   | ::: {.block}                      |
        |                                   | Evaluate a function against each  |
        |                                   | transaction on the graph.         |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `void`                            | `withVer                          |
        |                                   | tices(java.lang.Object callback)` |
        |                                   | ::: {.block}                      |
        |                                   | Evaluate a function against each  |
        |                                   | vertex on the graph.              |
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

        []{#graph}

        -   #### graph

                protected final SGraph graph

        []{#readableGraph}

        -   #### readableGraph

                protected final au.gov.asd.tac.constellation.graph.ReadableGraph readableGraph

    ```{=html}
    <!-- -->
    ```
    -   []{#method.detail}

        ### Method Detail

        []{#getReadableGraph--}

        -   #### getReadableGraph

                protected au.gov.asd.tac.constellation.graph.ReadableGraph getReadableGraph()

            ::: {.block}
            Get the actual read lock on the graph that this object
            represents.
            :::

            [Returns:]{.returnLabel}
            :   the read lock on the graph.

        []{#release--}

        -   #### release

                public void release()

            ::: {.block}
            Release the read lock on the graph. This should be called
            immediately after you finish reading from the graph.
            :::

        []{#globalModificationCounter--}

        -   #### globalModificationCounter

                public long globalModificationCounter()

            ::: {.block}
            Get a count representing the number of modifications that
            have occurred globally on the graph.
            :::

            [Returns:]{.returnLabel}
            :   the current global modification count.

        []{#structureModificationCounter--}

        -   #### structureModificationCounter

                public long structureModificationCounter()

            ::: {.block}
            Get a count representing the number of modifications that
            have occurred which modified the structure of the graph.
            :::

            [Returns:]{.returnLabel}
            :   the current structure modification count.

        []{#attributeModificationCounter--}

        -   #### attributeModificationCounter

                public long attributeModificationCounter()

            ::: {.block}
            Get a count representing the number of modifications that
            have occurred which modified attributes on the graph.
            :::

            [Returns:]{.returnLabel}
            :   the current attribute modification count.

        []{#valueModificationCounter-au.gov.asd.tac.constellation.graph.GraphElementType-java.lang.String-}

        -   #### valueModificationCounter

                public long valueModificationCounter(au.gov.asd.tac.constellation.graph.GraphElementType type,
                                                     java.lang.String name)

            ::: {.block}
            Get a count representing the number of modifications that
            have occurred to the value of a single attribute on the
            graph.
            :::

            [Parameters:]{.paramLabel}
            :   `type` - the element type of attribute.
            :   `name` - the name of the attribute.

            [Returns:]{.returnLabel}
            :   the current attribute value modification count.

        []{#attributeCount-au.gov.asd.tac.constellation.graph.GraphElementType-}

        -   #### attributeCount

                public int attributeCount(au.gov.asd.tac.constellation.graph.GraphElementType elementType)

            ::: {.block}
            Get the number of attributes on the graph for a specific
            element type.
            :::

            [Parameters:]{.paramLabel}
            :   `elementType` - the element type.

            [Returns:]{.returnLabel}
            :   the number of attributes for the given element type.

        []{#hasAttribute-au.gov.asd.tac.constellation.graph.GraphElementType-java.lang.String-}

        -   #### hasAttribute

                public boolean hasAttribute(au.gov.asd.tac.constellation.graph.GraphElementType elementType,
                                            java.lang.String name)

            ::: {.block}
            Check if the specified attribute exists on the graph.
            :::

            [Parameters:]{.paramLabel}
            :   `elementType` - the element type of the attribute
            :   `name` - the name of the attribute

            [Returns:]{.returnLabel}
            :   true if the attribute exists, false otherwise

        []{#attribute-int-}

        -   #### attribute

                public SAttribute attribute(int attributeId)

            ::: {.block}
            Get an attribute from the graph by id.
            :::

            [Parameters:]{.paramLabel}
            :   `attributeId` - the id of the attribute.

            [Returns:]{.returnLabel}
            :   the requested attribute as a
                [`SAttribute`](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SAttribute.html "class in au.gov.asd.tac.constellation.views.scripting.graph").

        []{#attribute-au.gov.asd.tac.constellation.graph.GraphElementType-java.lang.String-}

        -   #### attribute

                public SAttribute attribute(au.gov.asd.tac.constellation.graph.GraphElementType elementType,
                                            java.lang.String name)
                                     throws au.gov.asd.tac.constellation.views.scripting.graph.exceptions.NoSuchAttributeException

            ::: {.block}
            Get an attribute from the graph by name.
            :::

            [Parameters:]{.paramLabel}
            :   `elementType` - the element type of the attribute.
            :   `name` - the name of the attribute.

            [Returns:]{.returnLabel}
            :   the requested attribute.

            [Throws:]{.throwsLabel}
            :   `au.gov.asd.tac.constellation.views.scripting.graph.exceptions.NoSuchAttributeException`

        []{#attributes-au.gov.asd.tac.constellation.graph.GraphElementType-}

        -   #### attributes

                public SAttributeIterator attributes(au.gov.asd.tac.constellation.graph.GraphElementType elementType)

            ::: {.block}
            Get an iterator over attributes of the specified element
            type.
            :::

            [Parameters:]{.paramLabel}
            :   `elementType` - the element type.

            [Returns:]{.returnLabel}
            :   an attribute iterator.

        []{#vertexCount--}

        -   #### vertexCount

                public int vertexCount()

            ::: {.block}
            Get the number of vertices on the graph.
            :::

            [Returns:]{.returnLabel}
            :   the number of vertices.

        []{#hasVertex-int-}

        -   #### hasVertex

                public boolean hasVertex(int vertexId)

            ::: {.block}
            Check if the specified vertex exists on the graph.
            :::

            [Parameters:]{.paramLabel}
            :   `vertexId` - the vertex id.

            [Returns:]{.returnLabel}
            :   true if the vertex exists, false otherwise.

        []{#vertex-int-}

        -   #### vertex

                public SVertex vertex(int vertexId)

            ::: {.block}
            Get a vertex on the graph by id.
            :::

            [Parameters:]{.paramLabel}
            :   `vertexId` - the vertex id.

            [Returns:]{.returnLabel}
            :   the vertex as a {link SVertex}.

        []{#vertices--}

        -   #### vertices

                public SVertexIterator vertices()

            ::: {.block}
            Get an iterator over vertices.
            :::

            [Returns:]{.returnLabel}
            :   a vertex iterator.

        []{#withVertices-java.lang.Object-}

        -   #### withVertices

                public void withVertices(java.lang.Object callback)
                                  throws javax.script.ScriptException

            ::: {.block}
            Evaluate afunction against each vertex on the graph. Note:
            This method will only work for Python scripts as it makes
            use of Python specific syntax.
            :::

            [Parameters:]{.paramLabel}
            :   `callback` - the function to evaluate.

            [Throws:]{.throwsLabel}
            :   `javax.script.ScriptException`

        []{#filterVertices-java.lang.Object-}

        -   #### filterVertices

                public SCollection filterVertices(java.lang.Object callback)
                                           throws javax.script.ScriptException

            ::: {.block}
            Evaluate a function against each vertex on the graph in
            order to gather a filtered collection. The provided function
            must only return true or false. Note: This method will only
            work for Python scripts as it makes use of Python specific
            syntax.
            :::

            [Parameters:]{.paramLabel}
            :   `callback` - a function which returns true or false.

            [Returns:]{.returnLabel}
            :   a collection of vertices as a
                [`SCollection`](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SCollection.html "class in au.gov.asd.tac.constellation.views.scripting.graph").

            [Throws:]{.throwsLabel}
            :   `javax.script.ScriptException`

        []{#transactionCount--}

        -   #### transactionCount

                public int transactionCount()

            ::: {.block}
            Get the number of transactions on the graph.
            :::

            [Returns:]{.returnLabel}
            :   the number of transactions.

        []{#hasTransaction-int-}

        -   #### hasTransaction

                public boolean hasTransaction(int transactionId)

            ::: {.block}
            Check if the specified transaction exists on the graph.
            :::

            [Parameters:]{.paramLabel}
            :   `transactionId` - the transaction id.

            [Returns:]{.returnLabel}
            :   true if the transaction exists, false otherwise.

        []{#transaction-int-}

        -   #### transaction

                public STransaction transaction(int transactionId)

            ::: {.block}
            Get a transaction on the graph by id.
            :::

            [Parameters:]{.paramLabel}
            :   `transactionId` - the transaction id.

            [Returns:]{.returnLabel}
            :   the transaction as a {link STransaction}.

        []{#transactions--}

        -   #### transactions

                public STransactionIterator transactions()

            ::: {.block}
            Get an iterator over transactions.
            :::

            [Returns:]{.returnLabel}
            :   a transaction iterator.

        []{#withTransactions-java.lang.Object-}

        -   #### withTransactions

                public void withTransactions(java.lang.Object callback)
                                      throws javax.script.ScriptException

            ::: {.block}
            Evaluate a function against each transaction on the graph.
            Note: This method will only work for Python scripts as it
            makes use of Python specific syntax.
            :::

            [Parameters:]{.paramLabel}
            :   `callback` - the function to evaluate.

            [Throws:]{.throwsLabel}
            :   `javax.script.ScriptException`

        []{#filterTransactions-java.lang.Object-}

        -   #### filterTransactions

                public SCollection filterTransactions(java.lang.Object callback)
                                               throws javax.script.ScriptException

            ::: {.block}
            Evaluate a function against each transaction on the graph in
            order to gather a filtered collection. The provided function
            must only return true or false. Note: This method will only
            work for Python scripts as it makes use of Python specific
            syntax.
            :::

            [Parameters:]{.paramLabel}
            :   `callback` - a function which returns true or false.

            [Returns:]{.returnLabel}
            :   a collection of transactions as a
                [`SCollection`](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SCollection.html "class in au.gov.asd.tac.constellation.views.scripting.graph").

            [Throws:]{.throwsLabel}
            :   `javax.script.ScriptException`

        []{#edgeCount--}

        -   #### edgeCount

                public int edgeCount()

            ::: {.block}
            Get the number of edges on the graph.
            :::

            [Returns:]{.returnLabel}
            :   the number of edges.

        []{#hasEdge-int-}

        -   #### hasEdge

                public boolean hasEdge(int edgeId)

            ::: {.block}
            Check if the specified edge exists on the graph.
            :::

            [Parameters:]{.paramLabel}
            :   `edgeId` - the edge id.

            [Returns:]{.returnLabel}
            :   true if the edge exists, false otherwise.

        []{#edge-int-}

        -   #### edge

                public SEdge edge(int edgeId)

            ::: {.block}
            Get a edge on the graph by id.
            :::

            [Parameters:]{.paramLabel}
            :   `edgeId` - the edge id.

            [Returns:]{.returnLabel}
            :   the edge as a {link SEdge}.

        []{#edges--}

        -   #### edges

                public SEdgeIterator edges()

            ::: {.block}
            Get an iterator over edges.
            :::

            [Returns:]{.returnLabel}
            :   a edge iterator.

        []{#linkCount--}

        -   #### linkCount

                public int linkCount()

            ::: {.block}
            Get the number of links on the graph.
            :::

            [Returns:]{.returnLabel}
            :   the number of links.

        []{#hasLink-int-}

        -   #### hasLink

                public boolean hasLink(int linkId)

            ::: {.block}
            Check if the specified link exists on the graph.
            :::

            [Parameters:]{.paramLabel}
            :   `linkId` - the link id.

            [Returns:]{.returnLabel}
            :   true if the link exists, false otherwise.

        []{#link-int-}

        -   #### link

                public SLink link(int linkId)

            ::: {.block}
            Get a link on the graph by id.
            :::

            [Parameters:]{.paramLabel}
            :   `linkId` - the link id.

            [Returns:]{.returnLabel}
            :   the link as a {link SLink}.

        []{#link-au.gov.asd.tac.constellation.views.scripting.graph.SVertex-au.gov.asd.tac.constellation.views.scripting.graph.SVertex-}

        -   #### link

                public SLink link(SVertex sourceVertex,
                                  SVertex destinationVertex)

            ::: {.block}
            Get a link on the graph by its endpoints.
            :::

            [Parameters:]{.paramLabel}
            :   `sourceVertex` - the source vertex as a
                [`SVertex`](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SVertex.html "class in au.gov.asd.tac.constellation.views.scripting.graph").
            :   `destinationVertex` - the destination vertex as a
                [`SVertex`](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SVertex.html "class in au.gov.asd.tac.constellation.views.scripting.graph").

            [Returns:]{.returnLabel}
            :   the link as a {link SLink}.

        []{#links--}

        -   #### links

                public SLinkIterator links()

            ::: {.block}
            Get an iterator over links.
            :::

            [Returns:]{.returnLabel}
            :   a link iterator.

        []{#Z:Z__getitem__-java.lang.Object-}

        -   #### \_\_getitem\_\_

                protected java.lang.Object __getitem__(java.lang.Object key)

            ::: {.block}
            Python assignment to self\[key\].
            :::

            [Parameters:]{.paramLabel}
            :   `key` - an object representing the attribute to query.

            [Returns:]{.returnLabel}
            :   the value of the specified attribute.

        []{#Z:Z__enter__--}

        -   #### \_\_enter\_\_

                protected SReadableGraph __enter__()

            ::: {.block}
            The entry point for the Python context manager.
            :::

            [Returns:]{.returnLabel}
            :   a read lock on the graph.

        []{#Z:Z__exit__-java.lang.Object-java.lang.Object-java.lang.Object-}

        -   #### \_\_exit\_\_

                protected void __exit__(java.lang.Object exc_type,
                                        java.lang.Object exc_value,
                                        java.lang.Object traceback)

            ::: {.block}
            The exit point for the Python context manager.
            :::

            [Parameters:]{.paramLabel}
            :   `exc_type` - the exception type.
            :   `exc_value` - the exception value.
            :   `traceback` - the exception traceback.

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
-   [[Prev Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SLink.html "class in au.gov.asd.tac.constellation.views.scripting.graph")
-   [[Next Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/STransaction.html "class in au.gov.asd.tac.constellation.views.scripting.graph")

```{=html}
<!-- -->
```
-   [Frames](../../../../../../../index.html?au/gov/asd/tac/constellation/scripting/graph/SReadableGraph.html)
-   [No Frames](SReadableGraph.html)

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
-   Constr \| 
-   [Method](#method.summary)

```{=html}
<!-- -->
```
-   Detail: 
-   [Field](#field.detail) \| 
-   Constr \| 
-   [Method](#method.detail)

</div>

[]{#skip.navbar.bottom}
:::
