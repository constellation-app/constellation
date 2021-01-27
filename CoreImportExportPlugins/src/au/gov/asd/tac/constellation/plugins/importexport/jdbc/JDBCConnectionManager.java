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
package au.gov.asd.tac.constellation.plugins.importexport.jdbc;

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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import org.openide.util.Exceptions;

public class JDBCConnectionManager {

    static JDBCConnectionManager connectionManager = null;

    private Map<String, JDBCConnection> connectionMap = new HashMap<>();
    private SQLiteDBManager sql;
    private File driversDir;

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
            Exceptions.printStackTrace(ex);
        }
    }

    public List<JDBCConnection> getConnections() {
        return new ArrayList(connectionMap.values());
    }

    public boolean testConnection(final String connectionName, final JDBCDriver driver, final String username, final String password, final String connectionString) {
        final JDBCConnection conn = new JDBCConnection(connectionName, driver, connectionString);
        return conn.testConnection(username, password, true);
    }

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
                connectionMap.put(connectionName, new JDBCConnection(connectionName, driver, connectionString));

            } catch (final IOException | SQLException ex) {
                final Alert a = new Alert(AlertType.ERROR);
                a.setTitle("Connection add failed");
                a.setContentText("Failed to add the connection to the database.");
                final TextArea ta = new TextArea(ex.getMessage());
                ta.setEditable(false);
                ta.setWrapText(true);
                a.getDialogPane().setExpandableContent(ta);

                a.showAndWait();
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    public void deleteConnection(final String name) {
        final JDBCConnection d = connectionMap.get(name);
        if (d != null) {
            connectionMap.remove(name);
        }
        try (final Connection connection = sql.getConnection()) {
            try (final PreparedStatement statement = connection.prepareStatement("delete from connection where name=?")) {
                statement.setString(1, name);
                statement.executeUpdate();
            }
        } catch (final SQLException | IOException ex) {
            final Alert a = new Alert(AlertType.ERROR);
            a.setTitle("Connection delete failed");
            a.setContentText("Failed to delete the connection from the database.");
            final TextArea ta = new TextArea(ex.getMessage());
            ta.setEditable(false);
            ta.setWrapText(true);
            a.getDialogPane().setExpandableContent(ta);

            a.showAndWait();
        }
    }

    public static JDBCConnectionManager getConnectionManager() {
        if (connectionManager == null) {
            connectionManager = new JDBCConnectionManager();
        }
        return connectionManager;
    }

}
