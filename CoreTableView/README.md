# Table View Component

The table view component provides a table that is linked to the currently active graph
allowing the user to view the graph data in tabular form.

<p/>

The following is an outline of key concepts, functionality and code that will assist in understanding
how this module works.

## Table State

This is the table state that is embeded in the graph attributes as `table_view_state`.

<p/>

It holds the currently active state of the table. That includes

* __*Selected Only Mode Active:*__

  This is a flag specifying if the table is currently in "Selected Only" mode.

* __*Element Type:*__

  This is the graph element type that the table is currently displaying, VERTEX or TRANSACTION.

* __*Visible Columns:*__

  This is the columns/graph attributes that are currently visible in the table

The below sections provide more information on these properties.

### *Selected Only Mode*

<br/>

Selected only mode is a toggle button on the table. It controls the interaction between the graph and the table.

<p/>

When the table __*IS IN*__ selected only mode the contents of the table is __*ONLY*__ the elements selected on the graph. This means
that when you click on rows in the table it will have no effect on the selection in the graph but if you select
elements in the graph, that will effect the data displayed in the table.

<p/>

When the table __*IS NOT IN*__ selected only mode the contents of the table is __*ALL*__ the elements in the graph regardless of
their selection status. When selecting rows in the table that will trigger the selection of the corresponding element
in the graph and vice versa.

</p>

### *Element Type*

<br/>

Element type is a toggle button on the table. It controls which types of elements in the graph are displayed in the table.
There are two options, VERTICES and TRANSACTIONS.

<p/>

When the selection is vertices the columns in the table will represent the attributes on those vertex elements. The columns
in this case will be prefixed with "`source.`". For example

| source.COLUMN_A | source.COLUMN_B |
| --------------- | --------------- |
| value1          | value2          |

When the selection is transactions the columns in the table will represent the attributes on those transaction elements. But
they will also include the attributes on the vertices as well. The vertex attributes will have two columns each, for each
attribute one column will be prefixed with "`source.`" and the other with "`destination.`". The transaction element attributes
will be prefixed with "`transaction.`". For example

| source.LOCATION | source.NAME | transaction.TRAVEL_TIME | destination.LOCATION | destination.NAME |
| --------------- | ----------- | ----------------------- | -------------------- | ---------------- |
| Sea of Serenity | Home Base   | 2 hrs                   | Sea of Tranquility   | Swimming Spot    |

### *Visible Columns*

<br/>

This is a list of columns or graph element attributes that are currently visible in the table. The list
contains `Pair` objects which consist of two parts.

* `attributeNamePrefix`: This is the prefix added to the column name. It will be one of
   "`.source`", "`.destination`", "`.transaction`". See the [Element Type Button](#element-type)
   section for more information.
* `attribute`: This is the graph element attribute. It links the column back to the source in the graph.

As described above the same `attribute` could be in two pairs but each would have a different
`attributeNamePrefix`.

## Active Table Reference

This represents reference data for the current table. This data is used to maintain user
settings between table updates and refreshes. These updates can happen for things like
page changes, graph modifications, user table preference changes, etc.

### *Selected Only Selected Rows*

<br/>

When in selected only mode the tables selection is independent of the selection in the graph. As a means
of tracking what rows are currently selected in the graph the reference maintains a list of selected
rows. When the table __*IS NOT IN*__ selected only mode this list is empty and irrelevant.

</p>

The reason that this is maintained is due to pagination. The table view will only contain a record
of selected rows for that page. Not all selected rows across multiple pages.

### *Column Index*

<br/>

This is the current list of columns displayed in the table. It is a list of `Column`
objects. Using the properties of the `Column` object you can go from a table column to
a graph element and vice versa. The `Column` object has the following properties.

* `attributeNamePrefix`: This is the prefix added to the column name. It will be one of
   ".source", ".destination", ".transaction". See the [Element Type Button](#element-type)
   section for more information.
* `attribute`: This is the graph element attribute. It links the column back to the source in the graph.
* `tableColumn`: This is the actual JavaFX column that is being displayed in the table.

### *Pagination*

<br/>

The `Pagination` object is part of JavaFX and is attached to the table. The table uses it, to determine
what to display when the user triggers a new page load. The `Pagination` object has a page factory
(`TableViewPageFactory`) that generates the new rows each time the user changes pages.

### *Sorted Row List*

<br/>

This is the list of all rows in the table, not just what is currently being displayed on the active
page of the table.

</p>

It is sorted based on the current sort settings. There is a comparator in the utilities package that
details the sort logic.

### *User Table Preferences*

<br/>

The table preferences are stored in a JSON file spearate to the graph star file. They can
be loaded and saved via buttons on the table. They are not loaded automatically.

</p>

This is different to the state because that is tied to the graph and this is tied to
the user. These can be loaded after a graph is opened to override those settings.

* __*Max Rows Per Page*__

  An integer representing the maximum number of rows that can be displayed in the table per page.

* __*Visible Columns*__

  A list of column names that are visible in the table. When preferences are loaded, it will make
  visible these columns if they exist for the current table/graph and then update the table state
  with the columns it found and made visible.

</p>

  When a user changes the column visibilities through the preference menus, this preference
  data is updated.

* __*Sort*__

  The column that the table is sorted by. The table only supports a single column sort.

## Table View

The Java FX UI component that actually renders the table is called `TableView`. This
component can only access the items from the current page and everytime the page is changed
or the data is refreshed all existing sorting, selection etc. is lost and needs to be re-applied
which is why the above data structures are needed to keep track of the overall table state.
