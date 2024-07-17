<div class="topNav">

<span id="navbar.top"></span>

<div class="skipNav">

[Skip navigation links](#skip.navbar.top "Skip navigation links")

</div>

<span id="navbar.top.firstrow"></span>

-   [Overview](../ext/docs/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/overview-summary.md)
-   [Package](../ext/docs/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/package-summary.md)
-   Class
-   [Tree](../ext/docs/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/package-tree.md)
-   [Deprecated](../ext/docs/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/deprecated-list.md)
-   [Index](../ext/docs/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/index-all.md)
-   [Help](../ext/docs/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/help-doc.md)

<div class="aboutLanguage">

au.gov.asd.tac.constellation.views.scripting 1.0

</div>

</div>

<div class="subNav">

-   [<span
    class="typeNameLink">Prev Class</span>](../ext/docs/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/STransaction.md "class in au.gov.asd.tac.constellation.views.scripting.graph")
-   [<span
    class="typeNameLink">Next Class</span>](../ext/docs/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/SWritableGraph.md "class in au.gov.asd.tac.constellation.views.scripting.graph")

<!-- -->

-   [All Classes](../ext/docs/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/allclasses-noframe.md)

<div>

</div>

<div>

-   Summary: 
-   Nested | 
-   Field | 
-   [Constructor](#constructor.summary) | 
-   [Method](#method.summary)

<!-- -->

-   Detail: 
-   Field | 
-   [Constructor](#constructor.detail) | 
-   [Method](#method.detail)

</div>

<span id="skip.navbar.top"></span>

</div>

<div class="header">

<div class="subTitle">

au.gov.asd.tac.constellation.views.scripting.graph

</div>

## Class SVertex

</div>

<div class="contentContainer">

-   java.lang.Object

-   -   au.gov.asd.tac.constellation.views.scripting.graph.SVertex

<div class="description">

-   -
    ------------------------------------------------------------------------ 

        public class SVertex
        extends java.lang.Object

    <div class="block">

    A representation of a vertex for use with scripting.

    </div>

    <span class="simpleTagLabel">Author:</span>  
    algol, cygnus_x-1

</div>

<div class="summary">

-   -   <span id="constructor.summary"></span>

        ### Constructor Summary

        <table class="memberSummary" data-border="0" data-cellpadding="3" data-cellspacing="0" data-summary="Constructor Summary table, listing constructors, and an explanation">
        <caption><span>Constructors</span><span class="tabEnd"> </span></caption>
        <thead>
        <tr class="header">
        <th class="colOne" scope="col">Constructor and Description</th>
        </tr>
        </thead>
        <tbody>
        <tr class="odd altColor">
        <td class="colOne"><code>SVertex(au.gov.asd.tac.constellation.graph.GraphReadMethods hlgraph,                                                 int id)</code> </td>
        </tr>
        </tbody>
        </table>

        Constructors<span class="tabEnd"> </span>

    <!-- -->

    -   <span id="method.summary"></span>

        ### Method Summary

        <table class="memberSummary" data-border="0" data-cellpadding="3" data-cellspacing="0" data-summary="Method Summary table, listing methods, and an explanation">
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
        <td class="colFirst"><code>java.lang.Object</code></td>
        <td class="colLast"><code>__getitem__(java.lang.Object key)</code>
        <div class="block">
        Python evaluation of self[key].
        </div></td>
        </tr>
        <tr id="i1" class="even rowColor">
        <td class="colFirst"><code>java.lang.Object</code></td>
        <td class="colLast"><code>__setitem__(java.lang.Object key,                                                 java.lang.Object value)</code>
        <div class="block">
        Python assignment to self[key].
        </div></td>
        </tr>
        <tr id="i2" class="odd altColor">
        <td class="colFirst"><code>void</code></td>
        <td class="colLast"><code>complete()</code>
        <div class="block">
        Complete this vertex using the schema of the graph.
        </div></td>
        </tr>
        <tr id="i3" class="even rowColor">
        <td class="colFirst"><code>int</code></td>
        <td class="colLast"><code>edgeCount()</code>
        <div class="block">
        Get the number of edges connected to this vertex.
        </div></td>
        </tr>
        <tr id="i4" class="odd altColor">
        <td class="colFirst"><code>SVertexEdgeIterator</code></td>
        <td class="colLast"><code>edges()</code>
        <div class="block">
        Get an iterator over edges attached to this vertex.
        </div></td>
        </tr>
        <tr id="i5" class="even rowColor">
        <td class="colFirst"><code>boolean</code></td>
        <td class="colLast"><code>equals(java.lang.Object obj)</code> </td>
        </tr>
        <tr id="i6" class="odd altColor">
        <td class="colFirst"><code>int</code></td>
        <td class="colLast"><code>hashCode()</code> </td>
        </tr>
        <tr id="i7" class="even rowColor">
        <td class="colFirst"><code>int</code></td>
        <td class="colLast"><code>id()</code>
        <div class="block">
        Get the id of this vertex.
        </div></td>
        </tr>
        <tr id="i8" class="odd altColor">
        <td class="colFirst"><code>void</code></td>
        <td class="colLast"><code>init()</code>
        <div class="block">
        Initialise this vertex using the schema of the graph.
        </div></td>
        </tr>
        <tr id="i9" class="even rowColor">
        <td class="colFirst"><code>int</code></td>
        <td class="colLast"><code>linkCount()</code>
        <div class="block">
        Get the number of links connected to this vertex.
        </div></td>
        </tr>
        <tr id="i10" class="odd altColor">
        <td class="colFirst"><code>SVertexLinkIterator</code></td>
        <td class="colLast"><code>links()</code>
        <div class="block">
        Get an iterator over links attached to this vertex.
        </div></td>
        </tr>
        <tr id="i11" class="even rowColor">
        <td class="colFirst"><code>java.lang.String</code></td>
        <td class="colLast"><code>toString()</code> </td>
        </tr>
        <tr id="i12" class="odd altColor">
        <td class="colFirst"><code>int</code></td>
        <td class="colLast"><code>transactionCount()</code>
        <div class="block">
        Get the number of transactions connected to this vertex.
        </div></td>
        </tr>
        <tr id="i13" class="even rowColor">
        <td class="colFirst"><code>SVertexTransactionIterator</code></td>
        <td class="colLast"><code>transactions()</code>
        <div class="block">
        Get an iterator over transactions attached to this vertex.
        </div></td>
        </tr>
        </tbody>
        </table>

        -   <span
            id="methods.inherited.from.class.java.lang.Object"></span>

            ### Methods inherited from class java.lang.Object

            `clone, finalize, getClass, notify, notifyAll, wait, wait, wait`

</div>

<div class="details">

-   -   <span id="constructor.detail"></span>

        ### Constructor Detail

        <span
        id="SVertex-au.gov.asd.tac.constellation.graph.GraphReadMethods-int-"></span>

        -   #### SVertex

                public SVertex(au.gov.asd.tac.constellation.graph.GraphReadMethods hlgraph,
                               int id)

    <!-- -->

    -   <span id="method.detail"></span>

        ### Method Detail

        <span id="id--"></span>

        -   #### id

                public int id()

            <div class="block">

            Get the id of this vertex.

            </div>

            <span class="returnLabel">Returns:</span>  
            the id of this vertex.

        <span id="init--"></span>

        -   #### init

                public void init()

            <div class="block">

            Initialise this vertex using the schema of the graph.

            </div>

        <span id="complete--"></span>

        -   #### complete

                public void complete()

            <div class="block">

            Complete this vertex using the schema of the graph.

            </div>

        <span id="transactionCount--"></span>

        -   #### transactionCount

                public int transactionCount()

            <div class="block">

            Get the number of transactions connected to this vertex.

            </div>

            <span class="returnLabel">Returns:</span>  
            the number of transactions connected to this vertex.

        <span id="transactions--"></span>

        -   #### transactions

                public SVertexTransactionIterator transactions()

            <div class="block">

            Get an iterator over transactions attached to this vertex.

            </div>

            <span class="returnLabel">Returns:</span>  
            a transaction iterator.

        <span id="edgeCount--"></span>

        -   #### edgeCount

                public int edgeCount()

            <div class="block">

            Get the number of edges connected to this vertex.

            </div>

            <span class="returnLabel">Returns:</span>  
            the number of edges connected to this vertex.

        <span id="edges--"></span>

        -   #### edges

                public SVertexEdgeIterator edges()

            <div class="block">

            Get an iterator over edges attached to this vertex.

            </div>

            <span class="returnLabel">Returns:</span>  
            a edge iterator.

        <span id="linkCount--"></span>

        -   #### linkCount

                public int linkCount()

            <div class="block">

            Get the number of links connected to this vertex.

            </div>

            <span class="returnLabel">Returns:</span>  
            the number of links connected to this vertex.

        <span id="links--"></span>

        -   #### links

                public SVertexLinkIterator links()

            <div class="block">

            Get an iterator over links attached to this vertex.

            </div>

            <span class="returnLabel">Returns:</span>  
            a link iterator.

        <span id="Z:Z__getitem__-java.lang.Object-"></span>

        -   #### \_\_getitem\_\_

                public java.lang.Object __getitem__(java.lang.Object key)

            <div class="block">

            Python evaluation of self\[key\].

            </div>

            <span class="paramLabel">Parameters:</span>  
            `key` - an object representing the attribute to be queried.

            <span class="returnLabel">Returns:</span>  
            the value of the specified attribute.

        <span
        id="Z:Z__setitem__-java.lang.Object-java.lang.Object-"></span>

        -   #### \_\_setitem\_\_

                public java.lang.Object __setitem__(java.lang.Object key,
                                                    java.lang.Object value)

            <div class="block">

            Python assignment to self\[key\].

            </div>

            <span class="paramLabel">Parameters:</span>  
            `key` - an object representing the attribute to be set.

            `value` - the new value for the specified attribute.

            <span class="returnLabel">Returns:</span>  
            the set value.

        <span id="hashCode--"></span>

        -   #### hashCode

                public int hashCode()

            <span class="overrideSpecifyLabel">Overrides:</span>  
            `hashCode` in class `java.lang.Object`

        <span id="equals-java.lang.Object-"></span>

        -   #### equals

                public boolean equals(java.lang.Object obj)

            <span class="overrideSpecifyLabel">Overrides:</span>  
            `equals` in class `java.lang.Object`

        <span id="toString--"></span>

        -   #### toString

                public java.lang.String toString()

            <span class="overrideSpecifyLabel">Overrides:</span>  
            `toString` in class `java.lang.Object`

</div>

</div>
