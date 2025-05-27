# Attribute Editor

<table class="table table-striped">
<colgroup>
<col style="width: 25%" />
<col style="width: 25%" />
<col style="width: 25%" />
<col style="width: 25%" />
</colgroup>
<thead>
<tr class="header">
<th>Constellation Action</th>
<th>Keyboard Shortcut</th>
<th>User Action</th>
<th style="text-align: center;">Menu Icon</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Open Attribute Editor</td>
<td>Ctrl + Shift + E</td>
<td>Views -&gt; Attribute Editor</td>
<td style="text-align: center;"><img src="../ext/docs/CoreAttributeEditorView/resources/attribute_editor.png" alt="Attribute Editor Icon" /></td>
</tr>
</tbody>
</table>

## Introduction

The Attribute Editor is the view where you can view, add, and edit
Graph, Node, and Transaction attributes on your graph. When you have a
graph element (node or transaction) selected, you are able to view all
the current values assigned to that particular graph element. If no
value is assigned to an attribute for a given graph element, the text
"&lt;No Value&gt;" will be displayed. If more than one value is assigned to a
set of selected graph elements, the text "&lt;Multiple Values&gt;" will be
displayed and the attribute will become a drop-down list which when
expanded displays all the values assigned to that attribute (this won't
occur if only one graph element selected).

<div style="text-align: center">
<img src="../ext/docs/CoreAttributeEditorView/resources/AttributeEditor.png" alt="Attribute
Editor" />
</div>

There are four different kinds of attributes you can see in the
Attribute Editor:

-   *Schema Attributes* - These are regular old attributes which
    Constellation has defined in a schema. They have a black background
    by default.
-   *Primary Key Attributes* - These are important attributes that
    define a graph element. The set of primary key attribute values are
    unique to each graph element. They have a red background by default.
-   *Custom Attributes* - These are attributes which are not defined in
    a Constellation schema. They have a blue background by default.
-   *Hidden Attributes* - These are attributes which have been hidden.
    You can hide an attribute by right-clicking on it and selecting the
    "Hide Attribute" <img src="../ext/docs/CoreAttributeEditorView/resources/ShowHidden.png" alt="Show Hidden" /> 
    toggle button. They have a grey background by default. These can
    be shown again by clicking the "Show Hidden" button.

By default all attributes are shown in the Attribute Editor. Sometimes these 
attributes can be empty, meaning no value has been set for that attribute. By 
deselecting the "Show Empty" <img src="../ext/docs/CoreAttributeEditorView/resources/ShowEmpty.png" alt="Show Empty" />
toggle button, empty attribute fields will be removed from the view. 
These can be added back to the view by reselecting the "Show Empty" toggle button.

## Editing An Attribute

To edit an attribute, select the nodes and transactions you want to edit
and then click on the attribute value. This will bring up a dialog box to change the value. Once you
have finished changing the value, press "OK" to apply the change. If
more than one graph element is selected than the new value will be
applied to all the relevant selected graph elements.

Attributes that are deactivated will not be editable and will express a text field with a grey tint. 
<img src="../ext/docs/CoreAttributeEditorView/resources/AttributeEditorDeactivatedIndicator.png" alt="Deactivated Attribute" />

NOTE: If you want to change the time zone of an attribute, right click
on the attribute say "DateTime" and select "Update time-zone of
selection" and set the time zone. This will update the time zone for for
the selected nodes/transactions only.

## Adding An Attribute

To add an attribute to your graph, click on the <img src="../ext/docs/CoreAttributeEditorView/resources/AttributeEditorAdd.png" alt="Add
Icon" />
button of the relevant graph element type and choose the attribute you
want to add. If you select "Custom", you will be required to fill in all
the details of the new attribute.

## Editing Primary Key

From the Attribute Editor, you can also edit the primary key for a graph
element type (recall these are the attributes which uniquely define a
graph element). To do so, click on the <img src="../ext/docs/CoreAttributeEditorView/resources/AttributeEditorKey.png" alt="Primary Key
Icon" />
button of the relevant graph element type and select which elements you
want to be a part of the primary key. You will only be able to choose
from attributes that are already on the graph (refer to adding an
attribute if the attribute you want to add to the primary key isn't
currently on the graph).

## Autocomplete with Schema

A change made in the Attribute Editor won't be applied to the graph
until a Complete with Schema (F5) is run. By default, you will have to
perform this step manually but there is an option to have this done
automatically for you after you edit an attribute. To enable this, Click
on the "Options" menu at the top of the view and select "Complete with
Schema After Edits". With this enabled, it will run Complete with Schema
after every attribute value you edit via the Attribute Editor. Select
that same option to disable it and return to default behaviour.
