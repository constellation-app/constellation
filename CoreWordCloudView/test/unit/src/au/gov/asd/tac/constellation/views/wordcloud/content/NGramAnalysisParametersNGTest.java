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

import au.gov.asd.tac.constellation.views.wordcloud.content.ContentAnalysisOptions.FollowUpChoice;
import au.gov.asd.tac.constellation.views.wordcloud.content.ContentAnalysisOptions.FollowUpScope;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for NGramAnalysisParameters 
 * 
 * @author Delphinus8821
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class NGramAnalysisParametersNGTest extends ConstellationTest {

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
     * Test of toString method, of class NGramAnalysisParameters.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        final int onAttributeID = -1;
        final boolean caseSensitive = true;
        final boolean removeDomain = true;
        final int nGramLength = 2;
        final boolean binarySpace = false;
        final float threshold = 2.0F;
        final FollowUpChoice followUpChoice = FollowUpChoice.ADD_TRANSACTIONS;
        final FollowUpScope followUpScope = FollowUpScope.ALL;
        
        final NGramAnalysisParameters instance = new NGramAnalysisParameters(caseSensitive, removeDomain, nGramLength, binarySpace, threshold, followUpChoice, followUpScope);
        
        final String expResult = String.format("NGramAnalysisParameters[onAttributeID:%s, caseSensitive:%b, removeDomain:%b, nGramLength:%b, binarySpace:%b, threshold:%s, followUpChoice:%s, followUpScope:%s]",
                onAttributeID, caseSensitive, removeDomain, nGramLength, binarySpace, threshold, followUpChoice, followUpScope);
        final String result = instance.toString();
        assertEquals(result, expResult);
    }

    /**
     * Test of setOnAttributeID method, of class NGramAnalysisParameters.
     */
    @Test
    public void testSetOnAttributeID() {
        System.out.println("setOnAttributeID");
        final int value = 2;
        final NGramAnalysisParameters instance = NGramAnalysisParameters.getDefaultParameters();
        instance.setOnAttributeID(value);
        final int result = instance.getOnAttributeID();
        assertEquals(value, result);
    }

    /**
     * Test of setCaseSensitive method, of class NGramAnalysisParameters.
     */
    @Test
    public void testSetCaseSensitive() {
        System.out.println("setCaseSensitive");
        final boolean value = false;
        final NGramAnalysisParameters instance = NGramAnalysisParameters.getDefaultParameters();
        instance.setCaseSensitive(value);
        final boolean result = instance.isCaseSensitive();
        assertEquals(value, result);
    }

    /**
     * Test of setRemoveDomain method, of class NGramAnalysisParameters.
     */
    @Test
    public void testSetRemoveDomain() {
        System.out.println("setRemoveDomain");
        final boolean value = true;
        final NGramAnalysisParameters instance = NGramAnalysisParameters.getDefaultParameters();
        instance.setRemoveDomain(value);
        final boolean result = instance.isRemoveDomain();
        assertEquals(value, result);
    }

    /**
     * Test of setNGramLength method, of class NGramAnalysisParameters.
     */
    @Test
    public void testSetNGramLength() {
        System.out.println("setNGramLength");
        final int value = 1;
        final NGramAnalysisParameters instance = NGramAnalysisParameters.getDefaultParameters();
        instance.setNGramLength(value);
        final int result = instance.getNGramLength();
        assertEquals(value, result);
    }

    /**
     * Test of setBinarySpace method, of class NGramAnalysisParameters.
     */
    @Test
    public void testSetBinarySpace() {
        System.out.println("setBinarySpace");
        final boolean value = false;
        final NGramAnalysisParameters instance = NGramAnalysisParameters.getDefaultParameters();
        instance.setBinarySpace(value);
        final boolean result = instance.isBinarySpace();
        assertEquals(value, result);
    }

    /**
     * Test of setThreshold method, of class NGramAnalysisParameters.
     */
    @Test
    public void testSetThreshold() {
        System.out.println("setThreshold");
        final float value = 3.0F;
        final NGramAnalysisParameters instance = NGramAnalysisParameters.getDefaultParameters();
        instance.setThreshold(value);
        final float result = instance.getThreshold();
        assertEquals(value, result);
    }

    /**
     * Test of setFollowUpChoice method, of class NGramAnalysisParameters.
     */
    @Test
    public void testSetFollowUpChoice() {
        System.out.println("setFollowUpChoice");
        final FollowUpChoice value = FollowUpChoice.ADD_TRANSACTIONS;
        final NGramAnalysisParameters instance = NGramAnalysisParameters.getDefaultParameters();
        instance.setFollowUpChoice(value);
        final FollowUpChoice result = instance.getFollowUpChoice();
        assertEquals(value, result);
    }

    /**
     * Test of setFollowUpScope method, of class NGramAnalysisParameters.
     */
    @Test
    public void testSetFollowUpScope() {
        System.out.println("setFollowUpScope");
        final FollowUpScope value = FollowUpScope.ALL;
        final NGramAnalysisParameters instance = NGramAnalysisParameters.getDefaultParameters();
        instance.setFollowUpScope(value);
        final FollowUpScope result = instance.getFollowUpScope();
        assertEquals(value, result);
    }
    
}
