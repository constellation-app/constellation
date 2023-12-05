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

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.views.wordcloud.content.ContentAnalysisOptions.FollowUpChoice;
import au.gov.asd.tac.constellation.views.wordcloud.content.ContentAnalysisOptions.FollowUpScope;

/**
 * A container for the parameters of any content analysis plugins which involve
 * N-Gram Analysis.
 *
 * @author twilight_sparkle
 */
public class NGramAnalysisParameters {

    private int onAttributeID;
    private boolean caseSensitive;
    private boolean removeDomain;
    private int nGramLength;
    private boolean binarySpace;
    private float threshold;
    private FollowUpChoice followUpChoice;
    private FollowUpScope followUpScope;

    static final String FOLLOW_UP_CHOICE_PARAMETER_ID = PluginParameter.buildId(NGramAnalysisParameters.class, "follow_up_choice");
    static final String FOLLOW_UP_CHOICE_NAME = "Followup Action on Similar Nodes";
    static final String FOLLOW_UP_CHOICE_DESCRIPTION = "The action to be performed on nodes that are determined to be 'n-grammatically' similar.";

    static final String FOLLOW_UP_SCOPE_PARAMETER_ID = PluginParameter.buildId(NGramAnalysisParameters.class, "follow_up_scope");
    static final String FOLLOW_UP_SCOPE_NAME = "Candidate Nodes for Followup Action";
    static final String FOLLOW_UP_SCOPE_DESCRIPTION = "The set of candidates nodes for the followup action.";

    static final String ATTRIBUTE_TO_ANALYSE_PARAMETER_ID = PluginParameter.buildId(NGramAnalysisParameters.class, "attribute_to_analyse");
    static final String ATTRIBUTE_TO_ANALYSE_NAME = "Attribute to Analyse";
    static final String ATTRIBUTE_TO_ANALYSE_DESCRIPTION = "The attribute to use for n-gram analysis when comparing similarity of nodes.";

    static final String NGRAM_LENGTH_PARAMETER_ID = PluginParameter.buildId(NGramAnalysisParameters.class, "ngram_length");
    static final String NGRAM_LENGTH_NAME = "n-gram Length";
    static final String NGRAM_LENGTH_DESCIPTION = "The length of the n-grams to be used to compare nodes.";

    static final String THRESHOLD_PARAMETER_ID = PluginParameter.buildId(NGramAnalysisParameters.class, "threshold");
    static final String THRESHOLD_NAME = "Similarity Threshold";
    static final String THRESHOLD_DESCRIPTION = "A threshold between 0 and 1 for two nodes to be considered n-grammatically similar. 0 matches nodes that share at least one n-gram, 1 requires nodes to be identical.";

    static final String BINARY_SPACE_PARAMETER_ID = PluginParameter.buildId(NGramAnalysisParameters.class, "binary_space");
    static final String BINARY_SPACE_NAME = "Analyse Unique n-grams";
    static final String BINARY_SPACE_DESCRIPTION = "If true, only the unique n-grams in a node will be considered.";

    static final String CASE_SENSITIVE_PARAMETER_ID = PluginParameter.buildId(NGramAnalysisParameters.class, "case_sensitive");
    static final String CASE_SENSITIVE_NAME = "Case Sensitivity";
    static final String CASE_SENSITIVE_DESCRIPTION = "If true, n-grams will only match if case matches.";

    static final String REMOVE_DOMAIN_PARAMETER_ID = PluginParameter.buildId(NGramAnalysisParameters.class, "remove_domain");
    static final String REMOVE_DOMAIN_NAME = "Remove Domain";
    static final String REMOVE_DOMAIN_DESCRIPTION = "If true, remove the domain after the '@' symbol in email addresses.";

    static final int NGRAM_LENGTH_MIN_VALUE = 1;
    static final int NGRAM_LENGTH_MAX_VALUE = 100;
    static final float THRESHOLD_MIN_VALUE = 0.00F;
    static final float THRESHOLD_MAX_VALUE = 1.00F;
    static final float THRESHOLD_STEP_SIZE_VALUE = 0.01F;

    public NGramAnalysisParameters(final boolean caseSensitive, final boolean removeDomain, final int nGramLength, final boolean binarySpace, final float threshold, final FollowUpChoice followUpChoice, final FollowUpScope followUpScope) {
        this.onAttributeID = -1;
        this.caseSensitive = caseSensitive;
        this.removeDomain = removeDomain;
        this.nGramLength = nGramLength;
        this.binarySpace = binarySpace;
        this.threshold = threshold;
        this.followUpChoice = followUpChoice;
        this.followUpScope = followUpScope;
    }

    public static NGramAnalysisParameters getDefaultParameters() {
        return new NGramAnalysisParameters(false, false, 5, false, 0F, FollowUpChoice.CLUSTER, FollowUpScope.ALL);
    }

    @Override
    public String toString() {
        return String.format("NGramAnalysisParameters[onAttributeID:%s, caseSensitive:%b, removeDomain:%b, nGramLength:%b, binarySpace:%b, threshold:%s, followUpChoice:%s, followUpScope:%s]",
                onAttributeID, caseSensitive, removeDomain, nGramLength, binarySpace, threshold, followUpChoice, followUpScope);
    }

    public int getOnAttributeID() {
        return this.onAttributeID;
    }

    public boolean isCaseSensitive() {
        return this.caseSensitive;
    }

    public boolean isRemoveDomain() {
        return this.removeDomain;
    }

    public int getNGramLength() {
        return this.nGramLength;
    }

    public boolean isBinarySpace() {
        return this.binarySpace;
    }

    public float getThreshold() {
        return this.threshold;
    }

    public float getThresholdPercentage() {
        return Math.round(100 * getThreshold());
    }

    public FollowUpChoice getFollowUpChoice() {
        return this.followUpChoice;
    }

    public FollowUpScope getFollowUpScope() {
        return this.followUpScope;
    }

    public void setOnAttributeID(final int value) {
        this.onAttributeID = value;
    }

    public void setCaseSensitive(final boolean value) {
        this.caseSensitive = value;
    }

    public void setRemoveDomain(final boolean value) {
        this.removeDomain = value;
    }

    public void setNGramLength(final int value) {
        this.nGramLength = value;
    }

    public void setBinarySpace(final boolean value) {
        this.binarySpace = value;
    }

    public void setThreshold(final float value) {
        this.threshold = value;
    }

    public void setFollowUpChoice(final FollowUpChoice value) {
        this.followUpChoice = value;
    }

    public void setFollowUpScope(final FollowUpScope value) {
        this.followUpScope = value;
    }
}
