Top N Selection
---------------

Select the top N nodes based on a type
``````````````````````````````````````

Select the top N nodes based on a node/transaction type and the count of transactions between the nodes.

This utility plugin automates an analytic that could also be achieved via the histogram in a few steps.

*Note:* that if multiple nodes are selected then a top N will be calculated for each node.

Parameters
``````````

An explanation of the options are:

*Mode*: The filtering mode being either looking at *Node* or *Transaction* types

*Type Category*: The category of the type which needs to be selected first

*Specific Types*: The specific types to be included when calculating the top N

*Limit*: The limit, default being top 10


.. help-id: au.gov.asd.tac.constellation.views.dataaccess.plugins.utility.SelectTopNPlugin
