Attribute Calculator
--------------------

How to open Attribute Calculator

.. csv-table::
   :header: "*CONSTELLATION Action*","*User Action*","*Menu Icon*"

   "Open Attribute Calculator","Views -> Attribute Calculator","|---resources-attribute-calculator.png|"

The attribute calculator is a tool which allows users to set the values of node and transaction attributes based on complex calculations involving other attributes and graph properties. These calculations are referred to as 'scripts'. The attribute calculator includes a number of inbuilt scripts, as well as features to assist in and simplify the process of creating new scripts.

|resources-AttributeCalculatorMain.png|

The interface to the attribute calculator is depicted above. It is organised into three main columns; the controls column, the script column, and the docs column. The sizes of the columns can be adjusted by dragging the grey dividers that separate them. There is also the execution controls at the bottom right which are used to run a script.

Controls Column
```````````````

|resources-AttributeCalculatorControlsColumn.png|

The controls column is used to set up and build an attribute calculator script. The first three drop down boxes allow for us to specify the details of the attribute we will be setting. The final drop-down and list allows access to a number of templates which assist in building scripts.

A detailed description of each control is as follows:

* *Graph Element* - Here we choose between setting the values of node and transaction attributes.
* *Attribute to Set* - Here we choose the name of the attribute to set. The list is pre-populated with the attributes that exist in the current graph, but we can enter a new name here to create a new attribute.
* *Attribute Type* - Here we choose the type of the attribute we are setting. This only applies if we are creating a new attribute - if we picked an existing attribute from the above drop-down, then this drop-down will be disabled.
* *Insert in Script* - This utility allows us to insert attributes, graph properties, and other functions into our script. The drop-down can be used to select a broad category, which will then populate the list below it with items. Single clicking an item will display information about it in the docs column. Double clicking an item inserts it at the current cursor location in the script.
* *Save Button* - Saves the current script along with the details of the attribute that is being set. A dialogue will be displayed asking for a name and description of the script.
* *Load Button* - Loads the currently selected script. This button is disabled when the 'complete scripts' category is not selected from the 'insert in script' drop down.
* *Del Button* - Deletes the currently selected script. This button is disabled when the 'complete scripts' category is not selected from the 'insert in script' drop down.
* *Help Button* - From here we can open this help, or the attribute calculator tutorial.
* *Docs Button* - Toggles the visibility of the Docs column.

Complete Scripts
````````````````

The insert in script utility also has a special category 'complete scripts'. These are entire scripts to perform predetermined calculations for specific attributes. Double clicking them will not place anything in the current script. Instead, the load button must be pressed, which will completely overwrite the current script as well as the attribute that is being set.

Script Column
`````````````

|resources-AttributeCalculatorScriptColumn.png|

The Script column contains the actual script that we want to use to set the values of an attribute. Text can be typed directly into this column, or inserted by using the control's column 'insert in script' utility.

Docs Column
```````````

|resources-AttributeCalculatorDocsColumn.png|

The Docs column provides information about the attribute that is being set, as well as information about the currently selected category and item in the controls column's 'insert in script' utility.

Execution Controls
``````````````````

|resources-AttributeCalculatorExecutionBar.png|

The Execution controls are used to configure and run the script.

* *Complete With Schema* - If this is selected, everything in the graph will be validated against the graph's schema after the script has finished running and the desired attribute has been set.
* *Selected Elements Only* - If this is selected, the script will only set the value of the desired attribute for currently selected elements.
* *Execute* - Runs the script.

.. |---resources-attribute-calculator.png| image:: ---resources-attribute-calculator.png
   :width: 16px
   :height: 16px

.. |resources-AttributeCalculatorMain.png| image:: resources-AttributeCalculatorMain.png

.. |resources-AttributeCalculatorControlsColumn.png| image:: resources-AttributeCalculatorControlsColumn.png

.. |resources-AttributeCalculatorScriptColumn.png| image:: resources-AttributeCalculatorScriptColumn.png

.. |resources-AttributeCalculatorDocsColumn.png| image:: resources-AttributeCalculatorDocsColumn.png

.. |resources-AttributeCalculatorExecutionBar.png| image:: resources-AttributeCalculatorExecutionBar.png


.. help-id: au.gov.asd.tac.constellation.views.attributecalculator
