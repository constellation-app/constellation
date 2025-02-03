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

package au.gov.asd.tac.constellation.views.histogram.bins;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.attribute.DoubleObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.FloatObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.LongObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.views.histogram.bins.edgeaggregates.EdgeAverageTransactionDoubleAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.edgeaggregates.EdgeAverageTransactionFloatAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.edgeaggregates.EdgeMaxTransactionDoubleAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.edgeaggregates.EdgeMaxTransactionFloatAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.edgeaggregates.EdgeMaxTransactionLongAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.edgeaggregates.EdgeMinTransactionDoubleAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.edgeaggregates.EdgeMinTransactionFloatAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.edgeaggregates.EdgeMinTransactionLongAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.edgeaggregates.EdgeSumTransactionDoubleAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.edgeaggregates.EdgeSumTransactionFloatAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.edgeaggregates.EdgeSumTransactionLongAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.linkaggregates.LinkAverageTransactionDoubleAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.linkaggregates.LinkMaxTransactionDoubleAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.linkaggregates.LinkMinTransactionDoubleAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.linkaggregates.LinkSumTransactionDoubleAttributeBin;
import org.openide.util.Exceptions;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author OrionsGuardian
 */
public class TransactionAggregateBinsNGTest {
    
    private int testLongAttrId;
    private int testDoubleAttrId;
    private int testFloatAttrId;
    
    private int vxId1;
    private int vxId2;
    private int vxId3;
    private int vxId4;
    
    private int txId1;
    private int txId2;
    private int txId3;
    private int txId3Xa;
    private int txId4a;
    private int txId4b;
    
    private Graph graph;

    public TransactionAggregateBinsNGTest() {
    }

    @BeforeClass
    public void setUpClass() throws Exception {
        setupGraph();
    }

