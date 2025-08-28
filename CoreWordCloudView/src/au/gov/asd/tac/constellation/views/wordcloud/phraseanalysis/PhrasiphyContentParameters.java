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
package au.gov.asd.tac.constellation.views.wordcloud.phraseanalysis;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import java.util.Arrays;
import java.util.List;

/*
 * A container for the parameters of any content analysis plugins which involve phrasiphying content
 *
 * @author twilight_sparkle
 */
public class PhrasiphyContentParameters {

    public static final String TRANS_ATTRIBUTE_PARAMETER_ID = PluginParameter.buildId(PhrasiphyContentParameters.class, "transaction_attribute");
    public static final String VERT_ATTRIBUTE_PARAMETER_ID = PluginParameter.buildId(PhrasiphyContentParameters.class, "vertex_attribute");

    public static final String ELEMENT_TYPE_PARAMETER_ID = PluginParameter.buildId(PhrasiphyContentParameters.class, "element_type");
    public static final String ELEMENT_TYPE_NAME = "Element Type";
    public static final String ELEMENT_TYPE_DESCRIPTION = "The type of graph element.";
    public static final String ELEMENT_TYPE_DEFAULT = "transaction";
    public static final List<String> ELEMENT_TYPE_CHOICES = Arrays.asList(ELEMENT_TYPE_DEFAULT, "node");

    public static final String ATTRIBUTE_TO_ANALYSE_PARAMETER_ID = PluginParameter.buildId(PhrasiphyContentParameters.class, "attribute_to_analyse");
    public static final String ATTRIBUTE_TO_ANALYSE_NAME = "Attribute";
    public static final String ATTRIBUTE_TO_ANALYSE_DESCRIPTION = "The attribute from which to generate the word cloud.";
    public static final String ATTRIBUTE_TO_ANALYSE_DEFAULT_TRANSACTIONS = "Content";
    public static final String ATTRIBUTE_TO_ANALYSE_DEFAULT_NODES = "Name";

    public static final String PHRASE_LENGTH_PARAMETER_ID = PluginParameter.buildId(PhrasiphyContentParameters.class, "phrase_length");
    public static final String PHRASE_LENGTH_NAME = "Phrase Length";
    public static final String PHRASE_LENGTH_DESCRIPTION = "The number of words in each phrase.";

    public static final String PROXIMITY_PARAMETER_ID = PluginParameter.buildId(PhrasiphyContentParameters.class, "proximity");
    public static final String PROXIMITY_NAME = "Phrase Span";
    public static final String PROXIMITY_DESCRIPTION = "The number of words a phrase can span in the original content.";

    public static final String THRESHOLD_PARAMETER_ID = PluginParameter.buildId(PhrasiphyContentParameters.class, "threshold");
    public static final String THRESHOLD_NAME = "Threshold";
    public static final String THRESHOLD_DESCRIPTION = "The minimum number of graph elements a phrase must occur in.";

    public static final String BACKGROUND_PARAMETER_ID = PluginParameter.buildId(PhrasiphyContentParameters.class, "background_file");
    public static final String BACKGROUND_NAME = "Background File";
    public static final String BACKGROUND_DESCRIPTION = "A text file containing a background for frequency comparison.";

    public static final String BACKGROUND_FILTER_PARAMETER_ID = PluginParameter.buildId(PhrasiphyContentParameters.class, "background_filter");
    public static final String BACKGROUND_FILTER_NAME = "Background Filter";
    public static final String BACKGROUND_FILTER_DESCRIPTION = "Filter on the sentences to use for comparison.";
    public static final String BACKGROUND_FILTER_DEFAULT = "Contain all words in phrase";
    public static final List<String> BACKGROUND_FILTER_CHOICES = Arrays.asList("Contain any word in phrase", BACKGROUND_FILTER_DEFAULT);

    public static final int PHRASE_LENGTH_MIN_VALUE = 1;
    public static final int PHRASE_LENGTH_MAX_VALUE = 10;
    public static final int PROXIMITY_MIN_VALUE = 1;
    public static final int PROXIMITY_MAX_VALUE = 20;
    public static final int THRESHOLD_MIN_VALUE = 1;

    private int onAttributeID;
    private GraphElementType elementType;
    private int phraseLength;
    private int proximity;
    private int threshold;
    private boolean filterAllWords;

    public PhrasiphyContentParameters(final int phraseLength, final int proximity, final int threshold) {
        this.onAttributeID = -1;
        this.phraseLength = phraseLength;
        this.proximity = proximity;
        this.threshold = threshold;
    }

    public static PhrasiphyContentParameters getDefaultParameters() {
        return new PhrasiphyContentParameters(1, 1, 5);
    }

    @Override
    public String toString() {
        return String.format("PhrasiphyContentParameters[onAttributeID:%d, phraseLength:%d, proximity:%d, threshold:%d]", onAttributeID, phraseLength, proximity, threshold);
    }

    public int getOnAttributeID() {
        return onAttributeID;
    }

    public GraphElementType getElementType() {
        return elementType;
    }

    public int getPhraseLength() {
        return phraseLength;
    }

    public int getProximity() {
        return proximity;
    }

    public int getThreshold() {
        return threshold;
    }

    public boolean hasFilterAllWords() {
        return filterAllWords;
    }

    public void setOnAttributeID(final int value) {
        onAttributeID = value;
    }

    public void setPhraseLength(final int value) {
        phraseLength = value;
    }

    public void setElementType(final String value) {
        elementType = GraphElementType.getValue(value);
    }

    public void setProximity(final int value) {
        proximity = value;
    }

    public void setThreshold(final int value) {
        threshold = value;
    }

    public void setBackgroundFilter(final String value) {
        filterAllWords = value.equals(BACKGROUND_FILTER_DEFAULT);
    }
}
