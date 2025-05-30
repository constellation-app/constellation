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
package au.gov.asd.tac.constellation.views.scatterplot.axis;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.FloatObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.IntegerAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.IntegerObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.LongAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.LongObjectAttributeDescription;
import java.util.Arrays;
import java.util.List;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = AxisBuilder.class)
public class NumberAxisBuilder implements AxisBuilder<Number> {

    @Override
    public List<String> getTypes() {
        return Arrays.asList(IntegerAttributeDescription.ATTRIBUTE_NAME, IntegerObjectAttributeDescription.ATTRIBUTE_NAME,
                FloatAttributeDescription.ATTRIBUTE_NAME, FloatObjectAttributeDescription.ATTRIBUTE_NAME,
                LongAttributeDescription.ATTRIBUTE_NAME, LongObjectAttributeDescription.ATTRIBUTE_NAME);
    }

    @Override
    public Number getValue(final GraphReadMethods graph, final GraphElementType elementType, final int attributeId, final int elementId) {
        return graph.getFloatValue(attributeId, elementId);
    }

    @Override
    public Axis<Number> build() {
        final NumberAxis axis = new NumberAxis();
        axis.setAutoRanging(true);
        axis.setForceZeroInRange(false);
        return axis;
    }
}
