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
package au.gov.asd.tac.constellation.graph.file.io;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.attribute.ObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.utilities.ImmutableObjectCache;
import au.gov.asd.tac.constellation.graph.versioning.UpdateProvider;
import au.gov.asd.tac.constellation.graph.versioning.UpdateProviderManager;
import au.gov.asd.tac.constellation.utilities.gui.IoProgress;
import au.gov.asd.tac.constellation.utilities.stream.ExtendedBuffer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Read a graph in JSON format.
 *
 * @author algol
 */
public final class GraphJsonReader {

    private static final Logger LOGGER = Logger.getLogger(GraphJsonReader.class.getName());

    // After each interval of this many elements, report to the user.
    private static final int REPORT_INTERVAL = 10000;

    // Track classes that know how to read particular types from JSON.
    private final Map<String, AbstractGraphIOProvider> providers;
    private Map<String, Integer> versionedItems;
    private JsonParser jp;
    private Graph graph;
    private int version;
    private long globalModCount;
    private long attrModCount;
    private long structModCount;
    private final Map<Integer, Long> attrValCount = new HashMap<>();
    private GraphByteReader byteReader;
    
    private static final String ATTRIBUTE_MOD_COUNT = "attribute_mod_count";
    private static final String GLOBAL_MOD_COUNT = "global_mod_count";
    private static final String STRUCTURE_MOD_COUNT = "structure_mod_count";
    
    private static final String DID_NOT_FIND_FORMAT = "Did not find '%s' at '%s'";
    private static final String EXPECTED_LONG_FORMAT = "Expected long value, found '%s' at %s";
    private static final String EXPECTED_NUMERIC_FORMAT = "Expected numeric value, found '%s' at %s";
    private static final String EXPECTED_START_OBJECT_FORMAT = "Expected START_OBJECT, found '%s'.";
    private static final String EXPECTED_END_OBJECT_FORMAT = "Expected END_OBJECT, found '%s'.";

    /**
     * No construction.
     */
    public GraphJsonReader() {
        providers = new HashMap<>();

        Lookup.Result<AbstractGraphIOProvider> providerResults = Lookup.getDefault().lookupResult(AbstractGraphIOProvider.class);
        providerResults.allInstances().forEach(provider -> providers.put(provider.getName(), provider));

        byteReader = null;
    }

    public Graph readGraphZip(final File graphFile, final IoProgress progress) throws IOException, GraphParseException {
        try (final InputStream in = new BufferedInputStream(new FileInputStream(graphFile))) {
            return readGraphZip(graphFile.getPath(), in, progress);
        }
    }

    public Graph readGraphZip(final String name, InputStream bin, final IoProgress progress) throws IOException, GraphParseException {
        progress.start(100);
        progress.progress("Reading file: " + name);

        byteReader = new GraphByteReader(bin);
        bin.close();

        try {
            // Get the graph first.
            final String graphEntry = "graph" + GraphFileConstants.FILE_EXTENSION;
            ExtendedBuffer in = byteReader.read(graphEntry);
            if (in == null) {
                final String msg = "Entry " + graphEntry + " not found in graph file";
                throw new GraphParseException(msg);
            }

            try {
                graph = readGraph(name, in.getInputStream(), in.getAvailableSize(), progress);
            } catch (IllegalStateException ex) {
                throw new GraphParseException(ex.getMessage(), ex);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new GraphParseException(ex.getMessage(), ex);
            }finally {
                in.getInputStream().close();
            }
        } finally {
            byteReader = null;
        }

        return graph;
    }

