# Types

Nodes and transactions in Constellation can be assigned a type, which is
used to both group these elements according to the type of data they
represent, as well as to allow the graph to infer various properties
about each element. Types are hierarchical, meaning that each type could
have a parent type, as well as one or more children type. Generally,
higher level types describe broad categories of data, while lower level
types are more specific.

## Node Types

Node types identify a node as belonging to a particular category of
data, such as "Document" or "Person". Each node type holds the following
properties:

-   *Name* - the name assigned to this type
-   *Description* - a description of what the type is
-   *Colour* - the colour which should be set for nodes of this type
-   *Foreground Icon* - the foreground icon which should be set for
    nodes of this type
-   *Background Icon* - the background icon which should be set for
    nodes of this type
-   *Detection Regex* - the regular expression which can be used to
    detect identifiers of this type
-   *Validation Regex* - the regular expression which can be used to
    check if vertices of this type are valid
-   *Super Type* - the immediate parent type of this type in the type
    hierarchy
-   *Overridden Type* - the type that this type overrides in the type
    hierarchy
-   *Properties* - a map of additional properties of this type which are
    not covered by the above default properties

## Transaction Types

Transaction types identify a transaction as describing a particular
category of relationship, such as "Correlation" or "Communication". Each
transaction type holds the following properties:

-   *Name* - the name assigned to this type
-   *Description* - a description of what the type is
-   *Colour* - the colour which should be set for transactions of this
    type
-   *Style* - the line style which should be set for transactions of
    this type
-   *Directed* - whether or not transactions of this type should be
    directed by default
-   *Super Type* - the immediate parent type of this type in the type
    hierarchy
-   *Overridden Type* - the type that this type overrides in the type
    hierarchy
-   *Properties* - a map of additional properties of this type which are
    not covered by the above default properties
