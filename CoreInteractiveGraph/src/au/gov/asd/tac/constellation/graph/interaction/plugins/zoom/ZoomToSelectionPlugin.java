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
package au.gov.asd.tac.constellation.graph.interaction.plugins.zoom;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.interaction.animation.AnimationUtilities;
import au.gov.asd.tac.constellation.graph.interaction.animation.PanAnimation;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.visual.utilities.BoundingBoxUtilities;
import au.gov.asd.tac.constellation.graph.visual.utilities.VisualGraphUtilities;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.camera.BoundingBox;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.camera.CameraUtilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Reset the camera.
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@Messages("ZoomToSelectionPlugin=Zoom to Selection")
@PluginInfo(minLogInterval = 5000, pluginType = PluginType.VIEW, tags = {PluginTags.VIEW})
public final class ZoomToSelectionPlugin extends SimpleEditPlugin {

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final Camera oldCamera = VisualGraphUtilities.getCamera(graph);
        final BoundingBox box = new BoundingBox();
        final Camera camera = new Camera(oldCamera);
        BoundingBoxUtilities.recalculateFromGraph(box, graph, true);
        CameraUtilities.zoomToBoundingBox(camera, box);
        final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
        if (activeGraph != null && activeGraph.getId().equals(graph.getId())) {
            // Only do the camera animation if the edited graph is currently active
            AnimationUtilities.startAnimation(new PanAnimation("Zoom to Selection", oldCamera, camera, true), activeGraph.getId());
        } else {
            // Skip the animation, just set the new camera position
            VisualGraphUtilities.setCamera(graph, camera);
        }        
    }
}
