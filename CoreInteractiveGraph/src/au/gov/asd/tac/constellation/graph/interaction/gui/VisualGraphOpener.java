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
package au.gov.asd.tac.constellation.graph.interaction.gui;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.file.GraphDataObject;
import au.gov.asd.tac.constellation.graph.file.GraphObjectUtilities;
import au.gov.asd.tac.constellation.graph.file.io.GraphJsonReader;
import au.gov.asd.tac.constellation.graph.file.io.GraphParseException;
import au.gov.asd.tac.constellation.graph.file.opener.GraphOpener;
import au.gov.asd.tac.constellation.graph.file.save.AutosaveUtilities;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLoggerHelper;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.gui.HandleIoProgress;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import org.apache.commons.io.FileUtils;
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
 * Note that if you don't have support for OpenGL then comment out the ServiceProvider annotation which will mean that
 * {@link au.gov.asd.tac.constellation.graph.node.gui.SimpleGraphTopComponent} is used instead.
 *
 * @author algol
 */
@Messages({
    "# {0} - fnam",
    "# {1} - save datetime",
    "# {2} - autosave datetime",
    "MSG_Autosave={0}\nFile saved on {1}\nAutosaved on {2}\nDo you want the more recent autosaved version to be loaded?"
})
@ServiceProvider(service = GraphOpener.class, position = 100)
public final class VisualGraphOpener extends GraphOpener {

    private static final Logger LOGGER = Logger.getLogger(VisualGraphOpener.class.getName());

    private static final String UNABLE_TO_REMOVE_SECONDARY_BACKUP_MESSAGE = "Unable to remove old secondary backup file: {0}";

    private static final List<String> openingGraphs = new ArrayList<>();

