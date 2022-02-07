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
package au.gov.asd.tac.constellation.graph.interaction.plugins.io;

import java.util.logging.Logger;
import org.openide.windows.OnShowing;

/**
 * Start the autosaver in the background.
 * <p>
 * An OnShowing Runnable runs in the EDT. This is good, because we need to look
 * for autosaved unsaved graphs.
 *
 * @author algol
 */
@OnShowing(position = 2000)
public final class AutosaveStartup implements Runnable {

    private static final String AUTOSAVE_THREAD_NAME = "Autosave Startup";
    private static final Logger LOGGER = Logger.getLogger(AutosaveStartup.class.getName());
    /**
     * The number of milliseconds after which we purge old autosaves.
     */
    private static final long PURGE_PERIOD_MS = 28 * 24 * 60 * 60 * 1000L;

    @Override
    public void run() {
//        synchronized (String.class) {
//            // Look for existing autosaved in-memory graphs.
//            final File[] saveFiles = AutosaveUtilities.getAutosaves(FileExtensionConstants.STAR_AUTOSAVE);
//            final long now = new Date().getTime();
//
//            for (final File f : saveFiles) {
//                try {
//                    final Properties props = new Properties();
//                    try (InputStream in = new FileInputStream(f)) {
//                        props.load(in);
//                    }
//
//                    final String dtprop = props.getProperty(AutosaveUtilities.DT);
//                    final String name = props.getProperty(AutosaveUtilities.NAME);
//                    final boolean unsaved = "true".equals(props.getProperty(AutosaveUtilities.UNSAVED));
//
//                    if (dtprop != null) {
//                        if (unsaved) {
//                            final String msg = String.format("Graph %s (autosaved at %s).%nDo you want to recover it?", name != null ? name : "<unknown>", dtprop);
//                            final NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, "Autosaved graph", NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE);
//                            if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION) {
//                                // Load the autosaved graph away from the EDT.
//                                new Thread() {
//                                    @Override
//                                    public void run() {
//                                        setName(AUTOSAVE_THREAD_NAME);
//                                        final String loading = String.format("Loading autosaved graph %s", name);
//                                        try {
//                                            // Remove the "_auto" from the end and load the matching graph.
//                                            String path = f.getPath();
//                                            path = path.substring(0, path.length() - 5);
//                                            final Graph g = new GraphJsonReader().readGraphZip(new File(path), new HandleIoProgress(loading));
//                                            GraphOpener.getDefault().openGraph(g, name, false);
//
//                                            AutosaveUtilities.deleteAutosave(f);
//                                        } catch (GraphParseException | IOException ex) {
//                                            LOGGER.log(Level.WARNING, "Error loading graph", ex);
//                                            NotifyDisplayer.display("Error loading graph: " + ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
//                                        }
//                                    }
//                                }.start();
//                            } else {
//                                // If the user doesn't want it we get rid of it.
//                                AutosaveUtilities.deleteAutosave(f);
//                            }
//                        } else if (now - f.lastModified() > PURGE_PERIOD_MS) {
//                            // This autosave is old enough to be purged; the user won't remember the details of the graph.
//                            AutosaveUtilities.deleteAutosave(f);
//                        } else {
//                            // Do nothing
//                        }
//                    } else {
//                        // Some information about this autosave is missing so get rid of it.
//                        AutosaveUtilities.deleteAutosave(f);
//                    }
//                } catch (IOException ex) {
//                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
//                }
//            }
//
//            // Start autosaving in the background.
//            Autosaver.schedule(0);
//        }
    }
}
