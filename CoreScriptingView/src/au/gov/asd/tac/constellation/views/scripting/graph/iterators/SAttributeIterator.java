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
package au.gov.asd.tac.constellation.views.scripting.graph.iterators;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.views.scripting.graph.SAttribute;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator for accessing attributes via scripting.
 *
 * @author cygnus_x-1
 */
public class SAttributeIterator implements Iterator<SAttribute> {

    private final GraphReadMethods readableGraph;
    private final GraphElementType elementType;
    private final int attributeCount;
    private int currentPosition;

    public SAttributeIterator(final GraphReadMethods readableGraph, final GraphElementType elementType) {
        this.readableGraph = readableGraph;
        this.elementType = elementType;
        this.attributeCount = readableGraph.getAttributeCount(elementType);
        this.currentPosition = 0;
    }

    @Override
    public boolean hasNext() {
        return currentPosition < attributeCount;
    }

    @Override
    public SAttribute next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        final int attributeId = readableGraph.getAttribute(elementType, currentPosition++);
        return new SAttribute(readableGraph, attributeId);
    }
}
