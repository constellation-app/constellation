# Data Access View Component

The data access view component provides a tab pane. Each tab in the pane contains
the available data access plugins that can be executed on 1 or more nodes in the
current graph. A single tab can be executed or multiple in one go.

<p/>

The following is an outline of key concepts, functionality and code that will assist in understanding
how this module works.

## Data Access Pane State

Not to be confused with the `DataAccessState` which is stored with the graph.

This state refers to the active state of the data access view per open graph and
is dealt with in `DataAccessPaneState`. The state contains the following for each graph...

* `queriesRunning`: Has the execute button been pressed for this graph and are the plugins still executing.
* `executeButtonIsGo`: Does the execute button have the text 'Go' written on it.
* `runningPlugins`: If there are running plugins then this map will contain a reference to the
actual execution through the key value. That key will then map to the name of the plugin being executed
in the future.

The `DataAccessState` also holds a static reference to all the data access plugins that were
found in the classpath when the data access view was first opened.

## Execute Button

The execute button's state changes based on several factors.

* Enabled Plugin: True if all tabs in the tab pane have at least one plugin enabled
* Valid Enabled Plugins: True if all enabled plugins in the tab pane have valid parameters set
* Valid Date Range: True if each tab's date range is valid
* Running Queries: True if there are queries running for the current graph

The following table describes what is needed for the execute button to be enabled in its different
states.

|                | Enabled Plugins         | Valid Enabled Plugins  | Valid Date Range        | Running Queries               |
| -------------- | ----------------------  | ---------------------- | ----------------------- | ----------------------------- |
| "Go"/Enabled   | :white_check_mark:      | :white_check_mark:     | :white_check_mark:      | :negative_squared_cross_mark: |
| "Stop"/Enabled | :eight_spoked_asterisk: | :eight_spoked_asterisk:| :eight_spoked_asterisk: | :white_check_mark:            |

## Tabs

1. Each tab consists of a `QueryPhasePane`.
2. A `QueryPhasePane` has a group of `HeadingPane`'s.
3. Each `HeadingPane` represents one type of data access plugin. Within each pane is a collapsible list of
  all available data access plugins belonging to that `HeadingPane`'s particular type.
4. Within the collapsible parts of the `HeadingPane` is a `DataSourceTitledPane`.
5. A `DataSourceTitledPane` is the pane that represents the specific plugin.
