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
package au.gov.asd.tac.constellation.views.histogram.bins;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.views.histogram.Bin;

/**
 * A bin that holds float values.
 *
 * @author sirius
 */
public class FloatBin extends Bin {

    protected float key;

    public float getKey() {
        return key;
    }

    @Override
    public int compareTo(Bin o) {
        return Float.compare(key, ((FloatBin) o).key);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Float.floatToIntBits(this.key);
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this.getClass() == o.getClass()) {
            FloatBin bin = (FloatBin) o;
            return key == bin.key;
        }
        return false;
    }

    @Override
    public void prepareForPresentation() {
        label = String.valueOf(key);
    }

    @Override
    public void setKey(GraphReadMethods graph, int attribute, int element) {
        key = graph.getFloatValue(attribute, element);
    }

    @Override
    public Bin create() {
        return new FloatBin();
    }

    @Override
    public Object getKeyAsObject() {
        return key;
    }

    public void calculateAggregates(GraphReadMethods graph, int attribute, int element, Bin.AGGREGATION aggregation, boolean edgeOnly) {
        float sum = 0;
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        int nullCount = 0;
        setAllElementsAreNull(false);
        final int transactionCount = edgeOnly ? graph.getEdgeTransactionCount(element) : graph.getLinkTransactionCount(element);
        for (int t = 0; t < transactionCount; t++) {
            final int transaction = edgeOnly ? graph.getEdgeTransaction(element, t) : graph.getLinkTransaction(element, t);
            if (graph.getObjectValue(attribute, transaction) == null) {
                nullCount++;
                continue;
            }
            switch (aggregation) {
                case AVERAGE, SUM -> sum += graph.getFloatValue(attribute, transaction);
                case MIN -> min = Math.min(graph.getFloatValue(attribute, transaction), min);
                case MAX -> max = Math.max(graph.getFloatValue(attribute, transaction), max);
            }
        }
        if (nullCount >= transactionCount) {
            setAllElementsAreNull(true);
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
