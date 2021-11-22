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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.arrangements.grid.GridArranger;
import au.gov.asd.tac.constellation.plugins.arrangements.grid.GridChoiceParameters;
import au.gov.asd.tac.constellation.plugins.arrangements.subgraph.SubgraphFactory;
import au.gov.asd.tac.constellation.plugins.arrangements.utilities.ArrangementUtilities;
import java.util.Map;
import java.util.Set;

/**
 * The GraphTaxonomy arranger.
 * <p>
 * A GraphTaxonomy arranger instance provides a consistent way to arrange a
 * GraphTaxonomy.
 * <p>
 * A GraphTaxonomyArranger uses three Arranger instances: an inner Arranger, an
 * outer Arranger, and an implicit ArrangeInGrid Arranger. It is also possible
 * to provide separate arrangers for singleton and doublet taxa: these will
 * typically also be an ArrangeInGrid instance.
 * <p>
 * These instances are applied in the arrange() method.
 *
 * @author algol
 */
public abstract class GraphTaxonomyArranger implements Arranger {

    private final Arranger inner;
    private final Arranger outer;
    protected final SelectedInclusionGraph.Connections connectionType;
    private final SubgraphFactory subgraphFactory;
    protected Arranger singletonArranger;
    protected Arranger doubletArranger;
    protected Arranger rectArranger;
    private boolean maintainMean;
    private PluginInteraction interaction;

    // An optional uncollide step.
    private Arranger uncollider;

    /**
     * Construct a new GraphTaxonomyArranger.
     *
     * @param inner The inner Arranger.
     * @param outer The outer Arranger.
     * @param connectionType the connection type.
     * @param subgraphFactory the subgraphFactory.
     */
    public GraphTaxonomyArranger(final Arranger inner, final Arranger outer, SelectedInclusionGraph.Connections connectionType, final SubgraphFactory subgraphFactory) {
        this.inner = inner;
        this.outer = outer;
        this.connectionType = connectionType;
        this.subgraphFactory = subgraphFactory;

        rectArranger = new GridArranger(GridChoiceParameters.getDefaultParameters());

        uncollider = null;
        interaction = null;
    }

    public void setUncollider(final Arranger uncollider) {
        this.uncollider = uncollider;
    }

    /**
     * Set the arranger for arranging singletons.
     *
     * @param singletonArranger The singleton arranger.
     */
    public void setSingletonArranger(final Arranger singletonArranger) {
        this.singletonArranger = singletonArranger;
    }

    /**
     * Set the arranger for arranging doublets.
     *
     * @param doubletArranger The doublet arranger.
     */
    public void setDoubletArranger(final Arranger doubletArranger) {
        this.doubletArranger = doubletArranger;
    }

    public void setInteraction(final PluginInteraction interaction) {
        this.interaction = interaction;
    }

