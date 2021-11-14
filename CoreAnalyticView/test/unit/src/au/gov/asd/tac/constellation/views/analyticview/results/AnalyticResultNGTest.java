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
package au.gov.asd.tac.constellation.views.analyticview.results;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.views.analyticview.results.ScoreResult.ElementScore;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author mimosa2
 */
public class AnalyticResultNGTest {

    public AnalyticResultNGTest() {
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
     * Test of sort method, of class AnalyticResult.
     */
    @Test
    public void testSort() {
        System.out.println("sort");

        LinkedHashMap<IdentificationData, ElementScore> testResult
                = new LinkedHashMap<>();

        AnalyticResultImpl instance = new AnalyticResultImpl();

//      Create unsorted ElementScore results
        final GraphElementType node = GraphElementType.getValue("Node");
        final IdentificationData idData1 = new IdentificationData(node, 1, "Node1");
        final IdentificationData idData2 = new IdentificationData(node, 2, "Node2");
        final IdentificationData idData3 = new IdentificationData(node, 3, "Node3");
        final IdentificationData idData4 = new IdentificationData(node, 4, "Node4");
        final IdentificationData idData5 = new IdentificationData(node, 5, "Node5");

        final Map<String, Float> namedScores1 = new HashMap<>();
        namedScores1.put("Centrality.OutBetweenness", (float) .8);
        final Map<String, Float> namedScores2 = new HashMap<>();
        namedScores2.put("Centrality.OutBetweenness", (float) 1.2);
        final Map<String, Float> namedScores3 = new HashMap<>();
        namedScores3.put("Centrality.OutBetweenness", (float) .7);
        final Map<String, Float> namedScores4 = new HashMap<>();
        namedScores4.put("Centrality.OutBetweenness", (float) 1.1);
        final Map<String, Float> namedScores5 = new HashMap<>();
        namedScores5.put("Centrality.OutBetweenness", (float) .6);

        final ElementScore elementScore1 = new ElementScore(node, 1, "Node1", false, namedScores1);
        final ElementScore elementScore2 = new ElementScore(node, 2, "Node1", false, namedScores2);
        final ElementScore elementScore3 = new ElementScore(node, 3, "Node1", false, namedScores3);
        final ElementScore elementScore4 = new ElementScore(node, 4, "Node1", false, namedScores4);
        final ElementScore elementScore5 = new ElementScore(node, 5, "Node1", false, namedScores5);

        instance.getResult().put(idData1, elementScore1);
        instance.getResult().put(idData2, elementScore2);
        instance.getResult().put(idData3, elementScore3);
        instance.getResult().put(idData4, elementScore4);
        instance.getResult().put(idData5, elementScore5);

//      Populte the unsorted values in hashmap
        testResult.put(idData1, elementScore1);
        testResult.put(idData2, elementScore2);
        testResult.put(idData3, elementScore3);
        testResult.put(idData4, elementScore4);
        testResult.put(idData5, elementScore5);

//      Compare the unsorted map to the instance map to verify
        assertEquals(instance.result, testResult);

//      Do the instance sort
        instance.sort();

//      Populte the sorted values in hashmap
        testResult.clear();
        testResult.put(idData2, elementScore2);
        testResult.put(idData4, elementScore4);
        testResult.put(idData1, elementScore1);
        testResult.put(idData3, elementScore3);
        testResult.put(idData5, elementScore5);

//      Compare the sorted map to the instance map to verify
        assertEquals(instance.result, testResult);
    }

    private class AnalyticResultImpl extends AnalyticResult<ElementScore> {

    }

}
