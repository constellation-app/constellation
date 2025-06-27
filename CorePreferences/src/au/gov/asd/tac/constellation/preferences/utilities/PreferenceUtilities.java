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
package au.gov.asd.tac.constellation.preferences.utilities;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Various helper methods for handling preferences.
 *
 * @author algol
 */
public class PreferenceUtilities {
    
    private static final Logger LOGGER = Logger.getLogger(PreferenceUtilities.class.getName());

    private PreferenceUtilities() {
        throw new IllegalStateException("Utility class");
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
        } catch (final BackingStoreException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
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
        } catch (final BackingStoreException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }

        return false;
    }
}
