/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.interaction.gui;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.file.GraphDataObject;
import au.gov.asd.tac.constellation.graph.file.GraphObjectUtilities;
import au.gov.asd.tac.constellation.graph.file.opener.GraphOpener;
import au.gov.asd.tac.constellation.graph.file.save.AutosaveUtilities;
import au.gov.asd.tac.constellation.graph.file.io.GraphJsonReader;
import au.gov.asd.tac.constellation.graph.file.io.GraphParseException;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLoggerHelper;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import au.gov.asd.tac.constellation.utilities.gui.HandleIoProgress;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 * A GraphOpener that opens a graph into a VisualTopComponent.
 * <p>
 * Note that if you don't have support for OpenGL then comment out the
 * ServiceProvider annotation which will mean that
 * {@link au.gov.asd.tac.constellation.graph.node.gui.SimpleGraphTopComponent}
 * is used instead.
 *
 * @author algol
 */
@Messages({
    "# {0} - fnam",
    "# {1} - save datetime",
    "# {2} - autosave datetime",
    "MSG_Autosave=File {0}\nsaved on {1}\nautosaved on {2}\nDo you want the more recent autosaved version?"
})
@ServiceProvider(service = GraphOpener.class, position = 100)
public final class VisualGraphOpener extends GraphOpener {

    private static final Logger LOGGER = Logger.getLogger(VisualGraphOpener.class.getName());

    /**
     * Open a graph file into a VisualTopComponent.
     * <p>
     * A check is done to see if the file to be opened is already open. If it
     * is, that TopComponent is made active, rather than opening the file again.
     *
     * @param gdo The GraphDataObject to read from.
     */
    @Override
    public void openGraph(final GraphDataObject gdo) {
        for (TopComponent tc : TopComponent.getRegistry().getOpened()) {
            if (tc instanceof VisualGraphTopComponent) {
                // Get the VisualTopComponent's GraphDataObject from its Lookup.
                // Two DataObjects are equivalent if their primary files are equal.
                final GraphDataObject existingGdo = tc.getLookup().lookupAll(GraphDataObject.class).iterator().next();
                if (gdo.equals(existingGdo)) {
                    tc.requestActive();
                    return;
                }
            }
        }

        // The file isn't already open.
        // Check to see if there is a more recent autosave for this file.
        // If there is, ask the user if they want to open it.
        final File f = FileUtil.toFile(gdo.getPrimaryFile());
        final Properties props = AutosaveUtilities.getAutosave(f);
        if (props != null) {
            final String dtprop = props.getProperty(AutosaveUtilities.DT);
            if (dtprop != null) {
                final ZonedDateTime zdtAutosave = ZonedDateTimeAttributeDescription.parseString(dtprop);
                final long dtFile = f.lastModified();
                if (zdtAutosave.toEpochSecond() * 1000 > dtFile) {
                    final String dtf = new Date(dtFile).toString();
                    final String msg = Bundle.MSG_Autosave(f.getPath(), dtf, dtprop);
                    final NotifyDescriptor nd = new NotifyDescriptor(msg, "Open autosaved file?", NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE, null, null);
                    if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION) {
                        // The user wants the more recent autosaved version.
                        // Rename the actual file (to .bak), copy the autosaved version to the actual name, and delete the bak file.
                        final File autosaved = new File(AutosaveUtilities.getAutosaveDir(), props.getProperty(AutosaveUtilities.ID) + GraphDataObject.FILE_EXTENSION);
                        try {
                            AutosaveUtilities.copyFile(autosaved, f);
                        } catch (IOException ex) {
                            LOGGER.log(Level.WARNING, "Copying autosaved file", ex);
                        }
                    }

                    AutosaveUtilities.deleteAutosave(props.getProperty(AutosaveUtilities.ID));
                }
            }
        }

        // The file isn't already open, so open it.
        new GraphFileOpener(gdo, null, null).execute();
    }

    @Override
    public void openGraph(final Graph graph, final String name) {
        final GraphDataObject gdo = GraphObjectUtilities.createMemoryDataObject(name, true);
        new GraphFileOpener(gdo, graph, null).execute();
    }

    @Override
    public void openGraph(final Graph graph, final String name, Runnable doAfter) {
        final GraphDataObject gdo = GraphObjectUtilities.createMemoryDataObject(name, true);
        new GraphFileOpener(gdo, graph, doAfter).execute();
    }

    @Override
    public void openGraph(final Graph graph, final String name, final boolean numbered) {
        final GraphDataObject gdo = GraphObjectUtilities.createMemoryDataObject(name, numbered);
        new GraphFileOpener(gdo, graph, null).execute();
    }

    /**
     * A worker that opens the file on a background thread.
     */
    private static class GraphFileOpener extends SwingWorker<Void, Integer> {

        private final GraphDataObject gdo;
        private Graph graph;
        private final Runnable doAfter;
        private Exception gex;
        private long time;

        /**
         * Construct a new GraphFileOpener.
         *
         * @param gdo The DataObject containing the graph file.
         * @param graph The graph to write into.
         */
        GraphFileOpener(final GraphDataObject gdo, final Graph graph, Runnable doAfter) {
            this.gdo = gdo;
            this.graph = graph;
            this.doAfter = doAfter;
        }

        @Override
        protected Void doInBackground() throws Exception {
            final File graphFile = FileUtil.toFile(gdo.getPrimaryFile());
            if (graph == null) {
                try {
                    final long t0 = System.currentTimeMillis();
                    graph = new GraphJsonReader().readGraphZip(graphFile, new HandleIoProgress(String.format("Reading %s...", graphFile.getName())));
                    time = System.currentTimeMillis() - t0;
                } catch (GraphParseException | IOException | RuntimeException ex) {
                    gex = ex;
                }

                PluginExecution.withPlugin(new SimplePlugin("Open Graph File") {
                    @Override
                    protected void execute(PluginGraphs graphs, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
                        ConstellationLoggerHelper.viewPropertyBuilder(this, graphFile, ConstellationLoggerHelper.SUCCESS);
                    }
                }).executeLater(null);
            }

            return null;
        }

        @Override
        protected void done() {
            if (gex != null) {
                Logger.getLogger(GraphFileOpener.class.getName()).log(Level.INFO, "Opening " + gdo.getPrimaryFile().getPath(), gex);
                String exName = gex.getClass().getCanonicalName();
                if (exName.lastIndexOf('.') != -1) {
                    exName = exName.substring(exName.lastIndexOf('.') + 1);
                }

                final String name = exName;
                final ActionListener al = new ActionListener() {
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        final NotifyDescriptor d = new NotifyDescriptor.Message(String.format("%s error opening graph:\n%s", name, gex.getMessage()), NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(d);
                    }
                };

                NotificationDisplayer.getDefault().notify("Reading " + gdo.getPrimaryFile().getName(),
                        UserInterfaceIconProvider.ERROR.buildIcon(16, ConstellationColor.CHERRY.getJavaColor()),
                        "There was a problem reading " + gdo.getPrimaryFile().getPath(),
                        al,
                        NotificationDisplayer.Priority.HIGH
                );
            } else if (graph != null) {
                final String msg = String.format("%s read complete (%.1fs)", gdo.getPrimaryFile().getName(), time / 1000f);
                StatusDisplayer.getDefault().setStatusText(msg);
                LOGGER.info(msg);

                final VisualGraphTopComponent vtc = new VisualGraphTopComponent(gdo, graph);
                vtc.open();
                vtc.requestActive();

                if (doAfter != null) {
                    doAfter.run();
                }
            }
        }
    }
}
