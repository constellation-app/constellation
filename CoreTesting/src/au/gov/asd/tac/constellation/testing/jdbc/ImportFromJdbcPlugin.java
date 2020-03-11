/*
 * Copyright 2010-2019 Australian Signals Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.gov.asd.tac.constellation.testing.jdbc;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.file.io.GraphFileConstants;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.gui.InfoTextPanel;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
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
@NbBundle.Messages("ImportFromJdbcPlugin=Import from JDBC")
public class ImportFromJdbcPlugin extends SimpleEditPlugin {

    private static final Logger LOGGER = Logger.getLogger(ImportFromJdbcPlugin.class.getName());

    private final JdbcData data;
    private final Map<Integer, Integer> columnVxId2GraphVxId;

    ImportFromJdbcPlugin(final JdbcData data) {
        this.data = data;
        columnVxId2GraphVxId = new HashMap<>();
    }

    @Override
    protected void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        try {
            try (final Connection conn = JdbcUtilities.getConnection(new File(data.jar), data.driverName, data.url, data.username, data.password)) {
                boolean complete = readVerticesFromJdbc(wg, conn, interaction);
                if (complete) {
                    complete = readTransactionsFromJdbc(wg, conn, interaction);
                }

                if (complete) {
                    final String msg = String.format("JDBC import complete: %d vertices, %d transactions.", wg.getVertexCount(), wg.getTransactionCount());
                    interaction.setProgress(0, 0, msg, false);
                } else {
                    interaction.setProgress(0, 0, "JDBC import interrupted, database may be inconsistent.", false);
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

    private boolean readVerticesFromJdbc(final GraphWriteMethods wg, final Connection conn, final PluginInteraction interaction) throws SQLException, PluginException {
        // Build an SQL SELECT to prime a ResultSet.
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
                    final int attrId = wg.getAttribute(GraphElementType.VERTEX, attrLabel);
                    final Attribute attr = new GraphAttribute(wg, attrId);
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
        LOGGER.log(Level.INFO,"JDBC import vx SQL: {0}", select.toString());

        if (!labelMap.isEmpty()) {
            try (final Statement stmt = conn.createStatement()) {
                try (final ResultSet rs = stmt.executeQuery(select.toString())) {

                    while (rs.next()) {
                        final int vxId = wg.addVertex();

                        for (final Map.Entry<String, Attribute> entry : labelMap.entrySet()) {
                            final String label = entry.getKey();
                            final Attribute attr = entry.getValue();

                            if (attr == null) {
                                columnVxId2GraphVxId.put(rs.getInt(label), vxId);
                            } else {
                                setValue(wg, rs, label, attr, vxId);
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    private boolean readTransactionsFromJdbc(final GraphWriteMethods wg, final Connection conn, final PluginInteraction interaction) throws SQLException, PluginException {
        // Create some dummy attribute types as markers for the transaction pseudo-attributes.
        final int pseudoId = -9;
        final Attribute pseudo = new GraphAttribute(pseudoId, GraphElementType.TRANSACTION, "integer", "tx", "Pseudo tx", null, null);

        // Build an SQL SELECT to prime a ResultSet.
        // This is an SQL injection waiting to happen, so make a feeble attempt at mitigation...
        final StringBuilder select = new StringBuilder();
        select.append(" SELECT ");
        final Map<String, Attribute> labelMap = new HashMap<>();
        final Map<String, String> attrMap = new HashMap<>();

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
                    attrMap.put(attrLabel, colLabel);
                } else {
                    final int attrId = wg.getAttribute(GraphElementType.TRANSACTION, attrLabel);
                    final Attribute attr = new GraphAttribute(wg, attrId);
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
        LOGGER.log(Level.INFO,"JDBC import tx SQL: {0}", select.toString());

        if (!labelMap.isEmpty()) {
            try (final Statement stmt = conn.createStatement()) {
                try (final ResultSet rs = stmt.executeQuery(select.toString())) {

                    int txId;
                    while (rs.next()) {
                        final int src = rs.getInt(attrMap.get(GraphFileConstants.SRC));
                        final int dst = rs.getInt(attrMap.get(GraphFileConstants.DST));
                        boolean directed = rs.getBoolean(attrMap.get(GraphFileConstants.DIR));
                        if (rs.wasNull()) {
                            directed = true;
                        }

                        txId = wg.addTransaction(columnVxId2GraphVxId.get(src), columnVxId2GraphVxId.get(dst), directed);

                        for (final Map.Entry<String, Attribute> entry : labelMap.entrySet()) {
                            final String label = entry.getKey();
                            final Attribute attr = entry.getValue();

                            if (attr.getId() != pseudoId) {
                                setValue(wg, rs, label, attr, txId);
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    private static void setValue(final GraphWriteMethods wg, final ResultSet rs, final String label, final Attribute attr, final int id) throws SQLException {
        switch (attr.getAttributeType()) {
            case "boolean":
                wg.setBooleanValue(attr.getId(), id, rs.getBoolean(label));
                break;
            case "date":
                final Date d = rs.getDate(label);
                if (!rs.wasNull()) {
                    wg.setLongValue(attr.getId(), id, d.getTime());
                }
                break;
            case "datetime":
                final Timestamp ts = rs.getTimestamp(label);
                if (!rs.wasNull()) {
                    wg.setLongValue(attr.getId(), id, ts.getTime());
                }
                break;
            case "integer":
                wg.setIntValue(attr.getId(), id, rs.getInt(label));
                break;
            case "float":
                wg.setFloatValue(attr.getId(), id, rs.getFloat(label));
                break;
            case "time":
                final Time t = rs.getTime(label);
                if (!rs.wasNull()) {
                    wg.setLongValue(attr.getId(), id, t.getTime());
                }
                break;
            default:
                final String s = rs.getString(label);
                wg.setStringValue(attr.getId(), id, rs.wasNull() ? null : s);
                break;
        }
    }

    private static void notifyException(final Exception ex) {
        final ByteArrayOutputStream sb = new ByteArrayOutputStream();
        final PrintWriter w = new PrintWriter(sb);
        w.printf("Unexpected exception: %s%n%n", ex.getMessage());
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
