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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.importing;

import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType.FileParameterValue;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginCoreType;
import au.gov.asd.tac.constellation.views.dataaccess.templates.RecordStoreQueryPlugin;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.stage.FileChooser;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * Read graph data from a Pajek .net file and add it to a graph.
 *
 * @author canis_majoris
 */
@ServiceProviders({
    @ServiceProvider(service = DataAccessPlugin.class),
    @ServiceProvider(service = Plugin.class)})
@PluginInfo(pluginType = PluginType.IMPORT, tags = {"IMPORT"})
@Messages("ImportFromPajekPlugin=Import From Pajek File")
public class ImportFromPajekPlugin extends RecordStoreQueryPlugin implements DataAccessPlugin {

    private static final Logger LOGGER = Logger.getLogger(ImportFromPajekPlugin.class.getName());

    // plugin parameters
    public static final String FILE_PARAMETER_ID = PluginParameter.buildId(ImportFromPajekPlugin.class, "file");
    public static final String EDGE_PARAMETER_ID = PluginParameter.buildId(ImportFromPajekPlugin.class, "edge");
    public static final String VERTEX_HEADER = "*V";
    public static final String EDGE_HEADER = "*E";

    @Override
    public String getType() {
        return DataAccessPluginCoreType.IMPORT;
    }

    @Override
    public int getPosition() {
        return 100;
    }

    @Override
    public String getDescription() {
        return "Select a Pajek File and Import it into your graph";
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters params = new PluginParameters();

        // The Pajek file to read from
        final PluginParameter<FileParameterValue> file = FileParameterType.build(FILE_PARAMETER_ID);
        FileParameterType.setFileFilters(file, new FileChooser.ExtensionFilter("PAJEK files", "*.net"));
        FileParameterType.setKind(file, FileParameterType.FileParameterKind.OPEN);
        file.setName("Pajek File");
        file.setDescription("File to extract graph from");
        params.addParameter(file);

        // A boolean option for whether to grab transactions
        final PluginParameter<BooleanParameterValue> edge = BooleanParameterType.build(EDGE_PARAMETER_ID);
        edge.setName("Retrieve Transactions");
        edge.setDescription("Retrieve Transactions from Pajek File");
        edge.setBooleanValue(true);
        params.addParameter(edge);

        return params;
    }

    @Override
    protected RecordStore query(final RecordStore query, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final RecordStore result = new GraphRecordStore();

        interaction.setProgress(0, 0, "Importing...", true);
        // Initialize variables
        final String filename = parameters.getParameters().get(FILE_PARAMETER_ID).getStringValue();
        final boolean getEdges = parameters.getParameters().get(EDGE_PARAMETER_ID).getBooleanValue();
        BufferedReader in = null;
        String line;
        boolean processNodes = false;
        boolean processEdges = false;

        try {
            // Open file and loop through lines
            in = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
            while ((line = in.readLine()) != null) {
                if (line.startsWith(VERTEX_HEADER)) {
                    processNodes = true;
                } else if (line.startsWith(EDGE_HEADER)) {
                    processNodes = false;
                    processEdges = true;
                } else {
                    if (processNodes) {
                        try {
                            // Read node data
                            final String nodeId = line.split("\"")[0].trim();
                            final String nodeLabel = line.split("\"")[1].trim();
                            result.add();
                            result.set(GraphRecordStoreUtilities.SOURCE + GraphRecordStoreUtilities.ID, nodeId);
                            result.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, nodeLabel);
                            result.set(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.TYPE, "Unknown");
                            result.set(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.SOURCE, filename);
                        } catch (final ArrayIndexOutOfBoundsException ex) {
                        }
                    } else if (processEdges && getEdges) {
                        try {
                            // Read edge data
                            final String[] fields = line.split("\\s+");
                            final String srcId = fields[1];
                            final String dstId = fields[2];
                            final String weight = fields[3];

                            result.add();
                            result.set(GraphRecordStoreUtilities.SOURCE + GraphRecordStoreUtilities.ID, srcId);
                            result.set(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.SOURCE, filename);
                            result.set(GraphRecordStoreUtilities.DESTINATION + GraphRecordStoreUtilities.ID, dstId);
                            result.set(GraphRecordStoreUtilities.DESTINATION + AnalyticConcept.VertexAttribute.SOURCE, filename);
                            result.set(GraphRecordStoreUtilities.TRANSACTION + AnalyticConcept.TransactionAttribute.COUNT, weight);
                            result.set(GraphRecordStoreUtilities.TRANSACTION + AnalyticConcept.TransactionAttribute.SOURCE, filename);
                        } catch (final ArrayIndexOutOfBoundsException ex) {
                        }
                    }
                }
            }
            interaction.setProgress(1, 0, "Completed successfully - added " + result.size() + " entities.", true);
        } catch (final FileNotFoundException ex) {
            interaction.notify(PluginNotificationLevel.ERROR, "File " + filename + " not found");
            LOGGER.log(Level.SEVERE, ex, () -> "File " + filename + " not found");
        } catch (final IOException ex) {
            interaction.notify(PluginNotificationLevel.ERROR, "Error reading file: " + filename);
            LOGGER.log(Level.SEVERE, ex, () -> "Error reading file: " + filename);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final IOException ex) {
                    interaction.notify(PluginNotificationLevel.ERROR, "Error reading file: " + filename);
                    LOGGER.log(Level.SEVERE, ex, () -> "Error reading file: " + filename);
                }
            }
        }

        return result;
    }
}
