# Import/Export JDBC - Mapping

The mapping panel defines the mappings between table columns and graph
attributes.

The "Default mappings" button will automatically map columns and
attributes based on names. If two names are similar (after
non-alphanumeric characters are removed, and the names converted to
lower case, the names are equal), they will mapped by default. This does
not guarantee that mappings are unique; you must still check the
mappings for correctness.

Graph attributes that are not mapped to table columns will not be
exported. Likewise, table columns that are not mapped to graph
attributes will not be imported. On export, all table columns must
exist. On import, all graph attributes must exist.

When either exporting or importing, the graph node id must have a
mapping to a table column, and the graph transaction id, transaction
source, and transaction destination must have a mapping to a table
column. (The transaction directed attribute to indicate whether a
transaction is directed or not is optional. If the directed attribute is
not mapped on import, transactions are assumed to be directed.)

It is assumed that mapped table columns have the correct data type for
the mapped graph attribute. JDBC operations are done based on the
attribute type, not the column type.

The Save button can be used to save this export/import session. The data
in each of the panels will be saved, so you don't have to reenter most
information the next time you do an export/import. It is recommended
that you save your session in case of errors.

JDBC import/export sessions are saved in <span
class="mono">*HOME\_DIRECTORY*/.CONSTELLATION/ImportExportJDBC</span>.
Each file is saved JDBC session.

Selecting Finish will cause the export/import to be done.

When exporting, all graph elements will be exported, whether they are
selected or not.

When importing, the node ids in the table column will not be used to
create nodes in the graph. Instead, nodes will be added with the next
available node id. This avoids clashes with existing nodes. Transactions
will be added to the correct nodes. When the import is complete, the
current graph's schema will be applied. This may result in nodes being
merged.
