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
package au.gov.asd.tac.constellation.graph.processing;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionTypeUtilities;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexTypeUtilities;
import au.gov.asd.tac.constellation.graph.utilities.CompositeTransactionId;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * A collection of utilities to perform operations on {@link GraphRecordStore} *
 * objects.
 *
 * @author sirius
 */
public class GraphRecordStoreUtilities {

    private GraphRecordStoreUtilities() {
        throw new IllegalStateException("Utility class");
    }

    private static final int NO_ELEMENT = -1;
    private static final String TYPE_KEY = "Type<string>";

    public static final String COPY = "copy.";
    public static final String SOURCE = "source.";
    public static final String ALL = "all.";
    public static final String DESTINATION = "destination.";
    public static final String TRANSACTION = "transaction.";
    public static final String ID = "[id]<string>";
    public static final String GHOST = "[ghost]<string>";
    public static final String DIRECTED_KEY = "[directed]<string>";
    public static final String COMPLETE_WITH_SCHEMA_KEY = "[complete_with_schema]<string>";
    public static final String DELETE_KEY = "[delete]<string>";

    private static final String SELECTED_ATTRIBUTE_NAME = "selected";
    private static final String FALSE = "false";
    private static final String NUMBER_STRING_STRING_FORMAT = "%d:%s:%s";

    private static final Charset UTF8 = StandardCharsets.UTF_8;

    private static final Logger LOGGER = Logger.getLogger(GraphRecordStoreUtilities.class.getName());
    
    // Columns that contain the type, either through the Identifier or Type.
    private static final List<String> LabelTypes = Arrays.asList(
            "source.Identifier",
            "source.Label",
            "destination.Identifier",
            "destination.Label",
            "source.Type",
            "destination.Type"
    );
    private static final List<String> ApprovedTypes = SchemaVertexTypeUtilities.getTypes().stream().map(i -> i.getName()).collect(Collectors.toList());


    private static int addVertex(final GraphWriteMethods graph, final Map<String, String> values,
            final Map<String, Integer> vertexMap, final boolean initializeWithSchema, boolean completeWithSchema,
            final List<Integer> newVertices, final Set<Integer> ghostVertices, final List<String> vertexIdAttributes) {
        String idValue = values.remove(ID);

        // If the idValue has not been set and we have vertexIdAttributes then create an idValue automatically
        if (idValue == null && vertexIdAttributes != null) {
            final StringBuilder idBuilder = new StringBuilder();
            String delimiter = "";

            // If the vertexIdAttributes is not null but empty then use all attributes of the vertex
            if (vertexIdAttributes.isEmpty()) {
                for (final Entry<String, String> e : values.entrySet()) {
                    idBuilder.append(delimiter).append(e.getKey()).append("=");
                    if (e.getValue() != null) {
                        for (final byte b : e.getValue().getBytes(UTF8)) {
                            int i = b;
                            if (i < 0) {
                                i += 256;
                            }
                            if (i < 16) {
                                idBuilder.append('0');
                            }
                            idBuilder.append(Integer.toHexString(i));
                        }
                        delimiter = ",";
                    }
                }               
            } else { // Otherwise use the specified attributes to create the idValue
                for (final String attribute : vertexIdAttributes) {
                    final String value = values.get(attribute);
                    if (value != null) {
                        idBuilder.append(delimiter).append(attribute).append("=");
                        for (final byte b : value.getBytes(UTF8)) {
                            int i = b;
                            if (i < 0) {
                                i += 256;
                            }
                            if (i < 16) {
                                idBuilder.append('0');
                            }
                            idBuilder.append(Integer.toHexString(i));
                        }
                        delimiter = ",";
                    }
                }
            }

            idValue = idBuilder.length() == 0 ? "***" : idBuilder.toString();
        }

        final int vertex = getVertex(graph, idValue, vertexMap, initializeWithSchema, newVertices);

        if (values.remove(DELETE_KEY) != null) {
            graph.removeVertex(vertex);
            return NO_ELEMENT;
        }

        if (values.remove(GHOST) != null) {
            ghostVertices.add(vertex);
        }

        final String completeWithSchemaValue = values.remove(COMPLETE_WITH_SCHEMA_KEY);
        if (completeWithSchemaValue != null) {
            completeWithSchema = Boolean.parseBoolean(completeWithSchemaValue);
        }

        copyValues(graph, GraphElementType.VERTEX, vertex, values);

        if (completeWithSchema && graph.getSchema() != null) {
            graph.getSchema().completeVertex(graph, vertex);
        }

        return vertex;
    }

    private static int getVertex(final GraphWriteMethods graph, final String id, final Map<String, Integer> vertexMap, 
            final boolean initializeWithSchema, final List<Integer> newVertices) {
        if (StringUtils.isNotBlank(id)) {
            try {
                Integer vertex = Integer.valueOf(id);
                if (graph.vertexExists(vertex)) {
                    return vertex;
                }
            } catch (final NumberFormatException ex) {
                // it's a non-integer id being passed but that's ok, continue on
            }

            Integer vertex = vertexMap.get(id);
            if (vertex != null) {
                if (graph.vertexExists(vertex)) {
                    // if we hit here, it is because the vertex already exists on the graph but the graph doesn't have a vertex id attribute
                    // (since it skipped the earlier existence check due to the non-integer id generated in the super-fuction)
                    return vertex;
                }
                // if we hit here, we are likely trying to copy a vertex from another graph including its original id
                // calling addVertex ensrure the graph has capacity to handle the id
                vertex = graph.addVertex(vertex);
            } else {               
                vertex = graph.addVertex();
                vertexMap.put(id, vertex);
            }

            if (initializeWithSchema) {
                graph.getSchema().newVertex(graph, vertex);
            }
            newVertices.add(vertex);
            return vertex;
        }

        final int vertex = graph.addVertex();
        if (initializeWithSchema) {
            graph.getSchema().newVertex(graph, vertex);
        }
        newVertices.add(vertex);

        return vertex;
    }

    private static int addTransaction(final GraphWriteMethods graph, final int source, final int destination, 
            final Map<String, String> values, final Map<String, Integer> transactionMap, 
            final boolean initializeWithSchema, boolean completeWithSchema) {
        final String type = values.get(TYPE_KEY);
        final String directedValue = values.get(DIRECTED_KEY);
        boolean directed = true;
        if (directedValue != null) {
            directed = !"False".equalsIgnoreCase(directedValue);
        } else {
            final SchemaTransactionType transactionType = SchemaTransactionTypeUtilities.getType(type);
            if (transactionType != null) {
                directed = transactionType.isDirected();
            }
        }

        final String completeWithSchemaValue = values.remove(COMPLETE_WITH_SCHEMA_KEY);
        if (completeWithSchemaValue != null) {
            completeWithSchema = Boolean.parseBoolean(completeWithSchemaValue);
        }

        final String idValue = values.remove(ID);
        final int transaction = getTransaction(graph, idValue, source, destination, directed, transactionMap, initializeWithSchema);

        if (values.remove(DELETE_KEY) != null) {
            graph.removeTransaction(transaction);
            return NO_ELEMENT;
        }

        if (transaction == NO_ELEMENT) { // TODO: should this check be done before the delete?
            return NO_ELEMENT;
        }

        copyValues(graph, GraphElementType.TRANSACTION, transaction, values);

        if (completeWithSchema && graph.getSchema() != null) {
            graph.getSchema().completeTransaction(graph, transaction);
        }

        return transaction;
    }

