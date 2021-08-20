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
package au.gov.asd.tac.constellation.views.layers;

import au.gov.asd.tac.constellation.views.layers.LayersViewController.AddAttributesPlugin;
import au.gov.asd.tac.constellation.views.layers.LayersViewController.CaptureListenedAttributesPlugin;
import au.gov.asd.tac.constellation.views.layers.LayersViewController.LayersStateReaderPlugin;
import au.gov.asd.tac.constellation.views.layers.LayersViewController.LayersStateWriterPlugin;
import au.gov.asd.tac.constellation.views.layers.LayersViewController.UpdateQueryPlugin;
import au.gov.asd.tac.constellation.views.layers.shortcut.DeselectAllLayersPlugin;
import au.gov.asd.tac.constellation.views.layers.shortcut.EnableLayerPlugin;
import au.gov.asd.tac.constellation.views.layers.shortcut.NewLayerPlugin;
import au.gov.asd.tac.constellation.views.layers.utilities.ShuffleElementBitmaskPlugin;
import au.gov.asd.tac.constellation.views.layers.utilities.UpdateElementBitmaskPlugin;
import au.gov.asd.tac.constellation.views.layers.utilities.UpdateLayerSelectionPlugin;

/**
 * Registry of Layers View plugins.
 *
 * @author aldebaran30701
 */
public final class LayersPluginRegistry {

    // Utility
    public static final String UPDATE_ELEMENT_BITMASK = UpdateElementBitmaskPlugin.class.getName();
    public static final String UPDATE_LAYER_SELECTION = UpdateLayerSelectionPlugin.class.getName();
    public static final String SHUFFLE_ELEMENT_BITMASK = ShuffleElementBitmaskPlugin.class.getName();

    // Shortcuts
    public static final String DESELECT_ALL_LAYERS = DeselectAllLayersPlugin.class.getName();
    public static final String ENABLE_LAYER = EnableLayerPlugin.class.getName();
    public static final String NEW_LAYER = NewLayerPlugin.class.getName();

    // Controller
    public static final String ADD_ATTRIBUTES = AddAttributesPlugin.class.getName();
    public static final String CAPTURE_LISTENED_ATTRIBUTES = CaptureListenedAttributesPlugin.class.getName();
    public static final String UPDATE_QUERY = UpdateQueryPlugin.class.getName();

    // State
    public static final String STATE_WRITER = LayersStateWriterPlugin.class.getName();
    public static final String STATE_READER = LayersStateReaderPlugin.class.getName();
}
