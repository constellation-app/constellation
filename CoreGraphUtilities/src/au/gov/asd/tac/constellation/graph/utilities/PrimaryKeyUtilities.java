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
package au.gov.asd.tac.constellation.graph.utilities;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import java.util.HashSet;
import java.util.Set;

/**
 * Graph Primary Key Utilities
 *
 * @author arcturus
 */
public class PrimaryKeyUtilities {
    
    private PrimaryKeyUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Return a Set of primary key names defined by the graph
     *
     * @param graph The graph
     * @param type The element type to retrieve the primary keys
     * @return A Set of primary key names for the given {@link GraphElementType}
     */
    public static Set<String> getPrimaryKeyNames(final GraphReadMethods graph, final GraphElementType type) {
        final int[] primaryKeyIds = graph.getPrimaryKey(type);
        final Set<String> primaryKeys = new HashSet<>(primaryKeyIds.length);
        for (int i = 0; i < primaryKeyIds.length; i++) {
            primaryKeys.add(graph.getAttributeName(primaryKeyIds[i]));
        }
        return primaryKeys;
    }
}
