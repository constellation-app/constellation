/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.preferences;

import au.gov.asd.tac.constellation.preferences.rest.RestDirectory;
import java.io.File;
import java.util.prefs.Preferences;
import org.openide.util.Lookup;

/**
 * Keys used to access graph preferences.
 *
 * @author algol
 */
public final class ApplicationPreferenceKeys {

    /**
     * The directory where various user data is stored.
     * <p>
     * Note that this is separate from NbPreferences, which stores stuff in a
     * mysterious place that the user doesn't necessarily know about. Rather,
     * this is a directory in an obvious that the user knows about and may
     * access outside of the application.
     *
     * @param prefs Application preferences.
     *
     * @return The user data directory.
     */
    public static String getUserDir(final Preferences prefs) {
        String userDir = prefs.get(USER_DIR, null);
        if (userDir == null) {
            userDir = getDefaultUserDir();
        }

        final File f = new File(userDir);
        if (!f.exists()) {
            if (!f.mkdirs()) {
                // TODO: warn the user.
            }
        } else if (!f.isDirectory()) {
            // TODO: warn the user.
        } else {
            // Do nothing
        }

        return userDir;
    }

    /**
     * Return the default user data directory.
     *
     * @return The default user data directory.
     */
    private static String getDefaultUserDir() {
        return new File(System.getProperty("user.home"), ".CONSTELLATION").getPath();
    }

    /**
     * User directory.
     */
    public static final String USER_DIR = "userDir";
    public static final String USER_DIR_DEFAULT = getDefaultUserDir();

    /**
     * Autosave.
     */
    public static final String AUTOSAVE_ENABLED = "autosaveEnabled";
    public static final boolean AUTOSAVE_ENABLED_DEFAULT = true;
    public static final String AUTOSAVE_SCHEDULE = "autosaveSchedule";
    public static final int AUTOSAVE_SCHEDULE_DEFAULT = 5;

    /**
     * Whats New window.
     */
    public static final String TUTORIAL_ON_STARTUP = "tutorialMode";
    public static final boolean TUTORIAL_ON_STARTUP_DEFAULT = false;

    /**
     * Welcome window.
     */
    public static final String WELCOME_ON_STARTUP = "welcomeMode";
    public static final boolean WELCOME_ON_STARTUP_DEFAULT = true;

    /**
     * Web server listening port.
     */
    public static final String WEBSERVER_PORT = "webserverPort";
    public static final int WEBSERVER_PORT_DEFAULT = 1517;

    /**
     * Jupyter notebook directory.
     */
    public static final String JUPYTER_NOTEBOOK_DIR = "jupyterNotebookDir";
    public static final String JUPYTER_NOTEBOOK_DIR_DEFAULT = new File(System.getProperty("user.dir")).getPath();

    /**
     * File Save location.
     */
    public static final String FILE_OPEN_AND_SAVE_LOCATION = "fileOpenAndSaveLocation";

    /**
     * Enable/disable Spell Checking in SpellCheckingTextArea text fields.
     */
    public static final String ENABLE_SPELL_CHECKING = "enableSpellChecking";
    public static final boolean ENABLE_SPELL_CHECKING_DEFAULT = true;

    /**
     * A directory where the webserver can write files to emulate REST requests.
     *
     * @param prefs Application preferences.
     *
     * @return The rest directory.
     */
    public static String getRESTDir(final Preferences prefs) {
        String restDir = prefs.get(REST_DIR, "").trim();
        if (restDir.isEmpty()) {
            final RestDirectory rdir = Lookup.getDefault().lookup(RestDirectory.class);
            restDir = rdir.getRESTDirectory().toString();
        }

        final File f = new File(restDir);
        if (!f.exists()) {
            if (!f.mkdirs()) {
                // TODO: warn the user.
            }
        } else if (!f.isDirectory()) {
            // TODO: warn the user.
        } else {
            // Do nothing
        }

        return restDir;
    }

    /**
     * REST directory.
     */
    public static final String REST_DIR = "restDir";
    public static final String REST_DIR_DEFAULT = "";

    /**
     * REST API Python client auto-download.
     */
    public static final String PYTHON_REST_CLIENT_DOWNLOAD = "restPythonClientDownload";
    public static final boolean PYTHON_REST_CLIENT_DOWNLOAD_DEFAULT = true;

    /**
     * Output window preferences including font family, size and style.
     */
    public static final String OUTPUT2_PREFERENCE = "org/netbeans/core/output2";
    public static final String OUTPUT2_FONT_SIZE = "output.settings.font.size";
    public static final String OUTPUT2_FONT_SIZE_DEFAULT = "12";
    public static final String OUTPUT2_FONT_FAMILY = "output.settings.font.family";
    public static final String OUTPUT2_FONT_FAMILY_DEFAULT = "Dialog";

    /**
     * Charts.
     */
    public static final String CHART_DISPLAY = "chartDisplay";
    public static final String CHART_DISPLAY_CONSTELLATION = "constellation";
    public static final String CHART_DISPLAY_BROWSER = "browser";
    public static final String CHART_DISPLAY_DEFAULT = CHART_DISPLAY_CONSTELLATION;

    /**
     * Scripting.
     */
    public static final String DEFAULT_TEMPLATE = "defaultTemplate";
    public static final String DEFAULT_TEMPLATE_DEFAULT = null;

    /**
     * Quality Control View Priorities
     */
    public static final String RULE_PRIORITIES = "customRules";
    
    /**
     * Quality Control View Rule Enabled Statuses
     */
    public static final String RULE_ENABLED_STATUSES = "enabledRules";

    /**
     * Default Font.
     */
    public static final String FONT_PREFERENCES = "au/gov/asd/tac/constellation/preferences";
    public static final String FONT_FAMILY = "fontFamily";
    public static String FONT_FAMILY_DEFAULT = "Arial";
    public static final String FONT_SIZE = "fontSize";
    public static String FONT_SIZE_DEFAULT = "12";

    private ApplicationPreferenceKeys() {
    }
}
