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
package au.gov.asd.tac.constellation.plugins.arrangements;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.arrangements.subgraph.ComponentSubgraph;
import au.gov.asd.tac.constellation.plugins.arrangements.utilities.ArrangementUtilities;

/**
 * A GraphTaxonomyArranger that uses a taxonomy where each taxon is a component.
 *
 * @author algol
 */
public final class GraphComponentArranger extends GraphTaxonomyArranger {

    /**
     * Construct a new GraphComponentArranger instance.
     *
     * @param inner The inner Arranger.
     * @param outer The outer Arranger.
     * @param connectionType the connection type.
     */
    public GraphComponentArranger(final Arranger inner, final Arranger outer, final SelectedInclusionGraph.Connections connectionType) {
        super(inner, outer, connectionType, ComponentSubgraph.getSubgraphFactory());
    }

    @Override
    protected GraphTaxonomy getTaxonomy(final GraphWriteMethods wg) {
        return ArrangementUtilities.getComponents(wg);
    }
}
