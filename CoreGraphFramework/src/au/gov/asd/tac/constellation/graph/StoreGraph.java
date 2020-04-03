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
package au.gov.asd.tac.constellation.graph;

import static au.gov.asd.tac.constellation.graph.GraphConstants.NOT_FOUND;
import au.gov.asd.tac.constellation.graph.NativeAttributeType.NativeValue;
import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.AttributeRegistry;
import au.gov.asd.tac.constellation.graph.locking.GraphOperationMode;
import au.gov.asd.tac.constellation.graph.locking.LockingTarget;
import au.gov.asd.tac.constellation.graph.locking.ParameterReadAccess;
import au.gov.asd.tac.constellation.graph.locking.ParameterWriteAccess;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.undo.GraphEdit;
import au.gov.asd.tac.constellation.graph.utilities.MultiValueStore;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.memory.MemoryManager;
import au.gov.asd.tac.constellation.utilities.postfix.PostfixEvaluator;
import au.gov.asd.tac.constellation.utilities.postfix.ShuntingYard;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

enum Operator {
    EQUALS,
    NOTEQUALS,
    GREATERTHAN,
    LESSTHAN,
    GREATERTHANOREQ,
    LESSTHANOREQ,
    NOTFOUND
}

/**
 * A StoreGraph is an array-based implementation of GraphWriteMethods designed
 * for performance and memory efficiency. It is currently the default
 * implementation used in Constellation.
 *
 * @author sirius
 */
public class StoreGraph extends LockingTarget implements GraphWriteMethods, Serializable {

    private boolean avoidUpdate = false;
    private static final String SELECTED_FILTERMASK_ATTRIBUTE_LABEL = "selected_bitmask";
    private static final String FILTERMASK_ATTRIBUTE_LABEL = "layer_mask";
    private static final String FILTERVISIBILITY_ATTRIBUTE_LABEL = "layer_visibility";
    private static final int HIGH_BIT = 0x80000000;
    private static final int LOW_BITS = 0x7FFFFFFF;
    private static final int[] CATEGORY_TO_STATE = new int[]{6, 4, 5, 1, 3, 7, 2};
    private static final int[] STATE_TO_CATEGORY = new int[]{-1, 3, 6, 4, 1, 2, 0, 5};
    private static final int INCOMING_UNDIRECTED_6 = 0;
    private static final int UNDIRECTED_4 = 1;
    private static final int OUTGOING_UNDIRECTED_5 = 2;
    private static final int OUTGOING_1 = 3;
    private static final int OUTGOING_INCOMING_3 = 4;
    private static final int ALL_7 = 5;
    private static final int INCOMING_2 = 6;
    private final ElementStore vStore;
    private final ElementStore lStore;
    private final ElementStore eStore;
    private final ElementStore tStore;
    private final ElementStore aStore;
    private final ListStore vertexLinks;
    private final ListStore linkTransactions;
    private final ListStore vertexTransactions;
    private final ListStore linkEdges;
    private final ListStore vertexEdges;
    private final ListStore typeAttributes;
    private int linkHashLength;
    private int linkHashMask;
    private int[] linkHash;
    private int[] linkNext;
    private int[] linkPrev;
    private AttributeDescription[] attributeDescriptions;
    protected GraphAttribute[] attributes;
    private GraphIndexType[] attributeIndexTypes;
    private GraphIndex[] attributeIndices;
    private final Map<String, int[]> attributeNames;
    private long[] attributeModificationCounters;
    private AttributeRegistry attributeRegistry;
    private long globalModificationCounter = 0;
    private long attributeModificationCounter = 0;
    private long structureModificationCounter = 0;
    private long lastFiredModificationCount = Long.MIN_VALUE;
    protected final int[][] primaryKeys;
    private int[] primaryKeyLookup;
    private final ElementKeySet[] primaryKeyIndices;
    private final String id;
    private final Schema schema;
    private final MultiValueStore valueStore = new MultiValueStore();
    private ElementList[] removedFromKeys = new ElementList[GraphElementType.values().length];
    private final GraphElementMerger graphElementMerger;
    private NativeValue oldValue = new NativeValue();
    private NativeValue newValue = new NativeValue();
    private GraphEdit graphEdit;
    private int selectedFilterBitmaskAttrId = -1;
    private int vertexFilterBitmaskAttrId = -1;
    private int transactionFilterBitmaskAttrId = -1;
    private int vertexFilterVisibilityAttrId = -1;
    private int transactionFilterVisibilityAttrId = -1;
    private int cameraAttrId = -1;
    private int vSelectedAttrId = -1;
    private int tSelectedAttrId = -1;
    private int vXAttrId = -1;
    private int vYAttrId = -1;
    private int vZAttrId = -1;
    private int tXAttrId = -1;
    private int tYAttrId = -1;
    private int tZAttrId = -1;

    // TODO: TEMP VARIABLE to test updates - This will have to be migrated to a graph attribute
    public static int currentVisibleMask = 1;

    // 0000
    // first two bits are not used XX00
    // second bit from right is boolean for if you have to display the layer 00X0
    // last bit on right is whether it is a dynamic layer 000X
    private final List<Byte> layerPrefs = new ArrayList<>();
    public final List<String> LAYER_QUERIES = new ArrayList<>();

    /**
     * Creates a new StoreGraph with the specified capacities.
     *
     * @param vertexCapacity the initial number of vertices the graph can hold.
     * @param linkCapacity the initial number of links the graph can hold.
     * @param edgeCapacity the initial number of edges the graph can hold.
     * @param transactionCapacity the initial number of transactions the graph
     * can hold.
     * @param attributeCapacity the initial number of attributes the graph can
     * hold.
     */
    public StoreGraph(final int vertexCapacity, final int linkCapacity, final int edgeCapacity, final int transactionCapacity, final int attributeCapacity) {
        this(UUID.randomUUID().toString(), null, vertexCapacity, linkCapacity, edgeCapacity, transactionCapacity, attributeCapacity);
    }

    @Override
    public void setGraphEdit(GraphEdit graphEdit) {
        this.graphEdit = graphEdit;
    }

    @Override
    public boolean isRecordingEdit() {
        return graphEdit != null;
    }
    
    @Override
    public void setLayerQueries(final List<String> queries) {
        int count = 0;
        for(String query : queries){
            //if old query was not null (dynamic), and old query is different from new (query updated)
            if(count < LAYER_QUERIES.size() && LAYER_QUERIES.get(count) != null && !LAYER_QUERIES.get(count).equals(" ")&& !LAYER_QUERIES.get(count).equals(query)){
                // iterate all elements, remove that mask
                // Loop through all vertexes and remove bitmasks
                final int vertexCount = getVertexCount();
                for (int i = 0; i < vertexCount; i++) {
                    int bitmask = getIntValue(vertexFilterBitmaskAttrId, i);
                    bitmask = bitmask & ~(1 << count); // set bit to false
                    setIntValue(vertexFilterBitmaskAttrId, i, bitmask);
                }

                // Loop through all transactions and remove bitmasks
                final int transactionCount = getTransactionCount();
                for (int i = 0; i < transactionCount; i++) {
                    int bitmask = getIntValue(transactionFilterBitmaskAttrId, i);
                    bitmask = bitmask & ~(1 << count); // set bit to false
                    setIntValue(transactionFilterBitmaskAttrId, i, bitmask);
                }
            }
            count++;
        }
        LAYER_QUERIES.clear();
        LAYER_QUERIES.addAll(queries);
    }

    /**
     * Construct a new StoreGraph.
     * <p>
     * The capacity of each store will be adjusted up automatically when
     * necessary.
     *
     * @param id the id for this StoreGraph.
     * @param schema the schema for this StoreGraph.
     * @param vertexCapacity The initial capacity of the vertex store.
     * @param linkCapacity The initial capacity of the link store.
     * @param edgeCapacity The initial capacity of the edge store.
     * @param transactionCapacity The initial capacity of the transaction store.
     * @param attributeCapacity The initial capacity of the attribute store.
     */
    public StoreGraph(final String id, final Schema schema, int vertexCapacity, int linkCapacity, int edgeCapacity, int transactionCapacity, int attributeCapacity) {
        this.id = id;
        this.schema = schema;

        vertexCapacity = powerOf2(vertexCapacity);
        linkCapacity = powerOf2(linkCapacity);
        edgeCapacity = powerOf2(edgeCapacity);
        transactionCapacity = powerOf2(transactionCapacity);
        attributeCapacity = powerOf2(attributeCapacity);

        vStore = new ElementStore(vertexCapacity);
        lStore = new ElementStore(linkCapacity);
        eStore = new ElementStore(edgeCapacity);
        tStore = new ElementStore(transactionCapacity);
        aStore = new ElementStore(attributeCapacity);

        vertexLinks = new ListStore(7, vertexCapacity, linkCapacity * 2);
        vertexTransactions = new ListStore(3, vertexCapacity, transactionCapacity * 2);
        linkTransactions = new ListStore(3, linkCapacity, transactionCapacity);
        typeAttributes = new ListStore(1, GraphElementType.values().length, attributeCapacity);
        vertexEdges = new ListStore(3, vertexCapacity, edgeCapacity * 2);
        linkEdges = new ListStore(3, linkCapacity, edgeCapacity);

        attributeRegistry = AttributeRegistry.getDefault();

        linkHashLength = 1;
        while (linkHashLength < linkCapacity) {
            linkHashLength <<= 1;
        }
        linkHashMask = linkHashLength - 1;

        linkHash = new int[linkHashLength];
        Arrays.fill(linkHash, NOT_FOUND);
        linkNext = new int[linkCapacity];
        linkPrev = new int[linkCapacity];

        attributeDescriptions = new AttributeDescription[attributeCapacity];
        attributes = new GraphAttribute[attributeCapacity];
        attributeNames = new HashMap<>();
        attributeModificationCounters = new long[attributeCapacity];

        attributeIndices = new GraphIndex[attributeCapacity];
        Arrays.fill(attributeIndices, AttributeDescription.NULL_GRAPH_INDEX);

        attributeIndexTypes = new GraphIndexType[attributeCapacity];
        Arrays.fill(attributeIndexTypes, GraphIndexType.NONE);

        primaryKeys = new int[GraphElementType.values().length][0];
        primaryKeyLookup = new int[attributeCapacity];
        primaryKeyIndices = new ElementKeySet[GraphElementType.values().length];
        Arrays.fill(primaryKeyLookup, -1);

        graphElementMerger = schema == null ? null : schema.getFactory().getGraphElementMerger();

        MemoryManager.newObject(StoreGraph.class);
    }

    @Override
    public void finalize() {
        MemoryManager.finalizeObject(StoreGraph.class);
    }

    private static int powerOf2(int capacity) {
        int c = 2;
        while (c < capacity) {
            c <<= 1;
        }
        return c;
    }

    /**
     * Creates a new StoreGraph with default capacities and a unique, random id.
     */
    public StoreGraph() {
        this(UUID.randomUUID().toString());
    }

    /**
     * Creates new StoreGraph with default capacities and the specified id.
     *
     * @param id the id for this StoreGraph.
     */
    public StoreGraph(final String id) {
        this(id, null, 64, 256, 256, 256, 16);
    }

    /**
     * Creates a new StoreGraph with default capacities, and a specified id and
     * schema.
     *
     * @param schema the schema for the new StoreGraph.
     * @param id the id for the new StoreGraph.
     */
    public StoreGraph(final Schema schema, String id) {
        this(id, schema, 64, 256, 256, 256, 16);
    }

    /**
     * Creates a new StoreGraph with a random, unique id, a specified schema and
     * specified capacities.
     *
     * @param schema the schema for this graph.
     */
    public StoreGraph(Schema schema) {
        this(UUID.randomUUID().toString(), schema, 64, 256, 256, 256, 16);
    }

    /**
     * Creates a new StoreGraph that is a copy of the original StoreGraph.
     *
     * @param original the original StoreGraph to copy.
     */
    public StoreGraph(final StoreGraph original) {
        this(original, false);
    }

    /**
     * Creates a new StoreGraph that is a copy of the original StoreGraph with
     * the option of creating a new id.
     *
     * @param original the original StoreGraph to be copied.
     * @param newId should a new id be created for this StoreGraph.
     */
    public StoreGraph(final StoreGraph original, boolean newId) {
        this(newId ? UUID.randomUUID().toString() : original.getId(), original.getSchema() == null ? null : original.getSchema().getFactory().createSchema(), original);
    }

    /**
     * Creates a new StoreGraph this is a copy of the original StoreGraph with
     * the option of creating a new id.
     *
     * @param original the original StoreGraph to be copied.
     * @param id the id for the new StoreGraph (if null then the id is copied
     * from the original StoreGraph)
     */
    public StoreGraph(final StoreGraph original, String id) {
        this(id == null ? original.getId() : id, original.getSchema() == null ? null : original.getSchema().getFactory().createSchema(), original);
    }

