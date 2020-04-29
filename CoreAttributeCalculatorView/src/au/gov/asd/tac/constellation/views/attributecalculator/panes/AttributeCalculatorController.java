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
package au.gov.asd.tac.constellation.views.attributecalculator.panes;

import au.gov.asd.tac.constellation.views.attributecalculator.plugins.AttributeCalculatorPlugin;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.DateAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.IntegerAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.LocalDateTimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.TimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.TransactionTypeAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.VertexTypeAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.ColorAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.IconAttributeDescription;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.views.attributecalculator.AttributeCalculatorTopComponent;
import java.util.Arrays;
import java.util.List;
import org.openide.awt.StatusDisplayer;

/**
 *
 * @author sirius
 */
public class AttributeCalculatorController {

    public static final List<String> ATTRIBUTE_TYPES = Arrays.asList(StringAttributeDescription.ATTRIBUTE_NAME, IntegerAttributeDescription.ATTRIBUTE_NAME, BooleanAttributeDescription.ATTRIBUTE_NAME, FloatAttributeDescription.ATTRIBUTE_NAME, ColorAttributeDescription.ATTRIBUTE_NAME, IconAttributeDescription.ATTRIBUTE_NAME, ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME, TimeAttributeDescription.ATTRIBUTE_NAME, DateAttributeDescription.ATTRIBUTE_NAME, LocalDateTimeAttributeDescription.ATTRIBUTE_NAME, VertexTypeAttributeDescription.ATTRIBUTE_NAME, TransactionTypeAttributeDescription.ATTRIBUTE_NAME);

    private final AttributeCalculatorTopComponent parent;

    public AttributeCalculatorController(final AttributeCalculatorTopComponent parent) {
        this.parent = parent;
    }

    public void execute() {
        final AttributeCalculatorPane attributeCalculatorPane = parent.getContent();
        if (attributeCalculatorPane != null) {
            final String script = attributeCalculatorPane.getScript();
            if (!script.trim().isEmpty()) {
                final GraphElementType elementType = attributeCalculatorPane.getElementType();
                final String attribute = attributeCalculatorPane.getAttribute();
                final String attributeType = attributeCalculatorPane.getAttributeType();
                final boolean selectedOnly = attributeCalculatorPane.isSelectedOnly();
                final boolean completeWithSchema = attributeCalculatorPane.isCompleteWithSchema();

                final AttributeCalculatorPlugin plugin = new AttributeCalculatorPlugin(elementType, attribute, attributeType, "text/python", script, selectedOnly, completeWithSchema);
                PluginExecution.withPlugin(plugin).executeLater(parent.getCurrentGraph());
            } else {
                StatusDisplayer.getDefault().setStatusText("Not executing empty script.");
            }
        }
    }
}
