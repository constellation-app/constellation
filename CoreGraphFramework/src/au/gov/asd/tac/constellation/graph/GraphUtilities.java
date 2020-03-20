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
package au.gov.asd.tac.constellation.graph;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A collection of utilities useful for manipulating graphs.
 *
 * @author sirius
 */
public class GraphUtilities {

    private static final Logger LOGGER = Logger.getLogger(GraphUtilities.class.getName());

    /**
     * Ensures that all modification counters on a pair of graphs are identical.
     * This is mainly used for debugging purposes.
     *
     * @param a the first graph.
     * @param b the second graph.
     */
    public static void compareModificationCounters(GraphReadMethods a, GraphReadMethods b) {
        LOGGER.info("COMPARE MODIFICATION COUNTERS");
        for (GraphElementType elementType : GraphElementType.values()) {
            int attributeCount = a.getAttributeCount(elementType);
            for (int p = 0; p < attributeCount; p++) {
                int attribute = a.getAttribute(elementType, p);
                if (a.getValueModificationCounter(attribute) != b.getValueModificationCounter(attribute)) {
                    LOGGER.log(Level.INFO, "\t{0} {1} {2} {3} {4} {5}", new Object[]{elementType, a.getAttributeType(attribute), attribute, a.getAttributeName(attribute), a.getValueModificationCounter(attribute), b.getValueModificationCounter(attribute)});
                }
            }
        }
    }
}
