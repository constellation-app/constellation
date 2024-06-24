# Application Preferences

Application preferences can be accessed via Setup -> Options ->
CONSTELLATION -> Application.

## User Directory

The user directory is where Constellation stores user-specific data such
as auto-save files, icons, and templates. By default the user directory
is "&lt;<HomeDirectory&gt;/.CONSTELLATION". If you want to change your user
directory, create the new directory first, copy any files you want from
the current directory to the new one, then change the directory in
preferences.

You can open your user directory in your default file browser via Help
-> User Directory.

## Autosave

By default, Constellation saves a copy of the graphs you're working on
every five minutes. You can change this interval, or disable autosave
altogether in preferences.

## Startup

By default, Constellation displays the Welcome page when on startup. The
Welcome page can be hidden on startup either in preferences or directly
on the Welcome page via the "Show on Startup" tickbox option.

## Internal Webserver

Constellation has a built in Restful web service allowing you to
programatically run plugins via web calls. The web service needs to be
started manually from Tools -> Start REST Service and it will by default
listen to port 1517 on localhost. To reduce the risk of potential
security vulnerabilities the web service is limited to localhost only.

You also have the ability make Restful calls via a file system. This can
be handy if you have a shared folder that multiple VMs can access and
want the ability to run commands over the network. To do this you can
specify a "REST directory" and Constellation will poll that directory
for plugins to run.

## Jupyter Notebooks

The "Notebook directory" specifies the location where your Jupyter
Notebooks are located and will launch the Jupyter Notebooks server from
this directory. You can start the Jupyter Notebooks server from
Constellation via Tools -> Start Jupyter Notebook.

Selecting to "Download Python REST client" means that when you start the
Jupyter Notebook server, Constellation will download the
constellation_client.py file to your .ipython folder. The
constellation_client.py provides easy access to make Restful calls to
Constellation.

## Open/Save Location

By default, Constellation opens your previous opened or saved location
when you try to open or save a file, and it will remember the last
location accessed for the next session. This can be altered in
preferences by unticking the "Remember Open/Save Location" option. When
this is unticked, the open or save location defaults to the user's home
directory.

## Application Font Preferences

This is where the user is able to set their preferred default font and
default font size for the Constellation application.

## Colorblind Mode

The user is able to set a colorblind mode for constellation. When a 
colorblind mode is set, nodes drawn are automatically generated with
colorblind-friendly colors, and existing graphs can be adjusted to 
the new color scheme using Tools -> Complete with Schema.

## Spell checking in Constellation

Spell checking is available in specified text areas of Constellation. 
E.g. Data access view (Extract Types and Extract Words Plugins). 
It can be added in other text areas as requested.

When this option is enabled, incorrect spellings and some grammar errors 
are highlighted and underlined. Left Clicking on a highlighted word/phrase 
will popup a suggestions list to choose from. An "Ignore All" button is 
available for spelling errors which will ignore the same error in the 
current text area.

When this option is set, the right click context menu option "Turn On Spell
Checking" will be available on fields where it has been enabled, and it 
can be used to toggle on/off spell checking dynamically.