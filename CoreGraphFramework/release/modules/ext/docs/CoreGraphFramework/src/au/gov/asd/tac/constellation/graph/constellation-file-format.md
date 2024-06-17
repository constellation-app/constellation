# The Constellation File Format

Constellation saves graphs in a ".star" file. This is simply a container
in zip format that holds the graph file "graph.txt" and optionally other
files, such as node icons.

This file format is also used when a graph is exported to JSON. There
are two differences between saving and exporting:

-   A graph is saved in a zip file with the JSON representation in a zip
    file entry called "graph.txt". A graph is exported to a file called
    "name.json", with ancillary files named using UUIDs.
-   A saved graph (in a .star file) does not contain entries for element
    values where the values are equal to the element's default value.
    For instance, if the element "color" has a default of "red", then
    elements where the color value is red will not have that value
    written to the file. This can result in significant space saving
    when writing and when the file is read, can result in a significant
    time saving. An exported graph will contain all values of all
    elements.

Other than these two differences, the description below applies to both
variations.

## graph.txt (or name.json)

The "graph.txt" (or "name.json") file contains data in UTF-8 encoded
JSON format that represents a graph. To extract the data manually, an
unzip utility can be used. The simplest way of doing this in Windows is
to append ".zip" to the filename (so the file type is recognised) and
double-click on the file in Windows Explorer.

Because the storage format is JSON, any language with a JSON parser can
read the graph. In particular, Python can be used to read and write a
JSON file in a zip container.

The output shown below from a Python script demonstrates the top-level
structure of a JSON document that describes a Constellation graph:

                [ { u'attribute_mod_count': 65,
                    u'global_mod_count': 591,
                    u'schema': u'au.gov.asd.tac.constellation.schema.InteractiveSchemaFactory',
                    u'structure_mod_count': 65,
                    u'version': 1},
                  { u'graph': [{ u'attrs': [...]}, { u'data': [...]}]},
                  { u'vertex': [{ u'attrs': [...]}, { u'data': [...]}]},
                  { u'transaction': [{ u'attrs': [...]}, { u'data': [...]}]},
                  { u'meta': [{ u'attrs': [...]}, { u'data': [...]}]}]
            

Furthermore, Constellation encodes the JSON document in "pretty-printed"
style, so it is possible to use simple tools such as grep(1) to search
for text.

## graph.txt (or name.json) Sections

The outermost structure of the graph is an ordered list containing five
elements. Each element (apart from "version") contains a dictionary with
a single key defining the graph section contained in that element. The
sections must appear in the following order:

1.  *version* - An integer version number that defines the remaining
    structure. This section may also contain other unspecified data.
2.  *graph* - Contains data relevant to the graph (e.g. background
    color).
3.  *vertex* - Contains data relevant to the nodes (e.g. the name of the
    node).
4.  *transaction* - Contains data relevant to the transactions (e.g.
    line style).
5.  *meta* - Contains data about the graph environment (e.g. the
    attributes used describe View states).

Why do it like this? Why not just a use a top-level dictionary
containing the section keys? Unlike XML, JSON objects contain unordered
name/value pairs. This means that (for example) when a Python dictionary
is serialised, the "transaction" key might appear before the "vertex"
key in the resulting JSON. Since transactions can't be added to the
graph before their corresponding nodes, this would require that the
entire structure be loaded into memory to ensure that "vertex" could be
accessed first, which would not be a good idea for large graphs.

By making the top-level structure a JSON array with a specified order,
the data can be streamed from the file in the required order, making
graph reading more efficient.

Given that the order is defined, putting a JSON object with a single
name in each element is superfluous. However, the name provides a
built-in level of documentation, making the file (slightly) more
readable, and adds little overhead when reading and writing the file.

Each graph section "graph", "vertex", "transaction", "meta" contains
data with the same format: a list of two single-named objects.

(The structure here is a list of single-named dictionaries, rather than
a single dictionary with two names, for the same reason that the
top-most structure is a list of single-named dictionaries: the
attributes must be defined before they can be used):

-   *attrs* - Defines the attributes used by this section of the graph.
    The value of this object is an array of objects, where each object
    is an attribute definition.
