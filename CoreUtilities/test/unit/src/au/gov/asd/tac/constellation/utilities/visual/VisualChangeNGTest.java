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
package au.gov.asd.tac.constellation.utilities.visual;

import static org.mockito.Mockito.mock;
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
 * @author aldebaran30701
 */
public class VisualChangeNGTest {
    
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
     * Test of testConstruction method, of class VisualChange.
     */
    @Test
    public void testConstruction() {
        final VisualProperty property = mock(VisualProperty.class);
        final int[] changeList = new int[25];
        for(int i=0;i<changeList.length;i++){
            changeList[i] = i;
        }
        final int numChangedItems = 5;
        final int id = 0;
        
        final VisualChange vc = new VisualChange(property, changeList, numChangedItems, id);
        
        int currentOrder = vc.getOrder();
        
        assertEquals(vc.getSize(), 25);
        assertEquals(vc.property, property);
        assertEquals(vc.getChangeList(), changeList);
        assertEquals(vc.id, id);
        
        
        final int[] changeList2 = null;
        final VisualChange vc2 = new VisualChange(property, changeList2, numChangedItems, id);
        
        assertEquals(currentOrder + 1, vc2.getOrder());
        assertEquals(vc2.getSize(), numChangedItems);
        assertEquals(vc2.property, property);
        assertNull(vc2.getChangeList());
        assertEquals(vc2.id, id);
    }

    /**
     * Test of hasSameChangeList method, of class VisualChange.
     */
    @Test
    public void testHasSameChangeList() {
        System.out.println("hasSameChangeList");
        
        final VisualProperty property = mock(VisualProperty.class);
        final int[] changeList = new int[25];
        for (int i = 0 ; i< changeList.length; i++){
            changeList[i] = i;
        }
        final int numChangedItems = 5;
        final int id = 0;
        
        final VisualChange vc = new VisualChange(property, changeList, numChangedItems, id);
        final VisualChange vc1 = new VisualChange(property, changeList, numChangedItems, id);
        final int[] changeList2 = null;
        final VisualChange vc2 = new VisualChange(property, changeList2, numChangedItems, id);
        final int[] changeList3 = new int[25];
        for(int i=0;i<changeList3.length-10;i++){
            changeList3[i] = i;
        }
        final VisualChange vc3 = new VisualChange(property, changeList3, numChangedItems, id);
        
        assertFalse(vc.hasSameChangeList(vc2));
        assertFalse(vc2.hasSameChangeList(vc));
        assertTrue(vc.hasSameChangeList(vc1));
        assertTrue(vc1.hasSameChangeList(vc));
        assertFalse(vc.hasSameChangeList(null));
        assertFalse(vc.hasSameChangeList(vc3));
        assertFalse(vc3.hasSameChangeList(vc));
    }

    /**
     * Test of getElement method, of class VisualChange.
     */
    @Test
    public void testGetElement() {
        System.out.println("getElement");
        final VisualProperty property = mock(VisualProperty.class);
        final int[] changeList = new int[25];
        for(int i = 0 ; i <changeList.length; i++){
            changeList[i] = i;
        }
        final int numChangedItems = 5;
        final int id = 0;
        
        final VisualChange vc = new VisualChange(property, changeList, numChangedItems, id);
        for(int i = 0; i < changeList.length; i++){
            assertEquals(vc.getElement(i), changeList[i]);
        }
        
        // Assert that the numChangedItems variable is returned when the list is null
        assertEquals(vc.getElement(0), 0);
        assertEquals(vc.getElement(1), 1);
        assertEquals(vc.getElement(100), 100);
        
        
        final VisualChange vcNull = new VisualChange(property, null, numChangedItems, id);
        
        // Assert that the position variable is returned when the list is null
        assertEquals(vcNull.getElement(0), 0);
        assertEquals(vcNull.getElement(1), 1);
        assertEquals(vcNull.getElement(100), 100);
        
        final int[] changeList2 = new int[25];
        for(int i = 0; i < changeList2.length; i++){
            changeList2[i] = i + 2;
        }
        
        final VisualChange vcAdjusted = new VisualChange(property, changeList2, numChangedItems, id);
        for(int i = 0; i < changeList2.length; i++){
            assertEquals(vcAdjusted.getElement(i), changeList2[i]);
        }
        
        // Assert that the correct int is returned when the list is present
        assertEquals(vcAdjusted.getElement(0), 0 + 2);
        assertEquals(vcAdjusted.getElement(1), 1 + 2);
        assertEquals(vcAdjusted.getElement(100), 100);
    }

