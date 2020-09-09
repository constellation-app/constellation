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

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.value.Access;
import au.gov.asd.tac.constellation.graph.value.readables.BooleanReadable;
import au.gov.asd.tac.constellation.graph.value.readables.IntReadable;

/**
 *
 * @author sirius
 */
public class BitMaskQuery {

    public static final String DEFAULT_QUERY_STRING = "Default";
    public static final String DEFAULT_QUERY_DESCRIPTION = "Show All";

    private Query query;
    private String description;
    private final int bitIndex;
    private boolean visible;
    private final long mask;
    private BooleanReadable result;

    public BitMaskQuery(Query query, int bitIndex, final String description) {
        this.query = query;
        this.description = description;
        this.bitIndex = bitIndex;
        this.mask = 0xFFFFFFFFFFFFFFFFL ^ (1L << bitIndex); // TODO: What is the mask used for?
    }

    public BitMaskQuery(final BitMaskQuery copy) {
        this.query = copy.query;
        this.description = copy.description;
        this.bitIndex = copy.bitIndex;
        this.visible = copy.visible;
        this.mask = 0xFFFFFFFFFFFFFFFFL ^ (1L << copy.bitIndex);
        this.result = copy.result;
    }

    public String getDescription() {
        return description;
    }

    public int getIndex() {
        return bitIndex;
    }

    public boolean getVisibility() {
        return visible;
    }

    public void setVisibility(final boolean visible) {
        this.visible = visible;
    }

    public void setQueryString(final String queryString) {
        if (isQueryLayer()) {
            this.query.setQueryString(queryString);
        }
        // meaning query is just a manually added layer
    }

    public void setQuery(final Query query) {
        this.query = query;
    }

    public Query getQuery() {
        return query;
    }

    public boolean isQueryLayer() {
        return query != null;
    }

    // can now return null
    public String getQueryString() {
        return isQueryLayer() ? query.getQueryString() : null;
    }
// can now return null

    public GraphElementType getQueryElementType() {
        return isQueryLayer() ? query.getQueryElementType() : null;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public boolean isActive(long activeQueryBitMask) {
        return (activeQueryBitMask & (1 << bitIndex)) == 0;
    }

    // check if the layer is defined by a query - only update if it is
    public boolean update(GraphReadMethods graph, IntReadable index) {
        if (query == null || query.requiresUpdate(graph)) { // query == empty, or it requires an update
            if (isQueryLayer()) {
                final Object compiledExpresssion = query.compile(graph, index);
                this.result = Access.getDefault().getRegistry(BooleanReadable.class).convert(compiledExpresssion);
            }
            return true;
        } else {
            return false;
        }
    }

    //original = 5
    //result = null
    // bitindex = 3
    // returns = 5
    public long updateBitMask(long original) {
        if (result != null && result.readBoolean()) { // result isnt null, and get bool.
            return original | (1L << bitIndex);
        } else {
            return original & mask;
        }
    }
}
