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
package au.gov.asd.tac.constellation.graph.schema.visual.concept;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
public class VisualConceptNGTest {
    
    public VisualConceptNGTest() {
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
     * Test whether all the attributes of VisualConcept are registered in the Visual Schema
     */
    @Test
    public void testVisualAttributesRegistered() {
        System.out.println("visualAttributesRegistered");
        
        final SchemaFactory schemaFactory = SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID);
        
        final List<SchemaAttribute> registeredAttributes = new ArrayList<>();
        for (final Map<String, SchemaAttribute> graphElementAttributes : schemaFactory.getRegisteredAttributes().values()) {
            registeredAttributes.addAll(graphElementAttributes.values());
        }

        final VisualConcept instance = new VisualConcept();
        final Collection<SchemaAttribute> visualAttributes = instance.getSchemaAttributes();
        
        for (final SchemaAttribute visualAttribute : visualAttributes) {
            assertTrue(registeredAttributes.contains(visualAttribute));
        }
    }
    
    /**
     * Test to check whether all attributes in VisualConcept have been added to 
     * the collection of schema attributes for the concept
     */
    @Test
    public void testAttributesCorrectlyAdded() {
        System.out.println("attributesCorrectlyAdded");
        
        final VisualConcept instance = new VisualConcept();
        final Collection<SchemaAttribute> visualAttributes = instance.getSchemaAttributes();
        
        final List<SchemaAttribute> graphAttributes = getElementTypeSpecificAttributes(visualAttributes, GraphElementType.GRAPH);
        final List<SchemaAttribute> nodeAttributes = getElementTypeSpecificAttributes(visualAttributes, GraphElementType.VERTEX);
        final List<SchemaAttribute> transactionAttributes = getElementTypeSpecificAttributes(visualAttributes, GraphElementType.TRANSACTION);
        
        final int graphAttributeCount = getAttributeCount(VisualConcept.GraphAttribute.class);
        final int nodeAttributeCount = getAttributeCount(VisualConcept.VertexAttribute.class);
        final int transactionAttributeCount = getAttributeCount(VisualConcept.TransactionAttribute.class);
        
        // ensure that all created attributes have been added to the schema attributes collection
        assertEquals(graphAttributes.size(), graphAttributeCount);
        assertEquals(nodeAttributes.size(), nodeAttributeCount);
        assertEquals(transactionAttributes.size(), transactionAttributeCount);
        // this check will catch out any new attribute classes added to the concept
        assertEquals(visualAttributes.size(), graphAttributeCount + nodeAttributeCount + transactionAttributeCount);
    }
    
    private int getAttributeCount(final Class<?> attributeClass) {
        int attributeCount = 0;
        for (final Field attribute : attributeClass.getDeclaredFields()) {
            if (attribute.getType() == SchemaAttribute.class) {
                attributeCount++;
            }
        }
        return attributeCount;
    }
    
    private List<SchemaAttribute> getElementTypeSpecificAttributes(final Collection<SchemaAttribute> attributes, 
            final GraphElementType graphElementType) {
        return attributes.stream()
                .filter(attribute -> attribute.getElementType() == graphElementType)
                .collect(Collectors.toUnmodifiableList());
    }
}