    @AfterClass
    public void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of Transaction Edge Aggregate Bins for DOUBLE datatype.
     */
    @Test
    public void testEdgeDoubleAggregates() {
        
        try {
            System.out.println("START EDGE DOUBLE TEST");
            
            EdgeMaxTransactionDoubleAttributeBin edge3Max = new EdgeMaxTransactionDoubleAttributeBin();
            EdgeMinTransactionDoubleAttributeBin edge4Min = new EdgeMinTransactionDoubleAttributeBin();
            
            EdgeAverageTransactionDoubleAttributeBin edge3Average = new EdgeAverageTransactionDoubleAttributeBin();
            EdgeAverageTransactionDoubleAttributeBin edge3XAverage = new EdgeAverageTransactionDoubleAttributeBin();
            EdgeAverageTransactionDoubleAttributeBin edge4aAverage = new EdgeAverageTransactionDoubleAttributeBin();
            EdgeAverageTransactionDoubleAttributeBin edge4bAverage = new EdgeAverageTransactionDoubleAttributeBin();

            EdgeSumTransactionDoubleAttributeBin edge3Sum = new EdgeSumTransactionDoubleAttributeBin();
            EdgeSumTransactionDoubleAttributeBin edge3XSum = new EdgeSumTransactionDoubleAttributeBin();
            EdgeSumTransactionDoubleAttributeBin edge4aSum = new EdgeSumTransactionDoubleAttributeBin();
            EdgeSumTransactionDoubleAttributeBin edge4bSum = new EdgeSumTransactionDoubleAttributeBin();
            
            WritableGraph wg = graph.getWritableGraph("", true);
            
            edge3Max.setKey(wg, testDoubleAttrId, wg.getTransactionEdge(txId3Xa));
            edge4Min.setKey(wg, testDoubleAttrId, wg.getTransactionEdge(txId4a));
            
            double result3Max = (Double) edge3Max.getKeyAsObject();
            double result4Min = (Double) edge4Min.getKeyAsObject();
            
            edge3Average.setKey(wg, testDoubleAttrId, wg.getTransactionEdge(txId3));
            edge3XAverage.setKey(wg, testDoubleAttrId, wg.getTransactionEdge(txId3Xa));
            edge4aAverage.setKey(wg, testDoubleAttrId, wg.getTransactionEdge(txId4a));
            edge4bAverage.setKey(wg, testDoubleAttrId, wg.getTransactionEdge(txId4b));
            double result3 = (Double) edge3Average.getKeyAsObject();
            double result3X = (Double) edge3XAverage.getKeyAsObject();
            double result4a = (Double) edge4aAverage.getKeyAsObject();
            double result4b = (Double) edge4bAverage.getKeyAsObject();
            
            edge3Sum.setKey(wg, testDoubleAttrId, wg.getTransactionEdge(txId3));
            edge3XSum.setKey(wg, testDoubleAttrId, wg.getTransactionEdge(txId3Xa));
            edge4aSum.setKey(wg, testDoubleAttrId, wg.getTransactionEdge(txId4a));
            edge4bSum.setKey(wg, testDoubleAttrId, wg.getTransactionEdge(txId4b));
            double sresult3 = (Double) edge3Sum.getKeyAsObject();
            double sresult3X = (Double) edge3XSum.getKeyAsObject();
            double sresult4a = (Double) edge4aSum.getKeyAsObject();
            double sresult4b = (Double) edge4bSum.getKeyAsObject();
            
            System.out.println("Avgs " + result3 + " , " + result3X + " , [" + result4a + " , " + result4b + "]");
            System.out.println("Min/Max " + result4Min + " , " + result3Max);
            System.out.println("Sums " + sresult3 + " , " + sresult3X + " , [" + sresult4a + "{" + edge4aSum.isAllElementsAreNull() + "}" + " , " + sresult4b + "{" + edge4bSum.isAllElementsAreNull() + "}" + "]");
            wg.commit();
            assertEquals(190.0D, result3);
            assertEquals(20.0D, result4a);
            assertEquals(240.0D, sresult3X);
            assertTrue(edge4bSum.isAllElementsAreNull());
            
            System.out.println("END EDGE DOUBLE TEST");
            
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Test of Transaction Edge Aggregate Bins for DOUBLE datatype.
     */
    @Test
    public void testLinkDoubleAggregates() {
        
        try {
            System.out.println("START LINK DOUBLE TEST");
            
            LinkMaxTransactionDoubleAttributeBin link3Max = new LinkMaxTransactionDoubleAttributeBin();
            LinkMinTransactionDoubleAttributeBin link4Min = new LinkMinTransactionDoubleAttributeBin();
            
            LinkAverageTransactionDoubleAttributeBin link3Average = new LinkAverageTransactionDoubleAttributeBin();
            LinkAverageTransactionDoubleAttributeBin link3XAverage = new LinkAverageTransactionDoubleAttributeBin();
            LinkAverageTransactionDoubleAttributeBin link4aAverage = new LinkAverageTransactionDoubleAttributeBin();
            LinkAverageTransactionDoubleAttributeBin link4bAverage = new LinkAverageTransactionDoubleAttributeBin();

            LinkSumTransactionDoubleAttributeBin link3Sum = new LinkSumTransactionDoubleAttributeBin();
            LinkSumTransactionDoubleAttributeBin link3XSum = new LinkSumTransactionDoubleAttributeBin();
            LinkSumTransactionDoubleAttributeBin link4aSum = new LinkSumTransactionDoubleAttributeBin();
            LinkSumTransactionDoubleAttributeBin link4bSum = new LinkSumTransactionDoubleAttributeBin();
            
            WritableGraph wg = graph.getWritableGraph("", true);
            
            link3Max.setKey(wg, testDoubleAttrId, wg.getTransactionLink(txId3Xa));
            link4Min.setKey(wg, testDoubleAttrId, wg.getTransactionLink(txId4a));
            
            double result3Max = (Double) link3Max.getKeyAsObject();
            double result4Min = (Double) link4Min.getKeyAsObject();
            
            link3Average.setKey(wg, testDoubleAttrId, wg.getTransactionLink(txId3));
            link3XAverage.setKey(wg, testDoubleAttrId, wg.getTransactionLink(txId3Xa));
            link4aAverage.setKey(wg, testDoubleAttrId, wg.getTransactionLink(txId4a));
            link4bAverage.setKey(wg, testDoubleAttrId, wg.getTransactionLink(txId4b));
            double result3 = (Double) link3Average.getKeyAsObject();
            double result3X = (Double) link3XAverage.getKeyAsObject();
            double result4a = (Double) link4aAverage.getKeyAsObject();
            double result4b = (Double) link4bAverage.getKeyAsObject();
            
            link3Sum.setKey(wg, testDoubleAttrId, wg.getTransactionLink(txId3));
            link3XSum.setKey(wg, testDoubleAttrId, wg.getTransactionLink(txId3Xa));
            link4aSum.setKey(wg, testDoubleAttrId, wg.getTransactionLink(txId4a));
            link4bSum.setKey(wg, testDoubleAttrId, wg.getTransactionLink(txId4b));
            double sresult3 = (Double) link3Sum.getKeyAsObject();
            double sresult3X = (Double) link3XSum.getKeyAsObject();
            double sresult4a = (Double) link4aSum.getKeyAsObject();
            double sresult4b = (Double) link4bSum.getKeyAsObject();
            
            System.out.println("Avgs " + result3 + " , " + result3X + " , [" + result4a + " , " + result4b + "]");
            System.out.println("Min/Max " + result4Min + " , " + result3Max);
            System.out.println("Sums " + sresult3 + " , " + sresult3X + " , [" + sresult4a + "{" + link4aSum.isAllElementsAreNull() + "}" + " , " + sresult4b + "{" + link4bSum.isAllElementsAreNull() + "}" + "]");
            wg.commit();
            assertEquals(result3, 162.0D);
            assertEquals(result4Min, 5.0D);
            assertEquals(result3Max, 330.0D);
            assertFalse(link4bSum.isAllElementsAreNull());
            
            System.out.println("END LINK DOUBLE TEST");
            
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Test of Transaction Edge Aggregate Bins for FLOAT datatype.
     */
    @Test
    public void testEdgeFloatAggregates() {
        
        try {
            System.out.println("START EDGE FLOAT TEST");
            
            EdgeMaxTransactionFloatAttributeBin edge1Max = new EdgeMaxTransactionFloatAttributeBin();
            EdgeMinTransactionFloatAttributeBin edge1Min = new EdgeMinTransactionFloatAttributeBin();
            
            EdgeAverageTransactionFloatAttributeBin edge1Average = new EdgeAverageTransactionFloatAttributeBin();
            EdgeSumTransactionFloatAttributeBin edge1Sum = new EdgeSumTransactionFloatAttributeBin();
            
            WritableGraph wg = graph.getWritableGraph("", true);
            
            edge1Max.setKey(wg, testFloatAttrId, wg.getTransactionEdge(txId1));
            edge1Min.setKey(wg, testFloatAttrId, wg.getTransactionEdge(txId1));
            
            float result1Max = (Float) edge1Max.getKeyAsObject();
            float result1Min = (Float) edge1Min.getKeyAsObject();
            
            edge1Average.setKey(wg, testFloatAttrId, wg.getTransactionEdge(txId1));
            float result1Avg = (Float) edge1Average.getKeyAsObject();
            
            edge1Sum.setKey(wg, testFloatAttrId, wg.getTransactionEdge(txId1));
            float result1Sum = (Float) edge1Sum.getKeyAsObject();
            
            System.out.println("Avg " + result1Avg);
            System.out.println("Min/Max " + result1Min + " , " + result1Max);
            System.out.println("Sums " + result1Sum);
            wg.commit();
            assertEquals(215.0F, result1Avg);
            assertEquals(100.0F, result1Min);
            assertEquals(330.0F, result1Max);
            assertEquals(430.0F, result1Sum);
            
            System.out.println("END EDGE FLOAT TEST");
            
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Test of Transaction Edge Aggregate Bins for LONG datatype.
     */
    @Test
    public void testEdgeLongAggregates() {
        
        try {
            System.out.println("START EDGE LONG TEST");
            
            EdgeMaxTransactionLongAttributeBin edge2Max = new EdgeMaxTransactionLongAttributeBin();
            EdgeMinTransactionLongAttributeBin edge2Min = new EdgeMinTransactionLongAttributeBin();
            EdgeSumTransactionLongAttributeBin edge2Sum = new EdgeSumTransactionLongAttributeBin();
            
            WritableGraph wg = graph.getWritableGraph("", true);
            
            edge2Max.setKey(wg, testLongAttrId, wg.getTransactionEdge(txId2));
            edge2Min.setKey(wg, testLongAttrId, wg.getTransactionEdge(txId2));
            
            long result2Max = (Long) edge2Max.getKeyAsObject();
            long result2Min = (Long) edge2Min.getKeyAsObject();
            
            edge2Sum.setKey(wg, testLongAttrId, wg.getTransactionEdge(txId2));
            long result2Sum = (Long) edge2Sum.getKeyAsObject();
            
            System.out.println("Min/Max " + result2Min + " , " + result2Max);
            System.out.println("Sum " + result2Sum);
            wg.commit();
            assertEquals(100, result2Min);
            assertEquals(330, result2Max);
            assertEquals(630, result2Sum);
            
            System.out.println("END EDGE LONG TEST");
            
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Set up a graph with four vertices and three transactions
     */
    private void setupGraph() throws InterruptedException {
        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
        
        WritableGraph wg = graph.getWritableGraph("", true);
        try {    
            // add custom attributes

            final SchemaAttribute LONG_TEST_VAL = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, LongObjectAttributeDescription.ATTRIBUTE_NAME, "long_test_val")
                .setDescription("Long or Null value")
                .setDefaultValue(null)
                .create()
                .build();
            
            testLongAttrId = LONG_TEST_VAL.ensure(wg);
            
            final SchemaAttribute DOUBLE_TEST_VAL = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, DoubleObjectAttributeDescription.ATTRIBUTE_NAME, "long_test_val")
                .setDescription("Double or Null value")
                .setDefaultValue(null)
                .create()
                .build();
            
            testDoubleAttrId = DOUBLE_TEST_VAL.ensure(wg);

            final SchemaAttribute FLOAT_TEST_VAL = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, FloatObjectAttributeDescription.ATTRIBUTE_NAME, "long_test_val")
                .setDescription("Float or Null value")
                .setDefaultValue(null)
                .create()
                .build();
            
            testFloatAttrId = FLOAT_TEST_VAL.ensure(wg);

            // add vertices
            vxId1 = wg.addVertex();
            vxId2 = wg.addVertex();
            vxId3 = wg.addVertex();
            vxId4 = wg.addVertex();
            
            // Adding multiple Transactions
            txId1 = wg.addTransaction(vxId1, vxId2, true);
            int txId1b = wg.addTransaction(vxId1, vxId2, true);
            int txId1c = wg.addTransaction(vxId1, vxId2, true);

            txId2 = wg.addTransaction(vxId2, vxId3, true);
            int txId2b = wg.addTransaction(vxId2, vxId3, true);
            int txId2c = wg.addTransaction(vxId2, vxId3, true);
            int txId2d = wg.addTransaction(vxId2, vxId3, true);

            txId3 = wg.addTransaction(vxId3, vxId4, true);
            int txId3b = wg.addTransaction(vxId3, vxId4, true);
            int txId3c = wg.addTransaction(vxId3, vxId4, true);
            int txId3d = wg.addTransaction(vxId3, vxId4, true);
            int txId3e = wg.addTransaction(vxId3, vxId4, true);

            txId3Xa = wg.addTransaction(vxId4, vxId3, true);
            int txId3Xb = wg.addTransaction(vxId4, vxId3, true);
            int txId3Xc = wg.addTransaction(vxId4, vxId3, true);
            int txId3Xd = wg.addTransaction(vxId4, vxId3, true);
            int txId3Xe = wg.addTransaction(vxId4, vxId3, true);

            txId4a = wg.addTransaction(vxId2, vxId4, true);
            txId4b = wg.addTransaction(vxId4, vxId2, true);
            int txId4c = wg.addTransaction(vxId2, vxId4, true);
            int txId4d = wg.addTransaction(vxId4, vxId2, true);
            int txId4e = wg.addTransaction(vxId2, vxId4, true);

            // Add data to custom attributes
            wg.setObjectValue(testFloatAttrId, txId1, 100.0F);            
            wg.setObjectValue(testFloatAttrId, txId1b, null);
            wg.setObjectValue(testFloatAttrId, txId1c, 330.0F);
            
            wg.setObjectValue(testLongAttrId, txId2, 100L);
            wg.setObjectValue(testLongAttrId, txId2b, 200L);
            wg.setObjectValue(testLongAttrId, txId2c, 330L);
            wg.setObjectValue(testLongAttrId, txId2d, null);

            wg.setObjectValue(testDoubleAttrId, txId3, null);
            wg.setObjectValue(testDoubleAttrId, txId3b, 200.0D);
            wg.setObjectValue(testDoubleAttrId, txId3c, 330.0D);
            wg.setObjectValue(testDoubleAttrId, txId3d, null);
            wg.setObjectValue(testDoubleAttrId, txId3e, 40.0D);

            wg.setObjectValue(testDoubleAttrId, txId3Xa, null);
            wg.setObjectValue(testDoubleAttrId, txId3Xb, 200.0D);
            wg.setObjectValue(testDoubleAttrId, txId3Xc, null);
            wg.setObjectValue(testDoubleAttrId, txId3Xd, null);
            wg.setObjectValue(testDoubleAttrId, txId3Xe, 40.0D);

            wg.setObjectValue(testDoubleAttrId, txId4a, 5.0D);
            wg.setObjectValue(testDoubleAttrId, txId4b, null);
            wg.setObjectValue(testDoubleAttrId, txId4c, 10.0D);
            wg.setObjectValue(testDoubleAttrId, txId4d, null);
            wg.setObjectValue(testDoubleAttrId, txId4e, 45.0D);

        } finally {
            wg.commit();
        }
    }
    
}
