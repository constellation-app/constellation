Quality Control View
--------------------

*Although CONSTELLATION provides this Quality Control View, the user remains responsible for ensuring that their data is acceptable for further analysis.*

How to open Quality Control View

.. csv-table::
   :header: "*CONSTELLATION Action*","*User Action*","*Menu Icon*"

   "Open Quality Control View","Views -> Quality Control View","|---resources-quality-control-view.png|"
   "Select rows","Left click row",""
   "Select multiple rows","Hold shift and left click rows",""
   "Toggle row selection","Hold ctrl and left click rows",""
   "Show all registered rules and scores","Double click a row",""

The Quality Control View is a quick and easy way to look at nodes on the graph and determine whether or not they have any obvious quality issues based on a series of rules built into CONSTELLATION. Selecting nodes in the graph will cause them to be processed against any registered quality control rules, and ranked in the Quality Control View according to their quality. This quality rating will be a score out of 100 (with higher scores indicating lesser quality) along with reasoning as to why they may pose a quality control issue. Users can then highlight rows in the quality control view and apply options via the buttons at the bottom of the view.

There are four levels of quality control for nodes that are selected.

* Any node which is specifically disallowed (whether it be disallowed for queries, or simply should not be in your graph) will have a black background, and may be blocked from further analysis.
* Any node which is considered of particularly bad quality will have a red background, but will never be blocked.
* Any node which is considered of questionable quality will have a blue background.
* Any node which is considered of good quality will have a white background. This does not mean that these nodes are necessarily of high quality, only that no quality control rules matched - such nodes require manual checking by the user.

Double-clicking a row in the Quality Control View will open a dialog box that shows the scores given to the node in that row by the rules.

* Zoom to Selection: Resets the camera on the graph to make every selected node visible.
* Select on graph: Selects the nodes highlighted in the Quality Control View and deselects everything else in the graph.
* Remove From Selection: Deselect the nodes highlighted in the Quality Control View from the selection in the graph.
* Delete From Graph: Deletes the nodes highlighted in the Quality Control View from the graph.

.. |---resources-quality-control-view.png| image:: ---resources-quality-control-view.png
   :width: 16px
   :height: 16px
   :alt: Quality Control View Icon


.. help-id: au.gov.asd.tac.constellation.views.qualitycontrol.QualityControlViewTopComponent
