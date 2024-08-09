/*
 * Copyright 2010-2024 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.security.proxy.ProxyUtilities;
import au.gov.asd.tac.constellation.utilities.BrandingUtilities;
import au.gov.asd.tac.constellation.utilities.font.FontUtilities;
import au.gov.asd.tac.constellation.utilities.log.ConstellationLogFormatter;
import au.gov.asd.tac.constellation.utilities.log.LogPreferences;
import java.util.logging.Handler;
import java.util.logging.Logger;
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

    // DO NOT CHANGE THIS VALUE
    // continous integration scripts use this to update the version dynamically
    private static final String VERSION = "(under development)";

    /**
     * This is the system property that is set to true in order to make the AWT
     * thread run in headless mode for tests, etc.
     */
    private static final String AWT_HEADLESS_PROPERTY = "java.awt.headless";

    private static final Logger LOGGER = Logger.getLogger(Startup.class.getName());

    @Override
    public void run() {

        // Setup the logging format
        if( LOGGER.getUseParentHandlers() ){
            final Handler[] parentHandlers = LOGGER.getParent().getHandlers();
            for (final Handler handler : parentHandlers) {
                handler.setFormatter(new ConstellationLogFormatter());
            }
        }

        ConstellationSecurityManager.startSecurityLater(null);

        // application environment
        final String environment = System.getProperty(SYSTEM_ENVIRONMENT);
        final String name = environment != null
                ? String.format("%s %s", BrandingUtilities.APPLICATION_NAME, environment)
                : BrandingUtilities.APPLICATION_NAME;

        // We only want to run this if headless is NOT set to true
        if (!Boolean.TRUE.toString().equalsIgnoreCase(System.getProperty(AWT_HEADLESS_PROPERTY))) {
            ConstellationLAFSettings.applyTabColorSettings();        
            // update the main window title with the version number
            WindowManager.getDefault().invokeWhenUIReady(() -> {
                final JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
                final String title = String.format("%s - %s", name, VERSION);
                frame.setTitle(title);
            });
        }

        FontUtilities.initialiseOutputFontPreferenceOnFirstUse();
        FontUtilities.initialiseApplicationFontPreferenceOnFirstUse();

        ProxyUtilities.setProxySelector(null);
    }
}
