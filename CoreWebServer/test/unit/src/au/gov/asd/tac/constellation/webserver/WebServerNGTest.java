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
import static au.gov.asd.tac.constellation.webserver.WebServer.getScriptDir;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
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
     * Test of start method, of class WebServer.
     */
    @Test
    public void testStart() {
        System.out.println("start");
        int expResult = 1517;
        int result = WebServer.start();
        assertEquals(expResult, result);

        final String homeDir = System.getProperty("user.home");
        final Path filePath = Path.of(homeDir, IPYTHON).resolve(WebServer.CONSTELLATION_CLIENT);

        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final Path filePathNotebook = Path.of(prefs.get(ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR, ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR_DEFAULT)).resolve(WebServer.CONSTELLATION_CLIENT);

        // Delete files
        try {
            Files.delete(filePath);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error deleting file");
        }
        try {
            Files.delete(filePathNotebook);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error deleting file");
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
        
        // Delete file
        try {
            Files.delete(filePath);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error deleting file");
        }
        // Make sure it's deleted
        assertFalse(Files.exists(filePath));
    }

    /**
     * Test of downloadPythonClientNotebookDir method, of class WebServer.
     */
    @Test
    public void testDownloadPythonClientNotebookDir() {
        System.out.println("downloadPythonClientNotebookDir");
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final Path filePath = Path.of(prefs.get(ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR, ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR_DEFAULT)).resolve(WebServer.CONSTELLATION_CLIENT);

        WebServer.downloadPythonClientNotebookDir();
        assertTrue(Files.exists(filePath));
        
        // Delete file
        try {
            Files.delete(filePath);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error deleting file");
        }
        // Make sure it's deleted
        assertFalse(Files.exists(filePath));
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
        
        // Delete file
        try {
            Files.delete(filePath);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error deleting file");
        }
        // Make sure it's deleted
        assertFalse(Files.exists(filePath));
    }
}
