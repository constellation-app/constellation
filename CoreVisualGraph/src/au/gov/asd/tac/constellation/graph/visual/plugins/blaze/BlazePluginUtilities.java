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
package au.gov.asd.tac.constellation.graph.visual.plugins.blaze;

import static au.gov.asd.tac.constellation.graph.schema.visual.utilities.BlazeUtilities.VERTEX_IDS_PARAMETER_ID;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.util.BitSet;
import java.util.List;

/**
 *
 * @author algol
 */
public class BlazePluginUtilities {
    
    private BlazePluginUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Convert a String or Integer to an int.
     * <p>
     * The vertex ids should be strings, but we need to accept integers for
     * backwards compatibility.
     *
     * @param o A String or Integer.
     *
     * @return The int corresponding to the input parameter.
     */
    private static int toInt(final Object o) {
        return o instanceof Integer integer ? integer: Integer.parseInt((String) o);
    }

    /**
     * A helper function to get vertex ids from both a BitSet and a
     * List<String>.
     * <p>
     * The VERTEX_IDS_PARAMETER_ID parameter was originally BitSet, but we want
     * to allow List<String> (and List<Integer> for backward compatibility), so
     * handle both.
     *
     * @param parameters Plugin parameters.
     *
     * @return A BitSet containing vertex ids, or null if the parameter wasn't
     * specified.
     */
    static BitSet verticesParam(final PluginParameters parameters) {
        // The VERTEX_IDS_PARAMETER_ID parameter was originally BitSet, but we want to allow List<String>, so handle both.
        final Object vParam = parameters.getObjectValue(VERTEX_IDS_PARAMETER_ID);
        final BitSet vertices;
        if (vParam == null) {
            vertices = null;
        } else if (vParam.getClass() == BitSet.class) {
            vertices = (BitSet) vParam;
        } else {
            @SuppressWarnings("unchecked") //vParam will be list of objects which extends from object type
            final List<Object> vertexList = (List<Object>) vParam;
            vertices = new BitSet(vertexList.size());
            vertexList.stream().forEach(ix -> vertices.set(toInt(ix)));
        }

        return vertices;
    }
}
