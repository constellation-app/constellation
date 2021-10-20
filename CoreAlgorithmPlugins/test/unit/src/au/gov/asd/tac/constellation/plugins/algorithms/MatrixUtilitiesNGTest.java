/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.algorithms;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import org.ejml.simple.SimpleMatrix;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author cygnus_x-1
 */
public class MatrixUtilitiesNGTest {

    private int vxId0, vxId1, vxId2, vxId3, vxId4;
    private int txId0, txId1, txId2, txId3, txId4;
    private StoreGraph graph;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // create an analytic graph
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);

        // add vertices
        vxId0 = graph.addVertex();
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();
        vxId3 = graph.addVertex();
        vxId4 = graph.addVertex();

        // add transactions
        txId0 = graph.addTransaction(vxId0, vxId1, false);
        txId1 = graph.addTransaction(vxId1, vxId2, false);
        txId2 = graph.addTransaction(vxId1, vxId3, false);
        txId3 = graph.addTransaction(vxId2, vxId3, false);
        txId4 = graph.addTransaction(vxId3, vxId4, false);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        graph = null;
    }

    /**
     * Test of getIdentityMatrix method, of class MatrixUtilities.
     */
    @Test
    public void testGetIdentityMatrix() {
        final SimpleMatrix result = MatrixUtilities.identity(graph);
        final SimpleMatrix expResult = SimpleMatrix.identity(5);
        assertTrue(isEqual(result, expResult, 1E-3));
    }

    /**
     * Test of adjacency method, of class MatrixUtilities when graph is empty.
     */
    @Test
    public void testAdjacencyEmptyGraph() {
        final SimpleMatrix expResult = new SimpleMatrix(0, 0);
        final SimpleMatrix result = MatrixUtilities.adjacency(new StoreGraph(), false);
        assertEquals(result.numRows(), expResult.numRows());
        assertEquals(result.numCols(), expResult.numCols());
        assertEquals(result.getNumElements(), 0);
    }
    
    /**
     * Test of adjacency method, of class MatrixUtilities.
     */
    @Test
    public void testAdjacency() {
        final boolean weighted = false;
        final double[][] expData = new double[5][5];
        expData[0][1] = 1.0;
        expData[1][0] = 1.0;
        expData[1][2] = 1.0;
        expData[1][3] = 1.0;
        expData[2][1] = 1.0;
        expData[2][3] = 1.0;
        expData[3][1] = 1.0;
        expData[3][2] = 1.0;
        expData[3][4] = 1.0;
        expData[4][3] = 1.0;
        final SimpleMatrix expResult = new SimpleMatrix(expData);
        final SimpleMatrix result = MatrixUtilities.adjacency(graph, weighted);
        assertTrue(isEqual(result, expResult, 1E-3));
    }

    /**
     * Test of getIncidenceMatrix method, of class MatrixUtilities.
     */
    @Test
    public void testGetIncidenceMatrix() {
        final boolean weighted = false;
        final double[][] expData = new double[5][5];
        expData[0][0] = 1.0;
        expData[1][0] = 1.0;
        expData[1][1] = 1.0;
        expData[1][2] = 1.0;
        expData[2][1] = 1.0;
        expData[2][3] = 1.0;
        expData[3][2] = 1.0;
        expData[3][3] = 1.0;
        expData[3][4] = 1.0;
        expData[4][4] = 1.0;
        final SimpleMatrix expResult = new SimpleMatrix(expData);
        final SimpleMatrix result = MatrixUtilities.incidence(graph, weighted);
        assertTrue(isEqual(result, expResult, 1E-3));
    }

    /**
     * Test of getDegreeMatrix method, of class MatrixUtilities.
     */
    @Test
    public void testGetDegreeMatrix() {
        final double[][] expData = new double[5][5];
        expData[0][0] = 1.0;
        expData[1][1] = 3.0;
        expData[2][2] = 2.0;
        expData[3][3] = 3.0;
        expData[4][4] = 1.0;
        final SimpleMatrix expResult = new SimpleMatrix(expData);
        final SimpleMatrix result = MatrixUtilities.degree(graph);
        assertTrue(isEqual(result, expResult, 1E-3));
    }

    /**
     * Test of getLaplacianMatrix method, of class MatrixUtilities.
     */
    @Test
    public void testGetLaplacianMatrix() {
        final double[][] expData = new double[5][5];
        expData[0][0] = 1.0;
        expData[0][1] = -1.0;
        expData[1][0] = -1.0;
        expData[1][1] = 3.0;
        expData[1][2] = -1.0;
        expData[1][3] = -1.0;
        expData[2][1] = -1.0;
        expData[2][2] = 2.0;
        expData[2][3] = -1.0;
        expData[3][1] = -1.0;
        expData[3][2] = -1.0;
        expData[3][3] = 3.0;
        expData[3][4] = -1.0;
        expData[4][3] = -1.0;
        expData[4][4] = 1.0;
        final SimpleMatrix expResult = new SimpleMatrix(expData);
        final SimpleMatrix result = MatrixUtilities.laplacian(graph);
        assertTrue(isEqual(result, expResult, 1E-3));
    }

    /**
     * Test of inverseLaplacian method, of class MatrixUtilities.
     */
    @Test
    public void testInverseLaplacianEmptyGraph() {
        final SimpleMatrix expResult = new SimpleMatrix(0, 0);
        final SimpleMatrix result = MatrixUtilities.inverseLaplacian(new StoreGraph());
        assertEquals(result.numRows(), expResult.numRows());
        assertEquals(result.numCols(), expResult.numCols());
        assertEquals(result.getNumElements(), 0);
    }
    
    /**
     * Test of inverseLaplacian method, of class MatrixUtilities.
     */
    @Test
    public void testInverseLaplacian() {
        final double[][] expData = new double[5][5];
        expData[0][0] = 0.867;
        expData[0][1] = 0.067;
        expData[0][2] = -0.2;
        expData[0][3] = -0.267;
        expData[0][4] = -0.467;
        expData[1][0] = 0.067;
        expData[1][1] = 0.267;
        expData[1][3] = -0.067;
        expData[1][4] = -0.267;
        expData[2][0] = -0.2;
        expData[2][2] = 0.4;
        expData[2][4] = -0.2;
        expData[3][0] = -0.267;
        expData[3][1] = -0.067;
        expData[3][3] = 0.267;
        expData[3][4] = 0.067;
        expData[4][0] = -0.467;
        expData[4][1] = -0.267;
        expData[4][2] = -0.2;
        expData[4][3] = 0.067;
        expData[4][4] = 0.867;
        final SimpleMatrix expResult = new SimpleMatrix(expData);
        final SimpleMatrix result = MatrixUtilities.inverseLaplacian(graph);
        assertTrue(isEqual(result, expResult, 1E-3));
    }

    private boolean isEqual(final SimpleMatrix one, final SimpleMatrix two, final double tolerance) {
        if (one.getNumElements() != two.getNumElements()
                || one.getMatrix().getNumRows() != two.getMatrix().getNumRows()
                || one.getMatrix().getNumCols() != two.getMatrix().getNumCols()) {
            return false;
        } else {
            final int m = one.getMatrix().getNumRows();
            final int n = one.getMatrix().getNumCols();
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    if (Math.abs(one.get(i, j)) - Math.abs(two.get(i, j)) > Math.abs(tolerance)) {
                        return false;
                    }
                }
            }
            return true;
        }
    }
}
