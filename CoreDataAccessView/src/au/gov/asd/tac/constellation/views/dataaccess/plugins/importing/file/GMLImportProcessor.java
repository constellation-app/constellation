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
import au.gov.asd.tac.constellation.graph.processing.Record;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.importing.ImportGraphFilePlugin.FILE_NAME_PARAMETER_ID;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.importing.ImportGraphFilePlugin.RETRIEVE_TRANSACTIONS_PARAMETER_ID;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
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
    public void process(final PluginParameters parameters, final Record input, final RecordStore output) throws ProcessingException {
        final RecordStore nodeRecords = new GraphRecordStore();
        final RecordStore edgeRecords = new GraphRecordStore();

        // Initialize variables
        final String filename = parameters.getParameters().get(FILE_NAME_PARAMETER_ID).getStringValue();
        final boolean retrieveTransactions = parameters.getParameters().get(RETRIEVE_TRANSACTIONS_PARAMETER_ID).getBooleanValue();
        BufferedReader in = null;
        String line;
        boolean node = false;
        boolean edge = false;

        try {
            // Open file and loop through lines
            in = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
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
                    node = false;
                    edge = false;
                } else if (node) {
                    try {
                        // Read node data
                        final String key = line.split(" ")[0].trim();
                        final String value = line.split(" ")[1].trim().replace("\"", "");
                        if (key.equals("id")) {
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

        } catch (final FileNotFoundException ex) {
            NotifyDisplayer.display(new NotifyDescriptor("Error:\n" + "File " + filename + " not found", "Import GML File", DEFAULT_OPTION, 
                    NotifyDescriptor.ERROR_MESSAGE, new Object[]{NotifyDescriptor.OK_OPTION}, NotifyDescriptor.OK_OPTION));
            LOGGER.log(Level.SEVERE, ex, () -> "File " + filename + " not found");
        } catch (final IOException ex) {
            NotifyDisplayer.display(new NotifyDescriptor("Error:\n" + "Error reading file " + filename, "Import GML File", DEFAULT_OPTION, 
                    NotifyDescriptor.ERROR_MESSAGE, new Object[]{NotifyDescriptor.OK_OPTION}, NotifyDescriptor.OK_OPTION));
            LOGGER.log(Level.SEVERE, ex, () -> "Error reading file: " + filename);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final IOException ex) {
                    NotifyDisplayer.display(new NotifyDescriptor("Error:\n" + "Error reading file " + filename, "Import GML File", DEFAULT_OPTION, 
                            NotifyDescriptor.ERROR_MESSAGE, new Object[]{NotifyDescriptor.OK_OPTION}, NotifyDescriptor.OK_OPTION));
                    LOGGER.log(Level.SEVERE, ex, () -> "Error reading file: " + filename);
                }
            }
        }

        output.add(nodeRecords);
        output.add(edgeRecords);
    }
}
