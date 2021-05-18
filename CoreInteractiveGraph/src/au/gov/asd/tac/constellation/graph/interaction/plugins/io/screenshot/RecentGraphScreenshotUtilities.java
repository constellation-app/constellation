/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.interaction.plugins.io.screenshot;

import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.visual.VisualManager;
import java.io.File;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Recent Graph Screenshot Utilities
 *
 * @author arcturus
 */
public class RecentGraphScreenshotUtilities {

    private static final String SCREENSHOTS_DIR = "Screenshots";
    private static final Logger LOGGER = Logger.getLogger(RecentGraphScreenshotUtilities.class.getName());

    private RecentGraphScreenshotUtilities() {
        throw new IllegalArgumentException("Utility class");
    }

    /**
     * Retrieve the screenshots user directory that is used to save a screenshot of the graph
     *
     * @return The screenshot directory location
     */
    public static File getScreenshotsDir() {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final String userDir = ApplicationPreferenceKeys.getUserDir(prefs);
        final File saveDir = new File(userDir, SCREENSHOTS_DIR);
        if (!saveDir.exists()) {
            saveDir.mkdir();
        } else if (!saveDir.isDirectory()) {
            final String msg = String.format("Recent graph screenshots directory '%s' is not a directory", SCREENSHOTS_DIR);
            LOGGER.warning(msg);
            return null;
        } else {
            // Default case added per S126
            return saveDir;
        }

        return saveDir;
    }

    /**
     * Take a screenshot of the graph and save it to the screenshots directory.
     * <p>
     * Note that most of the code in this method was taken from the {@code ExportToImagePlugin}. Due to module
     * dependencies, the ExportToImagePlugin could not be run directly.
     *
     * @param filename The filename of the graph
     */
    public static void takeScreenshot(final String filename) {
        final File imageFile = new File(getScreenshotsDir() + File.separator + filename + ".png");
        final GraphNode graphNode = GraphNode.getGraphNode(GraphManager.getDefault().getActiveGraph());
        final VisualManager visualManager = graphNode.getVisualManager();
        if (visualManager != null) {
            visualManager.exportToImage(imageFile);
        }
    }
}