    /**
     * Test of equals method, of class VisualChange.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        
        final VisualProperty property = mock(VisualProperty.class);
        final int[] changeList = new int[25];
        for(int i = 0; i < changeList.length; i++){
            changeList[i] = i;
        }
        final int numChangedItems = 5;
        final int id = 0;
        final VisualChange vc = new VisualChange(property, changeList, numChangedItems, id);
        
        // Same values as vc, just a different object
        final VisualProperty property2 = mock(VisualProperty.class);
        final int[] changeList2 = new int[25];
        for(int i = 0; i < changeList2.length; i++){
            changeList2[i] = i;
        }
        final int numChangedItems2 = 5;
        final int id2 = 0;
        final VisualChange vc2 = new VisualChange(property2, changeList2, numChangedItems2, id2);
        
        // ID value is different to vc and vc2
        final VisualProperty property3 = mock(VisualProperty.class);
        final int[] changeList3 = new int[25];
        for(int i = 0; i < changeList3.length; i++){
            changeList3[i] = i;
        }
        final int numChangedItems3 = 5;
        final int id3 = 10;
        
        final VisualChange vc3 = new VisualChange(property3, changeList3, numChangedItems3, id3);
        
        assertTrue(vc.equals(vc2));
        assertTrue(vc2.equals(vc));
        assertTrue(vc.equals(vc));
        assertFalse(vc.equals(null));
        assertFalse(vc2.equals(null));
        assertFalse(vc.equals("String Value"));
        assertFalse(vc.equals(vc3));
        assertFalse(vc2.equals(vc3));
        assertFalse(vc3.equals(vc2));
        assertFalse(vc3.equals(vc));
    }

    /**
     * Test of compareTo method, of class VisualChange.
     */
    @Test
    public void testCompareTo() {
        System.out.println("compareTo");
        
        final VisualProperty property = mock(VisualProperty.class);
        final int[] changeList = new int[25];
        for(int i = 0; i < changeList.length; i++){
            changeList[i] = i;
        }
        final int numChangedItems = 5;
        final int id = 0;
        final VisualChange vc = new VisualChange(property, changeList, numChangedItems, id);
        
        // Same values as vc, just a different object
        final VisualProperty property2 = mock(VisualProperty.class);
        final int[] changeList2 = new int[25];
        for(int i = 0; i < changeList2.length; i++){
            changeList2[i] = i;
        }
        final int numChangedItems2 = 5;
        final int id2 = 0;
        final VisualChange vc2 = new VisualChange(property2, changeList2, numChangedItems2, id2);
        
        // ID value is different to vc and vc2
        final VisualProperty property3 = mock(VisualProperty.class);
        final int[] changeList3 = new int[25];
        for(int i = 0; i < changeList3.length; i++){
            changeList3[i] = i;
        }
        final int numChangedItems3 = 5;
        final int id3 = 10;
        
        final VisualChange vc3 = new VisualChange(property3, changeList3, numChangedItems3, id3);
        
        // Assert that the correct int values are returned when different objects are compared
        assertEquals(vc.compareTo(vc2), 0);
        assertEquals(vc.compareTo(null), -1);
        assertEquals(vc.compareTo(vc2), 0);
        assertEquals(vc.compareTo(vc3), -1);
        assertEquals(vc3.compareTo(vc), 1);
    }
}
