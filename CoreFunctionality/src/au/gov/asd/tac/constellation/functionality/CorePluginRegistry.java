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
package au.gov.asd.tac.constellation.functionality;

import au.gov.asd.tac.constellation.functionality.admin.DevOpsNotificationPlugin;
import au.gov.asd.tac.constellation.functionality.autosave.AutosaveGraphPlugin;
import au.gov.asd.tac.constellation.functionality.blaze.AddBlazePlugin;
import au.gov.asd.tac.constellation.functionality.blaze.AddCustomBlazePlugin;
import au.gov.asd.tac.constellation.functionality.blaze.DeSelectBlazesPlugin;
import au.gov.asd.tac.constellation.functionality.blaze.RemoveBlazePlugin;
import au.gov.asd.tac.constellation.functionality.blaze.SelectBlazesPlugin;
import au.gov.asd.tac.constellation.functionality.blaze.UpdateBlazeSizeOpacityPlugin;
import au.gov.asd.tac.constellation.functionality.browser.OpenInBrowserPlugin;
import au.gov.asd.tac.constellation.functionality.compare.CompareGraphPlugin;
import au.gov.asd.tac.constellation.functionality.composite.ContractAllCompositesPlugin;
import au.gov.asd.tac.constellation.functionality.composite.CreateCompositeFromSelectionPlugin;
import au.gov.asd.tac.constellation.functionality.composite.CreateCompositesFromDominantNodesPlugin;
import au.gov.asd.tac.constellation.functionality.composite.DestroyAllCompositesPlugin;
import au.gov.asd.tac.constellation.functionality.composite.ExpandAllCompositesPlugin;
import au.gov.asd.tac.constellation.functionality.copypaste.CopyToClipboardPlugin;
import au.gov.asd.tac.constellation.functionality.copypaste.CopyToNewGraphPlugin;
import au.gov.asd.tac.constellation.functionality.copypaste.CutToClipboardPlugin;
import au.gov.asd.tac.constellation.functionality.copypaste.PasteFromClipboardPlugin;
import au.gov.asd.tac.constellation.functionality.copypaste.PasteGraphPlugin;
import au.gov.asd.tac.constellation.functionality.copypaste.PasteTextPlugin;
import au.gov.asd.tac.constellation.functionality.delete.DeleteSelectionPlugin;
import au.gov.asd.tac.constellation.functionality.dim.DimAllPlugin;
import au.gov.asd.tac.constellation.functionality.dim.DimSelectedPlugin;
import au.gov.asd.tac.constellation.functionality.dim.DimUnselectedPlugin;
import au.gov.asd.tac.constellation.functionality.dim.SelectDimmedPlugin;
import au.gov.asd.tac.constellation.functionality.dim.SelectUndimmedPlugin;
import au.gov.asd.tac.constellation.functionality.dim.UndimAllPlugin;
import au.gov.asd.tac.constellation.functionality.dim.UndimSelectedPlugin;
import au.gov.asd.tac.constellation.functionality.dim.UndimUnselectedPlugin;
import au.gov.asd.tac.constellation.functionality.display.SetConnectionModePlugin;
import au.gov.asd.tac.constellation.functionality.display.SetDrawFlagPlugin;
import au.gov.asd.tac.constellation.functionality.display.SetVisibleAboveThresholdPlugin;
import au.gov.asd.tac.constellation.functionality.display.ToggleDisplayModePlugin;
import au.gov.asd.tac.constellation.functionality.display.ToggleDrawFlagPlugin;
import au.gov.asd.tac.constellation.functionality.draw.ToggleDrawDirectedPlugin;
import au.gov.asd.tac.constellation.functionality.draw.ToggleSelectionModePlugin;
import au.gov.asd.tac.constellation.functionality.email.SendToEmailClientPlugin;
import au.gov.asd.tac.constellation.functionality.hop.HopOutPlugin;
import au.gov.asd.tac.constellation.functionality.merge.PermanentMergePlugin;
import au.gov.asd.tac.constellation.functionality.save.SaveGraphPlugin;
import au.gov.asd.tac.constellation.functionality.select.ChangeSelectionPlugin;
import au.gov.asd.tac.constellation.functionality.select.DeselectAllPlugin;
import au.gov.asd.tac.constellation.functionality.select.DeselectTransactionsPlugin;
import au.gov.asd.tac.constellation.functionality.select.DeselectVerticesPlugin;
import au.gov.asd.tac.constellation.functionality.select.InvertSelectionPlugin;
import au.gov.asd.tac.constellation.functionality.select.SelectAllPlugin;
import au.gov.asd.tac.constellation.functionality.select.structure.SelectBackbonePlugin;
import au.gov.asd.tac.constellation.functionality.select.structure.SelectHalfHopInducedSubgraphPlugin;
import au.gov.asd.tac.constellation.functionality.select.structure.SelectLoopsPlugin;
import au.gov.asd.tac.constellation.functionality.select.structure.SelectOneHopInducedSubgraphPlugin;
import au.gov.asd.tac.constellation.functionality.select.structure.SelectPendantsPlugin;
import au.gov.asd.tac.constellation.functionality.select.structure.SelectSingletonsPlugin;
import au.gov.asd.tac.constellation.functionality.select.structure.SelectSinksPlugin;
import au.gov.asd.tac.constellation.functionality.select.structure.SelectSourcesPlugin;
import au.gov.asd.tac.constellation.functionality.zoom.PreviousViewPlugin;
import au.gov.asd.tac.constellation.functionality.zoom.ResetViewPlugin;
import au.gov.asd.tac.constellation.functionality.zoom.RotateCameraPlugin;
import au.gov.asd.tac.constellation.functionality.zoom.SetCameraVisibilityRange;
import au.gov.asd.tac.constellation.functionality.zoom.ZoomToSelectionPlugin;
import au.gov.asd.tac.constellation.functionality.zoom.ZoomToVerticesPlugin;

