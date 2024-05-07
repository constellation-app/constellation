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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.importing;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.ContentConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexTypeUtilities;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexTypeUtilities.ExtractedVertexType;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaPluginRegistry;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
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
import au.gov.asd.tac.constellation.plugins.reporting.PluginReportUtilities;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleQueryPlugin;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginCoreType;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
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
@Messages("ExtractWordsFromTextPlugin=Extract Words from Text")
@PluginInfo(pluginType = PluginType.IMPORT, tags = {PluginTags.IMPORT})
public class ExtractWordsFromTextPlugin extends SimpleQueryPlugin implements DataAccessPlugin {

    public static final String ATTRIBUTE_PARAMETER_ID = PluginParameter.buildId(ExtractWordsFromTextPlugin.class, "attribute");
    public static final String WORDS_PARAMETER_ID = PluginParameter.buildId(ExtractWordsFromTextPlugin.class, "words");
    public static final String MIN_WORD_LENGTH_PARAMETER_ID = PluginParameter.buildId(ExtractWordsFromTextPlugin.class, "min_word_length");
    public static final String REMOVE_SPECIAL_CHARS_PARAMETER_ID = PluginParameter.buildId(ExtractWordsFromTextPlugin.class, "ignore_special_chars");
    public static final String USE_REGEX_PARAMETER_ID = PluginParameter.buildId(ExtractWordsFromTextPlugin.class, "use_regex");
    public static final String WHOLE_WORDS_ONLY_PARAMETER_ID = PluginParameter.buildId(ExtractWordsFromTextPlugin.class, "whole_words_only");
    public static final String LOWER_CASE_PARAMETER_ID = PluginParameter.buildId(ExtractWordsFromTextPlugin.class, "lower_case");
    public static final String SCHEMA_TYPES_PARAMETER_ID = PluginParameter.buildId(ExtractWordsFromTextPlugin.class, "schema_types");
    public static final String IN_OR_OUT_PARAMETER_ID = PluginParameter.buildId(ExtractWordsFromTextPlugin.class, "in_or_out");
    public static final String SELECTED_ONLY_PARAMETER_ID = PluginParameter.buildId(ExtractWordsFromTextPlugin.class, "selected_only");
    public static final String REGEX_ONLY_PARAMETER_ID = PluginParameter.buildId(ExtractWordsFromTextPlugin.class, "regex_only");

    private static final String OUTGOING = "outgoing";
    private static final String INCOMING = "incoming";
    
    private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile("\\W", Pattern.UNICODE_CHARACTER_CLASS);

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
        attributeType.setRequired(true);
        params.addParameter(attributeType);

        final PluginParameter<StringParameterValue> text = StringParameterType.build(WORDS_PARAMETER_ID);
        StringParameterType.setLines(text, 15);
        text.setName("Words to Extract");
        text.setDescription("Whitelist of words to extract from content (new line delimited, extract all words if empty)");
        text.setSpellCheckEnabled(true);
        params.addParameter(text);

        final PluginParameter<BooleanParameterValue> useRegex = BooleanParameterType.build(USE_REGEX_PARAMETER_ID);
        useRegex.setName("Use Regular Expressions");
        useRegex.setDescription("Words to Extract will be treated as regex patterns");
        useRegex.setEnabled(false);
        params.addParameter(useRegex);

        final PluginParameter<BooleanParameterValue> wholeWordsOnly = BooleanParameterType.build(WHOLE_WORDS_ONLY_PARAMETER_ID);
        wholeWordsOnly.setName("Whole Words Only");
        wholeWordsOnly.setDescription("Words to Extract will be treated as whole words only");
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

        final PluginParameter<BooleanParameterValue> caseInsensitive = BooleanParameterType.build(LOWER_CASE_PARAMETER_ID);
        caseInsensitive.setName("Lower Case");
        caseInsensitive.setDescription("Results are lower-cased");
        caseInsensitive.setBooleanValue(true);
        params.addParameter(caseInsensitive);

        final PluginParameter<BooleanParameterValue> types = BooleanParameterType.build(SCHEMA_TYPES_PARAMETER_ID);
        types.setName("Extract Schema Types");
        types.setDescription("Extract schema types");
        types.setBooleanValue(true);
        params.addParameter(types);

