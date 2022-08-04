# Data Access View Options

All the options can be accessed via the <img src="../constellation/CoreDataAccessView/src/au/gov/asd/tac/constellation/views/dataaccess/docs/resources/DataAccessOptions.png" alt="Options
Button" />
menu in the top left corner of the Data Access View.

## Saving and loading data access preferences

Data access preferences can be saved and loaded using the "Save Templates" and
"Load Templates" options.

When a preference is saved, you are prompted to name the preference. If
a preference of that name has already been saved, you will be asked if
you want to overwrite it. Preferences are saved by default in the
directory &lt;HOME_DIRECTORY&gt;/.CONSTELLATION/DataAccessView. (The name of the
file in which the preference is saved is encoded so it doesn't clash
with file system limitations.) Files in this directory can be copied and
deleted using your favourite file management utility.

When you select "Load Templates", you will be presented with a list of saved
preferences. Select one from the list and select OK. The preference will
be loaded and will appear exactly as it was when it was saved. You can
also remove a saved preference from here by selecting one from the list
and selecting Remove.

## Saving data access results

The raw results of data access queries can be saved to files using the
"Save Results" option.

When you first check the "Save Results" item, a directory chooser dialog
will appear so you can select the directory that the results will be
saved to. The directory will be remembered and results will be saved
until you uncheck the item, even if you exit Constellation and start
again.

To stop saving results, uncheck "Save Results". The next time you check
it, Constellation will remember the folder that you were previously
saving to, and use it as the initial directory for the directory
chooser. (This is an easy way to find out where your save directory is.)

Whenever you run a data access query with "Save Results" checked, a
status message will remind you where your results are being saved. If
the directory has been removed, you will get an error notification and
no results will be saved.

Each plugin that saves results will create one or more files in your
save directory. It is up to each plugin how it names the files it
creates.

## Deselecting plugins on Go

Data Access plugins can be deselected after pressing the Go button via
the "Deselect on Go" option.

When you check the "Deselect on Go" item, data access plugins
that are ticked will be deselected after pressing "Go". This will
persist until the item is unchecked.

When "Deselect on Go" is unchecked, data access plugins will
remain ticked even after "Go" is pressed and will need to be unticked
manually.
