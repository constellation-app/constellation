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
package au.gov.asd.tac.constellation.plugins.arrangements.tree;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.arrangements.AbstractInclusionGraph.Connections;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import au.gov.asd.tac.constellation.plugins.arrangements.GraphTaxonomy;
import au.gov.asd.tac.constellation.plugins.arrangements.GraphTaxonomyArranger;
import au.gov.asd.tac.constellation.plugins.arrangements.subgraph.InducedSubgraph;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A GraphTaxonomyArranger that uses a taxonomy where each taxon is a tree.
 *
 * @author algol
 * @author sol
 */
public class TreeTaxonArranger extends GraphTaxonomyArranger {

    private boolean putSingletonTaxaWithSameNeighborsTogether;

    public TreeTaxonArranger(final Arranger inner, final Arranger outer) {
        super(inner, outer, Connections.LINKS, InducedSubgraph.getSubgraphFactory());
        putSingletonTaxaWithSameNeighborsTogether = true;
    }

    /**
     * If true, singleton taxa (those that have only one vertex) are joined with
     * others having the same set of neighbors; these are then arranged as
     * larger groups.
     *
     * @param putSingletonTaxaWithSameNeighborsTogether should singletons tax
     * with the same neighbours be combined. Default value is true.
     *
     */
    public void setPutSingletonTaxaWithSameNeighborsTogether(final boolean putSingletonTaxaWithSameNeighborsTogether) {
        this.putSingletonTaxaWithSameNeighborsTogether = putSingletonTaxaWithSameNeighborsTogether;
    }

    @Override
    public GraphTaxonomy getTaxonomy(final GraphWriteMethods graph) {
        final GraphTaxonomy taxByTrees = TaxFromTrees.getTaxonomy(graph, false);

        if (putSingletonTaxaWithSameNeighborsTogether) {
            // Remove all taxa with only one member and add all of the single members to a single new taxon.
            final Set<Integer> singletons = new HashSet<>();
            Map<Integer, Set<Integer>> taxa = taxByTrees.getTaxa();
            for (Iterator<Integer> ii = taxa.keySet().iterator(); ii.hasNext();) {
                final Integer vxRoot = ii.next();

                final Set<Integer> members = taxa.get(vxRoot);
                if (members.size() == 1) {
                    singletons.add(vxRoot);
                    ii.remove();
                }
            }

            final GraphTaxonomy taxByNeighbours = TaxFromNeighbours.getTaxonomy(graph, singletons);
            taxByTrees.add(taxByNeighbours);
            taxByTrees.setArrangeRectangularly(taxByNeighbours.getTaxa().keySet());
        }
        return taxByTrees;
    }
}
