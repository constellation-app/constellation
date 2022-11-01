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
 * Importer for the Pajek .net file type
 *
 * @author canis_majoris
 * @author antares
 */
@ServiceProvider(service = GraphFileImportProcessor.class)
public class PajekImportProcessor implements GraphFileImportProcessor {
    
    private static final Logger LOGGER = Logger.getLogger(PajekImportProcessor.class.getName());
    
    public static final String VERTEX_HEADER = "*V";
    public static final String EDGE_HEADER = "*E";

    @Override
    public String getName() {
        return "Pajek";
    }

    @Override
    public ExtensionFilter getExtensionFilter() {
        return new ExtensionFilter("Pajek files", "*.net");
    }

    @Override
    public void process(final PluginParameters parameters, final File input, final RecordStore output) throws ProcessingException {
        if (input == null) {
            throw new ProcessingException("Please specify a file to read from");
        }
        
        if (output == null) {
            throw new ProcessingException("Please specify a record store to output to");
        }
        
        // Initialize variables
        final String filename = input.getPath();
        final boolean retrieveTransactions = parameters == null
                || parameters.getParameters().get(RETRIEVE_TRANSACTIONS_PARAMETER_ID) == null
                || parameters.getParameters().get(RETRIEVE_TRANSACTIONS_PARAMETER_ID).getBooleanValue();
        String line;
        boolean processNodes = false;
        boolean processEdges = false;
        
        final Map<String, String> idLabelMap = new HashMap<>();

        try (final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(input), StandardCharsets.UTF_8))) {
            while ((line = in.readLine()) != null) {
                if (line.startsWith(VERTEX_HEADER)) {
                    processNodes = true;
                } else if (line.startsWith(EDGE_HEADER)) {
                    processNodes = false;
                    processEdges = true;
                } else if (processNodes) {
                    try {
                        // Read node data
                        final String nodeId = line.split("\"")[0].trim();
                        final String nodeLabel = line.split("\"")[1].trim();
                        if (retrieveTransactions) {
                            idLabelMap.put(nodeId, nodeLabel);
                        }
                        
                        output.add();
                        output.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, nodeLabel);
                        output.set(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.TYPE, SchemaVertexType.unknownType().getName());
                        output.set(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.SOURCE, filename);
                    } catch (final ArrayIndexOutOfBoundsException ex) {
                        // Do nothing
                    }
                } else if (processEdges && retrieveTransactions) {
                    try {
                        // Read edge data
                        final String[] fields = line.split("\\s+");
                        final String srcId = fields[0];
                        final String dstId = fields[1];
                        final String weight = fields[2];

                        output.add();
                        output.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, idLabelMap.get(srcId));
                        output.set(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.TYPE, SchemaVertexType.unknownType().getName());
                        output.set(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.SOURCE, filename);
                        output.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.IDENTIFIER, idLabelMap.get(dstId));
                        output.set(GraphRecordStoreUtilities.DESTINATION + AnalyticConcept.VertexAttribute.TYPE, SchemaVertexType.unknownType().getName());
                        output.set(GraphRecordStoreUtilities.DESTINATION + AnalyticConcept.VertexAttribute.SOURCE, filename);
                        output.set(GraphRecordStoreUtilities.TRANSACTION + AnalyticConcept.TransactionAttribute.COUNT, weight);
                        output.set(GraphRecordStoreUtilities.TRANSACTION + AnalyticConcept.TransactionAttribute.SOURCE, filename);
                    } catch (final ArrayIndexOutOfBoundsException ex) {
                        // Do nothing
                    }
                }
            }
        } catch (final FileNotFoundException ex) {
            NotifyDisplayer.display(new NotifyDescriptor("Error:\n" + "File " + filename + " not found", "Import Pajek File", DEFAULT_OPTION, 
                    NotifyDescriptor.ERROR_MESSAGE, new Object[]{NotifyDescriptor.OK_OPTION}, NotifyDescriptor.OK_OPTION));
            LOGGER.log(Level.SEVERE, ex, () -> "File " + filename + " not found");
        } catch (final IOException ex) {
            NotifyDisplayer.display(new NotifyDescriptor("Error:\n" + "Error reading file " + filename, "Import Pajek File", DEFAULT_OPTION, 
                    NotifyDescriptor.ERROR_MESSAGE, new Object[]{NotifyDescriptor.OK_OPTION}, NotifyDescriptor.OK_OPTION));
            LOGGER.log(Level.SEVERE, ex, () -> "Error reading file: " + filename);
        }
    }
}
