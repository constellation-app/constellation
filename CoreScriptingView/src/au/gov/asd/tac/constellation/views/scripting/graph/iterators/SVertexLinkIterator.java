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
package au.gov.asd.tac.constellation.views.scripting.graph.iterators;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.views.scripting.graph.SLink;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator for accessing links from the context of vertices via scripting.
 *
 * @author cygnus_x-1
 */
public class SVertexLinkIterator implements Iterator<SLink> {

    private final GraphReadMethods readableGraph;
    private final int vertexId;
    private final int linkCount;
    private int currentPosition;

    public SVertexLinkIterator(final GraphReadMethods readableGraph, final int vertexId) {
        this.readableGraph = readableGraph;
        this.vertexId = vertexId;
        this.linkCount = readableGraph.getVertexLinkCount(vertexId);
        this.currentPosition = 0;
    }

    @Override
    public boolean hasNext() {
        return currentPosition < linkCount;
    }

    @Override
    public SLink next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        final int linkId = readableGraph.getVertexLink(vertexId, currentPosition++);
        return new SLink((GraphWriteMethods) readableGraph, linkId);
    }
}
