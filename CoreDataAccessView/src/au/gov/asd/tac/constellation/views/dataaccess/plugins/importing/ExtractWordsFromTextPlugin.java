/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.importing;

import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import au.gov.asd.tac.constellation.preferences.utilities.PreferenceUtilites;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexTypeUtilities;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexTypeUtilities.ExtractedVertexType;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.SimpleQueryPlugin;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.ContentConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaPluginRegistry;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessPluginCoreType;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * Extract words from content and add them to a graph.
 *
 * @author canis_majorus
 */
@ServiceProviders({
    @ServiceProvider(service = DataAccessPlugin.class),
    @ServiceProvider(service = Plugin.class)})
@PluginInfo(pluginType = PluginType.IMPORT, tags = {"IMPORT"})
@Messages("ExtractWordsFromTextPlugin=Extract Words from Text")
public class ExtractWordsFromTextPlugin extends SimpleQueryPlugin implements DataAccessPlugin {

    public static final String ATTRIBUTE_PARAMETER_ID = PluginParameter.buildId(ExtractWordsFromTextPlugin.class, "attribute");
    public static final String WORDS_PARAMETER_ID = PluginParameter.buildId(ExtractWordsFromTextPlugin.class, "words");
    public static final String MIN_WORD_LENGTH_PARAMETER_ID = PluginParameter.buildId(ExtractWordsFromTextPlugin.class, "min_word_length");
    public static final String REMOVE_SPECIAL_CHARS_PARAMETER_ID = PluginParameter.buildId(ExtractWordsFromTextPlugin.class, "ignore_special_chars");
    public static final String USE_REGEX_PARAMETER_ID = PluginParameter.buildId(ExtractWordsFromTextPlugin.class, "use_regex");
    public static final String WHOLE_WORDS_ONLY_PARAMETER_ID = PluginParameter.buildId(ExtractWordsFromTextPlugin.class, "whole_words_only");
    public static final String CASE_INSENSITIVE_PARAMETER_ID = PluginParameter.buildId(ExtractWordsFromTextPlugin.class, "case_insensitive");
    public static final String SCHEMA_TYPES_PARAMETER_ID = PluginParameter.buildId(ExtractWordsFromTextPlugin.class, "schema_types");
    public static final String OUTGOING_PARAMETER_ID = PluginParameter.buildId(ExtractWordsFromTextPlugin.class, "outgoing");
    public static final String INCOMING_PARAMETER_ID = PluginParameter.buildId(ExtractWordsFromTextPlugin.class, "incoming");
    public static final String SELECTED_ONLY_PARAMETER_ID = PluginParameter.buildId(ExtractWordsFromTextPlugin.class, "selected_only");

    @Override
    public String getType() {
        return DataAccessPluginCoreType.IMPORT;
    }

    @Override
    public int getPosition() {
        return 1001;
    }

