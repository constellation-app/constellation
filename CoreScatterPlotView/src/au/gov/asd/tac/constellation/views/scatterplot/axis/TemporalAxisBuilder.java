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
import au.gov.asd.tac.constellation.graph.attribute.DateAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.TimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javafx.scene.chart.Axis;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = AxisBuilder.class)
public class TemporalAxisBuilder implements AxisBuilder<Date> {

    @Override
    public List<String> getTypes() {
        return Arrays.asList(ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME, DateAttributeDescription.ATTRIBUTE_NAME, TimeAttributeDescription.ATTRIBUTE_NAME);
    }

    @Override
    public Date getValue(final GraphReadMethods graph, final GraphElementType elementType, final int attributeId, final int elementId) {
        return graph.getLongValue(attributeId, elementId) == 0L
                ? Date.from(Instant.EPOCH) : Date.from(Instant.ofEpochMilli(graph.getLongValue(attributeId, elementId)));

    }

    @Override
    public Axis<Date> build() {
        final DateAxis axis = new DateAxis();
        axis.setAutoRanging(true);
        return axis;
    }
}
