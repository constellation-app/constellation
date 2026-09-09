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
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import au.gov.asd.tac.constellation.plugins.arrangements.GraphTaxonomy;
import au.gov.asd.tac.constellation.plugins.arrangements.grid.GridArranger;
import au.gov.asd.tac.constellation.plugins.arrangements.grid.GridChoiceParameters;
import au.gov.asd.tac.constellation.plugins.arrangements.subgraph.InducedSubgraph;
import au.gov.asd.tac.constellation.plugins.arrangements.subgraph.SubgraphFactory;
import au.gov.asd.tac.constellation.plugins.arrangements.utilities.ArrangementUtilities;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Arrange the graph in a tree like manner.
 *
 * @author algol
 * @author sol
 */
@ServiceProvider(service = Plugin.class)
@Messages("NewArrangeInTreesPlugin=Arrange in Trees")
@PluginInfo(pluginType = PluginType.DISPLAY, tags = {PluginTags.MODIFY})
public class NewArrangeInTreesPlugin extends SimpleEditPlugin {

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        interaction.setProgress(0, 0, "Arranging...", true);

        if (graph.getVertexCount() < 1) {
            interaction.setProgress(1, 0, "Finished", true);
            return;
        }

        final boolean splitIntoTrees = false;
        final boolean dimOrHideTrans = false;
        final boolean dimTrans = false;
        final boolean colorNodesByGroup = false;
        final float scale = 10f;

        final SubgraphFactory subgraphFactory = InducedSubgraph.getSubgraphFactory();
        final List<GraphWriteMethods> graphs = new ArrayList<>();

        // TODO: IDK what this does
//            final SetRadiusForArrangement radiusSetter = new SetRadiusForArrangement(graph);
//            radiusSetter.setRadii();

        // TODO: Implement pseudocode below
        // Seperate graph into sub graphs that are trees, if requested by user
        if (splitIntoTrees) {
            final GraphTaxonomy components = ArrangementUtilities.getComponents(graph);
            System.out.println("Components: " + components.size());
//            for ( ) {
            //subgraphFactory.constructSubgraph(graph, keyValue.getTwo());
//            }
            if (dimOrHideTrans) {
                if (dimTrans) {
                } else {
                    // Hide transcations instead
                }
            }
        } else {
            // Otherwise seperate graph into island sub graphs
            final List<MutableIntSet> islands = findIslands(graph);
//            System.out.println("islands: " + islands);
            System.out.println("islands.size(): " + islands.size());

            if (islands.size() == 1) {
                // If one big connected graph, dont bother with the overhead of making a subgraph
                graphs.add(graph);
            } else {
                for (final MutableIntSet island : islands) {
                    graphs.add(subgraphFactory.constructSubgraph(graph, island));
                }
            }

        }

        System.out.println("graphs: " + graphs);

        // For each sub graph, arrange
        final Arranger arranger = new NewTreeArranger();
//        arranger.arrange(graph);
        for (final GraphWriteMethods g : graphs) {
            arranger.arrange(g);
        }

        // Arrange in grid
        final GridChoiceParameters outerGcParams = GridChoiceParameters.getDefaultParameters();
        outerGcParams.setRowOffsets(false);
        final Arranger gridArranger = new GridArranger(outerGcParams);
        //gridArranger.arrange(graph);

        interaction.setProgress(1, 0, "Finished", true);
    }

    private List<MutableIntSet> findIslands(final GraphWriteMethods graph) {
        final List<MutableIntSet> islands = new ArrayList<>();
        final BitSet unvisitedNodes = new BitSet(graph.getVertexCount()); // Represent node positions
        unvisitedNodes.set(0, graph.getVertexCount());

        // Iterate through every vertex's neighbour recrusively, remove them from list of visited verts
        // Once done iterating, if any verts remain unvisited, begin the process anew as they are on a seperate island
        while (!unvisitedNodes.isEmpty()) {
            final MutableIntSet island = new IntHashSet();
            final int nextUnvisitedPos = unvisitedNodes.nextSetBit(0);
            islands.add(findIsland(graph, nextUnvisitedPos, unvisitedNodes, island));
        }

        return islands;
    }

    private MutableIntSet findIsland(final GraphWriteMethods graph, final int vxPos, final BitSet unvisitedNodes, final MutableIntSet islandSet) {
        // If visited already
        if (!unvisitedNodes.get(vxPos)) {
            return islandSet;
        }

        // Mark as visitied
        unvisitedNodes.clear(vxPos);

        islandSet.add(vxPos);

        final int vxID = graph.getVertex(vxPos);
        final int numNeighbours = graph.getVertexNeighbourCount(vxID);

        // Recursively find all neighbours
        for (int i = 0; i < numNeighbours; i++) {
            final int nxID = graph.getVertexNeighbour(vxID, i);
            final int nxPos = graph.getVertexPosition(nxID);
            islandSet.addAll(findIsland(graph, nxPos, unvisitedNodes, islandSet));
        }

        return islandSet;
    }

}
