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
package au.gov.asd.tac.constellation.webserver.services;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.webserver.api.RestUtilities;
import au.gov.asd.tac.constellation.webserver.restapi.RestService;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.openide.util.lookup.ServiceProvider;

/**
 * Get the graph attribute values.
 *
 * @author algol
 */
@ServiceProvider(service = RestService.class)
public class GetGraphValues extends RestService {

    private static final String NAME = "get_graph_values";
    private static final String GRAPH_ID_PARAMETER_ID = "graph_id";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Get the graph attribute values.";
    }

    @Override
    public String[] getTags() {
        return new String[]{"graph"};
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<StringParameterValue> graphIdParam = StringParameterType.build(GRAPH_ID_PARAMETER_ID);
        graphIdParam.setName("Graph id");
        graphIdParam.setDescription("The id of the graph to get the values of. (Default is the active graph)");
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
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode root = mapper.createObjectNode();
        final ArrayNode columns = root.putArray("columns");
        final ArrayNode data = root.putArray("data");
        final ArrayNode row = data.addArray();
        final ReadableGraph rg = graph.getReadableGraph();
        try {
            final int gCount = rg.getAttributeCount(GraphElementType.GRAPH);
            for (int i = 0; i < gCount; i++) {
                final int attrId = rg.getAttribute(GraphElementType.GRAPH, i);
                final String type = rg.getAttributeType(attrId);
                final String label = rg.getAttributeName(attrId);
                String value = rg.getStringValue(attrId, 0);

                columns.add(String.format("%s|%s", label, type));
                RestUtilities.addData(row, type, value);
            }
        } finally {
            rg.release();
        }

        mapper.writeValue(out, root);
    }
}
