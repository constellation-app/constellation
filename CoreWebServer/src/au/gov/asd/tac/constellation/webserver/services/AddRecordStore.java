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
package au.gov.asd.tac.constellation.webserver.services;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.webserver.api.RestUtilities;
import au.gov.asd.tac.constellation.webserver.restapi.RestService;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceException;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceUtilities;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.lookup.ServiceProvider;

/**
 * Add a RecordStore to a graph.
 *
 * @author algol
 */
@ServiceProvider(service = RestService.class)
public class AddRecordStore extends RestService {

    private static final String NAME = "add_recordstore";
    private static final String GRAPH_ID_PARAMETER_ID = "graph_id";
    private static final String COMPLETE_PARAMETER_ID = "complete_with_schema";
    private static final String ARRANGE_PARAMETER_ID = "arrange";
    private static final String RESET_PARAMETER_ID = "reset_view";
    private static final String DATA_PARAMETER_ID = "data";

    private static final String API_SOURCE = "REST API";
    private static final String TX_SOURCE = GraphRecordStoreUtilities.TRANSACTION + AnalyticConcept.TransactionAttribute.SOURCE;

    private static final String COLUMNS = "columns";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Add a RecordStore to a graph.";
    }

    @Override
    public String[] getTags() {
        return new String[]{"recordstore"};
    }

    @Override
    public RestServiceUtilities.HttpMethod getHttpMethod() {
        return RestServiceUtilities.HttpMethod.POST;
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<StringParameterValue> graphidParam = StringParameterType.build(GRAPH_ID_PARAMETER_ID);
        graphidParam.setName("Graph id");
        graphidParam.setDescription("The id of the graph to add the recordstore to. (Default is the active graph)");
        parameters.addParameter(graphidParam);

        final PluginParameter<BooleanParameterValue> completeParam = BooleanParameterType.build(COMPLETE_PARAMETER_ID);
        completeParam.setName("Complete with schema");
        completeParam.setDescription("If true (the default), perform a schema completion after the RecordStore is added.");
        completeParam.setBooleanValue(true);
        parameters.addParameter(completeParam);

        final PluginParameter<StringParameterValue> arrangeParam = StringParameterType.build(ARRANGE_PARAMETER_ID);
        arrangeParam.setName("Arrange");
        arrangeParam.setDescription("If not specifed (the default), perform a basic arrange of the graph after the RecordStore is added. If specified, the named plugin is run, or no plugin is run if the name is '' or 'None'.");
        parameters.addParameter(arrangeParam);

        final PluginParameter<BooleanParameterValue> resetParam = BooleanParameterType.build(RESET_PARAMETER_ID);
        resetParam.setName("Reset view");
        resetParam.setDescription("If true (the default), run the ResetView plugin after adding, completing, and arranging.");
        resetParam.setBooleanValue(true);
        parameters.addParameter(resetParam);

        final PluginParameter<StringParameterValue> dataParam = StringParameterType.build(DATA_PARAMETER_ID);
        dataParam.setName("Data (body)");
        dataParam.setDescription("A JSON representation of the RecordStore data, in the form {\"columns\": [\"COL1\",\"COL2\",\"COL3\"], \"data\": [[r1c1, r1c2, r1c3],[r2c1,r2c2,r2c3]]. This is the same as the output of pandas.DataFrame.to_json(orient='split', date_format='iso').");
        dataParam.setRequestBodyExampleJson("#/components/examples/addRecordStoreExample");
        dataParam.setRequired(true);
        parameters.addParameter(dataParam);

        return parameters;
    }

    @Override
    public void callService(final PluginParameters parameters, final InputStream in, final OutputStream out) throws IOException {
        final String graphId = parameters.getStringValue(GRAPH_ID_PARAMETER_ID);
        final boolean completeWithSchema = parameters.getBooleanValue(COMPLETE_PARAMETER_ID);
        final String arrange = parameters.getStringValue(ARRANGE_PARAMETER_ID);
        final boolean resetView = parameters.getBooleanValue(RESET_PARAMETER_ID);

        final RecordStore rs = new GraphRecordStore();
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode json = mapper.readTree(in);

        final Graph graph = graphId == null ? RestUtilities.getActiveGraph() : GraphNode.getGraph(graphId);
        if (graph == null) {
            throw new RestServiceException(HTTP_UNPROCESSABLE_ENTITY, "No graph with id " + graphId);
        }
        // We want to read a JSON document that looks like:
        //
        // {"columns":["A","B"],"data":[[1,"a"],[2,"b"],[3,"c"]]}
        //
        // which is what is output by pandas.to_json(..., orient="split').
        // (We ignore the index array.)
        if (!json.hasNonNull(COLUMNS) || !json.get(COLUMNS).isArray()) {
            throw new RestServiceException("Could not find columns object containing column names");
        }

        if (!json.hasNonNull("data") || !json.get("data").isArray()) {
            throw new RestServiceException("Could not find data object containing data rows");
        }

        final ArrayNode columns = (ArrayNode) json.get(COLUMNS);
        final String[] headers = new String[columns.size()];
        for (int i = 0; i < headers.length; i++) {
            headers[i] = columns.get(i).asText();
        }

        final ArrayNode data = (ArrayNode) json.get("data");
        for (final Iterator<JsonNode> i = data.elements(); i.hasNext();) {
            final ArrayNode jrow = (ArrayNode) i.next();
            rs.add();
            boolean txFound = false;
            boolean txSourceFound = false;
            for (int ix = 0; ix < headers.length; ix++) {
                final String h = headers[ix];
                final JsonNode jn = jrow.get(ix);
                if (!jn.isNull()) {
                    if (jn.getNodeType() == JsonNodeType.ARRAY) {
                        rs.set(h, RestServiceUtilities.toList((ArrayNode) jn));
                    } else {
                        rs.set(h, jn.asText());
                    }
                }
                txFound |= h.startsWith(GraphRecordStoreUtilities.TRANSACTION);
                txSourceFound |= TX_SOURCE.equals(h);
            }

            if (txFound && !txSourceFound) {
                rs.set(TX_SOURCE, API_SOURCE);
            }
        }

        addToGraph(graph, rs, completeWithSchema, arrange, resetView);
    }

    private static void addToGraph(final Graph graph, final RecordStore recordStore, final boolean completeWithSchema, final String arrange, final boolean resetView) {
        final Plugin p = new ImportFromRestApiPlugin(recordStore, completeWithSchema, arrange);

        PluginExecutor pe = PluginExecutor.startWith(p);

        if (resetView) {
            pe = pe.followedBy(InteractiveGraphPluginRegistry.RESET_VIEW);
        }

        try {
            pe.executeNow(graph);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RestServiceException(ex);
        } catch (final PluginException ex) {
            throw new RestServiceException(ex);
        }
    }

    @PluginInfo(pluginType = PluginType.IMPORT, tags = {PluginTags.IMPORT})
    private static class ImportFromRestApiPlugin extends SimpleEditPlugin {
        
        private static final Logger LOGGER = Logger.getLogger(ImportFromRestApiPlugin.class.getName());

        private final RecordStore recordStore;
        private final boolean completeWithSchema;
        private final String arrange;

        public ImportFromRestApiPlugin(final RecordStore recordStore, final boolean completeWithSchema, final String arrange) {
            this.recordStore = recordStore;
            this.completeWithSchema = completeWithSchema;
            this.arrange = arrange;
        }

        @Override
        public String getName() {
            return "Import from REST API";
        }

        @Override
        protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            GraphRecordStoreUtilities.addRecordStoreToGraph(graph, recordStore, false, completeWithSchema, null);

            // Do the optional arrangement inside this anonymous "addRecordStoreToGraph" plugin.
            // This way, any extra nodes are added and arranged in one go.
            // If the arrangement is done separately, not only does the "add + arrange" become two steps,
            // but if enough extra vertices are drawn at (0, 0, 0), some graphics drivers will crash.
            // It is still possible to do this (by manually setting x,y,z to 0,0,0 and specifying no arrangement),
            // but then it becomes the malicious user's fault.
            //
            try {
                if (arrange == null) {
                    PluginExecutor
                            .startWith(ArrangementPluginRegistry.GRID_COMPOSITE)
                            .followedBy(ArrangementPluginRegistry.PENDANTS)
                            .followedBy(ArrangementPluginRegistry.UNCOLLIDE)
                            .executeNow(graph);
                } else if (arrange.isEmpty() || "None".equalsIgnoreCase(arrange)) {
                    // Don't do anything.
                } else {
                    PluginExecution.withPlugin(arrange).executeNow(graph);
                }
            } catch (final PluginException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }
    }
}