    /**
     * Creates a new StoreGraph that is a copy of the original StoreGraph but
     * with the specified id and schema.
     *
     * @param id the id of this StoreGraph.
     * @param schema the Schema for this StoreGraph.
     * @param original the original StoreGraph to copy.
     */
    public StoreGraph(final String id, Schema schema, final StoreGraph original) {
        this.id = id;
        this.schema = schema;

        this.vStore = new ElementStore(original.vStore);
        this.lStore = new ElementStore(original.lStore);
        this.eStore = new ElementStore(original.eStore);
        this.tStore = new ElementStore(original.tStore);
        this.aStore = new ElementStore(original.aStore);

        this.vertexLinks = new ListStore(original.vertexLinks);
        this.vertexTransactions = new ListStore(original.vertexTransactions);
        this.linkTransactions = new ListStore(original.linkTransactions);
        this.typeAttributes = new ListStore(original.typeAttributes);
        this.vertexEdges = new ListStore(original.vertexEdges);
        this.linkEdges = new ListStore(original.linkEdges);

        this.attributeRegistry = original.attributeRegistry;

        this.linkHashLength = original.linkHashLength;
        this.linkHashMask = original.linkHashMask;
        this.linkHash = Arrays.copyOf(original.linkHash, original.linkHash.length);
        this.linkNext = Arrays.copyOf(original.linkNext, original.linkNext.length);
        this.linkPrev = Arrays.copyOf(original.linkPrev, original.linkPrev.length);

        this.globalModificationCounter = original.globalModificationCounter;
        this.attributeModificationCounter = original.attributeModificationCounter;
        this.structureModificationCounter = original.structureModificationCounter;

        this.lastFiredModificationCount = original.lastFiredModificationCount;

        this.attributeDescriptions = new AttributeDescription[original.attributeDescriptions.length];
        for (int i = 0; i < attributeDescriptions.length; i++) {
            if (original.attributeDescriptions[i] != null) {
                attributeDescriptions[i] = original.attributeDescriptions[i].copy(this);
            }
        }

        this.attributes = new GraphAttribute[original.attributes.length];
        for (int i = 0; i < this.attributes.length; i++) {
            final GraphAttribute ia = original.attributes[i];
            this.attributes[i] = ia != null ? new GraphAttribute(ia) : null;
        }

        attributeNames = new HashMap<>();
        for (Entry<String, int[]> e : original.attributeNames.entrySet()) {
            this.attributeNames.put(e.getKey(), Arrays.copyOf(e.getValue(), e.getValue().length));
        }
        this.attributeModificationCounters = Arrays.copyOf(original.attributeModificationCounters, original.attributeModificationCounters.length);

        this.primaryKeys = new int[original.primaryKeys.length][];
        for (int i = 0; i < this.primaryKeys.length; i++) {
            this.primaryKeys[i] = Arrays.copyOf(original.primaryKeys[i], original.primaryKeys[i].length);
        }

        this.primaryKeyLookup = Arrays.copyOf(original.primaryKeyLookup, original.primaryKeyLookup.length);
        this.primaryKeyIndices = new ElementKeySet[original.primaryKeyIndices.length];
        for (int i = 0; i < this.primaryKeyIndices.length; i++) {
            final ElementKeySet ks = original.primaryKeyIndices[i];
            if (ks != null) {
                if (ks instanceof TransactionKeySet) {
                    this.primaryKeyIndices[i] = new TransactionKeySet((TransactionKeySet) ks);
                } else {
                    this.primaryKeyIndices[i] = new ElementKeySet(ks);
                }
                this.removedFromKeys[i] = new ElementList(original.removedFromKeys[i]);
            }
        }

        this.operationMode = original.operationMode;

        graphElementMerger = schema == null ? null : schema.getFactory().getGraphElementMerger();

        this.attributeIndexTypes = new GraphIndexType[original.attributeIndexTypes.length];
        Arrays.fill(this.attributeIndexTypes, GraphIndexType.NONE);
        this.attributeIndices = new GraphIndex[original.attributeIndices.length];
        Arrays.fill(this.attributeIndices, AttributeDescription.NULL_GRAPH_INDEX);
        for (GraphElementType elementType : GraphElementType.values()) {
            int attributeCount = getAttributeCount(elementType);
            for (int i = 0; i < attributeCount; i++) {
                int attribute = getAttribute(elementType, i);
                setAttributeIndexType(attribute, original.attributeIndexTypes[attribute]);
            }
        }

        // New Parameters copied
        this.selectedFilterBitmaskAttrId = original.selectedFilterBitmaskAttrId;
        this.vertexFilterBitmaskAttrId = original.vertexFilterBitmaskAttrId;
        this.transactionFilterBitmaskAttrId = original.transactionFilterBitmaskAttrId;
        this.vertexFilterVisibilityAttrId = original.vertexFilterVisibilityAttrId;
        this.transactionFilterVisibilityAttrId = original.transactionFilterVisibilityAttrId;
        this.tXAttrId = original.tXAttrId;
        this.tYAttrId = original.tYAttrId;
        this.tZAttrId = original.tZAttrId;
        this.vXAttrId = original.vXAttrId;
        this.vYAttrId = original.vYAttrId;
        this.vZAttrId = original.vZAttrId;
        this.vSelectedAttrId = original.vSelectedAttrId;
        this.tSelectedAttrId = original.tSelectedAttrId;
        this.cameraAttrId = original.cameraAttrId;
        this.layerPrefs.addAll(original.layerPrefs);
        this.setLayerQueries(original.LAYER_QUERIES); // TODO: Static queries arent going to be any good, a copy will result in editing of same query - Fix this

        MemoryManager.newObject(StoreGraph.class);
    }

    public void setModificationCounters(long globalModificationCounter, long structureModificationCounter, long attributeModificationCounter) {
        this.globalModificationCounter = globalModificationCounter;
        this.structureModificationCounter = structureModificationCounter;
        this.attributeModificationCounter = attributeModificationCounter;
    }

    public void setValueModificationCounter(int attribute, long modificationCounter) {
        attributeModificationCounters[attribute] = modificationCounter;
    }

    @Override
    public long getModificationCounter() {
        return globalModificationCounter;
    }

    @Override
    public void validateKeys() throws DuplicateKeyException {
        validateKey(GraphElementType.VERTEX, true);
        validateKey(GraphElementType.TRANSACTION, true);
    }

    @Override
    public void executeGraphOperation(GraphOperation operation) {
        if (operation.isMoreEfficient()) {
            GraphEdit savedEdit = graphEdit;
            graphEdit = null;
            try {
                operation.execute(this);
                if (savedEdit != null) {
                    savedEdit.executeGraphOperation(operation);
                }
            } finally {
                graphEdit = savedEdit;
            }
        } else {
            operation.execute(this);
        }
    }

    @Override
    public void validateKey(final GraphElementType elementType, boolean allowMerging) throws DuplicateKeyException {
        IntHashSet index = primaryKeyIndices[elementType.ordinal()];
        if (index != null) {
            ElementList removed = removedFromKeys[elementType.ordinal()];
            while (removed.getSize() > 0) {

                // Get the next element to be added back into the index
                int element = removed.getFirst();

                // Attempt to add the element to the index
                int existingElement = index.add(element);

                // If there was no clash in the index...
                if (existingElement < 0) {

                    removed.remove(element);

                    // If the element was not unique then attempt a merge
                } else {

                    if (allowMerging && graphElementMerger != null) {
                        try {
                            if (graphElementMerger.mergeElement(this, elementType, existingElement, element)) {
                                continue;
                            }
                        } catch (Exception ex) {
                        }
                    }

                    StringBuilder elementValues = new StringBuilder("New[" + element + "]: ");
                    StringBuilder existingElementValues = new StringBuilder("Existing[" + existingElement + "]: ");
                    String separator = "";
                    for (int attribute : primaryKeys[elementType.ordinal()]) {

                        elementValues.append(separator);
                        existingElementValues.append(separator);
                        separator = ", ";

                        elementValues.append(getAttributeName(attribute));
                        existingElementValues.append(getAttributeName(attribute));

                        elementValues.append(" = ");
                        existingElementValues.append(" = ");

                        elementValues.append(getStringValue(attribute, element));
                        existingElementValues.append(getStringValue(attribute, existingElement));
                    }
                    throw new DuplicateKeyException("Duplicate Primary Keys (" + elementType + "): \n\n\t" + existingElementValues.toString() + ", \n\n\t" + elementValues.toString(), elementType, existingElement, element);
                }
            }
        }
    }

    @Override
    public void validateKey(final GraphElementType elementType, int element, boolean allowMerging) throws DuplicateKeyException {
        final IntHashSet index = primaryKeyIndices[elementType.ordinal()];
        if (index != null) {
            final ElementList removed = removedFromKeys[elementType.ordinal()];

            if (!removed.contains(element)) {
                return;
            }

            // Attempt to add the element to the index
            final int existingElement = index.add(element);

            // If there was no clash in the index...
            if (existingElement < 0) {
                removed.remove(element);

                // If there was a clash in the index...
            } else {
                if (allowMerging && graphElementMerger != null) {
                    try {
                        if (graphElementMerger.mergeElement(this, elementType, existingElement, element)) {
                            return;
                        }
                    } catch (Exception ex) {
                    }
                }

                final StringBuilder elementValues = new StringBuilder("New[" + element + "]: ");
                final StringBuilder existingElementValues = new StringBuilder("Existing[" + existingElement + "]: ");
                String separator = "";
                for (int attribute : primaryKeys[elementType.ordinal()]) {
                    elementValues.append(separator);
                    existingElementValues.append(separator);
                    separator = ", ";

                    elementValues.append(getAttributeName(attribute));
                    existingElementValues.append(getAttributeName(attribute));

                    elementValues.append(" = ");
                    existingElementValues.append(" = ");

                    elementValues.append(getStringValue(attribute, element));
                    existingElementValues.append(getStringValue(attribute, existingElement));
                }
                throw new DuplicateKeyException("Duplicate Primary Keys (" + elementType + "): \n\n\t" + existingElementValues.toString() + ", \n\n\t" + elementValues.toString(), elementType, existingElement, element);
            }
        }
    }

    @Override
    public GraphReadMethods copy() {
        return new StoreGraph(this);
    }