    private static int getTransaction(final GraphWriteMethods graph, final String id, final int source, 
            final int destination, final boolean directed, final Map<String, Integer> transactionMap, 
            final boolean initializeWithSchema) {
        if (StringUtils.isNotBlank(id)) {
            try {
                final Integer transaction = Integer.valueOf(id);
                if (graph.transactionExists(transaction)) {
                    return transaction;
                }
            } catch (final NumberFormatException ex) {
                // it's a non-integer id being passed but that's ok, continue on
            }

            Integer transaction = transactionMap.get(id);
            if (transaction != null) {
                if (graph.transactionExists(transaction)) {
                    // if we hit here, it is because the transaction already exists on the graph but the graph doesn't have a transaction id attribute
                    // (since it skipped the earlier existence check due to the null id being passed)
                    return transaction;
                }
                // if we hit here, we are likely trying to copy a transaction from another graph including its original id
                // calling addTransaction ensures the graph has capacity to handle the id
                transaction = graph.addTransaction(transaction, source, destination, directed);
            } else if (source == NO_ELEMENT || destination == NO_ELEMENT) {
                return NO_ELEMENT;
            } else {
                transaction = graph.addTransaction(source, destination, directed);
                transactionMap.put(id, transaction);
            }
            
            if (initializeWithSchema) {
                graph.getSchema().newTransaction(graph, transaction);
            }
            return transaction;
        }

        final int transaction = graph.addTransaction(source, destination, directed);
        if (initializeWithSchema) {
            graph.getSchema().newTransaction(graph, transaction);
        }
        return transaction;
    }

    private static void copyValues(final GraphWriteMethods graph, final GraphElementType elementType, final int element, 
            final Map<String, String> values) {
        /**
         * check whether a transaction type is inconsistent with the direction
         * attribute, if so make a custom type
         */
        if (GraphElementType.TRANSACTION.equals(elementType)) {
            final String requestedDirected = values.remove(DIRECTED_KEY);
            if (requestedDirected != null) {
                final String type = values.get(TYPE_KEY);
                final SchemaTransactionType currentType = SchemaTransactionTypeUtilities.getType(type);
                if (currentType != null) {
                    final boolean directed = Boolean.parseBoolean(requestedDirected);
                    // if the requested direction is different to the type's direction then make a new type
                    if (currentType.isDirected() != directed) {
                        final String typeName = String.format("%s (%s)", currentType, directed ? "directed" : "undirected");
                        final SchemaTransactionType modifiedType = new SchemaTransactionType.Builder(currentType, typeName)
                                .setDirected(Boolean.valueOf(requestedDirected))
                                .build();

                        if (!SchemaTransactionTypeUtilities.containsType(modifiedType)) {
                            SchemaTransactionTypeUtilities.addCustomType(modifiedType, false);
                        }
                        values.put(TYPE_KEY, modifiedType.getName());
                    }
                }
            }
        }

        values.entrySet().stream().forEach(entry -> {
            String key = entry.getKey();
            String type = "string";
            if (key.endsWith(">")) {
                final int typeStart = key.lastIndexOf('<');
                if (typeStart > 0) {
                    type = key.substring(typeStart + 1, key.length() - 1);
                    key = key.substring(0, typeStart);
                }
            }

            // TODO: look at ensure(true/fale)
            int attribute = graph.getAttribute(elementType, key);
            if (attribute == Graph.NOT_FOUND) {
                attribute = graph.getSchema() != null ? graph.getSchema().getFactory().ensureAttribute(graph, elementType, key) : Graph.NOT_FOUND;
                if (attribute == Graph.NOT_FOUND) {
                    attribute = graph.addAttribute(elementType, type, key, key, null, null);
                }
            }

            try {
                graph.setStringValue(attribute, element, entry.getValue());
            } catch (final Exception ex) {
                // keeping this as an Exception to catch broad exceptions that can be thrown due to bad data
                LOGGER.log(Level.SEVERE, "Discarding unexpected value {0} seen in attribute {1}", new Object[]{entry.getValue(), graph.getAttributeName(attribute)});
            }
        });
    }

    /**
     * Adds the contents of the specified {@link RecordStore} to the specified
     * graph.
     *
     * @param graph A {@link GraphWriteMethods} object to add the
     * {@link RecordStore} contents to.
     * @param recordStore The {@link RecordStore} to be added to the graph.
     * @param initializeWithSchema A boolean value specifying whether or not to
     * initialise the graph with its schema before adding the
     * {@link RecordStore} to it.
     * @param completeWithSchema A boolean value specifying whether or not to
     * complete the graph with its schema after adding the {@link RecordStore}
     * to it.
     * @param vertexIdAttributes if not null, this list of attributes will be
     * used to create an id value. An empty list will cause all attributes to be
     * used.
     * @return A {@link List} of {@link Integer} objects representing the vertex
     * id's of the newly added vertices.
     */
    public static List<Integer> addRecordStoreToGraph(final GraphWriteMethods graph, final RecordStore recordStore,
            final boolean initializeWithSchema, final boolean completeWithSchema, final List<String> vertexIdAttributes) {
        return addRecordStoreToGraph(graph, recordStore, initializeWithSchema, completeWithSchema, vertexIdAttributes, null, null);
    }

