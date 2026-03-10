/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.preferences;

import au.gov.asd.tac.constellation.views.AbstractTopComponent;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 * Utility class for view options.
 *
 * @author sol695510
 */
public class ViewOptionsUtility {

    private static final Logger LOGGER = Logger.getLogger(ViewOptionsUtility.class.getName());

    private final Preferences prefs = NbPreferences.forModule(ViewOptionsPanelController.class);
    private final static Map<String, Boolean> defaultPrefs = new TreeMap<>();

    /**
     * Retrieve the default floating preferences for all views included in the lookup.
     *
     * @return a map of the default floating preferences for views included in the lookup.
     */
    public static final Map<String, Boolean> getDefaultFloatingPreferences() {
        if (defaultPrefs.isEmpty()) {
            Lookup.getDefault().lookupAll(AbstractTopComponent.class).forEach(lookup -> defaultPrefs.putAll(lookup.getDefaultFloatingPreference()));
        }

        return Collections.unmodifiableMap(defaultPrefs);
    }
}
