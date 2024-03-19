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
package au.gov.asd.tac.constellation.graph.locking;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.IntegerAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.LongAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.ObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.BareSchemaFactory;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * A suite of methods to test that the mod counts stay consistent between the
 * two graphs in a DualGraph after various sequences of operations have been
 * performed.
 *
 * The tests here should assert that the graph itself has the expected
 * structure, attributes and values. However the purpose of this particular
 * suite is not to comprehensively test possible sequences of graph operations.
 * Indeed the sequences of operations contained within each test are
 * deliberately simple, since the main focus is testing that each type of
 * operation will correctly adjust mod counts (although the mod count
 * adjustments themselves may depend on the state of the graph prior to the
 * operation running).
 *
 * The method of ensuring that the mod counts match is to retrieve the mod count
 * from a write lock immediately before a commit. We then compare this to the
 * mod count obtained from a new write lock immediately after the commit. This
 * second mod count represents the changes made by the commit method to the
 * original read graph so that it matches the write graph. Note that in order
 * for it to make sense that these two mod counts are equal, we need to assume
 * that the commit method itself doesn't adjust the mod count of the write
 * graph. Presently the only way this happens is due to merging on primary key
 * clashes. Hence all tests here should call validateKeys()explicitly prior to
 * committing if their is any chance that merging may occur.
 *
 * A better solution for comparing mod counts would be some form of equality
 * testing for graphs (at the implementation level rather than the data level,
 * ie. we care about the internals like the mappings from position to ID and the
 * mod counts). This suite could then utilise this on the two targets in a Dual
 * Graph after each commit. This would also provide for more comprehensive
 * testing than just looking at the mod counts.
 *
 * @author twilight_sparkle
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class ModCountNGTest extends ConstellationTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    /**
     * Tests that the mod counts on the two graphs in a DualGraph match after a
     * string value is set on a graph with a single vertex.
     */
    @Test
    public void setStringValueModCounts() {
        final DualGraph g = new DualGraph(null);
        try {
            long modCount;
            WritableGraph wg = g.getWritableGraph("", true);
            int attribute, vertex;
            final String value = "s", defaultValue = "";
            try {
                vertex = wg.addVertex();
                attribute = wg.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "name", "description", defaultValue, null);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(defaultValue, wg.getStringValue(attribute, vertex));
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                wg.setStringValue(attribute, vertex, value);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(value, wg.getStringValue(attribute, vertex));
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }
        } catch (InterruptedException ex) {
            assertTrue(ex.toString(), false);
        }
    }

    /**
     * Tests that the mod counts on the two graphs in a DualGraph match after a
     * char value is set on a graph with a single vertex.
     *
     * Note that this test currently uses an integer attribute as there is no
     * CharAttributeDescription class.
     */
    @Test
    public void setChareValueModCounts() {
        final DualGraph g = new DualGraph(null);
        try {
            long modCount;
            WritableGraph wg = g.getWritableGraph("", true);
            int attribute, vertex;
            final char value = 1, defaultValue = 0;
            try {
                vertex = wg.addVertex();
                attribute = wg.addAttribute(GraphElementType.VERTEX, IntegerAttributeDescription.ATTRIBUTE_NAME, "name", "description", defaultValue, null);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(defaultValue, wg.getCharValue(attribute, vertex));
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                wg.setCharValue(attribute, vertex, value);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(value, wg.getCharValue(attribute, vertex));
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }
        } catch (InterruptedException ex) {
            assertTrue(ex.toString(), false);
        }
    }

    /**
     * Tests that the mod counts on the two graphs in a DualGraph match after a
     * double value is set on a graph with a single vertex.
     *
     * Note that this test currently uses a float attribute as there is no
     * DoubleAttributeDescription class.
     */
    @Test
    public void setDoubleValueModCounts() {
        final DualGraph g = new DualGraph(null);
        try {
            long modCount;
            WritableGraph wg = g.getWritableGraph("", true);
            int attribute, vertex;
            final double value = 1, defaultValue = -1;
            try {
                vertex = wg.addVertex();
                attribute = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "name", "description", defaultValue, null);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(defaultValue, wg.getDoubleValue(attribute, vertex));
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                wg.setDoubleValue(attribute, vertex, value);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(value, wg.getDoubleValue(attribute, vertex));
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }
        } catch (InterruptedException ex) {
            assertTrue(ex.toString(), false);
        }
    }

    /**
     * Tests that the mod counts on the two graphs in a DualGraph match after a
     * long value is set on a graph with a single vertex.
     */
    @Test
    public void setLongValueModCounts() {
        final DualGraph g = new DualGraph(null);
        try {
            long modCount;
            WritableGraph wg = g.getWritableGraph("", true);
            int attribute, vertex;
            final long value = 1, defaultValue = 0;
            try {
                vertex = wg.addVertex();
                attribute = wg.addAttribute(GraphElementType.VERTEX, LongAttributeDescription.ATTRIBUTE_NAME, "name", "description", defaultValue, null);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(defaultValue, wg.getLongValue(attribute, vertex));
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                wg.setLongValue(attribute, vertex, value);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(value, wg.getLongValue(attribute, vertex));
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }
        } catch (InterruptedException ex) {
            assertTrue(ex.toString(), false);
        }
    }

    /**
     * Tests that the mod counts on the two graphs in a DualGraph match after a
     * short value is set on a graph with a single vertex.
     *
     * Note that this test currently uses an integer attribute as there is no
     * ShortAttributeDescription class.
     */
    @Test
    public void setShortValueModCounts() {
        final DualGraph g = new DualGraph(null);
        try {
            long modCount;
            WritableGraph wg = g.getWritableGraph("", true);
            int attribute, vertex;
            final short value = 1, defaultValue = 0;
            try {
                vertex = wg.addVertex();
                attribute = wg.addAttribute(GraphElementType.VERTEX, IntegerAttributeDescription.ATTRIBUTE_NAME, "name", "description", defaultValue, null);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(defaultValue, wg.getShortValue(attribute, vertex));
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                wg.setShortValue(attribute, vertex, value);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(value, wg.getShortValue(attribute, vertex));
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }
        } catch (InterruptedException ex) {
            assertTrue(ex.toString(), false);
        }
    }

    /**
     * Tests that the mod counts on the two graphs in a DualGraph match after a
     * float value is set on a graph with a single vertex.
     */
    @Test
    public void setFloatValueModCounts() {
        final DualGraph g = new DualGraph(null);
        try {
            long modCount;
            WritableGraph wg = g.getWritableGraph("", true);
            int attribute, vertex;
            final float value = 1f, defaultValue = 0f;
            try {
                vertex = wg.addVertex();
                attribute = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "name", "description", defaultValue, null);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(defaultValue, wg.getFloatValue(attribute, vertex));
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                wg.setFloatValue(attribute, vertex, value);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(value, wg.getFloatValue(attribute, vertex));
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }
        } catch (InterruptedException ex) {
            assertTrue(ex.toString(), false);
        }
    }

    /**
     * Tests that the mod counts on the two graphs in a DualGraph match after an
     * integer value is set on a graph with a single vertex.
     */
    @Test
    public void setIntegerValueModCounts() {
        final DualGraph g = new DualGraph(null);
        try {
            long modCount;
            WritableGraph wg = g.getWritableGraph("", true);
            int attribute, vertex;
            final int value = 1, defaultValue = 0;
            try {
                vertex = wg.addVertex();
                attribute = wg.addAttribute(GraphElementType.VERTEX, IntegerAttributeDescription.ATTRIBUTE_NAME, "name", "description", defaultValue, null);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(defaultValue, wg.getIntValue(attribute, vertex));
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                wg.setIntValue(attribute, vertex, value);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(value, wg.getIntValue(attribute, vertex));
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }
        } catch (InterruptedException ex) {
            assertTrue(ex.toString(), false);
        }
    }

    /**
     * Tests that the mod counts on the two graphs in a DualGraph match after an
     * object value is set on a graph with a single vertex.
     */
    @Test
    public void setObjectValueModCounts() {
        final DualGraph g = new DualGraph(null);
        try {
            long modCount;
            WritableGraph wg = g.getWritableGraph("", true);
            int attribute, vertex;
            final Object object = new Object();
            try {
                vertex = wg.addVertex();
                attribute = wg.addAttribute(GraphElementType.VERTEX, ObjectAttributeDescription.ATTRIBUTE_NAME, "name", "description", null, null);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(null, (Object) wg.getObjectValue(attribute, vertex));
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                wg.setObjectValue(attribute, vertex, object);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(object, wg.getObjectValue(attribute, vertex));
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }
        } catch (InterruptedException ex) {
            assertTrue(ex.toString(), false);
        }
    }

    /**
     * Tests that the mod counts on the two graphs in a DualGraph match after
     * two vertices are added and then one is removed.
     */
    @Test
    public void addDeleteVertexModCounts() {
        final DualGraph g = new DualGraph(null);
        try {
            long modCount;
            WritableGraph wg = g.getWritableGraph("", true);
            int vertex0, vertex1;
            try {
                vertex0 = wg.addVertex();
                vertex1 = wg.addVertex();
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(wg.getVertexCount(), 2);
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                wg.removeVertex(vertex0);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(wg.getVertexCount(), 1);
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }
        } catch (InterruptedException ex) {
            assertTrue(ex.toString(), false);
        }
    }

    /**
     * Tests that the mod counts on the two graphs in a DualGraph match after
     * two vertices and two transactions are added and then one of the vertices
     * is removed.
     */
    @Test
    public void addDeleteVertexWithTransactionModCounts() {
        final DualGraph g = new DualGraph(null);
        try {
            long modCount;
            WritableGraph wg = g.getWritableGraph("", true);
            int vertex0, vertex1;
            int trans0, trans1;
            try {
                vertex0 = wg.addVertex();
                vertex1 = wg.addVertex();
                trans0 = wg.addTransaction(vertex0, vertex1, true);
                trans1 = wg.addTransaction(vertex0, vertex1, true);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(wg.getVertexCount(), 2);
                assertEquals(wg.getTransactionCount(), 2);
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                wg.removeVertex(vertex0);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(wg.getVertexCount(), 1);
                assertEquals(wg.getTransactionCount(), 0);
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }
        } catch (InterruptedException ex) {
            assertTrue(ex.toString(), false);
        }
    }

    /**
     * Tests that the mod counts on the two graphs in a DualGraph match after
     * two transactions are added and then one is removed between a pair of
     * vertices.
     */
    @Test
    public void addDeleteTransactionModCounts() {
        final DualGraph g = new DualGraph(null);
        try {
            long modCount;
            WritableGraph wg = g.getWritableGraph("", true);
            int vertex0, vertex1;
            int trans0, trans1;
            try {
                vertex0 = wg.addVertex();
                vertex1 = wg.addVertex();
                trans0 = wg.addTransaction(vertex0, vertex1, true);
                trans1 = wg.addTransaction(vertex0, vertex1, true);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(wg.getVertexCount(), 2);
                assertEquals(wg.getTransactionCount(), 2);
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                wg.removeTransaction(trans1);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(wg.getVertexCount(), 2);
                assertEquals(wg.getTransactionCount(), 1);
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }
        } catch (InterruptedException ex) {
            assertTrue(ex.toString(), false);
        }
    }

    /**
     * Tests that the mod counts on the two graphs in a DualGraph match after a
     * transactions is added between two vertices and then its source vertex is
     * set to a third vertex.
     */
    @Test
    public void setTransactionSourceModCounts() {
        final DualGraph g = new DualGraph(null);
        try {
            long modCount;
            WritableGraph wg = g.getWritableGraph("", true);
            int vertex0, vertex1, vertex2;
            int trans;
            try {
                vertex0 = wg.addVertex();
                vertex1 = wg.addVertex();
                vertex2 = wg.addVertex();
                trans = wg.addTransaction(vertex0, vertex1, true);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(wg.getVertexCount(), 3);
                assertEquals(wg.getTransactionCount(), 1);
                assertEquals(wg.getTransactionSourceVertex(trans), vertex0);
                assertEquals(wg.getTransactionDestinationVertex(trans), vertex1);
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                wg.setTransactionSourceVertex(trans, vertex2);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(wg.getVertexCount(), 3);
                assertEquals(wg.getTransactionCount(), 1);
                assertEquals(wg.getTransactionSourceVertex(trans), vertex2);
                assertEquals(wg.getTransactionDestinationVertex(trans), vertex1);
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }
        } catch (InterruptedException ex) {
            assertTrue(ex.toString(), false);
        }
    }

    /**
     * Tests that the mod counts on the two graphs in a DualGraph match after a
     * transactions is added between two vertices and then its destination
     * vertex is set to a third vertex.
     */
    @Test
    public void setTransactionDestinationModCounts() {
        final DualGraph g = new DualGraph(null);
        try {
            long modCount;
            WritableGraph wg = g.getWritableGraph("", true);
            int vertex0, vertex1, vertex2;
            int trans;
            try {
                vertex0 = wg.addVertex();
                vertex1 = wg.addVertex();
                vertex2 = wg.addVertex();
                trans = wg.addTransaction(vertex0, vertex1, true);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(wg.getVertexCount(), 3);
                assertEquals(wg.getTransactionCount(), 1);
                assertEquals(wg.getTransactionSourceVertex(trans), vertex0);
                assertEquals(wg.getTransactionDestinationVertex(trans), vertex1);
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                wg.setTransactionDestinationVertex(trans, vertex2);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(wg.getVertexCount(), 3);
                assertEquals(wg.getTransactionCount(), 1);
                assertEquals(wg.getTransactionSourceVertex(trans), vertex0);
                assertEquals(wg.getTransactionDestinationVertex(trans), vertex2);
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }
        } catch (InterruptedException ex) {
            assertTrue(ex.toString(), false);
        }
    }

    /**
     * Tests that the mod counts on the two graphs in a DualGraph match after a
     * transactions is added between two vertices and then its source vertex is
     * set to its destination vertex to create a loop.
     */
    @Test
    public void setTransactionSourceToDestinationModCounts() {
        final DualGraph g = new DualGraph(null);
        try {
            long modCount;
            WritableGraph wg = g.getWritableGraph("", true);
            int vertex0, vertex1;
            int trans;
            try {
                vertex0 = wg.addVertex();
                vertex1 = wg.addVertex();
                trans = wg.addTransaction(vertex0, vertex1, true);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(wg.getVertexCount(), 2);
                assertEquals(wg.getTransactionCount(), 1);
                assertEquals(wg.getTransactionSourceVertex(trans), vertex0);
                assertEquals(wg.getTransactionDestinationVertex(trans), vertex1);
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                wg.setTransactionSourceVertex(trans, vertex1);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(wg.getVertexCount(), 2);
                assertEquals(wg.getTransactionCount(), 1);
                assertEquals(wg.getTransactionSourceVertex(trans), vertex1);
                assertEquals(wg.getTransactionDestinationVertex(trans), vertex1);
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }
        } catch (InterruptedException ex) {
            assertTrue(ex.toString(), false);
        }
    }

    /**
     * Tests that the mod counts on the two graphs in a DualGraph match after a
     * transactions is added between two vertices and then its destination
     * vertex is set to its source vertex to create a loop.
     */
    @Test
    public void setTransactionDestinationToSourceModCounts() {
        final DualGraph g = new DualGraph(null);
        try {
            long modCount;
            WritableGraph wg = g.getWritableGraph("", true);
            int vertex0, vertex1;
            int trans;
            try {
                vertex0 = wg.addVertex();
                vertex1 = wg.addVertex();
                trans = wg.addTransaction(vertex0, vertex1, true);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(wg.getVertexCount(), 2);
                assertEquals(wg.getTransactionCount(), 1);
                assertEquals(wg.getTransactionSourceVertex(trans), vertex0);
                assertEquals(wg.getTransactionDestinationVertex(trans), vertex1);
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                wg.setTransactionDestinationVertex(trans, vertex0);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(wg.getVertexCount(), 2);
                assertEquals(wg.getTransactionCount(), 1);
                assertEquals(wg.getTransactionSourceVertex(trans), vertex0);
                assertEquals(wg.getTransactionDestinationVertex(trans), vertex0);
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }
        } catch (InterruptedException ex) {
            assertTrue(ex.toString(), false);
        }
    }

    /**
     * Tests that the mod counts on the two graphs in a DualGraph match after a
     * vertex attribute is added and then removed to a graph containing a single
     * vertex.
     */
    @Test
    public void addDeleteAttributeModCounts() {
        final DualGraph g = new DualGraph(null);
        try {
            long modCount;
            WritableGraph wg = g.getWritableGraph("", true);
            int attribute;
            try {
                wg.addVertex();
                attribute = wg.addAttribute(GraphElementType.VERTEX, IntegerAttributeDescription.ATTRIBUTE_NAME, "name", "description", null, null);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(1, wg.getVertexCount());
                assertEquals(1, wg.getAttributeCount(GraphElementType.VERTEX));
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                wg.removeAttribute(attribute);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(1, wg.getVertexCount());
                assertEquals(0, wg.getAttributeCount(GraphElementType.VERTEX));
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }
        } catch (InterruptedException ex) {
            assertTrue(ex.toString(), false);
        }
    }

    /**
     * Tests that the mod counts on the two graphs in a DualGraph match after a
     * vertex attribute is added and then its value is cleared for a single
     * vertex.
     */
    @Test
    public void clearAttributeValueModCounts() {
        final DualGraph g = new DualGraph(null);
        try {
            long modCount;
            WritableGraph wg = g.getWritableGraph("", true);
            int attribute;
            try {
                wg.addVertex();
                attribute = wg.addAttribute(GraphElementType.VERTEX, IntegerAttributeDescription.ATTRIBUTE_NAME, "name", "description", null, null);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(1, wg.getVertexCount());
                assertEquals(1, wg.getAttributeCount(GraphElementType.VERTEX));
                assertEquals(0, wg.getIntValue(attribute, 0));
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                wg.clearValue(attribute, 0);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(1, wg.getVertexCount());
                assertEquals(1, wg.getAttributeCount(GraphElementType.VERTEX));
                assertEquals(0, wg.getIntValue(attribute, 0));
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }
        } catch (InterruptedException ex) {
            assertTrue(ex.toString(), false);
        }
    }

    /**
     * Tests that the mod counts on the two graphs in a DualGraph match after a
     * vertex attribute is added, this is set as the primary key, and then the
     * primary key is removed. This is performed on a graph with a single vertex
     * so the primary key should not perform any merging.
     */
    @Test
    public void addDeletePrimaryKeyModCounts() {
        final DualGraph g = new DualGraph(null);
        try {
            long modCount;
            WritableGraph wg = g.getWritableGraph("", true);
            int attribute;
            try {
                wg.addVertex();
                attribute = wg.addAttribute(GraphElementType.VERTEX, IntegerAttributeDescription.ATTRIBUTE_NAME, "name", "description", null, null);
                wg.setPrimaryKey(GraphElementType.VERTEX, attribute);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(1, wg.getVertexCount());
                assertEquals(1, wg.getAttributeCount(GraphElementType.VERTEX));
                assertEquals(1, wg.getPrimaryKey(GraphElementType.VERTEX).length);
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                wg.setPrimaryKey(GraphElementType.VERTEX);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(1, wg.getVertexCount());
                assertEquals(1, wg.getAttributeCount(GraphElementType.VERTEX));
                assertEquals(0, wg.getPrimaryKey(GraphElementType.VERTEX).length);
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }
        } catch (InterruptedException ex) {
            assertTrue(ex.toString(), false);
        }
    }

    /**
     * Tests that the mod counts on the two graphs in a DualGraph match after a
     * vertex attribute is added, this is set as the primary key, and then the
     * primary key is removed. This is performed on a graph with a two vertices
     * so the setting of the primary key should cause merging to occur. As a
     * result, we need to crate a dual graph using the bare schema so that a
     * merger is available. We also need to explicitly performing the merging
     * ourselves; if we let the commit perform it then the mod count on the
     * locked target will be altered by the commit method itself.
     *
     * TODO: One should be able to set the GraphElementMerger on a
     * StoreGraph/GraphWriteMethods, as a graph should not have to have a schema
     * in order to perform any sort of merging.
     */
    @Test
    public void addMergeDeletePrimaryKeyModCounts() {
        final DualGraph g = new DualGraph(new BareSchemaFactory().createSchema());
        try {
            long modCount;
            WritableGraph wg = g.getWritableGraph("", true);
            int attribute;
            try {
                wg.addVertex();
                wg.addVertex();
                attribute = wg.addAttribute(GraphElementType.VERTEX, IntegerAttributeDescription.ATTRIBUTE_NAME, "name", "description", null, null);
                wg.setPrimaryKey(GraphElementType.VERTEX, attribute);
                wg.validateKey(GraphElementType.VERTEX, true);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(1, wg.getVertexCount());
                assertEquals(1, wg.getAttributeCount(GraphElementType.VERTEX));
                assertEquals(1, wg.getPrimaryKey(GraphElementType.VERTEX).length);
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                wg.setPrimaryKey(GraphElementType.VERTEX);
                modCount = wg.getGlobalModificationCounter();
            } finally {
                wg.commit();
            }

            wg = g.getWritableGraph("", true);
            try {
                assertEquals(1, wg.getVertexCount());
                assertEquals(1, wg.getAttributeCount(GraphElementType.VERTEX));
                assertEquals(0, wg.getPrimaryKey(GraphElementType.VERTEX).length);
                assertEquals(modCount, wg.getGlobalModificationCounter());
            } finally {
                wg.commit();
            }
        } catch (InterruptedException ex) {
            assertTrue(ex.toString(), false);
        }
    }
}
