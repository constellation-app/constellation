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

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.views.wordcloud.content.ContentAnalysisOptions.Delimiter;
import au.gov.asd.tac.constellation.views.wordcloud.content.ContentAnalysisOptions.FollowUpChoice;
import au.gov.asd.tac.constellation.views.wordcloud.content.ContentAnalysisOptions.FollowUpScope;
import au.gov.asd.tac.constellation.views.wordcloud.content.ContentAnalysisOptions.TokenThresholdMethod;
import au.gov.asd.tac.constellation.views.wordcloud.content.ContentAnalysisOptions.TokenizingMethod;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A container for the parameters of any content analysis plugins which involve
 * Document Clustering.
 *
 * @author twilight_sparkle
 */
public class ClusterDocumentsParameters {

    public static final String TRANS_ATTRIBUTE_PARAMETER_ID = PluginParameter.buildId(ClusterDocumentsParameters.class, "trans_attribute");
    public static final String VERT_ATTRIBUTE_PARAMETER_ID = PluginParameter.buildId(ClusterDocumentsParameters.class, "vert_attribute");

    public static final String FOLLOW_UP_CHOICE_PARAMETER_ID = PluginParameter.buildId(ClusterDocumentsParameters.class, "follow_up_choice");
    public static final String FOLLOW_UP_CHOICE_NAME = "Followup Action on Elements in Same Cluster";
    public static final String FOLLOW_UP_CHOICE_DESCRIPTION = "The action to be performed on graph elements whose documents are placed in the same cluster";

    public static final String FOLLOW_UP_SCOPE_PARAMETER_ID = PluginParameter.buildId(ClusterDocumentsParameters.class, "follow_up_scope");
    public static final String FOLLOW_UP_SCOPE_NAME = "Candidate Elements for Followup Action";
    public static final String FOLLOW_UP_SCOPE_DESCRIPTION = "The set of candidates graph elements for the followup action.";

    public static final String ELEMENT_TYPE_PARAMETER_ID = PluginParameter.buildId(ClusterDocumentsParameters.class, "element_type");
    public static final String ELEMENT_TYPE_NAME = "Element Type";
    public static final String ELEMENT_TYPE_DESCRIPTION = "The type of graph element to cluster";
    public static final String ELEMENT_TYPE_DEFAULT = "transaction";
    public static final List<String> ELEMENT_TYPE_CHOICES = Arrays.asList(ELEMENT_TYPE_DEFAULT, "node");

    public static final String ATTRIBUTE_TO_ANALYSE_PARAMETER_ID = PluginParameter.buildId(ClusterDocumentsParameters.class, "attribute_to_analyse");
    public static final String ATTRIBUTE_TO_ANALYSE_NAME = "Attribute to Analyse";
    public static final String ATTRIBUTE_TO_ANALYSE_DESCRIPTION = "The attribute which holds the documents being clustered.";

    public static final String TO_FILTER_PARAMETER_ID = PluginParameter.buildId(ClusterDocumentsParameters.class, "to_filter");
    public static final String TO_FILTER_NAME = "Characters To Filter";
    public static final String TO_FILTER_DESCRIPTION = "The list of characters ignored when clustering documents.";

    public static final String TOKENIZING_METHOD_PARAMETER_ID = PluginParameter.buildId(ClusterDocumentsParameters.class, "tokenizing_method");
    public static final String TOKENIZING_METHOD_NAME = "Tokenizing Method";
    public static final String TOKENIZING_METHOD_DESCRIPTION = "The method used to break content into small tokens which can be compared";

    public static final String DELIMITER_PARAMETER_ID = PluginParameter.buildId(ClusterDocumentsParameters.class, "delimiter");
    public static final String DELIMITER_NAME = "Delimiter";
    public static final String DELIMITER_DESCRIPTION = "The Delimiter to use to deliniate between tokens when using n-word tokenization methods.";

    public static final String TOKEN_LENGTH_PARAMETER_ID = PluginParameter.buildId(ClusterDocumentsParameters.class, "token_length");
    public static final String TOKEN_LENGTH_NAME = "Token Length";
    public static final String TOKEN_LENGTH_DESCRIPTION = "The length of the tokens to be used to compare documents.";

