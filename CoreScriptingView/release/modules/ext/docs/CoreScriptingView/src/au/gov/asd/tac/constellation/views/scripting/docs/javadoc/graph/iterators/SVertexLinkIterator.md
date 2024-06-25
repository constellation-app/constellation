<div class="topNav">

<span id="navbar.top"></span>

<div class="skipNav">

[Skip navigation links](#skip.navbar.top "Skip navigation links")

</div>

<span id="navbar.top.firstrow"></span>

-   [Overview](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/overview-summary.md)
-   [Package](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/iterators/package-summary.md)
-   Class
-   [Tree](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/iterators/package-tree.md)
-   [Deprecated](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/deprecated-list.md)
-   [Index](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/index-all.md)
-   [Help](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/help-doc.md)

<div class="aboutLanguage">

au.gov.asd.tac.constellation.views.scripting 1.0

</div>

</div>

<div class="subNav">

-   [<span
    class="typeNameLink">Prev Class</span>](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/iterators/SVertexIterator.md "class in au.gov.asd.tac.constellation.views.scripting.graph.iterators")
-   [<span
    class="typeNameLink">Next Class</span>](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/iterators/SVertexTransactionIterator.md "class in au.gov.asd.tac.constellation.views.scripting.graph.iterators")

<!-- -->

-   [Frames](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/iterators/SVertexLinkIterator.md)
-   [No Frames](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/iterators/SVertexLinkIterator.md)

<!-- -->

-   [All Classes](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/allclasses-noframe.md)

<div>

</div>

<div>

-   Summary: 
-   Nested | 
-   Field | 
-   [Constr](#constructor.summary) | 
-   [Method](#method.summary)

<!-- -->

-   Detail: 
-   Field | 
-   [Constr](#constructor.detail) | 
-   [Method](#method.detail)

</div>

<span id="skip.navbar.top"></span>

</div>

<div class="header">

<div class="subTitle">

au.gov.asd.tac.constellation.views.scripting.graph.iterators

</div>

## Class SVertexLinkIterator

</div>

<div class="contentContainer">

-   java.lang.Object

-   -   au.gov.asd.tac.constellation.views.scripting.graph.iterators.SVertexLinkIterator

<div class="description">

-   All Implemented Interfaces:  
    java.util.Iterator\<[SLink](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/SLink.md "class in au.gov.asd.tac.constellation.views.scripting.graph")\>

    ------------------------------------------------------------------------

      

        public class SVertexLinkIterator
        extends java.lang.Object
        implements java.util.Iterator<SLink>

    <div class="block">

    An iterator for accessing links from the context of vertices via
    scripting.

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
        <td class="colOne"><code>SVertexLinkIterator(au.gov.asd.tac.constellation.graph.GraphReadMethods readableGraph,                                                 int vertexId)</code> </td>
        </tr>
        </tbody>
        </table>

        Constructors<span class="tabEnd"> </span>

    <!-- -->

    -   <span id="method.summary"></span>

        ### Method Summary

        <table class="memberSummary" data-border="0" data-cellpadding="3" data-cellspacing="0" data-summary="Method Summary table, listing methods, and an explanation">
        <caption><span id="t0" class="activeTableTab"><span>All Methods</span><span class="tabEnd"> </span></span><span id="t2" class="tableTab"><span><a href="javascript:show(2);">Instance Methods</a></span><span class="tabEnd"> </span></span><span id="t4" class="tableTab"><span><a href="javascript:show(8);">Concrete Methods</a></span><span class="tabEnd"> </span></span></caption>
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
        <td class="colFirst"><code>SLink</code></td>
        <td class="colLast"><code>next()</code> </td>
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
        id="SVertexLinkIterator-au.gov.asd.tac.constellation.graph.GraphReadMethods-int-"></span>

        -   #### SVertexLinkIterator

                public SVertexLinkIterator(au.gov.asd.tac.constellation.graph.GraphReadMethods readableGraph,
                                           int vertexId)

    <!-- -->

    -   <span id="method.detail"></span>

        ### Method Detail

        <span id="hasNext--"></span>

        -   #### hasNext

                public boolean hasNext()

            <span class="overrideSpecifyLabel">Specified by:</span>  
            `hasNext` in interface `java.util.Iterator<SLink>`

        <span id="next--"></span>

        -   #### next

                public SLink next()

            <span class="overrideSpecifyLabel">Specified by:</span>  
            `next` in interface `java.util.Iterator<SLink>`

</div>

</div>

<div class="bottomNav">

<span id="navbar.bottom"></span>

<div class="skipNav">

[Skip navigation links](#skip.navbar.bottom "Skip navigation links")

</div>

<span id="navbar.bottom.firstrow"></span>

-   [Overview](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/overview-summary.md)
-   [Package](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/iterators/package-summary.md)
-   Class
-   [Tree](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/iterators/package-tree.md)
-   [Deprecated](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/deprecated-list.md)
-   [Index](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/index-all.md)
-   [Help](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/help-doc.md)

<div class="aboutLanguage">

au.gov.asd.tac.constellation.views.scripting 1.0

</div>

</div>

<div class="subNav">

-   [<span
    class="typeNameLink">Prev Class</span>](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/iterators/SVertexIterator.md "class in au.gov.asd.tac.constellation.views.scripting.graph.iterators")
-   [<span
    class="typeNameLink">Next Class</span>](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/iterators/SVertexTransactionIterator.md "class in au.gov.asd.tac.constellation.views.scripting.graph.iterators")

<!-- -->

-   [Frames](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/iterators/SVertexLinkIterator.md)
-   [No Frames](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/iterators/SVertexLinkIterator.md)

<!-- -->

-   [All Classes](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/allclasses-noframe.md)

<div>

</div>

<div>

-   Summary: 
-   Nested | 
-   Field | 
-   [Constr](#constructor.summary) | 
-   [Method](#method.summary)

<!-- -->

-   Detail: 
-   Field | 
-   [Constr](#constructor.detail) | 
-   [Method](#method.detail)

</div>

<span id="skip.navbar.bottom"></span>

</div>
