# Hierarchy

<table class="table table-striped">
<thead>
<tr class="header">
<th>Constellation Action</th>
<th>Keyboard Shortcut</th>
<th>User Action</th>
<th style="text-align: center;">Menu Icon</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Run Hierarchy Arrangement</td>
<td>Ctrl + H</td>
<td>Arrange -&gt; Hierarchy</td>
<td style="text-align: center;"><img src="../constellation/CoreArrangementPlugins/src/au/gov/asd/tac/constellation/plugins/arrangements/docs/resources/arrangeInHierarchy.png" alt="Hierarchy Arrangement Icon" /></td>
</tr>
</tbody>
</table>

The hierarchy arrangement arranges each component into a hierarchical
structure with specified roots at the top and each subsequent level
containing nodes that are the same distance away from the roots (i.e. if
the root nodes are at Level 0, then Level n contains nodes that are n
hops away from the root). Singleton nodes are arranged together in a
grid, similarly for doublets (pairs of nodes only connected to each
other).

The roots are specified via Named Selections (refer
[here](../constellation/CoreNamedSelectionView/src/au/gov/asd/tac/constellation/views/namedselection/docs/named-selections-view.md)
for creating a named selection). When you run the arrangement, choose
the named selection which will form the roots (the arrangement can't be
run if no named selection exists). Only one component is required to
have its root(s) specified in the named selection in order to run the
hierarchy arrangement. Any component without a specified root will have
an arbitrary node chosen to be the root.

Example Hierarchy Arrangement:

<div style="text-align: center">

<img src="../constellation/CoreArrangementPlugins/src/au/gov/asd/tac/constellation/plugins/arrangements/docs/resources/HierarchyArrangement.png" alt="Example Hierarchy
Arrangement" />

</div>
