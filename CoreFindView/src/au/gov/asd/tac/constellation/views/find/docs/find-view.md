# Find and Replace

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
<td>Open Find View</td>
<td>Ctrl + F</td>
<td>Edit -&gt; Find...</td>
<td><div style="text-align: center">
<img src="../constellation/CoreFindView/src/au/gov/asd/tac/constellation/views/find/docs/resources/find.png" width="16" height="16" />
</div></td>
</tr>
</tbody>
</table>

The Find View allows the creation of sophisticated rules to be searched
for, and 'matched' on a graph. You can choose to find on nodes,
transactions, edges, or links.

<div style="text-align: center">

<img src="../constellation/CoreFindView/src/au/gov/asd/tac/constellation/views/find/docs/resources/FindView.png" alt="Find" />

</div>

## Basic Find

In the Basic Find tab, you are able to before a basic search on
attribute values in your graph. Click on "Select attributes..." to
display a drop down list of attributes you can search on and type your
search term into the "Find what:" text box. There are options below the
text box to interpret your search as standard text or a regular
expression (RegEx) and options to make the search case-sensitive or only
match the exact search term. You can reset your search by clicking on
"Reset to Default".

Once you have setup your search, click "Find" to select everything on
the graph matching the specified criteria.

NOTE: Anything already selected on the graph will be deselected if it is
not a part of the result set of the search. If you want to instead
append the results to the current selection, tick "Add results to
current selection".

## Replace

In the Replace tab, you are able to perform a basic search and replace
on attribute values in your graph. The setup is similar to that of the
basic find except with an additional "Replace with:" text box to specify
the replacement text.

Once you have setup your search, click "Replace". This will highlight
everything on the graph matching the find text and replace the find text
with the replace text.

## Advanced Find

In the Advanced Find tab, you are able to create a series of find rules
to be used for your search. At the top of the tab, you can choose a
graph element has to match all of the rules or any of them. You can add
or remove rules from by clicking on the "+" or "-" located to the left
of a rule.

There are currently 8 supported attribute types that can be searched for
in various ways. Attribute types that are not registered as one of the
following types will be searched as 'strings.' The types are as follows:

-   Boolean searches:  
    <img src="../constellation/CoreFindView/src/au/gov/asd/tac/constellation/views/find/docs/resources/find-boolean.png" />
-   Color searches:  
    <img src="../constellation/CoreFindView/src/au/gov/asd/tac/constellation/views/find/docs/resources/find-color.png" />
-   Date searches:  
    <img src="../constellation/CoreFindView/src/au/gov/asd/tac/constellation/views/find/docs/resources/find-date.png" />
-   DateTime searches:  
    <img src="../constellation/CoreFindView/src/au/gov/asd/tac/constellation/views/find/docs/resources/find-datetime.png" />
-   Float searches:  
    <img src="../constellation/CoreFindView/src/au/gov/asd/tac/constellation/views/find/docs/resources/find-float.png" />
-   Icon searches:  
    <img src="../constellation/CoreFindView/src/au/gov/asd/tac/constellation/views/find/docs/resources/find-icon.png" />
-   String searches:  
    <img src="../constellation/CoreFindView/src/au/gov/asd/tac/constellation/views/find/docs/resources/find-string.png" />
-   Time searches:  
    <img src="../constellation/CoreFindView/src/au/gov/asd/tac/constellation/views/find/docs/resources/find-time.png" />

Once you have setup your search, click "Find" to select everything on
the graph matching any or all (depending on what was selected) of the
specified criteria.
