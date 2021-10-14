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
-   [Package](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/graph/package-summary.md)
-   Class
-   [Tree](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/graph/package-tree.md)
-   [Deprecated](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/deprecated-list.md)
-   [Index](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/index-all.md)
-   [Help](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/help-doc.md)

<div class="aboutLanguage">

au.gov.asd.tac.constellation.views.scripting 1.0

</div>

</div>

<div class="subNav">

-   [<span
    class="typeNameLink">Prev Class</span>](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/graph/SVertex.md "class in au.gov.asd.tac.constellation.views.scripting.graph")
-   Next Class

<!-- -->

-   [Frames](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/graph/SWritableGraph.md)
-   [No Frames](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/graph/SWritableGraph.md)

<!-- -->

-   [All Classes](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/allclasses-noframe.md)

<div>

</div>

<div>

-   Summary: 
-   Nested | 
-   [Field](#fields.inherited.from.class.au.gov.asd.tac.constellation.views.scripting.graph.SReadableGraph) | 
-   Constr | 
-   [Method](#method.summary)

<!-- -->

-   Detail: 
-   Field | 
-   Constr | 
-   [Method](#method.detail)

</div>

<span id="skip.navbar.top"></span>

</div>

<div class="header">

<div class="subTitle">

au.gov.asd.tac.constellation.views.scripting.graph

</div>

## Class SWritableGraph

</div>

<div class="contentContainer">

-   java.lang.Object

-   -   [au.gov.asd.tac.constellation.views.scripting.graph.SReadableGraph](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/graph/SReadableGraph.md "class in au.gov.asd.tac.constellation.views.scripting.graph")

    -   -   au.gov.asd.tac.constellation.views.scripting.graph.SWritableGraph

<div class="description">

-   

    ------------------------------------------------------------------------

      

        public class SWritableGraph
        extends SReadableGraph

    <div class="block">

    A representation of a graph write lock for use with scripting.

    </div>

    <span class="simpleTagLabel">Author:</span>  
    algol, cygnus_x-1

</div>

<div class="summary">

-   -   <span id="field.summary"></span>

        ### Field Summary

        -   <span
            id="fields.inherited.from.class.au.gov.asd.tac.constellation.views.scripting.graph.SReadableGraph"></span>

            ### Fields inherited from class au.gov.asd.tac.constellation.views.scripting.graph.[SReadableGraph](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/graph/SReadableGraph.md "class in au.gov.asd.tac.constellation.views.scripting.graph")

            `graph, readableGraph`

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
        <td class="colFirst"><code>SWritableGraph</code></td>
        <td class="colLast"><code>__enter__()</code>
        <div class="block">
        The entry point for the Python context manager.
        </div></td>
        </tr>
        <tr id="i1" class="even rowColor">
        <td class="colFirst"><code>void</code></td>
        <td class="colLast"><code>__exit__(java.lang.Object exc_type,                                                 java.lang.Object exc_value,                                                 java.lang.Object traceback)</code>
        <div class="block">
        The exit point for the Python context manager.
        </div></td>
        </tr>
        <tr id="i2" class="odd altColor">
        <td class="colFirst"><code>java.lang.Object</code></td>
        <td class="colLast"><code>__setitem__(java.lang.Object key,                                                 java.lang.Object value)</code>
        <div class="block">
        Python assignment to self[key].
        </div></td>
        </tr>
        <tr id="i3" class="even rowColor">
        <td class="colFirst"><code>SAttribute</code></td>
        <td class="colLast"><code>addAttribute(au.gov.asd.tac.constellation.graph.GraphElementType elementType,                                                 java.lang.String attributeType,                                                 java.lang.String name,                                                 java.lang.String description,                                                 java.lang.Object defaultValue,                                                 java.lang.String attributeMergerId)</code>
        <div class="block">
        Add a new attribute to the graph.
        </div></td>
        </tr>
        <tr id="i4" class="odd altColor">
        <td class="colFirst"><code>STransaction</code></td>
        <td class="colLast"><code>addTransaction(SVertex sourceVertex,                                                 SVertex destinationVertex,                                                 boolean directed)</code>
        <div class="block">
        Add a transaction to the graph.
        </div></td>
        </tr>
        <tr id="i5" class="even rowColor">
        <td class="colFirst"><code>SVertex</code></td>
        <td class="colLast"><code>addVertex()</code>
        <div class="block">
        Add a vertex to the graph.
        </div></td>
        </tr>
        <tr id="i6" class="odd altColor">
        <td class="colFirst"><code>void</code></td>
        <td class="colLast"><code>commit()</code>
        <div class="block">
        Commit changes made to the graph using this write lock, then release the lock.
        </div></td>
        </tr>
        <tr id="i7" class="even rowColor">
        <td class="colFirst"><code>SAttribute</code></td>
        <td class="colLast"><code>ensureAttribute(au.gov.asd.tac.constellation.graph.GraphElementType elementType,                                                 java.lang.String name)</code>
        <div class="block">
        Get a schema attribute from the graph if it exists, otherwise create it.
        </div></td>
        </tr>
        <tr id="i8" class="odd altColor">
        <td class="colFirst"><code>protected au.gov.asd.tac.constellation.graph.WritableGraph</code></td>
        <td class="colLast"><code>getWritableGraph()</code>
        <div class="block">
        Get the actual write lock on the graph that this object represents.
        </div></td>
        </tr>
        <tr id="i9" class="even rowColor">
        <td class="colFirst"><code>void</code></td>
        <td class="colLast"><code>mergeVertices(int leadVertex,                                                 int mergeVertex)</code>
        <div class="block">
        Merge two vertices together.
        </div></td>
        </tr>
        <tr id="i10" class="odd altColor">
        <td class="colFirst"><code>void</code></td>
        <td class="colLast"><code>mergeVertices(int leadVertex,                                                 SCollection vertices)</code>
        <div class="block">
        Merge more than two vertices together.
        </div></td>
        </tr>
        <tr id="i11" class="even rowColor">
        <td class="colFirst"><code>void</code></td>
        <td class="colLast"><code>removeAttribute(int attributeId)</code>
        <div class="block">
        Remove an attribute from the graph.
        </div></td>
        </tr>
        <tr id="i12" class="odd altColor">
        <td class="colFirst"><code>void</code></td>
        <td class="colLast"><code>removeTransaction(int transaction)</code>
        <div class="block">
        Remove a transaction from the graph.
        </div></td>
        </tr>
        <tr id="i13" class="even rowColor">
        <td class="colFirst"><code>void</code></td>
        <td class="colLast"><code>removeTransactions(SCollection transactions)</code>
        <div class="block">
        Remove a collection of transactions from the graph.
        </div></td>
        </tr>
        <tr id="i14" class="odd altColor">
        <td class="colFirst"><code>void</code></td>
        <td class="colLast"><code>removeVertex(int vertexId)</code>
        <div class="block">
        Remove a vertex from the graph.
        </div></td>
        </tr>
        <tr id="i15" class="even rowColor">
        <td class="colFirst"><code>void</code></td>
        <td class="colLast"><code>removeVertices(SCollection vertices)</code>
        <div class="block">
        Remove a collection of vertices from the graph.
        </div></td>
        </tr>
        <tr id="i16" class="odd altColor">
        <td class="colFirst"><code>void</code></td>
        <td class="colLast"><code>rollback()</code>
        <div class="block">
        Rollback changes made to the graph using this write lock, then release the lock.
        </div></td>
        </tr>
        <tr id="i17" class="even rowColor">
        <td class="colFirst"><code>java.lang.String</code></td>
        <td class="colLast"><code>toString()</code> </td>
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
            id="methods.inherited.from.class.au.gov.asd.tac.constellation.views.scripting.graph.SReadableGraph"></span>

            ### Methods inherited from class au.gov.asd.tac.constellation.views.scripting.graph.[SReadableGraph](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/graph/SReadableGraph.md "class in au.gov.asd.tac.constellation.views.scripting.graph")

            `__getitem__, attribute, attribute, attributeCount, attributeModificationCounter, attributes, edge, edgeCount, edges, filterTransactions, filterVertices, getReadableGraph, globalModificationCounter, hasAttribute, hasEdge, hasLink, hasTransaction, hasVertex, link, link, linkCount, links, release, structureModificationCounter, transaction, transactionCount, transactions, valueModificationCounter, vertex, vertexCount, vertices, withTransactions, withVertices`

        <!-- -->

        -   <span
            id="methods.inherited.from.class.java.lang.Object"></span>

            ### Methods inherited from class java.lang.Object

            `clone, equals, finalize, getClass, hashCode, notify, notifyAll, wait, wait, wait`

</div>

<div class="details">

-   -   <span id="method.detail"></span>

        ### Method Detail

        <span id="getWritableGraph--"></span>

        -   #### getWritableGraph

                protected au.gov.asd.tac.constellation.graph.WritableGraph getWritableGraph()

            <div class="block">

            Get the actual write lock on the graph that this object
            represents.

            </div>

            <span class="returnLabel">Returns:</span>  
            the write lock on the graph.

        <span id="commit--"></span>

        -   #### commit

                public void commit()

            <div class="block">

            Commit changes made to the graph using this write lock, then
            release the lock. This or
            [`rollback()`](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/graph/SWritableGraph.md#rollback--)
            should be called immediately after you finish writing to the
            graph.

            </div>

        <span id="rollback--"></span>

        -   #### rollback

                public void rollback()

            <div class="block">

            Rollback changes made to the graph using this write lock,
            then release the lock. This or
            [`commit()`](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/graph/SWritableGraph.md#commit--)
            should be called immediately after you finish writing to the
            graph.

            </div>

        <span
        id="addAttribute-au.gov.asd.tac.constellation.graph.GraphElementType-java.lang.String-java.lang.String-java.lang.String-java.lang.Object-java.lang.String-"></span>

        -   #### addAttribute

                public SAttribute addAttribute(au.gov.asd.tac.constellation.graph.GraphElementType elementType,
                                               java.lang.String attributeType,
                                               java.lang.String name,
                                               java.lang.String description,
                                               java.lang.Object defaultValue,
                                               java.lang.String attributeMergerId)

            <div class="block">

            Add a new attribute to the graph.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `elementType` - the element type of the attribute.

            `attributeType` - the type of the attribute.

            `name` - the name of the attribute.

            `description` - a description of the attribute.

            `defaultValue` - the default value for the attribute.

            `attributeMergerId` - the merger the attribute should use.

            <span class="returnLabel">Returns:</span>  
            the new attribute as a
            [`SAttribute`](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/graph/SAttribute.md "class in au.gov.asd.tac.constellation.views.scripting.graph").

        <span
        id="ensureAttribute-au.gov.asd.tac.constellation.graph.GraphElementType-java.lang.String-"></span>

        -   #### ensureAttribute

                public SAttribute ensureAttribute(au.gov.asd.tac.constellation.graph.GraphElementType elementType,
                                                  java.lang.String name)

            <div class="block">

            Get a schema attribute from the graph if it exists,
            otherwise create it.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `elementType` - the element type of the attribute.

            `name` - the name of the attribute.

            <span class="returnLabel">Returns:</span>  
            the attribute as a
            [`SAttribute`](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/graph/SAttribute.md "class in au.gov.asd.tac.constellation.views.scripting.graph").

        <span id="removeAttribute-int-"></span>

        -   #### removeAttribute

                public void removeAttribute(int attributeId)

            <div class="block">

            Remove an attribute from the graph.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `attributeId` - the attribute id.

        <span id="addVertex--"></span>

        -   #### addVertex

                public SVertex addVertex()

            <div class="block">

            Add a vertex to the graph.

            </div>

            <span class="returnLabel">Returns:</span>  
            the new vertex as a
            [`SVertex`](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/graph/SVertex.md "class in au.gov.asd.tac.constellation.views.scripting.graph").

        <span id="removeVertex-int-"></span>

        -   #### removeVertex

                public void removeVertex(int vertexId)

            <div class="block">

            Remove a vertex from the graph.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `vertexId` - the vertex id.

        <span
        id="removeVertices-au.gov.asd.tac.constellation.views.scripting.graph.SCollection-"></span>

        -   #### removeVertices

                public void removeVertices(SCollection vertices)

            <div class="block">

            Remove a collection of vertices from the graph.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `vertices` - the vertices to remove as a
            [`SCollection`](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/graph/SCollection.md "class in au.gov.asd.tac.constellation.views.scripting.graph").

        <span id="mergeVertices-int-int-"></span>

        -   #### mergeVertices

                public void mergeVertices(int leadVertex,
                                          int mergeVertex)

            <div class="block">

            Merge two vertices together.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `leadVertex` - the id of the consuming vertex.

            `mergeVertex` - the id of the consumed vertex.

        <span
        id="mergeVertices-int-au.gov.asd.tac.constellation.views.scripting.graph.SCollection-"></span>

        -   #### mergeVertices

                public void mergeVertices(int leadVertex,
                                          SCollection vertices)

            <div class="block">

            Merge more than two vertices together.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `leadVertex` - the id of the consuming vertex.

            `vertices` - the consumed vertices as a
            [`SCollection`](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/graph/SCollection.md "class in au.gov.asd.tac.constellation.views.scripting.graph").

        <span
        id="addTransaction-au.gov.asd.tac.constellation.views.scripting.graph.SVertex-au.gov.asd.tac.constellation.views.scripting.graph.SVertex-boolean-"></span>

        -   #### addTransaction

                public STransaction addTransaction(SVertex sourceVertex,
                                                   SVertex destinationVertex,
                                                   boolean directed)

            <div class="block">

            Add a transaction to the graph.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `sourceVertex` - the id of the source vertex.

            `destinationVertex` - the id of the destination vertex.

            `directed` - should the transaction be directed?

            <span class="returnLabel">Returns:</span>  
            the new transaction as a
            [`STransaction`](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/graph/STransaction.md "class in au.gov.asd.tac.constellation.views.scripting.graph").

        <span id="removeTransaction-int-"></span>

        -   #### removeTransaction

                public void removeTransaction(int transaction)

            <div class="block">

            Remove a transaction from the graph.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `transaction` - the id of the transaction.

        <span
        id="removeTransactions-au.gov.asd.tac.constellation.views.scripting.graph.SCollection-"></span>

        -   #### removeTransactions

                public void removeTransactions(SCollection transactions)

            <div class="block">

            Remove a collection of transactions from the graph.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `transactions` - the transaction to remove as a
            [`STransaction`](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/graph/STransaction.md "class in au.gov.asd.tac.constellation.views.scripting.graph").

        <span
        id="Z:Z__setitem__-java.lang.Object-java.lang.Object-"></span>

        -   #### \_\_setitem\_\_

                public java.lang.Object __setitem__(java.lang.Object key,
                                                    java.lang.Object value)

            <div class="block">

            Python assignment to self\[key\].

            </div>

            <span class="paramLabel">Parameters:</span>  
            `key` - an object representing the attribute to set.

            `value` - the new value for the specified attribute.

            <span class="returnLabel">Returns:</span>  
            the set value.

        <span id="Z:Z__enter__--"></span>

        -   #### \_\_enter\_\_

                public SWritableGraph __enter__()

            <div class="block">

            The entry point for the Python context manager.

            </div>

            <span class="overrideSpecifyLabel">Overrides:</span>  
            `__enter__` in class `SReadableGraph`

            <span class="returnLabel">Returns:</span>  
            a write lock on the graph.

        <span
        id="Z:Z__exit__-java.lang.Object-java.lang.Object-java.lang.Object-"></span>

        -   #### \_\_exit\_\_

                public void __exit__(java.lang.Object exc_type,
                                     java.lang.Object exc_value,
                                     java.lang.Object traceback)

            <div class="block">

            The exit point for the Python context manager.

            </div>

            <span class="overrideSpecifyLabel">Overrides:</span>  
            `__exit__` in class `SReadableGraph`

            <span class="paramLabel">Parameters:</span>  
            `exc_type` - the exception type.

            `exc_value` - the exception value.

            `traceback` - the exception traceback.

        <span id="toString--"></span>

        -   #### toString

                public java.lang.String toString()

            <span class="overrideSpecifyLabel">Overrides:</span>  
            `toString` in class `SReadableGraph`

</div>

</div>

<div class="bottomNav">

<span id="navbar.bottom"></span>

<div class="skipNav">

[Skip navigation links](#skip.navbar.bottom "Skip navigation links")

</div>

<span id="navbar.bottom.firstrow"></span>

-   [Overview](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/overview-summary.md)
-   [Package](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/graph/package-summary.md)
-   Class
-   [Tree](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/graph/package-tree.md)
-   [Deprecated](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/deprecated-list.md)
-   [Index](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/index-all.md)
-   [Help](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/help-doc.md)

<div class="aboutLanguage">

au.gov.asd.tac.constellation.views.scripting 1.0

</div>

</div>

<div class="subNav">

-   [<span
    class="typeNameLink">Prev Class</span>](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/graph/SVertex.md "class in au.gov.asd.tac.constellation.views.scripting.graph")
-   Next Class

<!-- -->

-   [Frames](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/graph/SWritableGraph.md)
-   [No Frames](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/graph/SWritableGraph.md)

<!-- -->

-   [All Classes](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/scripting/docs/javadoc/allclasses-noframe.md)

<div>

</div>

<div>

-   Summary: 
-   Nested | 
-   [Field](#fields.inherited.from.class.au.gov.asd.tac.constellation.views.scripting.graph.SReadableGraph) | 
-   Constr | 
-   [Method](#method.summary)

<!-- -->

-   Detail: 
-   Field | 
-   Constr | 
-   [Method](#method.detail)

</div>

<span id="skip.navbar.bottom"></span>

</div>
