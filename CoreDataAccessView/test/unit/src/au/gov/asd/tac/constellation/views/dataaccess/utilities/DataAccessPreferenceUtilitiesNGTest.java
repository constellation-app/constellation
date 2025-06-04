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
package au.gov.asd.tac.constellation.views.dataaccess.utilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
public class DataAccessPreferenceUtilitiesNGTest {
    
    private static final String TITLE = "a test title";
    
    private Preferences preferences;

    @BeforeMethod
    public void setUpMethod() throws Exception {
        preferences = NbPreferences.forModule(DataAccessPreferenceUtilities.class);
        preferences.clear();
    }

    @Test
    public void setExpanded() {
        assertEquals(DataAccessPreferenceUtilities.isExpanded(TITLE, true), true);
        assertEquals(DataAccessPreferenceUtilities.isExpanded(TITLE, false), false);

        DataAccessPreferenceUtilities.setExpanded(TITLE, true);
        assertEquals(DataAccessPreferenceUtilities.isExpanded(TITLE, false), true);
    }

    @Test
    public void setFavourite() {
        assertEquals(DataAccessPreferenceUtilities.isfavourite(TITLE, true), true);
        assertEquals(DataAccessPreferenceUtilities.isfavourite(TITLE, false), false);

        DataAccessPreferenceUtilities.setFavourite(TITLE, true);
        assertEquals(DataAccessPreferenceUtilities.isfavourite(TITLE, false), true);
    }
    
    /**
     * Test of getDir method, of class DataAccessPreferenceKeys. Tests for empty
     * string returned from preferences
     */
    public void testGetDir() {
        System.out.println("testGetDir");

        final String key = "preferenceKey";
        preferences.put(key, "");

        final File expResult = null;
        final File result = DataAccessPreferenceUtilities.getDir(key);

        assertEquals(result, expResult);
    }

    /**
     * Test of getDir method, of class DataAccessPreferenceKeys. Tests correct
     * operation when a key and value exist and are valid
     */
    public void testGetDir2() {
        System.out.println("testGetDir2");

        final String key = "preferenceKey";
        preferences.put(key, "preferenceValue");

        // Create static mock of NbPreferences to return the preferences mock
        final File expResult = new File("preferenceValue");
        final Path expectedPath = Paths.get(expResult.getAbsolutePath());
        
        final File result = DataAccessPreferenceUtilities.getDir(key);
        final Path resultantPath = Paths.get(result.getAbsolutePath());

        assertEquals(resultantPath.normalize(), expectedPath.normalize());
    }

    /**
     * Test of getDir method, of class DataAccessPreferenceKeys. Tests when a
     * null string is parsed into the preferences object
     */
    public void testGetDir3() {
        System.out.println("testGetDir3");
        
        final String key = null;
        final String value = null;

        preferences.put(key, value);
        
        final File expResult = null;
        final File result = DataAccessPreferenceUtilities.getDir(key);

        assertEquals(result, expResult);
    }

