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
package au.gov.asd.tac.constellation.plugins.arrangements;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import java.util.BitSet;
import java.util.List;

/**
 * An AbstractInclusionGraph that uses a specified list of vertices to include
 * vertices.
 *
 * @author algol
 */
public class VertexListInclusionGraph extends AbstractInclusionGraph {

    // Use a BitSet because it's much faster to test for contains than a List<>.
    private final BitSet vertexSet;

    /**
     * Create a new inclusion graph.
     *
     * @param wg The original graph.
     * @param connections How to copy transactions to the inclusion graph.
     * @param vertices A List&lt;Integer&gt; of vertices to be included.
     */
    public VertexListInclusionGraph(final GraphWriteMethods wg, final Connections connections, final List<Integer> vertices) {
        super(wg, connections);
        vertexSet = new BitSet();
        vertices.stream().forEach(vertexSet::set);
    }

    @Override
    public boolean isVertexIncluded(final int vxId) {
        return vertexSet.get(vxId);
    }
}
