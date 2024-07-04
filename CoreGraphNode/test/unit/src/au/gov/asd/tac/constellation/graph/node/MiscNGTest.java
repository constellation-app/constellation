package au.gov.asd.tac.constellation.graph.node;

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
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.ObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import org.openide.awt.UndoRedo;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author algol
 */
public class MiscNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void testCreateTransactionToNonexistentDestinationInPluginTest() {
        try {
            final DualGraph graph = new DualGraph(null);
            graph.setUndoManager(new UndoRedo.Manager());

            PluginExecution.withPlugin(new SimpleEditPlugin() {
                @Override
                public void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
                    for (int i = 0; i < 10; i++) {
                        final String s = String.format("x%d", i);
                        wg.addAttribute(GraphElementType.VERTEX, ObjectAttributeDescription.ATTRIBUTE_NAME, s, s, 99, null);
                        wg.addAttribute(GraphElementType.TRANSACTION, FloatAttributeDescription.ATTRIBUTE_NAME, s, s, 99, null);
                    }

                    int vx = 0;
                    for (int i = 0; i < 100; i++) {
                        vx = wg.addVertex();
                    }
                    final int tx = wg.addTransaction(vx, vx + 1, true);

                    Assert.fail("Shouldn't get here, wg.addTransaction() should fail.");
                    System.out.printf("New transaction: %d\n", tx);
                }

                @Override
                public String getName() {
                    return "Build graph + tx failure test";
                }
            }).executeNow(graph);
        } catch (InterruptedException ex) {
            Assert.fail("Nothing was interrupted.");
        } catch (PluginException ex) {
            Assert.fail("There shouldn't be a plugin exception.");
        } catch (RuntimeException ex) {
            final boolean containsIllegalArgumentException = ex.getLocalizedMessage().contains("Attempt to create transaction to destination vertex that does not exist");
            Assert.assertTrue(containsIllegalArgumentException);
        }
    }
}
