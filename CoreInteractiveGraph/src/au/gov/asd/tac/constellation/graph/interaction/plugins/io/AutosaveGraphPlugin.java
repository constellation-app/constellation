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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.file.io.GraphJsonWriter;
import au.gov.asd.tac.constellation.graph.file.save.AutosaveUtilities;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLoggerHelper;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.gui.HandleIoProgress;
import au.gov.asd.tac.constellation.utilities.temporal.TemporalFormatting;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Autosave a single graph.
 * <p>
 * The caller has to go through the graphs and pass them one by one.
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@Messages("AutosaveGraphPlugin=Autosave Graph")
@PluginInfo(pluginType = PluginType.EXPORT, tags = {PluginTags.EXPORT})
public final class AutosaveGraphPlugin extends SimplePlugin {

    private static final Logger LOGGER = Logger.getLogger(AutosaveGraphPlugin.class.getName());

    @Override
    public void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final Graph graph = graphs.getGraph();
        final String graphId = graph.getId();
        final GraphNode gnode = GraphNode.getGraphNode(graphId);

        // The user might have deleted the graph, so check first.
        if (gnode != null) {

            interaction.setProgress(-1, -1, "Autosaving: " + graphId, true);

            // We don't want to hold the user up while we're reading from a graph they might be using.
            // Make a copy of the graph so that we can release the read lock as soon as possible.
            GraphReadMethods copy;
            ReadableGraph rg = graph.getReadableGraph();
            try {
                copy = rg.copy();
            } finally {
                rg.release();
            }

            interaction.setProgress(1, 0, "Finished", true);

            final File saveDir = AutosaveUtilities.getAutosaveDir();

            try {
                final String gname = graph.getId() + FileExtensionConstants.STAR_EXTENSION;
                StatusDisplayer.getDefault().setStatusText(String.format("Auto saving %s as %s at %s...", graphId, gname, new Date()));
                final File saveFile = new File(saveDir, gname);
                new GraphJsonWriter().writeGraphToZip(copy, saveFile.getPath(), new HandleIoProgress("Autosaving..."));

                ConstellationLoggerHelper.exportPropertyBuilder(
                        this,
                        GraphRecordStoreUtilities.getVertices(copy, false, false, false).getAll(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL),
                        saveFile,
                        ConstellationLoggerHelper.SUCCESS
                );

                final Properties p = new Properties();
                p.setProperty(AutosaveUtilities.ID, graph.getId());
                p.setProperty(AutosaveUtilities.NAME, gnode.getName());
                p.setProperty(AutosaveUtilities.PATH, gnode.getDataObject().getPrimaryFile().getPath());
                p.setProperty(AutosaveUtilities.UNSAVED, Boolean.toString(gnode.getDataObject().isInMemory()));
                p.setProperty(AutosaveUtilities.DT, ZonedDateTime.now().format(TemporalFormatting.ZONED_DATE_TIME_FORMATTER));
                try (OutputStream s = new FileOutputStream(new File(saveDir, gname + "_auto"))) {
                    p.store(s, null);
                }
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }
    }
}
