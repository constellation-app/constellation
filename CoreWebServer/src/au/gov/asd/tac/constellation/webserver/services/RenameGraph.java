/*
 * Copyright 2010-2023 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.file.GraphDataObject;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.webserver.restapi.RestService;
import static au.gov.asd.tac.constellation.webserver.restapi.RestService.HTTP_UNPROCESSABLE_ENTITY;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceException;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceUtilities;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Rename the specified graph, or the active Graph
 * <p>
 * If a graph is not specified, the active Graph is renamed. Returns the id,
 * name, and schema of the renamed graph.
 *
 * @author Auriga2
 */
@ServiceProvider(service = RestService.class)
public class RenameGraph extends RestService {

    private static final Logger LOGGER = Logger.getLogger(NewGraph.class.getName());
    private static final String NAME = "rename_graph";
    private static final String NEW_GRAPH_NAME_PARAMETER_ID = "new_graph_name";
    private static final String GRAPH_ID_PARAMETER_ID = "graph_id";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Rename a graph. If a graph is not specified, the active graph is renamed. Returns the id, previous name and the new name of the graph.";
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
        graphIdParam.setDescription("The id of the graph to rename.(If left blank, the active graph will be renamed.)");
        parameters.addParameter(graphIdParam);

        final PluginParameter<StringParameterValue> graphNameParam = StringParameterType.build(NEW_GRAPH_NAME_PARAMETER_ID);
        graphNameParam.setName("New graph name");
        graphNameParam.setDescription("The new graph name");
        graphNameParam.setRequired(true);
        parameters.addParameter(graphNameParam);

        return parameters;
    }

    @Override
    public RestServiceUtilities.HttpMethod getHttpMethod() {
        return RestServiceUtilities.HttpMethod.POST;
    }

    @Override
    public void callService(final PluginParameters parameters, final InputStream in, final OutputStream out) throws IOException {
        final String newGraphName = parameters.getStringValue(NEW_GRAPH_NAME_PARAMETER_ID);
        String graphId = parameters.getStringValue(GRAPH_ID_PARAMETER_ID);

        if (graphId == null) {
            final Graph graph = GraphManager.getDefault().getActiveGraph();
            if (graph != null && graph.getSchema() != null) {
                graphId = graph.getId();
            }
        }

        if (GraphNode.fileNameExists(newGraphName)) {
            throw new RestServiceException(HTTP_UNPROCESSABLE_ENTITY, String.format("A graph with the name %s already exists.", newGraphName));
        }

        final GraphNode graphNode = GraphNode.getGraphNode(graphId);
        if (graphNode != null) {

            try {
                SwingUtilities.invokeAndWait(() -> renameGraph(newGraphName, graphNode, out));
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new RestServiceException(ex);
            } catch (final InvocationTargetException ex) {
                throw new RestServiceException(ex);
            }
        } else {
            throw new RestServiceException(HTTP_UNPROCESSABLE_ENTITY, graphId != null ? String.format("No graph with id %s.", graphId) : "No open graphs to rename.");
        }
    }

    private void renameGraph(final String newGraphName, final GraphNode graphNode, final OutputStream out) {
        try {
            final String previousGraphName = graphNode.getDisplayName();

            graphNode.getTopComponent().setName(newGraphName);
            graphNode.getTopComponent().setDisplayName(newGraphName);
            graphNode.getTopComponent().setHtmlDisplayName(newGraphName);
            graphNode.setName(newGraphName);
            graphNode.setDisplayName(newGraphName);
            final GraphDataObject dataObject = graphNode.getDataObject();
            dataObject.rename(newGraphName);

            final ObjectMapper mapper = new ObjectMapper();
            final ObjectNode root = mapper.createObjectNode();
            root.put("id", graphNode.getGraph().getId());
            root.put("previous_name", previousGraphName);
            root.put("new_name", graphNode.getDisplayName());
            mapper.writeValue(out, root);

        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
        }
    }
}
