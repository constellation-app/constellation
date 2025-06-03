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
package au.gov.asd.tac.constellation.views.wordcloud.content;

import au.gov.asd.tac.constellation.views.wordcloud.content.SparseMatrix.ArithmeticHandler;
import au.gov.asd.tac.constellation.views.wordcloud.content.SparseMatrix.FloatArithmeticHandler;
import au.gov.asd.tac.constellation.views.wordcloud.content.SparseMatrix.IntegerArithmeticHandler;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentNavigableMap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for SparseMatrix 
 * 
 * @author Delphinus8821
 */
public class SparseMatrixNGTest {

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
     * Test of constructMatrix method, of class SparseMatrix.
     */
    @Test
    public void testConstructMatrix() {
        System.out.println("constructMatrix");
        Number noEntryVal = 4;
        SparseMatrix result = SparseMatrix.constructMatrix(noEntryVal);
        ArithmeticHandler calc = result.getCalc();
        assertEquals(calc.getClass(), IntegerArithmeticHandler.class);
        
        noEntryVal = 2.0F;
        result = SparseMatrix.constructMatrix(noEntryVal);
        calc = result.getCalc();
        assertEquals(calc.getClass(), FloatArithmeticHandler.class);
        
        noEntryVal = null;
        result = SparseMatrix.constructMatrix(noEntryVal);
        assertEquals(result, null);
    }

    /**
     * Test of putCell method, of class SparseMatrix.
     */
    @Test
    public void testPutCell() {
        System.out.println("putCell");
        final int i = 2;
        final int j = 6;
        final Number val = 4;
        final SparseMatrix instance = SparseMatrix.constructMatrix(val);
        instance.putCell(i, j, val);
        final Number result = instance.getCell(i, j);
        assertEquals(result, val);
    }

    /**
     * Test of getCellPrimitive method, of class SparseMatrix.
     */
    @Test
    public void testGetCellPrimitive() {
        System.out.println("getCellPrimitive");
        final int i = 2;
        final int j = 6;
        final Number val = 4;
        final SparseMatrix instance = SparseMatrix.constructMatrix(val);
        final Number expResult = 4;
        final Number result = instance.getCellPrimitive(i, j);
        assertEquals(result, expResult);
    }

    /**
     * Test of clearCell method, of class SparseMatrix.
     */
    @Test
    public void testClearCell() {
        System.out.println("clearCell");
        final int i = 2;
        final int j = 6;
        final Number val = 4;
        final SparseMatrix instance = SparseMatrix.constructMatrix(val);
        instance.putCell(i, j, val);
        instance.clearCell(i, j);
        assertTrue(instance.getData().get(i) == null);
    }

    /**
     * Test of getColumnKeys method, of class SparseMatrix.
     */
    @Test
    public void testGetColumnKeys() {
        System.out.println("getColumnKeys");
        final int i = 2;
        final int j = 6;
        final Number val = 4;
        final SparseMatrix instance = SparseMatrix.constructMatrix(val);
        instance.putCell(i, j, val);
        final Integer[] expResult = {2};
        final Integer[] result = instance.getColumnKeys();
        assertEquals(result, expResult);
    }

    /**
     * Test of getNumColumns method, of class SparseMatrix.
     */
    @Test
    public void testGetNumColumns() {
        System.out.println("getNumColumns");
        final int i = 2;
        final int j = 6;
        final Number val = 4;
        final SparseMatrix instance = SparseMatrix.constructMatrix(val);
        instance.putCell(i, j, val);
        final int expResult = 1;
        final int result = instance.getNumColumns();
        assertEquals(result, expResult);
    }

    /**
     * Test of getEuclidianDistanceBetweenColumns method, of class SparseMatrix.
     */
    @Test
    public void testGetEuclidianDistanceBetweenColumns() {
        System.out.println("getEuclidianDistanceBetweenColumns");
        final Number val = 4;
        final SparseMatrix instance = SparseMatrix.constructMatrix(val);
        instance.putCell(1, 2, val);
        instance.putCell(0, 4, 6);
        instance.putCell(3, 8, 9);
        final int key1 = 1;
        final int key2 = 3;
        final float expResult = 10.0F;
        final float result = instance.getEuclidianDistanceBetweenColumns(key1, key2);
        assertEquals(result, expResult, 10.0);
    }

    /**
     * Test of getCommonalityDistanceBetweenColumns method, of class SparseMatrix.
     */
    @Test
    public void testGetCommonalityDistanceBetweenColumns() {
        System.out.println("getCommonalityDistanceBetweenColumns");
        final Number val = 4;
        final SparseMatrix instance = SparseMatrix.constructMatrix(val);
        instance.putCell(1, 2, val);
        instance.putCell(2, 4, 6);
        instance.putCell(3, 8, 9);
        final int key1 = 2;
        final int key2 = 1;
        final float expResult = 7.0F;
        final float result = instance.getCommonalityDistanceBetweenColumns(key1, key2);
        assertEquals(result, expResult, 7.0);
    }

