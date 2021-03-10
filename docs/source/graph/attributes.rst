Attributes
``````````

Attributes provide the means for the user to associate information with each node and transaction in a graph. The base application will automatically create a default set of attributes for the nodes and transactions. For example, an x, y and z node attributes are automatically created to support the coordinate position of each node.

Attributes may be created, deleted or modified by the user or by the plugins in a populated or empty graph.

Each attribute is defined by a name, description, type and a default value. The set of attribute types include.

The different types of attributes

.. csv-table::
   :header: "Type","Description"

   "boolean","A true or false value."
   "color","A color. Some colors have names, other are represented by red/green/blue/alpha values between 0 and 1."
   "date","A date with the representation yyyy-mm-dd."
   "datetime","A zoned datetime with the representation yyyy-mm-dd hh:mm:ss Z."
   "float","A floating point number."
   "hyperlink","A URL or URI."
   "icon","A small image used for foreground and background pictures on nodes, as well as node decorators."
   "integer","An integer number."
   "local_datetime","A datetime with the representation yyyy-mm-dd hh:mm:ss."
   "long","A long number."
   "object","A value that doesn't fit any of the other types."
   "string","Text."
   "time","A time with representation hh:mm:ss."
   "time_zone","A timezone for use with zoned datetimes."


.. help-id: au.gov.asd.tac.constellation.graph.attributes
