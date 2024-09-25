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
package au.gov.asd.tac.constellation.views.tableview.api;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.TransactionTypeAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Andromeda-224
 */
@ServiceProvider(service = TableDefaultColumns.class, position = 100)
public class TableDefaultsColumnsProvider implements TableDefaultColumns {

    @Override
    public List<GraphAttribute> getDefaultColumns(Graph graph) {
        final List<GraphAttribute> attributes = new ArrayList<>();
        if (graph != null && graph.getSchema() != null) {
            try (final ReadableGraph readableGraph = graph.getReadableGraph()) {
                final int attributeCount = readableGraph.getAttributeCount(GraphElementType.TRANSACTION);
                for (int i = 0; i < attributeCount; i++) {
                    attributes.add(new GraphAttribute(readableGraph, readableGraph.getAttribute(GraphElementType.TRANSACTION, i)));
                }
            }
            try (final ReadableGraph readableGraph = graph.getReadableGraph()) {
                final int attributeCount = readableGraph.getAttributeCount(GraphElementType.VERTEX);
                for (int i = 0; i < attributeCount; i++) {
                    attributes.add(new GraphAttribute(readableGraph, readableGraph.getAttribute(GraphElementType.VERTEX, i)));
                }
            }
        }

        List<String> selectedNames = new ArrayList<>();
        // set default columns to identifier, type, transaction_type, and
        // transaction datetime
        selectedNames.add(VisualConcept.VertexAttribute.IDENTIFIER.getName());
        selectedNames.add(TransactionTypeAttributeDescription.ATTRIBUTE_NAME);
        selectedNames.add(AnalyticConcept.TransactionAttribute.TYPE.getName());
        selectedNames.add(TemporalConcept.VertexAttribute.DATETIME.getName());
        return attributes.stream()
                .filter(attribute -> selectedNames.contains(attribute.getName()))
                .toList();        
    } 
}
