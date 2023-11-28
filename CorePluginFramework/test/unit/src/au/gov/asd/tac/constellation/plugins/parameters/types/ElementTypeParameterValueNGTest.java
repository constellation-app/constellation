/*
 * Copyright 2010-2023 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.graph.GraphElementType;
import static org.testng.Assert.assertEquals;
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
public class ElementTypeParameterValueNGTest {

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
     * Test of validateString method, of class ElementTypeParameterValue.
     */
    @Test
    public void testValidateString() {
        System.out.println("validateString");
        
        final ElementTypeParameterValue instance = new ElementTypeParameterValue();
        assertNull(instance.validateString("Node"));
        assertNull(instance.validateString("Transaction"));
        assertEquals(instance.validateString("made up"), "made up is not a valid element type");
    }

    /**
     * Test of setStringValue method, of class ElementTypeParameterValue.
     */
    @Test
    public void testSetStringValue() {
        System.out.println("setStringValue");
        
        final ElementTypeParameterValue elementTypeValue = new ElementTypeParameterValue(GraphElementType.VERTEX);
        assertEquals(elementTypeValue.getGraphElementType(), GraphElementType.VERTEX);
        
        assertTrue(elementTypeValue.setStringValue("Metadata"));
        assertEquals(elementTypeValue.getGraphElementType(), GraphElementType.META);
        
        // value is the same so no change made
        assertFalse(elementTypeValue.setStringValue("Metadata"));
        assertEquals(elementTypeValue.getGraphElementType(), GraphElementType.META);
    }

    /**
     * Test of setObjectValue method, of class ElementTypeParameterValue.
     */
    @Test
    public void testSetObjectValue() {
        System.out.println("setObjectValue");
        
        final ElementTypeParameterValue elementTypeValue = new ElementTypeParameterValue(GraphElementType.VERTEX);
        assertEquals(elementTypeValue.getObjectValue(), GraphElementType.VERTEX);
        
        // String isn't GraphElementType
        assertFalse(elementTypeValue.setObjectValue("Metadata"));
        assertEquals(elementTypeValue.getObjectValue(), GraphElementType.VERTEX);
        
        assertTrue(elementTypeValue.setObjectValue(GraphElementType.META));
        assertEquals(elementTypeValue.getObjectValue(), GraphElementType.META);
        
        // value is the same so no change made
        assertFalse(elementTypeValue.setObjectValue(GraphElementType.META));
        assertEquals(elementTypeValue.getObjectValue(), GraphElementType.META);
    }

    /**
     * Test of createCopy method, of class ElementTypeParameterValue.
     */
    @Test
    public void testCreateCopy() {
        System.out.println("createCopy");
        
        final ElementTypeParameterValue elementTypeValue = new ElementTypeParameterValue();
        final ParameterValue elementTypeCopy = elementTypeValue.createCopy();
        assertTrue(elementTypeValue.equals(elementTypeCopy));
        
        elementTypeValue.setObjectValue(GraphElementType.META);
        assertFalse(elementTypeValue.equals(elementTypeCopy));
    }

    /**
     * Test of toString method, of class ElementTypeParameterValue.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        
        final ElementTypeParameterValue elementTypeValue = new ElementTypeParameterValue();
        assertEquals(elementTypeValue.toString(), "No Value");
        
        elementTypeValue.setObjectValue(GraphElementType.EDGE);
        assertEquals(elementTypeValue.toString(), "Edge");
    }

    /**
     * Test of equals method, of class ElementTypeParameterValue.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        
        final ElementTypeParameterValue elementTypeValue = new ElementTypeParameterValue();
        elementTypeValue.setObjectValue(GraphElementType.META);
        final ElementTypeParameterValue comp1 = new ElementTypeParameterValue(GraphElementType.VERTEX);
        final ElementTypeParameterValue comp2 = new ElementTypeParameterValue(GraphElementType.META);
        
        assertFalse(elementTypeValue.equals(null));
        assertFalse(elementTypeValue.equals(true));
        assertFalse(elementTypeValue.equals(comp1));
        assertTrue(elementTypeValue.equals(comp2));
    }  
}