    public static final String THRESHOLD_PARAMETER_ID = PluginParameter.buildId(ClusterDocumentsParameters.class, "threshold");
    public static final String THRESHOLD_NAME = "Token Significance Threshold";
    public static final String THRESHOLD_DESCRIPTION = "A threshold between 0 and 1 for a token to be considered a 'significamt feature' of a document.";

    public static final String THRESHOLD_METHOD_PARAMETER_ID = PluginParameter.buildId(ClusterDocumentsParameters.class, "threshold_method");
    public static final String THRESHOLD_METHOD_NAME = "Token Threshold Method";
    public static final String THRESHOLD_METHOD_DESCRIPTION = "The method used to determine whether a token is above the desired threshold";

    public static final String THRESHOLD_DIRECTION_PARAMETER_ID = PluginParameter.buildId(ClusterDocumentsParameters.class, "threshold_direction");
    public static final String THRESHOLD_DIRECTION_NAME = "Take tokens above threshold";
    public static final String THRESHOLD_DIRECTION_DESCRIPTION = "If true, take only tokens which are more common than the threshold, if false, take those which are less common.";

    public static final String WEIGHTING_EXPONENT_PARAMETER_ID = PluginParameter.buildId(ClusterDocumentsParameters.class, "weighting_exponent");
    public static final String WEIGHTING_EXPONENT_NAME = "Token weighting exponent";
    public static final String WEIGHTING_EXPONENT_DESCRIPTION = "The exponent with which to weight tokens. Positive exponents weight common tokens more heavily, while negative exponents weight rare tokens more heavily. Zero exponent weights all tokens equally.";

    public static final String BINARY_SPACE_PARAMETER_ID = PluginParameter.buildId(ClusterDocumentsParameters.class, "binary_space");
    public static final String BINARY_SPACE_NAME = "Consider unique tokens only";
    public static final String BINARY_SPACE_DESCRIPTION = "If True, each unique token will be counted only once in a given document.";

    public static final String CASE_SENSITIVE_PARAMETER_ID = PluginParameter.buildId(ClusterDocumentsParameters.class, "case_sensitive");
    public static final String CASE_SENSITIVE_NAME = "Case Sensitivity";
    public static final String CASE_SENSITIVE_DESCRIPTION = "If True, tokens will only match if case matches.";

    public static final String NUMBER_OF_MEANS_PARAMETER_ID = PluginParameter.buildId(ClusterDocumentsParameters.class, "number_of_means");
    public static final String NUMBER_OF_MEANS_NAME = "Number of Means";
    public static final String NUMBER_OF_MEANS_DESCRIPTION = "The number of clusters to group the documents into.";

    public static final int TOKEN_LENGTH_MIN_VALUE = 1;
    public static final int TOKEN_LENGTH_MAX_VALUE = 100;
    public static final float THRESHOLD_MIN_VALUE = 0.00F;
    public static final float THRESHOLD_MAX_VALUE = 1.00F;
    public static final float THRESHOLD_STEP_SIZE_VALUE = 0.01F;
    public static final float WEIGHTING_EXPONENT_MIN_VALUE = -5.00F;
    public static final float WEIGHTING_EXPONENT_MAX_VALUE = 5.00F;
    public static final float WEIGHTING_EXPONENT_STEP_SIZE_VALUE = 0.10F;
    public static final int NUMBER_OF_MEANS_MIN_VALUE = 2;
    public static final int NUMBER_OF_MEANS_MAX_VALUE = 1000;

    private int onAttributeID;
    private boolean caseSensitive;
    private char[] toFilter;
    private TokenizingMethod tokenizingMethod;
    private Delimiter delimiter;
    private int tokenLength;
    private boolean binarySpace;
    private float threshold;
    private TokenThresholdMethod thresholdMethod;
    private boolean significantAboveThreshold;
    private float weightingExponent;
    private int numberOfMeans;
    private FollowUpChoice followUpChoice;
    private FollowUpScope followUpScope;
    private GraphElementType elementType;

