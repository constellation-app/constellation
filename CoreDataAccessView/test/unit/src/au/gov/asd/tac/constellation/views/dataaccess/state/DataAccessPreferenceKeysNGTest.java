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
package au.gov.asd.tac.constellation.views.dataaccess.state;

import static au.gov.asd.tac.constellation.views.dataaccess.state.DataAccessPreferenceKeys.DESELECT_PLUGINS_ON_EXECUTE_PREF;
import static au.gov.asd.tac.constellation.views.dataaccess.state.DataAccessPreferenceKeys.PREVIOUS_DATA_DIR_PREF;
import static au.gov.asd.tac.constellation.views.dataaccess.state.DataAccessPreferenceKeys.SAVE_DATA_DIR_PREF;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.Preferences;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
public class DataAccessPreferenceKeysNGTest {

    Preferences prefs;

    public DataAccessPreferenceKeysNGTest() {
    }

    @BeforeClass
    public void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Before each method, open the mocks.
        MockitoAnnotations.openMocks(this);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Check usage is correct before moving on
        validateMockitoUsage();
    }

    /**
     * Test of getDir method, of class DataAccessPreferenceKeys. Tests for empty
     * string returned from preferences
     */
    public void testGetDir() {
        System.out.println("testGetDir");
        final String key = "preferenceKey";

        prefs = mock(Preferences.class);
        when(prefs.get(Mockito.eq(key), Mockito.anyString())).thenReturn("");

        // Create static mock of NbPreferences to return the preferences mock
        try (MockedStatic<NbPreferences> mockedStatic = Mockito.mockStatic(NbPreferences.class)) {
            mockedStatic.when(() -> NbPreferences.forModule(Mockito.eq(DataAccessPreferenceKeys.class))).thenReturn(prefs);

            final File expResult = null;
            final File result = DataAccessPreferenceKeys.getDir(key);

            assertEquals(result, expResult);
        }
    }

    /**
     * Test of getDir method, of class DataAccessPreferenceKeys. Tests correct
     * operation when a key and value exist and are valid
     */
    public void testGetDir2() {
        System.out.println("testGetDir2");
        final String key = "preferenceKey";

        prefs = mock(Preferences.class);
        when(prefs.get(Mockito.eq(key), Mockito.anyString())).thenReturn("preferenceValue");

        // Create static mock of NbPreferences to return the preferences mock
        try (MockedStatic<NbPreferences> mockedStatic = Mockito.mockStatic(NbPreferences.class)) {
            mockedStatic.when(() -> NbPreferences.forModule(Mockito.eq(DataAccessPreferenceKeys.class))).thenReturn(prefs);

            final File expResult = new File("preferenceValue");
            final File result = DataAccessPreferenceKeys.getDir(key);
            final Path expectedPath = Paths.get(expResult.getAbsolutePath());
            final Path resultantPath = Paths.get(result.getAbsolutePath());

            assertEquals(resultantPath.normalize(), expectedPath.normalize());
        }
    }

    /**
     * Test of getDir method, of class DataAccessPreferenceKeys. Tests when a
     * null string is parsed into the preferences object
     */
    public void testGetDir3() {
        System.out.println("testGetDir3");
        final String key = null;
        final String value = null;

        prefs = mock(Preferences.class);
        when(prefs.get(Mockito.eq(key), Mockito.anyString())).thenReturn(value);

        // Create static mock of NbPreferences to return the preferences mock
        try (MockedStatic<NbPreferences> mockedStatic = Mockito.mockStatic(NbPreferences.class)) {
            mockedStatic.when(() -> NbPreferences.forModule(Mockito.eq(DataAccessPreferenceKeys.class))).thenReturn(prefs);

            final File expResult = null;
            final File result = DataAccessPreferenceKeys.getDir(key);

            assertEquals(result, expResult);
        }
    }

    /**
     * Tests with an invalid directory.
     */
    @Test
    public void testIsSaveResultsEnabled() {
        System.out.println("isSaveResultsEnabled");

        // mocking code from another test method
        final String key = SAVE_DATA_DIR_PREF;
        File tempFile = null;
        try {
            tempFile = File.createTempFile("testfile", ".txt");

            final String path = tempFile == null ? "" : tempFile.getAbsolutePath();

            prefs = mock(Preferences.class);
            when(prefs.get(Mockito.eq(key), Mockito.anyString())).thenReturn(path);

            // Create static mock of NbPreferences to return the preferences mock
            try (MockedStatic<NbPreferences> mockedStatic = Mockito.mockStatic(NbPreferences.class)) {
                mockedStatic.when(() -> NbPreferences.forModule(Mockito.eq(DataAccessPreferenceKeys.class))).thenReturn(prefs);
                mockedStatic.verify(() -> NbPreferences.forModule(Mockito.eq(DataAccessPreferenceKeys.class)), Mockito.never());

                final File result = DataAccessPreferenceKeys.getDir(key);
                mockedStatic.verify(() -> NbPreferences.forModule(Mockito.eq(DataAccessPreferenceKeys.class)), times(1));

                // Verify mock is working correctly
                assertEquals(result, tempFile);

                // Verify no file is returned because it needs to be a directory
                final File expResult = null;
                assertEquals(DataAccessPreferenceKeys.getDataAccessResultsDir(), expResult);

                // Test saveResultsEnabled with invalid DataAccessResultsDirectory
                boolean expResult2 = false;
                boolean result2 = DataAccessPreferenceKeys.isSaveResultsEnabled();
                assertEquals(result2, expResult2);
            }
        } catch (final IOException ex) {
            // Fail when test cannot create a file
            Exceptions.printStackTrace(ex);
            fail();
        } finally {
            // Cleanup
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * Test of isSaveResultsEnabled method, of class DataAccessPreferenceKeys.
     * Tests with a valid directory
     */
    @Test
    public void testIsSaveResultsEnabled2() {
        System.out.println("isSaveResultsEnabled2");

        // mocking code from another test method
        final String key = SAVE_DATA_DIR_PREF;

        prefs = mock(Preferences.class);
        when(prefs.get(Mockito.eq(key), Mockito.anyString())).thenReturn(System.getProperty("user.dir"));

        // Create static mock of NbPreferences to return the preferences mock
        try (MockedStatic<NbPreferences> mockedStatic = Mockito.mockStatic(NbPreferences.class)) {
            mockedStatic.when(() -> NbPreferences.forModule(Mockito.eq(DataAccessPreferenceKeys.class))).thenReturn(prefs);

            final File expResult = new File(System.getProperty("user.dir"));
            final File result = DataAccessPreferenceKeys.getDir(key);
            final Path expectedPath = Paths.get(expResult.getAbsolutePath());
            final Path resultantPath = Paths.get(result.getAbsolutePath());

            assertEquals(resultantPath.normalize(), expectedPath.normalize());

            assertEquals(DataAccessPreferenceKeys.getDataAccessResultsDir(), expResult);

            // Test saveResultsEnabled with valid DataAccessResultsDirectory
            boolean expResult2 = true;
            boolean result2 = DataAccessPreferenceKeys.isSaveResultsEnabled();
            assertEquals(result2, expResult2);

        }

    }

    /**
     * Test of getDataAccessResultsDir method, of class
     * DataAccessPreferenceKeys. Testing when a file is requested instead of a
     * directory. Should return null as it is invalid.
     */
    @Test
    public void testGetDataAccessResultsDir() {
        System.out.println("getDataAccessResultsDir");

        final String key = SAVE_DATA_DIR_PREF;
        File tempFile = null;
        try {
            tempFile = File.createTempFile("testfile", ".txt");

            final String path = tempFile == null ? "" : tempFile.getAbsolutePath();

            prefs = mock(Preferences.class);
            when(prefs.get(Mockito.eq(key), Mockito.anyString())).thenReturn(path);

            // Create static mock of NbPreferences to return the preferences mock
            try (MockedStatic<NbPreferences> mockedStatic = Mockito.mockStatic(NbPreferences.class)) {
                mockedStatic.when(() -> NbPreferences.forModule(Mockito.eq(DataAccessPreferenceKeys.class))).thenReturn(prefs);
                mockedStatic.verify(() -> NbPreferences.forModule(Mockito.eq(DataAccessPreferenceKeys.class)), Mockito.never());

                final File result = DataAccessPreferenceKeys.getDir(key);
                mockedStatic.verify(() -> NbPreferences.forModule(Mockito.eq(DataAccessPreferenceKeys.class)), times(1));

                // Verify mock is working correctly
                assertEquals(result, tempFile);

                // Verify no file is returned because it needs to be a directory
                final File expResult = null;
                assertEquals(DataAccessPreferenceKeys.getDataAccessResultsDir(), expResult);
            }
        } catch (final IOException ex) {
            // Fail when test cannot create a file
            Exceptions.printStackTrace(ex);
            fail();
        } finally {
            // Cleanup
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * Test of getDataAccessResultsDir method, of class
     * DataAccessPreferenceKeys.
     */
    @Test
    public void testGetDataAccessResultsDir2() {
        System.out.println("getDataAccessResultsDir2");

        final String key = "";

        prefs = mock(Preferences.class);
        when(prefs.get(Mockito.eq(key), Mockito.anyString())).thenReturn(null);

        // Create static mock of NbPreferences to return the preferences mock
        try (MockedStatic<NbPreferences> mockedStatic = Mockito.mockStatic(NbPreferences.class)) {
            mockedStatic.when(() -> NbPreferences.forModule(Mockito.eq(DataAccessPreferenceKeys.class))).thenReturn(prefs);
            mockedStatic.verify(() -> NbPreferences.forModule(Mockito.eq(DataAccessPreferenceKeys.class)), Mockito.never());
            final File expResult = null;
            final File result = DataAccessPreferenceKeys.getDir(key);
            mockedStatic.verify(() -> NbPreferences.forModule(Mockito.eq(DataAccessPreferenceKeys.class)), times(1));

            assertEquals(result, expResult);

            assertEquals(DataAccessPreferenceKeys.getDataAccessResultsDir(), expResult);
        }
    }

    /**
     * Test of getDataAccessResultsDir method, of class
     * DataAccessPreferenceKeys.
     */
    @Test
    public void testGetDataAccessResultsDir3() {
        System.out.println("getDataAccessResultsDir3");

        final String key = SAVE_DATA_DIR_PREF;

        prefs = mock(Preferences.class);
        when(prefs.get(Mockito.eq(key), Mockito.anyString())).thenReturn(System.getProperty("user.dir"));

        // Create static mock of NbPreferences to return the preferences mock
        try (MockedStatic<NbPreferences> mockedStatic = Mockito.mockStatic(NbPreferences.class)) {
            mockedStatic.when(() -> NbPreferences.forModule(Mockito.eq(DataAccessPreferenceKeys.class))).thenReturn(prefs);

            final File expResult = new File(System.getProperty("user.dir"));
            final File result = DataAccessPreferenceKeys.getDir(key);
            final Path expectedPath = Paths.get(expResult.getAbsolutePath());
            final Path resultantPath = Paths.get(result.getAbsolutePath());

            assertEquals(resultantPath.normalize(), expectedPath.normalize());

            assertEquals(DataAccessPreferenceKeys.getDataAccessResultsDir(), expResult);
        }
    }

    /**
     * Test of getDataAccessResultsDirEx method, of class
     * DataAccessPreferenceKeys.Test valid directory
     *
     */
    @Test
    public void testGetDataAccessResultsDirEx() {

        System.out.println("getDataAccessResultsDirEx");

        final String key = SAVE_DATA_DIR_PREF;

        prefs = mock(Preferences.class);
        when(prefs.get(Mockito.eq(key), Mockito.anyString())).thenReturn(System.getProperty("user.dir"));

        // Create static mock of NbPreferences to return the preferences mock
        try (MockedStatic<NbPreferences> mockedStatic = Mockito.mockStatic(NbPreferences.class)) {
            mockedStatic.when(() -> NbPreferences.forModule(Mockito.eq(DataAccessPreferenceKeys.class))).thenReturn(prefs);

            final File expResult = new File(System.getProperty("user.dir"));
            final File result = DataAccessPreferenceKeys.getDir(key);
            final Path expectedPath = Paths.get(expResult.getAbsolutePath());
            final Path resultantPath = Paths.get(result.getAbsolutePath());

            assertEquals(resultantPath.normalize(), expectedPath.normalize());

            assertEquals(DataAccessPreferenceKeys.getDataAccessResultsDir(), expResult);

            assertEquals(DataAccessPreferenceKeys.getDataAccessResultsDirEx(), expResult);
        }
    }

    /**
     * Test of getDataAccessResultsDirEx method, of class
     * DataAccessPreferenceKeys. Test invalid directory
     */
    @Test
    public void testGetDataAccessResultsDirEx2() {

        System.out.println("getDataAccessResultsDirEx2");

        final String key = SAVE_DATA_DIR_PREF;
        File tempFile = null;
        try {
            tempFile = File.createTempFile("testfile", ".txt");

            final String path = tempFile == null ? "" : tempFile.getAbsolutePath();

            prefs = mock(Preferences.class);
            when(prefs.get(Mockito.eq(key), Mockito.anyString())).thenReturn(path);

            // Create static mock of NbPreferences to return the preferences mock
            try (MockedStatic<NbPreferences> mockedStatic = Mockito.mockStatic(NbPreferences.class)) {
                mockedStatic.when(() -> NbPreferences.forModule(Mockito.eq(DataAccessPreferenceKeys.class))).thenReturn(prefs);
                mockedStatic.verify(() -> NbPreferences.forModule(Mockito.eq(DataAccessPreferenceKeys.class)), Mockito.never());

                final File result = DataAccessPreferenceKeys.getDir(key);
                mockedStatic.verify(() -> NbPreferences.forModule(Mockito.eq(DataAccessPreferenceKeys.class)), times(1));

                // Verify mock is working correctly
                assertEquals(result, tempFile);

                // Verify no file is returned because it needs to be a directory
                final File expResult = null;
                assertEquals(DataAccessPreferenceKeys.getDataAccessResultsDir(), expResult);

                // Verify that a file is returned because it does not care about directory.
                assertEquals(DataAccessPreferenceKeys.getDataAccessResultsDirEx(), tempFile);
            }
        } catch (final IOException ex) {
            // Fail when test cannot create a file
            Exceptions.printStackTrace(ex);
            fail();
        } finally {
            // Cleanup
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * Test of setDataAccessResultsDir method, of class
     * DataAccessPreferenceKeys. Test adding null file dir to preferences.
     * Should only add an empty string.
     */
    @Test
    public void testSetDataAccessResultsDir() {
        System.out.println("setDataAccessResultsDir");

        final String previousDir = PREVIOUS_DATA_DIR_PREF;
        final String currentDir = SAVE_DATA_DIR_PREF;

        final Preferences preferences = NbPreferences.forModule(DataAccessPreferenceKeys.class);
        preferences.remove(PREVIOUS_DATA_DIR_PREF);
        preferences.remove(SAVE_DATA_DIR_PREF);

        assertEquals(preferences.get(currentDir, null), null);
        assertEquals(preferences.get(previousDir, null), null);

        final File dir = null;
        DataAccessPreferenceKeys.setDataAccessResultsDir(dir);

        assertEquals(preferences.get(currentDir, null), "");
        assertEquals(preferences.get(previousDir, null), null);
    }

    /**
     * Test of setDataAccessResultsDir method, of class
     * DataAccessPreferenceKeys. Test adding file dir to preferences. Should not
     * store a path to a file...
     */
    @Test
    public void testSetDataAccessResultsDir2() {
        System.out.println("setDataAccessResultsDir2");

        final String previousDir = PREVIOUS_DATA_DIR_PREF;
        final String currentDir = SAVE_DATA_DIR_PREF;

        final Preferences preferences = NbPreferences.forModule(DataAccessPreferenceKeys.class);
        preferences.remove(PREVIOUS_DATA_DIR_PREF);
        preferences.remove(SAVE_DATA_DIR_PREF);

        assertEquals(preferences.get(currentDir, null), null);
        assertEquals(preferences.get(previousDir, null), null);

        File dir = null;
        try {
            dir = File.createTempFile("testfile", ".txt");

            DataAccessPreferenceKeys.setDataAccessResultsDir(dir);

            assertEquals(preferences.get(currentDir, null), "");
            assertEquals(preferences.get(previousDir, null), null);

        } catch (final IOException ex) {
            Exceptions.printStackTrace(ex);
            fail();
        } finally {
            // Cleanup
            if (dir != null && dir.exists()) {
                dir.delete();
            }
        }
    }

    /**
     * Test of setDataAccessResultsDir method, of class
     * DataAccessPreferenceKeys. Test adding dir to preferences. Should store
     * the path
     */
    @Test
    public void testSetDataAccessResultsDir3() {
        System.out.println("setDataAccessResultsDir3");

        final String previousDir = PREVIOUS_DATA_DIR_PREF;
        final String currentDir = SAVE_DATA_DIR_PREF;

        final Preferences preferences = NbPreferences.forModule(DataAccessPreferenceKeys.class);

        preferences.remove(PREVIOUS_DATA_DIR_PREF);
        preferences.remove(SAVE_DATA_DIR_PREF);

        assertEquals(preferences.get(currentDir, null), null);
        assertEquals(preferences.get(previousDir, null), null);

        final File dir = new File(System.getProperty("user.dir"));
        DataAccessPreferenceKeys.setDataAccessResultsDir(dir);
        assertEquals(preferences.get(currentDir, null), dir.getAbsolutePath());
        assertEquals(preferences.get(previousDir, null), "");

        final File dir2 = new File(System.getProperty("user.home"));
        DataAccessPreferenceKeys.setDataAccessResultsDir(dir2);
        assertEquals(preferences.get(currentDir, null), dir2.getAbsolutePath());
        assertEquals(preferences.get(previousDir, null), dir.getAbsolutePath());
    }

    /**
     * Test of getPreviousDataAccessResultsDir method, of class
     * DataAccessPreferenceKeys. Testing when a file is requested instead of a
     * directory. Should return null as it is invalid.
     */
    @Test
    public void testGetPreviousDataAccessResultsDir() {
        System.out.println("getPreviousDataAccessResultsDir");

        final String key = PREVIOUS_DATA_DIR_PREF;
        File tempFile = null;
        try {
            tempFile = File.createTempFile("testfile", ".txt");

            final String path = tempFile == null ? "" : tempFile.getAbsolutePath();

            prefs = mock(Preferences.class);
            when(prefs.get(Mockito.eq(key), Mockito.anyString())).thenReturn(path);

            // Create static mock of NbPreferences to return the preferences mock
            try (MockedStatic<NbPreferences> mockedStatic = Mockito.mockStatic(NbPreferences.class)) {
                mockedStatic.when(() -> NbPreferences.forModule(Mockito.eq(DataAccessPreferenceKeys.class))).thenReturn(prefs);
                mockedStatic.verify(() -> NbPreferences.forModule(Mockito.eq(DataAccessPreferenceKeys.class)), Mockito.never());

                final File result = DataAccessPreferenceKeys.getDir(key);
                mockedStatic.verify(() -> NbPreferences.forModule(Mockito.eq(DataAccessPreferenceKeys.class)), times(1));

                // Verify mock is working correctly
                assertEquals(result, tempFile);

                // Verify no file is returned because it needs to be a directory
                final File expResult = null;
                assertEquals(DataAccessPreferenceKeys.getPreviousDataAccessResultsDir(), expResult);
            }
        } catch (final IOException ex) {
            // Fail when test cannot create a file
            Exceptions.printStackTrace(ex);
            fail();
        } finally {
            // Cleanup
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * Test of getPreviousDataAccessResultsDir method, of class
     * DataAccessPreferenceKeys.
     */
    @Test
    public void testGetPreviousDataAccessResultsDir2() {
        System.out.println("getPreviousDataAccessResultsDir2");

        final String key = "";

        prefs = mock(Preferences.class);
        when(prefs.get(Mockito.eq(key), Mockito.anyString())).thenReturn(null);

        // Create static mock of NbPreferences to return the preferences mock
        try (MockedStatic<NbPreferences> mockedStatic = Mockito.mockStatic(NbPreferences.class)) {
            mockedStatic.when(() -> NbPreferences.forModule(Mockito.eq(DataAccessPreferenceKeys.class))).thenReturn(prefs);
            mockedStatic.verify(() -> NbPreferences.forModule(Mockito.eq(DataAccessPreferenceKeys.class)), Mockito.never());
            final File expResult = null;
            final File result = DataAccessPreferenceKeys.getDir(key);
            mockedStatic.verify(() -> NbPreferences.forModule(Mockito.eq(DataAccessPreferenceKeys.class)), times(1));

            assertEquals(result, expResult);

            assertEquals(DataAccessPreferenceKeys.getPreviousDataAccessResultsDir(), expResult);
        }
    }

    /**
     * Test of getPreviousDataAccessResultsDir method, of class
     * DataAccessPreferenceKeys.
     */
    @Test
    public void testGetPreviousDataAccessResultsDir3() {
        System.out.println("getPreviousDataAccessResultsDir3");

        final String key = PREVIOUS_DATA_DIR_PREF;

        prefs = mock(Preferences.class);
        when(prefs.get(Mockito.eq(key), Mockito.anyString())).thenReturn(System.getProperty("user.dir"));

        // Create static mock of NbPreferences to return the preferences mock
        try (MockedStatic<NbPreferences> mockedStatic = Mockito.mockStatic(NbPreferences.class)) {
            mockedStatic.when(() -> NbPreferences.forModule(Mockito.eq(DataAccessPreferenceKeys.class))).thenReturn(prefs);

            final File expResult = new File(System.getProperty("user.dir"));
            final File result = DataAccessPreferenceKeys.getDir(key);
            final Path expectedPath = Paths.get(expResult.getAbsolutePath());
            final Path resultantPath = Paths.get(result.getAbsolutePath());

            assertEquals(resultantPath.normalize(), expectedPath.normalize());

            assertEquals(DataAccessPreferenceKeys.getPreviousDataAccessResultsDir(), expResult);
        }
    }

    /**
     * Test of isDeselectPluginsOnExecuteEnabled method, of class
     * DataAccessPreferenceKeys. Tests both true and false setting of the
     * preference, as well as unset values.
     */
    @Test
    public void testIsDeselectPluginsOnExecuteEnabled() {
        System.out.println("isDeselectPluginsOnExecuteEnabled");
        final String preferenceKey = DESELECT_PLUGINS_ON_EXECUTE_PREF;

        final Preferences preferences = NbPreferences.forModule(DataAccessPreferenceKeys.class);

        preferences.remove(preferenceKey);

        // Verify preference does not exist and also returns the correct default of false
        assertEquals(preferences.getBoolean(preferenceKey, false), false);
        assertEquals(DataAccessPreferenceKeys.isDeselectPluginsOnExecuteEnabled(), false);
        assertEquals(preferences.getBoolean(preferenceKey, false), false);

        // verify preference is set correctly
        preferences.putBoolean(preferenceKey, true);
        assertEquals(preferences.getBoolean(preferenceKey, false), true);

        // Verify method call returns correct value
        assertEquals(DataAccessPreferenceKeys.isDeselectPluginsOnExecuteEnabled(), true);

        // put in a false value to check toggling, recheck
        preferences.putBoolean(preferenceKey, false);
        assertEquals(preferences.getBoolean(preferenceKey, false), false);
        assertEquals(DataAccessPreferenceKeys.isDeselectPluginsOnExecuteEnabled(), false);
        assertEquals(preferences.getBoolean(preferenceKey, false), false);

        // Verify method call returns correct value
        assertEquals(DataAccessPreferenceKeys.isDeselectPluginsOnExecuteEnabled(), false);

    }

    /**
     * Test of setDeselectPluginsOnExecute method, of class
     * DataAccessPreferenceKeys. Test setting true and false, as well as unset
     * return value
     */
    @Test
    public void testSetDeselectPluginsOnExecute() {
        System.out.println("setDeselectPluginsOnExecute");

        final String preferenceKey = DESELECT_PLUGINS_ON_EXECUTE_PREF;

        final Preferences preferences = NbPreferences.forModule(DataAccessPreferenceKeys.class);

        boolean expValue = false;
        preferences.remove(preferenceKey);

        // Verify preference does not exist and also returns the correct default of false
        assertEquals(preferences.getBoolean(preferenceKey, false), expValue);
        DataAccessPreferenceKeys.setDeselectPluginsOnExecute(expValue);
        assertEquals(preferences.getBoolean(preferenceKey, false), expValue);

        // Test setting true value
        expValue = true;
        DataAccessPreferenceKeys.setDeselectPluginsOnExecute(expValue);
        assertEquals(preferences.getBoolean(preferenceKey, false), expValue);

        // Test toggling
        expValue = false;
        DataAccessPreferenceKeys.setDeselectPluginsOnExecute(expValue);
        assertEquals(preferences.getBoolean(preferenceKey, false), expValue);
    }
}