    /**
     * Arrange the graph using a taxonomy provided by getTaxonomy().
     * <p>
     * If there is only one taxon, the inner arrangement will be done.
     * Otherwise, for each taxon, the rectangular, singleton, doublet, or inner
     * arranger will be performed according to the type of taxon. Then a
     * condensation of the taxonomy keys will be created, the outer arranger
     * will be performed on the condensation, an uncollision will be done, and
     * the taxonomy vertices repositioned according to the results.
     *
     * @param wg The graph to arrange.
     *
     * @throws InterruptedException if the thread is interrupted during
     * execution meaning that the operation has been canceled.
     */
    @Override
    public void arrange(final GraphWriteMethods wg) throws InterruptedException {
        final float[] oldMean = maintainMean ? ArrangementUtilities.getXyzMean(wg) : null;
        int singletonsKey = Graph.NOT_FOUND;
        int doubletsKey = Graph.NOT_FOUND;

        if (interaction != null) {
            interaction.setProgress(0, 0, "Discovering taxonomy...", true);
        }

        final GraphTaxonomy taxonomy = getTaxonomy(wg);
//        System.out.printf("@GTA tax size %d\n", taxonomy.size());

        if (taxonomy.size() == 1) {
            final int k = taxonomy.getTaxa().keySet().iterator().next();
            inner.arrange(subgraphFactory.constructSubgraph(wg, taxonomy.getTaxa().get(k)));
        } else {
            if (singletonArranger != null) {
                singletonsKey = taxonomy.getSingletonKey();
                if (singletonsKey == Graph.NOT_FOUND) {
                    singletonsKey = taxonomy.mergeSingletonTaxa();
                }
            }

            if (doubletArranger != null) {
                doubletsKey = taxonomy.getDoubletKey();
                if (doubletsKey == Graph.NOT_FOUND) {
                    doubletsKey = taxonomy.mergeDoubletTaxa();
                }
            }

            // Do the appropriate inner arrangement on each taxon.
            final Map<Integer, Set<Integer>> taxa = taxonomy.getTaxa();
            final int steps = taxa.size() + 1;
            int step = 0;
            for (final Map.Entry<Integer, Set<Integer>> entry : taxa.entrySet()) {
                if (taxonomy.isArrangeRectangularly(entry.getKey())) {
                    if (interaction != null) {
                        interaction.setProgress(step, steps, "Arrange grid...", true);
                    }
                    rectArranger.arrange(subgraphFactory.constructSubgraph(wg, entry.getValue()));
                } else if (entry.getKey() == singletonsKey) {
                    if (interaction != null) {
                        interaction.setProgress(step, steps, "Arrange singletons...", true);
                    }
                    singletonArranger.arrange(subgraphFactory.constructSubgraph(wg, entry.getValue()));
                } else if (entry.getKey() == doubletsKey) {
                    if (interaction != null) {
                        interaction.setProgress(step, steps, "Arrange doublets...", true);
                    }
                    doubletArranger.arrange(subgraphFactory.constructSubgraph(wg, entry.getValue()));
                } else {
                    if (interaction != null) {
                        final String msg = String.format("Arrange inner (%s)...", inner.getClass().getSimpleName());
                        interaction.setProgress(step, steps, msg, true);
                    }
                    inner.arrange(subgraphFactory.constructSubgraph(wg, entry.getValue()));
                }
                step++;
            }

//            System.out.printf("@GTA taxon count: %d\n", taxonomy.size());
            if (interaction != null) {
                final String msg = String.format("Arrange outer (%s)...", outer.getClass().getSimpleName());
                interaction.setProgress(step, steps, msg, true);
            }

            // Do the outer arrangement on a condensed graph of taxon keys.
            GraphTaxonomy.Condensation c = taxonomy.getCondensedGraph();
            outer.arrange(c.wg);

            // Do an uncollide arrangement to remove overlaps.
            // Doing the uncolliding can take a while.
            // If there are too many vertices, don't do it.
            if (uncollider != null && c.wg.getVertexCount() <= 10000) {
                if (interaction != null) {
                    interaction.setProgress(0, 0, "Arrange overlaps...", true);
                }

                uncollider.arrange(c.wg);
            }

            taxonomy.reposition(c);
        }

        if (maintainMean) {
            ArrangementUtilities.moveMean(wg, oldMean);
        }

        if (interaction != null) {
            interaction.setProgress(1, 0, null, false);
        }
    }

    protected abstract GraphTaxonomy getTaxonomy(final GraphWriteMethods graph);

    @Override
    public void setMaintainMean(final boolean b) {
        maintainMean = b;
        if (inner != null) {
            inner.setMaintainMean(b);
        }
        if (outer != null) {
            outer.setMaintainMean(b);
        }
    }

    public static void dump(final Map<Integer, Set<Integer>> taxa) {
        for (final Map.Entry<Integer, Set<Integer>> entry : taxa.entrySet()) {
            System.out.printf("tax %d: size %d%n", entry.getKey(), entry.getValue().size());
        }
    }
}
