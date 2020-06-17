Resource Allocation Index
-------------------------

Similarity measures are used to determine how many features two entities have in common.

The Resource Allocation Index analytic calculates the fraction of a "resource" that a node can send to another through their common neighbours. It is calculated by summing 1 divided by the degree of each common neighbour node. The Soundarajan-Hopcroft Community Resource Allocation parameter will only score pairs of nodes that are both selected (that is, sharing resources within the same community).


.. help-id: au.gov.asd.tac.constellation.views.analyticview.analytics.ResourceAllocationIndexAnalytic
