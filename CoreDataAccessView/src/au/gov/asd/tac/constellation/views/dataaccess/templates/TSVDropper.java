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
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
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
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLoggerHelper;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDropEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.zip.GZIPInputStream;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 * A GraphDropper that accepts files with .tsv or .tsv.gz extensions.
 *
 * The file is assumed to contained tab separated values where each row
 * represents a transaction. The columns should be compatible with a
 * RecordStore.
 *
 * @author sirius
 */
@PluginInfo(pluginType = PluginType.IMPORT, tags = {PluginTags.IMPORT})
@ServiceProvider(service = GraphDropper.class, position = 1)
public class TSVDropper implements GraphDropper {

    @Override
    public BiConsumer<Graph, DropInfo> drop(final DropTargetDropEvent dtde) {

        // Only work on files
        final Transferable transferable = dtde.getTransferable();
        if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            try {
                // Get the data as a list of files
                final Object data = dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                @SuppressWarnings("unchecked") //data will be list of files which extends Object type
                final List<File> files = (List<File>) data;

                // Create a record store to hold the combined results
                final RecordStore recordStore = new GraphRecordStore();

                boolean badData = false;

                // Process each file...
                for (final File file : files) {
                    // Only process files
                    if (file.isFile()) {
                        // Only process files that have a .tsv or .tsv.gz extension
                        // If any file does not have this extension then reject all the files.
                        final InputStream in;
                        if (file.getName().endsWith(".tsv.gz")) {
                            in = new GZIPInputStream(new FileInputStream(file));
                        } else if (file.getName().endsWith(".tsv")) {
                            in = new FileInputStream(file);
                        } else {
                            badData = true;
                            break;
                        }

                        // Open a reader so that we can read the file line by line
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8.name()))) {
                            String[] columnHeaders = null;

                            String line = reader.readLine();
                            while (line != null) {
                                String[] fields = line.split(SeparatorConstants.TAB);

                                if (columnHeaders == null) {
                                    columnHeaders = fields;
                                } else {
                                    recordStore.add();
                                    final int fieldsCount = Math.min(columnHeaders.length, fields.length);
                                    for (int i = 0; i < fieldsCount; i++) {
                                        recordStore.set(columnHeaders[i], fields[i]);
                                    }
                                }

                                line = reader.readLine();
                            }
                        }
                        // If any directories are encountered then don't allow the drop
                    } else {
                        badData = true;
                        break;
                    }
                }

                if (!badData && recordStore.size() > 0) {
                    return (graph, dropInfo) -> {
                        PluginExecution.withPlugin(new TSVDropperToGraphPlugin(recordStore, files)).executeLater(graph);
                    };
                }
            } catch (final UnsupportedFlavorException | IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return null;
    }

    @PluginInfo(pluginType = PluginType.IMPORT, tags = {PluginTags.IMPORT})
    public static class TSVDropperToGraphPlugin extends RecordStoreQueryPlugin {

        private final RecordStore recordStore;
        private final List<File> files;

        public TSVDropperToGraphPlugin(final RecordStore recordStore, final List<File> files) {
            this.recordStore = recordStore;
            this.files = files;
        }

        @Override
        protected RecordStore query(final RecordStore query, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            ConstellationLoggerHelper.importPropertyBuilder(
                    this,
                    recordStore.getAll(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL),
                    files,
                    ConstellationLoggerHelper.SUCCESS
            );
            return recordStore;
        }

        @Override
        public String getName() {
            return "Drag and Drop: TSV File to Graph";
        }

    }
}
