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
package au.gov.asd.tac.constellation.views.schemaview;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphIndexResult;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.utilities.GraphIndexUtilities;
import au.gov.asd.tac.constellation.graph.visual.dragdrop.GraphDropper;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLoggerHelper;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.schemaview.providers.VertexTypeNodeProvider;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDropEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.lookup.ServiceProvider;

/**
 * A GraphDropper that knows about vertex types and transaction types.
 *
 * @author algol
 */
@PluginInfo(pluginType = PluginType.IMPORT, tags = {PluginTags.IMPORT})
@ServiceProvider(service = GraphDropper.class, position = 1)
public class TypeDropper implements GraphDropper {
    
    private static final Logger LOGGER = Logger.getLogger(TypeDropper.class.getName());

    private static final String INDICATOR = SchemaVertexType.class.getSimpleName() + "=";

    private static final DataFlavor VX_DATA_FLAVOR;

    static {
        DataFlavor vx = null;
        try {
            vx = new DataFlavor(VertexTypeNodeProvider.MIMETYPE);
        } catch (final ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }

        VX_DATA_FLAVOR = vx;
    }

    @Override
    public BiConsumer<Graph, DropInfo> drop(final DropTargetDropEvent dtde) {
        final Transferable transferable = dtde.getTransferable();
        if (transferable.isDataFlavorSupported(VX_DATA_FLAVOR) || transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                final String data;
                if (transferable.isDataFlavorSupported(VX_DATA_FLAVOR)) {
                    final InputStream in = new ByteArrayInputStream(((ByteBuffer) transferable.getTransferData(VX_DATA_FLAVOR)).array());
                    final ObjectInputStream oin = new ObjectInputStream(in);
                    data = (String) oin.readObject();
                } else {
                    final String t = (String) transferable.getTransferData(DataFlavor.stringFlavor);

                    // Do we have the correct indicator?
                    if (t != null && t.startsWith(INDICATOR)) {
                        // Skip the leading "indicator=".
                        data = t.substring(INDICATOR.length());
                    } else {
                        data = null;
                    }
                }

                if (data != null) {
                    return (graph, dropInfo) -> PluginExecution.withPlugin(new TypeDropperPlugin(data, dropInfo, graph)).executeLater(graph);
                }
            } catch (final UnsupportedFlavorException | IOException | ClassNotFoundException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }
        return null;
    }

    /**
     * Plugin to help once the type had been dropped on a vertex
     */
    @PluginInfo(pluginType = PluginType.IMPORT, tags = {PluginTags.IMPORT})
    public static class TypeDropperPlugin extends SimpleEditPlugin {

        private final String data;
        private final DropInfo dropInfo;
        private final Graph graph;

        public TypeDropperPlugin(final String data, final DropInfo dropInfo, final Graph graph) {
            this.data = data;
            this.dropInfo = dropInfo;
            this.graph = graph;
        }

        @Override
        public String getName() {
            return "Drag and Drop: Type to Vertex";
        }

        @Override
        public void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            final int ix = data.indexOf(':');
            if (ix != -1) {
                final String attrLabel = data.substring(0, ix);
                final String value = data.substring(ix + 1);
                final int attrId = wg.getAttribute(GraphElementType.VERTEX, attrLabel);
                if (attrId != Graph.NOT_FOUND) {
                    final int vxId;

                    if (dropInfo.isIsVertex()) {
                        vxId = dropInfo.id;

                        // If the vertex is selected, modify all of the selected vertices.
                        // Otherwise, just modify the dropped-on vertex.
                        final int selectedId = VisualConcept.VertexAttribute.SELECTED.ensure(wg);
                        if (wg.getBooleanValue(selectedId, vxId)) {
                            final GraphIndexResult gir = GraphIndexUtilities.filterElements(wg, selectedId, true);
                            while (true) {
                                final int selVxId = gir.getNextElement();
                                if (selVxId == Graph.NOT_FOUND) {
                                    break;
                                }

                                wg.setStringValue(attrId, selVxId, data);
                                if (graph.getSchema() != null) {
                                    graph.getSchema().completeVertex(wg, selVxId);
                                }
                            }
                        } else {
                            wg.setStringValue(attrId, vxId, data);
                            if (graph.getSchema() != null) {
                                graph.getSchema().completeVertex(wg, vxId);
                            }
                        }
                    } else {
                        vxId = wg.addVertex();
                        final int xId = VisualConcept.VertexAttribute.X.ensure(wg);
                        final int yId = VisualConcept.VertexAttribute.Y.ensure(wg);
                        final int zId = VisualConcept.VertexAttribute.Z.ensure(wg);
                        wg.setStringValue(attrId, vxId, value);
                        wg.setFloatValue(xId, vxId, dropInfo.location.getX());
                        wg.setFloatValue(yId, vxId, dropInfo.location.getY());
                        wg.setFloatValue(zId, vxId, dropInfo.location.getZ());

                        if (graph.getSchema() != null) {
                            graph.getSchema().newVertex(wg, vxId);
                            graph.getSchema().completeVertex(wg, vxId);
                        }
                    }

                    ConstellationLoggerHelper.importPropertyBuilder(
                            this,
                            Arrays.asList(data),
                            null,
                            ConstellationLoggerHelper.SUCCESS
                    );
                }
            }
        }

    }

}
