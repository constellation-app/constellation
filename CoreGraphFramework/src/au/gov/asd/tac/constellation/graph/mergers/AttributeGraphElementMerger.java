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
package au.gov.asd.tac.constellation.graph.mergers;

import au.gov.asd.tac.constellation.graph.GraphAttributeMerger;
import au.gov.asd.tac.constellation.graph.GraphElementMerger;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import org.openide.util.lookup.ServiceProvider;

/**
 * A GraphElementMerger that uses the {@link GraphAttributeMerger} associated
 * with each attribute to merger the attribute values. If no attribute merger is
 * specified then the attribute values of the merged element take priority over
 * the attribute values of the surviving element.
 *
 * @author sirius
 */
@ServiceProvider(service = GraphElementMerger.class)
public class AttributeGraphElementMerger implements GraphElementMerger {

    @Override
    public boolean mergeElement(final GraphWriteMethods graph, final GraphElementType elementType, final int survivingElement, final int mergedElement) {
        final int elementAttributeCount = graph.getAttributeCount(elementType);
        for (int elementAttributePosition = 0; elementAttributePosition < elementAttributeCount; elementAttributePosition++) {
            final int elementAttribute = graph.getAttribute(elementType, elementAttributePosition);
            final GraphAttributeMerger attributeMerger = graph.getAttributeMerger(elementAttribute);
            if (attributeMerger != null) {
                if (!attributeMerger.mergeAttribute(graph, elementType, survivingElement, mergedElement, elementAttribute)) {
                    return false;
                }
            } else if (!graph.isDefaultValue(elementAttribute, mergedElement)) {
                graph.getNativeAttributeType(elementAttribute).copyAttributeValue(graph, elementAttribute, mergedElement, survivingElement);
            }
        }

        if (elementType == GraphElementType.VERTEX) {
            final int transactionCount = graph.getVertexTransactionCount(mergedElement);
            for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                final int mergedTransaction = graph.getVertexTransaction(mergedElement, 0);

                final int sourceVertex = graph.getTransactionSourceVertex(mergedTransaction);
                if (sourceVertex == mergedElement) {
                    graph.setTransactionSourceVertex(mergedTransaction, survivingElement);
                }

                final int destinationVertex = graph.getTransactionDestinationVertex(mergedTransaction);
                if (destinationVertex == mergedElement) {
                    graph.setTransactionDestinationVertex(mergedTransaction, survivingElement);
                }
            }
        }

        elementType.removeElement(graph, mergedElement);

        return true;
    }
}