    /**
     * Adds the contents of the specified {@link RecordStore} to the specified
     * graph.
     *
     * @param graph A {@link GraphWriteMethods} object to add the
     * {@link RecordStore} contents to.
     * @param recordStore The {link RecordStore} to be added to the graph.
     * @param initializeWithSchema Whether or not to initialise the graph with
     * its schema before adding the {@link RecordStore} to it.
     * @param completeWithSchema Whether or not to complete the graph with its
     * schema after adding the {@link RecordStore} to it.
     * @param vertexIdAttributes if not null, this list of attributes will be
     * used to create an id value. An empty list will cause all attributes to be
     * used.
     * @param vertexMap A map which will be populated with the mappings from
     * vertex id in the {@link RecordStore} (or created vertex id if no id was
     * provided in the {@link RecordStore}) to vertex id on the graph.
     * @param transactionMap A map which will be populated with the mappings
     * from transaction id in the {@link RecordStore} (or created transaction id
     * if no id was provided in the {@link RecordStore}) to transaction id on
     * the graph.
     * @return A {@link List} of {@link Integer} objects representing the vertex
     * id's of the newly added vertices.
     */
    public static List<Integer> addRecordStoreToGraph(final GraphWriteMethods graph, final RecordStore recordStore,
            final boolean initializeWithSchema, final boolean completeWithSchema, final List<String> vertexIdAttributes,
            Map<String, Integer> vertexMap, Map<String, Integer> transactionMap) {
        final List<Integer> newVertices = new ArrayList<>();
        final Set<Integer> ghostVertices = new HashSet<>();

        recordStore.reset();
        final List<String> keys = recordStore instanceof GraphRecordStore graphRecordStore 
                ? graphRecordStore.keysWithType() : recordStore.keys();

        if (vertexMap == null) {
            vertexMap = new HashMap<>();
        }
        if (transactionMap == null) {
            transactionMap = new HashMap<>();
        }

        while (recordStore.next()) {
            final Map<String, String> sourceValues = new TreeMap<>();
            final Map<String, String> destinationValues = new TreeMap<>();
            final Map<String, String> transactionValues = new TreeMap<>();
            for (final String key : keys) {
                if (recordStore.hasValue(key)) {
                    String value = recordStore.get(key);
                    final int dividerPosition = key.indexOf('.');

                    if (dividerPosition > 0) {
                        final String keyDescriptor = key.substring(0, dividerPosition).toLowerCase();
                        final String keyAttribute = key.substring(dividerPosition + 1);
                        final String[] parts = keyDescriptor.split("\\.");
                        final String label = key.split("<")[0];
                        
                        if (LabelTypes.indexOf(label) > -1) {
                            value = normalizeType(value);
                        }

                        switch (parts[0]) {
                            case "source" -> sourceValues.put(keyAttribute, value);
                            case "destination" -> destinationValues.put(keyAttribute, value);
                            case "transaction" -> transactionValues.put(keyAttribute, value);
                            default -> {
                                // do nothing
                            }
                        }
                    }
                }
            }

            if (sourceValues.isEmpty() && destinationValues.isEmpty() && transactionValues.containsKey(ID)) {
                // This will not add a new transaction to the graph (as source and destination are both -1), but if the transaction exists already it will be returned allowing it to be selected.
                addTransaction(graph, NO_ELEMENT, NO_ELEMENT, transactionValues, transactionMap, initializeWithSchema, completeWithSchema);
            } else if (!sourceValues.isEmpty() && !destinationValues.isEmpty()) {
                final int source = addVertex(graph, sourceValues, vertexMap, initializeWithSchema, completeWithSchema, newVertices, ghostVertices, vertexIdAttributes);
                final int destination = addVertex(graph, destinationValues, vertexMap, initializeWithSchema, completeWithSchema, newVertices, ghostVertices, vertexIdAttributes);
                addTransaction(graph, source, destination, transactionValues, transactionMap, initializeWithSchema, completeWithSchema);
            } else if (!sourceValues.isEmpty()) {
                addVertex(graph, sourceValues, vertexMap, initializeWithSchema, completeWithSchema, newVertices, ghostVertices, vertexIdAttributes);
            } else if (!destinationValues.isEmpty()) {
                addVertex(graph, destinationValues, vertexMap, initializeWithSchema, completeWithSchema, newVertices, ghostVertices, vertexIdAttributes);
            }
        }

        // Ghost vertices only exist to allow transactions to be present. If, after merging has occurred, a ghost
        // vertex still exists in the graph (ie has not been merged with another vertex) then is should be removed.
        if (!ghostVertices.isEmpty()) {
            graph.validateKey(GraphElementType.VERTEX, true);
            for (final Integer vertex : ghostVertices) {
                if (graph.vertexExists(vertex)) {
                    graph.removeVertex(vertex);
                }
            }
        }

        return newVertices;
    }

    /**
     * Populate a new {@link RecordStore} with the attribute values of vertices,
     * transactions and their endpoint vertices.
     *
     * @param graph A {@link GraphReadMethods} from which the
     * {@link RecordStore} will be created.
     * @param selectedOnly A boolean value specifying whether or not to only
     * include selected graph elements in the {@link RecordStore}.
     * @param disassociateIds If true, the ids of the graph elements in the
     * created {@link RecordStore} will be distinct from the ids of the graph
     * elements on the graph.
     * @return A {@link RecordStore} representing the graph's vertices and
     * transactions.
     */
    public static GraphRecordStore getAll(final GraphReadMethods graph, final boolean selectedOnly, final boolean disassociateIds) {
        return getAll(graph, false, selectedOnly, disassociateIds);
    }

    /**
     * Populate a new {@link RecordStore} with the attribute values of vertices,
     * transactions and their endpoint vertices.
     *
     * @param graph A {@link GraphReadMethods} from which the
     * {@link RecordStore} will be created.
     * @param singletonsOnly Only include singleton vertices. This is useful
     * when building a RecordStore that reflects the graph structure.
     * @param selectedOnly A boolean value specifying whether or not to only
     * include selected graph elements in the {@link RecordStore}.
     * @param disassociateIds If true, the ids of the graph elements in the
     * created {@link RecordStore} will be distinct from the ids of the graph
     * elements on the graph.
     * @return A {@link RecordStore} representing the graph's vertices and
     * transactions.
     */
    public static GraphRecordStore getAll(final GraphReadMethods graph, final boolean singletonsOnly, 
            final boolean selectedOnly, final boolean disassociateIds) {
        final GraphRecordStore recordstore = getVertices(graph, singletonsOnly, selectedOnly, disassociateIds);
        recordstore.add(getTransactions(graph, selectedOnly, disassociateIds));
        return recordstore;
    }

    /**
     * Populate a new {@link RecordStore} with the attribute values of any
     * selected vertices, transactions and their endpoint vertices.
     *
     * @param graph A {@link GraphReadMethods} from which the
     * {@link RecordStore} will be created.
     * @return A {@link RecordStore} representing the graph's vertices and
     * transactions.
     */
    public static GraphRecordStore getAllSelected(final GraphReadMethods graph) {
        return getAll(graph, true, false);
    }

    /**
     * Populate a new {@link RecordStore} with the attribute values of the
     * vertices.
     *
     * @param graph A {@link GraphReadMethods} from which {@link RecordStore}
     * will be created.
     * @param selectedOnly A boolean value specifying whether or not to only
     * include selected graph elements in the {@link RecordStore}.
     * @param singletonsOnly Only include singleton vertices. This is useful
     * when building a RecordStore that reflects the graph structure.
     * @param disassociateIds If true, the ids of the graph elements in the
     * created {@link RecordStore} will be distinct from the ids of the graph
     * elements on the graph.
     * @return A {@link RecordStore} representing the graph's vertices.
     */
    public static GraphRecordStore getVertices(final GraphReadMethods graph, final boolean singletonsOnly, 
            final boolean selectedOnly, final boolean disassociateIds) {
        return getVertices(graph, singletonsOnly, selectedOnly, disassociateIds, new int[]{0}, -1);
    }

