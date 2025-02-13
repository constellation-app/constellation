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
package au.gov.asd.tac.constellation.views.histogram.bins;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.views.histogram.Bin;

/**
 * A bin that holds long integer values.
 *
 * @author sirius
 */
public class LongBin extends Bin {

    protected long key;

    @Override
    public int compareTo(final Bin o) {
        final LongBin bin = (LongBin) o;
        if (key > bin.key) {
            return 1;
        } else if (key < bin.key) {
            return -1;
        } else {
            return 0;
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (int) (this.key ^ (this.key >>> 32));
        return hash;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        }
        if (this.getClass() == o.getClass()) {
            final LongBin bin = (LongBin) o;
            return key == bin.key;
        }
        return false;
    }

    @Override
    public void prepareForPresentation() {
        label = String.valueOf(key);
    }

    @Override
    public void setKey(final GraphReadMethods graph, final int attribute, final int element) {
        key = graph.getLongValue(attribute, element);
    }

    @Override
    public Bin create() {
        return new LongBin();
    }

    @Override
    public Object getKeyAsObject() {
        return key;
    }

    public void calculateAggregates(final GraphReadMethods graph, final int attribute, final int element, final Bin.AGGREGATION aggregation, final boolean edgeOnly) {
        long sum = 0;
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        int nullCount = 0;
        setOnlyNullElements(false);
        final int transactionCount = edgeOnly ? graph.getEdgeTransactionCount(element) : graph.getLinkTransactionCount(element);
        for (int t = 0; t < transactionCount; t++) {
            final int transaction = edgeOnly ? graph.getEdgeTransaction(element, t) : graph.getLinkTransaction(element, t);
            if (graph.getObjectValue(attribute, transaction) == null) {
                nullCount++;
                continue;
            }
            switch (aggregation) {
                case AVERAGE, SUM -> sum += graph.getLongValue(attribute, transaction);
                case MIN -> min = Math.min(graph.getLongValue(attribute, transaction), min);
                case MAX -> max = Math.max(graph.getLongValue(attribute, transaction), max);
            }
        }
        if (nullCount >= transactionCount) {
            setOnlyNullElements(true);
            return;
        }        
        key = switch (aggregation) {
            case AVERAGE -> sum / (transactionCount - nullCount);
            case SUM -> sum;
            case MIN -> min;
            case MAX -> max;
        };
    }
}
