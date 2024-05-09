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
package au.gov.asd.tac.constellation.views.qualitycontrol.rules;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
public class UnknownTypeRuleNGTest {

    public UnknownTypeRuleNGTest() {
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
     * Test of executeRule method, of class UnknownTypeRule.
     *
     * Return false when the vx's type is null
     */
    @Test
    public void testExecuteRule() {
        final StoreGraph graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
        final int vx0 = graph.addVertex();

        final int typeAttr = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        assertEquals(graph.getObjectValue(typeAttr, vx0), (Object) null);

        final UnknownTypeRule instance = new UnknownTypeRule();
        final boolean expResult = false;
        final boolean result = instance.executeRule(graph, vx0);
        assertEquals(result, expResult);
    }

    /**
     * Test of executeRule method, of class UnknownTypeRule.
     *
     * Return false when the vx's type is a known type
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testExecuteRuleWithType() throws InterruptedException {
        final StoreGraph graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema());
        final int typeAttr = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        final int vx0 = graph.addVertex();
        graph.setObjectValue(typeAttr, vx0, AnalyticConcept.VertexType.COUNTRY);

        final UnknownTypeRule instance = new UnknownTypeRule();
        final boolean expResult = false;
        final boolean result = instance.executeRule(graph, vx0);
        assertEquals(result, expResult);
    }

    /**
     * Test of executeRule method, of class UnknownTypeRule.
     *
     * Return true when the vx's type is of type 'Unknown'
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testExecuteRuleWithUnknownType() throws InterruptedException {
        final StoreGraph graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema());
        final int typeAttr = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        final int vx0 = graph.addVertex();
        graph.setObjectValue(typeAttr, vx0, SchemaVertexType.unknownType());

        final UnknownTypeRule instance = new UnknownTypeRule();
        final boolean expResult = true;
        final boolean result = instance.executeRule(graph, vx0);
        assertEquals(result, expResult);
    }

}
