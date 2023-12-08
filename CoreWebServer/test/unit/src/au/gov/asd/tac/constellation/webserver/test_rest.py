
"""A client for the CONSTELLATION external scripting API."""

import pandas as pd

# Add the directory containing the internal file to the import path.
#
cc_path = '../../../../../../../../../../../CoreUtilities/src/au/gov/asd/tac/constellation/utilities/webserver'
import sys
sys.path.append(cc_path)
import constellation_client

def _test_get(cc):
    #data, types = get_dataframe(keys='source.[id],source.icon,destination.[id],destination.icon,transaction.DateTime,transaction.Count')
    data = cc.get_dataframe(selected=True)

    if data is not None:
        print(data)
        print(data.columns)
        print(data.dtypes)
        print(cc.types)

def _test_post():
    import datetime
    columns = ['source.Name', 'destination.Name', 'transaction.Type', 'transaction.DateTime']
    data = [
        ['1.2.3.4<IP Address>', '5.6.7.8<IP Address>', 'Online Location', datetime.datetime(2016, 1, 1, 2, 3, 4, 0)],
        ['4.3.2.1<IP Address>', '9.8.7.5<IP Address>', 'Online Location', datetime.datetime(2016, 1, 1, 3, 4, 5, 0)]
    ]
    df = pd.DataFrame(columns=columns, data=data)
    cc.put_dataframe(df, arrange='')
    cc.run_plugin('ArrangeInTrees')
    cc.run_plugin('ResetView')

    cc.run_plugin('SelectAll')
    cc.run_plugin('DeleteSelection')

if __name__=='__main__':
    cc = constellation_client.Constellation()
    id = cc.new_graph()
    print('Created new graph {}'.format(id))
    _test_post(cc)
