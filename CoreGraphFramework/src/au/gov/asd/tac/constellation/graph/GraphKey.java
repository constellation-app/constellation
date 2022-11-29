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
package au.gov.asd.tac.constellation.graph;

import java.util.Arrays;

/**
 * A GraphKey represents the key values for a vertex or transaction in the
 * graph.
 *
 * @author sirius
 */
public class GraphKey {

    private final Object[] elements;
    private final GraphKey sourceKey;
    private final GraphKey destinationKey;
    private final boolean undirected;

    /**
     * Create a new graph key for a vertex in the graph.
     *
     * @param elements the values of the key attributes for this vertex.
     */
    public GraphKey(final Object... elements) {
        this(null, null, false, elements);
    }

    /**
     * Create a new graph key for a transaction in the graph.
     *
     * @param sourceKey the GraphKey for the transaction's source vertex.
     * @param destinationKey the GraphKey for the transaction's destination
     * vertex.
     * @param undirected is the transaction undirected?
     * @param elements the values of the key attributes for this transaction.
     */
    public GraphKey(final GraphKey sourceKey, final GraphKey destinationKey, final boolean undirected, final Object... elements) {
        this.elements = elements;
        this.sourceKey = sourceKey;
        this.destinationKey = destinationKey;
        this.undirected = undirected;
    }

    public int getElementCount() {
        return elements.length;
    }

    public Object getElement(final int position) {
        return elements[position];
    }

    public GraphKey getSourceKey() {
        return sourceKey;
    }

    public GraphKey getDestinationKey() {
        return destinationKey;
    }

    public boolean isUndirected() {
        return undirected;
    }

    @Override
    public int hashCode() {
        int hash = Arrays.hashCode(elements);
        if (sourceKey != null) {
            hash = (hash * 113) ^ sourceKey.hashCode();
        }
        if (destinationKey != null) {
            hash = (hash * 113) ^ destinationKey.hashCode();
        }
        if (undirected) {
            hash *= 113;
        }
        return hash;
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (this.getClass() == other.getClass()) {
            GraphKey key = (GraphKey) other;

            if (!Arrays.equals(elements, key.elements)) {
                return false;
            }

            if (sourceKey == null ? key.sourceKey != null : !sourceKey.equals(key.sourceKey)) {
                return false;
            }

            if (destinationKey == null ? key.destinationKey != null : !destinationKey.equals(key.destinationKey)) {
                return false;
            }

            return undirected == key.undirected;
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();

        out.append(Arrays.toString(elements));
        if (sourceKey != null) {
            out.append(", sourceKey = ");
            out.append(sourceKey);
        }
        if (destinationKey != null) {
            out.append(", destinationKey = ");
            out.append(destinationKey);
        }
        if (undirected) {
            out.append(", undirected");
        }
        return out.toString();
    }
}
