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
package au.gov.asd.tac.constellation.views.scatterplot.axis;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import java.util.Collections;
import java.util.List;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = AxisBuilder.class)
public class CategoryAxisBuilder implements AxisBuilder<String> {

    @Override
    public List<String> getTypes() {
        return Collections.emptyList();
    }

    @Override
    public String getValue(final GraphReadMethods graph, final GraphElementType elementType, final int attributeId, final int elementId) {
        return graph.getStringValue(attributeId, elementId) == null
                ? "<No Value>" : graph.getStringValue(attributeId, elementId);
    }

    @Override
    public Axis<String> build() {
        final CategoryAxis axis = new CategoryAxis();
        axis.setAutoRanging(true);
        return axis;
    }
}
