/*
 * Copyright 2010-2020 Australian Signals Directorate
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
 */package au.gov.asd.tac.constellation.webserver.services;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.webserver.api.RestUtilities;
import au.gov.asd.tac.constellation.webserver.restapi.RestService;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceException;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceUtilities;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.openide.util.lookup.ServiceProvider;

/**
 * Set the graph attribute values.
 *
 * @author algol
 */
@ServiceProvider(service = RestService.class)
public class SetGraphValues extends RestService {

    private static final String NAME = "set_graph_values";
    private static final String GRAPH_ID_PARAMETER_ID = "graph_id";

    private static final String COLUMNS = "columns";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Set the graph attribute values.";
    }

    @Override
    public String[] getTags() {
        return new String[]{"graph"};
    }

    @Override
    public RestServiceUtilities.HttpMethod getHttpMethod() {
        return RestServiceUtilities.HttpMethod.POST;
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<StringParameterValue> graphIdParam = StringParameterType.build(GRAPH_ID_PARAMETER_ID);
        graphIdParam.setName("Graph id");
        graphIdParam.setDescription("The id of the graph to get the values of.");
        parameters.addParameter(graphIdParam);

        return parameters;
    }

    @Override
    public void callService(final PluginParameters parameters, InputStream in, OutputStream out) throws IOException {
        final String graphId = parameters.getStringValue(GRAPH_ID_PARAMETER_ID);

        final Graph graph = graphId == null ? RestUtilities.getActiveGraph() : GraphNode.getGraph(graphId);
        if (graph == null) {
            throw new RestServiceException(HTTP_UNPROCESSABLE_ENTITY, "No graph with id " + graphId);
        }
        // We want to read a JSON document that looks like:
        //
        // {"columns":["A","B"],"data":[[1,"a"]]}
        //
        // which is what is output by pandas.to_json(..., orient="split').
        // (We ignore the index array.)
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode json = mapper.readTree(in);

        if (!json.hasNonNull(COLUMNS) || !json.get(COLUMNS).isArray()) {
            throw new RestServiceException(HTTP_UNPROCESSABLE_ENTITY, "Could not find columns object containing column names");
        }

        if (!json.hasNonNull("data") || !json.get("data").isArray()) {
            throw new RestServiceException(HTTP_UNPROCESSABLE_ENTITY, "Could not find data object containing data rows");
        }
        final ArrayNode columns = (ArrayNode) json.get(COLUMNS);
        final ArrayNode data = (ArrayNode) json.get("data");

        // Do we have one and only one row of data?
        if (data.size() != 1) {
            throw new RestServiceException("Must have one row of data");
        }

        final ArrayNode row = (ArrayNode) data.get(0);

        // Do the number of column headers and the number of data elements in the row match?
        if (columns.size() != row.size()) {
            throw new RestServiceException("Column names do not match data row");
        }

        setGraphAttributes(graph, columns, row);
    }

    private static void setGraphAttributes(final Graph graph, final ArrayNode columns, final ArrayNode row) {
        final Plugin p = new SimpleEditPlugin("Set graph attributes from REST API") {
            @Override
            protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
                for (int i = 0; i < columns.size(); i++) {
                    final String attributeName = columns.get(i).asText();
                    int attributeId = graph.getAttribute(GraphElementType.GRAPH, attributeName);
                    if (attributeId == Graph.NOT_FOUND) {
                        attributeId = graph.addAttribute(GraphElementType.GRAPH, StringAttributeDescription.ATTRIBUTE_NAME, attributeName, null, null, null);
                    }
                    final String attributeValue = row.get(i).asText();
                    graph.setStringValue(attributeId, 0, attributeValue);
                }
            }
        };

        PluginExecution pe = PluginExecution.withPlugin(p);

        try {
            pe.executeNow(graph);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RestServiceException(ex);
        } catch (final PluginException ex) {
            throw new RestServiceException(ex);
        }
    }
}
