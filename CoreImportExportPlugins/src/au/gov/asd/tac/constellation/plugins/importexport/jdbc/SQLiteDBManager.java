/*
 * Copyright 2010-2024 Australian Signals Directorate
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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLiteDBManager {
    
    private static final Logger LOGGER = Logger.getLogger(SQLiteDBManager.class.getName());

    private static SQLiteDBManager __instance__ = null;
    private File sqlite;

    private SQLiteDBManager() {
        try {
            final File basePath = new File(String.format("%s%s.CONSTELLATION%sJDBCImport%s", System.getProperty("user.home"), File.separator, File.separator, File.separator));
            if (!basePath.exists()) {
                basePath.mkdirs();
            }

            sqlite = new File(basePath.getAbsolutePath() + File.separator + "jdbcImporter.sqlite");

            Class.forName("org.sqlite.JDBC");
            try (final Connection connection = getConnection()) {
                try (final PreparedStatement stmt = connection.prepareStatement("SELECT count(*) from sqlite_master where type='table' AND name='connection'")) {
                    try (ResultSet rs = stmt.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) == 0) {
                            createConnectionTable(connection);
                        }
                    }
                }
                try (final PreparedStatement stmt = connection.prepareStatement("SELECT count(*) from sqlite_master where type='table' AND name='driver'")) {
                    try (final ResultSet rs = stmt.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) == 0) {
                            createDriverTable(connection);
                        }
                    }
                }
            } catch (final IOException | SQLException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }

        } catch (final ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }

    }

    public Connection getConnection() throws IOException, SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + sqlite.getCanonicalPath());
    }

    private void createDriverTable(final Connection connection) throws SQLException {
        try (final PreparedStatement statement = connection.prepareStatement("create table driver (name String, path String)")) {
            statement.executeUpdate();
        }
    }

    private void createConnectionTable(final Connection connection) throws SQLException {
        try (final PreparedStatement statement = connection.prepareStatement("create table connection (name String, driver_name String, connection_string String )")) {
            statement.executeUpdate();
        }
    }

    public static SQLiteDBManager getInstance() {
        if (__instance__ == null) {
            __instance__ = new SQLiteDBManager();
        }
        return __instance__;
    }
}
