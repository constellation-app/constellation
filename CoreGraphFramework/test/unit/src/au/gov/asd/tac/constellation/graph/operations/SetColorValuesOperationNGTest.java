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
    
    private final ConstellationColor ORIGINAL_COLOR = ConstellationColor.CLOUDS;
    private final ConstellationColor NEW_COLOR = ConstellationColor.CHOCOLATE;
    private WritableGraph wg;
    private final int ATTRIBUTEID = 101;
    private final int ELEMENTID = 1;
    
    public SetColorValuesOperationNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        wg = mock(WritableGraph.class);        
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of setValue method, of class SetColorValuesOperation.
     */
    @Test
    public void testSetValue() {
        System.out.println("SetColorValuesOperation setValue");

        SetColorValuesOperation instance = new SetColorValuesOperation(
                wg, GraphElementType.VERTEX, ATTRIBUTEID);
        doReturn(ORIGINAL_COLOR).when(wg).getObjectValue(ATTRIBUTEID, ELEMENTID);
        instance.setValue(ELEMENTID, NEW_COLOR);
        assertTrue(instance.originalColor == ORIGINAL_COLOR);
        assertTrue(instance.newColor == NEW_COLOR);
    }

    /**
     * Test of execute method, of class SetColorValuesOperation.
     */
    @Test
    public void testExecute() {
        System.out.println("SetColorValuesOperation execute");
        SetColorValuesOperation instance = new SetColorValuesOperation(
                wg, GraphElementType.VERTEX, ATTRIBUTEID);
        doReturn(ORIGINAL_COLOR).when(wg).getObjectValue(ATTRIBUTEID, ELEMENTID);
        
        // new color not set, setObjectValue not called
        assertTrue(instance.newColor == null);
        instance.execute(wg);
        verify(wg, times(0)).setObjectValue(ATTRIBUTEID, ELEMENTID, NEW_COLOR);
        
        // set new color, setObjectValue called
        instance.setValue(ELEMENTID, NEW_COLOR);
        assertTrue(instance.originalColor == ORIGINAL_COLOR);
        assertTrue(instance.newColor == NEW_COLOR);
        
        instance.execute(wg);
        verify(wg, times(1)).setObjectValue(ATTRIBUTEID, ELEMENTID, NEW_COLOR);        
    }

    /**
     * Test of undo method, of class SetColorValuesOperation.
     */
    @Test
    public void testUndo() {
        System.out.println("SetColorValuesOperation undo");
        SetColorValuesOperation instance = new SetColorValuesOperation(
                wg, GraphElementType.VERTEX, ATTRIBUTEID);
        
        // set the original color and new color
        doReturn(ORIGINAL_COLOR).when(wg).getObjectValue(ATTRIBUTEID, ELEMENTID);
        instance.setValue(ELEMENTID, NEW_COLOR);
                
        assertTrue(instance.originalColor == ORIGINAL_COLOR);
        assertTrue(instance.newColor == NEW_COLOR);
        
        // undo => set object to original color and new color set to null
        doNothing().when(wg).setObjectValue(ATTRIBUTEID, ELEMENTID, ORIGINAL_COLOR);
        instance.undo(wg);
        verify(wg, times(1)).setObjectValue(ATTRIBUTEID, ELEMENTID, ORIGINAL_COLOR);
        assertTrue(instance.newColor == null);        
    }

    /**
     * Test of isMoreEfficient method, of class SetColorValuesOperation.
     */
    @Test
    public void testIsMoreEfficient() {
        System.out.println("SetColorValuesOperation isMoreEfficient");
        SetColorValuesOperation instance = new SetColorValuesOperation(
                wg, GraphElementType.VERTEX, ATTRIBUTEID);
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
                wg, GraphElementType.VERTEX, ATTRIBUTEID);
        int expResult = GraphElementType.VERTEX.getElementCapacity(wg);
        int result = instance.size();
        System.out.println(result);
        assertEquals(result, expResult);        
    }    
}
