# Select Top N

## Select the top N nodes based on a type

Select the top N nodes based on a node/transaction type and the count of
transactions between the nodes.

This utility plugin automates an analytic that could also be achieved
via the histogram in a few steps.

NOTE: If multiple nodes are selected then a top N will be calculated for
each node.

## Parameters

-   *Mode* - The graph element (node, transaction) whose types you want
    to filter on
-   *Type Category* - The category of the type which needs to be
    selected first
-   *Specific Types* - The specific types to be included when
    calculating the top N
-   *Limit* - The limit (i.e. the value of N). Default is top 10
