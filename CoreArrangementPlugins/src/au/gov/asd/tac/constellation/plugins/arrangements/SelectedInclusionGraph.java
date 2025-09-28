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
package au.gov.asd.tac.constellation.plugins.arrangements;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;

/**
 * An AbstractInclusionGraph that uses the boolean selected attribute to include
 * vertices.
 *
 * @author algol
 */
public class SelectedInclusionGraph extends AbstractInclusionGraph {

    private final int selectedAttr;

    /**
     * Create a new inclusion graph.
     *
     * @param wg The original graph.
     * @param connections How to copy transactions to the inclusion graph.
     */
    public SelectedInclusionGraph(final GraphWriteMethods wg, final Connections connections) {
        super(wg, connections);
        selectedAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());
    }

    @Override
    public boolean isVertexIncluded(final int vxId) {

        // Don't forget to allow for selected not being present.
        return (selectedAttr == Graph.NOT_FOUND || wg.getBooleanValue(selectedAttr, vxId));
    }
}
