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
package au.gov.asd.tac.constellation.functionality.startup;

import au.gov.asd.tac.constellation.security.ConstellationSecurityManager;
import au.gov.asd.tac.constellation.security.proxy.ProxyUtilities;
import au.gov.asd.tac.constellation.utilities.BrandingUtilities;
import au.gov.asd.tac.constellation.utilities.font.FontUtilities;
import au.gov.asd.tac.constellation.utilities.log.ConstellationLogFormatter;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import org.netbeans.modules.quicksearch.QuickSearchAction;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
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
        if (LOGGER.getUseParentHandlers()) {
            final Handler[] parentHandlers = LOGGER.getParent().getHandlers();
            for (final Handler handler : parentHandlers) {
                handler.setFormatter(new ConstellationLogFormatter());
            }
        }

        ConstellationSecurityManager.startSecurityLater(null);
        
        final List<? extends Action> actions = Utilities.actionsForPath("Actions/Edit");
        for (final Action action : actions) {
            if (action instanceof QuickSearchAction) {
                final Component toolbarPresenter = ((Presenter.Toolbar) action).getToolbarPresenter();
                for (final Component c : ((Container)toolbarPresenter).getComponents()) {
                    processComponentTree(c);
                }
                break;
            }
        }
        
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

    /**
     * Traverse the component tree to find the right source to set the size.
     * @param source component to traverse.
     */
    public void processComponentTree(final Component source) {

        if (source instanceof JScrollPane jsp) {
            final Dimension origSize = jsp.getSize();
            Dimension newDimension;
            if (UIManager.get("customFontSize") == null) {
                newDimension = origSize;
            } else {
                Integer customFontSize = (Integer) UIManager.get("customFontSize");
                newDimension = new Dimension(origSize.width, 18 * customFontSize / 12);
            }
            jsp.setMinimumSize(newDimension);
            jsp.setPreferredSize(newDimension);
            jsp.getViewport().setPreferredSize(newDimension);
        }
        // traverse the component tree
        if (source instanceof Container sc) {
            for (final Component c : sc.getComponents()) {
                processComponentTree(c);
            }
        }   
    }
}
