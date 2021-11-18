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
package au.gov.asd.tac.constellation.views.dataaccess.templates;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.processing.RecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.dragdrop.GraphDropper;
import au.gov.asd.tac.constellation.graph.visual.dragdrop.GraphDropper.DropInfo;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLoggerHelper;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDropEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.BiConsumer;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 * Allows a RecordStore to be dropped onto the graph which will then be applied
 * to the graph.
 *
 * @author sirius
 */
@PluginInfo(pluginType = PluginType.IMPORT, tags = {PluginTags.IMPORT})
@ServiceProvider(service = GraphDropper.class, position = 1)
public class RecordStoreDropper implements GraphDropper {

    private static final byte[] RECORD_STORE_BYTES;

    protected static final DataFlavor RECORD_STORE_FLAVOR;

    static {
        DataFlavor recordStoreFlavor = null;
        byte[] recordStoreBytes = null;

        try {
            recordStoreFlavor = new DataFlavor("text/plain;class=java.io.InputStream;charset=UTF-8");
            recordStoreBytes = "RecordStore=".getBytes(StandardCharsets.UTF_8.name());
        } catch (ClassNotFoundException | UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        }

        RECORD_STORE_FLAVOR = recordStoreFlavor;
        RECORD_STORE_BYTES = recordStoreBytes;
    }

    @Override
    public BiConsumer<Graph, DropInfo> drop(final DropTargetDropEvent dtde) {
        try {
            final Transferable transferable = dtde.getTransferable();
            if (transferable.isDataFlavorSupported(RECORD_STORE_FLAVOR)) {
                final Object data = transferable.getTransferData(RECORD_STORE_FLAVOR);
                if (data instanceof InputStream) {
                    try (final InputStream in = (InputStream) data) {

                        final byte[] buffer = new byte[RECORD_STORE_BYTES.length];
                        if (in.read(buffer) == buffer.length && Arrays.equals(buffer, RECORD_STORE_BYTES)) {
                            final RecordStore recordStore = RecordStoreUtilities.fromJson(in);

                            if (recordStore != null) {
                                return (graph, dropInfo) -> {
                                    PluginExecution.withPlugin(new RecordStoreDropperToGraphPlugin(recordStore)).executeLater(graph);
                                };
                            }
                        }
                    }
                }
            }
        } catch (UnsupportedFlavorException | IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    @PluginInfo(pluginType = PluginType.IMPORT, tags = {PluginTags.IMPORT})
    public static class RecordStoreDropperToGraphPlugin extends RecordStoreQueryPlugin {

        private final RecordStore recordStore;

        public RecordStoreDropperToGraphPlugin(final RecordStore recordStore) {
            this.recordStore = recordStore;
        }

        @Override
        protected RecordStore query(final RecordStore query, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            ConstellationLoggerHelper.importPropertyBuilder(
                    this,
                    recordStore.getAll(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL),
                    null,
                    ConstellationLoggerHelper.SUCCESS
            );
            return recordStore;
        }

        @Override
        public String getName() {
            return "Drag and Drop: RecordStore To Graph";
        }

    }

}
