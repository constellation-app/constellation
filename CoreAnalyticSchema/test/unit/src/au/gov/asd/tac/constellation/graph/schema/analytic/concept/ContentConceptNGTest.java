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
package au.gov.asd.tac.constellation.graph.schema.analytic.concept;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import static org.testng.Assert.assertEquals;
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
public class ContentConceptNGTest {
    
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
     * Test whether all the attributes of ContentConcept are registered in the Analytic Schema
     */
    @Test
    public void testContentAttributesRegistered() {
        System.out.println("contentAttributesRegistered");
        
        final SchemaFactory schemaFactory = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID);
        
        final List<SchemaAttribute> registeredAttributes = new ArrayList<>();
        for (final Map<String, SchemaAttribute> graphElementAttributes : schemaFactory.getRegisteredAttributes().values()) {
            registeredAttributes.addAll(graphElementAttributes.values());
        }

        final ContentConcept instance = new ContentConcept();
        final Collection<SchemaAttribute> contentAttributes = instance.getSchemaAttributes();
        
        for (final SchemaAttribute contentAttribute : contentAttributes) {
            assertTrue(registeredAttributes.contains(contentAttribute));
        }
    }
    
    /**
     * Test to check whether all attributes in ContentConcept have been added to 
     * the collection of schema attributes for the concept
     */
    @Test
    public void testAttributesCorrectlyAdded() {
        System.out.println("attributesCorrectlyAdded");
        
        final ContentConcept instance = new ContentConcept();
        final Collection<SchemaAttribute> contentAttributes = instance.getSchemaAttributes();
        
        final List<SchemaAttribute> nodeAttributes = ConceptTestUtilities.getElementTypeSpecificAttributes(contentAttributes, GraphElementType.VERTEX);
        final List<SchemaAttribute> transactionAttributes = ConceptTestUtilities.getElementTypeSpecificAttributes(contentAttributes, GraphElementType.TRANSACTION);
        
        final int nodeAttributeCount = ConceptTestUtilities.getFieldCount(ContentConcept.VertexAttribute.class, SchemaAttribute.class);
        final int transactionAttributeCount = ConceptTestUtilities.getFieldCount(ContentConcept.TransactionAttribute.class, SchemaAttribute.class);
        
        // ensure that all created attributes have been added to the schema attributes collection
        assertEquals(nodeAttributes.size(), nodeAttributeCount);
        assertEquals(transactionAttributes.size(), transactionAttributeCount);
        // this check will catch out any new attribute classes added to the concept
        assertEquals(contentAttributes.size(), nodeAttributeCount + transactionAttributeCount);
    }
}
