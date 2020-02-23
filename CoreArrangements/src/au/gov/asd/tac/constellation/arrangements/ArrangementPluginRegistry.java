/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.arrangements;

import au.gov.asd.tac.constellation.arrangements.broccoli.ArrangeLikeBroccoliPlugin;
import au.gov.asd.tac.constellation.arrangements.circle.ArrangeInCirclePlugin;
import au.gov.asd.tac.constellation.arrangements.circle.ArrangeInCirclesPlugin;
import au.gov.asd.tac.constellation.arrangements.circle.ArrangeInSpherePlugin;
import au.gov.asd.tac.constellation.arrangements.gather.GatherNodesInGraphPlugin;
import au.gov.asd.tac.constellation.arrangements.gather.GatherNodesPlugin;
import au.gov.asd.tac.constellation.arrangements.grid.ArrangeInGridComponentsPlugin;
import au.gov.asd.tac.constellation.arrangements.grid.ArrangeInGridGeneralPlugin;
import au.gov.asd.tac.constellation.arrangements.group.ArrangeByNodeAttributePlugin;
import au.gov.asd.tac.constellation.arrangements.group.ArrangeByGroupPlugin;
import au.gov.asd.tac.constellation.arrangements.group.ArrangeByLayerPlugin;
import au.gov.asd.tac.constellation.arrangements.hde.HighDimensionEmbeddingPlugin;
import au.gov.asd.tac.constellation.arrangements.hierarchical.ArrangeInHierarchyPlugin;
import au.gov.asd.tac.constellation.arrangements.pendants.PendantsArranger;
import au.gov.asd.tac.constellation.arrangements.proximity.ArrangeByProximity3DPlugin;
import au.gov.asd.tac.constellation.arrangements.proximity.ArrangeByProximityPlugin;
import au.gov.asd.tac.constellation.arrangements.random.RandomArrangementPlugin;
import au.gov.asd.tac.constellation.arrangements.resize.ContractGraphPlugin;
import au.gov.asd.tac.constellation.arrangements.resize.ExpandGraphPlugin;
import au.gov.asd.tac.constellation.arrangements.spectral.SpectralArrangementPlugin;
import au.gov.asd.tac.constellation.arrangements.time.LayerByTimePlugin;
import au.gov.asd.tac.constellation.arrangements.tree.ArrangeInBubbleTreePlugin;
import au.gov.asd.tac.constellation.arrangements.tree.ArrangeInMDSPlugin;
import au.gov.asd.tac.constellation.arrangements.tree.ArrangeInTreesPlugin;
import au.gov.asd.tac.constellation.arrangements.uncollide.UncollidePlugin;
import au.gov.asd.tac.constellation.arrangements.utilities.FlattenZFieldPlugin;

/**
 * Registry of arrangement plugins.
 *
 * @author algol
 */
public class ArrangementPluginRegistry {

    public static final String ATTRIBUTE = ArrangeByNodeAttributePlugin.class.getName();
    public static final String BROCCOLI = ArrangeLikeBroccoliPlugin.class.getName();
    public static final String BUBBLE_TREE = ArrangeInBubbleTreePlugin.class.getName();
    public static final String CIRCLE = ArrangeInCirclePlugin.class.getName();
    public static final String CIRCLES = ArrangeInCirclesPlugin.class.getName();
    public static final String CONTRACT_GRAPH = ContractGraphPlugin.class.getName();
    public static final String EXPAND_GRAPH = ExpandGraphPlugin.class.getName();
    public static final String FLATTEN_Z = FlattenZFieldPlugin.class.getName();
    public static final String FR2D = ArrangeByProximityPlugin.class.getName();
    public static final String FR3D = ArrangeByProximity3DPlugin.class.getName();
    public static final String GATHER_NODES = GatherNodesPlugin.class.getName();
    public static final String GATHER_NODES_IN_GRAPH = GatherNodesInGraphPlugin.class.getName();
    public static final String GRID_GENERAL = ArrangeInGridGeneralPlugin.class.getName();
    public static final String GRID_COMPOSITE = ArrangeInGridComponentsPlugin.class.getName();
    public static final String GROUP = ArrangeByGroupPlugin.class.getName();
    public static final String HIERARCHICAL = ArrangeInHierarchyPlugin.class.getName();
    public static final String HIGH_DIMENSION_EMBEDDING = HighDimensionEmbeddingPlugin.class.getName();
    public static final String LAYER = ArrangeByLayerPlugin.class.getName();
    public static final String MDS = ArrangeInMDSPlugin.class.getName();
    public static final String PENDANTS = PendantsArranger.class.getName();
    public static final String RANDOM = RandomArrangementPlugin.class.getName();
    public static final String SPECTRAL = SpectralArrangementPlugin.class.getName();
    public static final String SPHERE = ArrangeInSpherePlugin.class.getName();
    public static final String TIME = LayerByTimePlugin.class.getName();
    public static final String TREES = ArrangeInTreesPlugin.class.getName();
    public static final String UNCOLLIDE = UncollidePlugin.class.getName();
}
