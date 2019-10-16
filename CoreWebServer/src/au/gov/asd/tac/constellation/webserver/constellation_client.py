
"""A client for the CONSTELLATION external scripting API."""


import json
import networkx as nx
import numpy as np
import os
import requests
import pandas as pd
from pathlib import Path
import subprocess
import sys
import tempfile
import time

# A version number of the form yyyymmdd.
#
# Increase this version number when a change happens.
# For example, if a new function is added, clients that require that function
# to be present can check the version.
#
__version__ = 201804010

# The HTTP header to be used to convey the server secret (if HTTP is used).
#
_SECRET = 'X-CONSTELLATION-SECRET'
_PORT = 'port'

_DEFAULT_PORT = 1517

# The environment variable that contains the transport to be used.
#
ENV_VAR = 'CONSTELLATION_TRANSPORT'

REQUESTS = {
    'get': requests.get,
    'post': requests.post,
    'put': requests.put
}

class FileResponse:
    """This class mirrors the requests library methods and properties for files
    using the file/sftp transports."""

    def __init__(self, response_fnam, content_fnam):
        """Wrap the JSON response and content from CONSTELLATION to look like requests.

        The response file will be deleted immediately.
        The content file will be deleted on use, so asking for
        content/text/json a second time will fail.

        :param response: The filename of the JSON response (similar to HTTP headers)
        :param content: The filename of the content of the response - JSON, binary, whatever."""

        with open(response_fnam) as f:
            self.response = json.load(f)

        # We've loaded teh data, so remove the file.
        #
        os.remove(response_fnam)

        if 'error' in self.response:
            raise ValueError(self.response['error'])

        self.content_fnam = content_fnam

    @property
    def content(self):
        with open(self.content_fnam, 'rb') as f:
            buf = f.read()
        os.remove(self.content_fnam)

        return buf

    @property
    def text(self):
        with open(self.content_fnam) as f:
            lines = f.readlines()
        os.remove(self.content_fnam)

        return '\n'.join(lines)

    def json(self):
        with open(self.content_fnam) as f:
            j = json.load(f)
        os.remove(self.content_fnam)

        return j

def _transport_data(t):
    """Return data from "transport = data".

    Transport 'file' returns a transfer directory (defaulting to
    '$HOME/.CONSTELLATION/REST'.
    Transport 'sftp' returns a hostname, directory tuple; the directory
    defaults to '.CONSTELLATION/REST'."""

    ix = t.find('=')
    if t.startswith('file'):
        if ix==-1:
            return str(Path.home() / '.CONSTELLATION/REST')
        else:
            return t[ix+1:].strip()
    elif t.startswith('sftp'):
        if ix==-1:
            raise ValueError(f'Separator "=" not found in "{t}"')

        host = t[ix+1:].strip()
        if ':' in host:
            host, directory = host.split(':')
            host = host.strip()
            directory = directory.strip()
        else:
            directory =  '.CONSTELLATION/REST'

        if not host:
            raise ValueError('A non-empty host must be specified')
        if not directory:
            raise ValueError('A non-empty directory must be specified')

        return host, directory
    else:
        raise ValueError('Only "file" and "sftp" transports accepted')