        final PluginParameter<SingleChoiceParameterValue> inOrOutParam = SingleChoiceParameterType.build(IN_OR_OUT_PARAMETER_ID);
        inOrOutParam.setName("Transactions");
        inOrOutParam.setDescription("Link nodes to outgoing or incoming words: 'outgoing' or 'incoming'");
        inOrOutParam.setRequired(true);
        SingleChoiceParameterType.setOptions(inOrOutParam, List.of(OUTGOING, INCOMING));
        inOrOutParam.setStringValue(OUTGOING);
        params.addParameter(inOrOutParam);

        final PluginParameter<BooleanParameterValue> selected = BooleanParameterType.build(SELECTED_ONLY_PARAMETER_ID);
        selected.setName("Selected Transactions Only");
        selected.setDescription("Only extract words from selected transactions only");
        params.addParameter(selected);

        final PluginParameter<BooleanParameterValue> regexOnlyParam = BooleanParameterType.build(REGEX_ONLY_PARAMETER_ID);
        regexOnlyParam.setName("Regular Expression Only");
        regexOnlyParam.setDescription("The regexes control everything");
        params.addParameter(regexOnlyParam);

        params.addController(WORDS_PARAMETER_ID, (master, parameters, change) -> {
            if (change == ParameterChange.VALUE) {
                final String words = master.getStringValue();
                final boolean noWords = StringUtils.isBlank(words);
                parameters.get(USE_REGEX_PARAMETER_ID).setEnabled(!noWords);
                parameters.get(WHOLE_WORDS_ONLY_PARAMETER_ID).setEnabled(!noWords);
                parameters.get(MIN_WORD_LENGTH_PARAMETER_ID).setEnabled(noWords);
                parameters.get(REMOVE_SPECIAL_CHARS_PARAMETER_ID).setEnabled(noWords);
            }
        });