    /**
     * reads the graph level mod count.
     *
     * @param current
     * @throws IOException
     * @throws GraphParseException
     */
    private JsonToken readGraphModCounts(JsonToken current) throws IOException, GraphParseException {
        //read global mod count
        final JsonNode node = jp.readValueAsTree();
        if (!node.has(GLOBAL_MOD_COUNT)) {
            final String msg = String.
                    format(EXPECTED_LONG_FORMAT, current, jp.getCurrentLocation());
            throw new GraphParseException(msg);
        }
        if (!node.has(STRUCTURE_MOD_COUNT)) {
            final String msg = String.
                    format(EXPECTED_LONG_FORMAT, current, jp.getCurrentLocation());
            throw new GraphParseException(msg);
        }
        if (!node.has(ATTRIBUTE_MOD_COUNT)) {
            final String msg = String.
                    format(EXPECTED_LONG_FORMAT, current, jp.getCurrentLocation());
            throw new GraphParseException(msg);
        }
        //global
        if (node.get(GLOBAL_MOD_COUNT).isNumber()) {
            globalModCount = node.get(GLOBAL_MOD_COUNT).asLong();
        } else {
            final String msg = String.
                    format(EXPECTED_NUMERIC_FORMAT, node.get(GLOBAL_MOD_COUNT).asText(), jp.getCurrentLocation());
            throw new GraphParseException(msg);
        }
        //structure
        if (node.get(STRUCTURE_MOD_COUNT).isNumber()) {
            globalModCount = node.get(STRUCTURE_MOD_COUNT).asLong();
        } else {
            final String msg = String.
                    format(EXPECTED_NUMERIC_FORMAT, node.get(STRUCTURE_MOD_COUNT), jp.getCurrentLocation());
            throw new GraphParseException(msg);
        }
        //attribute
        if (node.get(ATTRIBUTE_MOD_COUNT).isNumber()) {
            globalModCount = node.get(ATTRIBUTE_MOD_COUNT).asLong();
        } else {
            final String msg = String.
                    format(EXPECTED_NUMERIC_FORMAT, node.get(GLOBAL_MOD_COUNT), jp.getCurrentLocation());
            throw new GraphParseException(msg);
        }
        current = jp.getLastClearedToken();
        return current;
    }

