<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
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

-   [<font
    class="typeNameLink">Prev Class</font>](../ext/docs/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/SEdge.md "class in au.gov.asd.tac.constellation.views.scripting.graph")
-   [<font
    class="typeNameLink">Next Class</font>](../ext/docs/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/SLink.md "class in au.gov.asd.tac.constellation.views.scripting.graph")

<!-- -->

-   [All Classes](../ext/docs/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/allclasses-noframe.md)

<div>

</div>

<div>

-   Summary: 
-   Nested | 
-   [Field](#field.summary) | 
-   [Constructor](#constructor.summary) | 
-   [Method](#method.summary)

<!-- -->

-   Detail: 
-   [Field](#field.detail) | 
-   [Constructor](#constructor.detail) | 
-   [Method](#method.detail)

</div>

<span id="skip.navbar.top"></span>

</div>

<div class="header">

<div class="subTitle">

au.gov.asd.tac.constellation.views.scripting.graph

</div>

## Class SGraph

</div>

<div class="contentContainer">

-   java.lang.Object

-   -   au.gov.asd.tac.constellation.views.scripting.graph.SGraph

<div class="description">

-   -
    ------------------------------------------------------------------------ 

        public class SGraph
        extends java.lang.Object

    <div class="block">

    A representation of a graph for use with scripting.

    </div>

    <span class="simpleTagLabel">Author:</span>  
    algol, cygnus_x-1

</div>

<div class="summary">

-   -   <span id="field.summary"></span>

        ### Field Summary

        <table class="memberSummary" data-border="0" data-cellpadding="3" data-cellspacing="0" data-summary="Field Summary table, listing fields, and an explanation">
        <caption><span>Fields</span><span class="tabEnd"> </span></caption>
        <colgroup>
        <col style="width: 50%" />
        <col style="width: 50%" />
        </colgroup>
        <thead>
        <tr class="header">
        <th class="colFirst" scope="col">Modifier and Type</th>
        <th class="colLast" scope="col">Field and Description</th>
        </tr>
        </thead>
        <tbody>
        <tr class="odd altColor">
        <td class="colFirst"><code>static au.gov.asd.tac.constellation.graph.GraphElementType</code></td>
        <td class="colLast"><code>EDGE</code>
        <div class="block">
        A reference to the 'Edge' element type.
        </div></td>
        </tr>
        <tr class="even rowColor">
        <td class="colFirst"><code>static au.gov.asd.tac.constellation.graph.GraphElementType</code></td>
        <td class="colLast"><code>GRAPH</code>
        <div class="block">
        A reference to the 'Graph' element type.
        </div></td>
        </tr>
        <tr class="odd altColor">
        <td class="colFirst"><code>static au.gov.asd.tac.constellation.graph.GraphElementType</code></td>
        <td class="colLast"><code>LINK</code>
        <div class="block">
        A reference to the 'Link' element type.
        </div></td>
        </tr>
        <tr class="even rowColor">
        <td class="colFirst"><code>static au.gov.asd.tac.constellation.graph.GraphElementType</code></td>
        <td class="colLast"><code>TRANSACTION</code>
        <div class="block">
        A reference to the 'Transaction' element type.
        </div></td>
        </tr>
        <tr class="odd altColor">
        <td class="colFirst"><code>static au.gov.asd.tac.constellation.graph.GraphElementType</code></td>
        <td class="colLast"><code>VERTEX</code>
        <div class="block">
        A reference to the 'Vertex' element type.
        </div></td>
        </tr>
        </tbody>
        </table>

        Fields<span class="tabEnd"> </span>

    <!-- -->

    -   <span id="constructor.summary"></span>

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
        <td class="colOne"><code>SGraph(au.gov.asd.tac.constellation.graph.Graph graph)</code> </td>
        </tr>
        <tr class="even rowColor">
        <td class="colOne"><code>SGraph(javax.script.ScriptEngine engine,                                                 au.gov.asd.tac.constellation.graph.Graph graph)</code> </td>
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
        <td class="colFirst"><code>void</code></td>
        <td class="colLast"><code>cleanup()</code>
        <div class="block">
        Clean up any graphs created by scripting which remain in memory.
        </div></td>
        </tr>
        <tr id="i1" class="even rowColor">
        <td class="colFirst"><code>javax.script.ScriptEngine</code></td>
        <td class="colLast"><code>getEngine()</code>
        <div class="block">
        Get the scripting engine currently being used to evaluate scripts.
        </div></td>
        </tr>
        <tr id="i2" class="odd altColor">
        <td class="colFirst"><code>au.gov.asd.tac.constellation.graph.Graph</code></td>
        <td class="colLast"><code>getGraph()</code>
        <div class="block">
        Get the actual graph on which scripts are being executed.
        </div></td>
        </tr>
        <tr id="i3" class="even rowColor">
        <td class="colFirst"><code>SReadableGraph</code></td>
        <td class="colLast"><code>readableGraph()</code>
        <div class="block">
        Get a read lock on the graph so that its data can be interrogated.
        </div></td>
        </tr>
        <tr id="i4" class="odd altColor">
        <td class="colFirst"><code>void</code></td>
        <td class="colLast"><code>setEngine(javax.script.ScriptEngine engine)</code>
        <div class="block">
        Set a new scripting engine to be used for evaluating scripts.
        </div></td>
        </tr>
        <tr id="i5" class="even rowColor">
        <td class="colFirst"><code>java.lang.String</code></td>
        <td class="colLast"><code>toString()</code> </td>
        </tr>
        <tr id="i6" class="odd altColor">
        <td class="colFirst"><code>void</code></td>
        <td class="colLast"><code>withReadableGraph(java.lang.Object callback)</code>
        <div class="block">
        Evaluate a function against a read lock on the graph with releases handled automatically.
        </div></td>
        </tr>
        <tr id="i7" class="even rowColor">
        <td class="colFirst"><code>void</code></td>
        <td class="colLast"><code>withWritableGraph(java.lang.Object callback)</code>
        <div class="block">
        Evaluate a function against a write lock on the graph with commits or rollbacks handled automatically.
        </div></td>
        </tr>
        <tr id="i8" class="odd altColor">
        <td class="colFirst"><code>SWritableGraph</code></td>
        <td class="colLast"><code>writableGraph(java.lang.String editName)</code>
        <div class="block">
        Get a write lock on the graph so that edits can be made to its data.
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

-   -   <span id="field.detail"></span>

        ### Field Detail

        <span id="GRAPH"></span>

        -   #### GRAPH

                public static final au.gov.asd.tac.constellation.graph.GraphElementType GRAPH

            <div class="block">

            A reference to the 'Graph' element type.

            </div>

        <span id="VERTEX"></span>

        -   #### VERTEX

                public static final au.gov.asd.tac.constellation.graph.GraphElementType VERTEX

            <div class="block">

            A reference to the 'Vertex' element type.

            </div>

        <span id="TRANSACTION"></span>

        -   #### TRANSACTION

                public static final au.gov.asd.tac.constellation.graph.GraphElementType TRANSACTION

            <div class="block">

            A reference to the 'Transaction' element type.

            </div>

        <span id="EDGE"></span>

        -   #### EDGE

                public static final au.gov.asd.tac.constellation.graph.GraphElementType EDGE

            <div class="block">

            A reference to the 'Edge' element type.

            </div>

        <span id="LINK"></span>

        -   #### LINK

                public static final au.gov.asd.tac.constellation.graph.GraphElementType LINK

            <div class="block">

            A reference to the 'Link' element type.

            </div>

    <!-- -->

    -   <span id="constructor.detail"></span>

        ### Constructor Detail

        <span
        id="SGraph-javax.script.ScriptEngine-au.gov.asd.tac.constellation.graph.Graph-"></span>

        -   #### SGraph

                public SGraph(javax.script.ScriptEngine engine,
                              au.gov.asd.tac.constellation.graph.Graph graph)

        <span
        id="SGraph-au.gov.asd.tac.constellation.graph.Graph-"></span>

        -   #### SGraph

                public SGraph(au.gov.asd.tac.constellation.graph.Graph graph)

    <!-- -->

    -   <span id="method.detail"></span>

        ### Method Detail

        <span id="getEngine--"></span>

        -   #### getEngine

                public javax.script.ScriptEngine getEngine()

            <div class="block">

            Get the scripting engine currently being used to evaluate
            scripts.

            </div>

            <span class="returnLabel">Returns:</span>  
            the current scripting engine.

        <span id="setEngine-javax.script.ScriptEngine-"></span>

        -   #### setEngine

                public void setEngine(javax.script.ScriptEngine engine)

            <div class="block">

            Set a new scripting engine to be used for evaluating
            scripts.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `engine` - the scripting engine to use.

        <span id="getGraph--"></span>

        -   #### getGraph

                public au.gov.asd.tac.constellation.graph.Graph getGraph()

            <div class="block">

            Get the actual graph on which scripts are being executed.

            </div>

            <span class="returnLabel">Returns:</span>  
            the graph on which scripts are being executed.

        <span id="cleanup--"></span>

        -   #### cleanup

                public void cleanup()

            <div class="block">

            Clean up any graphs created by scripting which remain in
            memory.
            After a script completes, we have no way of reliably knowing
            if the script has called .release(), .commit(), or
            .rollback() on the graphs it used. It is undesirable to
            leave graphs remaining in memory if they are no longer in
            use. To address this issue, we keep track of any graphs used
            by scripting and offer this method to attempt to force them
            out of memory if they still exist.

            </div>

        <span id="readableGraph--"></span>

        -   #### readableGraph

                public SReadableGraph readableGraph()
                                             throws java.lang.InterruptedException

            <div class="block">

            Get a read lock on the graph so that its data can be
            interrogated.
            Note: If this method is called in a Python script using the
            'with' statement, a context manager will be created for you
            to automatically handle releases as appropriate.

            </div>

            <span class="returnLabel">Returns:</span>  
            a graph with an active read lock.

            <span class="throwsLabel">Throws:</span>  
            `java.lang.InterruptedException`

        <span id="withReadableGraph-java.lang.Object-"></span>

        -   #### withReadableGraph

                public void withReadableGraph(java.lang.Object callback)
                                       throws javax.script.ScriptException,
                                              java.lang.InterruptedException

            <div class="block">

            Evaluate a function against a read lock on the graph with
            releases handled automatically.
            Note: This method will only work for Python scripts as it
            makes use of Python specific syntax.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `callback` - the function to evaluate.

            <span class="throwsLabel">Throws:</span>  
            `javax.script.ScriptException`

            `java.lang.InterruptedException`

        <span id="writableGraph-java.lang.String-"></span>

        -   #### writableGraph

                public SWritableGraph writableGraph(java.lang.String editName)
                                             throws java.lang.InterruptedException

            <div class="block">

            Get a write lock on the graph so that edits can be made to
            its data.
            Note: If this method is called in a Python script using the
            'with' statement, a context manager will be created for you
            to automatically handle commits and rollbacks as
            appropriate.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `editName` - a name for the edit operation.

            <span class="returnLabel">Returns:</span>  
            a graph with an active write lock.

            <span class="throwsLabel">Throws:</span>  
            `java.lang.InterruptedException`

        <span id="withWritableGraph-java.lang.Object-"></span>

        -   #### withWritableGraph

                public void withWritableGraph(java.lang.Object callback)
                                       throws javax.script.ScriptException,
                                              java.lang.InterruptedException

            <div class="block">

            Evaluate a function against a write lock on the graph with
            commits or rollbacks handled automatically.
            Note: This method will only work for Python scripts as it
            makes use of Python specific syntax.

            </div>

            <span class="paramLabel">Parameters:</span>  
            `callback` - the function to evaluate.

            <span class="throwsLabel">Throws:</span>  
            `javax.script.ScriptException`

            `java.lang.InterruptedException`

        <span id="toString--"></span>

        -   #### toString

                public java.lang.String toString()

            <span class="overrideSpecifyLabel">Overrides:</span>  
            `toString` in class `java.lang.Object`

</div>

</div>
