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
package au.gov.asd.tac.constellation.views.attributeeditor;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphIndexResult;
import au.gov.asd.tac.constellation.graph.GraphIndexType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.framework.VisualGraphDefaults;
import au.gov.asd.tac.constellation.utilities.graphics.IntArray;
import au.gov.asd.tac.constellation.utilities.text.StringUtilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * The AttributeReader is responsible for populating the data model for
 * Constellation's attribute editor. It keeps a reference to the current graph,
 * and contains a number of methods that perform different types of updates in
 * response to various graph changes.
 * <br>
 * The data model which this class populates is an {@link AttributeState}
 * object. Some of this data is also stored in this class - this should be
 * changed in the future so that the model either belongs to the the class that
 * uses it, or alternatively is stored on the graph.
 * <br>
 * Note that this class doesn't listen to the graph - this is the role of the
 * {@link AttributeEditorTopComponent}.
 *
 * @author twinkle2_little
 */
public class AttributeReader {
    
    private static final Logger LOGGER = Logger.getLogger(AttributeReader.class.getName());

    private final Graph graph;
    private long lastAttributeModificationCount = -1;
    private long lastNodeSelectedModificationCount = -1;
    private long lastTransactionSelectedModificationcount = -1;
    private final Preferences prefs = NbPreferences.forModule(AttributePreferenceKey.class);
    private final IntArray selectedTransactions = new IntArray();
    private final IntArray selectedNodes = new IntArray();
    private final Map<GraphElementType, List<AttributeData>> elementAttributeData = new HashMap<>();
    private final Map<GraphElementType, Integer> elementAttributeCounts = new HashMap<>();
    
    // type appended with attribute name as key.
    private final HashMap<String, Object[]> elementAttributeValues = new HashMap<>();

    private static final List<GraphElementType> ACCEPTED_ELEMENT_TYPES = Arrays.asList(GraphElementType.GRAPH, GraphElementType.VERTEX, GraphElementType.TRANSACTION);

    private static final String UPDATE_SELECTED_NODE_THREAD_NAME = "Update Selected Node";

    public AttributeReader(final Graph graph) {
        this.graph = graph;
    }

    /**
     * Gets the attribute's name and values from the graph and returns an
     * AttributeState.
     *
     * @return The state of all the selected attributes.
     */
    public AttributeState refreshAttributes() {
        return refreshAttributes(false);
    }

    /**
     * Gets the attribute's name and values from the graph and returns an
     * AttributeState.
     *
     * @param preferenceChanged - since there are not modification counter for a
     * preference a boolean needs to be passed to determine if a preference has
     * changed.
     * @return The state of all the selected attributes.
     */
    public AttributeState refreshAttributes(final boolean preferenceChanged) {
        AttributeState result = null;
        final ArrayList<GraphElementType> activeElementTypes = new ArrayList<>();
        boolean selectionModified = false;
        boolean attributeModified = false;
        boolean valueModified = false;

        // show all preferences
        final Map<GraphElementType, Boolean> showAllPrefs = new HashMap<>();
        showAllPrefs.put(GraphElementType.GRAPH, prefs.getBoolean(AttributePreferenceKey.GRAPH_SHOW_ALL, false));
        showAllPrefs.put(GraphElementType.VERTEX, prefs.getBoolean(AttributePreferenceKey.NODE_SHOW_ALL, false));
        showAllPrefs.put(GraphElementType.TRANSACTION, prefs.getBoolean(AttributePreferenceKey.TRANSACTION_SHOW_ALL, false));
        
        final List<String> hiddenAttrs = StringUtilities.splitLabelsWithEscapeCharacters(prefs.get(AttributePreferenceKey.HIDDEN_ATTRIBUTES, ""), AttributePreferenceKey.SPLIT_CHAR_SET);
        final Set<String> hiddenAttrsSet = new HashSet<>(hiddenAttrs);

        final ReadableGraph rg = graph.getReadableGraph();
        try {
            selectionModified = updateSelectedElements(rg);
            updateElementTypes(activeElementTypes);

            final long currAttributeModificationCount = rg.getAttributeModificationCounter();
            attributeModified = currAttributeModificationCount != lastAttributeModificationCount;
            lastAttributeModificationCount = currAttributeModificationCount;
            if (attributeModified || preferenceChanged) {
                updateElementAttributeNames(rg, ACCEPTED_ELEMENT_TYPES, hiddenAttrsSet, showAllPrefs);
            }
            final List<GraphElementType> toPopulate = new ArrayList<>(activeElementTypes);
            toPopulate.add(GraphElementType.GRAPH);
            valueModified = populateValues(toPopulate, selectionModified, attributeModified, preferenceChanged, rg
            );

        } finally {
            rg.release();
        }
        if (selectionModified || attributeModified || valueModified || preferenceChanged) {
            result = new AttributeState(ACCEPTED_ELEMENT_TYPES, selectionModified ? activeElementTypes : Collections.emptyList(), elementAttributeData, elementAttributeValues, elementAttributeCounts);
        }

        return result;

    }