-   *data* - Defines the data in this section of the graph. The value of
    this object is an array of objects, where each object is a graph
    datum corresponding to the current section of the document.

If an object does not define a name, the value of that name is assumed
to be null. e.g. if the vertex "attrs" section defines a "Country"
attribute, and an object in the vertex "data" section has no "Country"
name, then the resulting graph will have a "Country" value of null.

## Attributes

Attributes define the data values that are attached to elements of the
graph. Each attribute has four components:

-   *label* - The name of the attribute.
-   *type* - The type of attribute (see below).
-   *descr* - A description of the attribute. (Optional)
-   *default* - The default value. (Optional)

Attributes are defined separately in each graph section; an attribute
defined in the "vertex" section cannot be used in the "transaction"
section.

## Data Types

Constellation defines some built-in data types. These are listed below.

All data type values have string representations so they can be
round-tripped from their internal representation, to a JSON document
when saved, and back to their internal representation when loaded
(although some floating point numbers may not be retrieved exactly due
to the inexactness inherent in the string representation). (Obviously
this round-tripping would work for other string formats such as CSV):

-   *boolean* - A true/false value, represented by the strings "true"
    and "false".
-   *blaze* - A node marker. The string representation has the format
    "angle;color;icon;isDisplayed".
    -   angle - degrees clockwise from 12 o'clock
    -   color - see color type
    -   icon - see icon type
    -   isDisplayed - true or false indicating whether the blaze is
        displayed or not.

    e.g. "45;LightBlue;Sphere_48;true".
-   *color* - An RGBA color. This value has two representations: a name
    ("red", "green", "blue", ...), or a comma-separated list of four
    floating-point numbers between 0 and 1 inclusive representing the
    red, green, blue, and alpha components of the color. The alpha
    component is optional; if it is not present, it defaults to 1. For
    instance "red", "1,0,0" and "1,0,0,1" are equivalent, as are
    "DarkGreen" and "0,0.5,0". Note that the alpha component is often
    ignored by Constellation; for instance, an alpha component of 0.5 in
    a node color will not result in a semi-transparent background icon.
-   *date* - A date. The representation is ISO 8601 format:
    "YYYY-MMM-DD".
-   *datetime* - A datetime. The representation is ISO 8601 format with
    'T' replaced by ' ': "YYYY-MMM-DD hh:mm:ss". Datetime values are
    always UTC, but are displayed to the user in a timezone defined by
    the value of a graph attribute called "time_zone" of type
    "time_zone" which defaults to "UTC".
-   *float* - A floating point number. This is represented internally as
    a Java IEEE-754 32-bit float.
-   *icon* - The name of an icon. Note that icon names need not be fully
    qualified, so "Background.Round Circle" and "Round Circle" both
    represent the same icon.
-   *integer* - An integer. This is represented internally as a Java
    32-bit signed integer.
-   *string* - A text string.
-   *time* - A time. The representation is ISO 8601 format: "hh:mm:ss".
-   *time_zone* - A time zone. The representation is a string accepted
    by the Java TimeZone class. This is typically a string of the form
    "Canberra/Australia" or "GMT+10".

## "graph" Section

An example JSON document section is shown below:

                "graph" : [ {
                  "attrs" : [ {
                    "label" : "color",
                    "type" : "color",
                    "descr" : "The background color of the graph",
                    "default" : "Black",
                    "mod_count" : 0
                  }, {
                    "label" : "time_zone",
                    "type" : "time_zone",
                    "descr" : "time_zone",
                    "default" : "UTC",
                    "mod_count" : 0
                  } ]
                }, {
                  "data" : [ { } ]
                } ]
            

Attributes are defined in the "attrs" object as an array of objects, in
Python terms, a list of dictionaries. (Although arrays have order, no
ordering is imposed by Constellation.) Each object has four defined
name/value pairs in no particular order.

-   *label* - The user-visible name of the attribute.
-   *type* - The type of the attribute. See below for defined types.
-   *descr* - The user-visible description of the attribute.
-   *default* - The default value of the attribute

Data are stored in the "data" object. In this case, no data have been
stored, so the defaults defined in the attributes will be used (e.g. the
graph will use the default black background).

## "vertex" Section

