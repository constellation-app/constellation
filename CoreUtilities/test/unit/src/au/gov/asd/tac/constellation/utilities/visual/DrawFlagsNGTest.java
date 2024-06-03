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

import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
public class DrawFlagsNGTest {
    
    public DrawFlagsNGTest() {
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
     * Test of drawNodes method, of class DrawFlags.
     */
    @Test
    public void testDrawNodes() {
        System.out.println("drawNodes");
        // 0000 & 0001 = 0000 (false)
        // 0001 & 0001 = 0001 (true)
        // 0010 & 0001 = 0000 (false)
        // 0011 & 0001 = 0001 (true)
        
        final DrawFlags instance0 = new DrawFlags(0);
        final DrawFlags instance1 = new DrawFlags(1);
        final DrawFlags instance2 = new DrawFlags(2);
        final DrawFlags instance3 = new DrawFlags(3);
        
        assertFalse(instance0.drawNodes());
        assertTrue(instance1.drawNodes());
        assertFalse(instance2.drawNodes());
        assertTrue(instance3.drawNodes());
    }

    /**
     * Test of drawConnections method, of class DrawFlags.
     */
    @Test
    public void testDrawConnections() {
        System.out.println("drawConnections");
        // 0000 & 0010 = 0000 (false)
        // 0001 & 0010 = 0000 (false)
        // 0010 & 0010 = 0010 (true)
        // 0011 & 0010 = 0010 (true)
        
        final DrawFlags instance0 = new DrawFlags(0);
        final DrawFlags instance1 = new DrawFlags(1);
        final DrawFlags instance2 = new DrawFlags(2);
        final DrawFlags instance3 = new DrawFlags(3);
        
        assertFalse(instance0.drawConnections());
        assertFalse(instance1.drawConnections());
        assertTrue(instance2.drawConnections());
        assertTrue(instance3.drawConnections());
    }

    /**
     * Test of drawNodeLabels method, of class DrawFlags.
     */
    @Test
    public void testDrawNodeLabels() {
        System.out.println("drawNodeLabels");
        // 0000 & 0100 = 0000 (false)
        // 0001 & 0100 = 0000 (false)
        // 0010 & 0100 = 0000 (false)
        // 0011 & 0100 = 0000 (false)
        // 0100 & 0100 = 0100 (TRUE)
        
        final DrawFlags instance0 = new DrawFlags(0);
        final DrawFlags instance1 = new DrawFlags(1);
        final DrawFlags instance2 = new DrawFlags(2);
        final DrawFlags instance3 = new DrawFlags(3);
        final DrawFlags instance4 = new DrawFlags(4);
        
        assertFalse(instance0.drawNodeLabels());
        assertFalse(instance1.drawNodeLabels());
        assertFalse(instance2.drawNodeLabels());
        assertFalse(instance3.drawNodeLabels());
        assertTrue(instance4.drawNodeLabels());
    }

    /**
     * Test of drawConnectionLabels method, of class DrawFlags.
     */
    @Test
    public void testDrawConnectionLabels() {
        System.out.println("drawConnectionLabels");
        // 0000 & 1000 = 0000 (false)
        // 0001 & 1000 = 0000 (false)
        // 0011 & 1000 = 0000 (false)
        // 0100 & 1000 = 0000 (false)
        // 1000 & 1000 = 1000 (TRUE)
        
        final DrawFlags instance0 = new DrawFlags(0);
        final DrawFlags instance1 = new DrawFlags(1);
        final DrawFlags instance2 = new DrawFlags(2);
        final DrawFlags instance3 = new DrawFlags(4);
        final DrawFlags instance4 = new DrawFlags(8);
        
        assertFalse(instance0.drawConnectionLabels());
        assertFalse(instance1.drawConnectionLabels());
        assertFalse(instance2.drawConnectionLabels());
        assertFalse(instance3.drawConnectionLabels());
        assertTrue(instance4.drawConnectionLabels());
    }

    /**
     * Test of drawBlazes method, of class DrawFlags.
     */
    @Test
    public void testDrawBlazes() {
        System.out.println("drawBlazes");
        // 00000 & 10000 = 00000 (false)
        // 00010 & 10000 = 00000 (false)
        // 00100 & 10000 = 00000 (false)
        // 01000 & 10000 = 00000 (false)
        // 10000 & 10000 = 10000 (TRUE)
        
        final DrawFlags instance0 = new DrawFlags(0);
        final DrawFlags instance1 = new DrawFlags(2);
        final DrawFlags instance2 = new DrawFlags(4);
        final DrawFlags instance3 = new DrawFlags(8);
        final DrawFlags instance4 = new DrawFlags(16);
        
        assertFalse(instance0.drawBlazes());
        assertFalse(instance1.drawBlazes());
        assertFalse(instance2.drawBlazes());
        assertFalse(instance3.drawBlazes());
        assertTrue(instance4.drawBlazes());
    }

    /**
     * Test of drawAll method, of class DrawFlags.
     */
    @Test
    public void testDrawAll() {
        System.out.println("drawAll");
        for(int i=0;i<50; i++){
            final DrawFlags instance = new DrawFlags(i);
            assertEquals(instance.drawAll(), i==31);
        }
    }

    /**
     * Test of drawAny method, of class DrawFlags.
     */
    @Test
    public void testDrawAny() {
        System.out.println("drawAny");
        for(int i=0;i<50; i++){
            final DrawFlags instance = new DrawFlags(i);
            assertEquals(instance.drawAny(), i!=0);
        }
    }

    /**
     * Test of equals method, of class DrawFlags.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        
        final DrawFlags instance0 = new DrawFlags(0);
        final DrawFlags instance1 = new DrawFlags(2);
        final DrawFlags instance2 = new DrawFlags(4);
        final DrawFlags instance3 = new DrawFlags(8);
        final DrawFlags instance4 = new DrawFlags(16);
        
        final DrawFlags instance0Pair = new DrawFlags(0);
        final DrawFlags instance1Pair = new DrawFlags(2);
        final DrawFlags instance2Pair = new DrawFlags(4);
        final DrawFlags instance3Pair = new DrawFlags(8);
        final DrawFlags instance4Pair = new DrawFlags(16);
        
        assertFalse(instance0.equals(instance1Pair));
        assertFalse(instance0.equals(instance2Pair));
        assertFalse(instance0.equals(instance3Pair));
        assertFalse(instance0.equals(instance4Pair));
        
        assertTrue(instance0.equals(instance0Pair));
        assertTrue(instance1.equals(instance1Pair));
        assertTrue(instance2.equals(instance2Pair));
        assertTrue(instance3.equals(instance3Pair));
        assertTrue(instance4.equals(instance4Pair));
        
        assertFalse(instance0.equals(null));
        assertFalse(instance0.equals(new String("breaking string")));
    }

    /**
     * Test of hashCode method, of class DrawFlags.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        final DrawFlags instance0 = new DrawFlags(0);
        final DrawFlags instance1 = new DrawFlags(2);
        final DrawFlags instance2 = new DrawFlags(4);
        final DrawFlags instance3 = new DrawFlags(8);
        final DrawFlags instance4 = new DrawFlags(16);
        
        final DrawFlags instance0Pair = new DrawFlags(0);
        final DrawFlags instance1Pair = new DrawFlags(2);
        final DrawFlags instance2Pair = new DrawFlags(4);
        final DrawFlags instance3Pair = new DrawFlags(8);
        final DrawFlags instance4Pair = new DrawFlags(16);
        final DrawFlags instance5Pair = new DrawFlags(0);
        
        assertNotEquals(instance0.hashCode(), instance1Pair);
        assertNotEquals(instance0.hashCode(), instance2Pair);
        assertNotEquals(instance0.hashCode(), instance3Pair);
        assertNotEquals(instance0.hashCode(), instance4Pair);
        
        assertEquals(instance0.hashCode(), instance0Pair.hashCode());
        assertEquals(instance1.hashCode(), instance1Pair.hashCode());
        assertEquals(instance2.hashCode(), instance2Pair.hashCode());
        assertEquals(instance3.hashCode(), instance3Pair.hashCode());
        assertEquals(instance0.hashCode(), instance5Pair.hashCode());
        
        assertEquals(instance4.hashCode(), instance4Pair.hashCode());
    }
}
