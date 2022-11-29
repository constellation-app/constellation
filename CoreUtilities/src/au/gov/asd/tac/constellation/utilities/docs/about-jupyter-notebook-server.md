# About The Jupyter Notebook Server

Selecting Tools -> Start Jupyter Notebook will start a Jupyter notebook
server. This will in turn automatically open a new page in your default
web browser. The HTTP REST server will be started automatically if it
hasn't already been started.

The command "jupyter-notebook" is used to start the server. This assumes
that the command is on your command path. If this command doesn't work
(e.g. you get a "Failed to start jupyter-notebook" error), you may not
have installed Jupyter. The easiest way to do this is to install the
Anaconda Python distribution.

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
"Log in". Alternatively, you can use the command "jupyter notebook list"
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

## Starting your own Jupyter notebook

Starting your own Jupyter notebook server by entering the command
"jupyter-notebook" at a command prompt in the correct directory has
exactly the same result. This is just a convenience provided by
Constellation.
