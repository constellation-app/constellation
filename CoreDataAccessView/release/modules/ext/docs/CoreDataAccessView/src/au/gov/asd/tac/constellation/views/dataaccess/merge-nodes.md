# Merge Nodes

## Merge nodes in your graph together

Merge nodes in your graph together based on the defined criteria.

## Parameters

-   *Merge By* - The rule to determine when nodes should be merged
    together. Options include:
    -   Geospatial Distance - Will merge nodes within a particular
        geospatial distance of each other. The distance to compare is
        defined by the threshold
    -   Identifier Prefix Length - Will merge nodes matching the start
        of the Identifier attribute. The length to compare is defined by
        the threshold
    -   Identifier Suffix Length - Will merge nodes matching the end of
        the Identifier attribute. The length to compare is defined by
        the threshold
    -   Supported Type - Will merge nodes based on the type by merging
        supported types overriding those not supported.
-   *Threshold* - The limit to apply to the selected rule (not
    applicable for Supported Type rule)
-   *Merging Rule* - The rule deciding how attributes are merged
-   *Lead Node* - The rule deciding how to choose the lead node (not
    applicable for Geospatial Distance or Supported Type rules). Options
    include:
    -   Longest Value - Use the node with the longer Identifier value
        length
    -   Shortest Value - Use the node with the shorter Identifier value
        length
    -   Ask Me - A dialog is displayed asking you to select the lead
        node by selecting its Identifier
-   *Selected Only* - Only apply to selected nodes
