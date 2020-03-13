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
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.file.GraphDataObject;
import au.gov.asd.tac.constellation.graph.file.opener.GraphOpener;
import au.gov.asd.tac.constellation.graph.file.io.GraphJsonReader;
import au.gov.asd.tac.constellation.graph.file.io.GraphParseException;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.utilities.visual.VisualManager;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.gui.HandleIoProgress;
import au.gov.asd.tac.constellation.webserver.api.EndpointException;
import au.gov.asd.tac.constellation.webserver.api.RestUtilities;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.Semaphore;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import org.openide.util.Exceptions;

/**
 *
 * @author algol
 */
public class GraphImpl {

    private static final String IMAGE_TYPE = "png";

    /**
     * Return the graph, vertex, and transaction attributes as a map of
     * name:type items.
     * <p>
     * Names are prefixed with "graph.", "source.", or "transaction".
     */
    public static void get_attributes(final String graphId, final OutputStream out) throws IOException {
        final Graph graph = graphId == null ? RestUtilities.getActiveGraph() : GraphNode.getGraph(graphId);
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode root = mapper.createObjectNode();

        final ReadableGraph rg = graph.getReadableGraph();
        try {
            final int gCount = rg.getAttributeCount(GraphElementType.GRAPH);
            for (int i = 0; i < gCount; i++) {
                final int attrId = rg.getAttribute(GraphElementType.GRAPH, i);
                final String type = rg.getAttributeType(attrId);
                final String label = rg.getAttributeName(attrId);

                root.put(String.format("graph.%s", label), type);
            }

            final int vCount = rg.getAttributeCount(GraphElementType.VERTEX);
            for (int i = 0; i < vCount; i++) {
                final int attrId = rg.getAttribute(GraphElementType.VERTEX, i);
                final String type = rg.getAttributeType(attrId);
                final String label = rg.getAttributeName(attrId);

                root.put(String.format("source.%s", label), type);
            }

            final int tCount = rg.getAttributeCount(GraphElementType.TRANSACTION);
            for (int i = 0; i < tCount; i++) {
                final int attrId = rg.getAttribute(GraphElementType.TRANSACTION, i);
                final String type = rg.getAttributeType(attrId);
                final String label = rg.getAttributeName(attrId);

                root.put(String.format("transaction.%s", label), type);
            }
        } finally {
            rg.release();
        }

        mapper.writeValue(out, root);
    }

    /**
     * Return the graph attribute values in DataFrame format.
     *
     * @param out An OutputStream to write the response to.
     *
     * @throws IOException
     */
    public static void get_get(final String graphId, final OutputStream out) throws IOException {
        final Graph graph = graphId == null ? RestUtilities.getActiveGraph() : GraphNode.getGraph(graphId);
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

    /**
     * Return a screenshot of the graph.
     * <p>
     * This must be done on the active graph, so there's no graphId parameter.
     *
     * @param out An OutputStream to write the response to.
     *
     * @return True if an image is returned, False otherwise.
     *
     * @throws IOException
     */
    public static boolean get_image(final OutputStream out) throws IOException {
        final Graph graph = GraphManager.getDefault().getActiveGraph();

        // This is asynchronous, so we need a Semaphore.
        //
        final GraphNode graphNode = GraphNode.getGraphNode(graph);
        final VisualManager visualManager = graphNode.getVisualManager();
        final BufferedImage[] img1 = new BufferedImage[1];

        final boolean hasContent = visualManager != null;
        if (hasContent) {
            final Semaphore waiter = new Semaphore(0);
            visualManager.exportToBufferedImage(img1, waiter);
            waiter.acquireUninterruptibly();

            final boolean written = ImageIO.write(img1[0], IMAGE_TYPE, out);
        }

        return hasContent;
    }

    /**
     * Return: the id of the active graph, or null if there is no active graph;
     * if there is an active graph, the graph's name and schema (which may be
     * null).
     *
     * @param out An OutputStream to write the response to.
     *
     * @throws IOException
     */
    public static void get_schema(final OutputStream out) throws IOException {
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
        if (name != null) {
            root.put("name", name);
        }
        if (id != null) {
            root.put("schema", schemaName);
        }

        mapper.writeValue(out, root);

    }

    /**
     * Return the id, name, and schema of all open graphs.
     *
     * @param out An OutputStream to write the response to.
     *
     * @throws IOException
     */
    public static void get_schema_all(final OutputStream out) throws IOException {
        final Graph graph = GraphManager.getDefault().getActiveGraph();
        final ObjectMapper mapper = new ObjectMapper();
        final ArrayNode root = mapper.createArrayNode();
        final Map<String, Graph> graphs = GraphNode.getAllGraphs();
        graphs.entrySet().forEach(entry -> {
            final String id = entry.getKey();
            final Graph g = entry.getValue();
            final ObjectNode obj = mapper.createObjectNode();
            obj.put("id", id);
            obj.put("name", GraphNode.getGraphNode(id).getDisplayName());
            final Schema schema = graph.getSchema();
            if (schema != null) {
                obj.put("schema", schema.getFactory().getName());
            }
            root.add(obj);
        });

        mapper.writeValue(out, root);
    }

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

    /**
     * Create a new graph.
     *
     * @param schemaParam The schema of the new graph, or the default schema if
     * null
     */
    public static void post_new(final String schemaParam) {
        String schemaName = null;
        for (final SchemaFactory schemaFactory : SchemaFactoryUtilities.getSchemaFactories().values()) {
            if (schemaFactory.isPrimarySchema()) {
                if (schemaParam == null || schemaParam.equals(schemaFactory.getName())) {
                    schemaName = schemaFactory.getName();
                    break;
                }
            }
        }

        if (schemaName == null) {
            throw new EndpointException(String.format("Unknown schema %s", schemaParam));
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

        // Now we wait for the new graph to become active.
        //
        int waits = 0;
        while (true) {
            // Wait a bit to give the new graph time to become active.
            //
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }

            final Graph newGraph = GraphManager.getDefault().getActiveGraph();
            final String newId = newGraph != null ? newGraph.getId() : null;
            if ((existingId == null && newId != null) || (existingId != null && newId != null && !existingId.equals(newId))) {
                // - there was no existing graph, and the new graph is active, or
                // - there was an existing graph, and the active graph is not the existing graph.
                // - we assume the user hasn't interfered by manually switching to another graph at the same time.
                //
                break;
            }

            // We only have so much patience.
            //
            if (++waits > 10) {
                throw new EndpointException("The new graph has taken too long to become active");
            }
        }
    }

    /**
     * Open the specified graph file.
     *
     * @param filenameParam The name of the file to open.
     *
     * @throws IOException
     */
    public static void post_open(final String filenameParam) throws IOException {
        final File fnam = new File(filenameParam).getAbsoluteFile();
        String name = fnam.getName();
        if (name.toUpperCase().endsWith(GraphDataObject.FILE_EXTENSION)) {
            name = name.substring(0, name.length() - 5);
        }
        try {
            final Graph g = new GraphJsonReader().readGraphZip(fnam, new HandleIoProgress(String.format("Loading graph %s...", fnam)));
            GraphOpener.getDefault().openGraph(g, name, false);
        } catch (final GraphParseException ex) {
            throw new EndpointException(ex);
        }
    }

    /**
     * Make the specified graph the current graph.
     *
     * @param graphId
     */
    public static void put_current(final String graphId) {
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
