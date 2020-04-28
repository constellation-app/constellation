/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.dataaccess.panes;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Track preferences to remember expansion states.
 *
 * @author algol
 */
class DataAccessPreferences {

    private static final Preferences PREFERENCES = NbPreferences.forModule(DataAccessPreferences.class);
    private static final String EXPAND = "Expand";
    private static final String FAVOURITE = "Favourite";
    private static final String STRING_STRING_FORMAT = "%s.%s";

    /**
     * Set an expanded preference.
     * <p>
     * Typically called from an expandedProperty ChangeListener.
     *
     * @param title Preference title.
     * @param isExpanded Boolean value.
     */
    static void setExpanded(final String title, final boolean isExpanded) {
        PREFERENCES.putBoolean(String.format(STRING_STRING_FORMAT, EXPAND, title), isExpanded);
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
    static boolean isExpanded(final String title, final boolean defaultExpanded) {
        return PREFERENCES.getBoolean(String.format(STRING_STRING_FORMAT, EXPAND, title), defaultExpanded);
    }

    static void setFavourite(final String title, final boolean isExpanded) {
        PREFERENCES.putBoolean(String.format(STRING_STRING_FORMAT, FAVOURITE, title), isExpanded);
    }

    static boolean isfavourite(final String title, final boolean defaultExpanded) {
        return PREFERENCES.getBoolean(String.format(STRING_STRING_FORMAT, FAVOURITE, title), defaultExpanded);
    }
}
