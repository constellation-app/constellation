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
package au.gov.asd.tac.constellation.views.histogram;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.ElementTypeParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * A plug-in that clears the filter on the histogram. This will cause all
 * elements to be included in the histogram display until the user creates a new
 * filter.
 *
 * @author sirius
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("HistogramClearFilterPlugin=Clear Histogram Filter")
@PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.LOW_LEVEL})
public class HistogramClearFilterPlugin extends SimpleEditPlugin {

    public static final String ELEMENT_TYPE_PARAMETER_ID = PluginParameter.buildId(HistogramClearFilterPlugin.class, "element_type");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<SingleChoiceParameterType.SingleChoiceParameterValue> elementTypeParameter = SingleChoiceParameterType.build(ELEMENT_TYPE_PARAMETER_ID, ElementTypeParameterValue.class);
        elementTypeParameter.setName("Element Type");
        elementTypeParameter.setDescription("The graph element type");
        final List<ElementTypeParameterValue> elementTypes = new ArrayList<>();
        elementTypes.add(new ElementTypeParameterValue(GraphElementType.TRANSACTION));
        elementTypes.add(new ElementTypeParameterValue(GraphElementType.VERTEX));
        elementTypes.add(new ElementTypeParameterValue(GraphElementType.EDGE));
        elementTypes.add(new ElementTypeParameterValue(GraphElementType.LINK));
        SingleChoiceParameterType.setOptionsData(elementTypeParameter, elementTypes);
        parameters.addParameter(elementTypeParameter);

        return parameters;
    }

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final GraphElementType elementType = (GraphElementType) ((ElementTypeParameterValue) parameters.getSingleChoice(ELEMENT_TYPE_PARAMETER_ID)).getObjectValue();

        final int histogramStateAttribute = HistogramConcept.MetaAttribute.HISTOGRAM_STATE.ensure(graph);
        HistogramState histogramState = (HistogramState) graph.getObjectValue(histogramStateAttribute, 0);
        histogramState = new HistogramState(histogramState);
        histogramState.setFilter(elementType, null);
        graph.setObjectValue(histogramStateAttribute, 0, histogramState);
    }
}