    public ClusterDocumentsParameters(final boolean caseSensitive, final char[] toFilter, final TokenizingMethod tokenizingMethod, final Delimiter delimiter, final int tokenLength, final boolean binarySpace, final float threshold, final TokenThresholdMethod thresholdMethod, final boolean significantAboveThreshold, final float weightingExponent, final int numberOfMeans, final FollowUpChoice followUpChoice, final FollowUpScope followUpScope, final GraphElementType elementType) {
        this.onAttributeID = -1;
        this.caseSensitive = caseSensitive;
        this.toFilter = toFilter.clone();
        this.tokenizingMethod = tokenizingMethod;
        this.delimiter = delimiter;
        this.tokenLength = tokenLength;
        this.binarySpace = binarySpace;
        this.threshold = threshold;
        this.thresholdMethod = thresholdMethod;
        this.significantAboveThreshold = significantAboveThreshold;
        this.weightingExponent = weightingExponent;
        this.numberOfMeans = numberOfMeans;
        this.followUpChoice = followUpChoice;
        this.followUpScope = followUpScope;
        this.elementType = elementType;
    }

    public static ClusterDocumentsParameters getDefaultParameters() {
        return new ClusterDocumentsParameters(false, new char[0], TokenizingMethod.NGRAMS, Delimiter.SPACE, 5, false, 0F, TokenThresholdMethod.APPEARANCE, true, 0F, 10, FollowUpChoice.CLUSTER, FollowUpScope.ALL, GraphElementType.TRANSACTION);
    }

    @Override
    public String toString() {
        return String.format("ContentAnalysisParameters[onAttributeID:%d, caseSensitive:%b, toFilter:%b, tokenizingMethod:%s, delimiter:%s, tokenLength:%d, binarySpace:%b, threshold:%f, thresholdMethod:%s, weightingExponent:%f, numberOfMeans:%d, followUpChoice:%s, followUpScope:%s]",
                onAttributeID, caseSensitive, toFilter, tokenizingMethod, delimiter, tokenLength, binarySpace, threshold, thresholdMethod, weightingExponent, numberOfMeans, followUpChoice, followUpScope);
    }

    public int getOnAttributeID() {
        return onAttributeID;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public char[] getToFilter() {
        return toFilter.clone();
    }

    public Set<Character> getToFilterSet() {
        final Set<Character> toFilterSet = new HashSet<>();
        for (int i = 0; i < toFilter.length; i++) {
            toFilterSet.add(toFilter[i]);
        }
        return toFilterSet;
    }

    public TokenizingMethod getTokenizingMethod() {
        return tokenizingMethod;
    }

    public Delimiter getDelimiter() {
        return delimiter;
    }

    public int getTokenLength() {
        return tokenLength;
    }

    public boolean isBinarySpace() {
        return binarySpace;
    }

    public float getThreshold() {
        return threshold;
    }

    public int getThresholdPercentage() {
        return Math.round(100 * getThreshold());
    }

    public TokenThresholdMethod getThresholdMethod() {
        return thresholdMethod;
    }

    public boolean isSignificantAboveThreshold() {
        return significantAboveThreshold;
    }

    public float getWeightingExponent() {
        return weightingExponent;
    }

    public int getNumberOfMeans() {
        return numberOfMeans;
    }

    public FollowUpChoice getFollowUpChoice() {
        return followUpChoice;
    }

    public FollowUpScope getFollowUpScope() {
        return followUpScope;
    }

    public GraphElementType getElementType() {
        return elementType;
    }

    public void setOnAttributeID(final int value) {
        onAttributeID = value;
    }

    public void setToFilter(final char[] value) {
        toFilter = value.clone();
    }

    public void setCaseSensitive(final boolean value) {
        caseSensitive = value;
    }

    public void setTokenizingMethod(final TokenizingMethod value) {
        tokenizingMethod = value;
    }

    public void setDelimiter(final Delimiter value) {
        delimiter = value;
    }

    public void setTokenLength(final int value) {
        tokenLength = value;
    }

    public void setBinarySpace(final boolean value) {
        binarySpace = value;
    }

    public void setThreshold(final float value) {
        threshold = value;
    }

    public void setThresholdMethod(final TokenThresholdMethod value) {
        thresholdMethod = value;
    }

    public void setSignificantAboveThreshold(final boolean value) {
        significantAboveThreshold = value;
    }

    public void setWeightingExponent(final float value) {
        weightingExponent = value;
    }

    public void setNumberOfMeans(final int value) {
        numberOfMeans = value;
    }

    public void setFollowUpChoice(final FollowUpChoice value) {
        followUpChoice = value;
    }

    public void setFollowUpScope(final FollowUpScope value) {
        followUpScope = value;
    }

    public void setElementType(final String value) {
        elementType = GraphElementType.getValue(value);
    }
}
