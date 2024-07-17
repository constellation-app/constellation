<div class="topNav">

<span id="navbar.top"></span>

<div class="skipNav">

[Skip navigation links](../ext/docs/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/iterators/SVertexTransactionIterator.md#skip.navbar.top "Skip navigation links")

</div>

<span id="navbar.top.firstrow"></span>

-   [Overview](../ext/docs/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/overview-summary.md)
-   [Package](../ext/docs/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/iterators/package-summary.md)
-   Class
-   [Tree](../ext/docs/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/iterators/package-tree.md)
-   [Deprecated](../ext/docs/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/deprecated-list.md)
-   [Index](../ext/docs/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/index-all.md)
-   [Help](../ext/docs/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/help-doc.md)

<div class="aboutLanguage">

au.gov.asd.tac.constellation.views.scripting 1.0

</div>

</div>

<div class="subNav">

-   [<span class="typeNameLink">Prev Class</span>](../ext/docs/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/iterators/SVertexLinkIterator.md "class in au.gov.asd.tac.constellation.views.scripting.graph.iterators")
-   Next Class

<!-- -->

-   [All Classes](../ext/docs/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/allclasses-noframe.md)

<!-- -->

<div>

</div>

<div>

-   Summary: 
-   Nested | 
-   Field | 
-   [Constructor](../ext/docs/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/iterators/SVertexTransactionIterator.md#constructor.summary) | 
-   [Method](../ext/docs/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/iterators/SVertexTransactionIterator.md#method.summary)

<!-- -->

-   Detail: 
-   Field | 
-   [Constructor](../ext/docs/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/iterators/SVertexTransactionIterator.md#constructor.detail) | 
-   [Method](../ext/docs/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/iterators/SVertexTransactionIterator.md#method.detail)

</div>

<span id="skip.navbar.top"></span>

</div>

<div class="header">

<div class="subTitle">

au.gov.asd.tac.constellation.views.scripting.graph.iterators

</div>

## Class SVertexTransactionIterator

</div>

<div class="contentContainer">

-   java.lang.Object

-   -   au.gov.asd.tac.constellation.views.scripting.graph.iterators.SVertexTransactionIterator

<div class="description">

-   All Implemented Interfaces:  
    java.util.Iterator\<[STransaction](../ext/docs/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/STransaction.md "class in au.gov.asd.tac.constellation.views.scripting.graph")\>

    ------------------------------------------------------------------------

      

        public class SVertexTransactionIterator
        extends java.lang.Object
        implements java.util.Iterator<STransaction>

    <div class="block">

    An iterator for accessing transactions from the context of vertices
    via scripting.

    </div>

    <span class="simpleTagLabel">Author:</span>  
    cygnus_x-1

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
        <td class="colOne"><code>SVertexTransactionIterator(au.gov.asd.tac.constellation.graph.GraphReadMethods readableGraph,                                                 int vertexId)</code> </td>
        </tr>
        </tbody>
        </table>

        Constructors<span class="tabEnd"> </span>

    <!-- -->

    -   <span id="method.summary"></span>

        ### Method Summary

        <table class="memberSummary" data-border="0" data-cellpadding="3" data-cellspacing="0" data-summary="Method Summary table, listing methods, and an explanation">
        <thead>
        <tr class="header">
        <th class="colFirst" scope="col">Modifier and Type</th>
        <th class="colLast" scope="col">Method and Description</th>
        </tr>
        </thead>
        <tbody>
        <tr id="i0" class="odd altColor">
        <td class="colFirst"><code>boolean</code></td>
        <td class="colLast"><code>hasNext()</code> </td>
        </tr>
        <tr id="i1" class="even rowColor">
        <td class="colFirst"><code>STransaction</code></td>
        <td class="colLast"><code>next()</code> </td>
        </tr>
        </tbody>
        </table>

        -   <span
            id="methods.inherited.from.class.java.lang.Object"></span>

            ### Methods inherited from class java.lang.Object

            `clone, equals, finalize, getClass, hashCode, notify, notifyAll, toString, wait, wait, wait`

        <!-- -->

        -   <span
            id="methods.inherited.from.class.java.util.Iterator"></span>

            ### Methods inherited from interface java.util.Iterator

            `forEachRemaining, remove`

</div>

<div class="details">

-   -   <span id="constructor.detail"></span>

        ### Constructor Detail

        <span
        id="SVertexTransactionIterator-au.gov.asd.tac.constellation.graph.GraphReadMethods-int-"></span>

        -   #### SVertexTransactionIterator

                public SVertexTransactionIterator(au.gov.asd.tac.constellation.graph.GraphReadMethods readableGraph,
                                                  int vertexId)

    <!-- -->

    -   <span id="method.detail"></span>

        ### Method Detail

        <span id="hasNext--"></span>

        -   #### hasNext

                public boolean hasNext()

            <span class="overrideSpecifyLabel">Specified by:</span>  
            `hasNext` in interface `java.util.Iterator<STransaction>`

        <span id="next--"></span>

        -   #### next

                public STransaction next()

            <span class="overrideSpecifyLabel">Specified by:</span>  
            `next` in interface `java.util.Iterator<STransaction>`

</div>

</div>