    @Override
    public String getDescription() {
        return "Extract words from text and add them to the graph";
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters params = new PluginParameters();

        final PluginParameter<SingleChoiceParameterValue> attributeType = SingleChoiceParameterType.build(ATTRIBUTE_PARAMETER_ID);
        attributeType.setName("Content Attribute");
        attributeType.setDescription("Set the attribute from which to extract words");
        params.addParameter(attributeType);

        final PluginParameter<StringParameterValue> text = StringParameterType.build(WORDS_PARAMETER_ID);
        StringParameterType.setLines(text, 15);
        text.setName("Words to Extract");
        text.setDescription("Whitelist of words to extract from content (new line delimited, extract all words if empty)");
        text.setStringValue(null);
        params.addParameter(text);

        final PluginParameter<BooleanParameterValue> useRegex = BooleanParameterType.build(USE_REGEX_PARAMETER_ID);
        useRegex.setName("Use Regular Expressions");
        useRegex.setDescription("Words to Extract will be treated as regex patterns");
        useRegex.setBooleanValue(false);
        useRegex.setEnabled(false);
        params.addParameter(useRegex);

        final PluginParameter<BooleanParameterValue> wholeWordsOnly = BooleanParameterType.build(WHOLE_WORDS_ONLY_PARAMETER_ID);
        wholeWordsOnly.setName("Whole Words Only");
        wholeWordsOnly.setDescription("Words to Extract will be treated as whole words only");
        wholeWordsOnly.setBooleanValue(false);
        wholeWordsOnly.setEnabled(false);
        params.addParameter(wholeWordsOnly);

        final PluginParameter<IntegerParameterValue> minWordLength = IntegerParameterType.build(MIN_WORD_LENGTH_PARAMETER_ID);
        minWordLength.setName("Minimum Word Length");
        minWordLength.setDescription("Only extract words of length equal to or greater than this");
        minWordLength.setIntegerValue(3);
        IntegerParameterType.setMinimum(minWordLength, 1);
        params.addParameter(minWordLength);

        final PluginParameter<BooleanParameterValue> removeSpecialChars = BooleanParameterType.build(REMOVE_SPECIAL_CHARS_PARAMETER_ID);
        removeSpecialChars.setName("Remove Special Characters");
        removeSpecialChars.setDescription("Removes special characters from words before extraction");
        removeSpecialChars.setBooleanValue(true);
        params.addParameter(removeSpecialChars);

        final PluginParameter<BooleanParameterValue> caseInsensitive = BooleanParameterType.build(CASE_INSENSITIVE_PARAMETER_ID);
        caseInsensitive.setName("Case Insensitive");
        caseInsensitive.setDescription("Treats all content as lower case");
        caseInsensitive.setBooleanValue(true);
        params.addParameter(caseInsensitive);

        final PluginParameter<BooleanParameterValue> types = BooleanParameterType.build(SCHEMA_TYPES_PARAMETER_ID);
        types.setName("Extract Schema Types");
        types.setDescription("Extract schema types");
        types.setBooleanValue(true);
        params.addParameter(types);

        final PluginParameter<BooleanParameterValue> outgoing = BooleanParameterType.build(OUTGOING_PARAMETER_ID);
        outgoing.setName("Outgoing Transactions");
        outgoing.setDescription("Links nodes to outgoing words");
        outgoing.setBooleanValue(true);
        params.addParameter(outgoing);

        final PluginParameter<BooleanParameterValue> incoming = BooleanParameterType.build(INCOMING_PARAMETER_ID);
        incoming.setName("Incoming Transactions");
        incoming.setDescription("Links nodes to incoming words");
        incoming.setBooleanValue(false);
        params.addParameter(incoming);

        final PluginParameter<BooleanParameterValue> selected = BooleanParameterType.build(SELECTED_ONLY_PARAMETER_ID);
        selected.setName("Selected Transactions Only");
        selected.setDescription("Only extract words from selected transactions only");
        selected.setBooleanValue(false);
        params.addParameter(selected);

        params.addController(WORDS_PARAMETER_ID, (master, parameters, change) -> {
            if (change == ParameterChange.VALUE) {
                final String words = master.getStringValue();
                if (words == null || words.isEmpty()) {
                    parameters.get(USE_REGEX_PARAMETER_ID).setEnabled(false);
                    parameters.get(WHOLE_WORDS_ONLY_PARAMETER_ID).setEnabled(false);
                    parameters.get(MIN_WORD_LENGTH_PARAMETER_ID).setEnabled(true);
                    parameters.get(REMOVE_SPECIAL_CHARS_PARAMETER_ID).setEnabled(true);
                } else {
                    parameters.get(USE_REGEX_PARAMETER_ID).setEnabled(true);
                    parameters.get(WHOLE_WORDS_ONLY_PARAMETER_ID).setEnabled(true);
                    parameters.get(MIN_WORD_LENGTH_PARAMETER_ID).setEnabled(false);
                    parameters.get(REMOVE_SPECIAL_CHARS_PARAMETER_ID).setEnabled(false);
                }
            }
        });

        return params;
    }

