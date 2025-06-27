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
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.webserver.restapi.RestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.openide.util.lookup.ServiceProvider;

/**
 * The id, name, and schema of the active graph as a JSON object.
 *
 * @author algol
 */
@ServiceProvider(service = RestService.class)
public class GetGraph extends RestService {

    private static final String NAME = "get_graph";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "The id, name, and schema of the active graph as a JSON object.";
    }

    @Override
    public String[] getTags() {
        return new String[]{"graph", "schema"};
    }

    @Override
    public void callService(final PluginParameters parameters, final InputStream in, final OutputStream out) throws IOException {
        final Graph graph = GraphManager.getDefault().getActiveGraph();
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode root = mapper.createObjectNode();
        String name = null;
        String schemaName = null;
        final String id = graph != null ? graph.getId() : null;
        if (graph != null) {
            name = GraphNode.getGraphNode(id).getDisplayName();
            final Schema schema = graph.getSchema();
            if (schema != null) {
                schemaName = schema.getFactory().getName();
            }
        }

        root.put("id", id);
        root.put("name", name);
        root.put("schema", schemaName);

        mapper.writeValue(out, root);
    }
}
