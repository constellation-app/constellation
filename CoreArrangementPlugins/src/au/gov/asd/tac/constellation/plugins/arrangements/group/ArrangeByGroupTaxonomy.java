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
package au.gov.asd.tac.constellation.plugins.arrangements.group;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.arrangements.AbstractInclusionGraph.Connections;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import au.gov.asd.tac.constellation.plugins.arrangements.GraphTaxonomy;
import au.gov.asd.tac.constellation.plugins.arrangements.GraphTaxonomyArranger;
import au.gov.asd.tac.constellation.plugins.arrangements.subgraph.InducedSubgraph;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A taxonomy that groups by a specified attribute.
 * <br>
 * The grouping is done by the attribute's getStringValue() value. For more
 * interesting grouping, use the histogram to create a binned attribute and use
 * that.
 *
 * @author algol
 */
public class ArrangeByGroupTaxonomy extends GraphTaxonomyArranger {

    private final String attrLabel;

    public ArrangeByGroupTaxonomy(final Arranger inner, final Arranger outer, final Connections connectionType, final String attrLabel) {
        super(inner, outer, connectionType, InducedSubgraph.getSubgraphFactory());
        this.attrLabel = attrLabel;
    }

    @Override
    protected GraphTaxonomy getTaxonomy(final GraphWriteMethods wg) {
        final int attrId = wg.getAttribute(GraphElementType.VERTEX, attrLabel);

        // Discover the unique attribute values.
        final HashMap<String, Set<Integer>> values = new HashMap<>();

        final int vxCount = wg.getVertexCount();
        for (int position = 0; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);

            final String value = wg.getStringValue(attrId, vxId);
            if (values.containsKey(value)) {
                values.get(value).add(vxId);
            } else {
                final Set<Integer> vxIds = new HashSet<>();
                vxIds.add(vxId);
                values.put(value, vxIds);
            }
        }

        // Create the taxonomy.
        // Use any vxId from each group as the taxonomy key, the actual key isn't important.
        final Map<Integer, Set<Integer>> tax = new HashMap<>();
        for (final Set<Integer> vxIds : values.values()) {
            tax.put(vxIds.iterator().next(), vxIds);
        }

        return new GraphTaxonomy(wg, tax);
    }
}