    /**
     * Populate a new {@link RecordStore} with the attribute values of the
     * vertices from the specified graph. This method also offers the ability to
     * start collecting vertices from the specified offset value, and to limit
     * to a specified number of results - essentially allowing paginated
     * queries.
     *
     * @param graph A {@link GraphReadMethods} from which {@link RecordStore}
     * will be created.
     * @param selectedOnly A boolean value specifying whether or not to only
     * include selected graph elements in the {@link RecordStore}.
     * @param singletonsOnly Only include singleton vertices. This is useful
     * when building a RecordStore that reflects the graph structure.
     * @param disassociateIds If true, the ids of the graph elements in the
     * created {@link RecordStore} will be distinct from the ids of the graph
     * elements on the graph.
     * @param offset An array of integers, where the zeroth value represents the
     * vertex position from which to begin collection.
     * @param limit An integer value representing the maximum number of vertices
     * to collect.
     * @return A {@link RecordStore} representing the graph's vertices.
     */
    public static GraphRecordStore getVertices(final GraphReadMethods graph, final boolean singletonsOnly,
            final boolean selectedOnly, final boolean disassociateIds, final int[] offset, final int limit) {
        final GraphRecordStore recordStore = new GraphRecordStore();

        final int attributeCount = graph.getAttributeCount(GraphElementType.VERTEX);
        final Attribute[] attributes = new Attribute[attributeCount];
        for (int a = 0; a < attributeCount; a++) {
            final int attributeId = graph.getAttribute(GraphElementType.VERTEX, a);
            attributes[a] = new GraphAttribute(graph, attributeId);
        }

        final int selected = graph.getAttribute(GraphElementType.VERTEX, SELECTED_ATTRIBUTE_NAME);
        final int vertexCount = graph.getVertexCount();
        boolean limitReached = false;
        for (int v = offset[0]; v < vertexCount; v++) {
            final int vxId = graph.getVertex(v);

            if ((!selectedOnly || graph.getBooleanValue(selected, vxId)) && (!singletonsOnly || graph.getVertexNeighbourCount(vxId) == 0)) {
                recordStore.add();

                for (final Attribute attribute : attributes) {
                    final String type = attribute.getAttributeType();
                    recordStore.set(SOURCE + attribute.getName() + "<" + type + ">", graph.getStringValue(attribute.getId(), vxId));
                }

                recordStore.set(SOURCE + ID, disassociateIds ? "id-" + vxId : String.valueOf(vxId));
                if (limit > 0 && recordStore.size() >= limit) {
                    offset[0] = v + 1;
                    limitReached = true;
                    break;
                }
            }
        }
        if (!limitReached) {
            offset[0] = graph.getVertexCount();
        }

        return recordStore;
    }

    /**
     * Populate a new {@link RecordStore} with the attribute values of the
     * selected vertices.
     *
     * @param graph A {@link GraphReadMethods} from whose vertices the
     * {@link RecordStore} will be created
     * @return A {@link RecordStore} representing the graph's selected vertices.
     */
    public static GraphRecordStore getSelectedVertices(final GraphReadMethods graph) {
        return getVertices(graph, false, true, false);
    }

    /**
     * Get all selected vertices from a graph in the form of a {@link List} of
     * {@link RecordStore} objects of the specified size.
     *
     * @param graph A {@link GraphReadMethods} from which the {@link List} of
     * {@link RecordStore} will be created.
     * @param batchSize An integer value representing the maximum size of each
     * {@link RecordStore}.
     * @return A {@link List} of {@link RecordStore} representing the selected
     * vertices on the specified graph or an empty list of no vertices where
     * selected.
     */
    public static List<GraphRecordStore> getSelectedVerticesBatches(final GraphReadMethods graph, final int batchSize) {
        final List<GraphRecordStore> batches = new ArrayList<>();

        final int[] offset = new int[]{0};
        while (offset[0] < graph.getVertexCount()) {
            batches.add(getVertices(graph, false, true, false, offset, batchSize));
        }
        return batches;
    }
    
    public static List<GraphRecordStore> getSelectedTransactionBatches(final GraphReadMethods graph, final int batchSize) {
        final List<GraphRecordStore> batches = new ArrayList<>();

        final int[] offset = new int[]{0};
        while (offset[0] < graph.getTransactionCount()) {
            batches.add(getTransactions(graph, true, false, offset, batchSize));
        }
        return batches;
    }

    /**
     * Puts all selected vertices in a {@link RecordStore} ready to be pasted
     * into another graph.
     * <p>
     * There are a few differences from {@link #getSelectedVertices} that are
     * specific to the requirement of copying to another graph:
     * <ul>
     * <li>Ids must not be valid graph ids otherwise they may clash with
     * elements on a destination graph</li>
     * <li>The keys for the attributes are suffixed with the attribute types as
     * the receiving graph may not have these attributes.</li>
     * <li>This method can be used when copying transactions to add 'ghost
     * vertices' to the {@link RecordStore}, that is vertices which are not
     * selected but where an incident transaction is. This allows selected
     * transaction to be pasted onto a destination graph if and only if the
     * endpoints which are not selected exist on that graph </li>
     * </ul>
     *
     * @param graph A {@link GraphReadMethods} from which the
     * {@link RecordStore} will be created.
     * @param recordStore A {@link RecordStore} to which the vertices will be
     * copied. If null, a new {@link RecordStore} will be created.
     * @param transactionEndPoints A {@link BitSet} containing ids of vertices
     * that are endpoints of selected transactions. When using this method to
     * purely copy vertices, this can be empty.
     * @return a {@link RecordStore} representing the selected vertices that can
     * be added to another graph. This will be the supplied {@link RecordStore}
     * if it was not null.
     */
    public static RecordStore copySelectedVertices(final GraphReadMethods graph, RecordStore recordStore, final BitSet transactionEndPoints) {
        if (recordStore == null) {
            recordStore = new GraphRecordStore();
        }
        final int attributeCount = graph.getAttributeCount(GraphElementType.VERTEX);
        final Attribute[] attributes = new Attribute[attributeCount];
        for (int a = 0; a < attributeCount; a++) {
            final int attributeId = graph.getAttribute(GraphElementType.VERTEX, a);
            attributes[a] = new GraphAttribute(graph, attributeId);
        }
        final int selected = graph.getAttribute(GraphElementType.VERTEX, SELECTED_ATTRIBUTE_NAME);
        final int vertexCount = graph.getVertexCount();
        final StringBuilder sb = new StringBuilder();
        for (int position = 0; position < vertexCount; position++) {
            final int vxId = graph.getVertex(position);

            if (graph.getBooleanValue(selected, vxId) || transactionEndPoints.get(vxId)) {
                recordStore.add();
                for (final Attribute attribute : attributes) {
                    sb.setLength(0);
                    sb.append(SOURCE)
                            .append(attribute.getName())
                            .append("<")
                            .append(attribute.getAttributeType())
                            .append(">");
                    recordStore.set(sb.toString(), graph.getStringValue(attribute.getId(), vxId));
                }
                recordStore.set(SOURCE + ID, COPY + String.valueOf(vxId));
            }
            if (transactionEndPoints.get(vxId) && !graph.getBooleanValue(selected, vxId)) {
                recordStore.set(SOURCE + GHOST, ""); // will be checked if it has a value to denote true.
            }
        }

        return recordStore;
    }

