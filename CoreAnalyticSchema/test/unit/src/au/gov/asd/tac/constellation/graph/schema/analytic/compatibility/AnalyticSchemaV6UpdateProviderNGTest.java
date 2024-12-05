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
package au.gov.asd.tac.constellation.graph.schema.analytic.compatibility;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class AnalyticSchemaV6UpdateProviderNGTest {
    
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
     * Test of schemaUpdate method, of class AnalyticSchemaV6UpdateProvider.
     */
    @Test
    public void testSchemaUpdate() {
        System.out.println("schemaUpdate");
        
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        final StoreGraph graph = new StoreGraph(schema);
        
        final int typeAttributeId = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        
        final int vxId1 = graph.addVertex();
        final int vxId2 = graph.addVertex();
        final int vxId3 = graph.addVertex();
        final int vxId4 = graph.addVertex();
        final int vxId5 = graph.addVertex();
        final int vxId6 = graph.addVertex();
        
        final SchemaVertexType ipv4Custom = new SchemaVertexType.Builder("IPv4 Address")
                .build();
        final SchemaVertexType ipv6Custom = new SchemaVertexType.Builder("IPv6 Address")
                .build();
        final SchemaVertexType emailCustom = new SchemaVertexType.Builder("Email")
                .build();
        final SchemaVertexType mgrsCustom = new SchemaVertexType.Builder("MGRS")
                .build();
        final SchemaVertexType urlCustom = new SchemaVertexType.Builder("URL")
                .build();
        
        graph.setObjectValue(typeAttributeId, vxId1, ipv4Custom);
        graph.setObjectValue(typeAttributeId, vxId2, ipv6Custom);
        graph.setObjectValue(typeAttributeId, vxId3, emailCustom);
        graph.setObjectValue(typeAttributeId, vxId4, mgrsCustom);
        graph.setObjectValue(typeAttributeId, vxId5, urlCustom);
        
        final SchemaVertexType vxId1TypeBefore = graph.getObjectValue(typeAttributeId, vxId1);
        final SchemaVertexType vxId2TypeBefore = graph.getObjectValue(typeAttributeId, vxId2);
        final SchemaVertexType vxId3TypeBefore = graph.getObjectValue(typeAttributeId, vxId3);
        final SchemaVertexType vxId4TypeBefore = graph.getObjectValue(typeAttributeId, vxId4);
        final SchemaVertexType vxId5TypeBefore = graph.getObjectValue(typeAttributeId, vxId5);
        final SchemaVertexType vxId6TypeBefore = graph.getObjectValue(typeAttributeId, vxId6);
        
        assertFalse(AnalyticConcept.VertexType.IPV4.equals(vxId1TypeBefore));
        assertFalse(AnalyticConcept.VertexType.IPV6.equals(vxId2TypeBefore));
        assertFalse(AnalyticConcept.VertexType.EMAIL_ADDRESS.equals(vxId3TypeBefore));
        assertFalse(AnalyticConcept.VertexType.MGRS.equals(vxId4TypeBefore));
        assertFalse(AnalyticConcept.VertexType.URL.equals(vxId5TypeBefore));
        assertNull(vxId6TypeBefore);
        
        final AnalyticSchemaV6UpdateProvider instance = new AnalyticSchemaV6UpdateProvider();
        instance.schemaUpdate(graph);
        
        final SchemaVertexType vxId1TypeAfter = graph.getObjectValue(typeAttributeId, vxId1);
        final SchemaVertexType vxId2TypeAfter = graph.getObjectValue(typeAttributeId, vxId2);
        final SchemaVertexType vxId3TypeAfter = graph.getObjectValue(typeAttributeId, vxId3);
        final SchemaVertexType vxId4TypeAfter = graph.getObjectValue(typeAttributeId, vxId4);
        final SchemaVertexType vxId5TypeAfter = graph.getObjectValue(typeAttributeId, vxId5);
        final SchemaVertexType vxId6TypeAfter = graph.getObjectValue(typeAttributeId, vxId6);
        
        assertTrue(AnalyticConcept.VertexType.IPV4.equals(vxId1TypeAfter));
        assertTrue(AnalyticConcept.VertexType.IPV6.equals(vxId2TypeAfter));
        assertTrue(AnalyticConcept.VertexType.EMAIL_ADDRESS.equals(vxId3TypeAfter));
        assertTrue(AnalyticConcept.VertexType.MGRS.equals(vxId4TypeAfter));
        // still false as this update doesn't cover this type
        assertFalse(AnalyticConcept.VertexType.URL.equals(vxId5TypeAfter));
        // still null
        assertNull(vxId6TypeAfter);
    }
}
