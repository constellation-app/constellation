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
package au.gov.asd.tac.constellation.graph.visual;

import au.gov.asd.tac.constellation.graph.visual.plugins.blaze.AddBlazePlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.blaze.AddCustomBlazePlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.blaze.DeselectBlazesPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.blaze.RemoveBlazePlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.blaze.SelectBlazesPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.blaze.UpdateBlazeSizeOpacityPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.dim.DimAllPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.dim.DimSelectedPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.dim.DimUnselectedPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.dim.SelectDimmedPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.dim.SelectUndimmedPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.dim.UndimAllPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.dim.UndimSelectedPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.dim.UndimUnselectedPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.hop.HopOutPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.merge.PermanentMergePlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.select.ChangeSelectionPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.select.DeselectAllPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.select.DeselectTransactionsPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.select.DeselectVerticesPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.select.InvertSelectionPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.select.SelectAllPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.select.SelectTransactionsPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.select.SelectVerticesPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.select.structure.SelectBackbonePlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.select.structure.SelectHalfHopInducedSubgraphPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.select.structure.SelectLoopsPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.select.structure.SelectOneHopInducedSubgraphPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.select.structure.SelectPendantsPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.select.structure.SelectSingletonsPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.select.structure.SelectSinksPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.select.structure.SelectSourcesPlugin;

/**
 * Visual Graph Plugin Registry.
 *
 * @author cygnus_x-1
 */
public class VisualGraphPluginRegistry {

    public static final String ADD_BLAZE = AddBlazePlugin.class.getName();
    public static final String ADD_CUSTOM_BLAZE = AddCustomBlazePlugin.class.getName();
    public static final String CHANGE_SELECTION = ChangeSelectionPlugin.class.getName();
    public static final String DESELECT_ALL = DeselectAllPlugin.class.getName();
    public static final String DESELECT_BLAZES = DeselectBlazesPlugin.class.getName();
    public static final String DESELECT_TRANSACTIONS = DeselectTransactionsPlugin.class.getName();
    public static final String DESELECT_VERTICES = DeselectVerticesPlugin.class.getName();
    public static final String DIM_ALL = DimAllPlugin.class.getName();
    public static final String DIM_SELECTED = DimSelectedPlugin.class.getName();
    public static final String DIM_UNSELECTED = DimUnselectedPlugin.class.getName();
    public static final String HOP_OUT = HopOutPlugin.class.getName();
    public static final String INVERT_SELECTION = InvertSelectionPlugin.class.getName();
    public static final String PERMANENT_MERGE = PermanentMergePlugin.class.getName();
    public static final String REMOVE_BLAZE = RemoveBlazePlugin.class.getName();
    public static final String SELECT_ALL = SelectAllPlugin.class.getName();
    public static final String SELECT_VERTICES = SelectVerticesPlugin.class.getName();
    public static final String SELECT_TRANSACTIONS = SelectTransactionsPlugin.class.getName();
    public static final String SELECT_BACKBONE = SelectBackbonePlugin.class.getName();
    public static final String SELECT_BLAZES = SelectBlazesPlugin.class.getName();
    public static final String SELECT_DIMMED = SelectDimmedPlugin.class.getName();
    public static final String SELECT_INDUCED_HALF_HOP = SelectHalfHopInducedSubgraphPlugin.class.getName();
    public static final String SELECT_INDUCED_ONE_HOP = SelectOneHopInducedSubgraphPlugin.class.getName();
    public static final String SELECT_LOOPS = SelectLoopsPlugin.class.getName();
    public static final String SELECT_PENDANTS = SelectPendantsPlugin.class.getName();
    public static final String SELECT_SINGLETONS = SelectSingletonsPlugin.class.getName();
    public static final String SELECT_SINKS = SelectSinksPlugin.class.getName();
    public static final String SELECT_SOURCES = SelectSourcesPlugin.class.getName();
    public static final String SELECT_UNDIMMED = SelectUndimmedPlugin.class.getName();
    public static final String UNDIM_ALL = UndimAllPlugin.class.getName();
    public static final String UNDIM_SELECTED = UndimSelectedPlugin.class.getName();
    public static final String UNDIM_UNSELECTED = UndimUnselectedPlugin.class.getName();
    public static final String UPDATE_BLAZE_SIZE_OPACITY = UpdateBlazeSizeOpacityPlugin.class.getName();
}
