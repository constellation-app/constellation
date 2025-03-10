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
package au.gov.asd.tac.constellation.plugins.parameters.types;

import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
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
public class GraphAttributeParameterValueNGTest {
    
    GraphAttribute graphAttribute;
    
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
        graphAttribute = new GraphAttribute(GraphElementType.VERTEX, "String", "my attribute", "a description");
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of validateString method, of class GraphAttributeParameterValue.
     */
    @Test(expectedExceptions = UnsupportedOperationException.class, 
            expectedExceptionsMessageRegExp = "Cannot set GraphAttributeParameterValue using a String")
    public void testValidateString() {
        System.out.println("validateString");
        
        final GraphAttributeParameterValue instance = new GraphAttributeParameterValue();
        instance.validateString("");
    }

    /**
     * Test of setStringValue method, of class GraphAttributeParameterValue.
     */
    @Test(expectedExceptions = UnsupportedOperationException.class, 
            expectedExceptionsMessageRegExp = "Cannot set GraphAttributeParameterValue using a String")
    public void testSetStringValue() {
        System.out.println("setStringValue");
        
        final GraphAttributeParameterValue instance = new GraphAttributeParameterValue();
        instance.setStringValue("");
    }
    
    /**
     * Test of setObjectValue method, of class GraphAttributeParameterValue.
     */
    @Test
    public void testSetObjectValue() {
        System.out.println("setObjectValue");
        
        final GraphAttributeParameterValue graphAttributeValue = new GraphAttributeParameterValue();
        assertNull(graphAttributeValue.getObjectValue());
        
        // String isn't GraphElementType
        assertFalse(graphAttributeValue.setObjectValue("My Graph Attribute"));
        assertNull(graphAttributeValue.getObjectValue());
        
        assertTrue(graphAttributeValue.setObjectValue(graphAttribute));
        assertEquals(graphAttributeValue.getObjectValue(), graphAttribute);
        
        // value is the same so no change made
        assertFalse(graphAttributeValue.setObjectValue(graphAttribute));
        assertEquals(graphAttributeValue.getObjectValue(), graphAttribute);
    }

    /**
     * Test of createCopy method, of class GraphAttributeParameterValue.
     */
    @Test
    public void testCreateCopy() {
        System.out.println("createCopy");
        
        final GraphAttributeParameterValue graphAttributeValue = new GraphAttributeParameterValue(graphAttribute);
        final ParameterValue graphAttributeCopy = graphAttributeValue.createCopy();
        assertEquals(graphAttributeValue.getObjectValue(), graphAttributeCopy.getObjectValue());
        
        graphAttributeValue.setObjectValue(new GraphAttribute(GraphElementType.TRANSACTION, "boolean", "my other attribute", "another description"));
        assertNotEquals(graphAttributeValue.getObjectValue(), graphAttributeCopy.getObjectValue());
    }

    /**
     * Test of toString method, of class GraphAttributeParameterValue.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        
        final GraphAttributeParameterValue graphAttributeValue = new GraphAttributeParameterValue();
        assertEquals(graphAttributeValue.toString(), "No Value");
        
        graphAttributeValue.setObjectValue(graphAttribute);
        assertEquals(graphAttributeValue.toString(), "my attribute (Node)");
    }

    /**
     * Test of compareTo method, of class GraphAttributeParameterValue.
     */
    @Test
    public void testCompareTo() {
        System.out.println("compareTo");
        
        final GraphAttributeParameterValue graphParameterValue = new GraphAttributeParameterValue(graphAttribute);
        
        final GraphAttribute comp1Attribute = new GraphAttribute(GraphElementType.TRANSACTION, "String", "my other attribute", "another description");
        final GraphAttribute comp2Attribute = new GraphAttribute(GraphElementType.VERTEX, "String", "My other attribute", "another description");
        final GraphAttribute comp3Attribute = new GraphAttribute(GraphElementType.VERTEX, "String", "my attribute", "another description");
        final GraphAttributeParameterValue comp1 = new GraphAttributeParameterValue(comp1Attribute);
        final GraphAttributeParameterValue comp2 = new GraphAttributeParameterValue(comp2Attribute);
        final GraphAttributeParameterValue comp3 = new GraphAttributeParameterValue(comp3Attribute);
        
        
        assertEquals(graphParameterValue.compareTo(comp1), 1);
        assertEquals(comp1.compareTo(graphParameterValue), -1);
        assertEquals(graphParameterValue.compareTo(comp2), 1);
        assertEquals(comp2.compareTo(graphParameterValue), -1);
        assertEquals(graphParameterValue.compareTo(comp3), 0);
    }  
}
