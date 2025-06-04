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
package au.gov.asd.tac.constellation.plugins.arrangements.group;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.arrangements.AbstractInclusionGraph.Connections;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import au.gov.asd.tac.constellation.plugins.arrangements.GraphTaxonomyArranger;
import au.gov.asd.tac.constellation.plugins.arrangements.SelectedInclusionGraph;
import au.gov.asd.tac.constellation.plugins.arrangements.grid.GridArranger;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Arrange By Attribute Plugin
 *
 * @author canis_majoris
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("ArrangeByNodeAttributePlugin=Arrange by Node Attribute")
@PluginInfo(pluginType = PluginType.DISPLAY, tags = {PluginTags.MODIFY})
public class ArrangeByNodeAttributePlugin extends SimpleEditPlugin {

    public static final String THREE_D_PARAMETER_ID = PluginParameter.buildId(ArrangeByNodeAttributePlugin.class, "3D");
    public static final String ATTRIBUTE_PARAMETER_ID = PluginParameter.buildId(ArrangeByNodeAttributePlugin.class, "attribute");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<BooleanParameterType.BooleanParameterValue> threeD = BooleanParameterType.build(THREE_D_PARAMETER_ID);
        threeD.setName("Arrange in 3D");
        threeD.setDescription("If checked, arrangement adjusts z-axis positions of nodes rather than organising into groups in 2D");
        threeD.setBooleanValue(false);
        parameters.addParameter(threeD);

        final PluginParameter<SingleChoiceParameterValue> attribute = SingleChoiceParameterType.build(ATTRIBUTE_PARAMETER_ID);
        attribute.setName("Attribute");
        attribute.setDescription("Attribute to arrange by.");
        parameters.addParameter(attribute);

        return parameters;
    }

    @Override
    public void updateParameters(final Graph graph, final PluginParameters parameters) {
        final List<String> attributes = new ArrayList<>();

        if (graph != null) {
            final ReadableGraph readableGraph = graph.getReadableGraph();
            try {
                final int axCount = readableGraph.getAttributeCount(GraphElementType.VERTEX);
                for (int i = 0; i < axCount; i++) {
                    final int ax = readableGraph.getAttribute(GraphElementType.VERTEX, i);
                    attributes.add(readableGraph.getAttributeName(ax));
                }
            } finally {
                readableGraph.release();
            }
        }
        Collections.sort(attributes);
        if (parameters != null && parameters.getParameters() != null) {
            @SuppressWarnings("unchecked") //ATTRIBUTE_PARAMETER always of type SingleChoiceParameter
            final PluginParameter<SingleChoiceParameterValue> attribute = (PluginParameter<SingleChoiceParameterValue>) parameters.getParameters().get(ATTRIBUTE_PARAMETER_ID);
            SingleChoiceParameterType.setOptions(attribute, attributes);
            SingleChoiceParameterType.setChoice(attribute, attributes.get(0));
        }
    }

    @Override
    public void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final boolean threeD = parameters.getParameters().get(THREE_D_PARAMETER_ID).getBooleanValue();
        final String attribute = parameters.getParameters().get(ATTRIBUTE_PARAMETER_ID).getStringValue();

        if (threeD) {
            if (attribute != null) {
                final SelectedInclusionGraph selectedGraph = new SelectedInclusionGraph(wg, SelectedInclusionGraph.Connections.NONE);
                final Attribute attr = new GraphAttribute(wg, wg.getAttribute(GraphElementType.VERTEX, attribute));
                selectedGraph.addAttributeToCopy(attr);

                final GraphWriteMethods ig = selectedGraph.getInclusionGraph();
                final int iattrId = ig.getAttribute(GraphElementType.VERTEX, attribute);
                final LayerArranger arranger = new LayerArranger();
                arranger.setLevelAttr(iattrId);
                arranger.setMaintainMean(!selectedGraph.isArrangingAll());
                arranger.arrange(ig);
                selectedGraph.retrieveCoords();
            }
        } else {
            if (attribute != null) {
                final Arranger inner = new GridArranger();
                final Arranger outer = new GridArranger();
                final GraphTaxonomyArranger arranger = new ArrangeByGroupTaxonomy(inner, outer, Connections.NONE, attribute);
                arranger.setMaintainMean(true);

                final SelectedInclusionGraph selectedGraph = new SelectedInclusionGraph(wg, Connections.NONE);
                selectedGraph.addAttributeToCopy(new GraphAttribute(wg, wg.getAttribute(GraphElementType.VERTEX, attribute)));
                arranger.arrange(selectedGraph.getInclusionGraph());
                selectedGraph.retrieveCoords();
            }
        }
    }
}
