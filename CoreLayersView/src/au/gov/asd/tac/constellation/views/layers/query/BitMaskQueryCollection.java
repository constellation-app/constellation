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
package au.gov.asd.tac.constellation.views.layers.query;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttributeUtilities;
import au.gov.asd.tac.constellation.graph.value.values.IntValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * Collection of all query bit masks in layers view
 * Contains lists of all queries, which queries are active and which require updates
 * 
 * @author sirius
 */
public class BitMaskQueryCollection {

    public static final int MAX_QUERY_AMT = 64;
    private static final String INVALID_INDEX_ERROR = " is not a valid index for a layer";
    private final BitMaskQuery[] queries = new BitMaskQuery[MAX_QUERY_AMT];
    private final GraphElementType elementType;
    private final IntValue index = new IntValue();

    public static BitMaskQuery[] getDefaultVxQueries() {
        return new BitMaskQuery[]{
            new BitMaskQuery(new Query(null, BitMaskQuery.DEFAULT_QUERY_STRING), 0, BitMaskQuery.DEFAULT_QUERY_DESCRIPTION),
            new BitMaskQuery(new Query(GraphElementType.VERTEX, null), 1, StringUtils.EMPTY)
        };
    }

    public static BitMaskQuery[] getDefaultTxQueries() {
        return new BitMaskQuery[]{
            new BitMaskQuery(new Query(null, BitMaskQuery.DEFAULT_QUERY_STRING), 0, BitMaskQuery.DEFAULT_QUERY_DESCRIPTION),
            new BitMaskQuery(new Query(GraphElementType.TRANSACTION, null), 1, StringUtils.EMPTY)
        };
    }

    // queries currently active
    private final List<BitMaskQuery> activeQueries = new ArrayList<>();

    // queries which need updating
    private final List<BitMaskQuery> updateQueries = new ArrayList<>();

    // currently active query layers bitmask
    private long activeQueriesBitMask = 0;

    public BitMaskQueryCollection(final GraphElementType elementType) {
        this.elementType = elementType;
    }

    protected List<BitMaskQuery> getActiveQueries() {
        return Collections.unmodifiableList(activeQueries);
    }

    /**
     * Set a query at a specific bit mask index
     * 
     * @param query
     * @param bitMaskIndex
     */
    public void setQuery(final String query, final int bitMaskIndex) {
        if (bitMaskIndex > MAX_QUERY_AMT) {
            throw new IndexOutOfBoundsException(bitMaskIndex + INVALID_INDEX_ERROR);
        }
        queries[bitMaskIndex] = new BitMaskQuery(new Query(elementType, query), bitMaskIndex, StringUtils.EMPTY);
    }

    /**
     * Determine which queries are currently active on the graph
     * 
     * @param activeQueriesBitMask
     */
    public void setActiveQueries(final long activeQueriesBitMask) {
        this.activeQueriesBitMask = activeQueriesBitMask;
        activeQueries.clear();
        boolean anySelected = false;
        for (final BitMaskQuery query : queries) {
            if (query != null && query.isVisible()) {
                activeQueries.add(query);
                anySelected = true;
            }
        }
        if (!anySelected) {
            for (final BitMaskQuery query : queries) {
                if (query != null && query.getIndex() == 0) {
                    activeQueries.add(query);
                }
            }
        }
    }

    /**
     * Get the highest bit mask index
     *
     * @return highest index
     */
    public int getHighestQueryIndex() {
        int highestIndex = 0;
        for (final BitMaskQuery bitMaskQuery : queries) {
            if (bitMaskQuery != null) {
                highestIndex = bitMaskQuery.getIndex();
            }
        }
        return highestIndex;
    }

    public BitMaskQuery[] getQueries() {
        return queries;
    }

    public BitMaskQuery getQuery(final int index) {
        if (index >= MAX_QUERY_AMT || index < 0) {
            throw new IndexOutOfBoundsException(index + INVALID_INDEX_ERROR);
        }
        return queries[index];
    }

    /**
     * Determine whether the active queries on the graph require updating
     *
     * @param graph
     * @return whether any queries need to be updated
     */
    public boolean update(final GraphReadMethods graph) {
        updateQueries.clear();
        for (final BitMaskQuery activeQuery : activeQueries) {
            if (activeQuery != null && activeQuery.update(graph, index)) {
                updateQueries.add(activeQuery);
            }
        }
        return !updateQueries.isEmpty();
    }

    /**
     * Update the bit mask depending on the current queries
     *
     * @param bitMask
     * @return bitMask
     */
    public long updateBitMask(long bitMask) {
        for (final BitMaskQuery updateQuery : updateQueries) {
            bitMask = updateQuery.updateBitMask(bitMask);
        }
        return bitMask;
    }

    public long updateQueryBitmap(final long bitMask) {
        long resultingBitmap = bitMask;
        for (final BitMaskQuery updateQuery : updateQueries) {
            resultingBitmap = updateQuery.combineBitmap(resultingBitmap);
            }
        return resultingBitmap;        
    }
    
