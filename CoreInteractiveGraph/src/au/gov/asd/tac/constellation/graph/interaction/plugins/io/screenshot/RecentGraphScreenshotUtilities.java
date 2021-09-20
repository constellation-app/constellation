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
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import org.openide.util.NbPreferences;

/**
 * Recent Graph Screenshot Utilities
 *
 * @author arcturus
 */
public class RecentGraphScreenshotUtilities {

    private static final String SCREENSHOTS_DIR = "Screenshots";
    private static final Logger LOGGER = Logger.getLogger(RecentGraphScreenshotUtilities.class.getName());

    // width and height of tile image
    public static final int IMAGE_SIZE = 145;

    private RecentGraphScreenshotUtilities() {
        throw new IllegalArgumentException("Utility class");
    }

    /**
     * Retrieve the screenshots user directory that is used to save a screenshot
     * of the graph
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
            return saveDir;
        }

        return saveDir;
    }

    /**
     * Take a screenshot of the graph and save it to the screenshots directory
     * so that it can be used by the Welcome View.
     *
     * @param filename The filename of the graph
     */
    public static void takeScreenshot(final String filename) {
        final String imageFile = getScreenshotsDir() + File.separator + filename + ".png";
        final Path source = Paths.get(imageFile);
        final GraphNode graphNode = GraphNode.getGraphNode(GraphManager.getDefault().getActiveGraph());
        final VisualManager visualManager = graphNode.getVisualManager();

        final BufferedImage[] originalImage = new BufferedImage[1];
        originalImage[0] = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_INT_ARGB);

        if (visualManager != null) {
            final Semaphore waiter = new Semaphore(0);
            visualManager.exportToBufferedImage(originalImage, waiter);
            waiter.acquireUninterruptibly();
        }

        try {
            // resizeAndSave the buffered image in memory and write the image to disk
            resizeAndSave(originalImage[0], source, IMAGE_SIZE, IMAGE_SIZE);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Resize the {@code BufferedImage} and write it to disk
     * <p>
     * Referenced from
     * https://mkyong.com/java/how-to-resizeAndSave-an-image-in-java/
     * </p>
     *
     * @param originalImage
     * @param target the file path were we want to store the resized image
     * @param height the new height of the resized image
     * @param width the new width of the resized image
     * @throws IOException
     */
    public static void resizeAndSave(final BufferedImage originalImage, final Path target, final int height, final int width) throws IOException {
        // create a new BufferedImage for drawing
        final BufferedImage newResizedImage
                = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = newResizedImage.createGraphics();

        g.setComposite(AlphaComposite.Src);
        g.fillRect(0, 0, width, height);

        final Map<RenderingHints.Key, Object> hints = new HashMap<>();
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.addRenderingHints(hints);

        // puts the original image into the newResizedImage
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();

        // get file extension
        final String s = target.getFileName().toString();
        final String fileExtension = s.substring(s.lastIndexOf(".") + 1);

        // we want image in png format
        ImageIO.write(newResizedImage, fileExtension, target.toFile());
    }
}