    /**
     * Open a graph file into a VisualTopComponent.
     * <p>
     * A check is done to see if the file to be opened is already open. If it is, that TopComponent is made active,
     * rather than opening the file again.
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

        final File f = FileUtil.toFile(gdo.getPrimaryFile());

        // If graph is currently being opened, but isn't open yet
        if (openingGraphs.contains(f.getPath())) {
            return;
        }

        // The file isn't already open.
        // Check to see if there is a more recent autosave for this file.
        // If there is, ask the user if they want to open it.
        final Properties props = AutosaveUtilities.getAutosave(f);
        if (props != null) {
            final String dtprop = props.getProperty(AutosaveUtilities.DT);
            if (dtprop != null) {
                final ZonedDateTimeAttributeDescription datetimeAttributeDescription = new ZonedDateTimeAttributeDescription();
                final ZonedDateTime zdtAutosave = datetimeAttributeDescription.convertFromString(dtprop);
                final long dtFile = f.lastModified();
                final long zdtAutosaveSeconds = zdtAutosave.toEpochSecond() * 1000;
                if (zdtAutosaveSeconds > dtFile) {
                    final String dateTime = new Date(zdtAutosaveSeconds).toString();
                    final String dtf = new Date(dtFile).toString();
                    final String msg = Bundle.MSG_Autosave(f.getPath(), dtf, dateTime);
                    final NotifyDescriptor nd = new NotifyDescriptor(msg, "Open autosaved file?", NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE, null, null);
                    if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION) {
                        // The user wants the more recent autosaved version.
                        // Backup the current actual file and replace it with the autosave file.
                        final File autosaved = new File(AutosaveUtilities.getAutosaveDir(), props.getProperty(AutosaveUtilities.ID) + FileExtensionConstants.STAR);
                        try {
                            // make temp copy of any backup file that exists to try if we find both autosave and main file are corrupt
                            final File toBak = new File(f.getPath() + FileExtensionConstants.BACKUP);
                            if (toBak.exists()) {
                                // Make a backup of our backup which is about to be overwrtten by the Autodsave call
                                final File toBakBak = new File(toBak.getPath() + FileExtensionConstants.BACKUP);
                                final boolean toRenamed = toBak.renameTo(toBakBak);
                                if (!toRenamed) {
                                    LOGGER.log(Level.WARNING, "Unable to backup file: {0}", toBak);
                                } else {
                                    AutosaveUtilities.copyFile(autosaved, f);
                                }
                            } else {
                                AutosaveUtilities.copyFile(autosaved, f);
                            }

                        } catch (final IOException ex) {
                            LOGGER.log(Level.WARNING, "Copying autosaved file", ex);
                        }
                    }

                    AutosaveUtilities.deleteAutosave(props.getProperty(AutosaveUtilities.ID));
                }
            }
        }

        // Add to list of graphs currently being opened
        openingGraphs.add(f.getPath());

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

    public static List<String> getOpeningGraphs() {
        return new ArrayList<>(openingGraphs);
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
            final File backupFile = new File(graphFile.toString().concat(FileExtensionConstants.BACKUP));
            final File backupBackupFile = new File(backupFile.toString().concat(FileExtensionConstants.BACKUP));

            if (graph == null) {
                HandleIoProgress ioProgressHandler = new HandleIoProgress(String.format("Reading %s...", graphFile.getName()));
                try {
                    final long t0 = System.currentTimeMillis();
                    LOGGER.log(Level.INFO, "Attempting to open {0}", graphFile);
                    graph = new GraphJsonReader().readGraphZip(graphFile, ioProgressHandler);
                    time = System.currentTimeMillis() - t0;

                    // Everything worked, there was no need for any bakbak file
                    if (backupBackupFile.exists()) {
                        try {
                            Files.delete(Path.of(backupBackupFile.getPath()));
                        } catch (final IOException ex) {
                            LOGGER.log(Level.WARNING, UNABLE_TO_REMOVE_SECONDARY_BACKUP_MESSAGE, backupBackupFile);
                        }
                    }

                } catch (final GraphParseException | IOException | RuntimeException ex) {
                    gex = ex;
                }

                try {
                    if (gex != null) {
                        // An exception was thrown trying to read specified star file. The most likely reason for this is
                        // a corrupt star file. Check to see if there was a 'backup' star file generated before the star file
                        // was written - if so, attempt to load this.

                        // Clear previous progress message and reset to indicate we are trying to use backup.
                        ioProgressHandler.finish();
                        if (backupFile.exists()) {
                            // Set new progress message to highlight attempt to load backup
                            ioProgressHandler = new HandleIoProgress(String.format("Unable to read %s, reading backup %s...", graphFile.getName(), backupFile.getName()));
                            // Try to load backup file that was located, if it loads then clear previous exception, if not the
                            // original exception is kept to be handled in the done method
                            final long t0 = System.currentTimeMillis();
                            LOGGER.log(Level.WARNING, "Unable to open requested file, attempting to open backup {0}", backupFile);
                            graph = new GraphJsonReader().readGraphZip(backupFile, ioProgressHandler);
                            time = System.currentTimeMillis() - t0;
                            gex = null;

                            // Backup file successfully loaded, copy it over top of corrupt actual file - theres no reason to keep the corrupted file.
                            // Don't do a move, rather perform the move in two stages, a copy, then a delete to ensure there
                            // is always going to be a valid file somewhere as only the copy or the delete can fail in a given run.
                            LOGGER.log(Level.INFO, "Successfully opened backup file: {0}, replacing star file", backupFile);
                            FileUtils.copyFile(new File(backupFile.toString()), new File(graphFile.toString()));
                            // Everything worked, there was no need for any bakbak file
                            if (backupBackupFile.exists()) {
                                try {
                                    Files.delete(Path.of(backupBackupFile.getPath()));
                                } catch (final IOException ex) {
                                    LOGGER.log(Level.WARNING, UNABLE_TO_REMOVE_SECONDARY_BACKUP_MESSAGE, backupBackupFile);
                                }
                            }
                        }
                    }
                } catch (final GraphParseException | IOException | RuntimeException ex) {

                    if (!backupBackupFile.exists()) {
                        LOGGER.log(Level.WARNING, "Unable to open requested file ({0}) or associated backup", graphFile);
                    }
                    gex = ex;
                    // Clear previous progress message and reset to indicate we are trying to use backup.
                    ioProgressHandler.finish();
                }

                // Handle the rare case where user elected to open an autosave, however this autosave is corrupt.
                // Code then tried to open the original file, which is also found to be corrupt. The way autsave manages this
                // is to copy the original file to .bak and the autosave to the original file. As we can also have a .bak
                // we are copying this off to .bak.bak and only teying it if both the autosave and original file failed.
                if (gex != null && backupBackupFile.exists()) {
                    try {
                        final long t0 = System.currentTimeMillis();
                        ioProgressHandler = new HandleIoProgress(String.format("Unable to read backup %s, reading secondary backup %s...",
                                backupFile.getName(), backupBackupFile.getName()));
                        graph = new GraphJsonReader().readGraphZip(backupBackupFile, ioProgressHandler);
                        time = System.currentTimeMillis() - t0;
                        gex = null;

                        LOGGER.log(Level.INFO, "Successfully opened secondary backup file: {0}, replacing star file", backupBackupFile);
                        FileUtils.copyFile(new File(backupBackupFile.toString()), new File(graphFile.toString()));

                        try {
                            Files.delete(Path.of(backupFile.getPath()));
                        } catch (final IOException ex) {
                            LOGGER.log(Level.WARNING, "Unable to remove old backup file: {0}", backupFile);
                        }
                        try {
                            Files.delete(Path.of(backupBackupFile.getPath()));
                        } catch (final IOException ex) {
                            LOGGER.log(Level.WARNING, UNABLE_TO_REMOVE_SECONDARY_BACKUP_MESSAGE, backupBackupFile);
                        }
                    } catch (final GraphParseException | IOException | RuntimeException ex) {
                        LOGGER.log(Level.WARNING, "Unable to open requested file ({0}) or associated backups", graphFile);
                        ioProgressHandler.finish();
                    }
                }
                PluginExecution.withPlugin(new OpenGraphFile(graphFile)).executeLater(null);
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
                final ActionListener al = e -> {
                    final NotifyDescriptor d = new NotifyDescriptor.Message(String.format("%s error opening graph:%n%s", name, gex.getMessage()), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(d);
                };

                NotificationDisplayer.getDefault().notify("Reading " + gdo.getPrimaryFile().getName(),
                        UserInterfaceIconProvider.ERROR.buildIcon(16, ConstellationColor.CHERRY.getJavaColor()),
                        "There was a problem reading " + gdo.getPrimaryFile().getPath(),
                        al,
                        NotificationDisplayer.Priority.HIGH
                );
            } else if (graph != null) {
                final String msg = String.format("%s read complete (%.1fs)", gdo.getPrimaryFile().getName(), time / 1000F);
                StatusDisplayer.getDefault().setStatusText(msg);
                LOGGER.info(msg);

                final VisualGraphTopComponent vtc = new VisualGraphTopComponent(gdo, graph);
                vtc.open();
                vtc.requestActive();

                if (doAfter != null) {
                    doAfter.run();
                }
            } else {
                // Do nothing
            }

            // Graph has finished opening, so remove from list
            openingGraphs.remove(gdo.getPrimaryFile().getPath());
        }
    }

    /**
     * Plugin to open graph file.
     */
    @PluginInfo(pluginType = PluginType.IMPORT, tags = {PluginTags.IMPORT})
    private static class OpenGraphFile extends SimplePlugin {

        private final File graphFile;

        public OpenGraphFile(final File graphFile) {
            this.graphFile = graphFile;
        }

        @Override
        public String getName() {
            return "Visual Graph: Open Graph File";
        }

        @Override
        protected void execute(PluginGraphs graphs, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
            ConstellationLoggerHelper.viewPropertyBuilder(this, graphFile, ConstellationLoggerHelper.SUCCESS);
        }
    }
}
