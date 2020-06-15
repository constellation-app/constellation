Saving and loading data access preferences
------------------------------------------

Data access preferences can be saved and loaded using "Save..." and "Load..." items in the "Options" menu in the top left corner of the Data Access View.

When a preference is saved, you are prompted to name the preference. If a preference of that name has already been saved, you will be asked if you want to overwrite it. You can also remove a saved preference using the Remove button. Preferences are saved by default in the directory HOME_DIRECTORY/.CONSTELLATION/DataAccessView. (The name of the file in which the preference is saved is encoded so it doesn't clash with file system limitations.) Files in this directory can be copied and deleted using your favourite file management utility.

When you select "Load...", you will be presented with a list of saved preferences. Select one from the list and select Ok. The preference will be loaded and will appear exactly as it was when it was saved.

Saving data access results
--------------------------

The raw results of data access queries can be saved to files by toggling the "Save results" item in the "Options" menu in the top left corner of the Data Access View .

When you first check the "Save results" item, a directory chooser dialog will appear so you can select the directory that the results will be saved to. The directory will be remembered and results will be saved until you uncheck the item, even if you exit CONSTELLATION and start again.

To stop saving results, uncheck the "Save results" item. The next time you check it, CONSTELLATION will remember the folder that you were previously saving to, and use it as the initial directory for the directory chooser. (This is an easy way to find out where your save directory is.)

Whenever you run a data access query with "Save results" checked, a status message will remind you where your results are being saved. If the directory has been removed, you will get an error notification and no results will be saved.

Each plugin that saves results will create one or more files in your save directory. It is up to each plugin how it names the files it creates.


.. help-id: au.gov.asd.tac.constellation.views.dataaccess.io.ParameterIO
