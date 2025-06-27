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

import au.gov.asd.tac.constellation.views.wordcloud.content.ContentAnalysisOptions.Delimiter;
import au.gov.asd.tac.constellation.views.wordcloud.content.ContentAnalysisOptions.FollowUpChoice;
import au.gov.asd.tac.constellation.views.wordcloud.content.ContentAnalysisOptions.FollowUpScope;
import au.gov.asd.tac.constellation.views.wordcloud.content.ContentAnalysisOptions.TokenThresholdMethod;
import au.gov.asd.tac.constellation.views.wordcloud.content.ContentAnalysisOptions.TokenizingMethod;
import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for ContentAnalysisOptions 
 * 
 * @author Delphinus8821
 */
public class ContentAnalysisOptionsNGTest {

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
    public void testDelimiterDefaultEnum() {
        final Delimiter delimiter = Delimiter.defaultEnum();
        final Delimiter result = Delimiter.SPACE;
        assertEquals(delimiter, result);
    }

    @Test 
    public void testDelimiterGetValue() {
        String value = "space";
        Delimiter delimiter = Delimiter.getValue(value);
        assertEquals(value, delimiter.toString());
        
        value = ", (comma)";
        delimiter = Delimiter.getValue(value);
        assertEquals(value, delimiter.toString());
        
        value = "\\ (backslash)";
        delimiter = Delimiter.getValue(value);
        assertEquals(value, delimiter.toString());
        
        value = "/ (slash)";
        delimiter = Delimiter.getValue(value);
        assertEquals(value, delimiter.toString());
        
        value = ". (full stop)";
        delimiter = Delimiter.getValue(value);
        assertEquals(value, delimiter.toString());
        
        value = "something else";
        delimiter = Delimiter.getValue(value);
        assertEquals(delimiter.toString(), Delimiter.SPACE.toString());
    }
  
    @Test
    public void testDelimiterGetChoices() {
        final List<String> result = new ArrayList<>();
        result.add(Delimiter.SPACE.toString());
        result.add(Delimiter.COMMA.toString());
        result.add(Delimiter.BACKSLASH.toString());
        result.add(Delimiter.SLASH.toString());
        result.add(Delimiter.FULL_STOP.toString());
        
        final List<String> delimiterList = Delimiter.getChoices();
        assertEquals(delimiterList, result);
    }

    @Test
    public void testTokenThresholdMethodDefaultEnum() {
        final TokenThresholdMethod token = TokenThresholdMethod.defaultEnum();
        final TokenThresholdMethod result = TokenThresholdMethod.APPEARANCE;
        assertEquals(token, result);
    }
    
    @Test
    public void testTokenThresholdMethodGetValue() {
        String value = "Proportion of elements token appeared in";
        TokenThresholdMethod token = TokenThresholdMethod.getValue(value);
        assertEquals(token.toString(), value);
        
        value = "Percentile of tokens ranked by appearances";
        token = TokenThresholdMethod.getValue(value);
        assertEquals(value, token.toString());
        
        value = "something else";
        token = TokenThresholdMethod.getValue(value);
        assertEquals(token.toString(), TokenThresholdMethod.APPEARANCE.toString());
    }
    
    @Test
    public void testTokenThresholdMethodGetChoices() {
        final List<String> result = new ArrayList<>();
        result.add(TokenThresholdMethod.APPEARANCE.toString());
        result.add(TokenThresholdMethod.RANK.toString());
        assertEquals(result, TokenThresholdMethod.getChoices());
    }
    
    @Test
    public void testTokenizingMethodDefaultEnum() {
        final TokenizingMethod result = TokenizingMethod.defaultEnum();
        final TokenizingMethod token = TokenizingMethod.NGRAMS;
        assertEquals(result, token);
    }
    
    @Test
    public void testTokenizingMethodGetValue() {
        String value = "n-grams";
        TokenizingMethod token = TokenizingMethod.getValue(value);
        assertEquals(token.toString(), value);
        
        value = "delimited n-grams";
        token = TokenizingMethod.getValue(value);
        assertEquals(value, token.toString());
        
        value = "n-words";
        token = TokenizingMethod.getValue(value);
        assertEquals(value, token.toString());
        
        value = "something else";
        token = TokenizingMethod.getValue(value);
        assertEquals(token.toString(), TokenizingMethod.NGRAMS.toString());
    }
    
    @Test
    public void testTokenizingMethodGetChoices() {
        final List<String> result = new ArrayList<>();
        result.add(TokenizingMethod.NGRAMS.toString());
        result.add(TokenizingMethod.DELIMITED_NGRAMS.toString());
        result.add(TokenizingMethod.NWORDS.toString());
        assertEquals(result, TokenizingMethod.getChoices());
    }
    
    @Test
    public void testFollowUpChoiceDefaultEnum() {
        final FollowUpChoice result = FollowUpChoice.defaultEnum();
        final FollowUpChoice choice = FollowUpChoice.CLUSTER;
        assertEquals(result, choice);
    }
    
    @Test
    public void testFollowUpChoiceGetValue() {
        String value = "Cluster";
        FollowUpChoice choice = FollowUpChoice.getValue(value);
        assertEquals(value, choice.toString());
        
        value = "Add Transactions";
        choice = FollowUpChoice.getValue(value);
        assertEquals(value, choice.toString());
        
        value = "Make Selections";
        choice = FollowUpChoice.getValue(value);
        assertEquals(value, choice.toString());
        
        value = "something else";
        choice = FollowUpChoice.getValue(value);
        assertEquals(choice.toString(), FollowUpChoice.ADD_TRANSACTIONS.toString());
    }
    
    @Test
    public void testFollowUpChoiceGetDocumentClusteringChoices() {
        final List<String> result = new ArrayList<>();
        result.add(FollowUpChoice.CLUSTER.toString());
        result.add(FollowUpChoice.MAKE_SELECTIONS.toString());
        assertEquals(result, FollowUpChoice.getDocumentClusteringChoices());
    }
    
    @Test
    public void testFollowUpChoicesGetNodeSimilarityChoices() {
        final List<String> result = new ArrayList<>();
        result.add(FollowUpChoice.ADD_TRANSACTIONS.toString());
        assertEquals(result, FollowUpChoice.getNodeSimilarityChoices());
    }
    
    @Test
    public void testFollowUpScopeDefaultEnum() {
        final FollowUpScope scope = FollowUpScope.ALL;
        final FollowUpScope result = FollowUpScope.defaultEnum();
        assertEquals(scope, result);
    }
    
    @Test 
    public void testFollowUpScopeGetValue() {
        String value = "All";
        FollowUpScope scope = FollowUpScope.getValue(value);
        assertEquals(scope.toString(), value);
        
        value = "Selected";
        scope = FollowUpScope.getValue(value);
        assertEquals(value, scope.toString());
        
        value = "Similar to Selected";
        scope = FollowUpScope.getValue(value);
        assertEquals(value, scope.toString());
        
        value = "something else";
        scope = FollowUpScope.getValue(value);
        assertEquals(scope.toString(), FollowUpScope.ALL.toString());
    }
    
    @Test
    public void testFollowUpScopeGetChoices() {
        final List<String> result = new ArrayList<>();
        result.add(FollowUpScope.ALL.toString());
        result.add(FollowUpScope.SELECTED.toString());
        result.add(FollowUpScope.SIMILAR_TO_SELECTED.toString());
        assertEquals(result, FollowUpScope.getChoices());
    }
}
