/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.layers.utilities;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.value.values.IntValue;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author sirius
 */
public class BitMaskQueryCollection {

    private static final Logger LOGGER = Logger.getLogger(BitMaskQueryCollection.class.getName());

    // layer query still gets cleared when selecting.. - maybe null off by one index?
    // overrides bitmasks on some elements.
    // concurrent mod on active queries without the future
    // query can now be null - to handle layers without queries
    // deselect all queries doesn't work
    // avoid default layer - placeholder for show all elements?
    // unable to find assign when querying Label = 's';
    // Added getQueriesCount method
    private final List<BitMaskQuery> queries;

    private final IntValue index = new IntValue();

    private final List<BitMaskQuery> activeQueries = new ArrayList<>();
    private final List<BitMaskQuery> updateQueries = new ArrayList<>();

    public BitMaskQueryCollection(List<BitMaskQuery> queries) {
        this.queries = queries;
    }

    public void setActiveQueries(long activeQueryBitMask) {
        activeQueries.clear();
        for (BitMaskQuery bitMaskQuery : queries) {
            boolean result = bitMaskQuery.isActive(activeQueryBitMask);
            LOGGER.log(Level.WARNING, bitMaskQuery.getDescription() + " :" + bitMaskQuery.getIndex() + "active mask = " + activeQueryBitMask + (result ? "Success" : "fail"));
            if (result) {
                activeQueries.add(bitMaskQuery);
            }
        }
    }

    public int getQueriesCount() {
        return queries.size();
    }

    public List<BitMaskQuery> getQueries() {
        return queries;
    }

    public boolean update(GraphReadMethods graph) {
        updateQueries.clear();
        for (BitMaskQuery activeQuery : activeQueries) {
            if (!BitMaskQuery.DEFAULT_QUERY_STRING.equals(activeQuery.getQueryString()) && activeQuery.update(graph, index)) {
                updateQueries.add(activeQuery);
            }
        }
        return !updateQueries.isEmpty();
    }

    public long updateBitMask(long bitMask) {
        for (BitMaskQuery updateQuery : updateQueries) {
            bitMask = updateQuery.updateBitMask(bitMask);
        }
        return bitMask;
    }

    public void updateBitMasks(GraphWriteMethods graph, int bitMaskAttributeId, int visibleAttributeId, GraphElementType elementType, long activeQueriesBitMask) {
        setActiveQueries(activeQueriesBitMask);
        if (update(graph)) {
            final int elementCount = elementType.getElementCount(graph);
            for (int position = 0; position < elementCount; position++) {
                index.writeInt(position);
                final int elementId = elementType.getElement(graph, position);
                if (bitMaskAttributeId == Graph.NOT_FOUND) {
                    final long bitMask = 0;
                    final long updatedBitMask = updateBitMask(bitMask);
                    graph.setFloatValue(visibleAttributeId, elementId, updatedBitMask == 0 ? 0.0f : 1.0f);
                } else {
                    final long bitMask = graph.getLongValue(bitMaskAttributeId, elementId);
                    final long updatedBitMask = updateBitMask(bitMask);
                    //graph.setLongValue(bitMaskAttributeId, elementId, updatedBitMask); // not needed to re-set the bit value on the node ?
                    graph.setFloatValue(visibleAttributeId, elementId, (updatedBitMask & activeQueriesBitMask) == 0 ? 0.0f : 1.0f);
                }
            }
        }
    }

    /**
     * Set the default layers to the ones defined below
     */
    public void setDefaultQueries() {
        queries.clear();
        queries.addAll(getDefaultQueries());
    }

    public static List<BitMaskQuery> getDefaultQueries() {
        final List<BitMaskQuery> defaultQueries = new ArrayList<>();
        defaultQueries.add(new BitMaskQuery(new Query(GraphElementType.VERTEX, BitMaskQuery.DEFAULT_QUERY_STRING), 1, BitMaskQuery.DEFAULT_QUERY_DESCRIPTION));
        defaultQueries.add(new BitMaskQuery(null, 2, StringUtils.EMPTY));
        return defaultQueries;
    }

    public void add(final BitMaskQuery layer) {
        queries.add(layer);
    }

    public void add(final Query query, final int bitIndex, final String description) {
        queries.add(new BitMaskQuery(query, bitIndex, description));
    }

    // clear queries and add fresh ones.
    public void setQueries(List<BitMaskQuery> queries) {
        this.queries.clear();
        this.queries.addAll(queries);
    }

    public List<SchemaAttribute> getListenedAttributes(final long currentBitMask) {
        final List<SchemaAttribute> attributes = new ArrayList<>();
//        for (final BitMaskQuery query : queries) {
//            if (!query.isActive(currentBitMask) || StringUtils.isEmpty(query.getQueryString()) || BitMaskQuery.DEFAULT_QUERY_STRING.equals(query.getQueryString())) {
//                continue;
//            }
//
//            ExpressionParser.SequenceExpression expression = ExpressionParser.parse(query.getQueryString());
//            for (final ExpressionParser.Expression exp : expression.getChildren()) {
//                if (exp instanceof ExpressionParser.VariableExpression) {
//                    attributes.add(SchemaAttributeUtilities.getAttribute(query.getQueryElementType(), ((ExpressionParser.VariableExpression) exp).getContent()));
//                    // TODO: Check this cast is ok to do ?
//                }
//            }
//        }
//        attributes.removeIf(item -> item == null);

        return attributes;
    }

    public void setVisibilityOnAll(final boolean visibility) {
        for (final BitMaskQuery query : queries) {
            query.setVisibility(visibility);
        }
    }

    public void clear() {
        queries.clear();
    }

}