    /**
     * Updating parameters to retrieve the transaction attributes
     *
     * @param graph
     * @param parameters
     */
    @Override
    public void updateParameters(final Graph graph, final PluginParameters parameters) {
        final List<String> attributes = new ArrayList<>();
        if (graph != null) {
            final ReadableGraph readableGraph = graph.getReadableGraph();
            try {
                final int attributeCount = readableGraph.getAttributeCount(GraphElementType.TRANSACTION);
                for (int attributePosition = 0; attributePosition < attributeCount; attributePosition++) {
                    final int attributeId = readableGraph.getAttribute(GraphElementType.TRANSACTION, attributePosition);
                    final String attributeType = readableGraph.getAttributeType(attributeId);
                    if (attributeType.equals(StringAttributeDescription.ATTRIBUTE_NAME)) {
                        attributes.add(readableGraph.getAttributeName(attributeId));
                    }
                }
            } finally {
                readableGraph.release();
            }
        }

        attributes.sort(String::compareTo);

        if (parameters != null && parameters.getParameters() != null) {

            final PluginParameter contentAttribute = parameters.getParameters().get(ATTRIBUTE_PARAMETER_ID);
            contentAttribute.suppressEvent(true, new ArrayList());
            SingleChoiceParameterType.setOptions(contentAttribute, attributes);
            if (attributes.contains(ContentConcept.TransactionAttribute.CONTENT.getName())) {
                SingleChoiceParameterType.setChoice(contentAttribute, ContentConcept.TransactionAttribute.CONTENT.getName());
            }
            contentAttribute.suppressEvent(false, new ArrayList());
        }
    }

    @Override
    public void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        interaction.setProgress(0, 0, "Extracting...", true);

        /*
         Retrieving attributes
         */
        final Map<String, PluginParameter<?>> extractEntityParameters = parameters.getParameters();
        final String contentAttribute = extractEntityParameters.get(ATTRIBUTE_PARAMETER_ID).getStringValue();
        final String words = extractEntityParameters.get(WORDS_PARAMETER_ID).getStringValue() == null
                ? null : extractEntityParameters.get(WORDS_PARAMETER_ID).getStringValue().trim();
        final boolean useRegex = extractEntityParameters.get(USE_REGEX_PARAMETER_ID).getBooleanValue();
        final boolean wholeWordOnly = extractEntityParameters.get(WHOLE_WORDS_ONLY_PARAMETER_ID).getBooleanValue();
        final int wordLength = parameters.getParameters().get(MIN_WORD_LENGTH_PARAMETER_ID).getIntegerValue();
        final boolean removeSpecialChars = extractEntityParameters.get(REMOVE_SPECIAL_CHARS_PARAMETER_ID).getBooleanValue();
        final boolean ignoreCase = extractEntityParameters.get(CASE_INSENSITIVE_PARAMETER_ID).getBooleanValue();
        final boolean types = extractEntityParameters.get(SCHEMA_TYPES_PARAMETER_ID).getBooleanValue();
        final boolean outgoing = extractEntityParameters.get(OUTGOING_PARAMETER_ID).getBooleanValue();
        final boolean incoming = extractEntityParameters.get(INCOMING_PARAMETER_ID).getBooleanValue();
        final boolean selectedOnly = extractEntityParameters.get(SELECTED_ONLY_PARAMETER_ID).getBooleanValue();

        assert outgoing || incoming : "You must select either outgoing or incoming transactions, or both";

        /*
         Retrieving attribute IDs
         */
        final int vertexIdentifierAttributeId = VisualConcept.VertexAttribute.IDENTIFIER.ensure(wg);
        final int vertexTypeAttributeId = AnalyticConcept.VertexAttribute.TYPE.ensure(wg);
        final int transactionTypeAttributeId = AnalyticConcept.TransactionAttribute.TYPE.ensure(wg);
        final int transactionDatetimeAttributeId = TemporalConcept.TransactionAttribute.DATETIME.ensure(wg);
        final int transactionContentAttributeId = wg.getAttribute(GraphElementType.TRANSACTION, contentAttribute);
        final int transactionSelectedAttributeId = VisualConcept.TransactionAttribute.SELECTED.ensure(wg);