    /**
     * Copy the attribute values of a single graph vertex to a
     * {@link RecordStore}.
     *
     * @param graph A {@link GraphReadMethods} from which the RecordStore will
     * be created
     * @param recordStore A {@link RecordStore} to which the vertices will be
     * copied. If null, a new {@link RecordStore} will be created.
     * @param vxId The vertex id of the vertex being copied.
     * @param copiedId An array of {@link String} containing the id of the
     * vertex in the {@link RecordStore} in the zeroth position.
     * @return A {@link RecordStore} with the vertex copied to it. This will be
     * the supplied {@link RecordStore} if it was not null.
     */
    public static RecordStore copySpecifiedVertex(final GraphReadMethods graph, RecordStore recordStore, final int vxId, final String[] copiedId) {
        if (recordStore == null) {
            recordStore = new GraphRecordStore();
        }
        final int attributeCount = graph.getAttributeCount(GraphElementType.VERTEX);
        final Attribute[] attributes = new Attribute[attributeCount];
        for (int a = 0; a < attributeCount; a++) {
            final int attributeId = graph.getAttribute(GraphElementType.VERTEX, a);
            attributes[a] = new GraphAttribute(graph, attributeId);
        }

        final StringBuilder sb = new StringBuilder();

        recordStore.add();
        for (final Attribute attribute : attributes) {
            sb.setLength(0);
            sb.append(SOURCE)
                    .append(attribute.getName())
                    .append("<")
                    .append(attribute.getAttributeType())
                    .append(">");
            recordStore.set(sb.toString(), graph.getStringValue(attribute.getId(), vxId));
        }
        String copyId = "";
        for (int primarykeyAttr : graph.getPrimaryKey(GraphElementType.VERTEX)) {
            final String val = graph.getStringValue(primarykeyAttr, vxId);
            copyId += graph.getAttributeName(primarykeyAttr) + "<" + (StringUtils.defaultString(val)) + ">";
        }
        copiedId[0] = COPY + copyId;
        recordStore.set(SOURCE + ID, copiedId[0]);

        return recordStore;
    }

    /**
     * Populate a new {@link RecordStore} with the attribute values of the
     * transactions and their endpoint vertices.
     *
     * @param graph A {@link GraphReadMethods} from which the RecordStore will
     * be created.
     * @param selectedOnly A boolean value specifying whether or not to only
     * include selected transactions in the {@link RecordStore}.
     * @param disassociateIds If true, the ids of the transactions in the
     * created {@link RecordStore} will be distinct from the ids of the
     * transactions on the graph.
     * @return A {@link RecordStore} representing the graph's transactions.
     */
    public static GraphRecordStore getTransactions(final GraphReadMethods graph, final boolean selectedOnly, final boolean disassociateIds) {
        return getTransactions(graph, selectedOnly, disassociateIds, new int[]{0}, -1);
    }
    
    /**
     * Populate a new {@link RecordStore} with the attribute values of the
     * transactions and their endpoint vertices.
     *
     * @param graph A {@link GraphReadMethods} from which the RecordStore will
     * be created.
     * @param selectedOnly A boolean value specifying whether or not to only
     * include selected transactions in the {@link RecordStore}.
     * @param disassociateIds If true, the ids of the transactions in the
     * created {@link RecordStore} will be distinct from the ids of the
     * transactions on the graph.
     * @param offset An array of integers, where the zeroth value represents the 
     * transaction position from which to begin collection
     * @param limit An integer value representing the maximum number of transactions 
     * to collect.
     * @return A {@link RecordStore} representing the graph's transactions.
     */
    public static GraphRecordStore getTransactions(final GraphReadMethods graph, final boolean selectedOnly, final boolean disassociateIds, final int[] offset, final int limit) {
        final GraphRecordStore recordStore = new GraphRecordStore();

        final int transactionAttributeCount = graph.getAttributeCount(GraphElementType.TRANSACTION);
        final Attribute[] transactionAttributes = new Attribute[transactionAttributeCount];
        for (int a = 0; a < transactionAttributeCount; a++) {
            final int attributeId = graph.getAttribute(GraphElementType.TRANSACTION, a);
            transactionAttributes[a] = new GraphAttribute(graph, attributeId);
        }

        final int vertexAttributeCount = graph.getAttributeCount(GraphElementType.VERTEX);
        final Attribute[] vertexAttributes = new Attribute[vertexAttributeCount];
        for (int a = 0; a < vertexAttributeCount; a++) {
            final int attributeId = graph.getAttribute(GraphElementType.VERTEX, a);
            vertexAttributes[a] = new GraphAttribute(graph, attributeId);
        }

        final int selected = graph.getAttribute(GraphElementType.TRANSACTION, SELECTED_ATTRIBUTE_NAME);
        final int transactionCount = graph.getTransactionCount();
        boolean limitReached = false;
        for (int t = offset[0]; t < transactionCount; t++) {
            final int txId = graph.getTransaction(t);
            final int source = graph.getTransactionSourceVertex(txId);
            final int destination = graph.getTransactionDestinationVertex(txId);

            if (!selectedOnly || graph.getBooleanValue(selected, txId)) {
                recordStore.add();

                for (final Attribute transactionAttribute : transactionAttributes) {
                    final String type = transactionAttribute.getAttributeType();
                    recordStore.set(TRANSACTION + transactionAttribute.getName() + "<" + type + ">", graph.getStringValue(transactionAttribute.getId(), txId));
                }

                for (final Attribute vertexAttribute : vertexAttributes) {
                    final String type = vertexAttribute.getAttributeType();
                    recordStore.set(SOURCE + vertexAttribute.getName() + "<" + type + ">", graph.getStringValue(vertexAttribute.getId(), source));
                    recordStore.set(DESTINATION + vertexAttribute.getName() + "<" + type + ">", graph.getStringValue(vertexAttribute.getId(), destination));
                }
                if (graph.getTransactionDirection(txId) == Graph.UNDIRECTED) {
                    recordStore.set(TRANSACTION + DIRECTED_KEY, FALSE);
                }
                recordStore.set(TRANSACTION + ID, disassociateIds ? "id-" + txId : String.valueOf(txId));
                recordStore.set(SOURCE + ID, disassociateIds ? "id-" + source : String.valueOf(source));
                recordStore.set(DESTINATION + ID, disassociateIds ? "id-" + destination : String.valueOf(destination));
                
                if (limit > 0 && recordStore.size() >= limit) {
                    offset[0] = t + 1;
                    limitReached = true;
                    break;
                }
            }
        }
        
        if (!limitReached) {
            offset[0] = graph.getTransactionCount();
        }

        return recordStore;
    }

