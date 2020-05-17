/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.utilities.hashmod;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import java.util.BitSet;

/**
 *
 * @author CrucisGamma
 */
public class HashmodUtilities {

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
