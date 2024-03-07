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
package au.gov.asd.tac.constellation.views.qualitycontrol.rules;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
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
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class IdentifierInconsistentWithTypeRuleNGTest extends ConstellationTest {

    public IdentifierInconsistentWithTypeRuleNGTest() {
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
     * Test of executeRule method, of class IdentifierInconsistentWithTypeRule.
     *
     * Return false when the vx's type is null
     */
    @Test
    public void testExecuteRuleNullType() {
        final StoreGraph graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
        final int vx0 = graph.addVertex();

        final String identifierName = "vx0";

        // Set identifer and verify correct setup
        final int identifierAttr = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        graph.setStringValue(identifierAttr, vx0, identifierName);
        assertEquals(graph.getObjectValue(identifierAttr, vx0), identifierName);

        // Ensure there is a type attribute on the graph and check its value
        final int typeAttr = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        assertEquals(graph.getObjectValue(typeAttr, vx0), (Object) null);

        // Running rule on vertex
        final IdentifierInconsistentWithTypeRule instance = new IdentifierInconsistentWithTypeRule();
        final boolean expResult = false;
        final boolean result = instance.executeRule(graph, vx0);
        assertEquals(result, expResult);
    }

    /**
     * Test of executeRule method, of class IdentifierInconsistentWithTypeRule.
     *
     * Return false when the vx's identifier is null
     */
    @Test
    public void testExecuteRuleNullIdentifier() {
        final StoreGraph graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
        final int vx0 = graph.addVertex();

        final String identifierName = null;

        // Set identifer and verify correct setup
        final int identifierAttr = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        graph.setStringValue(identifierAttr, vx0, identifierName);
        assertEquals(graph.getObjectValue(identifierAttr, vx0), identifierName);

        final SchemaVertexType type = AnalyticConcept.VertexType.COUNTRY;

        // Ensure there is a type attribute on the graph and check its value
        final int typeAttr = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        graph.setObjectValue(typeAttr, vx0, type);
        assertEquals(graph.getObjectValue(typeAttr, vx0), type);

        // Running rule on vertex
        final IdentifierInconsistentWithTypeRule instance = new IdentifierInconsistentWithTypeRule();
        final boolean expResult = false;
        final boolean result = instance.executeRule(graph, vx0);
        assertEquals(result, expResult);
    }

    /**
     * Test of executeRule method, of class IdentifierInconsistentWithTypeRule.
     *
     * Return false when the vx's type is missing
     */
    @Test
    public void testExecuteRuleMissingType() {
        final StoreGraph graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
        final int vx0 = graph.addVertex();

        // Running rule on vertex
        final IdentifierInconsistentWithTypeRule instance = new IdentifierInconsistentWithTypeRule();
        final boolean expResult = false;
        final boolean result = instance.executeRule(graph, vx0);
        assertEquals(result, expResult);
    }

    /**
     * Test of executeRule method, of class IdentifierInconsistentWithTypeRule.
     *
     * Return false when the vx's identifier is missing
     */
    @Test
    public void testExecuteRuleMissingIdentifier() {
        final StoreGraph graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
        final int vx0 = graph.addVertex();

        // Running rule on vertex
        final IdentifierInconsistentWithTypeRule instance = new IdentifierInconsistentWithTypeRule();
        final boolean expResult = false;
        final boolean result = instance.executeRule(graph, vx0);
        assertEquals(result, expResult);
    }

    /**
     * Test of executeRule method, of class IdentifierInconsistentWithTypeRule.
     *
     * Return false when the identifier and type matches
     */
    @Test
    public void testExecuteRuleValidIdentifierAndType() {
        final StoreGraph graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
        final int vx0 = graph.addVertex();

        final String identifierName = "AUS";

        // Set identifer and verify correct setup
        final int identifierAttr = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        graph.setStringValue(identifierAttr, vx0, identifierName);
        assertEquals(graph.getObjectValue(identifierAttr, vx0), identifierName);

        final SchemaVertexType type = AnalyticConcept.VertexType.COUNTRY;

        // Ensure there is a type attribute on the graph and check its value
        final int typeAttr = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        graph.setObjectValue(typeAttr, vx0, type);
        assertEquals(graph.getObjectValue(typeAttr, vx0), type);

        // Running rule on vertex
        final IdentifierInconsistentWithTypeRule instance = new IdentifierInconsistentWithTypeRule();
        final boolean expResult = false;
        final boolean result = instance.executeRule(graph, vx0);
        assertEquals(result, expResult);
    }

    /**
     * Test of executeRule method, of class IdentifierInconsistentWithTypeRule.
     *
     * Return true when the identifier and type doesn't match
     */
    @Test
    public void testExecuteRuleInValidIdentifierAndValidType() {
        final StoreGraph graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
        final int vx0 = graph.addVertex();

        final String identifierName = "A";

        // Set identifer and verify correct setup
        final int identifierAttr = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        graph.setStringValue(identifierAttr, vx0, identifierName);
        assertEquals(graph.getObjectValue(identifierAttr, vx0), identifierName);

        final SchemaVertexType type = AnalyticConcept.VertexType.COUNTRY;

        // Ensure there is a type attribute on the graph and check its value
        final int typeAttr = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        graph.setObjectValue(typeAttr, vx0, type);
        assertEquals(graph.getObjectValue(typeAttr, vx0), type);

        // Running rule on vertex
        final IdentifierInconsistentWithTypeRule instance = new IdentifierInconsistentWithTypeRule();
        final boolean expResult = true;
        final boolean result = instance.executeRule(graph, vx0);
        assertEquals(result, expResult);
    }

    /**
     * Test of executeRule method, of class IdentifierInconsistentWithTypeRule.
     *
     * Return false when the identifier is valid and type is Unknown
     */
    @Test
    public void testExecuteRuleInValidIdentifierAndUnknownType() {
        final StoreGraph graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
        final int vx0 = graph.addVertex();

        final String identifierName = "A";

        // Set identifer and verify correct setup
        final int identifierAttr = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        graph.setStringValue(identifierAttr, vx0, identifierName);
        assertEquals(graph.getObjectValue(identifierAttr, vx0), identifierName);

        final SchemaVertexType type = SchemaVertexType.unknownType();

        // Ensure there is a type attribute on the graph and check its value
        final int typeAttr = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        graph.setObjectValue(typeAttr, vx0, type);
        assertEquals(graph.getObjectValue(typeAttr, vx0), type);

        // Running rule on vertex
        final IdentifierInconsistentWithTypeRule instance = new IdentifierInconsistentWithTypeRule();
        final boolean expResult = false;
        final boolean result = instance.executeRule(graph, vx0);
        assertEquals(result, expResult);
    }
}
