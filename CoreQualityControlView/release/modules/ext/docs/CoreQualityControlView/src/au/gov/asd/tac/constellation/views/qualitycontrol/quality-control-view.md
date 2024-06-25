# Quality Control View

<table class="table table-striped">
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
<td>Open Quality Control View</td>
<td>Ctrl + Shift + Q</td>
<td>Views -&gt; Quality Control View</td>
<td style="text-align: center;"><img src="../ext/docs/CoreQualityControlView/src/au/gov/asd/tac/constellation/views/qualitycontrol/resources/quality-control-view.png" width="16" height="16" alt="Quality Control View Icon" /></td>
</tr>
<tr class="even">
<td>Select rows</td>
<td></td>
<td>Left click row</td>
<td style="text-align: center;"></td>
</tr>
<tr class="odd">
<td>Select multiple rows</td>
<td></td>
<td>Hold shift and left click rows</td>
<td style="text-align: center;"></td>
</tr>
<tr class="even">
<td>Toggle row selection</td>
<td></td>
<td>Hold ctrl and left click rows</td>
<td style="text-align: center;"></td>
</tr>
<tr class="odd">
<td>Show all registered rules and scores</td>
<td></td>
<td>Double click a row</td>
<td style="text-align: center;"></td>
</tr>
</tbody>
</table>

## IMPORTANT!!!

Although Constellation provides this Quality Control View, the user
remains responsible for ensuring that their data is acceptable for
further analysis.

## Introduction

The Quality Control View is a quick and easy way to look at nodes on the
graph and determine whether or not they have any obvious quality issues
based on a series of rules built into Constellation. Selecting nodes in
the graph will cause them to be processed against any registered quality
control rules, and ranked in the Quality Control View according to their
quality. This quality rating will be a given category (see below) along
with reasoning as to why they may pose a quality control issue. Users
can then highlight rows in the quality control view and apply options
via the buttons at the bottom of the view.

<div style="text-align: center">

<img src="../ext/docs/CoreQualityControlView/src/au/gov/asd/tac/constellation/views/qualitycontrol/resources/QualityControlView.png" alt="Quality Control
View" />

</div>
  
&nbsp;   
## Quality Control Categories

There are six levels of quality control categories for nodes that are
selected.

-   *CRITICAL* - Any node which is specifically disallowed (whether it
    be disallowed for queries, or simply should not be in your graph)
    will have a dark red background, and may be blocked from further
    analysis.
-   *SEVERE* - Any node which is considered of particularly bad quality
    will have a red background, but will never be blocked.
-   *MAJOR* - Any node which is considered of very questionable quality
    will have an orange background.
-   *MEDIUM* - Any node which is considered of questionable quality will
    have a yellow background.
-   *MINOR* - Any node which is considered of slightly questionable
    quality will have a blue background.
-   *OK* - Any node which is considered of good quality will have a
    green background. This does not mean that these nodes are
    necessarily of high quality, only that no quality control rules
    matched - such nodes require manual checking by the user.

## Options

These options are accessible from the bottom of the Quality Control View

-   *Delete From Graph* - Deletes the nodes highlighted in the Quality
    Control View from the graph.
-   *Select On Graph* - Selects the nodes highlighted in the Quality
    Control View and deselects everything else in the graph.
-   *Deselect On Graph* - Deselect the nodes highlighted in the Quality
    Control View from the selection in the graph.
-   *Zoom On Graph* - Resets the camera on the graph to make every
    selected node visible.
-   *Category Priority* - Allows you to view and change the quality
    category flagged for each of the registered rules.

#### Category Priority

Clicking the Category Priority button opens a small dialog window where you can assign any priority level to each rule.  

<div style="text-align: center">

<img src="../ext/docs/CoreQualityControlView/src/au/gov/asd/tac/constellation/views/qualitycontrol/resources/CategoryPriority.png" alt="Quality Control
View" />

</div>
&nbsp;   
Each rule can be set and reset individually.  
If any rules were already modified when opening the Category Priority screen, those rules will have a more informative reset button, indicating the default priority level for each modified rule.  

<div style="text-align: center">

<img src="../ext/docs/CoreQualityControlView/src/au/gov/asd/tac/constellation/views/qualitycontrol/resources/ModifiedCategoryPriority.png" alt="Quality Control
View" />

</div>
&nbsp;   
*Note: A node is considered to have an OK Quality by default. Only when a rule fails will the Quality will be modified, matching the priority value assigned to the rule that failed.*  
&nbsp; &nbsp; &nbsp; *If multiple rules fail, then the Quality for the node will be set to the highest priority value from among the rules that failed.*
