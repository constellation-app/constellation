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
package au.gov.asd.tac.constellation.graph.visual.plugins.select;

import au.gov.asd.tac.constellation.graph.Graph;
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
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType.ObjectParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Change Selection Plugin
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("ChangeSelectionPlugin=Change Selection")
@PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
public class ChangeSelectionPlugin extends SimpleEditPlugin {

    public static final String ELEMENT_BIT_SET_PARAMETER_ID = PluginParameter.buildId(ChangeSelectionPlugin.class, "element_bit_set");
    public static final String ELEMENT_TYPE_PARAMETER_ID = PluginParameter.buildId(ChangeSelectionPlugin.class, "element_type");
    public static final String SELECTION_MODE_PARAMETER_ID = PluginParameter.buildId(ChangeSelectionPlugin.class, "selection_mode");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<ObjectParameterValue> elementBitSet = ObjectParameterType.build(ELEMENT_BIT_SET_PARAMETER_ID);
        elementBitSet.setName("Element Ids");
        elementBitSet.setDescription("A set of the element id's, such as the vertex ids");
        parameters.addParameter(elementBitSet);

        final PluginParameter<SingleChoiceParameterValue> elementTypeParameter = SingleChoiceParameterType.build(ELEMENT_TYPE_PARAMETER_ID, ElementTypeParameterValue.class);
        elementTypeParameter.setName("Element Type");
        elementTypeParameter.setDescription("The graph element type");
        final List<ElementTypeParameterValue> elementTypes = new ArrayList<>();
        elementTypes.add(new ElementTypeParameterValue(GraphElementType.TRANSACTION));
        elementTypes.add(new ElementTypeParameterValue(GraphElementType.VERTEX));
        SingleChoiceParameterType.setOptionsData(elementTypeParameter, elementTypes);
        parameters.addParameter(elementTypeParameter);

        final PluginParameter<ObjectParameterValue> selectionMode = ObjectParameterType.build(SELECTION_MODE_PARAMETER_ID);
        selectionMode.setName("Selection Mode");
        selectionMode.setDescription("The selection mode being ADD, REMOVE, REPLACE or INVERT");
        parameters.addParameter(selectionMode);

        return parameters;
    }

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final BitSet elementBitSet = (BitSet) parameters.getParameters().get(ELEMENT_BIT_SET_PARAMETER_ID).getObjectValue();
        final GraphElementType elementType = (GraphElementType) ((ElementTypeParameterValue) parameters.getSingleChoice(ELEMENT_TYPE_PARAMETER_ID)).getObjectValue();
        final SelectionMode selectionMode = (SelectionMode) parameters.getParameters().get(SELECTION_MODE_PARAMETER_ID).getObjectValue();

        final int selectedAttribute = graph.getAttribute(elementType, "selected");
        if (selectedAttribute == Graph.NOT_FOUND) {
            return;
        }

        final int elementCount = elementType.getElementCount(graph);
        for (int elementPosition = 0; elementPosition < elementCount; elementPosition++) {
            final int elementId = elementType.getElement(graph, elementPosition);
            final boolean currentlySelected = graph.getBooleanValue(selectedAttribute, elementId);
            final boolean newSelected = elementBitSet.get(elementId);
            graph.setBooleanValue(selectedAttribute, elementId, selectionMode.calculateSelection(currentlySelected, newSelected));
        }
    }
}