class Constellation:

    def __init__(self, transport=None):
        """Initialise the REST client.

        If the transport is not specified, it is read from the environment
        variable CONSTELLATION_TRANSPORT.

        The transport can be one of
        - http - the default; no options are allowed, the server is always
            localhost. The HTTP header secret and port are read from
            the REST file created by the REST server.
        - file - a directory for transferring the REST files in may be
            specified, for example 'file = /users/user1/.CONSTELLATION/REST'.
            The default is $HOME/.CONSTELLATION/REST
        - sftp - an sftp destination must be specified. A directory may also
            be specified, the default directory is '.CONSTELLATION/REST'.
            Examples:
                'sftp = workstation.example.com'
                'sftp = me@workstation'
                'sftp = workstation:.CONSTELLATION/REST'

        :param transport: One of
            'http' (default),
            'file [= directory]',
            'sftp = [user@]host[:dir]'.
        """

        if transport is None:
            env = os.getenv(ENV_VAR)
            if env:
                transport = env

        if transport is None or transport=='http':
            self.rest_request = self.http_request

            self.data = _get_rest()
            self.headers = {}
            if _SECRET in self.data:
                self.headers[_SECRET] = self.data[_SECRET]

            if _PORT in self.data:
                self.port = self.data[_PORT]
            else:
                self.port = _DEFAULT_PORT
        elif transport.startswith('file'):
            self.rest_request = self.file_request

            self.file_dest = _transport_data(transport)
            print(f'Transport: file in {self.file_dest}', file=sys.stderr)
        elif transport.startswith('sftp'):
            self.rest_request = self.sftp_request

            self.sftp_host, self.sftp_dir = _transport_data(transport)
            print(f'Transport: sftp to {self.sftp_host}:{self.sftp_dir}', file=sys.stderr)

            self.tmpdir = tempfile.TemporaryDirectory()
        else:
            raise ValueError('Transport must be one of http, file, sftp')

    def http_request(self, verb='get', endpoint=None, path=None, params=None, json_=None, data=None):
        """Call CONSTELLATION's REST API over HTTP.

        HTTP calls are only made to localhost for simplicity and security."""

        if verb in REQUESTS:
            f = REQUESTS[verb]
        else:
            raise ValueError(f'Unrecognised verb "{verb}"')
        url = f'http://localhost:{self.port}{endpoint}/{path}'
        r = f(url, params=params, json=json_, data=data, headers=self.headers)
        r.raise_for_status()

        return r

    def _write_files(self, td, verb='get', endpoint=None, path=None, params=None, json_=None, data=None):
        """Write the request and content files for CONSTELLATION's REST over files API.

        Ensure that content.in exists before request.json, and that request.json
        is renamed, not created, so it exists atomically.

        The HTTP request GET http://localhost/v1/plugin/run?name=selectall
        becomes the request.json JSON document
        {
          "verb": "get",
          "endpoint": "/v1/plugin",
          "path": "run",
          "args": { "name": "selectall" }
        }

        :return: A tuple of (request.json path, content.in path or None)
        """

        if json_ is not None and data is not None:
            raise ValueError('Use only one of json_ and data')

        rd = dict(verb=verb, endpoint=endpoint, path=path)
        if params:
            rd['args'] = params

        c = None
        if json_ is not None:
            c = td / 'content.in'
            with open(str(c), 'w') as f:
                json.dump(json_, f)

        if data is not None:
            c = td / 'content.in'
            with open(str(c), 'wb') as f:
                f.write(data)

        r_ = td / 'request.json_'
        with open(str(r_), 'w') as f:
            json.dump(rd, f)

        r = td / 'request.json'
        r_.rename(r)

        return r, c

    def file_request(self, verb='get', endpoint=None, path=None, params=None, json_=None, data=None):
        """Call CONSTELLATION's REST API over files in a directory.

        Do not specify both json_ and data.
        """

        td = Path(self.file_dest)
        self._write_files(td, verb=verb, endpoint=endpoint, path=path, params=params, json_=json_, data=data)

        # Poll for the response.
        #
        response = td / 'response.json'
        content = td / 'content.out'
        sleep = 0
        while sleep<30:
            # NFS is a truly terrible filesystem.
            # The obvious "response.is_file()" doesn't work here.
            #
            if 'response.json' in os.listdir(td):
                break
            sleep += 1
            time.sleep(1)

        # We have response.json.
        # If content.out exists, get it and delete it.
        # If content.out doesn't exist, not a problem.
        #
        return FileResponse(str(response), str(content))

    def sftp_request(self, verb='get', endpoint=None, path=None, params=None, json_=None, data=None):
        """Call CONSTELLATION's REST API over sftp.

        Do not specify both json_ and data.
        """

        td = Path(self.tmpdir.name)
        r, c = self._write_files(td, verb=verb, endpoint=endpoint, path=path, params=params, json_=json_, data=data)

        sftp_batch = str(td / 'sftp.batch')
        with open(sftp_batch, 'w') as f:
            print(f'cd {self.sftp_dir}', file=f)
            if c:
                print(f'put {c} content.in', file=f)
            print(f'put {r} request.json_', file=f)
            print('rename request.json_ request.json', file=f)

        # If sftp fails here, an exception will be raised because check=True.
        # This is where we find out if ssh is configured correctly.
        # We don't need to specify the directory here, because sftp does a cd.
        #
        subprocess.run(['sftp', '-b', sftp_batch, f'{self.sftp_host}:'], check=True)

        # Poll for the response.
        #
        response = td / 'response.json'
        content = td / 'content.out'
        with open(sftp_batch, 'w') as f:
            print(f'cd {self.sftp_dir}', file=f)
            print(f'get response.json {response}', file=f)
        sleep = 0
        while sleep<30:
            s = subprocess.run(['sftp', '-b', sftp_batch, self.sftp_host])
            if s.returncode==0:
                break
            sleep += 1
            time.sleep(1)

        # We have response.json, delete it at the other end.
        # If content.out exists, get it and delete it at the other end.
        # If content.out doesn't exist, let the batch file fail.
        #
        with open(sftp_batch, 'w') as f:
            print(f'cd {self.sftp_dir}', file=f)
            print('rm response.json', file=f)
            print(f'get content.out {content}', file=f)
            print('rm content.out', file=f)
        subprocess.run(['sftp', '-b', sftp_batch, self.sftp_host])

        return FileResponse(str(response), str(content))

    def get_data(self, params):
        """Get data from the graph as specified by the params dictionary.

        :param params: Parameters to be passed to requests.get().
        See the external scripting documentation for individual parameters.

        Raises requests.exceptions.ConnectionError if the CONSTELLATION
        external scripting server is not running.

        :returns: The data as a string.
        """

        #    # Check that there is a currently active graph.
        #    #
        #    r = requests.get('http://localhost:1517/v1/graph/schema')
        #    if r.status_code != requests.codes.ok:
        #        r.raise_for_status()
        #    data = json.loads(r.text)
        #
        #    if not data['id']:
        #        print('There is no active graph.')
        #        return None
        if 'attrs' in params and params['attrs']:
            attrs = params['attrs']
            if isinstance(attrs, list):
                params['attrs'] = ','.join(attrs)

        # Fetch the graph data.
        #
        r = self.rest_request(endpoint='/v1/recordstore', path='get', params=params)

        return r.text

    def get_json(self, params):
        """Get a Python data structure from the graph as specified by
        the params dictionary.

        The data is read using get_data() and converted to a data
        structure using json.loads(), which can be slow for large amounts
        of data. Use get_dataframe() for its faster JSON parser.

        :returns: A Python data structure created by treating the data
        as JSON.
        """

        data = self.get_data(params)

        return json.loads(data)

    def get_dataframe(self, **kwargs):
        """Get a Pandas DataFrame from the current or specified graph.

        By default, all vertices and transactions, and all attributes
        of those elements, will be fetched. The vx and tx boolean parameters
        can be used to only fetch vertices or transactions. (Note that
        transactions include the vertices at their ends.)

        By default, all attributes are fetched. This can be resource
        consuming, so use of the attrs parameter to fetch only required
        attributes is encouraged.

        The data in the DataFrame will be converted to the correct types
        where possible. In particular, the CONSTELLATION types boolean,
        datetime, float, integer will have their respective types;
        color will be converted to a list of three floats.
        All other types will be strings.

        The DataFrame may change a type. For example, an integer column
        that contains null values will become a float64 containing NaNs,
        because int64 has no null representation.

        The JSON from the graph is parsed using the Pandas parser,
        which is significantly faster than json.loads().

        The CONSTELLATION types are made available in the self.types
        attribute. This is a dictionary containing a mapping from
        column names to CONSTELLATION type names.

        :param selected: If True, include only selected graph elements.
        :param vx: If True, include only vertices.
        :param tx: If True, include only transactions.
        :param attrs: A list of attribute names. If specified, only the
            listed attributes will be fetched.
        :param graph_id: The id of the graph to get data from.

        :returns: A DataFrame containing the requested data.
        """

        data = self.get_data(kwargs)

        # We can't create a DataFrame if there is no data.
        #
        if data:
            df = pd.read_json(data, orient='split', dtype=False, convert_dates=False)

            self.types = self._fix_types(df)
            return df
        else:
            self.types = {}
            return pd.DataFrame()

    def _fix_types(self, df):
        """Collect the type names and prepare them for renaming the columns.

        Use the type names in the column headers to fix datetime columns."""

        types = {}
        rename_dict = {}
        for c in df.columns:
            n,t = c.split('|')
            types[n] = t
            rename_dict[c] = n
            if t=='datetime':
                df[c] = pd.to_datetime(df[c])

        # Remove the type suffix from the column names.
        #
        df.rename(columns=rename_dict, inplace=True)

        return types

    def put_dataframe(self, df, **params):
        """Add the contents of a Pandas DataFrame to the current or specified graph.

        :param df: The DataFrame to send to CONSTELLATION.
        :param complete_with_schema: By default, CONSTELLATION will update
        the attributes of an element to match the graph's schema.
        Specify False to make this not happen.
        :param arrange: By default, CONSTELLATION will perform a default
        arrangement. Specify the name of an arrangement plugin to be
        performed instead of the default, or specify the empty string ''
        to not perform an arrangement.
        :param reset_view: By default, CONSTELLATION will reset the view.
        Specify False to not do this.
        :param graph_id: The id of the graph to be updated.
        """

        j = df.to_json(orient='split', date_format='iso')
        self.rest_request(verb='post', endpoint='/v1/recordstore', path='add', data=j.encode('utf-8'), params=params)

    def get_attributes(self, graph_id=None):
        """Get the graph, node, and transaction attributes of the current or specified graph.

        :param graph_id: If specified, the id of the graph from which to get the
            attributes.
        """

        params = {}
        if graph_id:
            params = {'graph_id':graph_id}

        r = self.rest_request(endpoint='/v1/graph', path='getattrs', params=params)

        return r.json()

    def get_graph_attributes(self, graph_id=None):
        """Get the graph attribute values."""

        params = {}
        if graph_id:
            params = {'graph_id':graph_id}

        r = self.rest_request(endpoint='/v1/graph', path='get', params=params)

        df = pd.read_json(r.text, orient='split', dtype=False, convert_dates=False)

        self._fix_types(df)

        return df

    def set_graph_attributes(self, df, graph_id=None):
        """Set graph attributes.

        :param graph_id: If specified, the graph to get attributes from,
            or the active graph if not specified.
        """

        params = {}
        if graph_id:
            params = {'graph_id':graph_id}

        j = df.to_json(orient='split', date_format='iso')
        self.rest_request(verb='post', endpoint='/v1/graph', path='set', params=params, data=j.encode('utf-8'))

    def set_current_graph(self, graph_id):
        """Make the specified graph the currently active graph."""

        self.rest_request(verb='put', endpoint='/v1/graph', path='current', params={'graph_id':graph_id})

    def open_graph(self, filename):
        """Open a graph from the file system"""

        self.rest_request(verb='post', endpoint='/v1/graph', path='open', params={'filename':filename})

    def new_graph(self, schema=None):
        """Open a new graph using the given schema.

        Opening a new graph is an asynchronous operation; this convenience
        function opens a new graph and waits until it is ready to be used.

        The default schema is whatever CONSTELLATION's default schema is.

        :param schema: The optional schema of the new graph.
        :param get_id: Get the id of the new graph.

        :returns: The id of the new graph.
        """

        params = {}
        if schema:
            params['schema'] = schema

        r = self.rest_request(verb='post', endpoint='/v1/graph', path='new', params=params)

        r = self.rest_request(endpoint='/v1/graph', path='schema')
        data = json.loads(r.text)
        id = data.get('id', '')

        return id

    def get_graph_image(self):
        """Get the visualisation of the current active graph as an image encoded in PNG format.

        :returns: The PNG-encoded bytes.
        """

        r = self.rest_request(endpoint='/v1/graph', path='image')

        return r.content

    def run_plugin(self, name, args=None, *, graph_id=None):
        """Run the specified plugin.

        Plugin names are case-insensitive.

        :param name: The name of the plugin to run
        :param args: The arguments to be passed to the plugin; a dictionary
            in the form {parameter_name:value, ...}.
        :param graph_id: The id of the graph to run the plugin on,
            or the active graph if not specified.
        """

        if args is None:
            args = {}
        if not isinstance(args, dict):
            raise ValueError('args must be a dictionary')

        self.rest_request(verb='post', endpoint='/v1/plugin', path='run', params={'name': name, 'graph_id':graph_id}, json_=args)

    def list_plugins(self, alias=True):
        """List the available plugins.

        :param alias: If True, list the plugins by alias rather than
            fully qualified name."""

        r = self.rest_request(endpoint='/v1/plugin', path='list', params={'alias': alias})

        data = json.loads(r.text)

        return data

    def list_graphs(self):
        """List the open graphs."""

        r = self.rest_request(endpoint='/v1/graph', path='schema_all')

        data = json.loads(r.text)

        return data

    def describe_type(self, name):
        """Describe the specified CONSTELLATION type.

        :param name: The name of the type to describe.

        :returns: A dictionary describing the named CONSTELLATION type."""

        r = self.rest_request(endpoint='/v1/type', path='describe', params={'type': name})

        return r.json()

    def list_icons(self, editable=False):
        """List the icons known by CONSTELLATION.

        :param editable: If True, include user icons."""

        r = self.rest_request(endpoint='/v1/icon', path='list', params={'editable':editable})

        return r.json()

    def get_icon(self, name):
        """Get the named icon in PNG format.

        :param name: The name of the icon to get."""

        r = self.rest_request(endpoint='/v1/icon', path='get', params={'name':name})

        return r.content

