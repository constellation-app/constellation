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
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;

public class JDBCConnection {

    private static final Logger LOGGER = Logger.getLogger(JDBCConnection.class.getName());
    private String connectionName;
    private JDBCDriver driver;
    private String connectionString;

    public JDBCConnection(final String connectionName, final JDBCDriver driver, final String connectionString) {
        this.connectionName = connectionName;
        this.driver = driver;
        this.connectionString = connectionString;
    }

    public Connection getConnection(final String user, final String password) throws MalformedURLException, ClassNotFoundException, SQLException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final Driver currentDriver = driver.getDriver();

        final Properties props = new Properties();
        props.put("user", user);
        props.put("password", password);
        return currentDriver.connect(connectionString, props);
    }

    public boolean testConnection(final String user, final String password, final boolean showError) {
        try (final Connection conn = getConnection(user, password)) {
            if (conn == null) {
                NotifyDisplayer.displayLargeAlert("Database Import", "Connection Failed",
                        "Testing of the connection failed, please recheck your connection string settings.",
                        Alert.AlertType.ERROR);
                return false;
            }
        } catch (final MalformedURLException | ClassNotFoundException | SQLException
                | NoSuchMethodException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException ex) {
            if (showError) {
                LOGGER.log(Level.WARNING, ex.getMessage());
                NotifyDisplayer.displayLargeAlert("Database Import", "Testing of the connection failed, "
                        + "please recheck your settings.", ex.getMessage(), Alert.AlertType.ERROR);
            }
            return false;
        }
        return true;
    }

    public JDBCDriver getDriver() {
        return driver;
    }

    public void setDriver(final JDBCDriver _driver) {
        this.driver = _driver;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(final String _connectionString) {
        this.connectionString = _connectionString;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(final String _connectionName) {
        this.connectionName = _connectionName;
    }

    @Override
    public String toString() {
        return connectionName;
    }
}
