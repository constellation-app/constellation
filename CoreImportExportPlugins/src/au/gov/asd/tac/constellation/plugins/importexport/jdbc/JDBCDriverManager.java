/*
 * Copyright 2010-2021 Australian Signals Directorate
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

import com.google.common.io.Files;
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

public class JDBCDriverManager {
    
    private static final Logger LOGGER = Logger.getLogger(JDBCDriverManager.class.getName());

    static JDBCDriverManager driverManager = null;

    private Map<String, JDBCDriver> driversMap = new HashMap<>();
    File driversDir;
    private SQLiteDBManager sql;

    private JDBCDriverManager() {
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
        try (final Connection connection = sql.getConnection()) {
            try (final PreparedStatement statement = connection.prepareStatement("SELECT name, path from driver")) {
                try (final ResultSet drivers = statement.executeQuery()) {
                    while (drivers.next()) {
                        final JDBCDriver d = new JDBCDriver(drivers.getString("name"), new File(drivers.getString("path")));
                        driversMap.put(d.getName(), d);
                    }
                }
            }
        } catch (final SQLException | IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    public List<JDBCDriver> getDrivers() {
        return new ArrayList(driversMap.values());
    }

    public JDBCDriver getDriver(final String name) {
        return driversMap.get(name);
    }

    public boolean isDriverUsed(final String name) {
        try (final Connection connection = sql.getConnection()) {
            try (final PreparedStatement statement = connection.prepareStatement("select count(*) as c from connection where driver_name=?")) {
                statement.setString(1, name);
                try (ResultSet rs = statement.executeQuery()) {
                    rs.next();
                    if (rs.getInt("c") > 0) {
                        return true;
                    }
                }
            }
        } catch (final IOException | SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
        return false;
    }

    public void removeConnectionsWithDriver(final String name) {
        try (final Connection connection = sql.getConnection()) {
            try (final PreparedStatement statement = connection.prepareStatement("delete from connection where driver_name=?")) {
                statement.setString(1, name);
                statement.executeUpdate();
            }
        } catch (final IOException | SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    public void addDriver(final String name, final File jar) {
        try (final Connection connection = sql.getConnection()) {
            final File out = new File(driversDir.getAbsolutePath() + File.separator + jar.getName());
            if (jar.exists() && jar.isFile() && jar.canRead()) {
                Files.copy(jar, out);
                driversMap.put(name, new JDBCDriver(name, out));
            }

            try (final PreparedStatement statement = connection.prepareStatement("insert into driver (name, path) values (?, ?)")) {
                statement.setString(1, name);
                statement.setString(2, out.getAbsolutePath());
                statement.executeUpdate();
            }

        } catch (final IOException | SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    public void deleteDriver(final String name) {
        final JDBCDriver d = driversMap.get(name);
        if (d != null) {
            d.getJarFileLocation().delete();
            driversMap.remove(name);
        }
        try (final Connection connection = sql.getConnection()) {
            try (final PreparedStatement statement = connection.prepareStatement("delete from driver where name=?")) {
                statement.setString(1, name);
                statement.executeUpdate();
            }
        } catch (final IOException | SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    public static JDBCDriverManager getDriverManager() {
        if (driverManager == null) {
            driverManager = new JDBCDriverManager();
        }
        return driverManager;
    }

}
