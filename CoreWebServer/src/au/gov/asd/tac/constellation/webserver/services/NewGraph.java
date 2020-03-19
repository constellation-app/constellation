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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.file.opener.GraphOpener;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameter;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameters;
import au.gov.asd.tac.constellation.pluginframework.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.pluginframework.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceException;
import au.gov.asd.tac.constellation.webserver.restapi.RestService;
import au.gov.asd.tac.constellation.webserver.restapi.ServiceUtilities;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author algol
 */
@ServiceProvider(service=RestService.class)
public class NewGraph extends RestService {
    private static final String NAME = "new_graph";
    private static final String SCHEMA_PARAMETER_ID = ServiceUtilities.buildId(NAME, "schema_name");

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Create a new graph using the specified schema. If a schema is not specified, the default schema is used. Returns the id, name, and schema of the new graph.";
    }

    @Override
    public String[] getTags() {
        return new String[]{"graph", "schema"};
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<StringParameterValue> schemaParam = StringParameterType.build(SCHEMA_PARAMETER_ID);
        schemaParam.setName("Graph schema");
        schemaParam.setDescription("The schema used to create the new graph.");
        parameters.addParameter(schemaParam);

        return parameters;
    }

    @Override
    public ServiceUtilities.HttpMethod getHttpMethod() {
        return ServiceUtilities.HttpMethod.POST;
    }

    @Override
    public void callService(final PluginParameters parameters, final InputStream in, final OutputStream out) throws IOException {
        final String schemaParam = parameters.getStringValue(SCHEMA_PARAMETER_ID);

        String schemaName = null;
        for(final SchemaFactory schemaFactory : SchemaFactoryUtilities.getSchemaFactories().values()) {
            if(schemaFactory.isPrimarySchema()) {
                if(schemaParam == null || schemaParam.equals(schemaFactory.getName())) {
                    schemaName = schemaFactory.getName();
                    break;
                }
            }
        }

        if(schemaName == null) {
            throw new RestServiceException(String.format("Unknown schema %s", schemaParam));
        }

        // Creating a new graph is asynchronous; we want to hide this from the client.
        //
        final Graph existingGraph = GraphManager.getDefault().getActiveGraph();
        final String existingId = existingGraph != null ? existingGraph.getId() : null;

        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(schemaName).createSchema();
        final StoreGraph sg = new StoreGraph(schema);
        schema.newGraph(sg);
        final Graph dualGraph = new DualGraph(sg, false);

        final String graphName = SchemaFactoryUtilities.getSchemaFactory(schemaName).getLabel().replace(" ", "").toLowerCase();
        GraphOpener.getDefault().openGraph(dualGraph, graphName);

        final String newId = ServiceUtilities.waitForGraphChange(existingId);

//        // Now we wait for the new graph to become active.
//        //
//        int waits = 0;
//        while(true) {
//            // Wait a bit to give the new graph time to become active.
//            //
//            try {
//                Thread.sleep(1000);
//            } catch(InterruptedException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//
//            final Graph newGraph = GraphManager.getDefault().getActiveGraph();
//            final String newId = newGraph != null ? newGraph.getId() : null;
//            if((existingId == null && newId != null) || (existingId != null && newId != null && !existingId.equals(newId))) {
//                // - there was no existing graph, and the new graph is active, or
//                // - there was an existing graph, and the active graph is not the existing graph.
//                // - we assume the user hasn't interfered by manually switching to another graph at the same time.
//                //
                final ObjectMapper mapper = new ObjectMapper();
                final ObjectNode root = mapper.createObjectNode();
                root.put("id", newId);
                root.put("name", GraphNode.getGraphNode(newId).getDisplayName());
                root.put("schema", schemaName);
                mapper.writeValue(out, root);
//
//                break;
//            }
//
//            // We only have so much patience.
//            //
//            if (++waits > 10) {
//                throw new EndpointException("The new graph has taken too long to become active");
//            }
//        }
    }
}
