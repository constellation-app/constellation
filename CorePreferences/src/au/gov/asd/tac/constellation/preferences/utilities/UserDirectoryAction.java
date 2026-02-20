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

import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;

@ActionID(category = "Help", id = "au.gov.asd.tac.constellation.preferences.utilities.UserDirectoryAction")
@ActionRegistration(displayName = "#CTL_UserDirectoryAction",
        iconBase = "au/gov/asd/tac/constellation/preferences/utilities/userDirectory.png")
@ActionReference(path = "Menu/Help", position = 325)
@Messages("CTL_UserDirectoryAction=User Directory")
public final class UserDirectoryAction implements ActionListener {
    
    private static final Logger LOGGER = Logger.getLogger(UserDirectoryAction.class.getName());

    @Override
    public void actionPerformed(final ActionEvent e) {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final String userDir = ApplicationPreferenceKeys.getUserDir(prefs);
        try {
            Desktop.getDesktop().open(new File(userDir));
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }
}
