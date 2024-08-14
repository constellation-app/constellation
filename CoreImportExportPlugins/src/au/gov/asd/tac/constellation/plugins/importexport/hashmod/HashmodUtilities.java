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
package au.gov.asd.tac.constellation.plugins.importexport.hashmod;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import java.util.BitSet;

/**
 *
 * @author CrucisGamma
 */
public class HashmodUtilities {
    
    private HashmodUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Gather a Graph's vxIds into a BitSet for faster checking.
     *
     * @param wg The graph.
     *
     * @return A BitSet where vertex ids in the Graph are set.
     */
    public static BitSet vertexBits(final GraphWriteMethods wg) {
        final int vxCount = wg.getVertexCount();
        final BitSet bs = new BitSet(wg.getVertexCapacity());
        for (int position = 0; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);
            bs.set(vxId);
        }

        return bs;
    }

}
