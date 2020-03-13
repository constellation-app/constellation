/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.preferences.utilities;

import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 * Various helper methods for handling preferences.
 *
 * @author algol
 */
public class PreferenceUtilites {

    private PreferenceUtilites() {
    }

    /**
     * Add a PreferenceChangeListener to a specified preference node.
     * <p>
     * For example, to listen for font size changes, listen to
     * "org/netbeans/core/output2".
     *
     * @param preferenceNode The preference node to listen to.
     * @param pcl A PreferenceChangeListener
     *
     * @return True if the addPreferenceChangeListener() worked, false
     * otherwise.
     */
    public static boolean addPreferenceChangeListener(final String preferenceNode, final PreferenceChangeListener pcl) {
        try {
            Preferences p = NbPreferences.root();
            if (p.nodeExists(preferenceNode)) {
                p = p.node(preferenceNode);
                p.addPreferenceChangeListener(pcl);

                return true;
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }

        return false;
    }

    /**
     * Remove a PreferenceChangeListener from a specified preference node.
     *
     * @param preferenceNode The preference node being listened to.
     * @param pcl A PreferenceChangeListener
     *
     * @return True if the addPreferenceChangeListener() worked, false
     * otherwise.
     */
    public static boolean removePreferenceChangeListener(final String preferenceNode, final PreferenceChangeListener pcl) {
        try {
            Preferences p = NbPreferences.root();
            if (p.nodeExists(preferenceNode)) {
                p = p.node(preferenceNode);
                p.removePreferenceChangeListener(pcl);

                return true;
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }

        return false;
    }

    /**
     * Return whether or not to freeze the graph view
     *
     * @return True if the graph view should be frozen, False otherwise
     */
    public static boolean isGraphViewFrozen() {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        return prefs.getBoolean(ApplicationPreferenceKeys.FREEZE_GRAPH_VIEW, ApplicationPreferenceKeys.FREEZE_GRAPH_VIEW_DEFAULT);
    }
}
