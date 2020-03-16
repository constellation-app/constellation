/*
 * Copyright 2010-2019 Australian Signals Directorate. All Rights Reserved.
 *
 * NOTICE: All information contained herein remains the property of the
 * Australian Signals Directorate. The intellectual and technical concepts
 * contained herein are proprietary to the Australian Signals Directorate and
 * are protected by copyright law. Dissemination of this information or
 * reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from the Australian Signals Directorate.
 */
package au.gov.asd.tac.constellation.testing.jdbc;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.file.io.GraphFileConstants;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLoggerHelper;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.gui.InfoTextPanel;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author algol
 */
//@ServiceProvider(service = Plugin.class)
@PluginInfo(pluginType = PluginType.EXPORT, tags = {"EXPORT"})
@NbBundle.Messages("ExportToJdbcPlugin=Export to JDBC")
public class ExportToJdbcPlugin extends SimpleReadPlugin {

    private static final Logger LOGGER = Logger.getLogger(ExportToJdbcPlugin.class.getName());
    private static final int BATCH_COUNT = 2000;
    private static final int INTERACT_COUNT = 1000;

    private final JdbcData data;

    ExportToJdbcPlugin(final JdbcData data) {
        this.data = data;
    }

    @Override
    public void read(final GraphReadMethods rg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        try {
            try (final Connection conn = JdbcUtilities.getConnection(new File(data.jar), data.driverName, data.url, data.username, data.password)) {
                boolean complete = writeVerticesToJdbcBatch(rg, conn, interaction);
                if (complete) {
                    complete = writeTransactionsToJdbcBatch(rg, conn, interaction);
                }

                if (complete) {
                    final String msg = String.format("JDBC export complete: %d vertices, %d transactions.", rg.getVertexCount(), rg.getTransactionCount());
                    interaction.setProgress(0, 0, msg, false);
                    ConstellationLoggerHelper.exportPropertyBuilder(
                            this,
                            GraphRecordStoreUtilities.getVertices(rg, false, false, false).getAll(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL),
                            null,
                            ConstellationLoggerHelper.SUCCESS
                    );
                } else {
                    interaction.setProgress(0, 0, "JDBC export interrupted, database may be inconsistent.", false);
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | MalformedURLException ex) {
                notifyException(ex);
//                throw new PluginException(PluginNotificationLevel.INFO, ex);
            }
        } catch (final SQLException ex) {
            notifyException(ex);
//            throw new PluginException(PluginNotificationLevel.INFO, ex);
        }
    }

    private boolean writeVerticesToJdbc(final GraphReadMethods rg, final Connection conn, final PluginInteraction interaction) throws SQLException, PluginException {
        final int start = 0;
        final int total = rg.getVertexCount() + rg.getTransactionCount();

        // Build an SQL SELECT to prime an updateable ResultSet.
        // We use a funky where clause because we don't want to fetch what's already there.
        // This is an SQL injection waiting to happen, so make a feeble attempt at mitigation...
        final StringBuilder select = new StringBuilder();
        select.append(" SELECT ");
        final Map<String, Attribute> labelMap = new HashMap<>();

        final int len = data.vxMappings[0].length;
        for (int i = 0; i < len; i++) {
            final String colLabel = data.vxMappings[0][i];
            JdbcUtilities.checkSqlLabel(colLabel);
            final String attrLabel = data.vxMappings[1][i];
            if (!(colLabel.isEmpty() || attrLabel.isEmpty())) {
                if (attrLabel.equals(GraphFileConstants.VX_ID)) {
                    labelMap.put(colLabel, null);
                } else {
                    final int attrId = rg.getAttribute(GraphElementType.VERTEX, attrLabel);
                    final Attribute attr = new GraphAttribute(rg, attrId);
                    labelMap.put(colLabel, attr);
                }

                select.append(colLabel);
                select.append(',');
            }
        }

        select.deleteCharAt(select.length() - 1);
        select.append(" FROM ");
        JdbcUtilities.checkSqlLabel(data.vxTable);
        select.append(data.vxTable);
        select.append(" WHERE 1<>1");
        LOGGER.log(Level.INFO, "JDBC export vx SQL: {0}", select.toString());

        if (!labelMap.isEmpty()) {
            try (final Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                try (final ResultSet rs = stmt.executeQuery(select.toString())) {

                    final int vxCount = rg.getVertexCount();
                    for (int position = 0; position < vxCount; position++) {
                        final int vxId = rg.getVertex(position);

                        rs.moveToInsertRow();
                        for (final Map.Entry<String, Attribute> entry : labelMap.entrySet()) {
                            final String label = entry.getKey();
                            final Attribute attr = entry.getValue();

                            if (attr == null) {
                                rs.updateInt(label, vxId);
                            } else {
                                updateResultSetParam(rg, rs, label, attr, vxId);
                            }
                        }

                        rs.insertRow();

                        if (position % INTERACT_COUNT == 0) {
                            try {
                                interaction.setProgress(start + position, total, String.format("Exported %d vertices", position), true);
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                                return false;
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    private boolean writeTransactionsToJdbc(final GraphReadMethods rg, final Connection conn, final PluginInteraction interaction) throws SQLException, PluginException {
        final int start = rg.getVertexCount();
        final int total = rg.getVertexCount() + rg.getTransactionCount();

        // Create some dummy attribute types as markers for the transaction pseudo-attributes.
        final int pseudoId = -9;
        final Attribute pseudo = new GraphAttribute(pseudoId, GraphElementType.TRANSACTION, "integer", "tx", "Pseudo tx", null, null);

        // Build an SQL SELECT to prime an updateable ResultSet.
        // We use a funky where clause because we don't want to fetch what's already there.
        // This is an SQL injection waiting to happen, so make a feeble attempt at mitigation...
        final StringBuilder select = new StringBuilder();
        select.append(" SELECT ");
        final Map<String, Attribute> labelMap = new HashMap<>();

        final int len = data.txMappings[0].length;
        for (int i = 0; i < len; i++) {
            final String colLabel = data.txMappings[0][i];
            JdbcUtilities.checkSqlLabel(colLabel);
            final String attrLabel = data.txMappings[1][i];
            if (!(colLabel.isEmpty() || attrLabel.isEmpty())) {
                if (attrLabel.equals(GraphFileConstants.TX_ID)
                        || attrLabel.equals(GraphFileConstants.SRC)
                        || attrLabel.equals(GraphFileConstants.DST)
                        || attrLabel.equals(GraphFileConstants.DIR)) {
                    labelMap.put(colLabel, pseudo);
                } else {
                    final int attrId = rg.getAttribute(GraphElementType.TRANSACTION, attrLabel);
                    final Attribute attr = new GraphAttribute(rg, attrId);
                    labelMap.put(colLabel, attr);
                }

                select.append(colLabel);
                select.append(',');
            }
        }

        select.deleteCharAt(select.length() - 1);
        select.append(" FROM ");
        JdbcUtilities.checkSqlLabel(data.txTable);
        select.append(data.txTable);
        select.append(" WHERE 1<>1");
        LOGGER.log(Level.INFO,"JDBC export tx SQL: {0}", select.toString());

        if (!labelMap.isEmpty()) {
            try (final Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                try (final ResultSet rs = stmt.executeQuery(select.toString())) {

                    final int txCount = rg.getTransactionCount();
                    for (int position = 0; position < txCount; position++) {
                        final int txId = rg.getTransaction(position);

                        rs.moveToInsertRow();
                        for (final Map.Entry<String, Attribute> entry : labelMap.entrySet()) {
                            final String label = entry.getKey();
                            final Attribute attr = entry.getValue();

                            if (attr.getId() == pseudoId) {
                                if (label.equals(GraphFileConstants.TX_ID)) {
                                    rs.updateInt(label, txId);
                                } else if (label.equals(GraphFileConstants.SRC)) {
                                    rs.updateInt(label, rg.getTransactionSourceVertex(txId));
                                } else if (label.equals(GraphFileConstants.DST)) {
                                    rs.updateInt(label, rg.getTransactionDestinationVertex(txId));
                                } else if (label.equals(GraphFileConstants.DIR)) {
                                    rs.updateBoolean(label, rg.getTransactionDirection(txId) != Graph.FLAT);
                                }
                            } else {
                                updateResultSetParam(rg, rs, label, attr, txId);
                            }
                        }

                        rs.insertRow();

                        if (position % INTERACT_COUNT == 0) {
                            try {
                                interaction.setProgress(start + position, total, String.format("Exported %d transactions", position), true);
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                                return false;
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    private boolean writeVerticesToJdbcBatch(final GraphReadMethods rg, final Connection conn, final PluginInteraction interaction) throws SQLException, PluginException {
        final int start = 0;
        final int total = rg.getVertexCount() + rg.getTransactionCount();

        final StringBuilder insert = new StringBuilder();
        final StringBuilder values = new StringBuilder();
        insert.append("INSERT INTO ");
        JdbcUtilities.checkSqlLabel(data.vxTable);
        insert.append(data.vxTable);
        insert.append("(");
        final ArrayList<Attribute> attrsToInsert = new ArrayList<>();

        final int len = data.vxMappings[0].length;
        for (int i = 0; i < len; i++) {
            final String colLabel = data.vxMappings[0][i];
            JdbcUtilities.checkSqlLabel(colLabel);
            final String attrLabel = data.vxMappings[1][i];
            if (!(colLabel.isEmpty() || attrLabel.isEmpty())) {
                final Attribute attr;
                if (attrLabel.equals(GraphFileConstants.VX_ID)) {
                    attr = null;
                } else {
                    final int attrId = rg.getAttribute(GraphElementType.VERTEX, attrLabel);
                    attr = new GraphAttribute(rg, attrId);
                }

                attrsToInsert.add(attr);

                insert.append(colLabel);
                insert.append(',');
                values.append("?,");
            }
        }

        insert.deleteCharAt(insert.length() - 1);
        values.deleteCharAt(values.length() - 1);
        insert.append(") VALUES(");
        insert.append(values.toString());
        insert.append(")");
        LOGGER.log(Level.INFO,"JDBC export vx SQL: {0}", insert.toString());

        if (!attrsToInsert.isEmpty()) {
            try (final PreparedStatement stmt = conn.prepareStatement(insert.toString())) {
                final int vxCount = rg.getVertexCount();
                for (int position = 0; position < vxCount; position++) {
                    final int vxId = rg.getVertex(position);

                    for (int i = 0; i < attrsToInsert.size(); i++) {
                        final Attribute attr = attrsToInsert.get(i);
                        final int paramIx = i + 1;

                        if (attr == null) {
                            stmt.setInt(paramIx, vxId);
                        } else {
                            setBatchParam(rg, stmt, paramIx, attr, vxId);
                        }
                    }

                    stmt.addBatch();

                    if (position > 0 && position % BATCH_COUNT == 0) {
                        stmt.executeBatch();
                        stmt.clearBatch();
                    }

                    if (position % INTERACT_COUNT == 0) {
                        try {
                            interaction.setProgress(start + position, total, String.format("Exported %d vertices", position), true);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            return false;
                        }
                    }
                }

                stmt.executeBatch();
            }
        }

        return true;
    }

    private boolean writeTransactionsToJdbcBatch(final GraphReadMethods rg, final Connection conn, final PluginInteraction interaction) throws SQLException, PluginException {
        final int start = rg.getVertexCount();
        final int total = rg.getVertexCount() + rg.getTransactionCount();

        // Create some dummy attribute types as markers for the transaction pseudo-attributes.
        final int pseudoId = -9;

        // Build an SQL SELECT to prime an updateable ResultSet.
        // We use a funky where clause because we don't want to fetch what's already there.
        // This is an SQL injection waiting to happen, so make a feeble attempt at mitigation...
        final StringBuilder insert = new StringBuilder();
        final StringBuilder values = new StringBuilder();
        insert.append("INSERT INTO ");
        JdbcUtilities.checkSqlLabel(data.txTable);
        insert.append(data.txTable);
        insert.append("(");
        final ArrayList<Attribute> attrsToInsert = new ArrayList<>();

        final int len = data.txMappings[0].length;
        for (int i = 0; i < len; i++) {
            final String colLabel = data.txMappings[0][i];
            JdbcUtilities.checkSqlLabel(colLabel);
            final String attrLabel = data.txMappings[1][i];
            if (!(colLabel.isEmpty() || attrLabel.isEmpty())) {
                final Attribute attr;
                if (attrLabel.equals(GraphFileConstants.TX_ID)
                        || attrLabel.equals(GraphFileConstants.SRC)
                        || attrLabel.equals(GraphFileConstants.DST)
                        || attrLabel.equals(GraphFileConstants.DIR)) {
//                    labelMap.put(colLabel, pseudo);
                    attr = new GraphAttribute(pseudoId, GraphElementType.TRANSACTION, "integer", attrLabel, "Pseudo tx", null, null);
//                    attrsToInsert.add(pseudo);
                } else {
                    final int attrId = rg.getAttribute(GraphElementType.TRANSACTION, attrLabel);
                    attr = new GraphAttribute(rg, attrId);
//                    labelMap.put(colLabel, attr);
//                    attrsToInsert.add(attr);
                }

                attrsToInsert.add(attr);

                insert.append(colLabel);
                insert.append(',');
                values.append("?,");
            }
        }

        insert.deleteCharAt(insert.length() - 1);
        values.deleteCharAt(values.length() - 1);
        insert.append(") VALUES(");
        insert.append(values.toString());
        insert.append(")");
        LOGGER.log(Level.INFO,"JDBC export tx SQL: {0}", insert.toString());

        if (!attrsToInsert.isEmpty()) {
            try (final PreparedStatement stmt = conn.prepareStatement(insert.toString())) {
                final int txCount = rg.getTransactionCount();
                for (int position = 0; position < txCount; position++) {
                    final int txId = rg.getTransaction(position);

                    for (int i = 0; i < attrsToInsert.size(); i++) {
                        final Attribute attr = attrsToInsert.get(i);
                        final int paramIx = i + 1;

                        if (attr.getId() == pseudoId) {
                            switch (attr.getName()) {
                                case GraphFileConstants.TX_ID:
                                    stmt.setInt(paramIx, txId);
                                    break;
                                case GraphFileConstants.SRC:
                                    stmt.setInt(paramIx, rg.getTransactionSourceVertex(txId));
                                    break;
                                case GraphFileConstants.DST:
                                    stmt.setInt(paramIx, rg.getTransactionDestinationVertex(txId));
                                    break;
                                case GraphFileConstants.DIR:
                                    stmt.setBoolean(paramIx, rg.getTransactionDirection(txId) != Graph.FLAT);
                                    break;
                                default:
                                    // do nothing
                            }
                        } else {
                            setBatchParam(rg, stmt, paramIx, attr, txId);
                        }
                    }

                    stmt.addBatch();

                    if (position > 0 && position % BATCH_COUNT == 0) {
                        stmt.executeBatch();
                        stmt.clearBatch();
                    }

                    if (position % INTERACT_COUNT == 0) {
                        try {
                            interaction.setProgress(start + position, total, String.format("Exported %d transactions", position), true);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            return false;
                        }
                    }
                }

                stmt.executeBatch();
            }
        }

        return true;
    }

    private static void updateResultSetParam(final GraphReadMethods rg, final ResultSet rs, final String label, final Attribute attr, final int id) throws SQLException {
        switch (attr.getAttributeType()) {
            case "boolean":
                rs.updateBoolean(label, rg.getBooleanValue(attr.getId(), id));
                break;
            case "date":
                final long date = rg.getLongValue(attr.getId(), id);
                if (date != Long.MIN_VALUE) {
                    rs.updateDate(label, new Date(date));
                }
                break;
            case "datetime":
                final long timestamp = rg.getLongValue(attr.getId(), id);
                if (timestamp != Long.MIN_VALUE) {
                    rs.updateTimestamp(label, new Timestamp(timestamp));
                }
                break;
            case "integer":
                rs.updateInt(label, rg.getIntValue(attr.getId(), id));
                break;
            case "float":
                rs.updateFloat(label, rg.getFloatValue(attr.getId(), id));
                break;
            case "time":
                final long time = rg.getLongValue(attr.getId(), id);
                if (time != Long.MIN_VALUE) {
                    rs.updateTime(label, new Time(time));
                }
                break;
            default:
                final String s = rg.getStringValue(attr.getId(), id);
                if (s != null) {
                    rs.updateString(label, s);
                }
                break;
        }
    }

    private static void setBatchParam(final GraphReadMethods rg, final PreparedStatement stmt, final int paramIx, final Attribute attr, final int id) throws SQLException {
        switch (attr.getAttributeType()) {
            case "boolean":
                stmt.setBoolean(paramIx, rg.getBooleanValue(attr.getId(), id));
                break;
            case "date":
                final long date = rg.getLongValue(attr.getId(), id);
                if (date != Long.MIN_VALUE) {
                    stmt.setDate(paramIx, new Date(date));
                } else {
                    stmt.setNull(paramIx, Types.DATE);
                }
                break;
            case "datetime":
                final long timestamp = rg.getLongValue(attr.getId(), id);
                if (timestamp != Long.MIN_VALUE) {
                    stmt.setTimestamp(paramIx, new Timestamp(timestamp));
                } else {
                    stmt.setNull(paramIx, Types.TIMESTAMP);
                }
                break;
            case "integer":
                stmt.setInt(paramIx, rg.getIntValue(attr.getId(), id));
                break;
            case "float":
                stmt.setFloat(paramIx, rg.getFloatValue(attr.getId(), id));
                break;
            case "time":
                final long time = rg.getLongValue(attr.getId(), id);
                if (time != Long.MIN_VALUE) {
                    stmt.setTime(paramIx, new Time(time));
                } else {
                    stmt.setNull(paramIx, Types.TIME);
                }
                break;
            default:
                final String s = rg.getStringValue(attr.getId(), id);
                if (s != null) {
                    stmt.setString(paramIx, s);
                } else {
                    stmt.setNull(paramIx, Types.VARCHAR);
                }
                break;
        }
    }

    private static void notifyException(final Exception ex) {
        final ByteArrayOutputStream sb = new ByteArrayOutputStream();
        final PrintWriter w = new PrintWriter(sb);
        w.printf("Unexpected JDBC export exception: %s%n%n", ex.getMessage());
        w.printf("Stack trace:%n%n");
        ex.printStackTrace(w);
        w.flush();
        SwingUtilities.invokeLater(() -> {
            String message;
            try {
                message = sb.toString(StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException ex1) {
                LOGGER.severe(ex1.getLocalizedMessage());
                message = sb.toString();
            }
            final InfoTextPanel itp = new InfoTextPanel(message);
            final NotifyDescriptor d = new NotifyDescriptor.Message(itp, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        });
    }
}
