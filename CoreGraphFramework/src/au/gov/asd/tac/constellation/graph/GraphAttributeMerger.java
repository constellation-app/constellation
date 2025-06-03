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
package au.gov.asd.tac.constellation.graph;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.Lookup;

/**
 *
 * @author sirius
 */
public abstract class GraphAttributeMerger {

    private static final GraphAttributeMerger DEFAULT_MERGER = Lookup.getDefault().lookup(GraphAttributeMerger.class);

    private static final Map<String, GraphAttributeMerger> MERGERS = new HashMap<>();
    private static final Map<String, GraphAttributeMerger> U_MERGERS = Collections.unmodifiableMap(MERGERS);

    public static final GraphAttributeMerger getDefault() {
        return DEFAULT_MERGER;
    }

    public static synchronized Map<String, GraphAttributeMerger> getMergers() {
        if (MERGERS.isEmpty()) {
            for (final GraphAttributeMerger merger : Lookup.getDefault().lookupAll(GraphAttributeMerger.class)) {
                MERGERS.put(merger.getId(), merger);
            }
        }
        return U_MERGERS;
    }

    public abstract String getId();

    public abstract boolean mergeAttribute(final GraphWriteMethods graph, final GraphElementType elementType,
            final int survivingElement, final int mergedElement, final int attribute);
}
