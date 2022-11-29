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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;

/**
 * A BinSelector is the class that actually updates the selected attribute
 * values of elements on the graph. This may involve selecting a number of
 * elements to reflect the selection event, such as selecting transactions under
 * a link.
 *
 * @author sirius
 */
public class BinSelector {

    private GraphElementType elementType;
    private int attribute;

    public void setElementType(GraphReadMethods graph, GraphElementType elementType) {
        this.elementType = elementType;
        attribute = graph.getAttribute(getSelectionElementType(elementType), "selected");
    }

    public void select(GraphWriteMethods graph, int element, boolean selected) {
        if (attribute != Graph.NOT_FOUND) {
            if (elementType.equals(GraphElementType.VERTEX)) {
                graph.setBooleanValue(attribute, element, selected);
            } else {
                int transactionCount = elementType.getTransactionCount(graph, element);
                for (int i = 0; i < transactionCount; i++) {
                    int transaction = elementType.getTransaction(graph, element, i);
                    graph.setBooleanValue(attribute, transaction, selected);
                }
            }
        }
    }

    public boolean isSelected(GraphReadMethods graph, int element) {
        if (elementType.equals(GraphElementType.VERTEX)) {
            return graph.getBooleanValue(attribute, element);
        } else {
            int transactionCount = elementType.getTransactionCount(graph, element);
            for (int i = 0; i < transactionCount; i++) {
                int transaction = elementType.getTransaction(graph, element, i);
                if (graph.getBooleanValue(attribute, transaction)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static GraphElementType getSelectionElementType(GraphElementType elementType) {
        return elementType == GraphElementType.VERTEX ? GraphElementType.VERTEX : GraphElementType.TRANSACTION;
    }
}
