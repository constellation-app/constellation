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
package au.gov.asd.tac.constellation.graph.file.save;

import au.gov.asd.tac.constellation.graph.file.GraphDataObject;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;

/**
 *
 * @author algol
 */
@Messages({
    "# {0} - properties file",
    "MSG_FileError=Error reading properties file {0}"})
public final class AutosaveUtilities {

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String PATH = "path";
    public static final String UNSAVED = "unsaved";
    public static final String DT = "dt";
    public static final String AUTO_EXT = ".star_auto";
    private static final Logger LOGGER = Logger.getLogger(AutosaveUtilities.class.getName());
    private static final String AUTOSAVE_DIR = "Autosave";

    private AutosaveUtilities() {
        throw new IllegalStateException("Utility class");
    }

    public static File getAutosaveDir() {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final String userDir = ApplicationPreferenceKeys.getUserDir(prefs);
        final File saveDir = new File(userDir, AUTOSAVE_DIR);
        if (!saveDir.exists()) {
            saveDir.mkdir();
        } else if (!saveDir.isDirectory()) {
            final String msg = String.format("Autosave directory '%s' is not a directory", AUTOSAVE_DIR);
            LOGGER.warning(msg);

            return null;
        } else {
            return saveDir;
        }

        return saveDir;
    }

    /**
     * Return an array of files with the given name suffix.
     *
     * @param ext the file name suffix.
     * @return An array of files with the given name suffix.
     */
    public static File[] getAutosaves(final String ext) {
        // Look for existing autosaved in-memory graphs.
        final File saveDir = AutosaveUtilities.getAutosaveDir();
        if (saveDir == null) {
            return new File[0];
        }

        return saveDir.listFiles((dir, name) -> name.endsWith(ext));
    }

    /**
     * Delete the 
     * files belonging to the specified graph id.
     * <p>
     * This is typically for when the VisualTopComponent is closing and the
     * graph has not been modified.
     *
     * @param id The id of the graph from ReadableGraph.getId().
     */
    public static void deleteAutosave(final String id) {
        final File dir = getAutosaveDir();
        final File f = new File(dir, id + AUTO_EXT);
        deleteAutosave(f);
    }

    /**
     * Delete a pair of autosave files.
     * <p>
     * If the .star is given, the matching .star_auto will be deleted, and vice
     * versa.
     *
     * @param f A .star or .star_auto to be deleted.
     */
    public static void deleteAutosave(final File f) {
        final String path = f.getPath();
        final boolean fIsDeleted = f.delete();
        if (!fIsDeleted) {
            //TODO: Handle case where file not successfully deleted
        }

        File f2 = null;
        if (path.endsWith(GraphDataObject.FILE_EXTENSION)) {
            f2 = new File(path + "_auto");
        } else if (path.endsWith(AUTO_EXT)) {
            f2 = new File(path.substring(0, path.length() - 5));
        } else {
            // Do nothing
        }

        if (f2 != null) {
            final boolean f2IsDeleted = f2.delete();
            if (!f2IsDeleted) {
                //TODO: Handle case where file not successfully deleted
            }
        }
    }

    /**
     * For a given File, find and return the autosave properties for that file
     * if it exists.
     *
     * @param f A File.
     *
     * @return The autosave properties for that file if an autosave exists,
     * otherwise null.
     */
    public static Properties getAutosave(final File f) {
        for (final File autosave : getAutosaves(AUTO_EXT)) {
            try {
                final Properties p = new Properties();
                try (InputStream in = new FileInputStream(autosave)) {
                    p.load(in);
                }

                final String path = p.getProperty(PATH);
                if (path != null) {
                    final File fpath = new File(path);
                    if (fpath.equals(f)) {
                        return p;
                    }
                }
            } catch (IOException ex) {
                final String msg = Bundle.MSG_FileError(autosave);
                LOGGER.log(Level.WARNING, msg, ex);
            }
        }

        return null;
    }

    /**
     * Safely move a file.
     * <p>
     * The destination file is renamed to file.bak, the source file is copied,
     * the .bak file is deleted.
     *
     * @param autosave The autosave source file.
     * @param to The destination file.
     *
     * @throws IOException When an error happens.
     */
    public static void copyFile(final File autosave, final File to) throws IOException {
        final File toBak = new File(to.getPath() + ".bak");
        LOGGER.log(Level.INFO, "Processing request to open autosave file: {0}", autosave);
        if (toBak.exists()) {
            final boolean toBakIsDeleted = toBak.delete();
            if (!toBakIsDeleted) {
                LOGGER.log(Level.WARNING, "Unable to remove old backup file: {0}", toBak);
            }
        }

        if (to.exists()) {
            final boolean toRenamed = to.renameTo(toBak);
            LOGGER.log(Level.INFO, "Backing up {0} to {1}", new Object[]{to, toBak});
            if (!toRenamed) {
                LOGGER.log(Level.WARNING, "Unable to backup file: {0}", to);
            }
        }

        try (InputStream in = new FileInputStream(autosave)) {
            try (final OutputStream out = new FileOutputStream(to)) {
                final int bufsiz = 1024 * 1024;
                final byte[] buf = new byte[bufsiz];
                while (true) {
                    final int len = in.read(buf);
                    if (len == -1) {
                        break;
                    }
                    out.write(buf, 0, len);
                }
            }
        }
        LOGGER.log(Level.INFO, "Replacing {0} with autosave file prior to opening", to);
    }

    /**
     * Clean up stray files in the autosave directory.
     * <p>
     * It's possible to have .star files without a corresponding .star_auto, and
     * vice versa, depending on exactly where a crash happened. This method gets
     * rid of dangling files.
     */
    public static void cleanup() {
        // Find .star files aithout a .star_auto.
        for (final File star : getAutosaves(GraphDataObject.FILE_EXTENSION)) {
            final File auto = new File(star.getPath() + "_auto");
            if (!auto.exists()) {
                final boolean starIsDeleted = star.delete();
                if (!starIsDeleted) {
                    //TODO: Handle case where file not successfully deleted
                }
            }
        }

        // Find .star_auto files without a .star.
        for (final File auto : getAutosaves(AUTO_EXT)) {
            final String autos = auto.getPath();
            final File star = new File(autos.substring(0, autos.length() - 5));
            if (!star.exists()) {
                final boolean autoIsDeleted = auto.delete();
                if (!autoIsDeleted) {
                    //TODO: Handle case where file not successfully deleted
                }
            }
        }
    }
}
