# Remove Nodes

## Remove nodes from your graph


<img src="../ext/docs/CoreDataAccessView/src/au/gov/asd/tac/constellation/views/dataaccess/resources/RemoveNodes.png" alt="Remove Nodes" />


Remove nodes from the graph based on defined criteria. Only applies to nodes that are currently selected

## Parameters

-   *Remove By* - The rule used to determine if a node should be removed
-   *Threshold* - The limit to apply to the selected rule. A node is removed if the the chosen rule's value is less than or equal to the threshold.

NOTE: Identifier length is the only criteria currently supported.

###Example
"Remove By" is set to Identifier Length, and "Threshold" is set to 10. When run, all selected nodes with an identifier length of 10 or less will be removed.