def _get_rest(rest=None):
    """Get data from the file created by the CONSTELLATION HTTP REST server.

    :param rest: The file to read the REST secret from. The CONSTELLATION default filename is used if not specified.
    """

    if not rest:
        rest = os.path.join(os.path.expanduser('~'), '.CONSTELLATION', 'rest.json')

    try:
        with open(rest) as f:
            data = json.load(f)
    except FileNotFoundError:
        print('REST file {} not found'.format(rest), file=sys.stderr)
        data = {}
    except json.decoder.JSONDecodeError as e:
        print('Error decoding REST JSON: {}'.format(e), file=sys.stderr)
        data = {}

    return data

def _row_dict(row, names, prefix):
    """Extract the relevant names/values from a DataFrame row and convert them
    to a dictionary without the prefixes."""

    L = len(prefix)
    r = row.to_dict()
    d = {}
    for name in names:
        d[name[L:]] = r[name]

    return d

def nx_from_dataframe(df, g=None, src_col=None, dst_col=None):
    """Create a networkx Graph from a DataFrame returned from
    constellation_client.get_dataframe().

    All source., destination., and transaction. column values will be
    set as attributes on their respective source nodes, destination
    nodes, and transactions.

    The source.[id] and destination.[id] values must be the integers
    provided by CONSTELLATION. By default, these are used for networkx
    node ids.

    :param df: A DataFrame.
    :param g: If defaulted to None, a new DiGraph is created,
    otherwise an existing graph is used.
    :param src_col: If specified, the values in this DataFrame column
    are used for the networkx source node ids.
    :param dst_col: If specified, the values in this DataFrame column
    are used for the networkx destination node ids.

    :returns: A networkx.Graph() representing the CONSTELLATION graph
    in the DataFrame.
    """

    if g is None:
        g = nx.DiGraph()

    snames = [i for i in df.columns if i.startswith('source.')]
    dnames = [i for i in df.columns if i.startswith('destination.')]
    tnames = [i for i in df.columns if i.startswith('transaction.')]

    SID = 'source.[id]'
    DID = 'destination.[id]'

    for i in range(len(df)):
        row = df.iloc[i]
        sid = row[SID]
        did = row[DID]
        if np.isnan(did):
            # This is a singleton.
            #
            if src_col:
                sid = row[src_col]
            g.add_node(sid, **_row_dict(row, snames, 'source.'))
        else:
            if src_col:
                sid = row[src_col]
            if sid not in g:
                g.add_node(sid, **_row_dict(row, snames, 'source.'))

            if dst_col:
                did = row[dst_col]
            if did not in g:
                g.add_node(did, **_row_dict(row, dnames, 'destination.'))

            g.add_edge(sid, did, **_row_dict(row, tnames, 'transaction.'))

    return g

def get_nx_pos(g):
    """Get x,y positions from a NetworkX CONSTELLATION graph.

    The returned dictionary is suitable to pass to networkx.draw(g, pos)."""

    pos = {}
    for n in g.nodes():
        x = g.node[n]['x']
        y = g.node[n]['y']
        pos[n] = x, y

    return pos
