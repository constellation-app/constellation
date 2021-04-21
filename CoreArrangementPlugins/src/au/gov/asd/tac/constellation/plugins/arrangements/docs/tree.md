## Arrange In Tree

Many graphs consists of a set of hubs, where each hub is comprised of a
set inter-connected nodes.  
  
The arrange by trees algorithm does the following:  
Every node with one neighbor is removed while noting the node it is
connected to.  
This may result in several new nodes that have only one neighbor.
Therefore,these nodes are removed in a second pass, and the removed
neighbors are passed on to the adjacent neighbors. This process
continues until there are no more nodes with a single neighbor.  
Each of the remaining "backbone" nodes represent a group of nodes that
have a subsidiary to it which was removed.  
Next, the groups are arranged around each of the backbone nodes, by
positioning the backbone nodes using the Arrange By Proximity function.
The By Proximity function will leave room to accommodate the extents of
the groups around each backbone node.  
Once the backbones have been arranged, the subsidiary nodes are placed
circularly about the backbone nodes.
