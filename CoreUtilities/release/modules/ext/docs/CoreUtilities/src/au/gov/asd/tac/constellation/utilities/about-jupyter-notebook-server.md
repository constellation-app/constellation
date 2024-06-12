# About The Jupyter Notebook Server

Selecting Tools -> Start Jupyter Notebook will start a Jupyter notebook
server. This will in turn automatically open a new page in your default
web browser. The HTTP REST server will be started automatically if it
hasn't already been started.

The commands `jupyter-notebook` and `pip install` are used when starting 
the server. The command `pip install`, along with other arguements, is run first  
and installs a package necessary for using Constellation in your Jupyter notebook. 
This assumes that boths commands have been added to your command path.
The easiest way to install and add these to the command path is to install the 
Anaconda Python distribution and check the box "Add Anaconda3 to my PATH environment variable".

<br />
<img src="../ext/docs/CoreUtilities/src/au/gov/asd/tac/constellation/utilities/AnacondaInstall.png" alt="Installing Anaconda" />
<br />

**NOTE:** Using the "Add Anaconda3 to my PATH environment variable" checkbox to add commands to PATH is not the only way of doing so, it is simply an easy method.
If you believe adding commands to PATH using this method will cause issues, simply search online for guides on manually adding commands to PATH.


If these commands don't work (e.g. you get a "Failed to start jupyter-notebook" error), 
you may not have Jupyter or pip installed, or may have not added these to your command path.

<br />
<img src="../ext/docs/CoreUtilities/src/au/gov/asd/tac/constellation/utilities/PipError.png" alt="Pip Error" />
<br />
Example of pip error
<br />

<br />
<img src="../ext/docs/CoreUtilities/src/au/gov/asd/tac/constellation/utilities/JupyterError.png" alt="Jupyter Error" />
<br />
Example of Jupyter error
<br />

<br />
The notebook server is started in the directory specified by the
"Notebook directory" preference (in Application preferences).

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

When the notebook server starts, its output is sent to the output window
"Jupyter notebook".

When Constellation exits, it will automatically shut down the Jupyter
notebook that it started.

If for some reason Constellation cannot start a Jupyter notebook server,
you can start one manually using the command above at a command prompt.
Don't forget to manually start the REST server. You will also have to
manually shut down the notebook server.

<br />
## Starting your own Jupyter notebook

Starting your own Jupyter notebook server by entering the command
"jupyter-notebook" at a command prompt in the correct directory has
exactly the same result. This is just a convenience provided by
Constellation.

<br />
## In Depth Tutorial

A tutorial for how to use Jupyter notebook with Constellation can be found on our github
[here](https://github.com/constellation-app/constellation-training/blob/master/Analyst%20Training/Exercise%2010%20-%20Network%20Analysis%20With%20Python/notebooks_and_constellation.ipynb).

Simply download the .ipynb file and open it in Jupyter notebook.