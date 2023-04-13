# Chinese Whispers Clustering

**Chinese Whispers - an Efficient Graph Clustering Algorithm and its
Application to Natural Language Processing Problems.**

  
Chris Biemann  
University of Liepzig, NLP Department

Intuitively, the algorithm works as follows in a bottom-up fashion:
First all nodes get different classes. Then the nodes are processed for
a small number of iterations and inherit the strongest class in the
local neighborhood. This is the class whose sum of edge weights to the
current node is maximal. In case of multiple strongest classes, one is
chosen randomly. Regions of the same class stabilize during the
iteration and grow until they reach the border of a stable region of
another class. Note that classes are updated immediately: a node can
obtain classes from the neighborhood that were introduced there in the
same iteration.

                initialize:
                for all vi in V:
                  class(vi) = i
                while changes:
                  for all v in V, randomized order:
                    class(v) = highest ranked class in neighborhood of v;
            

Apart from ties, the classes usually do not change any more after a
handful of iterations. The number of iterations depends on the diameter
of the graph: the larger the distance between two nodes is, the more
iterations it takes to percolate information from one to another.

## Other features

Chinese Whispers in CONSTELLATION makes use of overlay colors. When the
clustering algorithm has been run, each cluster is assigned a unique
color. Node backgrounds and intra-cluster transactions are colored using
the cluster's color, while inter-cluster transactions are colored dark
grey.

Rather than set the color directly, Chinese Whispers in CONSTELLATION
creates a new color attribute and tells Constellation to display the
graph using the new attribute. To switch back to the default color
attribute, use Edit → Color attribute selection.

When the clustering algorithm runs, no arrangement is done, so the graph
can look confusing.

The clustering algorithms add an integer attribute "cluster" to the
vertices. This attribute has a unique per-cluster value to indicate
which cluster a vertex belongs to, which other algorithms can later use.
