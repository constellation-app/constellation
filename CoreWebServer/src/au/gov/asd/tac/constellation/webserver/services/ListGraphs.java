/*
 * Copyright 2010-2024 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.webserver.restapi.RestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

/**
 * The id, name, and schema of each open graph as a JSON array of objects.
 *
 * @author algol
 */
@ServiceProvider(service = RestService.class)
public class ListGraphs extends RestService {

    private static final String NAME = "list_graphs";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "The id, name, and schema of each open graph as a JSON array of objects.";
    }

    @Override
    public String[] getTags() {
        return new String[]{"graph", "schema"};
    }

    @Override
    public void callService(final PluginParameters parameters, final InputStream in, final OutputStream out) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final ArrayNode root = mapper.createArrayNode();
        final Map<String, Graph> graphs = GraphNode.getAllGraphs();
        graphs.entrySet().forEach(entry -> {
            final String id = entry.getKey();
            final Graph graph = entry.getValue();
            final ObjectNode obj = mapper.createObjectNode();
            obj.put("id", id);
            obj.put("name", GraphNode.getGraphNode(id).getDisplayName());
            final Schema schema = graph.getSchema();
            obj.put("schema", schema != null ? schema.getFactory().getName() : null);
            root.add(obj);
        });

        mapper.writeValue(out, root);
    }
}
