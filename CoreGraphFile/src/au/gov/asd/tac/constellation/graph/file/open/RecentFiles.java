/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package au.gov.asd.tac.constellation.graph.file.open;

import au.gov.asd.tac.constellation.graph.file.open.RecentFiles.HistoryItem;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import com.google.common.collect.ImmutableList;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbPreferences;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Manages prioritized set of recently closed files.
 *
 * @author Dafe Simonek
 */
public final class RecentFiles {

    private static final Logger LOGGER = Logger.getLogger(RecentFiles.class.getName());

    private static final WindowRegistryL RECENT_FILE_SAVED = new WindowRegistryL();

    private static final String PROP_SAVED = "__graph_saved__";

    /**
     * List of recently closed files
     */
    private static final List<HistoryItem> HISTORY = new ArrayList<>();

    /**
     * Preferences node for storing history info
     */
    private static Preferences prefs;

    private static final Object HISTORY_LOCK = new Object();

    /**
     * Name of preferences node where we persist history
     */
    private static final String PREFS_NODE = "RecentFilesHistory"; //NOI18N

    /**
     * Prefix of property for recent file URL
     */
    private static final String PROP_URL_PREFIX = "RecentFilesURL."; //NOI18N

    /**
     * Boundary for items count in history
     */
    private static final int MAX_HISTORY_ITEMS = 10;
    
    /**
     * Flag to indicate if the init process has completed, 
     * which means the recent files "history" data is now available
     */
    private static final CountDownLatch historyReady = new CountDownLatch(1);

    private RecentFiles() {
        // Intentionally left blank
    }

    /**
     * Starts to listen for recently closed files
     */
    public static void init() {
        WindowManager.getDefault().invokeWhenUIReady(() -> {
            final List<HistoryItem> loaded = load();
            synchronized (HISTORY_LOCK) {
                HISTORY.addAll(0, loaded);
            }
            TopComponent.getRegistry().
                    addPropertyChangeListener(RECENT_FILE_SAVED);
            decrementHistoryReadyLatch();
        });
    }

    /**
     * Will decrement the historyReady CountDownLatch
     * Placed this code into it's own method so it can be called separately in unit testing.
     */
    public static void decrementHistoryReadyLatch(){
        historyReady.countDown();
    }
    
    /**
     * Add the specified path to the recent file list.
     *
     * @param path The path to be added to the recent file list.
     */
    public static void saved(final String path) {
        // Don't include the temp files
        if (!path.contains("_tmp")) {
            // Convert to use the default name-separator character.
            final String normPath = new File(path).getPath();
            RECENT_FILE_SAVED.propertyChange(new PropertyChangeEvent(normPath, PROP_SAVED, null, normPath));
        }
    }

    /**
     * Returns read-only list of recently closed files
     *
     * @return list of recent files
     */
    protected static List<HistoryItem> getRecentFiles() {
        assert Thread.holdsLock(HISTORY_LOCK);
        checkHistory();
        return Collections.unmodifiableList(HISTORY);
    }

    /**
     * Gets the read-only list of unique, existing recent closed files
     *
     * @return list of recent files
     */
    public static List<HistoryItem> getUniqueRecentFiles() {
        if (historyReady.getCount() != 0) {
            try {
                LOGGER.log(Level.WARNING, ">> Timing issue encountered: Recent Files data is being accessed before it has been initialised <<");
                if (!historyReady.await(300, TimeUnit.SECONDS)) {
                    LOGGER.log(Level.WARNING, ">> Recent Files did not initialise within 5 minutes <<", new Exception(NotifyDisplayer.BLOCK_POPUP_FLAG + "WARNING: Recent Files data did not initialise within a reasonable time"));
                }
            } catch (final InterruptedException ex) { // NOSONAR
                LOGGER.log(Level.SEVERE, ex.toString(), ex);
            }
        }
        synchronized (HISTORY_LOCK) {
            return getRecentFiles().stream()
                    .filter(file -> convertPath2File(file.getPath()) != null)
                    .distinct()
                    .collect(ImmutableList.toImmutableList());
        }
    }

    private static volatile boolean historyProbablyValid;

    /**
     * True if there are probably some recently closed files. Note: will still
     * be true if all of them are in fact invalid, but this is much faster than
     * calling {@link #getRecentFiles}.
     *
     * @return true if there are probably some recently closed files.
     */
    public static boolean hasRecentFiles() {
        if (!historyProbablyValid) {
            synchronized (HISTORY_LOCK) {
                checkHistory();
                return !HISTORY.isEmpty();
            }
        }
        return historyProbablyValid;
    }

    /**
     * Loads list of recent files stored in previous system sessions.
     *
     * @return list of stored recent files
     */
    protected static List<HistoryItem> load() {
        final String[] keys;
        final Preferences localPrefs = getPrefs();
        try {
            keys = localPrefs.keys();
        } catch (final BackingStoreException ex) {
            LOGGER.log(Level.FINE, ex.getMessage(), ex);
            return Collections.emptyList();
        }

        final List<HistoryItem> result = new ArrayList<>();
        for (final String curKey : keys) {
            final String value = localPrefs.get(curKey, null);
            if (value != null) {
                try {
                    final int id = Integer.parseInt(curKey.substring(PROP_URL_PREFIX.length()));
                    final HistoryItem hItem = new HistoryItem(id, value);
                    final int ind = result.indexOf(hItem);
                    if (ind == -1) {
                        result.add(hItem);
                    } else {
                        localPrefs.remove(PROP_URL_PREFIX
                                + Math.max(result.get(ind).id, id));
                        result.get(ind).id = Math.min(result.get(ind).id, id);
                    }
                } catch (final NumberFormatException ex) {
                    LOGGER.log(Level.FINE, ex.getMessage(), ex);
                    localPrefs.remove(curKey);
                }
            } else {
                // clear the recent files history file from the old,
                // not known and broken keys
                localPrefs.remove(curKey);
            }
        }
        Collections.sort(result);
        store(result);

        return result;
    }