    private boolean updateSelectedElements(final ReadableGraph rg) {
        final int selectedNodeAttribute = rg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());
        final int selectedTransactionAttribute = rg.getAttribute(GraphElementType.TRANSACTION, VisualConcept.TransactionAttribute.SELECTED.getName());
        final long currentNodeSelectedModificationCount = selectedNodeAttribute != Graph.NOT_FOUND ? rg.getValueModificationCounter(selectedNodeAttribute) : 0;
        final long currentTransactionSelectedModificationcount = selectedTransactionAttribute != Graph.NOT_FOUND ? rg.getValueModificationCounter(selectedTransactionAttribute) : 0;
        final boolean nodeSelectionChanged = currentNodeSelectedModificationCount != lastNodeSelectedModificationCount;
        final boolean transactionSelectionChanged = currentTransactionSelectedModificationcount != lastTransactionSelectedModificationcount;
        lastNodeSelectedModificationCount = currentNodeSelectedModificationCount;
        lastTransactionSelectedModificationcount = currentTransactionSelectedModificationcount;
        Thread selectedNodethread = null;

        if (nodeSelectionChanged) {
            selectedNodes.clear();
            // find selected nodes
            selectedNodethread = new Thread() {
                @Override
                public void run() {
                    final GraphIndexType selectedIndexType = selectedNodeAttribute != Graph.NOT_FOUND ? rg.getAttributeIndexType(selectedNodeAttribute) : GraphIndexType.NONE;
                    if (selectedIndexType == GraphIndexType.NONE) {
                        for (int i = 0; i < rg.getVertexCount(); i++) {
                            final boolean selected = selectedNodeAttribute != Graph.NOT_FOUND ? rg.getBooleanValue(selectedNodeAttribute, rg.getVertex(i)) : VisualGraphDefaults.DEFAULT_VERTEX_SELECTED;
                            if (selected) {
                                selectedNodes.add(rg.getVertex(i));
                            }
                        }
                    } else {
                        final GraphIndexResult result = rg.getElementsWithAttributeValue(selectedNodeAttribute, Boolean.TRUE);
                        final int resultCount = result.getCount();
                        for (int i = 0; i < resultCount; i++) {
                            selectedNodes.add(result.getNextElement());
                        }
                    }

                }
            };
            selectedNodethread.setName(UPDATE_SELECTED_NODE_THREAD_NAME);
            selectedNodethread.start();
        }
        // find selected transactions
        if (transactionSelectionChanged) {
            selectedTransactions.clear();
            final GraphIndexType selectedIndexType = selectedTransactionAttribute != Graph.NOT_FOUND ? rg.getAttributeIndexType(selectedTransactionAttribute) : GraphIndexType.NONE;
            if (selectedIndexType == GraphIndexType.NONE) {
                for (int i = 0; i < rg.getTransactionCount(); i++) {
                    final boolean selected = selectedTransactionAttribute != Graph.NOT_FOUND ? rg.getBooleanValue(selectedTransactionAttribute, rg.getTransaction(i)) : VisualGraphDefaults.DEFAULT_TRANSACTION_SELECTED;
                    if (selected) {
                        selectedTransactions.add(rg.getTransaction(i));
                    }
                }
            } else {
                final GraphIndexResult result = rg.getElementsWithAttributeValue(selectedTransactionAttribute, Boolean.TRUE);
                final int resultCount = result.getCount();
                for (int i = 0; i < resultCount; i++) {
                    selectedTransactions.add(result.getNextElement());
                }
            }
        }

