# Import/Export JDBC - Connection

Java Database Connectivity (JDBC) is an application program interface
specification for connecting programs written in Java to the data in
popular databases.

Note: currently the only supported JDBC database is MySQL.

The JDBC connection panel collects the information required to connect
to the JDBC database that you are going to export to or import from. You
can manually enter the information, or you can load it from a previous
saved session.

-   Username: the JDBC username
-   Password: the JDBC password
-   Connection URL: The JDBC URL that will be used to connect to the
    database. This is typically of the form
    [jdbc:mysql://*server*/*schema*]{.mono}.
-   JDBC JAR file: a JAR file containing the driver implementation of
    the database.
-   JDBC driver: A list of JDBC drivers in the JAR file. Select the
    appropriate driver for your database.\
    After the JAR file is chosen, the driver is first looked up in
    [META-INF/services/java.sql.Driver]{.mono}. If no driver definition
    is found, every class in the JAR file is instantiated looking for a
    class compatible with [java.sql.Driver]{.mono}.

The Load button presents a list of saved import/export sessions.
Selecting a saved session will load the details (except for the
password, which you still have to enter manually).

When exporting, the node and transaction tables must already exist.
(This is obviously the case when importing.) The \"Display node SQL\"
and \"Display transaction SQL\" buttons will each display an SQL
\"CREATE TABLE\" statement that can be used to create the tables. The
SQL is not guaranteed to be correct: for instance, graph attributes
\"fg_icon\" and \"fg.icon\" will both result in a column name of
\"fgicon\". It is up to you to ensure that the SQL will work with your
database.

The node table must include an INT column that will hold the node id.
(This will typically be the primary key.)

The transaction table must include an INT column that will hold the
transaction id. (This will typically be the primary key.) The
transaction table must also include an INT column for the source node
and an INT column for the destination node. (These will typically be
foreign keys.) A boolean directed column is optional.

The SQL data types used must be compatible with the graph attribute data
types. For example, the attribute \"selected\" is boolean, which maps to
INT. However, you may choose to use TINYINT, CHAR(1), or some other
compatible SQL data type.

The tables below shows the JDBC API used for each graph data type. Note
that for the graph attribute types not specifically shown, import/export
makes use of each attribute type\'s ability to read and write its own
data type as a string. For instance, the color type will write a string
value when exporting that it can read back when importing.

### Export

  Attribute type    JDBC API
  ----------------- --------------------------------------
  boolean           [ResultSet.updateBoolean()]{.mono}
  date              [ResultSet.updateDate()]{.mono}
  datetime          [ResultSet.updateTimestamp()]{.mono}
  float             [ResultSet.updateFloat()]{.mono}
  integer           [ResultSet.updateInt()]{.mono}
  string            [ResultSet.updateString()]{.mono}
  time              [ResultSet.updateTime()]{.mono}
  all other types   [ResultSet.updateString()]{.mono}

  : The JDBC API used to export for each attribute type

### Import

  Attribute type    JDBC API
  ----------------- -----------------------------------
  boolean           [ResultSet.getBoolean()]{.mono}
  date              [ResultSet.getDate()]{.mono}
  datetime          [ResultSet.getTimestamp()]{.mono}
  float             [ResultSet.getFloat()]{.mono}
  integer           [ResultSet.getInt()]{.mono}
  string            [ResultSet.getString()]{.mono}
  time              [ResultSet.getTime()]{.mono}
  all other types   [ResultSet.getString()]{.mono}

  : The JDBC API used to import for each attribute type