        // How well does this interact with the controller above?
        params.addController(REGEX_ONLY_PARAMETER_ID, (master, parameters, change) -> {
            if (change == ParameterChange.VALUE) {
                final boolean regexOnly = master.getBooleanValue();
                parameters.get(USE_REGEX_PARAMETER_ID).setEnabled(!regexOnly);
                parameters.get(WHOLE_WORDS_ONLY_PARAMETER_ID).setEnabled(!regexOnly);
                parameters.get(MIN_WORD_LENGTH_PARAMETER_ID).setEnabled(!regexOnly);
                parameters.get(REMOVE_SPECIAL_CHARS_PARAMETER_ID).setEnabled(!regexOnly);
                parameters.get(SCHEMA_TYPES_PARAMETER_ID).setEnabled(!regexOnly);

                if (!regexOnly) {
                    // If the checkbox is being unchecked, trigger a WORDS_PARAMETER_ID
                    // change to set the GUI state.
                    parameters.get(WORDS_PARAMETER_ID).fireChangeEvent(ParameterChange.VALUE);
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

            @SuppressWarnings("unchecked") //ATTRIBUTE_PARAMETER will always be of type SingleChoiceParameter
            final PluginParameter<SingleChoiceParameterValue> contentAttribute = (PluginParameter<SingleChoiceParameterValue>) parameters.getParameters().get(ATTRIBUTE_PARAMETER_ID);

            SingleChoiceParameterType.setOptions(contentAttribute, attributes);
            contentAttribute.suppressEvent(true, new ArrayList<>());
            if (!attributes.isEmpty() && contentAttribute.getSingleChoice() == null) {
                final String contentAttributeName = ContentConcept.TransactionAttribute.CONTENT.getName();
                // set the attribute to the Content attribute if it exists and the first option otherwise
                SingleChoiceParameterType.setChoice(contentAttribute, attributes.contains(contentAttributeName) ? contentAttributeName : attributes.get(0));
            }
            contentAttribute.suppressEvent(false, new ArrayList<>());
            contentAttribute.setObjectValue(parameters.getObjectValue(ATTRIBUTE_PARAMETER_ID));
        }
    }

    @Override
    public void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        
        // Retrieve PluginParameter values 
        final String contentAttribute = parameters.getParameters().get(ATTRIBUTE_PARAMETER_ID).getStringValue();
        final boolean wholeWordOnly = parameters.getParameters().get(WHOLE_WORDS_ONLY_PARAMETER_ID).getBooleanValue();
        final int wordLength = parameters.getParameters().get(MIN_WORD_LENGTH_PARAMETER_ID).getIntegerValue();
        final boolean removeSpecialChars = parameters.getParameters().get(REMOVE_SPECIAL_CHARS_PARAMETER_ID).getBooleanValue();
        final boolean toLowerCase = parameters.getParameters().get(LOWER_CASE_PARAMETER_ID).getBooleanValue();
        final boolean types = parameters.getParameters().get(SCHEMA_TYPES_PARAMETER_ID).getBooleanValue();
        final String inOrOut = parameters.getParameters().get(IN_OR_OUT_PARAMETER_ID).getStringValue();
        final boolean selectedOnly = parameters.getParameters().get(SELECTED_ONLY_PARAMETER_ID).getBooleanValue();
        final boolean regexOnly = parameters.getParameters().get(REGEX_ONLY_PARAMETER_ID).getBooleanValue();
        final boolean useRegex = parameters.getParameters().get(USE_REGEX_PARAMETER_ID).getBooleanValue();
        final String words = parameters.getParameters().get(WORDS_PARAMETER_ID).getStringValue() == null
                ? null : parameters.getParameters().get(WORDS_PARAMETER_ID).getStringValue().trim();
        
        // Retrieve Attribute values
        final int vertexIdentifierAttributeId = VisualConcept.VertexAttribute.IDENTIFIER.ensure(wg);
        final int vertexTypeAttributeId = AnalyticConcept.VertexAttribute.TYPE.ensure(wg);
        final int transactionTypeAttributeId = AnalyticConcept.TransactionAttribute.TYPE.ensure(wg);
        final int transactionDatetimeAttributeId = TemporalConcept.TransactionAttribute.DATETIME.ensure(wg);
        final int transactionContentAttributeId = wg.getAttribute(GraphElementType.TRANSACTION, contentAttribute);
        final int transactionSelectedAttributeId = VisualConcept.TransactionAttribute.SELECTED.ensure(wg);
        
        // Throw Errors
        if (!OUTGOING.equals(inOrOut) && !INCOMING.equals(inOrOut)) {
            final String msg = String.format("Parameter %s must be '%s' or '%s'", REGEX_ONLY_PARAMETER_ID, OUTGOING, INCOMING);
            throw new PluginException(PluginNotificationLevel.ERROR, msg);
        }
        if (transactionContentAttributeId == Graph.NOT_FOUND) {
            NotifyDisplayer.display(String.format("The specified attribute %s does not exist.", contentAttribute), NotifyDescriptor.WARNING_MESSAGE);
            return;
        }
        
        final int transactionCount = wg.getTransactionCount();
        final boolean outgoing = OUTGOING.equals(inOrOut);
        final Set<String> newNodes = new HashSet<>();
        
        // Local process-tracking varables.
        int currentProcessStep = 0;
        final int totalProcessSteps = transactionCount; 
        int newTransactionCount = 0;
        int newNodeCount = 0;
        interaction.setProgress(currentProcessStep, totalProcessSteps, "Extracting...", true);
        
        if (regexOnly) {
            /*
             This choice ignores several other parameters, so is a bit simpler
             even if there code commonalities, but combining the if/else
             code would make things even more complex.

             The input words are treated as trusted regular expressions,
             so the caller has to know what they're doing.
             This is power-use mode.

             Each line of the input words is a regex.
             Use them as-is for the power users.
            */
            final List<Pattern> patterns = new ArrayList<>();
            if (StringUtils.isNotBlank(words)) {
                for (String word : words.split(SeparatorConstants.NEWLINE)) {
                    word = word.strip();
                    if (!word.isEmpty()) {
                        final Pattern pattern = Pattern.compile(word);
                        patterns.add(pattern);
                    }
                }
            }

            if (!patterns.isEmpty()) {
                // Use a set to hold the words.
                // If a word is found multiple times, there's no point adding multiple nodes.
                final Set<String> matched = new HashSet<>();

                // Iterate over all the transactions in the graph.
                for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                    
                    // Update progress for each transaction
                    interaction.setProgress(++currentProcessStep, totalProcessSteps, true);
                    
                    final int transactionId = wg.getTransaction(transactionPosition);

                    final boolean selectedTx = wg.getBooleanValue(transactionSelectedAttributeId, transactionId);
                    if (selectedOnly && !selectedTx) {
                        continue;
                    }

                    final String content = wg.getStringValue(transactionContentAttributeId, transactionId);

                    // Does the transaction have content?
                    if (StringUtils.isBlank(content)) {
                        continue;
                    }

                    // Ignore other "referenced" transactions because that's not useful
                    if (wg.getObjectValue(transactionTypeAttributeId, transactionId) != null && wg.getObjectValue(transactionTypeAttributeId, transactionId).equals(AnalyticConcept.TransactionType.REFERENCED)) {
                        continue;
                    }

                    patterns.stream().map(pattern -> pattern.matcher(content))
                            .forEach(matcher -> {
                                while (matcher.find()) {
                                    if (matcher.groupCount() == 0) {
                                        // The regex doesn't have an explicit capture group, so capture the lot.
                                        final String g = matcher.group();
                                        matched.add(toLowerCase ? g.toLowerCase() : g);
                                    } else {
                                        // The regex has one or more explicit capture groups: capture those.
                                        for (int i = 1; i <= matcher.groupCount(); i++) {
                                            final String g = matcher.group(i);
                                            matched.add(toLowerCase ? g.toLowerCase() : g);
                                        }
                                    }
                                }
                            });

                    // Add matched words to the graph.
                    if (!matched.isEmpty()) {
                        // Retrieving information needed to create new transactions
                        final int sourceVertexId = wg.getTransactionSourceVertex(transactionId);
                        final int destinationVertexId = wg.getTransactionDestinationVertex(transactionId);
                        final ZonedDateTime datetime = wg.getObjectValue(transactionDatetimeAttributeId, transactionId);

                        for (final String word : matched){
                            final int newVertexId = wg.addVertex();
                            wg.setStringValue(vertexIdentifierAttributeId, newVertexId, word);
                            wg.setObjectValue(vertexTypeAttributeId, newVertexId, AnalyticConcept.VertexType.WORD);

                            final int newTransactionId = outgoing
                                    ? wg.addTransaction(sourceVertexId, newVertexId, true)
                                    : wg.addTransaction(newVertexId, destinationVertexId, true);
                            wg.setObjectValue(transactionDatetimeAttributeId, newTransactionId, datetime);
                            wg.setObjectValue(transactionTypeAttributeId, newTransactionId, AnalyticConcept.TransactionType.REFERENCED);
                            wg.setStringValue(transactionContentAttributeId, newTransactionId, content);
                            newTransactionCount++;
                        }
                    }
                }
                newNodeCount = matched.size();
            }
            // End of regexOnly.
        } else { // The original logic.
            final List<Pattern> patterns = patternsFromWords(words, useRegex, wholeWordOnly);

            // Iterating over all the transactions in the graph
            final List<String> foundWords = new ArrayList<>();
            for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {

                // Update progress for each transaction
                interaction.setProgress(++currentProcessStep, totalProcessSteps, true);
                
                foundWords.clear();
                final int transactionId = wg.getTransaction(transactionPosition);

                final boolean selectedTx = wg.getBooleanValue(transactionSelectedAttributeId, transactionId);
                if (selectedOnly && !selectedTx) {
                    continue;
                }

                final String content = wg.getStringValue(transactionContentAttributeId, transactionId);

                // Does the transaction have content?
                if (StringUtils.isBlank(content)) {
                    continue;
                }

                // Ignore other "referenced" transactions because that's not useful
                if (wg.getObjectValue(transactionTypeAttributeId, transactionId) != null && wg.getObjectValue(transactionTypeAttributeId, transactionId).equals(AnalyticConcept.TransactionType.REFERENCED)) {
                    continue;
                }

                // Retrieving information needed to create new transactions
                final int sourceVertexId = wg.getTransactionSourceVertex(transactionId);
                final int destinationVertexId = wg.getTransactionDestinationVertex(transactionId);
                final ZonedDateTime datetime = wg.getObjectValue(transactionDatetimeAttributeId, transactionId);

                final Set<String> typesExtracted = new HashSet<>();

                /*
                 Extracting Schema Types
                 */
                if (types) {
                    final List<ExtractedVertexType> extractedTypes = SchemaVertexTypeUtilities.extractVertexTypes(content);

                    final Map<String, SchemaVertexType> identifiers = new HashMap<>();
                    extractedTypes.forEach(extractedType -> identifiers.put(extractedType.getIdentifier(), extractedType.getType()));

                    for (final String identifier : identifiers.keySet()) {
                        final int newVertexId = wg.addVertex();
                        wg.setStringValue(vertexIdentifierAttributeId, newVertexId, identifier);
                        wg.setObjectValue(vertexTypeAttributeId, newVertexId, identifiers.get(identifier));

                        final int newTransactionId = outgoing
                                ? wg.addTransaction(sourceVertexId, newVertexId, true)
                                : wg.addTransaction(newVertexId, destinationVertexId, true);
                        wg.setObjectValue(transactionDatetimeAttributeId, newTransactionId, datetime);
                        wg.setObjectValue(transactionTypeAttributeId, newTransactionId, AnalyticConcept.TransactionType.REFERENCED);
                        wg.setStringValue(transactionContentAttributeId, newTransactionId, content);

                        typesExtracted.add(identifier.toLowerCase());
                    }
                }

                if (StringUtils.isBlank(words)) {
                    // Extracting all words of the specified length if no word list has been provided
                    for (String word : content.split(" ")) {
                        if (toLowerCase) {
                            word = word.toLowerCase();
                        }
                        if (removeSpecialChars) {
                            word = SPECIAL_CHARS_PATTERN.matcher(word).replaceAll("");
                        }
                        if (word.length() < wordLength) {
                            continue;
                        }
                        foundWords.add(word);
                    }
                } else {
                    patterns.stream().map(pattern -> pattern.matcher(content))
                            .forEach(matcher -> {
                                while (matcher.find()) {
                                    final String g = matcher.group();
                                    foundWords.add(toLowerCase ? g.toLowerCase() : g);
                                }
                            });
                }

                /*
                 Add words to graph
                 */
                for (final String word : foundWords) {
                    if (types && typesExtracted.contains(word.toLowerCase())) {
                        continue;
                    }
                    newNodes.add(word);
                    final int newVertexId = wg.addVertex();
                    wg.setStringValue(vertexIdentifierAttributeId, newVertexId, word);
                    wg.setObjectValue(vertexTypeAttributeId, newVertexId, AnalyticConcept.VertexType.WORD);

                    final int newTransactionId = outgoing
                            ? wg.addTransaction(sourceVertexId, newVertexId, true)
                            : wg.addTransaction(newVertexId, destinationVertexId, true);
                    wg.setObjectValue(transactionDatetimeAttributeId, newTransactionId, datetime);
                    wg.setObjectValue(transactionTypeAttributeId, newTransactionId, AnalyticConcept.TransactionType.REFERENCED);
                    wg.setStringValue(transactionContentAttributeId, newTransactionId, content);
                    newTransactionCount++;
                }
            }
            newNodeCount = newNodes.size();
        }
 
        // Set process to complete
        interaction.setProgress(currentProcessStep, 
                0, 
                String.format("Created %s & %s.", 
                        PluginReportUtilities.getNodeCountString(newNodeCount),
                        PluginReportUtilities.getTransactionCountString(newTransactionCount)
                ), 
                true
        );
        
        PluginExecutor.startWith(VisualSchemaPluginRegistry.COMPLETE_SCHEMA)
                .followedBy(InteractiveGraphPluginRegistry.RESET_VIEW)
                .executeNow(wg);
    }
        
