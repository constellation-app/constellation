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
package au.gov.asd.tac.constellation.functionality.composite;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.visual.composites.CompositeUtilities;
import au.gov.asd.tac.constellation.pluginframework.Plugin;
import au.gov.asd.tac.constellation.pluginframework.PluginException;
import au.gov.asd.tac.constellation.pluginframework.PluginExecution;
import au.gov.asd.tac.constellation.pluginframework.PluginInteraction;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameters;
import au.gov.asd.tac.constellation.pluginframework.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.schema.visualschema.VisualSchemaPluginRegistry;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * This plugin destroys all composites in the graph.
 * <p>
 * This is achieved by first expanding contracted composites into their
 * constituents, and then setting the composite state of all constituents to
 * null. If there are no composites or composite constituents in the graph, this
 * plugin will do nothing.
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("DestroyAllCompositesPlugin=Destroy Composites")
public class DestroyAllCompositesPlugin extends SimpleEditPlugin {

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {

        final boolean anythingDestroyed = CompositeUtilities.destroyAllComposites(graph);
        if (anythingDestroyed) {
            PluginExecution.withPlugin(VisualSchemaPluginRegistry.COMPLETE_SCHEMA).executeNow(graph);
        }
    }
}
