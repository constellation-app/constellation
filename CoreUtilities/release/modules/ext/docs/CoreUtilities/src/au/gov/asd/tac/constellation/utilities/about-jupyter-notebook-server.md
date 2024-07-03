# About The Jupyter Notebook Server

Selecting Tools -> Start Jupyter Notebook will start a Jupyter notebook
server. This will in turn automatically open a new page in your default
web browser. The HTTP REST server will be started automatically if it
hasn't already been started.

Using Jupyter Notebook with constellation requires various Python related packages and software.
These are all included the Anaconda Python distribution, and thus is recommended for users to install.

## Starting your own Jupyter notebook

Starting your own Jupyter notebook server by entering the command
"jupyter-notebook" at a command prompt in the correct directory has
exactly the same result. This is just a convenience provided by
Constellation.
he notebook server is started in the directory specified by the
"Notebook directory" preference (in Application preferences).

The command `pip install`, along with other arguements, is run when starting the server  
and installs a custom Python package, constellation_client.
This package is necessary for using Constellation in your Jupyter notebook.

When the notebook server starts, its output is sent to the output window
"Jupyter notebook". If you start a notebook server and the new page
contains a "Password or token" prompt, look in the output window for
text that looks like this:

        Copy/paste this URL into your browser when you connect for the first time,
        to login with a token:
            http://localhost:8888/?token=7f9658570b3bc41bd04cfb20caa382ab67eee7457c7391dc
            

Copy/paste the token into the "Password or token" input field and select
"Log in". Alternatively, you can use the command `jupyter notebook list`
at a command prompt to see what notebook servers are running, and use an
existing token.

When Constellation exits, it will automatically shut down the Jupyter
notebook that it started.

If for some reason Constellation cannot start a Jupyter notebook server,
you can start one manually using the command above at a command prompt.
Don't forget to manually start the REST server. You will also have to
manually shut down the notebook server.

## Common Errors
If the server fails to start and you get an error such as "Failed to start jupyter-notebook", 
you may not have Jupyter or pip installed, or may have not added these to your 
command path (in the case of using Windows).

Example of error from pip not being installed on windows:
<br />
<img src="../ext/docs/CoreUtilities/src/au/gov/asd/tac/constellation/utilities/PipError.png" alt="Pip Error" />
<br />
<br />
Example of error from Jupyter not being installed on windows:
<br />
<img src="../ext/docs/CoreUtilities/src/au/gov/asd/tac/constellation/utilities/JupyterError.png" alt="Jupyter Error" />
<br />
<br />

## In Depth Tutorial

A tutorial for how to use Jupyter notebook with Constellation can be found on our github
[here](https://github.com/constellation-app/constellation-training/blob/master/Analyst%20Training/Exercise%2010%20-%20Network%20Analysis%20With%20Python/notebooks_and_constellation.ipynb).

Simply download the .ipynb file and open it in Jupyter notebook.

<br />

## Setup

### Windows
On Windows, the easiest way to install these and add them to the command path is to install the 
Anaconda Python distribution and check the box "Add Anaconda3 to my PATH environment variable".

<br />
<img src="../ext/docs/CoreUtilities/src/au/gov/asd/tac/constellation/utilities/AnacondaInstall.png" alt="Installing Anaconda" />
<br />

**NOTE:** Using the "Add Anaconda3 to my PATH environment variable" checkbox to add commands to PATH is not the only way of doing so, it is simply an easy method.
If you believe adding commands to PATH using this method will cause issues, simply search online for guides on manually adding commands to PATH.

<br />

### Linux and Mac
Similar to Windows, the Anaconda Python distribution can also be used to install 
all the required software on Linux and Mac.

**NOTE:** The required commands and packages could instead be installed from a command 
line interface. However, this is only recommended for advanced users.
