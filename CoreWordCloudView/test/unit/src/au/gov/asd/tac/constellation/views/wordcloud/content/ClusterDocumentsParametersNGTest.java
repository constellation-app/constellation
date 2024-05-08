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
package au.gov.asd.tac.constellation.views.wordcloud.content;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.views.wordcloud.content.ContentAnalysisOptions.Delimiter;
import au.gov.asd.tac.constellation.views.wordcloud.content.ContentAnalysisOptions.FollowUpChoice;
import au.gov.asd.tac.constellation.views.wordcloud.content.ContentAnalysisOptions.FollowUpScope;
import au.gov.asd.tac.constellation.views.wordcloud.content.ContentAnalysisOptions.TokenThresholdMethod;
import au.gov.asd.tac.constellation.views.wordcloud.content.ContentAnalysisOptions.TokenizingMethod;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for ClusterDocumentsParameters 
 * 
 * @author Delphinus8821
 */
public class ClusterDocumentsParametersNGTest {

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
     * Test of toString method, of class ClusterDocumentsParameters.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        final int onAttributeID = -1;
        final boolean caseSensitive = true;
        final char[] toFilter = new char[4];
        final TokenizingMethod tokenizingMethod = TokenizingMethod.NWORDS;
        final Delimiter delimiter = Delimiter.COMMA;
        final int tokenLength = 1;
        final boolean binarySpace = false;
        final float threshold = 2.0F;
        final TokenThresholdMethod thresholdMethod = TokenThresholdMethod.APPEARANCE;
        final boolean significantAboveThreshold = false;
        final float weightingExponent = 3.0F;
        final int numberOfMeans = 4;
        final FollowUpChoice followUpChoice = FollowUpChoice.ADD_TRANSACTIONS;
        final FollowUpScope followUpScope = FollowUpScope.ALL;
        final GraphElementType elementType = GraphElementType.LINK;
        