        /*
         Verify that the content transaction attribute exists
         */
        if (transactionContentAttributeId == Graph.NOT_FOUND) {
            final NotifyDescriptor nd = new NotifyDescriptor.Message(String.format("The specified attribute %s does not exist.", contentAttribute), NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return;
        }

        final List<String> foundWords = new ArrayList<>();
        final int transactionCount = wg.getTransactionCount();

        /*
         Iterating over all the transactions in the graph
         */
        for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
            foundWords.clear();

            final int transactionId = wg.getTransaction(transactionPosition);

            final boolean selectedTx = wg.getBooleanValue(transactionSelectedAttributeId, transactionId);
            if (selectedOnly && !selectedTx) {
                continue;
            }

            String content = wg.getStringValue(transactionContentAttributeId, transactionId);

            /*
             Does the transaction have content?
             */
            if (content == null || content.isEmpty()) {
                continue;
            }

            /*
             Ignore other "referenced" transactions because that's not useful
             */
            if (wg.getObjectValue(transactionTypeAttributeId, transactionId) != null && wg.getObjectValue(transactionTypeAttributeId, transactionId).equals(AnalyticConcept.TransactionType.REFERENCED)) {
                continue;
            }

            /*
             Retrieving information needed to create new transactions
             */
            final int sourceVertexId = wg.getTransactionSourceVertex(transactionId);
            final int destinationVertexId = wg.getTransactionDestinationVertex(transactionId);
            final ZonedDateTime datetime = wg.getObjectValue(transactionDatetimeAttributeId, transactionId);

            final HashSet<String> typesExtracted = new HashSet<>();

            /*
             Extracting Schema Types
             */
            if (types) {
                final List<ExtractedVertexType> extractedTypes = SchemaVertexTypeUtilities.extractVertexTypes(content);

                final Map<String, SchemaVertexType> identifiers = new HashMap<>();
                extractedTypes.forEach(extractedType -> {
                    identifiers.put(extractedType.getIdentifier(), extractedType.getType());
                });

                for (String identifier : identifiers.keySet()) {
                    final int newVertexId = wg.addVertex();
                    wg.setStringValue(vertexIdentifierAttributeId, newVertexId, identifier);
                    wg.setObjectValue(vertexTypeAttributeId, newVertexId, identifiers.get(identifier));

                    if (outgoing) {
                        final int newTransactionId = wg.addTransaction(sourceVertexId, newVertexId, true);
                        wg.setObjectValue(transactionTypeAttributeId, newTransactionId, AnalyticConcept.TransactionType.REFERENCED);
                        wg.setObjectValue(transactionDatetimeAttributeId, newTransactionId, datetime);
                        wg.setStringValue(transactionContentAttributeId, newTransactionId, content);
                        typesExtracted.add(identifier.toLowerCase());
                    }
                    if (incoming) {
                        final int newTransactionId = wg.addTransaction(newVertexId, destinationVertexId, true);
                        wg.setObjectValue(transactionTypeAttributeId, newTransactionId, AnalyticConcept.TransactionType.REFERENCED);
                        wg.setObjectValue(transactionDatetimeAttributeId, newTransactionId, datetime);
                        wg.setStringValue(transactionContentAttributeId, newTransactionId, content);
                        typesExtracted.add(identifier.toLowerCase());
                    }
                }
            }

            if (words == null || words.isEmpty()) {
                /*
                 Extracting all words of the specified length if no word list has been provided
                 */
                for (String word : content.split(" ")) {
                    if (ignoreCase) {
                        word = word.toLowerCase();
                    }
                    if (removeSpecialChars) {
                        word = word.replaceAll("\\W", "");
                    }
                    if (word.length() < wordLength) {
                        continue;
                    }
                    foundWords.add(word);
                }
            } else {
                /*
                 Checking each supplied word from Words to Extract
                 */
                for (String word : words.split("\n")) {
                    if (word == null || word.isEmpty()) {
                        continue;
                    }
                    /*
                     Build regex and search for matches
                     */
                    if (!useRegex) {
                        word = cleanRegex(word);
                    }
                    if (wholeWordOnly) {
                        word = "\\b(" + word + ")\\b";
                    } else {
                        word = "\\b([A-Za-z0-9]*" + word + "[A-Za-z0-9]*)\\b";
                    }
                    final Pattern pattern;
                    if (ignoreCase) {
                        word = word.toLowerCase();
                        content = content.toLowerCase();
                        pattern = Pattern.compile(word);
                    } else {
                        pattern = Pattern.compile(word, Pattern.CASE_INSENSITIVE);
                    }
                    final Matcher matcher = pattern.matcher(content);
                    while (matcher.find()) {

                        foundWords.add(matcher.group());
                    }
                }
            }

            /*
             Add words to graph
             */
            for (String word : foundWords) {
                if (types) {
                    if (typesExtracted.contains(word.toLowerCase())) {
                        continue;
                    }
                }
                final int newVertexId = wg.addVertex();
                wg.setStringValue(vertexIdentifierAttributeId, newVertexId, word);
                wg.setObjectValue(vertexTypeAttributeId, newVertexId, AnalyticConcept.VertexType.WORD);

                if (outgoing) {
                    final int newTransactionId = wg.addTransaction(sourceVertexId, newVertexId, true);
                    wg.setObjectValue(transactionDatetimeAttributeId, newTransactionId, datetime);
                    wg.setObjectValue(transactionTypeAttributeId, newTransactionId, AnalyticConcept.TransactionType.REFERENCED);
                    wg.setStringValue(transactionContentAttributeId, newTransactionId, content);
                }
                if (incoming) {
                    final int newTransactionId = wg.addTransaction(newVertexId, destinationVertexId, true);
                    wg.setObjectValue(transactionDatetimeAttributeId, newTransactionId, datetime);
                    wg.setObjectValue(transactionTypeAttributeId, newTransactionId, AnalyticConcept.TransactionType.REFERENCED);
                    wg.setStringValue(transactionContentAttributeId, newTransactionId, content);
                }
            }
        }

