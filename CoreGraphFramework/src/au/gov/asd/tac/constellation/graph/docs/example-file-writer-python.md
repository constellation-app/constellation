# Example Constellation File Writer

The Constellation file format uses JSON to store data. One of the
advantages of using JSON is that a .star file can be manipulated using
any language that has a JSON library.

This section will use Python 2.6 to provide examples of writing .star
files.

## Writing a .star file

Suppose we have some data that we want to use to create a graph that can
be displayed in Constellation. The easiest way of importing the data is
probably to import it in CSV format. Another way would be to use the
built-in scripting capability to directly build the graph from the data.

However, here we will create a JSON document from scratch that, when
properly saved, can be loaded directly into Constellation.

Because JSON corresponds closely with Python's built-in dictionary and
list data types, we can use idiomatic Python to build the graph, then
save it using the "json.dumps()" method.

            #
            # Write a CONSTELLATION .star file.
            #

            from __future__ import division
            from __future__ import print_function

            import json
            import math
            import pprint
            import sys
            import zipfile

            def attr(label, type, descr, default=None):
                a = {
                    #'mod_count': 0,
                    'label': label,
                    'type': type,
                    'descr': descr
                }
                if default is not None:
                    a['default'] = default

                return a

            def create_graph(vertexCount):
                '''Create a graph containing vertexCount vertices.'''

                incAngle = 360 / vertexCount
                radius = (vertexCount*3) / (2 * math.pi)

                vx_attrs = [
                    attr('x', 'float', 'x coord', 0),
                    attr('y', 'float', 'y coord', 0),
                    attr('z', 'float', 'z coord', 0),
                    attr('Name', 'string', 'Node name'),
                    attr('background_icon', 'icon', 'icon'),
                    attr('color', 'color', 'color')
                ]

                tx_attrs = []

                vx_data = []
                for i in range(vertexCount):
                    name = 'Node %d' % i
                    x = radius * math.cos(math.radians(i * incAngle))
                    y = radius * math.sin(math.radians(i * incAngle))
                    color = 'Yellow' if x>0 else 'Blue'
                    vx = {
                        'vx_id_': i,
                        'Name': name,
                        'x': x,
                        'y': y,
                        'z': 0,
                        'background_icon': 'Background.Round Circle',
                        'color': color
                    }
                    vx_data.append(vx)

                tx_data = []
                for i in range(vertexCount):
                    src = i
                    dst = i+1 if i<(vertexCount-1) else 0
                    tx = {
                        'vx_src_': src,
                        'vx_dst_': dst,
                        'tx_dir_': True
                    }
                    tx_data.append(tx)

                labels_data = {'bottom' : [{'attr':'Name', 'color':'Green'}]}

                graph = [
                    {'version':1, 'global_mod_count':0, 'structure_mod_count': 0, 'attribute_mod_count': 0},
                    #{'version':1},
                    {'graph': [{'attrs':[attr('color', 'color', 'color', 'Black')]}, {'data':[]}]},
                    {'vertex': [{'attrs':vx_attrs}, {'data':vx_data}]},
                    {'transaction': [{'attrs':tx_attrs}, {'data':tx_data}]},
                    {'meta': [{'attrs':[attr('labels', 'labels', 'labels')]}, {'data':[{'labels':labels_data}]}]}
                ]

                return graph

            def write_file(graph, fnam):
                f = open(fnam, 'wb')
                doc = json.dumps(graph, indent=2, encoding='UTF-8')
                f.write(doc)
                f.close()

            def write_graph(graph, fnam):
                zf = zipfile.ZipFile(fnam, 'w')
                try:
                    doc = json.dumps(graph, indent=2, encoding='UTF-8')
                    #print(doc)
                    zf.writestr('graph.txt', doc)
                finally:
                    zf.close()

            if __name__=='__main__':
                graph = create_graph(10)
                #pprint.pprint(graph, indent=2)

                write_graph(graph, 'circle.star')
                write_file(graph, 'graph.txt')
            
