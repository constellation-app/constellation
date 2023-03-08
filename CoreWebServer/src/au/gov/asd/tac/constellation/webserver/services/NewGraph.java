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
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.file.opener.GraphOpener;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.webserver.restapi.RestService;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceException;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceUtilities;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.openide.util.lookup.ServiceProvider;

/**
 * Create a new graph using the specified schema.
 * <p>
 * If a schema is not specified, the default schema is used. Returns the id,
 * name, and schema of the new graph.
 *
 * @author algol
 */
@ServiceProvider(service = RestService.class)
public class NewGraph extends RestService {

    private static final String NAME = "new_graph";
    private static final String SCHEMA_PARAMETER_ID = "schema_name";

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
    public RestServiceUtilities.HttpMethod getHttpMethod() {
        return RestServiceUtilities.HttpMethod.POST;
    }

    @Override
    public void callService(final PluginParameters parameters, final InputStream in, final OutputStream out) throws IOException {
        final String schemaParam = parameters.getStringValue(SCHEMA_PARAMETER_ID);

        String schemaName = null;
        for (final SchemaFactory schemaFactory : SchemaFactoryUtilities.getSchemaFactories().values()) {
            if (schemaFactory.isPrimarySchema() && (schemaParam == null || schemaParam.equals(schemaFactory.getName()))) {
                schemaName = schemaFactory.getName();
                break;
            }
        }

        if (schemaName == null) {
            throw new RestServiceException(HTTP_UNPROCESSABLE_ENTITY, String.format("Unknown schema %s", schemaParam));
        }

        // Creating a new graph is asynchronous; we want to hide this from the client.
        //
        final Graph existingGraph = GraphManager.getDefault().getActiveGraph();
        final String existingId = existingGraph != null ? existingGraph.getId() : null;

        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(schemaName).createSchema();
        final StoreGraph sg = new StoreGraph(schema);
        schema.newGraph(sg);
        final Graph dualGraph = new DualGraph(sg, false);

        final String graphName = SchemaFactoryUtilities.getSchemaFactory(schemaName).getLabel().trim().toLowerCase();
        GraphOpener.getDefault().openGraph(dualGraph, graphName);

        final String newId;

        try {
            newId = RestServiceUtilities.waitForGraphChange(existingId).get(10, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            throw new RestServiceException(ex);
        } catch (ExecutionException ex) {
            throw new RestServiceException(ex);
        } catch (TimeoutException ex) {
            throw new RestServiceException(ex);
        }

        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode root = mapper.createObjectNode();
        root.put("id", newId);
        root.put("name", GraphNode.getGraphNode(newId).getDisplayName());
        root.put("schema", schemaName);
        mapper.writeValue(out, root);
    }
}
