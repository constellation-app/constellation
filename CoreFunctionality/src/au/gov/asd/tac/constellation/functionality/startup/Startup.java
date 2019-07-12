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
package au.gov.asd.tac.constellation.functionality.startup;

import au.gov.asd.tac.constellation.security.ConstellationSecurityManager;
import au.gov.asd.tac.constellation.visual.fonts.FontUtilities;
import javax.swing.JFrame;
import org.openide.windows.OnShowing;
import org.openide.windows.WindowManager;

/**
 *
 * @author cygnus_x-1
 */
@OnShowing()
public class Startup implements Runnable {

    @Override
    public void run() {
        ConstellationSecurityManager.startSecurityLater(null);

        // Change the main window title to reflect the most recent module version as the application version.
        WindowManager.getDefault().invokeWhenUIReady(() -> {
            String mostRecentVersion = MostRecentModules.getMostRecentVersion();
            if (mostRecentVersion == null) {
                mostRecentVersion = "(under development)";
            }

            final JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
            final String title = String.format("CONSTELLATION - %s", mostRecentVersion);
            frame.setTitle(title);
        });

        FontUtilities.initialiseFontPreferenceOnFirstUse();
    }
}