    /**
     * Read a graph file into a graph.
     * <p>
     * The GRAPH, VERTEX, TRANSACTION objects must be present in that order.
     * Other objects (in particular META) are optional.
     * <p>
     * When the JSON file was written, the graph vertex id was used as the JSON
     * id by {@link GraphJsonWriter}. Since these can be in any order (due to
     * vertex deletes), it's very possible that the id of an newly added vertex
     * won't match the JSON id. Therefore, a mapping of JSON id to graph vertex
     * id must be maintained here, and used by any implementers of
     * {@link au.gov.asd.tac.constellation.graph.io.providers.AbstractGraphIOProvider#readObject}
     * that uses vertex ids.
     *
     * @param path The name of the file being read.
     * @param in The InputStream to read from.
     * @param entrySize The size of the file being read (-1 if unknown).
     * @param progress A progress indicator.
     *
     * @return A new Graph.
     *
     * @throws IOException If an I/O error occurs.
     * @throws java.lang.InterruptedException If the graph can't be locked.
     * @throws GraphParseException On graph parsing errors.
     */
    public Graph readGraph(final String path, final InputStream in, final long entrySize, final IoProgress progress) throws IOException, InterruptedException, GraphParseException {

        final ImmutableObjectCache immutableObjectCache = new ImmutableObjectCache();

        // Use a combination of stream and tree-model parsing.
        jp = new MappingJsonFactory().createParser(in);

        final Map<Integer, Integer> vertexMap = new HashMap<>();
        final Map<Integer, Integer> transactionMap = new HashMap<>();
        final StoreGraph storeGraph;

        JsonToken current = jp.nextToken();
        if (current != JsonToken.START_ARRAY) {
            throw new GraphParseException(String.format(EXPECTED_START_OBJECT_FORMAT, current));
        }

        // Read the graph data.
        // "version", GRAPH, VERTEX, TRANSACTION are mandatory in that order.
        // META is optional.
        current = jp.nextToken();
        if (current != JsonToken.START_OBJECT) {
            throw new GraphParseException(String.format(EXPECTED_START_OBJECT_FORMAT, current));
        }

        // Read the file format version number.
        current = jp.nextToken();
        if (current == JsonToken.FIELD_NAME && jp.getCurrentName().equals("version")) {
            current = jp.nextToken();
            if (current == JsonToken.VALUE_NUMBER_INT) {
                version = jp.getIntValue();
            } else {
                final String msg = String.format("Expected version integer, found '%s' at %s", current, jp.getCurrentLocation());
                throw new GraphParseException(msg);
            }
        } else {
            final String msg = String.format("Expected FIELD_NAME 'version', found %s '%s' at %s", current, jp.getCurrentName(), jp.getCurrentLocation());
            throw new GraphParseException(msg);
        }

        if (version < 0 || version > GraphJsonWriter.VERSION) {
            final String msg = String.format("Version number %d is unknown.", version);
            throw new GraphParseException(msg);
        }

        versionedItems = new HashMap<>();

        // Get the versions of various items in this graph (if the graph supports it)
        if (version >= 2) {
            current = jp.nextToken();
            if (current == JsonToken.FIELD_NAME && jp.getCurrentName().equals("versionedItems")) {
                current = jp.nextToken();
                if (current == JsonToken.START_OBJECT) {
                    while ((current = jp.nextToken()) != JsonToken.END_OBJECT) {
                        if (current != JsonToken.FIELD_NAME) {
                            final String msg = String.format("Expected versioned item name, found '%s' at %s", current, jp.getCurrentLocation());
                            throw new GraphParseException(msg);
                        }
                        final String versionedItem = jp.getCurrentName();
                        current = jp.nextToken();
                        if (current != JsonToken.VALUE_NUMBER_INT) {
                            final String msg = String.format("Expected version integer, found '%s' at %s", current, jp.getCurrentLocation());
                            throw new GraphParseException(msg);
                        }
                        final int versionNumber = jp.getIntValue();
                        versionedItems.put(versionedItem, versionNumber);
                    }
                } else {
                    final String msg = String.format("Expected start object for 'versionedItems', found '%s' at %s", current, jp.getCurrentLocation());
                    throw new GraphParseException(msg);
                }
            } else {
                final String msg = String.format("Expected FIELD_NAME 'versionedItems', found '%s' at %s", current, jp.getCurrentLocation());
                throw new GraphParseException(msg);
            }
        }

        // some dodgey that shouldnt happen any more... increment the version number
        // everytime we add something to prevent this.. its too late now.
        String schemaFactoryName = null;
        current = jp.nextToken();
        if (current == JsonToken.FIELD_NAME && jp.getCurrentName().equals("schema")) {
            current = jp.nextToken();
            if (current == JsonToken.VALUE_STRING) {
                schemaFactoryName = jp.getValueAsString();
            } else {
                final String msg = String.format("Expected schema string, found '%s' at %s", current, jp.getCurrentLocation());
                throw new GraphParseException(msg);
            }
            current = jp.nextToken();
        }

        if (version >= 1) {
            current = readGraphModCounts(current);
        }

        if (current != JsonToken.END_OBJECT) {
            throw new GraphParseException(String.format(EXPECTED_END_OBJECT_FORMAT, current));
        }

        SchemaFactory schemaFactory = SchemaFactoryUtilities.getSchemaFactory(schemaFactoryName);
        if (schemaFactory == null) {
            final SchemaFactory defaultSchemaFactory = SchemaFactoryUtilities.getDefaultSchemaFactory();
            LOGGER.warning(String.format("Unknown schema factory '%s'; falling back to '%s'", schemaFactoryName, defaultSchemaFactory.getName()));
            schemaFactory = defaultSchemaFactory;
        }

        storeGraph = new StoreGraph(schemaFactory.createSchema());
        UpdateProviderManager.getRegisteredProviders().forEach((item, itemProviders) -> {
            if (item.appliesToGraph(storeGraph)) {
                final int currentVersion = versionedItems.containsKey(item.getName()) ? versionedItems.get(item.getName()) : UpdateProvider.DEFAULT_VERSION;
                if (itemProviders.containsKey(currentVersion)) {
                    itemProviders.get(currentVersion).configure(storeGraph);
                }
            }
        });

        try {
            // Depending on the version number, different things could happen.
            // **************************
            // * Read the GRAPH object. *
            // **************************
            current = jp.nextToken();
            if (current != JsonToken.START_OBJECT) {
                throw new GraphParseException(String.format(EXPECTED_START_OBJECT_FORMAT, current));
            }

            current = jp.nextToken();
            if (current == JsonToken.FIELD_NAME && jp.getCurrentName().equals("graph")) {
                parseElement(storeGraph, GraphElementType.GRAPH, null, null, progress, entrySize, immutableObjectCache);
            } else {
                final String msg = String.format("Expected FIELD_NAME 'graph', found '%s' at %s", current, jp.getCurrentLocation());
                throw new GraphParseException(msg);
            }

            current = jp.nextToken();
            if (current != JsonToken.END_OBJECT) {
                throw new GraphParseException(String.format(EXPECTED_END_OBJECT_FORMAT, current));
            }

            // ***************************
            // * Read the VERTEX object. *
            // ***************************
            current = jp.nextToken();
            if (current != JsonToken.START_OBJECT) {
                throw new GraphParseException(String.format(EXPECTED_START_OBJECT_FORMAT, current));
            }

            current = jp.nextToken();
            if (current == JsonToken.FIELD_NAME && jp.getCurrentName().equals("vertex")) {
                parseElement(storeGraph, GraphElementType.VERTEX, vertexMap, null, progress, entrySize, immutableObjectCache);
            } else {
                final String msg = String.format("Expected FIELD_NAME 'vertex', found '%s' at %s", current, jp.getCurrentLocation());
                throw new GraphParseException(msg);
            }

            current = jp.nextToken();
            if (current != JsonToken.END_OBJECT) {
                throw new GraphParseException(String.format(EXPECTED_END_OBJECT_FORMAT, current));
            }

            // ********************************
            // * Read the TRANSACTION object. *
            // ********************************
            current = jp.nextToken();
            if (current != JsonToken.START_OBJECT) {
                throw new GraphParseException(String.format(EXPECTED_START_OBJECT_FORMAT, current));
            }

            current = jp.nextToken();
            if (current == JsonToken.FIELD_NAME && jp.getCurrentName().equals("transaction")) {
                parseElement(storeGraph, GraphElementType.TRANSACTION, vertexMap, transactionMap, progress, entrySize, immutableObjectCache);
            } else {
                final String msg = String.format("Expected FIELD_NAME 'transaction', found '%s' at %s", current, jp.getCurrentLocation());
                throw new GraphParseException(msg);
            }

            current = jp.nextToken();
            if (current != JsonToken.END_OBJECT) {
                throw new GraphParseException(String.format(EXPECTED_END_OBJECT_FORMAT, current));
            }

            // **********************************
            // * Read the optional META object. *
            // **********************************
            current = jp.nextToken();
            if (current != JsonToken.START_OBJECT) {
                throw new GraphParseException(String.format(EXPECTED_START_OBJECT_FORMAT, current));
            }

            current = jp.nextToken();
            if (current == JsonToken.FIELD_NAME && jp.getCurrentName().equals("meta")) {
                parseElement(storeGraph, GraphElementType.META, vertexMap, transactionMap, progress, entrySize, immutableObjectCache);
            } else if (current != JsonToken.END_OBJECT) {
                final String msg = String.format("Error: expected END_OBJECT, found '%s' at %s", current, jp.getCurrentLocation());
                throw new GraphParseException(msg);
            }

            current = jp.nextToken();
            if (current != JsonToken.END_OBJECT) {
                throw new GraphParseException(String.format(EXPECTED_END_OBJECT_FORMAT, current));
            }

            // Ensure the document finishes correctly.
            current = jp.nextToken();
            if (current != JsonToken.END_ARRAY) {
                throw new GraphParseException(String.format("Expected END_ARRAY, found '%s'.", current));
            }

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            jp.close();
        }

        //set mod count vals
        if (version >= 1) {
            storeGraph.setModificationCounters(globalModCount, structModCount, attrModCount);
            for (Entry<Integer, Long> e : attrValCount.entrySet()) {
                storeGraph.setValueModificationCounter(e.getKey(), e.getValue());
            }
        }

        try {
            // Allow any relevant version providers to update the graph if necessary.
            UpdateProviderManager.getRegisteredProviders().forEach((item, itemProviders) -> {
                if (item.appliesToGraph(storeGraph)) {
                    int currentVersion = versionedItems.containsKey(item.getName()) ? versionedItems.get(item.getName()) : UpdateProvider.DEFAULT_VERSION;
                    while (itemProviders.containsKey(currentVersion)) {
                        UpdateProvider provider = itemProviders.get(currentVersion);
                        provider.update(storeGraph);
                        currentVersion = provider.getToVersionNumber();
                    }
                }
            });
        } catch (Exception ex) {
            final String msg = "There was an error loading some parts of the graph. The error was " + ex.getLocalizedMessage();
            // TODO: throw a plugin exception
//            throw new PluginException(PluginNotificationLevel.ERROR, msg); 
            LOGGER.warning(msg);
            Exceptions.printStackTrace(ex);
        }

        graph = new DualGraph(schemaFactory.createSchema(), storeGraph);

        if (progress != null) {
            progress.finish();
        }

        LOGGER.log(Level.INFO, "immutableObjectCache={0}", immutableObjectCache);

        return graph;
    }