        if (!PreferenceUtilites.isGraphViewFrozen()) {
            // complete with schema, arrange in trees, and reset view
            PluginExecutor.startWith(VisualSchemaPluginRegistry.COMPLETE_SCHEMA)
                    .followedBy(ArrangementPluginRegistry.TREES)
                    .executeNow(wg);
        } else {
            PluginExecutor.startWith(VisualSchemaPluginRegistry.COMPLETE_SCHEMA)
                    .followedBy(InteractiveGraphPluginRegistry.RESET_VIEW)
                    .executeNow(wg);
        }

        interaction.setProgress(1, 0, "Completed successfully", true);
    }

    /*
     This cleans up words so that special regex characters are escaped
     */
    private String cleanRegex(String regex) {

        return regex.replace("\\.", SeparatorConstants.PERIOD)
                .replace("\\^", "^")
                .replace("\\$", "$")
                .replace("\\[", "[")
                .replace("\\]", "]")
                .replace("\\{", "{")
                .replace("\\}", "}")
                .replace("\\(", ")")
                .replace("\\(", ")")
                .replace("\\|", "|")
                .replace("\\?", "?")
                .replace("\\+", "+")
                .replace("\\*", "*")
                .replace("\\\\", "\\")
                .replace("\\", "\\\\")
                .replace(SeparatorConstants.PERIOD, "\\.")
                .replace("^", "\\^")
                .replace("$", "\\$")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("(", "\\)")
                .replace("(", "\\)")
                .replace("|", "\\|")
                .replace("?", "\\?")
                .replace("+", "\\+")
                .replace("*", "\\*");
    }
}
