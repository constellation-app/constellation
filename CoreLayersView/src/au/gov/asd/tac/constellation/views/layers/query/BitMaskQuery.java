/*
 * Copyright 2010-2025 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.value.Access;
import au.gov.asd.tac.constellation.graph.value.readables.BooleanReadable;
import au.gov.asd.tac.constellation.graph.value.readables.IntReadable;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author sirius
 */
public class BitMaskQuery {

    public static final String DEFAULT_QUERY_STRING = "Default";
    public static final String DEFAULT_QUERY_DESCRIPTION = "Show Complete Graph";

    private Query query;
    private String description;
    private int bitIndex;
    private boolean visible;
    private long mask;
    private BooleanReadable result;

    public BitMaskQuery(final Query query, final int bitIndex, final String description) {
        this.query = query;
        this.description = description;
        this.bitIndex = bitIndex;
        this.mask = 0xFFFFFFFFFFFFFFFFL ^ (1L << bitIndex + 1);
    }

    public String getDescription() {
        return description;
    }

    public int getIndex() {
        return bitIndex;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisibility(final boolean visible) {
        this.visible = visible;
    }

    public void setQuery(final Query query) {
        this.query = query;
    }

    public Query getQuery() {
        return query;
    }

    public String getQueryString() {
        return query.getQueryString();
    }
    
    public void setQueryString(final String queryString) {
        query = new Query(query.getElementType(),queryString);
    }

    public GraphElementType getQueryElementType() {
        return query.getElementType();
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public boolean isActive(final long activeQueryBitMask) {
        return (activeQueryBitMask & (1 << bitIndex + 1)) == 0;
    }

    public boolean update(final GraphReadMethods graph, final IntReadable index) {
        if (StringUtils.isNotBlank(query.getQueryString()) && bitIndex != 0) {
            final Object compiledExpression = query.compile(graph, index);
            if (compiledExpression != null) {
                this.result = Access.getDefault().getRegistry(BooleanReadable.class).convert(compiledExpression);
            }
        } else {
            this.result = null;
        }
        return true;
    }

    public long updateBitMask(final long original) {
        if (result != null && result.readBoolean()) {
            return original | (1L << bitIndex);
        } else {
            return original & mask;
        }
    }

    public long combineBitmap(final long original) {
        if (result != null && result.readBoolean()) {
            return original | (long) Math.pow(2.0, bitIndex); // Set the appropriate bit in the result
        } else {
            return original; // No change for inactive query
        }        
    }
    
    protected void setIndex(final int i) {
        this.bitIndex = i;
        this.mask = 0xFFFFFFFFFFFFFFFFL ^ (1L << i + 1);
    }
}
