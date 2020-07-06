Extract Schema Types from Text
------------------------------

Identify schema type values within text and add them to your graph
``````````````````````````````````````````````````````````````````

Extract from Text cycles through the schema node types known by CONSTELLATION. For each schema type that has an associated regular expression, all matches for that regular expression are found in the text and added to the graph.

New nodes are added with the following attributes:

Source Node
```````````

* Seed - True


.. help-id: au.gov.asd.tac.constellation.views.dataaccess.plugins.importing.ExtractFromTextPlugin