    /**
     * Test of calculateCentreOfColumns method, of class SparseMatrix.
     */
    @Test
    public void testCalculateCentreOfColumns() {
        System.out.println("calculateCentreOfColumns");
        final Integer[] keys = {0, 1, 2, 3};
        final int keyToPlaceCentre = 0;
        final Number val = 4;
        final SparseMatrix instance = SparseMatrix.constructMatrix(val);
        instance.putCell(0, 0, 0);
        instance.putCell(1, 2, val);
        instance.putCell(2, 4, 6);
        instance.putCell(3, 8, 9);
        instance.calculateCentreOfColumns(keys, keyToPlaceCentre);
        final ConcurrentNavigableMap<Integer, Number> result = (ConcurrentNavigableMap) instance.getData().get(keyToPlaceCentre);
        assertEquals(result.size(), 4);
    }

    /**
     * Test of getColumnAsExpandedArray method, of class SparseMatrix.
     */
    @Test
    public void testGetColumnAsExpandedArray() {
        System.out.println("getColumnAsExpandedArray");
        final int key = 1;
        final int fullColumnSize = 3;
        final Number val = 4;
        final SparseMatrix instance = SparseMatrix.constructMatrix(val);
        instance.putCell(0, 0, 0);
        instance.putCell(1, 2, val);
        instance.putCell(2, 4, 6);
        instance.putCell(3, 8, 9);
        final Number[] expResult = {4, 0, 0};
        final Number[] result = instance.getColumnAsExpandedArray(key, fullColumnSize);
        assertEquals(result, expResult);
    }

    /**
     * Test of getColumnAsArray method, of class SparseMatrix.
     */
    @Test
    public void testGetColumnAsArray() {
        System.out.println("getColumnAsArray");
        final int key = 1;
        final Number val = 4;
        final SparseMatrix instance = SparseMatrix.constructMatrix(val);
        instance.putCell(0, 0, 0);
        instance.putCell(1, 2, val);
        instance.putCell(2, 4, 6);
        instance.putCell(3, 8, 9);
        final Number[] expResult = {4};
        final Number[] result = instance.getColumnAsArray(key);
        assertEquals(result, expResult);
    }

    /**
     * Test of getColumnSum method, of class SparseMatrix.
     */
    @Test
    public void testGetColumnSum() {
        System.out.println("getColumnSum");
        final int key = 1;
        final Number val = 4;
        final SparseMatrix instance = SparseMatrix.constructMatrix(val);
        instance.putCell(0, 0, 0);
        instance.putCell(1, 2, val);
        instance.putCell(2, 4, 6);
        instance.putCell(3, 8, 9);
        Number expResult = 8;
        Number result = instance.getColumnSum(key);
        assertEquals(result, expResult);
    }

    /**
     * Test of getColumnElementUnion method, of class SparseMatrix.
     */
    @Test
    public void testGetColumnElementUnion() {
        System.out.println("getColumnElementUnion");
        final Set<Integer> set = new HashSet();
        set.add(0);
        set.add(1);
        final Iterable<Integer> keySet = set;
        final Number val = 4;
        final SparseMatrix instance = SparseMatrix.constructMatrix(val);
        instance.putCell(0, 0, 0);
        instance.putCell(1, 2, val);
        instance.putCell(2, 4, 6);
        instance.putCell(3, 8, 9);
        final Set result = instance.getColumnElementUnion(keySet);
        assertTrue(result.size() == 2);
    }

    /**
     * Test of getColumnElementIntersection method, of class SparseMatrix.
     */
    @Test
    public void testGetColumnElementIntersection() {
        System.out.println("getColumnElementIntersection");
        final Set<Integer> set = new HashSet();
        set.add(0);
        set.add(1);
        final Iterable<Integer> keySet = set;
        final Number val = 4;
        final SparseMatrix instance = SparseMatrix.constructMatrix(val);
        instance.putCell(0, 0, 0);
        instance.putCell(1, 2, val);
        final Set result = instance.getColumnElementIntersection(keySet);
        assertEquals(result.size(), 0);
    }

