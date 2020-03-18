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
 */
package au.gov.asd.tac.constellation.webserver.services;

import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameter;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameters;
import au.gov.asd.tac.constellation.pluginframework.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.pluginframework.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.webserver.api.EndpointException;
import au.gov.asd.tac.constellation.webserver.restapi.RestService;
import au.gov.asd.tac.constellation.webserver.restapi.ServiceUtilities;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author algol
 */
@ServiceProvider(service=RestService.class)
public class SetGraph extends RestService {
    private static final String NAME = "set_graph";
    private static final String GRAPHID_PARAMETER_ID = ServiceUtilities.buildId(NAME, "graph_id");

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
    public ServiceUtilities.HttpMethod getHttpMethod() {
        return ServiceUtilities.HttpMethod.PUT;
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<StringParameterValue> methodParam = StringParameterType.build(GRAPHID_PARAMETER_ID);
        methodParam.setName("Graph id");
        methodParam.setDescription("The id of a graph to make active.");
        parameters.addParameter(methodParam);

        return parameters;
    }

    @Override
    public void callService(final PluginParameters parameters, InputStream in, OutputStream out) throws IOException {
        final String graphId = parameters.getStringValue(GRAPHID_PARAMETER_ID);
        
        final GraphNode graphNode = GraphNode.getGraphNode(graphId);
        if (graphNode != null) {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    graphNode.getTopComponent().requestActive();
                });
            } catch (final InterruptedException | InvocationTargetException ex) {
                throw new EndpointException(ex);
            }
        } else {
            throw new EndpointException(String.format("No graph with id '%s'", graphId));
        }
    }
}
