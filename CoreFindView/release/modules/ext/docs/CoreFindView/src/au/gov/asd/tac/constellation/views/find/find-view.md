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
<td><img src="../ext/docs/CoreFindView/src/au/gov/asd/tac/constellation/views/find/resources/find.png" width="16" height="16" alt="Find View Icon" /></td>
</tr>
</tbody>
</table>

The Find View allows the creation of sophisticated rules to be searched
for, and 'matched' on a graph. You can choose to find on nodes,
transactions, edges, or links.


## Basic Find

<div style="text-align: center">
    <img src="../ext/docs/CoreFindView/src/au/gov/asd/tac/constellation/views/find/resources/find-view.png" alt="Find" />
</div>

In the Basic Find tab, you are able to perform a basic search on
attribute values in your graph. Click on "Search Attributes:" drop down to
display a list of attributes you can search on and type your
search term into the "Find:" text box. There are options below the
text box to interpret your search as standard text or a regular
expression (RegEx) and options to make the search case-sensitive or only
match the exact search term.

Once you have setup your search, click "Find All" to select everything on
the graph matching the specified criteria.

NOTE: Anything already selected on the graph will be deselected if it is
not a part of the result set of the search. If you want to instead
append the results to the current selection, change the "Post-Search Action:" to "Add To Selection".

## Replace

<div style="text-align: center">
    <img src="../ext/docs/CoreFindView/src/au/gov/asd/tac/constellation/views/find/resources/replace-view.png" alt="Replace Find" />
</div>

In the Replace tab, you are able to perform a basic search and replace
on attribute values in your graph. The setup is similar to that of the
basic find except with an additional "Replace With:" text box to specify
the replacement text.

Once you have setup your search, click "Replace All". This will highlight
everything on the graph matching the find text and replace the find text
with the replace text.

## Advanced Find

<div style="text-align: center">
    <img src="../ext/docs/CoreFindView/src/au/gov/asd/tac/constellation/views/find/resources/advanced-view.png" alt="Advanced Find" />
</div>

In the Advanced Find tab, you are able to create a series of find rules
to be used for your search. At the top of the tab, you can choose a
graph element has to match all of the rules or any of them. You can add
or remove rules from by clicking on the "+" or "-" located to the left
of a rule.

There are currently 6 supported attribute types that can be searched for
in various ways. Attribute types that are not registered as one of the
following types will be searched as 'strings.' The types are as follows:

-   Boolean searches:  
    <img src="../ext/docs/CoreFindView/src/au/gov/asd/tac/constellation/views/find/resources/find-boolean.png" />
-   Color searches:  
    <img src="../ext/docs/CoreFindView/src/au/gov/asd/tac/constellation/views/find/resources/find-color.png" />
-   DateTime searches:  
    <img src="../ext/docs/CoreFindView/src/au/gov/asd/tac/constellation/views/find/resources/find-datetime.png" />
-   Float searches:  
    <img src="../ext/docs/CoreFindView/src/au/gov/asd/tac/constellation/views/find/resources/find-float.png" />
-   Icon searches:  
    <img src="../ext/docs/CoreFindView/src/au/gov/asd/tac/constellation/views/find/resources/find-icon.png" />
-   String searches:  
    <img src="../ext/docs/CoreFindView/src/au/gov/asd/tac/constellation/views/find/resources/find-string.png" />

Once you have setup your search, click "Find All" to select everything on
the graph matching any or all (depending on what was selected) of the
specified criteria.
