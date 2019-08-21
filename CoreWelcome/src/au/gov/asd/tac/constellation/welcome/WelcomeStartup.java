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
package au.gov.asd.tac.constellation.welcome;

import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import org.openide.util.NbPreferences;
import org.openide.windows.OnShowing;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author imranraza83
 */
@OnShowing
public class WelcomeStartup implements Runnable {

    @Override
    public void run() {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        if(prefs.getBoolean(ApplicationPreferenceKeys.SHOW_WELCOME_SCREEN,ApplicationPreferenceKeys.SHOW_WELCOME_SCREEN_DEFAULT))
        SwingUtilities.invokeLater(() -> {
            final TopComponent welcome = WindowManager.getDefault().findTopComponent(WelcomeTopComponent.class.getSimpleName());
            welcome.open();
            welcome.requestActive();
            //prefs.putBoolean(ApplicationPreferenceKeys.SHOW_WELCOME_SCREEN, false);
        });
    }
    
}
