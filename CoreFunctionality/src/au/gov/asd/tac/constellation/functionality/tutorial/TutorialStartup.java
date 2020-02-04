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
package au.gov.asd.tac.constellation.functionality.tutorial;

import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import org.openide.util.NbPreferences;
import org.openide.windows.OnShowing;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Opens the TutorialTopComponent on application launch if the preference has
 * been set.
 *
 * @author algol
 */
@OnShowing
public class TutorialStartup implements Runnable {

    @Override
    public void run() {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        if (prefs.getBoolean(ApplicationPreferenceKeys.TUTORIAL_ON_STARTUP, ApplicationPreferenceKeys.TUTORIAL_ON_STARTUP_DEFAULT)) {
            SwingUtilities.invokeLater(() -> {
                final TopComponent tutorial = WindowManager.getDefault().findTopComponent(TutorialTopComponent.class.getSimpleName());
                if (tutorial != null) {
                    if (!tutorial.isOpened()) {
                        tutorial.open();
                    }
                    tutorial.setEnabled(true);
                    tutorial.requestActive();
                }
            });
        }
    }
}