    /**
     * Populate a new {@link RecordStore} with the attribute values of the
     * selected transactions.
     *
     * @param graph A GraphReadMethods from which the RecordStore will be
     * created
     * @return A RecordStore representing the graph's selected transactions.
     */
    public static RecordStore getSelectedTransactions(final GraphReadMethods graph) {
        return getTransactions(graph, true, false);
    }

    /**
     * Puts all selected vertices in a {@link RecordStore} ready to be pasted
     * into another graph.
     * <p>
     * There are a few differences between this method and
     * {@link #getSelectedTransactions} which are specific to the requirement of
     * copying to another graph:
     * <ul>
     * <li>Ids must not be valid graph ids otherwise they may clash with
     * elements on a destination graph</li>
     * <li>The keys for the attributes are suffixed with the attribute types as
     * the receiving graph may not have these attributes.</li>
     * </ul>
     *
     * @param graph A {@link GraphReadMethods} from which the
     * {@link RecordStore} will be created.
     * @param recordStore A {@link RecordStore} to which the transactions will
     * be copied. If null, a new {@link RecordStore} will be created.
     * @param vertices A {@link BitSet} with capacity equivalent to the number
     * of vertex ids. This will have bits set for all ids of the endpoints of
     * the copied transactions.
     * @return a {@link RecordStore} representing the selected transactions that
     * can be added to another graph. This will be the supplied
     * {@link RecordStore} if it was not null.
     */
    public static RecordStore copySelectedTransactions(final GraphReadMethods graph, RecordStore recordStore, final BitSet vertices) {
        if (recordStore == null) {
            recordStore = new GraphRecordStore();
        }
        final int transactionAttributeCount = graph.getAttributeCount(GraphElementType.TRANSACTION);
        final Attribute[] transactionAttributes = new Attribute[transactionAttributeCount];
        for (int a = 0; a < transactionAttributeCount; a++) {
            int attributeId = graph.getAttribute(GraphElementType.TRANSACTION, a);
            transactionAttributes[a] = new GraphAttribute(graph, attributeId);
        }

        final int selected = graph.getAttribute(GraphElementType.TRANSACTION, SELECTED_ATTRIBUTE_NAME);
        final int transactionCount = graph.getTransactionCount();
        for (int t = 0; t < transactionCount; t++) {
            final int transaction = graph.getTransaction(t);

            if (graph.getBooleanValue(selected, transaction)) {
                recordStore.add();

                final int source = graph.getTransactionSourceVertex(transaction);
                final int destination = graph.getTransactionDestinationVertex(transaction);

                for (final Attribute transactionAttribute : transactionAttributes) {
                    recordStore.set(TRANSACTION + transactionAttribute.getName() + "<" + transactionAttribute.getAttributeType() + ">", graph.getStringValue(transactionAttribute.getId(), transaction));
                }

                if (graph.getTransactionDirection(transaction) == Graph.UNDIRECTED) {
                    recordStore.set(TRANSACTION + DIRECTED_KEY, FALSE);
                }
                recordStore.set(TRANSACTION + ID, COPY + String.valueOf(transaction));
                recordStore.set(SOURCE + ID, COPY + String.valueOf(source));
                recordStore.set(DESTINATION + ID, COPY + String.valueOf(destination));

                vertices.set(source);
                vertices.set(destination);
            }
        }

        return recordStore;
    }

    /**
     * Takes transactions connected to a composite and adds to a
     * {@link RecordStore} copies of the corresponding transactions connected
     * instead to the composite's constituents. This is used when a composite
     * node is expanded.
     *
     * @param graph A {@link GraphReadMethods} from which the
     * {@link RecordStore} will be created.
     * @param recordStore The {@link RecordStore} to add the transactions to.
     * @param compositeVxId The vertex id of the composite node to copy
     * transactions from.
     * @param compositeStoredId The id of the composite node to be used in the
     * {@link RecordStore}.
     * @param toIds A list of the ids of the composite constituents to be used
     * in the {@link RecordStore}.
     * @param uniqueIdAttr The id of uniqueId attribute for transactions. This
     * is required as the uniqueId attribute is used to hold information about
     * the original source and destination vertices of transactions connected to
     * composite nodes.
     */
    public static void copyTransactionsFromComposite(final GraphReadMethods graph, final RecordStore recordStore,
            final int compositeVxId, final String compositeStoredId, final List<String> toIds, final Attribute uniqueIdAttr) {
        final int transactionAttributeCount = graph.getAttributeCount(GraphElementType.TRANSACTION);
        final Attribute[] transactionAttributes = new Attribute[transactionAttributeCount];
        for (int a = 0; a < transactionAttributeCount; a++) {
            final int attributeId = graph.getAttribute(GraphElementType.TRANSACTION, a);
            transactionAttributes[a] = new GraphAttribute(graph, attributeId);
        }

        final int transactionCount = graph.getVertexTransactionCount(compositeVxId);
        for (int t = 0; t < transactionCount; t++) {
            final int transaction = graph.getVertexTransaction(compositeVxId, t);
            final int source = graph.getTransactionSourceVertex(transaction);
            final int destination = graph.getTransactionDestinationVertex(transaction);
            final CompositeTransactionId compositeTransactionId = CompositeTransactionId.fromString(graph.getStringValue(uniqueIdAttr.getId(), transaction));

            String sourceId = null;
            String destId = null;
            final String uniqueId;
            if (compositeVxId == source) {
                destId = String.valueOf(destination);
                if (compositeTransactionId.isSourceContracted() && compositeTransactionId.getOriginalSourceNode() != null) {
                    sourceId = compositeTransactionId.getOriginalSourceNode();
                    compositeTransactionId.setOriginalSourceNode(null);
                    uniqueId = compositeTransactionId.toString();
                } else {
                    compositeTransactionId.setSourceContracted(false);
                    compositeTransactionId.setOriginalSourceNode(compositeStoredId);
                    uniqueId = compositeTransactionId.toString();
                }
            } else {
                sourceId = String.valueOf(source);
                if (compositeTransactionId.isDestContracted() && compositeTransactionId.getOriginalDestinationNode() != null) {
                    destId = compositeTransactionId.getOriginalDestinationNode();
                    compositeTransactionId.setOriginalDestinationNode(null);
                    uniqueId = compositeTransactionId.toString();
                } else {
                    compositeTransactionId.setDestContracted(false);
                    compositeTransactionId.setOriginalDestinationNode(compositeStoredId);
                    uniqueId = compositeTransactionId.toString();
                }
            }

            final List<String> sourceIds = sourceId == null ? toIds : Arrays.asList(sourceId);
            final List<String> destIds = destId == null ? toIds : Arrays.asList(destId);

            for (final String srcId : sourceIds) {
                for (final String dstId : destIds) {
                    recordStore.add();
                    for (final Attribute transactionAttribute : transactionAttributes) {
                        final String value = transactionAttribute.getName().equals(uniqueIdAttr.getName()) ? uniqueId : graph.getStringValue(transactionAttribute.getId(), transaction);
                        recordStore.set(TRANSACTION + transactionAttribute.getName() + "<" + transactionAttribute.getAttributeType() + ">", value);
                    }

                    if (graph.getTransactionDirection(transaction) == Graph.UNDIRECTED) {
                        recordStore.set(TRANSACTION + DIRECTED_KEY, FALSE);
                    }
                    recordStore.set(TRANSACTION + ID, COPY + String.format(NUMBER_STRING_STRING_FORMAT, transaction, srcId, dstId));
                    recordStore.set(SOURCE + ID, srcId);
                    recordStore.set(DESTINATION + ID, dstId);
                }
            }
        }
    }

