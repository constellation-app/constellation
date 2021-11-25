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

import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.apache.commons.lang3.StringUtils;

public class JDBCDriver {

    private String driverName;
    private File jarFile;

    public JDBCDriver(final String name, final File file) {
        driverName = name;
        jarFile = file;
    }

    public String getName() {
        return driverName;
    }

    public static List<String> getDrivers(final File jarFile) {
        final List<String> driverList = new ArrayList<>();
        try {

            if (jarFile != null && jarFile.exists() && jarFile.isFile()) {
                try (final JarFile jf = new JarFile(jarFile)) {
                    final ZipEntry ze = jf.getEntry("META-INF/services/java.sql.Driver");
                    if (ze != null) {
                        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(jf.getInputStream(ze), StandardCharsets.UTF_8.name()))) {
                            while (true) {
                                final String line = reader.readLine();
                                if (line == null) {
                                    break;
                                }
                                if (!line.startsWith("#") && !line.isBlank()) {
                                    driverList.add(line);
                                }
                            }
                        }
                    } else {
                        final URL[] searchPath = new URL[]{new URL("file:///" + jarFile.getAbsolutePath())};
                        try (final URLClassLoader clloader = new URLClassLoader(searchPath)) {
                            for (final Enumeration<JarEntry> e = jf.entries(); e.hasMoreElements();) {
                                final JarEntry je = e.nextElement();
                                final String classname = je.getName();
                                if (StringUtils.endsWithIgnoreCase(classname, FileExtensionConstants.CLASS)) {
                                    try {
                                        // Remove ".class", convert '/' to '.' to create a proper class name.
                                        final int len = classname.length();
                                        final String name = classname.substring(0, len - 6).replace('/', '.');
                                        final Class<?> cl = clloader.loadClass(name);
                                        if (Driver.class.isAssignableFrom(cl)) {
                                            driverList.add(name);
                                        }
                                    } catch (final ClassNotFoundException ex) {
                                        // Not a valid class; ignore it.
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } catch (final IOException ex) {

        }
        return driverList;
    }

    public String getFilename() {
        return jarFile.getName();
    }

    public void setDriverName(final String driverName) {
        this.driverName = driverName;
    }

    public File getJarFileLocation() {
        return jarFile;
    }

    public void setJarFileLocation(final File jarFile) {
        this.jarFile = jarFile;
    }

    public Driver getDriver() throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, NoSuchMethodException, InstantiationException, InstantiationException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final URL[] searchPath = new URL[]{new URL("file:///" + jarFile.getAbsolutePath())};
        final ClassLoader clloader = URLClassLoader.newInstance(searchPath);

        // Note: we can't use DriverManager here: it only uses classes that have been loaded by the system class loader.
        // Since we're loading the class on the fly with our own Classloader, DriverManager will refuse to recognise it.
        final Driver driver = (Driver) Class.forName(driverName, true, clloader).getDeclaredConstructor().newInstance();
        return driver;
    }

    @Override
    public String toString() {
        return driverName;
    }

}
