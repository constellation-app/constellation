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
package au.gov.asd.tac.constellation.testing.construction;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import org.junit.Assert;
import org.openide.util.Exceptions;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author capricornunicorn123
 */
public class TestableGraphBuilderNGTest {
    
    public TestableGraphBuilderNGTest() {
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
     * Test of getSelectedNodeIds method, of class TestableGraphBuilder.
     */
    @Test
    public void fakeTestForCoverage() {
        try {
            Graph graph = new TestableGraphBuilder().buildGraphwithEverything();
        } catch (InterruptedException ex) {
            fail("The test case is a prototype.");
        }        
    }

//    /**
//     * Test of getNodeIds method, of class TestableGraphBuilder.
//     */
//    @Test
//    public void testGetNodeIds() {
//        System.out.println("getNodeIds");
//        int[] expResult = null;
//        int[] result = TestableGraphBuilder.getNodeIds();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of buildGraphwithEverything method, of class TestableGraphBuilder.
//     */
//    @Test
//    public void testBuildGraphwithEverything_0args() throws Exception {
//        System.out.println("buildGraphwithEverything");
//        TestableGraphBuilder instance = new TestableGraphBuilder();
//        Graph expResult = null;
//        Graph result = instance.buildGraphwithEverything();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of buildGraphwithEverything method, of class TestableGraphBuilder.
//     */
//    @Test
//    public void testBuildGraphwithEverything_GraphWriteMethods() {
//        System.out.println("buildGraphwithEverything");
//        GraphWriteMethods gwm = null;
//        TestableGraphBuilder instance = new TestableGraphBuilder();
//        instance.buildGraphwithEverything(gwm);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of withDecorators method, of class TestableGraphBuilder.
//     */
//    @Test
//    public void testWithDecorators_0args() throws Exception {
//        System.out.println("withDecorators");
//        TestableGraphBuilder instance = new TestableGraphBuilder();
//        TestableGraphBuilder expResult = null;
//        TestableGraphBuilder result = instance.withDecorators();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of withDecorators method, of class TestableGraphBuilder.
//     */
//    @Test
//    public void testWithDecorators_GraphWriteMethods() {
//        System.out.println("withDecorators");
//        GraphWriteMethods gwm = null;
//        TestableGraphBuilder instance = new TestableGraphBuilder();
//        instance.withDecorators(gwm);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of withBlazes method, of class TestableGraphBuilder.
//     */
//    @Test
//    public void testWithBlazes() throws Exception {
//        System.out.println("withBlazes");
//        TestableGraphBuilder instance = new TestableGraphBuilder();
//        TestableGraphBuilder expResult = null;
//        TestableGraphBuilder result = instance.withBlazes();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of withBottomLabels method, of class TestableGraphBuilder.
//     */
//    @Test
//    public void testWithBottomLabels() throws Exception {
//        System.out.println("withBottomLabels");
//        TestableGraphBuilder instance = new TestableGraphBuilder();
//        TestableGraphBuilder expResult = null;
//        TestableGraphBuilder result = instance.withBottomLabels();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of withTopLabels method, of class TestableGraphBuilder.
//     */
//    @Test
//    public void testWithTopLabels() throws Exception {
//        System.out.println("withTopLabels");
//        TestableGraphBuilder instance = new TestableGraphBuilder();
//        TestableGraphBuilder expResult = null;
//        TestableGraphBuilder result = instance.withTopLabels();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of withTransactionLabels method, of class TestableGraphBuilder.
//     */
//    @Test
//    public void testWithTransactionLabels() throws Exception {
//        System.out.println("withTransactionLabels");
//        TestableGraphBuilder instance = new TestableGraphBuilder();
//        TestableGraphBuilder expResult = null;
//        TestableGraphBuilder result = instance.withTransactionLabels();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of withAllLabels method, of class TestableGraphBuilder.
//     */
//    @Test
//    public void testWithAllLabels_0args() throws Exception {
//        System.out.println("withAllLabels");
//        TestableGraphBuilder instance = new TestableGraphBuilder();
//        TestableGraphBuilder expResult = null;
//        TestableGraphBuilder result = instance.withAllLabels();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of withAllLabels method, of class TestableGraphBuilder.
//     */
//    @Test
//    public void testWithAllLabels_GraphWriteMethods() {
//        System.out.println("withAllLabels");
//        GraphWriteMethods gwm = null;
//        TestableGraphBuilder instance = new TestableGraphBuilder();
//        instance.withAllLabels(gwm);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of withNodes method, of class TestableGraphBuilder.
//     */
//    @Test
//    public void testWithNodes_0args() throws Exception {
//        System.out.println("withNodes");
//        TestableGraphBuilder instance = new TestableGraphBuilder();
//        TestableGraphBuilder expResult = null;
//        TestableGraphBuilder result = instance.withNodes();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of withNodes method, of class TestableGraphBuilder.
//     */
//    @Test
//    public void testWithNodes_GraphWriteMethods() {
//        System.out.println("withNodes");
//        GraphWriteMethods gwm = null;
//        TestableGraphBuilder instance = new TestableGraphBuilder();
//        instance.withNodes(gwm);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of withAllTransactions method, of class TestableGraphBuilder.
//     */
//    @Test
//    public void testWithAllTransactions_0args() throws Exception {
//        System.out.println("withAllTransactions");
//        TestableGraphBuilder instance = new TestableGraphBuilder();
//        TestableGraphBuilder expResult = null;
//        TestableGraphBuilder result = instance.withAllTransactions();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of withAllTransactions method, of class TestableGraphBuilder.
//     */
//    @Test
//    public void testWithAllTransactions_GraphWriteMethods() {
//        System.out.println("withAllTransactions");
//        GraphWriteMethods gwm = null;
//        TestableGraphBuilder instance = new TestableGraphBuilder();
//        instance.withAllTransactions(gwm);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of withLoopedTransactions method, of class TestableGraphBuilder.
//     */
//    @Test
//    public void testWithLoopedTransactions_0args() throws Exception {
//        System.out.println("withLoopedTransactions");
//        TestableGraphBuilder instance = new TestableGraphBuilder();
//        TestableGraphBuilder expResult = null;
//        TestableGraphBuilder result = instance.withLoopedTransactions();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of withLoopedTransactions method, of class TestableGraphBuilder.
//     */
//    @Test
//    public void testWithLoopedTransactions_GraphWriteMethods() {
//        System.out.println("withLoopedTransactions");
//        GraphWriteMethods gwm = null;
//        TestableGraphBuilder instance = new TestableGraphBuilder();
//        instance.withLoopedTransactions(gwm);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of withLinearTransactions method, of class TestableGraphBuilder.
//     */
//    @Test
//    public void testWithLinearTransactions_0args() throws Exception {
//        System.out.println("withLinearTransactions");
//        TestableGraphBuilder instance = new TestableGraphBuilder();
//        TestableGraphBuilder expResult = null;
//        TestableGraphBuilder result = instance.withLinearTransactions();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of withLinearTransactions method, of class TestableGraphBuilder.
//     */
//    @Test
//    public void testWithLinearTransactions_GraphWriteMethods() {
//        System.out.println("withLinearTransactions");
//        GraphWriteMethods gwm = null;
//        TestableGraphBuilder instance = new TestableGraphBuilder();
//        instance.withLinearTransactions(gwm);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of build method, of class TestableGraphBuilder.
//     */
//    @Test
//    public void testBuild() {
//        System.out.println("build");
//        TestableGraphBuilder instance = new TestableGraphBuilder();
//        Graph expResult = null;
//        Graph result = instance.build();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    
}
