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
package au.gov.asd.tac.constellation.plugins.arrangements.broccoli;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("ArrangeLikeBroccoliPlugin=Arrange like Broccoli")
@PluginInfo(pluginType = PluginType.DISPLAY, tags = {PluginTags.MODIFY})
public class ArrangeLikeBroccoliPlugin extends SimpleEditPlugin {

    @Override
    public void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        // We don't use a SelectionGraph for this arrangement.
        // It needs to see the whole graph, so it handles selections itself.
        final BroccoliArranger cw = new BroccoliArranger();
        cw.arrange(wg);
    }
}
