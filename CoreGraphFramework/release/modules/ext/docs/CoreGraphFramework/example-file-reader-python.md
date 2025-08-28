# Example Constellation File Reader

The Constellation file format uses JSON to store data. One of the
advantages of using JSON is that a .star file can be manipulated using
any language that has a JSON library.

This section will use Python 2.6 to provide examples of reading .star
files.

## Reading a .star File

The Python script below (given the path of a .star file on the command
line) will read the JSON document from the .star file and display some
data from the JSON document. We'll use the output of this script to
investigate Constellation's JSON document format.

            #
            # Read the JSON document from a Constellation .star file.
            #

            from __future__ import print_function

            import json
            import pprint
            import sys
            import zipfile

            def read_graph(fnam):
                '''Read a Constellation graph from a .star file.'''

                try:
                    zf = zipfile.ZipFile(fnam, 'r')
                    bytes = zf.read('graph.txt')
                finally:
                    zf.close()

                json_data = json.loads(bytes, 'UTF-8')

                return json_data

            def graph_section(graph, section, sub=None):
                '''Given a graph, return the given section of the graph.
                If sub is also given, return that subsection.

                The section argument is one of 'graph', 'vertex', 'transaction', 'meta'.
                The sub argument is one of 'attrs', 'data'.
                Returns None if the section is not found.'''

                # Loop through the graph looking for the correct section.
                # We do it like this in case other sections are added in the future.
                #
                for sect in graph:
                    if section in sect:
                        if sub is None:
                            return sect[section]

                        for s in sect[section]:
                            if sub in s:
                                return s[sub]

            if __name__=='__main__':
                graph = read_graph(sys.argv[1])
                print('Graph structure:')
                pprint.pprint(graph, indent=2, depth=4)

                vertices = graph_section(graph, 'vertex')
                print('\nVertex section:')
                pprint.pprint(vertices, indent=2, depth=2)

                transactions = graph_section(graph, 'transaction')
                print('\nTransaction section:')
                pprint.pprint(transactions, indent=2, depth=2)

                vertex_data = graph_section(graph, 'vertex', sub='data')
                print('\nVertex data:')
                pprint.pprint(vertex_data[0], indent=2)
            

The output from the script (using a typical graph) is shown below:

            Graph structure:
            [ { u'attribute_mod_count': 65,
                u'global_mod_count': 591,
                u'schema': u'au.gov.asd.tac.constellation.graph.schema.analyticFactory',
                u'structure_mod_count': 65,
                u'version': 1},
              { u'graph': [{ u'attrs': [...]}, { u'data': [...]}]},
              { u'vertex': [{ u'attrs': [...]}, { u'data': [...]}]},
              { u'transaction': [{ u'attrs': [...]}, { u'data': [...]}]},
              { u'meta': [{ u'attrs': [...]}, { u'data': [...]}]}]

            Vertex section:
            [{ u'attrs': [...]}, { u'data': [...]}]

            Transaction section:
            [{ u'attrs': [...]}, { u'data': [...]}]

            Vertex data:
            { u'Country2': u'Canada',
              u'Date': u'2012-10-31',
              u'Datetime': u'2014-03-24 03:45:41.839',
              u'IsGood': True,
              u'Name': u'Node 0',
              u'Normalised': 0.8359702,
              u'Type': u'Online Identifier',
              u'Time': u'14:20:37',
              u'background_icon': u'Background.Flat Square',
              u'blaze': u'45;LightBlue;Sphere_48;false',
              u'color': u'0.25076652,0.5779746,0.37560248,1.0',
              u'icon': u'Person.Account',
              u'selected': True,
              u'visibility': 0.0,
              u'vx_id_': 0,
              u'x': 5.0,
              u'y': 5.0,
              u'z': 6.3245554000000004}
            

The basic top-level structure of the graph document (leaving aside the
first 'version' section) consists of lists of dictionaries with a single
key. The top level array contains 'graph', 'vertex', 'transaction', and
'meta', each containing a two-element list: a dictionary with the key
'attrs', and a dictionary with the key 'data'.

Suppose we want to find the names of nodes that have a secondary country
of 'Canada'. The following function will find those nodes. (The function
is shown in two variants: one using a list comprehension, and one using
long form. Use whichever variant you are most comfortable with.

            def find_vertex_country(graph, country):
                vertex_data = graph_section(graph, 'vertex', sub='data')

                # Use vx.get() rather than vx[]: if the key doesn't exist because
                # the value is null, we don't want a KeyError to be thrown.
                #
                names = [vx.get('Name', 'Unknown') for vx in vertex_data
                    if vx.get('Country')=='Canada']

                return names

            def find_vertex_country_2(graph, country):
                vertex_data = graph_section(graph, 'vertex', sub='data')
                names = []
                for vx in vertex_data:
                    if 'Country' in vx and vx['Country']=='Canada':
                        if 'Name' in vx:
                            name = vx['Name']
                        else:
                            name = 'Unknown'
                        names.append(name)

                return names
            

The output from both functions is (unsurprisingly) the same:

            [u'Node 1', u'Node 6']
            

NOTE: We use "vx.get" instead of using standard dictionary lookup
notation "vx\[\]". This is because a vertex attribute with a null value
is not written to the JSON document, and therefore does not have a key
in the Python dictionary when it is read. Attempting to read a
dictionary with a non-existent key results in a KeyError being thrown,
which isn't good for our output.

The documentation for get(key \[,default\]) says: "Return the value for
key if key is in the dictionary, else default. If default is not given,
it defaults to None, so that this method never raises a KeyError." This
is just what we want.

To find out which nodes do not have a country associated with them:

            def find_vertex_no_country(graph):
                vertex_data = graph_section(graph, 'vertex', sub='data')

                names = [vx.get('Name', 'Unknown') for vx in vertex_data
                    if 'Country' not in vx]

                return names
            

Output:

            [u'Node 0', u'Node 5']
            

These examples can be used as starting points for more complicated
queries, such as "all countries starting with 'U'", or "events between
two dates".

To find all of the countries that are associated with vertices in this
graph, we use a set to add the known countries, and then display a
sorted list.

            def find_unique_countries(graph):
                vertex_data = graph_section(graph, 'vertex', sub='data')

                countries = set()
                for vx in vertex_data:
                    if 'Country' in vx:
                        countries.add(vx['Country'])

                return countries

            countries = find_unique_countries(graph)
            print('\nNode countries:\n%s' % '\n'.join(sorted(countries)))
            

Output:

            Node countries:
            Australia
            Canada
            Sweden
            Germany
            India
            

Because the JSON document is designed for storage rather than graph
manipulation, it is more difficult to find results for queries such as
"find all transactions with the same country at both ends".
