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
package au.gov.asd.tac.constellation.plugins.parameters.types;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType.ObjectParameterValue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
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
public class ObjectParameterTypeNGTest {

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
     * Test of build method, of class ObjectParameterType.
     */
    @Test
    public void testBuild() {
        System.out.println("build");
        
        final PluginParameter<ObjectParameterValue> objectParam = ObjectParameterType.build("My Object");
        
        assertEquals(objectParam.getParameterValue(), new ObjectParameterValue());
        assertEquals(objectParam.getId(), "My Object");
        assertEquals(objectParam.getType().getId(), "object");
    }
    
    /**
     * Test of setStringValue method, of class ObjectParameterValue.
     */
    @Test
    public void testSetStringValue() {
        System.out.println("setStringValue");
        
        final ObjectParameterValue objectValue = new ObjectParameterValue(false);
        assertEquals(objectValue.getObjectValue(), false);
        
        assertTrue(objectValue.setStringValue("A string"));
        assertEquals(objectValue.getObjectValue(), "A string");
        // fails as value is same as the one already stored
        assertFalse(objectValue.setStringValue("A string"));
        assertEquals(objectValue.getObjectValue(), "A string");
    }
    
    /**
     * Test of setObjectValue method, of class ObjectParameterValue.
     */
    @Test
    public void testSetObjectValue() {
        System.out.println("setObjectValue");
        
        final ObjectParameterValue objectValue = new ObjectParameterValue(false);
        assertEquals(objectValue.getObjectValue(), false);
        
        assertTrue(objectValue.setObjectValue("A string"));
        assertEquals(objectValue.getObjectValue(), "A string");
        
        assertTrue(objectValue.setObjectValue(11));
        assertEquals(objectValue.getObjectValue(), 11);
        // fails as value is same as the one already stored
        assertFalse(objectValue.setObjectValue(11));
        assertEquals(objectValue.getObjectValue(), 11);
    }
    
    /**
     * Test of createCopy method, of class ObjectParameterValue.
     */
    @Test
    public void testCreateCopy() {
        System.out.println("createCopy");
        
        final ObjectParameterValue objectValue = new ObjectParameterValue(false);
        final ParameterValue objectCopy = objectValue.createCopy();
        assertTrue(objectValue.equals(objectCopy));
        
        objectValue.setObjectValue("A string");
        assertFalse(objectValue.equals(objectCopy));
    }
    
    /**
     * Test of equals method, of class ObjectParameterValue.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        
        final ObjectParameterValue objectValue = new ObjectParameterValue();
        objectValue.setStringValue("A string");
        final ObjectParameterValue comp1 = new ObjectParameterValue();
        final ObjectParameterValue comp2 = new ObjectParameterValue("A string");
        
        assertFalse(objectValue.equals(null));
        assertFalse(objectValue.equals(true));
        assertFalse(objectValue.equals(comp1));
        assertTrue(objectValue.equals(comp2));
    }
    
    /**
     * Test of toString method, of class ObjectParameterValue.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        
        final ObjectParameterValue objectValue = new ObjectParameterValue();
        assertEquals(objectValue.toString(), "null");
        objectValue.setObjectValue("A string");
        assertEquals(objectValue.toString(), "A string");
        objectValue.setObjectValue(11);
        assertEquals(objectValue.toString(), "11");
    }
}
