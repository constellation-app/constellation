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
-   Prev Class
-   [[Next Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SCollection.html "class in au.gov.asd.tac.constellation.views.scripting.graph")

```{=html}
<!-- -->
```
-   [Frames](../../../../../../../index.html?au/gov/asd/tac/constellation/scripting/graph/SAttribute.html)
-   [No Frames](SAttribute.html)

```{=html}
<!-- -->
```
-   [All Classes](../../../../../../../allclasses-noframe.html)

<div>

</div>

<div>

-   Summary: 
-   Nested \| 
-   Field \| 
-   [Constr](#constructor.summary) \| 
-   [Method](#method.summary)

```{=html}
<!-- -->
```
-   Detail: 
-   Field \| 
-   [Constr](#constructor.detail) \| 
-   [Method](#method.detail)

</div>

[]{#skip.navbar.top}
:::

::: {.header}
::: {.subTitle}
au.gov.asd.tac.constellation.views.scripting.graph
:::

## Class SAttribute {#class-sattribute .title title="Class SAttribute"}
:::

::: {.contentContainer}
-   java.lang.Object

-   -   au.gov.asd.tac.constellation.views.scripting.graph.SAttribute

::: {.description}
-   

    ------------------------------------------------------------------------

    \

        public class SAttribute
        extends java.lang.Object

    ::: {.block}
    A representation of an attribute for use with scripting.
    :::

    [Author:]{.simpleTagLabel}
    :   cygnus_x-1
:::

::: {.summary}
-   -   []{#constructor.summary}

        ### Constructor Summary

          Constructor and Description
          ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
          `SAttribute(au.gov.asd.tac.constellation.graph.GraphReadMethods readableGraph,                                                 au.gov.asd.tac.constellation.graph.GraphElementType elementType,                                                 java.lang.String name)` 
          `SAttribute(au.gov.asd.tac.constellation.graph.GraphReadMethods readableGraph,                                                 int id)` 
          `SAttribute(au.gov.asd.tac.constellation.graph.GraphReadMethods readableGraph,                                                 int id,                                                 au.gov.asd.tac.constellation.graph.GraphElementType elementType,                                                 java.lang.String attributeType,                                                 java.lang.String name,                                                 java.lang.String description,                                                 java.lang.Object defaultValue,                                                 java.lang.String mergerId)` 

          : Constructors[ ]{.tabEnd}

    ```{=html}
    <!-- -->
    ```
    -   []{#method.summary}

        ### Method Summary

        +-----------------------------------+-----------------------------------+
        | Modifier and Type                 | Method and Description            |
        +===================================+===================================+
        | `java.lang.String`                | `attributeType()`                 |
        |                                   | ::: {.block}                      |
        |                                   | Get the type of this attribute.   |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `java.lang.Object`                | `defaultValue()`                  |
        |                                   | ::: {.block}                      |
        |                                   | Get the default value of this     |
        |                                   | attribute.                        |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `java.lang.String`                | `description()`                   |
        |                                   | ::: {.block}                      |
        |                                   | Get a description of this         |
        |                                   | attribute.                        |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `au.gov.asd.tac.cons              | `elementType()`                   |
        | tellation.graph.GraphElementType` | ::: {.block}                      |
        |                                   | Get the element type of this      |
        |                                   | attribute.                        |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `int`                             | `id()`                            |
        |                                   | ::: {.block}                      |
        |                                   | Get the id of this attribute.     |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `java.lang.String`                | `mergerId()`                      |
        |                                   | ::: {.block}                      |
        |                                   | Get the id of the merger for this |
        |                                   | attribute.                        |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+
        | `java.lang.String`                | `name()`                          |
        |                                   | ::: {.block}                      |
        |                                   | Get the name of this attribute.   |
        |                                   | :::                               |
        +-----------------------------------+-----------------------------------+

        : [All Methods[ ]{.tabEnd}]{#t0 .activeTableTab}[[Instance
        Methods](javascript:show(2);)[ ]{.tabEnd}]{#t2
        .tableTab}[[Concrete
        Methods](javascript:show(8);)[ ]{.tabEnd}]{#t4 .tableTab}

        -   []{#methods.inherited.from.class.java.lang.Object}

            ### Methods inherited from class java.lang.Object

            `clone, equals, finalize, getClass, hashCode, notify, notifyAll, toString, wait, wait, wait`
:::

::: {.details}
-   -   []{#constructor.detail}

        ### Constructor Detail

        []{#SAttribute-au.gov.asd.tac.constellation.graph.GraphReadMethods-int-au.gov.asd.tac.constellation.graph.GraphElementType-java.lang.String-java.lang.String-java.lang.String-java.lang.Object-java.lang.String-}

        -   #### SAttribute

                public SAttribute(au.gov.asd.tac.constellation.graph.GraphReadMethods readableGraph,
                                  int id,
                                  au.gov.asd.tac.constellation.graph.GraphElementType elementType,
                                  java.lang.String attributeType,
                                  java.lang.String name,
                                  java.lang.String description,
                                  java.lang.Object defaultValue,
                                  java.lang.String mergerId)

        []{#SAttribute-au.gov.asd.tac.constellation.graph.GraphReadMethods-int-}

        -   #### SAttribute

                public SAttribute(au.gov.asd.tac.constellation.graph.GraphReadMethods readableGraph,
                                  int id)

        []{#SAttribute-au.gov.asd.tac.constellation.graph.GraphReadMethods-au.gov.asd.tac.constellation.graph.GraphElementType-java.lang.String-}

        -   #### SAttribute

                public SAttribute(au.gov.asd.tac.constellation.graph.GraphReadMethods readableGraph,
                                  au.gov.asd.tac.constellation.graph.GraphElementType elementType,
                                  java.lang.String name)

    ```{=html}
    <!-- -->
    ```
    -   []{#method.detail}

        ### Method Detail

        []{#id--}

        -   #### id

                public int id()

            ::: {.block}
            Get the id of this attribute.
            :::

            [Returns:]{.returnLabel}
            :   the id of this attribute.

        []{#elementType--}

        -   #### elementType

                public au.gov.asd.tac.constellation.graph.GraphElementType elementType()

            ::: {.block}
            Get the element type of this attribute.
            :::

            [Returns:]{.returnLabel}
            :   the element type of this attribute.

        []{#attributeType--}

        -   #### attributeType

                public java.lang.String attributeType()

            ::: {.block}
            Get the type of this attribute.
            :::

            [Returns:]{.returnLabel}
            :   the type of this attribute.

        []{#name--}

        -   #### name

                public java.lang.String name()

            ::: {.block}
            Get the name of this attribute.
            :::

            [Returns:]{.returnLabel}
            :   the name of this attribute.

        []{#description--}

        -   #### description

                public java.lang.String description()

            ::: {.block}
            Get a description of this attribute.
            :::

            [Returns:]{.returnLabel}
            :   a description of this attribute.

        []{#defaultValue--}

        -   #### defaultValue

                public java.lang.Object defaultValue()

            ::: {.block}
            Get the default value of this attribute.
            :::

            [Returns:]{.returnLabel}
            :   the default value of this attribute.

        []{#mergerId--}

        -   #### mergerId

                public java.lang.String mergerId()

            ::: {.block}
            Get the id of the merger for this attribute.
            :::

            [Returns:]{.returnLabel}
            :   the merger id of this attribute.
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
-   Prev Class
-   [[Next Class]{.typeNameLink}](../../../../../../../au/gov/asd/tac/constellation/scripting/graph/SCollection.html "class in au.gov.asd.tac.constellation.views.scripting.graph")

```{=html}
<!-- -->
```
-   [Frames](../../../../../../../index.html?au/gov/asd/tac/constellation/scripting/graph/SAttribute.html)
-   [No Frames](SAttribute.html)

```{=html}
<!-- -->
```
-   [All Classes](../../../../../../../allclasses-noframe.html)

<div>

</div>

<div>

-   Summary: 
-   Nested \| 
-   Field \| 
-   [Constr](#constructor.summary) \| 
-   [Method](#method.summary)

```{=html}
<!-- -->
```
-   Detail: 
-   Field \| 
-   [Constr](#constructor.detail) \| 
-   [Method](#method.detail)

</div>

[]{#skip.navbar.bottom}
:::
