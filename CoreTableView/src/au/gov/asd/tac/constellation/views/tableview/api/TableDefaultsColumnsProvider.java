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
package au.gov.asd.tac.constellation.views.tableview.api;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Andromeda-224
 */
@ServiceProvider(service = TableDefaultColumns.class, position = Integer.MAX_VALUE)
public class TableDefaultsColumnsProvider implements TableDefaultColumns {

    @Override
    public List<GraphAttribute> getDefaultAttributes(final Graph graph) {
        final Set<GraphAttribute> keyAttributes = new HashSet<>();
        
        if (graph != null && graph.getSchema() != null) {
            try (final ReadableGraph readableGraph = graph.getReadableGraph()) {
                final int[] vertexKeys = readableGraph.getPrimaryKey(GraphElementType.VERTEX);
                for (final int vertexKey : vertexKeys) {
                    keyAttributes.add(new GraphAttribute(readableGraph, vertexKey));
                }
                final int[] transactionKeys = readableGraph.getPrimaryKey(GraphElementType.TRANSACTION);
                for (final int transactionKey : transactionKeys) {
                    keyAttributes.add(new GraphAttribute(readableGraph, transactionKey));
                }
            }
        }
        return keyAttributes.stream().toList();
    } 
}
