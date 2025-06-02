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

import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.webserver.restapi.RestService;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceException;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceUtilities;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Make the graph with the specified id the active graph.
 *
 * @author algol
 */
@ServiceProvider(service = RestService.class)
public class SetGraph extends RestService {

    private static final String NAME = "set_graph";
    private static final String GRAPH_ID_PARAMETER_ID = "graph_id";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Make the graph with the specified id the active graph.";
    }

    @Override
    public String[] getTags() {
        return new String[]{"graph"};
    }

    @Override
    public RestServiceUtilities.HttpMethod getHttpMethod() {
        return RestServiceUtilities.HttpMethod.PUT;
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<StringParameterValue> graphIdParam = StringParameterType.build(GRAPH_ID_PARAMETER_ID);
        graphIdParam.setName("Graph id");
        graphIdParam.setDescription("The id of a graph to make active.");
        graphIdParam.setRequired(true);
        parameters.addParameter(graphIdParam);

        return parameters;
    }

    @Override
    public void callService(final PluginParameters parameters, final InputStream in, final OutputStream out) throws IOException {
        final String graphId = parameters.getStringValue(GRAPH_ID_PARAMETER_ID);

        final GraphNode graphNode = GraphNode.getGraphNode(graphId);
        if (graphNode != null) {
            try {
                SwingUtilities.invokeAndWait(() -> graphNode.getTopComponent().requestActive());
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new RestServiceException(ex);
            } catch (final InvocationTargetException ex) {
                throw new RestServiceException(ex);
            }
        } else {
            throw new RestServiceException(HTTP_UNPROCESSABLE_ENTITY, "No graph with id " + graphId);
        }
    }
}
