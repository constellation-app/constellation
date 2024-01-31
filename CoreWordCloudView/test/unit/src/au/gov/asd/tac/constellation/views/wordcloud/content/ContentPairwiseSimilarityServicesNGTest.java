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
package au.gov.asd.tac.constellation.views.wordcloud.content;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for ContentPairwiseSimilarityServices
 * 
 * @author Delphinus8821
 */
public class ContentPairwiseSimilarityServicesNGTest {
    
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
     * Test of clusterSimilarElements method, of class ContentPairwiseSimilarityServices.
     */
    @Test
    public void testClusterSimilarElements() {
        System.out.println("clusterSimilarElements");
        final Set<Integer> newSet = new HashSet<>();
        newSet.add(2);
        newSet.add(0);
        newSet.add(1);
        final PairwiseComparisonTokenHandler handler = new PairwiseComparisonTokenHandler(3, newSet);
        final NGramAnalysisParameters nGramParams = NGramAnalysisParameters.getDefaultParameters();
        final Map<Integer, Integer> expResult = new HashMap<>();
        final Map<Integer, Integer> result = ContentPairwiseSimilarityServices.clusterSimilarElements(handler, nGramParams);
        assertEquals(result, expResult);
    }

    /**
     * Test of scoreSimilarPairs method, of class ContentPairwiseSimilarityServices.
     */
    @Test
    public void testScoreSimilarPairs() {
        System.out.println("scoreSimilarPairs");
        final Set<Integer> newSet = new HashSet<>();
        newSet.add(2);
        newSet.add(0);
        newSet.add(1);
        final PairwiseComparisonTokenHandler handler = new PairwiseComparisonTokenHandler(3, newSet);
        final NGramAnalysisParameters nGramParams = NGramAnalysisParameters.getDefaultParameters();
        final List<ElementSimilarity> expResult = new LinkedList<>();
        final List<ElementSimilarity> result = ContentPairwiseSimilarityServices.scoreSimilarPairs(handler, nGramParams);
        assertEquals(result, expResult);
    }
    
}
