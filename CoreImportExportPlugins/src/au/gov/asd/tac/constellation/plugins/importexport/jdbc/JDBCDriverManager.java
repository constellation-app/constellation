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

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import org.openide.util.Exceptions;

public class JDBCDriverManager {

    static JDBCDriverManager __instance__ = null;

    private HashMap<String, JDBCDriver> __drivers = new HashMap<>();
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
                        __drivers.put(d.getName(), d);
                    }
                }
            }
        } catch (final SQLException ex) {
            Exceptions.printStackTrace(ex);
        } catch (final IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public ArrayList<JDBCDriver> getDrivers() {
        return new ArrayList(__drivers.values());
    }

    public JDBCDriver getDriver(final String name) {
        return __drivers.get(name);
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
        } catch (IOException | SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    public void removeConnectionsWithDriver(final String name) {
        try (final Connection connection = sql.getConnection()) {
            try (final PreparedStatement statement = connection.prepareStatement("delete from connection where driver_name=?")) {
                statement.setString(1, name);
                statement.executeUpdate();
            }
        } catch (IOException | SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void addDriver(final String name, final File jar) {
        try (final Connection connection = sql.getConnection()) {
            final File out = new File(driversDir.getAbsolutePath() + File.separator + jar.getName());
            if (jar.exists() && jar.isFile() && jar.canRead()) {
                Files.copy(jar, out);
                __drivers.put(name, new JDBCDriver(name, out));
            }

            try (final PreparedStatement statement = connection.prepareStatement("insert into driver (name, path) values (?, ?)")) {
                statement.setString(1, name);
                statement.setString(2, out.getAbsolutePath());
                statement.executeUpdate();
            }

        } catch (final IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (final SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void deleteDriver(final String name) {
        final JDBCDriver d = __drivers.get(name);
        if (d != null) {
            d.getJarFileLocation().delete();
            __drivers.remove(name);
        }
        try (final Connection connection = sql.getConnection()) {
            try (final PreparedStatement statement = connection.prepareStatement("delete from driver where name=?")) {
                statement.setString(1, name);
                statement.executeUpdate();
            }
        } catch (final IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (final SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static JDBCDriverManager getInstance() {
        if (__instance__ == null) {
            __instance__ = new JDBCDriverManager();
        }
        return __instance__;
    }

}