A part of an example "vertex" section is shown below:

                "vertex" : [ {
                  "attrs" : [ {
                    "label" : "x",
                    "type" : "float",
                    "descr" : "The x coordinate of the vertex",
                    "default" : 0.0
                  }, {
                    "label" : "icon",
                    "type" : "icon",
                    "descr" : "The icon of the vertex",
                    "default" : ""
                  }, {
                    "label" : "Name",
                    "type" : "string"
                  } ]
                }, {
                  "data" : [ {
                    "vx_id_" : 0,
                    "x" : 9.760799,
                    "icon" : "Flag.Australia",
                    "Name" : "Node 0"
                  }, {
                    "vx_id_" : 1,
                    "x" : 0.22238255,
                    "icon" : "Misc.Constellation",
                    "Name" : "Node 1"
                  } ]
                } ]
            

In this example, three vertex attributes are defined in the "attrs"
section: "x" (type float), "icon" (type icon), and "Name" (type
string).  
NOTE: "Name" has no defined "descr" or "default" values, so these will
be null. Two vertices are defined in the "data" section with specific
values assigned to their attributes.

The vertices define values for a special attribute:

-   *vx_id\_* - An integer that is used as a key for each vertex in the
    file. The values need not be consecutive or ordered, but they must
    be unique.

## "transaction" Section

This section is optional. If there is no "transaction" section, there
will be no transactions in the resulting graph.

A part of an example "transaction" section is shown below.

                "transaction" : [ {
                  "attrs" : [ {
                    "label" : "color",
                    "type" : "color",
                    "descr" : "The color of the transaction"
                  }, {
                    "label" : "line_style",
                    "type" : "line_style",
                    "descr" : "The line style of the transaction",
                    "default" : "SOLID"
                  } ]
                }, {
                  "data" : [ {
                    "vx_src_" : 8,
                    "vx_dst_" : 9,
                    "tx_dir_" : true,
                    "color" : "0.27553296,0.79927653,0.2556097,1.0",
                    "line_style" : "SOLID",
                    "Datetime" : "2014-03-21 06:15:48.471",
                    "Id" : "6"
                  }, {
                    "vx_src_" : 5,
                    "vx_dst_" : 9,
                    "tx_dir_" : true,
                    "color" : "0.7740898,0.7625852,0.9571049,1.0",
                    "visibility" : 0.11111111,
                    "line_style" : "SOLID",
                    "Datetime" : "2014-03-22 04:42:31.216",
                    "Id" : "918"
                  } ]
                } ]
            

Two transaction attributes are defined in the "attrs" section: "color"
(type color), and "line_style" (type line_style).  
NOTE: "color" has no defined 'default' value, so transactions without an
explicitly defined color will be null. Two transactions are defined in
the 'data' section with specific values assigned to their attributes.

The transactions define values for three special attributes:

-   *vx_src\_* - The source vertex of the transaction. This must be one
    of the vx_id\_ integer values defined in the vertex section.
-   *vx_dst\_* - The destination vertex of the transaction. This must be
    one of the vx_id\_ integer values defined in the vertex section.
-   *tx_dir\_* - A boolean indicating whether the transaction is
    directed or undirected. If true, the transaction is directed.

## "meta" Section

This section is optional. If there is no "meta" section, defaults will
be used.

The "meta" section contains data about the graph's environment. The data
is defined by various Constellation modules which write their state to
the JSON document on save, and read their state from the JSON document
on open.

Although the "meta" section has the same "attrs"+"data" format as the
other sections, the attributes are defined by modules, rather than being
built-in types. For instance, the module that defines what filters and 
configurations to display in the Conversation View saves its state in the 
attribute "conversation\_view\_state" of type "conversation\_view\_state". 
When the document is opened, the Constellation graph opener will find a 
"conversation\_view\_state" attribute and advertise it to the current 
modules. The module responsible for conversation\_view\_state (Core 
Conversation View in this case) will recognise the attribute, claim it, 
and read its state.

The data section array contains a single object, with each key having a
name corresponding to an attribute name. For instance, the module
responsible for Conversation View state will have a 
"conversation\_view\_state" key in which its state is saved.

Generally, the format of the data used by individual modules is
documented by the modules, rather than Constellation itself. Some
modules may consider their data to be for internal use only, and not
document their format.
