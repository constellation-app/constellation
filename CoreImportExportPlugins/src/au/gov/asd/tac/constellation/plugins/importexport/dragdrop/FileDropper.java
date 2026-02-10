/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.importexport.dragdrop;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.file.io.GraphJsonReader;
import au.gov.asd.tac.constellation.graph.file.io.GraphParseException;
import au.gov.asd.tac.constellation.graph.file.opener.GraphOpener;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.dragdrop.GraphDropper;
import au.gov.asd.tac.constellation.graph.visual.dragdrop.GraphDropper.DropInfo;
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
import au.gov.asd.tac.constellation.utilities.gui.HandleIoProgress;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.NotifyDescriptor;
import org.openide.util.lookup.ServiceProvider;

/**
 * A GraphDropper that accepts graph files.
 *
 * @author algol
 */
@PluginInfo(pluginType = PluginType.IMPORT, tags = {PluginTags.IMPORT})
@ServiceProvider(service = GraphDropper.class)
public class FileDropper implements GraphDropper {
    
    private static final Logger LOGGER = Logger.getLogger(FileDropper.class.getName());

    @Override
    public BiConsumer<Graph, DropInfo> drop(final DropTargetDropEvent dtde) {

        final Transferable transferable = dtde.getTransferable();
        if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            try {
                final Object data = dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                return (graph, dropInfo) -> {
                    @SuppressWarnings("unchecked") //data will be list of files which extends from object type
                    final List<File> files = (List<File>) data;
                    files.stream().forEach(file -> {
                        if (file.isFile()) {
                            PluginExecution.withPlugin(new DragAndDropFilePlugin(file)).executeLater(graph);
                        }
                    });
                };
            } catch (final UnsupportedFlavorException | IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }

        return null;
    }

    @PluginInfo(pluginType = PluginType.IMPORT, tags = {PluginTags.IMPORT})
    public static class DragAndDropFilePlugin extends SimplePlugin {

        private final File file;

        public DragAndDropFilePlugin(final File file) {
            this.file = file;
        }

        @Override
        public String getName() {
            return "Drag and Drop: File to Graph";
        }

        @Override
        public void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            try {
                final Graph g = new GraphJsonReader().readGraphZip(file, new HandleIoProgress(String.format("Reading %s...", file.getName())));
                GraphOpener.getDefault().openGraph(g, file.getName());

                final ReadableGraph rg = g.getReadableGraph();
                try {
                    ConstellationLoggerHelper.importPropertyBuilder(
                            this,
                            GraphRecordStoreUtilities.getVertices(rg, false, false, false).getAll(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL),
                            Arrays.asList(file),
                            ConstellationLoggerHelper.SUCCESS
                    );
                } finally {
                    rg.release();
                }
            } catch (final IOException | GraphParseException ex) {
                LOGGER.log(Level.WARNING, String.format("Error loading file %s: %s", file.getPath(), ex.getMessage()));
                NotifyDisplayer.display("Error loading graph: " + ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            }
        }
    }
}