        if (selectedNodethread != null) {
            try {
                selectedNodethread.join();
            } catch (final InterruptedException ex) {
                LOGGER.log(Level.SEVERE, "Thread was interrupted", ex);
                Thread.currentThread().interrupt();
            }
        }
        return (nodeSelectionChanged || transactionSelectionChanged);
    }

    private void updateElementTypes(final ArrayList<GraphElementType> activeElementTypes) {
        activeElementTypes.clear();
        if (selectedNodes.isEmpty() && selectedTransactions.isEmpty()) {
            activeElementTypes.add(GraphElementType.GRAPH);
        }
        if (!selectedNodes.isEmpty()) {
            activeElementTypes.add(GraphElementType.VERTEX);
        }
        if (!selectedTransactions.isEmpty()) {
            activeElementTypes.add(GraphElementType.TRANSACTION);
        }
    }

    private void updateElementAttributeNames(final ReadableGraph rg, final List<GraphElementType> elementTypes, final Set<String> hiddenAttrsSet, final Map<GraphElementType, Boolean> showAllPrefs) {

        elementAttributeData.clear();
        for (final GraphElementType elementType : elementTypes) {

            final int attributeCount = rg.getAttributeCount(elementType);
            final ArrayList<AttributeData> attributeNames = new ArrayList<>();
            final boolean showAll = showAllPrefs.get(elementType);
            for (int i = 0; i < attributeCount; i++) {

                final Attribute attr = new GraphAttribute(rg, rg.getAttribute(elementType, i));
                // do check only if not showing all

                if (!showAll && hiddenAttrsSet.contains(attr.getElementType().toString() + attr.getName())) {
                    continue;
                }

                final boolean isSchemaAttr = rg.getSchema() != null
                        && rg.getSchema().getFactory().getRegisteredAttributes(attr.getElementType()).containsKey(attr.getName());

                final AttributeData attrData = new AttributeData(attr.getName(), attr.getDescription(), attr.getId(), rg.getValueModificationCounter(attr.getId()), elementType, attr.getAttributeType(), attr.getDefaultValue(), rg.isPrimaryKey(attr.getId()), isSchemaAttr);
                attributeNames.add(attrData);
            }
            Collections.sort(attributeNames, (o1, o2) -> o1.getAttributeName().compareTo(o2.getAttributeName()));
            elementAttributeCounts.put(elementType, attributeCount);
            elementAttributeData.put(elementType, attributeNames);
        }
    }

    private boolean populateValues(final List<GraphElementType> elementTypes, final boolean selectionChanged, final boolean attributeModified, final boolean preferenceChanged, final ReadableGraph rg) {
        boolean valueChanged = false;
        if (selectionChanged) {
            elementAttributeValues.clear();
        }
        for (final GraphElementType type : ACCEPTED_ELEMENT_TYPES) { // for graphElement Type (graph,vertex,transaction)
            final List<AttributeData> attributes = elementAttributeData.get(type);
            final IntArray selectedElement;
            if (type.equals(GraphElementType.VERTEX)) {
                selectedElement = selectedNodes;
            } else if (type.equals(GraphElementType.TRANSACTION)) {
                selectedElement = selectedTransactions;
            } else {
                selectedElement = null;
            }
            for (final AttributeData data : attributes) { // for attribute(name, type etc)

                if (!elementTypes.contains(type)) {
                    final String attributeValuesKey = type.getLabel() + data.getAttributeName();
                    elementAttributeValues.put(attributeValuesKey, null);
                    continue;
                }

                if (preferenceChanged || selectionChanged || attributeModified || data.attibuteValueHasChanged(rg.getValueModificationCounter(data.getAttributeId()))) {
                    final Set<Object> values = new HashSet<>();
                    int valueCountLimit = 11;
                    // only load 10 values first... if the user wants more then another request is made. we load 11 to know that there are more than 10
                    if (data.getDataType().equals("boolean")) {
                        valueCountLimit = 2; 
                        // boolean only has two possibilities.
                    }
                    if (selectedElement != null) {
                        for (int i = 0; i < selectedElement.size() && values.size() < valueCountLimit; i++) {
                            values.add(rg.getObjectValue(data.getAttributeId(), selectedElement.get(i)));
                        }
                    } else { 
                        // assumed to be graphelementtype of graph.
                        values.add(rg.getObjectValue(data.getAttributeId(), 0));
                    }
                    if (!values.isEmpty()) {
                        valueChanged = true;
                    }
                    final String attributeValuesKey = type.getLabel() + data.getAttributeName();
                    elementAttributeValues.remove(attributeValuesKey);
                    elementAttributeValues.put(attributeValuesKey, sortHashMap(values));
                }
            }
        }

        return valueChanged;
    }

    public Object[] loadMoreDataFor(final AttributeData attribute) {
        final int AttributeID = attribute.getAttributeId();
        final HashSet<Object> values = new HashSet<>();
        final IntArray selectedElement;
        if (attribute.getElementType().equals(GraphElementType.VERTEX)) {
            selectedElement = selectedNodes;
        } else if (attribute.getElementType().equals(GraphElementType.TRANSACTION)) {
            selectedElement = selectedTransactions;
        } else {
            selectedElement = null;
        }
        final ReadableGraph rg = graph.getReadableGraph();
        try {
            if (selectedElement != null) {
                int elementSize = selectedElement.size();
                for (int i = 0; i < elementSize; i++) {
                    values.add(rg.getObjectValue(AttributeID, selectedElement.get(i)));
                }
            }
        } finally {
            rg.release();
        }
        return sortHashMap(values);
    }

    private Object[] sortHashMap(final Set<Object> values) {
        // If the values are Comparable, compare them.
        // This allows numbers to be sorted correctly, for example.
        if (!values.isEmpty()) {
            final Object o = values.iterator().next();
            if (o instanceof Comparable) {
                final Comparable<Object>[] valuesArray = new Comparable[values.size()];
                values.toArray(valuesArray);
                Arrays.sort(valuesArray, (a, b) -> a == null ? (b == null ? 0 : -1) : (b == null ? 1 : a.compareTo(b)));

                return valuesArray;
            }
        }

        final Object[] valuesArray = values.toArray();

        Arrays.sort(valuesArray, (final Object o1, final Object o2) -> {
            if ((o1 == null) || (o1.toString() == null)) {
                return 1;
            }
            if ((o2 == null) || (o2.toString() == null)) {
                return -1;
            }
            return o1.toString().compareToIgnoreCase(o2.toString());
        });
        return valuesArray;
    }
}
