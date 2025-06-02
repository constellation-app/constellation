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
package au.gov.asd.tac.constellation.graph.schema.analytic.compatibility;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class AnalyticSchemaV4UpdateProviderNGTest {
    
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
     * Test of schemaUpdate method, of class AnalyticSchemaV4UpdateProvider.
     */
    @Test
    public void testSchemaUpdate() {
        System.out.println("schemaUpdate");
        
        // These don't look exactly like what they would've looked like when the actual update occurred but will be sufficient for ensuring the updater does its job as expected
        final SchemaVertexType md5Type = new SchemaVertexType(AnalyticConcept.VertexType.MD5.getName(), null, null, null, null, null, null, null, null, null, false);
        final SchemaVertexType sha1Type = new SchemaVertexType(AnalyticConcept.VertexType.SHA1.getName(), null, null, null, null, null, null, null, null, null, false);
        final SchemaVertexType sha256Type = new SchemaVertexType(AnalyticConcept.VertexType.SHA256.getName(), null, null, null, null, null, null, null, null, null, false);
        final SchemaVertexType countryType = new SchemaVertexType(AnalyticConcept.VertexType.COUNTRY.getName(), null, null, null, null, null, null, null, null, null, false);
        final SchemaVertexType geohashType = new SchemaVertexType(AnalyticConcept.VertexType.GEOHASH.getName(), null, null, null, null, null, null, null, null, null, false);
        final SchemaVertexType mgrsType = new SchemaVertexType(AnalyticConcept.VertexType.MGRS.getName(), null, null, null, null, null, null, null, null, null, false);
        final SchemaVertexType ipv4Type = new SchemaVertexType(AnalyticConcept.VertexType.IPV4.getName(), null, null, null, null, null, null, null, null, null, false);
        final SchemaVertexType ipv6Type = new SchemaVertexType(AnalyticConcept.VertexType.IPV6.getName(), null, null, null, null, null, null, null, null, null, false);
        final SchemaVertexType emailAddressType = new SchemaVertexType(AnalyticConcept.VertexType.EMAIL_ADDRESS.getName(), null, null, null, null, null, null, null, null, null, false);
        final SchemaVertexType hostNameType = new SchemaVertexType(AnalyticConcept.VertexType.HOST_NAME.getName(), null, null, null, null, null, null, null, null, null, false);
        final SchemaVertexType urlType = new SchemaVertexType(AnalyticConcept.VertexType.URL.getName(), null, null, null, null, null, null, null, null, null, false);
        final SchemaVertexType telephoneIdentifierType = new SchemaVertexType(AnalyticConcept.VertexType.TELEPHONE_IDENTIFIER.getName(), null, null, null, null, null, null, null, null, null, false);
        
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        final StoreGraph graph = new StoreGraph(schema);
        
        final int vxId1 = graph.addVertex();
        final int vxId2 = graph.addVertex();
        final int vxId3 = graph.addVertex();
        final int vxId4 = graph.addVertex();
        final int vxId5 = graph.addVertex();
        final int vxId6 = graph.addVertex();
        final int vxId7 = graph.addVertex();
        final int vxId8 = graph.addVertex();
        final int vxId9 = graph.addVertex();
        final int vxId10 = graph.addVertex();
        final int vxId11 = graph.addVertex();
        final int vxId12 = graph.addVertex();
                
        final int typeVertexAttribute = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        graph.setObjectValue(typeVertexAttribute, vxId1, md5Type);
        graph.setObjectValue(typeVertexAttribute, vxId2, sha1Type);
        graph.setObjectValue(typeVertexAttribute, vxId3, sha256Type);
        graph.setObjectValue(typeVertexAttribute, vxId4, countryType);
        graph.setObjectValue(typeVertexAttribute, vxId5, geohashType);
        graph.setObjectValue(typeVertexAttribute, vxId6, mgrsType);
        graph.setObjectValue(typeVertexAttribute, vxId7, ipv4Type);
        graph.setObjectValue(typeVertexAttribute, vxId8, ipv6Type);
        graph.setObjectValue(typeVertexAttribute, vxId9, emailAddressType);
        graph.setObjectValue(typeVertexAttribute, vxId10, hostNameType);
        graph.setObjectValue(typeVertexAttribute, vxId11, urlType);
        graph.setObjectValue(typeVertexAttribute, vxId12, telephoneIdentifierType);
        
        final int[] vertexIds = new int[]{vxId1, vxId2, vxId3, vxId4, vxId5, vxId6, 
            vxId7, vxId8, vxId9, vxId10, vxId11, vxId12};
        for (final int vxId : vertexIds) {
            assertNull(((SchemaVertexType) graph.getObjectValue(typeVertexAttribute, vxId)).getDetectionRegex());
            assertNull(((SchemaVertexType) graph.getObjectValue(typeVertexAttribute, vxId)).getValidationRegex());           
        }
        
        final AnalyticSchemaV4UpdateProvider instance = new AnalyticSchemaV4UpdateProvider();
        instance.schemaUpdate(graph);
        
        assertEquals(((SchemaVertexType) graph.getObjectValue(typeVertexAttribute, vxId1)).getDetectionRegex(), AnalyticConcept.VertexType.MD5.getDetectionRegex());
        assertEquals(((SchemaVertexType) graph.getObjectValue(typeVertexAttribute, vxId1)).getValidationRegex(), AnalyticConcept.VertexType.MD5.getValidationRegex());
        assertEquals(((SchemaVertexType) graph.getObjectValue(typeVertexAttribute, vxId2)).getDetectionRegex(), AnalyticConcept.VertexType.SHA1.getDetectionRegex());
        assertEquals(((SchemaVertexType) graph.getObjectValue(typeVertexAttribute, vxId2)).getValidationRegex(), AnalyticConcept.VertexType.SHA1.getValidationRegex());
        assertEquals(((SchemaVertexType) graph.getObjectValue(typeVertexAttribute, vxId3)).getDetectionRegex(), AnalyticConcept.VertexType.SHA256.getDetectionRegex());
        assertEquals(((SchemaVertexType) graph.getObjectValue(typeVertexAttribute, vxId3)).getValidationRegex(), AnalyticConcept.VertexType.SHA256.getValidationRegex());
        assertEquals(((SchemaVertexType) graph.getObjectValue(typeVertexAttribute, vxId4)).getDetectionRegex(), AnalyticConcept.VertexType.COUNTRY.getDetectionRegex());
        assertEquals(((SchemaVertexType) graph.getObjectValue(typeVertexAttribute, vxId4)).getValidationRegex(), AnalyticConcept.VertexType.COUNTRY.getValidationRegex());
        assertEquals(((SchemaVertexType) graph.getObjectValue(typeVertexAttribute, vxId5)).getDetectionRegex(), AnalyticConcept.VertexType.GEOHASH.getDetectionRegex());
        assertEquals(((SchemaVertexType) graph.getObjectValue(typeVertexAttribute, vxId5)).getValidationRegex(), AnalyticConcept.VertexType.GEOHASH.getValidationRegex());
        assertEquals(((SchemaVertexType) graph.getObjectValue(typeVertexAttribute, vxId6)).getDetectionRegex(), AnalyticConcept.VertexType.MGRS.getDetectionRegex());
        assertEquals(((SchemaVertexType) graph.getObjectValue(typeVertexAttribute, vxId6)).getValidationRegex(), AnalyticConcept.VertexType.MGRS.getValidationRegex());
        assertEquals(((SchemaVertexType) graph.getObjectValue(typeVertexAttribute, vxId7)).getDetectionRegex(), AnalyticConcept.VertexType.IPV4.getDetectionRegex());
        assertEquals(((SchemaVertexType) graph.getObjectValue(typeVertexAttribute, vxId7)).getValidationRegex(), AnalyticConcept.VertexType.IPV4.getValidationRegex());
        assertEquals(((SchemaVertexType) graph.getObjectValue(typeVertexAttribute, vxId8)).getDetectionRegex(), AnalyticConcept.VertexType.IPV6.getDetectionRegex());
        assertEquals(((SchemaVertexType) graph.getObjectValue(typeVertexAttribute, vxId8)).getValidationRegex(), AnalyticConcept.VertexType.IPV6.getValidationRegex());
        assertEquals(((SchemaVertexType) graph.getObjectValue(typeVertexAttribute, vxId9)).getDetectionRegex(), AnalyticConcept.VertexType.EMAIL_ADDRESS.getDetectionRegex());
        assertEquals(((SchemaVertexType) graph.getObjectValue(typeVertexAttribute, vxId9)).getValidationRegex(), AnalyticConcept.VertexType.EMAIL_ADDRESS.getValidationRegex());
        assertEquals(((SchemaVertexType) graph.getObjectValue(typeVertexAttribute, vxId10)).getDetectionRegex(), AnalyticConcept.VertexType.HOST_NAME.getDetectionRegex());
        assertEquals(((SchemaVertexType) graph.getObjectValue(typeVertexAttribute, vxId10)).getValidationRegex(), AnalyticConcept.VertexType.HOST_NAME.getValidationRegex());
        assertEquals(((SchemaVertexType) graph.getObjectValue(typeVertexAttribute, vxId11)).getDetectionRegex(), AnalyticConcept.VertexType.URL.getDetectionRegex());
        assertEquals(((SchemaVertexType) graph.getObjectValue(typeVertexAttribute, vxId11)).getValidationRegex(), AnalyticConcept.VertexType.URL.getValidationRegex());
        assertEquals(((SchemaVertexType) graph.getObjectValue(typeVertexAttribute, vxId12)).getDetectionRegex(), AnalyticConcept.VertexType.TELEPHONE_IDENTIFIER.getDetectionRegex());
        assertEquals(((SchemaVertexType) graph.getObjectValue(typeVertexAttribute, vxId12)).getValidationRegex(), AnalyticConcept.VertexType.TELEPHONE_IDENTIFIER.getValidationRegex());
    }   
}
