# Save Graph

Constellation saves the graph to a custom format (check it out
[here](../ext/docs/CoreGraphFramework/src/au/gov/asd/tac/constellation/graph/constellation-file-format.md))
with file suffix ".star". A saved Constellation file retains almost
everything about the graph's current state including layout, selections,
attribute definitions and values.

When a file has changes pending, its name (in the upper tab) appear
bolded in blue, otherwise it is shown in simple black text.

There are a few different ways to save a graph:

## Save

File -> Save will prompt you for a file name the first time the file is
being saved. Otherwise, it will use the current name.

## Save As

File -> Save As can be used with new or existing graphs to explicitly
save the information to a specific file. Constellation won't allow you
to specify an existing filename for another currently open graph.

## Save All

File -> Save All can be used to save all currently open graphs. If any
of them are being saved for the first time, you will be prompted for a
file name for each one that requires it.
