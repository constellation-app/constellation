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
package au.gov.asd.tac.constellation.views.qualitycontrol.rules;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Missing Type Rule Test.
 *
 * @author arcturus
 */
public class MissingTypeRuleNGTest {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of executeRule method, of class MissingTypeRule.
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testExecuteRule() throws InterruptedException {
        final StoreGraph graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
        final int vx0 = graph.addVertex();

        final MissingTypeRule instance = new MissingTypeRule();
        final boolean expResult = true;
        final boolean result = instance.executeRule(graph, vx0);
        assertEquals(result, expResult);
    }

    /**
     * Test of executeRule method, of class MissingTypeRule.
     *
     * Test when null is returned from type attribute
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testExecuteRuleNull() throws InterruptedException {
        final StoreGraph graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
        final int vx0 = graph.addVertex();

        final int typeAttr = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        assertEquals(graph.getObjectValue(typeAttr, vx0), (Object) null);

        final MissingTypeRule instance = new MissingTypeRule();
        final boolean expResult = true;
        final boolean result = instance.executeRule(graph, vx0);
        assertEquals(result, expResult);
    }

    /**
     * Test of executeRule method, of class MissingTypeRule.
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testExecuteRuleWithType() throws InterruptedException {
        final StoreGraph graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema());
        final int typeAttr = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        final int vx0 = graph.addVertex();
        graph.setObjectValue(typeAttr, vx0, AnalyticConcept.VertexType.COUNTRY);

        final MissingTypeRule instance = new MissingTypeRule();
        final boolean expResult = false;
        final boolean result = instance.executeRule(graph, vx0);
        assertEquals(result, expResult);
    }
}