    protected static void store() {
        store(HISTORY);
    }

    protected static void store(final List<HistoryItem> history) {
        final Preferences localPrefs = getPrefs();
        for (int i = 0; i < history.size(); i++) {
            final HistoryItem hi = history.get(i);
            if ((hi.id != i) && (hi.id >= history.size())) {
                localPrefs.remove(PROP_URL_PREFIX + hi.id);
            }
            hi.id = i;
            localPrefs.put(PROP_URL_PREFIX + i, hi.getPath());
        }
    }

    protected static Preferences getPrefs() {
        if (prefs == null) {
            prefs = NbPreferences.forModule(RecentFiles.class).node(PREFS_NODE);
        }
        return prefs;
    }

    /**
     * Adds file represented by given TopComponent to the list, if conditions
     * are met.
     */
    private static void addFile(final TopComponent tc) {
        if (tc instanceof CloneableTopComponent) {
            addFile(obtainPath(tc));
        }
    }

    public static void addFile(final String path) {
        /**
         * Don't remember files that are saved in the temporary directory.
         *
         * algol
         */
        if (path != null && !path.startsWith(System.getProperty("java.io.tmpdir"))) {
            historyProbablyValid = false;
            synchronized (HISTORY_LOCK) {
                // avoid duplicates
                HistoryItem hItem = null;
                do {
                    hItem = findHistoryItem(path);
                } while (HISTORY.remove(hItem));

                hItem = new HistoryItem(0, path);
                HISTORY.add(0, hItem);
                for (int i = MAX_HISTORY_ITEMS; i < HISTORY.size(); i++) {
                    HISTORY.remove(i);
                }
                store();
            }
        }
    }

    /**
     * Removes file represented by given TopComponent from the list
     */
    private static void removeFile(final TopComponent tc) {
        historyProbablyValid = false;
        if (tc instanceof CloneableTopComponent) {
            final String path = obtainPath(tc);
            if (path != null) {
                synchronized (HISTORY_LOCK) {
                    final HistoryItem hItem = findHistoryItem(path);
                    if (hItem != null) {
                        HISTORY.remove(hItem);
                    }
                    store();
                }
            }
        }
    }

    private static String obtainPath(final TopComponent tc) {
        final DataObject dObj = tc.getLookup().lookup(DataObject.class);
        if (dObj != null) {
            final FileObject fo = dObj.getPrimaryFile();
            if (fo != null) {
                return convertFile2Path(fo);
            }
        }
        return null;
    }

    private static HistoryItem findHistoryItem(final String path) {
        for (final HistoryItem hItem : HISTORY) {
            if (path.equals(hItem.getPath())) {
                return hItem;
            }
        }
        return null;
    }

    protected static String convertFile2Path(final FileObject fo) {
        final File f = FileUtil.toFile(fo);
        return f == null ? null : f.getPath();
    }

    public static FileObject convertPath2File(final String path) {
        File f = new File(path);
        f = FileUtil.normalizeFile(f);
        return f == null ? null : FileUtil.toFileObject(f);
    }

    /**
     * Checks recent files history and removes non-valid entries
     */
    private static void checkHistory() {
        assert Thread.holdsLock(HISTORY_LOCK);
        historyProbablyValid = !HISTORY.isEmpty();
    }

    protected static void pruneHistory() {
        synchronized (HISTORY_LOCK) {
            final Iterator<HistoryItem> it = HISTORY.iterator();
            while (it.hasNext()) {
                final HistoryItem historyItem = it.next();
                final File f = new File(historyItem.getPath());
                if (!f.exists()) {
                    it.remove();
                }
            }
        }
    }

    /**
     * One item of the recently closed files history. Comparable by the time
     * field, ascending from most recent to older items.
     */
    public static final class HistoryItem implements Comparable<HistoryItem> {

        private int id;
        private final String path;
        private String fileName;

        public HistoryItem(final int id, final String path) {
            this.path = path;
            this.id = id;
        }

        public String getPath() {
            return path;
        }

        public String getFileName() {
            if (fileName == null) {
                int pos = path.lastIndexOf(File.separatorChar);
                if ((pos != -1) && (pos < path.length())) {
                    fileName = path.substring(pos + 1);
                } else {
                    fileName = path;
                }
            }
            return fileName;
        }

        @Override
        public int compareTo(final HistoryItem o) {
            return this.id - o.id;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof HistoryItem) {
                return ((HistoryItem) obj).getPath().equals(path);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 17 * hash + (this.path != null ? this.path.hashCode() : 0);
            return hash;
        }
    }

    /**
     * Receives info about opened and closed TopComponents from window system.
     */
    private static class WindowRegistryL implements PropertyChangeListener {

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            final String name = evt.getPropertyName();

            /**
             * Invert the original recent files logic: a file goes on the recent
             * list when it is opened.
             *
             * algol
             */
            if (TopComponent.Registry.PROP_TC_OPENED.equals(name)) {
                addFile((TopComponent) evt.getNewValue());
            }

            /**
             * Add a check for something being saved.
             */
            if (PROP_SAVED.equals(name)) {
                addFile((String) evt.getNewValue());
            }
        }
    }
}
