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
package au.gov.asd.tac.constellation.views.wordcloud.phraseanalysis;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for PhrasiphyContentParameters 
 * 
 * @author Delphinus8821
 */
public class PhrasiphyContentParametersNGTest {
    
    public PhrasiphyContentParametersNGTest() {
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
     * Test of getDefaultParameters method, of class PhrasiphyContentParameters.
     */
    @Test
    public void testGetDefaultParameters() {
        System.out.println("getDefaultParameters");
        final PhrasiphyContentParameters expResult = new PhrasiphyContentParameters(1, 1, 5);
        final PhrasiphyContentParameters result = PhrasiphyContentParameters.getDefaultParameters();
        assertEquals(result.toString(), expResult.toString());
    }

    /**
     * Test of toString method, of class PhrasiphyContentParameters.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        final PhrasiphyContentParameters instance = new PhrasiphyContentParameters(1, 1, 5);
        final String expResult = String.format("PhrasiphyContentParameters[onAttributeID:%d, phraseLength:%d, proximity:%d, threshold:%d]", -1, 1, 1, 5);;
        final String result = instance.toString();
        assertEquals(result, expResult);
    }

    /**
     * Test of setOnAttributeID method, of class PhrasiphyContentParameters.
     */
    @Test
    public void testSetOnAttributeID() {
        System.out.println("setOnAttributeID");
        final int value = 3;
        final PhrasiphyContentParameters instance = new PhrasiphyContentParameters(1, 2, 3);
        instance.setOnAttributeID(value);
        final int result = instance.getOnAttributeID();
        assertEquals(value, result);
    }

    /**
     * Test of setPhraseLength method, of class PhrasiphyContentParameters.
     */
    @Test
    public void testSetPhraseLength() {
        System.out.println("setPhraseLength");
        final int value = 5;
        final PhrasiphyContentParameters instance = new PhrasiphyContentParameters(1, 2, 3);
        instance.setPhraseLength(value);
        final int result = instance.getPhraseLength();
        assertEquals(value, result);
    }

    /**
     * Test of setElementType method, of class PhrasiphyContentParameters.
     */
    @Test
    public void testSetElementType() {
        System.out.println("setElementType");
        final String value = "Node";
        final PhrasiphyContentParameters instance = new PhrasiphyContentParameters(1, 2, 3);
        instance.setElementType(value);
        final GraphElementType result = instance.getElementType();
        assertTrue(value.equals(result.getLabel()));
    }

    /**
     * Test of setProximity method, of class PhrasiphyContentParameters.
     */
    @Test
    public void testSetProximity() {
        System.out.println("setProximity");
        final int value = 5;
        final PhrasiphyContentParameters instance = new PhrasiphyContentParameters(1, 2, 3);
        instance.setProximity(value);
        final int result = instance.getProximity();
        assertEquals(result, value);
    }

    /**
     * Test of setThreshold method, of class PhrasiphyContentParameters.
     */
    @Test
    public void testSetThreshold() {
        System.out.println("setThreshold");
        final int value = 2;
        final PhrasiphyContentParameters instance = new PhrasiphyContentParameters(1, 2, 3);
        instance.setThreshold(value);
        final int result = instance.getThreshold();
        assertEquals(result, value);
    }

    /**
     * Test of setBackgroundFilter method, of class PhrasiphyContentParameters.
     */
    @Test
    public void testSetBackgroundFilter() {
        System.out.println("setBackgroundFilter");
        final String value = "Contain all words in phrase";
        final PhrasiphyContentParameters instance = new PhrasiphyContentParameters(1, 2, 3);
        instance.setBackgroundFilter(value);
        final boolean result = instance.hasFilterAllWords();
        assertTrue(result);
    }
    
}
