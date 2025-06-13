# Hop Out

<table class="table table-striped">
<colgroup>
<col style="width: 25%" />
<col style="width: 25%" />
<col style="width: 25%" />
<col style="width: 25%" />
</colgroup>
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
<td>Run Hop Out Half</td>
<td>Ctrl + Shift + Right</td>
<td>Selection -&gt; Hop Out Half</td>
<td style="text-align: center;"><img src="../ext/docs/CoreVisualGraph/resources/hop_half.png" alt="Hop Out Half Icon" /></td>
</tr>
<tr class="even">
<td>Run Hop Out One</td>
<td>Ctrl + Right</td>
<td>Selection -&gt; Hop Out One</td>
<td style="text-align: center;"><img src="../ext/docs/CoreVisualGraph/resources/hop_one.png" alt="Hop Out One Icon" /></td>
</tr>
<tr class="odd">
<td>Run Hop Out Full</td>
<td></td>
<td>Selection -&gt; Hop Out Full</td>
<td style="text-align: center;"><img src="../ext/docs/CoreVisualGraph/resources/hop_full.png" alt="Hop Out Full Icon" /></td>
</tr>
</tbody>
</table>

## Hop Out Half

Hop Out Half will add to the current selection all graph elements that
can be reached within a half hop of the current selection. More
specifically, it adds the adjacent transactions for each selected
transaction and the adjacent nodes for each selected transaction.

## Hop Out One

Hop Out One will add to the current selection all graph elements that
can be reached within one hop of the current selection. More
specifically, it adds the nearest nodes from each selected node and the
nearest transactions from each selected transaction.

## Hop Out Full

Hop Out Full will add to the current selection all graph elements that
can be reached in any number of hops from the current selection. More
specifically, it selects the entire node and transaction sub-network of
the current selection.

Before Any Hop Out is run:

<div style="text-align: center">

<img src="../ext/docs/CoreVisualGraph/resources/HopOutBefore.png" alt="Graph before Hop Out is
run" />

</div>

After Hop Out Half is run:

<div style="text-align: center">

<img src="../ext/docs/CoreVisualGraph/resources/HopOutHalfAfter.png" alt="Graph after Hop Out Half is
run" />

</div>

After Hop Out One is run:

<div style="text-align: center">

<img src="../ext/docs/CoreVisualGraph/resources/HopOutOneAfter.png" alt="Graph after Hop Out One is
run" />

</div>

After Hop Out Full is run:

<div style="text-align: center">

<img src="../ext/docs/CoreVisualGraph/resources/HopOutFullAfter.png" alt="Graph after Hop Out Full is
run" />

</div>
