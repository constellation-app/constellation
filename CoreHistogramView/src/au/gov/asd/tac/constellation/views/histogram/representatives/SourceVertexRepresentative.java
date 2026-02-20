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
package au.gov.asd.tac.constellation.views.histogram.representatives;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.views.histogram.ElementRepresentative;

/**
 * A SourceVertexRepresentative implements an ElementRepresentative that allows
 * a connection type graph element to be represented by attributes and
 * properties of its source vertex.
 *
 * @author sirius
 */
public class SourceVertexRepresentative extends ElementRepresentative {

    @Override
    public int appliesToElementType(GraphElementType elementType) {
        return elementType == GraphElementType.LINK || elementType == GraphElementType.EDGE || elementType == GraphElementType.TRANSACTION ? 1 : -1;
    }

    @Override
    public GraphElementType getRepresentativeElementType(GraphElementType elementType) {
        return GraphElementType.VERTEX;
    }

    @Override
    public int findRepresentative(GraphReadMethods graph, GraphElementType elementType, int element) {
        return elementType.getSourceVertex(graph, element);
    }

}
