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
import au.gov.asd.tac.constellation.views.histogram.AttributeType;
import au.gov.asd.tac.constellation.views.histogram.BinCreator;
import au.gov.asd.tac.constellation.views.histogram.DefaultBinCreatorProvider;
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
import au.gov.asd.tac.constellation.views.histogram.bins.linkaggregates.LinkAverageTransactionFloatAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.linkaggregates.LinkMaxTransactionDoubleAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.linkaggregates.LinkMaxTransactionFloatAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.linkaggregates.LinkMaxTransactionLongAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.linkaggregates.LinkMinTransactionDoubleAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.linkaggregates.LinkMinTransactionFloatAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.linkaggregates.LinkMinTransactionLongAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.linkaggregates.LinkSumTransactionDoubleAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.linkaggregates.LinkSumTransactionFloatAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.linkaggregates.LinkSumTransactionLongAttributeBin;
import java.util.Map;
import org.openide.util.Exceptions;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
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
    private int txId3a;
    private int txId3b;
    private int txId4a;
    private int txId4b;
    
    private Graph graph;

    public TransactionAggregateBinsNGTest() {
        // Intentionally Empty
    }

    @BeforeClass
    public void setUpClass() throws Exception {
        setupGraph();
    }

    @AfterClass
    public void tearDownClass() throws Exception {
        // Intentionally Empty
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Intentionally Empty
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Intentionally Empty
    }

    /**
     * Test registry
     */
    @Test
    public void testRegistry() {
        System.out.println("[TransactionAggregateBinsNGTest] START REGISTRY TEST");
        final DefaultBinCreatorProvider dbcp = new DefaultBinCreatorProvider();
        dbcp.register();
        // read registry and confirm some values        
        AttributeType att = AttributeType.EDGE_AVERAGE_TRANSACTION_ATTRIBUTE;
        Map<String, BinCreator> attMap = att.getBinCreators();
        System.out.println("EDGE_AVERAGE_TRANSACTION_ATTRIBUTE registered keys: " + attMap.keySet());
        assertTrue(attMap.keySet().contains("double_or_null"));
        
        att = AttributeType.LINK_SUM_TRANSACTION_ATTRIBUTE;
        attMap = att.getBinCreators();
        System.out.println("LINK_SUM_TRANSACTION_ATTRIBUTE registered keys: " + attMap.keySet());
        assertTrue(attMap.keySet().contains("double_or_null"));
        
        System.out.println("[TransactionAggregateBinsNGTest] END REGISTRY TEST");
    }
    
    /**
     * Test of Transaction Edge Aggregate Bins for DOUBLE datatype.
     */
    @Test
    public void testEdgeDoubleAggregates() {
        
        try {
            System.out.println("[TransactionAggregateBinsNGTest] START EDGE DOUBLE TEST");
            
            final EdgeMaxTransactionDoubleAttributeBin edge3Max = new EdgeMaxTransactionDoubleAttributeBin();
            final EdgeMinTransactionDoubleAttributeBin edge4Min = new EdgeMinTransactionDoubleAttributeBin();
            
            final EdgeAverageTransactionDoubleAttributeBin edge3aAverage = new EdgeAverageTransactionDoubleAttributeBin();
            final EdgeAverageTransactionDoubleAttributeBin edge3bAverage = (EdgeAverageTransactionDoubleAttributeBin) edge3aAverage.create();
            final EdgeAverageTransactionDoubleAttributeBin edge4aAverage = new EdgeAverageTransactionDoubleAttributeBin();
            final EdgeAverageTransactionDoubleAttributeBin edge4bAverage = new EdgeAverageTransactionDoubleAttributeBin();

            final EdgeSumTransactionDoubleAttributeBin edge3Sum = new EdgeSumTransactionDoubleAttributeBin();
            final EdgeSumTransactionDoubleAttributeBin edge3XSum = (EdgeSumTransactionDoubleAttributeBin) edge3Sum.create();
            final EdgeSumTransactionDoubleAttributeBin edge4aSum = new EdgeSumTransactionDoubleAttributeBin();
            final EdgeSumTransactionDoubleAttributeBin edge4bSum = new EdgeSumTransactionDoubleAttributeBin();
            
            final WritableGraph wg = graph.getWritableGraph("", true);
            
            edge3Max.setKey(wg, testDoubleAttrId, wg.getTransactionEdge(txId3b));
            edge4Min.setKey(wg, testDoubleAttrId, wg.getTransactionEdge(txId4a));
            
            final double result3Max = (Double) edge3Max.getKeyAsObject();
            final double result4Min = (Double) edge4Min.getKeyAsObject();
            
            edge3aAverage.setKey(wg, testDoubleAttrId, wg.getTransactionEdge(txId3a));
            edge3bAverage.setKey(wg, testDoubleAttrId, wg.getTransactionEdge(txId3b));
            edge4aAverage.setKey(wg, testDoubleAttrId, wg.getTransactionEdge(txId4a));
            edge4bAverage.setKey(wg, testDoubleAttrId, wg.getTransactionEdge(txId4b));
            final double result3a = (Double) edge3aAverage.getKeyAsObject();
            final double result3b = (Double) edge3bAverage.getKeyAsObject();
            final double result4a = (Double) edge4aAverage.getKeyAsObject();
            final double result4b = (Double) edge4bAverage.getKeyAsObject();
            
            edge3Sum.setKey(wg, testDoubleAttrId, wg.getTransactionEdge(txId3a));
            edge3XSum.setKey(wg, testDoubleAttrId, wg.getTransactionEdge(txId3b));
            edge4aSum.setKey(wg, testDoubleAttrId, wg.getTransactionEdge(txId4a));
            edge4bSum.setKey(wg, testDoubleAttrId, wg.getTransactionEdge(txId4b));
            final double sresult3 = (Double) edge3Sum.getKeyAsObject();
            final double sresult3X = (Double) edge3XSum.getKeyAsObject();
            final double sresult4a = (Double) edge4aSum.getKeyAsObject();
            final double sresult4b = (Double) edge4bSum.getKeyAsObject();
            
            System.out.println("Avgs " + result3a + " , " + result3b + " , [" + result4a + " , " + result4b + "]");
            System.out.println("Min/Max " + result4Min + " , " + result3Max);
            System.out.println("Sums " + sresult3 + " , " + sresult3X + " , [" + sresult4a + "{" + edge4aSum.isAllElementsAreNull() + "}" + " , " + sresult4b + "{" + edge4bSum.isAllElementsAreNull() + "}" + "]");
            wg.commit();
            assertEquals(190.0D, result3a);
            assertEquals(20.0D, result4a);
            assertEquals(240.0D, sresult3X);
            assertTrue(edge4bSum.isAllElementsAreNull());
            
            System.out.println("[TransactionAggregateBinsNGTest] END EDGE DOUBLE TEST");
            
        } catch (final InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Test of Transaction Edge Aggregate Bins for DOUBLE datatype.
     */
    @Test
    public void testLinkDoubleAggregates() {
        
        try {
            System.out.println("[TransactionAggregateBinsNGTest] START LINK DOUBLE TEST");
            
            final LinkMaxTransactionDoubleAttributeBin link3Max = new LinkMaxTransactionDoubleAttributeBin();
            final LinkMinTransactionDoubleAttributeBin link4aMin = new LinkMinTransactionDoubleAttributeBin();
            final LinkMinTransactionDoubleAttributeBin link4bMin = (LinkMinTransactionDoubleAttributeBin) link4aMin.create();
            
            final LinkAverageTransactionDoubleAttributeBin link3aAverage = new LinkAverageTransactionDoubleAttributeBin();
            final LinkAverageTransactionDoubleAttributeBin link3bAverage = (LinkAverageTransactionDoubleAttributeBin) link3aAverage.create();
            final LinkAverageTransactionDoubleAttributeBin link4aAverage = new LinkAverageTransactionDoubleAttributeBin();
            final LinkAverageTransactionDoubleAttributeBin link4bAverage = new LinkAverageTransactionDoubleAttributeBin();

            final LinkSumTransactionDoubleAttributeBin link3aSum = new LinkSumTransactionDoubleAttributeBin();
            final LinkSumTransactionDoubleAttributeBin link3bSum = (LinkSumTransactionDoubleAttributeBin) link3aSum.create();
            final LinkSumTransactionDoubleAttributeBin link4aSum = new LinkSumTransactionDoubleAttributeBin();
            final LinkSumTransactionDoubleAttributeBin link4bSum = new LinkSumTransactionDoubleAttributeBin();
            
            final WritableGraph wg = graph.getWritableGraph("", true);
            
            link3Max.setKey(wg, testDoubleAttrId, wg.getTransactionLink(txId3b));
            link4aMin.setKey(wg, testDoubleAttrId, wg.getTransactionLink(txId4a));
            link4bMin.setKey(wg, testDoubleAttrId, wg.getTransactionLink(txId4b));
            
            final double result3Max = (Double) link3Max.getKeyAsObject();
            final double result4Min = (Double) link4aMin.getKeyAsObject();
            
            link3aAverage.setKey(wg, testDoubleAttrId, wg.getTransactionLink(txId3a));
            link3bAverage.setKey(wg, testDoubleAttrId, wg.getTransactionLink(txId3b));
            link4aAverage.setKey(wg, testDoubleAttrId, wg.getTransactionLink(txId4a));
            link4bAverage.setKey(wg, testDoubleAttrId, wg.getTransactionLink(txId4b));
            final double result3aAvg = (Double) link3aAverage.getKeyAsObject();
            final double result3bAvg = (Double) link3bAverage.getKeyAsObject();
            final double result4aAvg = (Double) link4aAverage.getKeyAsObject();
            final double result4bAvg = (Double) link4bAverage.getKeyAsObject();
            
            link3aSum.setKey(wg, testDoubleAttrId, wg.getTransactionLink(txId3a));
            link3bSum.setKey(wg, testDoubleAttrId, wg.getTransactionLink(txId3b));
            link4aSum.setKey(wg, testDoubleAttrId, wg.getTransactionLink(txId4a));
            link4bSum.setKey(wg, testDoubleAttrId, wg.getTransactionLink(txId4b));
            final double result3aSum = (Double) link3aSum.getKeyAsObject();
            final double result3bSum = (Double) link3bSum.getKeyAsObject();
            final double result4aSum = (Double) link4aSum.getKeyAsObject();
            final double result4bSum = (Double) link4bSum.getKeyAsObject();
            
            System.out.println("Avgs " + result3aAvg + " , " + result3bAvg + " , [" + result4aAvg + " , " + result4bAvg + "]");
            System.out.println("Min/Max " + result4Min + " , " + result3Max);
            System.out.println("Sums " + result3aSum + " , " + result3bSum + " , [" + result4aSum + "{" + link4aSum.isAllElementsAreNull() + "}" + " , " + result4bSum + "{" + link4bSum.isAllElementsAreNull() + "}" + "]");
            wg.commit();
            assertEquals(result3aAvg, 162.0D);
            assertEquals(result4Min, 5.0D);
            assertEquals(result3Max, 330.0D);
            assertTrue(!link4bSum.isAllElementsAreNull());
            
            System.out.println("[TransactionAggregateBinsNGTest] END LINK DOUBLE TEST");
            
        } catch (final InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Test of Transaction Edge Aggregate Bins for FLOAT datatype.
     */
    @Test
    public void testEdgeFloatAggregates() {
        
        try {
            System.out.println("[TransactionAggregateBinsNGTest] START EDGE FLOAT TEST");
            
            final EdgeMaxTransactionFloatAttributeBin edge1Max = new EdgeMaxTransactionFloatAttributeBin();
            final EdgeMinTransactionFloatAttributeBin edge1Min = new EdgeMinTransactionFloatAttributeBin();
            
            final EdgeAverageTransactionFloatAttributeBin edge1Average = new EdgeAverageTransactionFloatAttributeBin();
            final EdgeSumTransactionFloatAttributeBin edge1Sum = new EdgeSumTransactionFloatAttributeBin();
            
            final WritableGraph wg = graph.getWritableGraph("", true);
            
            edge1Max.setKey(wg, testFloatAttrId, wg.getTransactionEdge(txId1));
            edge1Min.setKey(wg, testFloatAttrId, wg.getTransactionEdge(txId1));
            
            final float result1Max = (Float) edge1Max.getKeyAsObject();
            final float result1Min = (Float) edge1Min.getKeyAsObject();
            
            edge1Average.setKey(wg, testFloatAttrId, wg.getTransactionEdge(txId1));
            final float result1Avg = (Float) edge1Average.getKeyAsObject();
            
            edge1Sum.setKey(wg, testFloatAttrId, wg.getTransactionEdge(txId1));
            final float result1Sum = (Float) edge1Sum.getKeyAsObject();
            
            System.out.println("Avg " + result1Avg);
            System.out.println("Min/Max " + result1Min + " , " + result1Max);
            System.out.println("Sums " + result1Sum);
            wg.commit();
            assertEquals(215.0F, result1Avg);
            assertEquals(100.0F, result1Min);
            assertEquals(330.0F, result1Max);
            assertEquals(430.0F, result1Sum);
            
            System.out.println("[TransactionAggregateBinsNGTest] END EDGE FLOAT TEST");
            
        } catch (final InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Test of Transaction Link Aggregate Bins for FLOAT datatype.
     */
    @Test
    public void testLinkFloatAggregates() {
        
        try {
            System.out.println("[TransactionAggregateBinsNGTest] START LINK FLOAT TEST");
            
            final LinkMaxTransactionFloatAttributeBin link1Max = new LinkMaxTransactionFloatAttributeBin();
            final LinkMinTransactionFloatAttributeBin link1Min = new LinkMinTransactionFloatAttributeBin();
            
            final LinkAverageTransactionFloatAttributeBin link1Average = new LinkAverageTransactionFloatAttributeBin();
            final LinkSumTransactionFloatAttributeBin link1Sum = new LinkSumTransactionFloatAttributeBin();
            
            final WritableGraph wg = graph.getWritableGraph("", true);
            
            link1Max.setKey(wg, testFloatAttrId, wg.getTransactionLink(txId1));
            link1Min.setKey(wg, testFloatAttrId, wg.getTransactionLink(txId1));
            
            final float result1Max = (Float) link1Max.getKeyAsObject();
            final float result1Min = (Float) link1Min.getKeyAsObject();
            
            link1Average.setKey(wg, testFloatAttrId, wg.getTransactionLink(txId1));
            final float result1Avg = (Float) link1Average.getKeyAsObject();
            
            link1Sum.setKey(wg, testFloatAttrId, wg.getTransactionLink(txId1));
            final float result1Sum = (Float) link1Sum.getKeyAsObject();
            
            System.out.println("Avg " + result1Avg);
            System.out.println("Min/Max " + result1Min + " , " + result1Max);
            System.out.println("Sums " + result1Sum);
            wg.commit();
            assertEquals(215.0F, result1Avg);
            assertEquals(100.0F, result1Min);
            assertEquals(330.0F, result1Max);
            assertEquals(430.0F, result1Sum);
            
            System.out.println("[TransactionAggregateBinsNGTest] END LINK FLOAT TEST");
            
        } catch (final InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Test of Transaction Edge Aggregate Bins for LONG datatype.
     */
    @Test
    public void testEdgeLongAggregates() {
        
        try {
            System.out.println("[TransactionAggregateBinsNGTest] START EDGE LONG TEST");
            
            final EdgeMaxTransactionLongAttributeBin edge2Max = new EdgeMaxTransactionLongAttributeBin();
            final EdgeMinTransactionLongAttributeBin edge2Min = new EdgeMinTransactionLongAttributeBin();
            final EdgeSumTransactionLongAttributeBin edge2Sum = new EdgeSumTransactionLongAttributeBin();
            
            final WritableGraph wg = graph.getWritableGraph("", true);
            
            edge2Max.setKey(wg, testLongAttrId, wg.getTransactionEdge(txId2));
            edge2Min.setKey(wg, testLongAttrId, wg.getTransactionEdge(txId2));
            
            final long result2Max = (Long) edge2Max.getKeyAsObject();
            final long result2Min = (Long) edge2Min.getKeyAsObject();
            
            edge2Sum.setKey(wg, testLongAttrId, wg.getTransactionEdge(txId2));
            final long result2Sum = (Long) edge2Sum.getKeyAsObject();
            
            System.out.println("Min/Max " + result2Min + " , " + result2Max);
            System.out.println("Sum " + result2Sum);
            wg.commit();
            assertEquals(100, result2Min);
            assertEquals(330, result2Max);
            assertEquals(630, result2Sum);
            
            System.out.println("[TransactionAggregateBinsNGTest] END EDGE LONG TEST");
            
        } catch (final InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Test of Transaction Link Aggregate Bins for LONG datatype.
     */
    @Test
    public void testLinkLongAggregates() {
        
        try {
            System.out.println("[TransactionAggregateBinsNGTest] START LINK LONG TEST");
            
            final LinkMaxTransactionLongAttributeBin link2Max = new LinkMaxTransactionLongAttributeBin();
            final LinkMinTransactionLongAttributeBin link2Min = new LinkMinTransactionLongAttributeBin();
            final LinkSumTransactionLongAttributeBin link2Sum = new LinkSumTransactionLongAttributeBin();
            
            final WritableGraph wg = graph.getWritableGraph("", true);
            
            link2Max.setKey(wg, testLongAttrId, wg.getTransactionLink(txId2));
            link2Min.setKey(wg, testLongAttrId, wg.getTransactionLink(txId2));
            
            final long result2Max = (Long) link2Max.getKeyAsObject();
            final long result2Min = (Long) link2Min.getKeyAsObject();
            
            link2Sum.setKey(wg, testLongAttrId, wg.getTransactionLink(txId2));
            final long result2Sum = (Long) link2Sum.getKeyAsObject();
            
            System.out.println("Min/Max " + result2Min + " , " + result2Max);
            System.out.println("Sum " + result2Sum);
            wg.commit();
            assertEquals(100, result2Min);
            assertEquals(330, result2Max);
            assertEquals(630, result2Sum);
            
            System.out.println("[TransactionAggregateBinsNGTest] END LINK LONG TEST");
            
        } catch (final InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Set up a graph with four vertices and three transactions
     */
    private void setupGraph() throws InterruptedException {
        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
        
        final WritableGraph wg = graph.getWritableGraph("", true);
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
            final int txId1b = wg.addTransaction(vxId1, vxId2, true);
            final int txId1c = wg.addTransaction(vxId1, vxId2, true);

            txId2 = wg.addTransaction(vxId2, vxId3, true);
            final int txId2b = wg.addTransaction(vxId2, vxId3, true);
            final int txId2c = wg.addTransaction(vxId2, vxId3, true);
            final int txId2d = wg.addTransaction(vxId2, vxId3, true);

            txId3a = wg.addTransaction(vxId3, vxId4, true);
            final int txId3a2 = wg.addTransaction(vxId3, vxId4, true);
            final int txId3a3 = wg.addTransaction(vxId3, vxId4, true);
            final int txId3a4 = wg.addTransaction(vxId3, vxId4, true);
            final int txId3a5 = wg.addTransaction(vxId3, vxId4, true);

            txId3b = wg.addTransaction(vxId4, vxId3, true);
            final int txId3b2 = wg.addTransaction(vxId4, vxId3, true);
            final int txId3b3 = wg.addTransaction(vxId4, vxId3, true);
            final int txId3b4 = wg.addTransaction(vxId4, vxId3, true);
            final int txId3b5 = wg.addTransaction(vxId4, vxId3, true);

            txId4a = wg.addTransaction(vxId2, vxId4, true);
            txId4b = wg.addTransaction(vxId4, vxId2, true);
            final int txId4a2 = wg.addTransaction(vxId2, vxId4, true);
            final int txId4b2 = wg.addTransaction(vxId4, vxId2, true);
            final int txId4a3 = wg.addTransaction(vxId2, vxId4, true);

            // Add data to custom attributes
            wg.setObjectValue(testFloatAttrId, txId1, 100.0F);            
            wg.setObjectValue(testFloatAttrId, txId1b, null);
            wg.setObjectValue(testFloatAttrId, txId1c, 330.0F);
            
            wg.setObjectValue(testLongAttrId, txId2, 100L);
            wg.setObjectValue(testLongAttrId, txId2b, 200L);
            wg.setObjectValue(testLongAttrId, txId2c, 330L);
            wg.setObjectValue(testLongAttrId, txId2d, null);

            wg.setObjectValue(testDoubleAttrId, txId3a, null);
            wg.setObjectValue(testDoubleAttrId, txId3a2, 200.0D);
            wg.setObjectValue(testDoubleAttrId, txId3a3, 330.0D);
            wg.setObjectValue(testDoubleAttrId, txId3a4, null);
            wg.setObjectValue(testDoubleAttrId, txId3a5, 40.0D);

            wg.setObjectValue(testDoubleAttrId, txId3b, null);
            wg.setObjectValue(testDoubleAttrId, txId3b2, 200.0D);
            wg.setObjectValue(testDoubleAttrId, txId3b3, null);
            wg.setObjectValue(testDoubleAttrId, txId3b4, null);
            wg.setObjectValue(testDoubleAttrId, txId3b5, 40.0D);

            wg.setObjectValue(testDoubleAttrId, txId4a, 5.0D);
            wg.setObjectValue(testDoubleAttrId, txId4b, null);
            wg.setObjectValue(testDoubleAttrId, txId4a2, 10.0D);
            wg.setObjectValue(testDoubleAttrId, txId4b2, null);
            wg.setObjectValue(testDoubleAttrId, txId4a3, 45.0D);

        } finally {
            wg.commit();
        }
    }
    
}
