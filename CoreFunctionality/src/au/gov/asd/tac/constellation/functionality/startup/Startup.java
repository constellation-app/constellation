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
import au.gov.asd.tac.constellation.utilities.BrandingUtilities;
import au.gov.asd.tac.constellation.utilities.font.FontUtilities;
import javax.swing.JFrame;
import org.openide.windows.OnShowing;
import org.openide.windows.WindowManager;

/**
 * Application Bootstrap on start up
 *
 * @author cygnus_x-1
 * @author arcturus
 */
@OnShowing()
public class Startup implements Runnable {

    private static final String SYSTEM_ENVIRONMENT = "constellation.environment";
    private static final String UNDER_DEVELOPMENT = "(under development)";

    @Override
    public void run() {
        ConstellationSecurityManager.startSecurityLater(null);

        // application environment
        final String environment = System.getProperty(SYSTEM_ENVIRONMENT);
        final String name = environment != null
                ? String.format("%s %s", BrandingUtilities.APPLICATION_NAME, environment)
                : BrandingUtilities.APPLICATION_NAME;

        // Change the main window title to reflect the most recent module version as the application version.
        WindowManager.getDefault().invokeWhenUIReady(() -> {
            String mostRecentVersion = MostRecentModules.getMostRecentVersion();
            if (mostRecentVersion == null) {
                // once issue #86 is fixed this should go back to UNDER_DEVELOPMENT"
                mostRecentVersion = "1.20200225.104006";
            }

            final JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
            final String title = String.format("%s - %s", name, mostRecentVersion);
            frame.setTitle(title);
        });

        FontUtilities.initialiseFontPreferenceOnFirstUse();
    }
}
