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

import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author algol
 */
class JdbcUtilities {

    /**
     * Get a JDBC connection.
     * <p>
     * Allows a driver to be loaded dynamically.
     *
     * @param jarFile A JAR file containing a JDBC driver implementation.
     * @param driverName The name of the JDBC driver class.
     * @param url The database URL.
     * @param username Username.
     * @param password Password.
     *
     * @return A JDBC Connection.
     *
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws SQLException
     * @throws IllegalAccessException
     * @throws MalformedURLException
     */
    static Connection getConnection(final File jarFile, final String driverName, final String url, final String username, final char[] password) throws ClassNotFoundException, InstantiationException, SQLException, IllegalAccessException, MalformedURLException {
        final URL[] searchPath = new URL[]{new URL("file:///" + jarFile.getAbsolutePath())};
        final ClassLoader clloader = URLClassLoader.newInstance(searchPath);

        // Note: we can't use DriverManager here: it only uses classes that have been loaded by the system class loader.
        // Since we're loading the class on the fly with our own Classloader, DriverManager will refuse to recognise it.
        final Driver driver = (Driver) Class.forName(driverName, true, clloader).newInstance();

        final Properties props = new Properties();
        props.put("user", username);
        props.put("password", new String(password));
        final Connection conn = driver.connect(url, props);

        return conn;
    }

    /**
     * Generate a canonical label so we can compare possibly dissimilar column
     * names with attribute labels.
     *
     * @param label A label.
     *
     * @return A canonicalised label.
     */
    static String canonicalLabel(final String label) {
        return canonicalLabel(label, true);
    }

    private static final String NON_ALPHANUM_CHARS = "[^\\p{Alnum}]+";

    static String canonicalLabel(final String label, final boolean lowerCase) {
//        final String s = label.replaceAll("[^\\dA-Za-z]", "");
        final String s = label.replaceAll(NON_ALPHANUM_CHARS, SeparatorConstants.UNDERSCORE);
        return lowerCase ? s.toLowerCase() : s;
    }

    static void checkSqlLabel(final String label) throws PluginException {
        if (label.contains("\"") || label.contains(",") || label.contains("--")) {
            throw new PluginException(PluginNotificationLevel.ERROR, String.format("Naughty SQL label: %s", label));
        }
    }
}
