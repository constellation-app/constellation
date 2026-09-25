/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.arrangements.tree;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.arrangements.AbstractInclusionGraph.Connections;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import au.gov.asd.tac.constellation.plugins.arrangements.GraphTaxonomy;
import au.gov.asd.tac.constellation.plugins.arrangements.GraphTaxonomyArranger;
import au.gov.asd.tac.constellation.plugins.arrangements.subgraph.InducedSubgraph;
import au.gov.asd.tac.constellation.plugins.arrangements.utilities.ArrangementUtilities;

/**
 * A GraphTaxonomyArranger that uses a taxonomy where each taxon is a tree.
 *
 * @author algol
 * @author sol
 */
public class NewTaxonArranger extends GraphTaxonomyArranger {

    public NewTaxonArranger(final Arranger inner, final Arranger outer) {
        super(inner, outer, Connections.LINKS, InducedSubgraph.getSubgraphFactory());
    }

    @Override
    public GraphTaxonomy getTaxonomy(final GraphWriteMethods wg) {
        return ArrangementUtilities.getComponents(wg);
    }
}