/**
 * List of all core actions/plugins
 *
 * @author algol
 */
public final class CorePluginRegistry {

    public static final String ADD_BLAZE = AddBlazePlugin.class.getName();
    public static final String ADD_CUSTOM_BLAZE = AddCustomBlazePlugin.class.getName();
    public static final String AUTOSAVE_SINGLE = AutosaveGraphPlugin.class.getName();
    public static final String CHANGE_SELECTION = ChangeSelectionPlugin.class.getName();
    public static final String CLOSE_GRAPH = CloseGraphPlugin.class.getName();
    public static final String COMPARE_GRAPH = CompareGraphPlugin.class.getName();
    public static final String COMPOSITE_CORRELATED_NODES = CreateCompositesFromDominantNodesPlugin.class.getName();
    public static final String CONTRACT_ALL_COMPOSITES = ContractAllCompositesPlugin.class.getName();
    public static final String COPY = CopyToClipboardPlugin.class.getName();
    public static final String COPY_TO_NEW_GRAPH = CopyToNewGraphPlugin.class.getName();
    public static final String CREATE_COMPOSITE_FROM_DOMINANT_NODES = CreateCompositesFromDominantNodesPlugin.class.getName();
    public static final String CREATE_COMPOSITE_FROM_SELECTION = CreateCompositeFromSelectionPlugin.class.getName();
    public static final String CUT = CutToClipboardPlugin.class.getName();
    public static final String DELETE_SELECTION = DeleteSelectionPlugin.class.getName();
    public static final String DESELECT_ALL = DeselectAllPlugin.class.getName();
    public static final String DESELECT_BLAZES = DeSelectBlazesPlugin.class.getName();
    public static final String DESELECT_TRANSACTIONS = DeselectTransactionsPlugin.class.getName();
    public static final String DESELECT_VERTICES = DeselectVerticesPlugin.class.getName();
    public static final String DESTROY_ALL_COMPOSITES = DestroyAllCompositesPlugin.class.getName();
    public static final String DEV_OPS_NOTIFICATION = DevOpsNotificationPlugin.class.getName();
    public static final String DIM_ALL = DimAllPlugin.class.getName();
    public static final String DIM_SELECTED = DimSelectedPlugin.class.getName();
    public static final String DIM_UNSELECTED = DimUnselectedPlugin.class.getName();
    public static final String EXPAND_ALL_COMPOSITES = ExpandAllCompositesPlugin.class.getName();
    public static final String HOP_OUT = HopOutPlugin.class.getName();
    public static final String INVERT_SELECTION = InvertSelectionPlugin.class.getName();
    public static final String OPEN_IN_BROWSER = OpenInBrowserPlugin.class.getName();
    public static final String PASTE = PasteFromClipboardPlugin.class.getName();
    public static final String PASTE_GRAPH = PasteGraphPlugin.class.getName();
    public static final String PASTE_TEXT = PasteTextPlugin.class.getName();
    public static final String PERMANENT_NODE_MERGE = PermanentMergePlugin.class.getName();
    public static final String PREVIOUS_VIEW = PreviousViewPlugin.class.getName();
    public static final String REMOVE_BLAZE = RemoveBlazePlugin.class.getName();
    public static final String RESET = ResetViewPlugin.class.getName();
    public static final String ROTATE_GRAPH = RotateCameraPlugin.class.getName();
    public static final String SAVE_GRAPH = SaveGraphPlugin.class.getName();
    public static final String SELECT_ALL = SelectAllPlugin.class.getName();
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
    public static final String SEND_TO_EMAIL_CLIENT = SendToEmailClientPlugin.class.getName();
    public static final String SET_CAMERA_VISIBILITY_RANGE = SetCameraVisibilityRange.class.getName();
    public static final String SET_CONNECTION_MODE = SetConnectionModePlugin.class.getName();
    public static final String SET_DRAW_FLAG = SetDrawFlagPlugin.class.getName();
    public static final String SET_VISIBLE_ABOVE_THRESHOLD = SetVisibleAboveThresholdPlugin.class.getName();
    public static final String TOGGLE_DISPLAY_MODE = ToggleDisplayModePlugin.class.getName();
    public static final String TOGGLE_DRAW_DIRECTED = ToggleDrawDirectedPlugin.class.getName();
    public static final String TOGGLE_DRAW_FLAG = ToggleDrawFlagPlugin.class.getName();
    public static final String TOGGLE_SELECTION_MODE = ToggleSelectionModePlugin.class.getName();
    public static final String UNDIM_ALL = UndimAllPlugin.class.getName();
    public static final String UNDIM_SELECTED = UndimSelectedPlugin.class.getName();
    public static final String UNDIM_UNSELECTED = UndimUnselectedPlugin.class.getName();
    public static final String UPDATE_BLAZE_SIZE_OPACITY = UpdateBlazeSizeOpacityPlugin.class.getName();
    public static final String ZOOM_TO_SELECTION = ZoomToSelectionPlugin.class.getName();
    public static final String ZOOM_TO_VERTICES = ZoomToVerticesPlugin.class.getName();
}
