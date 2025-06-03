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
package au.gov.asd.tac.constellation.graph.interaction;

import au.gov.asd.tac.constellation.graph.interaction.plugins.DragElementsPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard.CopyToClipboardPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard.CopyToNewGraphPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard.CutToClipboardPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard.PasteFromClipboardPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard.PasteGraphPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard.PasteTextPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.composite.ContractAllCompositesPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.composite.CreateCompositeFromSelectionPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.composite.CreateCompositesFromDominantNodesPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.composite.DestroyAllCompositesPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.composite.ExpandAllCompositesPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.delete.DeleteSelectionPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.display.SetConnectionModePlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.display.SetDrawFlagPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.display.SetVisibleAboveThresholdPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.display.ToggleDisplayModePlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.display.ToggleDrawFlagPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.draw.CreateTransactionPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.draw.CreateVertexPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.draw.ToggleDrawDirectedPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.draw.ToggleSelectionModePlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.io.AutosaveGraphPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.io.CloseGraphPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.io.SaveGraphPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.select.BoxSelectionPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.select.FreeformSelectionPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.select.PointSelectionPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.zoom.PreviousViewPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.zoom.ResetViewPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.zoom.RotateCameraPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.zoom.SetCameraVisibilityRange;
import au.gov.asd.tac.constellation.graph.interaction.plugins.zoom.ZoomInPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.zoom.ZoomOutPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.zoom.ZoomToSelectionPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.zoom.ZoomToVerticesPlugin;

/**
 * Interactive Graph Plugin Registry.
 *
 * @author cygnus_x-1
 */
public class InteractiveGraphPluginRegistry {

    public static final String AUTOSAVE_GRAPH = AutosaveGraphPlugin.class.getName();
    public static final String BOX_SELECTION = BoxSelectionPlugin.class.getName();
    public static final String CLOSE_GRAPH = CloseGraphPlugin.class.getName();
    public static final String COMPOSITE_CORRELATED_NODES = CreateCompositesFromDominantNodesPlugin.class.getName();
    public static final String CONTRACT_ALL_COMPOSITES = ContractAllCompositesPlugin.class.getName();
    public static final String COPY = CopyToClipboardPlugin.class.getName();
    public static final String COPY_TO_NEW_GRAPH = CopyToNewGraphPlugin.class.getName();
    public static final String CREATE_COMPOSITE_FROM_DOMINANT_NODES = CreateCompositesFromDominantNodesPlugin.class.getName();
    public static final String CREATE_COMPOSITE_FROM_SELECTION = CreateCompositeFromSelectionPlugin.class.getName();
    public static final String CREATE_TRANSACTION = CreateTransactionPlugin.class.getName();
    public static final String CREATE_VERTEX = CreateVertexPlugin.class.getName();
    public static final String CUT = CutToClipboardPlugin.class.getName();
    public static final String DELETE_SELECTION = DeleteSelectionPlugin.class.getName();
    public static final String DESTROY_ALL_COMPOSITES = DestroyAllCompositesPlugin.class.getName();
    public static final String DRAG_ELEMENTS = DragElementsPlugin.class.getName();
    public static final String EXPAND_ALL_COMPOSITES = ExpandAllCompositesPlugin.class.getName();
    public static final String FREEFORM_SELECTION = FreeformSelectionPlugin.class.getName();
    public static final String PASTE = PasteFromClipboardPlugin.class.getName();
    public static final String PASTE_GRAPH = PasteGraphPlugin.class.getName();
    public static final String PASTE_TEXT = PasteTextPlugin.class.getName();
    public static final String POINT_SELECTION = PointSelectionPlugin.class.getName();
    public static final String PREVIOUS_VIEW = PreviousViewPlugin.class.getName();
    public static final String RESET_VIEW = ResetViewPlugin.class.getName();
    public static final String ROTATE_GRAPH = RotateCameraPlugin.class.getName();
    public static final String SAVE_GRAPH = SaveGraphPlugin.class.getName();
    public static final String SET_CAMERA_VISIBILITY_RANGE = SetCameraVisibilityRange.class.getName();
    public static final String SET_CONNECTION_MODE = SetConnectionModePlugin.class.getName();
    public static final String SET_DRAW_FLAG = SetDrawFlagPlugin.class.getName();
    public static final String SET_VISIBLE_ABOVE_THRESHOLD = SetVisibleAboveThresholdPlugin.class.getName();
    public static final String TOGGLE_DISPLAY_MODE = ToggleDisplayModePlugin.class.getName();
    public static final String TOGGLE_DRAW_FLAG = ToggleDrawFlagPlugin.class.getName();
    public static final String TOGGLE_DRAW_DIRECTED = ToggleDrawDirectedPlugin.class.getName();
    public static final String TOGGLE_SELECTION_MODE = ToggleSelectionModePlugin.class.getName();
    public static final String ZOOM_IN = ZoomInPlugin.class.getName();
    public static final String ZOOM_OUT = ZoomOutPlugin.class.getName();
    public static final String ZOOM_TO_SELECTION = ZoomToSelectionPlugin.class.getName();
    public static final String ZOOM_TO_VERTICES = ZoomToVerticesPlugin.class.getName();
}
