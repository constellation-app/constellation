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
package au.gov.asd.tac.constellation.graph.utilities.placeholder;

import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.utilities.VertexDominanceCalculator;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.utilities.wrapper.GraphVertex;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 * Analytic Placeholder Dominance Calculator.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = VertexDominanceCalculator.class, position = Integer.MAX_VALUE)
public class PlaceholderDominanceCalculator extends VertexDominanceCalculator<GraphVertex> {

    @Override
    public List<SchemaVertexType> getTypePriority() {
        List<SchemaVertexType> priority = new ArrayList<>();
        priority.add(AnalyticConcept.VertexType.TELEPHONE_IDENTIFIER);
        priority.add(AnalyticConcept.VertexType.EMAIL_ADDRESS);
        priority.add(AnalyticConcept.VertexType.USER_NAME);
        priority.add(AnalyticConcept.VertexType.ONLINE_IDENTIFIER);
        priority.add(AnalyticConcept.VertexType.URL);
        priority.add(AnalyticConcept.VertexType.HOST_NAME);
        priority.add(AnalyticConcept.VertexType.ONLINE_LOCATION);
        priority.add(AnalyticConcept.VertexType.MACHINE_IDENTIFIER);
        priority.add(AnalyticConcept.VertexType.IPV6);
        priority.add(AnalyticConcept.VertexType.IPV4);
        priority.add(AnalyticConcept.VertexType.NETWORK_IDENTIFIER);
        priority.add(AnalyticConcept.VertexType.PERSON);
        priority.add(AnalyticConcept.VertexType.ORGANISATION);
        priority.add(AnalyticConcept.VertexType.DOCUMENT);
        priority.add(AnalyticConcept.VertexType.GEOHASH);
        priority.add(AnalyticConcept.VertexType.MGRS);
        priority.add(AnalyticConcept.VertexType.COUNTRY);
        priority.add(AnalyticConcept.VertexType.LOCATION);

        return priority;
    }

    @Override
    public SchemaVertexType convertType(final GraphVertex type) {
        return type.getTypeValue();
    }
}
