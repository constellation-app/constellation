# Find

<table data-border="1">
<caption>Find Actions</caption>
<colgroup>
<col style="width: 25%" />
<col style="width: 25%" />
<col style="width: 25%" />
<col style="width: 25%" />
</colgroup>
<thead>
<tr class="header">
<th scope="col"><strong>CONSTELLATION Action</strong></th>
<th scope="col"><strong>Keyboard Shortcut</strong></th>
<th scope="col"><strong>User Action</strong></th>
<th scope="col"><strong>Menu Icon</strong></th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Open Find View</td>
<td>Ctrl + F</td>
<td>Edit -&gt; Find</td>
<td><div style="text-align: center">
<img src="../resources/find.png" width="16" height="16" />
</div></td>
</tr>
</tbody>
</table>

Find Actions

The *Find Window* allows the creation of sophisticated rules to be
searched for, and 'matched' on a graph. An example of a Find Window can
be seen below.

  

## Major Elements of the Find Window

![](resources/find-main.png)

There are several major components found on a Find Window. They are:

1.  Combo-box where a selection between searching on 'Nodes' or
    'Transactions' may be made. Searches will only be performed on the
    selected graph element type.
2.  Combo-box where a selection between whether 'all' (binary AND) or
    'any' (binary OR) rules must be matched for a graph element to be
    returned as a result of a search.
3.  A find 'rule'. For any given search operation, one or more find
    rules must be created.
4.  An input box where the value to be matched is placed.
5.  A check-box that determines whether any results of a search are
    added to the current selection on the graph, or replace it. (Ticked
    indicates that the results are added.)
6.  Removes the find rule it appears on.
7.  Adds a blank find rule to the end of the list.
8.  Indicates the number of results found for the search.
9.  Button that reset the window to a single blank rule.
10. Combo-box that allows the selection of the graph attribute to search
    on.
11. Combo-box that indicates the form the search will take. For example
    'contains', 'is', 'occurred on', 'is between', etc.
12. Button that allows the adding of a delimited list of strings to be
    added.
13. Option for string attributes that allows a list of values to be
    searched for. A comma separated list in the value input box will be
    searched and results returned for any matches. (List searches are
    performed as binary OR operations).
14. Option to force case-sensitive searches on strings.
15. Button that performs a search with the rules specified.

  

## Supported Find Rule Types

There are currently 8 supported attribute types that can be searched for
in various ways. Attribute types that are not registered as one of the
following types will be searched as 'strings.' The types are as follows:

-   Boolean searches:  
    ![](resources/find-boolean.png)
-   Colour searches:  
    ![](resources/find-color.png)
-   Date searches:  
    ![](resources/find-date.png)
-   DateTime searches:  
    ![](resources/find-datetime.png)
-   Float searches:  
    ![](resources/find-float.png)
-   Icon searches:  
    ![](resources/find-icon.png)
-   String searches:  
    ![](resources/find-string.png)
-   Time searches:  
    ![](resources/find-time.png)
