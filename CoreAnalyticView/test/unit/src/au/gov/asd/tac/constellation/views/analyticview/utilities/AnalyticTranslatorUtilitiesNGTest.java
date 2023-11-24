/*
 * Copyright 2010-2023 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.analyticview.utilities;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.util.HashMap;
import java.util.Map;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Delphinus8821
 */
public class AnalyticTranslatorUtilitiesNGTest {
    
    public AnalyticTranslatorUtilitiesNGTest() {
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
     * Test of addToVertexSizeCache method, of class AnalyticTranslatorUtilities.
     */
    @Test
    public void testAddToVertexSizeCache() {
        System.out.println("addToVertexSizeCache");
        final String currentGraphKey = "vertexSize";
        final Map<Integer, Float> vertexSizes = new HashMap<>();
        AnalyticTranslatorUtilities.addToVertexSizeCache(currentGraphKey, vertexSizes);
        final boolean contains = AnalyticTranslatorUtilities.getVertexSizeCache().containsKey(currentGraphKey);
        assertTrue(contains);
    }

    /**
     * Test of addToTransactionSizeCache method, of class AnalyticTranslatorUtilities.
     */
    @Test
    public void testAddToTransactionSizeCache() {
        System.out.println("addToTransactionSizeCache");
        final String currentGraphKey = "transactionSize";
        final Map<Integer, Float> transactionSizes = new HashMap<>();
        AnalyticTranslatorUtilities.addToTransactionSizeCache(currentGraphKey, transactionSizes);
        final boolean contains = AnalyticTranslatorUtilities.getTransactionSizeCache().containsKey(currentGraphKey);
        assertTrue(contains);
    }

    /**
     * Test of addToVertexColorCache method, of class AnalyticTranslatorUtilities.
     */
    @Test
    public void testAddToVertexColorCache() {
        System.out.println("addToVertexColorCache");
        final String currentGraphKey = "vertexColor";
        final Map<Integer, ConstellationColor> vertexColors = new HashMap<>();
        AnalyticTranslatorUtilities.addToVertexColorCache(currentGraphKey, vertexColors);
        final boolean contains = AnalyticTranslatorUtilities.getVertexColorCache().containsKey(currentGraphKey);
        assertTrue(contains);
    }

    /**
     * Test of addToTransactionColorCache method, of class AnalyticTranslatorUtilities.
     */
    @Test
    public void testAddToTransactionColorCache() {
        System.out.println("addToTransactionColorCache");
        final String currentGraphKey = "transactionColor";
        final Map<Integer, ConstellationColor> transactionColors = new HashMap<>();
        AnalyticTranslatorUtilities.addToTransactionColorCache(currentGraphKey, transactionColors);
        final boolean contains = AnalyticTranslatorUtilities.getTransactionColorCache().containsKey(currentGraphKey);
        assertTrue(contains);
    }
    
    /**
     * Test of addToVertexHideCache method, of class AnalyticTranslatorUtilities.
     */
    @Test
    public void testAddToVertexHideCache() {
        System.out.println("addToVertexHideCache");
        final String currentGraphKey = "vertexHide";
        final Map<Integer, Float> vertexHideValues = new HashMap<>();
        AnalyticTranslatorUtilities.addToVertexHideCache(currentGraphKey, vertexHideValues);
        final boolean contains = AnalyticTranslatorUtilities.getVertexHideCache().containsKey(currentGraphKey);
        assertTrue(contains);
    }

    /**
     * Test of addToTransactionHideCache method, of class AnalyticTranslatorUtilities.
     */
    @Test
    public void testAddToTransactionHideCache() {
        System.out.println("addToTransactionHideCache");
        final String currentGraphKey = "transactionHide";
        final Map<Integer, Float> transactionHideValues = new HashMap<>();
        AnalyticTranslatorUtilities.addToTransactionHideCache(currentGraphKey, transactionHideValues);
        final boolean contains = AnalyticTranslatorUtilities.getTransactionHideCache().containsKey(currentGraphKey);
        assertTrue(contains);
    }
}
