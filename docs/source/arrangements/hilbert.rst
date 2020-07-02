Arrange Clusters on Hilbert Curve
---------------------------------

When a clustering algorithm runs, it sets the value of the ``cluster`` node integer attribute on each node so that nodes in a cluster have the same cluster value, and different clusters have different values.

The Hilbert curve uses the ``cluster`` value to arrange nodes. It first sorts the clusters by size (ie number of nodes in each cluster, largest cluster first, which means there is no analytic inter-cluster relevance to the order of the clusters). It then arranges the nodes along a line, and folds the line into a Hilbert curve.

A Hilbert curve has the property that points on the line that are close to each other will be geographically close to each other on the curve. Therefore, the nodes in each cluster will be close to each other and separate from nodes in other clusters.

Additionally, the x2, y2, z2 values for each node are set, so ``PageUp``/``PageDown`` will "explode" the clusters to make it easier to see the cluster separation.

Because each step of this arrangement is fast, it is suitable for running on large graphs.


.. help-id: au.gov.asd.tac.constellation.plugins.arrangements.clustersOnHilbertCurve
