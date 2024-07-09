
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
    class="typeNameLink">Prev Class</span>](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/SAttribute.md "class in au.gov.asd.tac.constellation.views.scripting.graph")
-   [<span
    class="typeNameLink">Next Class</span>](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/SEdge.md "class in au.gov.asd.tac.constellation.views.scripting.graph")

<!-- -->

-   [All Classes](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/allclasses-noframe.md)

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

## Class SCollection

</div>

<div class="contentContainer">

-   java.lang.Object

-   -   au.gov.asd.tac.constellation.views.scripting.graph.SCollection

<div class="description">

-   -
    ------------------------------------------------------------------------ 

        public class SCollection
        extends java.lang.Object

    <div class="block">

    A collection which efficiently stores vertices or transactions for
    use with scripting.

    </div>

    <span class="simpleTagLabel">Author:</span>  
    algol

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
        <td class="colOne"><code>SCollection(javax.script.ScriptEngine engine,                                                 au.gov.asd.tac.constellation.graph.GraphReadMethods readableGraph,                                                 au.gov.asd.tac.constellation.graph.GraphElementType elementType,                                                 java.util.BitSet elementIds)</code> </td>
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
        <td class="colFirst"><code>java.util.BitSet</code></td>
        <td class="colLast"><code>elementIds()</code>
        <div class="block">
        Get the ids of elements in this collection.
        </div></td>
        </tr>
        <tr id="i1" class="even rowColor">
        <td class="colFirst"><code>au.gov.asd.tac.constellation.graph.GraphElementType</code></td>
        <td class="colLast"><code>elementType()</code>
        <div class="block">
        Get the element type of elements in this collection.
        </div></td>
        </tr>
        <tr id="i2" class="odd altColor">
        <td class="colFirst"><code>boolean</code></td>
        <td class="colLast"><code>empty()</code>
        <div class="block">
        Check if this collection is empty.
        </div></td>
        </tr>
        <tr id="i3" class="even rowColor">
        <td class="colFirst"><code>SCollection</code></td>
        <td class="colLast"><code>filter(java.lang.Object callback)</code>
        <div class="block">
        Filter this collection using the provided function.
        </div></td>
        </tr>
        <tr id="i4" class="odd altColor">
        <td class="colFirst"><code>SCollection</code></td>
        <td class="colLast"><code>intersection(SCollection other)</code>
        <div class="block">
        Get the intersection of this collection with another.
        </div></td>
        </tr>
        <tr id="i5" class="even rowColor">
        <td class="colFirst"><code>int</code></td>
        <td class="colLast"><code>size()</code>
        <div class="block">
        Get the size of this collection.
        </div></td>
        </tr>
        <tr id="i6" class="odd altColor">
        <td class="colFirst"><code>java.lang.String</code></td>
        <td class="colLast"><code>toString()</code> </td>
        </tr>
        <tr id="i7" class="even rowColor">
        <td class="colFirst"><code>SCollection</code></td>
        <td class="colLast"><code>union(SCollection other)</code>
        <div class="block">
        Get the union of this collection with another.
        </div></td>
        </tr>
        </tbody>
        </table>

        
        -   <span
            id="methods.inherited.from.class.java.lang.Object"></span>

            ### Methods inherited from class java.lang.Object

            `clone, equals, finalize, getClass, hashCode, notify, notifyAll, wait, wait, wait`

</div>

<div class="details">

-   -   <span id="constructor.detail"></span>

        ### Constructor Detail

        <span
        id="SCollection-javax.script.ScriptEngine-au.gov.asd.tac.constellation.graph.GraphReadMethods-au.gov.asd.tac.constellation.graph.GraphElementType-java.util.BitSet-"></span>

        -   #### SCollection

                public SCollection(javax.script.ScriptEngine engine,
                                   au.gov.asd.tac.constellation.graph.GraphReadMethods readableGraph,
                                   au.gov.asd.tac.constellation.graph.GraphElementType elementType,
                                   java.util.BitSet elementIds)

    <!-- -->

    -   <span id="method.detail"></span>

        ### Method Detail

        <span id="elementType--"></span>

        -   #### elementType

                public au.gov.asd.tac.constellation.graph.GraphElementType elementType()

            <div class="block">

            Get the element type of elements in this collection.

            </div>

            <span class="returnLabel">Returns:</span>  
            the element type.

        <span id="elementIds--"></span>

        -   #### elementIds

                public java.util.BitSet elementIds()

            <div class="block">

            Get the ids of elements in this collection.

            </div>

            <span class="returnLabel">Returns:</span>  
            the element ids.

        <span id="filter-java.lang.Object-"></span>

        -   #### filter

                public SCollection filter(java.lang.Object callback)
                                   throws javax.script.ScriptException

            <div class="block">

            Filter this collection using the provided function. The
            provided function must only return true or false. Note: This
            method will only work for Python scripts as it makes use of
            Python specific syntax.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `callback` - the function which returns true or false.

            <span class="returnLabel">Returns:</span>  
            a collection of elements as a
            [`SCollection`](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/SCollection.md "class in au.gov.asd.tac.constellation.views.scripting.graph").

            <span class="throwsLabel">Throws:</span>  
            `javax.script.ScriptException`

        <span
        id="intersection-au.gov.asd.tac.constellation.views.scripting.graph.SCollection-"></span>

        -   #### intersection

                public SCollection intersection(SCollection other)

            <div class="block">

            Get the intersection of this collection with another.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `other` - the other collection as a
            [`SCollection`](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/SCollection.md "class in au.gov.asd.tac.constellation.views.scripting.graph").

            <span class="returnLabel">Returns:</span>  
            the intersection of this collection and another as a
            [`SCollection`](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/SCollection.md "class in au.gov.asd.tac.constellation.views.scripting.graph").

        <span
        id="union-au.gov.asd.tac.constellation.views.scripting.graph.SCollection-"></span>

        -   #### union

                public SCollection union(SCollection other)

            <div class="block">

            Get the union of this collection with another.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `other` - the other collection as a
            [`SCollection`](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/SCollection.md "class in au.gov.asd.tac.constellation.views.scripting.graph").

            <span class="returnLabel">Returns:</span>  
            the union of this collection and another as a
            [`SCollection`](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/SCollection.md "class in au.gov.asd.tac.constellation.views.scripting.graph").

        <span id="empty--"></span>

        -   #### empty

                public boolean empty()

            <div class="block">

            Check if this collection is empty.

            </div>

            <span class="returnLabel">Returns:</span>  
            true if this collection is empty, false otherwise.

        <span id="size--"></span>

        -   #### size

                public int size()

            <div class="block">

            Get the size of this collection.

            </div>

            <span class="returnLabel">Returns:</span>  
            the size of this collection.

        <span id="toString--"></span>

        -   #### toString

                public java.lang.String toString()

            <span class="overrideSpecifyLabel">Overrides:</span>  
            `toString` in class `java.lang.Object`

</div>

</div>
