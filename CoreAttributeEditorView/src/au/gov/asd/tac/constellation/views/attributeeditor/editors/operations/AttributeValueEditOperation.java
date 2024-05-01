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
package au.gov.asd.tac.constellation.views.attributeeditor.editors.operations;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.NativeAttributeType;
import au.gov.asd.tac.constellation.graph.attribute.interaction.AttributeValueTranslator;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLoggerHelper;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.attributeeditor.AttributeData;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author twilight_sparkle
 */
public class AttributeValueEditOperation extends PluginSequenceEditOperation {

    private final AttributeData attributeData;
    private final boolean completeWithSchema;
    private final AttributeValueTranslator translator;
    private Object value;

    public AttributeValueEditOperation(final AttributeData attributeData, final boolean completeWithSchema, final AttributeValueTranslator translator) {
        this.attributeData = attributeData;
        this.completeWithSchema = completeWithSchema;
        this.translator = translator;
    }

    @Override
    public Plugin mainEdit(final Object value) {
        this.value = value;
        return new AttributeEditPlugin();
    }

    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.MODIFY})
    private final class AttributeEditPlugin extends SimpleEditPlugin {

        @Override
        public String getName() {
            return "Attribute Editor: Set Attribute Values";
        }

        @Override
        protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {

            final Object newValue = translator.translate(value);
            final NativeAttributeType nativeType = graph.getNativeAttributeType(attributeData.getAttributeId());
            final NativeAttributeType.NativeValue nativeValue = nativeType.create(newValue);
            final GraphElementType elementType = attributeData.getElementType();

            final int selectedAttribute = graph.getAttribute(elementType.getSelectionElementType(), VisualConcept.VertexAttribute.SELECTED.getName());

            final int elementCount = elementType.getElementCount(graph);
            final List<String> previousValues = new ArrayList<>(elementCount);

            for (int position = 0; position < elementCount; position++) {
                final int element = elementType.getElement(graph, position);
                if (!elementType.canBeSelected() || elementType.isSelected(graph, element, selectedAttribute)) {
                    previousValues.add(
                            graph.getObjectValue(attributeData.getAttributeId(), position) != null ? graph.getStringValue(attributeData.getAttributeId(), position) : ""
                    );
                    nativeType.set(graph, attributeData.getAttributeId(), element, nativeValue);
                    if (completeWithSchema) {
                        elementType.completeWithSchema(graph, element);
                    }
                }
            }

            // create a string of the previous values separated by a comma
            final StringBuilder sb = new StringBuilder();
            previousValues.stream().forEach(previousValue -> {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(previousValue);
            });

            // log the edit
            // if we determine that this becomes a performance drain then we can remove the previous values
            ConstellationLoggerHelper.updatePropertyBuilder(this, sb.toString(), newValue == null ? "" : newValue.toString());
        }
    }

}
