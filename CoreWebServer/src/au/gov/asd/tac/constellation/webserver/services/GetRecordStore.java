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
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.utilities.gui.HandleIoProgress;
import au.gov.asd.tac.constellation.utilities.gui.IoProgress;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.webserver.api.RestUtilities;
import au.gov.asd.tac.constellation.webserver.restapi.RestService;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import org.openide.util.lookup.ServiceProvider;

/**
 * Get (parts of) the specified graph (default is the active graph) as a
 * RecordStore.
 *
 * @author algol
 */
@ServiceProvider(service = RestService.class)
public class GetRecordStore extends RestService {

    private static final String NAME = "get_recordstore";
    private static final String GRAPH_ID_PARAMETER_ID = "graph_id";
    private static final String SELECTED_PARAMETER_ID = "selected";
    private static final String VX_PARAMETER_ID = "vx";
    private static final String TX_PARAMETER_ID = "tx";
    private static final String ATTRS_PARAMETER_ID = "attrs";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Get (parts of) the specified graph (default is the active graph) as a RecordStore.";
    }

    @Override
    public String[] getTags() {
        return new String[]{"recordstore"};
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<StringParameterValue> graphIdParam = StringParameterType.build(GRAPH_ID_PARAMETER_ID);
        graphIdParam.setName("Graph id");
        graphIdParam.setDescription("The id of a graph to get data from. (Default is the active graph)");
        parameters.addParameter(graphIdParam);

        final PluginParameter<BooleanParameterValue> selectedParam = BooleanParameterType.build(SELECTED_PARAMETER_ID);
        selectedParam.setName("Selected elements only");
        selectedParam.setDescription("If false (the default), return all elements, else return only the selected elements.");
        selectedParam.setObjectValue(false);
        parameters.addParameter(selectedParam);

        final PluginParameter<BooleanParameterValue> vxParam = BooleanParameterType.build(VX_PARAMETER_ID);
        vxParam.setName("Vertices only");
        vxParam.setDescription("If true, return vertices only (default false).");
        vxParam.setObjectValue(false);
        parameters.addParameter(vxParam);

        final PluginParameter<BooleanParameterValue> txParam = BooleanParameterType.build(TX_PARAMETER_ID);
        txParam.setName("Transactions only");
        txParam.setDescription("If true, return transactions only (default false).");
        txParam.setObjectValue(false);
        parameters.addParameter(txParam);

        final PluginParameter<StringParameterValue> attrsParam = StringParameterType.build(ATTRS_PARAMETER_ID);
        attrsParam.setName("Attribute list");
        attrsParam.setDescription("Include only these comma-separated attributes in the recordstore. Use this for much greater efficiency. (optional)");
        parameters.addParameter(attrsParam);

        return parameters;
    }

    @Override
    public void callService(final PluginParameters parameters, final InputStream in, final OutputStream out) throws IOException {
        final String graphId = parameters.getStringValue(GRAPH_ID_PARAMETER_ID);
        final boolean selected = parameters.getBooleanValue(SELECTED_PARAMETER_ID);
        final boolean vx = parameters.getBooleanValue(VX_PARAMETER_ID);
        final boolean tx = parameters.getBooleanValue(TX_PARAMETER_ID);
        final String attrsParam = parameters.getStringValue(ATTRS_PARAMETER_ID);

        // Allow the user to specify a specific set of attributes,
        // cutting down data transfer and processing a lot,
        // particularly on the Python side.
        final String[] attrsArray = attrsParam != null ? attrsParam.split(",") : new String[0];
        final Set<String> attrs = new LinkedHashSet<>(); // Maintain the order specified by the user.
        Collections.addAll(attrs, attrsArray);

        // Build the JSON in a form suitable for passing to pandas.DataFrame.from_items().
        // This includes the datatypes with the names, so the client can do transforms
        // where required (for example, converting strings to timestamps).
        //
        final IoProgress ioph = new HandleIoProgress("External script: get RecordStore");
        ioph.start();
        ioph.progress("Building RecordStore...");
        final GraphRecordStore recordStore;
        final Graph graph = graphId == null ? RestUtilities.getActiveGraph() : GraphNode.getGraph(graphId);
        if (graph == null) {
            throw new RestServiceException(HTTP_UNPROCESSABLE_ENTITY, "No graph with id " + graphId);
        }
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
                throw new RestServiceException("The following attributes do not exist in the record store: " + buf.toString());
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
     * Convert a "key<type>" to "key|type" to make things slightly easier for
     * the attrWithType.
     *
     * @param attrWithType
     *
     * @return
     */
    private static String keyedName(final String attrWithType) {
        final int ix = attrWithType.lastIndexOf('<');

        return attrWithType.substring(0, ix) + SeparatorConstants.PIPE + attrWithType.substring(ix + 1, attrWithType.length() - 1);
    }
}
