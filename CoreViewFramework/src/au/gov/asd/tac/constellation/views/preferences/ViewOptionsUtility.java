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
import java.nio.file.FileSystems;
import java.nio.file.Files;
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

    private static File DFP_FILE;
    private static final String DFP_FILE_PATH = "/Constellation/constellation/CoreViewFramework/src/au/gov/asd/tac/constellation/views/preferences/resources/dfp.txt";
    private static final Map<String, Boolean> DFP_MAP = new TreeMap<>();

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
            createDFPFile(DFP_FILE_PATH);
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

        DFP_FILE = new File(filePath);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DFP_FILE))) {
            final Map<String, Boolean> DFPFromLookUp = getDFPFromLookUp();

            for (final Map.Entry<String, Boolean> entry : DFPFromLookUp.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue() + "\n");
            }

            DFP_FILE.createNewFile();

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

        DFP_FILE = new File(filePath);

        try (BufferedReader reader = new BufferedReader(new FileReader(DFP_FILE))) {
            final Scanner sc = new Scanner(reader);

            while (sc.hasNextLine()) {
                final String nextLine = sc.nextLine();
                final String[] ss = nextLine.split(":");
                DFP_MAP.put(ss[0].trim(), Boolean.valueOf(ss[1].trim()));
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
        if (DFP_MAP.isEmpty()) {
            readDFPFile(DFP_FILE_PATH);
        }

        return Collections.unmodifiableMap(DFP_MAP);
    }
}
