/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.preferences;

import au.gov.asd.tac.constellation.views.AbstractTopComponent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import org.openide.windows.OnShowing;

/**
 * Handles operations on the file containing the default floating preferences.
 *
 * @author sol695510
 */
@OnShowing()
public class ViewOptionsUtility implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ViewOptionsUtility.class.getName());

    private static final String DFP_FILE_NAME = "dfp.txt";
    private static File DFPFile;
    private static final Map<String, Boolean> DFPMap = new TreeMap<>();

    // This is the system property that is set to true in order to make the AWT thread run in headless mode for tests, etc.
    private static final String AWT_HEADLESS_PROPERTY = "java.awt.headless";

    /**
     * Generate the default viewing preferences in developer versions of code.
     */
    @Override
    public void run() {
        if (Boolean.TRUE.toString().equalsIgnoreCase(System.getProperty(AWT_HEADLESS_PROPERTY))) {
            return;
        }
        CompletableFuture.runAsync(this::updateDFPFile, Executors.newSingleThreadExecutor());
    }

    /**
     * Update the file containing the default viewing preferences.
     */
    protected void updateDFPFile() {
        // Change boolean to true to update default floating preferences file, revert back to false after updating.
        final Boolean updateDFP = Boolean.FALSE;

        if (updateDFP) {
            createDFPFile(getBaseDirectory());
        }
    }

    /**
     * Create a default viewing preferences file at the given file path.
     *
     * @param filePath
     */
    protected static void createDFPFile(final String filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException("Null file path used for creation of DFP file");
        }

        try {
            if (Files.deleteIfExists(FileSystems.getDefault().getPath(filePath))) {
                LOGGER.log(Level.FINE, "Previous DFP file was replaced at: {0}", filePath);
            }
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, "Path to DFP file was invalid: %s".formatted(filePath), ex);
        }

        DFPFile = new File(filePath);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DFPFile))) {
            final Map<String, Boolean> DFPFromLookUp = getDFPFromLookUp();

            for (final Map.Entry<String, Boolean> entry : DFPFromLookUp.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue() + "\n");
            }

            DFPFile.createNewFile();

            LOGGER.log(Level.FINE, "DFP file was created at: {0}", filePath);
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, "Unable to create DFP file. FilePath: %s".formatted(filePath), ex);
        }
    }

    /**
     * Read the default floating preferences file from the given file path.
     *
     * @param filePath
     */
    protected static void readDFPFile(final String filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException("Null file path used for reading of DFP file");
        }

        DFPFile = new File(filePath);

        try (BufferedReader reader = new BufferedReader(new FileReader(DFPFile))) {
            final Scanner sc = new Scanner(reader);

            while (sc.hasNextLine()) {
                final String nextLine = sc.nextLine();
                final String[] ss = nextLine.split(":");
                DFPMap.put(ss[0], Boolean.valueOf(ss[1]));
            }

            LOGGER.log(Level.FINE, "DFP file was read at: {0}", filePath);
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, "Unable to read DFP file. FilePath: %s".formatted(filePath), ex);
        }
    }

    /**
     * Get the file path of where the default floating preferences file is stored.
     *
     * @return a string representation of the file path where the default floating preferences file is stored.
     */
    protected static String getBaseDirectory() {
        String baseDirectory = "";
        final String resource = getResource();

        if (!resource.isBlank()) {
            baseDirectory = resource + File.separator + DFP_FILE_NAME;
        }

        return baseDirectory;
    }

    /**
     * Get the resource file path.
     *
     * @return the resource file path.
     * @throws IllegalArgumentException
     */
    protected static String getResource() throws IllegalArgumentException {
        final URL sourceLocation = ViewOptionsUtility.class.getProtectionDomain().getCodeSource().getLocation();
        final String pathLoc = sourceLocation.getPath();
        final URI uri = URI.create(pathLoc);
        final Path path = Paths.get(uri);
        final int jarIx = path.toString().lastIndexOf(File.separator);
        final String newPath = jarIx > -1 ? path.toString().substring(0, jarIx) : "";
        return newPath != null ? newPath + File.separator + "ext" : "";
    }

    /**
     * Get a map of the default floating preferences from the lookup.
     *
     * @return a map of the default floating preferences.
     */
    protected static Map<String, Boolean> getDFPFromLookUp() {
        final Map<String, Boolean> DFPFromLookUp = new TreeMap<>();

        if (DFPFromLookUp.isEmpty()) {
            Lookup.getDefault().lookupAll(AbstractTopComponent.class).forEach(lookup -> {
                DFPFromLookUp.putAll(lookup.getDefaultFloatingPreference());
            });
        }

        return Collections.unmodifiableMap(DFPFromLookUp);
    }

    /**
     * Get a map of the default floating preferences from the saved file.
     *
     * @return a map of the default floating preferences.
     */
    public static Map<String, Boolean> getDFPFromFile() {
        if (DFPMap.isEmpty()) {
            readDFPFile(getBaseDirectory());
        }

        return Collections.unmodifiableMap(DFPMap);
    }
}