    /**
     * Update the overall bit mask attribute and the attribute for which queries are currently active
     * 
     * @param graph
     * @param bitMaskAttributeId
     * @param visibleAttributeId
     */
    public void updateBitMasks(final GraphWriteMethods graph, final int bitMaskAttributeId, final int visibleAttributeId, boolean unionMode) {
        if (this.update(graph)) {
            final int elementCount = elementType.getElementCount(graph);
            for (int position = 0; position < elementCount; position++) {
                final int elementId = elementType.getElement(graph, position);
                index.writeInt(elementId);
                final long bitMask = graph.getLongValue(bitMaskAttributeId, elementId);
                final long queryCombinedBitMask = updateQueryBitmap(bitMask) & activeQueriesBitMask;
                final long unionResult = bitMask & activeQueriesBitMask;
                if (unionMode) {
                    graph.setFloatValue(visibleAttributeId, elementId, (unionResult == 0 && queryCombinedBitMask == 0)? 0.0F : 1.0F); // union logic - accepts any match
                } else {
                    graph.setFloatValue(visibleAttributeId, elementId, (unionResult | queryCombinedBitMask) != activeQueriesBitMask ? 0.0F : 1.0F); // intersection logic - must match with all
                }
            }
        }
    }

    /**
     * Set the default layers
     */
    public void setDefaultQueries() {
        this.clear();
        if (elementType == GraphElementType.VERTEX) {
            for (final BitMaskQuery query : getDefaultVxQueries()) {
                this.add(query);
            }
        } else if (elementType == GraphElementType.TRANSACTION) {
            for (final BitMaskQuery query : getDefaultTxQueries()) {
                this.add(query);
            }
        } else {
            // Do nothing
        }
    }

    /**
     * Add a new query to the collection if the index is not above the max
     * 
     * @param query
     */
    public void add(final BitMaskQuery query) {
        if (query != null) {
            if (query.getIndex() >= MAX_QUERY_AMT) {
                throw new IndexOutOfBoundsException(query.getIndex() + INVALID_INDEX_ERROR);
            }
            queries[query.getIndex()] = query;
        }
    }

    /**
     * Add a new query to the collection if the index is not above the max
     * 
     * @param query
     * @param bitIndex
     * @param description
     */
    public void add(final Query query, final int bitIndex, final String description) {
        if (bitIndex >= MAX_QUERY_AMT || bitIndex < 0) {
            throw new IndexOutOfBoundsException(bitIndex + INVALID_INDEX_ERROR);
        }
        queries[bitIndex] = new BitMaskQuery(query, bitIndex, description);
    }

    /**
     * Update the bit mask collection with the queries
     * 
     * @param queries
     */
    public void setQueries(final BitMaskQuery[] queries) {
        this.clear();
        for (final BitMaskQuery query : queries) {
            if (query != null) {
                this.queries[query.getIndex()] = query;
            }
        }
    }

    /**
     * Add attributes to the schema
     *
     * @param wg
     * @param currentBitMask
     * @return
     */
    public List<SchemaAttribute> getListenedAttributes(final GraphWriteMethods wg, final long currentBitMask) {
        activeQueriesBitMask = currentBitMask;
        final List<SchemaAttribute> attributes = new ArrayList<>();
        for (final BitMaskQuery query : queries) {
            if (query == null || !query.isVisible() || query.getIndex() == 0 || StringUtils.isEmpty(query.getQueryString())) {
                continue;
            }
            if (query.getQuery() != null && query.getQuery().getAttributeIds() != null) {
                for (final int currentAttrId : query.getQuery().getAttributeIds()) {
                    attributes.add(SchemaAttributeUtilities.getAttribute(this.elementType, wg.getAttributeName(currentAttrId)));
                }
            }
        }
        attributes.removeIf(Objects::isNull);
        
        return attributes;
    }

    public boolean isVisibilityOnAll() {
        boolean visibility = false;
        for (int position = 1; position < queries.length; position++) {
            final BitMaskQuery query = queries[position];
            if (query != null) {
                visibility = query.isVisible();
            }
        }
        return visibility;
    }

    /**
     * Set the visibility of all queries
     *
     * @param visibility
     */
    public void setVisibilityOnAll(final boolean visibility) {
        for (int position = 1; position < queries.length; position++) {
            final BitMaskQuery query = queries[position];
            if (query != null) {
                query.setVisibility(visibility);
            }
        }
    }

    /**
     * Clears the queries collection
     */
    public void clear() {
        for (int position = 0; position < queries.length; position++) {
            queries[position] = null;
        }
    }

    /**
     * Remove a query at the index given
     * 
     * @param index
     */
    public void removeQuery(final int index) {
        if (index >= MAX_QUERY_AMT || index < 0) {
            throw new IndexOutOfBoundsException(index + INVALID_INDEX_ERROR);
        }
        queries[index] = null;
    }

    /**
     * Remove a query, reset its index and shift the position in the array.
     *
     * @param index the query to remove.
     */
    public void removeQueryAndSort(final int index) {
        System.arraycopy(queries, index + 1, queries, index, queries.length - index - 1);
        int queryIndex = 0;
        for (final BitMaskQuery query : queries) {
            if (query != null) {
                query.setIndex(queryIndex);
                queryIndex++;
            }
        }
    }
}
