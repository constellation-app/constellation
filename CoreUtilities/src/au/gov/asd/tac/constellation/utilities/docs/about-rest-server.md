# About The Constellation REST server

The Constellation REST server offers a RESTful HTTP programming
interface to interact with the current graph. The API is documented
using Swagger; see Tools -> Display REST Server Documentation.

The server must be started manually; use Tools -> Start REST Server. The
server listens for HTTP requests on localhost: this cannot be changed.
The listen port can be changed in the application options.

When the server starts, it writes a JSON document to a file called
"rest.json" in your ".CONSTELLATION" directory. This JSON document
records the port that the server is listening on (the "port" key) and a
secret to be used when communicating with the server (the
"X-CONSTELLATION-SECRET" key). This secret must be used in the
"X-CONSTELLATION-SECRET" HTTP header; requests that do not include the
header with the secret will be rejected. This stops other clients on the
same system from using your REST server to interact with your graphs.

When started, the server also optionally writes a Python file called
"constellation_client.py" to your "~/.ipython" directory. This provides
a convenient interface to the REST API for Python scripts in Jupyter
notebooks. Python 3.6 is required. See the built-in help documentation
in the script for details.

The Python client library reads the "rest.json" document and uses it to
communicate with the REST server: client code that uses
"constellation_client" does not need to worry about the details.

If downloading is enabled in the options, the Python script file will be
downloaded if:

-   The script file does not already exist; or
-   The script file exists, but is different to the latest version known
    to Constellation.

This means that when the script is updated, the new version will
automatically overwrite an existing version the next time the REST
server is started.

## Alternate Transports

REST over HTTP works nicely when Constellation and the client are
running on the same workstation. However, this may not always be the
case. For example, a notebook running under Apache Zeppelin or
JupyterLab on a separate server may want to communicate with
Constellation.

To allow for this scenario, Constellation also provides a filesystem
transport. As well as an HTTP server, Constellation has a file listener
which polls a directory. A HTTP request is emulated by placing the files
"request.json" and "content.in" in this directory; the response is
emulated by placing the files "response.json" and "content.out" in the
directory.

The file listener can be started and stopped using Tools -> Start/Stop
File Listener. Starting the file listener will also write
"constellation_client.py" to the "~/.ipython" directory.

Constellation polls the directory waiting for the request files to
appear. The client places the files in the directory, and polls waiting
for the response files to appear. When Constellation sees the request
files, it reads and deletes them (to avoid confusion with the next
request), performs the required action, and writes the response files.
When the client sees the response files, it reads and deletes them (to
avoid further confusion).

Polling is initially done once per second. Over time, if no requests are
made, the polling period increases to once per five seconds, to decrease
resource usage on the filesystem. If a request is found, the polling
time is reset to once per second.

"constellation_client.py" client provides two ways of getting the files
into the directory and retrieving the response:

-   *Shared filesystem* - If the Constellation workstation and the
    system on which the client is running share the user's filesystem,
    the client can simply create and read files in the user's directory.
    This method is secure as long as the user's filesystem is secure.
-   *sftp* - If the Constellation workstation and the system on which
    the client is running do not share a filesystem, the client can use
    sftp to transfer files to and from the Constellation workstation.
    This method is as secure as sftp.

See "constellation_client.py" for the details of how the filesystem
transport is implemented.
