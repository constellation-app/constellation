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

import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.utilities.gui.HandleIoProgress;
import au.gov.asd.tac.constellation.webserver.api.EndpointException;
import au.gov.asd.tac.constellation.webserver.api.RestUtilities;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import org.openide.util.Exceptions;

/**
 *
 * @author algol
 */
public class RecordStoreImpl {

    private static final String API_SOURCE = "REST API";
    private static final String TX_SOURCE = GraphRecordStoreUtilities.TRANSACTION + AnalyticConcept.TransactionAttribute.SOURCE;

    /**
     * Get (parts of) the currently active graph as a RecordStore.
     *
     * @param vx Only get vertices.
     * @param tx Only get transactions.
     * @param selected Only get selected elements.
     * @param attrs A list of attributes to get; if null, get all attributes.
     *
     * @param out An OutputStream to write the response to.
     *
     * @throws IOException
     */
    public static void get_get(final String graphId, final boolean vx, final boolean tx, final boolean selected, final Set<String> attrs, final OutputStream out) throws IOException {
        // Build the JSON in a form suitable for passing to pandas.DataFrame.from_items().
        // This includes the datatypes with the names, so the client can do transforms
        // where required (for example, converting strings to timestamps).
        final HandleIoProgress ioph = new HandleIoProgress("External script: get RecordStore");
        ioph.start();
        ioph.progress("Building RecordStore...");
        final GraphRecordStore recordStore;
        final Graph graph = graphId == null ? RestUtilities.getActiveGraph() : GraphNode.getGraph(graphId);
        final ReadableGraph rg = graph.getReadableGraph();
        try {
            if ((vx && tx) || !(vx || tx)) {
                // We're getting the entire graph. We don't want all
                // of the vertices: since all of the vertices with
                // transactions are already included, we only want
                // the rest of the vertices, ie the singletons.
                recordStore = GraphRecordStoreUtilities.getAll(rg, true, selected, false);
            } else if (vx) {
                recordStore = GraphRecordStoreUtilities.getVertices(rg, false, selected, false);
            } else {
                recordStore = GraphRecordStoreUtilities.getTransactions(rg, selected, false);
            }
        } finally {
            rg.release();
        }

        // Create a mapping from "attrname" to "attrname<type>" for
        // all of the RecordStore attributes.
        final Map<String, String> attrToTypedAttr = new HashMap<>();
        for (final String kt : recordStore.keysWithType()) {
            final int ix = kt.lastIndexOf('<');
            final String key = kt.substring(0, ix);
            attrToTypedAttr.put(key, kt);
        }

        if (!attrs.isEmpty() && recordStore.size() > 0) {
            // Check that all of the user-specified attributes exist.
            final StringJoiner buf = new StringJoiner(",");
            for (final String key : attrs) {
                if (!attrToTypedAttr.containsKey(key)) {
                    buf.add(key);
                }
            }

            if (buf.length() != 0) {
                throw new EndpointException("The following attributes do not exist in the record store: " + buf.toString());
            }
        } else {
            // The user didn't specify any attributes, so use all of
            // the RecordStore keys.
            attrs.addAll(attrToTypedAttr.keySet());
        }

        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
        mapper.configure(SerializationFeature.CLOSE_CLOSEABLE, false);

        final ObjectNode root = mapper.createObjectNode();

        // We want to build a JSON document that looks like:
        //
        // {"columns":["A","B"],"data":[[1,"a"],[2,"b"],[3,"c"]]}
        //
        // which can be read by pandas.read_json(..., orient="split").
        // (It appears that the index parameter is not required.)
        final ArrayNode columns = root.putArray("columns");
        final ArrayNode data = root.putArray("data");

        if (recordStore.size() > 0) {
            ioph.progress("Building DataFrame...");
            for (final String attr : attrs) {
                final String kt = attrToTypedAttr.get(attr);
                columns.add(keyedName(kt));
            }

            recordStore.reset();
            while (recordStore.next()) {
                final ArrayNode row = data.addArray();
                for (final String attr : attrs) {
                    final String kt = attrToTypedAttr.get(attr);
                    final String type = kt.substring(kt.lastIndexOf('<') + 1, kt.length() - 1);
                    final String value = recordStore.get(kt);

                    RestUtilities.addData(row, type, value);
                }
            }
        }

        mapper.writeValue(out, root);

        ioph.finish();
    }

