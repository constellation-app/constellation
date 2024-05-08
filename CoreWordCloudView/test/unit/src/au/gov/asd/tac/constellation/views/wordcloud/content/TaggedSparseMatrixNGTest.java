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
package au.gov.asd.tac.constellation.views.wordcloud.content;

import java.util.Set;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for TaggedSparseMatrix 
 * 
 * @author Delphinus8821
 */
public class TaggedSparseMatrixNGTest {

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
     * Test of constructMatrix method, of class TaggedSparseMatrix.
     */
    @Test
    public void testConstructMatrix() {
        System.out.println("constructMatrix");
        Number noEntryVal = 4;
        TaggedSparseMatrix result = TaggedSparseMatrix.constructMatrix(noEntryVal);
        TaggedSparseMatrix.ArithmeticHandler calc = result.getCalc();
        assertEquals(calc.getClass(), TaggedSparseMatrix.IntegerArithmeticHandler.class);
        
        noEntryVal = 2.0F;
        result = TaggedSparseMatrix.constructMatrix(noEntryVal);
        calc = result.getCalc();
        assertEquals(calc.getClass(), SparseMatrix.FloatArithmeticHandler.class);
        
        noEntryVal = null;
        result = TaggedSparseMatrix.constructMatrix(noEntryVal);
        assertEquals(result, null);
    }

    /**
     * Test of tagColumn method, of class TaggedSparseMatrix.
     */
    @Test
    public void testTagColumn() {
        System.out.println("tagColumn");
        final Number val = 4;
        final TaggedSparseMatrix instance = TaggedSparseMatrix.constructMatrix(val);
        instance.putCell(1, 2, val);
        instance.putCell(0, 4, 6);
        instance.putCell(3, 8, 9);
        final int key = 0;
        final boolean tag = true;
        instance.tagColumn(key, tag);
        final boolean result = instance.hasTag(key);
        assertEquals(result, tag);
    }

    /**
     * Test of getColumnsWithTag method, of class TaggedSparseMatrix.
     */
    @Test
    public void testGetColumnsWithTag() {
        System.out.println("getColumnsWithTag");
        final boolean tag = true;
        final Number val = 4;
        final TaggedSparseMatrix instance = TaggedSparseMatrix.constructMatrix(val);
        instance.putCell(1, 2, val);
        instance.putCell(0, 4, 6);
        instance.putCell(3, 8, 9);
        instance.tagColumn(0, tag);
        instance.tagColumn(1, tag);
        final Set result = instance.getColumnsWithTag(tag);
        assertEquals(result.size(), 2);
    }

    /**
     * Test of getLargestColumnSumWithTag method, of class TaggedSparseMatrix.
     */
    @Test
    public void testGetLargestColumnSumWithTag() {
        System.out.println("getLargestColumnSumWithTag");
        final boolean tag = false;
        final Number val = 4;
        final TaggedSparseMatrix instance = TaggedSparseMatrix.constructMatrix(val);
        instance.putCell(1, 2, val);
        instance.putCell(0, 4, 6);
        instance.putCell(3, 8, 9);
        instance.tagColumn(0, tag);
        instance.tagColumn(1, tag);
        final Number expResult = 10;
        final Number result = instance.getLargestColumnSumWithTag(tag);
        assertEquals(result, expResult);
    }


    /**
     * Test of isTag method, of class TaggedSparseMatrix.
     */
    @Test
    public void testIsTag() {
        System.out.println("isTag");
        final int key = 0;
        final Number val = 4;
        final TaggedSparseMatrix instance = TaggedSparseMatrix.constructMatrix(val);
        instance.putCell(1, 2, val);
        instance.putCell(0, 4, 6);
        instance.putCell(3, 8, 9);
        final boolean tag = false;
        instance.tagColumn(0, tag);
        instance.tagColumn(1, tag);
        boolean expResult = false;
        boolean result = instance.isTag(key);
        assertEquals(result, expResult);
    }

    /**
     * Test of removeColumn method, of class TaggedSparseMatrix.
     */
    @Test
    public void testRemoveColumn() {
        System.out.println("removeColumn");
        final int key = 0;
        final Number val = 4;
        final TaggedSparseMatrix instance = TaggedSparseMatrix.constructMatrix(val);
        instance.putCell(1, 2, val);
        instance.putCell(0, 4, 6);
        instance.putCell(3, 8, 9);
        final boolean tag = false;
        instance.tagColumn(0, tag);
        instance.tagColumn(1, tag);
        instance.removeColumn(key);
        
        assertFalse(instance.hasTag(key));
    }

    /**
     * Test of clearCell method, of class TaggedSparseMatrix.
     */
    @Test
    public void testClearCell() {
        System.out.println("clearCell");
        final int i = 0;
        final int j = 0;
        final Number val = 4;
        final TaggedSparseMatrix instance = TaggedSparseMatrix.constructMatrix(val);
        instance.putCell(1, 2, val);
        instance.putCell(0, 4, 6);
        instance.putCell(3, 8, 9);
        final boolean tag = false;
        instance.tagColumn(0, tag);
        instance.tagColumn(1, tag);
        instance.clearCell(i, j);
        
        assertTrue(instance.hasTag(i));
    }
}
