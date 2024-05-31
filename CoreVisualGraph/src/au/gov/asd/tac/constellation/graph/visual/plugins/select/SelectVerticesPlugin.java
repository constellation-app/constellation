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
package au.gov.asd.tac.constellation.graph.visual.plugins.select;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.operations.SetBooleanValuesOperation;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLogger;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.Properties;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Select all vertices and transactions.
 *
 * @author Quasar985
 */
@ServiceProvider(service = Plugin.class)
@PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
@Messages("SelectVerticesPlugin=Add to Selection: Vertices")
public class SelectVerticesPlugin extends SimpleEditPlugin {

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final Properties properties = new Properties();
        final int vxSelected = VisualConcept.VertexAttribute.SELECTED.get(graph);
        if (vxSelected != Graph.NOT_FOUND) {
            final SetBooleanValuesOperation selectVerticesOperation = new SetBooleanValuesOperation(graph, GraphElementType.VERTEX, vxSelected);
            final int vertexCount = graph.getVertexCount();
            for (int position = 0; position < vertexCount; position++) {
                final int vertex = graph.getVertex(position);
                selectVerticesOperation.setValue(vertex, true);
            }
            graph.executeGraphOperation(selectVerticesOperation);
            properties.setProperty("vsize", String.valueOf(selectVerticesOperation.size()));
        }

        ConstellationLogger.getDefault().pluginProperties(this, properties);
    }
}
