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
package au.gov.asd.tac.constellation.graph.locking;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.IntegerAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import org.openide.awt.UndoRedo;
import org.openide.util.Exceptions;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Update Test.
 *
 * @author sirius
 */
public class UpdateNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @Test
    public void TestAttributeUpdating() {
        try {
            int a, v;

            DualGraph g = new DualGraph(null);
            WritableGraph wg = g.getWritableGraph("", true);
            try {
                a = wg.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "label", "description", null, null);
                v = wg.addVertex();
                wg.setStringValue(a, v, "Hello");
            } finally {
                wg.commit();
            }

            long mc;

            ReadableGraph rg = g.getReadableGraph();
            try {
                assertEquals(1, rg.getVertexCount());
                assertEquals("Hello", rg.getStringValue(a, v));
                mc = rg.getGlobalModificationCounter();
            } finally {
                rg.release();
            }

            wg = g.getWritableGraph("", true);
            try {
            } finally {
                wg.commit();
            }

            rg = g.getReadableGraph();
            try {
                assertEquals(1, rg.getVertexCount());
                assertEquals("Hello", rg.getStringValue(a, v));
                assertEquals(mc, rg.getGlobalModificationCounter());
            } finally {
                rg.release();
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Test
    public void rollingBack() {
        try {
            final DualGraph g = new DualGraph(null);
            final WritableGraph wg = g.getWritableGraph("", true);
            final int af;
            final int ac;
            final int v;
            try {
                af = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x", "x", 0, null);
                ac = wg.addAttribute(GraphElementType.VERTEX, IntegerAttributeDescription.ATTRIBUTE_NAME, "level", "description", null, null);
                v = wg.addVertex();
                wg.setFloatValue(af, v, 2.71828f);
                wg.setStringValue(ac, v, "3");
            } finally {
                wg.commit();
            }

            final WritableGraph wgr = g.getWritableGraph("", true);
            try {
                final int vr = wgr.addVertex();
                wgr.setFloatValue(af, vr, 99);
                wgr.setStringValue(ac, vr, "5");
//                throw new Exception();
            } finally {
                wgr.rollBack();
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Test
    public void commitThenRollback() {
        try {
            final DualGraph g = new DualGraph(null);
            final WritableGraph wg = g.getWritableGraph("", true);
            final int af;
            final int ac;
            final int v;
            try {
                af = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x", "x", 0, null);
                ac = wg.addAttribute(GraphElementType.VERTEX, IntegerAttributeDescription.ATTRIBUTE_NAME, "level", "description", null, null);
                v = wg.addVertex();
                wg.setFloatValue(af, v, 2.71828f);
                wg.setStringValue(ac, v, "3");
            } finally {
                wg.commit();
                try {
                    wg.rollBack();
                } catch (IllegalMonitorStateException ex) {
                }
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Test
    public void multiRelease() {
        final DualGraph g = new DualGraph(null);
        final ReadableGraph wg = g.getReadableGraph();
        wg.release();
        try {
            wg.release();
        } catch (IllegalMonitorStateException ex) {
            System.out.printf("%s\n", ex);
        }
    }

    @Test
    public void writeLockAndReadLock() throws InterruptedException {
        final DualGraph g = new DualGraph(null);

        final WritableGraph wg = g.getWritableGraph("Write", true);
        try {

            wg.addVertex();

            ReadableGraph rg = g.getReadableGraph();
            try {
                assert (rg.getVertexCount() == 1);
            } finally {
                rg.release();
            }

        } finally {
            wg.commit();
        }

    }

    @Test
    public void multiRollback() {
        try {
            final DualGraph g = new DualGraph(null);
            final WritableGraph wg = g.getWritableGraph("", true);
            wg.rollBack();
            try {
                wg.rollBack();
            } catch (IllegalMonitorStateException ex) {
                System.out.printf("%s\n", ex);
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

//    /**
//     * This test is strange: sometimes it fails (and sometimes with a
//     * CannotUndoException, sometimes with a NullPointerException), sometimes it
//     * doesn't.
//     */
//    @Test
//    public void rollbackNaN() {
//        final float defaultValue = 3;
//        try {
//            final DualGraph g = new DualGraph(null);
//            g.setUndoManager(new UndoRedo.Manager());
//            final WritableGraph wg = g.getWritableGraph("add stuff", true);
//            int attrId, vxId;
//            try {
//                attrId = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x", "x", defaultValue, null);
//                vxId = wg.addVertex();
//            } finally {
//                wg.commit();
//            }
//
//            final WritableGraph wg2 = g.getWritableGraph("set NaN", true);
//            try {
//                wg2.setFloatValue(attrId, vxId, Float.NaN);
//            } finally {
//                wg2.commit();
//            }
//
//            // Update the graph.
//            final ReadableGraph rg = g.getReadableGraph();
//            float v;
//            try {
//                v = rg.getFloatValue(attrId, vxId);
//            } finally {
//                rg.release();
//            }
//            assertTrue("should be Nan", Float.isNaN(v));
//
//            // Undo the update: should go back to the previous value.
//            try {
//                SwingUtilities.invokeAndWait(() -> {
//                    g.undo();
//                });
//            } catch (InterruptedException | InvocationTargetException ex) {
//            }
//
//            final ReadableGraph rg2 = g.getReadableGraph();
//            float v2;
//            try {
//                v2 = rg2.getFloatValue(attrId, vxId);
//            } finally {
//                rg2.release();
//            }
//            assertEquals(defaultValue, v2);
//        } catch (InterruptedException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//    }
    @Test
    public void createTransactionToNonexistentDestination() {
        final DualGraph g = new DualGraph(null);
        g.setUndoManager(new UndoRedo.Manager());

        WritableGraph wg = null;
        try {
            wg = g.getWritableGraph("Tx test", true);
            for (int i = 0; i < 100; i++) {
                final String s = String.format("x%d", i);
                wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, s, s, 0, null);
            }
            int vx = 0;
            for (int i = 0; i < 100; i++) {
                vx = wg.addVertex();
            }
            final int tx = wg.addTransaction(vx, vx + 1, true);

            Assert.fail("Shouldn't get here, wg.addTransaction() should fail.");
        } catch (IllegalArgumentException ex) {
            // Expected result.
            System.out.printf("Expected exception: %s\n", ex.getMessage());
//            ex.printStackTrace(System.out);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }

        if (wg != null) {
            wg.rollBack();
        }
    }

    // TODO: need to revisit this test
//    @Test
//    public void commitReadableGraph() {
//        final float f0 = 0;
//        final float f1 = 1;
//
//        final DualGraph g = new DualGraph(null);
//        g.setUndoManager(new UndoRedo.Manager());
//        WritableGraph wg;
//        int attrId;
//        int vx0;
//        int vx1;
//        try {
//            wg = g.getWritableGraph("Add attributes", true);
//            attrId = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x", "x", 0, null);
//            vx0 = wg.addVertex();
//            vx1 = wg.addVertex();
//            wg.setFloatValue(attrId, vx0, f0);
//            wg.setFloatValue(attrId, vx1, f1);
//            System.out.printf("%f %f\n", wg.getFloatValue(attrId, vx0), wg.getFloatValue(attrId, vx1));
//            wg.commit();
//
//            // This is naughty.
//            // In theory, nothing should happen because it's not writable.
//            // Maybe an exception should be thrown?
//            ReadableGraph rg = g.getReadableGraph();
//            wg = (WritableGraph) rg;
//            wg.setFloatValue(attrId, vx0, f0 + 10);
//            wg.setFloatValue(attrId, vx1, f1 + 10);
//            System.out.printf("%f %f\n", wg.getFloatValue(attrId, vx0), wg.getFloatValue(attrId, vx1));
//            rg.release();
//
//            // Since what we did was on a read-only graph, the values should not have changed.
//            wg = g.getWritableGraph("Look at values", true);
//            System.out.printf("%f %f\n", wg.getFloatValue(attrId, vx0), wg.getFloatValue(attrId, vx1));
//            assertEquals(f0, wg.getFloatValue(attrId, vx0));
//            assertEquals(f1, wg.getFloatValue(attrId, vx1));
//            wg.commit(); // TODO: if i comment this then the commit does not write the values 10 and 11 and the test passes.
//
//            // We've done nothing to the values, so the values should still not have changed.
//            wg = g.getWritableGraph("Look at values", true);
//            System.out.printf("%f %f\n", wg.getFloatValue(attrId, vx0), wg.getFloatValue(attrId, vx1));
//            assertEquals(f0, wg.getFloatValue(attrId, vx0));
//            assertEquals(f1, wg.getFloatValue(attrId, vx1));
//            wg.commit();
//        } catch (InterruptedException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//    }
}
