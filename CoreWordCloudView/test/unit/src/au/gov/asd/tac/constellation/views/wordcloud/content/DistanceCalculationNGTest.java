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

import au.gov.asd.tac.constellation.views.wordcloud.content.DistanceCalculation.CosineSimilarityCalculator;
import au.gov.asd.tac.constellation.views.wordcloud.content.DistanceCalculation.ElementSimilarityCalculator;
import au.gov.asd.tac.constellation.views.wordcloud.content.DistanceCalculation.IntegerSpaceDotProductCalculator;
import au.gov.asd.tac.constellation.views.wordcloud.content.DistanceCalculation.SimilarityAdjustmentCalculator;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for DistanceCalculation
 * 
 * @author Delphinus8821
 */
public class DistanceCalculationNGTest {

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

    @Test
    public void testIntegerSpaceDotProductCalculator() {
        final DistanceCalculation distanceCalculation = new DistanceCalculation();
        final ElementSimilarityCalculator calculator = distanceCalculation.new IntegerSpaceDotProductCalculator();
        int result = calculator.getSimilarity(1, 5, 7);
        int expected = 36;
        assertEquals(result, expected);
        
        result = calculator.getModulus(4, 1);
        expected = 5;
        assertEquals(result, expected);
    }
    
    @Test
    public void testBinarySpaceDotProductCalculator() {
        final DistanceCalculation distanceCalculation = new DistanceCalculation();
        final ElementSimilarityCalculator calculator = distanceCalculation.new BinarySpaceDotProductCalculator();
        int result = calculator.getSimilarity(2, 8, 1);
        int expected = 3;
        assertEquals(result, expected);
        
        result = calculator.getModulus(2, 4);
        expected = 3;
        assertEquals(result, expected);
    }
    
    @Test
    public void testCosineSimilarityCalculator() {
        final DistanceCalculation distanceCalculation = new DistanceCalculation();
        final SimilarityAdjustmentCalculator calculator = distanceCalculation.new CosineSimilarityCalculator(2);
        final int result = calculator.getAdjustedSimilarity(2, 6, 4);
        final int expected = 41;
        assertEquals(result, expected);
    }  
}
