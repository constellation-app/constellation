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
package au.gov.asd.tac.constellation.webserver;

import au.gov.asd.tac.constellation.help.utilities.Generator;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
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
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.apache.commons.io.IOUtils;
import static org.mockito.ArgumentMatchers.anyBoolean;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.stubbing.Answer;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbPreferences;
import org.testfx.api.FxToolkit;
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

    private static final String REST_FILE = "rest.json";
    private static final String TEST_TEXT = "TEST FILE";
    private static final String TEST_FOLDER = "test_folder";

    private static final Preferences PREFS = NbPreferences.forModule(ApplicationPreferenceKeys.class);

    private static final boolean OLD_PREF_VALUE = PREFS.getBoolean(ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD, ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD_DEFAULT);

    @BeforeClass
    public static void setUpClass() throws Exception {
        PREFS.putBoolean(ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD, false);
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Set the parameter back to its old value
        PREFS.putBoolean(ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD, OLD_PREF_VALUE);
        try {
            FxToolkit.cleanupStages();
        } catch (final TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
        final File testDir = new File(TEST_FOLDER);        
        if (!testDir.exists()) {
            testDir.mkdir();
        }
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Clean up created files from this test
        final String[] filesArray = {WebServer.CONSTELLATION_CLIENT, REST_FILE, "testFile.py"};
        for (String file : filesArray) {
            // Delete in test directory
            final Path filePath = Path.of(TEST_FOLDER).resolve(file);
            if (Files.exists(filePath)) {
                try {
                    Files.delete(filePath);
                } catch (final IOException e) {
                    LOGGER.log(Level.WARNING, "Error deleting file {0} at {1}", new Object[]{file, filePath});
                }
            }
        }
        final Path testPath = Path.of(TEST_FOLDER);
        if (Files.isDirectory(testPath)) {
            Files.delete(testPath);
        }
    }

    /**
     * Test of isRunning method, of class WebServer.
     */
    @Test
    public void testIsRunning() {
        System.out.println("isRunning");
        // Expected false before being started
        final boolean expResult = false;
        final boolean result = WebServer.isRunning();
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

        // Make rest file first
        final String restPref = PREFS.get(ApplicationPreferenceKeys.REST_DIR, ApplicationPreferenceKeys.REST_DIR_DEFAULT);
        final String restDir = "".equals(restPref) ? WebServer.getScriptDir(true).toString() : restPref;
        final File restFile = new File(restDir, REST_FILE);
        // Unix
        final String os = System.getProperty("os.name");
        if (!os.startsWith("Windows")) {
            final Set<PosixFilePermission> perms = EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
            try {
                Files.createFile(restFile.toPath(), PosixFilePermissions.asFileAttribute(perms));
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

        // Check server NOT running
        assertEquals(false, WebServer.isRunning());

        // Run start
        final int expResult = 1517;
        int result = WebServer.start();

        // Check port number
        assertEquals(expResult, result);

        // Check server running
        assertEquals(true, WebServer.isRunning());

        // Check file contents DO NOT match the initial values
        try {
            assertNotEquals(Files.readString(Path.of(restDir).resolve(REST_FILE), StandardCharsets.UTF_8), TEST_TEXT);
        } catch (final IOException e) {
            LOGGER.log(Level.WARNING, "Error matching files");
        }

        // Also test trying to start server when server is already running
        result = WebServer.start();

        // Check port number
        assertEquals(expResult, result);

        // Check server still running
        assertEquals(true, WebServer.isRunning());
    }

    @Test
    public void testInstallPythonPackageWindows() throws InterruptedException {
        System.out.println("testInstallPythonPackageWindows");
        installPythonPackageHelper(true);
    }

    @Test
    public void testInstallPythonPackageLinux() throws InterruptedException {
        System.out.println("testInstallPythonPackageLinux");
        installPythonPackageHelper(false);
    }

    public void installPythonPackageHelper(final boolean isWindows) throws InterruptedException {
        // Mocks
        Process processMock = mock(Process.class);
        when(processMock.waitFor()).thenReturn(0); // Return success
        when(processMock.getInputStream()).thenReturn(InputStream.nullInputStream());

        try (final MockedStatic<WebServer> webserverMock = Mockito.mockStatic(WebServer.class, Mockito.CALLS_REAL_METHODS); final MockedStatic<Generator> generatorMock = Mockito.mockStatic(Generator.class); final MockedConstruction<ProcessBuilder> processBuilderMock = Mockito.mockConstruction(ProcessBuilder.class, (mock, context)
                -> {
            // Return our mocked process when start is called
            when(mock.start()).thenReturn(processMock);
            when(mock.redirectErrorStream(anyBoolean())).thenReturn(mock);
        })) {
            generatorMock.when(Generator::getBaseDirectory).thenReturn("");
            webserverMock.when(WebServer::verifyInstalledPackageVersionSame).thenReturn(false);
            // Webserver is mocked, but calles real methods aside from isWindows()
            webserverMock.when(WebServer::isWindows).thenReturn(isWindows);
            webserverMock.when(WebServer::getNotebookDir).thenReturn(TEST_FOLDER);

            // Run function
            try {
                WebServer.installPythonPackage();
            } catch (final IOException ex) {
                System.out.println("Caught IOException when running WebServer.installPythonPackage()");
            }
            verify(processMock, times(2)).getInputStream();

            // Assert processBuilder was made
            assertEquals(2, processBuilderMock.constructed().size());

            try {
                verify(processMock, times(1)).waitFor();
            } catch (final InterruptedException ex) {
                System.out.println("Caught InterruptedException when running WebServer.installPythonPackage()");
            }
        }
    }

    @Test
    /**
     * This test checks that the process waitFor() is never called when the
     * installation process throws an IOException. Test that if IOException 
     * thrown, downloadPythonClientNotebookDir will still be run.
     * Notes: 
     * 1) Set up to throw IOException when process start() is called.
     * 3) In getInstalledVersion() startPythonProcess function is called, throwing IOException will call downloadPythonClientNotebookDir.
     * 4) Installation of package with startPythonProcess function will begin but will throw IOException as well, this will result in another call to downloadPythonClientNotebookDir.
     */
    public void testInstallPythonPackageIOException() throws InterruptedException {
        System.out.println("testInstallPythonPackageIOException");
        // Mocks
        Process processMock = mock(Process.class);
        when(processMock.waitFor()).thenReturn(0); // Return success
        when(processMock.getInputStream()).thenReturn(InputStream.nullInputStream());

        try (final MockedStatic<WebServer> webserverMock = Mockito.mockStatic(WebServer.class, Mockito.CALLS_REAL_METHODS); final MockedStatic<Generator> generatorMock = Mockito.mockStatic(Generator.class); final MockedConstruction<ProcessBuilder> processBuilderMock = Mockito.mockConstruction(ProcessBuilder.class, (mock, context)
                -> {
            // Return our mocked process when start is called
            when(mock.start()).thenThrow(IOException.class);
            when(mock.redirectErrorStream(anyBoolean())).thenReturn(mock);
        })) {
            // Setup mocks
            webserverMock.when(WebServer::getInstalledVersion).thenReturn(null);
            webserverMock.when(() ->  WebServer.downloadPythonClientToDir(Mockito.any())).then((Answer<Void>) invocation -> null);
            webserverMock.when(() ->  WebServer.equalScripts(Mockito.any())).thenReturn(true);
            webserverMock.when(WebServer::getNotebookDir).thenReturn(TEST_FOLDER);
           
            // Run function
            try {
                WebServer.installPythonPackage();
            } catch (final IOException ex) {
                System.out.println("Caught IOException when running WebServer.installPythonPackage()");
            }

            // Assert processBuilder was made once
            assertEquals(2, processBuilderMock.constructed().size());

            // Assert process never got to run
            try {
                verify(processMock, never()).waitFor();
            } catch (final InterruptedException ex) {
                System.out.println("Caught InterruptedException in testInstallPythonPackageIOException");
            }

            // Assert function was called to download script
            webserverMock.verify(WebServer::downloadPythonClientNotebookDir, times(2));
        }
    }

    @Test
    /**
     * This test checks that when process waitFor() returns -1 (unsuccessful
     * process) during the installPythonPackage installation process,
     * downloadPythonClientNotebookDir is called. 
     * Notes: 
     * 1) verifyInstalledPackageVersionSame = false, so need to uninstall
     * 2) processBuilder was constructed 4 times, for getInstalledVersion, uninstall, installation, and getInstalledVersion (for verification).
     * 3) InputStream is set to null so the while loop returns.
     */
    public void testInstallPythonPackageNotSuccessful() throws InterruptedException, IOException {
        System.out.println("testInstallPythonPackageNotSuccessful");
        // Mocks
        Process processMock = mock(Process.class);
        when(processMock.waitFor()).thenReturn(-1); // Return NOT success

        // Mocking Webserver only to verify functions were run
        try (final MockedStatic<WebServer> webserverMock = Mockito.mockStatic(WebServer.class, Mockito.CALLS_REAL_METHODS); final MockedStatic<Generator> generatorMock = Mockito.mockStatic(Generator.class); final MockedConstruction<ProcessBuilder> processBuilderMock = Mockito.mockConstruction(ProcessBuilder.class, (mock, context)
                -> {
            // Return our mocked process when start is called
            when(mock.start()).thenReturn(processMock);
            when(mock.redirectErrorStream(anyBoolean())).thenReturn(mock);
            when(processMock.getInputStream()).thenReturn(InputStream.nullInputStream());
        })) {
            // Setup mocks
            generatorMock.when(Generator::getBaseDirectory).thenReturn("");
            webserverMock.when(WebServer::getNotebookDir).thenReturn(TEST_FOLDER);
            // Run function
            try {
                WebServer.installPythonPackage();
            } catch (IOException ex) {
                System.out.println("Caught IOException when running WebServer.installPythonPackage()");
            }

            // Assert processBuilder was made
            assertEquals(4, processBuilderMock.constructed().size());

            // Assert process waitFor got to run
            try {
                verify(processMock, times(4)).waitFor();
            } catch (final InterruptedException ex) {
                System.out.println("Caught InterruptedException in testInstallPythonPackageNotSuccessful");
            }

            // Assert function was called to download script
            webserverMock.verify(WebServer::downloadPythonClientNotebookDir, times(1));
        }
    }

    @Test
    /**
     * This test checks that verifyInstalledPackageVersionSame returns false
     * when compared with a test current version 0.0.0 when stubbing the
     * installed version. 
     * 
     * Notes: 
     * 1) Set up so current version = 0.0.0
     * 2) processBuilder was constructed once, for getInstalledVersion.
     * 3) Result should always be false.
     */
    public void testVerifyInstalledPackageVersionSameFalse() throws InterruptedException, IOException {
        System.out.println("testVerifyInstalledPackageVersionSameFalse");
        // Mocks
        Process processMock = mock(Process.class);
        when(processMock.waitFor()).thenReturn(0); // Return success
        // mock the installed version
        final InputStream stubInputStream = IOUtils.toInputStream("constellation_client==0.0.0", "UTF-8");
        when(processMock.getInputStream()).thenReturn(stubInputStream);
        
        try (final MockedStatic<WebServer> webserverMock = Mockito.mockStatic(WebServer.class, Mockito.CALLS_REAL_METHODS); final MockedStatic<Generator> generatorMock = Mockito.mockStatic(Generator.class); final MockedConstruction<ProcessBuilder> processBuilderMock = Mockito.mockConstruction(ProcessBuilder.class, (mock, context)
                -> {
            // Return our mocked process when start is called
            when(mock.start()).thenReturn(processMock);
            when(mock.redirectErrorStream(anyBoolean())).thenReturn(mock);
        })) {
            // Run function
            try {
                // should not be the same!
                final Boolean result = WebServer.verifyInstalledPackageVersionSame();
                assertEquals(result, Boolean.FALSE);
            } catch (IOException ex) {
                System.out.println("Caught IOException when running WebServer.verifyInstalledPackageVersionSame()");
            }
            verify(processMock, times(1)).getInputStream();
            webserverMock.verify(WebServer::getInstalledVersion, times(1));
            
            // Assert processBuilder was made
            assertEquals(1, processBuilderMock.constructed().size());

            try {
                verify(processMock, times(1)).waitFor();
            } catch (final InterruptedException ex) {
                System.out.println("Caught InterruptedException in testVerifyInstalledPackageVersionSameFalse");
            }

            // Assert function was NOT called to download script
            webserverMock.verify(WebServer::downloadPythonClientNotebookDir, times(0));
        }
    }

    @Test
    /**
     * This test checks that if IOException is thrown, download of the 
     * file will still be performed.
     * Notes: 
     * 1) Set up so first process start function will throw IOException.
     * 2) Verify that downloadPythonClientNotebookDir will be called
     * 
     */
    public void testVerifyPythonPackageIOException() throws InterruptedException {
        System.out.println("testVerifyPythonPackageIOException");
        // Mocks
        Process processMock = mock(Process.class);
        when(processMock.waitFor()).thenReturn(0); // Return success
        when(processMock.getInputStream()).thenReturn(InputStream.nullInputStream());

        try (final MockedStatic<WebServer> webserverMock = Mockito.mockStatic(WebServer.class, Mockito.CALLS_REAL_METHODS); final MockedStatic<Generator> generatorMock = Mockito.mockStatic(Generator.class); final MockedConstruction<ProcessBuilder> processBuilderMock = Mockito.mockConstruction(ProcessBuilder.class, (mock, context)
                -> {
            // Return our mocked process when start is called
            when(mock.start()).thenThrow(IOException.class);
            when(mock.redirectErrorStream(anyBoolean())).thenReturn(mock);
        })) {
            //Setup mocks
            generatorMock.when(Generator::getBaseDirectory).thenReturn("");

            // Run verification function
            try {
                WebServer.verifyInstalledPackageVersionSame();
            } catch (final IOException ex) {
                System.out.println("Caught IOException when running WebServer.verifyInstalledPackageVersionSame()");
            }

            // Assert processBuilder was made once
            assertEquals(1, processBuilderMock.constructed().size());

            // Assert process never got to run
            try {
                verify(processMock, never()).waitFor();
            } catch (final InterruptedException ex) {
                System.out.println("Caught InterruptedException in testInstallPythonPackageIOException");
            }

            // Assert function was called to download script
            webserverMock.verify(WebServer::downloadPythonClientNotebookDir, times(1));
        }
    }

    @Test
    /**
     * This test will test when verifyInstalledPackageVersionSame returns false
     * when attempting to installPythonPackage.
     * This will then uninstall the package and install the new package as well
     * as download the constellation_client.py file.
     * Note: Process is called 3 times: 1)Verify 2)Uninstall 3)Install
     */
    public void testverifyInstalledPackageVersionDifferentUninstall() throws InterruptedException {
        System.out.println("testverifyInstalledPackageVersionDifferentUninstall");
        // Mocks
        Process processMock = mock(Process.class);
        
        try (final MockedStatic<WebServer> webserverMock = Mockito.mockStatic(WebServer.class, Mockito.CALLS_REAL_METHODS); final MockedStatic<Generator> generatorMock = Mockito.mockStatic(Generator.class); final MockedConstruction<ProcessBuilder> processBuilderMock = Mockito.mockConstruction(ProcessBuilder.class, (mock, context)
                -> {
            // Return success for getInstalledVersion, uninstall and installation
            when(processMock.waitFor()).thenReturn(0, 0, 0); 
            when(mock.start()).thenReturn(processMock);
            when(mock.redirectErrorStream(anyBoolean())).thenReturn(mock);
        })) {
            //Setup mocks
            generatorMock.when(Generator::getBaseDirectory).thenReturn("");
            webserverMock.when(WebServer::verifyInstalledPackageVersionSame).thenReturn(Boolean.FALSE);
            webserverMock.when(WebServer::getNotebookDir).thenReturn(TEST_FOLDER);
            when(processMock.getInputStream()).thenReturn(InputStream.nullInputStream(), InputStream.nullInputStream(), InputStream.nullInputStream());
                        
            // Run verification function
            try {
                WebServer.installPythonPackage();
            } catch (final IOException ex) {
                System.out.println("Caught IOException when running WebServer.installPythonPackage()");
            }

            // Assert processBuilder should be called 3 times: getInstalledVersion, Uninstall, Install
            assertEquals(3, processBuilderMock.constructed().size());

            // Assert function was called to download script
            webserverMock.verify(WebServer::downloadPythonClientNotebookDir, times(1));
        }
    }

    /**
     * Test of getScriptDir method, of class WebServer.
     */
    @Test
    public void testGetScriptDir() {
        System.out.println("getScriptDir");
        final boolean mkdir = false;
        final File result = WebServer.getScriptDir(mkdir);
        assertEquals(File.class, result.getClass());
    }

    /**
     * Test of getNotebookDir method, of class WebServer.
     */
    @Test
    public void testGetNotebookDir() {
        System.out.println("getNotebookDir");
        final String expResult = PREFS.get(ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR, ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR_DEFAULT);
        final String result = WebServer.getNotebookDir();

        assertEquals(expResult, result);
    }

    /**
     * Test of downloadPythonClient method, of class WebServer.
     */
    @Test
    public void testDownloadPythonClient() {
        System.out.println("downloadPythonClient");
        final Path filePath = Path.of(TEST_FOLDER).resolve(WebServer.CONSTELLATION_CLIENT);
        final Preferences prefsMock = mock(Preferences.class );
        when(prefsMock.getBoolean(ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD, ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD_DEFAULT)).thenReturn(true);
        
        final Path testFilePath = createTestFile(TEST_FOLDER);

        try (final MockedStatic<Paths> pathsStaticMock = Mockito.mockStatic(Paths.class);
                final MockedStatic<Generator> generatorMock = Mockito.mockStatic(Generator.class);
                final MockedStatic<WebServer> webserverMockStatic = Mockito.mockStatic(WebServer.class, Mockito.CALLS_REAL_METHODS);
                final MockedStatic<NbPreferences> nbPreferencesStatic = Mockito.mockStatic(NbPreferences.class, Mockito.CALLS_REAL_METHODS)) {            
            pathsStaticMock.when(() -> Paths.get(Mockito.anyString())).thenReturn(testFilePath);
            webserverMockStatic.when(() -> WebServer.getScriptDir(true)).thenReturn(new File(TEST_FOLDER));
            nbPreferencesStatic.when(() -> NbPreferences.forModule(ApplicationPreferenceKeys.class)).thenReturn(prefsMock);
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
        final Path filePath = Path.of(TEST_FOLDER).resolve(WebServer.CONSTELLATION_CLIENT);
        final Preferences prefsMock = mock(Preferences.class );
        when(prefsMock.getBoolean(ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD, ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD_DEFAULT)).thenReturn(true);
        when(prefsMock.get(ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR, ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR_DEFAULT)).thenReturn(TEST_FOLDER);
        final Path testFilePath = createTestFile(TEST_FOLDER);

        try (final MockedStatic<Paths> pathsStaticMock = Mockito.mockStatic(Paths.class);
                final MockedStatic<Generator> generatorMock = Mockito.mockStatic(Generator.class);
                final MockedStatic<NbPreferences> nbPreferencesStatic = Mockito.mockStatic(NbPreferences.class, Mockito.CALLS_REAL_METHODS)) {
            pathsStaticMock.when(() -> Paths.get(Mockito.anyString())).thenReturn(testFilePath);
            nbPreferencesStatic.when(() -> NbPreferences.forModule(ApplicationPreferenceKeys.class)).thenReturn(prefsMock);
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
        final Path filePath = Path.of(TEST_FOLDER).resolve(WebServer.CONSTELLATION_CLIENT);
        // set preferences to allow download true
        final Preferences prefsMock = mock(Preferences.class );
        when(prefsMock.getBoolean(ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD, ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD_DEFAULT)).thenReturn(true);

        final Path testFilePath = createTestFile(TEST_FOLDER);

        try (final MockedStatic<Paths> pathsStaticMock = Mockito.mockStatic(Paths.class); final MockedStatic<Generator> generatorMock = Mockito.mockStatic(Generator.class); final MockedStatic<StatusDisplayer> statusDisplayerStaticMock = Mockito.mockStatic(StatusDisplayer.class, Mockito.CALLS_REAL_METHODS); final MockedStatic<NbPreferences> nbPreferencesStatic = Mockito.mockStatic(NbPreferences.class, Mockito.CALLS_REAL_METHODS); final MockedStatic<WebServer> webserverMockStatic = Mockito.mockStatic(WebServer.class, Mockito.CALLS_REAL_METHODS); ) {
            pathsStaticMock.when(() -> Paths.get(Mockito.anyString())).thenReturn(testFilePath);
            generatorMock.when(Generator::getBaseDirectory).thenReturn("");
            nbPreferencesStatic.when(() -> NbPreferences.forModule(ApplicationPreferenceKeys.class)).thenReturn(prefsMock);
            
            webserverMockStatic.when(() -> WebServer.equalScripts(Mockito.any(File.class))).thenReturn(false);
            WebServer.downloadPythonClientToDir(new File(TEST_FOLDER));
            // Assert file was created
            assertTrue(Files.exists(filePath));
            // Verify functions were called
            pathsStaticMock.verify(() -> Paths.get(Mockito.anyString()), times(1));
            statusDisplayerStaticMock.verify(StatusDisplayer::getDefault, times(1));

            // Run the same function again, verify that function returned early as functions weren't called a second time
            webserverMockStatic.when(() -> WebServer.equalScripts(Mockito.any(File.class))).thenReturn(true);                        
            WebServer.downloadPythonClientToDir(new File(TEST_FOLDER));
            pathsStaticMock.verify(() -> Paths.get(Mockito.anyString()), times(1));
            statusDisplayerStaticMock.verify(StatusDisplayer::getDefault, times(1));
        }
    }
    
    /**
     * Function to create empty file to be used in tests.
     */
    private Path createTestFile(String dir) {
        final File testFile = new File(dir, "testFile.py");
        try {
            if (testFile.createNewFile()) {
                System.out.println("File created: " + testFile.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
        }

        return Paths.get(testFile.getAbsolutePath());
    }

    /**
     * Test of isWindows method, of class StartJupyterNotebookAction.
     */
    @Test
    public void testIsWindows() {
        System.out.println("isWindows");
        final boolean expResult = System.getProperty("os.name").toLowerCase().contains("win");
        final boolean result = WebServer.isWindows();
        assertEquals(result, expResult);
    }
}
