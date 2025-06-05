<div class="topNav">

<span id="navbar.top"></span>

<div class="skipNav">

[Skip navigation links](SVertexIterator.md#skip.navbar.top "Skip navigation links")

</div>

<span id="navbar.top.firstrow"></span>

-   [Overview](../../overview-summary.md)
-   [Package](package-summary.md)
-   Class
-   [Tree](package-tree.md)
-   [Deprecated](../../deprecated-list.md)
-   [Index](../../index-all.md)
-   [Help](../../help-doc.md)

<div class="aboutLanguage">

au.gov.asd.tac.constellation.views.scripting 1.0

</div>

</div>

<div class="subNav">

-   [<span
    class="typeNameLink">Prev Class</span>](SVertexEdgeIterator.md "class in au.gov.asd.tac.constellation.views.scripting.graph.iterators")  
-   [<span
    class="typeNameLink">Next Class</span>](SVertexLinkIterator.md "class in au.gov.asd.tac.constellation.views.scripting.graph.iterators")

<!-- -->

-   [All Classes](../../allclasses-noframe.md)

<div>

</div>

<div>

-   Summary: 
-   Nested | 
-   Field | 
-   [Constructor](SVertexIterator.md#constructor.summary) | 
-   [Method](SVertexIterator.md#method.summary)

<!-- -->

-   Detail: 
-   Field | 
-   [Constructor](SVertexIterator.md#constructor.detail) | 
-   [Method](SVertexIterator.md#method.detail)

</div>

<span id="skip.navbar.top"></span>

</div>

<div class="header">

<div class="subTitle">

au.gov.asd.tac.constellation.views.scripting.graph.iterators

</div>

## Class SVertexIterator

</div>

<div class="contentContainer">

-   java.lang.Object

-   -   au.gov.asd.tac.constellation.views.scripting.graph.iterators.SVertexIterator

<div class="description">

-   All Implemented Interfaces:  
    java.util.Iterator\<[SVertex](../SVertex.md "class in au.gov.asd.tac.constellation.views.scripting.graph")\>

    ------------------------------------------------------------------------

      

        public class SVertexIterator
        extends java.lang.Object
        implements java.util.Iterator<SVertex>

    <div class="block">

    An iterator for accessing vertices via scripting.

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
        <td class="colOne"><code>SVertexIterator(au.gov.asd.tac.constellation.graph.GraphReadMethods readableGraph)</code> </td>
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
        <td class="colFirst"><code>SVertex</code></td>
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
        id="SVertexIterator-au.gov.asd.tac.constellation.graph.GraphReadMethods-"></span>

        -   #### SVertexIterator

                public SVertexIterator(au.gov.asd.tac.constellation.graph.GraphReadMethods readableGraph)

    <!-- -->

    -   <span id="method.detail"></span>

        ### Method Detail

        <span id="hasNext--"></span>

        -   #### hasNext

                public boolean hasNext()

            <span class="overrideSpecifyLabel">Specified by:</span>  
            `hasNext` in interface `java.util.Iterator<SVertex>`

        <span id="next--"></span>

        -   #### next

                public SVertex next()

            <span class="overrideSpecifyLabel">Specified by:</span>  
            `next` in interface `java.util.Iterator<SVertex>`

</div>

</div>