    /**
     * Tests with an invalid d
     * @throws java.io.IOExceptionirectory.
     */
    @Test
    public void testIsSaveResultsEnabled() throws IOException {
        System.out.println("isSaveResultsEnabled");

        // mocking code from another test method
        final String key = "saveDataDir";
        File tempFile = null;
        try {
            tempFile = File.createTempFile("testfile", ".txt");

            final String path = tempFile == null ? "" : tempFile.getAbsolutePath();

            preferences.put(key, path);

            final File result = DataAccessPreferenceUtilities.getDir(key);

            // Verify mock is working correctly
            assertEquals(result, tempFile);

            // Verify no file is returned because it needs to be a directory
            final File expResult = null;
            assertEquals(DataAccessPreferenceUtilities.getDataAccessResultsDir(), expResult);

            // Test saveResultsEnabled with invalid DataAccessResultsDirectory
            boolean expResult2 = false;
            boolean result2 = DataAccessPreferenceUtilities.isSaveResultsEnabled();
            assertEquals(result2, expResult2);
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
        final String key = "saveDataDir";

        preferences.put(key, System.getProperty("user.dir"));

        final File expResult = new File(System.getProperty("user.dir"));
        final File result = DataAccessPreferenceUtilities.getDir(key);
        final Path expectedPath = Paths.get(expResult.getAbsolutePath());
        final Path resultantPath = Paths.get(result.getAbsolutePath());

        assertEquals(resultantPath.normalize(), expectedPath.normalize());

        assertEquals(DataAccessPreferenceUtilities.getDataAccessResultsDir(), expResult);

        // Test saveResultsEnabled with valid DataAccessResultsDirectory
        boolean expResult2 = true;
        boolean result2 = DataAccessPreferenceUtilities.isSaveResultsEnabled();
        assertEquals(result2, expResult2);
    }

    /**
     * Test of getDataAccessResultsDir method, of class
     * DataAccessPreferenceKeys. Testing when a file is requested instead of a
     * directory. Should return null as it is invalid.
     * @throws java.io.IOException
     */
    @Test
    public void testGetDataAccessResultsDir() throws IOException {
        System.out.println("getDataAccessResultsDir");

        final String key = "saveDataDir";
        File tempFile = null;
        try {
            tempFile = File.createTempFile("testfile", ".txt");

            final String path = tempFile == null ? "" : tempFile.getAbsolutePath();

            preferences.put(key, path);
                
            final File result = DataAccessPreferenceUtilities.getDir(key);

            // Verify mock is working correctly
            assertEquals(result, tempFile);

            // Verify no file is returned because it needs to be a directory
            final File expResult = null;
            assertEquals(DataAccessPreferenceUtilities.getDataAccessResultsDir(), expResult);
        } finally {
            // Cleanup
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * Test of getDataAccessResultsDir method, of class
     * DataAccessPreferenceUtilities.
     */
    @Test
    public void testGetDataAccessResultsDir2() {
        System.out.println("getDataAccessResultsDir2");

        final String key = "";

        // key is null in preferences
        
        final File expResult = null;
        final File result = DataAccessPreferenceUtilities.getDir(key);

        assertEquals(result, expResult);

        assertEquals(DataAccessPreferenceUtilities.getDataAccessResultsDir(), expResult);
    }

    /**
     * Test of getDataAccessResultsDir method, of class
     * DataAccessPreferenceUtilities.
     */
    @Test
    public void testGetDataAccessResultsDir3() {
        System.out.println("getDataAccessResultsDir3");

        final String key = "saveDataDir";

        preferences.put(key, System.getProperty("user.dir"));

        final File expResult = new File(System.getProperty("user.dir"));
        final Path expectedPath = Paths.get(expResult.getAbsolutePath());
        
        final File result = DataAccessPreferenceUtilities.getDir(key);
        final Path resultantPath = Paths.get(result.getAbsolutePath());

        assertEquals(resultantPath.normalize(), expectedPath.normalize());

        assertEquals(DataAccessPreferenceUtilities.getDataAccessResultsDir(), expResult);
    }

    /**
     * Test of getDataAccessResultsDirEx method, of class
     * DataAccessPreferenceUtilities. Test valid directory.
     *
     */
    @Test
    public void testGetDataAccessResultsDirEx() {
        System.out.println("getDataAccessResultsDirEx");

        final String key = "saveDataDir";

        preferences.put(key, System.getProperty("user.dir"));

        final File expResult = new File(System.getProperty("user.dir"));
        final File result = DataAccessPreferenceUtilities.getDir(key);
        final Path expectedPath = Paths.get(expResult.getAbsolutePath());
        final Path resultantPath = Paths.get(result.getAbsolutePath());

        assertEquals(resultantPath.normalize(), expectedPath.normalize());

        assertEquals(DataAccessPreferenceUtilities.getDataAccessResultsDir(), expResult);

        assertEquals(DataAccessPreferenceUtilities.getDataAccessResultsDirEx(), expResult);
    }

    /**
     * Test of getDataAccessResultsDirEx method, of class
     * DataAccessPreferenceKeys. Test invalid directory
     * @throws java.io.IOException
     */
    @Test
    public void testGetDataAccessResultsDirEx2() throws IOException {
        System.out.println("getDataAccessResultsDirEx2");

        final String key = "saveDataDir";
        File tempFile = null;
        try {
            tempFile = File.createTempFile("testfile", ".txt");

            final String path = tempFile == null ? "" : tempFile.getAbsolutePath();

            preferences.put(key, path);

            final File result = DataAccessPreferenceUtilities.getDir(key);

            // Verify mock is working correctly
            assertEquals(result, tempFile);

            // Verify no file is returned because it needs to be a directory
            final File expResult = null;
            assertEquals(DataAccessPreferenceUtilities.getDataAccessResultsDir(), expResult);

            // Verify that a file is returned because it does not care about directory.
            assertEquals(DataAccessPreferenceUtilities.getDataAccessResultsDirEx(), tempFile);
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

        final String previousDir = "prevSaveDataDir";
        final String currentDir = "saveDataDir";

        assertEquals(preferences.get(currentDir, null), null);
        assertEquals(preferences.get(previousDir, null), null);

        final File dir = null;
        DataAccessPreferenceUtilities.setDataAccessResultsDir(dir);

        assertEquals(preferences.get(currentDir, null), "");
        assertEquals(preferences.get(previousDir, null), null);
    }

    /**
     * Test of setDataAccessResultsDir method, of class
     * DataAccessPreferenceKeys. Test adding file dir to preferences. Should not
     * store a path to a file...
     * @throws java.io.IOException
     */
    @Test
    public void testSetDataAccessResultsDir2() throws IOException {
        System.out.println("setDataAccessResultsDir2");

        final String previousDir = "prevSaveDataDir";
        final String currentDir = "saveDataDir";

        assertEquals(preferences.get(currentDir, null), null);
        assertEquals(preferences.get(previousDir, null), null);

        File dir = null;
        try {
            dir = File.createTempFile("testfile", ".txt");

            DataAccessPreferenceUtilities.setDataAccessResultsDir(dir);

            assertEquals(preferences.get(currentDir, null), "");
            assertEquals(preferences.get(previousDir, null), null);

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

        final String previousDir = "prevSaveDataDir";
        final String currentDir = "saveDataDir";

        assertEquals(preferences.get(currentDir, null), null);
        assertEquals(preferences.get(previousDir, null), null);

        final File dir = new File(System.getProperty("user.dir"));
        DataAccessPreferenceUtilities.setDataAccessResultsDir(dir);
        assertEquals(preferences.get(currentDir, null), dir.getAbsolutePath());
        assertEquals(preferences.get(previousDir, null), "");

        final File dir2 = new File(System.getProperty("user.home"));
        DataAccessPreferenceUtilities.setDataAccessResultsDir(dir2);
        assertEquals(preferences.get(currentDir, null), dir2.getAbsolutePath());
        assertEquals(preferences.get(previousDir, null), dir.getAbsolutePath());
    }

    /**
     * Test of getPreviousDataAccessResultsDir method, of class
     * DataAccessPreferenceKeys. Testing when a file is requested instead of a
     * directory. Should return null as it is invalid.
     * @throws java.io.IOException
     */
    @Test
    public void testGetPreviousDataAccessResultsDir() throws IOException {
        System.out.println("getPreviousDataAccessResultsDir");

        final String key = "prevSaveDataDir";
        File tempFile = null;
        try {
            tempFile = File.createTempFile("testfile", ".txt");

            final String path = tempFile == null ? "" : tempFile.getAbsolutePath();

            preferences.put(key, path);

            final File result = DataAccessPreferenceUtilities.getDir(key);

            // Verify mock is working correctly
            assertEquals(result, tempFile);

            // Verify no file is returned because it needs to be a directory
            final File expResult = null;
            assertEquals(DataAccessPreferenceUtilities.getPreviousDataAccessResultsDir(), expResult);
        } finally {
            // Cleanup
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * Test of getPreviousDataAccessResultsDir method, of class
     * DataAccessPreferenceUtilities.
     */
    @Test
    public void testGetPreviousDataAccessResultsDir2() {
        System.out.println("getPreviousDataAccessResultsDir2");

        final String key = "";

        final File expResult = null;
        final File result = DataAccessPreferenceUtilities.getDir(key);

        assertEquals(result, expResult);

        assertEquals(DataAccessPreferenceUtilities.getPreviousDataAccessResultsDir(), expResult);
    }

    /**
     * Test of getPreviousDataAccessResultsDir method, of class
     * DataAccessPreferenceUtilities.
     */
    @Test
    public void testGetPreviousDataAccessResultsDir3() {
        System.out.println("getPreviousDataAccessResultsDir3");

        final String key = "prevSaveDataDir";

        preferences.put(key, System.getProperty("user.dir"));

        final File expResult = new File(System.getProperty("user.dir"));
        final File result = DataAccessPreferenceUtilities.getDir(key);
        final Path expectedPath = Paths.get(expResult.getAbsolutePath());
        final Path resultantPath = Paths.get(result.getAbsolutePath());

        assertEquals(resultantPath.normalize(), expectedPath.normalize());

        assertEquals(DataAccessPreferenceUtilities.getPreviousDataAccessResultsDir(), expResult);
    }

    /**
     * Test of isDeselectPluginsOnExecuteEnabled method, of class
     * DataAccessPreferenceKeys. Tests both true and false setting of the
     * preference, as well as unset values.
     */
    @Test
    public void testIsDeselectPluginsOnExecuteEnabled() {
        System.out.println("isDeselectPluginsOnExecuteEnabled");
        final String preferenceKey = "deselectPluginsOnExecute";

        // Verify preference does not exist and also returns the correct default of false
        assertEquals(preferences.getBoolean(preferenceKey, false), false);
        assertEquals(DataAccessPreferenceUtilities.isDeselectPluginsOnExecuteEnabled(), false);
        assertEquals(preferences.getBoolean(preferenceKey, false), false);

        // verify preference is set correctly
        preferences.putBoolean(preferenceKey, true);
        assertEquals(preferences.getBoolean(preferenceKey, false), true);

        // Verify method call returns correct value
        assertEquals(DataAccessPreferenceUtilities.isDeselectPluginsOnExecuteEnabled(), true);

        // put in a false value to check toggling, recheck
        preferences.putBoolean(preferenceKey, false);
        assertEquals(preferences.getBoolean(preferenceKey, false), false);
        assertEquals(DataAccessPreferenceUtilities.isDeselectPluginsOnExecuteEnabled(), false);
        assertEquals(preferences.getBoolean(preferenceKey, false), false);

        // Verify method call returns correct value
        assertEquals(DataAccessPreferenceUtilities.isDeselectPluginsOnExecuteEnabled(), false);

    }

    /**
     * Test of setDeselectPluginsOnExecute method, of class
     * DataAccessPreferenceKeys. Test setting true and false, as well as unset
     * return value
     */
    @Test
    public void testSetDeselectPluginsOnExecute() {
        System.out.println("setDeselectPluginsOnExecute");

        final String preferenceKey = "deselectPluginsOnExecute";

        boolean expValue = false;

        // Verify preference does not exist and also returns the correct default of false
        assertEquals(preferences.getBoolean(preferenceKey, false), expValue);
        DataAccessPreferenceUtilities.setDeselectPluginsOnExecute(expValue);
        assertEquals(preferences.getBoolean(preferenceKey, false), expValue);

        // Test setting true value
        expValue = true;
        DataAccessPreferenceUtilities.setDeselectPluginsOnExecute(expValue);
        assertEquals(preferences.getBoolean(preferenceKey, false), expValue);

        // Test toggling
        expValue = false;
        DataAccessPreferenceUtilities.setDeselectPluginsOnExecute(expValue);
        assertEquals(preferences.getBoolean(preferenceKey, false), expValue);
    }
}
