/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.webserver.impl;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.pluginframework.Plugin;
import au.gov.asd.tac.constellation.pluginframework.PluginException;
import au.gov.asd.tac.constellation.pluginframework.PluginExecution;
import au.gov.asd.tac.constellation.pluginframework.PluginInteraction;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameters;
import au.gov.asd.tac.constellation.pluginframework.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.webserver.api.EndpointException;
import au.gov.asd.tac.constellation.webserver.api.RestUtilities;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import org.openide.util.Exceptions;

/**
 *
 * @author algol
 */
public class GraphImpl {

    /**
     * Merge a dataframe into the current graph.
     *
     * @param in An InputStream to read the data from.
     *
     * @throws IOException
     */
    public static void post_set(final String graphId, final InputStream in) throws IOException {
        // We want to read a JSON document that looks like:
        //
        // {"columns":["A","B"],"data":[[1,"a"]]}
        //
        // which is what is output by pandas.to_json(..., orient="split').
        // (We ignore the index array.)
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode json = mapper.readTree(in);

        if (!json.hasNonNull("columns") || !json.get("columns").isArray()) {
            throw new EndpointException("Could not find columns object containing column names");
        }

        if (!json.hasNonNull("data") || !json.get("data").isArray()) {
            throw new EndpointException("Could not find data object containing data rows");
        }
        final ArrayNode columns = (ArrayNode) json.get("columns");
        final ArrayNode data = (ArrayNode) json.get("data");

        // Do we have one and only one row of data?
        if (data.size() != 1) {
            throw new EndpointException("Must have one row of data");
        }

        final ArrayNode row = (ArrayNode) data.get(0);

        // Do the number of column headers and the number of data elements in the row match?
        if (columns.size() != row.size()) {
            throw new EndpointException("Column names do not match data row");
        }

        setGraphAttributes(graphId, columns, row);
    }

    private static void setGraphAttributes(final String graphId, final ArrayNode columns, final ArrayNode row) {
        final Graph graph = graphId == null ? RestUtilities.getActiveGraph() : GraphNode.getGraph(graphId);

        final Plugin p = new SimpleEditPlugin("Set graph attributes from REST API") {
            @Override
            protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
                for (int i = 0; i < columns.size(); i++) {
                    final String attributeName = columns.get(i).asText();
                    int attributeId = graph.getAttribute(GraphElementType.GRAPH, attributeName);
                    if (attributeId == Graph.NOT_FOUND) {
                        attributeId = graph.addAttribute(GraphElementType.GRAPH, "string", attributeName, null, null, null);
                    }
                    final String attributeValue = row.get(i).asText();
                    graph.setStringValue(attributeId, 0, attributeValue);
                }
            }
        };

        PluginExecution pe = PluginExecution.withPlugin(p);

        try {
            pe.executeNow(graph);
        } catch (final InterruptedException | PluginException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
