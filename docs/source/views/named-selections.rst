Named Selections
````````````````

Named Selections are collections of graph elements that have been saved for later use. Named Selections can be used analytically through their use in union (binary OR) and intersection (binary AND) operations.

The following image shows the basic components of the Named Selection Window

Creating and Using Named Selections
```````````````````````````````````

There are two main ways to create and use Named Selections: Through the Named Selection Window, and through the use of shortcut keys.

To create a Named Selection with a shortcut key, hold Control-Shift and press one of the numbers from 1-9. To recall a Named Selection with a shortcut key, Control- and press the corresponding number from 1-9 that was used when creating the Named Selection. The combination to recall a Named Selection is shown next to the selection in the Named Selection Window as per the image above.

Performing Unions (OR)
``````````````````````

To perform a union (binary OR) operation, select two or more selections from the Named Selections Window, and press the 'Union' button (as per the image above). This will result in all elements from each Named Selection (and the graph if *Current Selection* has been selected) being selected on the graph.

Performing Intersections (AND)
``````````````````````````````

To perform an intersection (binary AND) operation, select two or more selections from the Named Selections Window, and press the 'Intersection' button (as per the image above). This will result in all common elements from each Named Selection (and the graph if *Current Selection* has been selected) being selected on the graph, and all other elements being unselected.

Advanced Use of Named Selections
````````````````````````````````

Protecting Selections
:::::::::::::::::::::

To ensure a selection is not inadvertently deleted, it can be protected. This can be achieved by right clicking the relevant Named Selection, and selecting the 'Protect Selection' menu item. A small padlock appears next to protected Named Selections to indicate this mode. (See the image above).

Cloning Selections
::::::::::::::::::

To create a duplicate of a Named Selection, right click it and select 'Clone Selection' from the menu. This will create a new named selection that contains all of the elements that the original Named Selection contained.

Renaming Selections
:::::::::::::::::::

Named Selections can be given meaningful names by highlighting the selection in the Named Selection Window and pressing 'F2', or right-clicking and selecting 'Rename Selection'. A prompt is shown where a new name can be entered.

Setting Selection Descriptions
::::::::::::::::::::::::::::::

Descriptions for Named Selections can be set by right-clicking a Named Selection, and selecting 'Set Selection Description' from the menu. A prompt is shown where a description can be entered. If a Named Selection has had a description appended, it will be placed in a tooltip if the mouse is hovered over the Named Selection for a short period of time.

.. |resources-namedselections.png| image:: resources-namedselections.png

.. |resources-namedselection-context.png| image:: resources-namedselection-context.png


.. help-id: au.gov.asd.tac.constellation.functionality.selection.named