    /**
     * Takes transactions connected to the constituent of an expanded composite
     * and adds to a {@link RecordStore} copies of the corresponding
     * transactions connected instead to the composite itself. This is used when
     * a composite node is contracted.
     *
     * @param graph A {@link GraphReadMethods} from which the
     * {@link RecordStore} will be created.
     * @param recordStore The {@link RecordStore} to add the transactions to.
     * @param expandedVxId The vertex id of the composite constituent to copy
     * transactions from.
     * @param expandedId The id of the composite constituent to be used in the
     * {@link RecordStore}.
     * @param expandedIds The set of expandedIds.
     * @param toId The id of the composite node to be used in the
     * {@link RecordStore}.
     * @param uniqueIdAttr The id of uniqueId attribute for transactions. This
     * is required as the uniqueId attribute is used to hold information about
     * the original source and destination vertices of transactions connected to
     * composite nodes.
     */
    public static void copyTransactionsToComposite(final GraphReadMethods graph, final RecordStore recordStore,
            final int expandedVxId, final String expandedId, final Set<Integer> expandedIds, final String toId, final Attribute uniqueIdAttr) {
        final int transactionAttributeCount = graph.getAttributeCount(GraphElementType.TRANSACTION);
        final Attribute[] transactionAttributes = new Attribute[transactionAttributeCount];
        for (int a = 0; a < transactionAttributeCount; a++) {
            final int attributeId = graph.getAttribute(GraphElementType.TRANSACTION, a);
            transactionAttributes[a] = new GraphAttribute(graph, attributeId);
        }

        final int transactionCount = graph.getVertexTransactionCount(expandedVxId);
        for (int t = 0; t < transactionCount; t++) {
            final int transaction = graph.getVertexTransaction(expandedVxId, t);
            final int source = graph.getTransactionSourceVertex(transaction);
            final int destination = graph.getTransactionDestinationVertex(transaction);

            // Don't add to a composite those transactions which are between two constituents of the composite!
            if (expandedIds.contains(source) && expandedIds.contains(destination)) {
                continue;
            }
            final CompositeTransactionId compositeTransactionId = CompositeTransactionId.fromString(graph.getStringValue(uniqueIdAttr.getId(), transaction));

            String sourceId = null;
            String destId = null;
            final String uniqueId;
            if (expandedVxId == source) {
                destId = String.valueOf(destination);
                if (!compositeTransactionId.isSourceContracted() && compositeTransactionId.getOriginalSourceNode() != null) {
                    sourceId = compositeTransactionId.getOriginalSourceNode();
                    compositeTransactionId.setOriginalSourceNode(null);
                    uniqueId = compositeTransactionId.toString();
                } else {
                    sourceId = toId;
                    compositeTransactionId.setSourceContracted(true);
                    compositeTransactionId.setOriginalSourceNode(expandedId);
                    uniqueId = compositeTransactionId.toString();
                }
            } else {
                sourceId = String.valueOf(source);
                if (!compositeTransactionId.isDestContracted() && compositeTransactionId.getOriginalDestinationNode() != null) {
                    destId = compositeTransactionId.getOriginalDestinationNode();
                    compositeTransactionId.setOriginalDestinationNode(null);
                    uniqueId = compositeTransactionId.toString();
                } else {
                    destId = toId;
                    compositeTransactionId.setDestContracted(true);
                    compositeTransactionId.setOriginalDestinationNode(expandedId);
                    uniqueId = compositeTransactionId.toString();
                }
            }

            recordStore.add();
            for (final Attribute transactionAttribute : transactionAttributes) {
                final String value = transactionAttribute.getName().equals(uniqueIdAttr.getName()) ? uniqueId : graph.getStringValue(transactionAttribute.getId(), transaction);
                recordStore.set(TRANSACTION + transactionAttribute.getName() + "<" + transactionAttribute.getAttributeType() + ">", value);
            }

            if (graph.getTransactionDirection(transaction) == Graph.UNDIRECTED) {
                recordStore.set(TRANSACTION + DIRECTED_KEY, FALSE);
            }
            recordStore.set(TRANSACTION + ID, COPY + String.format(NUMBER_STRING_STRING_FORMAT, transaction, sourceId, destId));
            recordStore.set(SOURCE + ID, sourceId);
            recordStore.set(DESTINATION + ID, destId);
        }
    }

    /**
     * Take transactions between two constituents of an expanded composite and
     * add to a {@link RecordStore} copies of these transactions. This is used
     * when a composite node is contracted so that these transactions can be *
     * added back when it is expanded.
     *
     * @param graph A {@link GraphReadMethods} from which the
     * {@link RecordStore} will be created.
     * @param recordStore The {@link RecordStore} to add the transactions to.
     * @param fromVxId The vertex id of the first composite constituent.
     * @param toVxId The vertex id of the second composite constituent.
     * @param fromId The id of the first composite constituent to be used in the
     * {@link RecordStore}.
     * @param toId The id of the first composite constituent to be used in the
     * {@link RecordStore}.
     */
    public static void copyTransactionsBetweenVertices(final GraphReadMethods graph, final RecordStore recordStore,
            final int fromVxId, final int toVxId, final String fromId, final String toId) {
        final int transactionAttributeCount = graph.getAttributeCount(GraphElementType.TRANSACTION);
        final Attribute[] transactionAttributes = new Attribute[transactionAttributeCount];
        for (int a = 0; a < transactionAttributeCount; a++) {
            int attributeId = graph.getAttribute(GraphElementType.TRANSACTION, a);
            transactionAttributes[a] = new GraphAttribute(graph, attributeId);
        }
        final int lxId = graph.getLink(fromVxId, toVxId);
        if (lxId != Graph.NOT_FOUND) {
            final int transactionCount = graph.getLinkTransactionCount(lxId);
            for (int t = 0; t < transactionCount; t++) {
                final int transaction = graph.getLinkTransaction(lxId, t);
                final int source = graph.getTransactionSourceVertex(transaction);
                final String sourceId = source == fromVxId ? fromId : toId;
                final String destId = source == fromVxId ? toId : fromId;
                recordStore.add();
                for (final Attribute transactionAttribute : transactionAttributes) {
                    final String value = graph.getStringValue(transactionAttribute.getId(), transaction);
                    recordStore.set(TRANSACTION + transactionAttribute.getName() + "<" + transactionAttribute.getAttributeType() + ">", value);
                }

                if (graph.getTransactionDirection(transaction) == Graph.UNDIRECTED) {
                    recordStore.set(TRANSACTION + DIRECTED_KEY, FALSE);
                }
                recordStore.set(TRANSACTION + ID, COPY + String.format(NUMBER_STRING_STRING_FORMAT, transaction, sourceId, destId));
                recordStore.set(SOURCE + ID, sourceId);
                recordStore.set(DESTINATION + ID, destId);
            }
        }
    }

