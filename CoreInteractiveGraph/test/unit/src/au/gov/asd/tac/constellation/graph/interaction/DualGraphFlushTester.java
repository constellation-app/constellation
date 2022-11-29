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
package au.gov.asd.tac.constellation.graph.interaction;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeEvent;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeListener;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.awt.UndoRedo;

/**
 * Dual Graph Flush Test.
 *
 * @author twilight_sparkle
 */
public class DualGraphFlushTester {

    private static final Logger LOGGER = Logger.getLogger(DualGraphFlushTester.class.getName());

    public static void main(String[] args) throws InterruptedException {
        StoreGraph g = new StoreGraph();
        Graph dg = new DualGraph(g, true);
        dg.setUndoManager(new UndoRedo.Manager());

        WritableGraph wg = dg.getWritableGraph("sdfgsdfg", true);
        final int x = VisualConcept.VertexAttribute.X.ensure(wg);
        final int y = VisualConcept.VertexAttribute.Y.ensure(wg);
        final int z = VisualConcept.VertexAttribute.Z.ensure(wg);
        final int id = VisualConcept.VertexAttribute.IDENTIFIER.ensure(wg);
        for (int i = 0; i < 1000; i++) {
            final int vxId = wg.addVertex();
            wg.setFloatValue(x, vxId, i);
            wg.setFloatValue(y, vxId, i);
            wg.setFloatValue(z, vxId, i);
            wg.setStringValue(id, vxId, String.valueOf(i));
        }
        wg.commit();

        final float[] coordSums = new float[1000];

        GraphChangeListener gcl = (GraphChangeEvent event) -> {
            ReadableGraph rg = dg.getReadableGraph();
            try {
                for (int i = 0; i < 1000; i++) {
                    final int vxId = rg.getVertex(i);
                    coordSums[i] = rg.getFloatValue(x, vxId) + rg.getFloatValue(y, vxId) + rg.getFloatValue(z, vxId);
                }
            } finally {
                rg.release();
            }
        };
        dg.addGraphChangeListener(gcl);

        long time = System.currentTimeMillis();

        wg = dg.getWritableGraph("test", false);
        for (int i = 0; i < 100000; i++) {
            for (int j = 0; j < 1000; j++) {
                final int vxId = wg.getVertex(j);
                wg.setFloatValue(x, vxId, wg.getFloatValue(x, vxId) + 1);
                wg.setFloatValue(y, vxId, wg.getFloatValue(y, vxId) + 2);
                wg.setFloatValue(z, vxId, wg.getFloatValue(z, vxId) + 3);
            }
            wg = wg.flush(false);
        }
        wg.commit();

        LOGGER.log(Level.INFO, "took: {0}", (System.currentTimeMillis() - time));
        Thread.sleep(3000);
        LOGGER.log(Level.INFO, "{0}", Arrays.toString(coordSums));
    }
}
