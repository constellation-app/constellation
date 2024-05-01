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
package au.gov.asd.tac.constellation.graph.utilities;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import java.util.Arrays;

/**
 *
 * @author sirius
 */
public class ElementSet {

    private static long[] emptyArray = new long[0];

    private long[] elements = emptyArray;
    private final GraphElementType type;

    public ElementSet(final GraphElementType type) {
        this.type = type;
    }

    public ElementSet(final ElementSet original) {
        this.elements = Arrays.copyOf(original.elements, original.elements.length);
        this.type = original.type;
    }

    public ElementSet(final String encoded) {
        final int typeEnd = encoded.indexOf(':');
        type = GraphElementType.getValue(encoded.substring(0, typeEnd));

        final int lengthEnd = encoded.indexOf(':', typeEnd + 1);
        elements = new long[Integer.parseInt(encoded.substring(typeEnd + 1, lengthEnd))];

        int start = lengthEnd + 1;
        for (int i = 0; i < elements.length - 1; i++) {
            final int end = encoded.indexOf(':', start);
            elements[i] = Integer.parseInt(encoded.substring(start, end));
            start = end + 1;
        }
    }

    public void add(final GraphReadMethods graph, final int element) {
        if (element >= elements.length) {
            elements = Arrays.copyOf(elements, type.getElementCapacity(graph));
        }

        elements[element] = type.getUID(graph, element);
    }

    public void remove(final int element) {
        if (element < elements.length) {
            elements[element] = 0;
        }
    }

    public boolean contains(final GraphReadMethods graph, final int element) {
        return element < elements.length && elements[element] == type.getUID(graph, element);
    }

    @Override
    public String toString() {
        final StringBuilder out = new StringBuilder();
        out.append(type.name());
        out.append(':');
        out.append(elements.length);
        out.append(':');
        for (long element : elements) {
            out.append(element);
            out.append(':');
        }
        return out.toString();
    }
}
