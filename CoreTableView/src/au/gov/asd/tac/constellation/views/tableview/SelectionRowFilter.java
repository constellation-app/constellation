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
package au.gov.asd.tac.constellation.views.tableview;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.BitSet;
import javax.swing.RowFilter;

/**
 * A RowFilter that filters out non-selected rows.
 *
 * @author algol
 */
class SelectionRowFilter extends RowFilter<GraphTableModel, Integer> {

    private final BitSet selected;

    SelectionRowFilter(final Graph graph, final GraphElementType et) {
        selected = new BitSet();
        final ReadableGraph rg = graph.getReadableGraph();
        try {
            final boolean isVertex = et == GraphElementType.VERTEX;
            final int count = isVertex ? rg.getVertexCount() : rg.getTransactionCount();
            final int selectedId = rg.getAttribute(et, (isVertex ? VisualConcept.VertexAttribute.SELECTED : VisualConcept.TransactionAttribute.SELECTED).getName());
            for (int position = 0; position < count; position++) {
                final int id = isVertex ? rg.getVertex(position) : rg.getTransaction(position);

                final boolean isSelected = rg.getBooleanValue(selectedId, id);
                if (isSelected) {
                    selected.set(position);
                }
            }
        } finally {
            rg.release();
        }
    }

    @Override
    public boolean include(Entry<? extends GraphTableModel, ? extends Integer> entry) {
        final int position = entry.getIdentifier();

        return selected.get(position);
    }
}
