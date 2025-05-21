# Application Preferences

Application preferences can be accessed via Setup -> Options ->
CONSTELLATION -> Application.

<div style="text-align: center">
<img src="../ext/docs/CorePreferences/resources/applicationPanel.png" alt="Application Options Panel" />
</div>

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

The What's New page can also be displayed on startup if the tickbox is selected.

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

Selecting to "Download Python REST client" means that when you start the Jupyter 
Notebook server, Constellation will install our custom python package, constellation_client.
The constellation_client package provides a convenient interface to the REST API 
for Python scripts in Jupyter notebooks.

## Application Font Preferences

This is where the user is able to set their preferred default font and
default font size for the Constellation application.

## Colorblind Mode

The user is able to set a colorblind mode for constellation. When a 
colorblind mode is set, nodes drawn are automatically generated with
colorblind-friendly colors, and existing graphs can be adjusted to 
the new color scheme using Tools -> Complete with Schema.

