/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.arrangements.group;

import au.gov.asd.tac.constellation.plugins.arrangements.SelectedInclusionGraph;
import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Plugin framework for arrange by level.
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@Messages("ArrangeByLayerPlugin=Arrange by Layer")
public class ArrangeByLayerPlugin extends SimpleEditPlugin {

    public static final String ATTRIBUTE_LABEL_PARAMETER_ID = PluginParameter.buildId(ArrangeByLayerPlugin.class, "attribute_label");

    @Override
    public void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final String attrLabel = parameters.getParameters().get(ATTRIBUTE_LABEL_PARAMETER_ID).getStringValue();
        if (attrLabel != null) {
            final SelectedInclusionGraph selectedGraph = new SelectedInclusionGraph(wg, SelectedInclusionGraph.Connections.NONE);
            final Attribute attr = new GraphAttribute(wg, wg.getAttribute(GraphElementType.VERTEX, attrLabel));
            selectedGraph.addAttributeToCopy(attr);

            final GraphWriteMethods ig = selectedGraph.getInclusionGraph();
            final int iattrId = ig.getAttribute(GraphElementType.VERTEX, attrLabel);
            final LayerArranger arranger = new LayerArranger();
            arranger.setLevelAttr(iattrId);
            arranger.setMaintainMean(!selectedGraph.isArrangingAll());
            arranger.arrange(ig);
            selectedGraph.retrieveCoords();
        }
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<StringParameterValue> attrParam = StringParameterType.build(ATTRIBUTE_LABEL_PARAMETER_ID);
        attrParam.setName("Atribute Label");
        attrParam.setDescription("The name of the attribute");
        parameters.addParameter(attrParam);

        return parameters;
    }
}
