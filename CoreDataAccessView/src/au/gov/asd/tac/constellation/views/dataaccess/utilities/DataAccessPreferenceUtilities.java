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
package au.gov.asd.tac.constellation.views.dataaccess.utilities;

import au.gov.asd.tac.constellation.utilities.log.LogPreferences;
import java.io.File;
import java.util.prefs.Preferences;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbPreferences;

/**
 *
 * @author cygnus_x-1
 */
public class DataAccessPreferenceUtilities {
    private static final Preferences PREFERENCES = NbPreferences.forModule(DataAccessPreferenceUtilities.class);
    
    private static final String SAVE_DATA_DIR_PREF = "saveDataDir";
    private static final String PREVIOUS_DATA_DIR_PREF = "prevSaveDataDir";
    private static final String DESELECT_PLUGINS_ON_EXECUTE_PREF = "deselectPluginsOnExecute";
    
    private static final String EXPAND = "Expand";
    private static final String FAVOURITE = "Favourite";
    private static final String STRING_STRING_FORMAT = "%s.%s";
    
    private DataAccessPreferenceUtilities() {
        throw new IllegalStateException("Utility class");
    }
    
    /**
     * Return whether the save results is enabled or not
     *
     * @return True if its enabled, False otherwise
     */
    public static boolean isSaveResultsEnabled() {
        return getDataAccessResultsDir() != null;
    }

    /**
     * The directory where data access results should be written to, or null if
     * it is unset or doesn't exist.
     * <p>
     * If a directory is specified, results should be written. To not write
     * results, unset the directory.
     *
     * @return The directory where data access results should be written to;
     * null if a directory is unset or does not exist.
     */
    public static File getDataAccessResultsDir() {
        final File f = getDir(SAVE_DATA_DIR_PREF);

        return f != null && f.isDirectory() ? f : null;
    }

    /**
     * The directory where data access results should be written to, or null if
     * it is unset.
     *
     * @return The directory where data access results should be written to.
     */
    public static File getDataAccessResultsDirEx() {
        return getDir(SAVE_DATA_DIR_PREF);
    }

    /**
     * Set the directory where data access results should be written to.
     * <p>
     * Use null to stop writing results.
     * <p>
     * If the path is a directory and is non-null, it will be saved separately
     * so it can be retrieved and used as a default the next time the user wants
     * to specify a directory. If the file is not a directory, an empty string
     * will be stored as the directory, and the previous directory will not be
     * updated.
     *
     * @param dir A directory to write data access results to, or null to not
     * write results.
     */
    public static void setDataAccessResultsDir(final File dir) {
        if (dir != null && dir.isDirectory()) {
            PREFERENCES.put(
                    PREVIOUS_DATA_DIR_PREF,
                    PREFERENCES.get(SAVE_DATA_DIR_PREF, StringUtils.EMPTY)
            );
        }
        PREFERENCES.put(
                SAVE_DATA_DIR_PREF, 
                dir == null || !dir.isDirectory() ? StringUtils.EMPTY : dir.getAbsolutePath()
        );
    }

    /**
     * The most recently used directory that data access results were written
     * to.
     * <p>
     * This is the directory that was most recently used to write data access
     * results to. useful for specifying a default when the user wants to
     * specify a new directory.
     *
     * @return The most recently used directory that data access results were
     * written to; null if there was no such directory, or the directory does
     * not exist.
     */
    public static File getPreviousDataAccessResultsDir() {
        final File f = getDir(PREVIOUS_DATA_DIR_PREF);

        return f != null && f.isDirectory() ? f : null;
    }

    /**
     * Whether the Deselect plugins on go preference is enabled
     *
     * @return The current preference
     */
    public static boolean isDeselectPluginsOnExecuteEnabled() {
        return PREFERENCES.getBoolean(DESELECT_PLUGINS_ON_EXECUTE_PREF, false);
    }

    /**
     * Set the new preference for whether the deselect plugins on go preference
     * is enabled
     *
     * @param checkChanged What the preference has been changed to
     *
     */
    public static void setDeselectPluginsOnExecute(boolean checkChanged) {
        PREFERENCES.putBoolean(DESELECT_PLUGINS_ON_EXECUTE_PREF, checkChanged);
    }
    
    /**
     * Whether the Connection Logging preference is enabled
     *
     * @return The current preference
     */
    public static boolean isConnectionLoggingEnabled() {
        return LogPreferences.isConnectionLoggingEnabled();
    }

    /**
     * Set the new preference for whether the Connection Logging preference
     * is enabled
     *
     * @param checkChanged What the preference has been changed to
     *
     */
    public static void setConnectionLogging(boolean checkChanged) {
        LogPreferences.setConnectionLogging(checkChanged);
    }

    /**
     * Set an expanded preference.
     * <p>
     * Typically called from an expandedProperty ChangeListener.
     *
     * @param title Preference title.
     * @param isExpanded Boolean value.
     */
    public static void setExpanded(final String title, final boolean isExpanded) {
        PREFERENCES.putBoolean(
                String.format(STRING_STRING_FORMAT, EXPAND, title),
                isExpanded
        );
    }

    /**
     * Retrieve an expanded preference.
     *
     * @param title Preference title.
     * @param defaultExpanded If the preference is not yet set, use this value
     * as the default.
     *
     * @return The value of the preference.
     */
    public static boolean isExpanded(final String title, final boolean defaultExpanded) {
        return PREFERENCES.getBoolean(
                String.format(STRING_STRING_FORMAT, EXPAND, title),
                defaultExpanded
        );
    }

    /**
     * 
     * @param title
     * @param isExpanded 
     */
    public static void setFavourite(final String title, final boolean isExpanded) {
        PREFERENCES.putBoolean(
                String.format(STRING_STRING_FORMAT, FAVOURITE, title),
                isExpanded
        );
    }

    /**
     * 
     * @param title
     * @param defaultExpanded
     * @return 
     */
    public static boolean isfavourite(final String title, final boolean defaultExpanded) {
        return PREFERENCES.getBoolean(
                String.format(STRING_STRING_FORMAT, FAVOURITE, title),
                defaultExpanded
        );
    }
    
    /**
     * The preference as a directory; null if the directory is not set.
     *
     * @param pref The preference.
     *
     * @return The preference as a directory; null if the directory is not set.
     */
    protected static File getDir(final String pref) {
        final String s = PREFERENCES.get(pref, StringUtils.EMPTY);

        return StringUtils.isNotEmpty(s) ? new File(s) : null;
    }
}