    @Override
    public GraphReadMethods copy(final String id) {
        return new StoreGraph(id, schema == null ? null : schema.getFactory().createSchema(), this);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Schema getSchema() {
        return schema;
    }

    @Override
    public long getGlobalModificationCounter() {
        return globalModificationCounter;
    }

    @Override
    public long getAttributeModificationCounter() {
        return attributeModificationCounter;
    }

    @Override
    public long getStructureModificationCounter() {
        return structureModificationCounter;
    }

    @Override
    public long getValueModificationCounter(final int attribute) {
        return attributeModificationCounters[attribute];
    }

    @Override
    public int getVertexCapacity() {
        return vStore.getCapacity();
    }

    @Override
    public int getLinkCapacity() {
        return lStore.getCapacity();
    }

    @Override
    public int getEdgeCapacity() {
        return eStore.getCapacity();
    }

    @Override
    public int getTransactionCapacity() {
        return tStore.getCapacity();
    }

    @Override
    public int getVertexCount() {
        return vStore.getCount();
    }

    @Override
    public int getLinkCount() {
        return lStore.getCount();
    }

    @Override
    public int getEdgeCount() {
        return eStore.getCount();
    }

    @Override
    public int getTransactionCount() {
        return tStore.getCount();
    }

    @Override
    public int getVertex(final int position) {
        return vStore.getElement(position);
    }

    @Override
    public int getLink(final int position) {
        return lStore.getElement(position);
    }

    @Override
    public int getLinkPosition(final int link) {
        return lStore.getElementPosition(link);
    }

    @Override
    public long getLinkUID(final int link) {
        return lStore.getUID(link);
    }

    @Override
    public int getEdge(final int position) {
        return eStore.getElement(position);
    }

    @Override
    public int getEdgePosition(final int edge) {
        return eStore.getElementPosition(edge);
    }

    @Override
    public long getEdgeUID(final int edge) {
        return eStore.getUID(edge);
    }

    @Override
    public int getTransaction(final int position) {
        return tStore.getElement(position);
    }

    @Override
    public int getTransactionPosition(final int transaction) {
        return tStore.getElementPosition(transaction);
    }

    @Override
    public long getTransactionUID(final int transaction) {
        return tStore.getUID(transaction);
    }

    @Override
    public boolean vertexExists(final int vertex) {
        return vStore.elementExists(vertex);
    }

    @Override
    public boolean linkExists(final int vertex) {
        return lStore.elementExists(vertex);
    }

    @Override
    public boolean edgeExists(final int edge) {
        return eStore.elementExists(edge);
    }

    @Override
    public boolean transactionExists(final int vertex) {
        return tStore.elementExists(vertex);
    }

    @Override
    public int addVertex() {
        return addVertex(-1);
    }

    public int addVertex(int vertex) {

        if (vertex < 0) {
            ensureVertexCapacity(vStore.getCount() + 1);
            vertex = vStore.add();
        } else {
            if (vStore.elementExists(vertex)) {
                throw new IllegalStateException("attempt to add vertex with duplicate id: " + vertex);
            }
            ensureVertexCapacity(vertex + 1);
            vStore.add(vertex);
        }

        structureModificationCounter += operationMode.getModificationIncrement();
        globalModificationCounter += operationMode.getModificationIncrement();

        vStore.setUID(vertex, structureModificationCounter);

        IntHashSet vertexIndex = primaryKeyIndices[GraphElementType.VERTEX.ordinal()];
        if (vertexIndex != null) {
            removedFromKeys[GraphElementType.VERTEX.ordinal()].addToBack(vertex);
        }

        addElementToIndices(GraphElementType.VERTEX, vertex);

        if (graphEdit != null) {
            graphEdit.addVertex(vertex);
        }

        return vertex;
    }

    private void addElementToIndices(GraphElementType elementType, int element) {
        int attributeCount = getAttributeCount(elementType);
        for (int i = 0; i < attributeCount; i++) {
            int attribute = getAttribute(elementType, i);
            attributeIndices[attribute].addElement(element);
        }
    }

    private void removeElementFromIndices(GraphElementType elementType, int element) {
        int attributeCount = getAttributeCount(elementType);
        for (int i = 0; i < attributeCount; i++) {
            int attribute = getAttribute(elementType, i);
            attributeIndices[attribute].removeElement(element);
        }
    }

    @Override
    public void removeVertex(final int vertex) {

        if (!vStore.elementExists(vertex)) {
            throw new IllegalArgumentException("Attempt to remove vertex that does not exist: " + vertex);
        }

        if (operationMode == GraphOperationMode.EXECUTE) {

            // Remove all the transactions that connect to the vertex - needs optimising
            while (vertexTransactions.getElementCount(vertex) > 0) {
                removeTransaction(vertexTransactions.getElement(vertex, 0) >>> 1);
            }

            // Clear all attribute values so that ths vertex starts with a clean slate
            int attributeCount = getAttributeCount(GraphElementType.VERTEX);
            for (int i = 0; i < attributeCount; i++) {
                int attribute = getAttribute(GraphElementType.VERTEX, i);
                if (!isDefaultValue(attribute, vertex)) {
                    clearValue(attribute, vertex);
                }
            }
        }

        removeElementFromIndices(GraphElementType.VERTEX, vertex);

        IntHashSet index = primaryKeyIndices[GraphElementType.VERTEX.ordinal()];
        if (index != null && !removedFromKeys[GraphElementType.VERTEX.ordinal()].remove(vertex)) {
            index.remove(vertex);
        }

        vStore.remove(vertex);

        structureModificationCounter += operationMode.getModificationIncrement();
        globalModificationCounter += operationMode.getModificationIncrement();

        if (graphEdit != null) {
            graphEdit.removeVertex(vertex);
        }
    }

    @Override
    public int getLink(final int vertex1, final int vertex2) {
        if (vertex1 <= vertex2) {
            int link = linkHash[calculateHash(vertex1, vertex2)];
            while (link >= 0) {
                if (vertexLinks.getElementList(link << 1) == vertex1 && vertexLinks.getElementList((link << 1) + 1) == vertex2) {
                    return link;
                }
                link = linkNext[link];
            }
        } else {
            int link = linkHash[calculateHash(vertex2, vertex1)];
            while (link >= 0) {
                if (vertexLinks.getElementList(link << 1) == vertex2 && vertexLinks.getElementList((link << 1) + 1) == vertex1) {
                    return link;
                }
                link = linkNext[link];
            }
        }
        return NOT_FOUND;
    }

    private int calculateHash(final int lowVertex, final int highVertex) {
        return (lowVertex * 11 + highVertex * 117) & linkHashMask;
    }

    @Override
    public int addTransaction(int sourceVertex, int destinationVertex, final boolean directed) {
        return addTransaction(-1, sourceVertex, destinationVertex, directed);
    }

    @Override
    public int addTransaction(int transaction, int sourceVertex, int destinationVertex, final boolean directed) {

        // Ensure that the source vertex exists
        if (!vStore.elementExists(sourceVertex)) {
            throw new IllegalArgumentException("Attempt to create transaction from source vertex that does not exist: " + sourceVertex);
        }

        // Ensure that the destination vertex exists
        if (!vStore.elementExists(destinationVertex)) {
            throw new IllegalArgumentException("Attempt to create transaction to destination vertex that does not exist: " + destinationVertex);
        }

        ensureLinkCapacity(lStore.getCount() + 1);
        ensureEdgeCapacity(eStore.getCount() + 1);

        if (transaction < 0) {
            ensureTransactionCapacity(tStore.getCount() + 1);
            transaction = tStore.add();
        } else {
            if (tStore.elementExists(transaction)) {
                throw new IllegalStateException("attempt to add transaction with duplicate id: " + transaction);
            }
            ensureTransactionCapacity(transaction + 1);
            tStore.add(transaction);
        }

        structureModificationCounter += operationMode.getModificationIncrement();
        globalModificationCounter += operationMode.getModificationIncrement();

        tStore.setUID(transaction, structureModificationCounter);

        final int lowVertex;
        final int highVertex;
        final int lowDirection;
        final int highDirection;
        final int sourceDirection;
        final int destinationDirection;
        if (sourceVertex <= destinationVertex) {
            lowVertex = sourceVertex;
            highVertex = destinationVertex;
            if (directed) {
                lowDirection = OUTGOING;
                highDirection = INCOMING;
                sourceDirection = OUTGOING;
                destinationDirection = INCOMING;
            } else {
                lowDirection = highDirection = UNDIRECTED;
                sourceDirection = destinationDirection = UNDIRECTED;
            }
        } else {
            lowVertex = destinationVertex;
            highVertex = sourceVertex;
            if (directed) {
                lowDirection = INCOMING;
                highDirection = OUTGOING;
                sourceDirection = OUTGOING;
                destinationDirection = INCOMING;
            } else {
                lowDirection = highDirection = UNDIRECTED;
                sourceDirection = destinationDirection = UNDIRECTED;
                sourceVertex = lowVertex;
                destinationVertex = highVertex;
            }
        }

        vertexTransactions.addElement(sourceVertex, transaction << 1, sourceDirection);
        vertexTransactions.addElement(destinationVertex, (transaction << 1) + 1, destinationDirection);

        int hash = calculateHash(lowVertex, highVertex);
        int link = linkHash[hash];

        while (true) {

            if (link < 0) {

                link = lStore.add();
                lStore.setUID(link, structureModificationCounter);

                vertexLinks.addElement(lowVertex, link * 2, STATE_TO_CATEGORY[1 << lowDirection]);
                vertexLinks.addElement(highVertex, link * 2 + 1, STATE_TO_CATEGORY[1 << highDirection]);

                // Add the link to the hash table
                final int first = linkHash[hash];
                linkNext[link] = first;
                linkPrev[link] = hash | HIGH_BIT;
                if (first >= 0) {
                    linkPrev[first] = link;
                }
                linkHash[hash] = link;

                final int edge = eStore.add();
                eStore.setUID(edge, structureModificationCounter);

                linkEdges.addElement(link, edge, lowDirection);
                vertexEdges.addElement(sourceVertex, edge << 1, sourceDirection);
                vertexEdges.addElement(destinationVertex, (edge << 1) + 1, destinationDirection);

                addElementToIndices(GraphElementType.LINK, link);

                break;
            }

            if (vertexLinks.getElementList(link << 1) == lowVertex && vertexLinks.getElementList((link << 1) + 1) == highVertex) {

                final int currentLowCategory = CATEGORY_TO_STATE[vertexLinks.getElementCategory(link << 1)];
                final int newLowCategory = currentLowCategory | (1 << lowDirection);
                if (newLowCategory > currentLowCategory) {

                    vertexLinks.removeElement(link << 1);
                    vertexLinks.addElement(lowVertex, link << 1, STATE_TO_CATEGORY[newLowCategory]);

                    final int currentHighCategory = CATEGORY_TO_STATE[vertexLinks.getElementCategory((link << 1) + 1)];
                    vertexLinks.removeElement((link << 1) + 1);
                    vertexLinks.addElement(highVertex, (link << 1) + 1, STATE_TO_CATEGORY[currentHighCategory | (1 << highDirection)]);

                    final int edge = eStore.add();
                    eStore.setUID(edge, structureModificationCounter);

                    linkEdges.addElement(link, edge, lowDirection);
                    vertexEdges.addElement(sourceVertex, edge << 1, sourceDirection);
                    vertexEdges.addElement(destinationVertex, (edge << 1) + 1, destinationDirection);

                    addElementToIndices(GraphElementType.EDGE, edge);
                }

                break;
            }

            link = linkNext[link];
        }

        linkTransactions.addElement(link, transaction, lowDirection);

        IntHashSet transactionIndex = primaryKeyIndices[GraphElementType.TRANSACTION.ordinal()];
        if (transactionIndex != null) {
            removedFromKeys[GraphElementType.TRANSACTION.ordinal()].addToBack(transaction);
        }

        addElementToIndices(GraphElementType.TRANSACTION, transaction);

        if (graphEdit != null) {
            graphEdit.addTransaction(sourceVertex, destinationVertex, directed, transaction);
        }

        return transaction;
    }

    @Override
    public void removeTransaction(final int transaction) {

        // Ensure that the transaction exists
        if (!tStore.elementExists(transaction)) {
            throw new IllegalArgumentException("Attempt to remove a transaction that does not exist: " + transaction);
        }

        if (operationMode == GraphOperationMode.EXECUTE) {

            // Clear all attribute values so the current values are saved to the undo stack
            int attributeCount = getAttributeCount(GraphElementType.TRANSACTION);
            for (int i = 0; i < attributeCount; i++) {
                int attribute = getAttribute(GraphElementType.TRANSACTION, i);
                if (!isDefaultValue(attribute, transaction)) {
                    clearValue(attribute, transaction);
                }
            }
        }

        removeElementFromIndices(GraphElementType.TRANSACTION, transaction);

        IntHashSet index = primaryKeyIndices[GraphElementType.TRANSACTION.ordinal()];
        if (index != null && !removedFromKeys[GraphElementType.TRANSACTION.ordinal()].remove(transaction)) {
            index.remove(transaction);
        }

        // Get the link that holds the transaction
        final int link = linkTransactions.getElementList(transaction);

        // Get the direction of the transaction
        final int direction = linkTransactions.getElementCategory(transaction);
        final int sourceVertex = vertexTransactions.getElementList(transaction << 1);
        final int destinationVertex = vertexTransactions.getElementList((transaction << 1) + 1);

        // Remove the transaction from the link
        linkTransactions.removeElement(transaction);

        // Remove the transactions from their vertices
        vertexTransactions.removeElement(transaction << 1);
        vertexTransactions.removeElement((transaction << 1) + 1);

        // Remove the transaction from the graph
        tStore.remove(transaction);

        // If there are no more transaction of this edge
        if (linkTransactions.getElementCount(link, direction) == 0) {

            // Remove the edge
            final int edge = linkEdges.getElement(link, direction, 0);

            removeElementFromIndices(GraphElementType.EDGE, edge);

            eStore.remove(edge);
            linkEdges.removeElement(edge);
            vertexEdges.removeElement(edge << 1);
            vertexEdges.removeElement((edge << 1) + 1);

            int lowVertex = vertexLinks.getElementList(link << 1);
            int highVertex = vertexLinks.getElementList((link << 1) + 1);
            int lowState = CATEGORY_TO_STATE[vertexLinks.getElementCategory(link << 1)];
            int highState = CATEGORY_TO_STATE[vertexLinks.getElementCategory((link << 1) + 1)];

            // Remove the low end of the transaction from its vertex
            vertexLinks.removeElement(link << 1);

            // Remove the high end of the transaction from its vertex
            vertexLinks.removeElement((link << 1) + 1);

            // If there are no more transaction of this link
            if (linkTransactions.getElementCount(link) == 0) {

                removeElementFromIndices(GraphElementType.LINK, link);

                // Remove the link from the graph
                lStore.remove(link);

                // Remove the link from the hash table
                final int p = linkPrev[link];
                final int n = linkNext[link];
                if (p < 0) {
                    linkHash[p & LOW_BITS] = n;
                } else {
                    linkNext[p] = n;
                }
                if (n >= 0) {
                    linkPrev[n] = p;
                }

            } else {

                switch (direction) {
                    case UPHILL:
                        lowState ^= (1 << OUTGOING);
                        highState ^= (1 << INCOMING);
                        break;
                    case DOWNHILL:
                        lowState ^= (1 << INCOMING);
                        highState ^= (1 << OUTGOING);
                        break;
                    case FLAT:
                        lowState ^= (1 << UNDIRECTED);
                        highState ^= (1 << UNDIRECTED);
                        break;
                    default:
                        break;
                }

                vertexLinks.addElement(lowVertex, link << 1, STATE_TO_CATEGORY[lowState]);
                vertexLinks.addElement(highVertex, (link << 1) + 1, STATE_TO_CATEGORY[highState]);
            }
        }

        structureModificationCounter += operationMode.getModificationIncrement();
        globalModificationCounter += operationMode.getModificationIncrement();

        if (graphEdit != null) {
            graphEdit.removeTransaction(sourceVertex, destinationVertex, direction != Graph.UNDIRECTED, transaction);
        }
    }

    @Override
    public void setTransactionSourceVertex(int transaction, int newSourceVertex) {

        // Ensure that the transaction exists
        if (!tStore.elementExists(transaction)) {
            throw new IllegalArgumentException("Attempt to set the source vertex of a transaction that does not exist: " + transaction);
        }

        // Ensure that the source vertex exists
        if (!vStore.elementExists(newSourceVertex)) {
            throw new IllegalArgumentException("Attempt to set the source vertex of a transaction to a vertex that does not exist: " + newSourceVertex);
        }

        GraphEdit savedGraphEdit = graphEdit;
        graphEdit = null;

        GraphOperationMode savedOperationMode = operationMode;
        if (operationMode == GraphOperationMode.EXECUTE) {
            operationMode = GraphOperationMode.REDO;
        }

        final int oldSourceVertex = getTransactionSourceVertex(transaction);
        int oldDestinationVertex = getTransactionDestinationVertex(transaction);
        boolean directed = getTransactionDirection(transaction) != Graph.UNDIRECTED;

        // Fix a nasty interaction with undo. (See also setTransactionDestinationVertex().)
        // For the full story, see the comment inside setTransactionDestinationVertex().
        removeTransaction(transaction);
        addTransaction(newSourceVertex, oldDestinationVertex, directed);

        graphEdit = savedGraphEdit;
        operationMode = savedOperationMode;

        if (graphEdit != null) {
            graphEdit.setTransactionSourceVertex(transaction, oldSourceVertex, newSourceVertex, !directed && newSourceVertex > oldDestinationVertex);
        }
    }

    @Override
    public void setTransactionDestinationVertex(int transaction, int newDestinationVertex) {

        // Ensure that the transaction exists
        if (!tStore.elementExists(transaction)) {
            throw new IllegalArgumentException("Attempt to set the destination vertex of a transaction that does not exist: " + transaction);
        }

        // Ensure that the source vertex exists
        if (!vStore.elementExists(newDestinationVertex)) {
            throw new IllegalArgumentException("Attempt to set the destination vertex of a transaction to a vertex that does not exist: " + newDestinationVertex);
        }

        GraphEdit savedGraphEdit = graphEdit;
        graphEdit = null;

        GraphOperationMode savedOperationMode = operationMode;
        if (operationMode == GraphOperationMode.EXECUTE) {
            operationMode = GraphOperationMode.REDO;
        }

        int oldSourceVertex = getTransactionSourceVertex(transaction);
        final int oldDestinationVertex = getTransactionDestinationVertex(transaction);
        boolean directed = getTransactionDirection(transaction) != Graph.UNDIRECTED;

        // Fix a nasty interaction with undo. (See also setTransactionSourceVertex().)
        // When undirected transactions are added, addTransaction() ensures that the source vertex id
        // is <= the destination id so undirected transaction always go uphill. (The details of why this is so
        // are currently lost to us.)
        // However, this mucks up when an undo happens, because the original addTransaction() did the
        // end swapping, but the undo isn't aware that the vertex ids were swapped: this is because
        // at this point, the graphEdit has deliberately been turned off (see above), so the
        // removeTransaction() + addTransaction (see below) can be done atomically with respect to
        // the undo/redo.
        // The apparent solution is to manually swap the source and destination vertex ids to be in the "correct"
        // uphill order before we do the out-of-edit remove/add, so no swapping occurs.
        removeTransaction(transaction);
        addTransaction(oldSourceVertex, newDestinationVertex, directed);

        graphEdit = savedGraphEdit;
        operationMode = savedOperationMode;

        if (graphEdit != null) {
            graphEdit.setTransactionDestinationVertex(transaction, oldDestinationVertex, newDestinationVertex, !directed && oldSourceVertex > newDestinationVertex);
        }
    }

    @Override
    public int getVertexLinkCount(final int vertex) {
        return vertexLinks.getElementCount(vertex);
    }

    @Override
    public int getVertexLink(final int vertex, final int position) {
        return vertexLinks.getElement(vertex, position) >>> 1;
    }

    @Override
    public int getVertexNeighbourCount(final int vertex) {
        return vertexLinks.getElementCount(vertex);
    }

    @Override
    public int getVertexEdgeCount(final int vertex) {
        return vertexEdges.getElementCount(vertex);
    }

    @Override
    public int getVertexEdge(final int vertex, final int position) {
        return vertexEdges.getElement(vertex, position) >>> 1;
    }

    @Override
    public int getVertexEdgeCount(final int vertex, final int direction) {
        return vertexEdges.getElementCount(vertex, direction);
    }

    @Override
    public int getVertexEdge(final int vertex, final int direction, final int position) {
        return vertexEdges.getElement(vertex, direction, position) >>> 1;
    }

    @Override
    public int getVertexNeighbour(final int vertex, final int position) {
        return vertexLinks.getElementList(vertexLinks.getElement(vertex, position) ^ 1);
    }

    @Override
    public int getLinkLowVertex(final int link) {
        return vertexLinks.getElementList(link << 1);
    }

    @Override
    public int getLinkHighVertex(final int link) {
        return vertexLinks.getElementList((link << 1) + 1);
    }

    @Override
    public int getLinkEdgeCount(final int link) {
        return linkEdges.getElementCount(link);
    }

    @Override
    public int getLinkEdge(final int link, final int position) {
        return linkEdges.getElement(link, position);
    }

    @Override
    public int getLinkEdgeCount(final int link, final int direction) {
        return linkEdges.getElementCount(link, direction);
    }

    @Override
    public int getLinkEdge(final int link, final int direction, final int position) {
        return linkEdges.getElement(link, direction, position);
    }

    @Override
    public int getLinkTransactionCount(final int link) {
        return linkTransactions.getElementCount(link);
    }

    @Override
    public int getLinkTransaction(final int link, final int position) {
        return linkTransactions.getElement(link, position);
    }

    @Override
    public int getLinkTransactionCount(final int link, final int direction) {
        return linkTransactions.getElementCount(link, direction);
    }

    @Override
    public int getLinkTransaction(final int link, final int direction, final int position) {
        return linkTransactions.getElement(link, direction, position);
    }

    @Override
    public int getTransactionLink(final int transaction) {
        return linkTransactions.getElementList(transaction);
    }

    @Override
    public int getTransactionEdge(final int transaction) {
        int link = linkTransactions.getElementList(transaction);
        int direction = linkTransactions.getElementCategory(transaction);
        return linkEdges.getElement(link, direction, 0);
    }

    @Override
    public int getTransactionDirection(final int transaction) {
        return linkTransactions.getElementCategory(transaction);
    }

    @Override
    public int getTransactionSourceVertex(final int transaction) {
        return vertexTransactions.getElementList(transaction << 1);
    }

    @Override
    public int getTransactionDestinationVertex(final int transaction) {
        return vertexTransactions.getElementList((transaction << 1) + 1);
    }

    @Override
    public int getVertexTransactionCount(final int vertex) {
        return vertexTransactions.getElementCount(vertex);
    }

    @Override
    public int getVertexPosition(final int vertex) {
        return vStore.getElementPosition(vertex);
    }

    @Override
    public long getVertexUID(final int vertex) {
        return vStore.getUID(vertex);
    }

    @Override
    public int getVertexTransaction(final int vertex, final int position) {
        return vertexTransactions.getElement(vertex, position) >>> 1;
    }

    @Override
    public int getVertexTransactionCount(final int vertex, final int direction) {
        return vertexTransactions.getElementCount(vertex, direction);
    }

    @Override
    public int getVertexTransaction(final int vertex, final int direction, final int position) {
        return vertexTransactions.getElement(vertex, direction, position) >>> 1;
    }

    @Override
    public int getEdgeLink(final int edge) {
        return linkEdges.getElementList(edge);
    }

    @Override
    public int getEdgeDirection(final int edge) {
        return linkEdges.getElementCategory(edge);
    }

    @Override
    public int getEdgeSourceVertex(final int edge) {
        return vertexEdges.getElementList(edge << 1);
    }

    @Override
    public int getEdgeDestinationVertex(final int edge) {
        return vertexEdges.getElementList((edge << 1) + 1);
    }

    @Override
    public int getEdgeTransactionCount(final int edge) {
        final int link = linkEdges.getElementList(edge);
        final int direction = linkEdges.getElementCategory(edge);
        return linkTransactions.getElementCount(link, direction);
    }

    @Override
    public int getEdgeTransaction(final int edge, final int position) {
        final int link = linkEdges.getElementList(edge);
        final int direction = linkEdges.getElementCategory(edge);
        return linkTransactions.getElement(link, direction, position);
    }

    private void ensureVertexCapacity(int capacity) {
        if (vStore.ensureCapacity(capacity)) {
            vertexLinks.expandListCapacity(vStore.getCapacity());
            vertexTransactions.expandListCapacity(vStore.getCapacity());
            vertexEdges.expandListCapacity(vStore.getCapacity());

            expandAttributeElementCapacity(GraphElementType.VERTEX.ordinal(), vStore.getCapacity());
        }
    }

    private boolean ensureLinkCapacity(int capacity) {
        if (lStore.ensureCapacity(capacity)) {
            vertexLinks.expandElementCapacity(lStore.getCapacity() << 1);
            linkTransactions.expandListCapacity(lStore.getCapacity());
            linkEdges.expandListCapacity(lStore.getCapacity());

            linkHashLength = lStore.getCapacity();
            linkHashMask = linkHashLength - 1;
            linkHash = new int[linkHashLength];
            Arrays.fill(linkHash, NOT_FOUND);
            linkNext = new int[lStore.getCapacity()];
            linkPrev = new int[lStore.getCapacity()];

            for (int i = 0; i < lStore.getCount(); i++) {
                int link = lStore.getElement(i);
                int hash = calculateHash(vertexLinks.getElementList(link << 1), vertexLinks.getElementList((link << 1) + 1));

                final int first = linkHash[hash];
                linkNext[link] = first;
                linkPrev[link] = hash | HIGH_BIT;
                if (first >= 0) {
                    linkPrev[first] = link;
                }
                linkHash[hash] = link;
            }

            expandAttributeElementCapacity(GraphElementType.LINK.ordinal(), lStore.getCapacity());

            return true;
        }

        return false;
    }

    private void ensureEdgeCapacity(int capacity) {
        if (eStore.ensureCapacity(capacity)) {
            linkEdges.expandElementCapacity(eStore.getCapacity());
            vertexEdges.expandElementCapacity(eStore.getCapacity() * 2);

            expandAttributeElementCapacity(GraphElementType.EDGE.ordinal(), eStore.getCapacity());
        }
    }

    private void ensureTransactionCapacity(int capacity) {
        if (tStore.ensureCapacity(capacity)) {
            vertexTransactions.expandElementCapacity(tStore.getCapacity() << 1);
            linkTransactions.expandElementCapacity(tStore.getCapacity());

            expandAttributeElementCapacity(GraphElementType.TRANSACTION.ordinal(), tStore.getCapacity());
        }
    }

    private void ensureAttributeCapacity(int capacity) {
        int oldAttributeCapacity = aStore.getCapacity();
        if (aStore.ensureCapacity(capacity)) {
            typeAttributes.expandElementCapacity(aStore.getCapacity());
            attributeDescriptions = Arrays.copyOf(attributeDescriptions, aStore.getCapacity());
            attributes = Arrays.copyOf(attributes, aStore.getCapacity());
            attributeModificationCounters = Arrays.copyOf(attributeModificationCounters, aStore.getCapacity());

            attributeIndices = Arrays.copyOf(attributeIndices, aStore.getCapacity());
            Arrays.fill(attributeIndices, oldAttributeCapacity, aStore.getCapacity(), AttributeDescription.NULL_GRAPH_INDEX);

            attributeIndexTypes = Arrays.copyOf(attributeIndexTypes, aStore.getCapacity());
            Arrays.fill(attributeIndexTypes, oldAttributeCapacity, aStore.getCapacity(), GraphIndexType.NONE);

            int oldPrimaryKeyLookupLength = primaryKeyLookup.length;
            primaryKeyLookup = Arrays.copyOf(primaryKeyLookup, aStore.getCapacity());
            Arrays.fill(primaryKeyLookup, oldPrimaryKeyLookupLength, primaryKeyLookup.length, -1);
        }
    }

    private void expandAttributeElementCapacity(final int elementTypeIndex, final int capacity) {

        int count = typeAttributes.getElementCount(elementTypeIndex);
        for (int i = 0; i < count; i++) {
            int attributeId = typeAttributes.getElement(elementTypeIndex, i);
            attributeDescriptions[attributeId].setCapacity(capacity);
            attributeIndices[attributeId].expandCapacity(capacity);
        }

        if (primaryKeyIndices[elementTypeIndex] != null) {
            GraphElementType elementType = GraphElementType.values()[elementTypeIndex];

            ElementKeySet keySet;
            if (elementType == GraphElementType.VERTEX) {
                keySet = primaryKeyIndices[elementTypeIndex] = new ElementKeySet(capacity, elementType);
            } else {
                keySet = primaryKeyIndices[elementTypeIndex] = new TransactionKeySet(capacity, elementType);
            }

            ElementList removed = removedFromKeys[elementTypeIndex];
            removed.ensureCapacity(capacity);

            int elementCount = elementType.getElementCount(this);
            for (int position = 0; position < elementCount; position++) {
                int element = elementType.getElement(this, position);
                if (!removed.contains(element)) {
                    keySet.add(element);
                }
            }
        }
    }

    public void setAttributeRegistry(final AttributeRegistry attributeRegistry) {
        this.attributeRegistry = attributeRegistry;
    }

    @Override
    public void updateAttributeName(final int attribute, final String newName) {
        String oldName = attributes[attribute].getName();
        int elementType = attributes[attribute].getElementType().ordinal();
        (attributeNames.get(oldName))[elementType] = NOT_FOUND;
        int[] existingAttributes = attributeNames.get(newName);
        if (existingAttributes == null) {
            existingAttributes = new int[GraphElementType.values().length];
            Arrays.fill(existingAttributes, NOT_FOUND);
            attributeNames.put(newName, existingAttributes);
        }
        (attributeNames.get(newName))[elementType] = attribute;

        GraphAttribute entry = attributes[attribute];
        entry.setName(newName);
        globalModificationCounter += operationMode.getModificationIncrement();
        attributeModificationCounter += operationMode.getModificationIncrement();

        if (graphEdit != null) {
            graphEdit.updateAttributeName(attribute, oldName, newName);
        }
    }

    @Override
    public void updateAttributeDescription(int attribute, String newDescription) {
        GraphAttribute entry = attributes[attribute];
        String oldDescription = entry.getDescription();
        entry.setDescription(newDescription);
        globalModificationCounter += operationMode.getModificationIncrement();
        attributeModificationCounter += operationMode.getModificationIncrement();

        if (graphEdit != null) {
            graphEdit.updateAttributeDescription(attribute, oldDescription, newDescription);
        }
    }

    @Override
    public void updateAttributeDefaultValue(int attribute, Object newDefault) {
        AttributeDescription description = attributeDescriptions[attribute];
        description.setDefault(newDefault);

        GraphAttribute entry = attributes[attribute];
        Object oldDefault = entry.getDefaultValue();
        entry.setDefaultValue(newDefault);

        // Clear all the unused attribute values
        switch (entry.getElementType()) {
            case VERTEX:
                for (int id = 0; id < vStore.getCapacity(); id++) {
                    if (!vStore.elementExists(id)) {
                        description.clear(id);
                    }
                }
                break;
            case LINK:
                for (int id = 0; id < lStore.getCapacity(); id++) {
                    if (!lStore.elementExists(id)) {
                        description.clear(id);
                    }
                }
                break;
            case EDGE:
                for (int id = 0; id < eStore.getCapacity(); id++) {
                    if (!eStore.elementExists(id)) {
                        description.clear(id);
                    }
                }
                break;
            case TRANSACTION:
                for (int id = 0; id < tStore.getCapacity(); id++) {
                    if (!tStore.elementExists(id)) {
                        description.clear(id);
                    }
                }
                break;
            default:
                break;
        }

        globalModificationCounter += operationMode.getModificationIncrement();
        attributeModificationCounter += operationMode.getModificationIncrement();

        if (graphEdit != null) {
            graphEdit.updateAttributeDefaultValue(attribute, oldDefault, newDefault);
        }
    }

    @Override
    public int addAttribute(final GraphElementType elementType, final String attributeType, final String label, final String description, final Object defaultValue, final String attributeMergerId) {

        Class<? extends AttributeDescription> dataType = attributeRegistry.getAttributes().get(attributeType);
        if (dataType == null) {
            throw new IllegalArgumentException("No attribute description found for attribute type: " + attributeType);
        }

        GraphAttributeMerger attributeMerger = null;
        if (attributeMergerId != null) {
            attributeMerger = GraphAttributeMerger.getMergers().get(attributeMergerId);
            if (attributeMerger == null) {
                throw new IllegalArgumentException("No attribute merger found for attribute merger id: " + attributeMergerId);
            }
        }
        int[] existingAttributes = attributeNames.get(label);

        if (existingAttributes != null && existingAttributes[elementType.ordinal()] >= 0) {

            int attribute = existingAttributes[elementType.ordinal()];
            if (attributes[attribute].getAttributeType().equals(attributeType)) {
                return attribute;
            }

            throw new IllegalArgumentException("Attempt to create a " + elementType + " attribute with a duplicate label: " + label);
        }

        AttributeDescription attributeDescription;
        try {
            attributeDescription = dataType.getDeclaredConstructor().newInstance();
            attributeDescription.setGraph(this);
            attributeDescription.setDefault(defaultValue);

            switch (elementType) {
                case META:
                case GRAPH:
                    attributeDescription.setCapacity(1);
                    break;
                case VERTEX:
                    attributeDescription.setCapacity(vStore.getCapacity());
                    break;
                case LINK:
                    attributeDescription.setCapacity(lStore.getCapacity());
                    break;
                case EDGE:
                    attributeDescription.setCapacity(eStore.getCapacity());
                    break;
                case TRANSACTION:
                    attributeDescription.setCapacity(tStore.getCapacity());
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognised element type " + elementType);
            }
        } catch (final IllegalAccessException | IllegalArgumentException
                | InstantiationException | NoSuchMethodException
                | SecurityException | InvocationTargetException ex) {
            final String msg = String.format("Error creating data type for new %s attribute '%s'", elementType, label);
            throw new IllegalStateException(msg, ex);
        }

        ensureAttributeCapacity(aStore.getCount() + 1);

        attributeModificationCounter += operationMode.getModificationIncrement();
        globalModificationCounter += operationMode.getModificationIncrement();

        final int attributeId = aStore.add();
        aStore.setUID(attributeId, globalModificationCounter);

        attributeModificationCounters[attributeId] = 0;

        typeAttributes.addElement(elementType.ordinal(), attributeId, 0);

        if (existingAttributes == null) {
            existingAttributes = new int[GraphElementType.values().length];
            Arrays.fill(existingAttributes, NOT_FOUND);
            attributeNames.put(label, existingAttributes);
        }

        // Use the default value from attributeDescription.
        // The defaultValue parameter could be of any type (for example String) which can parsed by the attributeDescription (for instance float).
        // Getting the default value from the attributeDescription gives us an object of the correct type.
        attributes[attributeId] = new GraphAttribute(attributeId, elementType, attributeDescription.getName(), label, description, attributeDescription.getDefault(), dataType, attributeMerger);

        existingAttributes[elementType.ordinal()] = attributeId;

        attributeDescriptions[attributeId] = attributeDescription;

        if (graphEdit != null) {
            graphEdit.addAttribute(elementType, attributeType, label, description, defaultValue, attributeMergerId, attributeId);
        }

        // Store IDs of key attributes used in subsequent ongoing calculations
        if (FILTERMASK_ATTRIBUTE_LABEL.equals(label)) {
            if (elementType == GraphElementType.VERTEX) {
                vertexFilterBitmaskAttrId = attributeId;
            } else if (elementType == GraphElementType.TRANSACTION) {
                transactionFilterBitmaskAttrId = attributeId;
            }
        }
        if (FILTERVISIBILITY_ATTRIBUTE_LABEL.equals(label)) {
            if (elementType == GraphElementType.VERTEX) {
                vertexFilterVisibilityAttrId = attributeId;
            } else if (elementType == GraphElementType.TRANSACTION) {
                transactionFilterVisibilityAttrId = attributeId;
            }
        }
        
        selectedFilterBitmaskAttrId = SELECTED_FILTERMASK_ATTRIBUTE_LABEL.equals(label) ? attributeId : selectedFilterBitmaskAttrId;
        cameraAttrId = Camera.ATTRIBUTE_NAME.equals(label) ? attributeId : cameraAttrId;
        vSelectedAttrId = "selected".equals(label) ? (elementType == GraphElementType.VERTEX ? attributeId : vSelectedAttrId) : vSelectedAttrId;
        vXAttrId = "x".equals(label) ? (elementType == GraphElementType.VERTEX ? attributeId : vXAttrId) : vXAttrId;
        vYAttrId = "y".equals(label) ? (elementType == GraphElementType.VERTEX ? attributeId : vYAttrId) : vYAttrId;
        vZAttrId = "z".equals(label) ? (elementType == GraphElementType.VERTEX ? attributeId : vZAttrId) : vZAttrId;
        tSelectedAttrId = "selected".equals(label) ? (elementType == GraphElementType.TRANSACTION ? attributeId : tSelectedAttrId) : tSelectedAttrId;
        tXAttrId = "x".equals(label) ? (elementType == GraphElementType.TRANSACTION ? attributeId : tXAttrId) : tXAttrId;
        tYAttrId = "y".equals(label) ? (elementType == GraphElementType.TRANSACTION ? attributeId : tYAttrId) : tYAttrId;
        tZAttrId = "z".equals(label) ? (elementType == GraphElementType.TRANSACTION ? attributeId : tZAttrId) : tZAttrId;
        
        return attributeId;
    }

    @Override
    public void removeAttribute(final int attribute) {

        if (!aStore.elementExists(attribute)) {
            throw new IllegalArgumentException("Attempt to remove attribute that does not exist: " + attribute);
        }

        if (primaryKeyLookup[attribute] >= 0) {
            throw new IllegalArgumentException("Attempt to remove an attribute that is part of the primary key: " + attribute);
        }

        GraphAttribute attributeObject = attributes[attribute];

        if (operationMode == GraphOperationMode.EXECUTE) {
            GraphElementType elementType = attributeObject.getElementType();
            int elementCount = elementType.getElementCount(this);
            for (int i = 0; i < elementCount; i++) {
                int element = elementType.getElement(this, i);
                if (!isDefaultValue(attribute, element)) {
                    clearValue(attribute, element);
                }
            }
        }

        aStore.remove(attribute);

        typeAttributes.removeElement(attribute);

        attributes[attribute] = null;

        attributeIndices[attribute] = AttributeDescription.NULL_GRAPH_INDEX;
        attributeIndexTypes[attribute] = GraphIndexType.NONE;

        attributeNames.get(attributeObject.getName())[attributeObject.getElementType().ordinal()] = NOT_FOUND;

        attributeDescriptions[attribute] = null;

        attributeModificationCounter += operationMode.getModificationIncrement();
        globalModificationCounter += operationMode.getModificationIncrement();

        if (graphEdit != null) {
            final String attributeMergerId = attributeObject.getAttributeMerger() == null ? null : attributeObject.getAttributeMerger().getId();
            graphEdit.removeAttribute(attributeObject.getElementType(), attributeObject.getAttributeType(), attributeObject.getName(), attributeObject.getDescription(), attributeObject.getDefaultValue(), attributeMergerId, attribute);
        }
    }

    @Override
    public int getAttributeCount(final GraphElementType elementType) {
        return typeAttributes.getElementCount(elementType.ordinal());
    }

    @Override
    public int getAttributeCapacity() {
        return aStore.getCapacity();
    }

    @Override
    public int getAttribute(final GraphElementType elementType, final int position) {
        return typeAttributes.getElement(elementType.ordinal(), position);
    }

    @Override
    public int getAttribute(final GraphElementType elementType, final String name) {
        int[] labelAttributes = attributeNames.get(name);
        return labelAttributes == null ? NOT_FOUND : labelAttributes[elementType.ordinal()];
    }

    @Override
    public String getAttributeName(final int attribute) {
        return attributes[attribute].getName();
    }

    @Override
    public long getAttributeUID(final int attribute) {
        return aStore.getUID(attribute);
    }

    @Override
    public String getAttributeType(int attribute) {
        return attributes[attribute].getAttributeType();
    }

    @Override
    public GraphAttributeMerger getAttributeMerger(int attribute) {
        return attributes[attribute].getAttributeMerger();
    }

    @Override
    public String getAttributeDescription(int attribute) {
        return attributes[attribute].getDescription();
    }

    @Override
    public GraphElementType getAttributeElementType(int attribute) {
        return attributes[attribute].getElementType();
    }

    @Override
    public Class<? extends AttributeDescription> getAttributeDataType(int attribute) {
        return attributes[attribute].getDataType();
    }

    @Override
    public Object getAttributeDefaultValue(int attribute) {
        return attributes[attribute].getDefaultValue();
    }

    @Override
    public NativeAttributeType getNativeAttributeType(final int attribute) {
        return attributeDescriptions[attribute].getNativeType();
    }

    @Override
    public boolean isDefaultValue(final int attribute, final int id) {
        return attributeDescriptions[attribute].isClear(id);
    }

    @Override
    public byte getByteValue(final int attribute, final int id) {
        return attributeDescriptions[attribute].getByte(id);
    }

    @Override
    public short getShortValue(final int attribute, final int id) {
        return attributeDescriptions[attribute].getShort(id);
    }

    @Override
    public int getIntValue(final int attribute, final int id) {
        return attributeDescriptions[attribute].getInt(id);
    }

    @Override
    public long getLongValue(final int attribute, final int id) {
        return attributeDescriptions[attribute].getLong(id);
    }

    @Override
    public float getFloatValue(final int attribute, final int id) {
        return attributeDescriptions[attribute].getFloat(id);
    }

    @Override
    public double getDoubleValue(final int attribute, final int id) {
        return attributeDescriptions[attribute].getDouble(id);
    }

    @Override
    public boolean getBooleanValue(final int attribute, final int id) {
        return attributeDescriptions[attribute].getBoolean(id);
    }

    @Override
    public char getCharValue(final int attribute, final int id) {
        return attributeDescriptions[attribute].getChar(id);
    }

    @Override
    public String getStringValue(final int attribute, final int id) {
        return attributeDescriptions[attribute].getString(id);
    }

    @Override
    public String acceptsStringValue(int attribute, String value) {
        return attributeDescriptions[attribute].acceptsString(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getObjectValue(final int attribute, final int id) {
        return (T) attributeDescriptions[attribute].getObject(id);
    }

    @Override
    public boolean isPrimaryKey(int attribute) {
        return primaryKeyLookup[attribute] >= 0;
    }

    @Override
    public Object copyAttribute(final int attribute) {
        return attributeDescriptions[attribute].saveData();
    }

    @Override
    public void clearValue(final int attribute, final int id) {
        if (graphEdit == null) {
            attributeDescriptions[attribute].clear(id);
            attributeIndices[attribute].updateElement(id);
            attributeModificationCounters[attribute] += operationMode.getModificationIncrement();
            globalModificationCounter += operationMode.getModificationIncrement();
            int keyType = primaryKeyLookup[attribute];
            if (keyType >= 0) {
                removeFromIndex(keyType, id);
            }
        } else {
            AttributeDescription description = attributeDescriptions[attribute];
            NativeAttributeType nativeType = description.getNativeType();
            nativeType.get(this, attribute, id, oldValue);

            description.clear(id);

            if (nativeType.addEdit(this, graphEdit, attribute, id, oldValue)) {
                attributeIndices[attribute].updateElement(id);
                attributeModificationCounters[attribute] += operationMode.getModificationIncrement();
                globalModificationCounter += operationMode.getModificationIncrement();
                int keyType = primaryKeyLookup[attribute];
                if (keyType >= 0) {
                    removeFromIndex(keyType, id);
                }
            }
        }
    }

    /**
     * 
     * Allows the recalculation of layer visibilities with respect to which 
     * layers are toggled.
     * Loops all queries and determines if they are dynamic, static, activated or
     * deactivated and appends certain bits in response.
     * 
     * Does not check all layers - only recalculates layers which are selected.
     * 
     * **KEY**
     * STATIC
     * 0b00 - NOT SHOWN
     * 0b10 - SHOWN
     * 
     * DYNAMIC
     * 0b10 - NOT SHOWN
     * 0b11 - SHOWN
     * 
     */
    private void recalculateVisibilities() {
        for (int i = 0; i < LAYER_QUERIES.size(); i++) {
            // if current mask has bit set, recheck
            if((currentVisibleMask & (1 << (i))) > 0){
                // " " Is a placeholder for Layer 1. 
                if (LAYER_QUERIES.get(i) == null || LAYER_QUERIES.get(i).equals(" ")) {
                    layerPrefs.remove(i);
                    layerPrefs.add(i, (currentVisibleMask & (1 << (i))) > 0 ? (byte) 0b10 : (byte) 0b0);
                } else {
                    layerPrefs.remove(i);
                    layerPrefs.add(i, (currentVisibleMask & (1 << (i))) > 0 ? (byte) 0b11 : (byte) 0b1);
                }
            }
        }
    }

    /**
     * Loop over all queries, recalculate dynamic ones. Append bit flag to
     * bitmask when it satifies the query.
     *
     *
     * @param id the id of the element
     * @return bitmask a 32 bit integer which represents a flagset of which
     * layer to show it on.
     */
    private int recalculateBitmask(final GraphElementType elementType, final int id) {
        int bitmask = 0;
        
        if (elementType == GraphElementType.VERTEX && vertexFilterBitmaskAttrId >= 0 && vertexFilterVisibilityAttrId >= 0) {
            bitmask = getIntValue(vertexFilterBitmaskAttrId, id);
        } else if (elementType == GraphElementType.TRANSACTION && transactionFilterBitmaskAttrId >= 0 && transactionFilterVisibilityAttrId >= 0) {
            bitmask = getIntValue(transactionFilterBitmaskAttrId, id);
        }

        for (int j = 0; j < LAYER_QUERIES.size(); j++) { // TODO: Concurrent Modification Exception sometiems - maybe with update of the queries?
            if (j < layerPrefs.size() && (layerPrefs.get(j) & 0b11) == 3 && LAYER_QUERIES.get(j) != null) { // calculate bitmask for dynamic layers that are displayed
                bitmask = (evaluateQuery(ShuntingYard.postfix(LAYER_QUERIES.get(j)), id, elementType) ? bitmask | (1 << j) : bitmask & ~(1 << j)); // set bit to false
            }
        }
        return bitmask;
    }

    // TODO: Check other operators for functionality andimplement them
    // TODO: Implement all other transaction types
    // TODO: Possibly parse a boolean in as to wether the query is on a vertex or transaction
    // TODO: Clean up this code and cases
    /**
     * Evaluates a string representation of a query in relation to a single
     * element on the graph returns true when the node satisfies the condition.
     * False otherwise. Query is structured as below:
     * vertex_color:<=:0,0,0 vertex_selected:==:true vertex_color:>:255,255,255
     *
     * @param postfixQuery the string query
     * @param id the id of the element
     * @return a boolean result determining whether the element satisfied the
     * constraints.
     */
    private boolean evaluateQuery(final String postfixQuery, final int id, final GraphElementType elementType) {
        // Exit condition for show all
        if (postfixQuery.equals("")) {
            return true;
        }

        Operator currentOperand = Operator.NOTFOUND;

        String[] rules = postfixQuery.split("`"); // Split with backtick to reduce chance of char in query.
        //System.out.println("Rules: " + rules.length);
        String evaluatedResult = "";
        boolean finalRes = true;
        boolean ignoreRes = false;

        for (String rule : rules) {
            //System.out.println("Rule: " + rule);
            String[] ruleSegments = rule.split(":");
            int segCount = 0;
            AttributeDescription ad = null;
            int attrID = 1;
            // iterate over each section and grab the values.
            for (String segment : ruleSegments) {
                //System.out.println("Current: " + segment);
                segCount++;
                switch (segCount) {
                    case 1: {
                        if (elementType == GraphElementType.VERTEX) {
                            attrID = getAttribute(GraphElementType.VERTEX, segment);
                        } else if (elementType == GraphElementType.TRANSACTION) {
                            attrID = getAttribute(GraphElementType.TRANSACTION, segment);
                        }
                        
                        if (attrID != Graph.NOT_FOUND) {
                            ad = attributeDescriptions[attrID];
                        }
                        break;
                    }
                    case 2: {
                        switch (segment) {
                            case "=": {// equality
                                currentOperand = Operator.EQUALS;
                                break;
                            }
                            case "!=": {// not equality
                                currentOperand = Operator.NOTEQUALS;
                                break;
                            }
                            case ">": {// greater than - how to handle things which can't be compared GT or LT?
                                currentOperand = Operator.GREATERTHAN;
                                break;
                            }
                            case "<": {// Less than
                                currentOperand = Operator.LESSTHAN;
                                break;
                            }
                            case ">=": {// greater than or EQ to
                                currentOperand = Operator.GREATERTHANOREQ;
                                break;
                            }
                            case "<=": {// Less than or EQ to
                                currentOperand = Operator.LESSTHANOREQ;
                                break;
                            }
                            default: {// Default do nothing - unsure of operator - Error?
                                currentOperand = Operator.NOTFOUND;
                                break;
                            }
                        }
                        break;
                    }
                    case 3: {
                        switch (currentOperand) {
                            case EQUALS: {
                                if (ad != null && ad.getString(id) != null && (ad.getString(id)).equals(segment)) { // not null
                                    //System.out.println("Values are the same! name: " + segment);
                                    finalRes = true;
                                } else {

                                    // System.out.print("Vals dont match: " + segment + " and: ");
                                    if (ad != null && ad.getString(id) != null) {
                                        //System.out.println(ad.getString(id));
                                    } else {
                                        //System.out.println("noval");
                                    }
                                    finalRes = false;
                                }
                                break;
                            }
                            case NOTEQUALS: {
                                finalRes = (ad != null && ad.getString(id) != null && !(ad.getString(id)).equals(segment));
                                /*
                                if (ad != null && ad.getString(id) != null && !(ad.getString(id)).equals(segment)) {
                                    finalRes = true;
                                } else {
                                    finalRes = false;
                                }
                                 */
                                break;
                            }
                            case GREATERTHAN: {
                                break;
                            }
                            case LESSTHAN: {
                                break;
                            }
                            case GREATERTHANOREQ: {
                                break;
                            }
                            case LESSTHANOREQ: {
                                break;
                            }
                            case NOTFOUND: {
                                break;
                            }
                        }
                        break;
                    }
                }

                // build the string to evaluate
                if (segment.equals("&&")) {
                    evaluatedResult += "&&" + ":";
                    ignoreRes = true;
                } else if (segment.equals("||")) {
                    evaluatedResult += "||" + ":";
                    ignoreRes = true;
                }
            }

            // when you need to ignore the result and just use an operator || &&
            if (ignoreRes) {
                ignoreRes = false;
            } else {
                evaluatedResult += finalRes + ":";
            }
        }

        evaluatedResult = evaluatedResult.length() == 0 ? evaluatedResult : evaluatedResult.substring(0, evaluatedResult.length() - 1);
        String[] splitted = evaluatedResult.split(":");

        String postfix = "";
        for (String s : splitted) {
            postfix += s + " ";
            //System.out.print(s + " ");
        }
        postfix = postfix.substring(0, postfix.length() - 1);
        //System.out.println("before eval: " + postfix);

        String result = PostfixEvaluator.evaluatePostfix(postfix);
        //System.out.println("Result calculated: " + result);

        if (result.equals("true")) {
            System.err.println();
            return true;
        } else if (result.equals("false")) {
            return false;
        } else {
            //error here
            return false;
        }
    }

    // DFetermine the visibility bitmask of the obkect in question.
    private void updateBitmask(final GraphElementType elementType, final int attribute, final int selectedFilterBitmask, final int id) {

        // TODO: Ultimately, loop through elements bitmask and determine if each layer (1-32) should be turned on or off.
        //       There are 2 types of layers, static and dynamic, if static, the layer is turned on if user has set it.
        // TODO: Eventually store a static bitmask, and then append to it any dynamic bits based on rules. For now assume all
        //       entries are dynamic for the sake of calculations
        int bitmask = recalculateBitmask(elementType, id);
        // avoid recalc of bitmask if it is a static layer

        if (elementType == GraphElementType.VERTEX) {

            // when attr ids are found
            if (vertexFilterBitmaskAttrId >= 0 && vertexFilterVisibilityAttrId >= 0) {
                // Attributes exist, update bitmask (this eventually becomes more comples to cover all layers). If the bitmask
                // is non-zero value then the element can be displayed, if not, layer filters will result in it being hidden.

                if (attribute != vertexFilterBitmaskAttrId) { // set filter bitmask when not updating (prevents infinite loop)
                    avoidUpdate = true; // cancels out the re-update when setting the int vals
                    setIntValue(vertexFilterBitmaskAttrId, id, bitmask);
                    avoidUpdate = false;
                }
                final float existingVisibility = getFloatValue(vertexFilterVisibilityAttrId, id);
                if ((bitmask & currentVisibleMask) > 0) {
                    // A value > 0 indicates the object is mapped to at least one visible layer
                    if (existingVisibility != 1.0f) {
                        attributeDescriptions[vertexFilterVisibilityAttrId].setFloat(id, 1.0f);
                        attributeIndices[vertexFilterVisibilityAttrId].updateElement(id);
                        attributeModificationCounters[vertexFilterVisibilityAttrId] += operationMode.getModificationIncrement();
                    }
                } else {
                    // A value = 0 indicates the object is not mapped to at least one visible layer
                    if (existingVisibility != 0.0f) {
                        attributeDescriptions[vertexFilterVisibilityAttrId].setFloat(id, 0.0f);
                        attributeIndices[vertexFilterVisibilityAttrId].updateElement(id);
                        attributeModificationCounters[vertexFilterVisibilityAttrId] += operationMode.getModificationIncrement();
                    }
                }
            }
        } else if (elementType == GraphElementType.TRANSACTION) {
            if (transactionFilterBitmaskAttrId >= 0 && transactionFilterVisibilityAttrId >= 0) {
                if (attribute != transactionFilterBitmaskAttrId) {
                    avoidUpdate = true;
                    setIntValue(transactionFilterBitmaskAttrId, id, bitmask);
                    avoidUpdate = false;
                }
                final float existingVisibility = getFloatValue(transactionFilterVisibilityAttrId, id);
                if ((bitmask & currentVisibleMask) > 0) {
                    if (existingVisibility != 1.0f) {
                        attributeDescriptions[transactionFilterVisibilityAttrId].setFloat(id, 1.0f);
                        attributeIndices[transactionFilterVisibilityAttrId].updateElement(id);
                        attributeModificationCounters[transactionFilterVisibilityAttrId] += operationMode.getModificationIncrement();
                    }
                } else {
                    if (existingVisibility != 0.0f) {
                        attributeDescriptions[transactionFilterVisibilityAttrId].setFloat(id, 0.0f);
                        attributeIndices[transactionFilterVisibilityAttrId].updateElement(id);
                        attributeModificationCounters[transactionFilterVisibilityAttrId] += operationMode.getModificationIncrement();
                    }
                }
            }
        }
    }

    // Check attribute being changed and make a decision on whehter object visibility needs to be recalculated
    private void updateBitmask(final int attribute, final int id) {
        if (attribute != vertexFilterVisibilityAttrId && attribute != transactionFilterVisibilityAttrId) {
            // Extract graphs current selected filter bitmask and element type use to recalculate element bitmask
            int selectedFilterBitmask = (selectedFilterBitmaskAttrId >= 0) ? getIntValue(selectedFilterBitmaskAttrId, 0) : 1;
            GraphElementType elementType = getAttributeElementType(attribute);

            // Preloading layerPrefs JIT incase of no instantiation
            if (layerPrefs.isEmpty()) {
                for (int j = 0; j < 31; j++) {
                    layerPrefs.add((byte) 0b0);
                }
            }
            updateBitmask(elementType, attribute, selectedFilterBitmask, id);
        }
    }

    // Force a visibility update of all objects
    private void updateAllBitmasks() {
        int selectedFilterBitmask = (selectedFilterBitmaskAttrId >= 0) ? getIntValue(selectedFilterBitmaskAttrId, 0) : 1;

        currentVisibleMask = selectedFilterBitmask;
        recalculateVisibilities();
        // Loop through all vertexes and recalculate bitmasks
        final int vertexCount = getVertexCount();
        for (int i = 0; i < vertexCount; i++) {
            updateBitmask(GraphElementType.VERTEX, -1, selectedFilterBitmask, vStore.getElement(i));
        }

        // Loop through all transactions and recalculate bitmasks
        final int transactionCount = getTransactionCount();
        for (int i = 0; i < transactionCount; i++) {
            updateBitmask(GraphElementType.TRANSACTION, -1, selectedFilterBitmask, tStore.getElement(i));
        }
    }

    @Override
    public void setByteValue(final int attribute, final int id, final byte value) {
        if (graphEdit == null) {
            attributeDescriptions[attribute].setByte(id, value);
            attributeIndices[attribute].updateElement(id);
            attributeModificationCounters[attribute] += operationMode.getModificationIncrement();
            globalModificationCounter += operationMode.getModificationIncrement();
            int keyType = primaryKeyLookup[attribute];
            if (keyType >= 0) {
                removeFromIndex(keyType, id);
            }
        } else {
            AttributeDescription description = attributeDescriptions[attribute];
            NativeAttributeType nativeType = description.getNativeType();
            nativeType.get(this, attribute, id, oldValue);

            description.setByte(id, value);

            if (nativeType.addEdit(this, graphEdit, attribute, id, oldValue)) {
                attributeIndices[attribute].updateElement(id);
                attributeModificationCounters[attribute] += operationMode.getModificationIncrement();
                globalModificationCounter += operationMode.getModificationIncrement();
                int keyType = primaryKeyLookup[attribute];
                if (keyType >= 0) {
                    removeFromIndex(keyType, id);
                }
            }
        }
        updateBitmask(attribute, id);
    }

    @Override
    public void setShortValue(final int attribute, final int id, final short value) {
        if (graphEdit == null) {
            attributeDescriptions[attribute].setShort(id, value);
            attributeIndices[attribute].updateElement(id);
            attributeModificationCounters[attribute] += operationMode.getModificationIncrement();
            globalModificationCounter += operationMode.getModificationIncrement();
            int keyType = primaryKeyLookup[attribute];
            if (keyType >= 0) {
                removeFromIndex(keyType, id);
            }
        } else {
            AttributeDescription description = attributeDescriptions[attribute];
            NativeAttributeType nativeType = description.getNativeType();
            nativeType.get(this, attribute, id, oldValue);

            description.setShort(id, value);

            if (nativeType.addEdit(this, graphEdit, attribute, id, oldValue)) {
                attributeIndices[attribute].updateElement(id);
                attributeModificationCounters[attribute] += operationMode.getModificationIncrement();
                globalModificationCounter += operationMode.getModificationIncrement();
                int keyType = primaryKeyLookup[attribute];
                if (keyType >= 0) {
                    removeFromIndex(keyType, id);
                }
            }
        }
        updateBitmask(attribute, id);
    }

    @Override
    public void setIntValue(final int attribute, final int id, final int value) {
        if (graphEdit == null) {
            attributeDescriptions[attribute].setInt(id, value);
            attributeIndices[attribute].updateElement(id);
            attributeModificationCounters[attribute] += operationMode.getModificationIncrement();
            globalModificationCounter += operationMode.getModificationIncrement();
            int keyType = primaryKeyLookup[attribute];
            if (keyType >= 0) {
                removeFromIndex(keyType, id);
            }
        } else {
            AttributeDescription description = attributeDescriptions[attribute];
            NativeAttributeType nativeType = description.getNativeType();
            nativeType.get(this, attribute, id, oldValue);

            description.setInt(id, value);

            if (nativeType.addEdit(this, graphEdit, attribute, id, oldValue)) {
                attributeIndices[attribute].updateElement(id);
                attributeModificationCounters[attribute] += operationMode.getModificationIncrement();
                globalModificationCounter += operationMode.getModificationIncrement();
                int keyType = primaryKeyLookup[attribute];
                if (keyType >= 0) {
                    removeFromIndex(keyType, id);
                }
            }
        }

        // If the selected layers to display value has changed then recalculate visibility of all objects, otherwise,
        // check if the change impacts an objects visibility
        if (selectedFilterBitmaskAttrId >= 0 && selectedFilterBitmaskAttrId == attribute) {
            // one node changes, pass to change list then only iterate that?
            updateAllBitmasks(); // hits here when changing layers only. not on update of elements to layers
        } else if (!avoidUpdate) {
            updateBitmask(attribute, id);
        }
    }

    @Override
    public void setLongValue(final int attribute, final int id, final long value) {
        if (graphEdit == null) {
            attributeDescriptions[attribute].setLong(id, value);
            attributeIndices[attribute].updateElement(id);
            attributeModificationCounters[attribute] += operationMode.getModificationIncrement();
            globalModificationCounter += operationMode.getModificationIncrement();
            int keyType = primaryKeyLookup[attribute];
            if (keyType >= 0) {
                removeFromIndex(keyType, id);
            }
        } else {
            AttributeDescription description = attributeDescriptions[attribute];
            NativeAttributeType nativeType = description.getNativeType();
            nativeType.get(this, attribute, id, oldValue);

            description.setLong(id, value);

            if (nativeType.addEdit(this, graphEdit, attribute, id, oldValue)) {
                attributeIndices[attribute].updateElement(id);
                attributeModificationCounters[attribute] += operationMode.getModificationIncrement();
                globalModificationCounter += operationMode.getModificationIncrement();
                int keyType = primaryKeyLookup[attribute];
                if (keyType >= 0) {
                    removeFromIndex(keyType, id);
                }
            }
        }
        updateBitmask(attribute, id);
    }

    @Override
    public void setFloatValue(final int attribute, final int id, final float value) {
        if (graphEdit == null) {
            attributeDescriptions[attribute].setFloat(id, value);
            attributeIndices[attribute].updateElement(id);
            attributeModificationCounters[attribute] += operationMode.getModificationIncrement();
            globalModificationCounter += operationMode.getModificationIncrement();
            int keyType = primaryKeyLookup[attribute];
            if (keyType >= 0) {
                removeFromIndex(keyType, id);
            }
        } else {
            AttributeDescription description = attributeDescriptions[attribute];
            NativeAttributeType nativeType = description.getNativeType();
            nativeType.get(this, attribute, id, oldValue);

            description.setFloat(id, value);

            if (nativeType.addEdit(this, graphEdit, attribute, id, oldValue)) {
                attributeIndices[attribute].updateElement(id);
                attributeModificationCounters[attribute] += operationMode.getModificationIncrement();
                globalModificationCounter += operationMode.getModificationIncrement();
                int keyType = primaryKeyLookup[attribute];
                if (keyType >= 0) {
                    removeFromIndex(keyType, id);
                }
            }
        }
        // when not any movement of nodes
        if(attribute != vXAttrId && attribute != vYAttrId  &&attribute != vZAttrId && attribute != tXAttrId && attribute != tYAttrId  &&attribute != tZAttrId ){
            updateBitmask(attribute, id);
        }
        
    }

    @Override
    public void setDoubleValue(final int attribute, final int id, final double value) {
        if (graphEdit == null) {
            attributeDescriptions[attribute].setDouble(id, value);
            attributeIndices[attribute].updateElement(id);
            attributeModificationCounters[attribute] += operationMode.getModificationIncrement();
            globalModificationCounter += operationMode.getModificationIncrement();
            int keyType = primaryKeyLookup[attribute];
            if (keyType >= 0) {
                removeFromIndex(keyType, id);
            }
        } else {
            AttributeDescription description = attributeDescriptions[attribute];
            NativeAttributeType nativeType = description.getNativeType();
            nativeType.get(this, attribute, id, oldValue);

            description.setDouble(id, value);

            if (nativeType.addEdit(this, graphEdit, attribute, id, oldValue)) {
                attributeIndices[attribute].updateElement(id);
                attributeModificationCounters[attribute] += operationMode.getModificationIncrement();
                globalModificationCounter += operationMode.getModificationIncrement();
                int keyType = primaryKeyLookup[attribute];
                if (keyType >= 0) {
                    removeFromIndex(keyType, id);
                }
            }
        }
        updateBitmask(attribute, id);
    }

    @Override
    public void setBooleanValue(final int attribute, final int id, final boolean value) {
        if (graphEdit == null) {
            attributeDescriptions[attribute].setBoolean(id, value);
            attributeIndices[attribute].updateElement(id);
            attributeModificationCounters[attribute] += operationMode.getModificationIncrement();
            globalModificationCounter += operationMode.getModificationIncrement();
            int keyType = primaryKeyLookup[attribute];
            if (keyType >= 0) {
                removeFromIndex(keyType, id);
            }
        } else {
            AttributeDescription description = attributeDescriptions[attribute];
            NativeAttributeType nativeType = description.getNativeType();
            nativeType.get(this, attribute, id, oldValue);

            description.setBoolean(id, value);

            if (nativeType.addEdit(this, graphEdit, attribute, id, oldValue)) {
                attributeIndices[attribute].updateElement(id);
                attributeModificationCounters[attribute] += operationMode.getModificationIncrement();
                globalModificationCounter += operationMode.getModificationIncrement();
                int keyType = primaryKeyLookup[attribute];
                if (keyType >= 0) {
                    removeFromIndex(keyType, id);
                }
            }
        }
        if(attribute != vSelectedAttrId && attribute != tSelectedAttrId){ // selected node ignore
            updateBitmask(attribute, id);
        }
        
    }

    @Override
    public void setCharValue(final int attribute, final int id, final char value) {
        if (graphEdit == null) {
            attributeDescriptions[attribute].setChar(id, value);
            attributeIndices[attribute].updateElement(id);
            attributeModificationCounters[attribute] += operationMode.getModificationIncrement();
            globalModificationCounter += operationMode.getModificationIncrement();
            int keyType = primaryKeyLookup[attribute];
            if (keyType >= 0) {
                removeFromIndex(keyType, id);
            }
        } else {
            AttributeDescription description = attributeDescriptions[attribute];
            NativeAttributeType nativeType = description.getNativeType();
            nativeType.get(this, attribute, id, oldValue);

            description.setChar(id, value);

            if (nativeType.addEdit(this, graphEdit, attribute, id, oldValue)) {
                attributeIndices[attribute].updateElement(id);
                attributeModificationCounters[attribute] += operationMode.getModificationIncrement();
                globalModificationCounter += operationMode.getModificationIncrement();
                int keyType = primaryKeyLookup[attribute];
                if (keyType >= 0) {
                    removeFromIndex(keyType, id);
                }
            }
        }
        updateBitmask(attribute, id);
    }

    @Override
    public void setStringValue(final int attribute, final int id, final String value) {
        if (graphEdit == null) {
            attributeDescriptions[attribute].setString(id, value);
            attributeIndices[attribute].updateElement(id);
            attributeModificationCounters[attribute] += operationMode.getModificationIncrement();
            globalModificationCounter += operationMode.getModificationIncrement();
            int keyType = primaryKeyLookup[attribute];
            if (keyType >= 0) {
                removeFromIndex(keyType, id);
            }
        } else {
            AttributeDescription description = attributeDescriptions[attribute];
            NativeAttributeType nativeType = description.getNativeType();
            nativeType.get(this, attribute, id, oldValue);

            description.setString(id, value);

            if (nativeType.addEdit(this, graphEdit, attribute, id, oldValue)) {
                attributeIndices[attribute].updateElement(id);
                attributeModificationCounters[attribute] += operationMode.getModificationIncrement();
                globalModificationCounter += operationMode.getModificationIncrement();
                int keyType = primaryKeyLookup[attribute];
                if (keyType >= 0) {
                    removeFromIndex(keyType, id);
                }
            }
        }
        updateBitmask(attribute, id);
    }

    @Override
    public void setObjectValue(final int attribute, final int id, final Object value) {
        if (graphEdit == null) {
            attributeDescriptions[attribute].setObject(id, value);
            attributeIndices[attribute].updateElement(id);
            attributeModificationCounters[attribute] += operationMode.getModificationIncrement();
            globalModificationCounter += operationMode.getModificationIncrement();
            int keyType = primaryKeyLookup[attribute];
            if (keyType >= 0) {
                removeFromIndex(keyType, id);
            }
        } else {
            AttributeDescription description = attributeDescriptions[attribute];
            NativeAttributeType nativeType = description.getNativeType();
            nativeType.get(this, attribute, id, oldValue);

            description.setObject(id, value);

            if (nativeType.addEdit(this, graphEdit, attribute, id, oldValue)) {
                attributeIndices[attribute].updateElement(id);
                attributeModificationCounters[attribute] += operationMode.getModificationIncrement();
                globalModificationCounter += operationMode.getModificationIncrement();
                int keyType = primaryKeyLookup[attribute];
                if (keyType >= 0) {
                    removeFromIndex(keyType, id);
                }
            }
        } 
        // when not a camera change
        if(attribute != cameraAttrId){
            updateBitmask(attribute, id);
        }
    }

    @Override
    public void setPrimaryKey(final GraphElementType elementType, final int... newPrimaryKeys) {
        for (int newKey : newPrimaryKeys) {
            if (getAttributeElementType(newKey) != elementType) {
                throw new IllegalArgumentException("Key attribute has the wrong element type");
            }
        }

        int[] oldPrimaryKeys = primaryKeys[elementType.ordinal()];
        for (int oldPrimaryKey : oldPrimaryKeys) {
            primaryKeyLookup[oldPrimaryKey] = -1;
            attributeModificationCounters[oldPrimaryKey]++;
            attributeModificationCounter++;
        }

        primaryKeys[elementType.ordinal()] = Arrays.copyOf(newPrimaryKeys, newPrimaryKeys.length);

        for (int attribute : newPrimaryKeys) {
            primaryKeyLookup[attribute] = elementType.ordinal();
            attributeModificationCounters[attribute]++;
            attributeModificationCounter++;
        }

        if (newPrimaryKeys.length > 0) {

            if (elementType == GraphElementType.VERTEX) {
                primaryKeyIndices[elementType.ordinal()] = new ElementKeySet(getVertexCapacity(), elementType);
            } else {
                primaryKeyIndices[elementType.ordinal()] = new TransactionKeySet(getTransactionCapacity(), elementType);
            }

            switch (elementType) {
                case VERTEX:
                    removedFromKeys[elementType.ordinal()] = new ElementList(vStore);
                    break;

                case LINK:
                    removedFromKeys[elementType.ordinal()] = new ElementList(lStore);
                    break;

                case EDGE:
                    removedFromKeys[elementType.ordinal()] = new ElementList(eStore);
                    break;

                case TRANSACTION:
                    removedFromKeys[elementType.ordinal()] = new ElementList(tStore);
                    break;
                default:
                    break;
            }

        } else {
            primaryKeyIndices[elementType.ordinal()] = null;
            removedFromKeys[elementType.ordinal()] = null;
        }

        if (graphEdit != null) {
            graphEdit.setPrimaryKey(elementType, oldPrimaryKeys, primaryKeys[elementType.ordinal()]);
        }

        globalModificationCounter++;
    }

    @Override
    public int[] getPrimaryKey(final GraphElementType elementType) {
        int[] keys = primaryKeys[elementType.ordinal()];
        return Arrays.copyOf(keys, keys.length);
    }

    private class ElementKeySet extends IntHashSet {

        protected final GraphElementType elementType;

        public ElementKeySet(final int capacity, final GraphElementType elementType) {
            super(capacity);
            this.elementType = elementType;
        }

        public ElementKeySet(final ElementKeySet original) {
            super(original);
            this.elementType = original.elementType;
        }

        @Override
        protected int getHash(final int element) {
            int hash = 0;
            for (int attribute : primaryKeys[elementType.ordinal()]) {
                hash = hash * 34829039 ^ attributeDescriptions[attribute].hashCode(element);
            }
            return hash;
        }

        @Override
        protected boolean equals(final int element1, final int element2) {
            for (int attribute : primaryKeys[elementType.ordinal()]) {
                if (!attributeDescriptions[attribute].equals(element1, element2)) {
                    return false;
                }
            }
            return true;
        }
    }

    private class TransactionKeySet extends ElementKeySet {

        public TransactionKeySet(final int capacity, final GraphElementType elementType) {
            super(capacity, elementType);
        }

        public TransactionKeySet(final TransactionKeySet original) {
            super(original);
        }

        @Override
        protected int getHash(final int element) {
            int hash = super.getHash(element);
            hash ^= getTransactionDirection(element) * 113;
            hash ^= getTransactionSourceVertex(element) ^ getTransactionDestinationVertex(element);
            return hash;
        }

        @Override
        protected boolean equals(final int element1, final int element2) {
            if (!super.equals(element1, element2)) {
                return false;
            }
            if (getTransactionDirection(element1) != getTransactionDirection(element2)) {
                return false;
            }
            if (getTransactionSourceVertex(element1) != getTransactionSourceVertex(element2)) {
                return false;
            }
            return getTransactionDestinationVertex(element1) == getTransactionDestinationVertex(element2);
        }
    }

    @Override
    public GraphKey getPrimaryKeyValue(final GraphElementType elementType, final int id) {
        int[] primaryKeyAttributes = primaryKeys[elementType.ordinal()];

        if (primaryKeyAttributes.length == 0) {
            return null;
        }

        if (elementType == GraphElementType.VERTEX) {
            Object[] elements = new Object[primaryKeyAttributes.length];
            for (int i = 0; i < elements.length; i++) {
                elements[i] = getObjectValue(primaryKeyAttributes[i], id);
            }

            return new GraphKey(elements);

        } else {
            Object[] elements = new Object[primaryKeyAttributes.length];
            for (int i = 0; i < elements.length; i++) {
                elements[i] = getObjectValue(primaryKeyAttributes[i], id);
            }

            int sourceVertex = getTransactionSourceVertex(id);
            GraphKey sourceKey = getPrimaryKeyValue(GraphElementType.VERTEX, sourceVertex);

            int destinationVertex = getTransactionDestinationVertex(id);
            GraphKey destinationKey = getPrimaryKeyValue(GraphElementType.VERTEX, destinationVertex);

            return new GraphKey(sourceKey, destinationKey, getTransactionDirection(id) == Graph.UNDIRECTED, elements);
        }
    }

    @Override
    public String toString() {
        return String.format("[%s; vertices:%d, transactions:%d]", getClass().getName(), getVertexCount(), getTransactionCount());
    }

    protected void save(final int attribute, final int id, final ParameterWriteAccess access) {
        attributeDescriptions[attribute].save(id, access);
    }

    protected void restore(final int attribute, final int id, final ParameterReadAccess access) {
        attributeDescriptions[attribute].restore(id, access);
        attributeModificationCounters[attribute] += operationMode.getModificationIncrement();
        globalModificationCounter += operationMode.getModificationIncrement();
    }

    protected Object saveData(final int attribute) {
        return attributeDescriptions[attribute].saveData();
    }

    protected void restoreData(final int attribute, final Object savedData) {
        attributeDescriptions[attribute].restoreData(savedData);
        attributeModificationCounters[attribute] += operationMode.getModificationIncrement();
        globalModificationCounter += operationMode.getModificationIncrement();
    }

    private void removeFromIndex(final int elementType, final int id) {
        IntHashSet index = primaryKeyIndices[elementType];
        if (index != null && removedFromKeys[elementType].addToBack(id)) {
            index.remove(id);
        }
    }

    @Override
    public boolean attributeSupportsIndexType(int attribute, GraphIndexType indexType) {
        return attributeDescriptions[attribute].supportsIndexType(indexType);
    }

    @Override
    public GraphIndexType getAttributeIndexType(int attribute) {
        return attributeIndexTypes[attribute];
    }

    @Override
    public GraphIndexResult getElementsWithAttributeValue(int attribute, Object value) {
        return attributeIndices[attribute].getElementsWithAttributeValue(value);
    }

    @Override
    public GraphIndexResult getElementsWithAttributeValueRange(int attribute, Object start, Object end) {
        return attributeIndices[attribute].getElementsWithAttributeValueRange(start, end);
    }

    @Override
    public void setAttributeIndexType(int attribute, GraphIndexType indexType) {
        GraphIndexType oldIndexType = attributeIndexTypes[attribute];
        if (indexType != oldIndexType) {
            AttributeDescription attributeDescription = attributeDescriptions[attribute];
            if (attributeDescription.supportsIndexType(indexType)) {
                attributeIndexTypes[attribute] = indexType;
                GraphIndex index = attributeIndices[attribute] = attributeDescription.createIndex(indexType);

                GraphElementType elementType = attributes[attribute].getElementType();
                int elementCount = elementType.getElementCount(this);
                for (int i = 0; i < elementCount; i++) {
                    int element = elementType.getElement(this, i);
                    index.addElement(element);
                }

                if (graphEdit != null) {
                    graphEdit.setAttributeIndexType(attribute, oldIndexType, indexType);
                }
            }
        }
    }

    @Override
    public IntStream vertexStream() {
        return StreamSupport.intStream(new VertexSpliterator(this), false);
    }

    @Override
    public IntStream linkStream() {
        return StreamSupport.intStream(new LinkSpliterator(this), false);
    }

    @Override
    public IntStream edgeStream() {
        return StreamSupport.intStream(new EdgeSpliterator(this), false);
    }

    @Override
    public IntStream transactionStream() {
        return StreamSupport.intStream(new TransactionSpliterator(this), false);
    }

    public AttributeRegistry getAttributeRegistry() {
        return attributeRegistry;
    }

}
