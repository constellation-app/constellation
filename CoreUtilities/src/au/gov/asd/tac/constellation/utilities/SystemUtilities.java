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
package au.gov.asd.tac.constellation.utilities;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.netbeans.core.options.keymap.api.KeyStrokeUtils;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.core.options.keymap.spi.KeymapManager;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

/**
 * General Utilities that are useful across the application and are not tied to any specific functionality group.
 *
 * @author OrionsGuardian
 */
public class SystemUtilities {

    private static final Logger LOGGER = Logger.getLogger(SystemUtilities.class.getName());
    private static JFrame mainframe = null;

    /**
     * This is the system property that is set to true in order to make the AWT thread run in headless mode for tests,
     * etc.
     */
    private static final String AWT_HEADLESS_PROPERTY = "java.awt.headless";

    /**
     * Store an internal reference to current App window
     */
    private static void captureMainframePosition() {
        if (mainframe != null || Boolean.TRUE.toString().equalsIgnoreCase(System.getProperty(AWT_HEADLESS_PROPERTY))) {
            return;
        }
        try {
            EventQueue.invokeAndWait(() -> mainframe = (JFrame) WindowManager.getDefault().getMainWindow());
        } catch (final InterruptedException ex) {
            LOGGER.log(Level.WARNING, "Thread displaying dialog was interrupted.", ex);
            Thread.currentThread().interrupt();
        } catch (final InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, "Error Displaying Dialog", ex);
        }
    }

    /**
     * Return the internal reference to current App window
     * @return the internal reference to current App window
     */
    public static JFrame getMainframe() {
        captureMainframePosition();
        return mainframe;
    }

    /**
     * This returns the X position of where the main application window is located. Caters for multi-screen
     * environments.
     *
     * @return X position of main app window (returns 0 in headless environment)
     */
    public static double getMainframeXPos() {
        captureMainframePosition();
        if (mainframe == null) {
            return 0;
        }
        return mainframe.getX();
    }

    /**
     * This returns the Y position of where the main application window is located. Caters for multi-screen
     * environments.
     *
     * @return Y position of main app window (returns 0 in headless environment)
     */
    public static double getMainframeYPos() {
        captureMainframePosition();
        if (mainframe == null) {
            return 0;
        }
        return mainframe.getY();
    }

    /**
     * This returns the width of the main application window.
     *
     * @return width of main app window (returns 640 in headless environment)
     */
    public static double getMainframeWidth() {
        captureMainframePosition();
        if (mainframe == null) {
            return 640;
        }
        return mainframe.getSize().getWidth();
    }

    /**
     * This returns height of the main application window.
     *
     * @return height of main app window (returns 480 in headless environment)
     */
    public static double getMainframeHeight() {
        captureMainframePosition();
        if (mainframe == null) {
            return 480;
        }
        return mainframe.getSize().getHeight();
    }
    
    /**
     * Get list of currently used keyboard shortcuts within application
     * 
     * @return list of currently used keydboard shortcuts within application
     */
    public static Map<String, String> getCurrentKeyboardShortcuts() {
        final Map<String, String> shortcuts = new HashMap<>();

        for (final KeymapManager m : Lookup.getDefault().lookupAll(KeymapManager.class)) {

            final Object[] ret = new Object[2];

            ret[0] = m.getKeymap(m.getCurrentProfile());
            ret[1] = m.getActions().entrySet();

            final Map<ShortcutAction, Set<String>> curKeymap = (Map<ShortcutAction, Set<String>>) ret[0];
            final Set<Map.Entry<String, Set<ShortcutAction>>> entryset = (Set<Map.Entry<String, Set<ShortcutAction>>>) ret[1];

            for (final Map.Entry<String, Set<ShortcutAction>> entry : entryset) {
                for (final ShortcutAction sa : entry.getValue()) {
                    final Set<String> ks = curKeymap.get(sa);
                    if (Objects.nonNull(ks)) {                        
                        ks.forEach(s -> shortcuts.put(KeyStrokeUtils.getKeyStrokesAsText(Utilities.stringToKeys(s), " "), sa.getDisplayName()));
                    }
                }
            }
        }
        
        return shortcuts;
    }

}
