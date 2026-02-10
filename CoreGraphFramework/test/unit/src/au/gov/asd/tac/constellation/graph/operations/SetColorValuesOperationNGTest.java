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
package au.gov.asd.tac.constellation.graph.operations;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Andromeda-224
 */
public class SetColorValuesOperationNGTest {
    
    private final ConstellationColor originalColor = ConstellationColor.CLOUDS;
    private final ConstellationColor newColor = ConstellationColor.CHOCOLATE;
    private WritableGraph wg;
    private final int attributeId = 101;
    private final int elementId = 1;

    @BeforeMethod
    public void setUpMethod() {
        wg = mock(WritableGraph.class);        
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of setValue method, of class SetColorValuesOperation.
     */
    @Test
    public void testSetValue() {
        System.out.println("SetColorValuesOperation setValue");

        SetColorValuesOperation instance = new SetColorValuesOperation(
                wg, GraphElementType.VERTEX, attributeId);
        doReturn(originalColor).when(wg).getObjectValue(attributeId, elementId);
        instance.setValue(elementId, newColor);
        assertTrue(instance.originalColor == originalColor);
        assertTrue(instance.newColor == newColor);
    }

    /**
     * Test of execute method, of class SetColorValuesOperation.
     */
    @Test
    public void testExecute() {
        System.out.println("SetColorValuesOperation execute");
        SetColorValuesOperation instance = new SetColorValuesOperation(
                wg, GraphElementType.VERTEX, attributeId);
        doReturn(originalColor).when(wg).getObjectValue(attributeId, elementId);
        
        // new color not set, setObjectValue not called
        assertTrue(instance.newColor == null);
        instance.execute(wg);
        verify(wg, times(0)).setObjectValue(attributeId, elementId, newColor);
        
        // set new color, setObjectValue called
        instance.setValue(elementId, newColor);
        assertTrue(instance.originalColor == originalColor);
        assertTrue(instance.newColor == newColor);
        
        instance.execute(wg);
        verify(wg, times(1)).setObjectValue(attributeId, elementId, newColor);        
    }

    /**
     * Test of undo method, of class SetColorValuesOperation.
     */
    @Test
    public void testUndo() {
        System.out.println("SetColorValuesOperation undo");
        SetColorValuesOperation instance = new SetColorValuesOperation(
                wg, GraphElementType.VERTEX, attributeId);
        
        // set the original color and new color
        doReturn(originalColor).when(wg).getObjectValue(attributeId, elementId);
        instance.setValue(elementId, newColor);
                
        assertTrue(instance.originalColor == originalColor);
        assertTrue(instance.newColor == newColor);
        
        // undo => set object to original color and new color set to null
        doNothing().when(wg).setObjectValue(attributeId, elementId, originalColor);
        instance.undo(wg);
        verify(wg, times(1)).setObjectValue(attributeId, elementId, originalColor);
        assertTrue(instance.newColor == null);        
    }

    /**
     * Test of isMoreEfficient method, of class SetColorValuesOperation.
     */
    @Test
    public void testIsMoreEfficient() {
        System.out.println("SetColorValuesOperation isMoreEfficient");
        SetColorValuesOperation instance = new SetColorValuesOperation(
                wg, GraphElementType.VERTEX, attributeId);
        boolean expResult = true;
        boolean result = instance.isMoreEfficient();
        assertEquals(result, expResult);      
    }

    /**
     * Test of size method, of class SetColorValuesOperation.
     */
    @Test
    public void testSize() {
        System.out.println("SetColorValuesOperation size");
        doReturn(54).when(wg).getVertexCapacity();
        SetColorValuesOperation instance = new SetColorValuesOperation(
                wg, GraphElementType.VERTEX, attributeId);
        int expResult = GraphElementType.VERTEX.getElementCapacity(wg);
        int result = instance.size();
        System.out.println(result);
        assertEquals(result, expResult);        
    }    
}