    /**
     * Copy the values of all primary keys for a source vertex (or a vertex not
     * associated with a transaction) in the specified graph to the specified
     * {@link RecordStore}.
     *
     * @param graph The {@link GraphReadMethods} from which to copy the primary
     * keys.
     * @param recordStore The {@link RecordStore} to copy the primary keys into.
     * @param vertex An integer value representing the id of a vertex from which
     * to copy primary key values.
     */
    public static void setSourceKeys(final GraphReadMethods graph, final RecordStore recordStore, final int vertex) {
        for (final int key : graph.getPrimaryKey(GraphElementType.VERTEX)) {
            recordStore.set(SOURCE + graph.getAttributeName(key), graph.getStringValue(key, vertex));
        }
    }

    /**
     * Copy the values of all primary keys for a destination vertex in the
     * specified graph to the specified {@link RecordStore}.
     *
     * @param graph The {@link GraphReadMethods} from which to copy the primary
     * keys.
     * @param recordStore The {@link RecordStore} to copy the primary keys into.
     * @param vertex An integer value representing the id of a vertex from which
     * to copy primary key values.
     */
    public static void setDestinationKeys(final GraphReadMethods graph, final RecordStore recordStore, final int vertex) {
        for (final int key : graph.getPrimaryKey(GraphElementType.VERTEX)) {
            recordStore.set(DESTINATION + graph.getAttributeName(key), graph.getStringValue(key, vertex));
        }
    }
    
    /**
     * Normalize a type to existing schema types based on case sensitivity
     * e.g. person, PERSON, Person, will all normalize to the schema Type Person.
     * @param vxLabel The {@link String} representing the complete label of a vertex
     * e.g. def@example2.com&lt;email&gt;.
     * @return The normalized type e.g. Email
     */

    private static String normalizeType(final String vxLabel) {
        final String[] parts = vxLabel.split("<");
        final String type = parts.length != 2 ? parts[0] : parts[1].substring(0, parts[1].length()-1);
        // Identify a type that is spelt the same regardless of case.
        if (ApprovedTypes.indexOf(type) == -1) {
            final Optional<String> foundType = ApprovedTypes.stream().filter(i -> i.equalsIgnoreCase(type)).findFirst();
            if (foundType.isPresent()) {
                return parts.length != 2 ? foundType.get() : parts[0] + "<"+ foundType.get()+">";
            }
        }
        return vxLabel;
    }

    /**
     * Copy the values of all primary keys for a specified transaction (and its
     * endpoint vertices) in the specified graph to the specified
     * {@link RecordStore}.
     *
     * @param graph The {@link GraphReadMethods} from which to copy the primary
     * keys.
     * @param recordStore The {@link RecordStore} to copy the primary keys into.
     * @param transaction An integer value representing the id of a transaction
     * from which to copy primary key values.
     */
    public static void setTransactionKeys(final GraphReadMethods graph, final RecordStore recordStore, final int transaction) {
        setSourceKeys(graph, recordStore, graph.getTransactionSourceVertex(transaction));
        setDestinationKeys(graph, recordStore, graph.getTransactionDestinationVertex(transaction));
        for (final int key : graph.getPrimaryKey(GraphElementType.TRANSACTION)) {
            recordStore.set(TRANSACTION + graph.getAttributeName(key), graph.getStringValue(key, transaction));
        }
    }

    /**
     * Set the transaction described by the current {@link Record} as
     * undirected. This is useful as no attribute exists which specifies the
     * direction of a transaction, and by default all transactions described by
     * a {@link Record} in a {@link RecordStore} are directed.
     *
     * @param recordStore The {@link RecordStore} in which to set an undirected
     * transaction.
     */
    public static void setUndirected(final RecordStore recordStore) {
        recordStore.set(TRANSACTION + DIRECTED_KEY, FALSE);
    }

    /**
     * Convert a JSON document into a {@link RecordStore}.
     * <p>
     * The JSON document is a simplification of the graph file format:
     * <pre>
     * {
     *  "vertex" : [ {name:value, ...}, ...],
     *  "transaction" : [{name:value, ...}, ...]
     * }
     * </pre> Each vertex may have the integer attribute "vx_id_" to specify a
     * vertex id to be referenced by transactions.
     * <p>
     * Each transaction may have the integer attributes "vx_src_" and "vx_dst"
     * to reference a "vx_id_", and a boolean "tx_dir_" attribute that specifies
     * if a transaction is directed.
     *
     * @param json The {@link String} representing a JSON document to be parsed
     * into a {@link RecordStore}.
     * @return A {@link RecordStore} derived from the specified JSON document.
     * @throws java.io.IOException If something goes wrong while reading the
     * JSON document.
     */
    public static GraphRecordStore fromJson(final String json) throws IOException {
        final String VX = "vertex";
        final String VXID = "vx_id_";
        final String TX = "transaction";
        final String TX_SRC = "vx_src_";
        final String TX_DST = "vx_dst_";
        final String TX_DIR = "tx_dir_";

        final GraphRecordStore rs = new GraphRecordStore();

        final ObjectMapper om = new ObjectMapper();

        final Map<?, ?> read = om.readValue(json, Map.class);
        if (read.containsKey(VX)) {
            @SuppressWarnings("unchecked") //vertices will list of maps of string to object
            final List<Map<String, Object>> vertices = (List<Map<String, Object>>) read.get(VX);
            for (final Map<String, Object> vertex : vertices) {
                rs.add();
                vertex.entrySet().stream().forEach(entry -> {
                    final String ekey = entry.getKey();
                    final Object evalue = entry.getValue();
                    final String key = VXID.equals(ekey) ? GraphRecordStoreUtilities.ID : ekey;
                    rs.set(SOURCE + key, evalue != null ? evalue.toString() : null);
                });
            }
        }

        if (read.containsKey(TX)) {
            @SuppressWarnings("unchecked") //transactions will be a list of maps of string to object
            final List<Map<String, Object>> transactions = (List<Map<String, Object>>) read.get(TX);
            for (final Map<String, Object> transaction : transactions) {
                rs.add();
                transaction.entrySet().stream().forEach(entry -> {
                    final String ekey = entry.getKey();
                    final Object evalue = entry.getValue();
                    final String key = switch (ekey) {
                        case TX_SRC -> SOURCE + ID;
                        case TX_DST -> DESTINATION + ID;
                        case TX_DIR -> TRANSACTION + DIRECTED_KEY;
                        case null -> TRANSACTION + ekey;
                        default -> TRANSACTION + ekey;
                    };
                    rs.set(key, evalue != null ? evalue.toString() : null);
                });
            }
        }

        return rs;
    }
}
