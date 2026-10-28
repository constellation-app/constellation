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
package au.gov.asd.tac.constellation.plugins.importexport.dragdrop;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.dragdrop.GraphDropper;
import au.gov.asd.tac.constellation.graph.visual.dragdrop.GraphDropper.DropInfo;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.arrangements.AbstractInclusionGraph;
import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import au.gov.asd.tac.constellation.plugins.arrangements.VertexListInclusionGraph;
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLoggerHelper;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDropEvent;
import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.lookup.ServiceProvider;

/**
 * Drop a RecordStore JSON document.
 * <p>
 * Detect a JSON document, convert it to a RecordStore, then add the RecordStore
 * to the graph. This is for an alternative representation of Record Store to
 * JsonDropper.
 *
 * @author andromeda
 */
@PluginInfo(pluginType = PluginType.IMPORT, tags = {PluginTags.IMPORT})
@ServiceProvider(service = GraphDropper.class, position = 1)
public class RecordStoreJsonDropper implements GraphDropper {

    private static final Logger LOGGER = Logger.getLogger(RecordStoreJsonDropper.class.getName());

    private static final String INDICATOR = "JSON=";

    @Override
    public BiConsumer<Graph, DropInfo> drop(final DropTargetDropEvent dtde) {
        final Transferable transferable = dtde.getTransferable();
        if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                final String t = (String) transferable.getTransferData(DataFlavor.stringFlavor);

                // Do we have the correct indicator and json format with pattern similar to "source.Identifier"?
                String regex = "\"[a-zA-Z]+\\.[a-zA-Z]+\"";

                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(t);
                if (t != null && t.startsWith(INDICATOR) && matcher.find()) {
                    return (graph, dropInfo) -> {

                        // Skip the leading "indicator=".
                        final String data = t.substring(INDICATOR.length());

                        PluginExecution.withPlugin(new DragAndDropRecordStoreJSONPlugin(data)).executeLater(graph);
                    };
                }

            } catch (final UnsupportedFlavorException | IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }

        return null;
    }

    @PluginInfo(pluginType = PluginType.IMPORT, tags = {PluginTags.IMPORT})
    public static class DragAndDropRecordStoreJSONPlugin extends SimpleEditPlugin {

        private final String data;

        public DragAndDropRecordStoreJSONPlugin(final String data) {
            this.data = data;
        }

        @Override
        public String getName() {
            return "Drag and Drop: JSON (RecordStore) to Graph";
        }

        @Override
        public void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            try {
                final RecordStore rs = GraphRecordStoreUtilities.fromRecordStoreJson(data);
                final List<Integer> newVertices = GraphRecordStoreUtilities.addRecordStoreToGraph(wg, rs, true, true, null);
                if (!newVertices.isEmpty()) {
                    final VertexListInclusionGraph vlGraph = new VertexListInclusionGraph(wg, AbstractInclusionGraph.Connections.NONE, newVertices);
                    PluginExecution.withPlugin(ArrangementPluginRegistry.GRID_COMPOSITE).executeNow(vlGraph.getInclusionGraph());
                    vlGraph.retrieveCoords();

                    ConstellationLoggerHelper.importPropertyBuilder(
                            this,
                            rs.getAll(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL),
                            null,
                            ConstellationLoggerHelper.SUCCESS
                    );
                }
            } catch (final IOException | ClassCastException ex) {
                NotificationDisplayer.getDefault().notify("Drag/drop error",
                        UserInterfaceIconProvider.ERROR.buildIcon(16, ConstellationColor.CHERRY.getJavaColor()),
                        "The document you dragged could not be parsed: " + ex.getMessage(), null
                );
            }
        }
    }
}
