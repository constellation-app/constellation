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
package au.gov.asd.tac.constellation.webserver;

import au.gov.asd.tac.constellation.help.utilities.Generator;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import static au.gov.asd.tac.constellation.webserver.WebServer.getNotebookDir;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import static org.mockito.ArgumentMatchers.anyBoolean;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
    private static final Path NOTEBOOK_PATH = Path.of(NbPreferences.forModule(ApplicationPreferenceKeys.class).get(ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR, ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR_DEFAULT));
    private static final Preferences PREFS = NbPreferences.forModule(ApplicationPreferenceKeys.class);

    private static final boolean OLD_PREF_VALUE = PREFS.getBoolean(ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD, ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD_DEFAULT);

    @BeforeClass
    public static void setUpClass() {
        PREFS.putBoolean(ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD, false);
    }

    @AfterClass
    public static void tearDownClass() {
        // Set the parameter back to its old value
        PREFS.putBoolean(ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD, OLD_PREF_VALUE);
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Clean up created files from this test
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
            final Path filePathNotebook = NOTEBOOK_PATH.resolve(file);
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
        // Mocks
        Process processMock = mock(Process.class);
        try {
            when(processMock.waitFor()).thenReturn(0); // Return success
        } catch (InterruptedException ex) {
        }
        when(processMock.getInputStream()).thenReturn(null);

        // Make rest files first
        final String userDir = ApplicationPreferenceKeys.getUserDir(PREFS);
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
            pw.printf(TEST_TEXT);
        } catch (final IOException e) {
            LOGGER.log(Level.WARNING, "Error making file");
        }

        try (final PrintWriter pw = new PrintWriter(restFileNotebook)) {
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

    @Test
    public void testInstallPythonPackageWindows() {
        System.out.println("testInstallPythonPackageWindows");
        installPythonPackageHelper(true);
    }

    @Test
    public void testInstallPythonPackageLinux() {
        System.out.println("testInstallPythonPackageLinux");
        installPythonPackageHelper(false);
    }

    public void installPythonPackageHelper(final boolean isWindows) {
        // Mocks
        Process processMock = mock(Process.class);
        try {
            when(processMock.waitFor()).thenReturn(0); // Return success
        } catch (InterruptedException ex) {
            return;
        }
        when(processMock.getInputStream()).thenReturn(InputStream.nullInputStream());

        try (MockedStatic<WebServer> webserverMock = Mockito.mockStatic(WebServer.class, Mockito.CALLS_REAL_METHODS); MockedStatic<Generator> generatorMock = Mockito.mockStatic(Generator.class); MockedConstruction<ProcessBuilder> processBuilderMock = Mockito.mockConstruction(ProcessBuilder.class, (mock, context)
                -> {
            // Return our mocked process when start is called
            when(mock.start()).thenReturn(processMock);
            when(mock.redirectErrorStream(anyBoolean())).thenReturn(mock);
        })) {
            generatorMock.when(Generator::getBaseDirectory).thenReturn("");

            // Webserver is mocked, but calles real methods aside from isWindows()
            webserverMock.when(WebServer::isWindows).thenReturn(isWindows);

            // Run function
            try {
                WebServer.installPythonPackage();
            } catch (IOException ex) {
                System.out.println("Caught IOException when running WebServer.installPythonPackage()");
            }
            verify(processMock, times(1)).getInputStream();

            // Assert processBuilder was made
            assertEquals(processBuilderMock.constructed().size(), 1);

            try {
                verify(processMock, times(1)).waitFor();
            } catch (InterruptedException ex) {
            }
        }
    }
    
//        @Test
//    public void testInstallPythonPackageIOException() {
//        System.out.println("testInstallPythonPackageIOException");
//        // Mocks
//        Process processMock = mock(Process.class);
//        try {
//            when(processMock.waitFor()).thenReturn(0); // Return success
//        } catch (InterruptedException ex) {
//            return;
//        }
//        when(processMock.getInputStream()).thenReturn(InputStream.nullInputStream());
//
//        try (MockedStatic<Generator> generatorMock = Mockito.mockStatic(Generator.class); MockedConstruction<ProcessBuilder> processBuilderMock = Mockito.mockConstruction(ProcessBuilder.class, (mock, context)
//                -> {
//            // Return our mocked process when start is called
//            when(mock.start()).thenThrow(IOException.class);
//            when(mock.redirectErrorStream(anyBoolean())).thenReturn(mock);
//        })) {
//            generatorMock.when(Generator::getBaseDirectory).thenReturn("");
//            
//            // Run function
//            try {
//                WebServer.installPythonPackage();
//            } catch (IOException ex) {
//                System.out.println("Caught IOException when running WebServer.installPythonPackage()");
//            }
//
//            // Assert processBuilder was made
//            assertEquals(processBuilderMock.constructed().size(), 1);
//
//            // Assert process never got to run
//            try {
//                verify(processMock, never()).waitFor();
//            } catch (InterruptedException ex) {
//                System.out.println("Caught InterruptedException in testInstallPythonPackageIOException");
//            }
//        }
//    }
    
    @Test
    public void testInstallPythonPackageNotSuccessfull() {
        System.out.println("testInstallPythonPackageIOException");
        // Mocks
        Process processMock = mock(Process.class);
        try {
            when(processMock.waitFor()).thenReturn(-1); // Return NOT success
        } catch (InterruptedException ex) {
            return;
        }
        when(processMock.getInputStream()).thenReturn(InputStream.nullInputStream());

        try (MockedStatic<Generator> generatorMock = Mockito.mockStatic(Generator.class); MockedConstruction<ProcessBuilder> processBuilderMock = Mockito.mockConstruction(ProcessBuilder.class, (mock, context)
                -> {
            // Return our mocked process when start is called
            when(mock.start()).thenThrow(IOException.class);
            when(mock.redirectErrorStream(anyBoolean())).thenReturn(mock);
        })) {
            generatorMock.when(Generator::getBaseDirectory).thenReturn("");
            
            // Run function
            try {
                WebServer.installPythonPackage();
            } catch (IOException ex) {
                System.out.println("Caught IOException when running WebServer.installPythonPackage()");
            }

            // Assert processBuilder was made
            assertEquals(processBuilderMock.constructed().size(), 1);

            // Assert process never got to run
            try {
                verify(processMock, never()).waitFor();
            } catch (InterruptedException ex) {
                System.out.println("Caught InterruptedException in testInstallPythonPackageIOException");
            }
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
        final String expResult = PREFS.get(ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR, ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR_DEFAULT);
        String result = WebServer.getNotebookDir();

        assertEquals(expResult, result);
    }

    /**
     * Test of downloadPythonClient method, of class WebServer.
     */
    @Test
    public void testDownloadPythonClient() {
        System.out.println("downloadPythonClient");
        final String scriptDir = WebServer.getScriptDir(false).toString();
        final Path filePath = Path.of(scriptDir).resolve(WebServer.CONSTELLATION_CLIENT);

        final Path testFilePath = createTestFile();

        try (MockedStatic<Paths> pathsStaticMock = Mockito.mockStatic(Paths.class)) {

            pathsStaticMock.when(() -> Paths.get(Mockito.anyString())).thenReturn(testFilePath);
            WebServer.downloadPythonClient();
            assertTrue(Files.exists(filePath));
        }
    }

    /**
     * Test of downloadPythonClient method, of class WebServer.
     */
    @Test
    public void testDownloadPythonClientNotebookDir() {
        System.out.println("DownloadPythonClientNotebookDir");
        final String notebookDir = WebServer.getNotebookDir();
        final Path filePath = Path.of(notebookDir).resolve(WebServer.CONSTELLATION_CLIENT);

        final Path testFilePath = createTestFile();

        try (MockedStatic<Paths> pathsStaticMock = Mockito.mockStatic(Paths.class)) {

            pathsStaticMock.when(() -> Paths.get(Mockito.anyString())).thenReturn(testFilePath);
            WebServer.downloadPythonClientNotebookDir();
            assertTrue(Files.exists(filePath));
        }
    }

    /**
     * Test of downloadPythonClientToDir method, of class WebServer.
     */
    @Test
    public void testDownloadPythonClientToDir() {
        System.out.println("downloadPythonClientToDir");
        final String pathString = System.getProperty("user.dir");
        final Path filePath = Path.of(pathString).resolve(WebServer.CONSTELLATION_CLIENT);

        final Path testFilePath = createTestFile();

        try (MockedStatic<Paths> pathsStaticMock = Mockito.mockStatic(Paths.class)) {

            pathsStaticMock.when(() -> Paths.get(Mockito.anyString())).thenReturn(testFilePath);
            WebServer.downloadPythonClientToDir(new File(pathString));
            assertTrue(Files.exists(filePath));
        }
    }

    /**
     * Function to create empty file to be used in tests.
     */
    private Path createTestFile() {
        final File testFile = new File("testFile.py");
        try {
            if (testFile.createNewFile()) {
                System.out.println("File created: " + testFile.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        return Paths.get(testFile.getAbsolutePath());
    }

    /**
     * Test of isWindows method, of class StartJupyterNotebookAction.
     */
    @Test
    public void testIsWindows() {
        System.out.println("isWindows");
        boolean expResult = System.getProperty("os.name").toLowerCase().contains("win");
        boolean result = WebServer.isWindows();
        assertEquals(result, expResult);
    }
}
