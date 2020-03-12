/*
 * Copyright 2010-2020 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.plugins.arrangements.AbstractInclusionGraph.Connections;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import au.gov.asd.tac.constellation.plugins.arrangements.GraphTaxonomy;
import au.gov.asd.tac.constellation.plugins.arrangements.GraphTaxonomyArranger;
import au.gov.asd.tac.constellation.plugins.arrangements.subgraph.InducedSubgraph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
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
//        System.out.printf("~~ taxaA (%d)\n", taxByTrees.getTaxa().size());

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

//            System.out.printf("~~ taxaB (%d) singletons (%d)\n", taxByTrees.size(), singletons.size());
            final GraphTaxonomy taxByNeighbours = TaxFromNeighbours.getTaxonomy(graph, singletons);
            taxByTrees.add(taxByNeighbours);
            taxByTrees.setArrangeRectangularly(taxByNeighbours.getTaxa().keySet());

//            System.out.printf("~~ taxaC (%d) neighbours (%d)\n", taxByTrees.getTaxa().size(), taxByNeighbours.size());
//            Debug.debug("rectangular");
//            for(Integer k : taxByNeighbours.getTaxa().keySet())
//            {
//                Debug.debug(" %d(%d)", k, taxByNeighbours.getTaxa().get(k).size());
//            }
//            Debug.debug("\n");
        }

//        System.out.printf("Trees: %d\n", taxByTrees.getTaxa().size());
//        // Debug: colour the taxonomies so we can see what's going on.
//        //        graph.getWriteLock();
//        try
//        {
//            if(VisualConcept.VertexAttribute.BACKGROUND_ICON.get(graph)==Graph.NOT_FOUND)
//            {
//                graph.addAttribute(GraphElementType.VERTEX, "icon", "background_icon", "background_icon");
//            }
//            final int bgiconAttr = VisualConcept.VertexAttribute.BACKGROUND_ICON.get(graph);
//            if(VisualConcept.VertexAttribute.COLOR.get(graph)==Graph.NOT_FOUND)
//            {
//                graph.addAttribute(GraphElementType.VERTEX, "color", "color", "color");
//            }
//            final int colorAttr = VisualConcept.VertexAttribute.COLOR.get(graph);
//            final Random r = new Random();
//            for(Integer subvxId : taxByTrees.getTaxa().keySet())
//            {
//                final ColorValue color = ColorValue.getNamedColorValue(r.nextFloat(), r.nextFloat(), r.nextFloat());
//                final Set<Integer> subgraph = taxByTrees.getTaxa().get(subvxId);
//                final boolean rect = taxByTrees.getArrangeRectangularly(subvxId);
//                for(int vxId : subgraph)
//                {
//                    graph.setStringValue(bgiconAttr, vxId, rect ? "Background.Flat Square" : "Background.Round Circle");
//                    graph.setObjectValue(colorAttr, vxId, color);
//                }
//            }
//        }
//        finally
//        {
//            graph.releaseWriteLock();
//        }
        return taxByTrees;
    }
}
