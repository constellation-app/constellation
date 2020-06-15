Split Nodes Based on Identifier
-------------------------------

This plugin will split nodes based on whether a node Identifier contains a supplied character(s). If a selected node contains the character(s) in its identifier, two new nodes will be created (exception: Split on All Occurrences option is checked).

Parameters
``````````

*Split Character(s):* Character(s) that are used to split the identifier. By default, the original node is identified by everything before the character(s), and the extracted node is identified by everything after the character(s). The character(s) itself is not included in either node.

*Transaction Type:* Specify a transaction type that will represent the relationship between the original and extracted nodes.

*Split on All Occurrences:* If this option is checked, the selected node(s) will split on all occurrences of the split character(s) in the identifier. An additional node will be created for each additional split made (e.g. splitting an identifier in two places will create 3 nodes (being the original and 2 extracted)).


.. help-id: au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.SplitNodesPlugin
