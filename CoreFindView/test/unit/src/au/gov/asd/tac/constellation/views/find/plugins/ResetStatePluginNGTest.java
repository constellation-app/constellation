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
package au.gov.asd.tac.constellation.views.find.plugins;

import au.gov.asd.tac.constellation.views.find.plugins.ResetStatePlugin;
import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.views.find.state.FindViewConcept;
import au.gov.asd.tac.constellation.views.find.utilities.BasicFindReplaceParameters;
import au.gov.asd.tac.constellation.views.find.utilities.FindResultsList;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Atlas139mkm
 */
public class ResetStatePluginNGTest {

    private Graph graph;
    private static final Logger LOGGER = Logger.getLogger(ResetStatePluginNGTest.class.getName());

    public ResetStatePluginNGTest() {
    }

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

    /**
     * Test of edit method, of class ResetStatePlugin.
     */
    @Test
    public void testEdit() throws Exception {
        System.out.println("edit");
        setupGraph();
        ResetStatePlugin resetStatePlugin = new ResetStatePlugin();
        PluginExecution.withPlugin(resetStatePlugin).executeNow(graph);
        ReadableGraph rg = graph.getReadableGraph();

        final int stateId = FindViewConcept.MetaAttribute.FINDVIEW_STATE.get(rg);
        FindResultsList graphFindResults = rg.getObjectValue(stateId, 0);
        rg.close();

        /**
         * The FINDVIEW_STATE object was set to an empty FindResults list with a
         * given current index of 2. The resetStatePlugin should reset its
         * current index to -1.
         */
        assertEquals(graphFindResults.getCurrentIndex(), -1);

    }

    /**
     * Test of getName method, of class ResetStatePlugin.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");

        ResetStatePlugin instance = new ResetStatePlugin();
        String expResult = "Find: Update State";
        String result = instance.getName();
        assertEquals(result, expResult);
    }

    private void setupGraph() {
        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());

        try {
            WritableGraph wg = graph.getWritableGraph("", true);
            final int stateId = FindViewConcept.MetaAttribute.FINDVIEW_STATE.ensure(wg);

            ArrayList<Attribute> attributeList = new ArrayList<>();
            BasicFindReplaceParameters parameters = new BasicFindReplaceParameters("label name", "", GraphElementType.GRAPH.VERTEX, attributeList, true, false, false, false, true, false, false, false, false, true, false);
            FindResultsList foundResult = new FindResultsList(2, parameters);

            wg.setObjectValue(stateId, 0, foundResult);

            wg.commit();

        } catch (final InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            Thread.currentThread().interrupt();
        }
    }
}