    /**
     * Add data to a new store, and add the store to the graph.
     * <p>
     * If any transaction does not specify a source, add our own.
     *
     * @param completeWithSchema If true, update the graph with the schema.
     * @param arrange If true, perform a basic arrangement.
     * @param resetView If true, reset the graph view.
     * @param in An InputStream to read the data from.
     *
     * @throws IOException
     */
    public static void post_add(final String graphId, final boolean completeWithSchema, final String arrange, final boolean resetView, final InputStream in) throws IOException {
        // Add data to a new store, and add the store to the graph.
        // If any transaction does not specify a source, add our own.

        final RecordStore rs = new GraphRecordStore();
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode json = mapper.readTree(in);

        // We want to read a JSON document that looks like:
        //
        // {"columns":["A","B"],"data":[[1,"a"],[2,"b"],[3,"c"]]}
        //
        // which is what is output by pandas.to_json(..., orient="split').
        // (We ignore the index array.)
        if (!json.hasNonNull("columns") || !json.get("columns").isArray()) {
            throw new EndpointException("Could not find columns object containing column names");
        }

        if (!json.hasNonNull("data") || !json.get("data").isArray()) {
            throw new EndpointException("Could not find data object containing data rows");
        }

        final ArrayNode columns = (ArrayNode) json.get("columns");
        final String[] headers = new String[columns.size()];
        for (int i = 0; i < headers.length; i++) {
            headers[i] = columns.get(i).asText();
        }

        final ArrayNode data = (ArrayNode) json.get("data");
        for (final Iterator<JsonNode> i = data.elements(); i.hasNext();) {
            final ArrayNode jrow = (ArrayNode) i.next();
            rs.add();
            boolean txFound = false;
            boolean txSourceFound = false;
            for (int ix = 0; ix < headers.length; ix++) {
                final String h = headers[ix];
                final JsonNode jn = jrow.get(ix);
                if (!jn.isNull()) {
                    rs.set(h, jn.asText());
                }
                txFound |= h.startsWith(GraphRecordStoreUtilities.TRANSACTION);
                txSourceFound |= TX_SOURCE.equals(h);
            }

            if (txFound && !txSourceFound) {
                rs.set(TX_SOURCE, API_SOURCE);
            }
        }

        addToGraph(graphId, rs, completeWithSchema, arrange, resetView);
    }

    /**
     * Convert a "key<type>" to "key|type" to make things slightly easier for
     * the attrWithType.
     *
     * @param attrWithType
     *
     * @return
     */
    private static String keyedName(final String attrWithType) {
        final int ix = attrWithType.lastIndexOf("<");

        return attrWithType.substring(0, ix) + "|" + attrWithType.substring(ix + 1, attrWithType.length() - 1);
    }

    private static void addToGraph(final String graphId, final RecordStore recordStore, final boolean completeWithSchema, final String arrange, final boolean resetView) {
        final Graph graph = graphId == null ? RestUtilities.getActiveGraph() : GraphNode.getGraph(graphId);

        final Plugin p = new SimpleEditPlugin("Import from REST API") {
            @Override
            protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
                GraphRecordStoreUtilities.addRecordStoreToGraph(graph, recordStore, false, completeWithSchema, null);

                // Do the optional arrangement inside this anonymous "addRecordStoreToGraph" plugin.
                // This way, any extra nodes are added and arranged in one go.
                // If the arrangement is done separately, not only does the "add + arrange" become two steps,
                // but if enough extra vertices are drawn at (0, 0, 0), some graphics drivers will crash.
                // It is still possible to do this (by manually setting x,y,z to 0,0,0 and specifying no arrangement),
                // but then it becomes the malicious user's fault.
                //
                try {
                    if (arrange == null) {
                        PluginExecutor
                                .startWith(ArrangementPluginRegistry.GRID_COMPOSITE)
                                .followedBy(ArrangementPluginRegistry.PENDANTS)
                                .followedBy(ArrangementPluginRegistry.UNCOLLIDE)
                                .executeNow(graph);
                    } else if (arrange.isEmpty() || arrange.equalsIgnoreCase("None")) {
                        // Don't do anything.
                    } else {
                        PluginExecution.withPlugin(arrange).executeNow(graph);
                    }
                } catch (final PluginException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };

        PluginExecutor pe = PluginExecutor.startWith(p);

        if (resetView) {
            pe = pe.followedBy(InteractiveGraphPluginRegistry.RESET_VIEW);
        }

        try {
            pe.executeNow(graph);
        } catch (final InterruptedException | PluginException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
