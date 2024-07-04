<meta http-equiv="Content-Type">
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

-   Prev Class
-   [<span
    class="typeNameLink">Next Class</span>](../constellation/CoreScriptingView/src/au/gov/asd/tac/constellation/views/scripting/docs/javadoc/graph/SCollection.md "class in au.gov.asd.tac.constellation.views.scripting.graph")

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

## Class SAttribute

</div>

<div class="contentContainer">

-   java.lang.Object

-   -   au.gov.asd.tac.constellation.views.scripting.graph.SAttribute

<div class="description">

-   -
    ------------------------------------------------------------------------

        public class SAttribute
        extends java.lang.Object

    <div class="block">

    A representation of an attribute for use with scripting.

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
        <td class="colOne"><code>SAttribute(au.gov.asd.tac.constellation.graph.GraphReadMethods readableGraph,                                                 au.gov.asd.tac.constellation.graph.GraphElementType elementType,                                                 java.lang.String name)</code> </td>
        </tr>
        <tr class="even rowColor">
        <td class="colOne"><code>SAttribute(au.gov.asd.tac.constellation.graph.GraphReadMethods readableGraph,                                                 int id)</code> </td>
        </tr>
        <tr class="odd altColor">
        <td class="colOne"><code>SAttribute(au.gov.asd.tac.constellation.graph.GraphReadMethods readableGraph,                                                 int id,                                                 au.gov.asd.tac.constellation.graph.GraphElementType elementType,                                                 java.lang.String attributeType,                                                 java.lang.String name,                                                 java.lang.String description,                                                 java.lang.Object defaultValue,                                                 java.lang.String mergerId)</code> </td>
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
        <td class="colFirst"><code>java.lang.String</code></td>
        <td class="colLast"><code>attributeType()</code>
        <div class="block">
        Get the type of this attribute.
        </div></td>
        </tr>
        <tr id="i1" class="even rowColor">
        <td class="colFirst"><code>java.lang.Object</code></td>
        <td class="colLast"><code>defaultValue()</code>
        <div class="block">
        Get the default value of this attribute.
        </div></td>
        </tr>
        <tr id="i2" class="odd altColor">
        <td class="colFirst"><code>java.lang.String</code></td>
        <td class="colLast"><code>description()</code>
        <div class="block">
        Get a description of this attribute.
        </div></td>
        </tr>
        <tr id="i3" class="even rowColor">
        <td class="colFirst"><code>au.gov.asd.tac.constellation.graph.GraphElementType</code></td>
        <td class="colLast"><code>elementType()</code>
        <div class="block">
        Get the element type of this attribute.
        </div></td>
        </tr>
        <tr id="i4" class="odd altColor">
        <td class="colFirst"><code>int</code></td>
        <td class="colLast"><code>id()</code>
        <div class="block">
        Get the id of this attribute.
        </div></td>
        </tr>
        <tr id="i5" class="even rowColor">
        <td class="colFirst"><code>java.lang.String</code></td>
        <td class="colLast"><code>mergerId()</code>
        <div class="block">
        Get the id of the merger for this attribute.
        </div></td>
        </tr>
        <tr id="i6" class="odd altColor">
        <td class="colFirst"><code>java.lang.String</code></td>
        <td class="colLast"><code>name()</code>
        <div class="block">
        Get the name of this attribute.
        </div></td>
        </tr>
        </tbody>
        </table>


        -   <span
            id="methods.inherited.from.class.java.lang.Object"></span>

            ### Methods inherited from class java.lang.Object

            `clone, equals, finalize, getClass, hashCode, notify, notifyAll, toString, wait, wait, wait`

</div>

<div class="details">

-   -   <span id="constructor.detail"></span>

        ### Constructor Detail

        <span
        id="SAttribute-au.gov.asd.tac.constellation.graph.GraphReadMethods-int-au.gov.asd.tac.constellation.graph.GraphElementType-java.lang.String-java.lang.String-java.lang.String-java.lang.Object-java.lang.String-"></span>

        -   #### SAttribute

                public SAttribute(au.gov.asd.tac.constellation.graph.GraphReadMethods readableGraph,
                                  int id,
                                  au.gov.asd.tac.constellation.graph.GraphElementType elementType,
                                  java.lang.String attributeType,
                                  java.lang.String name,
                                  java.lang.String description,
                                  java.lang.Object defaultValue,
                                  java.lang.String mergerId)

        <span
        id="SAttribute-au.gov.asd.tac.constellation.graph.GraphReadMethods-int-"></span>

        -   #### SAttribute

                public SAttribute(au.gov.asd.tac.constellation.graph.GraphReadMethods readableGraph,
                                  int id)

        <span
        id="SAttribute-au.gov.asd.tac.constellation.graph.GraphReadMethods-au.gov.asd.tac.constellation.graph.GraphElementType-java.lang.String-"></span>

        -   #### SAttribute

                public SAttribute(au.gov.asd.tac.constellation.graph.GraphReadMethods readableGraph,
                                  au.gov.asd.tac.constellation.graph.GraphElementType elementType,
                                  java.lang.String name)

    <!-- -->

    -   <span id="method.detail"></span>

        ### Method Detail

        <span id="id--"></span>

        -   #### id

                public int id()

            <div class="block">

            Get the id of this attribute.

            </div>

            <span class="returnLabel">Returns:</span>  
            the id of this attribute.

        <span id="elementType--"></span>

        -   #### elementType

                public au.gov.asd.tac.constellation.graph.GraphElementType elementType()

            <div class="block">

            Get the element type of this attribute.

            </div>

            <span class="returnLabel">Returns:</span>  
            the element type of this attribute.

        <span id="attributeType--"></span>

        -   #### attributeType

                public java.lang.String attributeType()

            <div class="block">

            Get the type of this attribute.

            </div>

            <span class="returnLabel">Returns:</span>  
            the type of this attribute.

        <span id="name--"></span>

        -   #### name

                public java.lang.String name()

            <div class="block">

            Get the name of this attribute.

            </div>

            <span class="returnLabel">Returns:</span>  
            the name of this attribute.

        <span id="description--"></span>

        -   #### description

                public java.lang.String description()

            <div class="block">

            Get a description of this attribute.

            </div>

            <span class="returnLabel">Returns:</span>  
            a description of this attribute.

        <span id="defaultValue--"></span>

        -   #### defaultValue

                public java.lang.Object defaultValue()

            <div class="block">

            Get the default value of this attribute.

            </div>

            <span class="returnLabel">Returns:</span>  
            the default value of this attribute.

        <span id="mergerId--"></span>

        -   #### mergerId

                public java.lang.String mergerId()

            <div class="block">

            Get the id of the merger for this attribute.

            </div>

            <span class="returnLabel">Returns:</span>  
            the merger id of this attribute.

</div>

</div>

