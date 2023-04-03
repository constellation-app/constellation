# Table View

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
<th>Menu Icon</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Open Table View</td>
<td>Ctrl + Shift + Y</td>
<td>Views -&gt; Table View</td>
<td><div style="text-align: center">
<img src="../ext/docs/CoreTableView/src/au/gov/asd/tac/constellation/views/tableview/resources/table-view.png" width="16" height="16" />
</div></td>
</tr>
</tbody>
</table>

## Introduction

The Table View presents attribute data from the graph in a tabular
format. Highlighted rows in the table represent selected elements in the
graph. Selection of the table rows (including the use of the shift and
control buttons) will result in the selection of the table rows and the
associated elements in the graph itself. Conversely, changes to the
graph selection are reflected in the table.

<img src="../ext/docs/CoreTableView/src/au/gov/asd/tac/constellation/views/tableview/resources/TableView.png" alt="Table
View" />

## Column Sorting

Left-clicking any column header will sort the table by the values in
that column. A second click will reverse the sort order and a third
click will remove the sort. If you hold shift while you click on column
headers you can sort by multiple columns. This sorting will occur in the
order that you click the columns (as indicated by the dots and numbers).

## Column Filtering

Right-clicking any column header will open a filter dialog allowing you
to select / deselect values manually, or type something to apply a
filter to the data.

## Context Menu

Right-clicking anywhere on the table will open a context menu providing
options to copy data from the clicked cell, row or column.

## Menu Items

-   *Column Visibility* <img src="../ext/docs/CoreTableView/src/au/gov/asd/tac/constellation/views/tableview/resources/TableColumnVisibility.png" alt="Column Visibility
    Button" />
    - Clicking the column visibility toolbar button will open a menu
    allowing you to customise which attributes are displayed in the
    table.
-   *Selected Only* <img src="../ext/docs/CoreTableView/src/au/gov/asd/tac/constellation/views/tableview/resources/TableVisibilityAll.png" alt="Selected Only Button
    Unselected" />
    \<\> <img src="../ext/docs/CoreTableView/src/au/gov/asd/tac/constellation/views/tableview/resources/TableVisibilitySelectedOnly.png" alt="Selected Only Button
    Selected" />
    - Clicking the selected only toolbar button will hide any elements
    which are not selected on the graph. Note that while this option is
    enabled, selection in the table will not update selection on the
    graph.
-   *Element Type* <img src="../ext/docs/CoreTableView/src/au/gov/asd/tac/constellation/views/tableview/resources/TableElementTypeTransactions.png" alt="Transaction Element Type
    Button" />
    \<\> <img src="../ext/docs/CoreTableView/src/au/gov/asd/tac/constellation/views/tableview/resources/TableElementTypeNodes.png" alt="Node Element Type
    Button" />
    - Clicking the element type toolbar button will switch between
    tabular views of transaction data (which includes the nodes at
    either end), or node data.
-   *Copy Table* <img src="../ext/docs/CoreTableView/src/au/gov/asd/tac/constellation/views/tableview/resources/TableCopy.png" alt="Copy Table
    Button" />
    - Clicking on the copy toolbar button will provide you with options
    to copy the table to the system clipboard. The table will be copied
    exactly as it appears in the Table View.
-   *Export Table* <img src="../ext/docs/CoreTableView/src/au/gov/asd/tac/constellation/views/tableview/resources/TableExport.png" alt="Export Table
    Button" />
    - Clicking on the export toolbar button will provide you with
    options to export the table to CSV or Excel. The table will be
    copied exactly as it appears in the Table View.
-   *Other Settings* <img src="../ext/docs/CoreTableView/src/au/gov/asd/tac/constellation/views/tableview/resources/TableOtherSettings.png" alt="Other Settings
    Button" />
    - Clicking on the other settings toolbar button will provide you
    with options to load and save your table preferences (e.g. column
    ordering, column sorting) as well as change the size of each page in
    the table.
