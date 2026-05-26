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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.openide.util.Lookup;
import org.openide.windows.OnShowing;

/**
 * Handles operations on the file the default floating preferences are saved to.
 *
 * @author sol695510
 */
@OnShowing()
public class ViewOptionsUtility implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ViewOptionsUtility.class.getName());

    private static String resourceDirectory = "";
    private static File dfpFile;
    private static final Map<String, Boolean> dfpFromFile = new TreeMap<>();

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
            createDFPFile(getResourceDirectory());
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

        dfpFile = new File(filePath);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dfpFile))) {
            final Map<String, Boolean> dfpFromLookUp = getDFPFromLookUp();

            for (final Map.Entry<String, Boolean> entry : dfpFromLookUp.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue() + "\n");
            }

            dfpFile.createNewFile();

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

        dfpFile = new File(filePath);

        try (BufferedReader reader = new BufferedReader(new FileReader(dfpFile))) {
            final Scanner sc = new Scanner(reader);

            while (sc.hasNextLine()) {
                final String nextLine = sc.nextLine();
                final String[] ss = nextLine.split(":");
                dfpFromFile.put(ss[0].trim(), Boolean.valueOf(ss[1].trim()));
            }

            LOGGER.log(Level.FINE, "DFP file was read at: {0}", filePath);
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, "Unable to read DFP file. FilePath: %s".formatted(filePath), ex);
        }
    }

    /**
     * Get a map of the default floating preferences from the lookup.
     *
     * @return a map of the default floating preferences.
     */
    protected static Map<String, Boolean> getDFPFromLookUp() {
        final Map<String, Boolean> dfpFromLookUp = new TreeMap<>();

        if (dfpFromLookUp.isEmpty()) {
            Lookup.getDefault().lookupAll(AbstractTopComponent.class).forEach(lookup -> dfpFromLookUp.putAll(lookup.getDefaultFloatingPreference()));
        }

        return Collections.unmodifiableMap(dfpFromLookUp);
    }

    /**
     * Get a map of the default floating preferences from the file they are saved to.
     *
     * @return a map of the default floating preferences.
     */
    public static Map<String, Boolean> getDFPFromFile() {
        if (dfpFromFile.isEmpty()) {
            readDFPFile(getResourceDirectory());
        }

        return Collections.unmodifiableMap(dfpFromFile);
    }

    /**
     * Get the file path to the file where the default floating preferences are saved to.
     *
     * @return the file path to the default floating preferences file.
     */
    protected static String getResourceDirectory() {
        if (resourceDirectory.isBlank()) {
            try {
                final String source = ViewOptionsUtility.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                final URI uri = URI.create(source);
                final Path path = Paths.get(uri);

                final String sep = File.separator;
                String[] splitPath = path.toString().split(Pattern.quote(sep));

                // Keep removing the last directory in the path array until the constellation directory is reached.
                while (!"constellation".equals(splitPath[splitPath.length - 1])) {
                    splitPath = Arrays.copyOfRange(splitPath, 0, splitPath.length - 1);
                }

                resourceDirectory = String.join(sep, splitPath) + sep + "dfp.txt";

                LOGGER.log(Level.FINE, "DFP file directory was retrieved at: {0}", resourceDirectory);
            } catch (final IllegalArgumentException ex) {
                LOGGER.log(Level.SEVERE, "There was a problem retrieving the directory of the DFP file.", ex);
            }
        }

        return resourceDirectory;
    }
}