    /**
     * The input words are transformed into pre-determined regular expressions.
     *
     * @param words
     * @param useRegex
     * @param wholeWordOnly
     * @param ignoreCase
     * @return
     */
    private static List<Pattern> patternsFromWords(final String words, final boolean useRegex, final boolean wholeWordOnly) {
        final List<Pattern> patterns = new ArrayList<>();
        if (StringUtils.isNotBlank(words)) {
            for (String word : words.split(SeparatorConstants.NEWLINE)) {
                if (StringUtils.isBlank(word)) {
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

                final Pattern pattern = Pattern.compile(word, Pattern.CASE_INSENSITIVE);
                patterns.add(pattern);
            }
        }

        return patterns;
    }

    /*
     This cleans up words so that special regex characters are escaped
     */
    private static String cleanRegex(String regex) {

        return regex.replace("\\.", SeparatorConstants.PERIOD)
                .replace("\\^", "^")
                .replace("\\$", "$")
                .replace("\\[", "[")
                .replace("\\]", "]")
                .replace("\\{", "{")
                .replace("\\}", "}")
                .replace("\\(", ")")
                .replace("\\(", ")")
                .replace("\\|", SeparatorConstants.PIPE)
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
                .replace(SeparatorConstants.PIPE, "\\|")
                .replace("?", "\\?")
                .replace("+", "\\+")
                .replace("*", "\\*");
    }
}
