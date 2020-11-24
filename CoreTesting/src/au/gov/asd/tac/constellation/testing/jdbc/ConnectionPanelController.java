/*
 * Copyright 2010-2020 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.graph.Graph;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

/**
 *
 * @author algol
 */
public class ConnectionPanelController implements WizardDescriptor.ExtendedAsynchronousValidatingPanel<WizardDescriptorData>, PropertyChangeListener {

    private final Graph graph;
    private ConnectionPanel panel = null;
    private JdbcData data;
    private final List<ChangeListener> listeners = new ArrayList<>();

    private static final Logger LOGGER = Logger.getLogger(ConnectionPanelController.class.getName());

    public ConnectionPanelController(final Graph graph) {
        this.graph = graph;
        data = null;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Component getComponent() {
        if (panel == null) {
            panel = new ConnectionPanel(graph);
            panel.addPropertyChangeListener(this);
        }

        return panel;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx(ConnectionPanelController.class.getName());
    }

    @Override
    public void addChangeListener(final ChangeListener l) {
        listeners.add(l);
    }

    @Override
    public void removeChangeListener(final ChangeListener l) {
        listeners.remove(l);
    }

    @Override
    public void readSettings(final WizardDescriptorData settings) {
        data = settings.data;

        panel.setUsername(data.username);
        panel.setConnectionUrl(data.url);
        panel.setJarFile(data.jar);
        panel.setDriverName(data.driverName);
    }

    @Override
    public void storeSettings(final WizardDescriptorData settings) {
        data = settings.data;

        // If the user loaded data from a file,
        // bring that data in.
        final JdbcData newData = panel.getData();
        if (newData != null) {
            newData.copyTo(data);
        }

        data.username = panel.getUsername();
        data.password = panel.getPassword();
        data.url = panel.getConnectionUrl();
        data.jar = panel.getJarFile();
        data.driverName = panel.getDriverName();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        listeners.stream().forEach(l -> l.stateChanged(null));
    }

    @Override
    public void validate() throws WizardValidationException {
        if (panel.getUsername().isEmpty()) {
            throw new WizardValidationException(panel.usernameText, "Username must be specified", null);
        }

        if (panel.getPassword().length == 0) {
            throw new WizardValidationException(panel.passwordText, "Password must be specified", null);
        }

        if (panel.getConnectionUrl().isEmpty()) {
            throw new WizardValidationException(panel.connectionText, "Connection URL must be specified", null);
        }

        if (panel.getJarFile().isEmpty()) {
            throw new WizardValidationException(panel.findJarButton, "JDBC JAR file must be specified", null);
        }

        final File jarFile = new File(panel.getJarFile());
        if (!jarFile.canRead()) {
            throw new WizardValidationException(panel.findJarButton, "JDBC JAR file cannot be read", null);
        }

        if (StringUtils.isBlank(panel.getDriverName())) {
            throw new WizardValidationException(panel.driverCombo, "JDBC driver must be specified", null);
        }

        // Attempt a JDBC connection here.
        try {
            final ArrayList<String> tables = new ArrayList<>();
            try (final Connection conn = JdbcUtilities.getConnection(jarFile, panel.getDriverName(), panel.getConnectionUrl(), panel.getUsername(), panel.getPassword())) {
                // Nothing to do, we're just checking to see if we can get a connection.
                try (final ResultSet tablesRs = conn.getMetaData().getTables(null, null, null, null)) {
                    while (tablesRs.next()) {
                        final String tableName = tablesRs.getString("TABLE_NAME");
                        tables.add(tableName);
                    }
                }
            }

            if (tables.isEmpty()) {
                throw new WizardValidationException(panel, "There are no tables in this database", null);
            }

            Collections.sort(tables);
            data.tables = tables;
        } catch (final MalformedURLException | ClassNotFoundException
                | IllegalAccessException | IllegalArgumentException
                | InstantiationException | NoSuchMethodException
                | SecurityException | InvocationTargetException
                | SQLException ex) {
            LOGGER.severe(ex.getLocalizedMessage());
            final String msg = String.format("%s: %s", ex.getClass().getSimpleName(), ex.getMessage());
            throw new WizardValidationException(panel, msg, msg);
        }
    }

    @Override
    public void prepareValidation() {
        // Required for ExtendedAsynchronousValidatingPanel, intentionally left blank
    }

    @Override
    public void finishValidation() {
        // Required for ExtendedAsynchronousValidatingPanel, intentionally left blank
    }
}