    /**
     * Test of getConstituentExtendedColumnAsArray method, of class SparseMatrix.
     */
    @Test
    public void testGetConstituentExtendedColumnAsArray() {
        System.out.println("getConstituentExtendedColumnAsArray");
        final int key = 1;
        final Set<Integer> set = new HashSet();
        set.add(0);
        set.add(1);
        final Iterable<Integer> elements = set;
        final Number val = 4;
        final SparseMatrix instance = SparseMatrix.constructMatrix(val);
        instance.putCell(0, 0, 0);
        instance.putCell(1, 2, val);
        instance.putCell(2, 4, 6);
        instance.putCell(3, 8, 9);
        final Number[] expResult = {0, 0, 4};
        final Number[] result = instance.getConstituentExtendedColumnAsArray(key, elements);
        assertEquals(result, expResult);
    }

    /**
     * Test of getLargestColumnSum method, of class SparseMatrix.
     */
    @Test
    public void testGetLargestColumnSum_Set() {
        System.out.println("getLargestColumnSum");
        final Set<Integer> keys = new HashSet();
        keys.add(0);
        keys.add(1);
        final Number val = 4;
        final SparseMatrix instance = SparseMatrix.constructMatrix(val);
        instance.putCell(0, 0, 0);
        instance.putCell(1, 2, val);
        instance.putCell(2, 4, 6);
        final Number expResult = 8;
        final Number result = instance.getLargestColumnSum(keys);
        assertEquals(result, expResult);
    }

    /**
     * Test of getColumnSize method, of class SparseMatrix.
     */
    @Test
    public void testGetColumnSize() {
        System.out.println("getColumnSize");
        final int key = 1;
        final Number val = 4;
        final SparseMatrix instance = SparseMatrix.constructMatrix(val);
        instance.putCell(0, 0, 0);
        instance.putCell(1, 2, val);
        instance.putCell(2, 4, 6);
        final int expResult = 1;
        final int result = instance.getColumnSize(key);
        assertEquals(result, expResult);
    }

    /**
     * Test of getLargestColumnSize method, of class SparseMatrix.
     */
    @Test
    public void testGetLargestColumnSize() {
        System.out.println("getLargestColumnSize");
        final Number val = 4;
        final SparseMatrix instance = SparseMatrix.constructMatrix(val);
        instance.putCell(0, 0, 0);
        instance.putCell(1, 2, val);
        instance.putCell(2, 4, 6);
        final int expResult = 1;
        final int result = instance.getLargestColumnSize();
        assertEquals(result, expResult);
    }

    /**
     * Test of constructTokenSets method, of class SparseMatrix.
     */
    @Test
    public void testConstructTokenSets() {
        System.out.println("constructTokenSets");
        final Number val = 4;
        final SparseMatrix instance = SparseMatrix.constructMatrix(val);
        instance.putCell(0, 0, 0);
        instance.putCell(1, 2, val);
        instance.putCell(2, 4, 6);
        final int expResult = 3;
        final Map result = instance.constructTokenSets();
        assertEquals(result.size(), expResult);
    }
    
    /**
     * Tests for IntegerArithmeticHandler
     */
    @Test
    public void testIntegerArithmeticHandler() {
        final ArithmeticHandler instance = IntegerArithmeticHandler.INSTANCE;
        
        // Test getZero
        assertEquals(instance.getZero(), 0);
        
        // Test max
        assertEquals(instance.max(2, 4), 4);
        
        // Test min
        assertEquals(instance.min(2, 4), 2);
        
        // Test add
        assertEquals(instance.add(1, 3), 4);
        
        // Test difference
        assertEquals(instance.difference(3, 7), 4);
        
        // Test square
        assertEquals(instance.square(3), 9);
        
        // Test sqrt
        assertEquals(instance.sqrt(9), 3);
        
        // Test scale
        assertEquals(instance.scale(2, 1), 2);
        
        // Test makeArray
        assertEquals(instance.makeArray(2).length, 2);
    }
    
    /**
     * Tests for FloatArithmeticHandler
     */
    @Test
    public void testFloatArithmeticHandler() {
        final ArithmeticHandler instance = FloatArithmeticHandler.INSTANCE;
        
        // Test getZero
        assertEquals(instance.getZero(), 0.0F);
        
        // Test max
        assertEquals(instance.max(2.2F, 4.2F), 4.2F);
        
        // Test min
        assertEquals(instance.min(2.5F, 4.8F), 2.5F);
        
        // Test add
        assertEquals(instance.add(1.0F, 3.0F), 4.0F);
        
        // Test difference
        assertEquals(instance.difference(3.7F, 7.7F), 3.9999998F);
        
        // Test square
        assertEquals(instance.square(3.0F), 9.0F);
        
        // Test sqrt
        assertEquals(instance.sqrt(9.0F), 3.0F);
        
        // Test scale
        assertEquals(instance.scale(2.2F, 1.5F), 1.4666667F);
        
        // Test makeArray
        assertEquals(instance.makeArray(2).length, 2);        
    }
}
