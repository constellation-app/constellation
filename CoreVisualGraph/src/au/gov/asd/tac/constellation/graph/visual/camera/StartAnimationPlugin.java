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
package au.gov.asd.tac.constellation.graph.visual.camera;

import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameter;

/**
 * Reset the camera.
 *
 * @author algol
 */
//@ServiceProvider(service = Plugin.class)
//@PluginInfo(minLogInterval = 5000, pluginType = PluginType.DISPLAY, tags = {"LOW LEVEL"})
//@Messages("ResetViewPlugin=Reset View")
public final class StartAnimationPlugin /*extends SimpleEditPlugin*/ {
//

    public static final String ANIMATION_PARAMETER_ID = PluginParameter.buildId(StartAnimationPlugin.class, "animation");
//
//    @Override
//    public PluginParameters createParameters() {
//        final PluginParameters parameters = new PluginParameters();
//
//        final PluginParameter<ObjectParameterValue> animationParam = ObjectParameterType.buildId(ANIMATION_PARAMETER_ID);
//        animationParam.setName(ANIMATION_PARAMETER_ID);
//        animationParam.setStringValue("z");
//        parameters.addParameter(animationParam);
//
//        return parameters;
//    }
//
//    @Override
//    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
//        final int cameraAttribute = graph.getAttribute(GraphElementType.META, Camera.ATTRIBUTE_NAME);
//        final Camera camera = new Camera(graph.getObjectValue(cameraAttribute, 0));
////        camera.animation = (Animation) parameters.getObjectValue(ANIMATION_PARAMETER_ID);
//        graph.setObjectValue(cameraAttribute, 0, camera);
//    }
}