        final ClusterDocumentsParameters instance = new ClusterDocumentsParameters(caseSensitive, toFilter, tokenizingMethod, 
                delimiter, tokenLength, binarySpace, threshold, thresholdMethod, significantAboveThreshold, weightingExponent, 
                numberOfMeans, followUpChoice, followUpScope, elementType);
        final String expResult = String.format("ContentAnalysisParameters[onAttributeID:%d, caseSensitive:%b, toFilter:%b, tokenizingMethod:%s, delimiter:%s, tokenLength:%d, binarySpace:%b, threshold:%f, thresholdMethod:%s, weightingExponent:%f, numberOfMeans:%d, followUpChoice:%s, followUpScope:%s]",
                onAttributeID, caseSensitive, toFilter, tokenizingMethod, delimiter, tokenLength, binarySpace, threshold, thresholdMethod, weightingExponent, numberOfMeans, followUpChoice, followUpScope);
        final String result = instance.toString();
        assertEquals(result, expResult);
    }
    
    /**
     * Test of setOnAttributeID method, of class ClusterDocumentsParameters.
     */
    @Test
    public void testSetOnAttributeID() {
        System.out.println("setOnAttributeID");
        final int value = 2;
        final ClusterDocumentsParameters instance = ClusterDocumentsParameters.getDefaultParameters();
        instance.setOnAttributeID(value);
        final int result = instance.getOnAttributeID();
        assertEquals(value, result);
    }

    /**
     * Test of setToFilter method, of class ClusterDocumentsParameters.
     */
    @Test
    public void testSetToFilter() {
        System.out.println("setToFilter");
        final char[] value = new char[7];
        final ClusterDocumentsParameters instance = ClusterDocumentsParameters.getDefaultParameters();
        instance.setToFilter(value);
        final char[] result = instance.getToFilter();
        assertEquals(result, value);
    }

    /**
     * Test of setCaseSensitive method, of class ClusterDocumentsParameters.
     */
    @Test
    public void testSetCaseSensitive() {
        System.out.println("setCaseSensitive");
        final boolean value = false;
        final ClusterDocumentsParameters instance = ClusterDocumentsParameters.getDefaultParameters();
        instance.setCaseSensitive(value);
        final boolean result = instance.isCaseSensitive();
        assertEquals(result, value);
    }

    /**
     * Test of setTokenizingMethod method, of class ClusterDocumentsParameters.
     */
    @Test
    public void testSetTokenizingMethod() {
        System.out.println("setTokenizingMethod");
        final TokenizingMethod value = TokenizingMethod.NWORDS;
        final ClusterDocumentsParameters instance = ClusterDocumentsParameters.getDefaultParameters();
        instance.setTokenizingMethod(value);
        final ContentAnalysisOptions.TokenizingMethod result = instance.getTokenizingMethod();
        assertEquals(result, value);
    }

    /**
     * Test of setDelimiter method, of class ClusterDocumentsParameters.
     */
    @Test
    public void testSetDelimiter() {
        System.out.println("setDelimiter");
        final Delimiter value = Delimiter.FULL_STOP;
        final ClusterDocumentsParameters instance = ClusterDocumentsParameters.getDefaultParameters();
        instance.setDelimiter(value);
        final Delimiter result = instance.getDelimiter();
        assertEquals(result, value);
    }

    /**
     * Test of setTokenLength method, of class ClusterDocumentsParameters.
     */
    @Test
    public void testSetTokenLength() {
        System.out.println("setTokenLength");
        final int value = 2;
        final ClusterDocumentsParameters instance = ClusterDocumentsParameters.getDefaultParameters();
        instance.setTokenLength(value);
        final int result = instance.getTokenLength();
        assertEquals(result, value);
    }

    /**
     * Test of setBinarySpace method, of class ClusterDocumentsParameters.
     */
    @Test
    public void testSetBinarySpace() {
        System.out.println("setBinarySpace");
        final boolean value = false;
        final ClusterDocumentsParameters instance = ClusterDocumentsParameters.getDefaultParameters();
        instance.setBinarySpace(value);
        final boolean result = instance.isBinarySpace();
        assertEquals(result, value);
    }

    /**
     * Test of setThreshold method, of class ClusterDocumentsParameters.
     */
    @Test
    public void testSetThreshold() {
        System.out.println("setThreshold");
        final float value = 1.0F;
        final ClusterDocumentsParameters instance = ClusterDocumentsParameters.getDefaultParameters();
        instance.setThreshold(value);
        final float result = instance.getThreshold();
        assertEquals(result, value);
    }

    /**
     * Test of setThresholdMethod method, of class ClusterDocumentsParameters.
     */
    @Test
    public void testSetThresholdMethod() {
        System.out.println("setThresholdMethod");
        final TokenThresholdMethod value = TokenThresholdMethod.APPEARANCE;
        final ClusterDocumentsParameters instance = ClusterDocumentsParameters.getDefaultParameters();
        instance.setThresholdMethod(value);
        final TokenThresholdMethod result = instance.getThresholdMethod();
        assertEquals(value, result);
    }

    /**
     * Test of setSignificantAboveThreshold method, of class ClusterDocumentsParameters.
     */
    @Test
    public void testSetSignificantAboveThreshold() {
        System.out.println("setSignificantAboveThreshold");
        final boolean value = true;
        final ClusterDocumentsParameters instance = ClusterDocumentsParameters.getDefaultParameters();
        instance.setSignificantAboveThreshold(value);
        final boolean result = instance.isSignificantAboveThreshold();
        assertEquals(result, value);
    }

    /**
     * Test of setWeightingExponent method, of class ClusterDocumentsParameters.
     */
    @Test
    public void testSetWeightingExponent() {
        System.out.println("setWeightingExponent");
        final float value = 6.0F;
        final ClusterDocumentsParameters instance = ClusterDocumentsParameters.getDefaultParameters();
        instance.setWeightingExponent(value);
        final float result = instance.getWeightingExponent();
        assertEquals(result, value);
    }

    /**
     * Test of setNumberOfMeans method, of class ClusterDocumentsParameters.
     */
    @Test
    public void testSetNumberOfMeans() {
        System.out.println("setNumberOfMeans");
        final int value = 1;
        final ClusterDocumentsParameters instance = ClusterDocumentsParameters.getDefaultParameters();
        instance.setNumberOfMeans(value);
        final int result = instance.getNumberOfMeans();
        assertEquals(result, value);
    }

    /**
     * Test of setFollowUpChoice method, of class ClusterDocumentsParameters.
     */
    @Test
    public void testSetFollowUpChoice() {
        System.out.println("setFollowUpChoice");
        final FollowUpChoice value = FollowUpChoice.ADD_TRANSACTIONS;
        final ClusterDocumentsParameters instance = ClusterDocumentsParameters.getDefaultParameters();
        instance.setFollowUpChoice(value);
        final FollowUpChoice result = instance.getFollowUpChoice();
        assertEquals(result, value);
    }

    /**
     * Test of setFollowUpScope method, of class ClusterDocumentsParameters.
     */
    @Test
    public void testSetFollowUpScope() {
        System.out.println("setFollowUpScope");
        final FollowUpScope value = FollowUpScope.ALL;
        final ClusterDocumentsParameters instance = ClusterDocumentsParameters.getDefaultParameters();
        instance.setFollowUpScope(value);
        final FollowUpScope result = instance.getFollowUpScope();
        assertEquals(value, result);
    }

    /**
     * Test of setElementType method, of class ClusterDocumentsParameters.
     */
    @Test
    public void testSetElementType() {
        System.out.println("setElementType");
        final String value = "Node";
        final ClusterDocumentsParameters instance = ClusterDocumentsParameters.getDefaultParameters();
        instance.setElementType(value);
        final String result = instance.getElementType().getLabel();
        assertTrue(result.equals(value));
    }
    
}
