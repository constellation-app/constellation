/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.importing.file;

import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.ProcessingException;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.importing.ImportGraphFilePlugin.RETRIEVE_TRANSACTIONS_PARAMETER_ID;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.stage.FileChooser.ExtensionFilter;
import org.openide.NotifyDescriptor;
import static org.openide.NotifyDescriptor.DEFAULT_OPTION;
import org.openide.util.lookup.ServiceProvider;

/**
 * Importer for the GML file type
 *
 * @author canis_majoris
 * @author antares
 */
@ServiceProvider(service = GraphFileImportProcessor.class)
public class GMLImportProcessor implements GraphFileImportProcessor {
    
    private static final Logger LOGGER = Logger.getLogger(GMLImportProcessor.class.getName());
    
    public static final String NODE_TAG = "node";
    public static final String EDGE_TAG = "edge";
    public static final String START_TAG = "[";
    public static final String END_TAG = "]";

    @Override
    public String getName() {
        return "GML";
    }

    @Override
    public ExtensionFilter getExtensionFilter() {
        return new ExtensionFilter("GML files", "*.gml");
    }

    @Override
    public void process(final PluginParameters parameters, final File input, final RecordStore output) throws ProcessingException {
        if (input == null) {
            throw new ProcessingException("Please specify a file to read from");
        }
        
        if (output == null) {
            throw new ProcessingException("Please specify a record store to output to");
        }
        
        final RecordStore nodeRecords = new GraphRecordStore();
        final RecordStore edgeRecords = new GraphRecordStore();

        // Initialize variables
        final String filename = input.getPath();
        final boolean retrieveTransactions = parameters == null
                || parameters.getParameters().get(RETRIEVE_TRANSACTIONS_PARAMETER_ID) == null
                || parameters.getParameters().get(RETRIEVE_TRANSACTIONS_PARAMETER_ID).getBooleanValue();
        final Map<String, String> nodeIdToType = new HashMap<>();
        String line;
        boolean node = false;
        boolean edge = false;

        try (final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(input), StandardCharsets.UTF_8))) {
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.startsWith(NODE_TAG)) {
                    node = true;
                    nodeRecords.add();
                    nodeRecords.set(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.SOURCE, filename);
                } else if (line.startsWith(EDGE_TAG)) {
                    edge = true;
                    if (retrieveTransactions) {
                        edgeRecords.add();
                        edgeRecords.set(GraphRecordStoreUtilities.TRANSACTION + AnalyticConcept.TransactionAttribute.SOURCE, filename);
                    }
                } else if (line.startsWith(START_TAG)) {
                    //do nothing
                } else if (line.startsWith(END_TAG)) {
                    if (node) {
                        // check the type of the node, if it doesn't exist, add one
                        if (!nodeRecords.hasValue(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.TYPE)) {
                            nodeRecords.set(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.TYPE, SchemaVertexType.unknownType().getName());
                        }
                        nodeIdToType.put(
                                nodeRecords.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER), 
                                nodeRecords.get(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.TYPE));
                    }
                    node = false;
                    edge = false;
                } else if (node) {
                    try {
                        // Read node data
                        final String key = line.split(" ")[0].trim();
                        final String value = line.split(" ")[1].trim().replace("\"", "");
                        if ("id".equals(key)) {
                            nodeRecords.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, value);
                        } else {
                            nodeRecords.set(GraphRecordStoreUtilities.SOURCE + key, value);
                        }
                    } catch (final ArrayIndexOutOfBoundsException ex) {
                        // Do nothing
                    }
                } else if (retrieveTransactions && edge) {
                    try {
                        final String key = line.split(" ")[0].trim();
                        final String value = line.split(" ")[1].trim().replace("\"", "");
                        switch (key) {
                            case "source":
                                edgeRecords.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, value);
                                break;
                            case "target":
                                edgeRecords.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.IDENTIFIER, value);
                                break;
                            default:
                                edgeRecords.set(GraphRecordStoreUtilities.TRANSACTION + key, value);
                                break;
                        }
                    } catch (final ArrayIndexOutOfBoundsException ex) {
                        // Do nothing
                    }
                }
            }
            
            // resolve the node types between edges so that each edge is correctly added to the graph
            for (int i = 0; i < edgeRecords.size(); i++) {
                edgeRecords.set(i, GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.TYPE, 
                        nodeIdToType.get(edgeRecords.get(i, GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER)));
                edgeRecords.set(i, GraphRecordStoreUtilities.DESTINATION + AnalyticConcept.VertexAttribute.TYPE, 
                        nodeIdToType.get(edgeRecords.get(i, GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.IDENTIFIER)));
            }
        } catch (final FileNotFoundException ex) {
            NotifyDisplayer.display(new NotifyDescriptor("Error:\n" + "File " + filename + " not found", "Import GML File", DEFAULT_OPTION, 
                    NotifyDescriptor.ERROR_MESSAGE, new Object[]{NotifyDescriptor.OK_OPTION}, NotifyDescriptor.OK_OPTION));
            LOGGER.log(Level.SEVERE, ex, () -> "File " + filename + " not found");
        } catch (final IOException ex) {
            NotifyDisplayer.display(new NotifyDescriptor("Error:\n" + "Error reading file " + filename, "Import GML File", DEFAULT_OPTION, 
                    NotifyDescriptor.ERROR_MESSAGE, new Object[]{NotifyDescriptor.OK_OPTION}, NotifyDescriptor.OK_OPTION));
            LOGGER.log(Level.SEVERE, ex, () -> "Error reading file: " + filename);
        }

        output.add(nodeRecords);
        output.add(edgeRecords);
    }
}
