<div class="topNav">

<span id="navbar.top"></span>

<div class="skipNav">

[Skip navigation links](#skip.navbar.top "Skip navigation links")

</div>

<span id="navbar.top.firstrow"></span>

-   [Overview](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/overview-summary.md)
-   [Package](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/package-summary.md)
-   Class
-   [Tree](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/package-tree.md)
-   [Deprecated](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/deprecated-list.md)
-   [Index](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/index-all.md)
-   [Help](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/help-doc.md)

<div class="aboutLanguage">

au.gov.asd.tac.constellation.views.scripting 1.0

</div>

</div>

<div class="subNav">

-   [<span
    class="typeNameLink">Prev Class</span>](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/SLink.md "class in au.gov.asd.tac.constellation.views.scripting.graph")
-   [<span
    class="typeNameLink">Next Class</span>](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/STransaction.md "class in au.gov.asd.tac.constellation.views.scripting.graph")

<!-- -->

-   [Frames](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/SReadableGraph.md)
-   [No Frames](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/SReadableGraph.md)

<!-- -->

-   [All Classes](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/allclasses-noframe.md)

<div>

</div>

<div>

-   Summary: 
-   Nested | 
-   [Field](#field.summary) | 
-   Constr | 
-   [Method](#method.summary)

<!-- -->

-   Detail: 
-   [Field](#field.detail) | 
-   Constr | 
-   [Method](#method.detail)

</div>

<span id="skip.navbar.top"></span>

</div>

<div class="header">

<div class="subTitle">

au.gov.asd.tac.constellation.views.scripting.graph

</div>

## Class SReadableGraph

</div>

<div class="contentContainer">

-   java.lang.Object

-   -   au.gov.asd.tac.constellation.views.scripting.graph.SReadableGraph

<div class="description">

-   Direct Known Subclasses:  
    [SWritableGraph](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/SWritableGraph.md "class in au.gov.asd.tac.constellation.views.scripting.graph")

    ------------------------------------------------------------------------

        public class SReadableGraph
        extends java.lang.Object

    <div class="block">

    A representation of a read lock on a graph for use with scripting.

    </div>

    <span class="simpleTagLabel">Author:</span>  
    algol, cygnus_x-1

</div>

<div class="summary">

-   -   <span id="field.summary"></span>

        ### Field Summary

        <table class="memberSummary" data-border="0" data-cellpadding="3" data-cellspacing="0" data-summary="Field Summary table, listing fields, and an explanation">
        <caption><span>Fields</span><span class="tabEnd"> </span></caption>
        <thead>
        <tr class="header">
        <th class="colFirst" scope="col">Modifier and Type</th>
        <th class="colLast" scope="col">Field and Description</th>
        </tr>
        </thead>
        <tbody>
        <tr class="odd altColor">
        <td class="colFirst"><code>protected SGraph</code></td>
        <td class="colLast"><code>graph</code> </td>
        </tr>
        <tr class="even rowColor">
        <td class="colFirst"><code>protected au.gov.asd.tac.constellation.graph.ReadableGraph</code></td>
        <td class="colLast"><code>readableGraph</code> </td>
        </tr>
        </tbody>
        </table>

        Fields<span class="tabEnd"> </span>

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
        <td class="colFirst"><code>protected SReadableGraph</code></td>
        <td class="colLast"><code>__enter__()</code>
        <div class="block">
        The entry point for the Python context manager.
        </div></td>
        </tr>
        <tr id="i1" class="even rowColor">
        <td class="colFirst"><code>protected void</code></td>
        <td class="colLast"><code>__exit__(java.lang.Object exc_type,                                                 java.lang.Object exc_value,                                                 java.lang.Object traceback)</code>
        <div class="block">
        The exit point for the Python context manager.
        </div></td>
        </tr>
        <tr id="i2" class="odd altColor">
        <td class="colFirst"><code>protected java.lang.Object</code></td>
        <td class="colLast"><code>__getitem__(java.lang.Object key)</code>
        <div class="block">
        Python assignment to self[key].
        </div></td>
        </tr>
        <tr id="i3" class="even rowColor">
        <td class="colFirst"><code>SAttribute</code></td>
        <td class="colLast"><code>attribute(au.gov.asd.tac.constellation.graph.GraphElementType elementType,                                                 java.lang.String name)</code>
        <div class="block">
        Get an attribute from the graph by name.
        </div></td>
        </tr>
        <tr id="i4" class="odd altColor">
        <td class="colFirst"><code>SAttribute</code></td>
        <td class="colLast"><code>attribute(int attributeId)</code>
        <div class="block">
        Get an attribute from the graph by id.
        </div></td>
        </tr>
        <tr id="i5" class="even rowColor">
        <td class="colFirst"><code>int</code></td>
        <td class="colLast"><code>attributeCount(au.gov.asd.tac.constellation.graph.GraphElementType elementType)</code>
        <div class="block">
        Get the number of attributes on the graph for a specific element type.
        </div></td>
        </tr>
        <tr id="i6" class="odd altColor">
        <td class="colFirst"><code>long</code></td>
        <td class="colLast"><code>attributeModificationCounter()</code>
        <div class="block">
        Get a count representing the number of modifications that have occurred which modified attributes on the graph.
        </div></td>
        </tr>
        <tr id="i7" class="even rowColor">
        <td class="colFirst"><code>SAttributeIterator</code></td>
        <td class="colLast"><code>attributes(au.gov.asd.tac.constellation.graph.GraphElementType elementType)</code>
        <div class="block">
        Get an iterator over attributes of the specified element type.
        </div></td>
        </tr>
        <tr id="i8" class="odd altColor">
        <td class="colFirst"><code>SEdge</code></td>
        <td class="colLast"><code>edge(int edgeId)</code>
        <div class="block">
        Get a edge on the graph by id.
        </div></td>
        </tr>
        <tr id="i9" class="even rowColor">
        <td class="colFirst"><code>int</code></td>
        <td class="colLast"><code>edgeCount()</code>
        <div class="block">
        Get the number of edges on the graph.
        </div></td>
        </tr>
        <tr id="i10" class="odd altColor">
        <td class="colFirst"><code>SEdgeIterator</code></td>
        <td class="colLast"><code>edges()</code>
        <div class="block">
        Get an iterator over edges.
        </div></td>
        </tr>
        <tr id="i11" class="even rowColor">
        <td class="colFirst"><code>SCollection</code></td>
        <td class="colLast"><code>filterTransactions(java.lang.Object callback)</code>
        <div class="block">
        Evaluate a function against each transaction on the graph in order to gather a filtered collection.
        </div></td>
        </tr>
        <tr id="i12" class="odd altColor">
        <td class="colFirst"><code>SCollection</code></td>
        <td class="colLast"><code>filterVertices(java.lang.Object callback)</code>
        <div class="block">
        Evaluate a function against each vertex on the graph in order to gather a filtered collection.
        </div></td>
        </tr>
        <tr id="i13" class="even rowColor">
        <td class="colFirst"><code>protected au.gov.asd.tac.constellation.graph.ReadableGraph</code></td>
        <td class="colLast"><code>getReadableGraph()</code>
        <div class="block">
        Get the actual read lock on the graph that this object represents.
        </div></td>
        </tr>
        <tr id="i14" class="odd altColor">
        <td class="colFirst"><code>long</code></td>
        <td class="colLast"><code>globalModificationCounter()</code>
        <div class="block">
        Get a count representing the number of modifications that have occurred globally on the graph.
        </div></td>
        </tr>
        <tr id="i15" class="even rowColor">
        <td class="colFirst"><code>boolean</code></td>
        <td class="colLast"><code>hasAttribute(au.gov.asd.tac.constellation.graph.GraphElementType elementType,                                                 java.lang.String name)</code>
        <div class="block">
        Check if the specified attribute exists on the graph.
        </div></td>
        </tr>
        <tr id="i16" class="odd altColor">
        <td class="colFirst"><code>boolean</code></td>
        <td class="colLast"><code>hasEdge(int edgeId)</code>
        <div class="block">
        Check if the specified edge exists on the graph.
        </div></td>
        </tr>
        <tr id="i17" class="even rowColor">
        <td class="colFirst"><code>boolean</code></td>
        <td class="colLast"><code>hasLink(int linkId)</code>
        <div class="block">
        Check if the specified link exists on the graph.
        </div></td>
        </tr>
        <tr id="i18" class="odd altColor">
        <td class="colFirst"><code>boolean</code></td>
        <td class="colLast"><code>hasTransaction(int transactionId)</code>
        <div class="block">
        Check if the specified transaction exists on the graph.
        </div></td>
        </tr>
        <tr id="i19" class="even rowColor">
        <td class="colFirst"><code>boolean</code></td>
        <td class="colLast"><code>hasVertex(int vertexId)</code>
        <div class="block">
        Check if the specified vertex exists on the graph.
        </div></td>
        </tr>
        <tr id="i20" class="odd altColor">
        <td class="colFirst"><code>SLink</code></td>
        <td class="colLast"><code>link(int linkId)</code>
        <div class="block">
        Get a link on the graph by id.
        </div></td>
        </tr>
        <tr id="i21" class="even rowColor">
        <td class="colFirst"><code>SLink</code></td>
        <td class="colLast"><code>link(SVertex sourceVertex,                                                 SVertex destinationVertex)</code>
        <div class="block">
        Get a link on the graph by its endpoints.
        </div></td>
        </tr>
        <tr id="i22" class="odd altColor">
        <td class="colFirst"><code>int</code></td>
        <td class="colLast"><code>linkCount()</code>
        <div class="block">
        Get the number of links on the graph.
        </div></td>
        </tr>
        <tr id="i23" class="even rowColor">
        <td class="colFirst"><code>SLinkIterator</code></td>
        <td class="colLast"><code>links()</code>
        <div class="block">
        Get an iterator over links.
        </div></td>
        </tr>
        <tr id="i24" class="odd altColor">
        <td class="colFirst"><code>void</code></td>
        <td class="colLast"><code>release()</code>
        <div class="block">
        Release the read lock on the graph.
        </div></td>
        </tr>
        <tr id="i25" class="even rowColor">
        <td class="colFirst"><code>long</code></td>
        <td class="colLast"><code>structureModificationCounter()</code>
        <div class="block">
        Get a count representing the number of modifications that have occurred which modified the structure of the graph.
        </div></td>
        </tr>
        <tr id="i26" class="odd altColor">
        <td class="colFirst"><code>java.lang.String</code></td>
        <td class="colLast"><code>toString()</code> </td>
        </tr>
        <tr id="i27" class="even rowColor">
        <td class="colFirst"><code>STransaction</code></td>
        <td class="colLast"><code>transaction(int transactionId)</code>
        <div class="block">
        Get a transaction on the graph by id.
        </div></td>
        </tr>
        <tr id="i28" class="odd altColor">
        <td class="colFirst"><code>int</code></td>
        <td class="colLast"><code>transactionCount()</code>
        <div class="block">
        Get the number of transactions on the graph.
        </div></td>
        </tr>
        <tr id="i29" class="even rowColor">
        <td class="colFirst"><code>STransactionIterator</code></td>
        <td class="colLast"><code>transactions()</code>
        <div class="block">
        Get an iterator over transactions.
        </div></td>
        </tr>
        <tr id="i30" class="odd altColor">
        <td class="colFirst"><code>long</code></td>
        <td class="colLast"><code>valueModificationCounter(au.gov.asd.tac.constellation.graph.GraphElementType type,                                                 java.lang.String name)</code>
        <div class="block">
        Get a count representing the number of modifications that have occurred to the value of a single attribute on the graph.
        </div></td>
        </tr>
        <tr id="i31" class="even rowColor">
        <td class="colFirst"><code>SVertex</code></td>
        <td class="colLast"><code>vertex(int vertexId)</code>
        <div class="block">
        Get a vertex on the graph by id.
        </div></td>
        </tr>
        <tr id="i32" class="odd altColor">
        <td class="colFirst"><code>int</code></td>
        <td class="colLast"><code>vertexCount()</code>
        <div class="block">
        Get the number of vertices on the graph.
        </div></td>
        </tr>
        <tr id="i33" class="even rowColor">
        <td class="colFirst"><code>SVertexIterator</code></td>
        <td class="colLast"><code>vertices()</code>
        <div class="block">
        Get an iterator over vertices.
        </div></td>
        </tr>
        <tr id="i34" class="odd altColor">
        <td class="colFirst"><code>void</code></td>
        <td class="colLast"><code>withTransactions(java.lang.Object callback)</code>
        <div class="block">
        Evaluate a function against each transaction on the graph.
        </div></td>
        </tr>
        <tr id="i35" class="even rowColor">
        <td class="colFirst"><code>void</code></td>
        <td class="colLast"><code>withVertices(java.lang.Object callback)</code>
        <div class="block">
        Evaluate a function against each vertex on the graph.
        </div></td>
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
            id="methods.inherited.from.class.java.lang.Object"></span>

            ### Methods inherited from class java.lang.Object

            `clone, equals, finalize, getClass, hashCode, notify, notifyAll, wait, wait, wait`

</div>

<div class="details">

-   -   <span id="field.detail"></span>

        ### Field Detail

        <span id="graph"></span>

        -   #### graph

                protected final SGraph graph

        <span id="readableGraph"></span>

        -   #### readableGraph

                protected final au.gov.asd.tac.constellation.graph.ReadableGraph readableGraph

    <!-- -->

    -   <span id="method.detail"></span>

        ### Method Detail

        <span id="getReadableGraph--"></span>

        -   #### getReadableGraph

                protected au.gov.asd.tac.constellation.graph.ReadableGraph getReadableGraph()

            <div class="block">

            Get the actual read lock on the graph that this object
            represents.

            </div>

            <span class="returnLabel">Returns:</span>  
            the read lock on the graph.

        <span id="release--"></span>

        -   #### release

                public void release()

            <div class="block">

            Release the read lock on the graph. This should be called
            immediately after you finish reading from the graph.

            </div>

        <span id="globalModificationCounter--"></span>

        -   #### globalModificationCounter

                public long globalModificationCounter()

            <div class="block">

            Get a count representing the number of modifications that
            have occurred globally on the graph.

            </div>

            <span class="returnLabel">Returns:</span>  
            the current global modification count.

        <span id="structureModificationCounter--"></span>

        -   #### structureModificationCounter

                public long structureModificationCounter()

            <div class="block">

            Get a count representing the number of modifications that
            have occurred which modified the structure of the graph.

            </div>

            <span class="returnLabel">Returns:</span>  
            the current structure modification count.

        <span id="attributeModificationCounter--"></span>

        -   #### attributeModificationCounter

                public long attributeModificationCounter()

            <div class="block">

            Get a count representing the number of modifications that
            have occurred which modified attributes on the graph.

            </div>

            <span class="returnLabel">Returns:</span>  
            the current attribute modification count.

        <span
        id="valueModificationCounter-au.gov.asd.tac.constellation.graph.GraphElementType-java.lang.String-"></span>

        -   #### valueModificationCounter

                public long valueModificationCounter(au.gov.asd.tac.constellation.graph.GraphElementType type,
                                                     java.lang.String name)

            <div class="block">

            Get a count representing the number of modifications that
            have occurred to the value of a single attribute on the
            graph.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `type` - the element type of attribute.

            `name` - the name of the attribute.

            <span class="returnLabel">Returns:</span>  
            the current attribute value modification count.

        <span
        id="attributeCount-au.gov.asd.tac.constellation.graph.GraphElementType-"></span>

        -   #### attributeCount

                public int attributeCount(au.gov.asd.tac.constellation.graph.GraphElementType elementType)

            <div class="block">

            Get the number of attributes on the graph for a specific
            element type.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `elementType` - the element type.

            <span class="returnLabel">Returns:</span>  
            the number of attributes for the given element type.

        <span
        id="hasAttribute-au.gov.asd.tac.constellation.graph.GraphElementType-java.lang.String-"></span>

        -   #### hasAttribute

                public boolean hasAttribute(au.gov.asd.tac.constellation.graph.GraphElementType elementType,
                                            java.lang.String name)

            <div class="block">

            Check if the specified attribute exists on the graph.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `elementType` - the element type of the attribute

            `name` - the name of the attribute

            <span class="returnLabel">Returns:</span>  
            true if the attribute exists, false otherwise

        <span id="attribute-int-"></span>

        -   #### attribute

                public SAttribute attribute(int attributeId)

            <div class="block">

            Get an attribute from the graph by id.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `attributeId` - the id of the attribute.

            <span class="returnLabel">Returns:</span>  
            the requested attribute as a
            [`SAttribute`](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/SAttribute.md "class in au.gov.asd.tac.constellation.views.scripting.graph").

        <span
        id="attribute-au.gov.asd.tac.constellation.graph.GraphElementType-java.lang.String-"></span>

        -   #### attribute

                public SAttribute attribute(au.gov.asd.tac.constellation.graph.GraphElementType elementType,
                                            java.lang.String name)
                                     throws au.gov.asd.tac.constellation.views.scripting.graph.exceptions.NoSuchAttributeException

            <div class="block">

            Get an attribute from the graph by name.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `elementType` - the element type of the attribute.

            `name` - the name of the attribute.

            <span class="returnLabel">Returns:</span>  
            the requested attribute.

            <span class="throwsLabel">Throws:</span>  
            `au.gov.asd.tac.constellation.views.scripting.graph.exceptions.NoSuchAttributeException`

        <span
        id="attributes-au.gov.asd.tac.constellation.graph.GraphElementType-"></span>

        -   #### attributes

                public SAttributeIterator attributes(au.gov.asd.tac.constellation.graph.GraphElementType elementType)

            <div class="block">

            Get an iterator over attributes of the specified element
            type.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `elementType` - the element type.

            <span class="returnLabel">Returns:</span>  
            an attribute iterator.

        <span id="vertexCount--"></span>

        -   #### vertexCount

                public int vertexCount()

            <div class="block">

            Get the number of vertices on the graph.

            </div>

            <span class="returnLabel">Returns:</span>  
            the number of vertices.

        <span id="hasVertex-int-"></span>

        -   #### hasVertex

                public boolean hasVertex(int vertexId)

            <div class="block">

            Check if the specified vertex exists on the graph.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `vertexId` - the vertex id.

            <span class="returnLabel">Returns:</span>  
            true if the vertex exists, false otherwise.

        <span id="vertex-int-"></span>

        -   #### vertex

                public SVertex vertex(int vertexId)

            <div class="block">

            Get a vertex on the graph by id.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `vertexId` - the vertex id.

            <span class="returnLabel">Returns:</span>  
            the vertex as a {link SVertex}.

        <span id="vertices--"></span>

        -   #### vertices

                public SVertexIterator vertices()

            <div class="block">

            Get an iterator over vertices.

            </div>

            <span class="returnLabel">Returns:</span>  
            a vertex iterator.

        <span id="withVertices-java.lang.Object-"></span>

        -   #### withVertices

                public void withVertices(java.lang.Object callback)
                                  throws javax.script.ScriptException

            <div class="block">

            Evaluate afunction against each vertex on the graph. Note:
            This method will only work for Python scripts as it makes
            use of Python specific syntax.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `callback` - the function to evaluate.

            <span class="throwsLabel">Throws:</span>  
            `javax.script.ScriptException`

        <span id="filterVertices-java.lang.Object-"></span>

        -   #### filterVertices

                public SCollection filterVertices(java.lang.Object callback)
                                           throws javax.script.ScriptException

            <div class="block">

            Evaluate a function against each vertex on the graph in
            order to gather a filtered collection. The provided function
            must only return true or false. Note: This method will only
            work for Python scripts as it makes use of Python specific
            syntax.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `callback` - a function which returns true or false.

            <span class="returnLabel">Returns:</span>  
            a collection of vertices as a
            [`SCollection`](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/SCollection.md "class in au.gov.asd.tac.constellation.views.scripting.graph").

            <span class="throwsLabel">Throws:</span>  
            `javax.script.ScriptException`

        <span id="transactionCount--"></span>

        -   #### transactionCount

                public int transactionCount()

            <div class="block">

            Get the number of transactions on the graph.

            </div>

            <span class="returnLabel">Returns:</span>  
            the number of transactions.

        <span id="hasTransaction-int-"></span>

        -   #### hasTransaction

                public boolean hasTransaction(int transactionId)

            <div class="block">

            Check if the specified transaction exists on the graph.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `transactionId` - the transaction id.

            <span class="returnLabel">Returns:</span>  
            true if the transaction exists, false otherwise.

        <span id="transaction-int-"></span>

        -   #### transaction

                public STransaction transaction(int transactionId)

            <div class="block">

            Get a transaction on the graph by id.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `transactionId` - the transaction id.

            <span class="returnLabel">Returns:</span>  
            the transaction as a {link STransaction}.

        <span id="transactions--"></span>

        -   #### transactions

                public STransactionIterator transactions()

            <div class="block">

            Get an iterator over transactions.

            </div>

            <span class="returnLabel">Returns:</span>  
            a transaction iterator.

        <span id="withTransactions-java.lang.Object-"></span>

        -   #### withTransactions

                public void withTransactions(java.lang.Object callback)
                                      throws javax.script.ScriptException

            <div class="block">

            Evaluate a function against each transaction on the graph.
            Note: This method will only work for Python scripts as it
            makes use of Python specific syntax.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `callback` - the function to evaluate.

            <span class="throwsLabel">Throws:</span>  
            `javax.script.ScriptException`

        <span id="filterTransactions-java.lang.Object-"></span>

        -   #### filterTransactions

                public SCollection filterTransactions(java.lang.Object callback)
                                               throws javax.script.ScriptException

            <div class="block">

            Evaluate a function against each transaction on the graph in
            order to gather a filtered collection. The provided function
            must only return true or false. Note: This method will only
            work for Python scripts as it makes use of Python specific
            syntax.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `callback` - a function which returns true or false.

            <span class="returnLabel">Returns:</span>  
            a collection of transactions as a
            [`SCollection`](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/SCollection.md "class in au.gov.asd.tac.constellation.views.scripting.graph").

            <span class="throwsLabel">Throws:</span>  
            `javax.script.ScriptException`

        <span id="edgeCount--"></span>

        -   #### edgeCount

                public int edgeCount()

            <div class="block">

            Get the number of edges on the graph.

            </div>

            <span class="returnLabel">Returns:</span>  
            the number of edges.

        <span id="hasEdge-int-"></span>

        -   #### hasEdge

                public boolean hasEdge(int edgeId)

            <div class="block">

            Check if the specified edge exists on the graph.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `edgeId` - the edge id.

            <span class="returnLabel">Returns:</span>  
            true if the edge exists, false otherwise.

        <span id="edge-int-"></span>

        -   #### edge

                public SEdge edge(int edgeId)

            <div class="block">

            Get a edge on the graph by id.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `edgeId` - the edge id.

            <span class="returnLabel">Returns:</span>  
            the edge as a {link SEdge}.

        <span id="edges--"></span>

        -   #### edges

                public SEdgeIterator edges()

            <div class="block">

            Get an iterator over edges.

            </div>

            <span class="returnLabel">Returns:</span>  
            a edge iterator.

        <span id="linkCount--"></span>

        -   #### linkCount

                public int linkCount()

            <div class="block">

            Get the number of links on the graph.

            </div>

            <span class="returnLabel">Returns:</span>  
            the number of links.

        <span id="hasLink-int-"></span>

        -   #### hasLink

                public boolean hasLink(int linkId)

            <div class="block">

            Check if the specified link exists on the graph.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `linkId` - the link id.

            <span class="returnLabel">Returns:</span>  
            true if the link exists, false otherwise.

        <span id="link-int-"></span>

        -   #### link

                public SLink link(int linkId)

            <div class="block">

            Get a link on the graph by id.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `linkId` - the link id.

            <span class="returnLabel">Returns:</span>  
            the link as a {link SLink}.

        <span
        id="link-au.gov.asd.tac.constellation.views.scripting.graph.SVertex-au.gov.asd.tac.constellation.views.scripting.graph.SVertex-"></span>

        -   #### link

                public SLink link(SVertex sourceVertex,
                                  SVertex destinationVertex)

            <div class="block">

            Get a link on the graph by its endpoints.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `sourceVertex` - the source vertex as a
            [`SVertex`](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/SVertex.md "class in au.gov.asd.tac.constellation.views.scripting.graph").

            `destinationVertex` - the destination vertex as a
            [`SVertex`](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/SVertex.md "class in au.gov.asd.tac.constellation.views.scripting.graph").

            <span class="returnLabel">Returns:</span>  
            the link as a {link SLink}.

        <span id="links--"></span>

        -   #### links

                public SLinkIterator links()

            <div class="block">

            Get an iterator over links.

            </div>

            <span class="returnLabel">Returns:</span>  
            a link iterator.

        <span id="Z:Z__getitem__-java.lang.Object-"></span>

        -   #### \_\_getitem\_\_

                protected java.lang.Object __getitem__(java.lang.Object key)

            <div class="block">

            Python assignment to self\[key\].

            </div>

            <span class="paramLabel">Parameters:</span>  
            `key` - an object representing the attribute to query.

            <span class="returnLabel">Returns:</span>  
            the value of the specified attribute.

        <span id="Z:Z__enter__--"></span>

        -   #### \_\_enter\_\_

                protected SReadableGraph __enter__()

            <div class="block">

            The entry point for the Python context manager.

            </div>

            <span class="returnLabel">Returns:</span>  
            a read lock on the graph.

        <span
        id="Z:Z__exit__-java.lang.Object-java.lang.Object-java.lang.Object-"></span>

        -   #### \_\_exit\_\_

                protected void __exit__(java.lang.Object exc_type,
                                        java.lang.Object exc_value,
                                        java.lang.Object traceback)

            <div class="block">

            The exit point for the Python context manager.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `exc_type` - the exception type.

            `exc_value` - the exception value.

            `traceback` - the exception traceback.

        <span id="toString--"></span>

        -   #### toString

                public java.lang.String toString()

            <span class="overrideSpecifyLabel">Overrides:</span>  
            `toString` in class `java.lang.Object`

</div>

</div>

<div class="bottomNav">

<span id="navbar.bottom"></span>

<div class="skipNav">

[Skip navigation links](#skip.navbar.bottom "Skip navigation links")

</div>

<span id="navbar.bottom.firstrow"></span>

-   [Overview](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/overview-summary.md)
-   [Package](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/package-summary.md)
-   Class
-   [Tree](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/package-tree.md)
-   [Deprecated](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/deprecated-list.md)
-   [Index](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/index-all.md)
-   [Help](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/help-doc.md)

<div class="aboutLanguage">

au.gov.asd.tac.constellation.views.scripting 1.0

</div>

</div>

<div class="subNav">

-   [<span
    class="typeNameLink">Prev Class</span>](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/SLink.md "class in au.gov.asd.tac.constellation.views.scripting.graph")
-   [<span
    class="typeNameLink">Next Class</span>](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/STransaction.md "class in au.gov.asd.tac.constellation.views.scripting.graph")

<!-- -->

-   [Frames](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/SReadableGraph.md)
-   [No Frames](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/SReadableGraph.md)

<!-- -->

-   [All Classes](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/allclasses-noframe.md)

<div>

</div>

<div>

-   Summary: 
-   Nested | 
-   [Field](#field.summary) | 
-   Constr | 
-   [Method](#method.summary)

<!-- -->

-   Detail: 
-   [Field](#field.detail) | 
-   Constr | 
-   [Method](#method.detail)

</div>

<span id="skip.navbar.bottom"></span>

</div>
