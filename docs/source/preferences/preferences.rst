Application Preferences
-----------------------

User Directory
``````````````

The user directory is where CONSTELLATION stores user-specific data such as auto-save files, icons, and delimited importer templates. By default the user directory is ``*HomeDirectory*/.CONSTELLATION``.

You can open your user directory in your default file browser using the Help → User Directory menu.

If you want to change your user directory, create the new directory first, copy any files you want from the current directory to the new one, then change the directory in preferences.

Autosave
````````

By default, CONSTELLATION saves a copy of the graphs you're working on every five minutes. You can change this interval, or disable autosave altogether.

Scripting
`````````

Select the default language to be used by the Scripting View. Default language options are:

* Python
* Javascript
* Groovy

Memory Usage
````````````

CONSTELLATION is a java application which means that the memory management is handles by the *Garbage Collector* (GC) process inside the JVM (Java Virtual Machine). It is best left to the (GC) to decide when to release unused memory but we can give a hint to the (GC) to clear unused memory when a graph is open or closed.

The following options have been enabled by default and does not guarantee that any garbage collection will be performed because it is ultimately up to the (GC) process to decide. A side effect of having this enabled means that opening and closing graphs could take longer to complete if the (GC) decides to clean up unused memory on a open or close.

* Garbage Collect (GC) on graph open
* Garbage Collect (GC) on graph close

Troubleshooting
```````````````

Verbose information like debug statements could be presented if this option is enabled. Only use this if you have been asked to enable this by CONSTELLATION support or a developer.


.. help-id: au.gov.asd.tac.constellation.preferences.application
