/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.importexport.jdbc;

import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert.AlertType;
import org.apache.commons.lang3.StringUtils;

public class JDBCConnectionManager {
    
    private static final Logger LOGGER = Logger.getLogger(JDBCConnectionManager.class.getName());

    static JDBCConnectionManager connectionManager = null;

    private Map<String, JDBCConnection> connectionMap = new HashMap<>();
    private SQLiteDBManager sql;
    private File driversDir;

    private static final String ACTION_JDBC_IMPORT = "JDBC Import";

    private JDBCConnectionManager() {
        //load drivers from db
        final File basePath = new File(String.format("%s%s.CONSTELLATION%sJDBCImport%s", System.getProperty("user.home"), File.separator, File.separator, File.separator));
        if (!basePath.exists()) {
            basePath.mkdirs();
        }
        driversDir = new File(basePath.getAbsolutePath() + File.separator + "jars");
        if (!driversDir.exists()) {
            driversDir.mkdirs();
        }

        sql = SQLiteDBManager.getInstance();

        final JDBCDriverManager dm = JDBCDriverManager.getDriverManager();
        try (final Connection connection = sql.getConnection()) {

            try (final PreparedStatement statement = connection.prepareStatement("SELECT * from connection")) {
                try (final ResultSet connections = statement.executeQuery()) {
                    while (connections.next()) {
                        final JDBCDriver driver = dm.getDriver(connections.getString("driver_name"));
                        if (driver != null) {
                            final JDBCConnection d = new JDBCConnection(connections.getString("name"), driver, connections.getString("connection_string"));
                            connectionMap.put(d.getConnectionName(), d);
                        }
                    }
                }
            }
        } catch (final SQLException | IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    public List<JDBCConnection> getConnections() {
        return new ArrayList<>(connectionMap.values());
    }

    public boolean testConnection(final String connectionName, final JDBCDriver driver, final String username, final String password, final String connectionString) {
        final JDBCConnection conn = new JDBCConnection(connectionName, driver, connectionString);
        return conn.testConnection(username, password, true);
    }

    /**
     * Add a connection without a username or password.
     *
     * @param connectionName
     * @param driver
     * @param connectionString
     * @return true if the connection was successful, false otherwise.
     */
    public boolean addConnection(final String connectionName, final JDBCDriver driver, final String connectionString) {
        return addConnection(connectionName, driver, StringUtils.EMPTY, StringUtils.EMPTY, connectionString);
    }

    /**
     * Add a connection with a username and password.
     *
     * @param connectionName
     * @param driver
     * @param username
     * @param password
     * @param connectionString
     * @return true if the connection was successful, false otherwise.
     */
    public boolean addConnection(final String connectionName, final JDBCDriver driver, final String username, final String password, final String connectionString) {
        final JDBCConnection conn = new JDBCConnection(connectionName, driver, connectionString);
        if (testConnection(connectionName, driver, username, password, connectionString)) {
            try (final Connection connection = sql.getConnection()) {
                try (final PreparedStatement statement = connection.prepareStatement("insert into connection (name, driver_name, connection_string) values (?, ?, ?)")) {
                    statement.setString(1, connectionName);
                    statement.setString(2, driver.getName());
                    statement.setString(3, connectionString);
                    statement.executeUpdate();
                }
                connectionMap.put(connectionName, conn);

            } catch (final IOException | SQLException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
                NotifyDisplayer.displayLargeAlert(ACTION_JDBC_IMPORT, "Failed to add the connection to the database.",
                        ex.getMessage(), AlertType.ERROR);
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Add a connection with a username and password.
     *
     * @param currentconnectionName
     * @param newconnectionName
     * @param driver
     * @param username
     * @param password
     * @param connectionString
     * @return true if the connection was successful, false otherwise.
     */
    public boolean updateConnection(final String currentconnectionName, final String newconnectionName, final JDBCDriver driver, final String username, final String password, final String connectionString) {
        final JDBCConnection newConnection = new JDBCConnection(newconnectionName, driver, connectionString);
        if (testConnection(newconnectionName, driver, username, password, connectionString)) {
            try (final Connection connection = sql.getConnection()) {
                try (final PreparedStatement statement = connection.prepareStatement("update connection set "
                        + " name = ?, driver_name = ?, connection_string = ? where name = ?")) {
                    statement.setString(1, newconnectionName);
                    statement.setString(2, driver.getName());
                    statement.setString(3, connectionString);
                    statement.setString(4, currentconnectionName);
                    statement.executeUpdate();
                }
                connectionMap.remove(currentconnectionName);
                connectionMap.put(newconnectionName, newConnection);

            } catch (final IOException | SQLException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
                NotifyDisplayer.displayLargeAlert(ACTION_JDBC_IMPORT, "Failed to update the connection " + newconnectionName + " in the database.",
                        ex.getMessage(), AlertType.ERROR);
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    public void deleteConnection(final String name) {
        final JDBCConnection d = connectionMap.get(name);

        try (final Connection connection = sql.getConnection()) {
            try (final PreparedStatement statement = connection.prepareStatement("delete from connection where name=?")) {
                statement.setString(1, name);
                statement.executeUpdate();
                if (d != null) {
                    connectionMap.remove(name);
                }
            }
        } catch (final SQLException | IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
            NotifyDisplayer.displayLargeAlert(ACTION_JDBC_IMPORT, "Failed to delete the connection from the database.",
                    ex.getMessage(), AlertType.ERROR);
        }
    }

    public static JDBCConnectionManager getConnectionManager() {
        if (connectionManager == null) {
            connectionManager = new JDBCConnectionManager();
        }
        return connectionManager;
    }

}
