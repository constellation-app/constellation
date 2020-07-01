Types
`````

Nodes and transactions in CONSTELLATION can be assigned a type, which is used to both group these elements according to the type of data they represent, as well as to allow the graph to infer various properties about each element. Types are hierarchical, meaning that each type could have a parent type, as well as one or more children type. Generally, higher level types describe broad categories of data, while lower level types are more specific.

Node Types
``````````

Node types identify a node as belonging to a particular category of data, such as 'Document' or 'Person'. Each node type holds the following properties...

* *Name:* the name assigned to this type.
* *Original Value:* the string value used to create this type (this does not always apply).
* *Colour:* the colour which should be set for nodes of this type.
* *Foreground Icon:* the foreground icon which should be set for nodes of this type.
* *Background Icon:* the background icon which should be set for nodes of this type.
* *Regex:* the regular expression which can be used to match identifiers of this type.
* *Subtypes:* the collection of all child types of this type in the type hierarchy.
* *Supertype:* the immediate parent type of this type in the type hierarchy.
* *Top-level Type:* the highest level type which this type falls under in the type hierarchy.
* *Properties:* a map of additional properties of this type which are not covered by the above default properties.

Transaction types identify a transaction as describing a particular category of relationship, such as 'Talked to' or 'Meeting with'. Each transaction type holds the following properties...

* *Name:* the name assigned to this type.
* *Colour:* the colour which should be set for transactions of this type.
* *Style:* the line style which should be set for transactions of this type.
* *Directed:* whether or not transactions of this type should be directed by default.
* *Subtypes:* the collection of all child types of this type in the type hierarchy.
* *Supertype:* the immediate parent type of this type in the type hierarchy.
* *Top-level Type:* the highest level type which this type falls under in the type hierarchy.


.. help-id: au.gov.asd.tac.constellation.graph.types
