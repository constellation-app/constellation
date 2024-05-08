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
package au.gov.asd.tac.constellation.webserver;

import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import static au.gov.asd.tac.constellation.webserver.WebServer.getNotebookDir;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Web Server Test.
 *
 * @author algol
 */
public class WebServerNGTest {

    private static final Logger LOGGER = Logger.getLogger(WebServerNGTest.class.getName());
    private static final String IPYTHON = ".ipython";
    private static final String REST_FILE = "rest.json";
    private static final String TEST_TEXT = "TEST FILE";

    public WebServerNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Clean up created files from this test
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final String[] filesArray = {WebServer.CONSTELLATION_CLIENT, REST_FILE};
        for (String file : filesArray) {
            // Delete in home directory
            final String homeDir = System.getProperty("user.home");
            final Path filePath = Path.of(homeDir, IPYTHON).resolve(file);
            if (Files.exists(filePath)) {
                try {
                    Files.delete(filePath);
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Error deleting file {0} at {1}", new Object[]{file, filePath});
                }
            }

            // Delete in notebook directory
            final Path filePathNotebook = Path.of(prefs.get(ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR, ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR_DEFAULT)).resolve(file);
            if (Files.exists(filePathNotebook)) {
                try {
                    Files.delete(filePathNotebook);
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Error deleting file {0} at {1}", new Object[]{file, filePath});
                }
            }
        }
    }

    /**
     * Test of isRunning method, of class WebServer.
     */
    @Test
    public void testIsRunning() {
        System.out.println("isRunning");
        // Expected false before being started
        boolean expResult = false;
        boolean result = WebServer.isRunning();
        assertEquals(expResult, result);
    }

    /**
     * Test of start method with existing rest and constellation_client.py files, of class WebServer.
     */
    @Test
    public void testStartExistingRest() {
        System.out.println("testStartExistingRest");

        // Make rest files first
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final String userDir = ApplicationPreferenceKeys.getUserDir(prefs);
        final String scriptDir = WebServer.getScriptDir(false).toString();
        final File restFile = new File(userDir, REST_FILE);
        final File restFileNotebook = new File(getNotebookDir(), REST_FILE);
        // Unix
        final String os = System.getProperty("os.name");
        if (!os.startsWith("Windows")) {
            final Set<PosixFilePermission> perms = EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
            try {
                Files.createFile(restFile.toPath(), PosixFilePermissions.asFileAttribute(perms));
            } catch (final IOException e) {
                LOGGER.log(Level.WARNING, "Error making file");
            }
            try {
                Files.createFile(restFileNotebook.toPath(), PosixFilePermissions.asFileAttribute(perms));
            } catch (final IOException e) {
                LOGGER.log(Level.WARNING, "Error making file");
            }
        }
        // Windows
        try (final PrintWriter pw = new PrintWriter(restFile)) {
            // Couldn't be bothered starting up a JSON writer for two simple values.
            pw.printf(TEST_TEXT);
        } catch (final IOException e) {
            LOGGER.log(Level.WARNING, "Error making file");
        }

        try (final PrintWriter pw = new PrintWriter(restFileNotebook)) {
            // Couldn't be bothered starting up a JSON writer for two simple values.
            pw.printf(TEST_TEXT);
        } catch (final IOException e) {
            LOGGER.log(Level.WARNING, "Error making file");
        }

        // Check server NOT running
        assertEquals(false, WebServer.isRunning());

        // Run start
        int expResult = 1517;
        int result = WebServer.start();
        // Check port number
        assertEquals(expResult, result);

        // Check server running
        assertEquals(true, WebServer.isRunning());

        // Check file contents DO NOT match the initial values
        try {
            assertNotEquals(Files.readString(Path.of(userDir).resolve(REST_FILE), StandardCharsets.UTF_8), TEST_TEXT);
            assertNotEquals(Files.readString(Path.of(scriptDir).resolve(REST_FILE), StandardCharsets.UTF_8), TEST_TEXT);
        } catch (final IOException e) {
            LOGGER.log(Level.WARNING, "Error matching files");
        }
    }

    /**
     * Test of getScriptDir method, of class WebServer.
     */
    @Test
    public void testGetScriptDir() {
        System.out.println("getScriptDir");
        boolean mkdir = false;
        File result = WebServer.getScriptDir(mkdir);
        assertEquals(File.class, result.getClass());
    }

    /**
     * Test of getNotebookDir method, of class WebServer.
     */
    @Test
    public void testGetNotebookDir() {
        System.out.println("getNotebookDir");
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final String expResult = prefs.get(ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR, ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR_DEFAULT);
        String result = WebServer.getNotebookDir();

        assertEquals(expResult, result);
    }

    /**
     * Test of downloadPythonClient method, of class WebServer.
     */
    @Test
    public void testDownloadPythonClient() {
        System.out.println("downloadPythonClient");
        final String homeDir = System.getProperty("user.home");
        final Path filePath = Path.of(homeDir, IPYTHON).resolve(WebServer.CONSTELLATION_CLIENT);

        WebServer.downloadPythonClient();
        assertTrue(Files.exists(filePath));
    }

    /**
     * Test of downloadPythonClientToDir method, of class WebServer.
     */
    @Test
    public void testDownloadPythonClientToDir() {
        System.out.println("downloadPythonClientToDir");
        final String pathString = System.getProperty("user.dir");
        final Path filePath = Path.of(pathString).resolve(WebServer.CONSTELLATION_CLIENT);
        WebServer.downloadPythonClientToDir(new File(pathString));

        assertTrue(Files.exists(filePath));
        // Validate file contents
        assertTrue(WebServer.equalScripts(new File(pathString + File.separator + WebServer.CONSTELLATION_CLIENT)));
    }
}
