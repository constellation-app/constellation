# Split Nodes Based on Identifier

## Split nodes based on supplied character(s) in the identifier

<br />
<img src="../ext/docs/CoreDataAccessView/src/au/gov/asd/tac/constellation/views/dataaccess/resources/SplitNodes.png" alt="Split Nodes" />
<br />

This plugin will split nodes based on whether a node Identifier contains
a supplied character(s).

If a selected node contains the character(s) and Split on All
Occurrences is not ticked, two new nodes will be created based on the
first occurrence of the split

If a selected node contains the character(s) and Split on All
Occurrences is ticked, an additional node will be created for each
additional split made(e.g. splitting an identifier in two places will
create 3 nodes (being the original and 2 extracted))

## Parameters

-   *Split Character(s)* - Character(s) that are used to split the
    identifier. The character(s) itself is not included in any of the
    resulting nodes.
-   *Duplicate Transactions* - Make copies of transactions attached to
    the original node for each of the extracted nodes (exploding the
    node). Selecting this will result in no transaction being created
    between the original and extracted nodes
-   *Transaction Type* - Specify a transaction type that will represent
    the relationship between the original and extracted nodes (not
    applicable if Duplicate Transactions is ticked)
-   *Split on All Occurrences* - Split on all occurrences of the split
    character(s) in the identifier (rather than just the first
    occurrence)
-   *Complete with Schema* - Run Complete with Schema following the
    completion of the plugin
