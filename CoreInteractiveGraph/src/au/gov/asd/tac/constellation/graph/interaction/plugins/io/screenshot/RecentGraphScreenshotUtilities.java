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
package au.gov.asd.tac.constellation.graph.interaction.plugins.io.screenshot;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.file.open.RecentFiles;
import au.gov.asd.tac.constellation.graph.interaction.gui.VisualGraphTopComponent;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.visual.VisualManager;
import java.awt.AlphaComposite;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

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
            return saveDir;
        }

        return saveDir;
    }

    /**
     * Creates a MD5 hash of the filepath.
     *
     * @param filepath The filepath of the graph
     * @return MD5 hash of filepath
     */
    protected static String hashFilePath(final String filepath) {
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(filepath.getBytes(StandardCharsets.UTF_8));
            final byte[] digest = md.digest();
            return DatatypeConverter.printHexBinary(digest).toUpperCase();
        } catch (final NoSuchAlgorithmException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
        return filepath;
    }

    /**
     * Finds the path for the screenshot
     *
     * @param filepath the filepath for the graph
     * @param filename the filename of the graph
     * @return the filepath to the screenshot png
     */
    public static Optional<File> findScreenshot(final String filepath, final String filename) {
        final String screenshotFilenameFormat = getScreenshotsDir() + File.separator + "%s" + FileExtensionConstants.PNG;
        final String screenshotHash = RecentGraphScreenshotUtilities.hashFilePath(filepath);
        final String screenshotFilename = String.format(screenshotFilenameFormat, screenshotHash);
        final String legacyScreenshotFilename = String.format(screenshotFilenameFormat, filename);

        if (new File(screenshotFilename).exists()) {
            return Optional.of(new File(screenshotFilename));
        } else if (new File(legacyScreenshotFilename).exists()) {
            return Optional.of(new File(legacyScreenshotFilename));
        }
        return Optional.empty();
    }

    /**
     * Take a screenshot of the current active graph and save it to the screenshots directory so that it can be used by
     * the Welcome View.
     *
     * @param filepath The filepath of the graph
     */
    public static void takeScreenshot(final String filepath) {
        takeScreenshot(filepath, GraphManager.getDefault().getActiveGraph());
    }

    /**
     * Take a screenshot of the given graph and save it to the screenshots directory so that it can be used by the
     * Welcome View.
     *
     * @param filepath The filepath of the graph
     * @param graph The graph to take a screenshot of
     */
    public static synchronized void takeScreenshot(final String filepath, final Graph graph) {
        final String pathHash = hashFilePath(filepath);
        final String imageFile = getScreenshotsDir() + File.separator + pathHash + FileExtensionConstants.PNG;
        final Path source = Paths.get(imageFile);
        final GraphNode graphNode = GraphNode.getGraphNode(graph);

        if (graphNode != null) {
            final VisualManager visualManager = graphNode.getVisualManager();
            final BufferedImage[] originalImage = new BufferedImage[1];
            originalImage[0] = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_INT_ARGB);

            if (visualManager != null) {
                final Semaphore waiter = new Semaphore(0);
                requestGraphActive(graph);

                visualManager.exportToBufferedImage(originalImage, waiter); // Requires 0 permits, becomes 1 when done

                // This seems to be here so the program has to wait for exporting to finish before moving on
                waiter.acquireUninterruptibly(); // Wait for 0 permits to be 1
                resestGraphActive();
            }

            try {
                // resizeAndSave the buffered image in memory and write the image to disk
                resizeAndSave(originalImage[0], source, IMAGE_SIZE, IMAGE_SIZE);
                refreshScreenshotsDir();
            } catch (final IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }
    }

    protected static void requestGraphActive(final Graph graph) {
        final Set<TopComponent> topComponents = WindowManager.getDefault().getRegistry().getOpened();

        if (topComponents == null) {
            return;
        }
        
        if (graph == null) {
            return;
        }

        topComponents.forEach(component -> {
            if ((component instanceof VisualGraphTopComponent) && ((VisualGraphTopComponent) component).getGraphNode().getGraph().getId().equals(graph.getId())) {
                try {
                    // Update each graph and revert any changes made by the analytic view visualisations
                    EventQueue.invokeAndWait(((VisualGraphTopComponent) component)::requestActive);
                } catch (final InterruptedException ex) {
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
                    Thread.currentThread().interrupt();
                } catch (final InvocationTargetException ex) {
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
                }
            }
        });
    }

    protected static void resestGraphActive() {
        final Set<TopComponent> topComponents = WindowManager.getDefault().getRegistry().getOpened();
        final Graph activeGraph = GraphManager.getDefault().getActiveGraph();

        if (topComponents == null) {
            return;
        }
        
        // Make the originally active graph the active graph again.
        topComponents.forEach(component -> {
            if ((component instanceof VisualGraphTopComponent) && ((VisualGraphTopComponent) component).getGraphNode().getGraph().getId().equals(activeGraph.getId())) {
                try {
                    EventQueue.invokeAndWait(((VisualGraphTopComponent) component)::requestActive);
                } catch (final InterruptedException ex) {
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
                    Thread.currentThread().interrupt();
                } catch (final InvocationTargetException ex) {
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
                }
            }
        });

    }

    /**
     * Resize the {@code BufferedImage} and write it to disk
     * <p>
     * Referenced from https://mkyong.com/java/how-to-resizeAndSave-an-image-in-java/
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

    /**
     * Refresh stored screenshots of recent files to match the recent files stored in history.
     */
    public static void refreshScreenshotsDir() {

        final List<String> filesInHistory = new ArrayList<>();
        final List<File> filesInDirectory = new ArrayList<>();
        final File screenShotsDir = getScreenshotsDir();

        if (screenShotsDir != null) {
            filesInDirectory.addAll(Arrays.asList(screenShotsDir.listFiles()));
        }

        RecentFiles.getUniqueRecentFiles().forEach(item -> {
            filesInHistory.add(item.getFileName() + FileExtensionConstants.PNG);
            findScreenshot(item.getPath(), item.getFileName()).ifPresent(file
                    -> filesInHistory.add(file.getAbsolutePath())
            );
        });

        filesInDirectory.forEach(file -> {
            // Backward compatible with <filename>.png and newer <hashed filepath>.png
            final boolean[] found = new boolean[1];
            filesInHistory
                    .stream()
                    .filter(Objects::nonNull)
                    .forEach(f -> {
                        if (f.contains(file.getName())) {
                            found[0] = true;
                        }
                    });

            if (!found[0]) {
                try {
                    Files.delete(file.toPath());
                } catch (final IOException ex) {
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                }
            }
        });
    }
}