    /**
     * Parse an element type's attributes and data from a JSON file.
     *
     * @param elementType The element type of the attributes and data being
     * read.
     * @param vertexPositions The mapping of Constants.ID to vertex id; written
     * when vertices are parsed, read when transactions are parsed.
     * @param ph Progress handle.
     * @param entrySize The size of the file being read; -1 if unknown.
     *
     * @throws IOException If there is an IOException.
     * @throws GraphParseException If there is a GraphParseException.
     */
    private void parseElement(final GraphWriteMethods graph, final GraphElementType elementType, final Map<Integer, Integer> vertexPositions, final Map<Integer, Integer> transactionPositions, final IoProgress ph, final long entrySize, ImmutableObjectCache immutableObjectCache) throws GraphParseException, Exception {
        final String elementTypeLabel = IoUtilities.getGraphElementTypeString(elementType);

        JsonToken current;

        final Map<String, AttrInfo> attributes = new HashMap<>();
        int counter = 0;

        current = jp.nextToken();
        if (current != JsonToken.START_ARRAY) {
            final String msg = String.format("Expected '%s' START_ARRAY, found '%s'", elementTypeLabel, current);
            throw new GraphParseException(msg);
        }

        current = jp.nextToken();
        if (current != JsonToken.START_OBJECT) {
            final String msg = String.format("Expected '%s' START_OBJECT, found '%s'", elementTypeLabel, current);
            throw new GraphParseException(msg);
        }

        current = jp.nextToken();
        if (current != JsonToken.FIELD_NAME || !"attrs".equals(jp.getCurrentName())) {
            final String msg = String.format("Expected name 'attrs', found '%s'", current);
            throw new GraphParseException(msg);
        }

        current = jp.nextToken();
        if (current != JsonToken.START_ARRAY) {
            throw new GraphParseException(String.format("Expected 'attrs' START_ARRAY, found '%s'", current));
        }

        while (jp.nextToken() != JsonToken.END_ARRAY) {
            // Read the next attribute object into a tree node for easier parsing.
            final JsonNode node = jp.readValueAsTree();
            if (!node.has("label")) {
                final String msg = String.format("Did not find '%s' attribute 'label' at '%s'", elementTypeLabel, jp.getCurrentLocation());
                throw new GraphParseException(msg);
            }

            if (!node.has("type")) {
                final String msg = String.format("Did not find '%s' attribute 'type' at '%s'", elementTypeLabel, jp.getCurrentLocation());
                throw new GraphParseException(msg);
            }

            final String attrLabel = node.get("label").textValue();
            final String attrType = node.get("type").textValue();
            final String attrDesc = node.has("descr") ? node.get("descr").textValue() : null;
            final JsonNode dv = node.get("default");
            final Object attrDefault
                    = (dv == null || dv.isNull()) ? null
                            : dv.isNumber() ? dv.numberValue()
                            : dv.isBoolean() ? dv.booleanValue()
                            : dv.textValue();

            final String attributeMergerId = node.has("merger") ? node.get("merger").textValue() : null;

            try {
                final int attrId = graph.addAttribute(elementType, attrType, attrLabel, attrDesc, attrDefault, attributeMergerId);

                final Attribute attr = new GraphAttribute(graph, attrId);
                final boolean isNumber = attrType.equals("integer") || attrType.equals("float");
                final boolean isBoolean = attrType.equals("boolean");
                final boolean isObject = ObjectAttributeDescription.class.isAssignableFrom(attr.getDataType());
                attributes.put(attrLabel, new AttrInfo(attrId, attrType, isNumber, isBoolean, isObject));

                if (version >= 1) {
                    //get mod count for attribute
                    final Long modCount = node.get("mod_count").longValue();
                    attrValCount.put(attrId, modCount);
                }
            } catch (IllegalArgumentException ex) {
                // It's possible that we're reading a graph that contains an attribute type that we don't know about.
                // This can happen when a module adds a new META type to a saved graph, and someone attempts to open
                // that graph without that module (or an older version of that module).
                // We don't want an exception to be thrown, because the rest of the META section won't be read, and data
                // will be missing from the graph. Instead, we'll catch exceptions from addAttribute(), log them, and continue.
                if (elementType != GraphElementType.META) {
                    throw ex;
                }

                LOGGER.warning(String.format("While adding %s attribute: %s", elementType, ex.getMessage()));
            }
        }

        // Check for optional key definition.
        current = jp.nextToken();
        final String name = jp.getCurrentName();
        if (current == JsonToken.FIELD_NAME && "key".equals(name)) {
            if (elementType != GraphElementType.VERTEX && elementType != GraphElementType.TRANSACTION) {
                final String msg = String.format("Graph keys only allowed for %s and %s, not %s", GraphElementType.VERTEX, GraphElementType.TRANSACTION, elementType);
                throw new GraphParseException(msg);
            }

            current = jp.nextToken();
            if (current != JsonToken.START_ARRAY) {
                final String msg = String.format("Expected 'key' array, found '%s'", current);
                throw new GraphParseException(msg);
            }

            // Gather the key labels.
            final ArrayList<Integer> keyAttrIds = new ArrayList<>();
            while (jp.nextToken() != JsonToken.END_ARRAY) {
                // Read the key attributes from the array and create the graph key.
                final String keyLabel = jp.readValueAs(String.class);
                if (!attributes.containsKey(keyLabel)) {
                    final String msg = String.format("Key '%s' is not a valid attribute", keyLabel);
                    throw new GraphParseException(msg);
                }

                keyAttrIds.add(attributes.get(keyLabel).attrId);
            }

            // Create the primary key.
            final int[] keyAttributes = new int[keyAttrIds.size()];
            for (int i = 0; i < keyAttrIds.size(); i++) {
                keyAttributes[i] = keyAttrIds.get(i);
            }

            graph.setPrimaryKey(elementType, keyAttributes);

            current = jp.nextToken();
        }

        if (current != JsonToken.END_OBJECT) {
            final String msg = String.format("Expected '%s' attrs END_OBJECT, found '%s'", elementTypeLabel, current);
            throw new GraphParseException(msg);
        }

        current = jp.nextToken();
        if (current != JsonToken.START_OBJECT) {
            final String msg = String.format("Expected '%s' data START_OBJECT, found '%s'", elementTypeLabel, current);
            throw new GraphParseException(msg);
        }

        current = jp.nextToken();
        final String inData = jp.getCurrentName();
        if (current != JsonToken.FIELD_NAME || !"data".equals(inData)) {
            final String msg = String.format("Expected 'data', found '%s'", current);
            throw new GraphParseException(msg);
        }

        current = jp.nextToken();
        if (current != JsonToken.START_ARRAY) {
            final String msg = String.format("Expected '%s' data array start, found '%s'", elementTypeLabel, current);
            throw new GraphParseException(msg);
        }

        while (jp.nextToken() != JsonToken.END_ARRAY) {
            // Read the object into a tree model.
            final JsonNode node = jp.readValueAsTree();
            final int id;
            if (elementType == GraphElementType.VERTEX) {
                final JsonNode idNode = node.get(GraphFileConstants.VX_ID);
                if (idNode == null) {
                    final String msg = String.format(DID_NOT_FIND_FORMAT, GraphFileConstants.VX_ID, jp.getCurrentLocation());
                    throw new GraphParseException(msg);
                }

                final int jsonId = idNode.intValue();
                id = graph.addVertex();
                vertexPositions.put(jsonId, id);
            } else if (elementType == GraphElementType.TRANSACTION) {
                final JsonNode idNode = node.get(GraphFileConstants.TX_ID);
                // We can't test for null and throw an exception here: putting the txId in the file is an afterthought,
                // so lots of existing graphs won't have it.

                final JsonNode srcNode = node.get(GraphFileConstants.SRC);
                if (srcNode == null) {
                    final String msg = String.format(DID_NOT_FIND_FORMAT, GraphFileConstants.SRC, jp.getCurrentLocation());
                    throw new GraphParseException(msg);
                }

                final JsonNode dstNode = node.get(GraphFileConstants.DST);
                if (dstNode == null) {
                    final String msg = String.format(DID_NOT_FIND_FORMAT, GraphFileConstants.DST, jp.getCurrentLocation());
                    throw new GraphParseException(msg);
                }

                final JsonNode dirNode = node.get(GraphFileConstants.DIR);
                if (dirNode == null) {
                    final String msg = String.format(DID_NOT_FIND_FORMAT, GraphFileConstants.DIR, jp.getCurrentLocation());
                    throw new GraphParseException(msg);
                }

                // Map the ids in the JSON file to the vertex ids in the graph.
                final int jsonId = idNode != null ? idNode.intValue() : Graph.NOT_FOUND;
                final int jsonSrc = srcNode.intValue();
                final int jsonDst = dstNode.intValue();

                final int src = vertexPositions.get(jsonSrc);
                final int dst = vertexPositions.get(jsonDst);
                final boolean directed = dirNode.booleanValue();
                id = graph.addTransaction(src, dst, directed);
                if (jsonId != Graph.NOT_FOUND) {
                    transactionPositions.put(jsonId, id);
                }
            } else if (elementType == GraphElementType.GRAPH || elementType == GraphElementType.META) {
                id = 0;
            } else {
                id = Graph.NOT_FOUND;
            }

            for (Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext();) {
                final Map.Entry<String, JsonNode> entry = it.next();
                final String label = entry.getKey();
                final JsonNode jnode = entry.getValue();
                final AttrInfo ai = attributes.get(label);
                if (ai != null && providers.containsKey(ai.attrType)) {
                    AbstractGraphIOProvider ioProvider = providers.get(ai.attrType);
                    ioProvider.readObject(ai.attrId, id, jnode, graph, vertexPositions, transactionPositions, byteReader, immutableObjectCache);
                } else if (ai != null) {
                    throw new Exception("No IO provider found for attribute type: " + ai.attrType);
                }
            }

            if (++counter % REPORT_INTERVAL == 0) {
                final String msg = String.format("Vertices: %d; Transactions %d", graph.getVertexCount(), graph.getTransactionCount());
                final long charOffset = jp.getCurrentLocation().getByteOffset();
                if (entrySize != -1 && charOffset != -1 && ph != null) {
                    final int workunit = (int) (100 * (charOffset / (double) entrySize));
                    ph.progress(msg, workunit);
                } else if (ph != null) {
                    ph.progress(msg);
                }
            }
        }

        current = jp.nextToken();
        if (current != JsonToken.END_OBJECT) {
            final String msg = String.format("Expected '%s' END_OBJECT after data, found '%s'", elementTypeLabel, current);
            throw new GraphParseException(msg);
        }

        current = jp.nextToken();
        if (current != JsonToken.END_ARRAY) {
            final String msg = String.format("Expected '%s' END_ARRAY, found '%s'", elementTypeLabel, current);
            throw new GraphParseException(msg);
        }
    }

    /**
     * Maintain attribute information.
     */
    private static class AttrInfo {

        final int attrId;
        final String attrType;
        final boolean isNumber;
        final boolean isBoolean;
        final boolean isObject;

        /**
         * Construct a new instance.
         *
         * @param attrId Attribute id.
         * @param attrType Attribute type.
         * @param isNumber Is the attribute a number?
         * @param isBoolean Is the attribute a boolean?
         * @param isObject is the attribute an object?
         */
        AttrInfo(final int attrId, final String attrType, final boolean isNumber, final boolean isBoolean, final boolean isObject) {
            this.attrId = attrId;
            this.attrType = attrType;
            this.isNumber = isNumber;
            this.isBoolean = isBoolean;
            this.isObject = isObject;
        }

        @Override
        public String toString() {
            return String.format("AttrInfo[id=%d,type=%s,isNumber=%s,isBoolean=%s,isObject=%s]", attrId, attrType, isNumber, isBoolean, isObject);
        }
    }
}
