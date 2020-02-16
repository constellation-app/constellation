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

import java.awt.Component;
import java.io.File;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

/**
 *
 * @author algol
 */
public class TablesPanelController implements WizardDescriptor.ExtendedAsynchronousValidatingPanel<WizardDescriptorData> {

    private static final Logger LOGGER = Logger.getLogger(TablesPanelController.class.getName());

    private TablesPanel panel;
    private JdbcData data;

    public TablesPanelController() {
        panel = null;
        data = null;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Component getComponent() {
        if (panel == null) {
            panel = new TablesPanel();
        }

        return panel;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx(TablesPanelController.class.getName());
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    @Override
    public void readSettings(final WizardDescriptorData settings) {
        data = settings.data;
        panel.setTables(data.tables, data.vxTable, data.txTable);
    }

    @Override
    public void storeSettings(final WizardDescriptorData settings) {
        data = settings.data;
        data.vxTable = panel.getVxTable();
        data.txTable = panel.getTxTable();
    }

    @Override
    public void prepareValidation() {
    }

    @Override
    public void validate() throws WizardValidationException {
        final ArrayList<String> vxColumns = new ArrayList<>();
        final ArrayList<String> txColumns = new ArrayList<>();

        // Connect to the database and attempt to read the columns from the table schemas.
        try {
            final File jarFile = new File(data.jar);
            try (final Connection conn = JdbcUtilities.getConnection(jarFile, data.driverName, data.url, data.username, data.password)) {
                final String vxTable = panel.getVxTable() != null ? panel.getVxTable().trim() : null;
                if (vxTable != null && !vxTable.isEmpty()) {
                    getColumns(conn, vxTable, vxColumns);
                }
                if (vxTable != null && !vxTable.equals(data.vxTable)) {
                    data.vxMappings = null;
                }

                data.vxColumns = vxColumns;

                final String txTable = panel.getTxTable() != null ? panel.getTxTable().trim() : null;
                if (txTable != null && !txTable.isEmpty()) {
                    getColumns(conn, txTable, txColumns);
                }
                if (txTable != null && !txTable.equals(data.txTable)) {
                    data.txMappings = null;
                }

                data.txColumns = txColumns;
            }
        } catch (final MalformedURLException | ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            final String msg = String.format("%s: %s", ex.getClass().getSimpleName(), ex.getMessage());
            throw new WizardValidationException(panel, msg, msg);
        }
    }

    private static void getColumns(final Connection conn, final String table, final List<String> columnNames) throws SQLException {
        try (final ResultSet columns = conn.getMetaData().getColumns(null, null, table, null)) {
            while (columns.next()) {
                final String columnName = columns.getString("COLUMN_NAME");
//                final String typeName = columns.getString("TYPE_NAME");
                columnNames.add(columnName);
            }
        }
    }

    @Override
    public void finishValidation() {
    }
}
