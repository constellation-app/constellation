Merge Nodes
-----------

Merge nodes in your graph together
``````````````````````````````````

Merge nodes in your graph together based on the defined criteria.

Parameters
``````````

An explanation of the options are:

*Merge By*: Nodes will be merged based on the following option:

* Identifier Prefix Length - Will merge nodes matching the start of the Identifier attribute. The length to compare is defined by the threshold below
* Identifier Suffix Length - Will merge nodes matching the end of the Identifier attribute. The length to compare is defined by the threshold below
* Supported Type - Will merge nodes based on the type by merging supported types overriding those not supported.

*Threshold*: Threshold (length)

*Merging Rule*: The rule deciding how attributes are merged is based on the following option:

* Retain lead node attributes if present
* Retain lead node attributes always
* Copy merged node attributes if present
* Copy merged node attributes always

*Lead Node*: The rule deciding how to choose the lead node based on the following option:

* Longest Value - Based on the node's Identifier value length, use the node with the longer value
* Shortest Value - Based on the node's Identifier value length, use the node with the shorter value
* Ask Me - A dialog is displayed asking you to select the lead node by selecting its Identifier

*Selected Only*: Merge Only Selected Nodes


.. help-id: au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeNodesPlugin
